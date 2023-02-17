package com.xcharge.charger.p006ui.adpter.p008c2.update;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.p000v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.data.bean.device.MobileNet;
import com.xcharge.charger.data.bean.device.Network;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.setting.PortSetting;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.DEVICE_STATUS;
import com.xcharge.charger.data.bean.type.PHASE;
import com.xcharge.charger.data.p004db.ContentDB;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.LocalSettingCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SoftwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.p006ui.adapter.p007c2.C0297R;
import com.xcharge.charger.p006ui.api.UICtrlMessageProxy;
import com.xcharge.charger.p006ui.api.bean.UIEventMessage;
import com.xcharge.charger.p006ui.p009c2.activity.BaseActivity;
import com.xcharge.charger.p006ui.p009c2.activity.charge.nfc.NFCConfigPersnalCardActivity;
import com.xcharge.charger.protocol.ocpp.bean.types.UnitOfMeasure;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;

/* renamed from: com.xcharge.charger.ui.adpter.c2.update.UpdateBaseView */
public class UpdateBaseView {
    public void BaseUIWidgeKey(UIEventMessage event, final Context context) {
        if (event.getStatus().equals("down")) {
            new Thread(new Runnable() {
                private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$DEVICE_STATUS;
                private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$PHASE;

                static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$DEVICE_STATUS() {
                    int[] iArr = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$DEVICE_STATUS;
                    if (iArr == null) {
                        iArr = new int[DEVICE_STATUS.values().length];
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
                        buffer.append(UnitOfMeasure.f121V + firewareVer + "-" + appVer).append(StringUtils.f146LF);
                    }
                    if (CHARGE_PLATFORM.anyo.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
                        buffer.append("SN:").append(SystemSettingCacheProvider.getInstance().getPlatformCustomizedData().get("id")).append(StringUtils.f146LF);
                    } else {
                        buffer.append("SN:").append(HardwareStatusCacheProvider.getInstance().getHardwareStatus().getSn()).append(StringUtils.f146LF);
                    }
                    if (!TextUtils.isEmpty(ip)) {
                        buffer.append("Ip:").append(ip).append(", ").append("Cloud:").append(ChargeStatusCacheProvider.getInstance().isCloudConnected());
                        buffer.append(", Proto:").append(SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform());
                        buffer.append(StringUtils.f146LF);
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
                            buffer.append(StringUtils.f146LF);
                        }
                    }
                    MobileNet mobile2 = network.getMobile();
                    if (mobile2 != null) {
                        String imsi = mobile2.getIMSI();
                        if (!TextUtils.isEmpty(imsi)) {
                            buffer.append("IMSI: ").append(imsi).append(StringUtils.f146LF);
                        }
                        String iccid = mobile2.getICCID();
                        if (!TextUtils.isEmpty(iccid) && iccid.length() >= 20) {
                            buffer.append("ICCID: ").append(iccid).append(StringUtils.f146LF);
                        }
                    }
                    PortSetting localPortSetting = LocalSettingCacheProvider.getInstance().getChargePortSetting("1");
                    if (localPortSetting != null) {
                        switch (localPortSetting.getGunLockSetting().getMode().getMode()) {
                            case 0:
                                buffer.append(context.getString(C0297R.string.local_lock_rule_disable)).append(", ");
                                break;
                            case 1:
                                buffer.append(context.getString(C0297R.string.local_lock_rule_unlock_before_paid)).append(", ");
                                break;
                            case 2:
                                buffer.append(context.getString(C0297R.string.local_lock_rule_unlock_after_paid)).append(", ");
                                break;
                            case 3:
                                buffer.append(context.getString(C0297R.string.local_lock_rule_auto)).append(", ");
                                break;
                        }
                    } else {
                        buffer.append(context.getString(C0297R.string.local_lock_rule_none)).append(", ");
                    }
                    PortSetting remotePortSetting = RemoteSettingCacheProvider.getInstance().getChargePortSetting("1");
                    if (remotePortSetting != null) {
                        switch (remotePortSetting.getGunLockSetting().getMode().getMode()) {
                            case 0:
                                buffer.append(context.getString(C0297R.string.cloud_lock_rule_disable)).append(StringUtils.f146LF);
                                break;
                            case 1:
                                buffer.append(context.getString(C0297R.string.cloud_lock_rule_unlock_before_paid)).append(StringUtils.f146LF);
                                break;
                            case 2:
                                buffer.append(context.getString(C0297R.string.cloud_lock_rule_unlock_after_paid)).append(StringUtils.f146LF);
                                break;
                            case 3:
                                buffer.append(context.getString(C0297R.string.cloud_lock_rule_auto)).append(StringUtils.f146LF);
                                break;
                        }
                    } else {
                        buffer.append(context.getString(C0297R.string.cloud_lock_rule_none)).append(StringUtils.f146LF);
                    }
                    Port port = DCAPProxy.getInstance().getPortStatus("1");
                    if (port != null) {
                        buffer.append(context.getString(C0297R.string.status));
                        if (port.getPortRuntimeStatus() != null) {
                            switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$DEVICE_STATUS()[port.getPortRuntimeStatus().ordinal()]) {
                                case 1:
                                    buffer.append(context.getString(C0297R.string.idle));
                                    break;
                                case 3:
                                    buffer.append(context.getString(C0297R.string.connecting));
                                    break;
                                case 4:
                                    buffer.append(context.getString(C0297R.string.connected));
                                    break;
                                case 5:
                                    buffer.append(context.getString(C0297R.string.charging));
                                    break;
                                case 6:
                                    buffer.append(context.getString(C0297R.string.charge_full));
                                    break;
                                case 7:
                                    buffer.append(context.getString(C0297R.string.charge_stopped));
                                    break;
                                default:
                                    if (port.getDeviceError().getCode() == 30017) {
                                        buffer.append(context.getString(C0297R.string.leakage));
                                        if (port.getLeakAmp() != null) {
                                            buffer.append("(" + String.format(BaseActivity.THREEDP, new Object[]{port.getLeakAmp()}) + ")");
                                            break;
                                        }
                                    }
                                    break;
                            }
                        } else {
                            buffer.append("null");
                        }
                        buffer.append(", ").append(context.getString(C0297R.string.datetime)).append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append(StringUtils.f146LF);
                        switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$PHASE()[HardwareStatusCacheProvider.getInstance().getHardwareStatus().getPhase().ordinal()]) {
                            case 1:
                                buffer.append(context.getString(C0297R.string.phase_unknown));
                                break;
                            case 2:
                                buffer.append(context.getString(C0297R.string.phase_single));
                                break;
                            case 3:
                                buffer.append(context.getString(C0297R.string.phase_triple));
                                break;
                            case 4:
                                buffer.append(context.getString(C0297R.string.phase_dc));
                                break;
                        }
                        buffer.append(", ").append(String.valueOf(context.getString(C0297R.string.lock_status)) + ChargeStatusCacheProvider.getInstance().getPortLockStatus("1").getStatus()).append(StringUtils.f146LF).append(context.getString(C0297R.string.power)).append(port.getKwatt()).append(", ").append(context.getString(C0297R.string.ammeter)).append(port.getMeter()).append(", ").append(context.getString(C0297R.string.radar_range)).append(port.getRadar().getDetectDist()).append(", ").append(context.getString(C0297R.string.radar_calibration)).append(port.getRadar().getCalibrateDist()).append(StringUtils.SPACE).append(StringUtils.f146LF).append(context.getString(C0297R.string.mode)).append(ChargeStatusCacheProvider.getInstance().getWorkMode().getMode()).append(", ").append(context.getString(C0297R.string.max_current)).append(ChargeStatusCacheProvider.getInstance().getAmpCapacity()).append(", ").append(context.getString(C0297R.string.working_current)).append(port.getAdjustAmp()).append(StringUtils.f146LF).append(context.getString(C0297R.string.current)).append(port.getAvgAmp()).append(StringUtils.SPACE).append(context.getString(C0297R.string.current_a)).append(port.getAmps().get(0)).append(StringUtils.SPACE).append(context.getString(C0297R.string.current_b)).append(port.getAmps().get(1)).append(StringUtils.SPACE).append(context.getString(C0297R.string.current_c)).append(port.getAmps().get(2)).append(StringUtils.SPACE).append(StringUtils.f146LF).append(context.getString(C0297R.string.voltage_a)).append(port.getVolts().get(0)).append(StringUtils.SPACE).append(context.getString(C0297R.string.voltage_b)).append(port.getVolts().get(1)).append(StringUtils.SPACE).append(context.getString(C0297R.string.voltage_c)).append(port.getVolts().get(2)).append(StringUtils.SPACE).append(StringUtils.f146LF).append(context.getString(C0297R.string.cp_voltage)).append(port.getCpVoltage()).append(", ").append(context.getString(C0297R.string.temperature)).append(port.getChipTemp());
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
        UICtrlMessageProxy.getInstance().sendCtrl(BaseActivity.class.getName(), UIEventMessage.TYPE_UI_ELEMENT, (String) null, "challenge", "update", hashMap);
    }

    public void cancelConfigPersnalCard(UIEventMessage event, Context context) {
        if (event.getActivity().equals(NFCConfigPersnalCardActivity.class.getName()) && event.getStatus().equals("up")) {
            Intent intent = new Intent(DCAPProxy.ACTION_CANCEL_U1_BIND_EVENT);
            intent.putExtra(ContentDB.ChargeTable.PORT, "1");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }
}
