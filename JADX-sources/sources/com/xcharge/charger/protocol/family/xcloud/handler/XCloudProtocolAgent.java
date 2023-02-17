package com.xcharge.charger.protocol.family.xcloud.handler;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
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
import com.xcharge.charger.device.c2.bean.PortRuntimeData;
import com.xcharge.charger.device.c2.service.C2DeviceProxy;
import com.xcharge.charger.protocol.family.xcloud.R;
import com.xcharge.charger.protocol.family.xcloud.bean.DeviceCapability;
import com.xcharge.charger.protocol.family.xcloud.bean.DeviceContent;
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
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
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
    private HandlerThread thread = null;
    private MsgHandler handler = null;
    private HandlerTimer handlerTimer = null;
    private Context context = null;
    private ThreadPoolExecutor connectThreadPoolExecutor = null;
    private MessageProxy xcloudProxy = null;
    private MessageProxyOptions xcloudOpt = null;
    private String sn = null;
    private String authKey = null;
    private AtomicLong sidGen = new AtomicLong(20000);
    private boolean isConnected = false;
    private HashMap<String, XCloudPortHandler> portHandlers = null;
    private HashMap<String, SendRequestState> sendReqestState = null;
    private NetworkStatusObserver networkStatusObserver = null;
    private CloudTimeSynchObserver cloudTimeSynchObserver = null;
    private DeviceSetting latestDeviceSettingError = null;
    private AtomicLong networkDiagnosisCnt = new AtomicLong(0);
    private Long mqttConnectBeginTime = null;
    private Long mqttConnectEndTime = null;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public interface OnSendMessageCallback {
        void onFailed(XCloudMessage xCloudMessage);

        void onSended(XCloudMessage xCloudMessage);
    }

    public static XCloudProtocolAgent getInstance() {
        if (instance == null) {
            instance = new XCloudProtocolAgent();
        }
        return instance;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class SendRequestState {
        XCloudMessage request;
        XCLOUD_REQUEST_STATE status;
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
            try {
                switch (msg.what) {
                    case 69633:
                        XCloudProtocolAgent.this.connect();
                        break;
                    case 69634:
                        Log.i("XCloudProtocolAgent.handleMessage", "connected !!!");
                        LogUtils.cloudlog("xcloud login !!!");
                        if (XCloudProtocolAgent.this.networkDiagnosisCnt.longValue() != 0) {
                            LogUtils.cloudlog("skip " + XCloudProtocolAgent.this.networkDiagnosisCnt.longValue() + " times network connection diagnosis !!!");
                            XCloudProtocolAgent.this.networkDiagnosisCnt.set(0L);
                        }
                        XCloudProtocolAgent.this.isConnected = true;
                        ChargeStatusCacheProvider.getInstance().updateCloudConnected(true);
                        XCloudProtocolAgent.this.handlerTimer.startTimer(1000L, 69641, null);
                        XCloudProtocolAgent.this.portsActive();
                        XCloudProtocolAgent.this.reportSystemInfo(null);
                        XCloudProtocolAgent.this.requestSetting();
                        break;
                    case 69635:
                        Log.i("XCloudProtocolAgent.handleMessage", "disconnected !!!");
                        LogUtils.cloudlog("xcloud logout !!!");
                        XCloudProtocolAgent.this.isConnected = false;
                        ChargeStatusCacheProvider.getInstance().updateCloudConnected(false);
                        XCloudProtocolAgent.this.handler.removeMessages(69649);
                        XCloudProtocolAgent.this.handlerTimer.stopTimer(69641);
                        XCloudProtocolAgent.this.portsDeactive();
                        XCloudProtocolAgent.this.handler.sendEmptyMessageDelayed(69633, 10000L);
                        break;
                    case 69636:
                        Log.i("XCloudProtocolAgent.handleMessage", "failed to connect !!! authKey: " + XCloudProtocolAgent.this.authKey);
                        if (HardwareStatusCacheProvider.getInstance().isNetworkConnected()) {
                            long nowCnt = XCloudProtocolAgent.this.networkDiagnosisCnt.getAndIncrement();
                            if (nowCnt >= 0 && nowCnt < 3) {
                                LogUtils.applog("failed to connnect to xcloud, try to diagnosis network connectivity ...");
                                new Thread(new Runnable() { // from class: com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.MsgHandler.1
                                    @Override // java.lang.Runnable
                                    public void run() {
                                        DCAPProxy.getInstance().networkConnectivityDiagnosis();
                                    }
                                }).start();
                            }
                        }
                        XCloudProtocolAgent.this.handler.sendEmptyMessageDelayed(69633, 20000L);
                        break;
                    case 69637:
                        XCloudMessage xcloudMessage = (XCloudMessage) msg.obj;
                        if (!XCloudProtocolAgent.this.isConnected()) {
                            XCloudProtocolAgent.this.handler.sendMessage(XCloudProtocolAgent.this.handler.obtainMessage(69639, xcloudMessage));
                            break;
                        } else {
                            XCloudProtocolAgent.this.sendXCloudMessage(xcloudMessage);
                            if (XCloudMessage.RequestChargeQRCode.equals(xcloudMessage.getMessageName()) || XCloudMessage.RequestChargeWithIDCard.equals(xcloudMessage.getMessageName()) || XCloudMessage.ReportChargeStarted.equals(xcloudMessage.getMessageName()) || XCloudMessage.ReportChargeEnded.equals(xcloudMessage.getMessageName()) || XCloudMessage.ReportLocalChargeBill.equals(xcloudMessage.getMessageName()) || XCloudMessage.SayHello.equals(xcloudMessage.getMessageName()) || XCloudMessage.ReportLocalChargeStarted.equals(xcloudMessage.getMessageName()) || XCloudMessage.ReportLocalChargeEnded.equals(xcloudMessage.getMessageName()) || XCloudMessage.RequestSetting.equals(xcloudMessage.getMessageName())) {
                                SendRequestState reqestState = new SendRequestState(null);
                                reqestState.request = xcloudMessage;
                                reqestState.status = XCLOUD_REQUEST_STATE.sending;
                                reqestState.timestamp = System.currentTimeMillis();
                                XCloudProtocolAgent.this.sendReqestState.put(xcloudMessage.getSessionId(), reqestState);
                                break;
                            }
                        }
                        break;
                    case 69638:
                        XCloudMessage xcloudMessage2 = (XCloudMessage) msg.obj;
                        Log.d("XCloudProtocolAgent.handleMessage", "succeed to send XCloud msg: " + xcloudMessage2.toJson());
                        if (XCloudMessage.RequestChargeQRCode.equals(xcloudMessage2.getMessageName()) || XCloudMessage.RequestChargeWithIDCard.equals(xcloudMessage2.getMessageName()) || XCloudMessage.ReportChargeStarted.equals(xcloudMessage2.getMessageName()) || XCloudMessage.ReportChargeEnded.equals(xcloudMessage2.getMessageName()) || XCloudMessage.ReportLocalChargeBill.equals(xcloudMessage2.getMessageName()) || XCloudMessage.SayHello.equals(xcloudMessage2.getMessageName()) || XCloudMessage.ReportLocalChargeStarted.equals(xcloudMessage2.getMessageName()) || XCloudMessage.ReportLocalChargeEnded.equals(xcloudMessage2.getMessageName()) || XCloudMessage.RequestSetting.equals(xcloudMessage2.getMessageName())) {
                            SendRequestState reqestState2 = (SendRequestState) XCloudProtocolAgent.this.sendReqestState.get(xcloudMessage2.getSessionId());
                            if (reqestState2 != null) {
                                reqestState2.status = XCLOUD_REQUEST_STATE.sended;
                                reqestState2.timestamp = System.currentTimeMillis();
                                XCloudProtocolAgent.this.handleSendMsgOk(reqestState2.request);
                                break;
                            } else {
                                Log.w("XCloudProtocolAgent.handleMessage", "maybe timeout to send XCloud request msg: " + xcloudMessage2.toJson());
                                break;
                            }
                        } else {
                            XCloudProtocolAgent.this.handleSendMsgOk(xcloudMessage2);
                            break;
                        }
                        break;
                    case 69639:
                        XCloudProtocolAgent.this.handleSendMsgFail((XCloudMessage) msg.obj);
                        break;
                    case 69640:
                        XCloudMessage xcloudMessage3 = (XCloudMessage) msg.obj; // MSG_RECEIVE
                        Log.d("XCloudProtocolAgent.handleMessage", "received XCloud msg: " + xcloudMessage3.toJson());
                        if (XCloudMessage.SendChargeQRCode.equals(xcloudMessage3.getMessageName()) || XCloudMessage.RequestStartCharge.equals(xcloudMessage3.getMessageName()) || XCloudMessage.RequestRefuseCharge.equals(xcloudMessage3.getMessageName()) || XCloudMessage.ConfirmChargeStarted.equals(xcloudMessage3.getMessageName()) || XCloudMessage.ConfirmChargeEnded.equals(xcloudMessage3.getMessageName()) || XCloudMessage.AnswerHello.equals(xcloudMessage3.getMessageName()) || XCloudMessage.ConfirmLocalChargeBill.equals(xcloudMessage3.getMessageName()) || XCloudMessage.ApplySetting.equals(xcloudMessage3.getMessageName())) {
                            String requestSessionId = xcloudMessage3.getSessionId();
                            SendRequestState reqestState3 = (SendRequestState) XCloudProtocolAgent.this.sendReqestState.get(requestSessionId);
                            if (reqestState3 != null) {
                                XCloudMessage request = reqestState3.request;
                                XCloudProtocolAgent.this.sendReqestState.remove(requestSessionId);
                                xcloudMessage3.setPort(request.getPort());
                                XCloudProtocolAgent.this.dispatchXCloudMessage(xcloudMessage3, request);
                                break;
                            } else if (!XCloudMessage.RequestStartCharge.equals(xcloudMessage3.getMessageName()) && !XCloudMessage.RequestRefuseCharge.equals(xcloudMessage3.getMessageName()) && !XCloudMessage.ApplySetting.equals(xcloudMessage3.getMessageName())) {
                                Log.w("XCloudProtocolAgent.handleMessage", "maybe timeout to wait for response msg: " + xcloudMessage3.toJson());
                                break;
                            } else {
                                XCloudProtocolAgent.this.dispatchXCloudMessage(xcloudMessage3, null);
                                break;
                            }
                        } else {
                            XCloudProtocolAgent.this.dispatchXCloudMessage(xcloudMessage3, null);
                            break;
                        }
                        break;
                    case 69641:
                        try {
                            XCloudProtocolAgent.this.requestTimeoutCheck();
                        } catch (Exception e) {
                        }
                        XCloudProtocolAgent.this.handlerTimer.startTimer(1000L, 69641, null);
                        break;
                    case 69648:
                        if (!HardwareStatusCacheProvider.getInstance().isNetworkConnected()) {
                            XCloudProtocolAgent.this.handler.sendEmptyMessageDelayed(69648, 5000L);
                            break;
                        } else {
                            Log.i("XCloudProtocolAgent.handleMessage", "sync time from cloud !!!");
                            if (!XCloudProtocolAgent.this.syncTime()) {
                                XCloudProtocolAgent.this.handler.sendEmptyMessageDelayed(69648, 5000L);
                                break;
                            }
                        }
                        break;
                    case 69649:
                        XCloudProtocolAgent.this.resendRequest((XCloudMessage) msg.obj);
                        break;
                    case 69650:
                        try {
                            XCloudProtocolAgent.this.sendSayHello();
                        } catch (Exception e2) {
                        }
                        XCloudProtocolAgent.this.handlerTimer.startTimer(1000L, 69641, null);
                        break;
                    case XCloudProtocolAgent.MSG_CLOUD_TIMESYNCH_TIMER /* 69651 */:
                        XCloudProtocolAgent.this.handlerTimer.stopTimer(XCloudProtocolAgent.MSG_CLOUD_TIMESYNCH_TIMER);
                        try {
                            XCloudProtocolAgent.this.syncTime();
                        } catch (Exception e3) {
                        }
                        XCloudProtocolAgent.this.handlerTimer.startTimer(43200000L, XCloudProtocolAgent.MSG_CLOUD_TIMESYNCH_TIMER, null);
                        break;
                    case XCloudProtocolAgent.MSG_MQTT_CONNECT_BLOCK_CHECK_TIMER /* 69652 */:
                        XCloudProtocolAgent.this.handleMqttConnectBlocked();
                        break;
                    case 135169:
                        Uri uri = (Uri) msg.obj;
                        XCloudProtocolAgent.this.handleNetworkStatusChanged(uri);
                        break;
                    case CloudTimeSynchObserver.MSG_CLOUD_TIME_SYNCHED /* 143361 */:
                        Uri uri2 = (Uri) msg.obj;
                        XCloudProtocolAgent.this.handleCloudTimeSynch(uri2);
                        break;
                }
            } catch (Exception e4) {
                Log.e("XCloudProtocolAgent.handleMessage", "except: " + Log.getStackTraceString(e4));
                LogUtils.syslog("XCloudProtocolAgent handleMessage exception: " + Log.getStackTraceString(e4));
            }
            super.handleMessage(msg);
        }
    }

    public void init(Context context) {
        this.context = context;
        if (TextUtils.isEmpty(RemoteSettingCacheProvider.getInstance().getProtocolTimezone())) {
            RemoteSettingCacheProvider.getInstance().updateProtocolTimezone("+08:00");
        }
        this.portHandlers = new HashMap<>();
        HashMap<String, Port> ports = HardwareStatusCacheProvider.getInstance().getPorts();
        if (ports != null) {
            for (String port : ports.keySet()) {
                XCloudPortHandler portHandler = new XCloudPortHandler();
                portHandler.init(context, port);
                this.portHandlers.put(port, portHandler);
            }
        }
        this.connectThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue(20), new RejectedExecutionHandler() { // from class: com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.1
            @Override // java.util.concurrent.RejectedExecutionHandler
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
        this.handlerTimer.init(context);
        this.networkStatusObserver = new NetworkStatusObserver(this.context, this.handler);
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor(Network.class.getSimpleName()), true, this.networkStatusObserver);
        this.cloudTimeSynchObserver = new CloudTimeSynchObserver(context, this.handler);
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
        this.sn = HardwareStatusCacheProvider.getInstance().getSn();
        this.authKey = MD5Utils.MD5(TextUtils.concat(clientKey, magicKey, this.sn).toString()).toLowerCase();
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

    /* JADX INFO: Access modifiers changed from: private */
    public void handleNetworkStatusChanged(Uri uri) {
        Log.i("XCloudProtocolAgent.handleNetworkStatusChanged", "network status changed, uri: " + uri.toString());
        String lastSegment = uri.getLastPathSegment();
        if (!"connected".equals(lastSegment)) {
            "disconnected".equals(lastSegment);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleCloudTimeSynch(Uri uri) {
        Log.i("XCloudProtocolAgent.handleCloudTimeSynch", "cloud time synch setted, uri: " + uri.toString());
        if (ChargeStatusCacheProvider.getInstance().isCloudTimeSynch()) {
            this.handlerTimer.startTimer(43200000L, MSG_CLOUD_TIMESYNCH_TIMER, null);
        }
    }

    public void initServerTimeSync() {
        this.handler.sendEmptyMessage(69648);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean syncTime() {
        try {
            BasicHttpParams basicHttpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(basicHttpParams, 10000);
            HttpConnectionParams.setSoTimeout(basicHttpParams, 10000);
            DefaultHttpClient httpClient = new DefaultHttpClient(basicHttpParams);
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
            Header[] headers = response.getHeaders("Date");
            Date date = new Date(headers[0].getValue());
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

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isConnected() {
        return this.isConnected;
    }

    public void initConnection() {
        this.handler.sendEmptyMessage(69633);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleMqttConnectBlocked() {
        Log.d("XCloudProtocolAgent.handleMqttConnectBlocked", "mqtt connect call maybe blocked forever, here will reinit mqtt communication module");
        LogUtils.cloudlog("mqtt connect call maybe blocked forever, here will reinit mqtt communication module");
        this.connectThreadPoolExecutor.shutdown();
        this.connectThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue(10), new RejectedExecutionHandler() { // from class: com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.2
            @Override // java.util.concurrent.RejectedExecutionHandler
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

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class ConnectTask implements Runnable {
        private MessageHandler msgHandler;

        public ConnectTask(MessageHandler msgHandler) {
            this.msgHandler = null;
            this.msgHandler = msgHandler;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                XCloudProtocolAgent.this.mqttConnectBeginTime = Long.valueOf(System.nanoTime());
                XCloudProtocolAgent.this.handlerTimer.stopTimer(XCloudProtocolAgent.MSG_MQTT_CONNECT_BLOCK_CHECK_TIMER);
                XCloudProtocolAgent.this.handlerTimer.startTimer(300000L, XCloudProtocolAgent.MSG_MQTT_CONNECT_BLOCK_CHECK_TIMER, null);
                if (XCloudProtocolAgent.this.xcloudProxy.connect(XCloudProtocolAgent.this.sn, XCloudProtocolAgent.this.authKey, this.msgHandler)) {
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

    /* JADX INFO: Access modifiers changed from: private */
    public void connect() {
        if (HardwareStatusCacheProvider.getInstance().isNetworkConnected()) {
            Log.i("XCloudProtocolAgent.connect", "init connection !!!");
            this.connectThreadPoolExecutor.execute(new ConnectTask(this));
            return;
        }
        this.handler.sendEmptyMessageDelayed(69633, 5000L);
    }

    private void disconnect() {
        try {
            this.xcloudProxy.disconnect();
        } catch (Exception e) {
            Log.e("XCloudProtocolAgent.disconnect", Log.getStackTraceString(e));
        }
        this.handler.sendEmptyMessage(69635);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class SendTask implements Runnable {
        private OnSendMessageCallback callback;
        private XCloudMessage msg;

        public SendTask(XCloudMessage msg, OnSendMessageCallback callback) {
            this.callback = null;
            this.msg = null;
            this.callback = callback;
            this.msg = msg;
        }

        public XCloudMessage getMessage() {
            return this.msg;
        }

        @Override // java.lang.Runnable
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

    /* JADX INFO: Access modifiers changed from: private */
    public void sendXCloudMessage(XCloudMessage msg) {
        this.connectThreadPoolExecutor.execute(new SendTask(msg, new OnSendMessageCallback() { // from class: com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.3
            @Override // com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.OnSendMessageCallback
            public void onSended(XCloudMessage msg2) {
                XCloudProtocolAgent.this.handler.sendMessage(XCloudProtocolAgent.this.handler.obtainMessage(69638, msg2));
            }

            @Override // com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.OnSendMessageCallback
            public void onFailed(XCloudMessage msg2) {
                XCloudProtocolAgent.this.handler.sendMessage(XCloudProtocolAgent.this.handler.obtainMessage(69639, msg2));
            }
        }));
    }

    private MqttOptions getOptions() {
        String mqttCfg = ContextUtils.readFileData("xcloud_family_mqtt_cfg.json", this.context);
        if (TextUtils.isEmpty(mqttCfg)) {
            mqttCfg = ContextUtils.getRawFileToString(this.context, R.raw.xcloud_family_mqtt_ops);
            if (!TextUtils.isEmpty(mqttCfg)) {
                ContextUtils.writeFileData("xcloud_family_mqtt_cfg.json", mqttCfg, this.context);
            }
        }
        Log.d("XCloudProtocolAgent.getOptions", "configured options: " + mqttCfg);
        MqttOptions options = null;
        if (!TextUtils.isEmpty(mqttCfg)) {
            MqttOptions options2 = new MqttOptions().fromJson(mqttCfg);
            options = options2;
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

    /* JADX INFO: Access modifiers changed from: private */
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
        if (xcloudPortHandler == null) {
            Log.w("XCloudProtocolAgent.getChargeSession", "no available port handler for port: " + port);
            return null;
        }
        return xcloudPortHandler.getChargeSession();
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

    /* JADX INFO: Access modifiers changed from: private */
    public void requestSetting() {
        RequestSetting reqSetting = new RequestSetting();
        long sessionId = getInstance().genSid();
        reqSetting.setSid(Long.valueOf(sessionId));
        reqSetting.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        XCloudMessage msg = new XCloudMessage();
        msg.setMessageName(XCloudMessage.RequestSetting);
        msg.setSrcId(this.sn);
        msg.setBody(reqSetting);
        msg.setSessionId(String.valueOf(sessionId));
        msg.setData(reqSetting.toJson());
        if (!sendMessage(msg)) {
            this.handler.sendMessage(this.handler.obtainMessage(69639, msg));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
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
        String billId = String.valueOf(requestAutoStop.getBillId());
        String port = getPort(billId);
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
        String billId = String.valueOf(cancelAutoStop.getBillId());
        String port = getPort(billId);
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
        String billId = String.valueOf(requestStopCharge.getBillId());
        String port = getPort(billId);
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
        String billId = String.valueOf(requestEndCharge.getBillId());
        String port = getPort(billId);
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
        QuerySystemInfo querySystemInfo = (QuerySystemInfo) request.getBody();
        reportSystemInfo(querySystemInfo.getSid());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendSayHello() {
        long sessionId = genSid();
        SayHello sayHelloRequest = new SayHello();
        sayHelloRequest.setSid(Long.valueOf(sessionId));
        XCloudMessage request = new XCloudMessage();
        request.setMessageName(XCloudMessage.SayHello);
        request.setSrcId(this.sn);
        request.setBody(sayHelloRequest);
        request.setData(sayHelloRequest.toJson());
        request.setSessionId(String.valueOf(sessionId));
        this.handler.sendMessage(this.handler.obtainMessage(69637, request)); // MSG_SEND
    }

    /* JADX INFO: Access modifiers changed from: private */
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
            MobileNet mNet = HardwareStatusCacheProvider.getInstance().getMobileNetStatus();
            rptSystemInfo.setSimId(mNet.getIMSI());
        }
        PortRuntimeData runtime = C2DeviceProxy.getInstance().getPortRuntimeInfo("1");
        if (runtime != null) {
            rptSystemInfo.setAmmeter(runtime.getEnergy());
        }
        XCloudMessage report = new XCloudMessage();
        report.setMessageName(XCloudMessage.ReportSystemInfo);
        report.setSrcId(this.sn);
        report.setBody(rptSystemInfo);
        report.setData(rptSystemInfo.toJson());
        report.setSessionId(sessionId);
        this.handler.sendMessage(this.handler.obtainMessage(69637, report));
    }

    private void handleQueryState(XCloudMessage request) {
        Wifi info;
        QueryState queryState = (QueryState) request.getBody();
        Long sid = queryState.getSid();
        String sessionId = null;
        if (sid != null) {
            sessionId = String.valueOf(sid);
        }
        JSONObject workStatus = new JSONObject();
        try {
            String update = C2DeviceProxy.getInstance().getRawPortRuntimeInfo("1");
            if (!TextUtils.isEmpty(update)) {
                JSONObject updateJson = new JSONObject(update);
                JSONObject runtime = updateJson.getJSONObject("data");
                if (runtime != null) {
                    workStatus.put("runtime", runtime);
                }
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
        report.setSrcId(this.sn);
        report.setBody(reportState);
        report.setData(reportState.toJson());
        report.setSessionId(sessionId);
        this.handler.sendMessage(this.handler.obtainMessage(69637, report));
    }

    private void handleQueryLog(XCloudMessage request) {
        QueryLog queryLog = (QueryLog) request.getBody();
        LogUploadAgent.getInstance().upload(queryLog);
    }

    private void handleRequestVerification(XCloudMessage request) {
        XCloudDCAPGateway.getInstance().sendMessage(XCloudDCAPGateway.getInstance().obtainMessage(77827, request));
    }

    private void handleRequestUpdateStartTime(XCloudMessage request) {
        RequestUpdateStartTime requestUpdateStartTime = (RequestUpdateStartTime) request.getBody();
        String billId = String.valueOf(requestUpdateStartTime.getBillId());
        String port = getPort(billId);
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
        response.setSrcId(this.sn);
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
            reportSettingResult.setError(new DeviceError(null, null, this.latestDeviceSettingError));
        }
        reportSettingResult.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        XCloudMessage response = new XCloudMessage();
        response.setMessageName(XCloudMessage.ReportSettingResult);
        response.setSrcId(this.sn);
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
        response.setSrcId(this.sn);
        response.setBody(reportSettingResult);
        response.setData(reportSettingResult.toJson());
        this.handler.sendMessage(this.handler.obtainMessage(69637, response));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleSendMsgOk(XCloudMessage msg) {
        String port = msg.getPort();
        if (!TextUtils.isEmpty(port)) {
            XCloudPortHandler portHandler = getPortHandler(port);
            portHandler.sendMessage(portHandler.obtainMessage(73731, msg));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleSendMsgFail(XCloudMessage msg) {
        String port = msg.getPort();
        if (!TextUtils.isEmpty(port)) {
            XCloudPortHandler portHandler = getPortHandler(port);
            portHandler.sendMessage(portHandler.obtainMessage(73732, msg)); // MSG_SEND_FAIL
            return;
        }
        handleMsgResend(msg);
    }

    private void handleRequestTimeout(XCloudMessage request) {
        String port = request.getPort();
        if (!TextUtils.isEmpty(port)) {
            XCloudPortHandler portHandler = getPortHandler(port);
            portHandler.sendMessage(portHandler.obtainMessage(73733, request)); // MSG_REQUEST_TIMEOUT
            return;
        }
        handleMsgResend(request);
    }

    private void handleMsgResend(XCloudMessage msg) {
        String name = msg.getMessageName();
        if (XCloudMessage.RequestSetting.equals(name)) {
            int interval = (msg.getResendCnt() + 1) * 10;
            if (interval > 60) {
                interval = 60;
            }
            this.handler.sendMessageDelayed(this.handler.obtainMessage(69649, msg), interval * 1000);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void requestTimeoutCheck() {
        if (this.sendReqestState.size() > 0) {
            Iterator<Map.Entry<String, SendRequestState>> it2 = this.sendReqestState.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry<String, SendRequestState> entry = it2.next();
                SendRequestState requestState = entry.getValue();
                XCLOUD_REQUEST_STATE state = requestState.status;
                long timestamp = requestState.timestamp;
                if (XCLOUD_REQUEST_STATE.sending.equals(state)) {
                    if (System.currentTimeMillis() - timestamp > 5000) {
                        it2.remove();
                        handleSendMsgFail(requestState.request);
                    }
                } else if (XCLOUD_REQUEST_STATE.sended.equals(state) && System.currentTimeMillis() - timestamp > 10000) {
                    it2.remove();
                    handleRequestTimeout(requestState.request);
                }
            }
        }
    }

    public void sendReportActionResult(XCloudMessage request, EnumActionStatus status, DeviceError error) {
        Long sid = null;
        String name = request.getMessageName();
        if (name.equals(XCloudMessage.RequestAction)) {
            RequestAction requestAction = (RequestAction) request.getBody();
            sid = requestAction.getSid();
        } else if (name.equals(XCloudMessage.RequestUpgrade)) {
            RequestUpgrade requestUpgrade = (RequestUpgrade) request.getBody();
            sid = requestUpgrade.getSid();
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
        response.setSrcId(this.sn);
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
                return;
            } else {
                xcloudPortHandler.reportChargeStarted(chargeId);
                return;
            }
        }
        Log.w("XCloudProtocolAgent.sendReportChargeStarted", "failed to find related port for charge: " + chargeId);
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
        RequestVerification requestVerification = (RequestVerification) request.getBody();
        ReportVerification reportVerification = new ReportVerification();
        reportVerification.setSid(requestVerification.getSid());
        reportVerification.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        XCloudMessage report = new XCloudMessage();
        report.setMessageName(XCloudMessage.ReportVerification);
        report.setSrcId(this.sn);
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
                return;
            } else {
                xcloudPortHandler.reportChargePaused(chargeId, error, time);
                return;
            }
        }
        Log.w("XCloudProtocolAgent.sendReportChargePaused", "failed to find related port for charge: " + chargeId);
    }

    public void sendReportChargeResumed(String chargeId, DeviceError error, long time) {
        String port = getPort(chargeId);
        if (!TextUtils.isEmpty(port)) {
            XCloudPortHandler xcloudPortHandler = getPortHandler(port);
            if (xcloudPortHandler == null) {
                Log.w("XCloudProtocolAgent.sendReportChargeResumed", "no available port handler for port: " + port);
                return;
            } else {
                xcloudPortHandler.reportChargeResumed(chargeId, error, time);
                return;
            }
        }
        Log.w("XCloudProtocolAgent.sendReportChargeResumed", "failed to find related port for charge: " + chargeId);
    }

    public void sendReportDelayCountStarted(String chargeId) {
        String port = getPort(chargeId);
        if (!TextUtils.isEmpty(port)) {
            XCloudPortHandler xcloudPortHandler = getPortHandler(port);
            if (xcloudPortHandler == null) {
                Log.w("XCloudProtocolAgent.sendReportDelayCountStarted", "no available port handler for port: " + port);
                return;
            } else {
                xcloudPortHandler.reportDelayCountStarted(chargeId);
                return;
            }
        }
        Log.w("XCloudProtocolAgent.sendReportDelayCountStarted", "failed to find related port for charge: " + chargeId);
    }

    public void sendReportDelayFeeStarted(String chargeId, long delayStart) {
        String port = getPort(chargeId);
        if (!TextUtils.isEmpty(port)) {
            XCloudPortHandler xcloudPortHandler = getPortHandler(port);
            if (xcloudPortHandler == null) {
                Log.w("XCloudProtocolAgent.sendReportDelayFeeStarted", "no available port handler for port: " + port);
                return;
            } else {
                xcloudPortHandler.reportDelayFeeStarted(chargeId, delayStart);
                return;
            }
        }
        Log.w("XCloudProtocolAgent.sendReportDelayFeeStarted", "failed to find related port for charge: " + chargeId);
    }

    public XCloudMessage sendRequestChargeWithIDCard(String port, String cardNo, String timestamp, String nonce, String signature) {
        XCloudPortHandler xcloudPortHandler = getPortHandler(port);
        if (xcloudPortHandler == null) {
            Log.w("XCloudProtocolAgent.sendRequestChargeWithIDCard", "no available port handler for port: " + port);
            return null;
        }
        return xcloudPortHandler.requestChargeWithIDCard(cardNo, timestamp, nonce, signature);
    }

    @Override // net.xcharger.sdk.device.MessageHandler
    public void onDisconnected() {
        this.handler.sendEmptyMessage(69635);
        if (HardwareStatusCacheProvider.getInstance().isNetworkConnected()) {
            long nowCnt = this.networkDiagnosisCnt.getAndIncrement();
            if (nowCnt >= 0 && nowCnt < 3) {
                LogUtils.applog("xcloud connection lost, try to diagnosis network connectivity ...");
                new Thread(new Runnable() { // from class: com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.4
                    @Override // java.lang.Runnable
                    public void run() {
                        DCAPProxy.getInstance().networkConnectivityDiagnosis();
                    }
                }).start();
            }
        }
    }

    // MQTT Nachrichten senden
    @Override // net.xcharger.sdk.device.MessageHandler
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
            } else if (messageName.equals(XCloudMessage.AnswerHello)) { // AnswerHello
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
        if (decodedBody != null) {
            msg.setBody(decodedBody);
            msg.setData(data);
            return msg;
        }
        return null;
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
        deviceCapability.setMaxCurrent(Double.valueOf(ChargeStatusCacheProvider.getInstance().getAmpCapacity() * 1.0d));
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
        if (portFeeRate != null && (portFeeRates = portFeeRate.getFeeRates()) != null && portFeeRates.size() > 0) {
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
                PortSetting portSetting = entry.getValue();
                XCloudPort portInfo = SettingUtils.getXCloudPortInfo(port, portSetting);
                ports.put(port, portInfo);
            }
            deviceSetting.setPorts(ports);
        }
        DeviceContent deviceContent = SettingUtils.getDeviceContent();
        deviceSetting.setContent(deviceContent);
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
        return "en".equals(lang) || "zh".equals(lang) || "de".equals(lang);
    }

    private boolean isOnlyPropInBean(Object o, String prop) {
        try {
            JSONObject json = new JSONObject(JsonBean.getGsonBuilder().create().toJson(o));
            JSONArray allProps = json.names();
            Log.d("XCloudProtocolAgent.isOnlyPropInBean", json.toString());
            Log.d("XCloudProtocolAgent.isOnlyPropInBean", allProps.toString());
            if (allProps.length() == 3 && json.has(prop) && json.has("sid")) {
                if (json.has(ChargeStopCondition.TYPE_TIME)) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e("XCloudProtocolAgent.isOnlyPropInBean", Log.getStackTraceString(e));
        }
        return false;
    }

    private boolean setDeviceSetting(DeviceSetting deviceSetting, Long sid) {
        JSONObject params;
        Set<String> portNos = HardwareStatusCacheProvider.getInstance().getPorts().keySet();
        Object anyOptions = deviceSetting.getAnyOptions();
        if (anyOptions != null) {
            try {
                JSONObject json = new JSONObject(JsonBean.ObjectToJson(anyOptions));
                if (json.has("isPlug2Charge")) {
                    boolean isPlug2Charge = json.getBoolean("isPlug2Charge");
                    SystemSettingCacheProvider.getInstance().setPlug2Charge(isPlug2Charge);
                    SystemSettingCacheProvider.getInstance().persist();
                }
                if (json.has("isMonitor")) {
                    boolean isMonitor = json.getBoolean("isMonitor");
                    SystemSettingCacheProvider.getInstance().setYZXMonitor(isMonitor);
                    SystemSettingCacheProvider.getInstance().persist();
                }
                if (json.has("customer")) {
                    PLATFORM_CUSTOMER platformCustomer = null;
                    String customer = json.getString("customer");
                    if (!TextUtils.isEmpty(customer)) {
                        platformCustomer = PLATFORM_CUSTOMER.valueOf(customer);
                    }
                    SystemSettingCacheProvider.getInstance().updatetPlatformCustomer(platformCustomer);
                    SystemSettingCacheProvider.getInstance().persist();
                }
                if (json.has("isWWlanPoll")) {
                    boolean isWWlanPoll = json.getBoolean("isWWlanPoll");
                    SystemSettingCacheProvider.getInstance().setWWlanPolling(Boolean.valueOf(isWWlanPoll));
                    SystemSettingCacheProvider.getInstance().persist();
                    C2DeviceProxy.getInstance().switchWWlanPoll(isWWlanPoll);
                }
                if (json.has("isCPWait")) {
                    boolean isCPWait = json.getBoolean("isCPWait");
                    SystemSettingCacheProvider.getInstance().setCPWait(Boolean.valueOf(isCPWait));
                    SystemSettingCacheProvider.getInstance().persist();
                    C2DeviceProxy.getInstance().switchCPWait(isCPWait);
                }
                if (json.has("uiBgColor")) {
                    String uiBgColor = json.getString("uiBgColor");
                    if (TextUtils.isEmpty(uiBgColor)) {
                        uiBgColor = null;
                    }
                    SystemSettingCacheProvider.getInstance().setUiBackgroundColor(uiBgColor);
                    SystemSettingCacheProvider.getInstance().persist();
                }
                if (isOnlyPropInBean(deviceSetting, "anyOptions")) {
                    if (json.has("deleteDb")) {
                        boolean isDeleteDb = json.getBoolean("deleteDb");
                        if (isDeleteDb) {
                            int ret = FileUtils.execShell("rm -rf /data/data/com.xcharge.charger/databases/content.db*");
                            Log.i("XCloudProtocolAgent.setDeviceSetting", "delete database, ret: " + ret);
                        }
                    } else if (json.has("queryBillLog")) {
                        String uploadUrl = null;
                        String billId = json.optString("queryBillLog");
                        if (TextUtils.isEmpty(billId) && (params = json.optJSONObject("queryBillLog")) != null && params.length() > 0) {
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
                CountrySetting nowCountrySetting = RemoteSettingCacheProvider.getInstance().getCountrySetting().m10clone();
                HashMap<String, Object> localeSetting = new HashMap<>();
                if (!TextUtils.isEmpty(localZone) && !localZone.equals(nowCountrySetting.getZone())) {
                    localeSetting.put("zone", localZone);
                    nowCountrySetting.setZone(localZone);
                }
                if (useDST != null && useDST.booleanValue() != nowCountrySetting.isUseDaylightTime()) {
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
            if (deviceSetting.getCpErrorRange().intValue() >= 0 && deviceSetting.getCpErrorRange().intValue() <= 100) {
                HashMap<String, Object> values = new HashMap<>();
                values.put("value", String.valueOf(deviceSetting.getCpErrorRange()));
                SettingUtils.setDCAPRequest(SetDirective.SET_ID_DEVICE_CP_RANGE, values);
                RemoteSettingCacheProvider.getInstance().getChargeSetting().setCpRange(deviceSetting.getCpErrorRange().intValue());
                LocalSettingCacheProvider.getInstance().getChargeSetting().setCpRange(deviceSetting.getCpErrorRange().intValue());
                LocalSettingCacheProvider.getInstance().persist();
            } else {
                getLatestDeviceSettingError().setCpErrorRange(deviceSetting.getCpErrorRange());
            }
        }
        if (deviceSetting.getvErrorRange() != null) {
            if (deviceSetting.getvErrorRange().intValue() >= 0 && deviceSetting.getvErrorRange().intValue() <= 100) {
                HashMap<String, Object> values2 = new HashMap<>();
                values2.put("value", String.valueOf(deviceSetting.getvErrorRange()));
                SettingUtils.setDCAPRequest(SetDirective.SET_ID_DEVICE_VOLT_RANGE, values2);
                RemoteSettingCacheProvider.getInstance().getChargeSetting().setVoltageRange(deviceSetting.getvErrorRange().intValue());
                LocalSettingCacheProvider.getInstance().getChargeSetting().setVoltageRange(deviceSetting.getvErrorRange().intValue());
                LocalSettingCacheProvider.getInstance().persist();
            } else {
                getLatestDeviceSettingError().setvErrorRange(deviceSetting.getvErrorRange());
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
            Iterator<NFCGroupSeed> it2 = nfcGroupSeedList.iterator();
            while (it2.hasNext()) {
                NFCGroupSeed nfcGroupSeed = it2.next();
                Long id = nfcGroupSeed.getId();
                if (id != null) {
                    String groupId = String.format("%06d", id);
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

    /* JADX INFO: Access modifiers changed from: private */
    public void portsActive() {
        for (XCloudPortHandler portHandler : this.portHandlers.values()) {
            portHandler.sendMessage(portHandler.obtainMessage(73729));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void portsDeactive() {
        for (XCloudPortHandler portHandler : this.portHandlers.values()) {
            portHandler.sendMessage(portHandler.obtainMessage(73730));
        }
    }

    public long genSid() {
        long sid = this.sidGen.incrementAndGet();
        if (sid > 65535) {
            return 20000L;
        }
        return sid;
    }

    private void testVerification() {
        RequestVerification requestVerification = new RequestVerification();
        requestVerification.setCustomer("深圳智充"); // Shenzhen Smart Charge
        requestVerification.setExpireInterval(600);
        requestVerification.setSid(123456L);
        requestVerification.setTime(TimeUtils.getXCloudFormat(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
        XCloudMessage request = new XCloudMessage();
        request.setMessageName(XCloudMessage.RequestVerification); // RequestVerification
        request.setSrcId(this.sn);
        request.setBody(requestVerification);
        request.setData(requestVerification.toJson());
        request.setSessionId(String.valueOf(123456L));
        this.handler.sendMessage(this.handler.obtainMessage(69640, request));
    }

    private void testFeePolicySetting() {
        ApplySetting applySetting = (ApplySetting) new ApplySetting().fromJson("{\"defaultFeePolicy\":758233743713505280,\"feePolicy\":[{\"id\":758233743713505280,\"timedPrice\":[[0,700,38,80,0,0],[700,800,90,80,0,0],[800,1000,90,80,10,0],[1000,1200,145,80,10,0],[1200,1500,145,80,10,0],[1500,1800,90,80,10,0],[1800,1900,145,80,10,0],[1900,2000,145,80,10,0],[2000,2100,145,80,10,0],[2100,2300,90,80,0,0],[2300,2400,38,80,0,0]]},{\"id\":758233743713505282,\"timedPrice\":[[0,700,72,0,0,0],[700,1000,105,0,0,0],[1000,1500,138,0,0,0],[1500,1800,105,0,0,0],[1800,2100,138,0,0,0],[2100,2300,105,0,0,0],[2300,2400,72,0,0,0]]},{\"id\":758233743713505284,\"timedPrice\":[[0,700,72,60,0,0],[700,1000,105,60,0,0],[1000,1500,138,60,0,0],[1500,1800,105,60,0,0],[1800,2100,138,60,0,0],[2100,2300,105,60,0,0],[2300,2400,72,60,0,0]]},{\"id\":839643082415939584,\"timedPrice\":[[700,1000,90,0,0,0],[1000,1500,145,0,0,0],[1500,1800,90,0,0,0],[1800,2100,145,0,0,0],[2100,2300,90,0,0,0],[2300,700,38,0,0,0]]},{\"id\":846982049071108096,\"timedPrice\":[[0,700,38,80,0,0],[700,800,90,80,0,0],[800,1000,90,80,10,0],[1000,1200,145,80,10,0],[1200,1500,145,80,10,0],[1500,1800,90,80,10,0],[1800,1900,145,80,10,0],[1900,2000,145,80,10,0],[2000,2100,145,80,0,0],[2100,2300,90,80,0,0],[2300,2400,38,80,0,0]]},{\"id\":852058412249518080,\"timedPrice\":[[2300,700,38,40,0,0],[700,800,90,40,0,0],[800,1000,90,40,10,0],[1000,1500,145,40,10,0],[1500,1800,90,40,10,0],[1800,2000,145,40,10,0],[2000,2100,145,40,0,0],[2100,2300,90,40,0,0]]}],\"time\":20170426102611,\"sid\":857058194495905792}");
        XCloudMessage request = new XCloudMessage();
        request.setMessageName(XCloudMessage.ApplySetting); // ApplySetting
        request.setSrcId(this.sn);
        request.setBody(applySetting);
        request.setData(applySetting.toJson());
        request.setSessionId(String.valueOf(applySetting.getSid()));
        this.handler.sendMessage(this.handler.obtainMessage(69640, request));
    }

    private void testApplySetting() {
        ApplySetting applySetting = (ApplySetting) new ApplySetting().fromJson("{\"nfcGroupSeed\":[{\"id\":010101, \"seedM1\":\"wwfcefref32r446t45greewdwqde32e43r3\"}]}");
        XCloudMessage request = new XCloudMessage();
        request.setMessageName(XCloudMessage.ApplySetting); // ApplySetting
        request.setSrcId(this.sn);
        request.setBody(applySetting);
        request.setData(applySetting.toJson());
        request.setSessionId(String.valueOf(applySetting.getSid()));
        this.handler.sendMessage(this.handler.obtainMessage(69640, request));
    }

    private void testUIDeviceCodeSetting() {
        ApplySetting applySetting = (ApplySetting) new ApplySetting().fromJson("{\"qrcodeChars\":\"WDFRJG\"}"); // QRcode als Chars? ApplySetting
        XCloudMessage request = new XCloudMessage();
        request.setMessageName(XCloudMessage.ApplySetting); // ApplySetting
        request.setSrcId(this.sn);
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
        request.setSrcId(this.sn);
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
        FtpUtils.download("upload/xcharger.apk", "/data/data/1.jar", cfg, new FtpUtils.TransferListener() { // from class: com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent.5
            @Override // com.xcharge.common.utils.FtpUtils.TransferListener
            public void onTransferPercentage(int percent) {
            }

            @Override // com.xcharge.common.utils.FtpUtils.TransferListener
            public void onTransferPercentage(long downloaded, long total) {
                Log.d("XCloudProtocolAgent.testFtp", "ftp is transfering ...");
            }

            @Override // com.xcharge.common.utils.FtpUtils.TransferListener
            public void onTransferComplete() {
                Log.d("XCloudProtocolAgent.testFtp", "ftp transfer completed");
            }

            @Override // com.xcharge.common.utils.FtpUtils.TransferListener
            public void onTransferFail() {
                Log.w("XCloudProtocolAgent.testFtp", "ftp transfer failed");
            }

            @Override // com.xcharge.common.utils.FtpUtils.TransferListener
            public void onConnected() {
                Log.w("XCloudProtocolAgent.testFtp", "ftp connected");
            }

            @Override // com.xcharge.common.utils.FtpUtils.TransferListener
            public void onConnectFail() {
                Log.w("XCloudProtocolAgent.testFtp", "ftp connect failed");
            }
        });
    }
}
