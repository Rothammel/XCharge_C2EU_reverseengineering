package com.xcharge.charger.protocol.anyo.handler;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.support.p000v4.view.MotionEventCompat;
import android.text.TextUtils;
import android.util.Log;
import com.google.zxing.aztec.encoder.Encoder;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.UpgradeData;
import com.xcharge.charger.data.bean.UpgradeProgress;
import com.xcharge.charger.data.bean.device.MobileNet;
import com.xcharge.charger.data.bean.device.Network;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SoftwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.NetworkStatusObserver;
import com.xcharge.charger.protocol.anyo.C0245R;
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
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

public class AnyoProtocolAgent extends IoHandlerAdapter {
    public static String ANYO_CLOUD_HOST = null;
    public static int ANYO_CLOUD_PORT = 0;
    public static final int MSG_CONNECTED = 69634;
    public static final int MSG_CONNECT_ERROR = 69636;
    public static final int MSG_DISCONNECTED = 69635;
    public static final int MSG_INIT_CONNECTION = 69633;
    public static final int MSG_RECEIVED = 69639;
    public static final int MSG_SECOND_TIMER = 69640;
    public static final int MSG_SEND = 69637;
    public static final int MSG_SENDED = 69638;
    public static String SettedQrcode = null;
    public static final int TIMEOUT_CONNECT = 10;
    public static final int TIMEOUT_RESPONSE = 10;
    public static final int TIMEOUT_SEND = 5;
    public static final int TIMEOUT_WAIT_PLUGIN = 60;
    public static final int TIMEOUT_WAIT_PLUGOUT = -1;
    public static final int TIMEOUT_WAIT_START_CHARGE = -1;
    public static String firewareType = null;
    private static AnyoProtocolAgent instance = null;
    public static int magicNumber = 0;
    public static String protocolVersion = null;
    public static byte provider = 0;
    public static String softwareVersion = null;
    /* access modifiers changed from: private */
    public byte checkSumRand = 0;
    private ThreadPoolExecutor connectThreadPoolExecutor = null;
    private Context context = null;
    /* access modifiers changed from: private */
    public MsgHandler handler = null;
    /* access modifiers changed from: private */
    public HandlerTimer handlerTimer = null;
    private NetworkStatusObserver networkStatusObserver = null;
    private byte peerCheckSumRand = 0;
    private String pileNo = null;
    private byte pileType = 0;
    private HashMap<String, AnyoPortHandler> portHandlers = null;
    private AtomicInteger requestSeq = null;
    /* access modifiers changed from: private */
    public HashMap<String, SendRequestState> sendReqestState = null;
    /* access modifiers changed from: private */
    public IoSession session = null;
    private HandlerThread thread = null;
    private AnyoUpgradeSession upgradeSession = null;

    public static AnyoProtocolAgent getInstance() {
        if (instance == null) {
            instance = new AnyoProtocolAgent();
        }
        return instance;
    }

    private static class SendRequestState {
        AnyoMessage request;
        ANYO_REQUEST_STATE status;
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
        public void handleMessage(android.os.Message r15) {
            /*
                r14 = this;
                r12 = 104(0x68, float:1.46E-43)
                r2 = 0
                int r9 = r15.what     // Catch:{ Exception -> 0x0012 }
                switch(r9) {
                    case 69633: goto L_0x000c;
                    case 69634: goto L_0x0042;
                    case 69635: goto L_0x007c;
                    case 69636: goto L_0x00b1;
                    case 69637: goto L_0x00e4;
                    case 69638: goto L_0x013f;
                    case 69639: goto L_0x01ae;
                    case 69640: goto L_0x026b;
                    case 135169: goto L_0x0281;
                    default: goto L_0x0008;
                }
            L_0x0008:
                super.handleMessage(r15)
            L_0x000b:
                return
            L_0x000c:
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent r9 = com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.this     // Catch:{ Exception -> 0x0012 }
                r9.connect()     // Catch:{ Exception -> 0x0012 }
                goto L_0x0008
            L_0x0012:
                r3 = move-exception
                java.lang.String r9 = "AnyoProtocolAgent.handleMessage"
                java.lang.StringBuilder r10 = new java.lang.StringBuilder
                java.lang.String r11 = "except: "
                r10.<init>(r11)
                java.lang.String r11 = android.util.Log.getStackTraceString(r3)
                java.lang.StringBuilder r10 = r10.append(r11)
                java.lang.String r10 = r10.toString()
                android.util.Log.e(r9, r10)
                java.lang.StringBuilder r9 = new java.lang.StringBuilder
                java.lang.String r10 = "AnyoProtocolAgent handleMessage exception: "
                r9.<init>(r10)
                java.lang.String r10 = android.util.Log.getStackTraceString(r3)
                java.lang.StringBuilder r9 = r9.append(r10)
                java.lang.String r9 = r9.toString()
                com.xcharge.common.utils.LogUtils.syslog(r9)
                goto L_0x0008
            L_0x0042:
                java.lang.String r9 = "AnyoProtocolAgent.handleMessage"
                java.lang.String r10 = "connected !!!"
                android.util.Log.i(r9, r10)     // Catch:{ Exception -> 0x0012 }
                java.lang.String r9 = "anyo cloud connected !!!"
                com.xcharge.common.utils.LogUtils.cloudlog(r9)     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.data.provider.ChargeStatusCacheProvider r9 = com.xcharge.charger.data.provider.ChargeStatusCacheProvider.getInstance()     // Catch:{ Exception -> 0x0012 }
                r10 = 1
                r9.updateCloudConnected(r10)     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent r9 = com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.this     // Catch:{ Exception -> 0x0012 }
                java.util.Random r10 = new java.util.Random     // Catch:{ Exception -> 0x0012 }
                r10.<init>()     // Catch:{ Exception -> 0x0012 }
                int r10 = r10.nextInt()     // Catch:{ Exception -> 0x0012 }
                r10 = r10 & 255(0xff, float:3.57E-43)
                byte r10 = (byte) r10     // Catch:{ Exception -> 0x0012 }
                r9.checkSumRand = r10     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent r9 = com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.this     // Catch:{ Exception -> 0x0012 }
                com.xcharge.common.utils.HandlerTimer r9 = r9.handlerTimer     // Catch:{ Exception -> 0x0012 }
                r10 = 1000(0x3e8, double:4.94E-321)
                r12 = 69640(0x11008, float:9.7586E-41)
                r13 = 0
                r9.startTimer(r10, r12, r13)     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent r9 = com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.this     // Catch:{ Exception -> 0x0012 }
                r9.portsLogin()     // Catch:{ Exception -> 0x0012 }
                goto L_0x0008
            L_0x007c:
                java.lang.String r9 = "AnyoProtocolAgent.handleMessage"
                java.lang.String r10 = "disconnected !!!"
                android.util.Log.i(r9, r10)     // Catch:{ Exception -> 0x0012 }
                java.lang.String r9 = "anyo cloud disconnected !!!"
                com.xcharge.common.utils.LogUtils.cloudlog(r9)     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.data.provider.ChargeStatusCacheProvider r9 = com.xcharge.charger.data.provider.ChargeStatusCacheProvider.getInstance()     // Catch:{ Exception -> 0x0012 }
                r10 = 0
                r9.updateCloudConnected(r10)     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent r9 = com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.this     // Catch:{ Exception -> 0x0012 }
                com.xcharge.common.utils.HandlerTimer r9 = r9.handlerTimer     // Catch:{ Exception -> 0x0012 }
                r10 = 69640(0x11008, float:9.7586E-41)
                r9.stopTimer(r10)     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent r9 = com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.this     // Catch:{ Exception -> 0x0012 }
                r9.portsLogout()     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent r9 = com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.this     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent$MsgHandler r9 = r9.handler     // Catch:{ Exception -> 0x0012 }
                r10 = 69633(0x11001, float:9.7577E-41)
                r12 = 5000(0x1388, double:2.4703E-320)
                r9.sendEmptyMessageDelayed(r10, r12)     // Catch:{ Exception -> 0x0012 }
                goto L_0x0008
            L_0x00b1:
                java.lang.String r9 = "AnyoProtocolAgent.handleMessage"
                java.lang.String r10 = "failed to connect !!!"
                android.util.Log.i(r9, r10)     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.data.provider.HardwareStatusCacheProvider r9 = com.xcharge.charger.data.provider.HardwareStatusCacheProvider.getInstance()     // Catch:{ Exception -> 0x0012 }
                boolean r9 = r9.isNetworkConnected()     // Catch:{ Exception -> 0x0012 }
                if (r9 == 0) goto L_0x00d4
                java.lang.String r9 = "failed to connect to anyo cloud, try to diagnosis network connectivity ..."
                com.xcharge.common.utils.LogUtils.applog(r9)     // Catch:{ Exception -> 0x0012 }
                java.lang.Thread r9 = new java.lang.Thread     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent$MsgHandler$1 r10 = new com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent$MsgHandler$1     // Catch:{ Exception -> 0x0012 }
                r10.<init>()     // Catch:{ Exception -> 0x0012 }
                r9.<init>(r10)     // Catch:{ Exception -> 0x0012 }
                r9.start()     // Catch:{ Exception -> 0x0012 }
            L_0x00d4:
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent r9 = com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.this     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent$MsgHandler r9 = r9.handler     // Catch:{ Exception -> 0x0012 }
                r10 = 69633(0x11001, float:9.7577E-41)
                r12 = 20000(0x4e20, double:9.8813E-320)
                r9.sendEmptyMessageDelayed(r10, r12)     // Catch:{ Exception -> 0x0012 }
                goto L_0x0008
            L_0x00e4:
                java.lang.Object r9 = r15.obj     // Catch:{ Exception -> 0x0012 }
                r0 = r9
                com.xcharge.charger.protocol.anyo.bean.AnyoMessage r0 = (com.xcharge.charger.protocol.anyo.bean.AnyoMessage) r0     // Catch:{ Exception -> 0x0012 }
                r2 = r0
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent r9 = com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.this     // Catch:{ Exception -> 0x0012 }
                boolean r9 = r9.isConnected()     // Catch:{ Exception -> 0x0012 }
                if (r9 == 0) goto L_0x012e
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent r9 = com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.this     // Catch:{ Exception -> 0x0012 }
                org.apache.mina.core.session.IoSession r9 = r9.session     // Catch:{ Exception -> 0x0012 }
                r9.write(r2)     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.bean.AnyoHead r9 = r2.getHead()     // Catch:{ Exception -> 0x0012 }
                byte r9 = r9.getStartCode()     // Catch:{ Exception -> 0x0012 }
                if (r9 != r12) goto L_0x0008
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent$SendRequestState r4 = new com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent$SendRequestState     // Catch:{ Exception -> 0x0012 }
                r9 = 0
                r4.<init>(r9)     // Catch:{ Exception -> 0x0012 }
                r4.request = r2     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.type.ANYO_REQUEST_STATE r9 = com.xcharge.charger.protocol.anyo.type.ANYO_REQUEST_STATE.sending     // Catch:{ Exception -> 0x0012 }
                r4.status = r9     // Catch:{ Exception -> 0x0012 }
                long r10 = java.lang.System.currentTimeMillis()     // Catch:{ Exception -> 0x0012 }
                r4.timestamp = r10     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent r9 = com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.this     // Catch:{ Exception -> 0x0012 }
                java.util.HashMap r9 = r9.sendReqestState     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.bean.AnyoHead r10 = r2.getHead()     // Catch:{ Exception -> 0x0012 }
                byte r10 = r10.getSeq()     // Catch:{ Exception -> 0x0012 }
                java.lang.String r10 = java.lang.String.valueOf(r10)     // Catch:{ Exception -> 0x0012 }
                r9.put(r10, r4)     // Catch:{ Exception -> 0x0012 }
                goto L_0x0008
            L_0x012e:
                com.xcharge.charger.protocol.anyo.bean.AnyoHead r9 = r2.getHead()     // Catch:{ Exception -> 0x0012 }
                byte r9 = r9.getStartCode()     // Catch:{ Exception -> 0x0012 }
                if (r9 != r12) goto L_0x0008
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent r9 = com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.this     // Catch:{ Exception -> 0x0012 }
                r9.handleSendRequestFail(r2)     // Catch:{ Exception -> 0x0012 }
                goto L_0x0008
            L_0x013f:
                java.lang.Object r9 = r15.obj     // Catch:{ Exception -> 0x0012 }
                r0 = r9
                com.xcharge.charger.protocol.anyo.bean.AnyoMessage r0 = (com.xcharge.charger.protocol.anyo.bean.AnyoMessage) r0     // Catch:{ Exception -> 0x0012 }
                r2 = r0
                java.lang.String r9 = "AnyoProtocolAgent.handleMessage"
                java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0012 }
                java.lang.String r11 = "succeed to send anyo msg: "
                r10.<init>(r11)     // Catch:{ Exception -> 0x0012 }
                java.lang.String r11 = r2.toJson()     // Catch:{ Exception -> 0x0012 }
                java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ Exception -> 0x0012 }
                java.lang.String r10 = r10.toString()     // Catch:{ Exception -> 0x0012 }
                android.util.Log.d(r9, r10)     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.bean.AnyoHead r9 = r2.getHead()     // Catch:{ Exception -> 0x0012 }
                byte r9 = r9.getStartCode()     // Catch:{ Exception -> 0x0012 }
                if (r9 != r12) goto L_0x0008
                com.xcharge.charger.protocol.anyo.bean.AnyoHead r9 = r2.getHead()     // Catch:{ Exception -> 0x0012 }
                byte r9 = r9.getSeq()     // Catch:{ Exception -> 0x0012 }
                java.lang.String r6 = java.lang.String.valueOf(r9)     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent r9 = com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.this     // Catch:{ Exception -> 0x0012 }
                java.util.HashMap r9 = r9.sendReqestState     // Catch:{ Exception -> 0x0012 }
                java.lang.Object r4 = r9.get(r6)     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent$SendRequestState r4 = (com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.SendRequestState) r4     // Catch:{ Exception -> 0x0012 }
                if (r4 == 0) goto L_0x0194
                com.xcharge.charger.protocol.anyo.type.ANYO_REQUEST_STATE r9 = com.xcharge.charger.protocol.anyo.type.ANYO_REQUEST_STATE.sended     // Catch:{ Exception -> 0x0012 }
                r4.status = r9     // Catch:{ Exception -> 0x0012 }
                long r10 = java.lang.System.currentTimeMillis()     // Catch:{ Exception -> 0x0012 }
                r4.timestamp = r10     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent r9 = com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.this     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.bean.AnyoMessage r10 = r4.request     // Catch:{ Exception -> 0x0012 }
                r9.handleSendRequestOk(r10)     // Catch:{ Exception -> 0x0012 }
                goto L_0x0008
            L_0x0194:
                java.lang.String r9 = "AnyoProtocolAgent.handleMessage"
                java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0012 }
                java.lang.String r11 = "maybe timeout to send anyo request msg: "
                r10.<init>(r11)     // Catch:{ Exception -> 0x0012 }
                java.lang.String r11 = r2.toJson()     // Catch:{ Exception -> 0x0012 }
                java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ Exception -> 0x0012 }
                java.lang.String r10 = r10.toString()     // Catch:{ Exception -> 0x0012 }
                android.util.Log.w(r9, r10)     // Catch:{ Exception -> 0x0012 }
                goto L_0x0008
            L_0x01ae:
                java.lang.Object r9 = r15.obj     // Catch:{ Exception -> 0x0012 }
                r0 = r9
                com.xcharge.charger.protocol.anyo.bean.AnyoMessage r0 = (com.xcharge.charger.protocol.anyo.bean.AnyoMessage) r0     // Catch:{ Exception -> 0x0012 }
                r2 = r0
                java.lang.String r9 = "AnyoProtocolAgent.handleMessage"
                java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0012 }
                java.lang.String r11 = "received anyo msg: "
                r10.<init>(r11)     // Catch:{ Exception -> 0x0012 }
                java.lang.String r11 = r2.toJson()     // Catch:{ Exception -> 0x0012 }
                java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ Exception -> 0x0012 }
                java.lang.String r10 = r10.toString()     // Catch:{ Exception -> 0x0012 }
                android.util.Log.d(r9, r10)     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.bean.AnyoHead r9 = r2.getHead()     // Catch:{ Exception -> 0x0012 }
                byte r9 = r9.getCmdCode()     // Catch:{ Exception -> 0x0012 }
                r10 = 16
                if (r9 == r10) goto L_0x0200
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent r9 = com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.this     // Catch:{ Exception -> 0x0012 }
                byte r9 = r9.getPeerCheckSumRand()     // Catch:{ Exception -> 0x0012 }
                boolean r9 = r2.verifyCheckSum(r9)     // Catch:{ Exception -> 0x0012 }
                if (r9 != 0) goto L_0x0200
                java.lang.String r9 = "AnyoProtocolAgent.handleMessage"
                java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0012 }
                java.lang.String r11 = "checksum error, peer rand: "
                r10.<init>(r11)     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent r11 = com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.this     // Catch:{ Exception -> 0x0012 }
                byte r11 = r11.getPeerCheckSumRand()     // Catch:{ Exception -> 0x0012 }
                java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ Exception -> 0x0012 }
                java.lang.String r10 = r10.toString()     // Catch:{ Exception -> 0x0012 }
                android.util.Log.w(r9, r10)     // Catch:{ Exception -> 0x0012 }
                goto L_0x000b
            L_0x0200:
                com.xcharge.charger.protocol.anyo.bean.AnyoHead r9 = r2.getHead()     // Catch:{ Exception -> 0x0012 }
                byte r9 = r9.getStartCode()     // Catch:{ Exception -> 0x0012 }
                r10 = -86
                if (r9 != r10) goto L_0x0259
                com.xcharge.charger.protocol.anyo.bean.AnyoHead r9 = r2.getHead()     // Catch:{ Exception -> 0x0012 }
                byte r9 = r9.getSeq()     // Catch:{ Exception -> 0x0012 }
                java.lang.String r7 = java.lang.String.valueOf(r9)     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent r9 = com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.this     // Catch:{ Exception -> 0x0012 }
                java.util.HashMap r9 = r9.sendReqestState     // Catch:{ Exception -> 0x0012 }
                java.lang.Object r4 = r9.get(r7)     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent$SendRequestState r4 = (com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.SendRequestState) r4     // Catch:{ Exception -> 0x0012 }
                if (r4 == 0) goto L_0x023f
                com.xcharge.charger.protocol.anyo.bean.AnyoMessage r5 = r4.request     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent r9 = com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.this     // Catch:{ Exception -> 0x0012 }
                java.util.HashMap r9 = r9.sendReqestState     // Catch:{ Exception -> 0x0012 }
                r9.remove(r7)     // Catch:{ Exception -> 0x0012 }
                java.lang.String r9 = r5.getPort()     // Catch:{ Exception -> 0x0012 }
                r2.setPort(r9)     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent r9 = com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.this     // Catch:{ Exception -> 0x0012 }
                r9.dispatchAnyoMessage(r2, r5)     // Catch:{ Exception -> 0x0012 }
                goto L_0x0008
            L_0x023f:
                java.lang.String r9 = "AnyoProtocolAgent.handleMessage"
                java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0012 }
                java.lang.String r11 = "maybe timeout to wait for response msg: "
                r10.<init>(r11)     // Catch:{ Exception -> 0x0012 }
                java.lang.String r11 = r2.toJson()     // Catch:{ Exception -> 0x0012 }
                java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ Exception -> 0x0012 }
                java.lang.String r10 = r10.toString()     // Catch:{ Exception -> 0x0012 }
                android.util.Log.w(r9, r10)     // Catch:{ Exception -> 0x0012 }
                goto L_0x0008
            L_0x0259:
                com.xcharge.charger.protocol.anyo.bean.AnyoHead r9 = r2.getHead()     // Catch:{ Exception -> 0x0012 }
                byte r9 = r9.getStartCode()     // Catch:{ Exception -> 0x0012 }
                if (r9 != r12) goto L_0x0008
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent r9 = com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.this     // Catch:{ Exception -> 0x0012 }
                r10 = 0
                r9.dispatchAnyoMessage(r2, r10)     // Catch:{ Exception -> 0x0012 }
                goto L_0x0008
            L_0x026b:
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent r9 = com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.this     // Catch:{ Exception -> 0x0012 }
                r9.requestTimeoutCheck()     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent r9 = com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.this     // Catch:{ Exception -> 0x0012 }
                com.xcharge.common.utils.HandlerTimer r9 = r9.handlerTimer     // Catch:{ Exception -> 0x0012 }
                r10 = 1000(0x3e8, double:4.94E-321)
                r12 = 69640(0x11008, float:9.7586E-41)
                r13 = 0
                r9.startTimer(r10, r12, r13)     // Catch:{ Exception -> 0x0012 }
                goto L_0x0008
            L_0x0281:
                java.lang.Object r8 = r15.obj     // Catch:{ Exception -> 0x0012 }
                android.net.Uri r8 = (android.net.Uri) r8     // Catch:{ Exception -> 0x0012 }
                com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent r9 = com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.this     // Catch:{ Exception -> 0x0012 }
                r9.handleNetworkStatusChanged(r8)     // Catch:{ Exception -> 0x0012 }
                goto L_0x0008
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent.MsgHandler.handleMessage(android.os.Message):void");
        }
    }

    public void init(Context context2) {
        this.context = context2;
        this.requestSeq = new AtomicInteger(-1);
        this.portHandlers = new HashMap<>();
        HashMap<String, Port> ports = HardwareStatusCacheProvider.getInstance().getPorts();
        if (ports != null) {
            for (String port : ports.keySet()) {
                String anyoPort = getAnyoPort(port);
                AnyoPortHandler portHandler = new AnyoPortHandler();
                portHandler.init(context2, anyoPort, this);
                this.portHandlers.put(anyoPort, portHandler);
            }
        }
        this.connectThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue(1), new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                Log.e("AnyoProtocolAgent.ThreadPoolExecutor.rejectedExecution", "rejected runnable: " + r.toString() + ", active runnables: " + executor.getActiveCount());
            }
        });
        this.sendReqestState = new HashMap<>();
        this.thread = new HandlerThread("AnyoProtocolAgent", 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(context2);
        this.networkStatusObserver = new NetworkStatusObserver(context2, this.handler);
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor(Network.class.getSimpleName()), true, this.networkStatusObserver);
        AnyoConfig anyoConfig = loadConfig();
        ANYO_CLOUD_HOST = anyoConfig.getCloudHost();
        ANYO_CLOUD_PORT = anyoConfig.getCloudPort();
        provider = (byte) (anyoConfig.getProvider() & MotionEventCompat.ACTION_MASK);
        magicNumber = (int) (anyoConfig.getMagicNumber() & -1);
        SettedQrcode = anyoConfig.getQrcode();
        protocolVersion = anyoConfig.getProtocolVersion();
        softwareVersion = anyoConfig.getSoftwareVersion();
        SoftwareStatusCacheProvider.getInstance().updateAppVer(softwareVersion);
        firewareType = HardwareStatusCacheProvider.getInstance().getSn().substring(0, 4);
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
        String cfg = ContextUtils.getRawFileToString(this.context, C0245R.raw.anyo_cfg);
        AnyoConfig config = null;
        if (!TextUtils.isEmpty(cfg)) {
            config = (AnyoConfig) new AnyoConfig().fromJson(cfg);
            if ("C2011601CNZQMUWJ".equals(HardwareStatusCacheProvider.getInstance().getSn())) {
                config.setCloudHost("192.168.1.100");
                config.setCloudPort(8003);
            }
        }
        String userCfg = ContextUtils.readFileData("anyo_cfg.json", this.context);
        if (!TextUtils.isEmpty(userCfg) && (ucfg = (UserConfig) new UserConfig().fromJson(userCfg)) != null) {
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

    /* access modifiers changed from: private */
    public boolean isConnected() {
        return this.session != null && this.session.isConnected();
    }

    public void initConnection() {
        this.handler.sendEmptyMessage(69633);
    }

    private class ConnectTask implements Runnable {
        private IoHandler ioHandler = null;
        private ProtocolCodecFilter protocolCodecFilter = null;

        public ConnectTask(ProtocolCodecFilter filter, IoHandler handler) {
            this.protocolCodecFilter = filter;
            this.ioHandler = handler;
        }

        public void run() {
            IoConnector connector = new NioSocketConnector();
            connector.setConnectTimeoutMillis(10000);
            connector.getFilterChain().addLast("AnyoProtocol", this.protocolCodecFilter);
            connector.setHandler(this.ioHandler);
            connector.getSessionConfig().setReadBufferSize(2048);
            connector.getSessionConfig().setWriteTimeout(5);
            try {
                ConnectFuture future = connector.connect((SocketAddress) new InetSocketAddress(AnyoProtocolAgent.ANYO_CLOUD_HOST, AnyoProtocolAgent.ANYO_CLOUD_PORT));
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

    /* access modifiers changed from: private */
    public void connect() {
        if (HardwareStatusCacheProvider.getInstance().isNetworkConnected()) {
            Log.i("AnyoProtocolAgent.connect", "init connection !!!");
            this.connectThreadPoolExecutor.execute(new ConnectTask(new ProtocolCodecFilter((ProtocolEncoder) new AnyoMessageEncoder(), (ProtocolDecoder) new AnyoMessageDecoder()), this));
            return;
        }
        this.handler.sendEmptyMessageDelayed(69633, 5000);
    }

    public void disconnect() {
        if (this.session != null) {
            Log.d("AnyoProtocolAgent.disconnect", "force to disconnect !!!");
            this.session.closeNow().awaitUninterruptibly(10000);
        }
    }

    public void exceptionCaught(IoSession session2, Throwable cause) throws Exception {
        Log.w("AnyoProtocolAgent.exceptionCaught", "exception: " + Log.getStackTraceString(cause));
        super.exceptionCaught(session2, cause);
        disconnect();
    }

    public void inputClosed(IoSession session2) throws Exception {
        Log.w("AnyoProtocolAgent.inputClosed", "session input closed: " + session2.getId());
        super.inputClosed(session2);
        LogUtils.cloudlog("anyo cloud connection has been closed by remote !!!");
        disconnect();
    }

    public void messageReceived(IoSession session2, Object message) throws Exception {
        super.messageReceived(session2, message);
        this.handler.sendMessage(this.handler.obtainMessage(69639, (AnyoMessage) message));
    }

    public void messageSent(IoSession session2, Object message) throws Exception {
        super.messageSent(session2, message);
        this.handler.sendMessage(this.handler.obtainMessage(69638, (AnyoMessage) message));
    }

    public void sessionClosed(IoSession session2) throws Exception {
        super.sessionClosed(session2);
        Log.d("AnyoProtocolAgent.sessionClosed", "session closed: " + session2.getId());
    }

    public void sessionCreated(IoSession session2) throws Exception {
        super.sessionCreated(session2);
        Log.d("AnyoProtocolAgent.sessionCreated", "session created: " + session2.getId());
    }

    public void sessionOpened(IoSession session2) throws Exception {
        super.sessionOpened(session2);
        Log.d("AnyoProtocolAgent.sessionOpened", "session opend: " + session2.getId());
    }

    public void sessionIdle(IoSession session2, IdleStatus status) throws Exception {
        super.sessionIdle(session2, status);
        Log.d("AnyoProtocolAgent.sessionIdle", "session idle: " + session2.getIdleCount(status));
    }

    /* access modifiers changed from: private */
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
        if (anyoPortHandler != null) {
            return anyoPortHandler.getChargeSession();
        }
        Log.w("AnyoProtocolAgent.getChargeSession", "no available port handler for port: " + anyoPort);
        return null;
    }

    /* access modifiers changed from: private */
    public void dispatchAnyoMessage(AnyoMessage msg, AnyoMessage sendedRequest) {
        byte startCode = msg.getHead().getStartCode();
        if (startCode == 104) {
            handleRequestMessage(msg);
        } else if (startCode == -86) {
            handleResponseMessage(msg, sendedRequest);
        } else {
            Log.w("AnyoProtocolAgent.dispatchAnyoMessage", "unsupported message start code: " + startCode);
        }
    }

    private void handleRequestMessage(AnyoMessage request) {
        switch (request.getHead().getCmdCode()) {
            case Byte.MIN_VALUE:
                ResetChargeRequest resetChargeRequest = (ResetChargeRequest) request;
                byte anyoPort = resetChargeRequest.getPortNo();
                if (anyoPort == 0) {
                    anyoPort = 10;
                }
                String port = String.valueOf(anyoPort);
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
                String port2 = String.valueOf(anyoPort2);
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
                String port3 = String.valueOf(anyoPort3);
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
                String port4 = String.valueOf(anyoPort4);
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
            ErrorCode error = HardwareStatusCacheProvider.getInstance().getPort("1").getDeviceError();
            Port errorPortStatus = null;
            HashMap<String, Object> errData = error.getData();
            if (errData != null && errData.size() > 0) {
                String errorPortStatusJson = (String) errData.get("portStatus");
                if (!TextUtils.isEmpty(errorPortStatusJson)) {
                    errorPortStatus = (Port) new Port().fromJson(errorPortStatusJson);
                }
            }
            switch (error.getCode()) {
                case 200:
                    acDeviceFaultStatus = 0;
                    break;
                case ErrorCode.EC_DEVICE_NO_GROUND:
                    acDeviceFaultStatus = 32;
                    break;
                case ErrorCode.EC_DEVICE_EMERGENCY_STOP:
                    acDeviceFaultStatus = 256;
                    break;
                case ErrorCode.EC_DEVICE_VOLT_ERROR:
                    if (errorPortStatus == null) {
                        acDeviceFaultStatus = 8;
                        break;
                    } else {
                        ArrayList<Double> volts = errorPortStatus.getVolts();
                        if (volts != null && volts.size() > 0) {
                            Double volt = volts.get(0);
                            if (volt != null) {
                                if (volt.doubleValue() <= 220.0d) {
                                    acDeviceFaultStatus = 16;
                                    break;
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
                    }
                    break;
                case ErrorCode.EC_DEVICE_POWER_LEAK:
                    acDeviceFaultStatus = 4;
                    break;
                case ErrorCode.EC_DEVICE_COMM_ERROR:
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
            Log.w("AnyoProtocolAgent.handleStartUpgradeRequest", "not matched client id !!! this pile: " + provider + ", in request: " + clientId);
            startUpgradeResponse(startUpgradeRequest, (byte) 19);
            return;
        }
        byte upgradeType = startUpgradeRequest.getUpgradeType();
        if (upgradeType != 3) {
            Log.w("AnyoProtocolAgent.handleStartUpgradeRequest", "now only support HTTP upgrade !!! type: " + upgradeType);
            startUpgradeResponse(startUpgradeRequest, (byte) 19);
            return;
        }
        AnyoUpgradeSession upgradeSession2 = getUpgradeSession();
        if (TextUtils.isEmpty(upgradeSession2.getStage())) {
            startUpgradeResponse(startUpgradeRequest, (byte) 0);
            upgradeSession2.setStage(UpgradeProgress.STAGE_DOWNLOAD);
            upgradeSession2.setRequestUpgrade(startUpgradeRequest);
            String url = startUpgradeRequest.getUpgradeAddr();
            upgradeSession2.setDownloadFile("/data/data/com.xcharge.charger/download/upgrade/update.dat");
            Log.i("AnyoProtocolAgent.handleStartUpgradeRequest", "start download ..., url: " + url);
            downloadProgress(200, 1, 0);
            HttpDownloadManager.getInstance().downloadFile(this.context, url, "/data/data/com.xcharge.charger/download/upgrade/update.dat", new HttpDownloadManager.DownLoadListener() {
                public void onDownLoadPercentage(long curPosition, long total) {
                }

                public void onDownLoadPercentage(int p) {
                    AnyoProtocolAgent.this.downloadProgress(200, 2, p);
                }

                public void onDownLoadFail() {
                    AnyoProtocolAgent.this.downloadProgress(ErrorCode.EC_UPGRADE_DOWNLOAD_FAIL, 0, 0);
                    AnyoProtocolAgent.this.reportUpgradeDownloadCompleteRequest(false);
                    AnyoProtocolAgent.this.clearUpgradeSession();
                }

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

    /* access modifiers changed from: private */
    public void downloadProgress(int error, int status, int progress) {
        UpgradeProgress upgradeProgress = SoftwareStatusCacheProvider.getInstance().getUpgradeProgress();
        upgradeProgress.setStage(UpgradeProgress.STAGE_DOWNLOAD);
        upgradeProgress.setUpgradeData((UpgradeData) null);
        upgradeProgress.setError(new ErrorCode(error));
        upgradeProgress.setStatus(status);
        upgradeProgress.setProgress(progress);
        SoftwareStatusCacheProvider.getInstance().updateUpgradeProgress(upgradeProgress);
    }

    /* access modifiers changed from: private */
    public boolean verifyFileCRC32() {
        AnyoUpgradeSession upgradeSession2 = getUpgradeSession();
        long crc32 = 0;
        long sourceCRC32 = ((long) upgradeSession2.getRequestUpgrade().getChecksum()) & XMSZHead.ID_BROADCAST;
        boolean isOk = false;
        String downloadFile = upgradeSession2.getDownloadFile();
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

    /* access modifiers changed from: private */
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
        switch (response.getHead().getCmdCode()) {
            case Encoder.DEFAULT_EC_PERCENT:
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

    /* access modifiers changed from: private */
    public void portsLogin() {
        for (AnyoPortHandler portHandler : this.portHandlers.values()) {
            portHandler.sendMessage(portHandler.obtainMessage(73729));
        }
    }

    /* access modifiers changed from: private */
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

    public byte setCheckSumRand(byte checkSumRand2) {
        return this.checkSumRand;
    }

    public byte getPeerCheckSumRand() {
        return this.peerCheckSumRand;
    }

    public byte setPeerCheckSumRand(byte checkSumRand2) {
        this.peerCheckSumRand = checkSumRand2;
        return checkSumRand2;
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
            return 0;
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

    /* access modifiers changed from: private */
    public void requestTimeoutCheck() {
        if (this.sendReqestState.size() > 0) {
            Iterator<Map.Entry<String, SendRequestState>> it = this.sendReqestState.entrySet().iterator();
            while (it.hasNext()) {
                SendRequestState requestState = it.next().getValue();
                ANYO_REQUEST_STATE state = requestState.status;
                long timestamp = requestState.timestamp;
                if (ANYO_REQUEST_STATE.sending.equals(state)) {
                    if (System.currentTimeMillis() - timestamp > 5000) {
                        it.remove();
                        handleSendRequestFail(requestState.request);
                    }
                } else if (ANYO_REQUEST_STATE.sended.equals(state) && System.currentTimeMillis() - timestamp > 10000) {
                    it.remove();
                    handleRequestTimeout(requestState.request);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleSendRequestOk(AnyoMessage request) {
        String port = request.getPort();
        if (!TextUtils.isEmpty(port)) {
            AnyoPortHandler portHandler = getPortHandler(port);
            portHandler.sendMessage(portHandler.obtainMessage(73731, request));
        }
    }

    /* access modifiers changed from: private */
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
        if (anyoPortHandler != null) {
            return anyoPortHandler.authRequest(cardNo);
        }
        Log.w("AnyoProtocolAgent.sendAuthRequest", "no available port handler for port: " + port);
        return null;
    }

    public boolean sendUnlockPortResponse(UnlockPortRequest request, byte statusCode) {
        String anyoPort = request.getPort();
        AnyoPortHandler anyoPortHandler = getPortHandler(anyoPort);
        if (anyoPortHandler != null) {
            return anyoPortHandler.unlockPortResponse(request, statusCode);
        }
        Log.w("AnyoProtocolAgent.sendUnlockPortResponse", "no available port handler for port: " + anyoPort);
        return false;
    }

    public boolean sendStartChargeResponse(StartChargeRequest request, byte statusCode) {
        String anyoPort = request.getPort();
        AnyoPortHandler anyoPortHandler = getPortHandler(anyoPort);
        if (anyoPortHandler != null) {
            return anyoPortHandler.startChargeResponse(request, statusCode);
        }
        Log.w("AnyoProtocolAgent.sendStartChargeResponse", "no available port handler for port: " + anyoPort);
        return false;
    }

    public boolean sendStopChargeResponse(StopChargeRequest request, byte statusCode) {
        String anyoPort = request.getPort();
        AnyoPortHandler anyoPortHandler = getPortHandler(anyoPort);
        if (anyoPortHandler != null) {
            return anyoPortHandler.stopChargeResponse(request, statusCode);
        }
        Log.w("AnyoProtocolAgent.sendStopChargeResponse", "no available port handler for port: " + anyoPort);
        return false;
    }

    public boolean sendResetChargeResponse(ResetChargeRequest request, byte statusCode) {
        String anyoPort = request.getPort();
        AnyoPortHandler anyoPortHandler = getPortHandler(anyoPort);
        if (anyoPortHandler != null) {
            return anyoPortHandler.resetChargeResponse(request, statusCode);
        }
        Log.w("AnyoProtocolAgent.sendStopChargeResponse", "no available port handler for port: " + anyoPort);
        return false;
    }

    public void handleUpdateQrcodeRequest(String localPort) {
        ChargeStatusCacheProvider.getInstance().updatePortQrcodeContent(localPort, getQrcodeContent(getAnyoPort(localPort)));
    }
}
