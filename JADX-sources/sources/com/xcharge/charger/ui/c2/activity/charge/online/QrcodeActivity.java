package com.xcharge.charger.ui.c2.activity.charge.online;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.xcharge.charger.R;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.data.bean.ContentItem;
import com.xcharge.charger.data.bean.PortFeeRate;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.setting.UserDefineUISetting;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.ADVERT_POLICY;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.PARK_STATUS;
import com.xcharge.charger.data.bean.type.PHASE;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.ui.api.UIEventMessageProxy;
import com.xcharge.charger.ui.api.bean.UICtrlMessage;
import com.xcharge.charger.ui.api.bean.UIEventMessage;
import com.xcharge.charger.ui.c2.activity.BaseActivity;
import com.xcharge.charger.ui.c2.activity.advert.WakeupAdDialog;
import com.xcharge.charger.ui.c2.activity.data.Price;
import com.xcharge.charger.ui.c2.activity.data.Variate;
import com.xcharge.charger.ui.c2.activity.fault.OnlineParkBusyHintActivity;
import com.xcharge.charger.ui.c2.activity.utiles.Utils;
import com.xcharge.charger.ui.c2.activity.widget.LoadingDialog;
import java.util.ArrayList;
import java.util.HashMap;

/* loaded from: classes.dex */
public class QrcodeActivity extends BaseActivity {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$PHASE;
    private FeeRateContentObserver feeRateContentObserver;
    private HintContentObserver hintContentObserver;
    private LoadingDialog initLoadingDialog;
    private ImageView iv_phase;
    private ImageView iv_qrcode;
    private LinearLayout ll_phase;
    private QrcodeContentObserver qrcodeContentObserver;
    private TextView tv_device_code;
    private TextView tv_phase;
    private TextView tv_power_price;
    private TextView tv_service_price;
    private TextView tv_subhead;
    private TextView tv_title;
    private final int MSG_SCREEN_ON = 1;
    private final int MSG_SCREEN_OFF = 2;
    private ScreenStatusReceiver mScreenStatusReceiver = null;
    protected Handler mHandler = new Handler() { // from class: com.xcharge.charger.ui.c2.activity.charge.online.QrcodeActivity.1
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            Port port;
            switch (msg.what) {
                case 1:
                    ArrayList<ContentItem> wakeArrayList = RemoteSettingCacheProvider.getInstance().getAdvertContent(ADVERT_POLICY.wakeUpAdvsite);
                    if (wakeArrayList != null && Utils.listIsEmpty(wakeArrayList) && Utils.fileIsEmpty(Utils.platformAdPath(ADVERT_POLICY.wakeUpAdvsite.getPolicy())) && Utils.fileNameIsEqual(wakeArrayList)) {
                        Log.i("wakeUpAdvsite", RemoteSettingCacheProvider.getInstance().getRemoteSetting().getAdvertSetting().toJson());
                        if (QrcodeActivity.this.wakeupAdDialog == null) {
                            QrcodeActivity.this.wakeupAdDialog = WakeupAdDialog.createDialog(QrcodeActivity.context);
                            QrcodeActivity.this.wakeupAdDialog.setCancelable(false);
                        } else {
                            QrcodeActivity.this.wakeupAdDialog.initView();
                        }
                        if (QrcodeActivity.this.wakeupAdDialog != null && !QrcodeActivity.this.wakeupAdDialog.isShowing()) {
                            QrcodeActivity.this.wakeupAdDialog.show();
                            return;
                        }
                        return;
                    }
                    return;
                case 2:
                    if (Variate.getInstance().isRadar() && (port = HardwareStatusCacheProvider.getInstance().getPort("1")) != null) {
                        String parkStatus = port.getParkStatus().getParkStatus().getStatus();
                        if (PARK_STATUS.occupied.getStatus().equals(parkStatus) && HardwareStatusCacheProvider.getInstance().getNetworkStatus().isConnected()) {
                            QrcodeActivity.this.startActivity(new Intent(QrcodeActivity.context, OnlineParkBusyHintActivity.class));
                            QrcodeActivity.this.finish();
                            return;
                        }
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$PHASE() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$PHASE;
        if (iArr == null) {
            iArr = new int[PHASE.valuesCustom().length];
            try {
                iArr[PHASE.DC_PHASE.ordinal()] = 4;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[PHASE.SINGLE_PHASE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[PHASE.THREE_PHASE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[PHASE.UNKOWN_PHASE.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            $SWITCH_TABLE$com$xcharge$charger$data$bean$type$PHASE = iArr;
        }
        return iArr;
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    @SuppressLint({"NewApi"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        setScreenBrightness(MotionEventCompat.ACTION_MASK);
        this.qrcodeContentObserver = new QrcodeContentObserver(new Handler());
        getContentResolver().registerContentObserver(ChargeStatusCacheProvider.getInstance().getUriFor("ports/qrcode/1"), false, this.qrcodeContentObserver);
        this.feeRateContentObserver = new FeeRateContentObserver(new Handler());
        getContentResolver().registerContentObserver(RemoteSettingCacheProvider.getInstance().getUriFor(String.valueOf(PortFeeRate.class.getSimpleName()) + "/1"), false, this.feeRateContentObserver);
        this.hintContentObserver = new HintContentObserver(new Handler());
        getContentResolver().registerContentObserver(RemoteSettingCacheProvider.getInstance().getUriFor("content"), true, this.hintContentObserver);
        registScreenReceiver();
        UIEventMessageProxy.getInstance().sendEvent(getClass().getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, getClass().getName(), "create", null);
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        Utils.setPermitNFC(true, true, true, false);
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void initView() {
        this.iv_qrcode = (ImageView) findViewById(R.id.iv_qrcode);
        this.ll_phase = (LinearLayout) findViewById(R.id.ll_phase);
        this.tv_phase = (TextView) findViewById(R.id.tv_phase);
        this.iv_phase = (ImageView) findViewById(R.id.iv_phase);
        this.tv_title = (TextView) findViewById(R.id.tv_title);
        this.tv_subhead = (TextView) findViewById(R.id.tv_subhead);
        this.tv_device_code = (TextView) findViewById(R.id.tv_device_code);
        this.tv_power_price = (TextView) findViewById(R.id.tv_power_price);
        this.tv_service_price = (TextView) findViewById(R.id.tv_service_price);
        if (CountrySettingCacheProvider.getInstance().isSetRTL()) {
            this.ll_phase.setLayoutDirection(1);
        } else {
            this.ll_phase.setLayoutDirection(0);
        }
        this.tv_phase.setText(getString(R.string.home_phash_type, new Object[]{Integer.valueOf(ChargeStatusCacheProvider.getInstance().getAmpCapacity())}));
        switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$PHASE()[HardwareStatusCacheProvider.getInstance().getHardwareStatus().getPhase().ordinal()]) {
            case 1:
                this.iv_phase.setImageResource(R.drawable.ic_charger_type_unkown);
                break;
            case 2:
                this.iv_phase.setImageResource(R.drawable.ic_charger_type_single);
                break;
            case 3:
                this.iv_phase.setImageResource(R.drawable.ic_charger_type_three);
                break;
            case 4:
                this.iv_phase.setImageResource(R.drawable.ic_charger_type_dc);
                break;
        }
        if (HardwareStatusCacheProvider.getInstance().getNetworkStatus().isConnected()) {
            UserDefineUISetting userDefineUISetting = RemoteSettingCacheProvider.getInstance().getUserDefineUISetting();
            if (userDefineUISetting == null || TextUtils.isEmpty(userDefineUISetting.getScanHintTitle())) {
                getPlatformScanHintTitle();
            } else {
                this.tv_title.setText(userDefineUISetting.getScanHintTitle());
            }
        } else {
            getPlatformScanHintTitle();
        }
        if (HardwareStatusCacheProvider.getInstance().getNetworkStatus().isConnected()) {
            UserDefineUISetting userDefineUISetting2 = RemoteSettingCacheProvider.getInstance().getUserDefineUISetting();
            if (userDefineUISetting2 == null || TextUtils.isEmpty(userDefineUISetting2.getScanHintDesc())) {
                getPlatformScanHintDesc();
            } else {
                this.tv_subhead.setText(userDefineUISetting2.getScanHintDesc());
            }
        } else {
            getPlatformScanHintDesc();
        }
        String deviceCode = RemoteSettingCacheProvider.getInstance().getUIDeviceCode();
        if (!TextUtils.isEmpty(deviceCode)) {
            this.tv_device_code.setText(getString(R.string.home_device_code, new Object[]{deviceCode}));
        }
        if (CHARGE_PLATFORM.anyo.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
            HashMap<String, String> platformData = SystemSettingCacheProvider.getInstance().getPlatformCustomizedData();
            this.tv_power_price.setText(platformData.get("id"));
            this.tv_service_price.setText("");
        } else if (Utils.getPricingStrategy(HardwareStatusCacheProvider.getInstance().getNetworkStatus().isConnected())) {
            this.tv_power_price.setText(getString(R.string.home_cur_electrovalence, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(Price.getInstance().getPowerPrice() + Price.getInstance().getServicePrice())), CountrySettingCacheProvider.getInstance().getMoneyDisp()}));
            this.tv_service_price.setText(getString(R.string.home_cur_service_charge, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(Price.getInstance().getServicePrice())), CountrySettingCacheProvider.getInstance().getMoneyDisp()}));
        } else {
            this.tv_power_price.setText(R.string.home_no_feepolicy);
            this.tv_service_price.setText("");
        }
    }

    private void getPlatformScanHintTitle() {
        CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
        if (CHARGE_PLATFORM.anyo.equals(platform) || CHARGE_PLATFORM.xmsz.equals(platform) || CHARGE_PLATFORM.yzx.equals(platform) || CHARGE_PLATFORM.ecw.equals(platform) || CHARGE_PLATFORM.ocpp.equals(platform)) {
            this.tv_title.setText(R.string.home_wechat_app_scan_text);
        } else {
            this.tv_title.setText(R.string.home_wechat_scan_text);
        }
    }

    private void getPlatformScanHintDesc() {
        CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
        if (CHARGE_PLATFORM.anyo.equals(platform) || CHARGE_PLATFORM.xmsz.equals(platform) || CHARGE_PLATFORM.yzx.equals(platform) || CHARGE_PLATFORM.ecw.equals(platform) || CHARGE_PLATFORM.ocpp.equals(platform)) {
            this.tv_subhead.setText(R.string.home_wechat_app_operating_instructions);
        } else {
            this.tv_subhead.setText(R.string.home_wechat_operating_instructions);
        }
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void onUICtrlReceived(UICtrlMessage msg) {
        super.onUICtrlReceived(msg);
        String activity = msg.getActivity();
        if (!TextUtils.isEmpty(activity) && getClass().getName().equals(activity)) {
            String type = msg.getType();
            String subType = msg.getSubType();
            String name = msg.getName();
            String opr = msg.getOpr();
            HashMap<String, Object> data = msg.getData();
            if (UIEventMessage.TYPE_UI_ELEMENT.equals(type) && UIEventMessage.SUBTYPE_UI_LOADING_DIALOG.equals(subType) && "initLoadingDialog".equals(name) && "update".equals(opr)) {
                String status = (String) data.get("nfcDialogStatus");
                updateInitLoadingDidlog(status);
            }
        }
    }

    private void updateInitLoadingDidlog(String status) {
        if ("show".equals(status)) {
            if (this.initLoadingDialog == null) {
                this.initLoadingDialog = LoadingDialog.createDialog(this, getString(R.string.check_device_loading));
            } else {
                this.initLoadingDialog.changeLoadingText(getString(R.string.check_device_loading));
            }
            this.initLoadingDialog.show();
        } else if ("dismiss".equals(status) && this.initLoadingDialog != null && this.initLoadingDialog.isShowing()) {
            this.initLoadingDialog.dismiss();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateQrcodeView() {
        PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus("1");
        if (portStatus == null || portStatus.getQrcodeContent() == null) {
            this.iv_qrcode.setImageBitmap(null);
            return;
        }
        Bitmap mQrcodeBitmap = Utils.createQRImage(portStatus.getQrcodeContent(), 253, 253);
        this.iv_qrcode.setImageBitmap(mQrcodeBitmap);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class ScreenStatusReceiver extends BroadcastReceiver {
        ScreenStatusReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
                QrcodeActivity.this.mHandler.sendEmptyMessage(1);
            } else if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                QrcodeActivity.this.mHandler.sendEmptyMessage(2);
            }
        }
    }

    private void registScreenReceiver() {
        this.mScreenStatusReceiver = new ScreenStatusReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        registerReceiver(this.mScreenStatusReceiver, intentFilter);
    }

    private void unregisterReceiver() {
        if (this.mScreenStatusReceiver != null) {
            unregisterReceiver(this.mScreenStatusReceiver);
            this.mScreenStatusReceiver = null;
        }
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        this.mHandler.removeCallbacksAndMessages(null);
        getContentResolver().unregisterContentObserver(this.qrcodeContentObserver);
        getContentResolver().unregisterContentObserver(this.feeRateContentObserver);
        getContentResolver().unregisterContentObserver(this.hintContentObserver);
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        Log.d("QrcodeActivity", "onResume");
        initView();
        DCAPProxy.getInstance().updatePortPluginStatus("1");
        if (ChargeStatusCacheProvider.getInstance().isCloudConnected()) {
            UIEventMessageProxy.getInstance().sendEvent(QrcodeActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, getClass().getName(), "resume", null);
        }
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        Log.d("QrcodeActivity", "onPause");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
        Log.d("QrcodeActivity", "onStop");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        Log.d("QrcodeActivity", "onDestroy");
        unregisterReceiver();
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        UIEventMessageProxy.getInstance().sendEvent(getClass().getName(), "key", null, getClass().getName(), "up", null);
    }

    private void setScreenBrightness(int paramInt) {
        Window localWindow = getWindow();
        WindowManager.LayoutParams localLayoutParams = localWindow.getAttributes();
        float f = paramInt / 255.0f;
        localLayoutParams.screenBrightness = f;
        localWindow.setAttributes(localLayoutParams);
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void keepScreenOn() {
    }

    /* loaded from: classes.dex */
    public class QrcodeContentObserver extends ContentObserver {
        public QrcodeContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            QrcodeActivity.this.updateQrcodeView();
        }
    }

    /* loaded from: classes.dex */
    public class FeeRateContentObserver extends ContentObserver {
        public FeeRateContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            QrcodeActivity.this.initView();
        }
    }

    /* loaded from: classes.dex */
    public class HintContentObserver extends ContentObserver {
        public HintContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            QrcodeActivity.this.initView();
        }
    }
}
