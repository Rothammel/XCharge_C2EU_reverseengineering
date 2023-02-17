package com.xcharge.charger.protocol.family.xcloud.handler;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.api.bean.cap.SetDirective;
import com.xcharge.charger.core.type.FIN_MODE;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.FeeRate;
import com.xcharge.charger.data.bean.PortFeeRate;
import com.xcharge.charger.data.bean.XKeyseed;
import com.xcharge.charger.data.bean.device.Ethernet;
import com.xcharge.charger.data.bean.device.MobileNet;
import com.xcharge.charger.data.bean.device.Network;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.device.Wifi;
import com.xcharge.charger.data.bean.setting.CountrySetting;
import com.xcharge.charger.data.bean.setting.PortSetting;
import com.xcharge.charger.data.bean.setting.RadarSetting;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.bean.type.PHASE;
import com.xcharge.charger.data.bean.type.PLATFORM_CUSTOMER;
import com.xcharge.charger.data.bean.type.SERVICE_REGION;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.LocalSettingCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SoftwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.CloudTimeSynchObserver;
import com.xcharge.charger.data.proxy.NFCKeyContentProxy;
import com.xcharge.charger.data.proxy.NetworkStatusObserver;
import com.xcharge.charger.device.p005c2.bean.PortRuntimeData;
import com.xcharge.charger.device.p005c2.service.C2DeviceProxy;
import com.xcharge.charger.protocol.family.xcloud.C0252R;
import com.xcharge.charger.protocol.family.xcloud.bean.DeviceCapability;
import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.charger.protocol.family.xcloud.bean.DeviceSetting;
import com.xcharge.charger.protocol.family.xcloud.bean.FeePolicy;
import com.xcharge.charger.protocol.family.xcloud.bean.FinCause;
import com.xcharge.charger.protocol.family.xcloud.bean.LocaleOption;
import com.xcharge.charger.protocol.family.xcloud.bean.MqttOptions;
import com.xcharge.charger.protocol.family.xcloud.bean.NFCGroupSeed;
import com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage;
import com.xcharge.charger.protocol.family.xcloud.bean.XCloudPort;
import com.xcharge.charger.protocol.family.xcloud.bean.XCloudRadarSetting;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.AnswerHello;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.ApplySetting;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.CancelAutoStop;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.ConfirmChargeEnded;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.ConfirmChargeStarted;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.ConfirmLocalChargeBill;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.QueryLog;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.QueryState;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.QuerySystemInfo;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestAction;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestAutoStop;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestEndCharge;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestRefuseCharge;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestStartCharge;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestStopCharge;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestUpdateStartTime;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestUpgrade;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestVerification;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.SendChargeQRCode;
import com.xcharge.charger.protocol.family.xcloud.bean.device.ReportActionResult;
import com.xcharge.charger.protocol.family.xcloud.bean.device.ReportSettingResult;
import com.xcharge.charger.protocol.family.xcloud.bean.device.ReportState;
import com.xcharge.charger.protocol.family.xcloud.bean.device.ReportSystemInfo;
import com.xcharge.charger.protocol.family.xcloud.bean.device.ReportVerification;
import com.xcharge.charger.protocol.family.xcloud.bean.device.RequestSetting;
import com.xcharge.charger.protocol.family.xcloud.bean.device.SayHello;
import com.xcharge.charger.protocol.family.xcloud.bean.device.UploadLog;
import com.xcharge.charger.protocol.family.xcloud.router.XCloudDCAPGateway;
import com.xcharge.charger.protocol.family.xcloud.session.XCloudChargeSession;
import com.xcharge.charger.protocol.family.xcloud.session.XCloudRequestSession;
import com.xcharge.charger.protocol.family.xcloud.type.EnumActionStatus;
import com.xcharge.charger.protocol.family.xcloud.type.XCLOUD_REQUEST_STATE;
import com.xcharge.charger.protocol.family.xcloud.util.SettingUtils;
import com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.ContextUtils;
import com.xcharge.common.utils.FileUtils;
import com.xcharge.common.utils.FtpUtils;
import com.xcharge.common.utils.HandlerTimer;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.MD5Utils;
import com.xcharge.common.utils.TimeUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.xcharger.sdk.device.MessageHandler;
import net.xcharger.sdk.device.MessageProxy;
import net.xcharger.sdk.device.MessageProxyFactory;
import net.xcharger.sdk.device.MessageProxyOptions;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

public class XCloudProtocolAgent implements MessageHandler {
    public static final int MSG_CLOUD_TIMESYNCH_TIMER = 69651;
    public static final int MSG_CONNECTED = 69634;
    public static final int MSG_CONNECT_ERROR = 69636;
    public static final int MSG_DISCONNECTED = 69635;
    public static final int MSG_INIT_CONNECTION = 69633;
    public static final int MSG_MINUTE_TIMER = 69650;
    public static final int MSG_MQTT_CONNECT_BLOCK_CHECK_TIMER = 69652;
    public static final int MSG_RECEIVED = 69640;
    public static final int MSG_REQUSET_RESEND = 69649;
    public static final int MSG_SECOND_TIMER = 69641;
    public static final int MSG_SEND = 69637;
    public static final int MSG_SENDED = 69638;
    public static final int MSG_SEND_FAIL = 69639;
    public static final int MSG_SYNC_TIME = 69648;
    private static final String SYNC_TIME_SERVER = "http://addr.xcloud.xcharger.net/empty";
    public static final int TIMEOUT_RESPONSE = 10;
    public static final int TIMEOUT_SEND = 5;
    public static final int TIMER_CLOUD_TIMESYNCH = 43200;
    public static final int TIMER_MQTT_CONNECT_BLOCKED = 300;
    public static final String clientKey = "9569";
    private static XCloudProtocolAgent instance = null;
    public static final String magicKey = "$m9u4uEet5q13C0544AS";
    /* access modifiers changed from: private */
    public String authKey = null;
    private CloudTimeSynchObserver cloudTimeSynchObserver = null;
    private ThreadPoolExecutor connectThreadPoolExecutor = null;
    private Context context = null;
    /* access modifiers changed from: private */
    public MsgHandler handler = null;
    /* access modifiers changed from: private */
    public HandlerTimer handlerTimer = null;
    /* access modifiers changed from: private */
    public boolean isConnected = false;
    private DeviceSetting latestDeviceSettingError = null;
    /* access modifiers changed from: private */
    public Long mqttConnectBeginTime = null;
    /* access modifiers changed from: private */
    public Long mqttConnectEndTime = null;
    /* access modifiers changed from: private */
    public AtomicLong networkDiagnosisCnt = new AtomicLong(0);
    private NetworkStatusObserver networkStatusObserver = null;
    private HashMap<String, XCloudPortHandler> portHandlers = null;
    /* access modifiers changed from: private */
    public HashMap<String, SendRequestState> sendReqestState = null;
    private AtomicLong sidGen = new AtomicLong(20000);
    /* access modifiers changed from: private */

    /* renamed from: sn */
    public String f87sn = null;
    private HandlerThread thread = null;
    private MessageProxyOptions xcloudOpt = null;
    /* access modifiers changed from: private */
    public MessageProxy xcloudProxy = null;

    private interface OnSendMessageCallback {
        void onFailed(XCloudMessage xCloudMessage);

        void onSended(XCloudMessage xCloudMessage);
    }

    public static XCloudProtocolAgent getInstance() {
        if (instance == null) {
            instance = new XCloudProtocolAgent();
        }
        return instance;
    }

    private static class SendRequestState {
        XCloudMessage request;
        XCLOUD_REQUEST_STATE status;
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
        public void handleMessage(android.os.Message r17) {
            /*
                r16 = this;
                r14 = 0
                r9 = 0
                r8 = 0
                r0 = r17
                int r10 = r0.what     // Catch:{ Exception -> 0x0017 }
                switch(r10) {
                    case 69633: goto L_0x000f;
                    case 69634: goto L_0x0047;
                    case 69635: goto L_0x00cd;
                    case 69636: goto L_0x011e;
                    case 69637: goto L_0x0283;
                    case 69638: goto L_0x0347;
                    case 69639: goto L_0x041f;
                    case 69640: goto L_0x0180;
                    case 69641: goto L_0x043f;
                    case 69648: goto L_0x0473;
                    case 69649: goto L_0x0430;
                    case 69650: goto L_0x0459;
                    case 69651: goto L_0x04b2;
                    case 69652: goto L_0x04fd;
                    case 135169: goto L_0x04db;
                    case 143361: goto L_0x04ec;
                    default: goto L_0x000b;
                }
            L_0x000b:
                super.handleMessage(r17)
                return
            L_0x000f:
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                r10.connect()     // Catch:{ Exception -> 0x0017 }
                goto L_0x000b
            L_0x0017:
                r2 = move-exception
                java.lang.String r10 = "XCloudProtocolAgent.handleMessage"
                java.lang.StringBuilder r11 = new java.lang.StringBuilder
                java.lang.String r12 = "except: "
                r11.<init>(r12)
                java.lang.String r12 = android.util.Log.getStackTraceString(r2)
                java.lang.StringBuilder r11 = r11.append(r12)
                java.lang.String r11 = r11.toString()
                android.util.Log.e(r10, r11)
                java.lang.StringBuilder r10 = new java.lang.StringBuilder
                java.lang.String r11 = "XCloudProtocolAgent handleMessage exception: "
                r10.<init>(r11)
                java.lang.String r11 = android.util.Log.getStackTraceString(r2)
                java.lang.StringBuilder r10 = r10.append(r11)
                java.lang.String r10 = r10.toString()
                com.xcharge.common.utils.LogUtils.syslog(r10)
                goto L_0x000b
            L_0x0047:
                java.lang.String r10 = "XCloudProtocolAgent.handleMessage"
                java.lang.String r11 = "connected !!!"
                android.util.Log.i(r10, r11)     // Catch:{ Exception -> 0x0017 }
                java.lang.String r10 = "xcloud login !!!"
                com.xcharge.common.utils.LogUtils.cloudlog(r10)     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                java.util.concurrent.atomic.AtomicLong r10 = r10.networkDiagnosisCnt     // Catch:{ Exception -> 0x0017 }
                long r10 = r10.longValue()     // Catch:{ Exception -> 0x0017 }
                int r10 = (r10 > r14 ? 1 : (r10 == r14 ? 0 : -1))
                if (r10 == 0) goto L_0x0094
                java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0017 }
                java.lang.String r11 = "skip "
                r10.<init>(r11)     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                java.util.concurrent.atomic.AtomicLong r11 = r11.networkDiagnosisCnt     // Catch:{ Exception -> 0x0017 }
                long r12 = r11.longValue()     // Catch:{ Exception -> 0x0017 }
                java.lang.StringBuilder r10 = r10.append(r12)     // Catch:{ Exception -> 0x0017 }
                java.lang.String r11 = " times network connection diagnosis !!!"
                java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ Exception -> 0x0017 }
                java.lang.String r10 = r10.toString()     // Catch:{ Exception -> 0x0017 }
                com.xcharge.common.utils.LogUtils.cloudlog(r10)     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                java.util.concurrent.atomic.AtomicLong r10 = r10.networkDiagnosisCnt     // Catch:{ Exception -> 0x0017 }
                r12 = 0
                r10.set(r12)     // Catch:{ Exception -> 0x0017 }
            L_0x0094:
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                r11 = 1
                r10.isConnected = r11     // Catch:{ Exception -> 0x0017 }
                com.xcharge.charger.data.provider.ChargeStatusCacheProvider r10 = com.xcharge.charger.data.provider.ChargeStatusCacheProvider.getInstance()     // Catch:{ Exception -> 0x0017 }
                r11 = 1
                r10.updateCloudConnected(r11)     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                com.xcharge.common.utils.HandlerTimer r10 = r10.handlerTimer     // Catch:{ Exception -> 0x0017 }
                r12 = 1000(0x3e8, double:4.94E-321)
                r11 = 69641(0x11009, float:9.7588E-41)
                r14 = 0
                r10.startTimer(r12, r11, r14)     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                r10.portsActive()     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                r11 = 0
                r10.reportSystemInfo(r11)     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                r10.requestSetting()     // Catch:{ Exception -> 0x0017 }
                goto L_0x000b
            L_0x00cd:
                java.lang.String r10 = "XCloudProtocolAgent.handleMessage"
                java.lang.String r11 = "disconnected !!!"
                android.util.Log.i(r10, r11)     // Catch:{ Exception -> 0x0017 }
                java.lang.String r10 = "xcloud logout !!!"
                com.xcharge.common.utils.LogUtils.cloudlog(r10)     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                r11 = 0
                r10.isConnected = r11     // Catch:{ Exception -> 0x0017 }
                com.xcharge.charger.data.provider.ChargeStatusCacheProvider r10 = com.xcharge.charger.data.provider.ChargeStatusCacheProvider.getInstance()     // Catch:{ Exception -> 0x0017 }
                r11 = 0
                r10.updateCloudConnected(r11)     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent$MsgHandler r10 = r10.handler     // Catch:{ Exception -> 0x0017 }
                r11 = 69649(0x11011, float:9.7599E-41)
                r10.removeMessages(r11)     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                com.xcharge.common.utils.HandlerTimer r10 = r10.handlerTimer     // Catch:{ Exception -> 0x0017 }
                r11 = 69641(0x11009, float:9.7588E-41)
                r10.stopTimer(r11)     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                r10.portsDeactive()     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent$MsgHandler r10 = r10.handler     // Catch:{ Exception -> 0x0017 }
                r11 = 69633(0x11001, float:9.7577E-41)
                r12 = 10000(0x2710, double:4.9407E-320)
                r10.sendEmptyMessageDelayed(r11, r12)     // Catch:{ Exception -> 0x0017 }
                goto L_0x000b
            L_0x011e:
                java.lang.String r10 = "XCloudProtocolAgent.handleMessage"
                java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0017 }
                java.lang.String r12 = "failed to connect !!! authKey: "
                r11.<init>(r12)     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r12 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                java.lang.String r12 = r12.authKey     // Catch:{ Exception -> 0x0017 }
                java.lang.StringBuilder r11 = r11.append(r12)     // Catch:{ Exception -> 0x0017 }
                java.lang.String r11 = r11.toString()     // Catch:{ Exception -> 0x0017 }
                android.util.Log.i(r10, r11)     // Catch:{ Exception -> 0x0017 }
                com.xcharge.charger.data.provider.HardwareStatusCacheProvider r10 = com.xcharge.charger.data.provider.HardwareStatusCacheProvider.getInstance()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.isNetworkConnected()     // Catch:{ Exception -> 0x0017 }
                if (r10 == 0) goto L_0x016e
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                java.util.concurrent.atomic.AtomicLong r10 = r10.networkDiagnosisCnt     // Catch:{ Exception -> 0x0017 }
                long r4 = r10.getAndIncrement()     // Catch:{ Exception -> 0x0017 }
                int r10 = (r4 > r14 ? 1 : (r4 == r14 ? 0 : -1))
                if (r10 < 0) goto L_0x016e
                r10 = 3
                int r10 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1))
                if (r10 >= 0) goto L_0x016e
                java.lang.String r10 = "failed to connnect to xcloud, try to diagnosis network connectivity ..."
                com.xcharge.common.utils.LogUtils.applog(r10)     // Catch:{ Exception -> 0x0017 }
                java.lang.Thread r10 = new java.lang.Thread     // Catch:{ Exception -> 0x0017 }
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent$MsgHandler$1 r11 = new com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent$MsgHandler$1     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                r11.<init>()     // Catch:{ Exception -> 0x0017 }
                r10.<init>(r11)     // Catch:{ Exception -> 0x0017 }
                r10.start()     // Catch:{ Exception -> 0x0017 }
            L_0x016e:
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent$MsgHandler r10 = r10.handler     // Catch:{ Exception -> 0x0017 }
                r11 = 69633(0x11001, float:9.7577E-41)
                r12 = 20000(0x4e20, double:9.8813E-320)
                r10.sendEmptyMessageDelayed(r11, r12)     // Catch:{ Exception -> 0x0017 }
                goto L_0x000b
            L_0x0180:
                r0 = r17
                java.lang.Object r10 = r0.obj     // Catch:{ Exception -> 0x0017 }
                r0 = r10
                com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage r0 = (com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage) r0     // Catch:{ Exception -> 0x0017 }
                r9 = r0
                java.lang.String r10 = "XCloudProtocolAgent.handleMessage"
                java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0017 }
                java.lang.String r12 = "received XCloud msg: "
                r11.<init>(r12)     // Catch:{ Exception -> 0x0017 }
                java.lang.String r12 = r9.toJson()     // Catch:{ Exception -> 0x0017 }
                java.lang.StringBuilder r11 = r11.append(r12)     // Catch:{ Exception -> 0x0017 }
                java.lang.String r11 = r11.toString()     // Catch:{ Exception -> 0x0017 }
                android.util.Log.d(r10, r11)     // Catch:{ Exception -> 0x0017 }
                java.lang.String r10 = "SendChargeQRCode"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x0200
                java.lang.String r10 = "RequestStartCharge"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x0200
                java.lang.String r10 = "RequestRefuseCharge"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x0200
                java.lang.String r10 = "ConfirmChargeStarted"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x0200
                java.lang.String r10 = "ConfirmChargeEnded"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x0200
                java.lang.String r10 = "AnswerHello"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x0200
                java.lang.String r10 = "ConfirmLocalChargeBill"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x0200
                java.lang.String r10 = "ApplySetting"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 == 0) goto L_0x0279
            L_0x0200:
                java.lang.String r7 = r9.getSessionId()     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                java.util.HashMap r10 = r10.sendReqestState     // Catch:{ Exception -> 0x0017 }
                java.lang.Object r3 = r10.get(r7)     // Catch:{ Exception -> 0x0017 }
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent$SendRequestState r3 = (com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.SendRequestState) r3     // Catch:{ Exception -> 0x0017 }
                if (r3 == 0) goto L_0x0231
                com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage r6 = r3.request     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                java.util.HashMap r10 = r10.sendReqestState     // Catch:{ Exception -> 0x0017 }
                r10.remove(r7)     // Catch:{ Exception -> 0x0017 }
                java.lang.String r10 = r6.getPort()     // Catch:{ Exception -> 0x0017 }
                r9.setPort(r10)     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                r10.dispatchXCloudMessage(r9, r6)     // Catch:{ Exception -> 0x0017 }
                goto L_0x000b
            L_0x0231:
                java.lang.String r10 = "RequestStartCharge"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x0255
                java.lang.String r10 = "RequestRefuseCharge"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x0255
                java.lang.String r10 = "ApplySetting"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 == 0) goto L_0x025f
            L_0x0255:
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                r11 = 0
                r10.dispatchXCloudMessage(r9, r11)     // Catch:{ Exception -> 0x0017 }
                goto L_0x000b
            L_0x025f:
                java.lang.String r10 = "XCloudProtocolAgent.handleMessage"
                java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0017 }
                java.lang.String r12 = "maybe timeout to wait for response msg: "
                r11.<init>(r12)     // Catch:{ Exception -> 0x0017 }
                java.lang.String r12 = r9.toJson()     // Catch:{ Exception -> 0x0017 }
                java.lang.StringBuilder r11 = r11.append(r12)     // Catch:{ Exception -> 0x0017 }
                java.lang.String r11 = r11.toString()     // Catch:{ Exception -> 0x0017 }
                android.util.Log.w(r10, r11)     // Catch:{ Exception -> 0x0017 }
                goto L_0x000b
            L_0x0279:
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                r11 = 0
                r10.dispatchXCloudMessage(r9, r11)     // Catch:{ Exception -> 0x0017 }
                goto L_0x000b
            L_0x0283:
                r0 = r17
                java.lang.Object r10 = r0.obj     // Catch:{ Exception -> 0x0017 }
                r0 = r10
                com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage r0 = (com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage) r0     // Catch:{ Exception -> 0x0017 }
                r9 = r0
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.isConnected()     // Catch:{ Exception -> 0x0017 }
                if (r10 == 0) goto L_0x032b
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                r10.sendXCloudMessage(r9)     // Catch:{ Exception -> 0x0017 }
                java.lang.String r10 = "RequestChargeQRCode"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x0308
                java.lang.String r10 = "RequestChargeWithIDCard "
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x0308
                java.lang.String r10 = "ReportChargeStarted"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x0308
                java.lang.String r10 = "ReportChargeEnded"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x0308
                java.lang.String r10 = "ReportLocalChargeBill"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x0308
                java.lang.String r10 = "SayHello"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x0308
                java.lang.String r10 = "ReportLocalChargeStarted"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x0308
                java.lang.String r10 = "ReportLocalChargeEnded"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x0308
                java.lang.String r10 = "RequestSetting"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 == 0) goto L_0x000b
            L_0x0308:
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent$SendRequestState r3 = new com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent$SendRequestState     // Catch:{ Exception -> 0x0017 }
                r10 = 0
                r3.<init>(r10)     // Catch:{ Exception -> 0x0017 }
                r3.request = r9     // Catch:{ Exception -> 0x0017 }
                com.xcharge.charger.protocol.family.xcloud.type.XCLOUD_REQUEST_STATE r10 = com.xcharge.charger.protocol.family.xcloud.type.XCLOUD_REQUEST_STATE.sending     // Catch:{ Exception -> 0x0017 }
                r3.status = r10     // Catch:{ Exception -> 0x0017 }
                long r10 = java.lang.System.currentTimeMillis()     // Catch:{ Exception -> 0x0017 }
                r3.timestamp = r10     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                java.util.HashMap r10 = r10.sendReqestState     // Catch:{ Exception -> 0x0017 }
                java.lang.String r11 = r9.getSessionId()     // Catch:{ Exception -> 0x0017 }
                r10.put(r11, r3)     // Catch:{ Exception -> 0x0017 }
                goto L_0x000b
            L_0x032b:
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent$MsgHandler r10 = r10.handler     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r11 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent$MsgHandler r11 = r11.handler     // Catch:{ Exception -> 0x0017 }
                r12 = 69639(0x11007, float:9.7585E-41)
                android.os.Message r11 = r11.obtainMessage(r12, r9)     // Catch:{ Exception -> 0x0017 }
                r10.sendMessage(r11)     // Catch:{ Exception -> 0x0017 }
                goto L_0x000b
            L_0x0347:
                r0 = r17
                java.lang.Object r10 = r0.obj     // Catch:{ Exception -> 0x0017 }
                r0 = r10
                com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage r0 = (com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage) r0     // Catch:{ Exception -> 0x0017 }
                r9 = r0
                java.lang.String r10 = "XCloudProtocolAgent.handleMessage"
                java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0017 }
                java.lang.String r12 = "succeed to send XCloud msg: "
                r11.<init>(r12)     // Catch:{ Exception -> 0x0017 }
                java.lang.String r12 = r9.toJson()     // Catch:{ Exception -> 0x0017 }
                java.lang.StringBuilder r11 = r11.append(r12)     // Catch:{ Exception -> 0x0017 }
                java.lang.String r11 = r11.toString()     // Catch:{ Exception -> 0x0017 }
                android.util.Log.d(r10, r11)     // Catch:{ Exception -> 0x0017 }
                java.lang.String r10 = "RequestChargeQRCode"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x03d3
                java.lang.String r10 = "RequestChargeWithIDCard "
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x03d3
                java.lang.String r10 = "ReportChargeStarted"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x03d3
                java.lang.String r10 = "ReportChargeEnded"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x03d3
                java.lang.String r10 = "ReportLocalChargeBill"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x03d3
                java.lang.String r10 = "SayHello"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x03d3
                java.lang.String r10 = "ReportLocalChargeStarted"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x03d3
                java.lang.String r10 = "ReportLocalChargeEnded"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x03d3
                java.lang.String r10 = "RequestSetting"
                java.lang.String r11 = r9.getMessageName()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.equals(r11)     // Catch:{ Exception -> 0x0017 }
                if (r10 == 0) goto L_0x0416
            L_0x03d3:
                java.lang.String r7 = r9.getSessionId()     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                java.util.HashMap r10 = r10.sendReqestState     // Catch:{ Exception -> 0x0017 }
                java.lang.Object r3 = r10.get(r7)     // Catch:{ Exception -> 0x0017 }
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent$SendRequestState r3 = (com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.SendRequestState) r3     // Catch:{ Exception -> 0x0017 }
                if (r3 == 0) goto L_0x03fc
                com.xcharge.charger.protocol.family.xcloud.type.XCLOUD_REQUEST_STATE r10 = com.xcharge.charger.protocol.family.xcloud.type.XCLOUD_REQUEST_STATE.sended     // Catch:{ Exception -> 0x0017 }
                r3.status = r10     // Catch:{ Exception -> 0x0017 }
                long r10 = java.lang.System.currentTimeMillis()     // Catch:{ Exception -> 0x0017 }
                r3.timestamp = r10     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage r11 = r3.request     // Catch:{ Exception -> 0x0017 }
                r10.handleSendMsgOk(r11)     // Catch:{ Exception -> 0x0017 }
                goto L_0x000b
            L_0x03fc:
                java.lang.String r10 = "XCloudProtocolAgent.handleMessage"
                java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0017 }
                java.lang.String r12 = "maybe timeout to send XCloud request msg: "
                r11.<init>(r12)     // Catch:{ Exception -> 0x0017 }
                java.lang.String r12 = r9.toJson()     // Catch:{ Exception -> 0x0017 }
                java.lang.StringBuilder r11 = r11.append(r12)     // Catch:{ Exception -> 0x0017 }
                java.lang.String r11 = r11.toString()     // Catch:{ Exception -> 0x0017 }
                android.util.Log.w(r10, r11)     // Catch:{ Exception -> 0x0017 }
                goto L_0x000b
            L_0x0416:
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                r10.handleSendMsgOk(r9)     // Catch:{ Exception -> 0x0017 }
                goto L_0x000b
            L_0x041f:
                r0 = r17
                java.lang.Object r10 = r0.obj     // Catch:{ Exception -> 0x0017 }
                r0 = r10
                com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage r0 = (com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage) r0     // Catch:{ Exception -> 0x0017 }
                r9 = r0
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                r10.handleSendMsgFail(r9)     // Catch:{ Exception -> 0x0017 }
                goto L_0x000b
            L_0x0430:
                r0 = r17
                java.lang.Object r6 = r0.obj     // Catch:{ Exception -> 0x0017 }
                com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage r6 = (com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage) r6     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                r10.resendRequest(r6)     // Catch:{ Exception -> 0x0017 }
                goto L_0x000b
            L_0x043f:
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x050b }
                r10.requestTimeoutCheck()     // Catch:{ Exception -> 0x050b }
            L_0x0446:
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                com.xcharge.common.utils.HandlerTimer r10 = r10.handlerTimer     // Catch:{ Exception -> 0x0017 }
                r12 = 1000(0x3e8, double:4.94E-321)
                r11 = 69641(0x11009, float:9.7588E-41)
                r14 = 0
                r10.startTimer(r12, r11, r14)     // Catch:{ Exception -> 0x0017 }
                goto L_0x000b
            L_0x0459:
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0508 }
                r10.sendSayHello()     // Catch:{ Exception -> 0x0508 }
            L_0x0460:
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                com.xcharge.common.utils.HandlerTimer r10 = r10.handlerTimer     // Catch:{ Exception -> 0x0017 }
                r12 = 1000(0x3e8, double:4.94E-321)
                r11 = 69641(0x11009, float:9.7588E-41)
                r14 = 0
                r10.startTimer(r12, r11, r14)     // Catch:{ Exception -> 0x0017 }
                goto L_0x000b
            L_0x0473:
                com.xcharge.charger.data.provider.HardwareStatusCacheProvider r10 = com.xcharge.charger.data.provider.HardwareStatusCacheProvider.getInstance()     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.isNetworkConnected()     // Catch:{ Exception -> 0x0017 }
                if (r10 == 0) goto L_0x04a0
                java.lang.String r10 = "XCloudProtocolAgent.handleMessage"
                java.lang.String r11 = "sync time from cloud !!!"
                android.util.Log.i(r10, r11)     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                boolean r10 = r10.syncTime()     // Catch:{ Exception -> 0x0017 }
                if (r10 != 0) goto L_0x000b
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent$MsgHandler r10 = r10.handler     // Catch:{ Exception -> 0x0017 }
                r11 = 69648(0x11010, float:9.7598E-41)
                r12 = 5000(0x1388, double:2.4703E-320)
                r10.sendEmptyMessageDelayed(r11, r12)     // Catch:{ Exception -> 0x0017 }
                goto L_0x000b
            L_0x04a0:
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent$MsgHandler r10 = r10.handler     // Catch:{ Exception -> 0x0017 }
                r11 = 69648(0x11010, float:9.7598E-41)
                r12 = 5000(0x1388, double:2.4703E-320)
                r10.sendEmptyMessageDelayed(r11, r12)     // Catch:{ Exception -> 0x0017 }
                goto L_0x000b
            L_0x04b2:
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                com.xcharge.common.utils.HandlerTimer r10 = r10.handlerTimer     // Catch:{ Exception -> 0x0017 }
                r11 = 69651(0x11013, float:9.7602E-41)
                r10.stopTimer(r11)     // Catch:{ Exception -> 0x0017 }
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0506 }
                boolean unused = r10.syncTime()     // Catch:{ Exception -> 0x0506 }
            L_0x04c7:
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                com.xcharge.common.utils.HandlerTimer r10 = r10.handlerTimer     // Catch:{ Exception -> 0x0017 }
                r12 = 43200000(0x2932e00, double:2.1343636E-316)
                r11 = 69651(0x11013, float:9.7602E-41)
                r14 = 0
                r10.startTimer(r12, r11, r14)     // Catch:{ Exception -> 0x0017 }
                goto L_0x000b
            L_0x04db:
                r0 = r17
                java.lang.Object r10 = r0.obj     // Catch:{ Exception -> 0x0017 }
                r0 = r10
                android.net.Uri r0 = (android.net.Uri) r0     // Catch:{ Exception -> 0x0017 }
                r8 = r0
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                r10.handleNetworkStatusChanged(r8)     // Catch:{ Exception -> 0x0017 }
                goto L_0x000b
            L_0x04ec:
                r0 = r17
                java.lang.Object r10 = r0.obj     // Catch:{ Exception -> 0x0017 }
                r0 = r10
                android.net.Uri r0 = (android.net.Uri) r0     // Catch:{ Exception -> 0x0017 }
                r8 = r0
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                r10.handleCloudTimeSynch(r8)     // Catch:{ Exception -> 0x0017 }
                goto L_0x000b
            L_0x04fd:
                r0 = r16
                com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent r10 = com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.this     // Catch:{ Exception -> 0x0017 }
                r10.handleMqttConnectBlocked()     // Catch:{ Exception -> 0x0017 }
                goto L_0x000b
            L_0x0506:
                r10 = move-exception
                goto L_0x04c7
            L_0x0508:
                r10 = move-exception
                goto L_0x0460
            L_0x050b:
                r10 = move-exception
                goto L_0x0446
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.MsgHandler.handleMessage(android.os.Message):void");
        }
    }

    public void init(Context context2) {
        this.context = context2;
        if (TextUtils.isEmpty(RemoteSettingCacheProvider.getInstance().getProtocolTimezone())) {
            RemoteSettingCacheProvider.getInstance().updateProtocolTimezone("+08:00");
        }
        this.portHandlers = new HashMap<>();
        HashMap<String, Port> ports = HardwareStatusCacheProvider.getInstance().getPorts();
        if (ports != null) {
            for (String port : ports.keySet()) {
                XCloudPortHandler portHandler = new XCloudPortHandler();
                portHandler.init(context2, port);
                this.portHandlers.put(port, portHandler);
            }
        }
        this.connectThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue(20), new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                if (r instanceof SendTask) {
                    XCloudProtocolAgent.this.handler.sendMessage(XCloudProtocolAgent.this.handler.obtainMessage(69639, ((SendTask) r).getMessage()));
                    Log.w("XCloudProtocolAgent.connectThreadPoolExecutor.rejectedExecution", "rejected send task, msg: " + ((SendTask) r).getMessage().toJson() + ", active runnables: " + executor.getActiveCount());
                } else if (r instanceof ConnectTask) {
                    XCloudProtocolAgent.this.handler.sendEmptyMessage(69636);
                    Log.w("XCloudProtocolAgent.connectThreadPoolExecutor.rejectedExecution", "rejected connect task, active runnables: " + executor.getActiveCount());
                }
            }
        });
        this.sendReqestState = new HashMap<>();
        this.thread = new HandlerThread("XCloudProtocolAgent", 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(context2);
        this.networkStatusObserver = new NetworkStatusObserver(this.context, this.handler);
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor(Network.class.getSimpleName()), true, this.networkStatusObserver);
        this.cloudTimeSynchObserver = new CloudTimeSynchObserver(context2, this.handler);
        this.context.getContentResolver().registerContentObserver(ChargeStatusCacheProvider.getInstance().getUriFor("cloud/timeSynch"), true, this.cloudTimeSynchObserver);
        MqttOptions options = getOptions();
        this.xcloudOpt = new MessageProxyOptions();
        this.xcloudOpt.setDevMode(options.isDebugMode());
        this.xcloudOpt.setConnectionTimeout(options.getConnectionTimeout());
        this.xcloudOpt.setKeepAlive(options.getKeepAlive());
        this.xcloudOpt.setMsgIdCacheSize(options.getMaxMemorySize());
        this.xcloudOpt.setBinaryMode(options.isBinaryMode());
        this.xcloudOpt.setRegion(options.getRegion());
        this.xcloudOpt.setBroker(options.getBroker());
        this.xcloudOpt.setUserName(options.getUserName());
        this.xcloudOpt.setPassword(options.getPassword());
        this.xcloudOpt.setClientId(options.getClientId());
        this.xcloudOpt.setUpTopic(options.getUpTopic());
        this.xcloudOpt.setDownTopic(options.getDownTopic());
        this.xcloudProxy = MessageProxyFactory.createInstance(this.xcloudOpt);
        this.f87sn = HardwareStatusCacheProvider.getInstance().getSn();
        this.authKey = MD5Utils.MD5(TextUtils.concat(new CharSequence[]{clientKey, magicKey, this.f87sn}).toString()).toLowerCase();
        LogUploadAgent.getInstance().init(this.context);
    }

    public void destroy() {
        this.context.getContentResolver().unregisterContentObserver(this.cloudTimeSynchObserver);
        this.context.getContentResolver().unregisterContentObserver(this.networkStatusObserver);
        LogUploadAgent.getInstance().destroy();
        disconnect();
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
        this.handler.removeMessages(69649);
        this.handler.removeMessages(69650);
        this.handler.removeMessages(MSG_CLOUD_TIMESYNCH_TIMER);
        this.handler.removeMessages(MSG_MQTT_CONNECT_BLOCK_CHECK_TIMER);
        this.thread.quit();
        for (XCloudPortHandler portHandler : this.portHandlers.values()) {
            portHandler.destroy();
        }
        this.portHandlers.clear();
        this.sendReqestState.clear();
        this.connectThreadPoolExecutor.shutdown();
    }

    /* access modifiers changed from: private */
    public void handleNetworkStatusChanged(Uri uri) {
        Log.i("XCloudProtocolAgent.handleNetworkStatusChanged", "network status changed, uri: " + uri.toString());
        String lastSegment = uri.getLastPathSegment();
        if (!"connected".equals(lastSegment)) {
            "disconnected".equals(lastSegment);
        }
    }

    /* access modifiers changed from: private */
    public void handleCloudTimeSynch(Uri uri) {
        Log.i("XCloudProtocolAgent.handleCloudTimeSynch", "cloud time synch setted, uri: " + uri.toString());
        if (ChargeStatusCacheProvider.getInstance().isCloudTimeSynch()) {
            this.handlerTimer.startTimer(43200000, MSG_CLOUD_TIMESYNCH_TIMER, (Object) null);
        }
    }

    public void initServerTimeSync() {
        this.handler.sendEmptyMessage(69648);
    }

    /* access modifiers changed from: private */
    public boolean syncTime() {
        try {
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 10000);
            HttpConnectionParams.setSoTimeout(params, 10000);
            DefaultHttpClient httpClient = new DefaultHttpClient(params);
            String syncTimeURL = SYNC_TIME_SERVER;
            if (SERVICE_REGION.Europe.equals(this.xcloudOpt.getRegion())) {
                syncTimeURL = "http://eu-addr.xcloud.xcharger.net/empty";
            }
            HttpGet request = new HttpGet(syncTimeURL);
            request.setHeader(HttpHeaders.CONNECTION, "Close");
            request.setHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Log.i("XCloudProtocolAgent.syncTime", "request time sync, url: " + syncTimeURL);
            Log.i("XCloudProtocolAgent.syncTime", "local time before request: " + sdf.format(new Date(System.currentTimeMillis())));
            HttpResponse response = httpClient.execute(request);
            Log.i("XCloudProtocolAgent.syncTime", "local time when response: " + sdf.format(new Date(System.currentTimeMillis())));
            Date date = new Date(response.getHeaders("Date")[0].getValue());
            HashMap<String, Object> values = new HashMap<>();
            values.put("cloud_ts", String.valueOf(date.getTime()));
            values.put("local_ts", String.valueOf(System.currentTimeMillis()));
            DCAPProxy.getInstance().setRequest(0, true, SetDirective.SET_ID_DEVICE_TIME_CLOUDSYNCH, values);
            return true;
        } catch (Exception e) {
            Log.w("XCloudProtocolAgent.syncTime", Log.getStackTraceString(e));
            return false;
        }
    }

    /* access modifiers changed from: private */
    public boolean isConnected() {
        return this.isConnected;
    }

    public void initConnection() {
        this.handler.sendEmptyMessage(69633);
    }

    /* access modifiers changed from: private */
    public void handleMqttConnectBlocked() {
        Log.d("XCloudProtocolAgent.handleMqttConnectBlocked", "mqtt connect call maybe blocked forever, here will reinit mqtt communication module");
        LogUtils.cloudlog("mqtt connect call maybe blocked forever, here will reinit mqtt communication module");
        this.connectThreadPoolExecutor.shutdown();
        this.connectThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue(10), new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                if (r instanceof SendTask) {
                    XCloudProtocolAgent.this.handler.sendMessage(XCloudProtocolAgent.this.handler.obtainMessage(69639, ((SendTask) r).getMessage()));
                    Log.w("XCloudProtocolAgent.handleMqttConnectBlocked.rejectedExecution", "rejected send task, msg: " + ((SendTask) r).getMessage().toJson() + ", active runnables: " + executor.getActiveCount());
                } else if (r instanceof ConnectTask) {
                    XCloudProtocolAgent.this.handler.sendEmptyMessage(69636);
                    Log.w("XCloudProtocolAgent.handleMqttConnectBlocked.rejectedExecution", "rejected connect task, active runnables: " + executor.getActiveCount());
                }
            }
        });
        this.xcloudProxy = MessageProxyFactory.createInstance(this.xcloudOpt);
        this.xcloudProxy.forceToClearClient();
        initConnection();
        Log.d("XCloudProtocolAgent.handleMqttConnectBlocked", "mqtt communication module reinited");
        LogUtils.cloudlog("mqtt communication module reinited");
    }

    private class ConnectTask implements Runnable {
        private MessageHandler msgHandler = null;

        public ConnectTask(MessageHandler msgHandler2) {
            this.msgHandler = msgHandler2;
        }

        public void run() {
            try {
                XCloudProtocolAgent.this.mqttConnectBeginTime = Long.valueOf(System.nanoTime());
                XCloudProtocolAgent.this.handlerTimer.stopTimer(XCloudProtocolAgent.MSG_MQTT_CONNECT_BLOCK_CHECK_TIMER);
                XCloudProtocolAgent.this.handlerTimer.startTimer(300000, XCloudProtocolAgent.MSG_MQTT_CONNECT_BLOCK_CHECK_TIMER, (Object) null);
                if (XCloudProtocolAgent.this.xcloudProxy.connect(XCloudProtocolAgent.this.f87sn, XCloudProtocolAgent.this.authKey, this.msgHandler)) {
                    XCloudProtocolAgent.this.handler.sendEmptyMessage(69634);
                } else {
                    XCloudProtocolAgent.this.handler.sendEmptyMessage(69636);
                }
            } catch (Exception e) {
                Log.w("XCloudProtocolAgent.ConnectTask", Log.getStackTraceString(e));
                XCloudProtocolAgent.this.handler.sendEmptyMessage(69636);
            }
            XCloudProtocolAgent.this.handlerTimer.stopTimer(XCloudProtocolAgent.MSG_MQTT_CONNECT_BLOCK_CHECK_TIMER);
            XCloudProtocolAgent.this.mqttConnectEndTime = Long.valueOf(System.nanoTime());
            Log.d("XCloudProtocolAgent.ConnectTask", "connect execute time: " + ((XCloudProtocolAgent.this.mqttConnectEndTime.longValue() - XCloudProtocolAgent.this.mqttConnectBeginTime.longValue()) / 1000000) + " ms");
        }
    }

    /* access modifiers changed from: private */
    public void connect() {
        if (HardwareStatusCacheProvider.getInstance().isNetworkConnected()) {
            Log.i("XCloudProtocolAgent.connect", "init connection !!!");
            this.connectThreadPoolExecutor.execute(new ConnectTask(this));
            return;
        }
        this.handler.sendEmptyMessageDelayed(69633, 5000);
    }

    private void disconnect() {
        try {
            this.xcloudProxy.disconnect();
        } catch (Exception e) {
            Log.e("XCloudProtocolAgent.disconnect", Log.getStackTraceString(e));
        }
        this.handler.sendEmptyMessage(69635);
    }

    private class SendTask implements Runnable {
        private OnSendMessageCallback callback = null;
        private XCloudMessage msg = null;

        public SendTask(XCloudMessage msg2, OnSendMessageCallback callback2) {
            this.callback = callback2;
            this.msg = msg2;
        }

        public XCloudMessage getMessage() {
            return this.msg;
        }

        public void run() {
            try {
                XCloudProtocolAgent.this.xcloudProxy.sendMessage(this.msg.getMessageName(), this.msg.getVersion(), this.msg.getData());
                if (this.callback != null) {
                    this.callback.onSended(this.msg);
                }
            } catch (Exception e) {
                Log.e("XCloudProtocolAgent.SendTask", Log.getStackTraceString(e));
                if (this.callback != null) {
                    this.callback.onFailed(this.msg);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void sendXCloudMessage(XCloudMessage msg) {
        this.connectThreadPoolExecutor.execute(new SendTask(msg, new OnSendMessageCallback() {
            public void onSended(XCloudMessage msg) {
                XCloudProtocolAgent.this.handler.sendMessage(XCloudProtocolAgent.this.handler.obtainMessage(69638, msg));
            }

            public void onFailed(XCloudMessage msg) {
                XCloudProtocolAgent.this.handler.sendMessage(XCloudProtocolAgent.this.handler.obtainMessage(69639, msg));
            }
        }));
    }

    private MqttOptions getOptions() {
        String mqttCfg = ContextUtils.readFileData("xcloud_family_mqtt_cfg.json", this.context);
        if (TextUtils.isEmpty(mqttCfg)) {
            mqttCfg = ContextUtils.getRawFileToString(this.context, C0252R.raw.xcloud_family_mqtt_ops);
            if (!TextUtils.isEmpty(mqttCfg)) {
                ContextUtils.writeFileData("xcloud_family_mqtt_cfg.json", mqttCfg, this.context);
            }
        }
        Log.d("XCloudProtocolAgent.getOptions", "configured options: " + mqttCfg);
        MqttOptions options = null;
        if (!TextUtils.isEmpty(mqttCfg)) {
            options = (MqttOptions) new MqttOptions().fromJson(mqttCfg);
        }
        if (options == null) {
            options = new MqttOptions();
        }
        if (options.getRegion() == null) {
            options.setRegion(SERVICE_REGION.China);
            ContextUtils.writeFileData("xcloud_family_mqtt_cfg.json", options.toJson(), this.context);
        }
        LogUtils.applog("use mqtt config: " + options.toJson());
        return options;
    }

    public boolean sendMessage(XCloudMessage msg) {
        return this.handler.sendMessage(this.handler.obtainMessage(69637, msg));
    }

    /* access modifiers changed from: private */
    public void resendRequest(XCloudMessage request) {
        if (XCloudMessage.RequestSetting.equals(request.getMessageName())) {
            RequestSetting requestSetting = (RequestSetting) request.getBody();
            requestSetting.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
            request.setData(requestSetting.toJson());
        }
        request.setResendCnt(request.getResendCnt() + 1);
        if (!sendMessage(request)) {
            this.handler.sendMessage(this.handler.obtainMessage(69639, request));
        }
    }

    private XCloudPortHandler getPortHandler(String port) {
        return this.portHandlers.get(port);
    }

    public String getPort(String chargeId) {
        for (XCloudPortHandler portHandler : this.portHandlers.values()) {
            String port = portHandler.getPort(chargeId);
            if (!TextUtils.isEmpty(port)) {
                return port;
            }
        }
        return null;
    }

    public XCloudChargeSession getChargeSession(String port) {
        XCloudPortHandler xcloudPortHandler = getPortHandler(port);
        if (xcloudPortHandler != null) {
            return xcloudPortHandler.getChargeSession();
        }
        Log.w("XCloudProtocolAgent.getChargeSession", "no available port handler for port: " + port);
        return null;
    }

    public XCloudChargeSession getChargeSessionById(String chargeId) {
        for (XCloudPortHandler portHandler : this.portHandlers.values()) {
            XCloudChargeSession chargeSession = portHandler.getChargeSession();
            if (chargeId.equals(chargeSession.getCharge_id())) {
                return chargeSession;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public void requestSetting() {
        RequestSetting reqSetting = new RequestSetting();
        long sessionId = getInstance().genSid();
        reqSetting.setSid(Long.valueOf(sessionId));
        reqSetting.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        XCloudMessage msg = new XCloudMessage();
        msg.setMessageName(XCloudMessage.RequestSetting);
        msg.setSrcId(this.f87sn);
        msg.setBody(reqSetting);
        msg.setSessionId(String.valueOf(sessionId));
        msg.setData(reqSetting.toJson());
        if (!sendMessage(msg)) {
            this.handler.sendMessage(this.handler.obtainMessage(69639, msg));
        }
    }

    /* access modifiers changed from: private */
    public void dispatchXCloudMessage(XCloudMessage msg, XCloudMessage sendedRequest) {
        if (sendedRequest != null) {
            handleCloudResponseMessage(msg, sendedRequest);
        } else {
            handleCloudRequestMessage(msg);
        }
    }

    private void handleCloudResponseMessage(XCloudMessage response, XCloudMessage request) {
        String requestMsgName = request.getMessageName();
        String responseMsgName = response.getMessageName();
        if (requestMsgName.equals(XCloudMessage.RequestChargeQRCode) && responseMsgName.equals(XCloudMessage.SendChargeQRCode)) {
            handleSendChargeQRCodeResponse(request, response);
        } else if (requestMsgName.equals(XCloudMessage.ReportChargeStarted) && responseMsgName.equals(XCloudMessage.ConfirmChargeStarted)) {
            handleConfirmChargeStartedResponse(request, response);
        } else if ((requestMsgName.equals(XCloudMessage.ReportChargeEnded) || requestMsgName.equals(XCloudMessage.ReportLocalChargeEnded)) && responseMsgName.equals(XCloudMessage.ConfirmChargeEnded)) {
            handleConfirmChargeEndedResponse(request, response);
        } else if (requestMsgName.equals(XCloudMessage.RequestChargeWithIDCard) && responseMsgName.equals(XCloudMessage.RequestStartCharge)) {
            handleRequestStartChargeResponse(request, response);
        } else if (requestMsgName.equals(XCloudMessage.RequestChargeWithIDCard) && responseMsgName.equals(XCloudMessage.RequestRefuseCharge)) {
            handleRequestRefuseChargeResponse(request, response);
        } else if (requestMsgName.equals(XCloudMessage.ReportLocalChargeStarted) && responseMsgName.equals(XCloudMessage.ConfirmChargeStarted)) {
            handleConfirmChargeStartedResponse(request, response);
        } else if (requestMsgName.equals(XCloudMessage.RequestSetting) && responseMsgName.equals(XCloudMessage.ApplySetting)) {
            LogUtils.cloudlog("receive xcloud ApplySetting response: " + response.getData());
            handleRequestSettingResponse(request, response);
        }
    }

    private void handleRequestSettingResponse(XCloudMessage request, XCloudMessage response) {
        handleApplySettingRequest(response);
    }

    private void handleSendChargeQRCodeResponse(XCloudMessage request, XCloudMessage response) {
        String port = response.getPort();
        XCloudPortHandler portHandler = getPortHandler(port);
        if (portHandler != null) {
            XCloudRequestSession xcloudRequestSession = new XCloudRequestSession();
            xcloudRequestSession.setRequest(request);
            xcloudRequestSession.setResponse(response);
            portHandler.sendMessage(portHandler.obtainMessage(73736, xcloudRequestSession));
            return;
        }
        Log.w("XCloudProtocolAgent.handleSendChargeQRCodeResponse", "failed to find handler for port: " + port);
    }

    private void handleConfirmChargeStartedResponse(XCloudMessage request, XCloudMessage response) {
        String port = response.getPort();
        XCloudPortHandler portHandler = getPortHandler(port);
        if (portHandler != null) {
            XCloudRequestSession xcloudRequestSession = new XCloudRequestSession();
            xcloudRequestSession.setRequest(request);
            xcloudRequestSession.setResponse(response);
            portHandler.sendMessage(portHandler.obtainMessage(73736, xcloudRequestSession));
            return;
        }
        Log.w("XCloudProtocolAgent.handleConfirmChargeStartedResponse", "failed to find handler for port: " + port);
    }

    private void handleConfirmChargeEndedResponse(XCloudMessage request, XCloudMessage response) {
        String port = response.getPort();
        XCloudPortHandler portHandler = getPortHandler(port);
        if (portHandler != null) {
            XCloudRequestSession xcloudRequestSession = new XCloudRequestSession();
            xcloudRequestSession.setRequest(request);
            xcloudRequestSession.setResponse(response);
            portHandler.sendMessage(portHandler.obtainMessage(73736, xcloudRequestSession));
            return;
        }
        Log.w("XCloudProtocolAgent.handleConfirmChargeEndedResponse", "failed to find handler for port: " + port);
    }

    private void handleRequestStartChargeResponse(XCloudMessage request, XCloudMessage response) {
        String port = response.getPort();
        XCloudPortHandler portHandler = getPortHandler(port);
        if (portHandler != null) {
            XCloudRequestSession xcloudRequestSession = new XCloudRequestSession();
            xcloudRequestSession.setRequest(request);
            xcloudRequestSession.setResponse(response);
            portHandler.sendMessage(portHandler.obtainMessage(73736, xcloudRequestSession));
            return;
        }
        Log.w("XCloudProtocolAgent.handleRequestStartChargeResponse", "failed to find handler for port: " + port);
    }

    private void handleRequestRefuseChargeResponse(XCloudMessage request, XCloudMessage response) {
        String port = response.getPort();
        XCloudPortHandler portHandler = getPortHandler(port);
        if (portHandler != null) {
            XCloudRequestSession xcloudRequestSession = new XCloudRequestSession();
            xcloudRequestSession.setRequest(request);
            xcloudRequestSession.setResponse(response);
            portHandler.sendMessage(portHandler.obtainMessage(73736, xcloudRequestSession));
            return;
        }
        Log.w("XCloudProtocolAgent.handleRequestRefuseChargeResponse", "failed to find handler for port: " + port);
    }

    private void handleCloudRequestMessage(XCloudMessage request) {
        String msgName = request.getMessageName();
        if (msgName.equals(XCloudMessage.RequestStartCharge)) {
            handleRequestStartCharge(request);
        } else if (msgName.equals(XCloudMessage.RequestRefuseCharge)) {
            handleRequestRefuseCharge(request);
        } else if (msgName.equals(XCloudMessage.RequestStopCharge)) {
            LogUtils.cloudlog("receive xcloud RequestStopCharge: " + request.getData());
            handleRequestStopCharge(request);
        } else if (msgName.equals(XCloudMessage.ApplySetting)) {
            LogUtils.cloudlog("receive xcloud ApplySetting request: " + request.getData());
            handleApplySettingRequest(request);
        } else if (msgName.equals(XCloudMessage.QuerySystemInfo)) {
            handleQuerySystemInfoRequest(request);
        } else if (msgName.equals(XCloudMessage.RequestAutoStop)) {
            LogUtils.cloudlog("receive xcloud RequestAutoStop: " + request.getData());
            handleRequestAutoStop(request);
        } else if (msgName.equals(XCloudMessage.CancelAutoStop)) {
            LogUtils.cloudlog("receive xcloud CancelAutoStop: " + request.getData());
            handleCancelAutoStop(request);
        } else if (msgName.equals(XCloudMessage.RequestAction)) {
            LogUtils.cloudlog("receive xcloud RequestAction: " + request.getData());
            handleRequestAction(request);
        } else if (msgName.equals(XCloudMessage.RequestUpgrade)) {
            LogUtils.cloudlog("receive xcloud RequestUpgrade: " + request.getData());
            handleRequestUpgrade(request);
        } else if (msgName.equals(XCloudMessage.QueryLog)) {
            handleQueryLog(request);
        } else if (msgName.equals(XCloudMessage.RequestVerification)) {
            LogUtils.cloudlog("receive xcloud RequestVerification: " + request.getData());
            handleRequestVerification(request);
        } else if (msgName.equals(XCloudMessage.QueryState)) {
            handleQueryState(request);
        } else if (msgName.equals(XCloudMessage.RequestUpdateStartTime)) {
            LogUtils.cloudlog("receive xcloud RequestUpdateStartTime: " + request.getData());
            handleRequestUpdateStartTime(request);
        } else if (msgName.equals(XCloudMessage.RequestEndCharge)) {
            LogUtils.cloudlog("receive xcloud RequestEndCharge: " + request.getData());
            handleRequestEndCharge(request);
        }
    }

    private void handleRequestStartCharge(XCloudMessage request) {
        RequestStartCharge requestStartCharge = (RequestStartCharge) request.getBody();
        String port = String.valueOf(requestStartCharge.getPort());
        request.setPort(port);
        request.setSessionId(String.valueOf(requestStartCharge.getSid()));
        XCloudPortHandler portHandler = getPortHandler(port);
        if (portHandler != null) {
            portHandler.sendMessage(portHandler.obtainMessage(73735, request));
        } else {
            Log.w("XCloudProtocolAgent.handleRequestStartCharge", "unsupported port in requestStartCharge msg: " + request.toJson());
        }
    }

    private void handleRequestRefuseCharge(XCloudMessage request) {
        RequestRefuseCharge requestRefuseCharge = (RequestRefuseCharge) request.getBody();
        if (requestRefuseCharge.getSid() != null) {
            Log.w("XCloudProtocolAgent.handleRequestRefuseCharge", "may be RequestChargeWithIDCard response timeout: " + request.toJson());
            return;
        }
        String port = String.valueOf(requestRefuseCharge.getPort());
        request.setPort(port);
        XCloudPortHandler portHandler = getPortHandler(port);
        if (portHandler != null) {
            portHandler.sendMessage(portHandler.obtainMessage(73735, request));
        } else {
            Log.w("XCloudProtocolAgent.handleRequestRefuseCharge", "unsupported port in handleRequestRefuseCharge msg: " + request.toJson());
        }
    }

    private void handleRequestAutoStop(XCloudMessage request) {
        RequestAutoStop requestAutoStop = (RequestAutoStop) request.getBody();
        String port = getPort(String.valueOf(requestAutoStop.getBillId()));
        if (!TextUtils.isEmpty(port)) {
            request.setPort(port);
            request.setSessionId(String.valueOf(requestAutoStop.getSid()));
            XCloudPortHandler portHandler = getPortHandler(port);
            portHandler.sendMessage(portHandler.obtainMessage(73735, request));
            return;
        }
        Log.w("XCloudProtocolAgent.handleRequestAutoStop", "failed to find related charge for request: " + request.toJson());
    }

    private void handleCancelAutoStop(XCloudMessage request) {
        CancelAutoStop cancelAutoStop = (CancelAutoStop) request.getBody();
        String port = getPort(String.valueOf(cancelAutoStop.getBillId()));
        if (!TextUtils.isEmpty(port)) {
            request.setPort(port);
            request.setSessionId(String.valueOf(cancelAutoStop.getSid()));
            XCloudPortHandler portHandler = getPortHandler(port);
            portHandler.sendMessage(portHandler.obtainMessage(73735, request));
            return;
        }
        Log.w("XCloudProtocolAgent.handleCancelAutoStop", "failed to find related charge for request: " + request.toJson());
    }

    private void handleRequestStopCharge(XCloudMessage request) {
        RequestStopCharge requestStopCharge = (RequestStopCharge) request.getBody();
        String port = getPort(String.valueOf(requestStopCharge.getBillId()));
        if (!TextUtils.isEmpty(port)) {
            request.setPort(port);
            request.setSessionId(String.valueOf(requestStopCharge.getSid()));
            XCloudPortHandler portHandler = getPortHandler(port);
            portHandler.sendMessage(portHandler.obtainMessage(73735, request));
            return;
        }
        Log.w("XCloudProtocolAgent.handleRequestStopCharge", "failed to find related charge for request: " + request.toJson());
    }

    private void handleRequestEndCharge(XCloudMessage request) {
        RequestEndCharge requestEndCharge = (RequestEndCharge) request.getBody();
        String port = getPort(String.valueOf(requestEndCharge.getBillId()));
        if (!TextUtils.isEmpty(port)) {
            request.setPort(port);
            request.setSessionId(String.valueOf(requestEndCharge.getSid()));
            XCloudPortHandler portHandler = getPortHandler(port);
            portHandler.sendMessage(portHandler.obtainMessage(73735, request));
            return;
        }
        Log.w("XCloudProtocolAgent.handleRequestEndCharge", "failed to find related charge for request: " + request.toJson());
    }

    private void handleQuerySystemInfoRequest(XCloudMessage request) {
        reportSystemInfo(((QuerySystemInfo) request.getBody()).getSid());
    }

    /* access modifiers changed from: private */
    public void sendSayHello() {
        long sessionId = genSid();
        SayHello sayHelloRequest = new SayHello();
        sayHelloRequest.setSid(Long.valueOf(sessionId));
        XCloudMessage request = new XCloudMessage();
        request.setMessageName(XCloudMessage.SayHello);
        request.setSrcId(this.f87sn);
        request.setBody(sayHelloRequest);
        request.setData(sayHelloRequest.toJson());
        request.setSessionId(String.valueOf(sessionId));
        this.handler.sendMessage(this.handler.obtainMessage(69637, request));
    }

    /* access modifiers changed from: private */
    public void reportSystemInfo(Long sid) {
        String sessionId = null;
        if (sid != null) {
            sessionId = String.valueOf(sid);
        }
        ReportSystemInfo rptSystemInfo = new ReportSystemInfo();
        rptSystemInfo.setSid(sid);
        rptSystemInfo.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        rptSystemInfo.setSetting(getDeviceSetting());
        rptSystemInfo.setCapabilities(getDeviceCapability());
        String version = "firmware:v" + SoftwareStatusCacheProvider.getInstance().getFirewareVer() + ";app:v" + SoftwareStatusCacheProvider.getInstance().getAppVer();
        PLATFORM_CUSTOMER platformCustomer = SystemSettingCacheProvider.getInstance().getPlatformCustomer();
        if (platformCustomer != null) {
            version = String.valueOf(version) + ";custom:" + platformCustomer.getCustomer();
        }
        rptSystemInfo.setVersion(version);
        String activeNet = HardwareStatusCacheProvider.getInstance().getActiveNetwork();
        if (Network.NETWORK_TYPE_MOBILE.equals(activeNet) || "2G".equals(activeNet) || "3G".equals(activeNet) || "4G".equals(activeNet)) {
            rptSystemInfo.setSimId(HardwareStatusCacheProvider.getInstance().getMobileNetStatus().getIMSI());
        }
        PortRuntimeData runtime = C2DeviceProxy.getInstance().getPortRuntimeInfo("1");
        if (runtime != null) {
            rptSystemInfo.setAmmeter(runtime.getEnergy());
        }
        XCloudMessage report = new XCloudMessage();
        report.setMessageName(XCloudMessage.ReportSystemInfo);
        report.setSrcId(this.f87sn);
        report.setBody(rptSystemInfo);
        report.setData(rptSystemInfo.toJson());
        report.setSessionId(sessionId);
        this.handler.sendMessage(this.handler.obtainMessage(69637, report));
    }

    private void handleQueryState(XCloudMessage request) {
        Wifi info;
        JSONObject runtime;
        Long sid = ((QueryState) request.getBody()).getSid();
        String sessionId = null;
        if (sid != null) {
            sessionId = String.valueOf(sid);
        }
        JSONObject workStatus = new JSONObject();
        try {
            String update = C2DeviceProxy.getInstance().getRawPortRuntimeInfo("1");
            if (!TextUtils.isEmpty(update) && (runtime = new JSONObject(update).getJSONObject("data")) != null) {
                workStatus.put("runtime", runtime);
            }
            List<XKeyseed> allNFCGroupKeySeed = NFCKeyContentProxy.getInstance().getAllKeyseed();
            if (allNFCGroupKeySeed != null && allNFCGroupKeySeed.size() > 0) {
                ArrayList<String> nfcGroup = new ArrayList<>();
                for (XKeyseed keyseed : allNFCGroupKeySeed) {
                    if (!nfcGroup.contains(keyseed.getGroup())) {
                        nfcGroup.add(keyseed.getGroup());
                    }
                }
                workStatus.put("nfcGroup", new JSONArray(JsonBean.listToJson(nfcGroup)));
            }
            String workMode = ChargeStatusCacheProvider.getInstance().getWorkMode().getMode();
            int localGunlockMode = LocalSettingCacheProvider.getInstance().getChargePortSetting("1").getGunLockSetting().getMode().getMode();
            int remoteGunlockMode = RemoteSettingCacheProvider.getInstance().getChargePortSetting("1").getGunLockSetting().getMode().getMode();
            String gunlockStatus = ChargeStatusCacheProvider.getInstance().getPortLockStatus("1").getStatus();
            int cpRange = RemoteSettingCacheProvider.getInstance().getChargeSetting().getCpRange();
            int voltRange = RemoteSettingCacheProvider.getInstance().getChargeSetting().getVoltageRange();
            Integer maxLeakage = RemoteSettingCacheProvider.getInstance().getChargeSetting().getLeakageTolerance();
            Boolean isEarthDisable = RemoteSettingCacheProvider.getInstance().getChargeSetting().isEarthDisable();
            workStatus.put("workMode", workMode);
            workStatus.put("localGunlockMode", localGunlockMode);
            workStatus.put("remoteGunlockMode", remoteGunlockMode);
            workStatus.put("gunlockStatus", gunlockStatus);
            workStatus.put("cpRange", cpRange);
            workStatus.put("voltRange", voltRange);
            workStatus.put("maxLeakage", maxLeakage);
            workStatus.put("isEarthDisable", isEarthDisable);
            String activeNetwork = HardwareStatusCacheProvider.getInstance().getActiveNetwork();
            if (Network.NETWORK_TYPE_MOBILE.equals(activeNetwork)) {
                MobileNet info2 = HardwareStatusCacheProvider.getInstance().getMobileNetStatus();
                if (info2 != null) {
                    workStatus.put(Network.NETWORK_TYPE_MOBILE, new JSONObject(info2.toJson()));
                }
            } else if (Network.NETWORK_TYPE_ETHERNET.equals(activeNetwork)) {
                Ethernet info3 = HardwareStatusCacheProvider.getInstance().getEthernetStatus();
                if (info3 != null) {
                    workStatus.put(Network.NETWORK_TYPE_ETHERNET, new JSONObject(info3.toJson()));
                }
            } else if (Network.NETWORK_TYPE_WIFI.equals(activeNetwork) && (info = HardwareStatusCacheProvider.getInstance().getWiFiStatus()) != null) {
                workStatus.put(Network.NETWORK_TYPE_WIFI, new JSONObject(info.toJson()));
            }
        } catch (Exception e) {
            Log.e("XCloudProtocolAgent.handleQueryState", "except: " + Log.getStackTraceString(e));
        }
        ReportState reportState = new ReportState();
        reportState.setSid(sid);
        reportState.setTime(Long.valueOf(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone())));
        reportState.setData(workStatus.toString());
        XCloudMessage report = new XCloudMessage();
        report.setMessageName(XCloudMessage.ReportState);
        report.setSrcId(this.f87sn);
        report.setBody(reportState);
        report.setData(reportState.toJson());
        report.setSessionId(sessionId);
        this.handler.sendMessage(this.handler.obtainMessage(69637, report));
    }

    private void handleQueryLog(XCloudMessage request) {
        LogUploadAgent.getInstance().upload((QueryLog) request.getBody());
    }

    private void handleRequestVerification(XCloudMessage request) {
        XCloudDCAPGateway.getInstance().sendMessage(XCloudDCAPGateway.getInstance().obtainMessage(77827, request));
    }

    private void handleRequestUpdateStartTime(XCloudMessage request) {
        RequestUpdateStartTime requestUpdateStartTime = (RequestUpdateStartTime) request.getBody();
        String port = getPort(String.valueOf(requestUpdateStartTime.getBillId()));
        if (!TextUtils.isEmpty(port)) {
            request.setPort(port);
            request.setSessionId(String.valueOf(requestUpdateStartTime.getSid()));
            XCloudPortHandler portHandler = getPortHandler(port);
            portHandler.sendMessage(portHandler.obtainMessage(73735, request));
            return;
        }
        Log.w("XCloudProtocolAgent.handleRequestUpdateStartTime", "failed to find related charge for request: " + request.toJson());
    }

    public void sendUploadLog(Long sid, DeviceError error) {
        UploadLog uploadLog = new UploadLog();
        uploadLog.setSid(sid);
        uploadLog.setError(error);
        uploadLog.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        XCloudMessage response = new XCloudMessage();
        response.setMessageName(XCloudMessage.UploadLog);
        response.setSrcId(this.f87sn);
        response.setBody(uploadLog);
        response.setData(uploadLog.toJson());
        response.setSessionId(String.valueOf(sid));
        this.handler.sendMessage(this.handler.obtainMessage(69637, response));
    }

    private void handleRequestAction(XCloudMessage request) {
        XCloudDCAPGateway.getInstance().sendMessage(XCloudDCAPGateway.getInstance().obtainMessage(77827, request));
    }

    private void handleRequestUpgrade(XCloudMessage request) {
        XCloudDCAPGateway.getInstance().sendMessage(XCloudDCAPGateway.getInstance().obtainMessage(77827, request));
    }

    private void handleApplySettingRequest(XCloudMessage request) {
        ApplySetting applySetting = (ApplySetting) request.getBody();
        String sessionId = String.valueOf(applySetting.getSid());
        ReportSettingResult reportSettingResult = new ReportSettingResult();
        reportSettingResult.setSid(applySetting.getSid());
        this.latestDeviceSettingError = null;
        setDeviceSetting(applySetting, applySetting.getSid());
        if (this.latestDeviceSettingError != null) {
            reportSettingResult.setError(new DeviceError((String) null, (String) null, this.latestDeviceSettingError));
        }
        reportSettingResult.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        XCloudMessage response = new XCloudMessage();
        response.setMessageName(XCloudMessage.ReportSettingResult);
        response.setSrcId(this.f87sn);
        response.setBody(reportSettingResult);
        response.setData(reportSettingResult.toJson());
        response.setSessionId(sessionId);
        this.handler.sendMessage(this.handler.obtainMessage(69637, response));
    }

    public void ReportContentDownloadResult(Long sid, ArrayList<String> fileUrls, DeviceError error) {
        ReportSettingResult reportSettingResult = new ReportSettingResult();
        reportSettingResult.setSid(sid);
        reportSettingResult.setFileUrls(fileUrls);
        reportSettingResult.setError(error);
        reportSettingResult.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        XCloudMessage response = new XCloudMessage();
        response.setMessageName(XCloudMessage.ReportSettingResult);
        response.setSrcId(this.f87sn);
        response.setBody(reportSettingResult);
        response.setData(reportSettingResult.toJson());
        this.handler.sendMessage(this.handler.obtainMessage(69637, response));
    }

    /* access modifiers changed from: private */
    public void handleSendMsgOk(XCloudMessage msg) {
        String port = msg.getPort();
        if (!TextUtils.isEmpty(port)) {
            XCloudPortHandler portHandler = getPortHandler(port);
            portHandler.sendMessage(portHandler.obtainMessage(73731, msg));
        }
    }

    /* access modifiers changed from: private */
    public void handleSendMsgFail(XCloudMessage msg) {
        String port = msg.getPort();
        if (!TextUtils.isEmpty(port)) {
            XCloudPortHandler portHandler = getPortHandler(port);
            portHandler.sendMessage(portHandler.obtainMessage(73732, msg));
            return;
        }
        handleMsgResend(msg);
    }

    private void handleRequestTimeout(XCloudMessage request) {
        String port = request.getPort();
        if (!TextUtils.isEmpty(port)) {
            XCloudPortHandler portHandler = getPortHandler(port);
            portHandler.sendMessage(portHandler.obtainMessage(73733, request));
            return;
        }
        handleMsgResend(request);
    }

    private void handleMsgResend(XCloudMessage msg) {
        if (XCloudMessage.RequestSetting.equals(msg.getMessageName())) {
            int interval = (msg.getResendCnt() + 1) * 10;
            if (interval > 60) {
                interval = 60;
            }
            this.handler.sendMessageDelayed(this.handler.obtainMessage(69649, msg), (long) (interval * 1000));
        }
    }

    /* access modifiers changed from: private */
    public void requestTimeoutCheck() {
        if (this.sendReqestState.size() > 0) {
            Iterator<Map.Entry<String, SendRequestState>> it = this.sendReqestState.entrySet().iterator();
            while (it.hasNext()) {
                SendRequestState requestState = it.next().getValue();
                XCLOUD_REQUEST_STATE state = requestState.status;
                long timestamp = requestState.timestamp;
                if (XCLOUD_REQUEST_STATE.sending.equals(state)) {
                    if (System.currentTimeMillis() - timestamp > 5000) {
                        it.remove();
                        handleSendMsgFail(requestState.request);
                    }
                } else if (XCLOUD_REQUEST_STATE.sended.equals(state) && System.currentTimeMillis() - timestamp > 10000) {
                    it.remove();
                    handleRequestTimeout(requestState.request);
                }
            }
        }
    }

    public void sendReportActionResult(XCloudMessage request, EnumActionStatus status, DeviceError error) {
        Long sid = null;
        String name = request.getMessageName();
        if (name.equals(XCloudMessage.RequestAction)) {
            sid = ((RequestAction) request.getBody()).getSid();
        } else if (name.equals(XCloudMessage.RequestUpgrade)) {
            sid = ((RequestUpgrade) request.getBody()).getSid();
        }
        reportActionResult(sid, status, error);
    }

    public void reportActionResult(Long sid, EnumActionStatus status, DeviceError error) {
        ReportActionResult rptActionResult = new ReportActionResult();
        rptActionResult.setSid(sid);
        rptActionResult.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        if (status != null) {
            rptActionResult.setStatus(status.getStatus());
        }
        rptActionResult.setError(error);
        XCloudMessage response = new XCloudMessage();
        response.setMessageName(XCloudMessage.ReportActionResult);
        response.setSrcId(this.f87sn);
        response.setBody(rptActionResult);
        response.setData(rptActionResult.toJson());
        if (sid != null) {
            response.setSessionId(String.valueOf(sid));
        }
        this.handler.sendMessage(this.handler.obtainMessage(69637, response));
    }

    public void sendReportChargeCancelled(XCloudMessage request, String chargeId, DeviceError error) {
        String port = null;
        if (request != null) {
            port = request.getPort();
        } else if (!TextUtils.isEmpty(chargeId)) {
            port = getPort(chargeId);
        }
        XCloudPortHandler xcloudPortHandler = getPortHandler(port);
        if (xcloudPortHandler == null) {
            Log.w("XCloudProtocolAgent.sendReportChargeCancelled", "no available port handler for port: " + port);
        } else {
            xcloudPortHandler.reportChargeCancelled(request, error);
        }
    }

    public void sendReportChargeStarted(String chargeId) {
        String port = getPort(chargeId);
        if (!TextUtils.isEmpty(port)) {
            XCloudPortHandler xcloudPortHandler = getPortHandler(port);
            if (xcloudPortHandler == null) {
                Log.w("XCloudProtocolAgent.sendReportChargeStarted", "no available port handler for port: " + port);
            } else {
                xcloudPortHandler.reportChargeStarted(chargeId);
            }
        } else {
            Log.w("XCloudProtocolAgent.sendReportChargeStarted", "failed to find related port for charge: " + chargeId);
        }
    }

    public void sendReportAutoStopResult(XCloudMessage request, DeviceError error) {
        String port = request.getPort();
        XCloudPortHandler xcloudPortHandler = getPortHandler(port);
        if (xcloudPortHandler == null) {
            Log.w("XCloudProtocolAgent.sendReportAutoStopResult", "no available port handler for port: " + port);
        } else {
            xcloudPortHandler.reportAutoStopResult(request, error);
        }
    }

    public void sendReportVerification(XCloudMessage request) {
        ReportVerification reportVerification = new ReportVerification();
        reportVerification.setSid(((RequestVerification) request.getBody()).getSid());
        reportVerification.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        XCloudMessage report = new XCloudMessage();
        report.setMessageName(XCloudMessage.ReportVerification);
        report.setSrcId(this.f87sn);
        report.setBody(reportVerification);
        report.setData(reportVerification.toJson());
        report.setSessionId(String.valueOf(reportVerification.getSid()));
        sendMessage(report);
    }

    public void sendReportChargeStopped(XCloudMessage request, DeviceError error) {
        String port = request.getPort();
        XCloudPortHandler xcloudPortHandler = getPortHandler(port);
        if (xcloudPortHandler == null) {
            Log.w("XCloudProtocolAgent.sendReportChargeStopped", "no available port handler for port: " + port);
        } else {
            xcloudPortHandler.reportChargeStopped(request, error);
        }
    }

    public void sendReportChargePaused(String chargeId, DeviceError error, long time) {
        String port = getPort(chargeId);
        if (!TextUtils.isEmpty(port)) {
            XCloudPortHandler xcloudPortHandler = getPortHandler(port);
            if (xcloudPortHandler == null) {
                Log.w("XCloudProtocolAgent.sendReportChargePaused", "no available port handler for port: " + port);
            } else {
                xcloudPortHandler.reportChargePaused(chargeId, error, time);
            }
        } else {
            Log.w("XCloudProtocolAgent.sendReportChargePaused", "failed to find related port for charge: " + chargeId);
        }
    }

    public void sendReportChargeResumed(String chargeId, DeviceError error, long time) {
        String port = getPort(chargeId);
        if (!TextUtils.isEmpty(port)) {
            XCloudPortHandler xcloudPortHandler = getPortHandler(port);
            if (xcloudPortHandler == null) {
                Log.w("XCloudProtocolAgent.sendReportChargeResumed", "no available port handler for port: " + port);
            } else {
                xcloudPortHandler.reportChargeResumed(chargeId, error, time);
            }
        } else {
            Log.w("XCloudProtocolAgent.sendReportChargeResumed", "failed to find related port for charge: " + chargeId);
        }
    }

    public void sendReportDelayCountStarted(String chargeId) {
        String port = getPort(chargeId);
        if (!TextUtils.isEmpty(port)) {
            XCloudPortHandler xcloudPortHandler = getPortHandler(port);
            if (xcloudPortHandler == null) {
                Log.w("XCloudProtocolAgent.sendReportDelayCountStarted", "no available port handler for port: " + port);
            } else {
                xcloudPortHandler.reportDelayCountStarted(chargeId);
            }
        } else {
            Log.w("XCloudProtocolAgent.sendReportDelayCountStarted", "failed to find related port for charge: " + chargeId);
        }
    }

    public void sendReportDelayFeeStarted(String chargeId, long delayStart) {
        String port = getPort(chargeId);
        if (!TextUtils.isEmpty(port)) {
            XCloudPortHandler xcloudPortHandler = getPortHandler(port);
            if (xcloudPortHandler == null) {
                Log.w("XCloudProtocolAgent.sendReportDelayFeeStarted", "no available port handler for port: " + port);
            } else {
                xcloudPortHandler.reportDelayFeeStarted(chargeId, delayStart);
            }
        } else {
            Log.w("XCloudProtocolAgent.sendReportDelayFeeStarted", "failed to find related port for charge: " + chargeId);
        }
    }

    public XCloudMessage sendRequestChargeWithIDCard(String port, String cardNo, String timestamp, String nonce, String signature) {
        XCloudPortHandler xcloudPortHandler = getPortHandler(port);
        if (xcloudPortHandler != null) {
            return xcloudPortHandler.requestChargeWithIDCard(cardNo, timestamp, nonce, signature);
        }
        Log.w("XCloudProtocolAgent.sendRequestChargeWithIDCard", "no available port handler for port: " + port);
        return null;
    }

    public void onDisconnected() {
        this.handler.sendEmptyMessage(69635);
        if (HardwareStatusCacheProvider.getInstance().isNetworkConnected()) {
            long nowCnt = this.networkDiagnosisCnt.getAndIncrement();
            if (nowCnt >= 0 && nowCnt < 3) {
                LogUtils.applog("xcloud connection lost, try to diagnosis network connectivity ...");
                new Thread(new Runnable() {
                    public void run() {
                        DCAPProxy.getInstance().networkConnectivityDiagnosis();
                    }
                }).start();
            }
        }
    }

    public void onMessage(String messageName, String version, String srcId, String data) {
        XCloudMessage msg = parseXCloudMessage(messageName, version, srcId, data);
        if (msg != null) {
            this.handler.sendMessage(this.handler.obtainMessage(69640, msg));
        }
    }

    private XCloudMessage parseXCloudMessage(String messageName, String version, String srcId, String data) {
        XCloudMessage msg = new XCloudMessage();
        msg.setMessageName(messageName);
        msg.setVersion(version);
        msg.setSrcId(srcId);
        Object decodedBody = null;
        try {
            if (messageName.equals(XCloudMessage.SendChargeQRCode)) {
                decodedBody = new SendChargeQRCode().fromJson(data);
                msg.setSessionId(String.valueOf(((SendChargeQRCode) decodedBody).getSid()));
            } else if (messageName.equals(XCloudMessage.RequestStartCharge)) {
                decodedBody = new RequestStartCharge().fromJson(data);
                msg.setSessionId(String.valueOf(((RequestStartCharge) decodedBody).getSid()));
            } else if (messageName.equals(XCloudMessage.RequestRefuseCharge)) {
                decodedBody = new RequestRefuseCharge().fromJson(data);
                msg.setSessionId(String.valueOf(((RequestRefuseCharge) decodedBody).getSid()));
            } else if (messageName.equals(XCloudMessage.RequestUpgrade)) {
                decodedBody = new RequestUpgrade().fromJson(data);
                msg.setSessionId(String.valueOf(((RequestUpgrade) decodedBody).getSid()));
            } else if (messageName.equals(XCloudMessage.RequestAutoStop)) {
                decodedBody = new RequestAutoStop().fromJson(data);
                msg.setSessionId(String.valueOf(((RequestAutoStop) decodedBody).getSid()));
            } else if (messageName.equals(XCloudMessage.CancelAutoStop)) {
                decodedBody = new CancelAutoStop().fromJson(data);
                msg.setSessionId(String.valueOf(((CancelAutoStop) decodedBody).getSid()));
            } else if (messageName.equals(XCloudMessage.ConfirmChargeEnded)) {
                decodedBody = new ConfirmChargeEnded().fromJson(data);
                msg.setSessionId(String.valueOf(((ConfirmChargeEnded) decodedBody).getSid()));
            } else if (messageName.equals(XCloudMessage.QueryLog)) {
                decodedBody = new QueryLog().fromJson(data);
                msg.setSessionId(String.valueOf(((QueryLog) decodedBody).getSid()));
            } else if (messageName.equals(XCloudMessage.RequestAction)) {
                decodedBody = new RequestAction().fromJson(data);
                msg.setSessionId(String.valueOf(((RequestAction) decodedBody).getSid()));
            } else if (messageName.equals(XCloudMessage.ConfirmChargeStarted)) {
                decodedBody = new ConfirmChargeStarted().fromJson(data);
                msg.setSessionId(String.valueOf(((ConfirmChargeStarted) decodedBody).getSid()));
            } else if (messageName.equals(XCloudMessage.ApplySetting)) {
                decodedBody = new ApplySetting().fromJson(data);
                msg.setSessionId(String.valueOf(((ApplySetting) decodedBody).getSid()));
            } else if (messageName.equals(XCloudMessage.RequestStopCharge)) {
                decodedBody = new RequestStopCharge().fromJson(data);
                msg.setSessionId(String.valueOf(((RequestStopCharge) decodedBody).getSid()));
            } else if (messageName.equals(XCloudMessage.QuerySystemInfo)) {
                decodedBody = new QuerySystemInfo().fromJson(data);
                msg.setSessionId(String.valueOf(((QuerySystemInfo) decodedBody).getSid()));
            } else if (messageName.equals(XCloudMessage.ConfirmLocalChargeBill)) {
                decodedBody = new ConfirmLocalChargeBill().fromJson(data);
                msg.setSessionId(String.valueOf(((ConfirmLocalChargeBill) decodedBody).getSid()));
            } else if (messageName.equals(XCloudMessage.AnswerHello)) {
                decodedBody = new AnswerHello().fromJson(data);
                msg.setSessionId(String.valueOf(((AnswerHello) decodedBody).getSid()));
            } else if (messageName.equals(XCloudMessage.RequestVerification)) {
                decodedBody = new RequestVerification().fromJson(data);
                msg.setSessionId(String.valueOf(((RequestVerification) decodedBody).getSid()));
            } else if (messageName.equals(XCloudMessage.QueryState)) {
                decodedBody = new QueryState().fromJson(data);
                msg.setSessionId(String.valueOf(((QueryState) decodedBody).getSid()));
            } else if (messageName.equals(XCloudMessage.RequestSetting)) {
                decodedBody = new RequestSetting().fromJson(data);
                msg.setSessionId(String.valueOf(((RequestSetting) decodedBody).getSid()));
            } else if (messageName.equals(XCloudMessage.RequestUpdateStartTime)) {
                decodedBody = new RequestUpdateStartTime().fromJson(data);
                msg.setSessionId(String.valueOf(((RequestUpdateStartTime) decodedBody).getSid()));
            } else if (messageName.equals(XCloudMessage.RequestEndCharge)) {
                decodedBody = new RequestEndCharge().fromJson(data);
                msg.setSessionId(String.valueOf(((RequestEndCharge) decodedBody).getSid()));
            } else {
                Log.w("XCloudProtocolAgent.parseXCloudMessage", "receive unsupported msg, name: " + messageName + ", version: " + version + ", body: " + data);
            }
        } catch (Exception e) {
            Log.w("XCloudProtocolAgent.parseXCloudMessage", Log.getStackTraceString(e));
        }
        if (decodedBody == null) {
            return null;
        }
        msg.setBody(decodedBody);
        msg.setData(data);
        return msg;
    }

    public void handleUpdateQrcodeRequest(String port) {
        XCloudPortHandler portHandler = getPortHandler(port);
        if (portHandler != null) {
            portHandler.sendMessage(portHandler.obtainMessage(73737));
        } else {
            Log.w("XCloudProtocolAgent.handleUpdateQrcodeRequest", "no available port handler for port: " + port);
        }
    }

    private DeviceCapability getDeviceCapability() {
        DeviceCapability deviceCapability = new DeviceCapability();
        deviceCapability.setCurrentType("ac");
        PHASE phase = HardwareStatusCacheProvider.getInstance().getPhase();
        if (phase != null) {
            if (PHASE.SINGLE_PHASE.getPhase() == phase.getPhase()) {
                deviceCapability.setPhases(1);
            } else if (PHASE.THREE_PHASE.getPhase() == phase.getPhase()) {
                deviceCapability.setPhases(3);
            }
        }
        deviceCapability.setMaxCurrent(Double.valueOf(((double) ChargeStatusCacheProvider.getInstance().getAmpCapacity()) * 1.0d));
        deviceCapability.setScreen(DeviceCapability.TYPE_SCREEN_C2);
        HashMap<String, Port> ports = HardwareStatusCacheProvider.getInstance().getPorts();
        if (ports != null) {
            deviceCapability.setPorts(Integer.valueOf(ports.size()));
        }
        return deviceCapability;
    }

    private DeviceSetting getDeviceSetting() {
        HashMap<String, FeeRate> portFeeRates;
        DeviceSetting deviceSetting = new DeviceSetting();
        PortFeeRate portFeeRate = RemoteSettingCacheProvider.getInstance().getPortFeeRate("1");
        if (!(portFeeRate == null || (portFeeRates = portFeeRate.getFeeRates()) == null || portFeeRates.size() <= 0)) {
            ArrayList<FeePolicy> feePolicys = new ArrayList<>();
            for (FeeRate feeRate : portFeeRates.values()) {
                feePolicys.add(SettingUtils.feeRate2FeePolicy(feeRate));
            }
            deviceSetting.setFeePolicy(feePolicys);
            if (!TextUtils.isEmpty(portFeeRate.getActiveFeeRateId())) {
                deviceSetting.setDefaultFeePolicy(Long.valueOf(Long.parseLong(portFeeRate.getActiveFeeRateId())));
            }
        }
        HashMap<String, PortSetting> portsSetting = RemoteSettingCacheProvider.getInstance().getChargePortsSetting();
        if (portsSetting != null && portsSetting.size() > 0) {
            HashMap<String, XCloudPort> ports = new HashMap<>();
            for (Map.Entry<String, PortSetting> entry : portsSetting.entrySet()) {
                String port = entry.getKey();
                ports.put(port, SettingUtils.getXCloudPortInfo(port, entry.getValue()));
            }
            deviceSetting.setPorts(ports);
        }
        deviceSetting.setContent(SettingUtils.getDeviceContent());
        deviceSetting.setQrcodeChars(RemoteSettingCacheProvider.getInstance().getUIDeviceCode());
        deviceSetting.setDefaultLightColor(RemoteSettingCacheProvider.getInstance().getDefaultBLNColor());
        deviceSetting.setIntervalCancelCharge(Integer.valueOf(RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalChargeCancel()));
        deviceSetting.setIntervalChargeReport(Integer.valueOf(RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalChargeReport()));
        deviceSetting.setIntervalStandby(Integer.valueOf(RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalStandby()));
        deviceSetting.setIntervalStartDelayFee(Integer.valueOf(RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalDelayStart()));
        deviceSetting.setPowerFactor(Double.valueOf(RemoteSettingCacheProvider.getInstance().getChargeSetting().getPowerFactor()));
        deviceSetting.setCpErrorRange(Integer.valueOf(RemoteSettingCacheProvider.getInstance().getChargeSetting().getCpRange()));
        deviceSetting.setvErrorRange(Integer.valueOf(RemoteSettingCacheProvider.getInstance().getChargeSetting().getVoltageRange()));
        deviceSetting.setRcErrorThreshold(RemoteSettingCacheProvider.getInstance().getChargeSetting().getLeakageTolerance());
        deviceSetting.setEarthDisabled(RemoteSettingCacheProvider.getInstance().getChargeSetting().isEarthDisable());
        XCloudRadarSetting radar = new XCloudRadarSetting();
        radar.setEnabled(RemoteSettingCacheProvider.getInstance().getChargePortSetting("1").getRadarSetting().isEnable());
        deviceSetting.setRadar(radar);
        return deviceSetting;
    }

    public DeviceSetting getLatestDeviceSettingError() {
        if (this.latestDeviceSettingError == null) {
            this.latestDeviceSettingError = new DeviceSetting();
        }
        return this.latestDeviceSettingError;
    }

    private boolean verifyTimezone(String tz) {
        return TimeUtils.getTimezoneOffset(tz) != null;
    }

    private boolean verifyLang(String lang) {
        if ("en".equals(lang) || "zh".equals(lang) || "de".equals(lang)) {
            return true;
        }
        return false;
    }

    private boolean isOnlyPropInBean(Object o, String prop) {
        try {
            JSONObject json = new JSONObject(JsonBean.getGsonBuilder().create().toJson(o));
            JSONArray allProps = json.names();
            Log.d("XCloudProtocolAgent.isOnlyPropInBean", json.toString());
            Log.d("XCloudProtocolAgent.isOnlyPropInBean", allProps.toString());
            if (allProps.length() != 3 || !json.has(prop) || !json.has("sid") || !json.has(ChargeStopCondition.TYPE_TIME)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.e("XCloudProtocolAgent.isOnlyPropInBean", Log.getStackTraceString(e));
        }
    }

    private boolean setDeviceSetting(DeviceSetting deviceSetting, Long sid) {
        JSONObject params;
        Set<String> portNos = HardwareStatusCacheProvider.getInstance().getPorts().keySet();
        Object anyOptions = deviceSetting.getAnyOptions();
        if (anyOptions != null) {
            try {
                JSONObject jSONObject = new JSONObject(JsonBean.ObjectToJson(anyOptions));
                if (jSONObject.has("isPlug2Charge")) {
                    SystemSettingCacheProvider.getInstance().setPlug2Charge(jSONObject.getBoolean("isPlug2Charge"));
                    SystemSettingCacheProvider.getInstance().persist();
                }
                if (jSONObject.has("isMonitor")) {
                    SystemSettingCacheProvider.getInstance().setYZXMonitor(jSONObject.getBoolean("isMonitor"));
                    SystemSettingCacheProvider.getInstance().persist();
                }
                if (jSONObject.has("customer")) {
                    PLATFORM_CUSTOMER platformCustomer = null;
                    String customer = jSONObject.getString("customer");
                    if (!TextUtils.isEmpty(customer)) {
                        platformCustomer = PLATFORM_CUSTOMER.valueOf(customer);
                    }
                    SystemSettingCacheProvider.getInstance().updatetPlatformCustomer(platformCustomer);
                    SystemSettingCacheProvider.getInstance().persist();
                }
                if (jSONObject.has("isWWlanPoll")) {
                    boolean isWWlanPoll = jSONObject.getBoolean("isWWlanPoll");
                    SystemSettingCacheProvider.getInstance().setWWlanPolling(Boolean.valueOf(isWWlanPoll));
                    SystemSettingCacheProvider.getInstance().persist();
                    C2DeviceProxy.getInstance().switchWWlanPoll(isWWlanPoll);
                }
                if (jSONObject.has("isCPWait")) {
                    boolean isCPWait = jSONObject.getBoolean("isCPWait");
                    SystemSettingCacheProvider.getInstance().setCPWait(Boolean.valueOf(isCPWait));
                    SystemSettingCacheProvider.getInstance().persist();
                    C2DeviceProxy.getInstance().switchCPWait(isCPWait);
                }
                if (jSONObject.has("uiBgColor")) {
                    String uiBgColor = jSONObject.getString("uiBgColor");
                    if (TextUtils.isEmpty(uiBgColor)) {
                        uiBgColor = null;
                    }
                    SystemSettingCacheProvider.getInstance().setUiBackgroundColor(uiBgColor);
                    SystemSettingCacheProvider.getInstance().persist();
                }
                if (isOnlyPropInBean(deviceSetting, "anyOptions")) {
                    if (jSONObject.has("deleteDb")) {
                        if (jSONObject.getBoolean("deleteDb")) {
                            Log.i("XCloudProtocolAgent.setDeviceSetting", "delete database, ret: " + FileUtils.execShell("rm -rf /data/data/com.xcharge.charger/databases/content.db*"));
                        }
                    } else if (jSONObject.has("queryBillLog")) {
                        String uploadUrl = null;
                        String billId = jSONObject.optString("queryBillLog");
                        if (TextUtils.isEmpty(billId) && (params = jSONObject.optJSONObject("queryBillLog")) != null && params.length() > 0) {
                            billId = params.optString("id");
                            uploadUrl = params.optString("url");
                        }
                        if (!TextUtils.isEmpty(billId)) {
                            LogUploadAgent.getInstance().uploadBillLog(billId, uploadUrl);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("XCloudProtocolAgent.setDeviceSetting", Log.getStackTraceString(e));
            }
        }
        LocaleOption localeOption = deviceSetting.getLocale();
        if (localeOption != null) {
            LocaleOption errorLocaleOption = null;
            String protocolZone = localeOption.getTimezone();
            if (!TextUtils.isEmpty(protocolZone)) {
                if (!verifyTimezone(protocolZone)) {
                    if (0 == 0) {
                        errorLocaleOption = new LocaleOption();
                    }
                    errorLocaleOption.setTimezone(protocolZone);
                } else {
                    RemoteSettingCacheProvider.getInstance().updateProtocolTimezone(protocolZone);
                }
            }
            String localZone = localeOption.getTimezoneDisp();
            if (!TextUtils.isEmpty(localZone) && !verifyTimezone(localZone)) {
                if (errorLocaleOption == null) {
                    errorLocaleOption = new LocaleOption();
                }
                errorLocaleOption.setTimezoneDisp(localZone);
            }
            String localLang = localeOption.getLang();
            if (!TextUtils.isEmpty(localLang) && !verifyLang(localLang)) {
                if (errorLocaleOption == null) {
                    errorLocaleOption = new LocaleOption();
                }
                errorLocaleOption.setLang(localLang);
            }
            String localCurrency = localeOption.getCurrency();
            Boolean useDST = localeOption.getUseDST();
            if (errorLocaleOption == null || (errorLocaleOption.getTimezoneDisp() == null && errorLocaleOption.getLang() == null && errorLocaleOption.getCurrency() == null)) {
                CountrySetting nowCountrySetting = RemoteSettingCacheProvider.getInstance().getCountrySetting().clone();
                HashMap<String, Object> localeSetting = new HashMap<>();
                if (!TextUtils.isEmpty(localZone) && !localZone.equals(nowCountrySetting.getZone())) {
                    localeSetting.put("zone", localZone);
                    nowCountrySetting.setZone(localZone);
                }
                if (!(useDST == null || useDST.booleanValue() == nowCountrySetting.isUseDaylightTime())) {
                    localeSetting.put("dst", useDST);
                    nowCountrySetting.setUseDaylightTime(useDST.booleanValue());
                }
                if (!TextUtils.isEmpty(localLang) && !localLang.equals(nowCountrySetting.getLang())) {
                    localeSetting.put("lang", localLang);
                    nowCountrySetting.setLang(localLang);
                    if ("zh".equals(localLang)) {
                        nowCountrySetting.setMoneyDisp("元");
                    } else if ("en".equals(localLang) || "de".equals(localLang)) {
                        if ("EUR".equalsIgnoreCase(nowCountrySetting.getMoney())) {
                            nowCountrySetting.setMoneyDisp("Euro");
                        } else {
                            nowCountrySetting.setMoneyDisp(nowCountrySetting.getMoney());
                        }
                    }
                    localeSetting.put("moneyDisp", nowCountrySetting.getMoneyDisp());
                }
                if (!TextUtils.isEmpty(localCurrency) && !localCurrency.equals(nowCountrySetting.getMoney())) {
                    localeSetting.put("money", localCurrency);
                    nowCountrySetting.setMoney(localCurrency);
                    nowCountrySetting.setMoneyDisp(localCurrency);
                    if ("zh".equals(nowCountrySetting.getLang())) {
                        nowCountrySetting.setMoneyDisp("元");
                    } else if (("en".equals(nowCountrySetting.getLang()) || "de".equals(localLang)) && "EUR".equalsIgnoreCase(nowCountrySetting.getMoney())) {
                        nowCountrySetting.setMoneyDisp("Euro");
                    }
                    localeSetting.put("moneyDisp", nowCountrySetting.getMoneyDisp());
                }
                if (localeSetting.size() > 0) {
                    RemoteSettingCacheProvider.getInstance().updateCountrySetting(nowCountrySetting);
                    SettingUtils.setDCAPRequest(SetDirective.SET_ID_DEVICE_LOCALE, localeSetting);
                }
            }
            if (errorLocaleOption != null) {
                getLatestDeviceSettingError().setLocale(errorLocaleOption);
            }
        }
        SettingUtils.setFeePolicy(deviceSetting);
        if (deviceSetting.getPorts() != null) {
            SettingUtils.setPorts(deviceSetting.getPorts());
        }
        if (!TextUtils.isEmpty(deviceSetting.getDefaultLightColor())) {
            RemoteSettingCacheProvider.getInstance().updateDefaultBLNColor(deviceSetting.getDefaultLightColor());
        }
        if (deviceSetting.getIntervalCancelCharge() != null) {
            if (deviceSetting.getIntervalCancelCharge().intValue() > 0) {
                RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().setIntervalChargeCancel(deviceSetting.getIntervalCancelCharge().intValue());
            } else {
                getLatestDeviceSettingError().setIntervalCancelCharge(deviceSetting.getIntervalCancelCharge());
            }
        }
        if (deviceSetting.getIntervalChargeReport() != null) {
            if (deviceSetting.getIntervalChargeReport().intValue() > 0) {
                RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().setIntervalChargeReport(deviceSetting.getIntervalChargeReport().intValue());
            } else {
                getLatestDeviceSettingError().setIntervalChargeReport(deviceSetting.getIntervalChargeReport());
            }
        }
        if (deviceSetting.getIntervalStandby() != null) {
            if (deviceSetting.getIntervalStandby().intValue() > 0) {
                RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().setIntervalStandby(deviceSetting.getIntervalStandby().intValue());
            } else {
                getLatestDeviceSettingError().setIntervalStandby(deviceSetting.getIntervalStandby());
            }
        }
        if (deviceSetting.getIntervalStartDelayFee() != null) {
            if (deviceSetting.getIntervalStartDelayFee().intValue() > 0) {
                RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().setIntervalDelayStart(deviceSetting.getIntervalStartDelayFee().intValue());
            } else {
                getLatestDeviceSettingError().setIntervalStartDelayFee(deviceSetting.getIntervalStartDelayFee());
            }
        }
        if (deviceSetting.getCpErrorRange() != null) {
            if (deviceSetting.getCpErrorRange().intValue() < 0 || deviceSetting.getCpErrorRange().intValue() > 100) {
                getLatestDeviceSettingError().setCpErrorRange(deviceSetting.getCpErrorRange());
            } else {
                HashMap<String, Object> values = new HashMap<>();
                values.put("value", String.valueOf(deviceSetting.getCpErrorRange()));
                SettingUtils.setDCAPRequest(SetDirective.SET_ID_DEVICE_CP_RANGE, values);
                RemoteSettingCacheProvider.getInstance().getChargeSetting().setCpRange(deviceSetting.getCpErrorRange().intValue());
                LocalSettingCacheProvider.getInstance().getChargeSetting().setCpRange(deviceSetting.getCpErrorRange().intValue());
                LocalSettingCacheProvider.getInstance().persist();
            }
        }
        if (deviceSetting.getvErrorRange() != null) {
            if (deviceSetting.getvErrorRange().intValue() < 0 || deviceSetting.getvErrorRange().intValue() > 100) {
                getLatestDeviceSettingError().setvErrorRange(deviceSetting.getvErrorRange());
            } else {
                HashMap<String, Object> values2 = new HashMap<>();
                values2.put("value", String.valueOf(deviceSetting.getvErrorRange()));
                SettingUtils.setDCAPRequest(SetDirective.SET_ID_DEVICE_VOLT_RANGE, values2);
                RemoteSettingCacheProvider.getInstance().getChargeSetting().setVoltageRange(deviceSetting.getvErrorRange().intValue());
                LocalSettingCacheProvider.getInstance().getChargeSetting().setVoltageRange(deviceSetting.getvErrorRange().intValue());
                LocalSettingCacheProvider.getInstance().persist();
            }
        }
        if (deviceSetting.getRcErrorThreshold() != null) {
            if (deviceSetting.getRcErrorThreshold().intValue() >= 0) {
                HashMap<String, Object> values3 = new HashMap<>();
                values3.put("value", String.valueOf(deviceSetting.getRcErrorThreshold()));
                SettingUtils.setDCAPRequest(SetDirective.SET_ID_DEVICE_LEAKAGE_TOLERANCE, values3);
                RemoteSettingCacheProvider.getInstance().getChargeSetting().setLeakageTolerance(deviceSetting.getRcErrorThreshold());
                LocalSettingCacheProvider.getInstance().getChargeSetting().setLeakageTolerance(deviceSetting.getRcErrorThreshold());
                LocalSettingCacheProvider.getInstance().persist();
            } else {
                getLatestDeviceSettingError().setRcErrorThreshold(deviceSetting.getRcErrorThreshold());
            }
        }
        if (deviceSetting.getEarthDisabled() != null) {
            HashMap<String, Object> values4 = new HashMap<>();
            values4.put("value", deviceSetting.getEarthDisabled().booleanValue() ? "disable" : "enable");
            SettingUtils.setDCAPRequest(SetDirective.SET_ID_DEVICE_EARTH_DISABLE, values4);
            RemoteSettingCacheProvider.getInstance().getChargeSetting().setEarthDisable(deviceSetting.getEarthDisabled());
            LocalSettingCacheProvider.getInstance().getChargeSetting().setEarthDisable(deviceSetting.getEarthDisabled());
            LocalSettingCacheProvider.getInstance().persist();
        }
        if (deviceSetting.getPowerFactor() != null) {
            if (deviceSetting.getPowerFactor().doubleValue() > 0.0d) {
                RemoteSettingCacheProvider.getInstance().getChargeSetting().setPowerFactor(deviceSetting.getPowerFactor().doubleValue());
            } else {
                getLatestDeviceSettingError().setPowerFactor(deviceSetting.getPowerFactor());
            }
        }
        XCloudRadarSetting radar = deviceSetting.getRadar();
        if (radar != null) {
            for (String port : portNos) {
                PortSetting portSetting = RemoteSettingCacheProvider.getInstance().getChargePortSetting(port);
                if (portSetting == null) {
                    portSetting = new PortSetting();
                }
                RadarSetting radarSetting = new RadarSetting();
                radarSetting.setEnable(radar.isEnabled());
                portSetting.setRadarSetting(radarSetting);
                RemoteSettingCacheProvider.getInstance().updateChargePortSetting(port, portSetting);
                HardwareStatusCacheProvider.getInstance().updatePortRadarSwitch(port, radar.isEnabled());
            }
        }
        ArrayList<NFCGroupSeed> nfcGroupSeedList = deviceSetting.getNfcGroupSeed();
        if (nfcGroupSeedList != null) {
            Iterator<NFCGroupSeed> it = nfcGroupSeedList.iterator();
            while (it.hasNext()) {
                NFCGroupSeed nfcGroupSeed = it.next();
                Long id = nfcGroupSeed.getId();
                if (id != null) {
                    String groupId = String.format("%06d", new Object[]{id});
                    String M2KeySeed = nfcGroupSeed.getSeedM1();
                    if (!TextUtils.isEmpty(M2KeySeed)) {
                        NFCKeyContentProxy.getInstance().saveKeyseed(groupId, M2KeySeed.toUpperCase(), NFC_CARD_TYPE.M2.getType());
                    }
                }
            }
        }
        String deviceCode = deviceSetting.getQrcodeChars();
        if (!TextUtils.isEmpty(deviceCode)) {
            RemoteSettingCacheProvider.getInstance().updateUIDeviceCode(deviceCode);
        }
        if (deviceSetting.getContent() != null) {
            SettingUtils.setDeviceContent(this.context, deviceSetting.getContent(), sid);
        }
        Log.d("XCloudProtocolAgent.setDeviceSetting", "setted: " + RemoteSettingCacheProvider.getInstance().getRemoteSetting().toJson());
        RemoteSettingCacheProvider.getInstance().persist();
        return true;
    }

    public void handleFinConfirm(String chargeId, FIN_MODE mode, ErrorCode error) {
        String port = getPort(chargeId);
        if (!TextUtils.isEmpty(port)) {
            XCloudPortHandler portHandler = getPortHandler(port);
            FinCause cause = new FinCause();
            cause.setMode(mode);
            cause.setError(error);
            portHandler.sendMessage(portHandler.obtainMessage(73747, cause));
            return;
        }
        Log.w("XCloudProtocolAgent.handleFinConfirm", "failed to find related port for charge: " + chargeId);
    }

    /* access modifiers changed from: private */
    public void portsActive() {
        for (XCloudPortHandler portHandler : this.portHandlers.values()) {
            portHandler.sendMessage(portHandler.obtainMessage(73729));
        }
    }

    /* access modifiers changed from: private */
    public void portsDeactive() {
        for (XCloudPortHandler portHandler : this.portHandlers.values()) {
            portHandler.sendMessage(portHandler.obtainMessage(73730));
        }
    }

    public long genSid() {
        long sid = this.sidGen.incrementAndGet();
        if (sid > 65535) {
            return 20000;
        }
        return sid;
    }

    private void testVerification() {
        RequestVerification requestVerification = new RequestVerification();
        requestVerification.setCustomer("深圳智充");
        requestVerification.setExpireInterval(600);
        requestVerification.setSid(123456L);
        requestVerification.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        XCloudMessage request = new XCloudMessage();
        request.setMessageName(XCloudMessage.RequestVerification);
        request.setSrcId(this.f87sn);
        request.setBody(requestVerification);
        request.setData(requestVerification.toJson());
        request.setSessionId(String.valueOf(123456));
        this.handler.sendMessage(this.handler.obtainMessage(69640, request));
    }

    private void testFeePolicySetting() {
        ApplySetting applySetting = (ApplySetting) new ApplySetting().fromJson("{\"defaultFeePolicy\":758233743713505280,\"feePolicy\":[{\"id\":758233743713505280,\"timedPrice\":[[0,700,38,80,0,0],[700,800,90,80,0,0],[800,1000,90,80,10,0],[1000,1200,145,80,10,0],[1200,1500,145,80,10,0],[1500,1800,90,80,10,0],[1800,1900,145,80,10,0],[1900,2000,145,80,10,0],[2000,2100,145,80,10,0],[2100,2300,90,80,0,0],[2300,2400,38,80,0,0]]},{\"id\":758233743713505282,\"timedPrice\":[[0,700,72,0,0,0],[700,1000,105,0,0,0],[1000,1500,138,0,0,0],[1500,1800,105,0,0,0],[1800,2100,138,0,0,0],[2100,2300,105,0,0,0],[2300,2400,72,0,0,0]]},{\"id\":758233743713505284,\"timedPrice\":[[0,700,72,60,0,0],[700,1000,105,60,0,0],[1000,1500,138,60,0,0],[1500,1800,105,60,0,0],[1800,2100,138,60,0,0],[2100,2300,105,60,0,0],[2300,2400,72,60,0,0]]},{\"id\":839643082415939584,\"timedPrice\":[[700,1000,90,0,0,0],[1000,1500,145,0,0,0],[1500,1800,90,0,0,0],[1800,2100,145,0,0,0],[2100,2300,90,0,0,0],[2300,700,38,0,0,0]]},{\"id\":846982049071108096,\"timedPrice\":[[0,700,38,80,0,0],[700,800,90,80,0,0],[800,1000,90,80,10,0],[1000,1200,145,80,10,0],[1200,1500,145,80,10,0],[1500,1800,90,80,10,0],[1800,1900,145,80,10,0],[1900,2000,145,80,10,0],[2000,2100,145,80,0,0],[2100,2300,90,80,0,0],[2300,2400,38,80,0,0]]},{\"id\":852058412249518080,\"timedPrice\":[[2300,700,38,40,0,0],[700,800,90,40,0,0],[800,1000,90,40,10,0],[1000,1500,145,40,10,0],[1500,1800,90,40,10,0],[1800,2000,145,40,10,0],[2000,2100,145,40,0,0],[2100,2300,90,40,0,0]]}],\"time\":20170426102611,\"sid\":857058194495905792}");
        XCloudMessage request = new XCloudMessage();
        request.setMessageName(XCloudMessage.ApplySetting);
        request.setSrcId(this.f87sn);
        request.setBody(applySetting);
        request.setData(applySetting.toJson());
        request.setSessionId(String.valueOf(applySetting.getSid()));
        this.handler.sendMessage(this.handler.obtainMessage(69640, request));
    }

    private void testApplySetting() {
        ApplySetting applySetting = (ApplySetting) new ApplySetting().fromJson("{\"nfcGroupSeed\":[{\"id\":010101, \"seedM1\":\"wwfcefref32r446t45greewdwqde32e43r3\"}]}");
        XCloudMessage request = new XCloudMessage();
        request.setMessageName(XCloudMessage.ApplySetting);
        request.setSrcId(this.f87sn);
        request.setBody(applySetting);
        request.setData(applySetting.toJson());
        request.setSessionId(String.valueOf(applySetting.getSid()));
        this.handler.sendMessage(this.handler.obtainMessage(69640, request));
    }

    private void testUIDeviceCodeSetting() {
        ApplySetting applySetting = (ApplySetting) new ApplySetting().fromJson("{\"qrcodeChars\":\"WDFRJG\"}");
        XCloudMessage request = new XCloudMessage();
        request.setMessageName(XCloudMessage.ApplySetting);
        request.setSrcId(this.f87sn);
        request.setBody(applySetting);
        request.setData(applySetting.toJson());
        request.setSessionId(String.valueOf(applySetting.getSid()));
        this.handler.sendMessage(this.handler.obtainMessage(69640, request));
    }

    public void testLocaleSetting() {
        ApplySetting applySetting = new ApplySetting();
        applySetting.setSid(1234324543L);
        applySetting.setTime(System.currentTimeMillis());
        LocaleOption localeOption = new LocaleOption();
        localeOption.setTimezone("+08:00");
        localeOption.setTimezoneDisp("-05:00");
        localeOption.setUseDST(true);
        localeOption.setLang("zh");
        localeOption.setCurrency("USD");
        applySetting.setLocale(localeOption);
        XCloudMessage request = new XCloudMessage();
        request.setMessageName(XCloudMessage.ApplySetting);
        request.setSrcId(this.f87sn);
        request.setBody(applySetting);
        request.setData(applySetting.toJson());
        request.setSessionId(String.valueOf(applySetting.getSid()));
        this.handler.sendMessage(this.handler.obtainMessage(69640, request));
    }

    private void testFtp() {
        FtpUtils.FtpConfig cfg = new FtpUtils.FtpConfig();
        cfg.setHost("112.74.32.129");
        cfg.setUsername("heling");
        cfg.setPassword("123456");
        FtpUtils.download("upload/xcharger.apk", "/data/data/1.jar", cfg, new FtpUtils.TransferListener() {
            public void onTransferPercentage(int percent) {
            }

            public void onTransferPercentage(long downloaded, long total) {
                Log.d("XCloudProtocolAgent.testFtp", "ftp is transfering ...");
            }

            public void onTransferComplete() {
                Log.d("XCloudProtocolAgent.testFtp", "ftp transfer completed");
            }

            public void onTransferFail() {
                Log.w("XCloudProtocolAgent.testFtp", "ftp transfer failed");
            }

            public void onConnected() {
                Log.w("XCloudProtocolAgent.testFtp", "ftp connected");
            }

            public void onConnectFail() {
                Log.w("XCloudProtocolAgent.testFtp", "ftp connect failed");
            }
        });
    }
}
