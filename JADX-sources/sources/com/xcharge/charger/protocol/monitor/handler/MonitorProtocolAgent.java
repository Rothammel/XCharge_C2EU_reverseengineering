package com.xcharge.charger.protocol.monitor.handler;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.FeeRate;
import com.xcharge.charger.data.bean.PortFeeRate;
import com.xcharge.charger.data.bean.device.Ethernet;
import com.xcharge.charger.data.bean.device.MobileNet;
import com.xcharge.charger.data.bean.device.Network;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.setting.PortSetting;
import com.xcharge.charger.data.bean.setting.RadarSetting;
import com.xcharge.charger.data.bean.setting.TimerSetting;
import com.xcharge.charger.data.bean.type.PHASE;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.LocalSettingCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SoftwareStatusCacheProvider;
import com.xcharge.charger.data.proxy.NetworkStatusObserver;
import com.xcharge.charger.protocol.monitor.R;
import com.xcharge.charger.protocol.monitor.bean.DDAPMessage;
import com.xcharge.charger.protocol.monitor.bean.ErrorCodeMapping;
import com.xcharge.charger.protocol.monitor.bean.YZXDCAPError;
import com.xcharge.charger.protocol.monitor.bean.YZXDCAPMessage;
import com.xcharge.charger.protocol.monitor.bean.YZXDCAPOption;
import com.xcharge.charger.protocol.monitor.bean.YZXProperty;
import com.xcharge.charger.protocol.monitor.bean.YZXPropset;
import com.xcharge.charger.protocol.monitor.bean.dap.DAPCapability;
import com.xcharge.charger.protocol.monitor.bean.dap.DAPError;
import com.xcharge.charger.protocol.monitor.bean.dap.DAPEthernet;
import com.xcharge.charger.protocol.monitor.bean.dap.DAPFeePolicy;
import com.xcharge.charger.protocol.monitor.bean.dap.DAPFeeRate;
import com.xcharge.charger.protocol.monitor.bean.dap.DAPFeeRates;
import com.xcharge.charger.protocol.monitor.bean.dap.DAPMobile;
import com.xcharge.charger.protocol.monitor.bean.dap.DAPRadarParam;
import com.xcharge.charger.protocol.monitor.bean.dap.DAPTimingParam;
import com.xcharge.charger.protocol.monitor.bean.dap.DAPVersion;
import com.xcharge.charger.protocol.monitor.bean.ddap.DDAPDeviceSystem;
import com.xcharge.charger.protocol.monitor.bean.ddap.DDAPEthernetAccess;
import com.xcharge.charger.protocol.monitor.bean.ddap.DDAPMobileAccess;
import com.xcharge.charger.protocol.monitor.bean.request.HelloRequest;
import com.xcharge.charger.protocol.monitor.bean.response.HelloResponse;
import com.xcharge.charger.protocol.monitor.session.MonitorChargeSession;
import com.xcharge.charger.protocol.monitor.session.MonitorRequestSession;
import com.xcharge.charger.protocol.monitor.type.MONITOR_REQUEST_STATE;
import com.xcharge.charger.protocol.monitor.util.FieldConfigUtils;
import com.xcharge.charger.protocol.monitor.util.LogUtils;
import com.xcharge.charger.protocol.monitor.util.Md5;
import com.xcharge.charger.protocol.monitor.util.RandomUtils;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.HandlerTimer;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import org.apache.commons.lang3.CharEncoding;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/* loaded from: classes.dex */
public class MonitorProtocolAgent extends IoHandlerAdapter {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$PHASE = null;
    public static final int MSG_CONNECTED = 69634;
    public static final int MSG_CONNECT_ERROR = 69636;
    public static final int MSG_DISCONNECTED = 69635;
    public static final int MSG_INIT_CONNECTION = 69633;
    public static final int MSG_RECEIVED = 69640;
    public static final int MSG_SECOND_TIMER = 69641;
    public static final int MSG_SEND = 69637;
    public static final int MSG_SENDED = 69638;
    public static final int MSG_SEND_FAIL = 69639;
    public static final int MSG_SYNC_TIME = 69648;
    private static final String SYNC_TIME_SERVER = "http://api.yzxtech.net/iot/device/access";
    public static final int TIMEOUT_RESPONSE = 10;
    public static final int TIMEOUT_SEND = 5;
    private static MonitorProtocolAgent instance = null;
    private String center_ip_port;
    private String deviceName;
    private String deviceSecret;
    private String productKey;
    private String productSecret;
    private String topicOut;
    private MqttClient sampleClient = null;
    private HandlerThread thread = null;
    private MsgHandler handler = null;
    private HandlerTimer handlerTimer = null;
    private Context context = null;
    private ThreadPoolExecutor connectThreadPoolExecutor = null;
    private AtomicLong seqGen = new AtomicLong(0);
    private boolean isConnected = false;
    private HashMap<String, MonitorPortHandler> portHandlers = null;
    private HashMap<String, SendRequestState> sendReqestState = null;
    private NetworkStatusObserver networkStatusObserver = null;

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

    public static MonitorProtocolAgent getInstance() {
        if (instance == null) {
            instance = new MonitorProtocolAgent();
        }
        return instance;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class SendRequestState {
        YZXDCAPMessage request;
        MONITOR_REQUEST_STATE status;
        long timestamp;

        private SendRequestState() {
            this.request = null;
            this.status = null;
            this.timestamp = 0L;
        }

        /* synthetic */ SendRequestState(SendRequestState sendRequestState) {
            this();
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
            String seq;
            try {
                switch (msg.what) {
                    case 69633:
                        MonitorProtocolAgent.this.connect();
                        break;
                    case 69634:
                        LogUtils.log("MonitorProtocolAgent.handleMessage", "connected !!!");
                        MonitorProtocolAgent.this.isConnected = true;
                        MonitorProtocolAgent.this.handlerTimer.startTimer(1000L, 69641, null);
                        MonitorProtocolAgent.this.sendNetworkRequest();
                        MonitorProtocolAgent.this.sendCapabilityRequest();
                        MonitorProtocolAgent.this.sendVersionRequest();
                        MonitorProtocolAgent.this.portsActive();
                        break;
                    case 69635:
                        LogUtils.log("MonitorProtocolAgent.handleMessage", "disconnected !!!");
                        MonitorProtocolAgent.this.isConnected = false;
                        MonitorProtocolAgent.this.handlerTimer.stopTimer(69641);
                        MonitorProtocolAgent.this.portsDeactive();
                        MonitorProtocolAgent.this.handler.sendEmptyMessageDelayed(69633, 10000L);
                        break;
                    case 69637:
                        YZXDCAPMessage yzxdcapMessage = (YZXDCAPMessage) msg.obj;
                        if (MonitorProtocolAgent.this.isConnected()) {
                            if (MonitorProtocolAgent.this.isRequestMessage(yzxdcapMessage.getOp())) {
                                SendRequestState reqestState = new SendRequestState(null);
                                reqestState.request = yzxdcapMessage;
                                reqestState.status = MONITOR_REQUEST_STATE.sending;
                                reqestState.timestamp = System.currentTimeMillis();
                                MonitorProtocolAgent.this.sendReqestState.put(String.valueOf(yzxdcapMessage.getSeq()), reqestState);
                                break;
                            }
                        } else if (MonitorProtocolAgent.this.isRequestMessage(yzxdcapMessage.getOp())) {
                            MonitorProtocolAgent.this.handleSendRequestFail(yzxdcapMessage);
                            break;
                        }
                        break;
                    case 69639:
                        YZXDCAPMessage yzxdcapMessage2 = (YZXDCAPMessage) msg.obj;
                        LogUtils.log("MonitorProtocolAgent.handleMessage", "failed to send yzx msg: " + yzxdcapMessage2.toJson());
                        MonitorProtocolAgent.this.handleSendRequestFail(yzxdcapMessage2);
                        break;
                    case 69640:
                        YZXDCAPMessage yzxdcapMessage3 = (YZXDCAPMessage) msg.obj;
                        LogUtils.log("MonitorProtocolAgent.handleMessage", "received yzx msg: " + yzxdcapMessage3.toJson());
                        if (!MonitorProtocolAgent.this.isRequestMessage(yzxdcapMessage3.getOp())) {
                            if ("ack".equals(yzxdcapMessage3.getOpt().getOp())) {
                                seq = String.valueOf(yzxdcapMessage3.getSeq().longValue() - 2);
                            } else {
                                seq = String.valueOf(yzxdcapMessage3.getOpt().getSeq());
                            }
                            SendRequestState reqestState2 = (SendRequestState) MonitorProtocolAgent.this.sendReqestState.get(seq);
                            if (reqestState2 != null) {
                                YZXDCAPMessage request = reqestState2.request;
                                MonitorProtocolAgent.this.sendReqestState.remove(seq);
                                MonitorProtocolAgent.this.dispatchYZXMessage(yzxdcapMessage3, request);
                                break;
                            } else {
                                LogUtils.log("MonitorProtocolAgent.handleMessage", "maybe timeout to wait for response msg: " + yzxdcapMessage3.toJson());
                                break;
                            }
                        } else {
                            MonitorProtocolAgent.this.dispatchYZXMessage(yzxdcapMessage3, null);
                            break;
                        }
                    case 69641:
                        MonitorProtocolAgent.this.requestTimeoutCheck();
                        MonitorProtocolAgent.this.handlerTimer.startTimer(1000L, 69641, null);
                        break;
                    case 69648:
                        if (!HardwareStatusCacheProvider.getInstance().isNetworkConnected()) {
                            MonitorProtocolAgent.this.handler.sendEmptyMessageDelayed(69648, 5000L);
                            break;
                        } else {
                            LogUtils.log("MonitorProtocolAgent.handleMessage", "sync time from cloud !!!");
                            if (!MonitorProtocolAgent.this.syncTime()) {
                                MonitorProtocolAgent.this.handler.sendEmptyMessageDelayed(69648, 5000L);
                                break;
                            } else {
                                MonitorProtocolAgent.this.initConnection();
                                break;
                            }
                        }
                    case 135169:
                        Uri uri = (Uri) msg.obj;
                        MonitorProtocolAgent.this.handleNetworkStatusChanged(uri);
                        break;
                }
            } catch (Exception e) {
                Log.e("MonitorProtocolAgent.handleMessage", "except: " + Log.getStackTraceString(e));
            }
            super.handleMessage(msg);
        }
    }

    public void init(Context context) {
        this.context = context;
        this.portHandlers = new HashMap<>();
        HashMap<String, Port> ports = HardwareStatusCacheProvider.getInstance().getPorts();
        if (ports != null) {
            for (String port : ports.keySet()) {
                MonitorPortHandler portHandler = new MonitorPortHandler();
                portHandler.init(context, port, this);
                this.portHandlers.put(port, portHandler);
            }
        }
        this.connectThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue(1), new RejectedExecutionHandler() { // from class: com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.1
            @Override // java.util.concurrent.RejectedExecutionHandler
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                LogUtils.log("MonitorProtocolAgent.ThreadPoolExecutor.rejectedExecution", "rejected runnable: " + r.toString() + ", active runnables: " + executor.getActiveCount());
            }
        });
        this.sendReqestState = new HashMap<>();
        this.thread = new HandlerThread("MonitorProtocolAgent", 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(context);
        this.networkStatusObserver = new NetworkStatusObserver(this.context, this.handler);
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor(Network.class.getSimpleName()), true, this.networkStatusObserver);
    }

    public void destroy() {
        this.context.getContentResolver().unregisterContentObserver(this.networkStatusObserver);
        this.handlerTimer.destroy();
        this.handler.removeMessages(69633);
        this.handler.removeMessages(69634);
        this.handler.removeMessages(69635);
        this.handler.removeMessages(69636);
        this.handler.removeMessages(69640);
        this.handler.removeMessages(69637);
        this.handler.removeMessages(69638);
        this.handler.removeMessages(69639);
        this.handler.removeMessages(69641);
        this.handler.removeMessages(69648);
        this.thread.quit();
        for (MonitorPortHandler portHandler : this.portHandlers.values()) {
            portHandler.destroy();
        }
        this.portHandlers.clear();
        this.sendReqestState.clear();
        this.connectThreadPoolExecutor.shutdown();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleNetworkStatusChanged(Uri uri) {
        LogUtils.log("MonitorProtocolAgent.handleNetworkStatusChanged", "network status changed, uri: " + uri.toString());
        String lastSegment = uri.getLastPathSegment();
        if (!"connected".equals(lastSegment) && "disconnected".equals(lastSegment)) {
            disconnect();
        }
    }

    private void disconnect() {
        try {
            if (this.sampleClient != null) {
                this.sampleClient.disconnectForcibly();
                this.sampleClient = null;
            }
        } catch (MqttException e) {
            LogUtils.log("MonitorProtocolAgent.disconnect", Log.getStackTraceString(e));
        }
        this.handler.sendEmptyMessage(69635);
    }

    public void initServerTimeSync() {
        this.handler.sendEmptyMessage(69648);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean syncTime() {
        try {
            BasicHttpParams basicHttpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(basicHttpParams, 10000);
            DefaultHttpClient httpClient = new DefaultHttpClient(basicHttpParams);
            HttpPost request = new HttpPost(SYNC_TIME_SERVER);
            request.setHeader(HttpHeaders.CONNECTION, "Close");
            StringEntity se = new StringEntity(createHelloDDAPRequestMessage());
            se.setContentType("text/json");
            se.setContentEncoding(new BasicHeader("Content-Type", "application/json"));
            request.setEntity(se);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            LogUtils.log("MonitorProtocolAgent.syncTime", "request time sync, url: http://api.yzxtech.net/iot/device/access");
            LogUtils.log("MonitorProtocolAgent.syncTime", "local time before request: " + sdf.format(new Date(System.currentTimeMillis())));
            HttpResponse response = httpClient.execute(request);
            LogUtils.log("MonitorProtocolAgent.syncTime", "local time when response: " + sdf.format(new Date(System.currentTimeMillis())));
            LogUtils.log("MonitorProtocolAgent.syncTime", "response StatusCode:" + response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() == 200) {
                String result = EntityUtils.toString(response.getEntity(), "utf-8");
                LogUtils.log("MonitorProtocolAgent.syncTime", "receive HelloResponse:" + result);
                DDAPMessage ddapMessage = new DDAPMessage().fromJson(result);
                if (ddapMessage.getCode() == FieldConfigUtils.getCode(ErrorCodeMapping.E_OK)) {
                    YZXDCAPMessage yzxdcapMessage = ddapMessage.getData();
                    HelloResponse helloResponse = new HelloResponse().fromJson(JsonBean.ObjectToJson(yzxdcapMessage.getData()));
                    this.center_ip_port = "ssl://" + helloResponse.getIot_access().getHost();
                    this.productKey = helloResponse.getIot_access().getProduct_key();
                    this.deviceName = helloResponse.getIot_access().getDevice_name();
                    this.productSecret = helloResponse.getIot_access().getProduct_secret();
                    this.deviceSecret = helloResponse.getIot_access().getDevice_secret();
                    this.topicOut = MqttTopic.TOPIC_LEVEL_SEPARATOR + this.productKey + MqttTopic.TOPIC_LEVEL_SEPARATOR + this.deviceName + "/out";
                }
            }
            return true;
        } catch (Exception e) {
            LogUtils.log("MonitorProtocolAgent.syncTime", Log.getStackTraceString(e));
            return false;
        }
    }

    private String createHelloDDAPRequestMessage() {
        String sn = HardwareStatusCacheProvider.getInstance().getHardwareStatus().getSn();
        String firmware = SoftwareStatusCacheProvider.getInstance().getFirewareVer();
        YZXDCAPMessage yzxdcapMessage = new YZXDCAPMessage();
        HelloRequest helloRequest = new HelloRequest();
        DDAPDeviceSystem ddapDeviceSystem = new DDAPDeviceSystem();
        yzxdcapMessage.setTs(Long.valueOf(System.currentTimeMillis()));
        yzxdcapMessage.setSeq(Long.valueOf(genSeq()));
        yzxdcapMessage.setProt_type(YZXDCAPMessage.PROT_TYPE_DDAP);
        yzxdcapMessage.setProt_ver("1.00");
        yzxdcapMessage.setOp(YZXDCAPMessage.OP_HELLO);
        ddapDeviceSystem.setType(DDAPDeviceSystem.TYPE_TERMINAL);
        ddapDeviceSystem.setVid(DDAPDeviceSystem.VID_YZXTECH);
        ddapDeviceSystem.setPid(DDAPDeviceSystem.PID_C2);
        ddapDeviceSystem.setSn(sn);
        ddapDeviceSystem.setFirmware(firmware);
        helloRequest.setSystem(ddapDeviceSystem);
        yzxdcapMessage.setData(helloRequest);
        LogUtils.log("MonitorProtocolAgent.createHelloDDAPRequestMessage", "sendHelloRequest:" + yzxdcapMessage.toJson());
        return yzxdcapMessage.toJson();
    }

    public long genSeq() {
        return this.seqGen.incrementAndGet();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isConnected() {
        return this.isConnected;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initConnection() {
        this.handler.sendEmptyMessage(69633);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class ConnectTask implements Runnable {
        private String clientId;
        private String idtype;
        private String sdkVersion;
        private String topicIn;

        private ConnectTask() {
            this.idtype = "0";
            this.sdkVersion = "1.0.0";
            this.clientId = String.valueOf(MonitorProtocolAgent.this.productKey) + ":" + MonitorProtocolAgent.this.deviceName + ":" + this.idtype + ((this.sdkVersion == null || "".equals(this.sdkVersion)) ? "" : ":" + this.sdkVersion);
            this.topicIn = MqttTopic.TOPIC_LEVEL_SEPARATOR + MonitorProtocolAgent.this.productKey + MqttTopic.TOPIC_LEVEL_SEPARATOR + MonitorProtocolAgent.this.deviceName + "/in";
        }

        /* synthetic */ ConnectTask(MonitorProtocolAgent monitorProtocolAgent, ConnectTask connectTask) {
            this();
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                MemoryPersistence persistence = new MemoryPersistence();
                SSLSocketFactory socketFactory = MonitorProtocolAgent.this.createSSLSocket();
                MonitorProtocolAgent.this.sampleClient = new MqttClient(MonitorProtocolAgent.this.center_ip_port, this.clientId, persistence);
                final MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setMqttVersion(4);
                connOpts.setSocketFactory(socketFactory);
                connOpts.setCleanSession(true);
                String sign = String.valueOf(MonitorProtocolAgent.this.productKey) + MonitorProtocolAgent.this.productSecret + MonitorProtocolAgent.this.deviceName + MonitorProtocolAgent.this.deviceSecret;
                String signUserName = Md5.getInstance().md5_32(sign).toUpperCase();
                connOpts.setUserName(signUserName);
                connOpts.setConnectionTimeout(10);
                connOpts.setKeepAliveInterval(60);
                MonitorProtocolAgent.this.sampleClient.connect(connOpts);
                MonitorProtocolAgent.this.sampleClient.setCallback(new MqttCallback() { // from class: com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.ConnectTask.1
                    @Override // org.eclipse.paho.client.mqttv3.MqttCallback
                    public void connectionLost(Throwable cause) {
                        LogUtils.log("MonitorProtocolAgent.connectionLost", "连接失败,原因:" + cause);
                        while (MonitorProtocolAgent.this.sampleClient != null && !MonitorProtocolAgent.this.sampleClient.isConnected()) {
                            try {
                                Thread.sleep(1000L);
                                MonitorProtocolAgent.this.sampleClient.connect(connOpts);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override // org.eclipse.paho.client.mqttv3.MqttCallback
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        String recvMsg = new String(message.getPayload(), CharEncoding.UTF_8);
                        YZXDCAPMessage yzxdcapMessage = new YZXDCAPMessage().fromJson(recvMsg);
                        MonitorProtocolAgent.this.handler.sendMessage(MonitorProtocolAgent.this.handler.obtainMessage(69640, yzxdcapMessage));
                    }

                    @Override // org.eclipse.paho.client.mqttv3.MqttCallback
                    public void deliveryComplete(IMqttDeliveryToken token) {
                    }
                });
                LogUtils.log("MonitorProtocolAgent.ConnectTask", "连接成功！！！");
                MonitorProtocolAgent.this.handler.sendEmptyMessage(69634);
                MonitorProtocolAgent.this.sampleClient.subscribe(this.topicIn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public SSLSocketFactory createSSLSocket() throws Exception {
        InputStream in = this.context.getResources().openRawResource(R.raw.pubkey);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate ca = null;
        try {
            ca = cf.generateCertificate(in);
        } catch (CertificateException e) {
            e.printStackTrace();
        } finally {
            in.close();
        }
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);
        SSLContext context = SSLContext.getInstance("TLSV1.2");
        context.init(null, tmf.getTrustManagers(), null);
        SSLSocketFactory socketFactory = context.getSocketFactory();
        return socketFactory;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void connect() {
        if (HardwareStatusCacheProvider.getInstance().isNetworkConnected()) {
            if (this.sampleClient == null || !this.sampleClient.isConnected()) {
                LogUtils.log("MonitorProtocolAgent.connect", "init connection !!!");
                this.connectThreadPoolExecutor.execute(new ConnectTask(this, null));
                return;
            }
            return;
        }
        this.handler.sendEmptyMessageDelayed(69633, 5000L);
    }

    public boolean sendMessage(YZXDCAPMessage yzxdcapMessage) {
        MqttMessage mqttMessage = new MqttMessage(yzxdcapMessage.toJson().getBytes());
        mqttMessage.setQos(1);
        try {
            if (this.sampleClient != null) {
                this.sampleClient.publish(this.topicOut, mqttMessage);
                return this.handler.sendMessage(this.handler.obtainMessage(69637, yzxdcapMessage));
            }
        } catch (Exception e) {
            LogUtils.log("MonitorProtocolAgent.sendMessage", "send message exception");
        }
        return false;
    }

    private MonitorPortHandler getPortHandler(String port) {
        return this.portHandlers.get(port);
    }

    public String getPort(String chargeId) {
        for (MonitorPortHandler portHandler : this.portHandlers.values()) {
            String port = portHandler.getPort(chargeId);
            if (!TextUtils.isEmpty(port)) {
                return port;
            }
        }
        return null;
    }

    public MonitorChargeSession getChargeSession(String port) {
        MonitorPortHandler monitorPortHandler = getPortHandler(port);
        if (monitorPortHandler == null) {
            LogUtils.log("MonitorProtocolAgent.getChargeSession", "no available port handler for port: " + port);
            return null;
        }
        return monitorPortHandler.getChargeSession();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchYZXMessage(YZXDCAPMessage msg, YZXDCAPMessage sendedRequest) {
        if (sendedRequest == null) {
            handleRequestMessage(msg);
        } else {
            handleResponseMessage(msg, sendedRequest);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendNetworkRequest() {
        try {
            YZXDCAPMessage request = createDAPRequest("report", YZXDCAPOption.NETWORK);
            Network network = HardwareStatusCacheProvider.getInstance().getNetworkStatus();
            if (network.isConnected()) {
                YZXPropset yzxPropset = new YZXPropset();
                List<YZXProperty> propset = new ArrayList<>();
                YZXProperty yzxProperty = new YZXProperty();
                String active = network.getActive();
                if (Network.NETWORK_TYPE_ETHERNET.equals(active)) {
                    yzxProperty.setId(YZXDCAPOption.NETWORK);
                    DAPEthernet dapEthernet = new DAPEthernet();
                    DDAPEthernetAccess ddapEthernetAccess = new DDAPEthernetAccess();
                    Ethernet ethernet = network.getEthernet();
                    if (ethernet != null) {
                        String ip = ethernet.getIp();
                        if (!TextUtils.isEmpty(ip)) {
                            ddapEthernetAccess.setIp(ip);
                        }
                        String mask = ethernet.getMask();
                        if (!TextUtils.isEmpty(mask)) {
                            ddapEthernetAccess.setMask(mask);
                        }
                        String gw = ethernet.getGw();
                        if (!TextUtils.isEmpty(gw)) {
                            ddapEthernetAccess.setGw(gw);
                        }
                        String dns = ethernet.getDns();
                        if (!TextUtils.isEmpty(dns)) {
                            ddapEthernetAccess.setDns(dns);
                        }
                        String mac = ethernet.getMac();
                        if (!TextUtils.isEmpty(mac)) {
                            ddapEthernetAccess.setMac(mac);
                        }
                        dapEthernet.setEthernet(ddapEthernetAccess);
                        yzxProperty.setValue(dapEthernet);
                        propset.add(yzxProperty);
                        yzxPropset.setPropset(propset);
                        request.setData(yzxPropset);
                    }
                } else if (Network.NETWORK_TYPE_MOBILE.equals(active)) {
                    yzxProperty.setId(YZXDCAPOption.NETWORK);
                    DAPMobile dapMobile = new DAPMobile();
                    DDAPMobileAccess ddapMobileAccess = new DDAPMobileAccess();
                    MobileNet mobileNet = network.getMobile();
                    if (mobileNet != null) {
                        ddapMobileAccess.setType(mobileNet.getType());
                        String PLMN = mobileNet.getPLMN();
                        if (!TextUtils.isEmpty(PLMN)) {
                            ddapMobileAccess.setPLMN(PLMN);
                        }
                        String ip2 = mobileNet.getIp();
                        if (!TextUtils.isEmpty(ip2)) {
                            ddapMobileAccess.setIp(ip2);
                        }
                        String gw2 = mobileNet.getGw();
                        if (!TextUtils.isEmpty(gw2)) {
                            ddapMobileAccess.setGw(gw2);
                        }
                        String dns2 = mobileNet.getDns();
                        if (!TextUtils.isEmpty(dns2)) {
                            ddapMobileAccess.setDns(dns2);
                        }
                        String IMEI = mobileNet.getIMEI();
                        if (!TextUtils.isEmpty(IMEI)) {
                            ddapMobileAccess.setIMEI(IMEI);
                        }
                        String IMSI = mobileNet.getIMSI();
                        if (!TextUtils.isEmpty(IMSI)) {
                            ddapMobileAccess.setIMSI(IMSI);
                        }
                        dapMobile.setMoblie(ddapMobileAccess);
                        yzxProperty.setValue(dapMobile);
                        propset.add(yzxProperty);
                        yzxPropset.setPropset(propset);
                        request.setData(yzxPropset);
                    }
                }
                sendMessage(request);
                LogUtils.log("MonitorProtocolAgent.sendNetworkRequest", "send NetworkRequest:" + request.toJson());
            }
        } catch (Exception e) {
            LogUtils.log("MonitorProtocolAgent.sendNetworkRequest", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendCapabilityRequest() {
        try {
            YZXDCAPMessage request = createDAPRequest("report", YZXDCAPOption.CAPABILITY);
            YZXPropset yzxPropset = new YZXPropset();
            List<YZXProperty> propset = new ArrayList<>();
            YZXProperty yzxProperty = new YZXProperty();
            yzxProperty.setId(YZXDCAPOption.CAPABILITY);
            DAPCapability dapCapability = new DAPCapability();
            int ampCapacity = ChargeStatusCacheProvider.getInstance().getAmpCapacity();
            dapCapability.setAmp_capacity(ampCapacity);
            Port port = DCAPProxy.getInstance().getPortStatus("1");
            if (port != null && port.getVolts() != null) {
                double volt = port.getVolts().get(0).doubleValue();
                dapCapability.setKwatt_capacity((int) (ampCapacity * volt));
            }
            PHASE phase = HardwareStatusCacheProvider.getInstance().getHardwareStatus().getPhase();
            switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$PHASE()[phase.ordinal()]) {
                case 2:
                    dapCapability.setCurrent_type("ac");
                    dapCapability.setPhases(1);
                    break;
                case 3:
                    dapCapability.setCurrent_type("ac");
                    dapCapability.setPhases(3);
                    break;
                case 4:
                    dapCapability.setCurrent_type("dc");
                    break;
            }
            HashMap<String, Port> hashMap = HardwareStatusCacheProvider.getInstance().getPorts();
            if (hashMap != null) {
                dapCapability.setPorts(hashMap.size());
            }
            PortSetting portSetting = LocalSettingCacheProvider.getInstance().getChargePortSetting("1");
            if (portSetting != null) {
                dapCapability.setGun_lock(portSetting.isEnable());
            }
            WindowManager wm = (WindowManager) this.context.getSystemService("window");
            int width = wm.getDefaultDisplay().getWidth();
            int height = wm.getDefaultDisplay().getHeight();
            if (width > 0 && height > 0) {
                dapCapability.setScreen(String.valueOf(width) + "." + height);
            } else {
                dapCapability.setScreen("none");
            }
            dapCapability.setRadar(HardwareStatusCacheProvider.getInstance().getPortRadarSwitch("1"));
            yzxProperty.setValue(dapCapability);
            propset.add(yzxProperty);
            yzxPropset.setPropset(propset);
            request.setData(yzxPropset);
            sendMessage(request);
            LogUtils.log("MonitorProtocolAgent.sendCapabilityRequest", "send CapabilityRequest:" + request.toJson());
        } catch (Exception e) {
            LogUtils.log("MonitorProtocolAgent.sendCapabilityRequest", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendVersionRequest() {
        try {
            YZXDCAPMessage request = createDAPRequest("report", YZXDCAPOption.VERSION);
            YZXPropset yzxPropset = new YZXPropset();
            List<YZXProperty> propset = new ArrayList<>();
            YZXProperty yzxProperty = new YZXProperty();
            yzxProperty.setId(YZXDCAPOption.VERSION);
            DAPVersion dapVersion = new DAPVersion();
            String osVer = SoftwareStatusCacheProvider.getInstance().getOsVer();
            if (!TextUtils.isEmpty(osVer)) {
                dapVersion.setOs_ver(osVer);
            }
            String firewareVer = SoftwareStatusCacheProvider.getInstance().getFirewareVer();
            if (!TextUtils.isEmpty(firewareVer)) {
                dapVersion.setFirmware_ver(firewareVer);
            }
            String appVer = SoftwareStatusCacheProvider.getInstance().getAppVer();
            if (!TextUtils.isEmpty(appVer)) {
                dapVersion.setApp_ver(appVer);
            }
            yzxProperty.setValue(dapVersion);
            propset.add(yzxProperty);
            yzxPropset.setPropset(propset);
            request.setData(yzxPropset);
            sendMessage(request);
            LogUtils.log("MonitorProtocolAgent.sendVersionRequest", "send VersionRequest:" + request.toJson());
        } catch (Exception e) {
            LogUtils.log("MonitorProtocolAgent.sendVersionRequest", Log.getStackTraceString(e));
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code restructure failed: missing block: B:21:0x00a3, code lost:
        if (r8.equals(com.xcharge.charger.protocol.monitor.bean.YZXDCAPOption.GUN_LOCK_STATUS) != false) goto L20;
     */
    /* JADX WARN: Removed duplicated region for block: B:24:0x00a9 A[Catch: Exception -> 0x0071, TryCatch #0 {Exception -> 0x0071, blocks: (B:3:0x0001, B:4:0x0009, B:5:0x000c, B:7:0x0012, B:9:0x001a, B:10:0x0045, B:12:0x004b, B:16:0x007c, B:17:0x0092, B:18:0x0095, B:19:0x009a, B:20:0x009d, B:22:0x00a5, B:24:0x00a9, B:26:0x00b2, B:28:0x00bd, B:62:0x015a, B:64:0x0165, B:65:0x016e, B:67:0x0179, B:68:0x0182, B:70:0x018d, B:71:0x0196, B:72:0x01b5, B:29:0x00c5, B:32:0x00ce, B:35:0x00d7, B:37:0x00df, B:38:0x00e7, B:40:0x00ef, B:41:0x00f7, B:43:0x00ff, B:44:0x0107, B:46:0x010f, B:47:0x0113, B:50:0x011d, B:52:0x0125, B:53:0x012e, B:55:0x0136, B:56:0x013f, B:58:0x0147, B:59:0x0150), top: B:74:0x0001 }] */
    /* JADX WARN: Removed duplicated region for block: B:72:0x01b5 A[Catch: Exception -> 0x0071, TRY_LEAVE, TryCatch #0 {Exception -> 0x0071, blocks: (B:3:0x0001, B:4:0x0009, B:5:0x000c, B:7:0x0012, B:9:0x001a, B:10:0x0045, B:12:0x004b, B:16:0x007c, B:17:0x0092, B:18:0x0095, B:19:0x009a, B:20:0x009d, B:22:0x00a5, B:24:0x00a9, B:26:0x00b2, B:28:0x00bd, B:62:0x015a, B:64:0x0165, B:65:0x016e, B:67:0x0179, B:68:0x0182, B:70:0x018d, B:71:0x0196, B:72:0x01b5, B:29:0x00c5, B:32:0x00ce, B:35:0x00d7, B:37:0x00df, B:38:0x00e7, B:40:0x00ef, B:41:0x00f7, B:43:0x00ff, B:44:0x0107, B:46:0x010f, B:47:0x0113, B:50:0x011d, B:52:0x0125, B:53:0x012e, B:55:0x0136, B:56:0x013f, B:58:0x0147, B:59:0x0150), top: B:74:0x0001 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private void handleRequestMessage(com.xcharge.charger.protocol.monitor.bean.YZXDCAPMessage r12) {
        /*
            Method dump skipped, instructions count: 500
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.handleRequestMessage(com.xcharge.charger.protocol.monitor.bean.YZXDCAPMessage):void");
    }

    private YZXProperty handleQueryFeePolicyRequest(YZXDCAPMessage yzxdcapMessage) {
        HashMap<String, FeeRate> feeRates;
        try {
            YZXProperty yzxProperty = new YZXProperty();
            yzxProperty.setId(YZXDCAPOption.FEE_POLICY);
            DAPFeePolicy dapFeePolicy = new DAPFeePolicy();
            DAPFeeRates fee_policy = new DAPFeeRates();
            HashMap<String, DAPFeeRate> fee_rates = new HashMap<>();
            DAPFeeRate dapFeeRate = new DAPFeeRate();
            PortFeeRate portFeeRate = RemoteSettingCacheProvider.getInstance().getPortFeeRate("1");
            if (portFeeRate != null) {
                String activeFeeRateId = portFeeRate.getActiveFeeRateId();
                if (!TextUtils.isEmpty(activeFeeRateId) && (feeRates = portFeeRate.getFeeRates()) != null) {
                    FeeRate feeRate = feeRates.get(activeFeeRateId);
                    dapFeeRate.setFee_rate_id(feeRate.getFeeRateId());
                    dapFeeRate.setPower_price(feeRate.getPowerPrice());
                    dapFeeRate.setService_price(feeRate.getServicePrice());
                    dapFeeRate.setDelay_price(feeRate.getDelayPrice());
                    fee_rates.put(activeFeeRateId, dapFeeRate);
                    fee_policy.setDefault_fee_rate_id(activeFeeRateId);
                    fee_policy.setFee_rates(fee_rates);
                    dapFeePolicy.setFee_policy(fee_policy);
                    yzxProperty.setValue(dapFeePolicy);
                    return yzxProperty;
                }
            } else {
                createErrorResponse(yzxdcapMessage, ErrorCodeMapping.E_PROP_VALUE_NULL);
            }
        } catch (Exception e) {
            LogUtils.log("MonitorProtocolAgent.handleQueryFeePolicyRequest", Log.getStackTraceString(e));
        }
        return null;
    }

    private YZXProperty handleQueryRadarParamRequest(YZXDCAPMessage yzxdcapMessage) {
        try {
            PortSetting portSetting = RemoteSettingCacheProvider.getInstance().getChargePortSetting("1");
            if (portSetting != null) {
                YZXProperty yzxProperty = new YZXProperty();
                yzxProperty.setId(YZXDCAPOption.RADAR_PARAM);
                RadarSetting radarSetting = portSetting.getRadarSetting();
                DAPRadarParam dapRadarParam = new DAPRadarParam();
                dapRadarParam.setDist(radarSetting.getDistance());
                dapRadarParam.setWork_time(radarSetting.getWorkTime());
                yzxProperty.setValue(dapRadarParam);
                return yzxProperty;
            }
        } catch (Exception e) {
            LogUtils.log("MonitorProtocolAgent.handleQueryRadarParamRequest", Log.getStackTraceString(e));
        }
        return null;
    }

    private YZXProperty handleQueryTimingParamRequest(YZXDCAPMessage yzxdcapMessage) {
        try {
            YZXProperty yzxProperty = new YZXProperty();
            yzxProperty.setId(YZXDCAPOption.TIMING_PARAM);
            TimerSetting timerSetting = RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting();
            DAPTimingParam dapTimingParam = new DAPTimingParam();
            dapTimingParam.setInterval_charge_cancel(timerSetting.getIntervalChargeCancel());
            dapTimingParam.setInterval_delay_start(timerSetting.getIntervalDelayStart());
            dapTimingParam.setInterval_charge_report(timerSetting.getIntervalChargeReport());
            dapTimingParam.setInterval_standby(timerSetting.getIntervalStandby());
            yzxProperty.setValue(dapTimingParam);
            return yzxProperty;
        } catch (Exception e) {
            LogUtils.log("MonitorProtocolAgent.handleQueryTimingParamRequest", Log.getStackTraceString(e));
            return null;
        }
    }

    private YZXProperty handleQueryCPRangeRequest(YZXDCAPMessage yzxdcapMessage) {
        try {
            YZXProperty yzxProperty = new YZXProperty();
            yzxProperty.setId(YZXDCAPOption.CP_RANGE);
            yzxProperty.setValue(Integer.valueOf(RemoteSettingCacheProvider.getInstance().getChargeSetting().getCpRange()));
            return yzxProperty;
        } catch (Exception e) {
            LogUtils.log("MonitorProtocolAgent.handleQueryCPRangeRequest", Log.getStackTraceString(e));
            return null;
        }
    }

    private YZXProperty handleQueryVoltRangeRequest(YZXDCAPMessage yzxdcapMessage) {
        try {
            YZXProperty yzxProperty = new YZXProperty();
            yzxProperty.setId(YZXDCAPOption.VOLT_RANGE);
            yzxProperty.setValue(Integer.valueOf(RemoteSettingCacheProvider.getInstance().getChargeSetting().getVoltageRange()));
            return yzxProperty;
        } catch (Exception e) {
            LogUtils.log("MonitorProtocolAgent.handleQueryVoltRangeRequest", Log.getStackTraceString(e));
            return null;
        }
    }

    private YZXProperty handleQueryAmpPwmRequest(YZXDCAPMessage yzxdcapMessage) {
        try {
            YZXProperty yzxProperty = new YZXProperty();
            yzxProperty.setId(YZXDCAPOption.AMP_PWM);
            yzxProperty.setValue(Integer.valueOf(RemoteSettingCacheProvider.getInstance().getChargeSetting().getAdjustAmp()));
            return yzxProperty;
        } catch (Exception e) {
            LogUtils.log("MonitorProtocolAgent.handleQueryAmpPwmRequest", Log.getStackTraceString(e));
            return null;
        }
    }

    private YZXProperty handleQueryErrorRequest(YZXDCAPMessage yzxdcapMessage) {
        try {
            YZXProperty yzxProperty = new YZXProperty();
            yzxProperty.setId("error");
            DAPError dapError = new DAPError();
            List<YZXDCAPError> error_set = new ArrayList<>();
            HashMap<String, ErrorCode> errors = HardwareStatusCacheProvider.getInstance().getAllDeviceErrors("1");
            if (errors != null) {
                for (ErrorCode error : errors.values()) {
                    YZXDCAPError yzxdcapError = new YZXDCAPError();
                    switch (error.getCode()) {
                        case ErrorCode.EC_DEVICE_NOT_INIT /* 30010 */:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_NOT_INIT);
                            break;
                        case ErrorCode.EC_DEVICE_NO_GROUND /* 30011 */:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_NO_GROUND);
                            break;
                        case ErrorCode.EC_DEVICE_LOST_PHASE /* 30012 */:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_PHASE_LOST);
                            break;
                        case ErrorCode.EC_DEVICE_EMERGENCY_STOP /* 30013 */:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_EMERGENCY_STOP);
                            break;
                        case ErrorCode.EC_DEVICE_VOLT_ERROR /* 30014 */:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_VOLT);
                            break;
                        case ErrorCode.EC_DEVICE_AMP_ERROR /* 30015 */:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_AMP);
                            break;
                        case ErrorCode.EC_DEVICE_TEMP_ERROR /* 30016 */:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_TEMPRATURE);
                            break;
                        case ErrorCode.EC_DEVICE_POWER_LEAK /* 30017 */:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_LEAK_AMP);
                            break;
                        case ErrorCode.EC_DEVICE_COMM_ERROR /* 30018 */:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_COMM);
                            break;
                    }
                    error_set.add(yzxdcapError);
                }
            }
            dapError.setError_set(error_set);
            yzxProperty.setValue(dapError);
            return yzxProperty;
        } catch (Exception e) {
            LogUtils.log("MonitorProtocolAgent.handleQueryErrorRequest", Log.getStackTraceString(e));
            return null;
        }
    }

    private void handleQueryAmmeterRequest(YZXDCAPMessage yzxdcapMessage) {
        try {
            YZXDCAPMessage response = createResponse(yzxdcapMessage);
            YZXPropset yzxPropset = new YZXPropset();
            List<YZXProperty> propset = new ArrayList<>();
            YZXProperty yzxProperty = new YZXProperty();
            yzxProperty.setId(YZXDCAPOption.AMMETER);
            Port port = HardwareStatusCacheProvider.getInstance().getPort("1");
            yzxProperty.setValue(port.getMeter());
            propset.add(yzxProperty);
            yzxPropset.setPropset(propset);
            response.setData(yzxPropset);
            sendMessage(response);
            LogUtils.log("MonitorProtocolAgent.handleQueryAmmeterRequest", "send QueryAmmeterResponse:" + response.toJson());
        } catch (Exception e) {
            LogUtils.log("MonitorProtocolAgent.handleQueryAmmeterRequest", Log.getStackTraceString(e));
        }
    }

    private void handleResponseMessage(YZXDCAPMessage response, YZXDCAPMessage request) {
        String port = request.getPort();
        if (!TextUtils.isEmpty(port)) {
            MonitorPortHandler portHandler = getPortHandler(port);
            MonitorRequestSession yzxRequestSession = new MonitorRequestSession();
            yzxRequestSession.setRequest(request);
            yzxRequestSession.setResponse(response);
            portHandler.sendMessage(portHandler.obtainMessage(73736, yzxRequestSession));
            return;
        }
        String op = response.getOpt().getOp();
        op.hashCode();
    }

    public void handleDelayWaitStartedRequest(String chargeId) {
        String port = getPort(chargeId);
        if (!TextUtils.isEmpty(port)) {
            MonitorPortHandler yzxPortHandler = getPortHandler(port);
            if (yzxPortHandler == null) {
                LogUtils.log("MonitorProtocolAgent.handleDelayWaitStartedRequest", "no available port handler for port: " + port);
                return;
            } else {
                yzxPortHandler.sendDelayWaitStartedRequest(chargeId);
                return;
            }
        }
        LogUtils.log("MonitorProtocolAgent.handleDelayWaitStartedRequest", "failed to find related port for charge: " + chargeId);
    }

    public void handleDelayStartedRequest(String chargeId, long delayStart) {
        String port = getPort(chargeId);
        if (!TextUtils.isEmpty(port)) {
            MonitorPortHandler yzxPortHandler = getPortHandler(port);
            if (yzxPortHandler == null) {
                LogUtils.log("MonitorProtocolAgent.handleDelayStartedRequest", "no available port handler for port: " + port);
                return;
            } else {
                yzxPortHandler.sendDelayStartedRequest(chargeId, delayStart);
                return;
            }
        }
        LogUtils.log("MonitorProtocolAgent.handleDelayStartedRequest", "failed to find related port for charge: " + chargeId);
    }

    public void createErrorResponse(YZXDCAPMessage yzxdcapMessage, ErrorCodeMapping codeMapping) {
        try {
            YZXDCAPMessage responseMessage = createResponse(yzxdcapMessage);
            YZXDCAPError yzxdcapError = new YZXDCAPError();
            yzxdcapError.init(codeMapping);
            responseMessage.setError(yzxdcapError);
            sendMessage(responseMessage);
            LogUtils.log("MonitorProtocolAgent.responseYZXDCAPErrorMessage", "send YZXDCAPErrorMessage:" + responseMessage.toJson());
        } catch (Exception e) {
            LogUtils.log("MonitorProtocolAgent.responseYZXDCAPErrorMessage", Log.getStackTraceString(e));
        }
    }

    public YZXDCAPMessage createCAPRequest(String op, String prop_id) {
        YZXDCAPMessage yzxdcapMessage = new YZXDCAPMessage();
        yzxdcapMessage.setTs(Long.valueOf(System.currentTimeMillis()));
        yzxdcapMessage.setSeq(Long.valueOf(genSeq()));
        yzxdcapMessage.setOp(op);
        YZXDCAPOption yzxdcapOption = new YZXDCAPOption();
        if (!TextUtils.isEmpty(prop_id)) {
            List<String> prop_ids = new ArrayList<>();
            prop_ids.add(prop_id);
            yzxdcapOption.setProp_id(prop_ids);
            yzxdcapMessage.setOpt(yzxdcapOption);
        }
        yzxdcapMessage.setProt_type("cap");
        yzxdcapMessage.setProt_ver("1.00");
        return yzxdcapMessage;
    }

    public YZXDCAPMessage createDAPRequest(String op, String prop_id) {
        YZXDCAPMessage yzxdcapMessage = new YZXDCAPMessage();
        yzxdcapMessage.setTs(Long.valueOf(System.currentTimeMillis()));
        yzxdcapMessage.setSeq(Long.valueOf(genSeq()));
        yzxdcapMessage.setOp(op);
        YZXDCAPOption yzxdcapOption = new YZXDCAPOption();
        if (!TextUtils.isEmpty(prop_id)) {
            List<String> prop_ids = new ArrayList<>();
            prop_ids.add(prop_id);
            yzxdcapOption.setProp_id(prop_ids);
            yzxdcapMessage.setOpt(yzxdcapOption);
        }
        yzxdcapMessage.setProt_type(YZXDCAPMessage.PROT_TYPE_DAP);
        yzxdcapMessage.setProt_ver("1.00");
        return yzxdcapMessage;
    }

    public YZXDCAPMessage createResponse(YZXDCAPMessage request) {
        YZXDCAPMessage response = new YZXDCAPMessage();
        YZXDCAPOption option = new YZXDCAPOption();
        response.setVer(request.getVer());
        response.setTs(Long.valueOf(System.currentTimeMillis()));
        response.setSeq(Long.valueOf(genSeq()));
        response.setNonce(RandomUtils.getCharAndNumr(8));
        response.setXid(request.getXid());
        response.setProt_type(request.getProt_type());
        response.setProt_ver(request.getProt_ver());
        response.setOp("ack");
        option.setOp(request.getOp());
        option.setSeq(request.getSeq());
        YZXDCAPOption requestOption = request.getOpt();
        if (requestOption != null) {
            option.setSubnode(requestOption.getSubnode());
            option.setProp_id(requestOption.getProp_id());
        }
        response.setOpt(option);
        return response;
    }

    private void handleSendRequestOk(YZXDCAPMessage msg) {
        String port = msg.getPort();
        if (!TextUtils.isEmpty(port)) {
            MonitorPortHandler portHandler = getPortHandler(port);
            portHandler.sendMessage(portHandler.obtainMessage(73731, msg));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleSendRequestFail(YZXDCAPMessage request) {
        String port = request.getPort();
        if (!TextUtils.isEmpty(port)) {
            MonitorPortHandler portHandler = getPortHandler(port);
            portHandler.sendMessage(portHandler.obtainMessage(73732, request));
        }
    }

    private void handleRequestTimeout(YZXDCAPMessage request) {
        String port = request.getPort();
        if (!TextUtils.isEmpty(port)) {
            MonitorPortHandler portHandler = getPortHandler(port);
            portHandler.sendMessage(portHandler.obtainMessage(73733, request));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void requestTimeoutCheck() {
        if (this.sendReqestState.size() > 0) {
            Iterator<Map.Entry<String, SendRequestState>> it2 = this.sendReqestState.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry<String, SendRequestState> entry = it2.next();
                SendRequestState requestState = entry.getValue();
                MONITOR_REQUEST_STATE state = requestState.status;
                long timestamp = requestState.timestamp;
                if (MONITOR_REQUEST_STATE.sending.equals(state)) {
                    if (System.currentTimeMillis() - timestamp > 5000) {
                        it2.remove();
                        handleSendRequestFail(requestState.request);
                    }
                } else if (MONITOR_REQUEST_STATE.sended.equals(state) && System.currentTimeMillis() - timestamp > 10000) {
                    it2.remove();
                    handleRequestTimeout(requestState.request);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void portsActive() {
        for (MonitorPortHandler portHandler : this.portHandlers.values()) {
            portHandler.sendMessage(portHandler.obtainMessage(73729));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void portsDeactive() {
        for (MonitorPortHandler portHandler : this.portHandlers.values()) {
            portHandler.sendMessage(portHandler.obtainMessage(73730));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isRequestMessage(String op) {
        return !"ack".equals(op);
    }
}
