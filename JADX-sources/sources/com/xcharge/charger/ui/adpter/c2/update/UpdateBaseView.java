package com.xcharge.charger.ui.adpter.c2.update;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.data.bean.device.MobileNet;
import com.xcharge.charger.data.bean.device.Network;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.setting.PortSetting;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.DEVICE_STATUS;
import com.xcharge.charger.data.bean.type.GUN_LOCK_MODE;
import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.bean.type.PHASE;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.LocalSettingCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SoftwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.protocol.ocpp.bean.types.UnitOfMeasure;
import com.xcharge.charger.ui.adapter.c2.R;
import com.xcharge.charger.ui.api.UICtrlMessageProxy;
import com.xcharge.charger.ui.api.bean.UIEventMessage;
import com.xcharge.charger.ui.c2.activity.BaseActivity;
import com.xcharge.charger.ui.c2.activity.charge.nfc.NFCConfigPersnalCardActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;

/* loaded from: classes.dex */
public class UpdateBaseView {
    public void BaseUIWidgeKey(UIEventMessage event, final Context context) {
        if (event.getStatus().equals("down")) {
            new Thread(new Runnable() { // from class: com.xcharge.charger.ui.adpter.c2.update.UpdateBaseView.1
                private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$DEVICE_STATUS;
                private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$PHASE;

                static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$DEVICE_STATUS() {
                    int[] iArr = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$DEVICE_STATUS;
                    if (iArr == null) {
                        iArr = new int[DEVICE_STATUS.valuesCustom().length];
                        try {
                            iArr[DEVICE_STATUS.chargeFull.ordinal()] = 6;
                        } catch (NoSuchFieldError e) {
                        }
                        try {
                            iArr[DEVICE_STATUS.charging.ordinal()] = 5;
                        } catch (NoSuchFieldError e2) {
                        }
                        try {
                            iArr[DEVICE_STATUS.emergencyStop.ordinal()] = 11;
                        } catch (NoSuchFieldError e3) {
                        }
                        try {
                            iArr[DEVICE_STATUS.errorAmp.ordinal()] = 13;
                        } catch (NoSuchFieldError e4) {
                        }
                        try {
                            iArr[DEVICE_STATUS.errorComm.ordinal()] = 16;
                        } catch (NoSuchFieldError e5) {
                        }
                        try {
                            iArr[DEVICE_STATUS.errorTemp.ordinal()] = 14;
                        } catch (NoSuchFieldError e6) {
                        }
                        try {
                            iArr[DEVICE_STATUS.errorVolt.ordinal()] = 12;
                        } catch (NoSuchFieldError e7) {
                        }
                        try {
                            iArr[DEVICE_STATUS.idle.ordinal()] = 1;
                        } catch (NoSuchFieldError e8) {
                        }
                        try {
                            iArr[DEVICE_STATUS.lostPhase.ordinal()] = 10;
                        } catch (NoSuchFieldError e9) {
                        }
                        try {
                            iArr[DEVICE_STATUS.noGround.ordinal()] = 9;
                        } catch (NoSuchFieldError e10) {
                        }
                        try {
                            iArr[DEVICE_STATUS.notInited.ordinal()] = 8;
                        } catch (NoSuchFieldError e11) {
                        }
                        try {
                            iArr[DEVICE_STATUS.plugin.ordinal()] = 4;
                        } catch (NoSuchFieldError e12) {
                        }
                        try {
                            iArr[DEVICE_STATUS.plugout.ordinal()] = 3;
                        } catch (NoSuchFieldError e13) {
                        }
                        try {
                            iArr[DEVICE_STATUS.powerLeak.ordinal()] = 15;
                        } catch (NoSuchFieldError e14) {
                        }
                        try {
                            iArr[DEVICE_STATUS.selfCheck.ordinal()] = 2;
                        } catch (NoSuchFieldError e15) {
                        }
                        try {
                            iArr[DEVICE_STATUS.stopped.ordinal()] = 7;
                        } catch (NoSuchFieldError e16) {
                        }
                        $SWITCH_TABLE$com$xcharge$charger$data$bean$type$DEVICE_STATUS = iArr;
                    }
                    return iArr;
                }

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

                @Override // java.lang.Runnable
                public void run() {
                    HashMap<String, Object> data = new HashMap<>();
                    String ip = null;
                    Network network = HardwareStatusCacheProvider.getInstance().getNetworkStatus();
                    if (network.isConnected()) {
                        if (Network.NETWORK_TYPE_ETHERNET.equals(network.getActive())) {
                            ip = network.getEthernet().getIp();
                        } else if (Network.NETWORK_TYPE_WIFI.equals(network.getActive())) {
                            ip = network.getWifi().getIp();
                        } else if (Network.NETWORK_TYPE_MOBILE.equals(network.getActive())) {
                            ip = network.getMobile().getIp();
                        }
                    }
                    StringBuffer buffer = new StringBuffer();
                    String firewareVer = SoftwareStatusCacheProvider.getInstance().getFirewareVer();
                    String appVer = SoftwareStatusCacheProvider.getInstance().getAppVer();
                    if (!TextUtils.isEmpty(firewareVer) && !TextUtils.isEmpty(appVer)) {
                        buffer.append(UnitOfMeasure.V + firewareVer + "-" + appVer).append(StringUtils.LF);
                    }
                    if (CHARGE_PLATFORM.anyo.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
                        HashMap<String, String> platformData = SystemSettingCacheProvider.getInstance().getPlatformCustomizedData();
                        buffer.append("SN:").append(platformData.get("id")).append(StringUtils.LF);
                    } else {
                        buffer.append("SN:").append(HardwareStatusCacheProvider.getInstance().getHardwareStatus().getSn()).append(StringUtils.LF);
                    }
                    if (!TextUtils.isEmpty(ip)) {
                        buffer.append("Ip:").append(ip).append(", ").append("Cloud:").append(ChargeStatusCacheProvider.getInstance().isCloudConnected());
                        buffer.append(", Proto:").append(SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform());
                        buffer.append(StringUtils.LF);
                    } else {
                        MobileNet mobile = network.getMobile();
                        if (mobile != null) {
                            int signalDbm = mobile.getSignalDbm();
                            String simState = mobile.getSimState();
                            String ifName = mobile.getIfName();
                            if (signalDbm != -1000) {
                                buffer.append("Dbm:").append(signalDbm);
                            }
                            buffer.append(", Sim:").append(simState);
                            if (!"unknown".equals(ifName)) {
                                buffer.append(", Link:").append(ifName);
                            }
                            buffer.append(StringUtils.LF);
                        }
                    }
                    MobileNet mobile2 = network.getMobile();
                    if (mobile2 != null) {
                        String imsi = mobile2.getIMSI();
                        if (!TextUtils.isEmpty(imsi)) {
                            buffer.append("IMSI: ").append(imsi).append(StringUtils.LF);
                        }
                        String iccid = mobile2.getICCID();
                        if (!TextUtils.isEmpty(iccid) && iccid.length() >= 20) {
                            buffer.append("ICCID: ").append(iccid).append(StringUtils.LF);
                        }
                    }
                    PortSetting localPortSetting = LocalSettingCacheProvider.getInstance().getChargePortSetting("1");
                    if (localPortSetting == null) {
                        buffer.append(context.getString(R.string.local_lock_rule_none)).append(", ");
                    } else {
                        GUN_LOCK_MODE lockMode = localPortSetting.getGunLockSetting().getMode();
                        switch (lockMode.getMode()) {
                            case 0:
                                buffer.append(context.getString(R.string.local_lock_rule_disable)).append(", ");
                                break;
                            case 1:
                                buffer.append(context.getString(R.string.local_lock_rule_unlock_before_paid)).append(", ");
                                break;
                            case 2:
                                buffer.append(context.getString(R.string.local_lock_rule_unlock_after_paid)).append(", ");
                                break;
                            case 3:
                                buffer.append(context.getString(R.string.local_lock_rule_auto)).append(", ");
                                break;
                        }
                    }
                    PortSetting remotePortSetting = RemoteSettingCacheProvider.getInstance().getChargePortSetting("1");
                    if (remotePortSetting == null) {
                        buffer.append(context.getString(R.string.cloud_lock_rule_none)).append(StringUtils.LF);
                    } else {
                        GUN_LOCK_MODE lockMode2 = remotePortSetting.getGunLockSetting().getMode();
                        switch (lockMode2.getMode()) {
                            case 0:
                                buffer.append(context.getString(R.string.cloud_lock_rule_disable)).append(StringUtils.LF);
                                break;
                            case 1:
                                buffer.append(context.getString(R.string.cloud_lock_rule_unlock_before_paid)).append(StringUtils.LF);
                                break;
                            case 2:
                                buffer.append(context.getString(R.string.cloud_lock_rule_unlock_after_paid)).append(StringUtils.LF);
                                break;
                            case 3:
                                buffer.append(context.getString(R.string.cloud_lock_rule_auto)).append(StringUtils.LF);
                                break;
                        }
                    }
                    Port port = DCAPProxy.getInstance().getPortStatus("1");
                    if (port != null) {
                        buffer.append(context.getString(R.string.status));
                        if (port.getPortRuntimeStatus() == null) {
                            buffer.append("null");
                        } else {
                            switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$DEVICE_STATUS()[port.getPortRuntimeStatus().ordinal()]) {
                                case 1:
                                    buffer.append(context.getString(R.string.idle));
                                    break;
                                case 2:
                                default:
                                    if (port.getDeviceError().getCode() == 30017) {
                                        buffer.append(context.getString(R.string.leakage));
                                        if (port.getLeakAmp() != null) {
                                            buffer.append("(" + String.format(BaseActivity.THREEDP, port.getLeakAmp()) + ")");
                                            break;
                                        }
                                    }
                                    break;
                                case 3:
                                    buffer.append(context.getString(R.string.connecting));
                                    break;
                                case 4:
                                    buffer.append(context.getString(R.string.connected));
                                    break;
                                case 5:
                                    buffer.append(context.getString(R.string.charging));
                                    break;
                                case 6:
                                    buffer.append(context.getString(R.string.charge_full));
                                    break;
                                case 7:
                                    buffer.append(context.getString(R.string.charge_stopped));
                                    break;
                            }
                        }
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        buffer.append(", ").append(context.getString(R.string.datetime)).append(sdf.format(new Date())).append(StringUtils.LF);
                        switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$PHASE()[HardwareStatusCacheProvider.getInstance().getHardwareStatus().getPhase().ordinal()]) {
                            case 1:
                                buffer.append(context.getString(R.string.phase_unknown));
                                break;
                            case 2:
                                buffer.append(context.getString(R.string.phase_single));
                                break;
                            case 3:
                                buffer.append(context.getString(R.string.phase_triple));
                                break;
                            case 4:
                                buffer.append(context.getString(R.string.phase_dc));
                                break;
                        }
                        LOCK_STATUS lockStatus = ChargeStatusCacheProvider.getInstance().getPortLockStatus("1");
                        buffer.append(", ").append(String.valueOf(context.getString(R.string.lock_status)) + lockStatus.getStatus()).append(StringUtils.LF).append(context.getString(R.string.power)).append(port.getKwatt()).append(", ").append(context.getString(R.string.ammeter)).append(port.getMeter()).append(", ").append(context.getString(R.string.radar_range)).append(port.getRadar().getDetectDist()).append(", ").append(context.getString(R.string.radar_calibration)).append(port.getRadar().getCalibrateDist()).append(StringUtils.SPACE).append(StringUtils.LF).append(context.getString(R.string.mode)).append(ChargeStatusCacheProvider.getInstance().getWorkMode().getMode()).append(", ").append(context.getString(R.string.max_current)).append(ChargeStatusCacheProvider.getInstance().getAmpCapacity()).append(", ").append(context.getString(R.string.working_current)).append(port.getAdjustAmp()).append(StringUtils.LF).append(context.getString(R.string.current)).append(port.getAvgAmp()).append(StringUtils.SPACE).append(context.getString(R.string.current_a)).append(port.getAmps().get(0)).append(StringUtils.SPACE).append(context.getString(R.string.current_b)).append(port.getAmps().get(1)).append(StringUtils.SPACE).append(context.getString(R.string.current_c)).append(port.getAmps().get(2)).append(StringUtils.SPACE).append(StringUtils.LF).append(context.getString(R.string.voltage_a)).append(port.getVolts().get(0)).append(StringUtils.SPACE).append(context.getString(R.string.voltage_b)).append(port.getVolts().get(1)).append(StringUtils.SPACE).append(context.getString(R.string.voltage_c)).append(port.getVolts().get(2)).append(StringUtils.SPACE).append(StringUtils.LF).append(context.getString(R.string.cp_voltage)).append(port.getCpVoltage()).append(", ").append(context.getString(R.string.temperature)).append(port.getChipTemp());
                        data.put("keyText", buffer);
                        UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), "key", UIEventMessage.KEY_HOME, "key", "down", data);
                    }
                }
            }).start();
        }
    }

    public void veification(Bundle data) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("type", data.getString("type"));
        hashMap.put("xid", data.getString("xid"));
        hashMap.put("customer", data.getString("customer"));
        hashMap.put("expired", new StringBuilder(String.valueOf(data.getInt("expired"))).toString());
        UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ELEMENT, null, "challenge", "update", hashMap);
    }

    public void cancelConfigPersnalCard(UIEventMessage event, Context context) {
        if (event.getActivity().equals(NFCConfigPersnalCardActivity.class.getName()) && event.getStatus().equals("up")) {
            Intent intent = new Intent(DCAPProxy.ACTION_CANCEL_U1_BIND_EVENT);
            intent.putExtra(ContentDB.ChargeTable.PORT, "1");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }
}
