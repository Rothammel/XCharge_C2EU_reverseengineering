package com.xcharge.charger.ui.c2.activity.fault;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.xcharge.charger.R;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.data.bean.device.MobileNet;
import com.xcharge.charger.data.bean.device.Network;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.bean.type.PARK_STATUS;
import com.xcharge.charger.data.bean.type.PLATFORM_CUSTOMER;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.ui.api.UICtrlMessageProxy;
import com.xcharge.charger.ui.api.UIEventMessageProxy;
import com.xcharge.charger.ui.api.bean.UICtrlMessage;
import com.xcharge.charger.ui.api.bean.UIEventMessage;
import com.xcharge.charger.ui.c2.activity.BaseActivity;
import com.xcharge.charger.ui.c2.activity.HomeActivity;
import com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargeFinActivity;
import com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargeInitActivity;
import com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargeWaittingStartActivity;
import com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargingActivity;
import com.xcharge.charger.ui.c2.activity.charge.online.ChargeDelayedActivity;
import com.xcharge.charger.ui.c2.activity.charge.online.ChargingActivity;
import com.xcharge.charger.ui.c2.activity.charge.online.QrcodeActivity;
import com.xcharge.charger.ui.c2.activity.charge.online.WaittingStartChargeActivity;
import com.xcharge.charger.ui.c2.activity.data.UiBackgroundColorContentObserver;
import com.xcharge.charger.ui.c2.activity.data.Variate;
import com.xcharge.charger.ui.c2.activity.test.SetActivity;
import com.xcharge.charger.ui.c2.activity.test.TestChargeActivity;
import com.xcharge.charger.ui.c2.activity.utiles.Utils;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* loaded from: classes.dex */
public class BaseDialog extends Dialog {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$PARK_STATUS;
    private CloudDialogContentObserver cloudDialogContentObserver;
    private CompanyContentObserver companyContentObserver;
    public Context context;
    private long firstTime;
    boolean isOnKeyLongPress;
    protected ImageView iv_cloud;
    protected ImageView iv_company;
    protected ImageView iv_lock;
    protected ImageView iv_network;
    protected ImageView iv_nfc;
    protected ImageView iv_nfc_reading;
    protected ImageView iv_park;
    protected ImageView iv_signal;
    public ImageView iv_status_one;
    protected LinearLayout ll_base;
    private LockDialogContentObserver lockDialogContentObserver;
    private long lockTimestamp;
    private NetWorkDialogContentObserver netWorkDialogContentObserver;
    private NfcDialogContentObserver nfcDialogContentObserver;
    private ParkDialogContentObserver parkDialogContentObserver;
    private RadarDialogContentObserver radarDialogContentObserver;
    public TextView tv_bottom;
    public TextView tv_status_one;
    public TextView tv_status_two;
    public UiBackgroundColorContentObserver uiBackgroundColorContentObserver;

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

    public BaseDialog(Context context, int style) {
        super(context, style);
        this.lockTimestamp = 0L;
        this.firstTime = 0L;
        this.isOnKeyLongPress = false;
        this.context = context;
        getWindow().setType(2003);
        if (!PLATFORM_CUSTOMER.anyo_private.equals(SystemSettingCacheProvider.getInstance().getPlatformCustomer())) {
            getWindow().setFlags(128, 128);
        }
    }

    public void initView() {
        LayoutInflater inflater = LayoutInflater.from(this.context);
        View view = inflater.inflate(R.layout.activity_base_status, (ViewGroup) null);
        setContentView(view);
        this.ll_base = (LinearLayout) findViewById(R.id.ll_base);
        Utils.customizeUiBgColor(this.ll_base);
        this.iv_nfc = (ImageView) findViewById(R.id.iv_nfc);
        this.iv_nfc.setOnClickListener(new View.OnClickListener() { // from class: com.xcharge.charger.ui.c2.activity.fault.BaseDialog.1
            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                long secondTime = System.currentTimeMillis();
                if (secondTime - BaseDialog.this.firstTime > 2000) {
                    BaseDialog.this.firstTime = secondTime;
                } else {
                    Utils.screenshot(getClass().getName(), BaseDialog.this.getWindow().getDecorView());
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
            Port port = HardwareStatusCacheProvider.getInstance().getPort("1");
            if (port != null) {
                switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$PARK_STATUS()[port.getParkStatus().getParkStatus().ordinal()]) {
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
        this.iv_nfc_reading = (ImageView) findViewById(R.id.iv_nfc_reading);
        this.iv_company = (ImageView) view.findViewById(R.id.iv_company);
        if (SystemSettingCacheProvider.getInstance().isUsingXChargeLogo()) {
            this.iv_company.setImageResource(R.drawable.ic_company);
        } else {
            String companyResouce = RemoteSettingCacheProvider.getInstance().getCompanyResouce();
            if (TextUtils.isEmpty(companyResouce) || !Utils.fileIsExists(companyResouce)) {
                getCompanyLogo();
            } else {
                Utils.loadImage(companyResouce, this.iv_company, new ImageLoadingListener() { // from class: com.xcharge.charger.ui.c2.activity.fault.BaseDialog.2
                    @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                    public void onLoadingStarted(String arg0, View arg1) {
                    }

                    @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                    public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
                        BaseDialog.this.getCompanyLogo();
                    }

                    @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                    public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
                    }

                    @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                    public void onLoadingCancelled(String arg0, View arg1) {
                    }
                }, this.context);
            }
        }
        this.iv_company.setOnClickListener(new View.OnClickListener() { // from class: com.xcharge.charger.ui.c2.activity.fault.BaseDialog.3
            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                if (Utils.getCurrentClassName(BaseDialog.this.context).equals(NFCChargeInitActivity.class.getName()) || Utils.getCurrentClassName(BaseDialog.this.context).equals(QrcodeActivity.class.getName())) {
                    BaseDialog.this.dismiss();
                    Intent intent = new Intent(BaseDialog.this.context, SetActivity.class);
                    intent.setFlags(268435456);
                    BaseDialog.this.context.startActivity(intent);
                }
            }
        });
        this.iv_status_one = (ImageView) findViewById(R.id.iv_status_one);
        this.tv_status_one = (TextView) findViewById(R.id.tv_status_one);
        this.tv_status_two = (TextView) findViewById(R.id.tv_status_two);
        this.tv_bottom = (TextView) findViewById(R.id.tv_bottom);
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

    /* loaded from: classes.dex */
    public class NetWorkDialogContentObserver extends ContentObserver {
        public NetWorkDialogContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            BaseDialog.this.initView();
        }
    }

    /* loaded from: classes.dex */
    public class CloudDialogContentObserver extends ContentObserver {
        public CloudDialogContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            BaseDialog.this.initView();
        }
    }

    /* loaded from: classes.dex */
    public class LockDialogContentObserver extends ContentObserver {
        public LockDialogContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            BaseDialog.this.initView();
        }
    }

    /* loaded from: classes.dex */
    public class ParkDialogContentObserver extends ContentObserver {
        public ParkDialogContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            BaseDialog.this.initView();
        }
    }

    /* loaded from: classes.dex */
    public class RadarDialogContentObserver extends ContentObserver {
        public RadarDialogContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            BaseDialog.this.initView();
        }
    }

    /* loaded from: classes.dex */
    public class NfcDialogContentObserver extends ContentObserver {
        public NfcDialogContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        @SuppressLint({"NewApi"})
        public void onChange(boolean selfChange) {
            if (HardwareStatusCacheProvider.getInstance().getPortNFCStatus("1").isArrived()) {
                if (BaseDialog.this.iv_nfc_reading != null) {
                    BaseDialog.this.iv_nfc_reading.setVisibility(0);
                }
            } else if (BaseDialog.this.iv_nfc_reading != null) {
                BaseDialog.this.iv_nfc_reading.setVisibility(8);
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
            BaseDialog.this.initView();
        }
    }

    @Override // android.app.Dialog
    public void show() {
        super.show();
        if (CHARGE_PLATFORM.ocpp.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
            if (!Utils.getCurrentClassName(this.context).equals(NFCChargingActivity.class.getName()) && !Utils.getCurrentClassName(this.context).equals(NFCChargeWaittingStartActivity.class.getName()) && !Utils.getCurrentClassName(this.context).equals(NFCChargeFinActivity.class.getName()) && ((!Utils.getCurrentClassName(this.context).equals(ChargeDelayedActivity.class.getName()) || !Variate.getInstance().isNFC) && !Utils.getCurrentClassName(this.context).equals(ChargingActivity.class.getName()))) {
                Utils.setPermitNFC(false, false, false, false);
            }
        } else if (!Utils.getCurrentClassName(this.context).equals(NFCChargingActivity.class.getName()) && !Utils.getCurrentClassName(this.context).equals(NFCChargeWaittingStartActivity.class.getName()) && !Utils.getCurrentClassName(this.context).equals(NFCChargeFinActivity.class.getName()) && (!Utils.getCurrentClassName(this.context).equals(ChargeDelayedActivity.class.getName()) || !Variate.getInstance().isNFC)) {
            Utils.setPermitNFC(false, false, false, false);
        }
        initView();
        this.netWorkDialogContentObserver = new NetWorkDialogContentObserver(new Handler());
        this.cloudDialogContentObserver = new CloudDialogContentObserver(new Handler());
        this.lockDialogContentObserver = new LockDialogContentObserver(new Handler());
        this.parkDialogContentObserver = new ParkDialogContentObserver(new Handler());
        this.radarDialogContentObserver = new RadarDialogContentObserver(new Handler());
        this.nfcDialogContentObserver = new NfcDialogContentObserver(new Handler());
        this.companyContentObserver = new CompanyContentObserver(new Handler());
        this.uiBackgroundColorContentObserver = new UiBackgroundColorContentObserver(this.ll_base, new Handler());
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor(String.valueOf(Network.class.getSimpleName()) + MqttTopic.TOPIC_LEVEL_SEPARATOR), true, this.netWorkDialogContentObserver);
        this.context.getContentResolver().registerContentObserver(ChargeStatusCacheProvider.getInstance().getUriFor("cloud/connection"), false, this.cloudDialogContentObserver);
        this.context.getContentResolver().registerContentObserver(ChargeStatusCacheProvider.getInstance().getUriFor("ports/lock/status/1"), false, this.lockDialogContentObserver);
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor("ports/1"), false, this.parkDialogContentObserver);
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor("ports/radar/status/1"), false, this.radarDialogContentObserver);
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor("ports/1/nfc"), false, this.nfcDialogContentObserver);
        this.context.getContentResolver().registerContentObserver(RemoteSettingCacheProvider.getInstance().getUriFor("resource/company"), true, this.companyContentObserver);
        this.context.getContentResolver().registerContentObserver(SystemSettingCacheProvider.getInstance().getUriFor("uiBackgroundColor"), false, this.uiBackgroundColorContentObserver);
    }

    @Override // android.app.Dialog, android.content.DialogInterface
    public void dismiss() {
        super.dismiss();
        Utils.anewSetPermitNFC(this.context);
        if (this.netWorkDialogContentObserver != null && this.cloudDialogContentObserver != null && this.lockDialogContentObserver != null && this.parkDialogContentObserver != null && this.radarDialogContentObserver != null && this.nfcDialogContentObserver != null && this.companyContentObserver != null && this.uiBackgroundColorContentObserver != null) {
            this.context.getContentResolver().unregisterContentObserver(this.netWorkDialogContentObserver);
            this.context.getContentResolver().unregisterContentObserver(this.cloudDialogContentObserver);
            this.context.getContentResolver().unregisterContentObserver(this.lockDialogContentObserver);
            this.context.getContentResolver().unregisterContentObserver(this.parkDialogContentObserver);
            this.context.getContentResolver().unregisterContentObserver(this.radarDialogContentObserver);
            this.context.getContentResolver().unregisterContentObserver(this.nfcDialogContentObserver);
            this.context.getContentResolver().unregisterContentObserver(this.companyContentObserver);
            this.context.getContentResolver().unregisterContentObserver(this.uiBackgroundColorContentObserver);
        }
        if (this.iv_nfc_reading != null) {
            this.iv_nfc_reading.setVisibility(8);
        }
    }

    @Override // android.app.Dialog
    public void onBackPressed() {
        super.onBackPressed();
        if (System.currentTimeMillis() - this.lockTimestamp > 2000) {
            DCAPProxy.getInstance().gunLockCtrl("1", 0, LOCK_STATUS.unlock);
            this.lockTimestamp = System.currentTimeMillis();
        }
        if (Utils.getCurrentClassName(this.context).equals(NFCChargeWaittingStartActivity.class.getName())) {
            UIEventMessageProxy.getInstance().sendEvent("com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargeWaittingStartActivity", "key", null, "com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargeWaittingStartActivity", "up", null);
        } else if (Utils.getCurrentClassName(this.context).equals(WaittingStartChargeActivity.class.getName())) {
            UIEventMessageProxy.getInstance().sendEvent("com.xcharge.charger.ui.c2.activity.charge.online.WaittingStartChargeActivity", "key", null, "com.xcharge.charger.ui.c2.activity.charge.online.WaittingStartChargeActivity", "up", null);
        } else if (Utils.getCurrentClassName(this.context).equals(TestChargeActivity.class.getName())) {
            UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, "testHome", UICtrlMessage.OPR_SKIP, null);
        }
    }

    @Override // android.app.Dialog, android.view.KeyEvent.Callback
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

    @Override // android.app.Dialog, android.view.KeyEvent.Callback
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4 && event.getRepeatCount() == 10) {
            this.isOnKeyLongPress = true;
            if (!Utils.getCurrentClassName(this.context).equals(TestChargeActivity.class.getName()) && !Utils.getCurrentClassName(this.context).equals(HomeActivity.class.getName())) {
                UIEventMessageProxy.getInstance().sendEvent(getClass().getName(), "key", null, getClass().getName(), "down", null);
            }
        } else if (keyCode == 4) {
            event.getRepeatCount();
        }
        return true;
    }
}
