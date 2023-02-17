package com.xcharge.charger.p006ui.p009c2.activity.charge.nfc;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.xcharge.charger.C0221R;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.data.bean.ContentItem;
import com.xcharge.charger.data.bean.PortFeeRate;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.type.ADVERT_POLICY;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.PARK_STATUS;
import com.xcharge.charger.data.bean.type.PHASE;
import com.xcharge.charger.data.bean.type.PLATFORM_CUSTOMER;
import com.xcharge.charger.data.bean.type.WORK_MODE;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.LocalSettingCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.p006ui.api.UIEventMessageProxy;
import com.xcharge.charger.p006ui.api.bean.UICtrlMessage;
import com.xcharge.charger.p006ui.api.bean.UIEventMessage;
import com.xcharge.charger.p006ui.p009c2.activity.BaseActivity;
import com.xcharge.charger.p006ui.p009c2.activity.advert.WakeupAdDialog;
import com.xcharge.charger.p006ui.p009c2.activity.data.Price;
import com.xcharge.charger.p006ui.p009c2.activity.data.Variate;
import com.xcharge.charger.p006ui.p009c2.activity.fault.OnlineParkBusyHintActivity;
import com.xcharge.charger.p006ui.p009c2.activity.utiles.Utils;
import com.xcharge.charger.p006ui.p009c2.activity.widget.LoadingDialog;
import java.util.ArrayList;
import java.util.HashMap;

/* renamed from: com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargeInitActivity */
public class NFCChargeInitActivity extends BaseActivity {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$PHASE;
    private final int MSG_SCREEN_OFF = 2;
    private final int MSG_SCREEN_ON = 1;
    private FeeRateContentObserver feeRateContentObserver;
    private LoadingDialog initLoadingDialog;
    private ImageView iv_phase;
    private LinearLayout ll_default;
    private LinearLayout ll_jsm;
    private LinearLayout ll_phase;
    protected Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Port port;
            switch (msg.what) {
                case 1:
                    ArrayList<ContentItem> wakeArrayList = RemoteSettingCacheProvider.getInstance().getAdvertContent(ADVERT_POLICY.wakeUpAdvsite);
                    if (wakeArrayList != null && Utils.listIsEmpty(wakeArrayList) && Utils.fileIsEmpty(Utils.platformAdPath(ADVERT_POLICY.wakeUpAdvsite.getPolicy())) && Utils.fileNameIsEqual(wakeArrayList)) {
                        Log.i("wakeUpAdvsite", RemoteSettingCacheProvider.getInstance().getRemoteSetting().getAdvertSetting().toJson());
                        if (NFCChargeInitActivity.this.wakeupAdDialog == null) {
                            NFCChargeInitActivity.this.wakeupAdDialog = WakeupAdDialog.createDialog(NFCChargeInitActivity.context);
                            NFCChargeInitActivity.this.wakeupAdDialog.setCancelable(false);
                        } else {
                            NFCChargeInitActivity.this.wakeupAdDialog.initView();
                        }
                        if (NFCChargeInitActivity.this.wakeupAdDialog != null && !NFCChargeInitActivity.this.wakeupAdDialog.isShowing()) {
                            NFCChargeInitActivity.this.wakeupAdDialog.show();
                            return;
                        }
                        return;
                    }
                    return;
                case 2:
                    if (Variate.getInstance().isRadar() && (port = HardwareStatusCacheProvider.getInstance().getPort("1")) != null) {
                        if (PARK_STATUS.occupied.getStatus().equals(port.getParkStatus().getParkStatus().getStatus()) && HardwareStatusCacheProvider.getInstance().getNetworkStatus().isConnected()) {
                            NFCChargeInitActivity.this.startActivity(new Intent(NFCChargeInitActivity.context, OnlineParkBusyHintActivity.class));
                            NFCChargeInitActivity.this.finish();
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
    private ScreenStatusReceiver mScreenStatusReceiver = null;
    private TextView tv_phash;
    private TextView tv_price;

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$PHASE() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$PHASE;
        if (iArr == null) {
            iArr = new int[PHASE.values().length];
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

    /* access modifiers changed from: protected */
    @SuppressLint({"NewApi"})
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0221R.layout.activity_nfc_charge_init);
        this.feeRateContentObserver = new FeeRateContentObserver(new Handler());
        getContentResolver().registerContentObserver(LocalSettingCacheProvider.getInstance().getUriFor(String.valueOf(PortFeeRate.class.getSimpleName()) + "/1"), false, this.feeRateContentObserver);
        registScreenReceiver();
        UIEventMessageProxy.getInstance().sendEvent(getClass().getName(), UIEventMessage.TYPE_UI_ACTIVITY, (String) null, getClass().getName(), "create", (HashMap) null);
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        Utils.setPermitNFC(true, true, true, false);
    }

    /* access modifiers changed from: protected */
    public void initView() {
        this.ll_jsm = (LinearLayout) findViewById(C0221R.C0223id.ll_jsm);
        this.ll_default = (LinearLayout) findViewById(C0221R.C0223id.ll_default);
        this.tv_price = (TextView) findViewById(C0221R.C0223id.tv_price);
        this.ll_phase = (LinearLayout) findViewById(C0221R.C0223id.ll_phase);
        this.tv_phash = (TextView) findViewById(C0221R.C0223id.tv_phash);
        this.iv_phase = (ImageView) findViewById(C0221R.C0223id.iv_phase);
        if (CHARGE_PLATFORM.xcharge.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
            if (PLATFORM_CUSTOMER.jsmny.equals(SystemSettingCacheProvider.getInstance().getPlatformCustomer())) {
                this.ll_jsm.setVisibility(0);
                this.ll_default.setVisibility(8);
            } else {
                this.ll_jsm.setVisibility(8);
                this.ll_default.setVisibility(0);
            }
        }
        if (ChargeStatusCacheProvider.getInstance().getWorkMode().equals(WORK_MODE.group)) {
            this.tv_price.setVisibility(0);
        } else {
            this.tv_price.setVisibility(8);
        }
        if (Utils.getPricingStrategy(false)) {
            this.tv_price.setText(getString(C0221R.string.home_cur_electrovalence, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(Price.getInstance().getPowerPrice() + Price.getInstance().getServicePrice())), CountrySettingCacheProvider.getInstance().getMoneyDisp()}));
        } else {
            this.tv_price.setText(C0221R.string.home_no_feepolicy);
        }
        if (CountrySettingCacheProvider.getInstance().isSetRTL()) {
            this.ll_phase.setLayoutDirection(1);
        } else {
            this.ll_phase.setLayoutDirection(0);
        }
        this.tv_phash.setText(getString(C0221R.string.home_phash_type, new Object[]{Integer.valueOf(ChargeStatusCacheProvider.getInstance().getAmpCapacity())}));
        switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$PHASE()[HardwareStatusCacheProvider.getInstance().getHardwareStatus().getPhase().ordinal()]) {
            case 1:
                this.iv_phase.setImageResource(C0221R.C0222drawable.ic_charger_type_unkown);
                return;
            case 2:
                this.iv_phase.setImageResource(C0221R.C0222drawable.ic_charger_type_single);
                return;
            case 3:
                this.iv_phase.setImageResource(C0221R.C0222drawable.ic_charger_type_three);
                return;
            case 4:
                this.iv_phase.setImageResource(C0221R.C0222drawable.ic_charger_type_dc);
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: protected */
    public void onUICtrlReceived(UICtrlMessage msg) {
        super.onUICtrlReceived(msg);
        String activity = msg.getActivity();
        if (!TextUtils.isEmpty(activity) && getClass().getName().equals(activity)) {
            String type = msg.getType();
            String subType = msg.getSubType();
            String name = msg.getName();
            String opr = msg.getOpr();
            HashMap<String, Object> data = msg.getData();
            if (UIEventMessage.TYPE_UI_ELEMENT.equals(type) && UIEventMessage.SUBTYPE_UI_LOADING_DIALOG.equals(subType) && "initLoadingDialog".equals(name) && "update".equals(opr)) {
                updateInitLoadingDidlog((String) data.get("nfcDialogStatus"));
            }
        }
    }

    private void updateInitLoadingDidlog(String status) {
        if ("show".equals(status)) {
            if (this.initLoadingDialog == null) {
                this.initLoadingDialog = LoadingDialog.createDialog(this, getString(C0221R.string.check_device_loading));
            } else {
                this.initLoadingDialog.changeLoadingText(getString(C0221R.string.check_device_loading));
            }
            this.initLoadingDialog.show();
        } else if ("dismiss".equals(status) && this.initLoadingDialog != null && this.initLoadingDialog.isShowing()) {
            this.initLoadingDialog.dismiss();
        }
    }

    /* renamed from: com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargeInitActivity$ScreenStatusReceiver */
    class ScreenStatusReceiver extends BroadcastReceiver {
        ScreenStatusReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
                NFCChargeInitActivity.this.mHandler.sendEmptyMessage(1);
            } else if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                NFCChargeInitActivity.this.mHandler.sendEmptyMessage(2);
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
        this.mHandler.removeCallbacksAndMessages((Object) null);
        getContentResolver().unregisterContentObserver(this.feeRateContentObserver);
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        Log.d("NFCChargeInitActivity", "onResume");
        initView();
        DCAPProxy.getInstance().updatePortPluginStatus("1");
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        Log.d("NFCChargeInitActivity", "onPause");
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        Log.d("NFCChargeInitActivity", "onStop");
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        Log.d("NFCChargeInitActivity", "onDestroy");
        unregisterReceiver();
    }

    /* access modifiers changed from: protected */
    public void keepScreenOn() {
    }

    public void onBackPressed() {
    }

    /* renamed from: com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargeInitActivity$FeeRateContentObserver */
    public class FeeRateContentObserver extends ContentObserver {
        public FeeRateContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            NFCChargeInitActivity.this.initView();
        }
    }
}
