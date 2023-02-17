package com.xcharge.charger.device.c2.service;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.data.bean.device.NFC;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.PARK_STATUS;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.charger.device.api.PortStatusListener;
import com.xcharge.charger.device.c2.bean.DeviceBasicInfoData;
import com.xcharge.charger.device.c2.bean.NFCEventData;
import com.xcharge.charger.device.c2.bean.PortRuntimeData;
import com.xcharge.charger.device.c2.nfc.C2NFCAgent;
import com.xcharge.charger.device.c2.status.HardwareStatusHandler;
import com.xcharge.common.utils.LogUtils;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class C2DeviceEventDispatcher {
    public static final String EVENT_PARK_BUSY = "E@ParkBusy";
    public static final String EVENT_PARK_IDLE = "E@ParkIdle";
    public static final String EVENT_PARK_UNKOW = "E@ParkUnkow";
    public static final String EVENT_RADAR_CAKIBRATION = "E@RaderCalibration";
    public static final String EVENT_WARNING = "E@Warning";
    private static C2DeviceEventDispatcher instance = null;
    private Context context = null;
    private HardwareStatusHandler hardwareStatusHandler = null;
    private CopyOnWriteArrayList<PortStatusListener> portStatusListeners = null;

    public static C2DeviceEventDispatcher getInstance() {
        if (instance == null) {
            instance = new C2DeviceEventDispatcher();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        this.hardwareStatusHandler = new HardwareStatusHandler();
        this.hardwareStatusHandler.init(this.context);
        this.portStatusListeners = new CopyOnWriteArrayList<>();
    }

    public void destroy() {
        this.portStatusListeners.clear();
        this.hardwareStatusHandler.destroy();
    }

    public synchronized void attachPortStatusListener(PortStatusListener listener) {
        this.portStatusListeners.add(listener);
    }

    public synchronized void dettachPortStatusListener(PortStatusListener listener) {
        this.portStatusListeners.remove(listener);
    }

    public void dispatchEvent(Handler handler, String event) {
        try {
            if (TextUtils.isEmpty(event)) {
                Log.e("C2DeviceEventDispatcher.dispatchEvent", "receive empty device event !!!");
            } else {
                JSONObject json = new JSONObject(event);
                JSONArray names = json.names();
                if (names.length() != 1) {
                    Log.e("C2DeviceEventDispatcher.dispatchEvent", "illegal device event: " + event);
                } else {
                    Log.i("C2DeviceEventDispatcher.dispatchEvent", "receive device event: " + event);
                    String name = names.getString(0);
                    if (NFCEventData.EVENT_TAG.equals(name)) {
                        String data = json.getString(name);
                        NFCEventData nfcEventData = new NFCEventData().fromJson(data);
                        if (nfcEventData == null) {
                            Log.e("C2DeviceEventDispatcher.dispatchEvent", "illegal device event:" + name + ", content: " + data);
                        } else {
                            C2NFCAgent.getInstance(nfcEventData.getPort()).handleEvent(nfcEventData);
                        }
                    } else if (DeviceBasicInfoData.EVENT_GETINFO.equals(name)) {
                        new DeviceBasicInfoData().fromJson(json.getString(name));
                    } else if (EVENT_PARK_IDLE.equals(name)) {
                        onParkChanged("1", PARK_STATUS.idle);
                    } else if (EVENT_PARK_BUSY.equals(name)) {
                        onParkChanged("1", PARK_STATUS.occupied);
                    } else if (EVENT_PARK_UNKOW.equals(name)) {
                        onParkChanged("1", PARK_STATUS.unknown);
                    } else if (EVENT_WARNING.equals(name)) {
                        onWarning("1");
                    } else if (EVENT_RADAR_CAKIBRATION.equals(name)) {
                        JSONObject radarCalibration = json.getJSONObject(EVENT_RADAR_CAKIBRATION);
                        String msg = radarCalibration.getString("msg");
                        if ("successful".equals(msg)) {
                            onRaderCalibration("1", true);
                        } else if ("fail".equals(msg)) {
                            onRaderCalibration("1", false);
                        }
                    } else {
                        String data2 = json.getString(name);
                        PortRuntimeData chargeEventData = new PortRuntimeData().fromJson(data2);
                        if (chargeEventData == null) {
                            Log.e("C2DeviceEventDispatcher.dispatchEvent", "illegal device event:" + name + ", content: " + data2);
                        } else {
                            String port = chargeEventData.getPort();
                            if (PortRuntimeData.EVENT_PLUG_IN.equals(name)) {
                                onPlugChanged(port, true, chargeEventData);
                            } else if (PortRuntimeData.EVENT_PLUG_OUT.equals(name)) {
                                onPlugChanged(port, false, chargeEventData);
                            } else if (PortRuntimeData.EVENT_CHARGING_START.equals(name)) {
                                onChargeChanged(port, true, chargeEventData);
                            } else if (PortRuntimeData.EVENT_CHARGING_STOP.equals(name)) {
                                onChargeChanged(port, false, chargeEventData);
                            } else if (PortRuntimeData.EVENT_CHARGING_FULL.equals(name)) {
                                onChargingFull(port, chargeEventData);
                            } else if (PortRuntimeData.EVENT_SUSPEND.equals(name)) {
                                onPauseChanged(port, true, chargeEventData);
                            } else if (PortRuntimeData.EVENT_RESUME.equals(name)) {
                                onPauseChanged(port, false, chargeEventData);
                            } else if (PortRuntimeData.EVENT_UPDATE.equals(name)) {
                                onUpdate(port, chargeEventData);
                            } else if (PortRuntimeData.EVENT_AUTH_VALID.equals(name)) {
                                onAuthChanged(port, true, chargeEventData);
                            } else if (PortRuntimeData.EVENT_AUTH_INVALID.equals(name)) {
                                onAuthChanged(port, false, chargeEventData);
                            } else {
                                Log.e("C2DeviceEventDispatcher.dispatchEvent", "unsupported device event:" + event);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("DeviceEventDispatcher.dispatchEvent", Log.getStackTraceString(e));
            LogUtils.syslog("DeviceEventDispatcher dispatchEvent exception: " + Log.getStackTraceString(e));
        }
    }

    private void onAuthChanged(String port, boolean isAuthValid, PortRuntimeData data) {
        if (isAuthValid) {
            this.hardwareStatusHandler.sendMessage(this.hardwareStatusHandler.obtainMessage(HardwareStatusHandler.MSG_PORT_AUTH_VALID, data));
        } else {
            this.hardwareStatusHandler.sendMessage(this.hardwareStatusHandler.obtainMessage(HardwareStatusHandler.MSG_PORT_AUTH_INVALID, data));
        }
        PortStatus portStatus = C2DeviceProxy.getInstance().createPortChargeStatusFrom(data);
        Iterator<PortStatusListener> iterator = this.portStatusListeners.iterator();
        while (iterator.hasNext()) {
            PortStatusListener portStatusListener = iterator.next();
            if (portStatusListener != null) {
                if (isAuthValid) {
                    portStatusListener.onAuthValid(port, portStatus);
                } else {
                    portStatusListener.onAuthInvalid(port, portStatus);
                }
            }
        }
    }

    private void onPlugChanged(String port, boolean isPlugIn, PortRuntimeData data) {
        if (isPlugIn) {
            this.hardwareStatusHandler.sendMessage(this.hardwareStatusHandler.obtainMessage(HardwareStatusHandler.MSG_PORT_PLUGIN, data));
        } else {
            this.hardwareStatusHandler.sendMessage(this.hardwareStatusHandler.obtainMessage(HardwareStatusHandler.MSG_PORT_PLUGOUT, data));
        }
        PortStatus portStatus = C2DeviceProxy.getInstance().createPortChargeStatusFrom(data);
        Iterator<PortStatusListener> iterator = this.portStatusListeners.iterator();
        while (iterator.hasNext()) {
            PortStatusListener portStatusListener = iterator.next();
            if (portStatusListener != null) {
                if (isPlugIn) {
                    portStatusListener.onPlugin(port, portStatus);
                } else {
                    portStatusListener.onPlugout(port, portStatus);
                }
            }
        }
    }

    private void onChargeChanged(String port, boolean isStart, PortRuntimeData data) {
        if (isStart) {
            this.hardwareStatusHandler.sendMessage(this.hardwareStatusHandler.obtainMessage(HardwareStatusHandler.MSG_PORT_CHARGE_STARTED, data));
        } else {
            this.hardwareStatusHandler.sendMessage(this.hardwareStatusHandler.obtainMessage(HardwareStatusHandler.MSG_PORT_CHARGE_STOPPED, data));
        }
        PortStatus portStatus = C2DeviceProxy.getInstance().createPortChargeStatusFrom(data);
        Iterator<PortStatusListener> iterator = this.portStatusListeners.iterator();
        while (iterator.hasNext()) {
            PortStatusListener portStatusListener = iterator.next();
            if (portStatusListener != null) {
                if (isStart) {
                    portStatusListener.onChargeStart(port, portStatus);
                } else {
                    portStatusListener.onChargeStop(port, portStatus);
                }
            }
        }
    }

    private void onChargingFull(String port, PortRuntimeData data) {
        this.hardwareStatusHandler.sendMessage(this.hardwareStatusHandler.obtainMessage(HardwareStatusHandler.MSG_PORT_CHARGE_FULL, data));
        PortStatus portStatus = C2DeviceProxy.getInstance().createPortChargeStatusFrom(data);
        Iterator<PortStatusListener> iterator = this.portStatusListeners.iterator();
        while (iterator.hasNext()) {
            PortStatusListener portStatusListener = iterator.next();
            if (portStatusListener != null) {
                portStatusListener.onChargeFull(port, portStatus);
            }
        }
    }

    private void onPauseChanged(String port, boolean isPause, PortRuntimeData data) {
        if (isPause) {
            this.hardwareStatusHandler.sendMessage(this.hardwareStatusHandler.obtainMessage(HardwareStatusHandler.MSG_PORT_SUSPEND, data));
        } else {
            this.hardwareStatusHandler.sendMessage(this.hardwareStatusHandler.obtainMessage(HardwareStatusHandler.MSG_PORT_RESUME, data));
        }
        PortStatus portStatus = C2DeviceProxy.getInstance().createPortChargeStatusFrom(data);
        Iterator<PortStatusListener> iterator = this.portStatusListeners.iterator();
        while (iterator.hasNext()) {
            PortStatusListener portStatusListener = iterator.next();
            if (portStatusListener != null) {
                if (isPause) {
                    portStatusListener.onSuspend(port, portStatus);
                } else {
                    portStatusListener.onResume(port, portStatus);
                }
            }
        }
    }

    private void onWarning(String port) {
        PortRuntimeData data = new PortRuntimeData();
        data.setPort(port);
        this.hardwareStatusHandler.sendMessage(this.hardwareStatusHandler.obtainMessage(HardwareStatusHandler.MSG_PORT_WARN, data));
        Iterator<PortStatusListener> iterator = this.portStatusListeners.iterator();
        while (iterator.hasNext()) {
            PortStatusListener portStatusListener = iterator.next();
            if (portStatusListener != null) {
                portStatusListener.onWarning(port);
            }
        }
    }

    private void onUpdate(String port, PortRuntimeData data) {
        this.hardwareStatusHandler.sendMessage(this.hardwareStatusHandler.obtainMessage(HardwareStatusHandler.MSG_PORT_UPDATE, data));
        PortStatus portStatus = C2DeviceProxy.getInstance().createPortChargeStatusFrom(data);
        Iterator<PortStatusListener> iterator = this.portStatusListeners.iterator();
        while (iterator.hasNext()) {
            PortStatusListener portStatusListener = iterator.next();
            if (portStatusListener != null) {
                portStatusListener.onUpdate(port, portStatus);
            }
        }
    }

    private void onParkChanged(String port, PARK_STATUS status) {
        PortRuntimeData data = new PortRuntimeData();
        data.setPort(port);
        data.setParkStatus(status);
        this.hardwareStatusHandler.sendMessage(this.hardwareStatusHandler.obtainMessage(HardwareStatusHandler.MSG_PORT_PARK_STATUS, data));
        Iterator<PortStatusListener> iterator = this.portStatusListeners.iterator();
        while (iterator.hasNext()) {
            PortStatusListener portStatusListener = iterator.next();
            if (portStatusListener != null) {
                if (PARK_STATUS.occupied.equals(status)) {
                    portStatusListener.onParkBusy(port);
                } else if (PARK_STATUS.idle.equals(status)) {
                    portStatusListener.onParkIdle(port);
                } else if (PARK_STATUS.unknown.equals(status)) {
                    portStatusListener.onParkUnkow(port);
                }
            }
        }
    }

    private void onRaderCalibration(String port, boolean isSuccess) {
        PortRuntimeData data = new PortRuntimeData();
        data.setPort(port);
        data.setIsRadarCalibrated(Boolean.valueOf(isSuccess));
        this.hardwareStatusHandler.sendMessage(this.hardwareStatusHandler.obtainMessage(HardwareStatusHandler.MSG_PORT_RADAR_CALIBRATION, data));
        Iterator<PortStatusListener> iterator = this.portStatusListeners.iterator();
        while (iterator.hasNext()) {
            PortStatusListener portStatusListener = iterator.next();
            if (portStatusListener != null) {
                portStatusListener.onRadarCalibration(port, isSuccess);
            }
        }
    }

    public void handleNFCStatus(String port, NFC nfcStatus) {
        Bundle data = new Bundle();
        data.putString(ContentDB.ChargeTable.PORT, port);
        Message msg = this.hardwareStatusHandler.obtainMessage(HardwareStatusHandler.MSG_PORT_NFC_STATUS, nfcStatus.m8clone());
        msg.setData(data);
        this.hardwareStatusHandler.sendMessage(msg);
    }

    public void handleNetworkStatus(boolean isConnected) {
        this.hardwareStatusHandler.sendMessage(this.hardwareStatusHandler.obtainMessage(HardwareStatusHandler.MSG_NETWORK_CONNECTION, Boolean.valueOf(isConnected)));
    }

    public void notifyPortStatusUpdatedByCmd(Port portStatus) {
        this.hardwareStatusHandler.sendMessage(this.hardwareStatusHandler.obtainMessage(HardwareStatusHandler.MSG_PORT_UPDATE_BY_CMD, portStatus));
    }
}
