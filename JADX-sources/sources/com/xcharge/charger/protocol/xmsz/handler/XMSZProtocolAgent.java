package com.xcharge.charger.protocol.xmsz.handler;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.view.MotionEventCompat;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.UpgradeProgress;
import com.xcharge.charger.data.bean.device.MobileNet;
import com.xcharge.charger.data.bean.device.Network;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.type.PHASE;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SoftwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.NetworkStatusObserver;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.xmsz.bean.XMSZConfig;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.charger.protocol.xmsz.bean.XMSZPileStatus;
import com.xcharge.charger.protocol.xmsz.bean.XMSZPortStatus;
import com.xcharge.charger.protocol.xmsz.bean.cloud.BootNotificationResponse;
import com.xcharge.charger.protocol.xmsz.bean.cloud.HeartBeatResponse;
import com.xcharge.charger.protocol.xmsz.bean.cloud.RemoteStartChargingRequest;
import com.xcharge.charger.protocol.xmsz.bean.cloud.RemoteStopChargingRequest;
import com.xcharge.charger.protocol.xmsz.bean.cloud.UpdateFirmwareRequest;
import com.xcharge.charger.protocol.xmsz.bean.device.BootNotificationRequest;
import com.xcharge.charger.protocol.xmsz.bean.device.FirmwareUpdateInformRequest;
import com.xcharge.charger.protocol.xmsz.bean.device.HeartBeatRequest;
import com.xcharge.charger.protocol.xmsz.bean.device.StatusNotificationRequest;
import com.xcharge.charger.protocol.xmsz.bean.device.UpdateFirmwareResponse;
import com.xcharge.charger.protocol.xmsz.router.XMSZDCAPGateway;
import com.xcharge.charger.protocol.xmsz.session.XMSZChargeSession;
import com.xcharge.charger.protocol.xmsz.session.XMSZRequestSession;
import com.xcharge.charger.protocol.xmsz.session.XMSZUpgradeSession;
import com.xcharge.charger.protocol.xmsz.type.XMSZ_PILE_PRESENCE_STATE;
import com.xcharge.charger.protocol.xmsz.type.XMSZ_REQUEST_STATE;
import com.xcharge.common.utils.FileUtils;
import com.xcharge.common.utils.HandlerTimer;
import com.xcharge.common.utils.HttpDownloadManager;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.TimeUtils;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

/* loaded from: classes.dex */
public class XMSZProtocolAgent extends IoHandlerAdapter {
    public static final int MSG_CONNECTED = 69634;
    public static final int MSG_CONNECT_ERROR = 69636;
    public static final int MSG_DISCONNECTED = 69635;
    public static final int MSG_HERAT_BEAT_TIMER = 69641;
    public static final int MSG_INIT_CONNECTION = 69633;
    public static final int MSG_RECEIVED = 69639;
    public static final int MSG_REQUEST_CHECK_TIMER = 69640;
    public static final int MSG_SEND = 69637;
    public static final int MSG_SENDED = 69638;
    public static final int TIMEOUT_CONNECT = 6;
    public static final int TIMEOUT_RESPONSE = 3;
    public static final int TIMEOUT_SEND = 2;
    public static final int TIMEOUT_WAIT_PLUGIN = 60;
    public static final int TIMEOUT_WAIT_PLUGOUT = -1;
    public static final int TIMEOUT_WAIT_START_CHARGE = -1;
    private static XMSZProtocolAgent instance = null;
    private XMSZConfig configCache = null;
    private String siteId = "15";
    private String pileNo = "4020161130009000";
    public String firewareModel = null;
    private HandlerThread thread = null;
    private MsgHandler handler = null;
    private HandlerTimer handlerTimer = null;
    private Context context = null;
    private ThreadPoolExecutor connectThreadPoolExecutor = null;
    private IoSession session = null;
    private int reconnectFailCnt = 0;
    private XMSZ_PILE_PRESENCE_STATE xmszPilePresenceStatus = XMSZ_PILE_PRESENCE_STATE.offline;
    private boolean isPilePermitCharge = true;
    private long heartBeatInterval = 0;
    private int failHeartBeatCnt = 0;
    private XMSZPileStatus latestPileStatus = new XMSZPileStatus();
    private AtomicInteger requestSeq = null;
    private XMSZUpgradeSession upgradeSession = null;
    private HashMap<String, XMSZPortHandler> portHandlers = null;
    private HashMap<String, SendRequestState> sendReqestState = null;
    private NetworkStatusObserver networkStatusObserver = null;

    public static XMSZProtocolAgent getInstance() {
        if (instance == null) {
            instance = new XMSZProtocolAgent();
        }
        return instance;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class SendRequestState {
        XMSZMessage request;
        XMSZ_REQUEST_STATE status;
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
            switch (msg.what) {
                case 69633:
                    XMSZProtocolAgent.this.connect();
                    break;
                case 69634:
                    Log.i("XMSZProtocolAgent.MsgHandler", "connected !!!");
                    LogUtils.cloudlog("xmsz cloud connected !!!");
                    XMSZProtocolAgent.this.reconnectFailCnt = 0;
                    ChargeStatusCacheProvider.getInstance().updateCloudConnected(true);
                    XMSZProtocolAgent.this.handlerTimer.startTimer(1000L, 69640, null);
                    XMSZProtocolAgent.this.sendBootNotificationRequest();
                    break;
                case 69635:
                    Log.i("XMSZProtocolAgent.MsgHandler", "disconnected !!!");
                    LogUtils.cloudlog("xmsz cloud disconnected !!!");
                    ChargeStatusCacheProvider.getInstance().updateCloudConnected(false);
                    XMSZProtocolAgent.this.handlerTimer.stopTimer(69640);
                    XMSZProtocolAgent.this.handler.sendEmptyMessage(69633);
                    break;
                case 69636:
                    Log.i("XMSZProtocolAgent.MsgHandler", "failed to connect !!!");
                    if (XMSZ_PILE_PRESENCE_STATE.online.equals(XMSZProtocolAgent.this.xmszPilePresenceStatus)) {
                        XMSZProtocolAgent.this.reconnectFailCnt++;
                        if (XMSZProtocolAgent.this.reconnectFailCnt >= 5) {
                            XMSZProtocolAgent.this.handlePileOffline();
                            if (HardwareStatusCacheProvider.getInstance().isNetworkConnected()) {
                                LogUtils.applog("xmsz pile has been offline, try to diagnosis network connectivity ...");
                                new Thread(new Runnable() { // from class: com.xcharge.charger.protocol.xmsz.handler.XMSZProtocolAgent.MsgHandler.1
                                    @Override // java.lang.Runnable
                                    public void run() {
                                        DCAPProxy.getInstance().networkConnectivityDiagnosis();
                                    }
                                }).start();
                            }
                        }
                    }
                    XMSZProtocolAgent.this.handler.sendEmptyMessageDelayed(69633, 5000L);
                    break;
                case 69637:
                    XMSZMessage xmszMessage = (XMSZMessage) msg.obj;
                    if (XMSZProtocolAgent.this.isConnected()) {
                        XMSZProtocolAgent.this.session.write(xmszMessage);
                        if (XMSZProtocolAgent.this.isRequestMessage(xmszMessage.getHead().getFunctionCode())) {
                            SendRequestState reqestState = new SendRequestState(null);
                            reqestState.request = xmszMessage;
                            reqestState.status = XMSZ_REQUEST_STATE.sending;
                            reqestState.timestamp = System.currentTimeMillis();
                            XMSZProtocolAgent.this.sendReqestState.put(String.valueOf((int) xmszMessage.getHead().getPacketID()), reqestState);
                            break;
                        }
                    } else if (XMSZProtocolAgent.this.isRequestMessage(xmszMessage.getHead().getFunctionCode())) {
                        XMSZProtocolAgent.this.handleSendRequestFail(xmszMessage);
                        break;
                    }
                    break;
                case 69638:
                    XMSZMessage xmszMessage2 = (XMSZMessage) msg.obj;
                    Log.d("XMSZProtocolAgent.MsgHandler", "succeed to send xmsz msg: " + xmszMessage2.toJson());
                    if (XMSZProtocolAgent.this.isRequestMessage(xmszMessage2.getHead().getFunctionCode())) {
                        String requestSeq = String.valueOf((int) xmszMessage2.getHead().getPacketID());
                        SendRequestState reqestState2 = (SendRequestState) XMSZProtocolAgent.this.sendReqestState.get(requestSeq);
                        if (reqestState2 != null) {
                            reqestState2.status = XMSZ_REQUEST_STATE.sended;
                            reqestState2.timestamp = System.currentTimeMillis();
                            XMSZProtocolAgent.this.handleSendRequestOk(reqestState2.request);
                            break;
                        } else {
                            Log.w("XMSZProtocolAgent.MsgHandler", "maybe timeout to send xmsz request msg: " + xmszMessage2.toJson());
                            break;
                        }
                    }
                    break;
                case 69639:
                    XMSZMessage xmszMessage3 = (XMSZMessage) msg.obj;
                    Log.d("XMSZProtocolAgent.MsgHandler", "received xmsz msg: " + xmszMessage3.toJson());
                    if (!XMSZProtocolAgent.this.isRequestMessage(xmszMessage3.getHead().getFunctionCode())) {
                        if (!xmszMessage3.verifyCheckSum()) {
                            Log.w("XMSZProtocolAgent.MsgHandler", "response message checksum error, discard it !!!");
                            return;
                        }
                        xmszMessage3.setCheckOk(true);
                        String responseSeq = String.valueOf((int) xmszMessage3.getHead().getPacketID());
                        SendRequestState reqestState3 = (SendRequestState) XMSZProtocolAgent.this.sendReqestState.get(responseSeq);
                        if (reqestState3 != null) {
                            XMSZMessage request = reqestState3.request;
                            XMSZProtocolAgent.this.sendReqestState.remove(responseSeq);
                            xmszMessage3.setPort(request.getPort());
                            XMSZProtocolAgent.this.dispatchXMSZMessage(xmszMessage3, request);
                            break;
                        } else {
                            Log.w("XMSZProtocolAgent.MsgHandler", "maybe timeout to wait for response msg: " + xmszMessage3.toJson());
                            break;
                        }
                    } else {
                        boolean isCheckOk = xmszMessage3.verifyCheckSum();
                        xmszMessage3.setCheckOk(isCheckOk);
                        if (!isCheckOk) {
                            Log.w("XMSZProtocolAgent.MsgHandler", "request message checksum error !!!");
                        }
                        XMSZProtocolAgent.this.dispatchXMSZMessage(xmszMessage3, null);
                        break;
                    }
                case 69640:
                    XMSZProtocolAgent.this.requestTimeoutCheck();
                    XMSZProtocolAgent.this.handlerTimer.startTimer(1000L, 69640, null);
                    break;
                case 69641:
                    Log.d("XMSZProtocolAgent.MsgHandler", "send xmsz heart beat periodically !!!");
                    XMSZProtocolAgent.this.sendHeartBeatRequest();
                    XMSZProtocolAgent.this.handlerTimer.startTimer(XMSZProtocolAgent.this.heartBeatInterval * 1000, 69641, null);
                    break;
                case 135169:
                    Uri uri = (Uri) msg.obj;
                    XMSZProtocolAgent.this.handleNetworkStatusChanged(uri);
                    break;
            }
            super.handleMessage(msg);
        }
    }

    public void init(Context context) {
        this.context = context;
        this.requestSeq = new AtomicInteger(-1);
        this.portHandlers = new HashMap<>();
        HashMap<String, Port> ports = HardwareStatusCacheProvider.getInstance().getPorts();
        if (ports != null) {
            for (String port : ports.keySet()) {
                XMSZPortHandler portHandler = new XMSZPortHandler();
                portHandler.init(context, port, this);
                this.portHandlers.put(port, portHandler);
            }
        }
        this.connectThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue(1), new RejectedExecutionHandler() { // from class: com.xcharge.charger.protocol.xmsz.handler.XMSZProtocolAgent.1
            @Override // java.util.concurrent.RejectedExecutionHandler
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                Log.w("XMSZProtocolAgent.ThreadPoolExecutor.rejectedExecution", "rejected runnable: " + r.toString() + ", active runnables: " + executor.getActiveCount());
            }
        });
        this.sendReqestState = new HashMap<>();
        this.thread = new HandlerThread("XMSZProtocolAgent", 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(context);
        this.networkStatusObserver = new NetworkStatusObserver(context, this.handler);
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor(Network.class.getSimpleName()), true, this.networkStatusObserver);
        this.configCache = new XMSZConfig();
        this.configCache.init(this.context);
        String sn = HardwareStatusCacheProvider.getInstance().getSn();
        this.firewareModel = sn.substring(0, 4);
        HashMap<String, String> platformData = SystemSettingCacheProvider.getInstance().getPlatformCustomizedData();
        if (TextUtils.isEmpty(this.siteId)) {
            this.siteId = platformData.get("site");
        }
        if (TextUtils.isEmpty(this.pileNo)) {
            this.pileNo = String.valueOf(this.configCache.getVendor_name()) + sn;
        }
        XMSZPileStatus nowPileStatus = getPileStatusByPriority();
        updatePileStatusByPriority(nowPileStatus);
    }

    public void destroy() {
        this.context.getContentResolver().unregisterContentObserver(this.networkStatusObserver);
        disconnect();
        this.handlerTimer.destroy();
        this.handler.removeMessages(69633);
        this.handler.removeMessages(69634);
        this.handler.removeMessages(69635);
        this.handler.removeMessages(69636);
        this.handler.removeMessages(69637);
        this.handler.removeMessages(69638);
        this.handler.removeMessages(69639);
        this.handler.removeMessages(69640);
        this.handler.removeMessages(69641);
        this.thread.quit();
        for (XMSZPortHandler portHandler : this.portHandlers.values()) {
            portHandler.destroy();
        }
        this.portHandlers.clear();
        this.sendReqestState.clear();
        this.connectThreadPoolExecutor.shutdown();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isConnected() {
        return this.session != null && this.session.isConnected();
    }

    public void initConnection() {
        this.handler.sendEmptyMessage(69633);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class ConnectTask implements Runnable {
        private IoHandler ioHandler;
        private ProtocolCodecFilter protocolCodecFilter;

        public ConnectTask(ProtocolCodecFilter filter, IoHandler handler) {
            this.protocolCodecFilter = null;
            this.ioHandler = null;
            this.protocolCodecFilter = filter;
            this.ioHandler = handler;
        }

        @Override // java.lang.Runnable
        public void run() {
            IoConnector connector = new NioSocketConnector();
            connector.setConnectTimeoutMillis(6000L);
            connector.getFilterChain().addLast("XMSZProtocol", this.protocolCodecFilter);
            connector.setHandler(this.ioHandler);
            connector.getSessionConfig().setReadBufferSize(2048);
            connector.getSessionConfig().setWriteTimeout(2);
            String host = null;
            int port = -1;
            try {
                String[] hostPortSplit = XMSZProtocolAgent.this.configCache.getCenter_ip_port().split(":");
                host = hostPortSplit[0];
                port = Integer.parseInt(hostPortSplit[1]);
                ConnectFuture future = connector.connect(new InetSocketAddress(host, port));
                future.awaitUninterruptibly();
                XMSZProtocolAgent.this.session = future.getSession();
                XMSZProtocolAgent.this.handler.sendEmptyMessage(69634);
                XMSZProtocolAgent.this.session.getCloseFuture().awaitUninterruptibly();
                connector.dispose();
                XMSZProtocolAgent.this.handler.sendEmptyMessage(69635);
            } catch (Exception e) {
                Log.w("XMSZProtocolAgent.ConnectTask", "connect to " + host + ":" + port + " exception: " + Log.getStackTraceString(e));
                XMSZProtocolAgent.this.handler.sendEmptyMessage(69636);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void connect() {
        if (HardwareStatusCacheProvider.getInstance().isNetworkConnected()) {
            Log.i("XMSZProtocolAgent.connect", "init connection !!!");
            ProtocolCodecFilter protocolCodecFilter = new ProtocolCodecFilter(new XMSZMessageEncoder(), new XMSZMessageDecoder());
            this.connectThreadPoolExecutor.execute(new ConnectTask(protocolCodecFilter, this));
            return;
        }
        this.handler.sendEmptyMessageDelayed(69633, 5000L);
    }

    public void disconnect() {
        if (this.session != null) {
            Log.d("XMSZProtocolAgent.disconnect", "force to disconnect !!!");
            this.session.closeNow().awaitUninterruptibly(6000L);
        }
    }

    @Override // org.apache.mina.core.service.IoHandlerAdapter, org.apache.mina.core.service.IoHandler
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        Log.w("XMSZProtocolAgent.exceptionCaught", "exception: " + Log.getStackTraceString(cause));
        super.exceptionCaught(session, cause);
        disconnect();
    }

    @Override // org.apache.mina.core.service.IoHandlerAdapter, org.apache.mina.core.service.IoHandler
    public void inputClosed(IoSession session) throws Exception {
        Log.w("XMSZProtocolAgent.inputClosed", "session input closed: " + session.getId());
        super.inputClosed(session);
        LogUtils.cloudlog("xmsz cloud connection has been closed by remote !!!");
        disconnect();
    }

    @Override // org.apache.mina.core.service.IoHandlerAdapter, org.apache.mina.core.service.IoHandler
    public void messageReceived(IoSession session, Object message) throws Exception {
        super.messageReceived(session, message);
        XMSZMessage msg = (XMSZMessage) message;
        this.handler.sendMessage(this.handler.obtainMessage(69639, msg));
    }

    @Override // org.apache.mina.core.service.IoHandlerAdapter, org.apache.mina.core.service.IoHandler
    public void messageSent(IoSession session, Object message) throws Exception {
        super.messageSent(session, message);
        XMSZMessage msg = (XMSZMessage) message;
        this.handler.sendMessage(this.handler.obtainMessage(69638, msg));
    }

    @Override // org.apache.mina.core.service.IoHandlerAdapter, org.apache.mina.core.service.IoHandler
    public void sessionClosed(IoSession session) throws Exception {
        super.sessionClosed(session);
        Log.d("XMSZProtocolAgent.sessionClosed", "session closed: " + session.getId());
    }

    @Override // org.apache.mina.core.service.IoHandlerAdapter, org.apache.mina.core.service.IoHandler
    public void sessionCreated(IoSession session) throws Exception {
        super.sessionCreated(session);
        Log.d("XMSZProtocolAgent.sessionCreated", "session created: " + session.getId());
    }

    @Override // org.apache.mina.core.service.IoHandlerAdapter, org.apache.mina.core.service.IoHandler
    public void sessionOpened(IoSession session) throws Exception {
        super.sessionOpened(session);
        Log.d("XMSZProtocolAgent.sessionOpened", "session opend: " + session.getId());
    }

    @Override // org.apache.mina.core.service.IoHandlerAdapter, org.apache.mina.core.service.IoHandler
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        super.sessionIdle(session, status);
        Log.d("XMSZProtocolAgent.sessionIdle", "session idle: " + session.getIdleCount(status));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleNetworkStatusChanged(Uri uri) {
        Log.i("XMSZProtocolAgent.handleNetworkStatusChanged", "network status changed, uri: " + uri.toString());
        String lastSegment = uri.getLastPathSegment();
        if (!"connected".equals(lastSegment) && "disconnected".equals(lastSegment)) {
            disconnect();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendBootNotificationRequest() {
        try {
            BootNotificationRequest bootNotificationRequest = new BootNotificationRequest();
            byte connectorCount = (byte) (HardwareStatusCacheProvider.getInstance().getPorts().size() & MotionEventCompat.ACTION_MASK);
            String firmwareVersion = String.valueOf(SoftwareStatusCacheProvider.getInstance().getFirewareVer()) + "-" + SoftwareStatusCacheProvider.getInstance().getAppVer();
            PHASE phase = HardwareStatusCacheProvider.getInstance().getHardwareStatus().getPhase();
            int powerRated = new BigDecimal(((HardwareStatusCacheProvider.getInstance().getAmpCapacity() * (PHASE.SINGLE_PHASE.equals(phase) ? 1 : 3)) * 220.0d) / 10.0d).setScale(0, 3).intValue();
            bootNotificationRequest.setSN(this.pileNo);
            bootNotificationRequest.setModel(this.firewareModel);
            bootNotificationRequest.setVendorId((byte) (this.configCache.getVendor_id() & MotionEventCompat.ACTION_MASK));
            bootNotificationRequest.setFirmwareVersion(firmwareVersion);
            bootNotificationRequest.setConnectorCount(connectorCount);
            bootNotificationRequest.setPowerRated(powerRated);
            String activeNet = HardwareStatusCacheProvider.getInstance().getActiveNetwork();
            if (Network.NETWORK_TYPE_MOBILE.equals(activeNet) || "2G".equals(activeNet) || "3G".equals(activeNet) || "4G".equals(activeNet)) {
                MobileNet mNet = HardwareStatusCacheProvider.getInstance().getMobileNetStatus();
                bootNotificationRequest.setImsi(mNet.getIMSI());
                bootNotificationRequest.setIccid(mNet.getICCID());
            }
            XMSZHead head = createRequestHead((byte) 17);
            head.setPacketLength(bootNotificationRequest.bodyToBytes().length + 12);
            bootNotificationRequest.setHead(head);
            bootNotificationRequest.setCrc16(bootNotificationRequest.calcCheckSum());
            sendMessage(bootNotificationRequest);
            Log.i("XMSZProtocolAgent.sendBootNotificationRequest", "send bootNotificationRequest: " + bootNotificationRequest.toJson());
        } catch (Exception e) {
            Log.w("XMSZProtocolAgent.sendBootNotificationRequest", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendHeartBeatRequest() {
        try {
            HeartBeatRequest heartBeatRequest = new HeartBeatRequest();
            XMSZHead head = createRequestHead((byte) 21);
            head.setPacketLength(heartBeatRequest.bodyToBytes().length + 12);
            heartBeatRequest.setHead(head);
            heartBeatRequest.setCrc16(heartBeatRequest.calcCheckSum());
            sendMessage(heartBeatRequest);
            Log.i("XMSZProtocolAgent.sendHeartBeatRequest", "send sendHeartBeatRequest: " + heartBeatRequest.toJson());
        } catch (Exception e) {
            Log.w("XMSZProtocolAgent.sendHeartBeatRequest", Log.getStackTraceString(e));
        }
    }

    public void sendPileStatusNotificationRequest() {
        try {
            StatusNotificationRequest statusNotificationRequest = new StatusNotificationRequest();
            statusNotificationRequest.setConnectorId((byte) 0);
            statusNotificationRequest.setConnectorPlugStatus((byte) 0);
            statusNotificationRequest.setPointStatusCode(this.latestPileStatus.getPileStatus());
            statusNotificationRequest.setPointErrorCode(this.latestPileStatus.getPileError());
            statusNotificationRequest.setTime(System.currentTimeMillis() / 1000);
            XMSZHead head = createRequestHead((byte) 22);
            head.setPacketLength(statusNotificationRequest.bodyToBytes().length + 12);
            statusNotificationRequest.setHead(head);
            statusNotificationRequest.setCrc16(statusNotificationRequest.calcCheckSum());
            sendMessage(statusNotificationRequest);
            Log.i("XMSZProtocolAgent.sendPileStatusNotificationRequest", "send statusNotificationRequest: " + statusNotificationRequest.toJson());
        } catch (Exception e) {
            Log.w("XMSZProtocolAgent.sendPileStatusNotificationRequest", Log.getStackTraceString(e));
        }
    }

    public synchronized void sendPileStatusNotificationRequestByPortStatusChanged() {
        XMSZPileStatus newPileStatus = getPileStatusByPriority();
        if (updatePileStatusByPriority(newPileStatus)) {
            sendPileStatusNotificationRequest();
        }
    }

    public XMSZChargeSession getChargeSessionByPort(String port) {
        XMSZPortHandler xmszPortHandler = getPortHandler(port);
        if (xmszPortHandler == null) {
            Log.w("XMSZProtocolAgent.getChargeSessionByPort", "no available port handler for port: " + port);
            return null;
        }
        return xmszPortHandler.getChargeSession();
    }

    public XMSZChargeSession getChargeSessionByChargeId(String chargeId) {
        for (String port : this.portHandlers.keySet()) {
            XMSZPortHandler xmszPortHandler = getPortHandler(port);
            if (xmszPortHandler.hasCharge(chargeId)) {
                return xmszPortHandler.getChargeSession();
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchXMSZMessage(XMSZMessage msg, XMSZMessage sendedRequest) {
        boolean isRequest = isRequestMessage(msg.getHead().getFunctionCode());
        if (isRequest) {
            handleRequestMessage(msg);
        } else {
            handleResponseMessage(msg, sendedRequest);
        }
    }

    private void handleRequestMessage(XMSZMessage request) {
        byte functionCode = request.getHead().getFunctionCode();
        switch (functionCode) {
            case 2:
                RemoteStartChargingRequest remoteStartChargingRequest = (RemoteStartChargingRequest) request;
                String port = String.valueOf(remoteStartChargingRequest.getConnectorId() & 255);
                remoteStartChargingRequest.setPort(port);
                XMSZPortHandler portHandler = getPortHandler(port);
                if (portHandler != null) {
                    portHandler.sendMessage(portHandler.obtainMessage(73735, remoteStartChargingRequest));
                    return;
                } else {
                    Log.w("XMSZProtocolAgent.handleRequestMessage", "unsupported port in remoteStartChargingRequest: " + remoteStartChargingRequest.toJson());
                    return;
                }
            case 3:
                RemoteStopChargingRequest remoteStopChargingRequest = (RemoteStopChargingRequest) request;
                remoteStopChargingRequest.getTransactionId();
                remoteStopChargingRequest.setPort("1");
                XMSZPortHandler portHandler2 = getPortHandler("1");
                if (portHandler2 != null) {
                    portHandler2.sendMessage(portHandler2.obtainMessage(73735, remoteStopChargingRequest));
                    return;
                } else {
                    Log.w("XMSZProtocolAgent.handleRequestMessage", "unavailable transaction id param in remoteStopChargingRequest: " + remoteStopChargingRequest.toJson());
                    return;
                }
            default:
                return;
        }
    }

    public XMSZUpgradeSession getUpgradeSession() {
        if (this.upgradeSession == null) {
            this.upgradeSession = new XMSZUpgradeSession();
        }
        return this.upgradeSession;
    }

    public void clearUpgradeSession() {
        String downloadFile = getUpgradeSession().getDownloadFile();
        if (!TextUtils.isEmpty(downloadFile)) {
            FileUtils.deleteFile(downloadFile);
        }
        this.upgradeSession = null;
    }

    private void handleUpdateFirmwareRequest(final XMSZMessage request) {
        UpdateFirmwareRequest updateFirmwareRequest = (UpdateFirmwareRequest) request;
        Log.i("XMSZProtocolAgent.handleUpdateFirmwareRequest", "receive updateFirmwareRequest: " + updateFirmwareRequest.toJson());
        XMSZUpgradeSession upgradeSession = getUpgradeSession();
        if (TextUtils.isEmpty(upgradeSession.getStage())) {
            updateFirmwareResponse(updateFirmwareRequest, (byte) 1);
            upgradeSession.setStage(UpgradeProgress.STAGE_DOWNLOAD);
            upgradeSession.setRequestUpgrade(updateFirmwareRequest);
            String url = updateFirmwareRequest.getLocation();
            upgradeSession.setDownloadFile("/data/data/com.xcharge.charger/download/upgrade/update.dat");
            Log.i("XMSZProtocolAgent.handleUpdateFirmwareRequest", "start download ..., url: " + url);
            downloadProgress(200, 1, 0);
            HttpDownloadManager.getInstance().downloadFile(this.context, url, "/data/data/com.xcharge.charger/download/upgrade/update.dat", new HttpDownloadManager.DownLoadListener() { // from class: com.xcharge.charger.protocol.xmsz.handler.XMSZProtocolAgent.2
                @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                public void onDownLoadPercentage(long curPosition, long total) {
                }

                @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                public void onDownLoadPercentage(int p) {
                    XMSZProtocolAgent.this.downloadProgress(200, 2, p);
                }

                @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                public void onDownLoadFail() {
                    XMSZProtocolAgent.this.downloadProgress(ErrorCode.EC_UPGRADE_DOWNLOAD_FAIL, 0, 0);
                    XMSZProtocolAgent.this.reportFirmwareUpdateInformRequest((byte) 1);
                    XMSZProtocolAgent.this.clearUpgradeSession();
                }

                @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                public void onDownLoadComplete() {
                    XMSZProtocolAgent.this.reportFirmwareUpdateInformRequest((byte) 0);
                    XMSZDCAPGateway.getInstance().sendMessage(XMSZDCAPGateway.getInstance().obtainMessage(77827, request));
                }
            });
            return;
        }
        updateFirmwareResponse(updateFirmwareRequest, (byte) 3);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void downloadProgress(int error, int status, int progress) {
        UpgradeProgress upgradeProgress = SoftwareStatusCacheProvider.getInstance().getUpgradeProgress();
        upgradeProgress.setStage(UpgradeProgress.STAGE_DOWNLOAD);
        upgradeProgress.setUpgradeData(null);
        if (error != 200) {
            upgradeProgress.setError(new ErrorCode(error));
        } else {
            upgradeProgress.setStatus(status);
            upgradeProgress.setProgress(progress);
        }
        SoftwareStatusCacheProvider.getInstance().updateUpgradeProgress(upgradeProgress);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reportFirmwareUpdateInformRequest(byte updateStatus) {
        try {
            FirmwareUpdateInformRequest firmwareUpdateInformRequest = new FirmwareUpdateInformRequest();
            firmwareUpdateInformRequest.setUpdateStatus(updateStatus);
            XMSZHead head = createRequestHead((byte) 23);
            head.setPacketLength(firmwareUpdateInformRequest.bodyToBytes().length + 12);
            firmwareUpdateInformRequest.setHead(head);
            firmwareUpdateInformRequest.setCrc16(firmwareUpdateInformRequest.calcCheckSum());
            sendMessage(firmwareUpdateInformRequest);
            Log.i("XMSZProtocolAgent.reportFirmwareUpdateInformRequest", "send firmwareUpdateInformRequest: " + firmwareUpdateInformRequest.toJson());
        } catch (Exception e) {
            Log.w("XMSZProtocolAgent.reportFirmwareUpdateInformRequest", Log.getStackTraceString(e));
        }
    }

    private void handleResponseMessage(XMSZMessage response, XMSZMessage sendedRequest) {
        String port = response.getPort();
        if (!TextUtils.isEmpty(port)) {
            XMSZPortHandler portHandler = getPortHandler(port);
            XMSZRequestSession xmszRequestSession = new XMSZRequestSession();
            xmszRequestSession.setSendedRequest(sendedRequest);
            xmszRequestSession.setResponse(response);
            portHandler.sendMessage(portHandler.obtainMessage(73736, xmszRequestSession));
            return;
        }
        byte functionCode = response.getHead().getFunctionCode();
        switch (functionCode) {
            case -111:
                Log.i("XMSZProtocolAgent.handleResponseMessage", "receive xmsz BootNotificationResponse:" + response.toJson());
                handleBootNotificationResponse(sendedRequest, response);
                return;
            case -107:
                Log.i("XMSZProtocolAgent.handleResponseMessage", "receive xmsz HeartBeatResponse:" + response.toJson());
                handleHeartBeatResponse(sendedRequest, response);
                return;
            case -105:
                Log.i("XMSZProtocolAgent.handleResponseMessage", "receive xmsz FirmwareUpdateInformResponse:" + response.toJson());
                return;
            default:
                return;
        }
    }

    private void handleHeartBeatResponse(XMSZMessage request, XMSZMessage response) {
        HeartBeatResponse heartBeatResponse = (HeartBeatResponse) response;
        byte returnCode = heartBeatResponse.getReturnCode();
        if (2 == returnCode) {
            this.failHeartBeatCnt++;
            if (this.failHeartBeatCnt >= 3) {
                Log.w("XMSZProtocolAgent.handleHeartBeatResponse", "failed to heart beat 3 times, consider be offline !!!");
                this.failHeartBeatCnt = 0;
                if (XMSZ_PILE_PRESENCE_STATE.online.equals(this.xmszPilePresenceStatus)) {
                    handlePileOffline();
                    return;
                }
                return;
            }
            Log.w("XMSZProtocolAgent.handleHeartBeatResponse", "failed to heart beat request: " + request.toJson());
            return;
        }
        this.failHeartBeatCnt = 0;
        long serverUtcTime = heartBeatResponse.getTime() * 1000;
        long localTime = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Log.d("XMSZProtocolAgent.handleHeartBeatResponse", "server time: " + TimeUtils.getISO8601Format(serverUtcTime, "+08:00"));
        Log.d("XMSZProtocolAgent.handleHeartBeatResponse", "local time: " + dateFormat.format(Long.valueOf(localTime)));
        if (!ChargeStatusCacheProvider.getInstance().isCloudTimeSynch() || (ChargeStatusCacheProvider.getInstance().isCloudTimeSynch() && Math.abs(localTime - serverUtcTime) >= 30000)) {
            Log.i("XMSZProtocolAgent.handleHeartBeatResponse", "sync local time by server time: " + TimeUtils.getISO8601Format(serverUtcTime, "+08:00"));
            SystemClock.setCurrentTimeMillis(serverUtcTime);
            LogUtils.syslog("synch cloud time: " + TimeUtils.getISO8601Format(serverUtcTime, "+08:00"));
            ChargeStatusCacheProvider.getInstance().updateCloudTimeSynch(true);
        }
        if (XMSZ_PILE_PRESENCE_STATE.offline.equals(this.xmszPilePresenceStatus)) {
            handlePileOnline(this.isPilePermitCharge);
        }
    }

    private void handleBootNotificationResponse(XMSZMessage request, XMSZMessage response) {
        BootNotificationResponse bootNotificationResponse = (BootNotificationResponse) response;
        this.isPilePermitCharge = bootNotificationResponse.getReturnCode() != 0;
        this.heartBeatInterval = bootNotificationResponse.getHeartBeatInterval();
        long newPointId = bootNotificationResponse.getPointId();
        long oldPointId = this.configCache.getPoint_id();
        if (newPointId != oldPointId) {
            this.configCache.setPoint_id(newPointId);
            this.configCache.persist(this.context);
        }
        request.setRetrySend(0);
        handlePileOnline(this.isPilePermitCharge);
        LogUtils.cloudlog("xmsz cloud login !!!");
    }

    public void updateFirmwareResponse(UpdateFirmwareRequest request, byte returnCode) {
        try {
            UpdateFirmwareResponse updateFirmwareResponse = new UpdateFirmwareResponse();
            updateFirmwareResponse.setReturnCode(returnCode);
            XMSZHead head = createResponseHead(request.getHead());
            head.setPacketLength(updateFirmwareResponse.bodyToBytes().length + 12);
            updateFirmwareResponse.setHead(head);
            updateFirmwareResponse.setCrc16(updateFirmwareResponse.calcCheckSum());
            sendMessage(updateFirmwareResponse);
            Log.i("XMSZProtocolAgent.updateFirmwareResponse", "send updateFirmwareResponse: " + updateFirmwareResponse.toJson());
        } catch (Exception e) {
            Log.w("XMSZProtocolAgent.updateFirmwareResponse", Log.getStackTraceString(e));
        }
    }

    private XMSZPortHandler getPortHandler(String port) {
        return this.portHandlers.get(port);
    }

    private void handlePileOnline(boolean isPilePermitCharge) {
        this.xmszPilePresenceStatus = XMSZ_PILE_PRESENCE_STATE.online;
        this.handlerTimer.stopTimer(69641);
        sendHeartBeatRequest();
        this.handlerTimer.startTimer(this.heartBeatInterval * 1000, 69641, null);
        for (XMSZPortHandler portHandler : this.portHandlers.values()) {
            portHandler.sendMessage(portHandler.obtainMessage(73729, Boolean.valueOf(isPilePermitCharge)));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePileOffline() {
        this.xmszPilePresenceStatus = XMSZ_PILE_PRESENCE_STATE.offline;
        for (XMSZPortHandler portHandler : this.portHandlers.values()) {
            portHandler.sendMessage(portHandler.obtainMessage(73730));
        }
    }

    public byte getRequestSequence() {
        return (byte) (this.requestSeq.incrementAndGet() & MotionEventCompat.ACTION_MASK);
    }

    public void sendMessage(XMSZMessage msg) {
        this.handler.sendMessage(this.handler.obtainMessage(69637, msg));
    }

    public String getPileNo() {
        return this.pileNo;
    }

    public XMSZConfig GetConfig() {
        return this.configCache;
    }

    public String getQrcodeContent(String port) {
        return String.valueOf(this.configCache.getQrcode_url()) + "?" + String.format("%04d%03d%02d1000000", Integer.valueOf(Integer.parseInt(this.siteId)), Long.valueOf(this.configCache.getPoint_id()), Integer.valueOf(Integer.parseInt(port)));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void requestTimeoutCheck() {
        if (this.sendReqestState.size() > 0) {
            Iterator<Map.Entry<String, SendRequestState>> it2 = this.sendReqestState.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry<String, SendRequestState> entry = it2.next();
                SendRequestState requestState = entry.getValue();
                XMSZ_REQUEST_STATE state = requestState.status;
                long timestamp = requestState.timestamp;
                if (XMSZ_REQUEST_STATE.sending.equals(state)) {
                    if (System.currentTimeMillis() - timestamp > 2000) {
                        it2.remove();
                        handleSendRequestFail(requestState.request);
                    }
                } else if (XMSZ_REQUEST_STATE.sended.equals(state) && System.currentTimeMillis() - timestamp > 3000) {
                    it2.remove();
                    handleRequestTimeout(requestState.request);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleSendRequestOk(XMSZMessage request) {
        String port = request.getPort();
        if (!TextUtils.isEmpty(port)) {
            XMSZPortHandler portHandler = getPortHandler(port);
            portHandler.sendMessage(portHandler.obtainMessage(73731, request));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleSendRequestFail(XMSZMessage request) {
        String port = request.getPort();
        if (!TextUtils.isEmpty(port)) {
            XMSZPortHandler portHandler = getPortHandler(port);
            portHandler.sendMessage(portHandler.obtainMessage(73732, request));
            return;
        }
        byte functionCode = request.getHead().getFunctionCode();
        switch (functionCode) {
            case 17:
                if (request.getRetrySend() < 2) {
                    Log.w("XMSZProtocolAgent.handleSendRequestFail", "resend boot notify after 5 seconds !!!");
                    updateRequestForResend(request);
                    this.handler.sendMessageDelayed(this.handler.obtainMessage(69637, request), 5000L);
                    return;
                }
                Log.w("XMSZProtocolAgent.handleSendRequestFail", "failed to boot notify 3 times, diconnect !!!");
                getInstance().disconnect();
                return;
            case 21:
                this.failHeartBeatCnt++;
                if (this.failHeartBeatCnt >= 3) {
                    Log.w("XMSZProtocolAgent.handleSendRequestFail", "failed to heart beat 3 times, consider be offline !!!");
                    this.failHeartBeatCnt = 0;
                    if (XMSZ_PILE_PRESENCE_STATE.online.equals(this.xmszPilePresenceStatus)) {
                        handlePileOffline();
                        return;
                    }
                    return;
                }
                Log.w("XMSZProtocolAgent.handleSendRequestFail", "failed to heart beat request: " + request.toJson());
                return;
            default:
                return;
        }
    }

    private void handleRequestTimeout(XMSZMessage request) {
        String port = request.getPort();
        if (!TextUtils.isEmpty(port)) {
            XMSZPortHandler portHandler = getPortHandler(port);
            portHandler.sendMessage(portHandler.obtainMessage(73733, request));
            return;
        }
        byte functionCode = request.getHead().getFunctionCode();
        switch (functionCode) {
            case 17:
                if (request.getRetrySend() < 2) {
                    Log.w("XMSZProtocolAgent.handleRequestTimeout", "resend boot notify after 5 seconds !!!");
                    updateRequestForResend(request);
                    this.handler.sendMessageDelayed(this.handler.obtainMessage(69637, request), 5000L);
                    return;
                }
                Log.w("XMSZProtocolAgent.handleRequestTimeout", "failed to boot notify 3 times, diconnect !!!");
                getInstance().disconnect();
                return;
            case 21:
                this.failHeartBeatCnt++;
                if (this.failHeartBeatCnt >= 3) {
                    Log.w("XMSZProtocolAgent.handleRequestTimeout", "failed to heart beat 3 times, consider be offline !!!");
                    this.failHeartBeatCnt = 0;
                    if (XMSZ_PILE_PRESENCE_STATE.online.equals(this.xmszPilePresenceStatus)) {
                        handlePileOffline();
                        return;
                    }
                    return;
                }
                Log.w("XMSZProtocolAgent.handleRequestTimeout", "failed to heart beat request: " + request.toJson());
                return;
            default:
                return;
        }
    }

    public XMSZHead createRequestHead(byte functionCode) {
        XMSZHead head = new XMSZHead();
        head.setFunctionCode(functionCode);
        head.setPacketID(getRequestSequence());
        head.setSource(this.configCache.getPoint_id());
        head.setDest(0L);
        return head;
    }

    public XMSZHead createResponseHead(XMSZHead requestHead) {
        XMSZHead head = new XMSZHead();
        head.setFunctionCode((byte) (requestHead.getFunctionCode() | AnyoMessage.CMD_RESET_CHARGE));
        head.setPacketID(requestHead.getPacketID());
        head.setSource(this.configCache.getPoint_id());
        head.setDest(0L);
        return head;
    }

    public boolean sendRemoteStartChargingResponse(RemoteStartChargingRequest request, byte statusCode) {
        String port = request.getPort();
        XMSZPortHandler portHandler = getPortHandler(port);
        if (portHandler == null) {
            Log.w("XMSZProtocolAgent.sendRemoteStartChargingResponse", "no available port handler for port: " + port);
            return false;
        }
        return portHandler.responseRemoteStartCharging(request, statusCode);
    }

    public boolean sendRemoteStopChargingResponse(RemoteStopChargingRequest request, byte statusCode) {
        String port = request.getPort();
        XMSZPortHandler portHandler = getPortHandler(port);
        if (portHandler == null) {
            Log.w("XMSZProtocolAgent.sendRemoteStopChargingResponse", "no available port handler for port: " + port);
            return false;
        }
        return portHandler.responseRemoteStopCharging(request, statusCode);
    }

    public void handleUpdateQrcodeRequest(String port) {
        String qrcodeContent = getQrcodeContent(port);
        ChargeStatusCacheProvider.getInstance().updatePortQrcodeContent(port, qrcodeContent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isRequestMessage(byte functionCode) {
        int tmp = functionCode & AnyoMessage.CMD_RESET_CHARGE;
        return tmp == 0;
    }

    private void updateRequestForResend(XMSZMessage request) {
        Log.w("XMSZProtocolAgent.updateRequestForResend", "request: " + request.toJson());
        try {
            XMSZHead head = request.getHead();
            head.setPacketID(getRequestSequence());
            request.setHead(request.getHead());
            request.setCrc16(request.calcCheckSum());
            request.setRetrySend(request.getRetrySend() + 1);
        } catch (Exception e) {
            Log.w("XMSZProtocolAgent.updateRequestForResend", Log.getStackTraceString(e));
        }
    }

    private synchronized XMSZPileStatus getPileStatusByPriority() {
        XMSZPileStatus pileStatus;
        pileStatus = new XMSZPileStatus();
        int idlePortCnt = 0;
        int usingPortCnt = 0;
        int reservePortCnt = 0;
        int unavailablePortCnt = 0;
        Set<String> ports = this.portHandlers.keySet();
        int portNum = ports.size();
        Iterator<String> it2 = ports.iterator();
        while (true) {
            if (!it2.hasNext()) {
                break;
            }
            String port = it2.next();
            XMSZPortHandler xmszPortHandler = this.portHandlers.get(port);
            XMSZPortStatus portStatus = xmszPortHandler.getLatestPortStatus();
            if (portStatus.getPortStatus() == 2) {
                pileStatus.setPileStatus((byte) 2);
                pileStatus.setPileError(portStatus.getPortError());
                break;
            } else if (portStatus.getPortStatus() != 0) {
                idlePortCnt++;
            } else if (portStatus.getPortStatus() != 1) {
                usingPortCnt++;
            } else if (portStatus.getPortStatus() != 4) {
                reservePortCnt++;
            } else if (portStatus.getPortStatus() != 3) {
                unavailablePortCnt++;
            }
        }
        if (pileStatus.getPileStatus() != 2) {
            if (idlePortCnt >= 1) {
                pileStatus.setPileStatus((byte) 0);
            } else if (usingPortCnt == portNum) {
                pileStatus.setPileStatus((byte) 1);
            } else if (reservePortCnt == portNum) {
                pileStatus.setPileStatus((byte) 4);
            } else if (unavailablePortCnt == portNum) {
                pileStatus.setPileStatus((byte) 3);
            }
            pileStatus.setPileError((byte) 4);
        }
        return pileStatus;
    }

    private synchronized boolean updatePileStatusByPriority(XMSZPileStatus newPileStatus) {
        boolean z = true;
        synchronized (this) {
            byte newPileStatusCode = newPileStatus.getPileStatus();
            byte latestPileStatusCode = this.latestPileStatus.getPileStatus();
            if (newPileStatusCode != latestPileStatusCode) {
                if (latestPileStatusCode == 0) {
                    this.latestPileStatus = newPileStatus;
                } else if (latestPileStatusCode == 1) {
                    if (newPileStatusCode != 3 && newPileStatusCode != 4) {
                        this.latestPileStatus = newPileStatus;
                    }
                    z = false;
                } else if (latestPileStatusCode == 4) {
                    if (newPileStatusCode != 3) {
                        this.latestPileStatus = newPileStatus;
                    }
                    z = false;
                } else {
                    if (latestPileStatusCode == 3) {
                        this.latestPileStatus = newPileStatus;
                    }
                    z = false;
                }
            } else {
                if (newPileStatusCode == 2 && newPileStatus.getPileError() != this.latestPileStatus.getPileError()) {
                    this.latestPileStatus = newPileStatus;
                }
                z = false;
            }
        }
        return z;
    }
}
