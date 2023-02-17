package com.xcharge.charger.p006ui.p009c2.activity.fault;

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
import com.xcharge.charger.C0221R;
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
import com.xcharge.charger.p006ui.api.UICtrlMessageProxy;
import com.xcharge.charger.p006ui.api.UIEventMessageProxy;
import com.xcharge.charger.p006ui.api.bean.UICtrlMessage;
import com.xcharge.charger.p006ui.api.bean.UIEventMessage;
import com.xcharge.charger.p006ui.p009c2.activity.BaseActivity;
import com.xcharge.charger.p006ui.p009c2.activity.HomeActivity;
import com.xcharge.charger.p006ui.p009c2.activity.charge.nfc.NFCChargeFinActivity;
import com.xcharge.charger.p006ui.p009c2.activity.charge.nfc.NFCChargeInitActivity;
import com.xcharge.charger.p006ui.p009c2.activity.charge.nfc.NFCChargeWaittingStartActivity;
import com.xcharge.charger.p006ui.p009c2.activity.charge.nfc.NFCChargingActivity;
import com.xcharge.charger.p006ui.p009c2.activity.charge.online.ChargeDelayedActivity;
import com.xcharge.charger.p006ui.p009c2.activity.charge.online.ChargingActivity;
import com.xcharge.charger.p006ui.p009c2.activity.charge.online.QrcodeActivity;
import com.xcharge.charger.p006ui.p009c2.activity.charge.online.WaittingStartChargeActivity;
import com.xcharge.charger.p006ui.p009c2.activity.data.UiBackgroundColorContentObserver;
import com.xcharge.charger.p006ui.p009c2.activity.data.Variate;
import com.xcharge.charger.p006ui.p009c2.activity.test.SetActivity;
import com.xcharge.charger.p006ui.p009c2.activity.test.TestChargeActivity;
import com.xcharge.charger.p006ui.p009c2.activity.utiles.Utils;
import java.util.HashMap;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* renamed from: com.xcharge.charger.ui.c2.activity.fault.BaseDialog */
public class BaseDialog extends Dialog {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$PARK_STATUS;
    private CloudDialogContentObserver cloudDialogContentObserver;
    private CompanyContentObserver companyContentObserver;
    public Context context;
    /* access modifiers changed from: private */
    public long firstTime = 0;
    boolean isOnKeyLongPress = false;
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
    private long lockTimestamp = 0;
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
            iArr = new int[PARK_STATUS.values().length];
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

    public BaseDialog(Context context2, int style) {
        super(context2, style);
        this.context = context2;
        getWindow().setType(2003);
        if (!PLATFORM_CUSTOMER.anyo_private.equals(SystemSettingCacheProvider.getInstance().getPlatformCustomer())) {
            getWindow().setFlags(128, 128);
        }
    }

    public void initView() {
        View view = LayoutInflater.from(this.context).inflate(C0221R.layout.activity_base_status, (ViewGroup) null);
        setContentView(view);
        this.ll_base = (LinearLayout) findViewById(C0221R.C0223id.ll_base);
        Utils.customizeUiBgColor(this.ll_base);
        this.iv_nfc = (ImageView) findViewById(C0221R.C0223id.iv_nfc);
        this.iv_nfc.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                long secondTime = System.currentTimeMillis();
                if (secondTime - BaseDialog.this.firstTime > 2000) {
                    BaseDialog.this.firstTime = secondTime;
                } else {
                    Utils.screenshot(getClass().getName(), BaseDialog.this.getWindow().getDecorView());
                }
            }
        });
        this.iv_network = (ImageView) findViewById(C0221R.C0223id.iv_network);
        Network network = HardwareStatusCacheProvider.getInstance().getNetworkStatus();
        if (!network.isConnected()) {
            this.iv_network.setImageResource(C0221R.C0222drawable.ic_net_icon_disconn);
        } else if (TextUtils.isEmpty(network.getActive())) {
            this.iv_network.setImageResource(C0221R.C0222drawable.ic_net_icon_disconn);
        } else if (Network.NETWORK_TYPE_MOBILE.equals(network.getActive())) {
            MobileNet mobileNet = HardwareStatusCacheProvider.getInstance().getMobileNetStatus();
            if (mobileNet != null) {
                if ("2G".equals(mobileNet.getType())) {
                    this.iv_network.setImageResource(C0221R.C0222drawable.ic_net_icon_2g);
                } else if ("3G".equals(mobileNet.getType())) {
                    this.iv_network.setImageResource(C0221R.C0222drawable.ic_net_icon_3g);
                } else if ("4G".equals(mobileNet.getType())) {
                    this.iv_network.setImageResource(C0221R.C0222drawable.ic_net_icon_4g);
                }
            }
        } else if (Network.NETWORK_TYPE_ETHERNET.equals(network.getActive())) {
            this.iv_network.setImageResource(C0221R.C0222drawable.ic_net_icon_line);
        } else if (Network.NETWORK_TYPE_WIFI.equals(network.getActive())) {
            this.iv_network.setImageResource(C0221R.C0222drawable.ic_net_icon_wifi);
        } else if ("none".equals(network.getActive())) {
            this.iv_network.setImageResource(C0221R.C0222drawable.ic_net_icon_disconn);
        }
        this.iv_signal = (ImageView) findViewById(C0221R.C0223id.iv_signal);
        if (!network.isConnected()) {
            this.iv_signal.setVisibility(8);
        } else if (Network.NETWORK_TYPE_MOBILE.equals(network.getActive())) {
            MobileNet mobileNet2 = HardwareStatusCacheProvider.getInstance().getMobileNetStatus();
            if (mobileNet2 != null && (("2G".equals(mobileNet2.getType()) | "3G".equals(mobileNet2.getType())) || "4G".equals(mobileNet2.getType()))) {
                this.iv_signal.setVisibility(0);
                switch (mobileNet2.getDefaultSignalLevel()) {
                    case -1:
                        this.iv_signal.setVisibility(8);
                        break;
                    case 0:
                        this.iv_signal.setImageResource(C0221R.C0222drawable.ic_signal_0);
                        break;
                    case 1:
                        this.iv_signal.setImageResource(C0221R.C0222drawable.ic_signal_1);
                        break;
                    case 2:
                        this.iv_signal.setImageResource(C0221R.C0222drawable.ic_signal_2);
                        break;
                    case 3:
                        this.iv_signal.setImageResource(C0221R.C0222drawable.ic_signal_3);
                        break;
                    case 4:
                        this.iv_signal.setImageResource(C0221R.C0222drawable.ic_signal_4);
                        break;
                }
            }
        } else {
            this.iv_signal.setVisibility(8);
        }
        this.iv_cloud = (ImageView) findViewById(C0221R.C0223id.iv_cloud);
        if (ChargeStatusCacheProvider.getInstance().isCloudConnected()) {
            this.iv_cloud.setVisibility(0);
        } else {
            this.iv_cloud.setVisibility(8);
        }
        this.iv_lock = (ImageView) findViewById(C0221R.C0223id.iv_lock);
        this.iv_lock.setVisibility(0);
        LOCK_STATUS lockStatus = ChargeStatusCacheProvider.getInstance().getPortLockStatus("1");
        if (LOCK_STATUS.disable.getStatus().equals(lockStatus.getStatus())) {
            this.iv_lock.setImageResource(C0221R.C0222drawable.ic_disable);
        } else if (LOCK_STATUS.lock.getStatus().equals(lockStatus.getStatus())) {
            this.iv_lock.setImageResource(C0221R.C0222drawable.ic_lock);
        } else if (LOCK_STATUS.unlock.getStatus().equals(lockStatus.getStatus())) {
            this.iv_lock.setImageResource(C0221R.C0222drawable.ic_unlock);
        } else if (LOCK_STATUS.fault.getStatus().equals(lockStatus.getStatus())) {
            this.iv_lock.setVisibility(8);
        }
        this.iv_park = (ImageView) findViewById(C0221R.C0223id.iv_park);
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
        this.iv_nfc_reading = (ImageView) findViewById(C0221R.C0223id.iv_nfc_reading);
        this.iv_company = (ImageView) view.findViewById(C0221R.C0223id.iv_company);
        if (SystemSettingCacheProvider.getInstance().isUsingXChargeLogo()) {
            this.iv_company.setImageResource(C0221R.C0222drawable.ic_company);
        } else {
            String companyResouce = RemoteSettingCacheProvider.getInstance().getCompanyResouce();
            if (TextUtils.isEmpty(companyResouce) || !Utils.fileIsExists(companyResouce)) {
                getCompanyLogo();
            } else {
                Utils.loadImage(companyResouce, this.iv_company, new ImageLoadingListener() {
                    public void onLoadingStarted(String arg0, View arg1) {
                    }

                    public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
                        BaseDialog.this.getCompanyLogo();
                    }

                    public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
                    }

                    public void onLoadingCancelled(String arg0, View arg1) {
                    }
                }, this.context);
            }
        }
        this.iv_company.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (Utils.getCurrentClassName(BaseDialog.this.context).equals(NFCChargeInitActivity.class.getName()) || Utils.getCurrentClassName(BaseDialog.this.context).equals(QrcodeActivity.class.getName())) {
                    BaseDialog.this.dismiss();
                    Intent intent = new Intent(BaseDialog.this.context, SetActivity.class);
                    intent.setFlags(268435456);
                    BaseDialog.this.context.startActivity(intent);
                }
            }
        });
        this.iv_status_one = (ImageView) findViewById(C0221R.C0223id.iv_status_one);
        this.tv_status_one = (TextView) findViewById(C0221R.C0223id.tv_status_one);
        this.tv_status_two = (TextView) findViewById(C0221R.C0223id.tv_status_two);
        this.tv_bottom = (TextView) findViewById(C0221R.C0223id.tv_bottom);
    }

    /* access modifiers changed from: private */
    public void getCompanyLogo() {
        CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
        if (CHARGE_PLATFORM.xcharge.equals(platform)) {
            if (PLATFORM_CUSTOMER.jsmny.equals(SystemSettingCacheProvider.getInstance().getPlatformCustomer())) {
                this.iv_company.setImageResource(C0221R.C0222drawable.jsmlogo);
            } else {
                this.iv_company.setImageResource(C0221R.C0222drawable.ic_company);
            }
        } else if (CHARGE_PLATFORM.xmsz.equals(platform)) {
            this.iv_company.setImageResource(C0221R.C0222drawable.ic_company);
        } else if (CHARGE_PLATFORM.anyo.equals(platform)) {
            this.iv_company.setImageResource(C0221R.C0222drawable.aylogo);
        } else {
            this.iv_company.setImageResource(C0221R.C0222drawable.ic_company);
        }
    }

    /* renamed from: com.xcharge.charger.ui.c2.activity.fault.BaseDialog$NetWorkDialogContentObserver */
    public class NetWorkDialogContentObserver extends ContentObserver {
        public NetWorkDialogContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            BaseDialog.this.initView();
        }
    }

    /* renamed from: com.xcharge.charger.ui.c2.activity.fault.BaseDialog$CloudDialogContentObserver */
    public class CloudDialogContentObserver extends ContentObserver {
        public CloudDialogContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            BaseDialog.this.initView();
        }
    }

    /* renamed from: com.xcharge.charger.ui.c2.activity.fault.BaseDialog$LockDialogContentObserver */
    public class LockDialogContentObserver extends ContentObserver {
        public LockDialogContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            BaseDialog.this.initView();
        }
    }

    /* renamed from: com.xcharge.charger.ui.c2.activity.fault.BaseDialog$ParkDialogContentObserver */
    public class ParkDialogContentObserver extends ContentObserver {
        public ParkDialogContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            BaseDialog.this.initView();
        }
    }

    /* renamed from: com.xcharge.charger.ui.c2.activity.fault.BaseDialog$RadarDialogContentObserver */
    public class RadarDialogContentObserver extends ContentObserver {
        public RadarDialogContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            BaseDialog.this.initView();
        }
    }

    /* renamed from: com.xcharge.charger.ui.c2.activity.fault.BaseDialog$NfcDialogContentObserver */
    public class NfcDialogContentObserver extends ContentObserver {
        public NfcDialogContentObserver(Handler handler) {
            super(handler);
        }

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

    /* renamed from: com.xcharge.charger.ui.c2.activity.fault.BaseDialog$CompanyContentObserver */
    public class CompanyContentObserver extends ContentObserver {
        public CompanyContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            BaseDialog.this.initView();
        }
    }

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

    public void dismiss() {
        super.dismiss();
        Utils.anewSetPermitNFC(this.context);
        if (!(this.netWorkDialogContentObserver == null || this.cloudDialogContentObserver == null || this.lockDialogContentObserver == null || this.parkDialogContentObserver == null || this.radarDialogContentObserver == null || this.nfcDialogContentObserver == null || this.companyContentObserver == null || this.uiBackgroundColorContentObserver == null)) {
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

    public void onBackPressed() {
        super.onBackPressed();
        if (System.currentTimeMillis() - this.lockTimestamp > 2000) {
            DCAPProxy.getInstance().gunLockCtrl("1", 0, LOCK_STATUS.unlock);
            this.lockTimestamp = System.currentTimeMillis();
        }
        if (Utils.getCurrentClassName(this.context).equals(NFCChargeWaittingStartActivity.class.getName())) {
            UIEventMessageProxy.getInstance().sendEvent("com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargeWaittingStartActivity", "key", (String) null, "com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargeWaittingStartActivity", "up", (HashMap) null);
        } else if (Utils.getCurrentClassName(this.context).equals(WaittingStartChargeActivity.class.getName())) {
            UIEventMessageProxy.getInstance().sendEvent("com.xcharge.charger.ui.c2.activity.charge.online.WaittingStartChargeActivity", "key", (String) null, "com.xcharge.charger.ui.c2.activity.charge.online.WaittingStartChargeActivity", "up", (HashMap) null);
        } else if (Utils.getCurrentClassName(this.context).equals(TestChargeActivity.class.getName())) {
            UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ACTIVITY, (String) null, "testHome", UICtrlMessage.OPR_SKIP, (HashMap) null);
        }
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode != 4 || !this.isOnKeyLongPress) {
            if (keyCode == 4 && !this.isOnKeyLongPress && !event.isTainted()) {
                onBackPressed();
            }
            this.isOnKeyLongPress = false;
        } else {
            this.isOnKeyLongPress = false;
        }
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4 && event.getRepeatCount() == 10) {
            this.isOnKeyLongPress = true;
            if (!Utils.getCurrentClassName(this.context).equals(TestChargeActivity.class.getName()) && !Utils.getCurrentClassName(this.context).equals(HomeActivity.class.getName())) {
                UIEventMessageProxy.getInstance().sendEvent(getClass().getName(), "key", (String) null, getClass().getName(), "down", (HashMap) null);
            }
        } else if (keyCode == 4) {
            event.getRepeatCount();
        }
        return true;
    }
}
