package com.xcharge.charger.device.c2.status;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.device.NFC;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.type.DEVICE_STATUS;
import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.bean.type.PARK_STATUS;
import com.xcharge.charger.data.bean.type.SWITCH_STATUS;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.device.c2.bean.PortRuntimeData;
import com.xcharge.charger.device.c2.service.C2DeviceProxy;
import com.xcharge.common.utils.LogUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/* loaded from: classes.dex */
public class HardwareStatusHandler {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$DEVICE_STATUS = null;
    public static final int MSG_NETWORK_CONNECTION = 86049;
    public static final int MSG_PORT_AUTH_INVALID = 86018;
    public static final int MSG_PORT_AUTH_VALID = 86017;
    public static final int MSG_PORT_CHARGE_FULL = 86023;
    public static final int MSG_PORT_CHARGE_STARTED = 86021;
    public static final int MSG_PORT_CHARGE_STOPPED = 86022;
    public static final int MSG_PORT_ENABLE_STATUS = 86052;
    public static final int MSG_PORT_NFC_STATUS = 86048;
    public static final int MSG_PORT_PARK_STATUS = 86035;
    public static final int MSG_PORT_PLUGIN = 86019;
    public static final int MSG_PORT_PLUGOUT = 86020;
    public static final int MSG_PORT_RADAR_CALIBRATION = 86034;
    public static final int MSG_PORT_RADAR_STATUS = 86050;
    public static final int MSG_PORT_RESUME = 86025;
    public static final int MSG_PORT_SUSPEND = 86024;
    public static final int MSG_PORT_UPDATE = 86032;
    public static final int MSG_PORT_UPDATE_BY_CMD = 86051;
    public static final int MSG_PORT_WARN = 86033;
    private Context context = null;
    private HandlerThread thread = null;
    private MsgHandler handler = null;
    private int deviceStatus = 0;
    private PortRadarStatusObserver portRadarStatusObserver = null;
    private PortEnableStatusObserver portEnableStatusObserver = null;

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

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class PortRadarStatusObserver extends ContentObserver {
        private Handler handler;

        public PortRadarStatusObserver(Handler handler) {
            super(handler);
            this.handler = null;
            this.handler = handler;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            Log.d("HardwareStatusHandler.PortRadarStatusObserver.onChange", "selfChange: " + selfChange + ", uri: " + uri.toString());
            super.onChange(selfChange, uri);
            this.handler.obtainMessage(HardwareStatusHandler.MSG_PORT_RADAR_STATUS, uri).sendToTarget();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class PortEnableStatusObserver extends ContentObserver {
        private Handler handler;

        public PortEnableStatusObserver(Handler handler) {
            super(handler);
            this.handler = null;
            this.handler = handler;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            Log.d("HardwareStatusHandler.PortEnableStatusObserver.onChange", "selfChange: " + selfChange + ", uri: " + uri.toString());
            super.onChange(selfChange, uri);
            this.handler.obtainMessage(HardwareStatusHandler.MSG_PORT_ENABLE_STATUS, uri).sendToTarget();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class MsgHandler extends Handler {
        public MsgHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case HardwareStatusHandler.MSG_PORT_AUTH_VALID /* 86017 */:
                        PortRuntimeData data = (PortRuntimeData) msg.obj;
                        Log.i("HardwareStatusHandler.handleMessage", "port auth valid !!! data: " + data.toJson());
                        HardwareStatusHandler.this.handleAuthValid(data);
                        break;
                    case HardwareStatusHandler.MSG_PORT_AUTH_INVALID /* 86018 */:
                        PortRuntimeData data2 = (PortRuntimeData) msg.obj;
                        Log.i("HardwareStatusHandler.handleMessage", "port auth invalid !!!  data: " + data2.toJson());
                        HardwareStatusHandler.this.handleAuthInvalid(data2);
                        break;
                    case HardwareStatusHandler.MSG_PORT_PLUGIN /* 86019 */:
                        PortRuntimeData data3 = (PortRuntimeData) msg.obj;
                        Log.i("HardwareStatusHandler.handleMessage", "port plugin !!!  data: " + data3.toJson());
                        HardwareStatusHandler.this.handlePlugin(data3);
                        break;
                    case HardwareStatusHandler.MSG_PORT_PLUGOUT /* 86020 */:
                        PortRuntimeData data4 = (PortRuntimeData) msg.obj;
                        Log.i("HardwareStatusHandler.handleMessage", "port plugout !!!  data: " + data4.toJson());
                        HardwareStatusHandler.this.handlePlugout(data4);
                        break;
                    case HardwareStatusHandler.MSG_PORT_CHARGE_STARTED /* 86021 */:
                        PortRuntimeData data5 = (PortRuntimeData) msg.obj;
                        Log.i("HardwareStatusHandler.handleMessage", "port charge started !!!  data: " + data5.toJson());
                        HardwareStatusHandler.this.handleChargeStart(data5);
                        break;
                    case HardwareStatusHandler.MSG_PORT_CHARGE_STOPPED /* 86022 */:
                        PortRuntimeData data6 = (PortRuntimeData) msg.obj;
                        Log.i("HardwareStatusHandler.handleMessage", "port charge stopped !!!  data: " + data6.toJson());
                        HardwareStatusHandler.this.handleChargeStop(data6);
                        break;
                    case HardwareStatusHandler.MSG_PORT_CHARGE_FULL /* 86023 */:
                        PortRuntimeData data7 = (PortRuntimeData) msg.obj;
                        Log.i("HardwareStatusHandler.handleMessage", "port charge full !!!  data: " + data7.toJson());
                        HardwareStatusHandler.this.handleChargeFull(data7);
                        break;
                    case HardwareStatusHandler.MSG_PORT_SUSPEND /* 86024 */:
                        PortRuntimeData data8 = (PortRuntimeData) msg.obj;
                        Log.i("HardwareStatusHandler.handleMessage", "port suspended !!!  data: " + data8.toJson());
                        HardwareStatusHandler.this.handleSuspend(data8);
                        break;
                    case HardwareStatusHandler.MSG_PORT_RESUME /* 86025 */:
                        PortRuntimeData data9 = (PortRuntimeData) msg.obj;
                        Log.i("HardwareStatusHandler.handleMessage", "port resumed !!!  data: " + data9.toJson());
                        HardwareStatusHandler.this.handleResume(data9);
                        break;
                    case HardwareStatusHandler.MSG_PORT_UPDATE /* 86032 */:
                        PortRuntimeData data10 = (PortRuntimeData) msg.obj;
                        Log.i("HardwareStatusHandler.handleMessage", "port updated !!!  data: " + data10.toJson());
                        HardwareStatusHandler.this.handleUpdate(data10);
                        break;
                    case HardwareStatusHandler.MSG_PORT_WARN /* 86033 */:
                        Log.i("HardwareStatusHandler.handleMessage", "port warning !!! port: " + ((PortRuntimeData) msg.obj).getPort());
                        break;
                    case HardwareStatusHandler.MSG_PORT_RADAR_CALIBRATION /* 86034 */:
                        PortRuntimeData data11 = (PortRuntimeData) msg.obj;
                        Log.i("HardwareStatusHandler.handleMessage", "port radar calibration result changed !!! port: " + data11.getPort() + ", radar calibration result: " + data11.getIsRadarCalibrated());
                        HardwareStatusHandler.this.handleRadarCalibration(data11.getPort(), data11.getIsRadarCalibrated().booleanValue());
                        break;
                    case HardwareStatusHandler.MSG_PORT_PARK_STATUS /* 86035 */:
                        PortRuntimeData data12 = (PortRuntimeData) msg.obj;
                        Log.i("HardwareStatusHandler.handleMessage", "port park status changed !!! port: " + data12.getPort() + ", park status: " + data12.getParkStatus().getStatus());
                        HardwareStatusHandler.this.handleParkChanged(data12.getPort(), data12.getParkStatus());
                        break;
                    case HardwareStatusHandler.MSG_PORT_NFC_STATUS /* 86048 */:
                        String port = msg.getData().getString(ContentDB.ChargeTable.PORT);
                        NFC nfcStatus = (NFC) msg.obj;
                        Log.i("HardwareStatusHandler.handleMessage", "port nfc status changed !!! port: " + port + ", nfc status: " + nfcStatus.toJson());
                        HardwareStatusHandler.this.handleNFCStatus(port, nfcStatus);
                        break;
                    case HardwareStatusHandler.MSG_NETWORK_CONNECTION /* 86049 */:
                        boolean isConnected = ((Boolean) msg.obj).booleanValue();
                        Log.i("HardwareStatusHandler.handleMessage", "network connection status changed !!! is network connected: " + isConnected);
                        HardwareStatusHandler.this.handleNetworkConnectionStatus(isConnected);
                        break;
                    case HardwareStatusHandler.MSG_PORT_RADAR_STATUS /* 86050 */:
                        Uri uri = (Uri) msg.obj;
                        Log.i("HardwareStatusHandler.handleMessage", "radar status changed !!! uri: " + uri);
                        HardwareStatusHandler.this.handleRadarStatusChanged(uri);
                        break;
                    case HardwareStatusHandler.MSG_PORT_UPDATE_BY_CMD /* 86051 */:
                        Port portStatus = (Port) msg.obj;
                        Log.i("HardwareStatusHandler.handleMessage", "receive port status by update cmd: " + portStatus.toJson());
                        HardwareStatusHandler.this.handlePortUpdateByCmd(portStatus);
                        break;
                    case HardwareStatusHandler.MSG_PORT_ENABLE_STATUS /* 86052 */:
                        Uri uri2 = (Uri) msg.obj;
                        Log.i("HardwareStatusHandler.handleMessage", "port enable status changed !!! uri: " + uri2);
                        HardwareStatusHandler.this.handlePortEnableStatusChanged(uri2);
                        break;
                }
            } catch (Exception e) {
                Log.e("HardwareStatusHandler.handleMessage", "except: " + Log.getStackTraceString(e));
                LogUtils.syslog("HardwareStatusHandler handleMessage exception: " + Log.getStackTraceString(e));
            }
            super.handleMessage(msg);
        }
    }

    public void init(Context context) {
        this.context = context;
        this.thread = new HandlerThread("HardwareStatusHandler", 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.portRadarStatusObserver = new PortRadarStatusObserver(this.handler);
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor("ports/radar/status"), true, this.portRadarStatusObserver);
        this.portEnableStatusObserver = new PortEnableStatusObserver(this.handler);
        this.context.getContentResolver().registerContentObserver(ChargeStatusCacheProvider.getInstance().getUriFor("ports/enable"), true, this.portEnableStatusObserver);
    }

    public void destroy() {
        this.context.getContentResolver().unregisterContentObserver(this.portRadarStatusObserver);
        this.context.getContentResolver().unregisterContentObserver(this.portEnableStatusObserver);
        this.handler.removeMessages(MSG_PORT_AUTH_VALID);
        this.handler.removeMessages(MSG_PORT_AUTH_INVALID);
        this.handler.removeMessages(MSG_PORT_PLUGIN);
        this.handler.removeMessages(MSG_PORT_PLUGOUT);
        this.handler.removeMessages(MSG_PORT_CHARGE_STARTED);
        this.handler.removeMessages(MSG_PORT_CHARGE_STOPPED);
        this.handler.removeMessages(MSG_PORT_CHARGE_FULL);
        this.handler.removeMessages(MSG_PORT_SUSPEND);
        this.handler.removeMessages(MSG_PORT_RESUME);
        this.handler.removeMessages(MSG_PORT_UPDATE);
        this.handler.removeMessages(MSG_PORT_WARN);
        this.handler.removeMessages(MSG_PORT_RADAR_CALIBRATION);
        this.handler.removeMessages(MSG_PORT_PARK_STATUS);
        this.handler.removeMessages(MSG_PORT_NFC_STATUS);
        this.handler.removeMessages(MSG_NETWORK_CONNECTION);
        this.handler.removeMessages(MSG_PORT_RADAR_STATUS);
        this.handler.removeMessages(MSG_PORT_UPDATE_BY_CMD);
        this.handler.removeMessages(MSG_PORT_ENABLE_STATUS);
        this.thread.quit();
    }

    public Message obtainMessage(int what, Object obj) {
        return this.handler.obtainMessage(what, obj);
    }

    public boolean sendMessage(Message msg) {
        return this.handler.sendMessage(msg);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleNFCStatus(String port, NFC nfcStatus) {
        HardwareStatusCacheProvider.getInstance().updatePortNFCStatus(port, nfcStatus);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleRadarStatusChanged(Uri uri) {
        String port = uri.getLastPathSegment();
        boolean radarEnable = HardwareStatusCacheProvider.getInstance().getPortRadarSwitch(port);
        if (radarEnable) {
            updateParkStatusBLN(port);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePortEnableStatusChanged(Uri uri) {
        String port = uri.getLastPathSegment();
        boolean portEnable = ChargeStatusCacheProvider.getInstance().getPortSwitch(port);
        if (portEnable) {
            int defaultNorColor = Color.parseColor(RemoteSettingCacheProvider.getInstance().getDefaultBLNColor()) & ViewCompat.MEASURED_SIZE_MASK;
            C2DeviceProxy.getInstance().setSystemStatusBLN(port, this.deviceStatus, defaultNorColor);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleNetworkConnectionStatus(boolean isConnected) {
        updateNetworkStatusBLN();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleAuthValid(PortRuntimeData data) {
        String portNo = data.getPort();
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(portNo);
        portStatus.setDeviceError(getErrorCode(DEVICE_STATUS.valueBy(data.getStatus().intValue())));
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        updateDeviceStatusBLN(portNo, data.getStatus().intValue());
        this.deviceStatus = data.getStatus().intValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleAuthInvalid(PortRuntimeData data) {
        String portNo = data.getPort();
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(portNo);
        portStatus.setDeviceError(getErrorCode(DEVICE_STATUS.valueBy(data.getStatus().intValue())));
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        updateDeviceStatusBLN(portNo, data.getStatus().intValue());
        this.deviceStatus = data.getStatus().intValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePlugin(PortRuntimeData data) {
        String portNo = data.getPort();
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(portNo);
        portStatus.setDeviceError(getErrorCode(DEVICE_STATUS.valueBy(data.getStatus().intValue())));
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        updateDeviceStatusBLN(portNo, data.getStatus().intValue());
        this.deviceStatus = data.getStatus().intValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePlugout(PortRuntimeData data) {
        String portNo = data.getPort();
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(portNo);
        portStatus.setDeviceError(getErrorCode(DEVICE_STATUS.valueBy(data.getStatus().intValue())));
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        updateDeviceStatusBLN(portNo, data.getStatus().intValue());
        this.deviceStatus = data.getStatus().intValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleChargeStart(PortRuntimeData data) {
        String portNo = data.getPort();
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(portNo);
        portStatus.setDeviceError(getErrorCode(DEVICE_STATUS.valueBy(data.getStatus().intValue())));
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        updateDeviceStatusBLN(portNo, data.getStatus().intValue());
        this.deviceStatus = data.getStatus().intValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleChargeFull(PortRuntimeData data) {
        String portNo = data.getPort();
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(portNo);
        portStatus.setDeviceError(getErrorCode(DEVICE_STATUS.valueBy(data.getStatus().intValue())));
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        updateDeviceStatusBLN(portNo, data.getStatus().intValue());
        this.deviceStatus = data.getStatus().intValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleChargeStop(PortRuntimeData data) {
        String portNo = data.getPort();
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(portNo);
        portStatus.setDeviceError(getErrorCode(DEVICE_STATUS.valueBy(data.getStatus().intValue())));
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        updateDeviceStatusBLN(portNo, data.getStatus().intValue());
        this.deviceStatus = data.getStatus().intValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleSuspend(PortRuntimeData data) {
        String portNo = data.getPort();
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(portNo);
        DEVICE_STATUS deviceStatus = DEVICE_STATUS.valueBy(data.getStatus().intValue());
        ErrorCode error = getErrorCode(deviceStatus);
        HashMap<String, Object> errData = new HashMap<>();
        errData.put("raw", data.toJson());
        error.setData(errData);
        if (error.getCode() >= 30010 && error.getCode() <= 30018) {
            PortRuntimeData fullPortRuntimeInfo = C2DeviceProxy.getInstance().getPortRuntimeInfo(portNo);
            Double[] dArr = new Double[3];
            dArr[0] = Double.valueOf(fullPortRuntimeInfo.getCurrentA() == null ? 0.0d : fullPortRuntimeInfo.getCurrentA().doubleValue());
            dArr[1] = Double.valueOf(fullPortRuntimeInfo.getCurrentB() == null ? 0.0d : fullPortRuntimeInfo.getCurrentB().doubleValue());
            dArr[2] = Double.valueOf(fullPortRuntimeInfo.getCurrentC() == null ? 0.0d : fullPortRuntimeInfo.getCurrentC().doubleValue());
            portStatus.setAmps(new ArrayList<>(Arrays.asList(dArr)));
            Double[] dArr2 = new Double[3];
            dArr2[0] = Double.valueOf(fullPortRuntimeInfo.getVoltageA() == null ? 0.0d : fullPortRuntimeInfo.getVoltageA().doubleValue());
            dArr2[1] = Double.valueOf(fullPortRuntimeInfo.getVoltageB() == null ? 0.0d : fullPortRuntimeInfo.getVoltageB().doubleValue());
            dArr2[2] = Double.valueOf(fullPortRuntimeInfo.getVoltageC() == null ? 0.0d : fullPortRuntimeInfo.getVoltageC().doubleValue());
            portStatus.setVolts(new ArrayList<>(Arrays.asList(dArr2)));
            portStatus.setLeakAmp(Double.valueOf(fullPortRuntimeInfo.getCurrentN() == null ? 0.0d : fullPortRuntimeInfo.getCurrentN().doubleValue()));
            portStatus.setChipTemp(Double.valueOf(fullPortRuntimeInfo.getChipTemp() == null ? 0.0d : fullPortRuntimeInfo.getChipTemp().doubleValue()));
            portStatus.setCpVoltage(Integer.valueOf(fullPortRuntimeInfo.getCpVoltage() == null ? 0 : fullPortRuntimeInfo.getCpVoltage().intValue()));
            if (DEVICE_STATUS.emergencyStop.equals(deviceStatus)) {
                portStatus.getEmergencyStop().setStatus(SWITCH_STATUS.on);
            }
            errData.put("portStatus", portStatus.toJson());
            error.setData(errData);
            portStatus.setDeviceError(error);
            LOCK_STATUS lockStatus = ChargeStatusCacheProvider.getInstance().getPortLockStatus(portNo);
            if (!LOCK_STATUS.disable.equals(lockStatus)) {
                C2DeviceProxy.getInstance().unlockGun(portNo);
                ChargeStatusCacheProvider.getInstance().updatePortLockStatus(portNo, LOCK_STATUS.unlock);
            }
        }
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        if (error.getCode() >= 30010 && error.getCode() <= 30018) {
            HardwareStatusCacheProvider.getInstance().putDeviceError(portNo, error);
            HardwareStatusCacheProvider.getInstance().removeHigherPriorityDeviceErrors(portNo, error);
            LogUtils.syslog("receive error Suspend: " + data.toJson());
        }
        updateDeviceStatusBLN(portNo, data.getStatus().intValue());
        this.deviceStatus = data.getStatus().intValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleResume(PortRuntimeData data) {
        String portNo = data.getPort();
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(portNo);
        DEVICE_STATUS deviceStatus = DEVICE_STATUS.valueBy(data.getStatus().intValue());
        ErrorCode error = getErrorCode(deviceStatus);
        if (error.getCode() >= 30010 && error.getCode() <= 30018) {
            portStatus.setDeviceError(new ErrorCode(200));
            portStatus.getEmergencyStop().setStatus(SWITCH_STATUS.off);
        }
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        if (error.getCode() >= 30010 && error.getCode() <= 30018) {
            HardwareStatusCacheProvider.getInstance().removeDeviceError(portNo, null);
            LogUtils.syslog("receive error Resume: " + data.toJson());
        }
        updateDeviceStatusBLN(portNo, data.getStatus().intValue());
        this.deviceStatus = data.getStatus().intValue();
    }

    private void handleWarning(String port) {
        PortRuntimeData fullPortRuntimeInfo = C2DeviceProxy.getInstance().getPortRuntimeInfo(port);
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(port);
        Double[] dArr = new Double[3];
        dArr[0] = Double.valueOf(fullPortRuntimeInfo.getCurrentA() == null ? 0.0d : fullPortRuntimeInfo.getCurrentA().doubleValue());
        dArr[1] = Double.valueOf(fullPortRuntimeInfo.getCurrentB() == null ? 0.0d : fullPortRuntimeInfo.getCurrentB().doubleValue());
        dArr[2] = Double.valueOf(fullPortRuntimeInfo.getCurrentC() == null ? 0.0d : fullPortRuntimeInfo.getCurrentC().doubleValue());
        portStatus.setAmps(new ArrayList<>(Arrays.asList(dArr)));
        Double[] dArr2 = new Double[3];
        dArr2[0] = Double.valueOf(fullPortRuntimeInfo.getVoltageA() == null ? 0.0d : fullPortRuntimeInfo.getVoltageA().doubleValue());
        dArr2[1] = Double.valueOf(fullPortRuntimeInfo.getVoltageB() == null ? 0.0d : fullPortRuntimeInfo.getVoltageB().doubleValue());
        dArr2[2] = Double.valueOf(fullPortRuntimeInfo.getVoltageC() == null ? 0.0d : fullPortRuntimeInfo.getVoltageC().doubleValue());
        portStatus.setVolts(new ArrayList<>(Arrays.asList(dArr2)));
        portStatus.setLeakAmp(Double.valueOf(fullPortRuntimeInfo.getCurrentN() == null ? 0.0d : fullPortRuntimeInfo.getCurrentN().doubleValue()));
        portStatus.setChipTemp(Double.valueOf(fullPortRuntimeInfo.getChipTemp() != null ? fullPortRuntimeInfo.getChipTemp().doubleValue() : 0.0d));
        portStatus.setCpVoltage(Integer.valueOf(fullPortRuntimeInfo.getCpVoltage() == null ? 0 : fullPortRuntimeInfo.getCpVoltage().intValue()));
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleUpdate(PortRuntimeData data) {
        String portNo = data.getPort();
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(portNo);
        ErrorCode error = portStatus.getDeviceError();
        ErrorCode updateError = getErrorCode(DEVICE_STATUS.valueBy(data.getStatus().intValue()));
        portStatus.setDeviceError(updateError);
        if (data.getStatus().intValue() >= DEVICE_STATUS.notInited.getStatus()) {
            PortRuntimeData fullPortRuntimeInfo = C2DeviceProxy.getInstance().getPortRuntimeInfo(portNo);
            Double[] dArr = new Double[3];
            dArr[0] = Double.valueOf(fullPortRuntimeInfo.getCurrentA() == null ? 0.0d : fullPortRuntimeInfo.getCurrentA().doubleValue());
            dArr[1] = Double.valueOf(fullPortRuntimeInfo.getCurrentB() == null ? 0.0d : fullPortRuntimeInfo.getCurrentB().doubleValue());
            dArr[2] = Double.valueOf(fullPortRuntimeInfo.getCurrentC() == null ? 0.0d : fullPortRuntimeInfo.getCurrentC().doubleValue());
            portStatus.setAmps(new ArrayList<>(Arrays.asList(dArr)));
            Double[] dArr2 = new Double[3];
            dArr2[0] = Double.valueOf(fullPortRuntimeInfo.getVoltageA() == null ? 0.0d : fullPortRuntimeInfo.getVoltageA().doubleValue());
            dArr2[1] = Double.valueOf(fullPortRuntimeInfo.getVoltageB() == null ? 0.0d : fullPortRuntimeInfo.getVoltageB().doubleValue());
            dArr2[2] = Double.valueOf(fullPortRuntimeInfo.getVoltageC() == null ? 0.0d : fullPortRuntimeInfo.getVoltageC().doubleValue());
            portStatus.setVolts(new ArrayList<>(Arrays.asList(dArr2)));
            portStatus.setLeakAmp(Double.valueOf(fullPortRuntimeInfo.getCurrentN() == null ? 0.0d : fullPortRuntimeInfo.getCurrentN().doubleValue()));
            portStatus.setChipTemp(Double.valueOf(fullPortRuntimeInfo.getChipTemp() == null ? 0.0d : fullPortRuntimeInfo.getChipTemp().doubleValue()));
            portStatus.setCpVoltage(Integer.valueOf(fullPortRuntimeInfo.getCpVoltage() == null ? 0 : fullPortRuntimeInfo.getCpVoltage().intValue()));
            HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        } else if (((error.getCode() >= 30010 && error.getCode() <= 30018) || (portStatus.getAllDeviceErrorCache() != null && portStatus.getAllDeviceErrorCache().size() > 0)) && updateError.getCode() == 200) {
            Log.w("HardwareStatusHandler.handleUpdate", "error: " + error.toJson() + " -> normal: " + updateError.toJson());
            LogUtils.applog("error: " + (error.getCode() - 30000) + " resumed by update event !!!");
            portStatus.setDeviceError(new ErrorCode(200));
            portStatus.getEmergencyStop().setStatus(SWITCH_STATUS.off);
            HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
            HardwareStatusCacheProvider.getInstance().removeDeviceError(portNo, null);
        } else {
            HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        }
        updateDeviceStatusBLN(portNo, data.getStatus().intValue());
        this.deviceStatus = data.getStatus().intValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleParkChanged(String port, PARK_STATUS status) {
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(port);
        portStatus.getParkStatus().setParkStatus(status);
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
        updateParkStatusBLN(port);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleRadarCalibration(String port, boolean isSuccess) {
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(port);
        portStatus.getRadar().setCalibration(isSuccess);
        PortRuntimeData fullPortRuntimeInfo = C2DeviceProxy.getInstance().getPortRuntimeInfo(port);
        portStatus.getRadar().setCalibrateDist(Integer.valueOf(fullPortRuntimeInfo.getRaderCalibration() == null ? 0 : fullPortRuntimeInfo.getRaderCalibration().intValue()));
        portStatus.getRadar().setDetectDist(Integer.valueOf(fullPortRuntimeInfo.getRader() != null ? fullPortRuntimeInfo.getRader().intValue() : 0));
        HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePortUpdateByCmd(Port updatePortStatus) {
        int updateStatus = updatePortStatus.getPortRuntimeStatus().getStatus();
        String portNo = updatePortStatus.getPort();
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(portNo);
        ErrorCode error = portStatus.getDeviceError();
        ErrorCode updateError = getErrorCode(updatePortStatus.getPortRuntimeStatus());
        if (((error.getCode() >= 30010 && error.getCode() <= 30018) || (portStatus.getAllDeviceErrorCache() != null && portStatus.getAllDeviceErrorCache().size() > 0)) && updateError.getCode() == 200) {
            Port runtimePortChargeStatus = C2DeviceProxy.getInstance().getPortRuntimeStatus(portNo);
            if (getErrorCode(runtimePortChargeStatus.getPortRuntimeStatus()).getCode() == 200) {
                Log.w("HardwareStatusHandler.handlePortUpdateByCmd", "error: " + error.toJson() + " -> normal: " + updateError.toJson());
                LogUtils.applog("error: " + (error.getCode() - 30000) + " resumed by update cmd !!!");
                portStatus.setDeviceError(new ErrorCode(200));
                portStatus.getEmergencyStop().setStatus(SWITCH_STATUS.off);
                HardwareStatusCacheProvider.getInstance().updatePort(portStatus);
                HardwareStatusCacheProvider.getInstance().removeDeviceError(portNo, null);
                int defaultNorColor = Color.parseColor(RemoteSettingCacheProvider.getInstance().getDefaultBLNColor()) & ViewCompat.MEASURED_SIZE_MASK;
                C2DeviceProxy.getInstance().setSystemStatusBLN(portNo, updateStatus, defaultNorColor);
                this.deviceStatus = updateStatus;
            }
        }
    }

    private void updateDeviceStatusBLN(String port, int status) {
        int defaultNorColor = Color.parseColor(RemoteSettingCacheProvider.getInstance().getDefaultBLNColor()) & ViewCompat.MEASURED_SIZE_MASK;
        if (status != this.deviceStatus) {
            C2DeviceProxy.getInstance().setSystemStatusBLN(port, status, defaultNorColor);
        }
    }

    private void updateParkStatusBLN(String port) {
        int defaultColor = Color.parseColor(RemoteSettingCacheProvider.getInstance().getDefaultBLNColor()) & ViewCompat.MEASURED_SIZE_MASK;
        ErrorCode overallFault = HardwareStatusCacheProvider.getInstance().getDeviceFaultStatus();
        if (overallFault.getCode() != 200) {
            C2DeviceProxy.getInstance().setSystemStatusBLN(null, DEVICE_STATUS.notInited.getStatus(), defaultColor);
        } else if (this.deviceStatus < 10) {
            ErrorCode portFault = HardwareStatusCacheProvider.getInstance().getPort(port).getDeviceError();
            if (portFault.getCode() != 200) {
                if (portFault.getCode() >= 30010 && portFault.getCode() <= 30018) {
                    C2DeviceProxy.getInstance().setSystemStatusBLN(null, 10, defaultColor);
                    return;
                } else {
                    C2DeviceProxy.getInstance().setSystemStatusBLN(null, DEVICE_STATUS.notInited.getStatus(), defaultColor);
                    return;
                }
            }
            C2DeviceProxy.getInstance().setSystemStatusBLN(port, this.deviceStatus, defaultColor);
        } else {
            C2DeviceProxy.getInstance().setSystemStatusBLN(port, this.deviceStatus, defaultColor);
        }
    }

    private void updateNetworkStatusBLN() {
        int defaultColor = Color.parseColor(RemoteSettingCacheProvider.getInstance().getDefaultBLNColor()) & ViewCompat.MEASURED_SIZE_MASK;
        ErrorCode overallFault = HardwareStatusCacheProvider.getInstance().getDeviceFaultStatus();
        if (overallFault.getCode() != 200) {
            C2DeviceProxy.getInstance().setSystemStatusBLN(null, DEVICE_STATUS.notInited.getStatus(), defaultColor);
        } else if (this.deviceStatus < 10) {
            ErrorCode portFault = HardwareStatusCacheProvider.getInstance().getPort("1").getDeviceError();
            if (portFault.getCode() == 200) {
                C2DeviceProxy.getInstance().setSystemStatusBLN("1", this.deviceStatus, defaultColor);
            } else if (portFault.getCode() >= 30010 && portFault.getCode() <= 30018) {
                C2DeviceProxy.getInstance().setSystemStatusBLN(null, 10, defaultColor);
            } else {
                C2DeviceProxy.getInstance().setSystemStatusBLN(null, DEVICE_STATUS.notInited.getStatus(), defaultColor);
            }
        } else {
            C2DeviceProxy.getInstance().setSystemStatusBLN("1", this.deviceStatus, defaultColor);
        }
    }

    private ErrorCode getErrorCode(DEVICE_STATUS status) {
        int ec = 200;
        switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$DEVICE_STATUS()[status.ordinal()]) {
            case 8:
                ec = ErrorCode.EC_DEVICE_NOT_INIT;
                break;
            case 9:
                ec = ErrorCode.EC_DEVICE_NO_GROUND;
                break;
            case 10:
                ec = ErrorCode.EC_DEVICE_LOST_PHASE;
                break;
            case PortRuntimeData.STATUS_EX_11 /* 11 */:
                ec = ErrorCode.EC_DEVICE_EMERGENCY_STOP;
                break;
            case PortRuntimeData.STATUS_EX_12 /* 12 */:
                ec = ErrorCode.EC_DEVICE_VOLT_ERROR;
                break;
            case 13:
                ec = ErrorCode.EC_DEVICE_AMP_ERROR;
                break;
            case 14:
                ec = ErrorCode.EC_DEVICE_TEMP_ERROR;
                break;
            case 15:
                ec = ErrorCode.EC_DEVICE_POWER_LEAK;
                break;
            case 16:
                ec = ErrorCode.EC_DEVICE_COMM_ERROR;
                break;
        }
        return new ErrorCode(ec);
    }
}
