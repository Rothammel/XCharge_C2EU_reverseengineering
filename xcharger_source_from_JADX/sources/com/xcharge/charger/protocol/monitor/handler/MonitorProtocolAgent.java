package com.xcharge.charger.protocol.monitor.handler;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
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
import com.xcharge.charger.protocol.monitor.C0273R;
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
import java.security.SecureRandom;
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
import javax.net.ssl.KeyManager;
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
import org.apache.http.params.HttpParams;
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
    /* access modifiers changed from: private */
    public String center_ip_port;
    private ThreadPoolExecutor connectThreadPoolExecutor = null;
    private Context context = null;
    /* access modifiers changed from: private */
    public String deviceName;
    /* access modifiers changed from: private */
    public String deviceSecret;
    /* access modifiers changed from: private */
    public MsgHandler handler = null;
    /* access modifiers changed from: private */
    public HandlerTimer handlerTimer = null;
    /* access modifiers changed from: private */
    public boolean isConnected = false;
    private NetworkStatusObserver networkStatusObserver = null;
    private HashMap<String, MonitorPortHandler> portHandlers = null;
    /* access modifiers changed from: private */
    public String productKey;
    /* access modifiers changed from: private */
    public String productSecret;
    /* access modifiers changed from: private */
    public MqttClient sampleClient = null;
    /* access modifiers changed from: private */
    public HashMap<String, SendRequestState> sendReqestState = null;
    private AtomicLong seqGen = new AtomicLong(0);
    private HandlerThread thread = null;
    private String topicOut;

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

    public static MonitorProtocolAgent getInstance() {
        if (instance == null) {
            instance = new MonitorProtocolAgent();
        }
        return instance;
    }

    private static class SendRequestState {
        YZXDCAPMessage request;
        MONITOR_REQUEST_STATE status;
        long timestamp;

        private SendRequestState() {
            this.request = null;
            this.status = null;
            this.timestamp = 0;
        }

        /* synthetic */ SendRequestState(SendRequestState sendRequestState) {
            this();
        }
    }

    private class MsgHandler extends Handler {
        public MsgHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(android.os.Message r14) {
            /*
                r13 = this;
                r7 = 0
                r6 = 0
                int r8 = r14.what     // Catch:{ Exception -> 0x0011 }
                switch(r8) {
                    case 69633: goto L_0x000b;
                    case 69634: goto L_0x002b;
                    case 69635: goto L_0x005c;
                    case 69637: goto L_0x008a;
                    case 69639: goto L_0x0174;
                    case 69640: goto L_0x00dc;
                    case 69641: goto L_0x0199;
                    case 69648: goto L_0x01af;
                    case 135169: goto L_0x01ef;
                    default: goto L_0x0007;
                }
            L_0x0007:
                super.handleMessage(r14)
                return
            L_0x000b:
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                r8.connect()     // Catch:{ Exception -> 0x0011 }
                goto L_0x0007
            L_0x0011:
                r2 = move-exception
                java.lang.String r8 = "MonitorProtocolAgent.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder
                java.lang.String r10 = "except: "
                r9.<init>(r10)
                java.lang.String r10 = android.util.Log.getStackTraceString(r2)
                java.lang.StringBuilder r9 = r9.append(r10)
                java.lang.String r9 = r9.toString()
                android.util.Log.e(r8, r9)
                goto L_0x0007
            L_0x002b:
                java.lang.String r8 = "MonitorProtocolAgent.handleMessage"
                java.lang.String r9 = "connected !!!"
                com.xcharge.charger.protocol.monitor.util.LogUtils.log(r8, r9)     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                r9 = 1
                r8.isConnected = r9     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                com.xcharge.common.utils.HandlerTimer r8 = r8.handlerTimer     // Catch:{ Exception -> 0x0011 }
                r10 = 1000(0x3e8, double:4.94E-321)
                r9 = 69641(0x11009, float:9.7588E-41)
                r12 = 0
                r8.startTimer(r10, r9, r12)     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                r8.sendNetworkRequest()     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                r8.sendCapabilityRequest()     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                r8.sendVersionRequest()     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                r8.portsActive()     // Catch:{ Exception -> 0x0011 }
                goto L_0x0007
            L_0x005c:
                java.lang.String r8 = "MonitorProtocolAgent.handleMessage"
                java.lang.String r9 = "disconnected !!!"
                com.xcharge.charger.protocol.monitor.util.LogUtils.log(r8, r9)     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                r9 = 0
                r8.isConnected = r9     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                com.xcharge.common.utils.HandlerTimer r8 = r8.handlerTimer     // Catch:{ Exception -> 0x0011 }
                r9 = 69641(0x11009, float:9.7588E-41)
                r8.stopTimer(r9)     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                r8.portsDeactive()     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent$MsgHandler r8 = r8.handler     // Catch:{ Exception -> 0x0011 }
                r9 = 69633(0x11001, float:9.7577E-41)
                r10 = 10000(0x2710, double:4.9407E-320)
                r8.sendEmptyMessageDelayed(r9, r10)     // Catch:{ Exception -> 0x0011 }
                goto L_0x0007
            L_0x008a:
                java.lang.Object r8 = r14.obj     // Catch:{ Exception -> 0x0011 }
                r0 = r8
                com.xcharge.charger.protocol.monitor.bean.YZXDCAPMessage r0 = (com.xcharge.charger.protocol.monitor.bean.YZXDCAPMessage) r0     // Catch:{ Exception -> 0x0011 }
                r7 = r0
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                boolean r8 = r8.isConnected()     // Catch:{ Exception -> 0x0011 }
                if (r8 == 0) goto L_0x00c9
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                java.lang.String r9 = r7.getOp()     // Catch:{ Exception -> 0x0011 }
                boolean r8 = r8.isRequestMessage(r9)     // Catch:{ Exception -> 0x0011 }
                if (r8 == 0) goto L_0x0007
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent$SendRequestState r3 = new com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent$SendRequestState     // Catch:{ Exception -> 0x0011 }
                r8 = 0
                r3.<init>(r8)     // Catch:{ Exception -> 0x0011 }
                r3.request = r7     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.type.MONITOR_REQUEST_STATE r8 = com.xcharge.charger.protocol.monitor.type.MONITOR_REQUEST_STATE.sending     // Catch:{ Exception -> 0x0011 }
                r3.status = r8     // Catch:{ Exception -> 0x0011 }
                long r8 = java.lang.System.currentTimeMillis()     // Catch:{ Exception -> 0x0011 }
                r3.timestamp = r8     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                java.util.HashMap r8 = r8.sendReqestState     // Catch:{ Exception -> 0x0011 }
                java.lang.Long r9 = r7.getSeq()     // Catch:{ Exception -> 0x0011 }
                java.lang.String r9 = java.lang.String.valueOf(r9)     // Catch:{ Exception -> 0x0011 }
                r8.put(r9, r3)     // Catch:{ Exception -> 0x0011 }
                goto L_0x0007
            L_0x00c9:
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                java.lang.String r9 = r7.getOp()     // Catch:{ Exception -> 0x0011 }
                boolean r8 = r8.isRequestMessage(r9)     // Catch:{ Exception -> 0x0011 }
                if (r8 == 0) goto L_0x0007
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                r8.handleSendRequestFail(r7)     // Catch:{ Exception -> 0x0011 }
                goto L_0x0007
            L_0x00dc:
                java.lang.Object r8 = r14.obj     // Catch:{ Exception -> 0x0011 }
                r0 = r8
                com.xcharge.charger.protocol.monitor.bean.YZXDCAPMessage r0 = (com.xcharge.charger.protocol.monitor.bean.YZXDCAPMessage) r0     // Catch:{ Exception -> 0x0011 }
                r7 = r0
                java.lang.String r8 = "MonitorProtocolAgent.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0011 }
                java.lang.String r10 = "received yzx msg: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x0011 }
                java.lang.String r10 = r7.toJson()     // Catch:{ Exception -> 0x0011 }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x0011 }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.util.LogUtils.log(r8, r9)     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                java.lang.String r9 = r7.getOp()     // Catch:{ Exception -> 0x0011 }
                boolean r8 = r8.isRequestMessage(r9)     // Catch:{ Exception -> 0x0011 }
                if (r8 != 0) goto L_0x016c
                java.lang.String r8 = "ack"
                com.xcharge.charger.protocol.monitor.bean.YZXDCAPOption r9 = r7.getOpt()     // Catch:{ Exception -> 0x0011 }
                java.lang.String r9 = r9.getOp()     // Catch:{ Exception -> 0x0011 }
                boolean r8 = r8.equals(r9)     // Catch:{ Exception -> 0x0011 }
                if (r8 == 0) goto L_0x0145
                java.lang.Long r8 = r7.getSeq()     // Catch:{ Exception -> 0x0011 }
                long r8 = r8.longValue()     // Catch:{ Exception -> 0x0011 }
                r10 = 2
                long r8 = r8 - r10
                java.lang.String r5 = java.lang.String.valueOf(r8)     // Catch:{ Exception -> 0x0011 }
            L_0x0125:
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                java.util.HashMap r8 = r8.sendReqestState     // Catch:{ Exception -> 0x0011 }
                java.lang.Object r3 = r8.get(r5)     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent$SendRequestState r3 = (com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.SendRequestState) r3     // Catch:{ Exception -> 0x0011 }
                if (r3 == 0) goto L_0x0152
                com.xcharge.charger.protocol.monitor.bean.YZXDCAPMessage r4 = r3.request     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                java.util.HashMap r8 = r8.sendReqestState     // Catch:{ Exception -> 0x0011 }
                r8.remove(r5)     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                r8.dispatchYZXMessage(r7, r4)     // Catch:{ Exception -> 0x0011 }
                goto L_0x0007
            L_0x0145:
                com.xcharge.charger.protocol.monitor.bean.YZXDCAPOption r8 = r7.getOpt()     // Catch:{ Exception -> 0x0011 }
                java.lang.Long r8 = r8.getSeq()     // Catch:{ Exception -> 0x0011 }
                java.lang.String r5 = java.lang.String.valueOf(r8)     // Catch:{ Exception -> 0x0011 }
                goto L_0x0125
            L_0x0152:
                java.lang.String r8 = "MonitorProtocolAgent.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0011 }
                java.lang.String r10 = "maybe timeout to wait for response msg: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x0011 }
                java.lang.String r10 = r7.toJson()     // Catch:{ Exception -> 0x0011 }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x0011 }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.util.LogUtils.log(r8, r9)     // Catch:{ Exception -> 0x0011 }
                goto L_0x0007
            L_0x016c:
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                r9 = 0
                r8.dispatchYZXMessage(r7, r9)     // Catch:{ Exception -> 0x0011 }
                goto L_0x0007
            L_0x0174:
                java.lang.Object r8 = r14.obj     // Catch:{ Exception -> 0x0011 }
                r0 = r8
                com.xcharge.charger.protocol.monitor.bean.YZXDCAPMessage r0 = (com.xcharge.charger.protocol.monitor.bean.YZXDCAPMessage) r0     // Catch:{ Exception -> 0x0011 }
                r7 = r0
                java.lang.String r8 = "MonitorProtocolAgent.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0011 }
                java.lang.String r10 = "failed to send yzx msg: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x0011 }
                java.lang.String r10 = r7.toJson()     // Catch:{ Exception -> 0x0011 }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x0011 }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.util.LogUtils.log(r8, r9)     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                r8.handleSendRequestFail(r7)     // Catch:{ Exception -> 0x0011 }
                goto L_0x0007
            L_0x0199:
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                r8.requestTimeoutCheck()     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                com.xcharge.common.utils.HandlerTimer r8 = r8.handlerTimer     // Catch:{ Exception -> 0x0011 }
                r10 = 1000(0x3e8, double:4.94E-321)
                r9 = 69641(0x11009, float:9.7588E-41)
                r12 = 0
                r8.startTimer(r10, r9, r12)     // Catch:{ Exception -> 0x0011 }
                goto L_0x0007
            L_0x01af:
                com.xcharge.charger.data.provider.HardwareStatusCacheProvider r8 = com.xcharge.charger.data.provider.HardwareStatusCacheProvider.getInstance()     // Catch:{ Exception -> 0x0011 }
                boolean r8 = r8.isNetworkConnected()     // Catch:{ Exception -> 0x0011 }
                if (r8 == 0) goto L_0x01df
                java.lang.String r8 = "MonitorProtocolAgent.handleMessage"
                java.lang.String r9 = "sync time from cloud !!!"
                com.xcharge.charger.protocol.monitor.util.LogUtils.log(r8, r9)     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                boolean r8 = r8.syncTime()     // Catch:{ Exception -> 0x0011 }
                if (r8 == 0) goto L_0x01cf
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                r8.initConnection()     // Catch:{ Exception -> 0x0011 }
                goto L_0x0007
            L_0x01cf:
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent$MsgHandler r8 = r8.handler     // Catch:{ Exception -> 0x0011 }
                r9 = 69648(0x11010, float:9.7598E-41)
                r10 = 5000(0x1388, double:2.4703E-320)
                r8.sendEmptyMessageDelayed(r9, r10)     // Catch:{ Exception -> 0x0011 }
                goto L_0x0007
            L_0x01df:
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent$MsgHandler r8 = r8.handler     // Catch:{ Exception -> 0x0011 }
                r9 = 69648(0x11010, float:9.7598E-41)
                r10 = 5000(0x1388, double:2.4703E-320)
                r8.sendEmptyMessageDelayed(r9, r10)     // Catch:{ Exception -> 0x0011 }
                goto L_0x0007
            L_0x01ef:
                java.lang.Object r8 = r14.obj     // Catch:{ Exception -> 0x0011 }
                r0 = r8
                android.net.Uri r0 = (android.net.Uri) r0     // Catch:{ Exception -> 0x0011 }
                r6 = r0
                com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent r8 = com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.this     // Catch:{ Exception -> 0x0011 }
                r8.handleNetworkStatusChanged(r6)     // Catch:{ Exception -> 0x0011 }
                goto L_0x0007
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent.MsgHandler.handleMessage(android.os.Message):void");
        }
    }

    public void init(Context context2) {
        this.context = context2;
        this.portHandlers = new HashMap<>();
        HashMap<String, Port> ports = HardwareStatusCacheProvider.getInstance().getPorts();
        if (ports != null) {
            for (String port : ports.keySet()) {
                MonitorPortHandler portHandler = new MonitorPortHandler();
                portHandler.init(context2, port, this);
                this.portHandlers.put(port, portHandler);
            }
        }
        this.connectThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue(1), new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                LogUtils.log("MonitorProtocolAgent.ThreadPoolExecutor.rejectedExecution", "rejected runnable: " + r.toString() + ", active runnables: " + executor.getActiveCount());
            }
        });
        this.sendReqestState = new HashMap<>();
        this.thread = new HandlerThread("MonitorProtocolAgent", 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(context2);
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

    /* access modifiers changed from: private */
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

    /* access modifiers changed from: private */
    public boolean syncTime() {
        try {
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 10000);
            DefaultHttpClient httpClient = new DefaultHttpClient(params);
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
                DDAPMessage ddapMessage = (DDAPMessage) new DDAPMessage().fromJson(result);
                if (ddapMessage.getCode() == FieldConfigUtils.getCode(ErrorCodeMapping.E_OK)) {
                    HelloResponse helloResponse = (HelloResponse) new HelloResponse().fromJson(JsonBean.ObjectToJson(ddapMessage.getData().getData()));
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

    /* access modifiers changed from: private */
    public boolean isConnected() {
        return this.isConnected;
    }

    /* access modifiers changed from: private */
    public void initConnection() {
        this.handler.sendEmptyMessage(69633);
    }

    private class ConnectTask implements Runnable {
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

        public void run() {
            try {
                MemoryPersistence persistence = new MemoryPersistence();
                SSLSocketFactory socketFactory = MonitorProtocolAgent.this.createSSLSocket();
                MonitorProtocolAgent.this.sampleClient = new MqttClient(MonitorProtocolAgent.this.center_ip_port, this.clientId, persistence);
                final MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setMqttVersion(4);
                connOpts.setSocketFactory(socketFactory);
                connOpts.setCleanSession(true);
                connOpts.setUserName(Md5.getInstance().md5_32(String.valueOf(MonitorProtocolAgent.this.productKey) + MonitorProtocolAgent.this.productSecret + MonitorProtocolAgent.this.deviceName + MonitorProtocolAgent.this.deviceSecret).toUpperCase());
                connOpts.setConnectionTimeout(10);
                connOpts.setKeepAliveInterval(60);
                MonitorProtocolAgent.this.sampleClient.connect(connOpts);
                MonitorProtocolAgent.this.sampleClient.setCallback(new MqttCallback() {
                    public void connectionLost(Throwable cause) {
                        LogUtils.log("MonitorProtocolAgent.connectionLost", "连接失败,原因:" + cause);
                        while (MonitorProtocolAgent.this.sampleClient != null && !MonitorProtocolAgent.this.sampleClient.isConnected()) {
                            try {
                                Thread.sleep(1000);
                                MonitorProtocolAgent.this.sampleClient.connect(connOpts);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        String recvMsg = new String(message.getPayload(), CharEncoding.UTF_8);
                        MonitorProtocolAgent.this.handler.sendMessage(MonitorProtocolAgent.this.handler.obtainMessage(69640, (YZXDCAPMessage) new YZXDCAPMessage().fromJson(recvMsg)));
                    }

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

    /* access modifiers changed from: private */
    public SSLSocketFactory createSSLSocket() throws Exception {
        InputStream in = this.context.getResources().openRawResource(C0273R.raw.pubkey);
        Certificate ca = null;
        try {
            ca = CertificateFactory.getInstance("X.509").generateCertificate(in);
        } catch (CertificateException e) {
            e.printStackTrace();
        } finally {
            in.close();
        }
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load((InputStream) null, (char[]) null);
        keyStore.setCertificateEntry("ca", ca);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);
        SSLContext context2 = SSLContext.getInstance("TLSV1.2");
        context2.init((KeyManager[]) null, tmf.getTrustManagers(), (SecureRandom) null);
        return context2.getSocketFactory();
    }

    /* access modifiers changed from: private */
    public void connect() {
        if (!HardwareStatusCacheProvider.getInstance().isNetworkConnected()) {
            this.handler.sendEmptyMessageDelayed(69633, 5000);
        } else if (this.sampleClient == null || !this.sampleClient.isConnected()) {
            LogUtils.log("MonitorProtocolAgent.connect", "init connection !!!");
            this.connectThreadPoolExecutor.execute(new ConnectTask(this, (ConnectTask) null));
        }
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
        if (monitorPortHandler != null) {
            return monitorPortHandler.getChargeSession();
        }
        LogUtils.log("MonitorProtocolAgent.getChargeSession", "no available port handler for port: " + port);
        return null;
    }

    /* access modifiers changed from: private */
    public void dispatchYZXMessage(YZXDCAPMessage msg, YZXDCAPMessage sendedRequest) {
        if (sendedRequest == null) {
            handleRequestMessage(msg);
        } else {
            handleResponseMessage(msg, sendedRequest);
        }
    }

    /* access modifiers changed from: private */
    public void sendNetworkRequest() {
        try {
            YZXDCAPMessage request = createDAPRequest("report", YZXDCAPOption.NETWORK);
            Network network = HardwareStatusCacheProvider.getInstance().getNetworkStatus();
            if (network.isConnected()) {
                YZXPropset yzxPropset = new YZXPropset();
                ArrayList arrayList = new ArrayList();
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
                        arrayList.add(yzxProperty);
                        yzxPropset.setPropset(arrayList);
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
                        arrayList.add(yzxProperty);
                        yzxPropset.setPropset(arrayList);
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

    /* access modifiers changed from: private */
    public void sendCapabilityRequest() {
        try {
            YZXDCAPMessage request = createDAPRequest("report", YZXDCAPOption.CAPABILITY);
            YZXPropset yzxPropset = new YZXPropset();
            List<YZXProperty> propset = new ArrayList<>();
            YZXProperty yzxProperty = new YZXProperty();
            yzxProperty.setId(YZXDCAPOption.CAPABILITY);
            DAPCapability dapCapability = new DAPCapability();
            int ampCapacity = ChargeStatusCacheProvider.getInstance().getAmpCapacity();
            dapCapability.setAmp_capacity((double) ampCapacity);
            Port port = DCAPProxy.getInstance().getPortStatus("1");
            if (!(port == null || port.getVolts() == null)) {
                dapCapability.setKwatt_capacity((int) (((double) ampCapacity) * port.getVolts().get(0).doubleValue()));
            }
            switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$PHASE()[HardwareStatusCacheProvider.getInstance().getHardwareStatus().getPhase().ordinal()]) {
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
            if (width <= 0 || height <= 0) {
                dapCapability.setScreen("none");
            } else {
                dapCapability.setScreen(String.valueOf(width) + "." + height);
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

    /* access modifiers changed from: private */
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

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x009a, code lost:
        r1 = r1 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x00a3, code lost:
        if (r8.equals(com.xcharge.charger.protocol.monitor.bean.YZXDCAPOption.GUN_LOCK_STATUS) != false) goto L_0x00a5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00a7, code lost:
        if (r2.length != 3) goto L_0x01b5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00a9, code lost:
        r3 = getPortHandler(r2[2]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00b0, code lost:
        if (r3 == null) goto L_0x0196;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00bb, code lost:
        if (com.xcharge.charger.protocol.monitor.bean.YZXDCAPOption.GUN_LOCK_STATUS.equals(r2[0]) == false) goto L_0x015a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00bd, code lost:
        r5.add(r3.handleQueryGunLockStatusRequest(r12));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0163, code lost:
        if (com.xcharge.charger.protocol.monitor.bean.YZXDCAPOption.PORT_ENABLE.equals(r2[0]) == false) goto L_0x016e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0165, code lost:
        r5.add(r3.handleQueryPortEnableRequest(r12));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0177, code lost:
        if (com.xcharge.charger.protocol.monitor.bean.YZXDCAPOption.RADAR_STATUS.equals(r2[0]) == false) goto L_0x0182;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0179, code lost:
        r5.add(r3.handleQueryRadarStatusRequest(r12));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x018b, code lost:
        if ("charge".equals(r2[0]) == false) goto L_0x009a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x018d, code lost:
        r5.add(r3.handleQueryChargeInfoRequest(r12));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0196, code lost:
        com.xcharge.charger.protocol.monitor.util.LogUtils.log("MonitorProtocolAgent.handleRequestMessage", "unsupported port in ack: " + r12.toJson());
        createErrorResponse(r12, com.xcharge.charger.protocol.monitor.bean.ErrorCodeMapping.E_CHARGE_NOT_EXIST);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x01b5, code lost:
        createErrorResponse(r12, com.xcharge.charger.protocol.monitor.bean.ErrorCodeMapping.E_CHARGE_NOT_EXIST);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleRequestMessage(com.xcharge.charger.protocol.monitor.bean.YZXDCAPMessage r12) {
        /*
            r11 = this;
            r3 = 0
            java.lang.String r8 = r12.getOp()     // Catch:{ Exception -> 0x0071 }
            int r9 = r8.hashCode()     // Catch:{ Exception -> 0x0071 }
            switch(r9) {
                case 107944136: goto L_0x0012;
                default: goto L_0x000c;
            }     // Catch:{ Exception -> 0x0071 }
        L_0x000c:
            com.xcharge.charger.protocol.monitor.bean.ErrorCodeMapping r8 = com.xcharge.charger.protocol.monitor.bean.ErrorCodeMapping.E_PROP_ID_NOT_EXIST     // Catch:{ Exception -> 0x0071 }
            r11.createErrorResponse(r12, r8)     // Catch:{ Exception -> 0x0071 }
        L_0x0011:
            return
        L_0x0012:
            java.lang.String r9 = "query"
            boolean r8 = r8.equals(r9)     // Catch:{ Exception -> 0x0071 }
            if (r8 == 0) goto L_0x000c
            java.lang.String r8 = "MonitorProtocolAgent.handleRequestMessage"
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0071 }
            java.lang.String r10 = "receive QueryRequest"
            r9.<init>(r10)     // Catch:{ Exception -> 0x0071 }
            java.lang.String r10 = r12.toJson()     // Catch:{ Exception -> 0x0071 }
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x0071 }
            java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x0071 }
            com.xcharge.charger.protocol.monitor.util.LogUtils.log(r8, r9)     // Catch:{ Exception -> 0x0071 }
            com.xcharge.charger.protocol.monitor.bean.YZXPropset r7 = new com.xcharge.charger.protocol.monitor.bean.YZXPropset     // Catch:{ Exception -> 0x0071 }
            r7.<init>()     // Catch:{ Exception -> 0x0071 }
            java.util.ArrayList r5 = new java.util.ArrayList     // Catch:{ Exception -> 0x0071 }
            r5.<init>()     // Catch:{ Exception -> 0x0071 }
            com.xcharge.charger.protocol.monitor.bean.YZXDCAPOption r8 = r12.getOpt()     // Catch:{ Exception -> 0x0071 }
            java.util.List r4 = r8.getProp_id()     // Catch:{ Exception -> 0x0071 }
            r1 = 0
        L_0x0045:
            int r8 = r4.size()     // Catch:{ Exception -> 0x0071 }
            if (r1 < r8) goto L_0x007c
            com.xcharge.charger.protocol.monitor.bean.YZXDCAPMessage r6 = r11.createResponse(r12)     // Catch:{ Exception -> 0x0071 }
            r7.setPropset(r5)     // Catch:{ Exception -> 0x0071 }
            r6.setData(r7)     // Catch:{ Exception -> 0x0071 }
            r11.sendMessage(r6)     // Catch:{ Exception -> 0x0071 }
            java.lang.String r8 = "MonitorProtocolAgent.handleQueryFeePolicyRequest"
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0071 }
            java.lang.String r10 = "sendQueryResponse"
            r9.<init>(r10)     // Catch:{ Exception -> 0x0071 }
            java.lang.String r10 = r6.toJson()     // Catch:{ Exception -> 0x0071 }
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x0071 }
            java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x0071 }
            com.xcharge.charger.protocol.monitor.util.LogUtils.log(r8, r9)     // Catch:{ Exception -> 0x0071 }
            goto L_0x0011
        L_0x0071:
            r0 = move-exception
            java.lang.String r8 = "MonitorProtocolAgent.handleRequestMessage"
            java.lang.String r9 = android.util.Log.getStackTraceString(r0)
            com.xcharge.charger.protocol.monitor.util.LogUtils.log(r8, r9)
            goto L_0x0011
        L_0x007c:
            r12.setFlag(r1)     // Catch:{ Exception -> 0x0071 }
            java.lang.Object r8 = r4.get(r1)     // Catch:{ Exception -> 0x0071 }
            java.lang.String r8 = (java.lang.String) r8     // Catch:{ Exception -> 0x0071 }
            java.lang.String r9 = "/"
            java.lang.String[] r2 = r8.split(r9)     // Catch:{ Exception -> 0x0071 }
            r8 = 0
            r8 = r2[r8]     // Catch:{ Exception -> 0x0071 }
            int r9 = r8.hashCode()     // Catch:{ Exception -> 0x0071 }
            switch(r9) {
                case -1560018681: goto L_0x009d;
                case -1361632588: goto L_0x00c5;
                case -882091299: goto L_0x00ce;
                case -879502773: goto L_0x00d7;
                case -176518056: goto L_0x00e7;
                case -111222037: goto L_0x00f7;
                case 96784904: goto L_0x0107;
                case 551905099: goto L_0x0113;
                case 922170379: goto L_0x011d;
                case 1466476927: goto L_0x012e;
                case 1538503188: goto L_0x013f;
                case 2087528193: goto L_0x0150;
                default: goto L_0x0095;
            }     // Catch:{ Exception -> 0x0071 }
        L_0x0095:
            com.xcharge.charger.protocol.monitor.bean.ErrorCodeMapping r8 = com.xcharge.charger.protocol.monitor.bean.ErrorCodeMapping.E_PROP_ID_NOT_EXIST     // Catch:{ Exception -> 0x0071 }
            r11.createErrorResponse(r12, r8)     // Catch:{ Exception -> 0x0071 }
        L_0x009a:
            int r1 = r1 + 1
            goto L_0x0045
        L_0x009d:
            java.lang.String r9 = "gun_lock_status"
            boolean r8 = r8.equals(r9)     // Catch:{ Exception -> 0x0071 }
            if (r8 == 0) goto L_0x0095
        L_0x00a5:
            int r8 = r2.length     // Catch:{ Exception -> 0x0071 }
            r9 = 3
            if (r8 != r9) goto L_0x01b5
            r8 = 2
            r8 = r2[r8]     // Catch:{ Exception -> 0x0071 }
            com.xcharge.charger.protocol.monitor.handler.MonitorPortHandler r3 = r11.getPortHandler(r8)     // Catch:{ Exception -> 0x0071 }
            if (r3 == 0) goto L_0x0196
            java.lang.String r8 = "gun_lock_status"
            r9 = 0
            r9 = r2[r9]     // Catch:{ Exception -> 0x0071 }
            boolean r8 = r8.equals(r9)     // Catch:{ Exception -> 0x0071 }
            if (r8 == 0) goto L_0x015a
            com.xcharge.charger.protocol.monitor.bean.YZXProperty r8 = r3.handleQueryGunLockStatusRequest(r12)     // Catch:{ Exception -> 0x0071 }
            r5.add(r8)     // Catch:{ Exception -> 0x0071 }
            goto L_0x009a
        L_0x00c5:
            java.lang.String r9 = "charge"
            boolean r8 = r8.equals(r9)     // Catch:{ Exception -> 0x0071 }
            if (r8 != 0) goto L_0x00a5
            goto L_0x0095
        L_0x00ce:
            java.lang.String r9 = "ammeter"
            boolean r8 = r8.equals(r9)     // Catch:{ Exception -> 0x0071 }
            if (r8 != 0) goto L_0x009a
            goto L_0x0095
        L_0x00d7:
            java.lang.String r9 = "amp_pwm"
            boolean r8 = r8.equals(r9)     // Catch:{ Exception -> 0x0071 }
            if (r8 == 0) goto L_0x0095
            com.xcharge.charger.protocol.monitor.bean.YZXProperty r8 = r11.handleQueryAmpPwmRequest(r12)     // Catch:{ Exception -> 0x0071 }
            r5.add(r8)     // Catch:{ Exception -> 0x0071 }
            goto L_0x009a
        L_0x00e7:
            java.lang.String r9 = "timing_param"
            boolean r8 = r8.equals(r9)     // Catch:{ Exception -> 0x0071 }
            if (r8 == 0) goto L_0x0095
            com.xcharge.charger.protocol.monitor.bean.YZXProperty r8 = r11.handleQueryTimingParamRequest(r12)     // Catch:{ Exception -> 0x0071 }
            r5.add(r8)     // Catch:{ Exception -> 0x0071 }
            goto L_0x009a
        L_0x00f7:
            java.lang.String r9 = "cp_range"
            boolean r8 = r8.equals(r9)     // Catch:{ Exception -> 0x0071 }
            if (r8 == 0) goto L_0x0095
            com.xcharge.charger.protocol.monitor.bean.YZXProperty r8 = r11.handleQueryCPRangeRequest(r12)     // Catch:{ Exception -> 0x0071 }
            r5.add(r8)     // Catch:{ Exception -> 0x0071 }
            goto L_0x009a
        L_0x0107:
            java.lang.String r9 = "error"
            boolean r8 = r8.equals(r9)     // Catch:{ Exception -> 0x0071 }
            if (r8 == 0) goto L_0x0095
            r11.handleQueryErrorRequest(r12)     // Catch:{ Exception -> 0x0071 }
            goto L_0x009a
        L_0x0113:
            java.lang.String r9 = "radar_status"
            boolean r8 = r8.equals(r9)     // Catch:{ Exception -> 0x0071 }
            if (r8 != 0) goto L_0x00a5
            goto L_0x0095
        L_0x011d:
            java.lang.String r9 = "fee_policy"
            boolean r8 = r8.equals(r9)     // Catch:{ Exception -> 0x0071 }
            if (r8 == 0) goto L_0x0095
            com.xcharge.charger.protocol.monitor.bean.YZXProperty r8 = r11.handleQueryFeePolicyRequest(r12)     // Catch:{ Exception -> 0x0071 }
            r5.add(r8)     // Catch:{ Exception -> 0x0071 }
            goto L_0x009a
        L_0x012e:
            java.lang.String r9 = "volt_range"
            boolean r8 = r8.equals(r9)     // Catch:{ Exception -> 0x0071 }
            if (r8 == 0) goto L_0x0095
            com.xcharge.charger.protocol.monitor.bean.YZXProperty r8 = r11.handleQueryVoltRangeRequest(r12)     // Catch:{ Exception -> 0x0071 }
            r5.add(r8)     // Catch:{ Exception -> 0x0071 }
            goto L_0x009a
        L_0x013f:
            java.lang.String r9 = "radar_param"
            boolean r8 = r8.equals(r9)     // Catch:{ Exception -> 0x0071 }
            if (r8 == 0) goto L_0x0095
            com.xcharge.charger.protocol.monitor.bean.YZXProperty r8 = r11.handleQueryRadarParamRequest(r12)     // Catch:{ Exception -> 0x0071 }
            r5.add(r8)     // Catch:{ Exception -> 0x0071 }
            goto L_0x009a
        L_0x0150:
            java.lang.String r9 = "port_enable"
            boolean r8 = r8.equals(r9)     // Catch:{ Exception -> 0x0071 }
            if (r8 != 0) goto L_0x00a5
            goto L_0x0095
        L_0x015a:
            java.lang.String r8 = "port_enable"
            r9 = 0
            r9 = r2[r9]     // Catch:{ Exception -> 0x0071 }
            boolean r8 = r8.equals(r9)     // Catch:{ Exception -> 0x0071 }
            if (r8 == 0) goto L_0x016e
            com.xcharge.charger.protocol.monitor.bean.YZXProperty r8 = r3.handleQueryPortEnableRequest(r12)     // Catch:{ Exception -> 0x0071 }
            r5.add(r8)     // Catch:{ Exception -> 0x0071 }
            goto L_0x009a
        L_0x016e:
            java.lang.String r8 = "radar_status"
            r9 = 0
            r9 = r2[r9]     // Catch:{ Exception -> 0x0071 }
            boolean r8 = r8.equals(r9)     // Catch:{ Exception -> 0x0071 }
            if (r8 == 0) goto L_0x0182
            com.xcharge.charger.protocol.monitor.bean.YZXProperty r8 = r3.handleQueryRadarStatusRequest(r12)     // Catch:{ Exception -> 0x0071 }
            r5.add(r8)     // Catch:{ Exception -> 0x0071 }
            goto L_0x009a
        L_0x0182:
            java.lang.String r8 = "charge"
            r9 = 0
            r9 = r2[r9]     // Catch:{ Exception -> 0x0071 }
            boolean r8 = r8.equals(r9)     // Catch:{ Exception -> 0x0071 }
            if (r8 == 0) goto L_0x009a
            com.xcharge.charger.protocol.monitor.bean.YZXProperty r8 = r3.handleQueryChargeInfoRequest(r12)     // Catch:{ Exception -> 0x0071 }
            r5.add(r8)     // Catch:{ Exception -> 0x0071 }
            goto L_0x009a
        L_0x0196:
            java.lang.String r8 = "MonitorProtocolAgent.handleRequestMessage"
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0071 }
            java.lang.String r10 = "unsupported port in ack: "
            r9.<init>(r10)     // Catch:{ Exception -> 0x0071 }
            java.lang.String r10 = r12.toJson()     // Catch:{ Exception -> 0x0071 }
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x0071 }
            java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x0071 }
            com.xcharge.charger.protocol.monitor.util.LogUtils.log(r8, r9)     // Catch:{ Exception -> 0x0071 }
            com.xcharge.charger.protocol.monitor.bean.ErrorCodeMapping r8 = com.xcharge.charger.protocol.monitor.bean.ErrorCodeMapping.E_CHARGE_NOT_EXIST     // Catch:{ Exception -> 0x0071 }
            r11.createErrorResponse(r12, r8)     // Catch:{ Exception -> 0x0071 }
            goto L_0x009a
        L_0x01b5:
            com.xcharge.charger.protocol.monitor.bean.ErrorCodeMapping r8 = com.xcharge.charger.protocol.monitor.bean.ErrorCodeMapping.E_CHARGE_NOT_EXIST     // Catch:{ Exception -> 0x0071 }
            r11.createErrorResponse(r12, r8)     // Catch:{ Exception -> 0x0071 }
            goto L_0x009a
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
                        case ErrorCode.EC_DEVICE_NOT_INIT:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_NOT_INIT);
                            break;
                        case ErrorCode.EC_DEVICE_NO_GROUND:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_NO_GROUND);
                            break;
                        case ErrorCode.EC_DEVICE_LOST_PHASE:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_PHASE_LOST);
                            break;
                        case ErrorCode.EC_DEVICE_EMERGENCY_STOP:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_EMERGENCY_STOP);
                            break;
                        case ErrorCode.EC_DEVICE_VOLT_ERROR:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_VOLT);
                            break;
                        case ErrorCode.EC_DEVICE_AMP_ERROR:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_AMP);
                            break;
                        case ErrorCode.EC_DEVICE_TEMP_ERROR:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_TEMPRATURE);
                            break;
                        case ErrorCode.EC_DEVICE_POWER_LEAK:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_LEAK_AMP);
                            break;
                        case ErrorCode.EC_DEVICE_COMM_ERROR:
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
            yzxProperty.setValue(HardwareStatusCacheProvider.getInstance().getPort("1").getMeter());
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
        response.getOpt().getOp().hashCode();
    }

    public void handleDelayWaitStartedRequest(String chargeId) {
        String port = getPort(chargeId);
        if (!TextUtils.isEmpty(port)) {
            MonitorPortHandler yzxPortHandler = getPortHandler(port);
            if (yzxPortHandler == null) {
                LogUtils.log("MonitorProtocolAgent.handleDelayWaitStartedRequest", "no available port handler for port: " + port);
            } else {
                yzxPortHandler.sendDelayWaitStartedRequest(chargeId);
            }
        } else {
            LogUtils.log("MonitorProtocolAgent.handleDelayWaitStartedRequest", "failed to find related port for charge: " + chargeId);
        }
    }

    public void handleDelayStartedRequest(String chargeId, long delayStart) {
        String port = getPort(chargeId);
        if (!TextUtils.isEmpty(port)) {
            MonitorPortHandler yzxPortHandler = getPortHandler(port);
            if (yzxPortHandler == null) {
                LogUtils.log("MonitorProtocolAgent.handleDelayStartedRequest", "no available port handler for port: " + port);
            } else {
                yzxPortHandler.sendDelayStartedRequest(chargeId, delayStart);
            }
        } else {
            LogUtils.log("MonitorProtocolAgent.handleDelayStartedRequest", "failed to find related port for charge: " + chargeId);
        }
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

    /* access modifiers changed from: private */
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

    /* access modifiers changed from: private */
    public void requestTimeoutCheck() {
        if (this.sendReqestState.size() > 0) {
            Iterator<Map.Entry<String, SendRequestState>> it = this.sendReqestState.entrySet().iterator();
            while (it.hasNext()) {
                SendRequestState requestState = it.next().getValue();
                MONITOR_REQUEST_STATE state = requestState.status;
                long timestamp = requestState.timestamp;
                if (MONITOR_REQUEST_STATE.sending.equals(state)) {
                    if (System.currentTimeMillis() - timestamp > 5000) {
                        it.remove();
                        handleSendRequestFail(requestState.request);
                    }
                } else if (MONITOR_REQUEST_STATE.sended.equals(state) && System.currentTimeMillis() - timestamp > 10000) {
                    it.remove();
                    handleRequestTimeout(requestState.request);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void portsActive() {
        for (MonitorPortHandler portHandler : this.portHandlers.values()) {
            portHandler.sendMessage(portHandler.obtainMessage(73729));
        }
    }

    /* access modifiers changed from: private */
    public void portsDeactive() {
        for (MonitorPortHandler portHandler : this.portHandlers.values()) {
            portHandler.sendMessage(portHandler.obtainMessage(73730));
        }
    }

    /* access modifiers changed from: private */
    public boolean isRequestMessage(String op) {
        if ("ack".equals(op)) {
            return false;
        }
        return true;
    }
}
