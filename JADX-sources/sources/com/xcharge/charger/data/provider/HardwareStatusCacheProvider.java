package com.xcharge.charger.data.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.device.BLN;
import com.xcharge.charger.data.bean.device.Ethernet;
import com.xcharge.charger.data.bean.device.Hardware;
import com.xcharge.charger.data.bean.device.MobileNet;
import com.xcharge.charger.data.bean.device.NFC;
import com.xcharge.charger.data.bean.device.Network;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.device.Wifi;
import com.xcharge.charger.data.bean.device.ZigBee;
import com.xcharge.charger.data.bean.setting.APNSetting;
import com.xcharge.charger.data.bean.type.PHASE;
import com.xcharge.charger.data.bean.type.SWITCH_STATUS;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* loaded from: classes.dex */
public class HardwareStatusCacheProvider {
    public static final String AUTHORITY = "com.xcharge.charger.data.provider.device";
    public static final String PATH = "hardware";
    public static final Uri CONTENT_URI = Uri.parse("content://com.xcharge.charger.data.provider.device/hardware");
    private static HardwareStatusCacheProvider instance = null;
    private Context context = null;
    private ContentResolver resolver = null;
    private AtomicReference<Hardware> cache = new AtomicReference<>(new Hardware());
    private List<Integer> priorityErrors = Arrays.asList(Integer.valueOf((int) ErrorCode.EC_DEVICE_NO_GROUND), Integer.valueOf((int) ErrorCode.EC_DEVICE_EMERGENCY_STOP), Integer.valueOf((int) ErrorCode.EC_DEVICE_COMM_ERROR), Integer.valueOf((int) ErrorCode.EC_DEVICE_LOST_PHASE), Integer.valueOf((int) ErrorCode.EC_DEVICE_VOLT_ERROR), Integer.valueOf((int) ErrorCode.EC_DEVICE_POWER_LEAK), Integer.valueOf((int) ErrorCode.EC_DEVICE_AMP_ERROR));

    public static HardwareStatusCacheProvider getInstance() {
        if (instance == null) {
            instance = new HardwareStatusCacheProvider();
        }
        return instance;
    }

    public void init(Context context, Hardware status) {
        this.context = context;
        this.resolver = this.context.getContentResolver();
        if (status != null) {
            this.cache.set(status);
        }
    }

    public void destroy() {
    }

    public Uri getUriFor(String subPath) {
        String path = PATH;
        if (!TextUtils.isEmpty(subPath)) {
            path = String.valueOf(PATH) + MqttTopic.TOPIC_LEVEL_SEPARATOR + subPath;
        }
        return Uri.parse("content://com.xcharge.charger.data.provider.device/" + path);
    }

    private void notifyChange(Uri uri) {
        this.resolver.notifyChange(uri, null);
    }

    public synchronized Hardware getHardwareStatus() {
        return this.cache.get();
    }

    public synchronized BLN getBLNStatus() {
        return this.cache.get().getBln();
    }

    public synchronized boolean updateBLNStatus(BLN status) {
        this.cache.get().setBln(status);
        notifyChange(getUriFor(BLN.class.getSimpleName()));
        return true;
    }

    public synchronized int getDefaultBLNColor() {
        BLN bln;
        bln = this.cache.get().getBln();
        return bln != null ? bln.getDefaultColor() : 65280;
    }

    public synchronized boolean updateDefaultBLNColor(int color) {
        BLN bln = this.cache.get().getBln();
        if (bln == null) {
            bln = new BLN();
        }
        bln.setDefaultColor(color);
        this.cache.get().setBln(bln);
        notifyChange(getUriFor(String.valueOf(BLN.class.getSimpleName()) + "/defaultBLNColor"));
        return true;
    }

    public synchronized Network getNetworkStatus() {
        return this.cache.get().getNetwork();
    }

    public synchronized boolean updateNetworkStatus(Network status) {
        this.cache.get().setNetwork(status);
        notifyChange(getUriFor(Network.class.getSimpleName()));
        return true;
    }

    public synchronized Ethernet getEthernetStatus() {
        Network network;
        network = this.cache.get().getNetwork();
        return network != null ? network.getEthernet() : null;
    }

    public synchronized boolean updateEthernetStatus(Ethernet status) {
        Network network = this.cache.get().getNetwork();
        if (network == null) {
            network = new Network();
        }
        network.setEthernet(status);
        this.cache.get().setNetwork(network);
        notifyChange(getUriFor(String.valueOf(Network.class.getSimpleName()) + MqttTopic.TOPIC_LEVEL_SEPARATOR + Ethernet.class.getSimpleName()));
        return true;
    }

    public synchronized MobileNet getMobileNetStatus() {
        Network network;
        network = this.cache.get().getNetwork();
        return network != null ? network.getMobile() : null;
    }

    public synchronized boolean updateMobileNetStatus(MobileNet status) {
        Network network = this.cache.get().getNetwork();
        if (network == null) {
            network = new Network();
        }
        network.setMobile(status);
        this.cache.get().setNetwork(network);
        notifyChange(getUriFor(String.valueOf(Network.class.getSimpleName()) + MqttTopic.TOPIC_LEVEL_SEPARATOR + MobileNet.class.getSimpleName()));
        return true;
    }

    public synchronized boolean updateMobileNetSignal(int level, int signal, int asu) {
        Network network = this.cache.get().getNetwork();
        if (network == null) {
            network = new Network();
        }
        MobileNet mobile = network.getMobile();
        if (mobile == null) {
            mobile = new MobileNet();
        }
        int oldLevel = mobile.getDefaultSignalLevel();
        mobile.setDefaultSignalLevel(level);
        mobile.setSignalDbm(signal);
        mobile.setAsu(asu);
        network.setMobile(mobile);
        this.cache.get().setNetwork(network);
        if (oldLevel != level) {
            notifyChange(getUriFor(String.valueOf(Network.class.getSimpleName()) + MqttTopic.TOPIC_LEVEL_SEPARATOR + MobileNet.class.getSimpleName() + "/signal/" + level));
        }
        return true;
    }

    public synchronized String getSimState() {
        MobileNet mobileNet;
        mobileNet = getMobileNetStatus();
        return mobileNet != null ? mobileNet.getSimState() : "unknown";
    }

    public synchronized boolean isSimOk() {
        String simState;
        simState = getSimState();
        return MobileNet.SIM_STATE_OK.equals(simState);
    }

    public synchronized boolean updateSimState(String state) {
        Network network = this.cache.get().getNetwork();
        if (network == null) {
            network = new Network();
        }
        MobileNet mobile = network.getMobile();
        if (mobile == null) {
            mobile = new MobileNet();
        }
        String oldState = mobile.getSimState();
        mobile.setSimState(state);
        network.setMobile(mobile);
        this.cache.get().setNetwork(network);
        if (!oldState.equals(state)) {
            notifyChange(getUriFor(String.valueOf(Network.class.getSimpleName()) + MqttTopic.TOPIC_LEVEL_SEPARATOR + MobileNet.class.getSimpleName() + "/sim/state/" + state));
        }
        return true;
    }

    public synchronized boolean updateSimIdInfo(String imsi, String iccid) {
        Network network = this.cache.get().getNetwork();
        if (network == null) {
            network = new Network();
        }
        MobileNet mobile = network.getMobile();
        if (mobile == null) {
            mobile = new MobileNet();
        }
        mobile.setIMSI(imsi);
        mobile.setICCID(iccid);
        network.setMobile(mobile);
        this.cache.get().setNetwork(network);
        return true;
    }

    public synchronized boolean updateSimNetInfo(String mcc, String mnc) {
        Network network = this.cache.get().getNetwork();
        if (network == null) {
            network = new Network();
        }
        MobileNet mobile = network.getMobile();
        if (mobile == null) {
            mobile = new MobileNet();
        }
        mobile.setSimMCC(mcc);
        mobile.setSimMNC(mnc);
        network.setMobile(mobile);
        this.cache.get().setNetwork(network);
        return true;
    }

    public synchronized APNSetting getPreferAPN() {
        MobileNet mobileNet;
        mobileNet = getMobileNetStatus();
        return mobileNet != null ? mobileNet.getPreferApn() : null;
    }

    public synchronized boolean updatePreferAPN(APNSetting preferApn) {
        Network network = this.cache.get().getNetwork();
        if (network == null) {
            network = new Network();
        }
        MobileNet mobile = network.getMobile();
        if (mobile == null) {
            mobile = new MobileNet();
        }
        mobile.setPreferApn(preferApn);
        network.setMobile(mobile);
        this.cache.get().setNetwork(network);
        return true;
    }

    public synchronized Wifi getWiFiStatus() {
        Network network;
        network = this.cache.get().getNetwork();
        return network != null ? network.getWifi() : null;
    }

    public synchronized boolean updateWifiStatus(Wifi status) {
        Network network = this.cache.get().getNetwork();
        if (network == null) {
            network = new Network();
        }
        network.setWifi(status);
        this.cache.get().setNetwork(network);
        notifyChange(getUriFor(String.valueOf(Network.class.getSimpleName()) + MqttTopic.TOPIC_LEVEL_SEPARATOR + Wifi.class.getSimpleName()));
        return true;
    }

    public synchronized ZigBee getZigBeeStatus() {
        Network network;
        network = this.cache.get().getNetwork();
        return network != null ? network.getZigbee() : null;
    }

    public synchronized boolean updateZigBeeStatus(ZigBee status) {
        Network network = this.cache.get().getNetwork();
        if (network == null) {
            network = new Network();
        }
        network.setZigbee(status);
        this.cache.get().setNetwork(network);
        notifyChange(getUriFor(String.valueOf(Network.class.getSimpleName()) + MqttTopic.TOPIC_LEVEL_SEPARATOR + ZigBee.class.getSimpleName()));
        return true;
    }

    public synchronized String getPreferenceNetwork() {
        Network network;
        network = this.cache.get().getNetwork();
        return network != null ? network.getPreference() : "none";
    }

    public synchronized boolean updatePreferenceNetwork(String preference) {
        Network network = this.cache.get().getNetwork();
        if (network == null) {
            network = new Network();
        }
        network.setPreference(preference);
        this.cache.get().setNetwork(network);
        notifyChange(getUriFor(String.valueOf(Network.class.getSimpleName()) + "/preference"));
        return true;
    }

    public synchronized String getActiveNetwork() {
        Network network;
        network = this.cache.get().getNetwork();
        return network != null ? network.getActive() : "none";
    }

    public synchronized boolean isNetworkConnected() {
        Network network;
        network = this.cache.get().getNetwork();
        return network != null ? network.isConnected() : false;
    }

    public synchronized boolean updateActiveNetwork(String active, boolean connected) {
        Network network = this.cache.get().getNetwork();
        if (network == null) {
            network = new Network();
        }
        network.setActive(active);
        network.setConnected(connected);
        this.cache.get().setNetwork(network);
        notifyChange(getUriFor(String.valueOf(Network.class.getSimpleName()) + "/active/" + active + MqttTopic.TOPIC_LEVEL_SEPARATOR + (connected ? "connected" : "disconnected")));
        return true;
    }

    public synchronized HashMap<String, Port> getPorts() {
        return this.cache.get().getPorts();
    }

    public synchronized boolean updatePorts(HashMap<String, Port> ports) {
        if (this.cache.get().getPorts() == null && ports != null && this.cache.get().getDeviceError().getCode() == 30002) {
            updateDeviceFaultStatus(new ErrorCode(200));
        }
        this.cache.get().setPorts(ports);
        notifyChange(getUriFor("ports"));
        return true;
    }

    public synchronized Port getPort(String port) {
        HashMap<String, Port> ports;
        ports = this.cache.get().getPorts();
        return ports != null ? ports.get(port) : null;
    }

    public synchronized boolean updatePort(Port port) {
        HashMap<String, Port> ports = this.cache.get().getPorts();
        if (ports == null) {
            ports = new HashMap<>();
        }
        ports.put(port.getPort(), port);
        this.cache.get().setPorts(ports);
        notifyChange(getUriFor("ports/" + port.getPort()));
        return true;
    }

    public synchronized ErrorCode getPortFault(String port) {
        HashMap<String, Port> ports;
        Port portStatus;
        ports = this.cache.get().getPorts();
        return (ports == null || (portStatus = ports.get(port)) == null) ? null : portStatus.getDeviceError();
    }

    public synchronized boolean updatePortFault(String port, ErrorCode fault) {
        HashMap<String, Port> portsStatus = this.cache.get().getPorts();
        if (portsStatus == null) {
            portsStatus = new HashMap<>();
        }
        Port portStatus = portsStatus.get(port);
        if (portStatus == null) {
            portStatus = new Port();
        }
        portStatus.setDeviceError(fault);
        portsStatus.put(port, portStatus);
        this.cache.get().setPorts(portsStatus);
        notifyChange(getUriFor("ports/" + port + "/fault"));
        return true;
    }

    public synchronized boolean getPortPluginStatus(String port) {
        HashMap<String, Port> ports;
        Port portStatus;
        ports = this.cache.get().getPorts();
        return (ports == null || (portStatus = ports.get(port)) == null) ? false : portStatus.isPlugin();
    }

    public synchronized boolean updatePortPluginStatus(String port, boolean isPlugin) {
        HashMap<String, Port> portsStatus = this.cache.get().getPorts();
        if (portsStatus == null) {
            portsStatus = new HashMap<>();
        }
        Port portStatus = portsStatus.get(port);
        if (portStatus == null) {
            portStatus = new Port();
        }
        boolean oldStatus = portStatus.isPlugin();
        portStatus.setPlugin(isPlugin);
        portsStatus.put(port, portStatus);
        this.cache.get().setPorts(portsStatus);
        if (oldStatus != isPlugin) {
            notifyChange(getUriFor("ports/" + port + "/plugin"));
        }
        return true;
    }

    public synchronized NFC getPortNFCStatus(String port) {
        HashMap<String, Port> ports;
        Port portStatus;
        ports = this.cache.get().getPorts();
        return (ports == null || (portStatus = ports.get(port)) == null) ? null : portStatus.getNfcStatus();
    }

    public synchronized boolean updatePortNFCStatus(String port, NFC nfcStatus) {
        boolean z;
        Port portStatus;
        HashMap<String, Port> ports = this.cache.get().getPorts();
        if (ports == null || (portStatus = ports.get(port)) == null) {
            z = false;
        } else {
            portStatus.setNfcStatus(nfcStatus);
            ports.put(port, portStatus);
            this.cache.get().setPorts(ports);
            notifyChange(getUriFor("ports/" + port + "/nfc/" + nfcStatus.toJson()));
            z = true;
        }
        return z;
    }

    public synchronized String getSignatureCode() {
        return this.cache.get().getSignatureCode();
    }

    public synchronized boolean updateSignatureCode(String code) {
        this.cache.get().setSignatureCode(code);
        notifyChange(getUriFor("signatureCode"));
        return true;
    }

    public synchronized String getPid() {
        return this.cache.get().getPid();
    }

    public synchronized boolean updatePid(String id) {
        this.cache.get().setPid(id);
        notifyChange(getUriFor("pid"));
        return true;
    }

    public synchronized String getSn() {
        return this.cache.get().getSn();
    }

    public synchronized boolean updateSn(String sn) {
        if (TextUtils.isEmpty(this.cache.get().getSn()) && !TextUtils.isEmpty(sn) && this.cache.get().getDeviceError().getCode() == 30001) {
            updateDeviceFaultStatus(new ErrorCode(200));
        }
        this.cache.get().setSn(sn);
        notifyChange(getUriFor("sn"));
        return true;
    }

    public synchronized double getAmpCapacity() {
        return this.cache.get().getAmpCapacity();
    }

    public synchronized boolean updateAmpCapacity(double amp) {
        this.cache.get().setAmpCapacity(amp);
        notifyChange(getUriFor("amp/capacity"));
        return true;
    }

    public synchronized PHASE getPhase() {
        return this.cache.get().getPhase();
    }

    public synchronized ErrorCode getDeviceFaultStatus() {
        return this.cache.get().getDeviceError();
    }

    public synchronized boolean updateDeviceFaultStatus(ErrorCode fault) {
        this.cache.get().setDeviceError(fault);
        notifyChange(getUriFor("device/fault"));
        return true;
    }

    public synchronized boolean getPortRadarSwitch(String port) {
        boolean z = false;
        synchronized (this) {
            Port portStatus = getPort(port);
            if (portStatus != null) {
                if (!portStatus.getRadar().getStatus().equals(SWITCH_STATUS.disable)) {
                    z = true;
                }
            }
        }
        return z;
    }

    public synchronized boolean updatePortRadarSwitch(String port, boolean isEnable) {
        HashMap<String, Port> portsStatus = this.cache.get().getPorts();
        if (portsStatus == null) {
            portsStatus = new HashMap<>();
        }
        Port portStatus = portsStatus.get(port);
        if (portStatus == null) {
            portStatus = new Port();
        }
        SWITCH_STATUS nowRadarStatus = portStatus.getRadar().getStatus();
        SWITCH_STATUS setRadarStatus = isEnable ? nowRadarStatus.equals(SWITCH_STATUS.disable) ? SWITCH_STATUS.on : nowRadarStatus : SWITCH_STATUS.disable;
        portStatus.getRadar().setStatus(setRadarStatus);
        portsStatus.put(port, portStatus);
        this.cache.get().setPorts(portsStatus);
        if (!nowRadarStatus.equals(setRadarStatus)) {
            notifyChange(getUriFor("ports/radar/status/" + port));
        }
        return true;
    }

    public synchronized HashMap<String, ErrorCode> getAllDeviceErrors(String port) {
        HashMap<String, ErrorCode> cloned;
        HashMap<String, ErrorCode> allErrors;
        Port portStatus = getPort(port);
        if (portStatus != null && (allErrors = portStatus.getAllDeviceErrorCache()) != null && allErrors.size() > 0) {
            cloned = new HashMap<>();
            for (Map.Entry<String, ErrorCode> entry : allErrors.entrySet()) {
                cloned.put(entry.getKey(), entry.getValue());
            }
        } else {
            cloned = null;
        }
        return cloned;
    }

    public synchronized boolean hasDeviceErrors(String port) {
        boolean z;
        HashMap<String, ErrorCode> errors = getAllDeviceErrors(port);
        if (errors != null) {
            z = errors.size() > 0;
        }
        return z;
    }

    public synchronized boolean hasDeviceError(String port, ErrorCode error) {
        HashMap<String, ErrorCode> errors;
        errors = getAllDeviceErrors(port);
        return (errors == null || errors.size() <= 0) ? false : errors.containsKey(String.valueOf(error.getCode()));
    }

    /* JADX WARN: Code restructure failed: missing block: B:40:0x0029, code lost:
        if (r2.size() > 0) goto L20;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public synchronized java.util.HashMap<java.lang.String, com.xcharge.charger.data.bean.ErrorCode> getHigherPriorityDeviceErrors(java.lang.String r8, com.xcharge.charger.data.bean.ErrorCode r9) {
        /*
            r7 = this;
            monitor-enter(r7)
            java.util.HashMap r0 = r7.getAllDeviceErrors(r8)     // Catch: java.lang.Throwable -> L4f
            if (r0 == 0) goto L4d
            int r5 = r0.size()     // Catch: java.lang.Throwable -> L4f
            if (r5 <= 0) goto L4d
            java.util.List<java.lang.Integer> r5 = r7.priorityErrors     // Catch: java.lang.Throwable -> L4f
            int r6 = r9.getCode()     // Catch: java.lang.Throwable -> L4f
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)     // Catch: java.lang.Throwable -> L4f
            int r4 = r5.indexOf(r6)     // Catch: java.lang.Throwable -> L4f
            if (r4 <= 0) goto L4d
            java.util.HashMap r2 = new java.util.HashMap     // Catch: java.lang.Throwable -> L4f
            r2.<init>()     // Catch: java.lang.Throwable -> L4f
            r3 = 0
        L23:
            if (r3 < r4) goto L2d
            int r5 = r2.size()     // Catch: java.lang.Throwable -> L4f
            if (r5 <= 0) goto L4d
        L2b:
            monitor-exit(r7)
            return r2
        L2d:
            java.util.List<java.lang.Integer> r5 = r7.priorityErrors     // Catch: java.lang.Throwable -> L4f
            java.lang.Object r5 = r5.get(r3)     // Catch: java.lang.Throwable -> L4f
            java.lang.String r5 = java.lang.String.valueOf(r5)     // Catch: java.lang.Throwable -> L4f
            java.lang.Object r1 = r0.get(r5)     // Catch: java.lang.Throwable -> L4f
            com.xcharge.charger.data.bean.ErrorCode r1 = (com.xcharge.charger.data.bean.ErrorCode) r1     // Catch: java.lang.Throwable -> L4f
            if (r1 == 0) goto L4a
            int r5 = r1.getCode()     // Catch: java.lang.Throwable -> L4f
            java.lang.String r5 = java.lang.String.valueOf(r5)     // Catch: java.lang.Throwable -> L4f
            r2.put(r5, r1)     // Catch: java.lang.Throwable -> L4f
        L4a:
            int r3 = r3 + 1
            goto L23
        L4d:
            r2 = 0
            goto L2b
        L4f:
            r5 = move-exception
            monitor-exit(r7)
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.data.provider.HardwareStatusCacheProvider.getHigherPriorityDeviceErrors(java.lang.String, com.xcharge.charger.data.bean.ErrorCode):java.util.HashMap");
    }

    public synchronized int removeHigherPriorityDeviceErrors(String port, ErrorCode error) {
        int cnt;
        int idx;
        cnt = 0;
        HashMap<String, ErrorCode> errors = getAllDeviceErrors(port);
        if (errors != null && errors.size() > 0 && (idx = this.priorityErrors.indexOf(Integer.valueOf(error.getCode()))) > 0) {
            for (int i = 0; i < idx; i++) {
                ErrorCode higherPriorityDeviceError = errors.get(String.valueOf(this.priorityErrors.get(i)));
                if (higherPriorityDeviceError != null) {
                    removeDeviceError(port, higherPriorityDeviceError);
                    cnt++;
                }
            }
        }
        return cnt;
    }

    public synchronized boolean updateAllDeviceErrors(String port, HashMap<String, ErrorCode> errors) {
        HashMap<String, Port> portsStatus = this.cache.get().getPorts();
        if (portsStatus == null) {
            portsStatus = new HashMap<>();
        }
        Port portStatus = portsStatus.get(port);
        if (portStatus == null) {
            portStatus = new Port();
        }
        portStatus.setAllDeviceErrorCache(errors);
        portsStatus.put(port, portStatus);
        this.cache.get().setPorts(portsStatus);
        notifyChange(getUriFor("ports/fault/" + port + "/all"));
        return true;
    }

    public synchronized boolean putDeviceError(String port, ErrorCode error) {
        boolean z;
        HashMap<String, Port> portsStatus = this.cache.get().getPorts();
        if (portsStatus == null) {
            portsStatus = new HashMap<>();
        }
        Port portStatus = portsStatus.get(port);
        if (portStatus == null) {
            portStatus = new Port();
        }
        HashMap<String, ErrorCode> errors = portStatus.getAllDeviceErrorCache();
        if (errors == null) {
            errors = new HashMap<>();
        }
        String code = String.valueOf(error.getCode());
        if (errors.containsKey(code)) {
            z = false;
        } else {
            errors.put(code, error);
            portStatus.setAllDeviceErrorCache(errors);
            portsStatus.put(port, portStatus);
            this.cache.get().setPorts(portsStatus);
            notifyChange(getUriFor("ports/fault/" + port + "/new/" + code));
            z = true;
        }
        return z;
    }

    public synchronized boolean removeDeviceError(String port, ErrorCode error) {
        boolean z;
        Port portStatus;
        HashMap<String, Port> portsStatus = this.cache.get().getPorts();
        if (portsStatus != null && (portStatus = portsStatus.get(port)) != null) {
            if (error == null) {
                portStatus.setAllDeviceErrorCache(null);
                portsStatus.put(port, portStatus);
                this.cache.get().setPorts(portsStatus);
                notifyChange(getUriFor("ports/fault/" + port + "/remove/200"));
                z = true;
            } else {
                HashMap<String, ErrorCode> errors = portStatus.getAllDeviceErrorCache();
                if (errors != null) {
                    String code = String.valueOf(error.getCode());
                    if (errors.containsKey(code)) {
                        errors.remove(code);
                        portStatus.setAllDeviceErrorCache(errors);
                        portsStatus.put(port, portStatus);
                        this.cache.get().setPorts(portsStatus);
                        notifyChange(getUriFor("ports/fault/" + port + "/remove/" + code));
                        z = true;
                    }
                }
            }
        }
        z = false;
        return z;
    }

    public synchronized double getPortAmmeter(String port) {
        HashMap<String, Port> ports;
        Port portStatus;
        ports = this.cache.get().getPorts();
        return (ports == null || (portStatus = ports.get(port)) == null) ? 0.0d : portStatus.getMeter().doubleValue();
    }

    public synchronized boolean updatePortAmmeter(String port, double ammeter) {
        HashMap<String, Port> portsStatus = this.cache.get().getPorts();
        if (portsStatus == null) {
            portsStatus = new HashMap<>();
        }
        Port portStatus = portsStatus.get(port);
        if (portStatus == null) {
            portStatus = new Port();
        }
        Double oldMeter = portStatus.getMeter();
        portStatus.setMeter(Double.valueOf(ammeter));
        portsStatus.put(port, portStatus);
        this.cache.get().setPorts(portsStatus);
        if (oldMeter != null && oldMeter.doubleValue() != ammeter) {
            notifyChange(getUriFor("ports/" + port + "/ammeter"));
        }
        return true;
    }
}
