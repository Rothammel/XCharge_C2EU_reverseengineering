package com.xcharge.charger.data.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
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

public class HardwareStatusCacheProvider {
    public static final String AUTHORITY = "com.xcharge.charger.data.provider.device";
    public static final Uri CONTENT_URI = Uri.parse("content://com.xcharge.charger.data.provider.device/hardware");
    public static final String PATH = "hardware";
    private static HardwareStatusCacheProvider instance = null;
    private AtomicReference<Hardware> cache = new AtomicReference<>(new Hardware());
    private Context context = null;
    private List<Integer> priorityErrors = Arrays.asList(new Integer[]{Integer.valueOf(ErrorCode.EC_DEVICE_NO_GROUND), Integer.valueOf(ErrorCode.EC_DEVICE_EMERGENCY_STOP), Integer.valueOf(ErrorCode.EC_DEVICE_COMM_ERROR), Integer.valueOf(ErrorCode.EC_DEVICE_LOST_PHASE), Integer.valueOf(ErrorCode.EC_DEVICE_VOLT_ERROR), Integer.valueOf(ErrorCode.EC_DEVICE_POWER_LEAK), Integer.valueOf(ErrorCode.EC_DEVICE_AMP_ERROR)});
    private ContentResolver resolver = null;

    public static HardwareStatusCacheProvider getInstance() {
        if (instance == null) {
            instance = new HardwareStatusCacheProvider();
        }
        return instance;
    }

    public void init(Context context2, Hardware status) {
        this.context = context2;
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
            path = String.valueOf(path) + MqttTopic.TOPIC_LEVEL_SEPARATOR + subPath;
        }
        return Uri.parse("content://com.xcharge.charger.data.provider.device/" + path);
    }

    private void notifyChange(Uri uri) {
        this.resolver.notifyChange(uri, (ContentObserver) null);
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
        int i;
        BLN bln = this.cache.get().getBln();
        if (bln != null) {
            i = bln.getDefaultColor();
        } else {
            i = 65280;
        }
        return i;
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
        Ethernet ethernet;
        Network network = this.cache.get().getNetwork();
        if (network != null) {
            ethernet = network.getEthernet();
        } else {
            ethernet = null;
        }
        return ethernet;
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
        MobileNet mobileNet;
        Network network = this.cache.get().getNetwork();
        if (network != null) {
            mobileNet = network.getMobile();
        } else {
            mobileNet = null;
        }
        return mobileNet;
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
        String str;
        MobileNet mobileNet = getMobileNetStatus();
        if (mobileNet != null) {
            str = mobileNet.getSimState();
        } else {
            str = "unknown";
        }
        return str;
    }

    public synchronized boolean isSimOk() {
        boolean z;
        if (MobileNet.SIM_STATE_OK.equals(getSimState())) {
            z = true;
        } else {
            z = false;
        }
        return z;
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
        APNSetting aPNSetting;
        MobileNet mobileNet = getMobileNetStatus();
        if (mobileNet != null) {
            aPNSetting = mobileNet.getPreferApn();
        } else {
            aPNSetting = null;
        }
        return aPNSetting;
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
        Wifi wifi;
        Network network = this.cache.get().getNetwork();
        if (network != null) {
            wifi = network.getWifi();
        } else {
            wifi = null;
        }
        return wifi;
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
        ZigBee zigBee;
        Network network = this.cache.get().getNetwork();
        if (network != null) {
            zigBee = network.getZigbee();
        } else {
            zigBee = null;
        }
        return zigBee;
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
        String str;
        Network network = this.cache.get().getNetwork();
        if (network != null) {
            str = network.getPreference();
        } else {
            str = "none";
        }
        return str;
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
        String str;
        Network network = this.cache.get().getNetwork();
        if (network != null) {
            str = network.getActive();
        } else {
            str = "none";
        }
        return str;
    }

    public synchronized boolean isNetworkConnected() {
        boolean z;
        Network network = this.cache.get().getNetwork();
        if (network != null) {
            z = network.isConnected();
        } else {
            z = false;
        }
        return z;
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
        Port port2;
        HashMap<String, Port> ports = this.cache.get().getPorts();
        if (ports != null) {
            port2 = ports.get(port);
        } else {
            port2 = null;
        }
        return port2;
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
        ErrorCode errorCode;
        Port portStatus;
        HashMap<String, Port> ports = this.cache.get().getPorts();
        if (ports == null || (portStatus = ports.get(port)) == null) {
            errorCode = null;
        } else {
            errorCode = portStatus.getDeviceError();
        }
        return errorCode;
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
        boolean z;
        Port portStatus;
        HashMap<String, Port> ports = this.cache.get().getPorts();
        if (ports == null || (portStatus = ports.get(port)) == null) {
            z = false;
        } else {
            z = portStatus.isPlugin();
        }
        return z;
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
        NFC nfc;
        Port portStatus;
        HashMap<String, Port> ports = this.cache.get().getPorts();
        if (ports == null || (portStatus = ports.get(port)) == null) {
            nfc = null;
        } else {
            nfc = portStatus.getNfcStatus();
        }
        return nfc;
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
            if (portStatus != null && !portStatus.getRadar().getStatus().equals(SWITCH_STATUS.disable)) {
                z = true;
            }
        }
        return z;
    }

    public synchronized boolean updatePortRadarSwitch(String port, boolean isEnable) {
        SWITCH_STATUS setRadarStatus;
        HashMap<String, Port> portsStatus = this.cache.get().getPorts();
        if (portsStatus == null) {
            portsStatus = new HashMap<>();
        }
        Port portStatus = portsStatus.get(port);
        if (portStatus == null) {
            portStatus = new Port();
        }
        SWITCH_STATUS nowRadarStatus = portStatus.getRadar().getStatus();
        if (!isEnable) {
            setRadarStatus = SWITCH_STATUS.disable;
        } else if (nowRadarStatus.equals(SWITCH_STATUS.disable)) {
            setRadarStatus = SWITCH_STATUS.on;
        } else {
            setRadarStatus = nowRadarStatus;
        }
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
        if (portStatus == null || (allErrors = portStatus.getAllDeviceErrorCache()) == null || allErrors.size() <= 0) {
            cloned = null;
        } else {
            cloned = new HashMap<>();
            for (Map.Entry<String, ErrorCode> entry : allErrors.entrySet()) {
                cloned.put(entry.getKey(), entry.getValue());
            }
        }
        return cloned;
    }

    public synchronized boolean hasDeviceErrors(String port) {
        boolean z;
        HashMap<String, ErrorCode> errors = getAllDeviceErrors(port);
        if (errors == null || errors.size() <= 0) {
            z = false;
        } else {
            z = true;
        }
        return z;
    }

    public synchronized boolean hasDeviceError(String port, ErrorCode error) {
        boolean z;
        HashMap<String, ErrorCode> errors = getAllDeviceErrors(port);
        if (errors == null || errors.size() <= 0) {
            z = false;
        } else {
            z = errors.containsKey(String.valueOf(error.getCode()));
        }
        return z;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0029, code lost:
        if (r2.size() > 0) goto L_0x002b;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized java.util.HashMap<java.lang.String, com.xcharge.charger.data.bean.ErrorCode> getHigherPriorityDeviceErrors(java.lang.String r8, com.xcharge.charger.data.bean.ErrorCode r9) {
        /*
            r7 = this;
            monitor-enter(r7)
            java.util.HashMap r0 = r7.getAllDeviceErrors(r8)     // Catch:{ all -> 0x004f }
            if (r0 == 0) goto L_0x004d
            int r5 = r0.size()     // Catch:{ all -> 0x004f }
            if (r5 <= 0) goto L_0x004d
            java.util.List<java.lang.Integer> r5 = r7.priorityErrors     // Catch:{ all -> 0x004f }
            int r6 = r9.getCode()     // Catch:{ all -> 0x004f }
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)     // Catch:{ all -> 0x004f }
            int r4 = r5.indexOf(r6)     // Catch:{ all -> 0x004f }
            if (r4 <= 0) goto L_0x004d
            java.util.HashMap r2 = new java.util.HashMap     // Catch:{ all -> 0x004f }
            r2.<init>()     // Catch:{ all -> 0x004f }
            r3 = 0
        L_0x0023:
            if (r3 < r4) goto L_0x002d
            int r5 = r2.size()     // Catch:{ all -> 0x004f }
            if (r5 <= 0) goto L_0x004d
        L_0x002b:
            monitor-exit(r7)
            return r2
        L_0x002d:
            java.util.List<java.lang.Integer> r5 = r7.priorityErrors     // Catch:{ all -> 0x004f }
            java.lang.Object r5 = r5.get(r3)     // Catch:{ all -> 0x004f }
            java.lang.String r5 = java.lang.String.valueOf(r5)     // Catch:{ all -> 0x004f }
            java.lang.Object r1 = r0.get(r5)     // Catch:{ all -> 0x004f }
            com.xcharge.charger.data.bean.ErrorCode r1 = (com.xcharge.charger.data.bean.ErrorCode) r1     // Catch:{ all -> 0x004f }
            if (r1 == 0) goto L_0x004a
            int r5 = r1.getCode()     // Catch:{ all -> 0x004f }
            java.lang.String r5 = java.lang.String.valueOf(r5)     // Catch:{ all -> 0x004f }
            r2.put(r5, r1)     // Catch:{ all -> 0x004f }
        L_0x004a:
            int r3 = r3 + 1
            goto L_0x0023
        L_0x004d:
            r2 = 0
            goto L_0x002b
        L_0x004f:
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
        if (!errors.containsKey(code)) {
            errors.put(code, error);
            portStatus.setAllDeviceErrorCache(errors);
            portsStatus.put(port, portStatus);
            this.cache.get().setPorts(portsStatus);
            notifyChange(getUriFor("ports/fault/" + port + "/new/" + code));
            z = true;
        } else {
            z = false;
        }
        return z;
    }

    public synchronized boolean removeDeviceError(String port, ErrorCode error) {
        boolean z;
        Port portStatus;
        HashMap<String, Port> portsStatus = this.cache.get().getPorts();
        if (!(portsStatus == null || (portStatus = portsStatus.get(port)) == null)) {
            if (error == null) {
                portStatus.setAllDeviceErrorCache((HashMap<String, ErrorCode>) null);
                portsStatus.put(port, portStatus);
                this.cache.get().setPorts(portsStatus);
                notifyChange(getUriFor("ports/fault/" + port + "/remove/" + 200));
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
        double d;
        Port portStatus;
        HashMap<String, Port> ports = this.cache.get().getPorts();
        if (ports == null || (portStatus = ports.get(port)) == null) {
            d = 0.0d;
        } else {
            d = portStatus.getMeter().doubleValue();
        }
        return d;
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
        if (!(oldMeter == null || oldMeter.doubleValue() == ammeter)) {
            notifyChange(getUriFor("ports/" + port + "/ammeter"));
        }
        return true;
    }
}
