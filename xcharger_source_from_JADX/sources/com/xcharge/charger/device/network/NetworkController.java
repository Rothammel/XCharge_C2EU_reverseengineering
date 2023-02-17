package com.xcharge.charger.device.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.ethernet.EthernetDevInfo;
import android.net.ethernet.EthernetManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.xcharge.charger.data.bean.device.Ethernet;
import com.xcharge.charger.data.bean.device.MobileNet;
import com.xcharge.charger.data.bean.device.Network;
import com.xcharge.charger.data.bean.device.Wifi;
import com.xcharge.charger.data.bean.setting.APNSetting;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.HandlerTimer;
import com.xcharge.common.utils.LogUtils;

public class NetworkController {
    public static final int MSG_ETHERNET_CONFIGURATION_SUCCEEDED = 135170;
    public static final int MSG_ETHERNET_DISCONNECTED = 135171;
    public static final int MSG_MOBILE_NETWORK_STATE_CHECK_TIMER = 135174;
    public static final int MSG_MOBILE_SIGNAL_STRENGTH_CHANGED = 135173;
    public static final int MSG_MOBILE_SIM_STATE_CHANGED = 135172;
    public static final int MSG_NETWORK_CONNECTIVITY_CHANGED = 135169;
    public static final int TIMEOUT_MOBILE_NETWORK_STATE_CHECK = 10000;
    private static NetworkController instance = null;
    private Context context = null;
    private MsgHandler handler = null;
    /* access modifiers changed from: private */
    public HandlerTimer handlerTimer = null;
    private MobileListener mobileListener = null;
    private TelephonyManager mobileManager = null;
    private NetworkProxy networkProxy = null;
    private NetworkReceiver networkReceiver = null;
    private HandlerThread thread = null;

    private class NetworkReceiver extends BroadcastReceiver {
        private NetworkReceiver() {
        }

        /* synthetic */ NetworkReceiver(NetworkController networkController, NetworkReceiver networkReceiver) {
            this();
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("NetworkController.NetworkReceiver", "action: " + action);
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                Log.d("NetworkController.NetworkReceiver", "extraNetworkInfo: " + JsonBean.getGsonBuilder().create().toJson((Object) (NetworkInfo) intent.getParcelableExtra("networkInfo")));
                NetworkController.this.sendMessage(NetworkController.this.obtainMessage(135169, (Object) null));
            } else if ("android.net.ethernet.ETHERNET_STATE_CHANGED".equals(action)) {
                Log.i("NetworkController.NetworkReceiver", "ethernet_state: " + intent.getIntExtra("ethernet_state", 4));
                Log.d("NetworkController.NetworkReceiver", "ethernet devinfo: " + JsonBean.getGsonBuilder().create().toJson((Object) intent.getParcelableExtra("ethernetInfo")));
            } else if ("android.net.ethernet.STATE_CHANGE".equals(action)) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                LinkProperties linkProperties = (LinkProperties) intent.getParcelableExtra("linkProperties");
                int event = intent.getIntExtra("ethernet_state", 5);
                switch (event) {
                    case 5:
                        Log.d("NetworkController.NetworkReceiver", "ethernet networkInfo: " + JsonBean.getGsonBuilder().create().toJson((Object) networkInfo));
                        Log.d("NetworkController.NetworkReceiver", "ethernet linkProperties: " + JsonBean.getGsonBuilder().create().toJson((Object) linkProperties));
                        NetworkController.this.sendMessage(NetworkController.this.obtainMessage(NetworkController.MSG_ETHERNET_CONFIGURATION_SUCCEEDED, (Object) null));
                        return;
                    case 6:
                        Log.i("NetworkController.NetworkReceiver", "ethernet config failed");
                        Log.d("NetworkController.NetworkReceiver", "ethernet networkInfo: " + JsonBean.getGsonBuilder().create().toJson((Object) networkInfo));
                        Log.d("NetworkController.NetworkReceiver", "ethernet linkProperties: " + JsonBean.getGsonBuilder().create().toJson((Object) linkProperties));
                        return;
                    case 7:
                        Log.i("NetworkController.NetworkReceiver", "ethernet disconnected !!!");
                        Log.d("NetworkController.NetworkReceiver", "ethernet networkInfo: " + JsonBean.getGsonBuilder().create().toJson((Object) networkInfo));
                        Log.d("NetworkController.NetworkReceiver", "ethernet linkProperties: " + JsonBean.getGsonBuilder().create().toJson((Object) linkProperties));
                        NetworkController.this.sendMessage(NetworkController.this.obtainMessage(NetworkController.MSG_ETHERNET_DISCONNECTED, (Object) null));
                        return;
                    default:
                        Log.w("NetworkController.NetworkReceiver", "unhandled ethernet event: " + event);
                        return;
                }
            } else if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                Log.i("NetworkController.NetworkReceiver", "wifi AP state: " + ((WifiManager) context.getSystemService(Network.NETWORK_TYPE_WIFI)).getWifiApState());
            } else if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                String iccState = intent.getStringExtra("ss");
                Log.d("NetworkController.NetworkReceiver", "IccCardConstants.INTENT_KEY_ICC_STATE: " + iccState);
                NetworkController.this.sendMessage(NetworkController.this.obtainMessage(NetworkController.MSG_MOBILE_SIM_STATE_CHANGED, iccState));
            }
        }
    }

    private class MobileListener extends PhoneStateListener {
        private MobileListener() {
        }

        /* synthetic */ MobileListener(NetworkController networkController, MobileListener mobileListener) {
            this();
        }

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            NetworkController.this.sendMessage(NetworkController.this.obtainMessage(NetworkController.MSG_MOBILE_SIGNAL_STRENGTH_CHANGED, signalStrength));
        }
    }

    public static NetworkController getInstance() {
        if (instance == null) {
            instance = new NetworkController();
        }
        return instance;
    }

    private class MsgHandler extends Handler {
        public MsgHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(android.os.Message r8) {
            /*
                r7 = this;
                int r2 = r8.what     // Catch:{ Exception -> 0x0016 }
                switch(r2) {
                    case 135169: goto L_0x0009;
                    case 135170: goto L_0x0046;
                    case 135171: goto L_0x0053;
                    case 135172: goto L_0x0060;
                    case 135173: goto L_0x0071;
                    case 135174: goto L_0x007b;
                    default: goto L_0x0005;
                }
            L_0x0005:
                super.handleMessage(r8)
                return
            L_0x0009:
                java.lang.String r2 = "NetworkController.handleMessage"
                java.lang.String r3 = "network connectivity changed !!!"
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0016 }
                com.xcharge.charger.device.network.NetworkController r2 = com.xcharge.charger.device.network.NetworkController.this     // Catch:{ Exception -> 0x0016 }
                r2.handleNetworkConnectivityChanged()     // Catch:{ Exception -> 0x0016 }
                goto L_0x0005
            L_0x0016:
                r0 = move-exception
                java.lang.String r2 = "NetworkController.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder
                java.lang.String r4 = "except: "
                r3.<init>(r4)
                java.lang.String r4 = android.util.Log.getStackTraceString(r0)
                java.lang.StringBuilder r3 = r3.append(r4)
                java.lang.String r3 = r3.toString()
                android.util.Log.e(r2, r3)
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                java.lang.String r3 = "NetworkController handleMessage exception: "
                r2.<init>(r3)
                java.lang.String r3 = android.util.Log.getStackTraceString(r0)
                java.lang.StringBuilder r2 = r2.append(r3)
                java.lang.String r2 = r2.toString()
                com.xcharge.common.utils.LogUtils.syslog(r2)
                goto L_0x0005
            L_0x0046:
                java.lang.String r2 = "NetworkController.handleMessage"
                java.lang.String r3 = "ethernet configuration succeeded !!!"
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0016 }
                com.xcharge.charger.device.network.NetworkController r2 = com.xcharge.charger.device.network.NetworkController.this     // Catch:{ Exception -> 0x0016 }
                r2.handleEthernetConfigSucceeded()     // Catch:{ Exception -> 0x0016 }
                goto L_0x0005
            L_0x0053:
                java.lang.String r2 = "NetworkController.handleMessage"
                java.lang.String r3 = "ethernet disconnected !!!"
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0016 }
                com.xcharge.charger.device.network.NetworkController r2 = com.xcharge.charger.device.network.NetworkController.this     // Catch:{ Exception -> 0x0016 }
                r2.handleEthernetDisconnected()     // Catch:{ Exception -> 0x0016 }
                goto L_0x0005
            L_0x0060:
                java.lang.String r2 = "NetworkController.handleMessage"
                java.lang.String r3 = "mobile sim state changed !!!"
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0016 }
                com.xcharge.charger.device.network.NetworkController r3 = com.xcharge.charger.device.network.NetworkController.this     // Catch:{ Exception -> 0x0016 }
                java.lang.Object r2 = r8.obj     // Catch:{ Exception -> 0x0016 }
                java.lang.String r2 = (java.lang.String) r2     // Catch:{ Exception -> 0x0016 }
                r3.handleMobileSimStateChanged(r2)     // Catch:{ Exception -> 0x0016 }
                goto L_0x0005
            L_0x0071:
                java.lang.Object r1 = r8.obj     // Catch:{ Exception -> 0x0016 }
                android.telephony.SignalStrength r1 = (android.telephony.SignalStrength) r1     // Catch:{ Exception -> 0x0016 }
                com.xcharge.charger.device.network.NetworkController r2 = com.xcharge.charger.device.network.NetworkController.this     // Catch:{ Exception -> 0x0016 }
                r2.handleMobileSignalStrengthChanged(r1)     // Catch:{ Exception -> 0x0016 }
                goto L_0x0005
            L_0x007b:
                com.xcharge.charger.device.network.NetworkController r2 = com.xcharge.charger.device.network.NetworkController.this     // Catch:{ Exception -> 0x0091 }
                r2.checkMobileNetworkState()     // Catch:{ Exception -> 0x0091 }
            L_0x0080:
                com.xcharge.charger.device.network.NetworkController r2 = com.xcharge.charger.device.network.NetworkController.this     // Catch:{ Exception -> 0x0016 }
                com.xcharge.common.utils.HandlerTimer r2 = r2.handlerTimer     // Catch:{ Exception -> 0x0016 }
                r4 = 10000(0x2710, double:4.9407E-320)
                r3 = 135174(0x21006, float:1.89419E-40)
                r6 = 0
                r2.startTimer(r4, r3, r6)     // Catch:{ Exception -> 0x0016 }
                goto L_0x0005
            L_0x0091:
                r2 = move-exception
                goto L_0x0080
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.device.network.NetworkController.MsgHandler.handleMessage(android.os.Message):void");
        }
    }

    public void init(Context context2) {
        this.context = context2;
        this.mobileManager = (TelephonyManager) this.context.getSystemService("phone");
        this.networkProxy = new NetworkProxy();
        this.networkProxy.init(context2);
        String simState = this.networkProxy.getSimState();
        HardwareStatusCacheProvider.getInstance().updateSimState(simState);
        Log.d("NetworkController.init", "sim state: " + simState);
        this.thread = new HandlerThread("NetworkController", 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(context2);
        this.mobileListener = new MobileListener(this, (MobileListener) null);
        this.mobileManager.listen(this.mobileListener, 321);
        this.networkReceiver = new NetworkReceiver(this, (NetworkReceiver) null);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.ethernet.ETHERNET_STATE_CHANGED");
        filter.addAction("android.net.ethernet.STATE_CHANGE");
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        this.context.registerReceiver(this.networkReceiver, filter);
        initNetwork();
    }

    public void destroy() {
        this.context.unregisterReceiver(this.networkReceiver);
        this.mobileManager.listen(this.mobileListener, 0);
        this.networkProxy.destroy();
        this.handlerTimer.stopTimer(MSG_MOBILE_NETWORK_STATE_CHECK_TIMER);
        this.handlerTimer.destroy();
        this.handler.removeMessages(135169);
        this.handler.removeMessages(MSG_ETHERNET_CONFIGURATION_SUCCEEDED);
        this.handler.removeMessages(MSG_ETHERNET_DISCONNECTED);
        this.handler.removeMessages(MSG_MOBILE_SIM_STATE_CHANGED);
        this.handler.removeMessages(MSG_MOBILE_SIGNAL_STRENGTH_CHANGED);
        this.thread.quit();
    }

    public Message obtainMessage(int what) {
        return this.handler.obtainMessage(what);
    }

    public Message obtainMessage(int what, Object obj) {
        return this.handler.obtainMessage(what, obj);
    }

    public boolean sendMessage(Message msg) {
        return this.handler.sendMessage(msg);
    }

    private void initNetwork() {
        ConnectivityManager connectivityMananger = (ConnectivityManager) this.context.getSystemService("connectivity");
        connectivityMananger.setMobileDataEnabled(true);
        if (connectivityMananger.getNetworkInfo(9).isConnected()) {
            Log.i("NetworkController.initNetwork", "ethernet is connected");
            return;
        }
        Log.i("NetworkController.initNetwork", "ethernet is not connected");
        connectEthernet(true);
    }

    private void connectEthernet(final boolean enable) {
        new AsyncTask<Void, Void, Void>() {
            /* access modifiers changed from: protected */
            public void onPreExecute() {
            }

            /* access modifiers changed from: protected */
            public Void doInBackground(Void... unused) {
                try {
                    Log.i("NetworkController.connectEthernet", "connect or disconnect: " + enable);
                    EthernetManager.getInstance().setEnabled(enable);
                    if (EthernetManager.getInstance().isConfigured() || !enable) {
                        return null;
                    }
                    if (!EthernetManager.getInstance().isDhcp()) {
                        EthernetManager.getInstance().setDefaultConf();
                    }
                    publishProgress(new Void[0]);
                    return null;
                } catch (Exception e) {
                    Log.e("NetworkController.connectEthernet", Log.getStackTraceString(e));
                    return null;
                }
            }

            /* access modifiers changed from: protected */
            public void onProgressUpdate(Void... unused) {
                if (EthernetManager.getInstance().getDeviceNameList() != null && EthernetManager.getInstance().getDeviceNameList().size() > 0) {
                    for (EthernetDevInfo di : EthernetManager.getInstance().getDeviceNameList()) {
                        Log.i("NetworkController.connectEthernet", "config interface: " + di.getIfName());
                        EthernetManager.getInstance().updateDevInfo(di);
                    }
                }
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Void unused) {
            }
        }.execute(new Void[0]);
    }

    /* access modifiers changed from: private */
    public void handleNetworkConnectivityChanged() {
        try {
            NetworkInfo networkInfo = ((ConnectivityManager) this.context.getSystemService("connectivity")).getActiveNetworkInfo();
            if (networkInfo != null) {
                int networkType = networkInfo.getType();
                boolean isConnected = networkInfo.isConnected();
                String sNetworkType = this.networkProxy.getNetworkType(false);
                Log.i("NetworkController.handleNetworkConnectivityChanged", "now active network type: " + sNetworkType + "(" + networkType + "," + networkInfo.getSubtype() + "), connect: " + (isConnected ? "true" : "false"));
                if (isConnected) {
                    if (ConnectivityManager.isNetworkTypeMobile(networkType)) {
                        MobileNet oldMobileNet = HardwareStatusCacheProvider.getInstance().getMobileNetStatus();
                        MobileNet mobile = this.networkProxy.getMobileNetStatus();
                        mobile.setSignalDbm(oldMobileNet.getSignalDbm());
                        mobile.setDefaultSignalLevel(oldMobileNet.getDefaultSignalLevel());
                        mobile.setSimState(oldMobileNet.getSimState());
                        HardwareStatusCacheProvider.getInstance().updateMobileNetStatus(mobile);
                        Log.i("NetworkController.handleNetworkConnectivityChanged", "mobile network connected: " + mobile.toJson());
                        Log.i("NetworkController.handleNetworkConnectivityChanged", "mobile current apn: " + (this.networkProxy.getCurrentApn() == null ? "" : this.networkProxy.getCurrentApn().toJson()));
                        LogUtils.syslog("mobile network connected: " + mobile.toJson());
                        this.handlerTimer.stopTimer(MSG_MOBILE_NETWORK_STATE_CHECK_TIMER);
                        this.handlerTimer.startTimer(10000, MSG_MOBILE_NETWORK_STATE_CHECK_TIMER, (Object) null);
                    } else if (ConnectivityManager.isNetworkTypeWifi(networkType)) {
                        Wifi wifi = this.networkProxy.getWifiStatus();
                        HardwareStatusCacheProvider.getInstance().updateWifiStatus(wifi);
                        Log.i("NetworkController.handleNetworkConnectivityChanged", "wifi network connected: " + wifi.toJson());
                        LogUtils.syslog("wifi network connected: " + wifi.toJson());
                    }
                    HardwareStatusCacheProvider.getInstance().updateActiveNetwork(sNetworkType, true);
                    return;
                }
                if (ConnectivityManager.isNetworkTypeMobile(networkType)) {
                    MobileNet oldMobileNet2 = HardwareStatusCacheProvider.getInstance().getMobileNetStatus();
                    MobileNet newMobileNet = new MobileNet();
                    newMobileNet.setSignalDbm(oldMobileNet2.getSignalDbm());
                    newMobileNet.setDefaultSignalLevel(oldMobileNet2.getDefaultSignalLevel());
                    newMobileNet.setSimState(oldMobileNet2.getSimState());
                    HardwareStatusCacheProvider.getInstance().updateMobileNetStatus(newMobileNet);
                    Log.i("NetworkController.handleNetworkConnectivityChanged", "mobile network disconnected: " + newMobileNet.toJson());
                    LogUtils.syslog("mobile network disconnected !!!");
                    this.handlerTimer.stopTimer(MSG_MOBILE_NETWORK_STATE_CHECK_TIMER);
                } else if (ConnectivityManager.isNetworkTypeWifi(networkType)) {
                    HardwareStatusCacheProvider.getInstance().updateWifiStatus(new Wifi());
                    Log.i("NetworkController.handleNetworkConnectivityChanged", "wifi network disconnected !!!");
                    LogUtils.syslog("wifi network disconnected !!!");
                }
                if (sNetworkType.equals(HardwareStatusCacheProvider.getInstance().getActiveNetwork())) {
                    HardwareStatusCacheProvider.getInstance().updateActiveNetwork(sNetworkType, false);
                    return;
                }
                return;
            }
            Log.i("NetworkController.handleNetworkConnectivityChanged", "no active network");
            String activeNetwork = HardwareStatusCacheProvider.getInstance().getActiveNetwork();
            if (!"none".equals(activeNetwork) && HardwareStatusCacheProvider.getInstance().isNetworkConnected()) {
                Log.i("NetworkController.handleNetworkConnectivityChanged", "active network: " + activeNetwork + " disconnected !!!");
                LogUtils.syslog("active network: " + activeNetwork + " disconnected !!!");
                if (Network.NETWORK_TYPE_MOBILE.equals(activeNetwork)) {
                    MobileNet oldMobileNet3 = HardwareStatusCacheProvider.getInstance().getMobileNetStatus();
                    MobileNet newMobileNet2 = new MobileNet();
                    newMobileNet2.setSignalDbm(oldMobileNet3.getSignalDbm());
                    newMobileNet2.setDefaultSignalLevel(oldMobileNet3.getDefaultSignalLevel());
                    newMobileNet2.setSimState(oldMobileNet3.getSimState());
                    HardwareStatusCacheProvider.getInstance().updateMobileNetStatus(newMobileNet2);
                    Log.i("NetworkController.handleNetworkConnectivityChanged", "mobile network disconnected: " + newMobileNet2.toJson());
                } else if (Network.NETWORK_TYPE_WIFI.equals(activeNetwork)) {
                    HardwareStatusCacheProvider.getInstance().updateWifiStatus(new Wifi());
                } else if (Network.NETWORK_TYPE_ETHERNET.equals(activeNetwork)) {
                    HardwareStatusCacheProvider.getInstance().updateEthernetStatus(new Ethernet());
                }
            }
            HardwareStatusCacheProvider.getInstance().updateActiveNetwork((String) null, false);
        } catch (Exception e) {
            Log.e("NetworkController.handleNetworkConnectivityChanged", Log.getStackTraceString(e));
        }
    }

    /* access modifiers changed from: private */
    public void handleEthernetConfigSucceeded() {
        try {
            Ethernet netInfo = this.networkProxy.getEthernetStatus();
            Log.i("NetworkController.handleEthernetConfigSucceeded", "ethernet config success: " + netInfo.toJson());
            LogUtils.syslog("ethernet connected: " + netInfo.toJson());
            HardwareStatusCacheProvider.getInstance().updateEthernetStatus(netInfo);
        } catch (Exception e) {
            Log.e("NetworkController.handleEthernetConfigSucceeded", Log.getStackTraceString(e));
        }
    }

    /* access modifiers changed from: private */
    public void handleEthernetDisconnected() {
        try {
            HardwareStatusCacheProvider.getInstance().updateEthernetStatus(new Ethernet());
            String activeNetwork = HardwareStatusCacheProvider.getInstance().getActiveNetwork();
            if (Network.NETWORK_TYPE_ETHERNET.equals(activeNetwork) && HardwareStatusCacheProvider.getInstance().isNetworkConnected()) {
                HardwareStatusCacheProvider.getInstance().updateActiveNetwork(activeNetwork, false);
                LogUtils.syslog("ethernet disconnected !!!");
            }
        } catch (Exception e) {
            Log.e("NetworkController.handleEthernetDisconnected", Log.getStackTraceString(e));
        }
    }

    /* access modifiers changed from: private */
    public void handleMobileSimStateChanged(String iccState) {
        try {
            String simState = this.networkProxy.getSimState();
            Log.d("NetworkController.handleMobileSimStateChanged", "sim state: " + simState);
            HardwareStatusCacheProvider.getInstance().updateSimState(simState);
            if ("IMSI".equals(iccState)) {
                MobileNet mobile = this.networkProxy.getMobileNetStatus();
                HardwareStatusCacheProvider.getInstance().updateSimIdInfo(mobile.getIMSI(), mobile.getICCID());
            } else if ("LOADED".equals(iccState)) {
                MobileNet mobile2 = this.networkProxy.getMobileNetStatus();
                HardwareStatusCacheProvider.getInstance().updatePreferAPN(mobile2.getPreferApn());
                HardwareStatusCacheProvider.getInstance().updateSimNetInfo(mobile2.getSimMCC(), mobile2.getSimMNC());
            } else if ("NOT_READY".equals(iccState) || MobileNet.OPR_UNKNOWN.equals(iccState) || "ABSENT".equals(iccState)) {
                HardwareStatusCacheProvider.getInstance().updateSimIdInfo((String) null, (String) null);
                HardwareStatusCacheProvider.getInstance().updateSimNetInfo((String) null, (String) null);
                HardwareStatusCacheProvider.getInstance().updatePreferAPN((APNSetting) null);
            }
        } catch (Exception e) {
            Log.e("NetworkController.handleMobileSimStateChanged", Log.getStackTraceString(e));
        }
    }

    /* access modifiers changed from: private */
    public void handleMobileSignalStrengthChanged(SignalStrength signalStrength) {
        int dbm;
        try {
            String mobileType = HardwareStatusCacheProvider.getInstance().getMobileNetStatus().getType();
            if ("4G".equals(mobileType) || "3G".equals(mobileType)) {
                String operator = this.networkProxy.getNetworkOperator();
                if (MobileNet.OPR_CT.equals(operator)) {
                    if (signalStrength.isGsm()) {
                        dbm = (signalStrength.getGsmSignalStrength() * 2) - 113;
                    } else {
                        dbm = signalStrength.getEvdoDbm();
                        Log.d("NetworkController.handleMobileSignalStrengthChanged", "CT signal strength, EvdoDbm: " + dbm);
                    }
                } else if (MobileNet.OPR_CUCC.equals(operator)) {
                    if (signalStrength.isGsm()) {
                        dbm = (signalStrength.getGsmSignalStrength() * 2) - 113;
                    } else {
                        dbm = signalStrength.getCdmaDbm();
                        Log.d("NetworkController.handleMobileSignalStrengthChanged", "CUCC signal strength, CdmaDbm: " + dbm);
                    }
                } else if (MobileNet.OPR_CMCC.equals(operator)) {
                    dbm = (signalStrength.getGsmSignalStrength() * 2) - 113;
                } else {
                    dbm = (signalStrength.getGsmSignalStrength() * 2) - 113;
                }
            } else if ("2G".equals(mobileType)) {
                dbm = (signalStrength.getGsmSignalStrength() * 2) - 113;
            } else {
                dbm = (signalStrength.getGsmSignalStrength() * 2) - 113;
            }
            int signalLevel = this.networkProxy.calcSignalLevel(dbm);
            if (Network.NETWORK_TYPE_MOBILE.equals(HardwareStatusCacheProvider.getInstance().getActiveNetwork()) && HardwareStatusCacheProvider.getInstance().isNetworkConnected()) {
                if (dbm != -1000 && ((signalLevel == 4 || signalLevel == 3 || signalLevel == -1) && dbm != HardwareStatusCacheProvider.getInstance().getMobileNetStatus().getSignalDbm())) {
                    Log.d("NetworkController.handleMobileSignalStrengthChanged", "bad mobile signal, dbm: " + dbm + ", level: " + signalLevel);
                }
                int oldSignalLevel = HardwareStatusCacheProvider.getInstance().getMobileNetStatus().getDefaultSignalLevel();
                if ((oldSignalLevel == -1 || oldSignalLevel == 4 || signalLevel == 3) && (signalLevel == 0 || signalLevel == 1 || signalLevel == 2)) {
                    Log.d("NetworkController.handleMobileSignalStrengthChanged", "good mobile signal, dbm: " + dbm + ", level: " + signalLevel);
                }
            }
            HardwareStatusCacheProvider.getInstance().updateMobileNetSignal(signalLevel, dbm, signalStrength.getGsmSignalStrength());
        } catch (Exception e) {
            Log.e("NetworkController.handleMobileSignalStrengthChanged", Log.getStackTraceString(e));
        }
    }

    /* access modifiers changed from: private */
    public void checkMobileNetworkState() {
        MobileNet mobileNet;
        Integer subtype;
        int nowSubtype;
        try {
            String simState = HardwareStatusCacheProvider.getInstance().getSimState();
            String newSimState = this.networkProxy.getSimState();
            if (!newSimState.equals(simState)) {
                HardwareStatusCacheProvider.getInstance().updateSimState(newSimState);
            }
            NetworkInfo networkInfo = ((ConnectivityManager) this.context.getSystemService("connectivity")).getActiveNetworkInfo();
            if (networkInfo != null) {
                boolean isConnected = networkInfo.isConnected();
                int networkType = networkInfo.getType();
                if (isConnected && ConnectivityManager.isNetworkTypeMobile(networkType) && (mobileNet = HardwareStatusCacheProvider.getInstance().getMobileNetStatus()) != null && (subtype = mobileNet.getSubtype()) != null && (nowSubtype = networkInfo.getSubtype()) != subtype.intValue()) {
                    Log.d("NetworkController.checkMobileNetworkState", "mobile network type changed, type: " + nowSubtype + ", old type: " + subtype);
                    handleNetworkConnectivityChanged();
                }
            }
        } catch (Exception e) {
            Log.e("NetworkController.checkMobileNetworkState", Log.getStackTraceString(e));
        }
    }
}
