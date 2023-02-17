package com.xcharge.charger.protocol.anyo.handler;

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
import com.google.zxing.aztec.encoder.Encoder;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.UpgradeProgress;
import com.xcharge.charger.data.bean.device.MobileNet;
import com.xcharge.charger.data.bean.device.Network;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SoftwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.NetworkStatusObserver;
import com.xcharge.charger.protocol.anyo.R;
import com.xcharge.charger.protocol.anyo.bean.AnyoConfig;
import com.xcharge.charger.protocol.anyo.bean.AnyoHead;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.anyo.bean.UserConfig;
import com.xcharge.charger.protocol.anyo.bean.request.RebootRequest;
import com.xcharge.charger.protocol.anyo.bean.request.ReportNetworkInfoRequest;
import com.xcharge.charger.protocol.anyo.bean.request.ResetChargeRequest;
import com.xcharge.charger.protocol.anyo.bean.request.StartChargeRequest;
import com.xcharge.charger.protocol.anyo.bean.request.StartUpgradeRequest;
import com.xcharge.charger.protocol.anyo.bean.request.StopChargeRequest;
import com.xcharge.charger.protocol.anyo.bean.request.TimeSyncRequest;
import com.xcharge.charger.protocol.anyo.bean.request.UnlockPortRequest;
import com.xcharge.charger.protocol.anyo.bean.request.UpgradeDownloadCompleteRequest;
import com.xcharge.charger.protocol.anyo.bean.response.QueryDeviceFaultResponse;
import com.xcharge.charger.protocol.anyo.bean.response.RebootResponse;
import com.xcharge.charger.protocol.anyo.bean.response.StartUpgradeResponse;
import com.xcharge.charger.protocol.anyo.bean.response.TimeSyncResponse;
import com.xcharge.charger.protocol.anyo.router.AnyoDCAPGateway;
import com.xcharge.charger.protocol.anyo.session.AnyoChargeSession;
import com.xcharge.charger.protocol.anyo.session.AnyoRequestSession;
import com.xcharge.charger.protocol.anyo.session.AnyoUpgradeSession;
import com.xcharge.charger.protocol.anyo.type.ANYO_REQUEST_STATE;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.common.utils.ContextUtils;
import com.xcharge.common.utils.FileUtils;
import com.xcharge.common.utils.HandlerTimer;
import com.xcharge.common.utils.HttpDownloadManager;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.TimeUtils;
import java.io.File;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
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
public class AnyoProtocolAgent extends IoHandlerAdapter {
    public static final int MSG_CONNECTED = 69634;
    public static final int MSG_CONNECT_ERROR = 69636;
    public static final int MSG_DISCONNECTED = 69635;
    public static final int MSG_INIT_CONNECTION = 69633;
    public static final int MSG_RECEIVED = 69639;
    public static final int MSG_SECOND_TIMER = 69640;
    public static final int MSG_SEND = 69637;
    public static final int MSG_SENDED = 69638;
    public static final int TIMEOUT_CONNECT = 10;
    public static final int TIMEOUT_RESPONSE = 10;
    public static final int TIMEOUT_SEND = 5;
    public static final int TIMEOUT_WAIT_PLUGIN = 60;
    public static final int TIMEOUT_WAIT_PLUGOUT = -1;
    public static final int TIMEOUT_WAIT_START_CHARGE = -1;
    private static AnyoProtocolAgent instance = null;
    public static String ANYO_CLOUD_HOST = null;
    public static int ANYO_CLOUD_PORT = 0;
    public static String SettedQrcode = null;
    public static byte provider = 0;
    public static int magicNumber = 0;
    public static String protocolVersion = null;
    public static String softwareVersion = null;
    public static String firewareType = null;
    private byte pileType = 0;
    private String pileNo = null;
    private HandlerThread thread = null;
    private MsgHandler handler = null;
    private HandlerTimer handlerTimer = null;
    private Context context = null;
    private ThreadPoolExecutor connectThreadPoolExecutor = null;
    private IoSession session = null;
    private AtomicInteger requestSeq = null;
    private byte checkSumRand = 0;
    private byte peerCheckSumRand = 0;
    private AnyoUpgradeSession upgradeSession = null;
    private HashMap<String, AnyoPortHandler> portHandlers = null;
    private HashMap<String, SendRequestState> sendReqestState = null;
    private NetworkStatusObserver networkStatusObserver = null;

    public static AnyoProtocolAgent getInstance() {
        if (instance == null) {
            instance = new AnyoProtocolAgent();
        }
        return instance;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class SendRequestState {
        AnyoMessage request;
        ANYO_REQUEST_STATE status;
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
                        AnyoProtocolAgent.this.connect();
                        break;
                    case 69634:
                        Log.i("AnyoProtocolAgent.handleMessage", "connected !!!");
                        LogUtils.cloudlog("anyo cloud connected !!!");
                        ChargeStatusCacheProvider.getInstance().updateCloudConnected(true);
                        AnyoProtocolAgent.this.checkSumRand = (byte) (new Random().nextInt() & MotionEventCompat.ACTION_MASK);
                        AnyoProtocolAgent.this.handlerTimer.startTimer(1000L, 69640, null);
                        AnyoProtocolAgent.this.portsLogin();
                        break;
                    case 69635:
                        Log.i("AnyoProtocolAgent.handleMessage", "disconnected !!!");
                        LogUtils.cloudlog("anyo cloud disconnected !!!");
                        ChargeStatusCacheProvider.getInstance().updateCloudConnected(false);
                        AnyoProtocolAgent.this.handlerTimer.stopTimer(69640);
                        AnyoProtocolAgent.this.portsLogout();
                        AnyoProtocolAgent.this.handler.sendEmptyMessageDelayed(69633, 5000L);
                        break;
                    case 69636:
                        Log.i("AnyoProtocolAgent.handleMessage", "failed to connect !!!");
                        if (HardwareStatusCacheProvider.getInstance().isNetworkConnected()) {
                            LogUtils.applog("failed to connect to anyo cloud, try to diagnosis network connectivity ...");
                            new Thread(new Runnable() { // from class: com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.MsgHandler.1
                                @Override // java.lang.Runnable
                                public void run() {
                                    DCAPProxy.getInstance().networkConnectivityDiagnosis();
                                }
                            }).start();
                        }
                        AnyoProtocolAgent.this.handler.sendEmptyMessageDelayed(69633, 20000L);
                        break;
                    case 69637:
                        AnyoMessage anyoMessage = (AnyoMessage) msg.obj;
                        if (AnyoProtocolAgent.this.isConnected()) {
                            AnyoProtocolAgent.this.session.write(anyoMessage);
                            if (anyoMessage.getHead().getStartCode() == 104) {
                                SendRequestState reqestState = new SendRequestState(null);
                                reqestState.request = anyoMessage;
                                reqestState.status = ANYO_REQUEST_STATE.sending;
                                reqestState.timestamp = System.currentTimeMillis();
                                AnyoProtocolAgent.this.sendReqestState.put(String.valueOf((int) anyoMessage.getHead().getSeq()), reqestState);
                                break;
                            }
                        } else if (anyoMessage.getHead().getStartCode() == 104) {
                            AnyoProtocolAgent.this.handleSendRequestFail(anyoMessage);
                            break;
                        }
                        break;
                    case 69638:
                        AnyoMessage anyoMessage2 = (AnyoMessage) msg.obj;
                        Log.d("AnyoProtocolAgent.handleMessage", "succeed to send anyo msg: " + anyoMessage2.toJson());
                        if (anyoMessage2.getHead().getStartCode() == 104) {
                            String requestSeq = String.valueOf((int) anyoMessage2.getHead().getSeq());
                            SendRequestState reqestState2 = (SendRequestState) AnyoProtocolAgent.this.sendReqestState.get(requestSeq);
                            if (reqestState2 != null) {
                                reqestState2.status = ANYO_REQUEST_STATE.sended;
                                reqestState2.timestamp = System.currentTimeMillis();
                                AnyoProtocolAgent.this.handleSendRequestOk(reqestState2.request);
                                break;
                            } else {
                                Log.w("AnyoProtocolAgent.handleMessage", "maybe timeout to send anyo request msg: " + anyoMessage2.toJson());
                                break;
                            }
                        }
                        break;
                    case 69639:
                        AnyoMessage anyoMessage3 = (AnyoMessage) msg.obj;
                        Log.d("AnyoProtocolAgent.handleMessage", "received anyo msg: " + anyoMessage3.toJson());
                        if (anyoMessage3.getHead().getCmdCode() != 16 && !anyoMessage3.verifyCheckSum(AnyoProtocolAgent.this.getPeerCheckSumRand())) {
                            Log.w("AnyoProtocolAgent.handleMessage", "checksum error, peer rand: " + ((int) AnyoProtocolAgent.this.getPeerCheckSumRand()));
                            return;
                        } else if (anyoMessage3.getHead().getStartCode() == -86) {
                            String responseSeq = String.valueOf((int) anyoMessage3.getHead().getSeq());
                            SendRequestState reqestState3 = (SendRequestState) AnyoProtocolAgent.this.sendReqestState.get(responseSeq);
                            if (reqestState3 != null) {
                                AnyoMessage request = reqestState3.request;
                                AnyoProtocolAgent.this.sendReqestState.remove(responseSeq);
                                anyoMessage3.setPort(request.getPort());
                                AnyoProtocolAgent.this.dispatchAnyoMessage(anyoMessage3, request);
                                break;
                            } else {
                                Log.w("AnyoProtocolAgent.handleMessage", "maybe timeout to wait for response msg: " + anyoMessage3.toJson());
                                break;
                            }
                        } else if (anyoMessage3.getHead().getStartCode() == 104) {
                            AnyoProtocolAgent.this.dispatchAnyoMessage(anyoMessage3, null);
                            break;
                        }
                        break;
                    case 69640:
                        AnyoProtocolAgent.this.requestTimeoutCheck();
                        AnyoProtocolAgent.this.handlerTimer.startTimer(1000L, 69640, null);
                        break;
                    case 135169:
                        Uri uri = (Uri) msg.obj;
                        AnyoProtocolAgent.this.handleNetworkStatusChanged(uri);
                        break;
                }
            } catch (Exception e) {
                Log.e("AnyoProtocolAgent.handleMessage", "except: " + Log.getStackTraceString(e));
                LogUtils.syslog("AnyoProtocolAgent handleMessage exception: " + Log.getStackTraceString(e));
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
                String anyoPort = getAnyoPort(port);
                AnyoPortHandler portHandler = new AnyoPortHandler();
                portHandler.init(context, anyoPort, this);
                this.portHandlers.put(anyoPort, portHandler);
            }
        }
        this.connectThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue(1), new RejectedExecutionHandler() { // from class: com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.1
            @Override // java.util.concurrent.RejectedExecutionHandler
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                Log.e("AnyoProtocolAgent.ThreadPoolExecutor.rejectedExecution", "rejected runnable: " + r.toString() + ", active runnables: " + executor.getActiveCount());
            }
        });
        this.sendReqestState = new HashMap<>();
        this.thread = new HandlerThread("AnyoProtocolAgent", 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(context);
        this.networkStatusObserver = new NetworkStatusObserver(context, this.handler);
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor(Network.class.getSimpleName()), true, this.networkStatusObserver);
        AnyoConfig anyoConfig = loadConfig();
        ANYO_CLOUD_HOST = anyoConfig.getCloudHost();
        ANYO_CLOUD_PORT = anyoConfig.getCloudPort();
        provider = (byte) (anyoConfig.getProvider() & MotionEventCompat.ACTION_MASK);
        magicNumber = (int) (anyoConfig.getMagicNumber() & (-1));
        SettedQrcode = anyoConfig.getQrcode();
        protocolVersion = anyoConfig.getProtocolVersion();
        softwareVersion = anyoConfig.getSoftwareVersion();
        SoftwareStatusCacheProvider.getInstance().updateAppVer(softwareVersion);
        String sn = HardwareStatusCacheProvider.getInstance().getSn();
        firewareType = sn.substring(0, 4);
        HashMap<String, String> platformData = SystemSettingCacheProvider.getInstance().getPlatformCustomizedData();
        this.pileType = (byte) (Integer.parseInt(platformData.get("type")) & MotionEventCompat.ACTION_MASK);
        this.pileNo = platformData.get("id");
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
        this.thread.quit();
        for (AnyoPortHandler portHandler : this.portHandlers.values()) {
            portHandler.destroy();
        }
        this.portHandlers.clear();
        this.sendReqestState.clear();
        this.connectThreadPoolExecutor.shutdown();
    }

    private AnyoConfig loadConfig() {
        UserConfig ucfg;
        String cfg = ContextUtils.getRawFileToString(this.context, R.raw.anyo_cfg);
        AnyoConfig config = null;
        if (!TextUtils.isEmpty(cfg)) {
            AnyoConfig config2 = new AnyoConfig().fromJson(cfg);
            config = config2;
            String sn = HardwareStatusCacheProvider.getInstance().getSn();
            if ("C2011601CNZQMUWJ".equals(sn)) {
                config.setCloudHost("192.168.1.100");
                config.setCloudPort(8003);
            }
        }
        String userCfg = ContextUtils.readFileData("anyo_cfg.json", this.context);
        if (!TextUtils.isEmpty(userCfg) && (ucfg = new UserConfig().fromJson(userCfg)) != null) {
            if (config == null) {
                config = new AnyoConfig();
            }
            if (!TextUtils.isEmpty(ucfg.getCloudHost())) {
                config.setCloudHost(ucfg.getCloudHost());
            }
            if (ucfg.getCloudPort() != null) {
                config.setCloudPort(ucfg.getCloudPort().intValue());
            }
            if (ucfg.getProvider() != null) {
                config.setProvider(ucfg.getProvider().intValue());
            }
            if (ucfg.getMagicNumber() != null) {
                config.setMagicNumber(ucfg.getMagicNumber().longValue());
            }
            if (!TextUtils.isEmpty(ucfg.getQrcode())) {
                config.setQrcode(ucfg.getQrcode());
            }
        }
        if (config == null) {
            config = new AnyoConfig();
        }
        Log.d("AnyoProtocolAgent.loadConfig", "config: " + config.toJson());
        LogUtils.applog("use anyo config: " + config.toJson());
        return config;
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
            connector.setConnectTimeoutMillis(10000L);
            connector.getFilterChain().addLast("AnyoProtocol", this.protocolCodecFilter);
            connector.setHandler(this.ioHandler);
            connector.getSessionConfig().setReadBufferSize(2048);
            connector.getSessionConfig().setWriteTimeout(5);
            try {
                ConnectFuture future = connector.connect(new InetSocketAddress(AnyoProtocolAgent.ANYO_CLOUD_HOST, AnyoProtocolAgent.ANYO_CLOUD_PORT));
                future.awaitUninterruptibly();
                AnyoProtocolAgent.this.session = future.getSession();
                AnyoProtocolAgent.this.handler.sendEmptyMessage(69634);
                AnyoProtocolAgent.this.session.getCloseFuture().awaitUninterruptibly();
                connector.dispose();
                AnyoProtocolAgent.this.handler.sendEmptyMessage(69635);
            } catch (Exception e) {
                Log.w("AnyoProtocolAgent.ConnectTask", "connect to " + AnyoProtocolAgent.ANYO_CLOUD_HOST + ":" + AnyoProtocolAgent.ANYO_CLOUD_PORT + " exception: " + Log.getStackTraceString(e));
                AnyoProtocolAgent.this.handler.sendEmptyMessage(69636);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void connect() {
        if (HardwareStatusCacheProvider.getInstance().isNetworkConnected()) {
            Log.i("AnyoProtocolAgent.connect", "init connection !!!");
            ProtocolCodecFilter protocolCodecFilter = new ProtocolCodecFilter(new AnyoMessageEncoder(), new AnyoMessageDecoder());
            this.connectThreadPoolExecutor.execute(new ConnectTask(protocolCodecFilter, this));
            return;
        }
        this.handler.sendEmptyMessageDelayed(69633, 5000L);
    }

    public void disconnect() {
        if (this.session != null) {
            Log.d("AnyoProtocolAgent.disconnect", "force to disconnect !!!");
            this.session.closeNow().awaitUninterruptibly(10000L);
        }
    }

    @Override // org.apache.mina.core.service.IoHandlerAdapter, org.apache.mina.core.service.IoHandler
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        Log.w("AnyoProtocolAgent.exceptionCaught", "exception: " + Log.getStackTraceString(cause));
        super.exceptionCaught(session, cause);
        disconnect();
    }

    @Override // org.apache.mina.core.service.IoHandlerAdapter, org.apache.mina.core.service.IoHandler
    public void inputClosed(IoSession session) throws Exception {
        Log.w("AnyoProtocolAgent.inputClosed", "session input closed: " + session.getId());
        super.inputClosed(session);
        LogUtils.cloudlog("anyo cloud connection has been closed by remote !!!");
        disconnect();
    }

    @Override // org.apache.mina.core.service.IoHandlerAdapter, org.apache.mina.core.service.IoHandler
    public void messageReceived(IoSession session, Object message) throws Exception {
        super.messageReceived(session, message);
        AnyoMessage msg = (AnyoMessage) message;
        this.handler.sendMessage(this.handler.obtainMessage(69639, msg));
    }

    @Override // org.apache.mina.core.service.IoHandlerAdapter, org.apache.mina.core.service.IoHandler
    public void messageSent(IoSession session, Object message) throws Exception {
        super.messageSent(session, message);
        AnyoMessage msg = (AnyoMessage) message;
        this.handler.sendMessage(this.handler.obtainMessage(69638, msg));
    }

    @Override // org.apache.mina.core.service.IoHandlerAdapter, org.apache.mina.core.service.IoHandler
    public void sessionClosed(IoSession session) throws Exception {
        super.sessionClosed(session);
        Log.d("AnyoProtocolAgent.sessionClosed", "session closed: " + session.getId());
    }

    @Override // org.apache.mina.core.service.IoHandlerAdapter, org.apache.mina.core.service.IoHandler
    public void sessionCreated(IoSession session) throws Exception {
        super.sessionCreated(session);
        Log.d("AnyoProtocolAgent.sessionCreated", "session created: " + session.getId());
    }

    @Override // org.apache.mina.core.service.IoHandlerAdapter, org.apache.mina.core.service.IoHandler
    public void sessionOpened(IoSession session) throws Exception {
        super.sessionOpened(session);
        Log.d("AnyoProtocolAgent.sessionOpened", "session opend: " + session.getId());
    }

    @Override // org.apache.mina.core.service.IoHandlerAdapter, org.apache.mina.core.service.IoHandler
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        super.sessionIdle(session, status);
        Log.d("AnyoProtocolAgent.sessionIdle", "session idle: " + session.getIdleCount(status));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleNetworkStatusChanged(Uri uri) {
        Log.i("AnyoProtocolAgent.handleNetworkStatusChanged", "network status changed, uri: " + uri.toString());
        String lastSegment = uri.getLastPathSegment();
        if (!"connected".equals(lastSegment) && "disconnected".equals(lastSegment)) {
            disconnect();
        }
    }

    public String getAnyoPort(String localPort) {
        return String.valueOf(Integer.parseInt(localPort) + 9);
    }

    public String getLocalPort(String anyoPort) {
        return String.valueOf(Integer.parseInt(anyoPort) - 9);
    }

    public AnyoChargeSession getChargeSession(String anyoPort) {
        AnyoPortHandler anyoPortHandler = getPortHandler(anyoPort);
        if (anyoPortHandler == null) {
            Log.w("AnyoProtocolAgent.getChargeSession", "no available port handler for port: " + anyoPort);
            return null;
        }
        return anyoPortHandler.getChargeSession();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchAnyoMessage(AnyoMessage msg, AnyoMessage sendedRequest) {
        byte startCode = msg.getHead().getStartCode();
        if (startCode == 104) {
            handleRequestMessage(msg);
        } else if (startCode == -86) {
            handleResponseMessage(msg, sendedRequest);
        } else {
            Log.w("AnyoProtocolAgent.dispatchAnyoMessage", "unsupported message start code: " + ((int) startCode));
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    private void handleRequestMessage(AnyoMessage request) {
        byte cmd = request.getHead().getCmdCode();
        switch (cmd) {
            case Byte.MIN_VALUE:
                ResetChargeRequest resetChargeRequest = (ResetChargeRequest) request;
                byte anyoPort = resetChargeRequest.getPortNo();
                if (anyoPort == 0) {
                    anyoPort = 10;
                }
                String port = String.valueOf((int) anyoPort);
                resetChargeRequest.setPort(port);
                AnyoPortHandler portHandler = getPortHandler(port);
                if (portHandler != null) {
                    portHandler.sendMessage(portHandler.obtainMessage(73735, resetChargeRequest));
                    return;
                } else {
                    Log.w("AnyoProtocolAgent.handleRequestMessage", "unsupported port no param in reset charge request: " + resetChargeRequest.toJson());
                    return;
                }
            case 48:
                TimeSyncRequest timeSyncRequest = (TimeSyncRequest) request;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                long peerTimeStamp = timeSyncRequest.getTimestamp() * 1000;
                Log.i("AnyoProtocolAgent.handleRequestMessage", "peer timestamp: " + TimeUtils.getISO8601Format(peerTimeStamp, "+00:00") + ", local timestamp: " + sdf.format(new Date(System.currentTimeMillis())));
                SystemClock.setCurrentTimeMillis(peerTimeStamp);
                LogUtils.syslog("synch cloud time: " + TimeUtils.getISO8601Format(peerTimeStamp, "+00:00"));
                Log.i("AnyoProtocolAgent.handleRequestMessage", "cloud time setted, now local timestamp: " + sdf.format(new Date(System.currentTimeMillis())));
                timeSyncResponse(timeSyncRequest);
                return;
            case 49:
                break;
            case 60:
                StartChargeRequest startChargeRequest = (StartChargeRequest) request;
                byte anyoPort2 = startChargeRequest.getPortNo();
                if (anyoPort2 == 0) {
                    anyoPort2 = 10;
                }
                String port2 = String.valueOf((int) anyoPort2);
                startChargeRequest.setPort(port2);
                AnyoPortHandler portHandler2 = getPortHandler(port2);
                if (portHandler2 != null) {
                    portHandler2.sendMessage(portHandler2.obtainMessage(73735, startChargeRequest));
                    return;
                } else {
                    Log.w("AnyoProtocolAgent.handleRequestMessage", "unsupported port no param in start charge request: " + startChargeRequest.toJson());
                    return;
                }
            case 61:
                StopChargeRequest stopChargeRequest = (StopChargeRequest) request;
                byte anyoPort3 = stopChargeRequest.getPortNo();
                if (anyoPort3 == 0) {
                    anyoPort3 = 10;
                }
                String port3 = String.valueOf((int) anyoPort3);
                stopChargeRequest.setPort(port3);
                AnyoPortHandler portHandler3 = getPortHandler(port3);
                if (portHandler3 != null) {
                    portHandler3.sendMessage(portHandler3.obtainMessage(73735, stopChargeRequest));
                    return;
                } else {
                    Log.w("AnyoProtocolAgent.handleRequestMessage", "unsupported port no param in stop charge request: " + stopChargeRequest.toJson());
                    return;
                }
            case 62:
                handleStartUpgradeRequest(request);
                return;
            case 66:
                handleQueryDeviceFaultRequest(request);
                break;
            case 81:
                UnlockPortRequest unlockPortRequest = (UnlockPortRequest) request;
                byte anyoPort4 = unlockPortRequest.getPortNo();
                if (anyoPort4 == 0) {
                    anyoPort4 = 10;
                }
                String port4 = String.valueOf((int) anyoPort4);
                unlockPortRequest.setPort(port4);
                AnyoPortHandler portHandler4 = getPortHandler(port4);
                if (portHandler4 != null) {
                    portHandler4.sendMessage(portHandler4.obtainMessage(73735, unlockPortRequest));
                    return;
                } else {
                    Log.w("AnyoProtocolAgent.handleRequestMessage", "unsupported port in unlock port request: " + unlockPortRequest.toJson());
                    return;
                }
            default:
                return;
        }
        handleRebootRequest(request);
    }

    public AnyoUpgradeSession getUpgradeSession() {
        if (this.upgradeSession == null) {
            this.upgradeSession = new AnyoUpgradeSession();
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

    private boolean handleQueryDeviceFaultRequest(AnyoMessage request) {
        short acDeviceFaultStatus;
        try {
            Port portStatus = HardwareStatusCacheProvider.getInstance().getPort("1");
            ErrorCode error = portStatus.getDeviceError();
            Port errorPortStatus = null;
            HashMap<String, Object> errData = error.getData();
            if (errData != null && errData.size() > 0) {
                String errorPortStatusJson = (String) errData.get("portStatus");
                if (!TextUtils.isEmpty(errorPortStatusJson)) {
                    errorPortStatus = new Port().fromJson(errorPortStatusJson);
                }
            }
            switch (error.getCode()) {
                case 200:
                    acDeviceFaultStatus = 0;
                    break;
                case ErrorCode.EC_DEVICE_NO_GROUND /* 30011 */:
                    acDeviceFaultStatus = 32;
                    break;
                case ErrorCode.EC_DEVICE_EMERGENCY_STOP /* 30013 */:
                    acDeviceFaultStatus = 256;
                    break;
                case ErrorCode.EC_DEVICE_VOLT_ERROR /* 30014 */:
                    if (errorPortStatus != null) {
                        ArrayList<Double> volts = errorPortStatus.getVolts();
                        if (volts != null && volts.size() > 0) {
                            Double volt = volts.get(0);
                            if (volt != null) {
                                if (volt.doubleValue() > 220.0d) {
                                    acDeviceFaultStatus = 8;
                                    break;
                                } else {
                                    acDeviceFaultStatus = 16;
                                    break;
                                }
                            } else {
                                acDeviceFaultStatus = 8;
                                break;
                            }
                        } else {
                            acDeviceFaultStatus = 8;
                            break;
                        }
                    } else {
                        acDeviceFaultStatus = 8;
                        break;
                    }
                    break;
                case ErrorCode.EC_DEVICE_POWER_LEAK /* 30017 */:
                    acDeviceFaultStatus = 4;
                    break;
                case ErrorCode.EC_DEVICE_COMM_ERROR /* 30018 */:
                    acDeviceFaultStatus = 128;
                    break;
                default:
                    Log.w("AnyoProtocolAgent.handleQueryDeviceFaultRequest", "unsupported ac device error: " + error.toJson());
                    return false;
            }
            QueryDeviceFaultResponse response = new QueryDeviceFaultResponse();
            response.setAcDeviceFault(acDeviceFaultStatus);
            AnyoHead head = getInstance().createResponseHead(request.getHead());
            head.setStatusCode((byte) 0);
            head.setBodyLength(response.bodyToBytes().length);
            response.setHead(head);
            head.setCheckSum(response.calcCheckSum(getInstance().getCheckSumRand()));
            sendMessage(response);
            Log.i("AnyoPortHandler.handleQueryDeviceFaultRequest", "send QueryDeviceFault response: " + response.toJson());
            return true;
        } catch (Exception e) {
            Log.w("AnyoPortHandler.handleQueryDeviceFaultRequest", Log.getStackTraceString(e));
            return false;
        }
    }

    private void handleRebootRequest(AnyoMessage request) {
        try {
            RebootRequest rebootRequest = (RebootRequest) request;
            Log.i("AnyoProtocolAgent.handleRebootRequest", "receive reboot request: " + rebootRequest.toJson());
            LogUtils.cloudlog("receive reboot request: " + rebootRequest.toJson());
            AnyoDCAPGateway.getInstance().sendMessage(AnyoDCAPGateway.getInstance().obtainMessage(77827, request));
            RebootResponse response = new RebootResponse();
            response.setTs(System.currentTimeMillis() / 1000);
            AnyoHead head = getInstance().createResponseHead(request.getHead());
            head.setStatusCode((byte) 0);
            head.setBodyLength(response.bodyToBytes().length);
            response.setHead(head);
            head.setCheckSum(response.calcCheckSum(getCheckSumRand()));
            sendMessage(response);
            Log.i("AnyoPortHandler.handleRebootRequest", "send reboot response: " + response.toJson());
        } catch (Exception e) {
            Log.w("AnyoPortHandler.handleRebootRequest", Log.getStackTraceString(e));
        }
    }

    private void handleStartUpgradeRequest(final AnyoMessage request) {
        StartUpgradeRequest startUpgradeRequest = (StartUpgradeRequest) request;
        Log.i("AnyoProtocolAgent.handleStartUpgradeRequest", "receive start upgrade request: " + startUpgradeRequest.toJson());
        byte clientId = startUpgradeRequest.getProvider();
        if (clientId != provider) {
            Log.w("AnyoProtocolAgent.handleStartUpgradeRequest", "not matched client id !!! this pile: " + ((int) provider) + ", in request: " + ((int) clientId));
            startUpgradeResponse(startUpgradeRequest, (byte) 19);
            return;
        }
        byte upgradeType = startUpgradeRequest.getUpgradeType();
        if (upgradeType != 3) {
            Log.w("AnyoProtocolAgent.handleStartUpgradeRequest", "now only support HTTP upgrade !!! type: " + ((int) upgradeType));
            startUpgradeResponse(startUpgradeRequest, (byte) 19);
            return;
        }
        AnyoUpgradeSession upgradeSession = getUpgradeSession();
        if (TextUtils.isEmpty(upgradeSession.getStage())) {
            startUpgradeResponse(startUpgradeRequest, (byte) 0);
            upgradeSession.setStage(UpgradeProgress.STAGE_DOWNLOAD);
            upgradeSession.setRequestUpgrade(startUpgradeRequest);
            String url = startUpgradeRequest.getUpgradeAddr();
            upgradeSession.setDownloadFile("/data/data/com.xcharge.charger/download/upgrade/update.dat");
            Log.i("AnyoProtocolAgent.handleStartUpgradeRequest", "start download ..., url: " + url);
            downloadProgress(200, 1, 0);
            HttpDownloadManager.getInstance().downloadFile(this.context, url, "/data/data/com.xcharge.charger/download/upgrade/update.dat", new HttpDownloadManager.DownLoadListener() { // from class: com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.2
                @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                public void onDownLoadPercentage(long curPosition, long total) {
                }

                @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                public void onDownLoadPercentage(int p) {
                    AnyoProtocolAgent.this.downloadProgress(200, 2, p);
                }

                @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                public void onDownLoadFail() {
                    AnyoProtocolAgent.this.downloadProgress(ErrorCode.EC_UPGRADE_DOWNLOAD_FAIL, 0, 0);
                    AnyoProtocolAgent.this.reportUpgradeDownloadCompleteRequest(false);
                    AnyoProtocolAgent.this.clearUpgradeSession();
                }

                @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                public void onDownLoadComplete() {
                    if (AnyoProtocolAgent.this.verifyFileCRC32()) {
                        AnyoProtocolAgent.this.reportUpgradeDownloadCompleteRequest(true);
                        AnyoDCAPGateway.getInstance().sendMessage(AnyoDCAPGateway.getInstance().obtainMessage(77827, request));
                        return;
                    }
                    AnyoProtocolAgent.this.reportUpgradeDownloadCompleteRequest(false);
                    AnyoProtocolAgent.this.clearUpgradeSession();
                }
            });
            return;
        }
        startUpgradeResponse(startUpgradeRequest, (byte) 19);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void downloadProgress(int error, int status, int progress) {
        UpgradeProgress upgradeProgress = SoftwareStatusCacheProvider.getInstance().getUpgradeProgress();
        upgradeProgress.setStage(UpgradeProgress.STAGE_DOWNLOAD);
        upgradeProgress.setUpgradeData(null);
        upgradeProgress.setError(new ErrorCode(error));
        upgradeProgress.setStatus(status);
        upgradeProgress.setProgress(progress);
        SoftwareStatusCacheProvider.getInstance().updateUpgradeProgress(upgradeProgress);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean verifyFileCRC32() {
        AnyoUpgradeSession upgradeSession = getUpgradeSession();
        StartUpgradeRequest startUpgradeRequest = upgradeSession.getRequestUpgrade();
        long crc32 = 0;
        long sourceCRC32 = startUpgradeRequest.getChecksum() & XMSZHead.ID_BROADCAST;
        boolean isOk = false;
        String downloadFile = upgradeSession.getDownloadFile();
        try {
            Log.i("AnyoProtocolAgent.verifyFileCRC32", "begin integrity check: " + downloadFile);
            downloadProgress(200, 3, 0);
            crc32 = FileUtils.getCRC32(new File(downloadFile)).longValue();
            if (crc32 != sourceCRC32) {
                isOk = false;
            } else {
                isOk = true;
            }
        } catch (Exception e) {
            Log.e("AnyoProtocolAgent.verifyFileCRC32", Log.getStackTraceString(e));
        }
        if (!isOk) {
            Log.w("AnyoProtocolAgent.verifyFileCRC32", "sourceCRC32: " + sourceCRC32 + ", fileCRC32: " + crc32);
            downloadProgress(ErrorCode.EC_UPGRADE_NOT_INTEGRATED, 0, 0);
        }
        return isOk;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reportUpgradeDownloadCompleteRequest(boolean isOk) {
        try {
            UpgradeDownloadCompleteRequest upgradeDownloadCompleteRequest = new UpgradeDownloadCompleteRequest();
            if (isOk) {
                upgradeDownloadCompleteRequest.setResult((byte) 0);
            } else {
                upgradeDownloadCompleteRequest.setResult((byte) 2);
            }
            AnyoHead head = createRequestHead(AnyoMessage.CMD_REPORT_UPGRADE_DOWNLOAD_COMPLETE);
            head.setBodyLength(upgradeDownloadCompleteRequest.bodyToBytes().length);
            upgradeDownloadCompleteRequest.setHead(head);
            head.setCheckSum(upgradeDownloadCompleteRequest.calcCheckSum(getCheckSumRand()));
            sendMessage(upgradeDownloadCompleteRequest);
            Log.i("AnyoProtocolAgent.reportUpgradeDownloadCompleteRequest", "send upgrade download complete request: " + upgradeDownloadCompleteRequest.toJson());
        } catch (Exception e) {
            Log.w("AnyoProtocolAgent.reportUpgradeDownloadCompleteRequest", Log.getStackTraceString(e));
        }
    }

    public void reportNetworkInfo() {
        try {
            ReportNetworkInfoRequest reportNetworkInfoRequest = new ReportNetworkInfoRequest();
            String activeNet = HardwareStatusCacheProvider.getInstance().getActiveNetwork();
            if (Network.NETWORK_TYPE_MOBILE.equals(activeNet) || "2G".equals(activeNet) || "3G".equals(activeNet) || "4G".equals(activeNet)) {
                MobileNet mNet = HardwareStatusCacheProvider.getInstance().getMobileNetStatus();
                String mobileType = mNet.getType();
                reportNetworkInfoRequest.setImei(TextUtils.isEmpty(mNet.getIMEI()) ? "" : mNet.getIMEI());
                reportNetworkInfoRequest.setImsi(TextUtils.isEmpty(mNet.getIMSI()) ? "" : mNet.getIMSI());
                reportNetworkInfoRequest.setIccid(TextUtils.isEmpty(mNet.getICCID()) ? reportNetworkInfoRequest.getImsi() : mNet.getICCID());
                if ("2G".equals(mobileType)) {
                    reportNetworkInfoRequest.setNetType((byte) 0);
                } else if ("3G".equals(mobileType)) {
                    reportNetworkInfoRequest.setNetType((byte) 1);
                } else if ("4G".equals(mobileType)) {
                    reportNetworkInfoRequest.setNetType((byte) 2);
                }
                String plmn = mNet.getPLMN();
                if (!TextUtils.isEmpty(plmn) && (plmn.length() == 5 || plmn.length() == 6)) {
                    String mcc = plmn.substring(0, 3);
                    String mnc = plmn.substring(3, plmn.length());
                    try {
                        reportNetworkInfoRequest.setMcc(Integer.parseInt(mcc));
                        reportNetworkInfoRequest.setMnc(Integer.parseInt(mnc));
                    } catch (Exception e) {
                        Log.w("AnyoProtocolAgent.reportNetworkInfo", Log.getStackTraceString(e));
                    }
                }
            } else if (Network.NETWORK_TYPE_ETHERNET.equals(activeNet)) {
                reportNetworkInfoRequest.setNetType((byte) 5);
            } else if (Network.NETWORK_TYPE_WIFI.equals(activeNet)) {
                reportNetworkInfoRequest.setNetType((byte) 4);
            } else {
                Log.w("AnyoProtocolAgent.reportNetworkInfo", "no network or unknown network type: " + activeNet);
                return;
            }
            AnyoHead head = createRequestHead(AnyoMessage.CMD_REPORT_NETWORK_INFO);
            head.setBodyLength(reportNetworkInfoRequest.bodyToBytes().length);
            reportNetworkInfoRequest.setHead(head);
            head.setCheckSum(reportNetworkInfoRequest.calcCheckSum(getCheckSumRand()));
            sendMessage(reportNetworkInfoRequest);
            Log.i("AnyoProtocolAgent.reportNetworkInfo", "report network info request: " + reportNetworkInfoRequest.toJson());
        } catch (Exception e2) {
            Log.w("AnyoProtocolAgent.reportNetworkInfo", Log.getStackTraceString(e2));
        }
    }

    private void handleResponseMessage(AnyoMessage response, AnyoMessage sendedRequest) {
        String port = response.getPort();
        if (!TextUtils.isEmpty(port)) {
            AnyoPortHandler portHandler = getPortHandler(port);
            AnyoRequestSession anyoRequestSession = new AnyoRequestSession();
            anyoRequestSession.setSendedRequest(sendedRequest);
            anyoRequestSession.setResponse(response);
            portHandler.sendMessage(portHandler.obtainMessage(73736, anyoRequestSession));
            return;
        }
        byte cmd = response.getHead().getCmdCode();
        switch (cmd) {
            case Encoder.DEFAULT_EC_PERCENT /* 33 */:
                Log.i("AnyoProtocolAgent.handleResponseMessage", "receive anyo upgrade download complete response:" + response.toJson());
                return;
            case 34:
                Log.i("AnyoProtocolAgent.handleResponseMessage", "receive anyo report network info response:" + response.toJson());
                return;
            default:
                return;
        }
    }

    private void timeSyncResponse(TimeSyncRequest request) {
        try {
            TimeSyncResponse timeSyncResponse = new TimeSyncResponse();
            timeSyncResponse.setTimestamp(System.currentTimeMillis() / 1000);
            AnyoHead head = createResponseHead(request.getHead());
            head.setBodyLength(timeSyncResponse.bodyToBytes().length);
            timeSyncResponse.setHead(head);
            head.setCheckSum(timeSyncResponse.calcCheckSum(getCheckSumRand()));
            sendMessage(timeSyncResponse);
        } catch (Exception e) {
            Log.w("AnyoProtocolAgent.timeSyncResponse", Log.getStackTraceString(e));
        }
    }

    public void startUpgradeResponse(StartUpgradeRequest request, byte statusCode) {
        try {
            StartUpgradeResponse startUpgradeResponse = new StartUpgradeResponse();
            AnyoHead head = createResponseHead(request.getHead());
            head.setStatusCode(statusCode);
            head.setBodyLength(startUpgradeResponse.bodyToBytes().length);
            startUpgradeResponse.setHead(head);
            head.setCheckSum(startUpgradeResponse.calcCheckSum(getCheckSumRand()));
            sendMessage(startUpgradeResponse);
        } catch (Exception e) {
            Log.w("AnyoProtocolAgent.startUpgradeResponse", Log.getStackTraceString(e));
        }
    }

    private AnyoPortHandler getPortHandler(String port) {
        return this.portHandlers.get(port);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void portsLogin() {
        for (AnyoPortHandler portHandler : this.portHandlers.values()) {
            portHandler.sendMessage(portHandler.obtainMessage(73729));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void portsLogout() {
        for (AnyoPortHandler portHandler : this.portHandlers.values()) {
            portHandler.sendMessage(portHandler.obtainMessage(73730));
        }
    }

    public byte getRequestSequence() {
        return (byte) (this.requestSeq.incrementAndGet() & MotionEventCompat.ACTION_MASK);
    }

    public byte getCheckSumRand() {
        return this.checkSumRand;
    }

    public byte setCheckSumRand(byte checkSumRand) {
        return this.checkSumRand;
    }

    public byte getPeerCheckSumRand() {
        return this.peerCheckSumRand;
    }

    public byte setPeerCheckSumRand(byte checkSumRand) {
        this.peerCheckSumRand = checkSumRand;
        return checkSumRand;
    }

    public void sendMessage(AnyoMessage msg) {
        this.handler.sendMessage(this.handler.obtainMessage(69637, msg));
    }

    public byte getPileType() {
        return this.pileType;
    }

    public String getPileNo() {
        return this.pileNo;
    }

    public byte getPortByPileType(String port) {
        if (this.pileType == 1 || this.pileType == 4) {
            return (byte) 0;
        }
        return (byte) (Integer.valueOf(port).intValue() & MotionEventCompat.ACTION_MASK);
    }

    public String getQrcodeContent(String anyoPort) {
        if (!TextUtils.isEmpty(SettedQrcode)) {
            return SettedQrcode;
        }
        if (this.pileType == 1 || this.pileType == 4) {
            return this.pileNo;
        }
        return String.valueOf(this.pileNo) + "-" + Integer.toHexString(Integer.parseInt(anyoPort) & MotionEventCompat.ACTION_MASK).toUpperCase();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void requestTimeoutCheck() {
        if (this.sendReqestState.size() > 0) {
            Iterator<Map.Entry<String, SendRequestState>> it2 = this.sendReqestState.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry<String, SendRequestState> entry = it2.next();
                SendRequestState requestState = entry.getValue();
                ANYO_REQUEST_STATE state = requestState.status;
                long timestamp = requestState.timestamp;
                if (ANYO_REQUEST_STATE.sending.equals(state)) {
                    if (System.currentTimeMillis() - timestamp > 5000) {
                        it2.remove();
                        handleSendRequestFail(requestState.request);
                    }
                } else if (ANYO_REQUEST_STATE.sended.equals(state) && System.currentTimeMillis() - timestamp > 10000) {
                    it2.remove();
                    handleRequestTimeout(requestState.request);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleSendRequestOk(AnyoMessage request) {
        String port = request.getPort();
        if (!TextUtils.isEmpty(port)) {
            AnyoPortHandler portHandler = getPortHandler(port);
            portHandler.sendMessage(portHandler.obtainMessage(73731, request));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleSendRequestFail(AnyoMessage request) {
        String port = request.getPort();
        if (!TextUtils.isEmpty(port)) {
            AnyoPortHandler portHandler = getPortHandler(port);
            portHandler.sendMessage(portHandler.obtainMessage(73732, request));
        }
    }

    private void handleRequestTimeout(AnyoMessage request) {
        String port = request.getPort();
        if (!TextUtils.isEmpty(port)) {
            AnyoPortHandler portHandler = getPortHandler(port);
            portHandler.sendMessage(portHandler.obtainMessage(73733, request));
        }
    }

    public AnyoHead createRequestHead(byte cmd) {
        AnyoHead head = new AnyoHead();
        head.setStartCode(AnyoMessage.START_CODE_REQUEST);
        head.setCmdCode(cmd);
        head.setSeq(getRequestSequence());
        return head;
    }

    public AnyoHead createResponseHead(AnyoHead requestHead) {
        AnyoHead head = new AnyoHead();
        head.setStartCode(AnyoMessage.START_CODE_RESPONSE);
        head.setCmdCode(requestHead.getCmdCode());
        head.setSeq(requestHead.getSeq());
        return head;
    }

    public AnyoMessage sendAuthRequest(int port, int cardType, String cardNo) {
        AnyoPortHandler anyoPortHandler = getPortHandler(String.valueOf(port));
        if (anyoPortHandler == null) {
            Log.w("AnyoProtocolAgent.sendAuthRequest", "no available port handler for port: " + port);
            return null;
        }
        return anyoPortHandler.authRequest(cardNo);
    }

    public boolean sendUnlockPortResponse(UnlockPortRequest request, byte statusCode) {
        String anyoPort = request.getPort();
        AnyoPortHandler anyoPortHandler = getPortHandler(anyoPort);
        if (anyoPortHandler == null) {
            Log.w("AnyoProtocolAgent.sendUnlockPortResponse", "no available port handler for port: " + anyoPort);
            return false;
        }
        return anyoPortHandler.unlockPortResponse(request, statusCode);
    }

    public boolean sendStartChargeResponse(StartChargeRequest request, byte statusCode) {
        String anyoPort = request.getPort();
        AnyoPortHandler anyoPortHandler = getPortHandler(anyoPort);
        if (anyoPortHandler == null) {
            Log.w("AnyoProtocolAgent.sendStartChargeResponse", "no available port handler for port: " + anyoPort);
            return false;
        }
        return anyoPortHandler.startChargeResponse(request, statusCode);
    }

    public boolean sendStopChargeResponse(StopChargeRequest request, byte statusCode) {
        String anyoPort = request.getPort();
        AnyoPortHandler anyoPortHandler = getPortHandler(anyoPort);
        if (anyoPortHandler == null) {
            Log.w("AnyoProtocolAgent.sendStopChargeResponse", "no available port handler for port: " + anyoPort);
            return false;
        }
        return anyoPortHandler.stopChargeResponse(request, statusCode);
    }

    public boolean sendResetChargeResponse(ResetChargeRequest request, byte statusCode) {
        String anyoPort = request.getPort();
        AnyoPortHandler anyoPortHandler = getPortHandler(anyoPort);
        if (anyoPortHandler == null) {
            Log.w("AnyoProtocolAgent.sendStopChargeResponse", "no available port handler for port: " + anyoPort);
            return false;
        }
        return anyoPortHandler.resetChargeResponse(request, statusCode);
    }

    public void handleUpdateQrcodeRequest(String localPort) {
        String anyoPort = getAnyoPort(localPort);
        String qrcodeContent = getQrcodeContent(anyoPort);
        ChargeStatusCacheProvider.getInstance().updatePortQrcodeContent(localPort, qrcodeContent);
    }
}