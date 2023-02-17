package com.xcharge.charger.ui.c2.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.xcharge.charger.R;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.device.MobileNet;
import com.xcharge.charger.data.bean.device.NFC;
import com.xcharge.charger.data.bean.device.Network;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.type.CHARGE_INIT_TYPE;
import com.xcharge.charger.data.bean.type.CHARGE_MODE;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.CHARGE_USER_TYPE;
import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.bean.type.NFC_OPR_TYPE;
import com.xcharge.charger.data.bean.type.PARK_STATUS;
import com.xcharge.charger.data.bean.type.PLATFORM_CUSTOMER;
import com.xcharge.charger.data.bean.type.WORK_MODE;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.LocalSettingCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SoftwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition;
import com.xcharge.charger.ui.api.UIEventMessageProxy;
import com.xcharge.charger.ui.api.bean.UICtrlMessage;
import com.xcharge.charger.ui.api.bean.UIEventMessage;
import com.xcharge.charger.ui.c2.activity.advert.IdleAdDialog;
import com.xcharge.charger.ui.c2.activity.advert.PullAdActivity;
import com.xcharge.charger.ui.c2.activity.advert.ScanAdActivity;
import com.xcharge.charger.ui.c2.activity.advert.WakeupAdDialog;
import com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargeBalanceActivity;
import com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargeFinActivity;
import com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargeInitActivity;
import com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargeWaittingStartActivity;
import com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargingActivity;
import com.xcharge.charger.ui.c2.activity.charge.nfc.NFCConfigPersnalCardActivity;
import com.xcharge.charger.ui.c2.activity.charge.online.ChargeCompleteActivity;
import com.xcharge.charger.ui.c2.activity.charge.online.ChargeCostActivity;
import com.xcharge.charger.ui.c2.activity.charge.online.ChargeDelayedActivity;
import com.xcharge.charger.ui.c2.activity.charge.online.ChargingActivity;
import com.xcharge.charger.ui.c2.activity.charge.online.QrcodeActivity;
import com.xcharge.charger.ui.c2.activity.charge.online.ReservedActivity;
import com.xcharge.charger.ui.c2.activity.charge.online.WaittingStartChargeActivity;
import com.xcharge.charger.ui.c2.activity.data.InitImageLoader;
import com.xcharge.charger.ui.c2.activity.data.UiBackgroundColorContentObserver;
import com.xcharge.charger.ui.c2.activity.data.Variate;
import com.xcharge.charger.ui.c2.activity.fault.CloudFaultDialog;
import com.xcharge.charger.ui.c2.activity.fault.EnableActivity;
import com.xcharge.charger.ui.c2.activity.fault.ExceptionFaultDialog;
import com.xcharge.charger.ui.c2.activity.fault.NetworkFaultDialog;
import com.xcharge.charger.ui.c2.activity.fault.OnlineParkBusyHintActivity;
import com.xcharge.charger.ui.c2.activity.fault.VerificationDialog;
import com.xcharge.charger.ui.c2.activity.test.SetActivity;
import com.xcharge.charger.ui.c2.activity.test.TestChargeActivity;
import com.xcharge.charger.ui.c2.activity.upgrade.UpgradeActivity;
import com.xcharge.charger.ui.c2.activity.utiles.Utils;
import com.xcharge.charger.ui.c2.activity.widget.LoadingDialog;
import com.xcharge.charger.ui.c2.activity.widget.ShowInfoDialog;
import com.xcharge.charger.ui.c2.activity.widget.SmallDialog;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.HandlerTimer;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* loaded from: classes.dex */
public abstract class BaseActivity extends Activity {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$PARK_STATUS = null;
    public static final int MSG_NFC_CARD_ARRIVES = 4099;
    public static final int MSG_NFC_CARD_AWAY = 4100;
    public static final int MSG_NFC_DISMISS_MONEY = 4101;
    public static final int MSG_RELEASE_LOCK = 4098;
    public static final int MSG_UI_CTRL = 4097;
    public static final String ONEDP = "%.1f";
    public static final String THREEDP = "%.3f";
    public static final String TWODP = "%.2f";
    public static Context context;
    public static ErrorCode deviceError = new ErrorCode(200);
    public static SmallDialog smallDialog;
    public CloudContentObserver cloudContentObserver;
    public CloudFaultDialog cloudFaultDialog;
    public CompanyContentObserver companyContentObserver;
    public ExceptionFaultDialog exceptionFaultDialog;
    public FaultStatusContentObserver faultStatusContentObserver;
    public IdleAdDialog idleAdDialog;
    protected ImageView iv_cloud;
    protected ImageView iv_company;
    protected ImageView iv_lock;
    protected ImageView iv_network;
    protected ImageView iv_nfc;
    protected ImageView iv_nfc_reading;
    protected ImageView iv_park;
    protected ImageView iv_signal;
    protected LinearLayout ll_base;
    public LocaleLangContentObserver localeLangContentObserver;
    public LockStatusContentObserver lockStatusContentObserver;
    protected LoadingDialog moneyLoadingDialog;
    public NetWorkContentObserver netWorkContentObserver;
    public NetworkFaultDialog networkFaultDialog;
    protected LoadingDialog nfcLoadingDialog;
    public NfcStatusContentObserver nfcStatusContentObserver;
    protected CHARGE_MODE nowChargeMode;
    public PayContentObserver payContentObserver;
    public PluginContentObserver pluginContentObserver;
    public PortEnableContentObserver portEnableContentObserver;
    public RadarStatusContentObserver radarStatusContentObserver;
    public PowerManager.WakeLock screenWl;
    protected TextView tv_rader;
    public UiBackgroundColorContentObserver uiBackgroundColorContentObserver;
    public UpgradeContentObserver upgradeContentObserver;
    public VerificationDialog verificationDialog;
    public WakeupAdDialog wakeupAdDialog;
    public SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    private CtrlHandler handler = null;
    private CtrlMessageReceiver messageReceiver = null;
    private HandlerTimer handlerTimer = null;
    private int screenWlCnt = 0;
    private long firstTime = 0;
    boolean isOnKeyLongPress = false;
    protected Handler mRadarHandler = new Handler() { // from class: com.xcharge.charger.ui.c2.activity.BaseActivity.1
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 4098:
                    BaseActivity.this.releaseScreenWl();
                    return;
                case 4099:
                    if (BaseActivity.this.nfcLoadingDialog == null) {
                        BaseActivity.this.nfcLoadingDialog = LoadingDialog.createDialog(BaseActivity.this, BaseActivity.this.getString(R.string.nfc_processing));
                    } else {
                        BaseActivity.this.nfcLoadingDialog.changeLoadingText(BaseActivity.this.getString(R.string.nfc_processing));
                    }
                    if (BaseActivity.this.nfcLoadingDialog != null && !BaseActivity.this.nfcLoadingDialog.isShowing()) {
                        BaseActivity.this.nfcLoadingDialog.show();
                        return;
                    }
                    return;
                case BaseActivity.MSG_NFC_CARD_AWAY /* 4100 */:
                    removeMessages(BaseActivity.MSG_NFC_CARD_AWAY);
                    if (BaseActivity.this.nfcLoadingDialog != null) {
                        BaseActivity.this.nfcLoadingDialog.dismiss();
                        return;
                    }
                    return;
                case BaseActivity.MSG_NFC_DISMISS_MONEY /* 4101 */:
                    removeMessages(BaseActivity.MSG_NFC_DISMISS_MONEY);
                    if (BaseActivity.this.moneyLoadingDialog != null) {
                        BaseActivity.this.moneyLoadingDialog.dismiss();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };

    protected abstract void initView();

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$PARK_STATUS() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$PARK_STATUS;
        if (iArr == null) {
            iArr = new int[PARK_STATUS.valuesCustom().length];
            try {
                iArr[PARK_STATUS.idle.ordinal()] = 2;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[PARK_STATUS.occupied.ordinal()] = 3;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[PARK_STATUS.unknown.ordinal()] = 1;
            } catch (NoSuchFieldError e3) {
            }
            $SWITCH_TABLE$com$xcharge$charger$data$bean$type$PARK_STATUS = iArr;
        }
        return iArr;
    }

    @Override // android.app.Activity
    @SuppressLint({"NewApi"})
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        InitImageLoader.getInstance(getApplicationContext());
        initScreenWakeLock();
        initStatus();
        initView();
        deviceError = new ErrorCode(200);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class CtrlMessageReceiver extends BroadcastReceiver {
        private CtrlMessageReceiver() {
        }

        /* synthetic */ CtrlMessageReceiver(BaseActivity baseActivity, CtrlMessageReceiver ctrlMessageReceiver) {
            this();
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UICtrlMessage.ACTION_UI_CTRL)) {
                String body = intent.getStringExtra("body");
                Message msg = new Message();
                msg.what = 4097;
                msg.obj = body;
                BaseActivity.this.handler.sendMessage(msg);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class CtrlHandler extends Handler {
        private CtrlHandler() {
        }

        /* synthetic */ CtrlHandler(BaseActivity baseActivity, CtrlHandler ctrlHandler) {
            this();
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 4097:
                    String json = (String) msg.obj;
                    BaseActivity.this.onUICtrlReceived((UICtrlMessage) JsonBean.getGsonBuilder().create().fromJson(json, (Class<Object>) UICtrlMessage.class));
                    break;
            }
            super.handleMessage(msg);
        }
    }

    public void initStatus() {
        Port port;
        Variate.getInstance().setRadar(HardwareStatusCacheProvider.getInstance().getPortRadarSwitch("1"));
        if (Variate.getInstance().isRadar() && (port = HardwareStatusCacheProvider.getInstance().getPort("1")) != null) {
            String parkStatus = port.getParkStatus().getParkStatus().getStatus();
            if (PARK_STATUS.idle.getStatus().equals(parkStatus) && Utils.getCurrentClassName(context).equals(OnlineParkBusyHintActivity.class.getName())) {
                Utils.skipNfcQrcode(context);
                finish();
            }
        }
        boolean isPortEnable = ChargeStatusCacheProvider.getInstance().getPortSwitch("1");
        if (!isPortEnable && (Utils.getCurrentClassName(context).equals(NFCChargeInitActivity.class.getName()) || Utils.getCurrentClassName(context).equals(QrcodeActivity.class.getName()))) {
            startActivity(new Intent(context, EnableActivity.class));
            finish();
        }
        this.ll_base = (LinearLayout) findViewById(R.id.ll_base);
        Utils.customizeUiBgColor(this.ll_base);
        this.iv_nfc = (ImageView) findViewById(R.id.iv_nfc);
        this.iv_nfc.setOnClickListener(new View.OnClickListener() { // from class: com.xcharge.charger.ui.c2.activity.BaseActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                long secondTime = System.currentTimeMillis();
                if (secondTime - BaseActivity.this.firstTime > 2000) {
                    BaseActivity.this.firstTime = secondTime;
                } else {
                    Utils.screenshot(getClass().getName(), BaseActivity.this.getWindow().getDecorView());
                }
            }
        });
        this.iv_network = (ImageView) findViewById(R.id.iv_network);
        Network network = HardwareStatusCacheProvider.getInstance().getNetworkStatus();
        if (network.isConnected()) {
            if (TextUtils.isEmpty(network.getActive())) {
                this.iv_network.setImageResource(R.drawable.ic_net_icon_disconn);
            } else if (Network.NETWORK_TYPE_MOBILE.equals(network.getActive())) {
                MobileNet mobileNet = HardwareStatusCacheProvider.getInstance().getMobileNetStatus();
                if (mobileNet != null) {
                    if ("2G".equals(mobileNet.getType())) {
                        this.iv_network.setImageResource(R.drawable.ic_net_icon_2g);
                    } else if ("3G".equals(mobileNet.getType())) {
                        this.iv_network.setImageResource(R.drawable.ic_net_icon_3g);
                    } else if ("4G".equals(mobileNet.getType())) {
                        this.iv_network.setImageResource(R.drawable.ic_net_icon_4g);
                    }
                }
            } else if (Network.NETWORK_TYPE_ETHERNET.equals(network.getActive())) {
                this.iv_network.setImageResource(R.drawable.ic_net_icon_line);
            } else if (Network.NETWORK_TYPE_WIFI.equals(network.getActive())) {
                this.iv_network.setImageResource(R.drawable.ic_net_icon_wifi);
            } else if ("none".equals(network.getActive())) {
                this.iv_network.setImageResource(R.drawable.ic_net_icon_disconn);
            }
        } else {
            this.iv_network.setImageResource(R.drawable.ic_net_icon_disconn);
        }
        this.iv_signal = (ImageView) findViewById(R.id.iv_signal);
        if (network.isConnected()) {
            if (Network.NETWORK_TYPE_MOBILE.equals(network.getActive())) {
                MobileNet mobileNet2 = HardwareStatusCacheProvider.getInstance().getMobileNetStatus();
                if (mobileNet2 != null && ("2G".equals(mobileNet2.getType()) | "3G".equals(mobileNet2.getType()) | "4G".equals(mobileNet2.getType()))) {
                    this.iv_signal.setVisibility(0);
                    switch (mobileNet2.getDefaultSignalLevel()) {
                        case -1:
                            this.iv_signal.setVisibility(8);
                            break;
                        case 0:
                            this.iv_signal.setImageResource(R.drawable.ic_signal_0);
                            break;
                        case 1:
                            this.iv_signal.setImageResource(R.drawable.ic_signal_1);
                            break;
                        case 2:
                            this.iv_signal.setImageResource(R.drawable.ic_signal_2);
                            break;
                        case 3:
                            this.iv_signal.setImageResource(R.drawable.ic_signal_3);
                            break;
                        case 4:
                            this.iv_signal.setImageResource(R.drawable.ic_signal_4);
                            break;
                    }
                }
            } else {
                this.iv_signal.setVisibility(8);
            }
        } else {
            this.iv_signal.setVisibility(8);
        }
        this.iv_cloud = (ImageView) findViewById(R.id.iv_cloud);
        if (ChargeStatusCacheProvider.getInstance().isCloudConnected()) {
            this.iv_cloud.setVisibility(0);
        } else {
            this.iv_cloud.setVisibility(8);
        }
        this.iv_lock = (ImageView) findViewById(R.id.iv_lock);
        this.iv_lock.setVisibility(0);
        LOCK_STATUS lockStatus = ChargeStatusCacheProvider.getInstance().getPortLockStatus("1");
        if (LOCK_STATUS.disable.getStatus().equals(lockStatus.getStatus())) {
            this.iv_lock.setImageResource(R.drawable.ic_disable);
        } else if (LOCK_STATUS.lock.getStatus().equals(lockStatus.getStatus())) {
            this.iv_lock.setImageResource(R.drawable.ic_lock);
        } else if (LOCK_STATUS.unlock.getStatus().equals(lockStatus.getStatus())) {
            this.iv_lock.setImageResource(R.drawable.ic_unlock);
        } else if (LOCK_STATUS.fault.getStatus().equals(lockStatus.getStatus())) {
            this.iv_lock.setVisibility(8);
        }
        this.iv_park = (ImageView) findViewById(R.id.iv_park);
        if (Variate.getInstance().isRadar()) {
            Port port2 = HardwareStatusCacheProvider.getInstance().getPort("1");
            if (port2 != null) {
                switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$PARK_STATUS()[port2.getParkStatus().getParkStatus().ordinal()]) {
                    case 1:
                        this.iv_park.setVisibility(8);
                        break;
                    case 2:
                        this.iv_park.setVisibility(8);
                        break;
                    case 3:
                        this.iv_park.setVisibility(0);
                        break;
                }
            }
        } else {
            this.iv_park.setVisibility(8);
        }
        this.tv_rader = (TextView) findViewById(R.id.tv_rader);
        this.iv_nfc_reading = (ImageView) findViewById(R.id.iv_nfc_reading);
        this.iv_company = (ImageView) findViewById(R.id.iv_company);
        if (SystemSettingCacheProvider.getInstance().isUsingXChargeLogo()) {
            this.iv_company.setImageResource(R.drawable.ic_company);
        } else {
            String companyResouce = RemoteSettingCacheProvider.getInstance().getCompanyResouce();
            if (TextUtils.isEmpty(companyResouce) || !Utils.fileIsExists(companyResouce)) {
                getCompanyLogo();
            } else {
                Utils.loadImage(companyResouce, this.iv_company, new ImageLoadingListener() { // from class: com.xcharge.charger.ui.c2.activity.BaseActivity.3
                    @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                    public void onLoadingStarted(String arg0, View arg1) {
                    }

                    @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                    public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
                        BaseActivity.this.getCompanyLogo();
                    }

                    @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                    public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
                    }

                    @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                    public void onLoadingCancelled(String arg0, View arg1) {
                    }
                }, this);
            }
        }
        this.iv_company.setOnClickListener(new View.OnClickListener() { // from class: com.xcharge.charger.ui.c2.activity.BaseActivity.4
            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                if (Utils.getCurrentClassName(BaseActivity.context).equals(NFCChargeInitActivity.class.getName()) || Utils.getCurrentClassName(BaseActivity.context).equals(QrcodeActivity.class.getName())) {
                    BaseActivity.this.startActivity(new Intent(BaseActivity.context, SetActivity.class));
                    BaseActivity.this.finish();
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void getCompanyLogo() {
        CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
        if (CHARGE_PLATFORM.xcharge.equals(platform)) {
            PLATFORM_CUSTOMER customer = SystemSettingCacheProvider.getInstance().getPlatformCustomer();
            if (PLATFORM_CUSTOMER.jsmny.equals(customer)) {
                this.iv_company.setImageResource(R.drawable.jsmlogo);
            } else {
                this.iv_company.setImageResource(R.drawable.ic_company);
            }
        } else if (CHARGE_PLATFORM.xmsz.equals(platform)) {
            this.iv_company.setImageResource(R.drawable.ic_company);
        } else if (CHARGE_PLATFORM.anyo.equals(platform)) {
            this.iv_company.setImageResource(R.drawable.aylogo);
        } else {
            this.iv_company.setImageResource(R.drawable.ic_company);
        }
    }

    private void initDialog() {
        CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
        PLATFORM_CUSTOMER customer = SystemSettingCacheProvider.getInstance().getPlatformCustomer();
        if (!HardwareStatusCacheProvider.getInstance().getNetworkStatus().isConnected()) {
            if (CHARGE_PLATFORM.xcharge.equals(platform)) {
                if (PLATFORM_CUSTOMER.anyo_private.equals(customer)) {
                    showNetworkFault();
                }
            } else if ((CHARGE_PLATFORM.anyo.equals(platform) && !PLATFORM_CUSTOMER.anyo_svw.equals(customer)) || CHARGE_PLATFORM.xmsz.equals(platform)) {
                showNetworkFault();
            }
        }
        if (!ChargeStatusCacheProvider.getInstance().isCloudConnected()) {
            if (CHARGE_PLATFORM.xcharge.equals(platform)) {
                if (PLATFORM_CUSTOMER.anyo_private.equals(customer)) {
                    showCloudFaultDialog();
                }
            } else if ((CHARGE_PLATFORM.anyo.equals(platform) && !PLATFORM_CUSTOMER.anyo_svw.equals(customer)) || CHARGE_PLATFORM.xmsz.equals(platform)) {
                showCloudFaultDialog();
            }
        }
        ErrorCode error = HardwareStatusCacheProvider.getInstance().getPortFault("1");
        if (error.getCode() >= 30010 && error.getCode() <= 30018) {
            Log.w("ErrorCode", new StringBuilder(String.valueOf(HardwareStatusCacheProvider.getInstance().getPort("1").getDeviceError().getCode())).toString());
            Variate.getInstance().setActivity(Utils.getCurrentClassName(context));
            if (!Utils.getCurrentClassName(context).equals(NFCChargeBalanceActivity.class.getName()) && !Utils.getCurrentClassName(context).equals(ChargeCostActivity.class.getName()) && !Utils.getCurrentClassName(context).equals(SetActivity.class.getName())) {
                showExceptionFaltDialog(error);
                return;
            }
            return;
        }
        deviceError = error;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onUICtrlReceived(UICtrlMessage msg) {
        String type = msg.getType();
        String subType = msg.getSubType();
        String name = msg.getName();
        String opr = msg.getOpr();
        HashMap<String, Object> data = msg.getData();
        if (UIEventMessage.TYPE_UI_ACTIVITY.equals(type)) {
            if ("testCharge".equals(name)) {
                if (UICtrlMessage.OPR_SKIP.equals(opr)) {
                    startActivity(new Intent(context, TestChargeActivity.class));
                    finish();
                }
            } else if ("testHome".equals(name)) {
                if (UICtrlMessage.OPR_SKIP.equals(opr)) {
                    startActivity(new Intent(context, HomeActivity.class));
                    finish();
                }
            } else if ("nfcReady".equals(name)) {
                if (UICtrlMessage.OPR_SKIP.equals(opr) && !Utils.getCurrentClassName(context).equals(NFCChargeInitActivity.class.getName()) && !Utils.getCurrentClassName(context).equals(QrcodeActivity.class.getName())) {
                    Utils.skipNfcQrcode(context);
                    finish();
                }
            } else if ("nfcScanAdvert".equals(name)) {
                if (UICtrlMessage.OPR_SKIP.equals(opr)) {
                    startActivity(new Intent(context, ScanAdActivity.class));
                    finish();
                }
            } else if ("nfcInited".equals(name)) {
                if (UICtrlMessage.OPR_SKIP.equals(opr)) {
                    startActivity(new Intent(context, NFCChargeWaittingStartActivity.class));
                    finish();
                }
            } else if ("nfcCharging".equals(name)) {
                if (UICtrlMessage.OPR_SKIP.equals(opr)) {
                    startActivity(new Intent(context, NFCChargingActivity.class));
                    finish();
                }
            } else if ("nfcStopped".equals(name)) {
                if (UICtrlMessage.OPR_SKIP.equals(opr)) {
                    startActivity(new Intent(context, NFCChargeFinActivity.class));
                    finish();
                }
            } else if ("nfcDelay".equals(name)) {
                if (UICtrlMessage.OPR_SKIP.equals(opr)) {
                    startActivity(new Intent(context, ChargeDelayedActivity.class));
                    finish();
                }
            } else if ("nfcPullAdvsite".equals(name)) {
                if (UICtrlMessage.OPR_SKIP.equals(opr)) {
                    startActivity(new Intent(context, PullAdActivity.class));
                    finish();
                }
            } else if ("nfcBilled".equals(name)) {
                if (UICtrlMessage.OPR_SKIP.equals(opr)) {
                    startActivity(new Intent(context, NFCChargeBalanceActivity.class));
                    finish();
                }
            } else if ("onlineReady".equals(name)) {
                if (UICtrlMessage.OPR_SKIP.equals(opr) && !Utils.getCurrentClassName(context).equals(NFCChargeInitActivity.class.getName()) && !Utils.getCurrentClassName(context).equals(QrcodeActivity.class.getName())) {
                    Utils.skipNfcQrcode(context);
                    finish();
                }
            } else if ("onlineScanAdvert".equals(name)) {
                if (UICtrlMessage.OPR_SKIP.equals(opr)) {
                    startActivity(new Intent(context, ScanAdActivity.class));
                    finish();
                }
            } else if ("onlineUserReserved".equals(name)) {
                if (UICtrlMessage.OPR_SKIP.equals(opr)) {
                    startActivity(new Intent(context, ReservedActivity.class));
                    finish();
                }
            } else if ("onlineInited".equals(name)) {
                if (UICtrlMessage.OPR_SKIP.equals(opr)) {
                    startActivity(new Intent(context, WaittingStartChargeActivity.class));
                    finish();
                }
            } else if ("onlinePlugin".equals(name)) {
                if (UICtrlMessage.OPR_SKIP.equals(opr)) {
                    startActivity(new Intent(context, WaittingStartChargeActivity.class));
                    finish();
                }
            } else if ("onlineCharging".equals(name)) {
                if (UICtrlMessage.OPR_SKIP.equals(opr)) {
                    startActivity(new Intent(context, ChargingActivity.class));
                    finish();
                }
            } else if ("onlineStopped".equals(name)) {
                if (UICtrlMessage.OPR_SKIP.equals(opr)) {
                    startActivity(new Intent(context, ChargeCompleteActivity.class));
                    finish();
                }
            } else if ("onlineDelay".equals(name)) {
                if (UICtrlMessage.OPR_SKIP.equals(opr)) {
                    startActivity(new Intent(context, ChargeDelayedActivity.class));
                    finish();
                }
            } else if ("onlinePullAdvsite".equals(name)) {
                if (UICtrlMessage.OPR_SKIP.equals(opr)) {
                    startActivity(new Intent(context, PullAdActivity.class));
                    finish();
                }
            } else if ("onlineBilled".equals(name) && UICtrlMessage.OPR_SKIP.equals(opr)) {
                startActivity(new Intent(context, ChargeCostActivity.class));
                finish();
            }
        } else if (UIEventMessage.TYPE_UI_ELEMENT.equals(type)) {
            if ("challenge".equals(name)) {
                if ("update".equals(opr) && "verification".equals(data.get("type"))) {
                    showVerificationDialog(data);
                }
            } else if ("errorStop".equals(name) && "update".equals(opr)) {
                deviceError = new ErrorCode(Integer.parseInt((String) data.get("error")));
                this.exceptionFaultDialog.error = Integer.parseInt((String) data.get("error"));
                this.exceptionFaultDialog.visibility = "VISIBLE";
                this.exceptionFaultDialog.cnt = Integer.parseInt((String) data.get("cnt"));
                this.exceptionFaultDialog.initView();
                this.exceptionFaultDialog.show();
            }
        } else if ("key".equals(type) && UIEventMessage.KEY_HOME.equals(subType) && "key".equals(name) && "down".equals(opr)) {
            String text = (String) data.get("keyText");
            longPress(text);
        }
    }

    private void showVerificationDialog(Map<String, Object> data) {
        Utils.releaseScreenLock(context);
        this.verificationDialog = new VerificationDialog(context, (String) data.get("xid"), (String) data.get("customer"), Integer.valueOf((String) data.get("expired")).intValue());
        this.verificationDialog.setCancelable(false);
        if (this.verificationDialog != null && !this.verificationDialog.isShowing()) {
            this.verificationDialog.show();
        }
    }

    private void longPress(String text) {
        ShowInfoDialog showInfoDialog = new ShowInfoDialog(context, text);
        showInfoDialog.setCancelable(false);
        if (showInfoDialog != null && !showInfoDialog.isShowing()) {
            showInfoDialog.show();
        }
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == 4 && this.isOnKeyLongPress) {
            this.isOnKeyLongPress = false;
        } else {
            if (keyCode == 4 && !this.isOnKeyLongPress && !event.isTainted()) {
                onBackPressed();
            }
            this.isOnKeyLongPress = false;
        }
        return true;
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4 && event.getRepeatCount() == 10) {
            this.isOnKeyLongPress = true;
            if (!Utils.getCurrentClassName(context).equals(TestChargeActivity.class.getName()) && !Utils.getCurrentClassName(context).equals(HomeActivity.class.getName())) {
                UIEventMessageProxy.getInstance().sendEvent(getClass().getName(), "key", null, getClass().getName(), "down", null);
            }
        } else if (keyCode == 4) {
            event.getRepeatCount();
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        keepScreenOn();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        if (this.nowChargeMode != null && this.nowChargeMode.getMode() != CHARGE_MODE.normal_charge.getMode()) {
            if (PLATFORM_CUSTOMER.anyo_private.equals(SystemSettingCacheProvider.getInstance().getPlatformCustomer())) {
                acquireScreenLock();
            } else {
                acquireScreenLockAndKeep();
            }
        } else {
            acquireScreenLock();
        }
        initDialog();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onPause() {
        super.onPause();
        dismissAllDialog();
    }

    private void dismissAllDialog() {
        deviceError = new ErrorCode(200);
        String activity = Variate.getInstance().getActivity();
        if (activity != null && activity.equals(Utils.getCurrentClassName(context))) {
            if (this.exceptionFaultDialog != null) {
                Log.w("异常否", "onPause--" + Utils.getCurrentClassName(context));
                this.exceptionFaultDialog.dismiss();
            }
            if (this.networkFaultDialog != null) {
                Log.w("网络关", "onPause--" + Utils.getCurrentClassName(context));
                this.networkFaultDialog.dismiss();
            }
            if (this.cloudFaultDialog != null) {
                Log.w("云端断开", "onPause--" + Utils.getCurrentClassName(context));
                this.cloudFaultDialog.dismiss();
            }
            Variate.getInstance().setActivity(null);
        }
        if (this.wakeupAdDialog != null) {
            this.wakeupAdDialog.dismiss();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onStop() {
        super.onStop();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        destroy();
    }

    private void init() {
        context = getApplicationContext();
        CountrySettingCacheProvider.getInstance().setAppLang(context);
        this.handler = new CtrlHandler(this, null);
        this.messageReceiver = new CtrlMessageReceiver(this, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(UICtrlMessage.ACTION_UI_CTRL);
        LocalBroadcastManager.getInstance(context).registerReceiver(this.messageReceiver, filter);
        UIEventMessageProxy.getInstance().init(context);
        this.handlerTimer = new HandlerTimer(this.mRadarHandler);
        this.handlerTimer.init(context);
        this.netWorkContentObserver = new NetWorkContentObserver(this.handler);
        this.cloudContentObserver = new CloudContentObserver(this.handler);
        this.lockStatusContentObserver = new LockStatusContentObserver(this.handler);
        this.portEnableContentObserver = new PortEnableContentObserver(this.handler);
        this.radarStatusContentObserver = new RadarStatusContentObserver(this.handler);
        this.faultStatusContentObserver = new FaultStatusContentObserver(this.handler);
        this.nfcStatusContentObserver = new NfcStatusContentObserver(this.handler);
        this.upgradeContentObserver = new UpgradeContentObserver(this.handler);
        this.payContentObserver = new PayContentObserver(this.handler);
        this.companyContentObserver = new CompanyContentObserver(this.handler);
        this.pluginContentObserver = new PluginContentObserver(this.handler);
        this.localeLangContentObserver = new LocaleLangContentObserver(this.handler);
        this.uiBackgroundColorContentObserver = new UiBackgroundColorContentObserver(this.ll_base, this.handler);
        getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor(String.valueOf(Network.class.getSimpleName()) + MqttTopic.TOPIC_LEVEL_SEPARATOR), true, this.netWorkContentObserver);
        getContentResolver().registerContentObserver(ChargeStatusCacheProvider.getInstance().getUriFor("cloud/connection"), false, this.cloudContentObserver);
        getContentResolver().registerContentObserver(ChargeStatusCacheProvider.getInstance().getUriFor("ports/lock/status/1"), false, this.lockStatusContentObserver);
        getContentResolver().registerContentObserver(ChargeStatusCacheProvider.getInstance().getUriFor("ports/enable/1"), false, this.portEnableContentObserver);
        getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor("ports/radar/status/1"), false, this.radarStatusContentObserver);
        getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor("ports/1"), false, this.faultStatusContentObserver);
        getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor("ports/1/nfc/"), true, this.nfcStatusContentObserver);
        getContentResolver().registerContentObserver(SoftwareStatusCacheProvider.getInstance().getUriFor("upgrade"), false, this.upgradeContentObserver);
        getContentResolver().registerContentObserver(ChargeContentProxy.getInstance().getUriFor("pay"), true, this.payContentObserver);
        getContentResolver().registerContentObserver(RemoteSettingCacheProvider.getInstance().getUriFor("resource/company"), true, this.companyContentObserver);
        getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor("ports/1/plugin"), false, this.pluginContentObserver);
        getContentResolver().registerContentObserver(CountrySettingCacheProvider.getInstance().getUriFor("lang"), false, this.localeLangContentObserver);
        getContentResolver().registerContentObserver(SystemSettingCacheProvider.getInstance().getUriFor("uiBackgroundColor"), false, this.uiBackgroundColorContentObserver);
        this.networkFaultDialog = new NetworkFaultDialog(context);
        this.networkFaultDialog.setCancelable(false);
        this.cloudFaultDialog = new CloudFaultDialog(context);
        this.cloudFaultDialog.setCancelable(false);
        this.exceptionFaultDialog = new ExceptionFaultDialog(context);
        this.exceptionFaultDialog.setCancelable(false);
        this.idleAdDialog = new IdleAdDialog(context);
        this.idleAdDialog.setCancelable(false);
    }

    public void destroy() {
        UIEventMessageProxy.getInstance().destroy();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this.messageReceiver);
        getContentResolver().unregisterContentObserver(this.netWorkContentObserver);
        getContentResolver().unregisterContentObserver(this.cloudContentObserver);
        getContentResolver().unregisterContentObserver(this.lockStatusContentObserver);
        getContentResolver().unregisterContentObserver(this.portEnableContentObserver);
        getContentResolver().unregisterContentObserver(this.radarStatusContentObserver);
        getContentResolver().unregisterContentObserver(this.faultStatusContentObserver);
        getContentResolver().unregisterContentObserver(this.nfcStatusContentObserver);
        getContentResolver().unregisterContentObserver(this.upgradeContentObserver);
        getContentResolver().unregisterContentObserver(this.payContentObserver);
        getContentResolver().unregisterContentObserver(this.companyContentObserver);
        getContentResolver().unregisterContentObserver(this.pluginContentObserver);
        getContentResolver().unregisterContentObserver(this.localeLangContentObserver);
        getContentResolver().unregisterContentObserver(this.uiBackgroundColorContentObserver);
        this.handlerTimer.destroy();
        this.handler.removeMessages(4097);
        this.mRadarHandler.removeMessages(MSG_NFC_DISMISS_MONEY);
        this.mRadarHandler.removeMessages(4099);
        this.mRadarHandler.removeMessages(MSG_NFC_CARD_AWAY);
        if (this.moneyLoadingDialog != null) {
            this.moneyLoadingDialog.dismiss();
        }
        if (this.nfcLoadingDialog != null) {
            this.nfcLoadingDialog.dismiss();
        }
        if (this.iv_nfc_reading != null) {
            this.iv_nfc_reading.setVisibility(8);
        }
        releaseScreenWl();
    }

    /* loaded from: classes.dex */
    public class LocaleLangContentObserver extends ContentObserver {
        public LocaleLangContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            Log.i("LocaleLangContentObserver", new StringBuilder().append(uri).toString());
            CountrySettingCacheProvider.getInstance().setAppLang(BaseActivity.context);
        }
    }

    /* loaded from: classes.dex */
    public class NetWorkContentObserver extends ContentObserver {
        public NetWorkContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            Log.i("NetWorkContentObserver", new StringBuilder().append(uri).toString());
            BaseActivity.this.initStatus();
            CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
            PLATFORM_CUSTOMER customer = SystemSettingCacheProvider.getInstance().getPlatformCustomer();
            if (HardwareStatusCacheProvider.getInstance().getNetworkStatus().isConnected()) {
                if ((CHARGE_PLATFORM.xcharge.equals(platform) && PLATFORM_CUSTOMER.anyo_private.equals(customer)) || ((CHARGE_PLATFORM.anyo.equals(platform) && !PLATFORM_CUSTOMER.anyo_svw.equals(customer)) || CHARGE_PLATFORM.xmsz.equals(platform))) {
                    String activity = Variate.getInstance().getActivity();
                    Uri uriCloud = Uri.parse("content://com.xcharge.charger.data.provider.device/hardware/cloud/connection");
                    new CloudContentObserver(BaseActivity.this.handler).onChange(selfChange, uriCloud);
                    BaseActivity.this.acquireScreenLock();
                    if (activity != null && activity.equals(Utils.getCurrentClassName(BaseActivity.context)) && BaseActivity.this.networkFaultDialog != null) {
                        Log.e("网络开", "onChange--" + Utils.getCurrentClassName(BaseActivity.context));
                        BaseActivity.this.networkFaultDialog.dismiss();
                    }
                }
            } else if ((!CHARGE_PLATFORM.xcharge.equals(platform) || !PLATFORM_CUSTOMER.anyo_private.equals(customer)) && ((!CHARGE_PLATFORM.anyo.equals(platform) || PLATFORM_CUSTOMER.anyo_svw.equals(customer)) && !CHARGE_PLATFORM.xmsz.equals(platform))) {
            } else {
                BaseActivity.this.showNetworkFault();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void showNetworkFault() {
        Variate.getInstance().setActivity(Utils.getCurrentClassName(context));
        if (Utils.getCurrentClassName(context).equals(NFCChargeInitActivity.class.getName()) || Utils.getCurrentClassName(context).equals(QrcodeActivity.class.getName())) {
            Utils.releaseScreenLock(context);
            if (this.networkFaultDialog != null && !this.networkFaultDialog.isShowing()) {
                Log.e("网络关", Utils.getCurrentClassName(context));
                this.networkFaultDialog.show();
            }
        }
    }

    /* loaded from: classes.dex */
    public class CloudContentObserver extends ContentObserver {
        public CloudContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            Log.i("CloudContentObserver", new StringBuilder().append(uri).toString());
            BaseActivity.this.initStatus();
            CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
            PLATFORM_CUSTOMER customer = SystemSettingCacheProvider.getInstance().getPlatformCustomer();
            if (ChargeStatusCacheProvider.getInstance().isCloudConnected()) {
                UIEventMessageProxy.getInstance().sendEvent(QrcodeActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, getClass().getName(), "resume", null);
                if ((CHARGE_PLATFORM.xcharge.equals(platform) && !PLATFORM_CUSTOMER.anyo_private.equals(customer)) || CHARGE_PLATFORM.ecw.equals(platform) || CHARGE_PLATFORM.yzx.equals(platform) || ((CHARGE_PLATFORM.anyo.equals(platform) && PLATFORM_CUSTOMER.anyo_svw.equals(customer)) || CHARGE_PLATFORM.ocpp.equals(platform))) {
                    if (Utils.getCurrentClassName(BaseActivity.context).equals(NFCChargeInitActivity.class.getName())) {
                        BaseActivity.this.startActivity(new Intent(BaseActivity.context, QrcodeActivity.class));
                        BaseActivity.this.finish();
                    }
                } else if ((CHARGE_PLATFORM.xcharge.equals(platform) && PLATFORM_CUSTOMER.anyo_private.equals(customer)) || ((CHARGE_PLATFORM.anyo.equals(platform) && !PLATFORM_CUSTOMER.anyo_svw.equals(customer)) || CHARGE_PLATFORM.xmsz.equals(platform))) {
                    String activity = Variate.getInstance().getActivity();
                    BaseActivity.this.acquireScreenLock();
                    if (activity != null && activity.equals(Utils.getCurrentClassName(BaseActivity.context)) && BaseActivity.this.cloudFaultDialog != null) {
                        Log.e("云端连接", "onChange--" + Utils.getCurrentClassName(BaseActivity.context));
                        BaseActivity.this.cloudFaultDialog.dismiss();
                    }
                }
            } else if ((CHARGE_PLATFORM.xcharge.equals(platform) && !PLATFORM_CUSTOMER.anyo_private.equals(customer)) || CHARGE_PLATFORM.ecw.equals(platform) || CHARGE_PLATFORM.yzx.equals(platform) || ((CHARGE_PLATFORM.anyo.equals(platform) && PLATFORM_CUSTOMER.anyo_svw.equals(customer)) || CHARGE_PLATFORM.ocpp.equals(platform))) {
                if (Utils.getCurrentClassName(BaseActivity.context).equals(QrcodeActivity.class.getName())) {
                    BaseActivity.this.startActivity(new Intent(BaseActivity.context, NFCChargeInitActivity.class));
                    BaseActivity.this.finish();
                }
            } else if ((!CHARGE_PLATFORM.xcharge.equals(platform) || !PLATFORM_CUSTOMER.anyo_private.equals(customer)) && ((!CHARGE_PLATFORM.anyo.equals(platform) || PLATFORM_CUSTOMER.anyo_svw.equals(customer)) && !CHARGE_PLATFORM.xmsz.equals(platform))) {
            } else {
                BaseActivity.this.showCloudFaultDialog();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void showCloudFaultDialog() {
        if (HardwareStatusCacheProvider.getInstance().getNetworkStatus().isConnected()) {
            Variate.getInstance().setActivity(Utils.getCurrentClassName(context));
            if (Utils.getCurrentClassName(context).equals(NFCChargeInitActivity.class.getName()) || Utils.getCurrentClassName(context).equals(QrcodeActivity.class.getName())) {
                Utils.releaseScreenLock(context);
                if (this.cloudFaultDialog != null && !this.cloudFaultDialog.isShowing()) {
                    Log.e("云端断开", Utils.getCurrentClassName(context));
                    this.cloudFaultDialog.show();
                }
            }
        }
    }

    /* loaded from: classes.dex */
    public class LockStatusContentObserver extends ContentObserver {
        public LockStatusContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            Log.i("LockStatusContentObserver", new StringBuilder().append(uri).toString());
            BaseActivity.this.initStatus();
        }
    }

    /* loaded from: classes.dex */
    public class PortEnableContentObserver extends ContentObserver {
        public PortEnableContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            Log.i("PortEnableContentObserver", new StringBuilder().append(uri).toString());
            boolean isPortEnable = ChargeStatusCacheProvider.getInstance().getPortSwitch("1");
            if (!isPortEnable) {
                if (Utils.getCurrentClassName(BaseActivity.context).equals(NFCChargeInitActivity.class.getName()) || Utils.getCurrentClassName(BaseActivity.context).equals(QrcodeActivity.class.getName())) {
                    BaseActivity.this.startActivity(new Intent(BaseActivity.context, EnableActivity.class));
                    BaseActivity.this.finish();
                }
            } else if (Utils.getCurrentClassName(BaseActivity.context).equals(EnableActivity.class.getName())) {
                Utils.skipNfcQrcode(BaseActivity.context);
                BaseActivity.this.finish();
            }
        }
    }

    /* loaded from: classes.dex */
    public class RadarStatusContentObserver extends ContentObserver {
        public RadarStatusContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            Log.i("RadarStatusContentObserver", new StringBuilder().append(uri).toString());
            BaseActivity.this.initStatus();
        }
    }

    /* loaded from: classes.dex */
    public class FaultStatusContentObserver extends ContentObserver {
        public FaultStatusContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        @SuppressLint({"NewApi"})
        public synchronized void onChange(boolean selfChange, Uri uri) {
            Log.i("FaultStatusContentObserver", new StringBuilder().append(uri).toString());
            BaseActivity.this.initStatus();
            ErrorCode error = HardwareStatusCacheProvider.getInstance().getPort("1").getDeviceError();
            if (error.getCode() == 200) {
                BaseActivity.deviceError = error;
                String activity = Variate.getInstance().getActivity();
                if (activity != null && activity.equals(Utils.getCurrentClassName(BaseActivity.context)) && BaseActivity.this.exceptionFaultDialog != null) {
                    Log.e("异常否", "onChange--" + Utils.getCurrentClassName(BaseActivity.context));
                    BaseActivity.this.exceptionFaultDialog.dismiss();
                    if (BaseActivity.this.nowChargeMode != null && BaseActivity.this.nowChargeMode.getMode() != CHARGE_MODE.normal_charge.getMode()) {
                        if (PLATFORM_CUSTOMER.anyo_private.equals(SystemSettingCacheProvider.getInstance().getPlatformCustomer())) {
                            BaseActivity.this.acquireScreenLock();
                        } else {
                            BaseActivity.this.acquireScreenLockAndKeep();
                        }
                    } else {
                        BaseActivity.this.acquireScreenLock();
                    }
                }
            } else if (error.getCode() >= 30010 && error.getCode() <= 30018) {
                Log.e("ErrorCode", new StringBuilder(String.valueOf(HardwareStatusCacheProvider.getInstance().getPort("1").getDeviceError().getCode())).toString());
                Utils.releaseScreenLock(BaseActivity.context);
                Variate.getInstance().setActivity(Utils.getCurrentClassName(BaseActivity.context));
                if (!Utils.getCurrentClassName(BaseActivity.context).equals(NFCChargeBalanceActivity.class.getName()) && !Utils.getCurrentClassName(BaseActivity.context).equals(ChargeCostActivity.class.getName()) && !Utils.getCurrentClassName(BaseActivity.context).equals(SetActivity.class.getName())) {
                    BaseActivity.this.showExceptionFaltDialog(error);
                }
            } else {
                BaseActivity.deviceError = error;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void showExceptionFaltDialog(ErrorCode error) {
        if (error.getCode() != deviceError.getCode()) {
            deviceError = error;
            if (this.exceptionFaultDialog != null) {
                Log.e("异常是", Utils.getCurrentClassName(context));
                this.exceptionFaultDialog.error = error.getCode();
                this.exceptionFaultDialog.visibility = "GONE";
                this.exceptionFaultDialog.initView();
                this.exceptionFaultDialog.show();
            }
        }
    }

    /* loaded from: classes.dex */
    public class NfcStatusContentObserver extends ContentObserver {
        public NfcStatusContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        @SuppressLint({"NewApi"})
        public void onChange(boolean selfChange, Uri uri) {
            Log.i("NfcStatusContentObserver", new StringBuilder().append(uri).toString());
            if (uri.getPath().contains("ports/1/nfc/")) {
                String lastSegment = uri.getLastPathSegment();
                NFC nfcStatus = new NFC().fromJson(lastSegment);
                if (nfcStatus.isArrived()) {
                    if (BaseActivity.this.iv_nfc_reading != null) {
                        BaseActivity.this.iv_nfc_reading.setVisibility(0);
                        if (Utils.getCurrentClassName(BaseActivity.context).equals(OnlineParkBusyHintActivity.class.getName())) {
                            Utils.skipNfcQrcode(BaseActivity.context);
                            BaseActivity.this.finish();
                        }
                    }
                    Utils.releaseScreenLock(BaseActivity.context);
                    if (200 == nfcStatus.getLatestError().getCode() && Utils.getCurrentClassName(BaseActivity.context).equals(TestChargeActivity.class.getName())) {
                        BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_test_normal));
                        return;
                    }
                } else if (BaseActivity.this.iv_nfc_reading != null) {
                    BaseActivity.this.iv_nfc_reading.setVisibility(8);
                }
                boolean newHandleStatus = nfcStatus.isHandleStatus();
                boolean oldHandleStatus = Variate.getInstance().isHandleStatus();
                if (newHandleStatus != oldHandleStatus) {
                    if (!newHandleStatus && oldHandleStatus) {
                        Log.e("NFC刷卡ErrorCode", new StringBuilder(String.valueOf(nfcStatus.getLatestError().getCode())).toString());
                        ErrorCode error = nfcStatus.getLatestError();
                        switch (error.getCode()) {
                            case ErrorCode.EC_NFC_ERROR /* 40000 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_error));
                                break;
                            case ErrorCode.EC_NFC_UNRECOGNIZED_CARD /* 40001 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_card_invalid));
                                break;
                            case ErrorCode.EC_NFC_UNAVAILABLE_KEYSEED /* 40002 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_card_invalid));
                                break;
                            case ErrorCode.EC_NFC_INVALID_MANAGE_CARD_DATA /* 40003 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_invalid_manage_card_data));
                                break;
                            case ErrorCode.EC_NFC_SET_FAIL /* 40004 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_set_fail));
                                break;
                            case ErrorCode.EC_NFC_SIGN_ERROR /* 40005 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_card_invalid));
                                break;
                            case ErrorCode.EC_NFC_BIND_CARD_FAIL /* 40006 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_bind_card_fail));
                                break;
                            case ErrorCode.EC_NFC_SET_REFUSE /* 40007 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_system_busy_repeat));
                                break;
                            case ErrorCode.EC_NFC_CHARGE_REFUSE /* 40008 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_system_busy_repeat));
                                break;
                            case ErrorCode.EC_NFC_SCAN_REFUSE /* 40009 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_scan_refuse));
                                break;
                            case ErrorCode.EC_NFC_INVALID_PORT /* 40010 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_invalid_port));
                                break;
                            case ErrorCode.EC_NFC_UNPAID_BILL /* 40011 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_unpaid_bill));
                                break;
                            case ErrorCode.EC_NFC_CARD_RESERVED /* 40012 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_card_reserved));
                                break;
                            case ErrorCode.EC_NFC_CARD_RESERVE_FAIL /* 40013 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_init_fail));
                                break;
                            case ErrorCode.EC_NFC_CARD_RELEASE_FAIL /* 40014 */:
                                if (Utils.getCurrentClassName(BaseActivity.context).equals(NFCChargingActivity.class.getName())) {
                                    BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_end_charge_failure));
                                    break;
                                } else {
                                    BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_card_fail_repeat));
                                    break;
                                }
                            case ErrorCode.EC_NFC_CARD_AUTH_FAIL /* 40015 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_card_auth_fail, new Object[]{nfcStatus.getLatestCardNo()}));
                                break;
                            case ErrorCode.EC_NFC_CARD_UNPAID_CONSUME_FAIL /* 40017 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_card_unpaid_consume_fail));
                                break;
                            case ErrorCode.EC_NFC_CARD_UNPAID_BALANCE_NOT_ENOUGH /* 40018 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_card_unpaid_balance_not_enough));
                                break;
                            case ErrorCode.EC_NFC_CARD_CONSUME_FAIL /* 40019 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_card_consume_fail));
                                break;
                            case ErrorCode.EC_NFC_CARD_BALANCE_NOT_ENOUGH /* 40020 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_card_balance_not_enough));
                                break;
                            case ErrorCode.EC_NFC_RECOVERY_SIGN_FAIL /* 40021 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_recovery_sign_fail));
                                break;
                            case ErrorCode.EC_NFC_REWRITED_BALANCE /* 40022 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_card_invalid));
                                break;
                            case ErrorCode.EC_NFC_NOT_GROUP_MODE /* 40023 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_not_group_mode));
                                break;
                            case ErrorCode.EC_NFC_NOT_PERSONAL_MODE /* 40024 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_not_personal_mode));
                                break;
                            case ErrorCode.EC_NFC_UNAVAILABLE_CLOUD /* 40025 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_unavailable_cloud));
                                break;
                            case ErrorCode.EC_NFC_NOT_PERMIT_SWIPE /* 40026 */:
                                if ((BaseActivity.this.wakeupAdDialog != null && BaseActivity.this.wakeupAdDialog.isShowing()) || Utils.getCurrentClassName(BaseActivity.context).equals(ScanAdActivity.class.getName()) || Utils.getCurrentClassName(BaseActivity.context).equals(PullAdActivity.class.getName())) {
                                    BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_not_permit_swipe_advert));
                                    break;
                                } else if ((BaseActivity.this.exceptionFaultDialog != null && BaseActivity.this.exceptionFaultDialog.isShowing()) || (BaseActivity.this.networkFaultDialog != null && BaseActivity.this.networkFaultDialog.isShowing())) {
                                    BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_invalid_port));
                                    break;
                                } else if (Utils.getCurrentClassName(BaseActivity.context).equals(UpgradeActivity.class.getName())) {
                                    BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_not_permit_swipe_upgrade));
                                    break;
                                } else if (Utils.getCurrentClassName(BaseActivity.context).equals(NFCChargeWaittingStartActivity.class.getName()) || Utils.getCurrentClassName(BaseActivity.context).equals(NFCChargingActivity.class.getName()) || Utils.getCurrentClassName(BaseActivity.context).equals(NFCChargeFinActivity.class.getName()) || Utils.getCurrentClassName(BaseActivity.context).equals(NFCChargeBalanceActivity.class.getName()) || Utils.getCurrentClassName(BaseActivity.context).equals(WaittingStartChargeActivity.class.getName()) || Utils.getCurrentClassName(BaseActivity.context).equals(ChargingActivity.class.getName()) || Utils.getCurrentClassName(BaseActivity.context).equals(ChargeCompleteActivity.class.getName()) || Utils.getCurrentClassName(BaseActivity.context).equals(ChargeDelayedActivity.class.getName()) || Utils.getCurrentClassName(BaseActivity.context).equals(ChargeCostActivity.class.getName())) {
                                    BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_not_permit_swipe_charge));
                                    break;
                                } else if (Utils.getCurrentClassName(BaseActivity.context).equals(NFCConfigPersnalCardActivity.class.getName())) {
                                    BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_not_permit_swipe_bind));
                                    break;
                                } else {
                                    BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_not_permit_swipe));
                                    break;
                                }
                                break;
                            case ErrorCode.EC_NFC_READ_FAILED /* 40027 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_read_failed));
                                break;
                            case ErrorCode.EC_NFC_NOT_INIT_CHARGE_CARD /* 40028 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_not_init_charge_card));
                                break;
                            case ErrorCode.EC_NFC_INIT_FAIL /* 40029 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_init_fail));
                                break;
                            case ErrorCode.EC_NFC_SWIPE_PROCESSING /* 40030 */:
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_swipe_processing, new Object[]{nfcStatus.getLatestCardNo()}));
                                break;
                        }
                        Log.e("NFC_OPR_TYPE", new StringBuilder().append(nfcStatus.getLatestOprType()).toString());
                        Log.e("NFC_CARD_TYPE", new StringBuilder().append(nfcStatus.getLatestCardType()).toString());
                        if (200 == error.getCode() && nfcStatus.getLatestOprType() != null) {
                            if (NFC_OPR_TYPE.set.equals(nfcStatus.getLatestOprType())) {
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_card_set));
                                Log.e("WORK_MODE", new StringBuilder().append(LocalSettingCacheProvider.getInstance().getChargeSetting().getWorkMode()).toString());
                                if (WORK_MODE.personal.equals(LocalSettingCacheProvider.getInstance().getChargeSetting().getWorkMode()) && NFC_CARD_TYPE.M2.equals(nfcStatus.getLatestCardType()) && !Utils.getCurrentClassName(BaseActivity.context).equals(NFCConfigPersnalCardActivity.class.getName())) {
                                    BaseActivity.this.startActivity(new Intent(BaseActivity.context, NFCConfigPersnalCardActivity.class));
                                    BaseActivity.this.finish();
                                }
                            } else if (NFC_OPR_TYPE.bind.equals(nfcStatus.getLatestOprType())) {
                                BaseActivity.showSmallDialog(BaseActivity.this.getString(R.string.nfc_card_bind));
                            }
                        }
                    }
                    Variate.getInstance().setHandleStatus(newHandleStatus);
                }
            }
        }
    }

    /* loaded from: classes.dex */
    public class UpgradeContentObserver extends ContentObserver {
        public UpgradeContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            Log.i("UpgradeContentObserver", new StringBuilder().append(uri).toString());
            if (SoftwareStatusCacheProvider.getInstance().getUpgradeProgress() != null) {
                if ((Utils.getCurrentClassName(BaseActivity.context).equals(NFCChargeInitActivity.class.getName()) || Utils.getCurrentClassName(BaseActivity.context).equals(QrcodeActivity.class.getName())) && SoftwareStatusCacheProvider.getInstance().getUpgradeProgress().getStage() != null) {
                    if ((1 == SoftwareStatusCacheProvider.getInstance().getUpgradeProgress().getStatus() || 7 == SoftwareStatusCacheProvider.getInstance().getUpgradeProgress().getStatus()) && !Utils.getCurrentClassName(BaseActivity.context).equals(UpgradeActivity.class.getName())) {
                        BaseActivity.this.startActivity(new Intent(BaseActivity.context, UpgradeActivity.class));
                        BaseActivity.this.finish();
                    }
                }
            }
        }
    }

    /* loaded from: classes.dex */
    public class PayContentObserver extends ContentObserver {
        public PayContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            Log.i("PayContentObserver", new StringBuilder().append(uri).toString());
            String billId = uri.getLastPathSegment();
            ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(billId);
            NFC portNfCStatus = HardwareStatusCacheProvider.getInstance().getPortNFCStatus("1");
            if (chargeBill != null && portNfCStatus != null) {
                String latestCardNo = portNfCStatus.getLatestCardNo();
                String user_type = chargeBill.getUser_type();
                String user_code = chargeBill.getUser_code();
                CHARGE_INIT_TYPE init_type = chargeBill.getInit_type();
                if ((CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.U2).equals(user_type) && latestCardNo.equals(user_code)) {
                    if (!Utils.getCurrentClassName(BaseActivity.context).equals(NFCChargeInitActivity.class.getName()) && !Utils.getCurrentClassName(BaseActivity.context).equals(QrcodeActivity.class.getName())) {
                        return;
                    }
                    BaseActivity.this.showErrorNfc(R.string.nfc_card_unpaid_consume_ok_one, chargeBill);
                } else if ((CHARGE_USER_TYPE.nfc + "." + NFC_CARD_TYPE.U3).equals(user_type) && billId.equals(Variate.getInstance().getChargeId())) {
                    if (Utils.getCurrentClassName(BaseActivity.context).equals(NFCChargeBalanceActivity.class.getName())) {
                        BaseActivity.this.initView();
                    }
                } else if (!CHARGE_INIT_TYPE.nfc.equals(init_type) && billId.equals(Variate.getInstance().getChargeId()) && Utils.getCurrentClassName(BaseActivity.context).equals(ChargeCostActivity.class.getName())) {
                    BaseActivity.this.initView();
                }
            }
        }
    }

    /* loaded from: classes.dex */
    public class CompanyContentObserver extends ContentObserver {
        public CompanyContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            Log.i("CompanyContentObserver", new StringBuilder().append(uri).toString());
            BaseActivity.this.initStatus();
        }
    }

    /* loaded from: classes.dex */
    public class PluginContentObserver extends ContentObserver {
        public PluginContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            Log.i("PluginContentObserver", new StringBuilder().append(uri).toString());
            if (Utils.getCurrentClassName(BaseActivity.context).equals(NFCChargeInitActivity.class.getName()) || Utils.getCurrentClassName(BaseActivity.context).equals(QrcodeActivity.class.getName())) {
                Utils.releaseScreenLock(BaseActivity.context);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showErrorNfc(int resId, ChargeBill chargeBill) {
        String time = Utils.formatTime(chargeBill.getStop_time() - chargeBill.getStart_time());
        String power = CountrySettingCacheProvider.getInstance().format(TWODP, Double.valueOf(chargeBill.getTotal_power()));
        String totalFee = CountrySettingCacheProvider.getInstance().format(TWODP, Double.valueOf(chargeBill.getTotal_fee() / 100.0d));
        String user_balance = CountrySettingCacheProvider.getInstance().format(TWODP, Double.valueOf(chargeBill.getUser_balance() / 100.0d));
        if (this.moneyLoadingDialog == null) {
            this.moneyLoadingDialog = LoadingDialog.createDialog(this, getString(resId, new Object[]{time, power, totalFee, user_balance}));
        } else {
            this.moneyLoadingDialog.changeLoadingText(getString(resId, new Object[]{time, power, totalFee, user_balance}));
        }
        if (this.moneyLoadingDialog != null && !this.moneyLoadingDialog.isShowing()) {
            this.moneyLoadingDialog.show();
        }
        Message msg = new Message();
        msg.what = MSG_NFC_DISMISS_MONEY;
        this.mRadarHandler.sendMessageDelayed(msg, 5000L);
    }

    public static void showSmallDialog(String text) {
        if (smallDialog == null) {
            smallDialog = SmallDialog.createDialog(context, text, 2);
            smallDialog.setCancelable(false);
        } else {
            smallDialog.init(text, 2);
        }
        if (smallDialog != null && !smallDialog.isShowing()) {
            smallDialog.show();
        }
    }

    public void acquireScreenLock() {
        long timeout = RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalStandby() * 1000;
        this.handlerTimer.stopTimer(4098);
        holdScreenWl();
        this.handlerTimer.startTimer(timeout, 4098, null);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void acquireScreenLockAndKeep() {
        this.handlerTimer.stopTimer(4098);
        holdScreenWl();
    }

    private void initScreenWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(ChargeStopCondition.TYPE_POWER);
        this.screenWl = pm.newWakeLock(268435466, getClass().getName());
        this.screenWl.setReferenceCounted(false);
    }

    protected void keepScreenOn() {
        getWindow().setFlags(128, 128);
    }

    protected void releaseScreenWl() {
        this.handlerTimer.stopTimer(4098);
        if (this.screenWlCnt > 0) {
            this.screenWlCnt--;
            this.screenWl.release();
        }
    }

    protected void holdScreenWl() {
        if (this.screenWlCnt <= 0 && this.screenWl != null) {
            this.screenWl.acquire();
            this.screenWlCnt++;
        }
    }
}
