package com.xcharge.charger.protocol.anyo.handler;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.p000v4.view.MotionEventCompat;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.CHARGE_INIT_TYPE;
import com.xcharge.charger.data.bean.type.CHARGE_STATUS;
import com.xcharge.charger.data.bean.type.CHARGE_STOP_CAUSE;
import com.xcharge.charger.data.bean.type.CHARGE_USER_TYPE;
import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.data.proxy.PortChargeStatusObserver;
import com.xcharge.charger.data.proxy.PortStatusObserver;
import com.xcharge.charger.device.p005c2.bean.PortRuntimeData;
import com.xcharge.charger.protocol.anyo.bean.AnyoHead;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.anyo.bean.AnyoStatus;
import com.xcharge.charger.protocol.anyo.bean.request.AuthRequest;
import com.xcharge.charger.protocol.anyo.bean.request.HeartBeatRequest;
import com.xcharge.charger.protocol.anyo.bean.request.LoginRequest;
import com.xcharge.charger.protocol.anyo.bean.request.ReportChargeRequest;
import com.xcharge.charger.protocol.anyo.bean.request.ReportChargeStoppedRequest;
import com.xcharge.charger.protocol.anyo.bean.request.ReportEventRequest;
import com.xcharge.charger.protocol.anyo.bean.request.ReportHistoryBillRequest;
import com.xcharge.charger.protocol.anyo.bean.request.ResetChargeRequest;
import com.xcharge.charger.protocol.anyo.bean.request.StartChargeRequest;
import com.xcharge.charger.protocol.anyo.bean.request.StopChargeRequest;
import com.xcharge.charger.protocol.anyo.bean.request.UnlockPortRequest;
import com.xcharge.charger.protocol.anyo.bean.response.LoginResponse;
import com.xcharge.charger.protocol.anyo.bean.response.ReportChargeStoppedResponse;
import com.xcharge.charger.protocol.anyo.bean.response.ReportHistoryBillResponse;
import com.xcharge.charger.protocol.anyo.bean.response.ResetChargeResponse;
import com.xcharge.charger.protocol.anyo.bean.response.StartChargeResponse;
import com.xcharge.charger.protocol.anyo.bean.response.StopChargeResponse;
import com.xcharge.charger.protocol.anyo.bean.response.UnlockPortResponse;
import com.xcharge.charger.protocol.anyo.router.AnyoDCAPGateway;
import com.xcharge.charger.protocol.anyo.session.AnyoChargeSession;
import com.xcharge.charger.protocol.anyo.type.ANYO_PORT_STATE;
import com.xcharge.common.utils.HandlerTimer;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.TimeUtils;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.apache.commons.lang3.CharEncoding;

public class AnyoPortHandler {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STATUS = null;

    /* renamed from: $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE */
    private static /* synthetic */ int[] f78x26790b25 = null;
    public static final int MSG_CLOUD_REQUEST = 73735;
    public static final int MSG_CLOUD_RESPONSE = 73736;
    public static final int MSG_HERAT_BEAT_TIMER = 73737;
    public static final int MSG_INIT_LOGIN = 73729;
    public static final int MSG_LOGOUT = 73730;
    public static final int MSG_REPORT_CHARGE_TIMER = 73744;
    public static final int MSG_REPORT_HISTORY_CHARGE_TIMER = 73745;
    public static final int MSG_REQUEST_SEND_FAIL = 73732;
    public static final int MSG_REQUEST_SEND_OK = 73731;
    public static final int MSG_REQUEST_TIMEOUT = 73733;
    public static final int MSG_REQUSET_RESEND = 73734;
    public static final int TIMEOUT_HEART_BEAT = 120;
    public static final int TIMEOUT_REPORT_CHARGE = 60;
    public static final int TIMEOUT_REPORT_HISTORY_CHARGE = 120;
    private ANYO_PORT_STATE anyoPortStatus = ANYO_PORT_STATE.not_login;
    private AnyoChargeSession chargeSession = null;
    private Context context = null;
    private int failHeartBeatCnt = 0;
    private MsgHandler handler = null;
    /* access modifiers changed from: private */
    public HandlerTimer handlerTimer = null;
    private AnyoStatus latestAnyoStatus = new AnyoStatus();
    private long okHeartBeatCnt = 0;
    /* access modifiers changed from: private */
    public String port = null;
    private PortChargeStatusObserver portChargeStatusObserver = null;
    private PortStatusObserver portStatusObserver = null;
    /* access modifiers changed from: private */
    public CHARGE_STATUS status = CHARGE_STATUS.IDLE;
    private HandlerThread thread = null;

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STATUS() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STATUS;
        if (iArr == null) {
            iArr = new int[CHARGE_STATUS.values().length];
            try {
                iArr[CHARGE_STATUS.CHARGE_START_WAITTING.ordinal()] = 2;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[CHARGE_STATUS.CHARGE_STOP_WAITTING.ordinal()] = 4;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[CHARGE_STATUS.CHARGING.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[CHARGE_STATUS.IDLE.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STATUS = iArr;
        }
        return iArr;
    }

    /* renamed from: $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE */
    static /* synthetic */ int[] m21x26790b25() {
        int[] iArr = f78x26790b25;
        if (iArr == null) {
            iArr = new int[CHARGE_STOP_CAUSE.values().length];
            try {
                iArr[CHARGE_STOP_CAUSE.car.ordinal()] = 7;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.fault.ordinal()] = 12;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.full.ordinal()] = 8;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.local_user.ordinal()] = 3;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.no_balance.ordinal()] = 9;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.plugout.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.reboot.ordinal()] = 11;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.remote_user.ordinal()] = 4;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.system_user.ordinal()] = 5;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.undefined.ordinal()] = 1;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.user.ordinal()] = 2;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.user_set.ordinal()] = 10;
            } catch (NoSuchFieldError e12) {
            }
            f78x26790b25 = iArr;
        }
        return iArr;
    }

    private class MsgHandler extends Handler {
        public MsgHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* JADX WARNING: Unknown top exception splitter block from list: {B:26:0x0130=Splitter:B:26:0x0130, B:30:0x0147=Splitter:B:30:0x0147, B:21:0x00ef=Splitter:B:21:0x00ef} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(android.os.Message r14) {
            /*
                r13 = this;
                r2 = 0
                r7 = 0
                int r8 = r14.what     // Catch:{ Exception -> 0x002b }
                switch(r8) {
                    case 73729: goto L_0x000b;
                    case 73730: goto L_0x005b;
                    case 73731: goto L_0x007b;
                    case 73732: goto L_0x0082;
                    case 73733: goto L_0x008f;
                    case 73734: goto L_0x00b4;
                    case 73735: goto L_0x0159;
                    case 73736: goto L_0x0164;
                    case 73737: goto L_0x00bf;
                    case 73744: goto L_0x0101;
                    case 73745: goto L_0x0142;
                    case 131073: goto L_0x0177;
                    case 139265: goto L_0x01ac;
                    default: goto L_0x0007;
                }
            L_0x0007:
                super.handleMessage(r14)
                return
            L_0x000b:
                java.lang.String r8 = "AnyoPortHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002b }
                java.lang.String r10 = "init login !!! port: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler r10 = com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.this     // Catch:{ Exception -> 0x002b }
                java.lang.String r10 = r10.port     // Catch:{ Exception -> 0x002b }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002b }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002b }
                android.util.Log.i(r8, r9)     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler r8 = com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.this     // Catch:{ Exception -> 0x002b }
                r8.loginRequest()     // Catch:{ Exception -> 0x002b }
                goto L_0x0007
            L_0x002b:
                r4 = move-exception
                java.lang.String r8 = "AnyoPortHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder
                java.lang.String r10 = "except: "
                r9.<init>(r10)
                java.lang.String r10 = android.util.Log.getStackTraceString(r4)
                java.lang.StringBuilder r9 = r9.append(r10)
                java.lang.String r9 = r9.toString()
                android.util.Log.e(r8, r9)
                java.lang.StringBuilder r8 = new java.lang.StringBuilder
                java.lang.String r9 = "AnyoPortHandler handleMessage exception: "
                r8.<init>(r9)
                java.lang.String r9 = android.util.Log.getStackTraceString(r4)
                java.lang.StringBuilder r8 = r8.append(r9)
                java.lang.String r8 = r8.toString()
                com.xcharge.common.utils.LogUtils.syslog(r8)
                goto L_0x0007
            L_0x005b:
                java.lang.String r8 = "AnyoPortHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002b }
                java.lang.String r10 = "logout !!! port: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler r10 = com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.this     // Catch:{ Exception -> 0x002b }
                java.lang.String r10 = r10.port     // Catch:{ Exception -> 0x002b }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002b }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002b }
                android.util.Log.i(r8, r9)     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler r8 = com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.this     // Catch:{ Exception -> 0x002b }
                r8.clearLoginStatus()     // Catch:{ Exception -> 0x002b }
                goto L_0x0007
            L_0x007b:
                java.lang.Object r8 = r14.obj     // Catch:{ Exception -> 0x002b }
                r0 = r8
                com.xcharge.charger.protocol.anyo.bean.AnyoMessage r0 = (com.xcharge.charger.protocol.anyo.bean.AnyoMessage) r0     // Catch:{ Exception -> 0x002b }
                r2 = r0
                goto L_0x0007
            L_0x0082:
                java.lang.Object r8 = r14.obj     // Catch:{ Exception -> 0x002b }
                r0 = r8
                com.xcharge.charger.protocol.anyo.bean.AnyoMessage r0 = (com.xcharge.charger.protocol.anyo.bean.AnyoMessage) r0     // Catch:{ Exception -> 0x002b }
                r2 = r0
                com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler r8 = com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.this     // Catch:{ Exception -> 0x002b }
                r8.handleFailedRequest(r2)     // Catch:{ Exception -> 0x002b }
                goto L_0x0007
            L_0x008f:
                java.lang.Object r8 = r14.obj     // Catch:{ Exception -> 0x002b }
                r0 = r8
                com.xcharge.charger.protocol.anyo.bean.AnyoMessage r0 = (com.xcharge.charger.protocol.anyo.bean.AnyoMessage) r0     // Catch:{ Exception -> 0x002b }
                r2 = r0
                java.lang.String r8 = "AnyoPortHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002b }
                java.lang.String r10 = "send anyo request timeout: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002b }
                java.lang.String r10 = r2.toJson()     // Catch:{ Exception -> 0x002b }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002b }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002b }
                android.util.Log.w(r8, r9)     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler r8 = com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.this     // Catch:{ Exception -> 0x002b }
                r8.handleFailedRequest(r2)     // Catch:{ Exception -> 0x002b }
                goto L_0x0007
            L_0x00b4:
                java.lang.Object r5 = r14.obj     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.bean.AnyoMessage r5 = (com.xcharge.charger.protocol.anyo.bean.AnyoMessage) r5     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler r8 = com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.this     // Catch:{ Exception -> 0x002b }
                r8.resendRequest(r5)     // Catch:{ Exception -> 0x002b }
                goto L_0x0007
            L_0x00bf:
                java.lang.String r8 = "AnyoPortHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002b }
                java.lang.String r10 = "send anyo heart beat periodically, port: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler r10 = com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.this     // Catch:{ Exception -> 0x002b }
                java.lang.String r10 = r10.port     // Catch:{ Exception -> 0x002b }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002b }
                java.lang.String r10 = ", status: "
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler r10 = com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.this     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.data.bean.type.CHARGE_STATUS r10 = r10.status     // Catch:{ Exception -> 0x002b }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002b }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002b }
                android.util.Log.d(r8, r9)     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler r8 = com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.this     // Catch:{ Exception -> 0x01e7 }
                r9 = 0
                r8.heartBeatRequest(r9)     // Catch:{ Exception -> 0x01e7 }
            L_0x00ef:
                com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler r8 = com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.this     // Catch:{ Exception -> 0x002b }
                com.xcharge.common.utils.HandlerTimer r8 = r8.handlerTimer     // Catch:{ Exception -> 0x002b }
                r10 = 120000(0x1d4c0, double:5.9288E-319)
                r9 = 73737(0x12009, float:1.03328E-40)
                r12 = 0
                r8.startTimer(r10, r9, r12)     // Catch:{ Exception -> 0x002b }
                goto L_0x0007
            L_0x0101:
                java.lang.String r8 = "AnyoPortHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002b }
                java.lang.String r10 = "report charge request periodically, port: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler r10 = com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.this     // Catch:{ Exception -> 0x002b }
                java.lang.String r10 = r10.port     // Catch:{ Exception -> 0x002b }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002b }
                java.lang.String r10 = ", status: "
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler r10 = com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.this     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.data.bean.type.CHARGE_STATUS r10 = r10.status     // Catch:{ Exception -> 0x002b }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002b }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002b }
                android.util.Log.i(r8, r9)     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler r8 = com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.this     // Catch:{ Exception -> 0x01e4 }
                r8.reportChargeRequest()     // Catch:{ Exception -> 0x01e4 }
            L_0x0130:
                com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler r8 = com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.this     // Catch:{ Exception -> 0x002b }
                com.xcharge.common.utils.HandlerTimer r8 = r8.handlerTimer     // Catch:{ Exception -> 0x002b }
                r10 = 60000(0xea60, double:2.9644E-319)
                r9 = 73744(0x12010, float:1.03337E-40)
                r12 = 0
                r8.startTimer(r10, r9, r12)     // Catch:{ Exception -> 0x002b }
                goto L_0x0007
            L_0x0142:
                com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler r8 = com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.this     // Catch:{ Exception -> 0x01e1 }
                r8.reportHistoryChargeRequest()     // Catch:{ Exception -> 0x01e1 }
            L_0x0147:
                com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler r8 = com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.this     // Catch:{ Exception -> 0x002b }
                com.xcharge.common.utils.HandlerTimer r8 = r8.handlerTimer     // Catch:{ Exception -> 0x002b }
                r10 = 120000(0x1d4c0, double:5.9288E-319)
                r9 = 73745(0x12011, float:1.03339E-40)
                r12 = 0
                r8.startTimer(r10, r9, r12)     // Catch:{ Exception -> 0x002b }
                goto L_0x0007
            L_0x0159:
                java.lang.Object r6 = r14.obj     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.bean.AnyoMessage r6 = (com.xcharge.charger.protocol.anyo.bean.AnyoMessage) r6     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler r8 = com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.this     // Catch:{ Exception -> 0x002b }
                r8.handleRequest(r6)     // Catch:{ Exception -> 0x002b }
                goto L_0x0007
            L_0x0164:
                java.lang.Object r3 = r14.obj     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.session.AnyoRequestSession r3 = (com.xcharge.charger.protocol.anyo.session.AnyoRequestSession) r3     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler r8 = com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.this     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.bean.AnyoMessage r9 = r3.getSendedRequest()     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.bean.AnyoMessage r10 = r3.getResponse()     // Catch:{ Exception -> 0x002b }
                r8.handleResponse(r9, r10)     // Catch:{ Exception -> 0x002b }
                goto L_0x0007
            L_0x0177:
                java.lang.Object r8 = r14.obj     // Catch:{ Exception -> 0x002b }
                r0 = r8
                android.net.Uri r0 = (android.net.Uri) r0     // Catch:{ Exception -> 0x002b }
                r7 = r0
                java.lang.String r8 = "AnyoPortHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002b }
                java.lang.String r10 = "port charge status changed, port: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler r10 = com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.this     // Catch:{ Exception -> 0x002b }
                java.lang.String r10 = r10.port     // Catch:{ Exception -> 0x002b }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002b }
                java.lang.String r10 = ", uri: "
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002b }
                java.lang.String r10 = r7.toString()     // Catch:{ Exception -> 0x002b }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002b }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002b }
                android.util.Log.i(r8, r9)     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler r8 = com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.this     // Catch:{ Exception -> 0x002b }
                r8.handlePortChargeStatusChanged(r7)     // Catch:{ Exception -> 0x002b }
                goto L_0x0007
            L_0x01ac:
                java.lang.Object r8 = r14.obj     // Catch:{ Exception -> 0x002b }
                r0 = r8
                android.net.Uri r0 = (android.net.Uri) r0     // Catch:{ Exception -> 0x002b }
                r7 = r0
                java.lang.String r8 = "AnyoPortHandler.handleMessage"
                java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002b }
                java.lang.String r10 = "port status changed, port: "
                r9.<init>(r10)     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler r10 = com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.this     // Catch:{ Exception -> 0x002b }
                java.lang.String r10 = r10.port     // Catch:{ Exception -> 0x002b }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002b }
                java.lang.String r10 = ", uri: "
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002b }
                java.lang.String r10 = r7.toString()     // Catch:{ Exception -> 0x002b }
                java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ Exception -> 0x002b }
                java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x002b }
                android.util.Log.i(r8, r9)     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler r8 = com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.this     // Catch:{ Exception -> 0x002b }
                r8.handlePortStatusChanged(r7)     // Catch:{ Exception -> 0x002b }
                goto L_0x0007
            L_0x01e1:
                r8 = move-exception
                goto L_0x0147
            L_0x01e4:
                r8 = move-exception
                goto L_0x0130
            L_0x01e7:
                r8 = move-exception
                goto L_0x00ef
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.protocol.anyo.handler.AnyoPortHandler.MsgHandler.handleMessage(android.os.Message):void");
        }
    }

    public void init(Context context2, String port2, AnyoProtocolAgent protocolHandler) {
        this.context = context2;
        this.port = port2;
        this.thread = new HandlerThread("AnyoPortHandler#" + this.port, 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(context2);
        String localPort = AnyoProtocolAgent.getInstance().getLocalPort(this.port);
        this.portStatusObserver = new PortStatusObserver(this.context, localPort, this.handler);
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor("ports/" + localPort), true, this.portStatusObserver);
        this.portChargeStatusObserver = new PortChargeStatusObserver(this.context, localPort, this.handler);
        this.context.getContentResolver().registerContentObserver(ChargeStatusCacheProvider.getInstance().getUriFor("ports/" + localPort), true, this.portChargeStatusObserver);
    }

    public void destroy() {
        this.context.getContentResolver().unregisterContentObserver(this.portChargeStatusObserver);
        this.context.getContentResolver().unregisterContentObserver(this.portStatusObserver);
        this.handlerTimer.destroy();
        this.handler.removeMessages(73729);
        this.handler.removeMessages(73730);
        this.handler.removeMessages(73731);
        this.handler.removeMessages(73732);
        this.handler.removeMessages(73733);
        this.handler.removeMessages(73734);
        this.handler.removeMessages(73735);
        this.handler.removeMessages(73736);
        this.handler.removeMessages(73737);
        this.handler.removeMessages(73744);
        this.handler.removeMessages(73745);
        this.handler.removeMessages(PortChargeStatusObserver.MSG_PORT_CHARGE_STATUS_CHANGE);
        this.handler.removeMessages(PortStatusObserver.MSG_PORT_STATUS_CHANGE);
        this.thread.quit();
    }

    /* access modifiers changed from: private */
    public void clearLoginStatus() {
        this.handlerTimer.stopTimer(73737);
        this.handlerTimer.stopTimer(73745);
        this.handler.removeMessages(73731);
        this.handler.removeMessages(73732);
        this.handler.removeMessages(73733);
        this.handler.removeMessages(73734);
        this.handler.removeMessages(73735);
        this.handler.removeMessages(73736);
        this.anyoPortStatus = ANYO_PORT_STATE.not_login;
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

    public AnyoChargeSession getChargeSession() {
        if (this.chargeSession == null) {
            this.chargeSession = new AnyoChargeSession();
        }
        return this.chargeSession;
    }

    private void clearChargeSession() {
        this.chargeSession = null;
    }

    public boolean hasCharge(String chargeId) {
        return chargeId.equals(getChargeSession().getCharge_id());
    }

    /* access modifiers changed from: private */
    public void handleFailedRequest(AnyoMessage request) {
        switch (request.getHead().getCmdCode()) {
            case 16:
                if (request.getRetrySend() < 2) {
                    Log.w("AnyoPortHandler.handleFailedRequest", "failed to send login request: " + request.toJson());
                    this.handler.sendMessageDelayed(this.handler.obtainMessage(73734, request), 5000);
                    return;
                }
                Log.w("AnyoPortHandler.handleFailedRequest", "failed to login 3 times, diconnect !!!");
                AnyoProtocolAgent.getInstance().disconnect();
                return;
            case 17:
                AnyoDCAPGateway.getInstance().sendMessage(AnyoDCAPGateway.getInstance().obtainMessage(77829, request));
                return;
            case 21:
                this.failHeartBeatCnt++;
                if (this.failHeartBeatCnt >= 3) {
                    Log.w("AnyoPortHandler.handleFailedRequest", "failed to heart beat 3 times, diconnect !!!");
                    AnyoProtocolAgent.getInstance().disconnect();
                    this.failHeartBeatCnt = 0;
                    return;
                }
                Log.w("AnyoPortHandler.handleFailedRequest", "failed to send heart beat request: " + request.toJson());
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: private */
    public void handlePortStatusChanged(Uri uri) {
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(AnyoProtocolAgent.getInstance().getLocalPort(this.port));
        AnyoStatus changedAnyoStatus = new AnyoStatus();
        changedAnyoStatus.setPortPlugin(portStatus.isPlugin());
        changedAnyoStatus.setPortLocked(this.latestAnyoStatus.isPortLocked());
        ErrorCode error = portStatus.getDeviceError();
        if (error.getCode() != 200 && this.latestAnyoStatus.getError().getCode() == 200) {
            changedAnyoStatus.setError(error);
            changedAnyoStatus.setDeviceStatus(HeartBeatRequest.PORT_STATUS_FAULT);
            heartBeatRequest(changedAnyoStatus);
        } else if (error.getCode() == 200 && this.latestAnyoStatus.getError().getCode() != 200) {
            changedAnyoStatus.setError(error);
            changedAnyoStatus.setDeviceStatus(this.latestAnyoStatus.getDeviceStatus());
            heartBeatRequest(changedAnyoStatus);
        }
        checkReportEventRequest(changedAnyoStatus);
        this.latestAnyoStatus.setError(error);
        this.latestAnyoStatus.setPortPlugin(portStatus.isPlugin());
    }

    /* access modifiers changed from: private */
    public void handlePortChargeStatusChanged(Uri uri) {
        AnyoStatus changedAnyoStatus = getChangedAnyoStatus(uri);
        checkReportEventRequest(changedAnyoStatus);
        checkHeartBeatRequestStatus(changedAnyoStatus);
        this.latestAnyoStatus = changedAnyoStatus;
    }

    private AnyoStatus getChangedAnyoStatus(Uri uri) {
        byte anyoChargeStatus;
        double doubleValue;
        PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(AnyoProtocolAgent.getInstance().getLocalPort(this.port));
        Log.d("AnyoPortHandler.getChangedAnyoStatus", "now port status: " + portStatus.toJson());
        CHARGE_STATUS chargeStatus = portStatus.getChargeStatus();
        AnyoChargeSession chargeSession2 = getChargeSession();
        String chargeId = chargeSession2.getCharge_id();
        if (!TextUtils.isEmpty(chargeId) && chargeId.equals(portStatus.getCharge_id()) && (CHARGE_STATUS.CHARGING.equals(chargeStatus) || CHARGE_STATUS.CHARGE_STOP_WAITTING.equals(chargeStatus))) {
            chargeSession2.setChargeStartTime(portStatus.getChargeStartTime());
            chargeSession2.setChargeStopTime(portStatus.getChargeStopTime());
            if (portStatus.getPower() == null) {
                doubleValue = 0.0d;
            } else {
                doubleValue = portStatus.getPower().doubleValue();
            }
            chargeSession2.setPower(doubleValue);
            chargeSession2.setChargeStopCause(portStatus.getChargeStopCause());
        }
        if (!this.status.equals(chargeStatus)) {
            if (chargeStatus.equals(CHARGE_STATUS.CHARGE_START_WAITTING)) {
                Log.i("AnyoPortHandler.getChangedAnyoStatus", "enter wait charge status !!!");
                reportEventRequest((byte) 8);
                AnyoChargeSession chargeSession3 = getChargeSession();
                String chargeId2 = portStatus.getCharge_id();
                ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(chargeId2);
                if (chargeBill != null) {
                    chargeSession3.setCharge_id(chargeId2);
                    chargeSession3.setUser_type(chargeBill.getUser_type());
                    chargeSession3.setUser_code(chargeBill.getUser_code());
                    chargeSession3.setInit_type(chargeBill.getInit_type());
                    chargeSession3.setUser_tc_type(chargeBill.getUser_tc_type());
                    chargeSession3.setUser_tc_value(chargeBill.getUser_tc_value());
                    chargeSession3.setUser_balance(chargeBill.getUser_balance());
                    chargeSession3.setIs_free(chargeBill.getIs_free());
                    chargeSession3.setBinded_user(chargeBill.getBinded_user());
                    chargeSession3.setCharge_platform(chargeBill.getCharge_platform());
                } else {
                    Log.w("AnyoPortHandler.getChangedAnyoStatus", "failed to query info for charge: " + chargeId2);
                }
            } else if (chargeStatus.equals(CHARGE_STATUS.CHARGING)) {
                Log.i("AnyoPortHandler.getChangedAnyoStatus", "enter charging status !!!");
                AnyoChargeSession chargeSession4 = getChargeSession();
                if (TextUtils.isEmpty(chargeSession4.getCharge_id())) {
                    reportEventRequest((byte) 8);
                    String chargeId3 = portStatus.getCharge_id();
                    ChargeBill chargeBill2 = ChargeContentProxy.getInstance().getChargeBill(chargeId3);
                    if (chargeBill2 != null) {
                        chargeSession4.setCharge_id(chargeId3);
                        chargeSession4.setUser_type(chargeBill2.getUser_type());
                        chargeSession4.setUser_code(chargeBill2.getUser_code());
                        chargeSession4.setInit_type(chargeBill2.getInit_type());
                        chargeSession4.setUser_tc_type(chargeBill2.getUser_tc_type());
                        chargeSession4.setUser_tc_value(chargeBill2.getUser_tc_value());
                        chargeSession4.setUser_balance(chargeBill2.getUser_balance());
                        chargeSession4.setIs_free(chargeBill2.getIs_free());
                        chargeSession4.setBinded_user(chargeBill2.getBinded_user());
                        chargeSession4.setCharge_platform(chargeBill2.getCharge_platform());
                    } else {
                        Log.w("AnyoPortHandler.getChangedAnyoStatus", "failed to query info for charge: " + chargeId3);
                    }
                }
                this.handlerTimer.startTimer(60000, 73744, (Object) null);
            } else if (chargeStatus.equals(CHARGE_STATUS.CHARGE_STOP_WAITTING)) {
                Log.i("AnyoPortHandler.getChangedAnyoStatus", "enter charge stopped status !!!");
                this.handlerTimer.stopTimer(73744);
                reportChargeStoppedRequest(false);
            } else if (chargeStatus.equals(CHARGE_STATUS.IDLE)) {
                Log.i("AnyoPortHandler.getChangedAnyoStatus", "enter idle status !!!");
                this.handlerTimer.stopTimer(73744);
                if (!CHARGE_STATUS.CHARGE_STOP_WAITTING.equals(this.status)) {
                    reportChargeStoppedRequest(true);
                }
                ResetChargeRequest resetChargeRequest = chargeSession2.getResetChargeRequest();
                if (resetChargeRequest != null) {
                    AnyoProtocolAgent.getInstance().sendResetChargeResponse(resetChargeRequest, (byte) 0);
                }
                clearChargeSession();
            }
            this.status = chargeStatus;
        }
        AnyoStatus nowAnyoStatus = new AnyoStatus();
        nowAnyoStatus.setError(this.latestAnyoStatus.getError());
        if (this.latestAnyoStatus.getError().getCode() == 200) {
            switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STATUS()[this.status.ordinal()]) {
                case 1:
                    anyoChargeStatus = 0;
                    break;
                case 2:
                    anyoChargeStatus = 17;
                    break;
                case 3:
                    anyoChargeStatus = 6;
                    break;
                case 4:
                    anyoChargeStatus = 8;
                    break;
                default:
                    anyoChargeStatus = 17;
                    break;
            }
            nowAnyoStatus.setDeviceStatus(anyoChargeStatus);
        } else {
            nowAnyoStatus.setDeviceStatus(this.latestAnyoStatus.getDeviceStatus());
        }
        if (LOCK_STATUS.lock.equals(portStatus.getGunLockStatus())) {
            nowAnyoStatus.setPortLocked(true);
        }
        nowAnyoStatus.setPortPlugin(this.latestAnyoStatus.isPortPlugin());
        return nowAnyoStatus;
    }

    private void checkHeartBeatRequestStatus(AnyoStatus changedAnyoStatus) {
        if (this.latestAnyoStatus.getDeviceStatus() != changedAnyoStatus.getDeviceStatus()) {
            heartBeatRequest(changedAnyoStatus);
        }
    }

    private void checkReportEventRequest(AnyoStatus changedAnyoStatus) {
        boolean nowPortPluginStatus = changedAnyoStatus.isPortPlugin();
        if (this.latestAnyoStatus.isPortPlugin() != nowPortPluginStatus) {
            reportEventRequest(nowPortPluginStatus ? (byte) 3 : 9);
        }
        boolean nowPortLockedStatus = changedAnyoStatus.isPortLocked();
        if (this.latestAnyoStatus.isPortLocked() != nowPortLockedStatus) {
            reportEventRequest(nowPortLockedStatus ? (byte) 7 : 8);
        }
    }

    /* access modifiers changed from: private */
    public void handleRequest(AnyoMessage request) {
        if (!this.anyoPortStatus.equals(ANYO_PORT_STATE.logined)) {
            Log.w("AnyoPortHandler.handleRequest", "port: " + this.port + " is not login now, reveived request message is ignored !!! server request: " + request.toJson());
            return;
        }
        switch (request.getHead().getCmdCode()) {
            case Byte.MIN_VALUE:
                Log.i("AnyoPortHandler.handleRequest", "receive anyo reset charge request:" + request.toJson());
                AnyoDCAPGateway.getInstance().sendMessage(AnyoDCAPGateway.getInstance().obtainMessage(77827, request));
                return;
            case 60:
                Log.i("AnyoPortHandler.handleRequest", "receive anyo start charge request:" + request.toJson());
                AnyoDCAPGateway.getInstance().sendMessage(AnyoDCAPGateway.getInstance().obtainMessage(77827, request));
                return;
            case 61:
                Log.i("AnyoPortHandler.handleRequest", "receive anyo stop charge request:" + request.toJson());
                AnyoDCAPGateway.getInstance().sendMessage(AnyoDCAPGateway.getInstance().obtainMessage(77827, request));
                return;
            case 81:
                Log.i("AnyoPortHandler.handleRequest", "receive anyo unlock port request:" + request.toJson());
                AnyoDCAPGateway.getInstance().sendMessage(AnyoDCAPGateway.getInstance().obtainMessage(77827, request));
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: private */
    public void handleResponse(AnyoMessage request, AnyoMessage response) {
        switch (response.getHead().getCmdCode()) {
            case 16:
                Log.i("AnyoPortHandler.handleResponse", "receive anyo login response:" + response.toJson());
                handleLoginResponse(request, response);
                return;
            case 17:
                Log.i("AnyoPortHandler.handleResponse", "receive anyo auth response:" + response.toJson());
                AnyoDCAPGateway.getInstance().sendMessage(AnyoDCAPGateway.getInstance().obtainMessage(77828, response));
                return;
            case 20:
                Log.i("AnyoPortHandler.handleResponse", "receive anyo report history bill response:" + response.toJson());
                handleReportHistoryBillResponse(request, response);
                return;
            case 21:
                Log.i("AnyoPortHandler.handleResponse", "receive anyo heart beat response:" + response.toJson());
                if (this.okHeartBeatCnt == 0) {
                    AnyoProtocolAgent.getInstance().reportNetworkInfo();
                }
                this.failHeartBeatCnt = 0;
                this.okHeartBeatCnt++;
                return;
            case 24:
                Log.i("AnyoPortHandler.handleResponse", "receive anyo report charge stopped response:" + response.toJson());
                handleReportChargeStoppedResponse(request, response);
                return;
            default:
                return;
        }
    }

    private void sendMessage(AnyoMessage msg) {
        if (this.anyoPortStatus.equals(ANYO_PORT_STATE.logined) || msg.getHead().getCmdCode() == 16) {
            AnyoProtocolAgent.getInstance().sendMessage(msg);
            return;
        }
        Log.w("AnyoPortHandler.sendMessage", "port: " + this.port + " is not login now, send message is forbidened !!!");
        if (msg.getHead().getStartCode() == 104) {
            this.handler.obtainMessage(73732, msg).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    public void loginRequest() {
        try {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setPileNo(AnyoProtocolAgent.getInstance().getPileNo());
            loginRequest.setPileType(AnyoProtocolAgent.getInstance().getPileType());
            loginRequest.setProvider(AnyoProtocolAgent.provider);
            loginRequest.setMagicNum(AnyoProtocolAgent.magicNumber);
            String[] protocolVersionSplit = AnyoProtocolAgent.protocolVersion.split("\\.");
            String[] softwareVersionSplit = AnyoProtocolAgent.softwareVersion.split("\\.");
            loginRequest.setPileInfo(String.valueOf(new String(new byte[]{(byte) (Integer.parseInt(protocolVersionSplit[1]) & MotionEventCompat.ACTION_MASK), (byte) (Integer.parseInt(protocolVersionSplit[0]) & MotionEventCompat.ACTION_MASK), (byte) (Integer.parseInt(softwareVersionSplit[0]) & MotionEventCompat.ACTION_MASK), (byte) (Integer.parseInt(softwareVersionSplit[1]) & MotionEventCompat.ACTION_MASK), (byte) (Integer.parseInt(softwareVersionSplit[2]) & MotionEventCompat.ACTION_MASK)}, Charset.forName(CharEncoding.UTF_8))) + AnyoProtocolAgent.firewareType);
            loginRequest.setRand(AnyoProtocolAgent.getInstance().getCheckSumRand());
            AnyoHead head = AnyoProtocolAgent.getInstance().createRequestHead((byte) 16);
            head.setBodyLength(loginRequest.bodyToBytes().length);
            loginRequest.setHead(head);
            loginRequest.setPort(this.port);
            head.setCheckSum(loginRequest.calcCheckSum(AnyoProtocolAgent.getInstance().getCheckSumRand()));
            sendMessage((AnyoMessage) loginRequest);
            Log.i("AnyoPortHandler.loginRequest", "send login request: " + loginRequest.toJson());
        } catch (Exception e) {
            Log.w("AnyoPortHandler.loginRequest", Log.getStackTraceString(e));
        }
    }

    private void handleLoginResponse(AnyoMessage request, AnyoMessage response) {
        byte statusCode = response.getHead().getStatusCode();
        LoginResponse resp = (LoginResponse) response;
        if (statusCode == 0) {
            AnyoProtocolAgent.getInstance().setPeerCheckSumRand(resp.getRand());
            long peerTimeStamp = resp.getTimestamp() * 1000;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Log.i("AnyoPortHandler.handleLoginResponse", "peer timestamp: " + TimeUtils.getISO8601Format(peerTimeStamp, "+00:00") + ", local timestamp: " + sdf.format(new Date(System.currentTimeMillis())));
            SystemClock.setCurrentTimeMillis(peerTimeStamp);
            LogUtils.syslog("synch cloud time: " + TimeUtils.getISO8601Format(peerTimeStamp, "+00:00"));
            Log.i("AnyoProtocolAgent.handleLoginResponse", "cloud time setted, now local timestamp: " + sdf.format(new Date(System.currentTimeMillis())));
            ChargeStatusCacheProvider.getInstance().updateCloudTimeSynch(true);
            this.anyoPortStatus = ANYO_PORT_STATE.logined;
            LogUtils.cloudlog("anyo cloud login !!!");
            String localPort = AnyoProtocolAgent.getInstance().getLocalPort(this.port);
            Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(localPort);
            AnyoStatus initAnyoStatus = new AnyoStatus();
            if (portStatus.isPlugin()) {
                initAnyoStatus.setPortPlugin(true);
            } else {
                initAnyoStatus.setPortPlugin(this.latestAnyoStatus.isPortPlugin());
            }
            ErrorCode error = portStatus.getDeviceError();
            if (error.getCode() != 200) {
                initAnyoStatus.setError(error);
                initAnyoStatus.setDeviceStatus(HeartBeatRequest.PORT_STATUS_FAULT);
            } else {
                initAnyoStatus.setError(this.latestAnyoStatus.getError());
                initAnyoStatus.setDeviceStatus(this.latestAnyoStatus.getDeviceStatus());
            }
            if (LOCK_STATUS.lock.equals(ChargeStatusCacheProvider.getInstance().getPortLockStatus(localPort))) {
                initAnyoStatus.setPortLocked(true);
            } else {
                initAnyoStatus.setPortLocked(this.latestAnyoStatus.isPortLocked());
            }
            this.okHeartBeatCnt = 0;
            heartBeatRequest(initAnyoStatus);
            checkReportEventRequest(initAnyoStatus);
            this.latestAnyoStatus = initAnyoStatus;
            if (!TextUtils.isEmpty(getChargeSession().getCharge_id()) && CHARGE_STATUS.CHARGING.equals(this.status)) {
                reportChargeRequest();
            }
            this.handlerTimer.startTimer(120000, 73737, (Object) null);
            this.handlerTimer.startTimer(120000, 73745, (Object) null);
            request.setRetrySend(0);
            return;
        }
        Log.e("AnyoPortHandler.handleLoginResponse", "failed to login, error: " + statusCode);
        if (request.getRetrySend() < 2) {
            this.handler.sendMessageDelayed(this.handler.obtainMessage(73734, request), 5000);
            return;
        }
        Log.w("AnyoPortHandler.handleLoginResponse", "failed to login 3 times, diconnect !!!");
        AnyoProtocolAgent.getInstance().disconnect();
    }

    /* access modifiers changed from: private */
    public void resendRequest(AnyoMessage request) {
        Log.w("AnyoPortHandler.resendRequest", "resend anyo request: " + request.toJson());
        try {
            AnyoHead head = AnyoProtocolAgent.getInstance().createRequestHead(request.getHead().getCmdCode());
            head.setBodyLength(request.bodyToBytes().length);
            request.setHead(head);
            head.setCheckSum(request.calcCheckSum(AnyoProtocolAgent.getInstance().getCheckSumRand()));
            sendMessage(request);
            request.setRetrySend(request.getRetrySend() + 1);
        } catch (Exception e) {
            Log.w("AnyoPortHandler.resendRequest", Log.getStackTraceString(e));
        }
    }

    /* access modifiers changed from: private */
    public void heartBeatRequest(AnyoStatus anyoStatus) {
        if (anyoStatus == null) {
            try {
                anyoStatus = this.latestAnyoStatus;
            } catch (Exception e) {
                Log.w("AnyoPortHandler.heartBeatRequest", Log.getStackTraceString(e));
                return;
            }
        }
        HeartBeatRequest heartBeatRequest = new HeartBeatRequest();
        heartBeatRequest.setPortStatus(anyoStatus.getDeviceStatus());
        heartBeatRequest.setPortNo(AnyoProtocolAgent.getInstance().getPortByPileType(this.port));
        AnyoHead head = AnyoProtocolAgent.getInstance().createRequestHead((byte) 21);
        head.setBodyLength(heartBeatRequest.bodyToBytes().length);
        heartBeatRequest.setHead(head);
        heartBeatRequest.setPort(this.port);
        head.setCheckSum(heartBeatRequest.calcCheckSum(AnyoProtocolAgent.getInstance().getCheckSumRand()));
        sendMessage((AnyoMessage) heartBeatRequest);
        Log.i("AnyoPortHandler.heartBeatRequest", "send heart beat request: " + heartBeatRequest.toJson());
    }

    private void reportEventRequest(byte deviceEvent) {
        try {
            ReportEventRequest reportEventRequest = new ReportEventRequest();
            reportEventRequest.setEvent(deviceEvent);
            reportEventRequest.setPortNo(AnyoProtocolAgent.getInstance().getPortByPileType(this.port));
            AnyoHead head = AnyoProtocolAgent.getInstance().createRequestHead(AnyoMessage.CMD_REPORT_EVENT);
            head.setBodyLength(reportEventRequest.bodyToBytes().length);
            reportEventRequest.setHead(head);
            reportEventRequest.setPort(this.port);
            head.setCheckSum(reportEventRequest.calcCheckSum(AnyoProtocolAgent.getInstance().getCheckSumRand()));
            sendMessage((AnyoMessage) reportEventRequest);
            Log.i("AnyoPortHandler.reportEventRequest", "send report event request: " + reportEventRequest.toJson());
        } catch (Exception e) {
            Log.w("AnyoPortHandler.reportEventRequest", Log.getStackTraceString(e));
        }
    }

    /* access modifiers changed from: private */
    public void reportChargeRequest() {
        long chargeTime;
        try {
            PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(AnyoProtocolAgent.getInstance().getLocalPort(this.port));
            int amp = new BigDecimal(portStatus.getAmps().get(0).doubleValue() * 100.0d).setScale(0, 4).intValue();
            int volt = new BigDecimal(portStatus.getVolts().get(0).doubleValue() * 100.0d).setScale(0, 4).intValue();
            int power = new BigDecimal(portStatus.getPower().doubleValue() * 100.0d).setScale(0, 4).intValue();
            long chargeStopTime = portStatus.getChargeStopTime();
            if (chargeStopTime > 0) {
                chargeTime = new BigDecimal((chargeStopTime - portStatus.getChargeStartTime()) / 1000).setScale(0, 4).longValue();
            } else {
                chargeTime = new BigDecimal((System.currentTimeMillis() - portStatus.getChargeStartTime()) / 1000).setScale(0, 4).longValue();
            }
            ReportChargeRequest reportChargeRequest = new ReportChargeRequest();
            reportChargeRequest.setAmp(amp);
            reportChargeRequest.setVolt(volt);
            reportChargeRequest.setPower(power);
            reportChargeRequest.setChargeTime(chargeTime);
            reportChargeRequest.setPortNo(AnyoProtocolAgent.getInstance().getPortByPileType(this.port));
            AnyoHead head = AnyoProtocolAgent.getInstance().createRequestHead((byte) 18);
            head.setBodyLength(reportChargeRequest.bodyToBytes().length);
            reportChargeRequest.setHead(head);
            reportChargeRequest.setPort(this.port);
            head.setCheckSum(reportChargeRequest.calcCheckSum(AnyoProtocolAgent.getInstance().getCheckSumRand()));
            sendMessage((AnyoMessage) reportChargeRequest);
            Log.i("AnyoPortHandler.reportChargeRequest", "send report charge request: " + reportChargeRequest.toJson());
        } catch (Exception e) {
            Log.w("AnyoPortHandler.reportChargeRequest", Log.getStackTraceString(e));
        }
    }

    private void reportChargeStoppedRequest(boolean isForceEnded) {
        try {
            String chargeId = getChargeSession().getCharge_id();
            ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(chargeId);
            if (chargeBill != null) {
                Log.d("AnyoPortHandler.reportChargeStoppedRequest", "charge bill: " + chargeBill.toJson());
                int billId = Integer.parseInt(chargeId);
                int power = new BigDecimal(chargeBill.getTotal_power() * 100.0d).setScale(0, 4).intValue();
                long startTime = new BigDecimal(chargeBill.getStart_time() / 1000).setScale(0, 4).longValue();
                long stopTime = new BigDecimal(chargeBill.getStop_time() / 1000).setScale(0, 4).longValue();
                long chargeTime = stopTime - startTime;
                byte chargeStopCause = 0;
                CHARGE_STOP_CAUSE csc = chargeBill.getStop_cause();
                if (csc != null) {
                    switch (m21x26790b25()[csc.ordinal()]) {
                        case 2:
                        case 4:
                        case 5:
                            chargeStopCause = 0;
                            break;
                        case 3:
                            chargeStopCause = 2;
                            break;
                        case 6:
                            chargeStopCause = 1;
                            break;
                        case 7:
                            chargeStopCause = 5;
                            break;
                        case 8:
                            chargeStopCause = 3;
                            break;
                        case PortRuntimeData.STATUS_EX_12:
                            chargeStopCause = 4;
                            break;
                    }
                }
                byte userIdType = 2;
                byte cardBalanceFlag = 0;
                if (!CHARGE_INIT_TYPE.nfc.equals(chargeBill.getInit_type())) {
                    userIdType = 1;
                } else {
                    cardBalanceFlag = 1;
                }
                String userId = chargeBill.getUser_code();
                int userIdLength = 4;
                if (userIdType == 2) {
                    userIdLength = userId.getBytes(Charset.forName(CharEncoding.UTF_8)).length;
                }
                ReportChargeStoppedRequest reportChargeStoppedRequest = new ReportChargeStoppedRequest();
                reportChargeStoppedRequest.setBillId(billId);
                reportChargeStoppedRequest.setPower(power);
                reportChargeStoppedRequest.setStartTime(startTime);
                reportChargeStoppedRequest.setStopTime(stopTime);
                reportChargeStoppedRequest.setChargeTime(chargeTime);
                reportChargeStoppedRequest.setCardBalanceFlag(cardBalanceFlag);
                reportChargeStoppedRequest.setChargeStopCause(chargeStopCause);
                reportChargeStoppedRequest.setUserIdType(userIdType);
                reportChargeStoppedRequest.setUserId(userId);
                reportChargeStoppedRequest.setUserIdLength(userIdLength);
                reportChargeStoppedRequest.setPortNo(AnyoProtocolAgent.getInstance().getPortByPileType(this.port));
                AnyoHead head = AnyoProtocolAgent.getInstance().createRequestHead(AnyoMessage.CMD_REPORT_CHARGE_STOPPED);
                head.setBodyLength(reportChargeStoppedRequest.bodyToBytes().length);
                reportChargeStoppedRequest.setHead(head);
                reportChargeStoppedRequest.setPort(this.port);
                head.setCheckSum(reportChargeStoppedRequest.calcCheckSum(AnyoProtocolAgent.getInstance().getCheckSumRand()));
                sendMessage((AnyoMessage) reportChargeStoppedRequest);
                Log.i("AnyoPortHandler.reportChargeStoppedRequest", "send report charge stopped request: " + reportChargeStoppedRequest.toJson());
                return;
            }
            Log.w("AnyoPortHandler.reportChargeStoppedRequest", "failed to get info for charge: " + chargeId);
        } catch (Exception e) {
            Log.w("AnyoPortHandler.reportChargeStoppedRequest", Log.getStackTraceString(e));
        }
    }

    /* access modifiers changed from: private */
    public void reportHistoryChargeRequest() {
        if (this.latestAnyoStatus.getDeviceStatus() != 0) {
            Log.w("AnyoPortHandler.reportHistoryChargeRequest", "not anyo idle status, not report history charge info !!!");
            return;
        }
        try {
            ArrayList<ChargeBill> chargeBills = ChargeContentProxy.getInstance().getUnReportedBills(new String[]{CHARGE_USER_TYPE.anyo.toString(), String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.anyo1}, AnyoProtocolAgent.getInstance().getLocalPort(this.port));
            if (chargeBills != null && chargeBills.size() != 0) {
                ChargeBill chargeBill = chargeBills.get(0);
                Log.d("AnyoPortHandler.reportHistoryChargeRequest", "charge bill: " + chargeBill.toJson());
                int billId = Integer.parseInt(chargeBill.getCharge_id());
                int power = new BigDecimal(chargeBill.getTotal_power() * 100.0d).setScale(0, 4).intValue();
                long startTime = new BigDecimal(chargeBill.getStart_time() / 1000).setScale(0, 4).longValue();
                long stopTime = new BigDecimal(chargeBill.getStop_time() / 1000).setScale(0, 4).longValue();
                long chargeTime = stopTime - startTime;
                byte chargeStopCause = 0;
                CHARGE_STOP_CAUSE csc = chargeBill.getStop_cause();
                if (csc != null) {
                    switch (m21x26790b25()[csc.ordinal()]) {
                        case 2:
                        case 4:
                        case 5:
                            chargeStopCause = 0;
                            break;
                        case 3:
                            chargeStopCause = 2;
                            break;
                        case 6:
                            chargeStopCause = 1;
                            break;
                        case 7:
                            chargeStopCause = 5;
                            break;
                        case 8:
                            chargeStopCause = 3;
                            break;
                        case PortRuntimeData.STATUS_EX_12:
                            chargeStopCause = 4;
                            break;
                    }
                }
                byte userIdType = 2;
                byte cardBalanceFlag = 0;
                if (!CHARGE_INIT_TYPE.nfc.equals(chargeBill.getInit_type())) {
                    userIdType = 1;
                } else {
                    cardBalanceFlag = 1;
                }
                String userId = chargeBill.getUser_code();
                int userIdLength = 4;
                if (userIdType == 2) {
                    userIdLength = userId.getBytes(Charset.forName(CharEncoding.UTF_8)).length;
                }
                ReportHistoryBillRequest reportHistoryBillRequest = new ReportHistoryBillRequest();
                reportHistoryBillRequest.setBillId(billId);
                reportHistoryBillRequest.setPower(power);
                reportHistoryBillRequest.setStartTime(startTime);
                reportHistoryBillRequest.setStopTime(stopTime);
                reportHistoryBillRequest.setChargeTime(chargeTime);
                reportHistoryBillRequest.setCardBalanceFlag(cardBalanceFlag);
                reportHistoryBillRequest.setChargeStopCause(chargeStopCause);
                reportHistoryBillRequest.setUserIdType(userIdType);
                reportHistoryBillRequest.setUserId(userId);
                reportHistoryBillRequest.setUserIdLength(userIdLength);
                reportHistoryBillRequest.setPortNo(AnyoProtocolAgent.getInstance().getPortByPileType(this.port));
                AnyoHead head = AnyoProtocolAgent.getInstance().createRequestHead((byte) 20);
                head.setBodyLength(reportHistoryBillRequest.bodyToBytes().length);
                reportHistoryBillRequest.setHead(head);
                reportHistoryBillRequest.setPort(this.port);
                head.setCheckSum(reportHistoryBillRequest.calcCheckSum(AnyoProtocolAgent.getInstance().getCheckSumRand()));
                sendMessage((AnyoMessage) reportHistoryBillRequest);
                Log.i("AnyoPortHandler.reportHistoryChargeRequest", "send report history charge request: " + reportHistoryBillRequest.toJson());
            }
        } catch (Exception e) {
            Log.w("AnyoPortHandler.reportHistoryChargeRequest", Log.getStackTraceString(e));
        }
    }

    private void handleReportChargeStoppedResponse(AnyoMessage request, AnyoMessage response) {
        byte statusCode = response.getHead().getStatusCode();
        ReportChargeStoppedResponse reportChargeStoppedResponse = (ReportChargeStoppedResponse) response;
        if (statusCode == 0) {
            String chargeId = String.valueOf(((ReportChargeStoppedRequest) request).getBillId());
            Log.d("AnyoPortHandler.handleReportChargeStoppedResponse", "success to report charge stopped bill: " + chargeId);
            ChargeContentProxy.getInstance().setReportedFlag(chargeId);
            return;
        }
        Log.w("AnyoPortHandler.handleReportChargeStoppedResponse", "failed to report charge stopped bill, error: " + statusCode);
    }

    private void handleReportHistoryBillResponse(AnyoMessage request, AnyoMessage response) {
        byte statusCode = response.getHead().getStatusCode();
        ReportHistoryBillRequest req = (ReportHistoryBillRequest) request;
        ReportHistoryBillResponse reportHistoryBillResponse = (ReportHistoryBillResponse) response;
        if (statusCode == 0) {
            String chargeId = String.valueOf(req.getBillId());
            Log.d("AnyoPortHandler.handleReportChargeStoppedResponse", "success to report history bill: " + chargeId);
            ChargeContentProxy.getInstance().setReportedFlag(chargeId);
            req.setRetrySend(0);
            return;
        }
        Log.w("AnyoPortHandler.handleReportChargeStoppedResponse", "failed to report history bill, error: " + statusCode);
        if (req.getRetrySend() == 0) {
            this.handler.sendMessageDelayed(this.handler.obtainMessage(73734, req), 5000);
        }
    }

    public AnyoMessage authRequest(String cardNo) {
        try {
            AuthRequest authRequest = new AuthRequest();
            authRequest.setCardNo(cardNo);
            authRequest.setCardNoLength(cardNo.length());
            AnyoHead head = AnyoProtocolAgent.getInstance().createRequestHead((byte) 17);
            head.setBodyLength(authRequest.bodyToBytes().length);
            authRequest.setHead(head);
            authRequest.setPort(this.port);
            head.setCheckSum(authRequest.calcCheckSum(AnyoProtocolAgent.getInstance().getCheckSumRand()));
            sendMessage((AnyoMessage) authRequest);
            Log.i("AnyoPortHandler.authRequest", "send auth request: " + authRequest.toJson());
            return authRequest;
        } catch (Exception e) {
            Log.w("AnyoPortHandler.authRequest", Log.getStackTraceString(e));
            return null;
        }
    }

    public boolean unlockPortResponse(UnlockPortRequest request, byte statusCode) {
        try {
            UnlockPortResponse response = new UnlockPortResponse();
            response.setPortNo(request.getPortNo());
            AnyoHead head = AnyoProtocolAgent.getInstance().createResponseHead(request.getHead());
            head.setStatusCode(statusCode);
            head.setBodyLength(response.bodyToBytes().length);
            response.setHead(head);
            head.setCheckSum(response.calcCheckSum(AnyoProtocolAgent.getInstance().getCheckSumRand()));
            sendMessage((AnyoMessage) response);
            Log.i("AnyoPortHandler.unlockPortResponse", "send unlock port respose: " + response.toJson());
            return true;
        } catch (Exception e) {
            Log.w("AnyoPortHandler.unlockPortResponse", Log.getStackTraceString(e));
            return false;
        }
    }

    public boolean startChargeResponse(StartChargeRequest request, byte statusCode) {
        try {
            StartChargeResponse response = new StartChargeResponse();
            AnyoHead head = AnyoProtocolAgent.getInstance().createResponseHead(request.getHead());
            head.setStatusCode(statusCode);
            head.setBodyLength(response.bodyToBytes().length);
            response.setHead(head);
            head.setCheckSum(response.calcCheckSum(AnyoProtocolAgent.getInstance().getCheckSumRand()));
            sendMessage((AnyoMessage) response);
            Log.i("AnyoPortHandler.startChargeResponse", "send start charge respose: " + response.toJson());
            return true;
        } catch (Exception e) {
            Log.w("AnyoPortHandler.startChargeResponse", Log.getStackTraceString(e));
            return false;
        }
    }

    public boolean stopChargeResponse(StopChargeRequest request, byte statusCode) {
        try {
            StopChargeResponse response = new StopChargeResponse();
            AnyoHead head = AnyoProtocolAgent.getInstance().createResponseHead(request.getHead());
            head.setStatusCode(statusCode);
            head.setBodyLength(response.bodyToBytes().length);
            response.setHead(head);
            head.setCheckSum(response.calcCheckSum(AnyoProtocolAgent.getInstance().getCheckSumRand()));
            sendMessage((AnyoMessage) response);
            Log.i("AnyoPortHandler.stopChargeResponse", "send stop charge respose: " + response.toJson());
            return true;
        } catch (Exception e) {
            Log.w("AnyoPortHandler.stopChargeResponse", Log.getStackTraceString(e));
            return false;
        }
    }

    public boolean resetChargeResponse(ResetChargeRequest request, byte statusCode) {
        try {
            ResetChargeResponse response = new ResetChargeResponse();
            AnyoHead head = AnyoProtocolAgent.getInstance().createResponseHead(request.getHead());
            head.setStatusCode(statusCode);
            head.setBodyLength(response.bodyToBytes().length);
            response.setHead(head);
            head.setCheckSum(response.calcCheckSum(AnyoProtocolAgent.getInstance().getCheckSumRand()));
            sendMessage((AnyoMessage) response);
            Log.i("AnyoPortHandler.resetChargeResponse", "send reset charge respose: " + response.toJson());
            return true;
        } catch (Exception e) {
            Log.w("AnyoPortHandler.resetChargeResponse", Log.getStackTraceString(e));
            return false;
        }
    }
}
