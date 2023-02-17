package com.xcharge.charger.protocol.ocpp.handler;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.type.CHARGE_REFUSE_CAUSE;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.device.MobileNet;
import com.xcharge.charger.data.bean.device.Network;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.PHASE;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SoftwareStatusCacheProvider;
import com.xcharge.charger.data.proxy.AuthInfo;
import com.xcharge.charger.data.proxy.AuthInfoProxy;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.data.proxy.NetworkStatusObserver;
import com.xcharge.charger.protocol.monitor.bean.YZXDCAPOption;
import com.xcharge.charger.protocol.ocpp.bean.AuthCache;
import com.xcharge.charger.protocol.ocpp.bean.OcppConfig;
import com.xcharge.charger.protocol.ocpp.bean.OcppMessage;
import com.xcharge.charger.protocol.ocpp.bean.SecureConfig;
import com.xcharge.charger.protocol.ocpp.bean.cloud.AuthorizeConf;
import com.xcharge.charger.protocol.ocpp.bean.cloud.BootNotificationConf;
import com.xcharge.charger.protocol.ocpp.bean.cloud.CancelReservationReq;
import com.xcharge.charger.protocol.ocpp.bean.cloud.ChangeAvailabilityReq;
import com.xcharge.charger.protocol.ocpp.bean.cloud.ChangeConfigurationReq;
import com.xcharge.charger.protocol.ocpp.bean.cloud.ClearChargingProfileReq;
import com.xcharge.charger.protocol.ocpp.bean.cloud.DataTransferReq;
import com.xcharge.charger.protocol.ocpp.bean.cloud.GetCompositeScheduleReq;
import com.xcharge.charger.protocol.ocpp.bean.cloud.GetConfigurationReq;
import com.xcharge.charger.protocol.ocpp.bean.cloud.HeartbeatConf;
import com.xcharge.charger.protocol.ocpp.bean.cloud.RemoteStartTransactionReq;
import com.xcharge.charger.protocol.ocpp.bean.cloud.RemoteStopTransactionReq;
import com.xcharge.charger.protocol.ocpp.bean.cloud.ReserveNowReq;
import com.xcharge.charger.protocol.ocpp.bean.cloud.SendLocalListReq;
import com.xcharge.charger.protocol.ocpp.bean.cloud.SetChargingProfileReq;
import com.xcharge.charger.protocol.ocpp.bean.cloud.TriggerMessageReq;
import com.xcharge.charger.protocol.ocpp.bean.device.AuthorizeReq;
import com.xcharge.charger.protocol.ocpp.bean.device.BootNotificationReq;
import com.xcharge.charger.protocol.ocpp.bean.device.CancelReservationConf;
import com.xcharge.charger.protocol.ocpp.bean.device.ChangeConfigurationConf;
import com.xcharge.charger.protocol.ocpp.bean.device.ClearCacheConf;
import com.xcharge.charger.protocol.ocpp.bean.device.ClearChargingProfileConf;
import com.xcharge.charger.protocol.ocpp.bean.device.DataTransferConf;
import com.xcharge.charger.protocol.ocpp.bean.device.DiagnosticsStatusNotificationReq;
import com.xcharge.charger.protocol.ocpp.bean.device.FirmwareStatusNotificationReq;
import com.xcharge.charger.protocol.ocpp.bean.device.GetCompositeScheduleConf;
import com.xcharge.charger.protocol.ocpp.bean.device.GetConfigurationConf;
import com.xcharge.charger.protocol.ocpp.bean.device.GetDiagnosticsConf;
import com.xcharge.charger.protocol.ocpp.bean.device.GetLocalListVersionConf;
import com.xcharge.charger.protocol.ocpp.bean.device.MeterValuesReq;
import com.xcharge.charger.protocol.ocpp.bean.device.RemoteStartTransactionConf;
import com.xcharge.charger.protocol.ocpp.bean.device.RemoteStopTransactionConf;
import com.xcharge.charger.protocol.ocpp.bean.device.ReserveNowConf;
import com.xcharge.charger.protocol.ocpp.bean.device.ResetConf;
import com.xcharge.charger.protocol.ocpp.bean.device.SendLocalListConf;
import com.xcharge.charger.protocol.ocpp.bean.device.SetChargingProfileConf;
import com.xcharge.charger.protocol.ocpp.bean.device.StartTransactionReq;
import com.xcharge.charger.protocol.ocpp.bean.device.StopTransactionReq;
import com.xcharge.charger.protocol.ocpp.bean.device.TriggerMessageConf;
import com.xcharge.charger.protocol.ocpp.bean.device.UnlockConnectorConf;
import com.xcharge.charger.protocol.ocpp.bean.types.AuthorizationData;
import com.xcharge.charger.protocol.ocpp.bean.types.AuthorizationStatus;
import com.xcharge.charger.protocol.ocpp.bean.types.ChargingProfile;
import com.xcharge.charger.protocol.ocpp.bean.types.ChargingProfileKindType;
import com.xcharge.charger.protocol.ocpp.bean.types.ChargingProfilePurposeType;
import com.xcharge.charger.protocol.ocpp.bean.types.ChargingSchedule;
import com.xcharge.charger.protocol.ocpp.bean.types.ChargingSchedulePeriod;
import com.xcharge.charger.protocol.ocpp.bean.types.ClearChargingProfileStatus;
import com.xcharge.charger.protocol.ocpp.bean.types.DataTransferStatus;
import com.xcharge.charger.protocol.ocpp.bean.types.IdTagInfo;
import com.xcharge.charger.protocol.ocpp.bean.types.KeyValue;
import com.xcharge.charger.protocol.ocpp.bean.types.Location;
import com.xcharge.charger.protocol.ocpp.bean.types.Measurand;
import com.xcharge.charger.protocol.ocpp.bean.types.MeterValue;
import com.xcharge.charger.protocol.ocpp.bean.types.Phase;
import com.xcharge.charger.protocol.ocpp.bean.types.ReadingContext;
import com.xcharge.charger.protocol.ocpp.bean.types.RecurrencyKindType;
import com.xcharge.charger.protocol.ocpp.bean.types.RegistrationStatus;
import com.xcharge.charger.protocol.ocpp.bean.types.ReservationStatus;
import com.xcharge.charger.protocol.ocpp.bean.types.SampledValue;
import com.xcharge.charger.protocol.ocpp.bean.types.TransferType;
import com.xcharge.charger.protocol.ocpp.bean.types.TriggerMessageStatus;
import com.xcharge.charger.protocol.ocpp.bean.types.UnitOfMeasure;
import com.xcharge.charger.protocol.ocpp.bean.types.UpdateStatus;
import com.xcharge.charger.protocol.ocpp.bean.types.UpdateType;
import com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway;
import com.xcharge.charger.protocol.ocpp.session.OcppChargeSession;
import com.xcharge.charger.protocol.ocpp.session.OcppRequestSession;
import com.xcharge.charger.protocol.ocpp.session.OcppUpgradeSession;
import com.xcharge.charger.protocol.ocpp.type.OCPP_REQUEST_STATE;
import com.xcharge.charger.ui.adapter.api.UIServiceProxy;
import com.xcharge.charger.ui.adapter.type.CHARGE_UI_STAGE;
import com.xcharge.charger.ui.adapter.type.UI_MODE;
import com.xcharge.common.utils.HandlerTimer;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.TimeUtils;
import it.sauronsoftware.ftp4j.FTPCodes;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collections;
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
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.apache.commons.lang3.CharEncoding;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.protocols.Protocol;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class OcppProtocolAgent extends IoHandlerAdapter {
    private static final int BOOT_INTERVAL = 30;
    private static final int HERAT_BEAT_INTERVAL = 1800;
    public static final int MSG_BOOT_NOTIFICATION_TIMER = 69641;
    public static final int MSG_CONNECTED = 69634;
    public static final int MSG_CONNECT_ERROR = 69636;
    public static final int MSG_DISCONNECTED = 69635;
    public static final int MSG_HERAT_BEAT_TIMER = 69648;
    public static final int MSG_INIT_CONNECTION = 69633;
    public static final int MSG_PING_TIMER = 69650;
    public static final int MSG_RECEIVED = 69639;
    public static final int MSG_RESERVE_NOW_TIMER = 69649;
    public static final int MSG_SECOND_TIMER = 69640;
    public static final int MSG_SEND = 69637;
    public static final int TIMEOUT_CONNECT = 10;
    public static final int TIMEOUT_RESPONSE = 15;
    public static final int TIMEOUT_SEND = 5;
    private static OcppProtocolAgent instance = null;
    public WebSocketClient webSocketClient;
    private int failHeartBeatCnt = 0;
    private boolean isConnected = false;
    private AtomicLong seqGen = new AtomicLong(0);
    private OcppConfig configCache = null;
    private String bootStatus = "Rejected";
    private boolean isResetOnTxStopped = false;
    private HandlerThread thread = null;
    private MsgHandler handler = null;
    private HandlerTimer handlerTimer = null;
    private Context context = null;
    private ThreadPoolExecutor connectThreadPoolExecutor = null;
    private OcppUpgradeSession upgradeSession = null;
    private HashMap<String, OcppPortHandler> portHandlers = null;
    private HashMap<String, SendRequestState> sendReqestState = null;
    private HashMap<Integer, JSONArray> reserveNow = null;
    private NetworkStatusObserver networkStatusObserver = null;

    public static OcppProtocolAgent getInstance() {
        if (instance == null) {
            instance = new OcppProtocolAgent();
        }
        return instance;
    }

    public HashMap<Integer, JSONArray> getReserveNow() {
        return this.reserveNow;
    }

    public void setReserveNow(HashMap<Integer, JSONArray> reserveNow) {
        this.reserveNow = reserveNow;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class SendRequestState {
        JSONArray request;
        OCPP_REQUEST_STATE status;
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

    public String getBootStatus() {
        return this.bootStatus;
    }

    public void setBootStatus(String bootStatus) {
        this.bootStatus = bootStatus;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class MsgHandler extends Handler {
        public MsgHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARN: Not initialized variable reg: 11, insn: 0x0581: MOVE  (r10 I:??[OBJECT, ARRAY]) = (r11 I:??[OBJECT, ARRAY] A[D('jsonArray' org.json.JSONArray)]), block:B:88:0x0581 */
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            JSONArray jsonArray;
            try {
            } catch (Exception e) {
                e = e;
            }
            try {
                switch (msg.what) {
                    case 69633:
                        OcppProtocolAgent.this.connect();
                        break;
                    case 69634:
                        Log.i("OcppProtocolAgent.handleMessage", "connected !!!");
                        LogUtils.cloudlog("ocpp cloud connected !!!");
                        OcppProtocolAgent.this.isConnected = true;
                        ChargeStatusCacheProvider.getInstance().updateCloudConnected(true);
                        OcppProtocolAgent.this.handlerTimer.startTimer(1000L, 69640, null);
                        OcppProtocolAgent.this.portsActive();
                        OcppProtocolAgent.this.sendBootNotificationReq();
                        OcppProtocolAgent.this.sendPing();
                        break;
                    case 69635:
                        Log.i("OcppProtocolAgent.handleMessage", "disconnected !!!");
                        LogUtils.cloudlog("ocpp cloud disconnected !!!");
                        OcppProtocolAgent.this.isConnected = false;
                        ChargeStatusCacheProvider.getInstance().updateCloudConnected(false);
                        OcppProtocolAgent.this.handlerTimer.stopTimer(69640);
                        OcppProtocolAgent.this.portsDeactive();
                        OcppProtocolAgent.this.handler.sendEmptyMessageDelayed(69633, 5000L);
                        break;
                    case 69636:
                        Log.i("OcppProtocolAgent.handleMessage", "failed to connect !!!");
                        if (HardwareStatusCacheProvider.getInstance().isNetworkConnected()) {
                            LogUtils.applog("failed to connect to ocpp cloud, try to diagnosis network connectivity ...");
                            new Thread(new Runnable() { // from class: com.xcharge.charger.protocol.ocpp.handler.OcppProtocolAgent.MsgHandler.1
                                @Override // java.lang.Runnable
                                public void run() {
                                    DCAPProxy.getInstance().networkConnectivityDiagnosis();
                                }
                            }).start();
                        }
                        OcppProtocolAgent.this.handler.sendEmptyMessageDelayed(69633, 20000L);
                        break;
                    case 69637:
                        String ocppMessage = (String) msg.obj;
                        jsonArray = new JSONArray(ocppMessage);
                        Log.d("OcppProtocolAgent.handleMessage", "send ocpp msg: " + ocppMessage);
                        if (OcppProtocolAgent.this.isConnected) {
                            if (jsonArray.getInt(0) == 2) {
                                if (!jsonArray.getString(2).equals("BootNotification")) {
                                    if (!"Accepted".equals(OcppProtocolAgent.this.bootStatus)) {
                                        if (RegistrationStatus.Pending.equals(OcppProtocolAgent.this.bootStatus) && jsonArray.length() == 5) {
                                            JSONObject attach = jsonArray.getJSONObject(4);
                                            if (OcppMessage.TriggerMessage.equals(attach.getString("type"))) {
                                                SendRequestState reqestState = new SendRequestState(null);
                                                reqestState.request = jsonArray;
                                                reqestState.status = OCPP_REQUEST_STATE.sended;
                                                reqestState.timestamp = System.currentTimeMillis();
                                                OcppProtocolAgent.this.sendReqestState.put(jsonArray.getString(1), reqestState);
                                                JSONArray reJsonArray = new JSONArray();
                                                for (int i = 0; i < jsonArray.length() - 1; i++) {
                                                    reJsonArray.put(jsonArray.get(i));
                                                }
                                                OcppProtocolAgent.this.webSocketClient.send(reJsonArray.toString());
                                                break;
                                            }
                                        }
                                        break;
                                    } else {
                                        SendRequestState reqestState2 = new SendRequestState(null);
                                        reqestState2.request = jsonArray;
                                        reqestState2.status = OCPP_REQUEST_STATE.sended;
                                        reqestState2.timestamp = System.currentTimeMillis();
                                        OcppProtocolAgent.this.sendReqestState.put(jsonArray.getString(1), reqestState2);
                                        if (jsonArray.length() == 5) {
                                            JSONArray reJsonArray2 = new JSONArray();
                                            for (int i2 = 0; i2 < jsonArray.length() - 1; i2++) {
                                                reJsonArray2.put(jsonArray.get(i2));
                                            }
                                            OcppProtocolAgent.this.webSocketClient.send(reJsonArray2.toString());
                                            break;
                                        } else {
                                            OcppProtocolAgent.this.webSocketClient.send(ocppMessage);
                                            break;
                                        }
                                    }
                                } else {
                                    SendRequestState reqestState3 = new SendRequestState(null);
                                    reqestState3.request = jsonArray;
                                    reqestState3.status = OCPP_REQUEST_STATE.sended;
                                    reqestState3.timestamp = System.currentTimeMillis();
                                    OcppProtocolAgent.this.sendReqestState.put(jsonArray.getString(1), reqestState3);
                                    OcppProtocolAgent.this.webSocketClient.send(ocppMessage);
                                    break;
                                }
                            } else if (jsonArray.getInt(0) == 3) {
                                OcppProtocolAgent.this.webSocketClient.send(ocppMessage);
                                break;
                            }
                        } else if (jsonArray.getInt(0) == 2) {
                            OcppProtocolAgent.this.handleSendRequestFail(jsonArray);
                            break;
                        }
                        break;
                    case 69639:
                        String ocppMessage2 = (String) msg.obj;
                        jsonArray = new JSONArray(ocppMessage2);
                        Log.d("OcppProtocolAgent.handleMessage", "received ocpp msg: " + ocppMessage2);
                        if (jsonArray.getInt(0) == 3) {
                            String uid = jsonArray.getString(1);
                            SendRequestState reqestState4 = (SendRequestState) OcppProtocolAgent.this.sendReqestState.get(uid);
                            if (reqestState4 != null) {
                                JSONArray request = reqestState4.request;
                                OcppProtocolAgent.this.sendReqestState.remove(uid);
                                OcppProtocolAgent.this.dispatchOcppMessage(jsonArray, request);
                                break;
                            } else {
                                Log.w("OcppProtocolAgent.handleMessage", "maybe timeout to wait for response msg: " + jsonArray.toString());
                                break;
                            }
                        } else if (jsonArray.getInt(0) == 2) {
                            if (!"Accepted".equals(OcppProtocolAgent.this.bootStatus)) {
                                if (!RegistrationStatus.Pending.equals(OcppProtocolAgent.this.bootStatus)) {
                                    Log.w("OcppProtocolAgent.handleMessage", "boot status is Rejected");
                                    break;
                                } else {
                                    if (!OcppProtocolAgent.this.isChargeMsg(jsonArray.getString(2))) {
                                        OcppProtocolAgent.this.dispatchOcppMessage(jsonArray, null);
                                        break;
                                    }
                                    break;
                                }
                            } else {
                                OcppProtocolAgent.this.dispatchOcppMessage(jsonArray, null);
                                break;
                            }
                        } else {
                            Log.w("OcppProtocolAgent.handleMessage", "receive illegal msg: " + jsonArray.toString());
                            break;
                        }
                    case 69640:
                        OcppProtocolAgent.this.requestTimeoutCheck();
                        OcppProtocolAgent.this.handlerTimer.startTimer(1000L, 69640, null);
                        break;
                    case 69641:
                        OcppProtocolAgent.this.handlerTimer.stopTimer(69641);
                        if (OcppProtocolAgent.this.isConnected) {
                            OcppProtocolAgent.this.sendBootNotificationReq();
                            break;
                        }
                        break;
                    case 69648:
                        int interval = ((Integer) msg.obj).intValue();
                        Log.d("OcppProtocolAgent.MsgHandler", "send ocpp heart beat periodically !!!");
                        if (OcppProtocolAgent.this.sendReqestState.size() == 0) {
                            OcppProtocolAgent.this.sendHeartbeatReq(false);
                        }
                        OcppProtocolAgent.this.handlerTimer.startTimer(interval * 1000, 69648, Integer.valueOf(interval));
                        break;
                    case 69649:
                        int reservationId = ((Integer) msg.obj).intValue();
                        OcppProtocolAgent.this.reserveNow.remove(Integer.valueOf(reservationId));
                        break;
                    case 69650:
                        int pingInterval = ((Integer) msg.obj).intValue();
                        if (OcppProtocolAgent.this.isConnected) {
                            OcppProtocolAgent.this.webSocketClient.sendPing();
                            OcppProtocolAgent.this.handlerTimer.startTimer(pingInterval * 1000, 69650, Integer.valueOf(pingInterval));
                            break;
                        }
                        break;
                    case 135169:
                        Uri uri = (Uri) msg.obj;
                        OcppProtocolAgent.this.handleNetworkStatusChanged(uri);
                        break;
                }
            } catch (Exception e2) {
                e = e2;
                Log.e("OcppProtocolAgent.handleMessage", "except: " + Log.getStackTraceString(e));
                LogUtils.syslog("OcppProtocolAgent handleMessage exception: " + Log.getStackTraceString(e));
                super.handleMessage(msg);
            }
            super.handleMessage(msg);
        }
    }

    public void init(Context context) {
        this.context = context;
        if (TextUtils.isEmpty(RemoteSettingCacheProvider.getInstance().getProtocolTimezone())) {
            RemoteSettingCacheProvider.getInstance().updateProtocolTimezone("+00:00");
        }
        this.portHandlers = new HashMap<>();
        HashMap<String, Port> ports = HardwareStatusCacheProvider.getInstance().getPorts();
        if (ports != null) {
            for (String port : ports.keySet()) {
                OcppPortHandler portHandler = new OcppPortHandler();
                portHandler.init(context, port, this);
                this.portHandlers.put(port, portHandler);
            }
        }
        this.connectThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue(1), new RejectedExecutionHandler() { // from class: com.xcharge.charger.protocol.ocpp.handler.OcppProtocolAgent.1
            @Override // java.util.concurrent.RejectedExecutionHandler
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                Log.e("OcppProtocolAgent.ThreadPoolExecutor.rejectedExecution", "rejected runnable: " + r.toString() + ", active runnables: " + executor.getActiveCount());
            }
        });
        this.sendReqestState = new HashMap<>();
        this.reserveNow = new HashMap<>();
        this.thread = new HandlerThread("OcppProtocolAgent", 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(context);
        this.networkStatusObserver = new NetworkStatusObserver(context, this.handler);
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor(Network.class.getSimpleName()), true, this.networkStatusObserver);
        this.configCache = new OcppConfig();
        this.configCache.init(this.context);
        LogUploadAgent.getInstance().init(this.context);
    }

    public void destroy() {
        this.context.getContentResolver().unregisterContentObserver(this.networkStatusObserver);
        disconnect();
        LogUploadAgent.getInstance().destroy();
        this.handlerTimer.destroy();
        this.handler.removeMessages(69633);
        this.handler.removeMessages(69634);
        this.handler.removeMessages(69635);
        this.handler.removeMessages(69636);
        this.handler.removeMessages(69637);
        this.handler.removeMessages(69639);
        this.handler.removeMessages(69640);
        this.handler.removeMessages(69641);
        this.handler.removeMessages(69648);
        this.handler.removeMessages(69649);
        this.handler.removeMessages(69650);
        this.thread.quit();
        for (OcppPortHandler portHandler : this.portHandlers.values()) {
            portHandler.destroy();
        }
        this.portHandlers.clear();
        this.sendReqestState.clear();
        this.reserveNow.clear();
        this.connectThreadPoolExecutor.shutdown();
    }

    public void initConnection() {
        this.handler.sendEmptyMessage(69633);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class ConnectTask implements Runnable {
        private ConnectTask() {
        }

        /* synthetic */ ConnectTask(OcppProtocolAgent ocppProtocolAgent, ConnectTask connectTask) {
            this();
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                if (OcppProtocolAgent.this.configCache.isWsDebug()) {
                    WebSocketImpl.DEBUG = true;
                }
                String url = OcppProtocolAgent.this.configCache.getUrl();
                if (url.endsWith(MqttTopic.TOPIC_LEVEL_SEPARATOR)) {
                    url = String.valueOf(url) + HardwareStatusCacheProvider.getInstance().getSn();
                }
                URI uri = new URI(url);
                Map<String, String> httpHeaders = new HashMap<>();
                httpHeaders.put("Sec-WebSocket-Protocol", "ocpp1.6");
                Draft draft = new Draft_6455(Collections.emptyList(), Collections.singletonList(new Protocol("ocpp1.6")));
                OcppProtocolAgent.this.webSocketClient = new WebSocketClient(uri, draft, httpHeaders, 10000) { // from class: com.xcharge.charger.protocol.ocpp.handler.OcppProtocolAgent.ConnectTask.1
                    @Override // org.java_websocket.client.WebSocketClient
                    public void onClose(int code, String reason, boolean remote) {
                        Log.d("ConnectTask.onClose", "connection closed by " + (remote ? "remote peer" : "us") + ", code: " + code + ", reason: " + reason);
                        OcppProtocolAgent.this.handler.sendEmptyMessage(69635);
                    }

                    @Override // org.java_websocket.client.WebSocketClient
                    public void onError(Exception ex) {
                        Log.e("ConnectTask.onError", Log.getStackTraceString(ex));
                    }

                    @Override // org.java_websocket.client.WebSocketClient
                    public void onMessage(String message) {
                        Log.d("ConnectTask.onMessage", "receive msg: " + message);
                        OcppProtocolAgent.this.handler.sendMessage(OcppProtocolAgent.this.handler.obtainMessage(69639, message));
                    }

                    @Override // org.java_websocket.client.WebSocketClient
                    public void onOpen(ServerHandshake handshake) {
                        String content = null;
                        try {
                            String content2 = new String(handshake.getContent(), Charset.forName(CharEncoding.UTF_8));
                            content = content2;
                        } catch (Throwable th) {
                        }
                        Log.d("ConnectTask.onOpen", "opened, status: " + ((int) handshake.getHttpStatus()) + ", msg: " + handshake.getHttpStatusMessage() + ", content: " + content);
                        OcppProtocolAgent.this.handler.sendEmptyMessage(69634);
                    }

                    @Override // org.java_websocket.WebSocketAdapter, org.java_websocket.WebSocketListener
                    public void onWebsocketPing(WebSocket conn, Framedata f) {
                        super.onWebsocketPing(conn, f);
                        Log.d("ConnectTask.onPing", OcppProtocolAgent.this.parseFramedata(f));
                    }

                    @Override // org.java_websocket.WebSocketAdapter, org.java_websocket.WebSocketListener
                    public void onWebsocketPong(WebSocket conn, Framedata f) {
                        super.onWebsocketPong(conn, f);
                        Log.d("ConnectTask.onPong", OcppProtocolAgent.this.parseFramedata(f));
                    }
                };
                String scheme = uri.getScheme();
                if ("wss".equals(scheme)) {
                    SecureConfig secureConfig = OcppProtocolAgent.this.configCache.getSecureConfig();
                    String clientKeystorePath = secureConfig.getClientKeystorePath();
                    String clientKeystoreType = secureConfig.getClientKeystoreType();
                    String clientKeystorePassword = secureConfig.getClientKeystorePassword();
                    String clientKeyPassword = secureConfig.getClientKeyPassword();
                    String serverTrustKeystorePath = secureConfig.getServerTrustKeystorePath();
                    String serverTrustKeystoreType = secureConfig.getServerTrustKeystoreType();
                    String serverTrustKeystorePassword = secureConfig.getServerTrustKeystorePassword();
                    KeyManager[] km = null;
                    TrustManager[] tm = null;
                    if (!TextUtils.isEmpty(clientKeystorePath)) {
                        KeyStore clientKS = KeyStore.getInstance(clientKeystoreType);
                        File clientKF = new File(clientKeystorePath);
                        clientKS.load(new FileInputStream(clientKF), TextUtils.isEmpty(clientKeystorePassword) ? null : clientKeystorePassword.toCharArray());
                        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                        kmf.init(clientKS, TextUtils.isEmpty(clientKeyPassword) ? null : clientKeyPassword.toCharArray());
                        km = kmf.getKeyManagers();
                    }
                    if (!TextUtils.isEmpty(serverTrustKeystorePath)) {
                        KeyStore trustKS = KeyStore.getInstance(serverTrustKeystoreType);
                        File truetKF = new File(serverTrustKeystorePath);
                        trustKS.load(new FileInputStream(truetKF), TextUtils.isEmpty(serverTrustKeystorePassword) ? null : serverTrustKeystorePassword.toCharArray());
                        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                        tmf.init(trustKS);
                        tm = tmf.getTrustManagers();
                    }
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(km, tm, null);
                    SSLSocketFactory factory = sslContext.getSocketFactory();
                    OcppProtocolAgent.this.webSocketClient.setSocket(factory.createSocket());
                }
                OcppProtocolAgent.this.webSocketClient.connect();
            } catch (Exception e) {
                OcppProtocolAgent.this.handler.sendEmptyMessage(69636);
                e.printStackTrace();
            }
        }
    }

    public String parseFramedata(Framedata framedata) {
        byte[] data;
        ByteBuffer buffer = framedata.getPayloadData();
        if (buffer == null || (data = buffer.array()) == null || data.length <= 0) {
            return "null";
        }
        String result = new String(data);
        return result;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void connect() {
        if (HardwareStatusCacheProvider.getInstance().isNetworkConnected()) {
            Log.i("OcppProtocolAgent.connect", "init connection !!!");
            this.connectThreadPoolExecutor.execute(new ConnectTask(this, null));
            return;
        }
        this.handler.sendEmptyMessageDelayed(69633, 5000L);
    }

    public void disconnect() {
        if (this.webSocketClient != null) {
            Log.d("OcppProtocolAgent.disconnect", "force to disconnect !!!");
            this.webSocketClient.close();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleNetworkStatusChanged(Uri uri) {
        Log.i("OcppProtocolAgent.handleNetworkStatusChanged", "network status changed, uri: " + uri.toString());
        String lastSegment = uri.getLastPathSegment();
        if (!"connected".equals(lastSegment) && "disconnected".equals(lastSegment)) {
            disconnect();
        }
    }

    public OcppChargeSession getChargeSession(String ocppPort) {
        OcppPortHandler ocppPortHandler = getPortHandler(ocppPort);
        if (ocppPortHandler == null) {
            Log.w("OcppProtocolAgent.getChargeSession", "no available port handler for port: " + ocppPort);
            return null;
        }
        return ocppPortHandler.getChargeSession();
    }

    public OcppUpgradeSession getUpgradeSession() {
        if (this.upgradeSession == null) {
            this.upgradeSession = new OcppUpgradeSession();
        }
        return this.upgradeSession;
    }

    public String getPort(String chargeId) {
        for (OcppPortHandler portHandler : this.portHandlers.values()) {
            String port = portHandler.getPort(chargeId);
            if (!TextUtils.isEmpty(port)) {
                return port;
            }
        }
        return null;
    }

    public JSONArray getReserveNow(String idTag) {
        try {
            if (this.reserveNow != null && this.reserveNow.size() > 0) {
                for (Map.Entry<Integer, JSONArray> entry : this.reserveNow.entrySet()) {
                    int key = entry.getKey().intValue();
                    JSONArray jsonArray = entry.getValue();
                    ReserveNowReq reserveNowReq = new ReserveNowReq().fromJson(jsonArray.getJSONObject(3).toString());
                    if (key == reserveNowReq.getReservationId()) {
                        return jsonArray;
                    }
                }
            }
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.getReserveNow", Log.getStackTraceString(e));
        }
        return null;
    }

    public boolean isResetOnTxStopped() {
        return this.isResetOnTxStopped;
    }

    public void setResetOnTxStopped(boolean isResetOnTxStopped) {
        this.isResetOnTxStopped = isResetOnTxStopped;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendPing() {
        int pingInterval;
        String webSocketPingInterval = this.configCache.getMaps().get(OcppMessage.WebSocketPingInterval);
        if (!TextUtils.isEmpty(webSocketPingInterval) && TextUtils.isDigitsOnly(webSocketPingInterval) && (pingInterval = Integer.parseInt(webSocketPingInterval)) > 0) {
            this.handlerTimer.stopTimer(69650);
            this.handlerTimer.startTimer(pingInterval * 1000, 69650, Integer.valueOf(pingInterval));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendBootNotificationReq() {
        try {
            BootNotificationReq bootNotificationReq = new BootNotificationReq();
            bootNotificationReq.setChargePointSerialNumber(HardwareStatusCacheProvider.getInstance().getSn());
            bootNotificationReq.setChargePointModel(this.configCache.getChargePointModel());
            bootNotificationReq.setChargePointVendor(this.configCache.getChargePointVendor());
            bootNotificationReq.setFirmwareVersion(String.valueOf(SoftwareStatusCacheProvider.getInstance().getFirewareVer()) + "-" + SoftwareStatusCacheProvider.getInstance().getAppVer());
            MobileNet mobileNet = HardwareStatusCacheProvider.getInstance().getMobileNetStatus();
            if (mobileNet != null) {
                String IMSI = mobileNet.getIMSI();
                if (!TextUtils.isEmpty(IMSI)) {
                    bootNotificationReq.setImsi(IMSI);
                }
                String ICCID = mobileNet.getICCID();
                if (!TextUtils.isEmpty(ICCID)) {
                    bootNotificationReq.setIccid(ICCID);
                }
            }
            JSONArray jsonArray = createReqMessage(genSeq(), "BootNotification", bootNotificationReq.toJson());
            sendMessage(jsonArray);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.sendBootNotificationReq", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendHeartbeatReq(boolean isTrigger) {
        try {
            JSONArray jsonArray = createReqMessage(genSeq(), "Heartbeat", "{}");
            if (isTrigger) {
                JSONObject attach = new JSONObject();
                attach.put("type", OcppMessage.TriggerMessage);
                jsonArray.put(attach);
            }
            sendMessage(jsonArray);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.sendHeartbeatReq", Log.getStackTraceString(e));
        }
    }

    public void sendFirmwareStatusNotificationReq(Boolean isTrigger, String status) {
        try {
            FirmwareStatusNotificationReq firmwareStatusNotificationReq = new FirmwareStatusNotificationReq();
            firmwareStatusNotificationReq.setStatus(status);
            JSONArray jsonArray = createReqMessage(genSeq(), "FirmwareStatusNotification", firmwareStatusNotificationReq.toJson());
            if (isTrigger.booleanValue()) {
                JSONObject attach = new JSONObject();
                attach.put("type", OcppMessage.TriggerMessage);
                jsonArray.put(attach);
            }
            sendMessage(jsonArray);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.sendFirmwareStatusNotificationReq", Log.getStackTraceString(e));
        }
    }

    public void sendDiagnosticsStatusNotificationReq(Boolean isTrigger, String status) {
        try {
            DiagnosticsStatusNotificationReq diagnosticsStatusNotificationReq = new DiagnosticsStatusNotificationReq();
            diagnosticsStatusNotificationReq.setStatus(status);
            JSONArray jsonArray = createReqMessage(genSeq(), "DiagnosticsStatusNotification", diagnosticsStatusNotificationReq.toJson());
            if (isTrigger.booleanValue()) {
                JSONObject attach = new JSONObject();
                attach.put("type", OcppMessage.TriggerMessage);
                jsonArray.put(attach);
            }
            sendMessage(jsonArray);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.sendDiagnosticsStatusNotificationReq", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchOcppMessage(JSONArray response, JSONArray request) {
        if (request == null) {
            handleRequestMessage(response);
        } else {
            handleResponseMessage(response, request);
        }
    }

    public void handleRequestMessage(JSONArray jsonArray) {
        try {
            String string = jsonArray.getString(2);
            switch (string.hashCode()) {
                case -1802670779:
                    if (string.equals(OcppMessage.GetLocalListVersion)) {
                        handleGetLocalListVersionReq(jsonArray);
                        return;
                    }
                    return;
                case -1522526763:
                    if (string.equals(OcppMessage.DataTransfer)) {
                        return;
                    }
                    return;
                case -1013156522:
                    if (string.equals(OcppMessage.SetChargingProfile)) {
                        handleSetChargingProfileReq(jsonArray);
                        return;
                    }
                    return;
                case -977722974:
                    if (string.equals(OcppMessage.RemoteStartTransaction)) {
                        String port = String.valueOf(new RemoteStartTransactionReq().fromJson(jsonArray.getJSONObject(3).toString()).getConnectorId());
                        OcppPortHandler portHandler = getPortHandler(port);
                        if (portHandler != null) {
                            portHandler.sendMessage(portHandler.obtainMessage(73735, jsonArray));
                            return;
                        }
                        sendRemoteStartTransactionConf(jsonArray.getString(1), "Rejected");
                        Log.w("OcppProtocolAgent.handleRequestMessage", "unsupported port in ack: " + jsonArray.toString());
                        return;
                    }
                    return;
                case -964284085:
                    if (string.equals(OcppMessage.ChangeAvailability)) {
                        ChangeAvailabilityReq changeAvailabilityReq = new ChangeAvailabilityReq().fromJson(jsonArray.getJSONObject(3).toString());
                        int connectorId = changeAvailabilityReq.getConnectorId();
                        if (connectorId != 0) {
                            OcppPortHandler portHandler2 = getPortHandler(String.valueOf(connectorId));
                            if (portHandler2 != null) {
                                portHandler2.sendMessage(portHandler2.obtainMessage(73735, jsonArray));
                                return;
                            } else {
                                Log.w("OcppProtocolAgent.handleRequestMessage", "unsupported port in ack: " + jsonArray);
                                return;
                            }
                        }
                        int portNum = HardwareStatusCacheProvider.getInstance().getPorts().size();
                        for (int i = 1; i <= portNum; i++) {
                            OcppPortHandler portHandler3 = getPortHandler(String.valueOf(i));
                            if (portHandler3 != null) {
                                portHandler3.sendMessage(portHandler3.obtainMessage(73735, jsonArray));
                            } else {
                                Log.w("OcppProtocolAgent.handleRequestMessage", "unsupported port in ack: " + jsonArray);
                            }
                        }
                        return;
                    }
                    return;
                case -687158844:
                    if (string.equals(OcppMessage.UpdateFirmware)) {
                        sendUpdateFirmwareConf(jsonArray.getString(1));
                        OcppDCAPGateway.getInstance().sendMessage(OcppDCAPGateway.getInstance().obtainMessage(77827, jsonArray));
                        return;
                    }
                    return;
                case -595338935:
                    if (string.equals(OcppMessage.UnlockConnector)) {
                        OcppDCAPGateway.getInstance().sendMessage(OcppDCAPGateway.getInstance().obtainMessage(77827, jsonArray));
                        return;
                    }
                    return;
                case -391574318:
                    if (string.equals(OcppMessage.CancelReservation)) {
                        handleCancelReservationReq(jsonArray);
                        return;
                    }
                    return;
                case -362842058:
                    if (string.equals(OcppMessage.RemoteStopTransaction)) {
                        String transactionId = String.valueOf(new RemoteStopTransactionReq().fromJson(jsonArray.getJSONObject(3).toString()).getTransactionId());
                        ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBillcloud(transactionId);
                        if (chargeBill == null) {
                            sendRemoteStopTransactionConf(jsonArray.getString(1), "Rejected");
                            return;
                        }
                        OcppPortHandler portHandler4 = getPortHandler(chargeBill.getPort());
                        if (portHandler4 != null) {
                            portHandler4.sendMessage(portHandler4.obtainMessage(73735, jsonArray));
                            return;
                        }
                        sendRemoteStopTransactionConf(jsonArray.getString(1), "Rejected");
                        Log.w("OcppProtocolAgent.handleRequestMessage", "unsupported port in ack: " + jsonArray.toString());
                        return;
                    }
                    return;
                case -212893685:
                    if (string.equals(OcppMessage.ClearChargingProfile)) {
                        handleClearChargingProfileReq(jsonArray);
                        return;
                    }
                    return;
                case -199307295:
                    if (string.equals(OcppMessage.SendLocalList)) {
                        handleSendLocalListReq(jsonArray);
                        return;
                    }
                    return;
                case -27822752:
                    if (string.equals(OcppMessage.GetConfiguration)) {
                        handleGetConfigurationReq(jsonArray);
                        return;
                    }
                    return;
                case 78851375:
                    if (string.equals(OcppMessage.Reset)) {
                        OcppDCAPGateway.getInstance().sendMessage(OcppDCAPGateway.getInstance().obtainMessage(77827, jsonArray));
                        return;
                    }
                    return;
                case 167096143:
                    if (string.equals(OcppMessage.TriggerMessage)) {
                        handleTriggerMessageReq(jsonArray);
                        return;
                    }
                    return;
                case 280557722:
                    if (string.equals(OcppMessage.ReserveNow)) {
                        handleReserveNowReq(jsonArray);
                        return;
                    }
                    return;
                case 310433542:
                    if (string.equals(OcppMessage.ChangeConfiguration)) {
                        handleChangeConfigurationReq(jsonArray);
                        return;
                    }
                    return;
                case 1234226517:
                    if (string.equals(OcppMessage.ClearCache)) {
                        handleClearCacheReq(jsonArray);
                        return;
                    }
                    return;
                case 1850781096:
                    if (string.equals(OcppMessage.GetCompositeSchedule)) {
                        return;
                    }
                    return;
                case 1912319126:
                    if (string.equals(OcppMessage.GetDiagnostics)) {
                        handleGetDiagnosticsReq(jsonArray);
                        return;
                    }
                    return;
                default:
                    return;
            }
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.handleRequestMessage", Log.getStackTraceString(e));
        }
    }

    private void handleReserveNowReq(JSONArray jsonArray) {
        try {
            ReserveNowReq reserveNowReq = new ReserveNowReq().fromJson(jsonArray.getJSONObject(3).toString());
            String port = String.valueOf(reserveNowReq.getConnectorId());
            boolean isSend = true;
            String status = "Rejected";
            boolean isSupported = false;
            String reserveConnectorZeroSupported = this.configCache.getMaps().get(OcppMessage.ReserveConnectorZeroSupported);
            if (!TextUtils.isEmpty(reserveConnectorZeroSupported)) {
                isSupported = Boolean.parseBoolean(reserveConnectorZeroSupported);
            }
            Log.w("OcppProtocolAgent.handleReserveNowReq", "port:" + port + ", isSupported:" + isSupported);
            if (port.equals("0") && isSupported) {
                port = "1";
            } else if (port.equals("0") && !isSupported) {
                ReserveNowConf reserveNowConf = new ReserveNowConf();
                reserveNowConf.setStatus("Rejected");
                JSONArray jsonArrayConf = createConfMessage(jsonArray.getString(1), OcppMessage.ReserveNow, reserveNowConf.toJson());
                sendMessage(jsonArrayConf);
                return;
            }
            if (ChargeStatusCacheProvider.getInstance().getPortSwitch(port)) {
                HashMap<String, ErrorCode> errors = HardwareStatusCacheProvider.getInstance().getAllDeviceErrors(port);
                if (errors == null || errors.size() < 1) {
                    OcppPortHandler portHandler = getPortHandler(port);
                    if (this.reserveNow.get(Integer.valueOf(reserveNowReq.getReservationId())) != null || (portHandler != null && !TextUtils.isEmpty(portHandler.getChargeSession().getCharge_id()))) {
                        status = ReservationStatus.Occupied;
                        Bundle data = new Bundle();
                        data.putString("stage", CHARGE_UI_STAGE.refuse.getStage());
                        data.putString("cause", CHARGE_REFUSE_CAUSE.BUSY.getCause());
                        UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
                    } else {
                        isSend = false;
                        this.reserveNow.put(Integer.valueOf(reserveNowReq.getReservationId()), jsonArray);
                        boolean authStatus = localAuthReserveNow(reserveNowReq.getIdTag(), reserveNowReq.getParentIdTag());
                        long time = TimeUtils.getTsFromISO8601Format(reserveNowReq.getExpiryDate()) - System.currentTimeMillis();
                        Log.w("OcppProtocolAgent.handleReserveNowReq", "localAuthStatus:" + authStatus + ", time:" + time);
                        if (authStatus && time >= 0) {
                            OcppDCAPGateway.getInstance().sendMessage(OcppDCAPGateway.getInstance().obtainMessage(77827, jsonArray));
                            this.handlerTimer.startTimer(time, 69649, Integer.valueOf(reserveNowReq.getReservationId()));
                        } else {
                            sendAuthorizeReq(reserveNowReq.getIdTag());
                        }
                    }
                } else {
                    status = "Faulted";
                }
            } else {
                status = "Unavailable";
            }
            if (isSend) {
                ReserveNowConf reserveNowConf2 = new ReserveNowConf();
                reserveNowConf2.setStatus(status);
                JSONArray jsonArrayConf2 = createConfMessage(jsonArray.getString(1), OcppMessage.ReserveNow, reserveNowConf2.toJson());
                sendMessage(jsonArrayConf2);
            }
        } catch (Exception e) {
            Log.w("OcppPortHandler.handleReserveNowReq", Log.getStackTraceString(e));
        }
    }

    private JSONArray sendAuthorizeReq(String idTag) {
        try {
            AuthorizeReq authorizeReq = new AuthorizeReq();
            authorizeReq.setIdTag(idTag);
            JSONArray jsonArray = createReqMessage(genSeq(), OcppMessage.Authorize, authorizeReq.toJson());
            sendMessage(jsonArray);
            return jsonArray;
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.sendAuthorizeReq", Log.getStackTraceString(e));
            return null;
        }
    }

    private void handleCancelReservationReq(JSONArray jsonArray) {
        try {
            CancelReservationReq cancelReservationReq = new CancelReservationReq().fromJson(jsonArray.getJSONObject(3).toString());
            String status = "Rejected";
            JSONArray reserve = this.reserveNow.get(Integer.valueOf(cancelReservationReq.getReservationId()));
            if (reserve != null) {
                status = "Accepted";
                OcppDCAPGateway.getInstance().sendMessage(OcppDCAPGateway.getInstance().obtainMessage(77827, jsonArray));
            }
            CancelReservationConf cancelReservationConf = new CancelReservationConf();
            cancelReservationConf.setStatus(status);
            JSONArray jsonArrayConf = createConfMessage(jsonArray.getString(1), OcppMessage.CancelReservation, cancelReservationConf.toJson());
            sendMessage(jsonArrayConf);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.sendAuthorizeReq", Log.getStackTraceString(e));
        }
    }

    private void handleSetChargingProfileReq(JSONArray jsonArray) {
        try {
            SetChargingProfileReq setChargingProfileReq = new SetChargingProfileReq().fromJson(jsonArray.getJSONObject(3).toString());
            int connectorId = setChargingProfileReq.getConnectorId();
            ChargingProfile csChargingProfiles = setChargingProfileReq.getCsChargingProfiles();
            String chargingProfilePurpose = csChargingProfiles.getChargingProfilePurpose();
            String status = null;
            if (ChargingProfilePurposeType.ChargePointMaxProfile.equals(chargingProfilePurpose)) {
                if (connectorId == 0) {
                    status = "Accepted";
                    if (!queryChargingProfile(connectorId, csChargingProfiles)) {
                        addChargingProfile(connectorId, csChargingProfiles);
                    }
                } else {
                    status = "NotSupported";
                }
            } else if (ChargingProfilePurposeType.TxDefaultProfile.equals(chargingProfilePurpose)) {
                status = "Accepted";
                if (connectorId == 0) {
                    int portNum = HardwareStatusCacheProvider.getInstance().getPorts().size();
                    for (int i = 1; i <= portNum; i++) {
                        if (!queryChargingProfile(i, csChargingProfiles)) {
                            addChargingProfile(i, csChargingProfiles);
                        }
                    }
                } else if (connectorId > 0) {
                    if (!queryChargingProfile(connectorId, csChargingProfiles)) {
                        addChargingProfile(connectorId, csChargingProfiles);
                    }
                } else {
                    status = "NotSupported";
                }
            } else if (ChargingProfilePurposeType.TxProfile.equals(chargingProfilePurpose)) {
                if (connectorId > 0) {
                    String port = String.valueOf(setChargingProfileReq.getConnectorId());
                    OcppPortHandler portHandler = getPortHandler(port);
                    if (portHandler != null) {
                        portHandler.sendMessage(portHandler.obtainMessage(73735, jsonArray));
                    } else {
                        status = "Rejected";
                        Log.w("OcppProtocolAgent.handleSetChargingProfileReq", "unsupported port in ack: " + jsonArray.toString());
                    }
                } else {
                    status = "NotSupported";
                }
            }
            sendSetChargingProfileConf(jsonArray.getString(1), status);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.handleSetChargingProfileReq", Log.getStackTraceString(e));
        }
    }

    public void sendSetChargingProfileConf(String uid, String status) {
        try {
            SetChargingProfileConf setChargingProfileConf = new SetChargingProfileConf();
            setChargingProfileConf.setStatus(status);
            JSONArray jsonArrayConf = createConfMessage(uid, OcppMessage.SetChargingProfile, setChargingProfileConf.toJson());
            sendMessage(jsonArrayConf);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.sendSetChargingProfileConf", Log.getStackTraceString(e));
        }
    }

    private void handleGetCompositeScheduleReq(JSONArray jsonArray) {
        try {
            GetCompositeScheduleReq getCompositeScheduleReq = new GetCompositeScheduleReq().fromJson(jsonArray.getJSONObject(3).toString());
            int connectorId = getCompositeScheduleReq.getConnectorId();
            int duration = getCompositeScheduleReq.getDuration();
            String chargingRateUnit = getCompositeScheduleReq.getChargingRateUnit();
            long startTime = System.currentTimeMillis();
            ArrayList<ChargingSchedulePeriod> chargingSchedulePeriods = getChargingSchedulePeriod(connectorId, duration, chargingRateUnit, startTime);
            GetCompositeScheduleConf getCompositeScheduleConf = new GetCompositeScheduleConf();
            if (chargingSchedulePeriods == null || chargingSchedulePeriods.size() == 0) {
                getCompositeScheduleConf.setStatus("Rejected");
            } else {
                getCompositeScheduleConf.setStatus("Accepted");
                getCompositeScheduleConf.setConnectorId(Integer.valueOf(connectorId));
                getCompositeScheduleConf.setScheduleStart(TimeUtils.getISO8601Format(startTime, RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
                ChargingSchedule chargingSchedule = new ChargingSchedule();
                chargingSchedule.setDuration(Integer.valueOf(duration));
                chargingSchedule.setStartSchedule(TimeUtils.getISO8601Format(startTime, RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
                if (TextUtils.isEmpty(chargingRateUnit)) {
                    if (chargingSchedulePeriods.get(0).getLimit() > 32.0d) {
                        chargingSchedule.setChargingRateUnit("A");
                    } else {
                        chargingSchedule.setChargingRateUnit("W");
                    }
                } else {
                    chargingSchedule.setChargingRateUnit(chargingRateUnit);
                }
                chargingSchedule.setChargingSchedulePeriod(chargingSchedulePeriods);
                getCompositeScheduleConf.setChargingSchedule(chargingSchedule);
            }
            JSONArray jsonArrayConf = createConfMessage(jsonArray.getString(1), OcppMessage.GetCompositeSchedule, getCompositeScheduleConf.toJson());
            sendMessage(jsonArrayConf);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.handleGetCompositeScheduleReq", Log.getStackTraceString(e));
        }
    }

    private void handleDataTransferReq(JSONArray jsonArray) {
        String status;
        try {
            DataTransferReq dataTransferReq = new DataTransferReq();
            String vendorId = dataTransferReq.getVendorId();
            String messageId = dataTransferReq.getMessageId();
            String data = dataTransferReq.getData();
            if (TextUtils.isEmpty(vendorId)) {
                status = DataTransferStatus.UnknownVendorId;
            } else if (TextUtils.isEmpty(messageId)) {
                status = DataTransferStatus.UnknownMessageId;
            } else if (TextUtils.isEmpty(data)) {
                status = "Rejected";
            } else if (vendorId.equals("xchrage") && messageId.equals(YZXDCAPOption.QRCODE)) {
                status = "Accepted";
                ChargeStatusCacheProvider.getInstance().updatePortQrcodeContent("1", data);
            } else {
                status = "Rejected";
            }
            DataTransferConf dataTransferConf = new DataTransferConf();
            dataTransferConf.setStatus(status);
            JSONArray jsonArrayConf = createConfMessage(jsonArray.getString(1), OcppMessage.DataTransfer, dataTransferConf.toJson());
            sendMessage(jsonArrayConf);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.handleDataTransferReq", Log.getStackTraceString(e));
        }
    }

    private void handleGetConfigurationReq(JSONArray jsonArray) {
        try {
            GetConfigurationReq getConfigurationReq = new GetConfigurationReq().fromJson(jsonArray.getJSONObject(3).toString());
            ArrayList<String> list = getConfigurationReq.getKey();
            HashMap<String, String> maps = this.configCache.getMaps();
            ArrayList<KeyValue> configurationKey = new ArrayList<>();
            ArrayList<String> unknownKey = new ArrayList<>();
            if (list.size() > 0) {
                if (maps.size() > 0) {
                    Iterator<String> it2 = list.iterator();
                    while (it2.hasNext()) {
                        String key = it2.next();
                        if (!maps.containsKey(key)) {
                            if (!isSupportedConfig(key)) {
                                unknownKey.add(key);
                            }
                        } else {
                            KeyValue keyValue = new KeyValue();
                            keyValue.setKey(key);
                            keyValue.setReadonly(isChangeConfiguration(key));
                            keyValue.setValue(maps.get(key));
                            configurationKey.add(keyValue);
                        }
                    }
                } else {
                    Iterator<String> it3 = list.iterator();
                    while (it3.hasNext()) {
                        String key2 = it3.next();
                        if (!isSupportedConfig(key2)) {
                            unknownKey.add(key2);
                        }
                    }
                }
            } else if (maps.size() > 0) {
                for (Map.Entry<String, String> entry : maps.entrySet()) {
                    String key3 = entry.getKey();
                    String value = entry.getValue();
                    KeyValue keyValue2 = new KeyValue();
                    keyValue2.setKey(key3);
                    keyValue2.setReadonly(isChangeConfiguration(key3));
                    keyValue2.setValue(value);
                    configurationKey.add(keyValue2);
                }
            }
            GetConfigurationConf getConfigurationConf = new GetConfigurationConf();
            getConfigurationConf.setConfigurationKey(configurationKey);
            getConfigurationConf.setUnknownKey(unknownKey);
            JSONArray jsonArrayConf = createConfMessage(jsonArray.getString(1), OcppMessage.GetConfiguration, getConfigurationConf.toJson());
            sendMessage(jsonArrayConf);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.handleGetConfigurationReq", Log.getStackTraceString(e));
        }
    }

    private void handleClearChargingProfileReq(JSONArray jsonArray) {
        try {
            ClearChargingProfileReq clearChargingProfileReq = new ClearChargingProfileReq().fromJson(jsonArray.getJSONObject(3).toString());
            Integer id = clearChargingProfileReq.getId();
            Integer connectorId = clearChargingProfileReq.getConnectorId();
            String chargingProfilePurpose = clearChargingProfileReq.getChargingProfilePurpose();
            Integer stackLevel = clearChargingProfileReq.getStackLevel();
            ClearChargingProfileConf clearChargingProfileConf = new ClearChargingProfileConf();
            if (clearChargingProfile(id, connectorId, chargingProfilePurpose, stackLevel)) {
                clearChargingProfileConf.setStatus("Accepted");
            } else {
                clearChargingProfileConf.setStatus(ClearChargingProfileStatus.Unknown);
            }
            JSONArray jsonArrayConf = createConfMessage(jsonArray.getString(1), OcppMessage.ClearChargingProfile, clearChargingProfileConf.toJson());
            sendMessage(jsonArrayConf);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.handleClearChargingProfileReq", Log.getStackTraceString(e));
        }
    }

    private void handleChangeConfigurationReq(JSONArray jsonArray) {
        try {
            ChangeConfigurationReq chargeConfigurationReq = new ChangeConfigurationReq().fromJson(jsonArray.getJSONObject(3).toString());
            String key = chargeConfigurationReq.getKey();
            String value = chargeConfigurationReq.getValue();
            String status = "Rejected";
            HashMap<String, String> maps = this.configCache.getMaps();
            if (isSupportedConfig(key) && !isChangeConfiguration(key)) {
                maps.put(key, value);
                this.configCache.setMaps(maps);
                this.configCache.persist(this.context);
                status = "Accepted";
                if (OcppMessage.HeartbeatInterval.equals(key)) {
                    handleHeartbeatInterval();
                } else if (OcppMessage.ConnectionTimeOut.equals(key)) {
                    if (!TextUtils.isEmpty(value) && TextUtils.isDigitsOnly(value)) {
                        RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().setIntervalChargeCancel(Integer.parseInt(value));
                        RemoteSettingCacheProvider.getInstance().persist();
                    }
                } else if (OcppMessage.WebSocketPingInterval.equals(key)) {
                    sendPing();
                }
            }
            ChangeConfigurationConf changeConfigurationConf = new ChangeConfigurationConf();
            changeConfigurationConf.setStatus(status);
            JSONArray jsonArrayConf = createConfMessage(jsonArray.getString(1), OcppMessage.ChangeConfiguration, changeConfigurationConf.toJson());
            sendMessage(jsonArrayConf);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.handleChangeConfigurationReq", Log.getStackTraceString(e));
        }
    }

    public void handleHeartbeatInterval() {
        int interval;
        String heartbeatInterval = this.configCache.getMaps().get(OcppMessage.HeartbeatInterval);
        if (!TextUtils.isEmpty(heartbeatInterval) && TextUtils.isDigitsOnly(heartbeatInterval) && (interval = Integer.parseInt(heartbeatInterval)) > 0) {
            this.handlerTimer.stopTimer(69648);
            this.handlerTimer.startTimer(interval * 1000, 69648, Integer.valueOf(interval));
        }
    }

    private void handleGetLocalListVersionReq(JSONArray jsonArray) {
        int listVersion = -1;
        try {
            String localPreAuthorize = this.configCache.getMaps().get(OcppMessage.LocalPreAuthorize);
            if (!TextUtils.isEmpty(localPreAuthorize) && Boolean.parseBoolean(localPreAuthorize)) {
                listVersion = AuthInfoProxy.getInstance().queryAuthInfo().size() > 0 ? this.configCache.getListVersion() : 0;
            }
            GetLocalListVersionConf getLocalListVersionConf = new GetLocalListVersionConf();
            getLocalListVersionConf.setListVersion(listVersion);
            JSONArray jsonArrayConf = createConfMessage(jsonArray.getString(1), OcppMessage.GetLocalListVersion, getLocalListVersionConf.toJson());
            sendMessage(jsonArrayConf);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.handleGetLocalListVersionReq", Log.getStackTraceString(e));
        }
    }

    private void handleGetDiagnosticsReq(JSONArray jsonArray) {
        try {
            LogUploadAgent.getInstance().upload(jsonArray);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.handleGetDiagnosticsReq", Log.getStackTraceString(e));
        }
    }

    private void handleClearCacheReq(JSONArray jsonArray) {
        try {
            String status = "Rejected";
            this.configCache.setAuthInfos(null);
            if (this.configCache.persist(this.context)) {
                status = "Accepted";
            }
            ClearCacheConf clearCacheConf = new ClearCacheConf();
            clearCacheConf.setStatus(status);
            JSONArray jsonArrayConf = createConfMessage(jsonArray.getString(1), OcppMessage.ClearCache, clearCacheConf.toJson());
            sendMessage(jsonArrayConf);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.handleClearCacheReq", Log.getStackTraceString(e));
        }
    }

    private void handleSendLocalListReq(JSONArray jsonArray) {
        String updateStatus;
        try {
            JSONObject jsonObject = jsonArray.getJSONObject(3);
            SendLocalListReq sendLocalListReq = new SendLocalListReq().fromJson(jsonObject.toString());
            int newListVersion = sendLocalListReq.getListVersion();
            int nowListVersion = this.configCache.getListVersion();
            if (newListVersion > nowListVersion) {
                this.configCache.setListVersion(newListVersion);
                this.configCache.persist(this.context);
                ArrayList<AuthorizationData> localAuthorizationList = sendLocalListReq.getLocalAuthorisationList();
                String updateType = sendLocalListReq.getUpdateType();
                if (localAuthorizationList == null || (localAuthorizationList.size() == 0 && UpdateType.Full.equals(updateType))) {
                    AuthInfoProxy.getInstance().deleteAllAuthInfo();
                } else {
                    List<AuthInfo> authInfos = new ArrayList<>();
                    for (int i = 0; i < localAuthorizationList.size(); i++) {
                        AuthInfo authInfo = new AuthInfo();
                        String idTag = localAuthorizationList.get(i).getIdTag();
                        if (!TextUtils.isEmpty(idTag)) {
                            authInfo.setIdTag(localAuthorizationList.get(i).getIdTag());
                        }
                        String expiryDate = localAuthorizationList.get(i).getIdTagInfo().getExpiryDate();
                        if (!TextUtils.isEmpty(expiryDate)) {
                            authInfo.setExpiryDate(localAuthorizationList.get(i).getIdTagInfo().getExpiryDate());
                        }
                        String parenIdTag = localAuthorizationList.get(i).getIdTagInfo().getParentIdTag();
                        if (!TextUtils.isEmpty(parenIdTag)) {
                            authInfo.setParentIdTag(localAuthorizationList.get(i).getIdTagInfo().getParentIdTag());
                        }
                        String status = localAuthorizationList.get(i).getIdTagInfo().getStatus();
                        if (!TextUtils.isEmpty(status)) {
                            authInfo.setStatus(localAuthorizationList.get(i).getIdTagInfo().getStatus());
                        }
                        authInfos.add(authInfo);
                    }
                    if (UpdateType.Differential.equals(updateType)) {
                        AuthInfoProxy.getInstance().insertAuthInfos(authInfos);
                    } else if (UpdateType.Full.equals(updateType)) {
                        AuthInfoProxy.getInstance().insertAllAuthInfos(authInfos);
                    }
                }
                updateStatus = "Accepted";
            } else {
                updateStatus = UpdateStatus.VersionMismatch;
            }
            SendLocalListConf sendLocalListConf = new SendLocalListConf();
            sendLocalListConf.setStatus(updateStatus);
            JSONArray jsonArrayConf = createConfMessage(jsonArray.getString(1), OcppMessage.SendLocalList, sendLocalListConf.toJson());
            sendMessage(jsonArrayConf);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.handleSendLocalListReq", Log.getStackTraceString(e));
        }
    }

    public void test() {
    }

    private void sendUpdateFirmwareConf(String uid) {
        try {
            JSONArray jsonArray = createConfMessage(uid, OcppMessage.UpdateFirmware, "{}");
            sendMessage(jsonArray);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.sendUpdateFirmwareConf", Log.getStackTraceString(e));
        }
    }

    private void handleTriggerMessageReq(JSONArray jsonArray) {
        try {
            String uid = jsonArray.getString(1);
            TriggerMessageReq triggerMessageReq = new TriggerMessageReq().fromJson(jsonArray.getJSONObject(3).toString());
            String requestedMessage = triggerMessageReq.getRequestedMessage();
            if ("BootNotification".equals(requestedMessage)) {
                sendTriggerMessageConf(uid, "Accepted");
                sendBootNotificationReq();
            } else if ("DiagnosticsStatusNotification".equals(requestedMessage)) {
                sendTriggerMessageConf(uid, "Accepted");
                sendDiagnosticsStatusNotificationReq(true, "Idle");
            } else if ("FirmwareStatusNotification".equals(requestedMessage)) {
                sendTriggerMessageConf(uid, "Accepted");
                sendFirmwareStatusNotificationReq(true, "Idle");
            } else if ("Heartbeat".equals(requestedMessage)) {
                sendTriggerMessageConf(uid, "Accepted");
                sendHeartbeatReq(true);
            } else if ("MeterValues".equals(requestedMessage)) {
                sendTriggerMessageConf(uid, "Accepted");
                sendTriggerMeterValues();
            } else if ("StatusNotification".equals(requestedMessage)) {
                OcppPortHandler portHandler = getPortHandler("1");
                if (portHandler != null) {
                    sendTriggerMessageConf(uid, "Accepted");
                    portHandler.sendStatusNotificationReq(true, null);
                } else {
                    sendTriggerMessageConf(uid, "Rejected");
                    Log.w("OcppProtocolAgent.handleTriggerMessageReq", "unsupported port in ack: " + jsonArray.toString());
                }
            } else {
                sendTriggerMessageConf(uid, TriggerMessageStatus.NotImplemented);
            }
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.handleTriggerMessageReq", Log.getStackTraceString(e));
        }
    }

    private void sendTriggerMessageConf(String uid, String status) {
        try {
            TriggerMessageConf triggerMessageConf = new TriggerMessageConf();
            triggerMessageConf.setStatus(status);
            JSONArray jsonArray = createConfMessage(uid, OcppMessage.TriggerMessage, triggerMessageConf.toJson());
            sendMessage(jsonArray);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.sendTriggerMessageConf", Log.getStackTraceString(e));
        }
    }

    private void sendTriggerMeterValues() {
        try {
            ArrayList<MeterValue> meterValues = new ArrayList<>();
            MeterValue meterValue = new MeterValue();
            meterValue.setTimestamp(TimeUtils.getISO8601Format(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
            ArrayList<SampledValue> sampledValue = new ArrayList<>();
            PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus("1");
            if (portStatus != null) {
                Double temprature = portStatus.getTemprature();
                if (temprature != null) {
                    SampledValue item = new SampledValue();
                    item.setValue(String.valueOf(temprature.doubleValue() / 10.0d));
                    item.setContext(ReadingContext.Trigger);
                    item.setMeasurand(Measurand.Temperature);
                    item.setLocation(Location.Body);
                    item.setUnit(UnitOfMeasure.Celsius);
                    sampledValue.add(item);
                }
                ArrayList<Double> amps = portStatus.getAmps();
                if (amps != null) {
                    for (int j = 1; j < getPhase() + 1; j++) {
                        if (amps.get(j) != null) {
                            SampledValue item2 = new SampledValue();
                            item2.setValue(String.valueOf(amps.get(j)));
                            item2.setContext(ReadingContext.Trigger);
                            item2.setMeasurand(Measurand.CurrentImport);
                            switch (j) {
                                case 1:
                                    item2.setPhase(Phase.L1);
                                    break;
                                case 2:
                                    item2.setPhase(Phase.L2);
                                    break;
                                case 3:
                                    item2.setPhase(Phase.L3);
                                    break;
                            }
                            item2.setUnit("A");
                            sampledValue.add(item2);
                        }
                    }
                }
                ArrayList<Double> volts = portStatus.getVolts();
                if (volts != null) {
                    for (int j2 = 0; j2 < getPhase(); j2++) {
                        if (volts.get(j2) != null) {
                            SampledValue item3 = new SampledValue();
                            item3.setValue(String.valueOf(volts.get(j2)));
                            item3.setContext(ReadingContext.Trigger);
                            item3.setMeasurand(Measurand.Voltage);
                            switch (j2) {
                                case 0:
                                    item3.setPhase(Phase.L1);
                                    break;
                                case 1:
                                    item3.setPhase(Phase.L2);
                                    break;
                                case 2:
                                    item3.setPhase(Phase.L3);
                                    break;
                            }
                            item3.setUnit(UnitOfMeasure.V);
                            sampledValue.add(item3);
                        }
                    }
                }
                Double kwatt = portStatus.getKwatt();
                if (kwatt != null) {
                    SampledValue item4 = new SampledValue();
                    item4.setValue(String.valueOf(kwatt));
                    item4.setContext(ReadingContext.Trigger);
                    item4.setMeasurand(Measurand.PowerActiveImport);
                    item4.setUnit(UnitOfMeasure.kW);
                    sampledValue.add(item4);
                }
                Double ammeter = portStatus.getAmmeter();
                if (ammeter != null) {
                    SampledValue item5 = new SampledValue();
                    item5.setValue(String.valueOf(Math.round(ammeter.doubleValue() * 1000.0d)));
                    item5.setContext(ReadingContext.Trigger);
                    item5.setMeasurand(Measurand.EnergyActiveImportRegister);
                    item5.setUnit(UnitOfMeasure.Wh);
                    sampledValue.add(item5);
                }
                meterValue.setSampledValue(sampledValue);
                meterValues.add(meterValue);
            }
            MeterValuesReq meterValuesReq = new MeterValuesReq();
            meterValuesReq.setConnectorId(1);
            meterValuesReq.setMeterValue(meterValues);
            JSONArray jsonArray = createReqMessage(genSeq(), "MeterValues", meterValuesReq.toJson());
            jsonArray.put(OcppMessage.TriggerMessage);
            sendMessage(jsonArray);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.sendTriggerMeterValues", Log.getStackTraceString(e));
        }
    }

    private void handleResponseMessage(JSONArray response, JSONArray request) {
        try {
            String string = request.getString(2);
            switch (string.hashCode()) {
                case -2090102564:
                    if (string.equals("Heartbeat")) {
                        handleHeartbeatConf(response);
                        break;
                    }
                    break;
                case -815388727:
                    if (string.equals(OcppMessage.Authorize)) {
                        handleAuthorizeConf(request, response);
                        break;
                    }
                    break;
                case 77777212:
                    if (string.equals(OcppMessage.StartTransaction)) {
                        handleStartTransactionConf(request, response);
                        break;
                    }
                    break;
                case 641037660:
                    if (string.equals(OcppMessage.StopTransaction)) {
                        handleStopTransactionConf(request, response);
                        break;
                    }
                    break;
                case 1301119261:
                    if (string.equals("BootNotification")) {
                        handleBootNotificationConf(response);
                        break;
                    }
                    break;
            }
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.handleResponseMessage", Log.getStackTraceString(e));
        }
    }

    private void handleBootNotificationConf(JSONArray jsonArray) {
        try {
            BootNotificationConf bootNotificationConf = new BootNotificationConf().fromJson(jsonArray.getJSONObject(2).toString());
            int bootInterval = bootNotificationConf.getInterval();
            this.bootStatus = bootNotificationConf.getStatus();
            if ("Accepted".equals(this.bootStatus)) {
                SystemClock.setCurrentTimeMillis(TimeUtils.getTsFromISO8601Format(bootNotificationConf.getCurrentTime()));
                LogUtils.syslog("synch cloud time: " + bootNotificationConf.getCurrentTime());
                ChargeStatusCacheProvider.getInstance().updateCloudTimeSynch(true);
                OcppPortHandler portHandler = getPortHandler("1");
                if (portHandler != null) {
                    portHandler.sendStatusNotificationReq(false, null);
                }
                sendHeartbeatReq(false);
                int interval = HERAT_BEAT_INTERVAL;
                if (bootInterval <= 0) {
                    String heartbeatInterval = this.configCache.getMaps().get(OcppMessage.HeartbeatInterval);
                    if (!TextUtils.isEmpty(heartbeatInterval) && TextUtils.isDigitsOnly(heartbeatInterval) && Integer.parseInt(heartbeatInterval) > 0) {
                        interval = Integer.parseInt(heartbeatInterval);
                    }
                    this.handlerTimer.startTimer(interval * 1000, 69648, Integer.valueOf(interval));
                } else {
                    interval = bootInterval;
                    this.handlerTimer.startTimer(bootInterval * 1000, 69648, Integer.valueOf(bootInterval));
                }
                HashMap<String, String> maps = this.configCache.getMaps();
                maps.put(OcppMessage.HeartbeatInterval, String.valueOf(interval));
                this.configCache.setMaps(maps);
                this.configCache.persist(this.context);
            } else if (bootInterval <= 0) {
                this.handlerTimer.startTimer(30000L, 69641, null);
            } else {
                this.handlerTimer.startTimer(bootInterval * 1000, 69641, null);
            }
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.handleBootNotificationConf", Log.getStackTraceString(e));
        }
    }

    private void handleHeartbeatConf(JSONArray jsonArray) {
        try {
            JSONObject jsonObject = jsonArray.getJSONObject(2);
            HeartbeatConf heartbeatConf = new HeartbeatConf().fromJson(jsonObject.toString());
            long localTime = System.currentTimeMillis();
            long serverTime = TimeUtils.getTsFromISO8601Format(heartbeatConf.getCurrentTime());
            if (!ChargeStatusCacheProvider.getInstance().isCloudTimeSynch() || (ChargeStatusCacheProvider.getInstance().isCloudTimeSynch() && Math.abs(localTime - serverTime) >= 5000)) {
                Log.i("OcppProtocolAgent.handleHeartbeatConf", "sync local time by server time: " + heartbeatConf.getCurrentTime());
                SystemClock.setCurrentTimeMillis(serverTime);
                LogUtils.syslog("synch cloud time: " + heartbeatConf.getCurrentTime());
                ChargeStatusCacheProvider.getInstance().updateCloudTimeSynch(true);
            }
            this.failHeartBeatCnt = 0;
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.handleHeartbeatConf", Log.getStackTraceString(e));
        }
    }

    private void handleAuthorizeConf(JSONArray reqArray, JSONArray respArray) {
        try {
            AuthorizeReq authorizeReq = new AuthorizeReq().fromJson(reqArray.getJSONObject(3).toString());
            AuthorizeConf authorizeConf = new AuthorizeConf().fromJson(respArray.getJSONObject(2).toString());
            IdTagInfo idTagInfo = authorizeConf.getIdTagInfo();
            addIdTagInfoCache(authorizeReq.getIdTag(), authorizeConf.getIdTagInfo());
            JSONArray jsonArray = getReserveNow(authorizeReq.getIdTag());
            if (jsonArray != null) {
                sendReserveNowConf(jsonArray, idTagInfo);
            } else if ("Accepted".equals(idTagInfo.getStatus())) {
                OcppRequestSession ocppRequestSession = new OcppRequestSession();
                ocppRequestSession.setRequest(reqArray);
                ocppRequestSession.setResponse(respArray);
                OcppDCAPGateway.getInstance().sendMessage(OcppDCAPGateway.getInstance().obtainMessage(77828, ocppRequestSession));
            }
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.handleAuthorizeConf", Log.getStackTraceString(e));
        }
    }

    public void sendReserveNowConf(JSONArray jsonArray, IdTagInfo idTagInfo) {
        try {
            ReserveNowReq reserveNowReq = new ReserveNowReq().fromJson(jsonArray.getJSONObject(3).toString());
            String status = "Rejected";
            long time = TimeUtils.getTsFromISO8601Format(reserveNowReq.getExpiryDate()) - System.currentTimeMillis();
            if ("Accepted".equals(idTagInfo.getStatus()) && time > 0) {
                status = "Accepted";
                OcppDCAPGateway.getInstance().sendMessage(OcppDCAPGateway.getInstance().obtainMessage(77827, jsonArray));
                this.handlerTimer.startTimer(time, 69649, Integer.valueOf(reserveNowReq.getReservationId()));
            } else {
                this.reserveNow.remove(Integer.valueOf(reserveNowReq.getReservationId()));
            }
            ReserveNowConf reserveNowConf = new ReserveNowConf();
            reserveNowConf.setStatus(status);
            JSONArray jsonArrayConf = createConfMessage(jsonArray.getString(1), OcppMessage.ReserveNow, reserveNowConf.toJson());
            sendMessage(jsonArrayConf);
        } catch (Exception e) {
            Log.w("OcppPortHandler.sendReserveNowConf", Log.getStackTraceString(e));
        }
    }

    public void sendReserveNowConf(String uid, String status) {
        try {
            ReserveNowConf reserveNowConf = new ReserveNowConf();
            reserveNowConf.setStatus(status);
            JSONArray jsonArrayConf = createConfMessage(uid, OcppMessage.ReserveNow, reserveNowConf.toJson());
            sendMessage(jsonArrayConf);
        } catch (Exception e) {
            Log.w("OcppPortHandler.sendReserveNowConf", Log.getStackTraceString(e));
        }
    }

    private void handleStartTransactionConf(JSONArray reqArray, JSONArray respArray) {
        try {
            StartTransactionReq startTransactionReq = new StartTransactionReq().fromJson(reqArray.getJSONObject(3).toString());
            String port = String.valueOf(startTransactionReq.getConnectorId());
            OcppPortHandler portHandler = getPortHandler(port);
            if (portHandler != null) {
                OcppRequestSession ocppRequestSession = new OcppRequestSession();
                ocppRequestSession.setRequest(reqArray);
                ocppRequestSession.setResponse(respArray);
                portHandler.sendMessage(portHandler.obtainMessage(73736, ocppRequestSession));
            } else {
                Log.w("OcppProtocolAgent.handleStartTransactionConf", "failed to find handler for port: " + port);
            }
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.handleStartTransactionConf", Log.getStackTraceString(e));
        }
    }

    private void handleStopTransactionConf(JSONArray reqArray, JSONArray respArray) {
        try {
            StopTransactionReq stopTransactionReq = new StopTransactionReq().fromJson(reqArray.getJSONObject(3).toString());
            String cloudChargeId = String.valueOf(stopTransactionReq.getTransactionId());
            ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBillcloud(cloudChargeId);
            String port = chargeBill.getPort();
            OcppPortHandler portHandler = getPortHandler(port);
            if (portHandler != null) {
                OcppRequestSession ocppRequestSession = new OcppRequestSession();
                ocppRequestSession.setRequest(reqArray);
                ocppRequestSession.setResponse(respArray);
                portHandler.sendMessage(portHandler.obtainMessage(73736, ocppRequestSession));
            } else {
                Log.w("OcppProtocolAgent.handleStopTransactionConf", "failed to find handler for port: " + port);
            }
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.handleStopTransactionConf", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void requestTimeoutCheck() {
        if (this.sendReqestState.size() > 0) {
            Iterator<Map.Entry<String, SendRequestState>> it2 = this.sendReqestState.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry<String, SendRequestState> entry = it2.next();
                SendRequestState requestState = entry.getValue();
                OCPP_REQUEST_STATE state = requestState.status;
                long timestamp = requestState.timestamp;
                if (OCPP_REQUEST_STATE.sending.equals(state)) {
                    if (System.currentTimeMillis() - timestamp > 5000) {
                        it2.remove();
                        handleSendRequestFail(requestState.request);
                    }
                } else if (OCPP_REQUEST_STATE.sended.equals(state) && System.currentTimeMillis() - timestamp > 15000) {
                    it2.remove();
                    handleRequestTimeout(requestState.request);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Code restructure failed: missing block: B:18:0x005e, code lost:
        if (r4.equals(com.xcharge.charger.protocol.ocpp.bean.OcppMessage.StartTransaction) != false) goto L16;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void handleSendRequestFail(org.json.JSONArray r10) {
        /*
            r9 = this;
            r4 = 2
            java.lang.String r4 = r10.getString(r4)     // Catch: java.lang.Exception -> L32
            int r5 = r4.hashCode()     // Catch: java.lang.Exception -> L32
            switch(r5) {
                case -2090102564: goto Ld;
                case -815388727: goto L3d;
                case 77777212: goto L58;
                case 641037660: goto L7a;
                case 1301119261: goto L83;
                default: goto Lc;
            }     // Catch: java.lang.Exception -> L32
        Lc:
            return
        Ld:
            java.lang.String r5 = "Heartbeat"
            boolean r4 = r4.equals(r5)     // Catch: java.lang.Exception -> L32
            if (r4 == 0) goto Lc
            int r4 = r9.failHeartBeatCnt     // Catch: java.lang.Exception -> L32
            int r4 = r4 + 1
            r9.failHeartBeatCnt = r4     // Catch: java.lang.Exception -> L32
            int r4 = r9.failHeartBeatCnt     // Catch: java.lang.Exception -> L32
            r5 = 3
            if (r4 < r5) goto L98
            java.lang.String r4 = "OcppProtocolAgent.handleRequestTimeout"
            java.lang.String r5 = "failed to heart beat 3 times, consider be offline !!!"
            android.util.Log.w(r4, r5)     // Catch: java.lang.Exception -> L32
            com.xcharge.charger.protocol.ocpp.handler.OcppProtocolAgent r4 = getInstance()     // Catch: java.lang.Exception -> L32
            r4.disconnect()     // Catch: java.lang.Exception -> L32
            r4 = 0
            r9.failHeartBeatCnt = r4     // Catch: java.lang.Exception -> L32
            goto Lc
        L32:
            r1 = move-exception
            java.lang.String r4 = "OcppProtocolAgent.handleSendRequestFail"
            java.lang.String r5 = android.util.Log.getStackTraceString(r1)
            android.util.Log.w(r4, r5)
            goto Lc
        L3d:
            java.lang.String r5 = "Authorize"
            boolean r4 = r4.equals(r5)     // Catch: java.lang.Exception -> L32
            if (r4 == 0) goto Lc
            com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway r4 = com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway.getInstance()     // Catch: java.lang.Exception -> L32
            com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway r5 = com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway.getInstance()     // Catch: java.lang.Exception -> L32
            r6 = 77829(0x13005, float:1.09062E-40)
            android.os.Message r5 = r5.obtainMessage(r6, r10)     // Catch: java.lang.Exception -> L32
            r4.sendMessage(r5)     // Catch: java.lang.Exception -> L32
            goto Lc
        L58:
            java.lang.String r5 = "StartTransaction"
            boolean r4 = r4.equals(r5)     // Catch: java.lang.Exception -> L32
            if (r4 == 0) goto Lc
        L60:
            r4 = 4
            org.json.JSONObject r0 = r10.getJSONObject(r4)     // Catch: java.lang.Exception -> L32
            java.lang.String r4 = "port"
            java.lang.String r2 = r0.getString(r4)     // Catch: java.lang.Exception -> L32
            com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r3 = r9.getPortHandler(r2)     // Catch: java.lang.Exception -> L32
            r4 = 73732(0x12004, float:1.0332E-40)
            android.os.Message r4 = r3.obtainMessage(r4, r10)     // Catch: java.lang.Exception -> L32
            r3.sendMessage(r4)     // Catch: java.lang.Exception -> L32
            goto Lc
        L7a:
            java.lang.String r5 = "StopTransaction"
            boolean r4 = r4.equals(r5)     // Catch: java.lang.Exception -> L32
            if (r4 != 0) goto L60
            goto Lc
        L83:
            java.lang.String r5 = "BootNotification"
            boolean r4 = r4.equals(r5)     // Catch: java.lang.Exception -> L32
            if (r4 == 0) goto Lc
            com.xcharge.common.utils.HandlerTimer r4 = r9.handlerTimer     // Catch: java.lang.Exception -> L32
            r6 = 30000(0x7530, double:1.4822E-319)
            r5 = 69641(0x11009, float:9.7588E-41)
            r8 = 0
            r4.startTimer(r6, r5, r8)     // Catch: java.lang.Exception -> L32
            goto Lc
        L98:
            java.lang.String r4 = "ECWProtocolAgent.handleSendRequestFail"
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> L32
            java.lang.String r6 = "failed to heart beat request: "
            r5.<init>(r6)     // Catch: java.lang.Exception -> L32
            java.lang.String r6 = r10.toString()     // Catch: java.lang.Exception -> L32
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch: java.lang.Exception -> L32
            java.lang.String r5 = r5.toString()     // Catch: java.lang.Exception -> L32
            android.util.Log.w(r4, r5)     // Catch: java.lang.Exception -> L32
            goto Lc
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.protocol.ocpp.handler.OcppProtocolAgent.handleSendRequestFail(org.json.JSONArray):void");
    }

    /* JADX WARN: Code restructure failed: missing block: B:18:0x005e, code lost:
        if (r4.equals(com.xcharge.charger.protocol.ocpp.bean.OcppMessage.StartTransaction) != false) goto L16;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private void handleRequestTimeout(org.json.JSONArray r10) {
        /*
            r9 = this;
            r4 = 2
            java.lang.String r4 = r10.getString(r4)     // Catch: java.lang.Exception -> L32
            int r5 = r4.hashCode()     // Catch: java.lang.Exception -> L32
            switch(r5) {
                case -2090102564: goto Ld;
                case -815388727: goto L3d;
                case 77777212: goto L58;
                case 641037660: goto L7a;
                case 1301119261: goto L83;
                default: goto Lc;
            }     // Catch: java.lang.Exception -> L32
        Lc:
            return
        Ld:
            java.lang.String r5 = "Heartbeat"
            boolean r4 = r4.equals(r5)     // Catch: java.lang.Exception -> L32
            if (r4 == 0) goto Lc
            int r4 = r9.failHeartBeatCnt     // Catch: java.lang.Exception -> L32
            int r4 = r4 + 1
            r9.failHeartBeatCnt = r4     // Catch: java.lang.Exception -> L32
            int r4 = r9.failHeartBeatCnt     // Catch: java.lang.Exception -> L32
            r5 = 3
            if (r4 < r5) goto L98
            java.lang.String r4 = "OcppProtocolAgent.handleRequestTimeout"
            java.lang.String r5 = "failed to heart beat 3 times, consider be offline !!!"
            android.util.Log.w(r4, r5)     // Catch: java.lang.Exception -> L32
            com.xcharge.charger.protocol.ocpp.handler.OcppProtocolAgent r4 = getInstance()     // Catch: java.lang.Exception -> L32
            r4.disconnect()     // Catch: java.lang.Exception -> L32
            r4 = 0
            r9.failHeartBeatCnt = r4     // Catch: java.lang.Exception -> L32
            goto Lc
        L32:
            r1 = move-exception
            java.lang.String r4 = "OcppProtocolAgent.handleRequestTimeout"
            java.lang.String r5 = android.util.Log.getStackTraceString(r1)
            android.util.Log.w(r4, r5)
            goto Lc
        L3d:
            java.lang.String r5 = "Authorize"
            boolean r4 = r4.equals(r5)     // Catch: java.lang.Exception -> L32
            if (r4 == 0) goto Lc
            com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway r4 = com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway.getInstance()     // Catch: java.lang.Exception -> L32
            com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway r5 = com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway.getInstance()     // Catch: java.lang.Exception -> L32
            r6 = 77829(0x13005, float:1.09062E-40)
            android.os.Message r5 = r5.obtainMessage(r6, r10)     // Catch: java.lang.Exception -> L32
            r4.sendMessage(r5)     // Catch: java.lang.Exception -> L32
            goto Lc
        L58:
            java.lang.String r5 = "StartTransaction"
            boolean r4 = r4.equals(r5)     // Catch: java.lang.Exception -> L32
            if (r4 == 0) goto Lc
        L60:
            r4 = 4
            org.json.JSONObject r0 = r10.getJSONObject(r4)     // Catch: java.lang.Exception -> L32
            java.lang.String r4 = "port"
            java.lang.String r2 = r0.getString(r4)     // Catch: java.lang.Exception -> L32
            com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r3 = r9.getPortHandler(r2)     // Catch: java.lang.Exception -> L32
            r4 = 73733(0x12005, float:1.03322E-40)
            android.os.Message r4 = r3.obtainMessage(r4, r10)     // Catch: java.lang.Exception -> L32
            r3.sendMessage(r4)     // Catch: java.lang.Exception -> L32
            goto Lc
        L7a:
            java.lang.String r5 = "StopTransaction"
            boolean r4 = r4.equals(r5)     // Catch: java.lang.Exception -> L32
            if (r4 != 0) goto L60
            goto Lc
        L83:
            java.lang.String r5 = "BootNotification"
            boolean r4 = r4.equals(r5)     // Catch: java.lang.Exception -> L32
            if (r4 == 0) goto Lc
            com.xcharge.common.utils.HandlerTimer r4 = r9.handlerTimer     // Catch: java.lang.Exception -> L32
            r6 = 30000(0x7530, double:1.4822E-319)
            r5 = 69641(0x11009, float:9.7588E-41)
            r8 = 0
            r4.startTimer(r6, r5, r8)     // Catch: java.lang.Exception -> L32
            goto Lc
        L98:
            java.lang.String r4 = "ECWProtocolAgent.handleSendRequestFail"
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> L32
            java.lang.String r6 = "failed to heart beat request: "
            r5.<init>(r6)     // Catch: java.lang.Exception -> L32
            java.lang.String r6 = r10.toString()     // Catch: java.lang.Exception -> L32
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch: java.lang.Exception -> L32
            java.lang.String r5 = r5.toString()     // Catch: java.lang.Exception -> L32
            android.util.Log.w(r4, r5)     // Catch: java.lang.Exception -> L32
            goto Lc
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.protocol.ocpp.handler.OcppProtocolAgent.handleRequestTimeout(org.json.JSONArray):void");
    }

    public void sendRemoteStartTransactionConf(String uid, String status) {
        try {
            RemoteStartTransactionConf remoteStartTransactionConf = new RemoteStartTransactionConf();
            remoteStartTransactionConf.setStatus(status);
            JSONArray jsonArray = createConfMessage(uid, OcppMessage.RemoteStartTransaction, remoteStartTransactionConf.toJson());
            sendMessage(jsonArray);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.sendRemoteStartTransactionConf", Log.getStackTraceString(e));
        }
    }

    public void sendRemoteStopTransactionConf(String uid, String status) {
        try {
            RemoteStopTransactionConf remoteStopTransactionConf = new RemoteStopTransactionConf();
            remoteStopTransactionConf.setStatus(status);
            JSONArray jsonArray = createConfMessage(uid, OcppMessage.RemoteStopTransaction, remoteStopTransactionConf.toJson());
            sendMessage(jsonArray);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.sendRemoteStopTransactionConf", Log.getStackTraceString(e));
        }
    }

    public void sendGetDiagnosticsConf(String uid, String fileName) {
        JSONArray jsonArray;
        try {
            if (TextUtils.isEmpty(fileName)) {
                jsonArray = createConfMessage(uid, OcppMessage.GetDiagnostics, "{}");
            } else {
                GetDiagnosticsConf getDiagnosticsConf = new GetDiagnosticsConf();
                getDiagnosticsConf.setFileName(fileName);
                jsonArray = createConfMessage(uid, OcppMessage.GetDiagnostics, getDiagnosticsConf.toJson());
            }
            sendMessage(jsonArray);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.sendGetDiagnosticsConf", Log.getStackTraceString(e));
        }
    }

    public void sendResetConf(String uid, String status) {
        try {
            ResetConf resetConf = new ResetConf();
            resetConf.setStatus(status);
            JSONArray jsonArrayConf = createConfMessage(uid, OcppMessage.Reset, resetConf.toJson());
            sendMessage(jsonArrayConf);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.handleResetReq", Log.getStackTraceString(e));
        }
    }

    public void sendUnlockConnectorConf(String uid, String status) {
        try {
            UnlockConnectorConf unlockConnectorConf = new UnlockConnectorConf();
            unlockConnectorConf.setStatus(status);
            JSONArray jsonArray = createConfMessage(uid, OcppMessage.UnlockConnector, unlockConnectorConf.toJson());
            sendMessage(jsonArray);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.sendUnlockConnectorConf", Log.getStackTraceString(e));
        }
    }

    public JSONArray authorizeReq(String idTag, boolean isStart) {
        Boolean isAuthorizd;
        Boolean isAuthorizd2;
        HashMap<String, String> maps = this.configCache.getMaps();
        if (this.isConnected) {
            String localPreAuthorize = maps.get(OcppMessage.LocalPreAuthorize);
            if (!TextUtils.isEmpty(localPreAuthorize)) {
                if (Boolean.parseBoolean(localPreAuthorize)) {
                    if (isStart) {
                        isAuthorizd2 = isLocalAuthorizd(idTag);
                    } else {
                        isAuthorizd2 = isLocalAuthorizdWhenStop(idTag);
                    }
                    if (isAuthorizd2 == null || !isAuthorizd2.booleanValue()) {
                        if (!"Accepted".equals(getBootStatus())) {
                            return sendLocalAuthorizeReq(idTag, false);
                        }
                        return sendAuthorizeReq(idTag);
                    }
                    return sendLocalAuthorizeReq(idTag, true);
                } else if (!"Accepted".equals(getBootStatus())) {
                    return sendLocalAuthorizeReq(idTag, false);
                } else {
                    return sendAuthorizeReq(idTag);
                }
            } else if (!"Accepted".equals(getBootStatus())) {
                return sendLocalAuthorizeReq(idTag, false);
            } else {
                return sendAuthorizeReq(idTag);
            }
        }
        String allowOfflineTxForUnknownId = maps.get(OcppMessage.AllowOfflineTxForUnknownId);
        String localAuthorizeOffline = maps.get(OcppMessage.LocalAuthorizeOffline);
        if (!TextUtils.isEmpty(localAuthorizeOffline)) {
            if (Boolean.parseBoolean(localAuthorizeOffline)) {
                if (isStart) {
                    isAuthorizd = isLocalAuthorizd(idTag);
                } else {
                    isAuthorizd = isLocalAuthorizdWhenStop(idTag);
                }
                if (isAuthorizd == null) {
                    if (!TextUtils.isEmpty(allowOfflineTxForUnknownId) && Boolean.parseBoolean(allowOfflineTxForUnknownId)) {
                        return sendLocalAuthorizeReq(idTag, true);
                    }
                    return sendLocalAuthorizeReq(idTag, false);
                } else if (isAuthorizd.booleanValue()) {
                    return sendLocalAuthorizeReq(idTag, true);
                } else {
                    return sendLocalAuthorizeReq(idTag, false);
                }
            }
            return sendLocalAuthorizeReq(idTag, false);
        } else if (!TextUtils.isEmpty(allowOfflineTxForUnknownId) && Boolean.parseBoolean(allowOfflineTxForUnknownId)) {
            return sendLocalAuthorizeReq(idTag, true);
        } else {
            return sendLocalAuthorizeReq(idTag, false);
        }
    }

    private Boolean isLocalAuthorizdWhenStop(String stopIdTag) {
        OcppChargeSession chargeSession = getChargeSession("1");
        if (chargeSession != null) {
            String startIdTag = chargeSession.getUser_code();
            if (!TextUtils.isEmpty(startIdTag)) {
                if (stopIdTag.equals(startIdTag)) {
                    return isLocalAuthorizd(stopIdTag);
                }
                String localAuthListEnabled = this.configCache.getMaps().get(OcppMessage.LocalAuthListEnabled);
                if (!TextUtils.isEmpty(localAuthListEnabled)) {
                    if (Boolean.parseBoolean(localAuthListEnabled)) {
                        AuthInfo startAuthInfo = AuthInfoProxy.getInstance().getAuthInfo(startIdTag);
                        AuthInfo stopAuthInfo = AuthInfoProxy.getInstance().getAuthInfo(stopIdTag);
                        if (startAuthInfo != null && stopAuthInfo != null) {
                            String startParentIdTag = startAuthInfo.getParentIdTag();
                            String stopParentIdTag = stopAuthInfo.getParentIdTag();
                            if (!TextUtils.isEmpty(startParentIdTag) && !TextUtils.isEmpty(stopParentIdTag) && startParentIdTag.equals(stopParentIdTag)) {
                                examineDBIsExpired(startIdTag, startParentIdTag);
                                examineDBIsExpired(stopIdTag, stopParentIdTag);
                                return isLocalAuthorizd(stopIdTag);
                            }
                        } else {
                            return isCacheAuthorizdWhenStop(startIdTag, stopIdTag);
                        }
                    } else {
                        return isCacheAuthorizdWhenStop(startIdTag, stopIdTag);
                    }
                } else {
                    return isCacheAuthorizdWhenStop(startIdTag, stopIdTag);
                }
            }
        }
        return false;
    }

    private Boolean isCacheAuthorizdWhenStop(String startIdTag, String stopIdTag) {
        String authorizationCacheEnabled = this.configCache.getMaps().get(OcppMessage.AuthorizationCacheEnabled);
        if (!TextUtils.isEmpty(authorizationCacheEnabled) && Boolean.parseBoolean(authorizationCacheEnabled)) {
            AuthCache startAuthCache = queryAuthCache(startIdTag);
            AuthCache stopAuthCache = queryAuthCache(stopIdTag);
            if (startAuthCache != null && stopAuthCache != null) {
                String startParentIdTag = startAuthCache.getParentIdTag();
                String stopParentIdTag = stopAuthCache.getParentIdTag();
                if (!TextUtils.isEmpty(startParentIdTag) && !TextUtils.isEmpty(stopParentIdTag) && startParentIdTag.equals(stopParentIdTag)) {
                    examineCacheIsExpired(startIdTag, startParentIdTag);
                    examineCacheIsExpired(stopIdTag, stopParentIdTag);
                    return isCacheAuthorizd(stopIdTag);
                }
            }
        }
        return false;
    }

    private Boolean isLocalAuthorizd(String idTag) {
        examineDBIsExpired(idTag, null);
        String localAuthListEnabled = this.configCache.getMaps().get(OcppMessage.LocalAuthListEnabled);
        if (!TextUtils.isEmpty(localAuthListEnabled)) {
            if (Boolean.parseBoolean(localAuthListEnabled)) {
                AuthInfo authInfo = AuthInfoProxy.getInstance().getAuthInfo(idTag);
                if (authInfo != null) {
                    if ("Accepted".equals(authInfo.getStatus())) {
                        return true;
                    }
                    return false;
                }
                return isCacheAuthorizd(idTag);
            }
            return isCacheAuthorizd(idTag);
        }
        return isCacheAuthorizd(idTag);
    }

    private Boolean isCacheAuthorizd(String idTag) {
        examineCacheIsExpired(idTag, null);
        String authorizationCacheEnabled = this.configCache.getMaps().get(OcppMessage.AuthorizationCacheEnabled);
        if (!TextUtils.isEmpty(authorizationCacheEnabled) && Boolean.parseBoolean(authorizationCacheEnabled)) {
            AuthCache authCache = queryAuthCache(idTag);
            if (authCache == null) {
                return null;
            }
            if ("Accepted".equals(authCache.getStatus())) {
                return true;
            }
        }
        return false;
    }

    private JSONArray sendLocalAuthorizeReq(String idTag, boolean isAuth) {
        try {
            AuthorizeReq authorizeReq = new AuthorizeReq();
            authorizeReq.setIdTag(idTag);
            JSONArray jsonArray = createReqMessage(genSeq(), OcppMessage.Authorize, authorizeReq.toJson());
            jsonArray.put(isAuth);
            return jsonArray;
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.sendLocalAuthorizeReq", Log.getStackTraceString(e));
            return null;
        }
    }

    public void sendLocalAuthorizeConf(JSONArray jsonArray) {
        try {
            boolean isAuth = jsonArray.getBoolean(4);
            AuthorizeConf authorizeConf = new AuthorizeConf();
            IdTagInfo idTagInfo = new IdTagInfo();
            if (isAuth) {
                idTagInfo.setStatus("Accepted");
            } else {
                idTagInfo.setStatus(AuthorizationStatus.Invalid);
            }
            authorizeConf.setIdTagInfo(idTagInfo);
            JSONArray jsonArrayConf = new JSONArray();
            jsonArrayConf.put(3);
            jsonArrayConf.put(jsonArray.getString(1));
            jsonArrayConf.put(new JSONObject(authorizeConf.toJson()));
            OcppRequestSession session = new OcppRequestSession();
            session.setRequest(jsonArray);
            session.setResponse(jsonArrayConf);
            OcppDCAPGateway.getInstance().sendMessage(OcppDCAPGateway.getInstance().obtainMessage(77828, session));
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.sendLocalAuthorizeConf", Log.getStackTraceString(e));
        }
    }

    public void handleUpdateQrcodeRequest(String port) {
        String qrcode = this.configCache.getQrcode();
        if (!TextUtils.isEmpty(qrcode)) {
            ChargeStatusCacheProvider.getInstance().updatePortQrcodeContent(port, qrcode);
        }
    }

    public JSONArray createReqMessage(String uid, String action, String load) {
        try {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(2);
            jsonArray.put(uid);
            jsonArray.put(action);
            jsonArray.put(new JSONObject(load));
            return jsonArray;
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.createReqMessage", Log.getStackTraceString(e));
            return null;
        }
    }

    public JSONArray createConfMessage(String uid, String action, String load) {
        try {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(3);
            jsonArray.put(uid);
            jsonArray.put(new JSONObject(load));
            return jsonArray;
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.createConfMessage", Log.getStackTraceString(e));
            return null;
        }
    }

    public boolean sendMessage(JSONArray jsonArray) {
        try {
            return this.handler.sendMessage(this.handler.obtainMessage(69637, jsonArray.toString()));
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.sendMessage", Log.getStackTraceString(e));
            return false;
        }
    }

    public boolean sendMessage(JSONArray jsonArray, int delay) {
        try {
            return this.handler.sendMessageDelayed(this.handler.obtainMessage(69637, jsonArray.toString()), delay);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.sendMessage", Log.getStackTraceString(e));
            return false;
        }
    }

    public String genSeq() {
        return String.valueOf(this.seqGen.incrementAndGet());
    }

    public OcppPortHandler getPortHandler(String port) {
        return this.portHandlers.get(port);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void portsActive() {
        for (OcppPortHandler portHandler : this.portHandlers.values()) {
            portHandler.sendMessage(portHandler.obtainMessage(73729));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void portsDeactive() {
        for (OcppPortHandler portHandler : this.portHandlers.values()) {
            portHandler.sendMessage(portHandler.obtainMessage(73730));
        }
    }

    public OcppConfig getOcppConfig() {
        if (this.configCache == null) {
            this.configCache = new OcppConfig();
            this.configCache.init(this.context);
        }
        return this.configCache;
    }

    public void addIdTagInfoCache(String idTag, IdTagInfo idTagInfo) {
        boolean isEquals = false;
        ArrayList<AuthCache> authInfos = this.configCache.getAuthInfos();
        if (authInfos == null) {
            authInfos = new ArrayList<>();
        }
        for (int i = 0; i < authInfos.size(); i++) {
            AuthCache authCache = authInfos.get(i);
            if (authCache.getIdTag().equals(idTag)) {
                isEquals = true;
                authCache.setExpiryDate(idTagInfo.getExpiryDate());
                authCache.setParentIdTag(idTagInfo.getParentIdTag());
                authCache.setStatus(idTagInfo.getStatus());
            }
        }
        if (!isEquals) {
            AuthCache authCache2 = new AuthCache();
            authCache2.setIdTag(idTag);
            authCache2.setExpiryDate(idTagInfo.getExpiryDate());
            authCache2.setParentIdTag(idTagInfo.getParentIdTag());
            authCache2.setStatus(idTagInfo.getStatus());
            authInfos.add(authCache2);
        }
        this.configCache.setAuthInfos(authInfos);
        this.configCache.persist(this.context);
    }

    public AuthCache queryAuthCache(String idTag) {
        ArrayList<AuthCache> authInfos = this.configCache.getAuthInfos();
        if (TextUtils.isEmpty(idTag) || authInfos == null) {
            return null;
        }
        for (int i = 0; i < authInfos.size(); i++) {
            AuthCache authCache = authInfos.get(i);
            if (authCache.getIdTag().equals(idTag)) {
                return authCache;
            }
        }
        return null;
    }

    public AuthCache queryAuthCache(String idTag, String parentIdTag) {
        ArrayList<AuthCache> authInfos = this.configCache.getAuthInfos();
        if (TextUtils.isEmpty(idTag) || authInfos == null) {
            return null;
        }
        for (int i = 0; i < authInfos.size(); i++) {
            AuthCache authCache = authInfos.get(i);
            if (authCache.getIdTag().equals(idTag) && authCache.getParentIdTag().equals(parentIdTag)) {
                return authCache;
            }
        }
        return null;
    }

    private boolean localAuthReserveNow(String idTag, String parentIdTag) {
        Boolean isAuthorizd;
        HashMap<String, String> maps = this.configCache.getMaps();
        if (this.isConnected) {
            String localPreAuthorize = maps.get(OcppMessage.LocalPreAuthorize);
            if (!TextUtils.isEmpty(localPreAuthorize) && Boolean.parseBoolean(localPreAuthorize) && (isAuthorizd = isLocalAuthorizd(idTag)) != null && isAuthorizd.booleanValue()) {
                return true;
            }
        } else {
            String allowOfflineTxForUnknownId = maps.get(OcppMessage.AllowOfflineTxForUnknownId);
            String localAuthorizeOffline = maps.get(OcppMessage.LocalAuthorizeOffline);
            if (!TextUtils.isEmpty(localAuthorizeOffline)) {
                if (Boolean.parseBoolean(localAuthorizeOffline)) {
                    Boolean isAuthorizd2 = isLocalAuthorizd(idTag);
                    if (isAuthorizd2 == null) {
                        if (!TextUtils.isEmpty(allowOfflineTxForUnknownId) && Boolean.parseBoolean(allowOfflineTxForUnknownId)) {
                            return true;
                        }
                    } else if (isAuthorizd2.booleanValue()) {
                        return true;
                    }
                }
            } else if (!TextUtils.isEmpty(allowOfflineTxForUnknownId) && Boolean.parseBoolean(allowOfflineTxForUnknownId)) {
                return true;
            }
        }
        return false;
    }

    private void examineCacheIsExpired(String idTag, String parentIdTag) {
        try {
            ArrayList<AuthCache> authInfos = this.configCache.getAuthInfos();
            Iterator<AuthCache> it2 = authInfos.iterator();
            while (it2.hasNext()) {
                AuthCache authCache = it2.next();
                if (TextUtils.isEmpty(parentIdTag)) {
                    if (idTag.equals(authCache.getIdTag())) {
                        String expiryDate = authCache.getExpiryDate();
                        if (!TextUtils.isEmpty(expiryDate)) {
                            long nowTime = System.currentTimeMillis();
                            long expiryTime = TimeUtils.getTsFromISO8601Format(expiryDate);
                            if (nowTime >= expiryTime) {
                                authCache.setStatus(AuthorizationStatus.Expired);
                            }
                        }
                    }
                } else if (idTag.equals(authCache.getIdTag()) && parentIdTag.equals(authCache.getParentIdTag())) {
                    String expiryDate2 = authCache.getExpiryDate();
                    if (!TextUtils.isEmpty(expiryDate2)) {
                        long nowTime2 = System.currentTimeMillis();
                        long expiryTime2 = TimeUtils.getTsFromISO8601Format(expiryDate2);
                        if (nowTime2 >= expiryTime2) {
                            authCache.setStatus(AuthorizationStatus.Expired);
                        }
                    }
                }
            }
            this.configCache.setAuthInfos(authInfos);
            this.configCache.persist(this.context);
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.examineCacheIsExpired", Log.getStackTraceString(e));
        }
    }

    private void examineDBIsExpired(String idTag, String parentIdTag) {
        AuthInfo authInfo;
        try {
            if (TextUtils.isEmpty(parentIdTag)) {
                authInfo = AuthInfoProxy.getInstance().getAuthInfo(idTag);
            } else {
                authInfo = AuthInfoProxy.getInstance().getAuthInfo(idTag, parentIdTag);
            }
            if (authInfo != null) {
                String expiryDate = authInfo.getExpiryDate();
                if (!TextUtils.isEmpty(expiryDate)) {
                    long nowTime = System.currentTimeMillis();
                    long expiryTime = TimeUtils.getTsFromISO8601Format(expiryDate);
                    if (nowTime >= expiryTime) {
                        authInfo.setStatus(AuthorizationStatus.Expired);
                    }
                }
                AuthInfoProxy.getInstance().insertAuthInfo(authInfo);
            }
        } catch (Exception e) {
            Log.w("OcppProtocolAgent.examineDBIsExpired", Log.getStackTraceString(e));
        }
    }

    public ArrayList<ChargingSchedulePeriod> getChargingSchedulePeriod(int connectorId, int duration, String chargingRateUnit, long startTime) {
        int startSecond = (int) TimeUtils.getHHmmSeconds(startTime);
        int endSecond = startSecond + duration;
        HashMap<String, ArrayList<ChargingProfile>> maxChargingProfiles = this.configCache.getMaxChargingProfiles();
        ChargingProfile maxChargingProfile = getChargingProfile(maxChargingProfiles, 0, chargingRateUnit);
        HashMap<String, ArrayList<ChargingProfile>> defChargingProfiles = this.configCache.getDefChargingProfiles();
        ChargingProfile defChargingProfile = getChargingProfile(defChargingProfiles, connectorId, chargingRateUnit);
        OcppChargeSession chargeSession = getChargeSession(String.valueOf(connectorId));
        HashMap<String, ArrayList<ChargingProfile>> txChargingProfiles = chargeSession.getTxChargingProfiles();
        ChargingProfile txChargingProfile = getChargingProfile(txChargingProfiles, connectorId, chargingRateUnit);
        if (txChargingProfile == null) {
            if (defChargingProfile == null) {
                if (maxChargingProfile != null) {
                    return oneChargingProfile(maxChargingProfile, chargingRateUnit, startSecond, endSecond);
                }
                return null;
            } else if (maxChargingProfile == null) {
                return oneChargingProfile(defChargingProfile, chargingRateUnit, startSecond, endSecond);
            } else {
                return twoChargingProfile(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond, endSecond);
            }
        } else if (maxChargingProfile == null) {
            return oneChargingProfile(txChargingProfile, chargingRateUnit, startSecond, endSecond);
        } else {
            return twoChargingProfile(maxChargingProfile, txChargingProfile, chargingRateUnit, startSecond, endSecond);
        }
    }

    private ChargingProfile getChargingProfile(HashMap<String, ArrayList<ChargingProfile>> chargingProfiles, int connectorId, String chargingRateUnit) {
        ChargingProfile chargingProfile = null;
        int stackLevel = 0;
        if (chargingProfiles.size() > 0) {
            for (Map.Entry<String, ArrayList<ChargingProfile>> entry : chargingProfiles.entrySet()) {
                String key = entry.getKey();
                ArrayList<ChargingProfile> value = entry.getValue();
                if (key.equals(String.valueOf(connectorId)) && value.size() > 0) {
                    if (TextUtils.isEmpty(chargingRateUnit)) {
                        for (int i = 0; i < value.size(); i++) {
                            if (value.get(i).getStackLevel() > stackLevel) {
                                chargingProfile = value.get(i);
                                stackLevel = value.get(i).getStackLevel();
                            }
                        }
                    } else {
                        for (int i2 = 0; i2 < value.size(); i2++) {
                            if (value.get(i2).getStackLevel() > stackLevel && value.get(i2).getChargingSchedule().getChargingRateUnit().equals(chargingRateUnit)) {
                                chargingProfile = value.get(i2);
                                stackLevel = value.get(i2).getStackLevel();
                            }
                        }
                    }
                }
            }
        }
        return chargingProfile;
    }

    private ArrayList<ChargingSchedulePeriod> oneChargingProfile(ChargingProfile chargingProfile, String chargingRateUnit, int startSecond, int endSecond) {
        String validFrom = chargingProfile.getValidFrom();
        String validTo = chargingProfile.getValidTo();
        ChargingSchedule chargingSchedule = chargingProfile.getChargingSchedule();
        Integer duration = chargingSchedule.getDuration();
        String startSchedule = chargingSchedule.getStartSchedule();
        if (!TextUtils.isEmpty(validFrom) && !TextUtils.isEmpty(validTo)) {
            Integer startValidFrom = Integer.valueOf((int) TimeUtils.getHHmmSeconds(TimeUtils.getTsFromISO8601Format(validFrom)));
            Integer endValidTo = Integer.valueOf((int) TimeUtils.getHHmmSeconds(TimeUtils.getTsFromISO8601Format(validTo)));
            if (startValidFrom.intValue() < startSecond && endSecond < endValidTo.intValue()) {
                if (duration != null && !TextUtils.isEmpty(startSchedule)) {
                    Integer startTime = Integer.valueOf((int) TimeUtils.getHHmmSeconds(TimeUtils.getTsFromISO8601Format(startSchedule)));
                    Integer endTime = Integer.valueOf(startTime.intValue() + duration.intValue());
                    if (startTime.intValue() < startSecond && endSecond < endTime.intValue()) {
                        return rangeInterception(chargingProfile, chargingRateUnit, startSecond, endSecond, chargingSchedule);
                    }
                } else {
                    return rangeInterception(chargingProfile, chargingRateUnit, startSecond, endSecond, chargingSchedule);
                }
            }
            return null;
        }
        return rangeInterception(chargingProfile, chargingRateUnit, startSecond, endSecond, chargingSchedule);
    }

    private ArrayList<ChargingSchedulePeriod> rangeInterception(ChargingProfile chargingProfile, String chargingRateUnit, int startSecond, int endSecond, ChargingSchedule chargingSchedule) {
        String chargingProfileKind = chargingProfile.getChargingProfileKind();
        String recurrencyKind = chargingProfile.getRecurrencyKind();
        Integer startFlag = null;
        Integer endFlag = null;
        ArrayList<ChargingSchedulePeriod> chargingSchedulePeriod = chargingSchedule.getChargingSchedulePeriod();
        ArrayList<ChargingSchedulePeriod> chargingSchedulePeriods = new ArrayList<>();
        if (ChargingProfileKindType.Recurring.equals(chargingProfileKind) && !TextUtils.isEmpty(recurrencyKind) && RecurrencyKindType.Weekly.equals(recurrencyKind)) {
            startSecond += (convertWeek() - 1) * 86400;
            endSecond += (convertWeek() - 1) * 86400;
        }
        for (int i = 0; i < chargingSchedulePeriod.size(); i++) {
            if (i == chargingSchedulePeriod.size() - 1 && startFlag == null && endFlag == null) {
                startFlag = Integer.valueOf(chargingSchedulePeriod.size() - 1);
                endFlag = Integer.valueOf(chargingSchedulePeriod.size() - 1);
            } else {
                if (chargingSchedulePeriod.get(i).getStartPeriod() <= startSecond && startSecond < chargingSchedulePeriod.get(i + 1).getStartPeriod()) {
                    startFlag = Integer.valueOf(i);
                }
                if (chargingSchedulePeriod.get(i).getStartPeriod() <= endSecond && endSecond < chargingSchedulePeriod.get(i + 1).getStartPeriod()) {
                    endFlag = Integer.valueOf(i);
                }
            }
        }
        if (startFlag != null && endFlag != null) {
            for (int i2 = startFlag.intValue(); i2 <= endFlag.intValue(); i2++) {
                if (i2 == startFlag.intValue()) {
                    chargingSchedulePeriod.get(i2).setStartPeriod(startSecond);
                }
                chargingSchedulePeriod.get(i2).setLimit(getSuitableLimit(chargingSchedule, chargingRateUnit, i2));
                chargingSchedulePeriods.add(chargingSchedulePeriod.get(i2));
            }
        }
        return chargingSchedulePeriods;
    }

    private double getSuitableLimit(ChargingSchedule chargingSchedule, String unit, int i) {
        String chargingRateUnit = chargingSchedule.getChargingRateUnit();
        ArrayList<ChargingSchedulePeriod> chargingSchedulePeriod = chargingSchedule.getChargingSchedulePeriod();
        double limit = chargingSchedulePeriod.get(i).getLimit();
        if (TextUtils.isEmpty(unit)) {
            if ("A".equals(chargingRateUnit)) {
                if (limit < 32.0d) {
                    return limit;
                }
                return 32.0d;
            } else if ("W".equals(chargingRateUnit)) {
                if (limit / (getPhase() * FTPCodes.SERVICE_READY_FOR_NEW_USER) >= 32.0d) {
                    return getPhase() * 7040;
                }
                return limit;
            }
        } else if ("A".equals(unit)) {
            if ("A".equals(chargingRateUnit)) {
                if (limit >= 32.0d) {
                    return 32.0d;
                }
                return limit;
            } else if ("W".equals(chargingRateUnit)) {
                if (limit / (getPhase() * FTPCodes.SERVICE_READY_FOR_NEW_USER) < 32.0d) {
                    return limit / (getPhase() * FTPCodes.SERVICE_READY_FOR_NEW_USER);
                }
                return 32.0d;
            }
        } else if ("W".equals(unit)) {
            if ("A".equals(chargingRateUnit)) {
                if (limit < 32.0d) {
                    return 220.0d * limit * getPhase();
                }
                return getPhase() * 7040;
            } else if ("W".equals(chargingRateUnit)) {
                if (limit >= getPhase() * 7040) {
                    return getPhase() * 7040;
                }
                return limit;
            }
        }
        return 0.0d;
    }

    private ArrayList<ChargingSchedulePeriod> twoChargingProfile(ChargingProfile maxChargingProfile, ChargingProfile defChargingProfile, String chargingRateUnit, int startSecond, int endSecond) {
        String maxValidFrom = maxChargingProfile.getValidFrom();
        String maxValidTo = maxChargingProfile.getValidTo();
        String defValidFrom = defChargingProfile.getValidFrom();
        String defValidTo = defChargingProfile.getValidTo();
        if ((!TextUtils.isEmpty(maxValidFrom) && !TextUtils.isEmpty(maxValidTo) && TextUtils.isEmpty(defValidFrom)) || TextUtils.isEmpty(defValidTo)) {
            Integer maxStartValidFrom = Integer.valueOf((int) TimeUtils.getHHmmSeconds(TimeUtils.getTsFromISO8601Format(maxValidFrom)));
            Integer maxEndValidFrom = Integer.valueOf((int) TimeUtils.getHHmmSeconds(TimeUtils.getTsFromISO8601Format(maxValidTo)));
            if (maxStartValidFrom.intValue() <= startSecond && endSecond <= maxEndValidFrom.intValue()) {
                return isRange(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond, endSecond);
            }
        } else if ((!TextUtils.isEmpty(defValidFrom) && !TextUtils.isEmpty(defValidTo) && TextUtils.isEmpty(maxValidFrom)) || TextUtils.isEmpty(maxValidTo)) {
            Integer defStartValidFrom = Integer.valueOf((int) TimeUtils.getHHmmSeconds(TimeUtils.getTsFromISO8601Format(defValidFrom)));
            Integer defEndValidFrom = Integer.valueOf((int) TimeUtils.getHHmmSeconds(TimeUtils.getTsFromISO8601Format(defValidTo)));
            if (defStartValidFrom.intValue() < startSecond && endSecond < defEndValidFrom.intValue()) {
                return isRange(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond, endSecond);
            }
        } else if (!TextUtils.isEmpty(defValidFrom) && !TextUtils.isEmpty(defValidTo) && !TextUtils.isEmpty(maxValidFrom) && !TextUtils.isEmpty(maxValidTo)) {
            Integer maxStartValidFrom2 = Integer.valueOf((int) TimeUtils.getHHmmSeconds(TimeUtils.getTsFromISO8601Format(maxValidFrom)));
            Integer maxEndValidFrom2 = Integer.valueOf((int) TimeUtils.getHHmmSeconds(TimeUtils.getTsFromISO8601Format(maxValidTo)));
            Integer defStartValidFrom2 = Integer.valueOf((int) TimeUtils.getHHmmSeconds(TimeUtils.getTsFromISO8601Format(defValidFrom)));
            Integer defEndValidFrom2 = Integer.valueOf((int) TimeUtils.getHHmmSeconds(TimeUtils.getTsFromISO8601Format(defValidTo)));
            if (maxStartValidFrom2.intValue() <= startSecond && endSecond <= maxEndValidFrom2.intValue() && defStartValidFrom2.intValue() < startSecond && endSecond < defEndValidFrom2.intValue()) {
                return isRange(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond, endSecond);
            }
        } else {
            return isRange(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond, endSecond);
        }
        return null;
    }

    private ArrayList<ChargingSchedulePeriod> isRange(ChargingProfile maxChargingProfile, ChargingProfile defChargingProfile, String chargingRateUnit, int startSecond, int endSecond) {
        ChargingSchedule maxChargingSchedule = maxChargingProfile.getChargingSchedule();
        ChargingSchedule defChargingSchedule = defChargingProfile.getChargingSchedule();
        Integer maxDuration = maxChargingSchedule.getDuration();
        Integer defDuration = defChargingSchedule.getDuration();
        String maxStartSchedule = maxChargingSchedule.getStartSchedule();
        String defStartSchedule = defChargingSchedule.getStartSchedule();
        if ((maxDuration != null && !TextUtils.isEmpty(maxStartSchedule) && defDuration == null) || TextUtils.isEmpty(defStartSchedule)) {
            Integer maxStart = Integer.valueOf((int) TimeUtils.getHHmmSeconds(TimeUtils.getTsFromISO8601Format(maxStartSchedule)));
            Integer maxEnd = Integer.valueOf(maxStart.intValue() + maxDuration.intValue());
            if (maxStart.intValue() <= startSecond && endSecond <= maxEnd.intValue()) {
                return compareKindType(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond, endSecond);
            }
        } else if ((defDuration != null && !TextUtils.isEmpty(defStartSchedule) && maxDuration == null) || TextUtils.isEmpty(maxStartSchedule)) {
            Integer defStart = Integer.valueOf((int) TimeUtils.getHHmmSeconds(TimeUtils.getTsFromISO8601Format(defStartSchedule)));
            Integer defEnd = Integer.valueOf(defStart.intValue() + defDuration.intValue());
            if (defStart.intValue() <= startSecond && endSecond <= defEnd.intValue()) {
                return compareKindType(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond, endSecond);
            }
        } else if (maxDuration != null && !TextUtils.isEmpty(maxStartSchedule) && defDuration != null && !TextUtils.isEmpty(defStartSchedule)) {
            Integer maxStart2 = Integer.valueOf((int) TimeUtils.getHHmmSeconds(TimeUtils.getTsFromISO8601Format(maxStartSchedule)));
            Integer maxEnd2 = Integer.valueOf(maxStart2.intValue() + maxDuration.intValue());
            Integer defStart2 = Integer.valueOf((int) TimeUtils.getHHmmSeconds(TimeUtils.getTsFromISO8601Format(defStartSchedule)));
            Integer defEnd2 = Integer.valueOf(defStart2.intValue() + defDuration.intValue());
            if (maxStart2.intValue() <= startSecond && endSecond <= maxEnd2.intValue() && defStart2.intValue() <= startSecond && endSecond <= defEnd2.intValue()) {
                return compareKindType(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond, endSecond);
            }
        } else {
            return compareKindType(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond, endSecond);
        }
        return null;
    }

    private ArrayList<ChargingSchedulePeriod> compareKindType(ChargingProfile maxChargingProfile, ChargingProfile defChargingProfile, String chargingRateUnit, int startSecond, int endSecond) {
        String maxChargingProfileKind = maxChargingProfile.getChargingProfileKind();
        String defChargingProfileKind = defChargingProfile.getChargingProfileKind();
        String maxRecurrencyKind = maxChargingProfile.getRecurrencyKind();
        String defRecurrencyKind = defChargingProfile.getRecurrencyKind();
        ChargingSchedule maxChargingSchedule = maxChargingProfile.getChargingSchedule();
        ChargingSchedule defChargingSchedule = defChargingProfile.getChargingSchedule();
        ArrayList<ChargingSchedulePeriod> maxChargingSchedulePeriod = maxChargingSchedule.getChargingSchedulePeriod();
        ArrayList<ChargingSchedulePeriod> defChargingSchedulePeriod = defChargingSchedule.getChargingSchedulePeriod();
        if (ChargingProfileKindType.Absolute.equals(maxChargingProfileKind)) {
            if (ChargingProfileKindType.Absolute.equals(defChargingProfileKind)) {
                return groupChargingProfile(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond, endSecond, 0);
            }
            if (ChargingProfileKindType.Recurring.equals(defChargingProfileKind)) {
                if (!TextUtils.isEmpty(defRecurrencyKind)) {
                    if (RecurrencyKindType.Daily.equals(defRecurrencyKind)) {
                        return groupChargingProfile(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond, endSecond, 0);
                    }
                    if (RecurrencyKindType.Weekly.equals(defRecurrencyKind)) {
                        for (int i = 0; i < maxChargingSchedulePeriod.size(); i++) {
                            maxChargingSchedulePeriod.get(i).setStartPeriod(maxChargingSchedulePeriod.get(i).getStartPeriod() + ((convertWeek() - 1) * 86400));
                        }
                        maxChargingSchedule.setChargingSchedulePeriod(maxChargingSchedulePeriod);
                        maxChargingProfile.setChargingSchedule(maxChargingSchedule);
                        return groupChargingProfile(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond + ((convertWeek() - 1) * 86400), endSecond + ((convertWeek() - 1) * 86400), 1);
                    }
                }
            } else if (ChargingProfileKindType.Relative.equals(defChargingSchedulePeriod)) {
                return groupChargingProfile(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond, endSecond, 0);
            }
        } else if (ChargingProfileKindType.Recurring.equals(maxChargingProfileKind)) {
            if (!TextUtils.isEmpty(maxRecurrencyKind)) {
                if (RecurrencyKindType.Daily.equals(maxRecurrencyKind)) {
                    if (ChargingProfileKindType.Absolute.equals(defChargingSchedulePeriod)) {
                        return groupChargingProfile(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond, endSecond, 0);
                    }
                    if (ChargingProfileKindType.Recurring.equals(defChargingSchedulePeriod)) {
                        if (!TextUtils.isEmpty(defRecurrencyKind)) {
                            if (RecurrencyKindType.Daily.equals(defRecurrencyKind)) {
                                return groupChargingProfile(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond, endSecond, 0);
                            }
                            if (RecurrencyKindType.Weekly.equals(defRecurrencyKind)) {
                                for (int i2 = 0; i2 < maxChargingSchedulePeriod.size(); i2++) {
                                    maxChargingSchedulePeriod.get(i2).setStartPeriod(maxChargingSchedulePeriod.get(i2).getStartPeriod() + ((convertWeek() - 1) * 86400));
                                }
                                maxChargingSchedule.setChargingSchedulePeriod(maxChargingSchedulePeriod);
                                maxChargingProfile.setChargingSchedule(maxChargingSchedule);
                                return groupChargingProfile(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond + ((convertWeek() - 1) * 86400), endSecond + ((convertWeek() - 1) * 86400), 1);
                            }
                        }
                    } else if (ChargingProfileKindType.Relative.equals(defChargingSchedulePeriod)) {
                        return groupChargingProfile(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond, endSecond, 0);
                    }
                } else if (RecurrencyKindType.Weekly.equals(maxRecurrencyKind)) {
                    if (ChargingProfileKindType.Absolute.equals(defChargingSchedulePeriod)) {
                        for (int i3 = 0; i3 < defChargingSchedulePeriod.size(); i3++) {
                            defChargingSchedulePeriod.get(i3).setStartPeriod(defChargingSchedulePeriod.get(i3).getStartPeriod() + ((convertWeek() - 1) * 86400));
                        }
                        defChargingSchedule.setChargingSchedulePeriod(defChargingSchedulePeriod);
                        defChargingProfile.setChargingSchedule(defChargingSchedule);
                        return groupChargingProfile(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond + ((convertWeek() - 1) * 86400), endSecond + ((convertWeek() - 1) * 86400), 1);
                    } else if (ChargingProfileKindType.Recurring.equals(defChargingSchedulePeriod)) {
                        if (!TextUtils.isEmpty(defRecurrencyKind)) {
                            if (RecurrencyKindType.Daily.equals(defRecurrencyKind)) {
                                for (int i4 = 0; i4 < defChargingSchedulePeriod.size(); i4++) {
                                    defChargingSchedulePeriod.get(i4).setStartPeriod(defChargingSchedulePeriod.get(i4).getStartPeriod() + ((convertWeek() - 1) * 86400));
                                }
                                defChargingSchedule.setChargingSchedulePeriod(defChargingSchedulePeriod);
                                defChargingProfile.setChargingSchedule(defChargingSchedule);
                                return groupChargingProfile(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond + ((convertWeek() - 1) * 86400), endSecond + ((convertWeek() - 1) * 86400), 1);
                            } else if (RecurrencyKindType.Weekly.equals(defRecurrencyKind)) {
                                return groupChargingProfile(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond + ((convertWeek() - 1) * 86400), endSecond + ((convertWeek() - 1) * 86400), 0);
                            }
                        }
                    } else if (ChargingProfileKindType.Relative.equals(defChargingSchedulePeriod)) {
                        for (int i5 = 0; i5 < defChargingSchedulePeriod.size(); i5++) {
                            defChargingSchedulePeriod.get(i5).setStartPeriod(defChargingSchedulePeriod.get(i5).getStartPeriod() + ((convertWeek() - 1) * 86400));
                        }
                        defChargingSchedule.setChargingSchedulePeriod(defChargingSchedulePeriod);
                        defChargingProfile.setChargingSchedule(defChargingSchedule);
                        return groupChargingProfile(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond + ((convertWeek() - 1) * 86400), endSecond + ((convertWeek() - 1) * 86400), 1);
                    }
                }
            }
        } else if (ChargingProfileKindType.Relative.equals(maxChargingProfileKind)) {
            if (ChargingProfileKindType.Absolute.equals(defChargingProfileKind)) {
                return groupChargingProfile(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond, endSecond, 0);
            }
            if (ChargingProfileKindType.Recurring.equals(defChargingProfileKind)) {
                if (!TextUtils.isEmpty(defRecurrencyKind)) {
                    if (RecurrencyKindType.Daily.equals(defRecurrencyKind)) {
                        return groupChargingProfile(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond, endSecond, 0);
                    }
                    if (RecurrencyKindType.Weekly.equals(defRecurrencyKind)) {
                        for (int i6 = 0; i6 < maxChargingSchedulePeriod.size(); i6++) {
                            maxChargingSchedulePeriod.get(i6).setStartPeriod(maxChargingSchedulePeriod.get(i6).getStartPeriod() + ((convertWeek() - 1) * 86400));
                        }
                        maxChargingSchedule.setChargingSchedulePeriod(maxChargingSchedulePeriod);
                        maxChargingProfile.setChargingSchedule(maxChargingSchedule);
                        return groupChargingProfile(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond + ((convertWeek() - 1) * 86400), endSecond + ((convertWeek() - 1) * 86400), 1);
                    }
                }
            } else if (ChargingProfileKindType.Recurring.equals(defChargingProfileKind)) {
                return groupChargingProfile(maxChargingProfile, defChargingProfile, chargingRateUnit, startSecond, endSecond, 0);
            }
        }
        return null;
    }

    private ArrayList<ChargingSchedulePeriod> groupChargingProfile(ChargingProfile maxChargingProfile, ChargingProfile defChargingProfile, String chargingRateUnit, int startSecond, int endSecond, int type) {
        ChargingSchedule maxChargingSchedule = maxChargingProfile.getChargingSchedule();
        ChargingSchedule defChargingSchedule = defChargingProfile.getChargingSchedule();
        ArrayList<ChargingSchedulePeriod> maxChargingSchedulePeriod = maxChargingSchedule.getChargingSchedulePeriod();
        ArrayList<ChargingSchedulePeriod> defChargingSchedulePeriod = defChargingSchedule.getChargingSchedulePeriod();
        ArrayList<ChargingSchedulePeriod> chargingSchedulePeriods = new ArrayList<>();
        int maxStartMark = 0;
        int defStartMark = 0;
        int maxEndMark = 0;
        int defEndMark = 0;
        for (int i = 0; i < maxChargingSchedulePeriod.size(); i++) {
            if (maxChargingSchedulePeriod.get(i).getStartPeriod() < startSecond && startSecond < maxChargingSchedulePeriod.get(i + 1).getStartPeriod()) {
                maxStartMark = i;
            }
            if (maxChargingSchedulePeriod.get(i).getStartPeriod() < endSecond && endSecond < maxChargingSchedulePeriod.get(i + 1).getStartPeriod()) {
                maxEndMark = i;
            }
        }
        for (int i2 = 0; i2 < defChargingSchedulePeriod.size(); i2++) {
            if (defChargingSchedulePeriod.get(i2).getStartPeriod() < startSecond && startSecond < defChargingSchedulePeriod.get(i2 + 1).getStartPeriod()) {
                defStartMark = i2;
            }
            if (defChargingSchedulePeriod.get(i2).getStartPeriod() < endSecond && endSecond < defChargingSchedulePeriod.get(i2 + 1).getStartPeriod()) {
                defEndMark = i2;
            }
        }
        ChargingSchedulePeriod chargingSchedulePeriod = new ChargingSchedulePeriod();
        chargingSchedulePeriod.setStartPeriod(startSecond);
        chargingSchedulePeriod.setLimit(getMinLimit(maxChargingSchedule, defChargingSchedule, chargingRateUnit, maxStartMark, defStartMark));
        chargingSchedulePeriods.add(chargingSchedulePeriod);
        int defFlag = defStartMark + 1;
        int i3 = maxStartMark + 1;
        while (i3 < maxEndMark) {
            ChargingSchedulePeriod chargingSchedulePeriod1 = new ChargingSchedulePeriod();
            if (maxChargingSchedulePeriod.get(i3).getStartPeriod() < defChargingSchedulePeriod.get(defFlag).getStartPeriod()) {
                chargingSchedulePeriod1.setStartPeriod(maxChargingSchedulePeriod.get(i3).getStartPeriod());
            } else {
                chargingSchedulePeriod1.setStartPeriod(defChargingSchedulePeriod.get(defFlag).getStartPeriod());
                i3--;
                if (defFlag <= defEndMark) {
                    defFlag++;
                }
            }
            chargingSchedulePeriod1.setLimit(getMinLimit(maxChargingSchedule, defChargingSchedule, chargingRateUnit, i3, defFlag));
            chargingSchedulePeriods.add(chargingSchedulePeriod1);
            i3++;
        }
        if (type == 1 && chargingSchedulePeriods.size() > 0) {
            for (int i4 = 0; i4 < chargingSchedulePeriods.size(); i4++) {
                chargingSchedulePeriods.get(i4).setStartPeriod(chargingSchedulePeriods.get(i4).getStartPeriod() - ((convertWeek() - 1) * 86400));
            }
        }
        return chargingSchedulePeriods;
    }

    private double getMinLimit(ChargingSchedule maxChargingSchedule, ChargingSchedule defChargingSchedule, String chargingRateUnit, int maxStartMark, int defStartMark) {
        double maxLimit = 0.0d;
        double defLimit = 0.0d;
        String maxChargingRateUnit = maxChargingSchedule.getChargingRateUnit();
        String defChargingRateUnit = defChargingSchedule.getChargingRateUnit();
        ArrayList<ChargingSchedulePeriod> maxChargingSchedulePeriod = maxChargingSchedule.getChargingSchedulePeriod();
        ArrayList<ChargingSchedulePeriod> defChargingSchedulePeriod = defChargingSchedule.getChargingSchedulePeriod();
        if ("A".equals(maxChargingRateUnit) && "A".equals(defChargingRateUnit)) {
            maxLimit = maxChargingSchedulePeriod.get(maxStartMark).getLimit();
            defLimit = defChargingSchedulePeriod.get(defStartMark).getLimit();
        } else if ("A".equals(maxChargingRateUnit) && "W".equals(defChargingRateUnit)) {
            maxLimit = maxChargingSchedulePeriod.get(maxStartMark).getLimit();
            defLimit = defChargingSchedulePeriod.get(defStartMark).getLimit() / (getPhase() * FTPCodes.SERVICE_READY_FOR_NEW_USER);
        } else if ("W".equals(maxChargingRateUnit) && "A".equals(defChargingRateUnit)) {
            maxLimit = maxChargingSchedulePeriod.get(maxStartMark).getLimit() / (getPhase() * FTPCodes.SERVICE_READY_FOR_NEW_USER);
            defLimit = defChargingSchedulePeriod.get(defStartMark).getLimit();
        } else if ("W".equals(maxChargingRateUnit) && "W".equals(defChargingRateUnit)) {
            maxLimit = maxChargingSchedulePeriod.get(maxStartMark).getLimit() / (getPhase() * FTPCodes.SERVICE_READY_FOR_NEW_USER);
            defLimit = defChargingSchedulePeriod.get(defStartMark).getLimit() / (getPhase() * FTPCodes.SERVICE_READY_FOR_NEW_USER);
        }
        if (TextUtils.isEmpty(chargingRateUnit)) {
            if (defLimit >= maxLimit) {
                if (maxLimit < 32.0d) {
                    return maxLimit;
                }
                return 32.0d;
            }
            return defLimit;
        } else if ("A".equals(chargingRateUnit)) {
            if (defLimit >= maxLimit) {
                if (maxLimit < 32.0d) {
                    return maxLimit;
                }
                return 32.0d;
            }
            return defLimit;
        } else if ("W".equals(chargingRateUnit)) {
            if (defLimit < maxLimit) {
                return 220.0d * defLimit * getPhase();
            }
            if (maxLimit < 32.0d) {
                return 220.0d * maxLimit * getPhase();
            }
            return getPhase() * 7040;
        } else {
            return 0.0d;
        }
    }

    public boolean queryChargingProfile(int connectorId, ChargingProfile csChargingProfiles) {
        boolean isEquals = false;
        HashMap<String, ArrayList<ChargingProfile>> chargingProfiles = null;
        String chargingProfilePurpose = csChargingProfiles.getChargingProfilePurpose();
        if (ChargingProfilePurposeType.ChargePointMaxProfile.equals(chargingProfilePurpose)) {
            chargingProfiles = this.configCache.getMaxChargingProfiles();
        } else if (ChargingProfilePurposeType.TxDefaultProfile.equals(chargingProfilePurpose)) {
            chargingProfiles = this.configCache.getDefChargingProfiles();
        }
        for (Map.Entry<String, ArrayList<ChargingProfile>> entry : chargingProfiles.entrySet()) {
            String key = entry.getKey();
            ArrayList<ChargingProfile> value = entry.getValue();
            if (key.equals(String.valueOf(connectorId))) {
                for (int i = 0; i < value.size(); i++) {
                    ChargingProfile chargingProfile = value.get(i);
                    if (chargingProfile.getStackLevel() == csChargingProfiles.getStackLevel()) {
                        value.set(i, csChargingProfiles);
                        chargingProfiles.put(key, value);
                        isEquals = true;
                    }
                }
            }
        }
        if (ChargingProfilePurposeType.ChargePointMaxProfile.equals(chargingProfilePurpose)) {
            this.configCache.setMaxChargingProfiles(chargingProfiles);
        } else if (ChargingProfilePurposeType.TxDefaultProfile.equals(chargingProfilePurpose)) {
            this.configCache.setDefChargingProfiles(chargingProfiles);
        }
        this.configCache.persist(this.context);
        return isEquals;
    }

    private void addChargingProfile(int connectorId, ChargingProfile csChargingProfiles) {
        HashMap<String, ArrayList<ChargingProfile>> chargingProfiles = null;
        String chargingProfilePurpose = csChargingProfiles.getChargingProfilePurpose();
        if (ChargingProfilePurposeType.ChargePointMaxProfile.equals(chargingProfilePurpose)) {
            chargingProfiles = this.configCache.getMaxChargingProfiles();
        } else if (ChargingProfilePurposeType.TxDefaultProfile.equals(chargingProfilePurpose)) {
            chargingProfiles = this.configCache.getDefChargingProfiles();
        }
        if (chargingProfiles.size() > 0) {
            ArrayList<ChargingProfile> chargingProfile = chargingProfiles.get(String.valueOf(connectorId));
            if (chargingProfile == null) {
                chargingProfile = new ArrayList<>();
            }
            chargingProfile.add(csChargingProfiles);
            chargingProfiles.put(String.valueOf(connectorId), chargingProfile);
        } else {
            ArrayList<ChargingProfile> chargingProfile2 = new ArrayList<>();
            chargingProfile2.add(csChargingProfiles);
            chargingProfiles.put(String.valueOf(connectorId), chargingProfile2);
        }
        if (ChargingProfilePurposeType.ChargePointMaxProfile.equals(chargingProfilePurpose)) {
            this.configCache.setMaxChargingProfiles(chargingProfiles);
        } else if (ChargingProfilePurposeType.TxDefaultProfile.equals(chargingProfilePurpose)) {
            this.configCache.setDefChargingProfiles(chargingProfiles);
        }
        this.configCache.persist(this.context);
    }

    private boolean clearChargingProfile(Integer id, Integer connectorId, String chargingProfilePurpose, Integer clearStackLevel) {
        boolean isClear = false;
        HashMap<String, ArrayList<ChargingProfile>> maxChargingProfiles = this.configCache.getMaxChargingProfiles();
        HashMap<String, ArrayList<ChargingProfile>> defChargingProfiles = this.configCache.getDefChargingProfiles();
        if (TextUtils.isEmpty(chargingProfilePurpose)) {
            if (connectorId == null) {
                isClear = clearMaxChargingProfile(maxChargingProfiles, id, clearStackLevel);
                if (clearDefChargingProfile(defChargingProfiles, id, clearStackLevel)) {
                    isClear = true;
                }
            } else {
                isClear = connectorId.intValue() == 0 ? clearMaxChargingProfile(maxChargingProfiles, id, clearStackLevel) : clearConnectorIdDefChargingProfile(defChargingProfiles, id, connectorId, clearStackLevel);
            }
        } else if (ChargingProfilePurposeType.ChargePointMaxProfile.equals(chargingProfilePurpose)) {
            if (connectorId == null || connectorId.intValue() == 0) {
                isClear = clearMaxChargingProfile(maxChargingProfiles, id, clearStackLevel);
            }
        } else if (ChargingProfilePurposeType.TxDefaultProfile.equals(chargingProfilePurpose)) {
            if (connectorId == null) {
                isClear = clearDefChargingProfile(defChargingProfiles, id, clearStackLevel);
            } else if (connectorId.intValue() != 0) {
                isClear = clearConnectorIdDefChargingProfile(defChargingProfiles, id, connectorId, clearStackLevel);
            }
        }
        this.configCache.setMaxChargingProfiles(maxChargingProfiles);
        this.configCache.setDefChargingProfiles(defChargingProfiles);
        this.configCache.persist(this.context);
        return isClear;
    }

    private boolean clearMaxChargingProfile(HashMap<String, ArrayList<ChargingProfile>> maxchargingProfiles, Integer id, Integer clearStackLevel) {
        boolean isClear = false;
        Iterator<ChargingProfile> iterator = maxchargingProfiles.get("0").iterator();
        while (iterator.hasNext()) {
            ChargingProfile chargingProfile = iterator.next();
            int chargingProfileId = chargingProfile.getChargingProfileId();
            int stackLevel = chargingProfile.getStackLevel();
            if (id == null || clearStackLevel == null) {
                if (id == null || clearStackLevel != null) {
                    if (id == null && clearStackLevel != null) {
                        if (stackLevel == clearStackLevel.intValue()) {
                            iterator.remove();
                            isClear = true;
                        }
                    } else {
                        iterator.remove();
                        isClear = true;
                    }
                } else if (chargingProfileId == id.intValue()) {
                    iterator.remove();
                    isClear = true;
                }
            } else if (chargingProfileId == id.intValue() && stackLevel == clearStackLevel.intValue()) {
                iterator.remove();
                isClear = true;
            }
        }
        return isClear;
    }

    private boolean clearDefChargingProfile(HashMap<String, ArrayList<ChargingProfile>> defChargingProfiles, Integer id, Integer clearStackLevel) {
        boolean isClear = false;
        if (defChargingProfiles.size() > 0) {
            for (Map.Entry<String, ArrayList<ChargingProfile>> entry : defChargingProfiles.entrySet()) {
                entry.getKey();
                ArrayList<ChargingProfile> value = entry.getValue();
                if (value != null && value.size() > 0) {
                    Iterator<ChargingProfile> it2 = value.iterator();
                    while (it2.hasNext()) {
                        ChargingProfile chargingProfile = it2.next();
                        int chargingProfileId = chargingProfile.getChargingProfileId();
                        int stackLevel = chargingProfile.getStackLevel();
                        if (id == null || clearStackLevel == null) {
                            if (id == null || clearStackLevel != null) {
                                if (id == null && clearStackLevel != null) {
                                    if (stackLevel == clearStackLevel.intValue()) {
                                        it2.remove();
                                        isClear = true;
                                    }
                                } else {
                                    it2.remove();
                                    isClear = true;
                                }
                            } else if (chargingProfileId == id.intValue()) {
                                it2.remove();
                                isClear = true;
                            }
                        } else if (chargingProfileId == id.intValue() && stackLevel == clearStackLevel.intValue()) {
                            it2.remove();
                            isClear = true;
                        }
                    }
                }
            }
        }
        return isClear;
    }

    private boolean clearConnectorIdDefChargingProfile(HashMap<String, ArrayList<ChargingProfile>> defChargingProfiles, Integer id, Integer connectorId, Integer clearStackLevel) {
        boolean isClear = false;
        Iterator<ChargingProfile> iterator = defChargingProfiles.get(String.valueOf(connectorId)).iterator();
        while (iterator.hasNext()) {
            ChargingProfile chargingProfile = iterator.next();
            int chargingProfileId = chargingProfile.getChargingProfileId();
            int stackLevel = chargingProfile.getStackLevel();
            if (id == null || clearStackLevel == null) {
                if (id == null || clearStackLevel != null) {
                    if (id == null && clearStackLevel != null) {
                        if (stackLevel == clearStackLevel.intValue()) {
                            iterator.remove();
                            isClear = true;
                        }
                    } else {
                        iterator.remove();
                        isClear = true;
                    }
                } else if (chargingProfileId == id.intValue()) {
                    iterator.remove();
                    isClear = true;
                }
            } else if (chargingProfileId == id.intValue() && stackLevel == clearStackLevel.intValue()) {
                iterator.remove();
                isClear = true;
            }
        }
        return isClear;
    }

    public Boolean isSupported(String type) {
        String supportedFileTransferProtocols = this.configCache.getMaps().get(OcppMessage.SupportedFileTransferProtocols);
        if (!TextUtils.isEmpty(supportedFileTransferProtocols)) {
            String[] types = supportedFileTransferProtocols.split(",");
            for (String string : types) {
                if (type.equals(string)) {
                    return true;
                }
            }
            return false;
        }
        return null;
    }

    public boolean isHttp(String type) {
        return !TextUtils.isEmpty(type) && (TransferType.HTTP.equals(type) || TransferType.HTTPS.equals(type));
    }

    /* JADX WARN: Removed duplicated region for block: B:4:0x0007 A[ORIG_RETURN, RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0011 A[RETURN, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean isChangeConfiguration(java.lang.String r2) {
        /*
            r1 = this;
            int r0 = r2.hashCode()
            switch(r0) {
                case -2121227002: goto L9;
                case -1821344457: goto L13;
                case -1510895644: goto L1c;
                case -1494056596: goto L25;
                case -1464235374: goto L2e;
                case -1452406955: goto L37;
                case -1273715303: goto L40;
                case -384829039: goto L49;
                case -180335624: goto L52;
                case 296371243: goto L5b;
                case 430365494: goto L64;
                case 668463383: goto L6d;
                case 737726581: goto L76;
                case 753716873: goto L7f;
                case 780683099: goto L88;
                case 1042090510: goto L92;
                case 1217039096: goto L9c;
                case 1357152103: goto La6;
                case 1570397887: goto Lb0;
                case 1577972695: goto Lba;
                case 1598913502: goto Lc4;
                case 1669783276: goto Lce;
                case 1782947279: goto Ld8;
                default: goto L7;
            }
        L7:
            r0 = 0
        L8:
            return r0
        L9:
            java.lang.String r0 = "NumberOfConnectors"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L7
        L11:
            r0 = 1
            goto L8
        L13:
            java.lang.String r0 = "AuthorizeRemoteTxRequests"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        L1c:
            java.lang.String r0 = "StopTxnAlignedDataMaxLength"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        L25:
            java.lang.String r0 = "UnlockConnectorOnEVSideDisconnect"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        L2e:
            java.lang.String r0 = "SupportedFeatureProfiles"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        L37:
            java.lang.String r0 = "ReserveConnectorZeroSupported"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        L40:
            java.lang.String r0 = "LocalAuthListMaxLength"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        L49:
            java.lang.String r0 = "MeterValuesSampledDataMaxLength"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        L52:
            java.lang.String r0 = "GetConfigurationMaxKeys"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        L5b:
            java.lang.String r0 = "ChargeProfileMaxStackLevel"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        L64:
            java.lang.String r0 = "ChargingScheduleMaxPeriods"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        L6d:
            java.lang.String r0 = "ResetRetries"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        L76:
            java.lang.String r0 = "ChargingScheduleAllowedChargingRateUnit"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        L7f:
            java.lang.String r0 = "SendLocalListMaxLength"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        L88:
            java.lang.String r0 = "MaxChargingProfilesInstalled"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        L92:
            java.lang.String r0 = "StopTxnSampledDataMaxLength"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        L9c:
            java.lang.String r0 = "SupportedFeatureProfilesMaxLength"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        La6:
            java.lang.String r0 = "MeterValuesAlignedDataMaxLength"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        Lb0:
            java.lang.String r0 = "StopTransactionOnEVSideDisconnect"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        Lba:
            java.lang.String r0 = "ConnectorSwitch3to1PhaseSupported"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        Lc4:
            java.lang.String r0 = "ConnectorPhaseRotationMaxLength"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        Lce:
            java.lang.String r0 = "ConnectorPhaseRotation"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        Ld8:
            java.lang.String r0 = "TransactionMessageAttempts"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.protocol.ocpp.handler.OcppProtocolAgent.isChangeConfiguration(java.lang.String):boolean");
    }

    /* JADX WARN: Removed duplicated region for block: B:4:0x0007 A[ORIG_RETURN, RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0011 A[RETURN, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private boolean isSupportedConfig(java.lang.String r2) {
        /*
            Method dump skipped, instructions count: 614
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.protocol.ocpp.handler.OcppProtocolAgent.isSupportedConfig(java.lang.String):boolean");
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:4:0x0007 A[ORIG_RETURN, RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0011 A[RETURN, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean isChargeMsg(java.lang.String r2) {
        /*
            r1 = this;
            int r0 = r2.hashCode()
            switch(r0) {
                case -977722974: goto L9;
                case -815388727: goto L13;
                case -391574318: goto L1c;
                case -362842058: goto L25;
                case 77777212: goto L2e;
                case 280557722: goto L37;
                case 641037660: goto L40;
                default: goto L7;
            }
        L7:
            r0 = 0
        L8:
            return r0
        L9:
            java.lang.String r0 = "RemoteStartTransaction"
            boolean r0 = r2.equals(r0)
            if (r0 == 0) goto L7
        L11:
            r0 = 1
            goto L8
        L13:
            java.lang.String r0 = "Authorize"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        L1c:
            java.lang.String r0 = "CancelReservation"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        L25:
            java.lang.String r0 = "RemoteStopTransaction"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        L2e:
            java.lang.String r0 = "StartTransaction"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        L37:
            java.lang.String r0 = "ReserveNow"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        L40:
            java.lang.String r0 = "StopTransaction"
            boolean r0 = r2.equals(r0)
            if (r0 != 0) goto L11
            goto L7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.protocol.ocpp.handler.OcppProtocolAgent.isChargeMsg(java.lang.String):boolean");
    }

    public int getPhase() {
        PHASE phase = HardwareStatusCacheProvider.getInstance().getHardwareStatus().getPhase();
        if (PHASE.SINGLE_PHASE.getPhase() == phase.getPhase() || PHASE.THREE_PHASE.getPhase() != phase.getPhase()) {
            return 1;
        }
        return 3;
    }

    public int convertWeek() {
        int week = TimeUtils.getWeek(System.currentTimeMillis());
        if (week == 1) {
            return 7;
        }
        int i = week - 1;
        return week;
    }
}
