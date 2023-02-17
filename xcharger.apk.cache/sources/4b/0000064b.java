package com.xcharge.charger.protocol.anyo.handler;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.util.TimeUtils;
import android.support.v4.view.MotionEventCompat;
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
import com.xcharge.charger.device.c2.bean.PortRuntimeData;
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
import com.xcharge.charger.protocol.anyo.session.AnyoRequestSession;
import com.xcharge.charger.protocol.anyo.type.ANYO_PORT_STATE;
import com.xcharge.common.utils.HandlerTimer;
import com.xcharge.common.utils.LogUtils;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.apache.commons.lang3.CharEncoding;

/* loaded from: classes.dex */
public class AnyoPortHandler {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STATUS = null;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE = null;
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
    private HandlerThread thread = null;
    private MsgHandler handler = null;
    private HandlerTimer handlerTimer = null;
    private Context context = null;
    private String port = null;
    private ANYO_PORT_STATE anyoPortStatus = ANYO_PORT_STATE.not_login;
    private int failHeartBeatCnt = 0;
    private long okHeartBeatCnt = 0;
    private CHARGE_STATUS status = CHARGE_STATUS.IDLE;
    private AnyoChargeSession chargeSession = null;
    private PortStatusObserver portStatusObserver = null;
    private PortChargeStatusObserver portChargeStatusObserver = null;
    private AnyoStatus latestAnyoStatus = new AnyoStatus();

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STATUS() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STATUS;
        if (iArr == null) {
            iArr = new int[CHARGE_STATUS.valuesCustom().length];
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

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE;
        if (iArr == null) {
            iArr = new int[CHARGE_STOP_CAUSE.valuesCustom().length];
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
            $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE = iArr;
        }
        return iArr;
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
                    case 73729:
                        Log.i("AnyoPortHandler.handleMessage", "init login !!! port: " + AnyoPortHandler.this.port);
                        AnyoPortHandler.this.loginRequest();
                        break;
                    case 73730:
                        Log.i("AnyoPortHandler.handleMessage", "logout !!! port: " + AnyoPortHandler.this.port);
                        AnyoPortHandler.this.clearLoginStatus();
                        break;
                    case 73731:
                        AnyoMessage anyoMessage = (AnyoMessage) msg.obj;
                        break;
                    case 73732:
                        AnyoPortHandler.this.handleFailedRequest((AnyoMessage) msg.obj);
                        break;
                    case 73733:
                        AnyoMessage anyoMessage2 = (AnyoMessage) msg.obj;
                        Log.w("AnyoPortHandler.handleMessage", "send anyo request timeout: " + anyoMessage2.toJson());
                        AnyoPortHandler.this.handleFailedRequest(anyoMessage2);
                        break;
                    case 73734:
                        AnyoMessage req = (AnyoMessage) msg.obj;
                        AnyoPortHandler.this.resendRequest(req);
                        break;
                    case 73735:
                        AnyoMessage request = (AnyoMessage) msg.obj;
                        AnyoPortHandler.this.handleRequest(request);
                        break;
                    case 73736:
                        AnyoRequestSession anyoRequestSession = (AnyoRequestSession) msg.obj;
                        AnyoPortHandler.this.handleResponse(anyoRequestSession.getSendedRequest(), anyoRequestSession.getResponse());
                        break;
                    case 73737:
                        Log.d("AnyoPortHandler.handleMessage", "send anyo heart beat periodically, port: " + AnyoPortHandler.this.port + ", status: " + AnyoPortHandler.this.status);
                        try {
                            AnyoPortHandler.this.heartBeatRequest(null);
                        } catch (Exception e) {
                        }
                        AnyoPortHandler.this.handlerTimer.startTimer(120000L, 73737, null);
                        break;
                    case 73744:
                        Log.i("AnyoPortHandler.handleMessage", "report charge request periodically, port: " + AnyoPortHandler.this.port + ", status: " + AnyoPortHandler.this.status);
                        try {
                            AnyoPortHandler.this.reportChargeRequest();
                        } catch (Exception e2) {
                        }
                        AnyoPortHandler.this.handlerTimer.startTimer(60000L, 73744, null);
                        break;
                    case 73745:
                        try {
                            AnyoPortHandler.this.reportHistoryChargeRequest();
                        } catch (Exception e3) {
                        }
                        AnyoPortHandler.this.handlerTimer.startTimer(120000L, 73745, null);
                        break;
                    case PortChargeStatusObserver.MSG_PORT_CHARGE_STATUS_CHANGE /* 131073 */:
                        Uri uri = (Uri) msg.obj;
                        Log.i("AnyoPortHandler.handleMessage", "port charge status changed, port: " + AnyoPortHandler.this.port + ", uri: " + uri.toString());
                        AnyoPortHandler.this.handlePortChargeStatusChanged(uri);
                        break;
                    case PortStatusObserver.MSG_PORT_STATUS_CHANGE /* 139265 */:
                        Uri uri2 = (Uri) msg.obj;
                        Log.i("AnyoPortHandler.handleMessage", "port status changed, port: " + AnyoPortHandler.this.port + ", uri: " + uri2.toString());
                        AnyoPortHandler.this.handlePortStatusChanged(uri2);
                        break;
                }
            } catch (Exception e4) {
                Log.e("AnyoPortHandler.handleMessage", "except: " + Log.getStackTraceString(e4));
                LogUtils.syslog("AnyoPortHandler handleMessage exception: " + Log.getStackTraceString(e4));
            }
            super.handleMessage(msg);
        }
    }

    public void init(Context context, String port, AnyoProtocolAgent protocolHandler) {
        this.context = context;
        this.port = port;
        this.thread = new HandlerThread("AnyoPortHandler#" + this.port, 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(context);
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

    /* JADX INFO: Access modifiers changed from: private */
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
        String charge = getChargeSession().getCharge_id();
        return chargeId.equals(charge);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFailedRequest(AnyoMessage request) {
        byte cmd = request.getHead().getCmdCode();
        switch (cmd) {
            case 16:
                if (request.getRetrySend() < 2) {
                    Log.w("AnyoPortHandler.handleFailedRequest", "failed to send login request: " + request.toJson());
                    this.handler.sendMessageDelayed(this.handler.obtainMessage(73734, request), 5000L);
                    return;
                }
                Log.w("AnyoPortHandler.handleFailedRequest", "failed to login 3 times, diconnect !!!");
                AnyoProtocolAgent.getInstance().disconnect();
                return;
            case 17:
                AnyoDCAPGateway.getInstance().sendMessage(AnyoDCAPGateway.getInstance().obtainMessage(77829, request));
                return;
            case 18:
            case TimeUtils.HUNDRED_DAY_FIELD_LEN /* 19 */:
            case 20:
            default:
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
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePortStatusChanged(Uri uri) {
        String localPort = AnyoProtocolAgent.getInstance().getLocalPort(this.port);
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(localPort);
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

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePortChargeStatusChanged(Uri uri) {
        AnyoStatus changedAnyoStatus = getChangedAnyoStatus(uri);
        checkReportEventRequest(changedAnyoStatus);
        checkHeartBeatRequestStatus(changedAnyoStatus);
        this.latestAnyoStatus = changedAnyoStatus;
    }

    private AnyoStatus getChangedAnyoStatus(Uri uri) {
        byte anyoChargeStatus;
        String localPort = AnyoProtocolAgent.getInstance().getLocalPort(this.port);
        PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(localPort);
        Log.d("AnyoPortHandler.getChangedAnyoStatus", "now port status: " + portStatus.toJson());
        CHARGE_STATUS chargeStatus = portStatus.getChargeStatus();
        AnyoChargeSession chargeSession = getChargeSession();
        String chargeId = chargeSession.getCharge_id();
        if (!TextUtils.isEmpty(chargeId) && chargeId.equals(portStatus.getCharge_id()) && (CHARGE_STATUS.CHARGING.equals(chargeStatus) || CHARGE_STATUS.CHARGE_STOP_WAITTING.equals(chargeStatus))) {
            chargeSession.setChargeStartTime(portStatus.getChargeStartTime());
            chargeSession.setChargeStopTime(portStatus.getChargeStopTime());
            chargeSession.setPower(portStatus.getPower() == null ? 0.0d : portStatus.getPower().doubleValue());
            chargeSession.setChargeStopCause(portStatus.getChargeStopCause());
        }
        if (!this.status.equals(chargeStatus)) {
            if (chargeStatus.equals(CHARGE_STATUS.CHARGE_START_WAITTING)) {
                Log.i("AnyoPortHandler.getChangedAnyoStatus", "enter wait charge status !!!");
                reportEventRequest((byte) 8);
                AnyoChargeSession chargeSession2 = getChargeSession();
                String chargeId2 = portStatus.getCharge_id();
                ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(chargeId2);
                if (chargeBill != null) {
                    chargeSession2.setCharge_id(chargeId2);
                    chargeSession2.setUser_type(chargeBill.getUser_type());
                    chargeSession2.setUser_code(chargeBill.getUser_code());
                    chargeSession2.setInit_type(chargeBill.getInit_type());
                    chargeSession2.setUser_tc_type(chargeBill.getUser_tc_type());
                    chargeSession2.setUser_tc_value(chargeBill.getUser_tc_value());
                    chargeSession2.setUser_balance(chargeBill.getUser_balance());
                    chargeSession2.setIs_free(chargeBill.getIs_free());
                    chargeSession2.setBinded_user(chargeBill.getBinded_user());
                    chargeSession2.setCharge_platform(chargeBill.getCharge_platform());
                } else {
                    Log.w("AnyoPortHandler.getChangedAnyoStatus", "failed to query info for charge: " + chargeId2);
                }
            } else if (chargeStatus.equals(CHARGE_STATUS.CHARGING)) {
                Log.i("AnyoPortHandler.getChangedAnyoStatus", "enter charging status !!!");
                AnyoChargeSession chargeSession3 = getChargeSession();
                if (TextUtils.isEmpty(chargeSession3.getCharge_id())) {
                    reportEventRequest((byte) 8);
                    String chargeId3 = portStatus.getCharge_id();
                    ChargeBill chargeBill2 = ChargeContentProxy.getInstance().getChargeBill(chargeId3);
                    if (chargeBill2 != null) {
                        chargeSession3.setCharge_id(chargeId3);
                        chargeSession3.setUser_type(chargeBill2.getUser_type());
                        chargeSession3.setUser_code(chargeBill2.getUser_code());
                        chargeSession3.setInit_type(chargeBill2.getInit_type());
                        chargeSession3.setUser_tc_type(chargeBill2.getUser_tc_type());
                        chargeSession3.setUser_tc_value(chargeBill2.getUser_tc_value());
                        chargeSession3.setUser_balance(chargeBill2.getUser_balance());
                        chargeSession3.setIs_free(chargeBill2.getIs_free());
                        chargeSession3.setBinded_user(chargeBill2.getBinded_user());
                        chargeSession3.setCharge_platform(chargeBill2.getCharge_platform());
                    } else {
                        Log.w("AnyoPortHandler.getChangedAnyoStatus", "failed to query info for charge: " + chargeId3);
                    }
                }
                this.handlerTimer.startTimer(60000L, 73744, null);
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
                ResetChargeRequest resetChargeRequest = chargeSession.getResetChargeRequest();
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
        LOCK_STATUS gunLockStatus = portStatus.getGunLockStatus();
        if (LOCK_STATUS.lock.equals(gunLockStatus)) {
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
            byte deviceEvent = nowPortPluginStatus ? (byte) 3 : (byte) 9;
            reportEventRequest(deviceEvent);
        }
        boolean nowPortLockedStatus = changedAnyoStatus.isPortLocked();
        if (this.latestAnyoStatus.isPortLocked() != nowPortLockedStatus) {
            byte deviceEvent2 = nowPortLockedStatus ? (byte) 7 : (byte) 8;
            reportEventRequest(deviceEvent2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleRequest(AnyoMessage request) {
        if (!this.anyoPortStatus.equals(ANYO_PORT_STATE.logined)) {
            Log.w("AnyoPortHandler.handleRequest", "port: " + this.port + " is not login now, reveived request message is ignored !!! server request: " + request.toJson());
            return;
        }
        byte cmd = request.getHead().getCmdCode();
        switch (cmd) {
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

    /* JADX INFO: Access modifiers changed from: private */
    public void handleResponse(AnyoMessage request, AnyoMessage response) {
        byte cmd = response.getHead().getCmdCode();
        switch (cmd) {
            case 16:
                Log.i("AnyoPortHandler.handleResponse", "receive anyo login response:" + response.toJson());
                handleLoginResponse(request, response);
                return;
            case 17:
                Log.i("AnyoPortHandler.handleResponse", "receive anyo auth response:" + response.toJson());
                AnyoDCAPGateway.getInstance().sendMessage(AnyoDCAPGateway.getInstance().obtainMessage(77828, response));
                return;
            case 18:
            case TimeUtils.HUNDRED_DAY_FIELD_LEN /* 19 */:
            case 22:
            case 23:
            default:
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
        }
    }

    private void sendMessage(AnyoMessage msg) {
        if (!this.anyoPortStatus.equals(ANYO_PORT_STATE.logined) && msg.getHead().getCmdCode() != 16) {
            Log.w("AnyoPortHandler.sendMessage", "port: " + this.port + " is not login now, send message is forbidened !!!");
            if (msg.getHead().getStartCode() == 104) {
                this.handler.obtainMessage(73732, msg).sendToTarget();
                return;
            }
            return;
        }
        AnyoProtocolAgent.getInstance().sendMessage(msg);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void loginRequest() {
        try {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setPileNo(AnyoProtocolAgent.getInstance().getPileNo());
            loginRequest.setPileType(AnyoProtocolAgent.getInstance().getPileType());
            loginRequest.setProvider(AnyoProtocolAgent.provider);
            loginRequest.setMagicNum(AnyoProtocolAgent.magicNumber);
            String[] protocolVersionSplit = AnyoProtocolAgent.protocolVersion.split("\\.");
            String[] softwareVersionSplit = AnyoProtocolAgent.softwareVersion.split("\\.");
            byte[] pileInfoBytes = {(byte) (Integer.parseInt(protocolVersionSplit[1]) & MotionEventCompat.ACTION_MASK), (byte) (Integer.parseInt(protocolVersionSplit[0]) & MotionEventCompat.ACTION_MASK), (byte) (Integer.parseInt(softwareVersionSplit[0]) & MotionEventCompat.ACTION_MASK), (byte) (Integer.parseInt(softwareVersionSplit[1]) & MotionEventCompat.ACTION_MASK), (byte) (Integer.parseInt(softwareVersionSplit[2]) & MotionEventCompat.ACTION_MASK)};
            String pileInfo = new String(pileInfoBytes, Charset.forName(CharEncoding.UTF_8));
            loginRequest.setPileInfo(String.valueOf(pileInfo) + AnyoProtocolAgent.firewareType);
            loginRequest.setRand(AnyoProtocolAgent.getInstance().getCheckSumRand());
            AnyoHead head = AnyoProtocolAgent.getInstance().createRequestHead((byte) 16);
            head.setBodyLength(loginRequest.bodyToBytes().length);
            loginRequest.setHead(head);
            loginRequest.setPort(this.port);
            head.setCheckSum(loginRequest.calcCheckSum(AnyoProtocolAgent.getInstance().getCheckSumRand()));
            sendMessage(loginRequest);
            Log.i("AnyoPortHandler.loginRequest", "send login request: " + loginRequest.toJson());
        } catch (Exception e) {
            Log.w("AnyoPortHandler.loginRequest", Log.getStackTraceString(e));
        }
    }

    private void handleLoginResponse(AnyoMessage request, AnyoMessage response) {
        byte statusCode = response.getHead().getStatusCode();
        LoginResponse resp = (LoginResponse) response;
        if (statusCode == 0) {
            byte peerCheckSumRand = resp.getRand();
            AnyoProtocolAgent.getInstance().setPeerCheckSumRand(peerCheckSumRand);
            long peerTimeStamp = resp.getTimestamp() * 1000;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Log.i("AnyoPortHandler.handleLoginResponse", "peer timestamp: " + com.xcharge.common.utils.TimeUtils.getISO8601Format(peerTimeStamp, "+00:00") + ", local timestamp: " + sdf.format(new Date(System.currentTimeMillis())));
            SystemClock.setCurrentTimeMillis(peerTimeStamp);
            LogUtils.syslog("synch cloud time: " + com.xcharge.common.utils.TimeUtils.getISO8601Format(peerTimeStamp, "+00:00"));
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
            this.okHeartBeatCnt = 0L;
            heartBeatRequest(initAnyoStatus);
            checkReportEventRequest(initAnyoStatus);
            this.latestAnyoStatus = initAnyoStatus;
            AnyoChargeSession chargeSession = getChargeSession();
            if (!TextUtils.isEmpty(chargeSession.getCharge_id()) && CHARGE_STATUS.CHARGING.equals(this.status)) {
                reportChargeRequest();
            }
            this.handlerTimer.startTimer(120000L, 73737, null);
            this.handlerTimer.startTimer(120000L, 73745, null);
            request.setRetrySend(0);
            return;
        }
        Log.e("AnyoPortHandler.handleLoginResponse", "failed to login, error: " + ((int) statusCode));
        if (request.getRetrySend() < 2) {
            this.handler.sendMessageDelayed(this.handler.obtainMessage(73734, request), 5000L);
            return;
        }
        Log.w("AnyoPortHandler.handleLoginResponse", "failed to login 3 times, diconnect !!!");
        AnyoProtocolAgent.getInstance().disconnect();
    }

    /* JADX INFO: Access modifiers changed from: private */
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

    /* JADX INFO: Access modifiers changed from: private */
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
        sendMessage(heartBeatRequest);
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
            sendMessage(reportEventRequest);
            Log.i("AnyoPortHandler.reportEventRequest", "send report event request: " + reportEventRequest.toJson());
        } catch (Exception e) {
            Log.w("AnyoPortHandler.reportEventRequest", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reportChargeRequest() {
        long chargeTime;
        try {
            String localPort = AnyoProtocolAgent.getInstance().getLocalPort(this.port);
            PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(localPort);
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
            sendMessage(reportChargeRequest);
            Log.i("AnyoPortHandler.reportChargeRequest", "send report charge request: " + reportChargeRequest.toJson());
        } catch (Exception e) {
            Log.w("AnyoPortHandler.reportChargeRequest", Log.getStackTraceString(e));
        }
    }

    private void reportChargeStoppedRequest(boolean isForceEnded) {
        try {
            AnyoChargeSession chargeSession = getChargeSession();
            String chargeId = chargeSession.getCharge_id();
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
                    switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE()[csc.ordinal()]) {
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
                        case PortRuntimeData.STATUS_EX_12 /* 12 */:
                            chargeStopCause = 4;
                            break;
                    }
                }
                byte userIdType = 2;
                byte cardBalanceFlag = 0;
                CHARGE_INIT_TYPE initType = chargeBill.getInit_type();
                if (!CHARGE_INIT_TYPE.nfc.equals(initType)) {
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
                sendMessage(reportChargeStoppedRequest);
                Log.i("AnyoPortHandler.reportChargeStoppedRequest", "send report charge stopped request: " + reportChargeStoppedRequest.toJson());
                return;
            }
            Log.w("AnyoPortHandler.reportChargeStoppedRequest", "failed to get info for charge: " + chargeId);
        } catch (Exception e) {
            Log.w("AnyoPortHandler.reportChargeStoppedRequest", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reportHistoryChargeRequest() {
        if (this.latestAnyoStatus.getDeviceStatus() != 0) {
            Log.w("AnyoPortHandler.reportHistoryChargeRequest", "not anyo idle status, not report history charge info !!!");
            return;
        }
        try {
            String[] anyoUserTypes = {CHARGE_USER_TYPE.anyo.toString(), String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.anyo1};
            String localPort = AnyoProtocolAgent.getInstance().getLocalPort(this.port);
            ArrayList<ChargeBill> chargeBills = ChargeContentProxy.getInstance().getUnReportedBills(anyoUserTypes, localPort);
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
                    switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE()[csc.ordinal()]) {
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
                        case PortRuntimeData.STATUS_EX_12 /* 12 */:
                            chargeStopCause = 4;
                            break;
                    }
                }
                byte userIdType = 2;
                byte cardBalanceFlag = 0;
                CHARGE_INIT_TYPE initType = chargeBill.getInit_type();
                if (!CHARGE_INIT_TYPE.nfc.equals(initType)) {
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
                sendMessage(reportHistoryBillRequest);
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
            ReportChargeStoppedRequest req = (ReportChargeStoppedRequest) request;
            String chargeId = String.valueOf(req.getBillId());
            Log.d("AnyoPortHandler.handleReportChargeStoppedResponse", "success to report charge stopped bill: " + chargeId);
            ChargeContentProxy.getInstance().setReportedFlag(chargeId);
            return;
        }
        Log.w("AnyoPortHandler.handleReportChargeStoppedResponse", "failed to report charge stopped bill, error: " + ((int) statusCode));
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
        Log.w("AnyoPortHandler.handleReportChargeStoppedResponse", "failed to report history bill, error: " + ((int) statusCode));
        if (req.getRetrySend() == 0) {
            this.handler.sendMessageDelayed(this.handler.obtainMessage(73734, req), 5000L);
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
            sendMessage(authRequest);
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
            sendMessage(response);
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
            sendMessage(response);
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
            sendMessage(response);
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
            sendMessage(response);
            Log.i("AnyoPortHandler.resetChargeResponse", "send reset charge respose: " + response.toJson());
            return true;
        } catch (Exception e) {
            Log.w("AnyoPortHandler.resetChargeResponse", Log.getStackTraceString(e));
            return false;
        }
    }
}