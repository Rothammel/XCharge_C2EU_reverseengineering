package com.xcharge.charger.core.handler;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.api.Sequence;
import com.xcharge.charger.core.api.bean.AckDirective;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.NackDirective;
import com.xcharge.charger.core.api.bean.cap.AuthDirective;
import com.xcharge.charger.core.api.bean.cap.CAPDirectiveOption;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.core.api.bean.cap.ConditionDirective;
import com.xcharge.charger.core.api.bean.cap.EventDirective;
import com.xcharge.charger.core.api.bean.cap.FinDirective;
import com.xcharge.charger.core.api.bean.cap.InitAckDirective;
import com.xcharge.charger.core.api.bean.cap.InitDirective;
import com.xcharge.charger.core.api.bean.cap.StartDirective;
import com.xcharge.charger.core.api.bean.cap.StopDirective;
import com.xcharge.charger.core.bean.ChargeSession;
import com.xcharge.charger.core.bean.RequestSession;
import com.xcharge.charger.core.controller.ChargeController;
import com.xcharge.charger.core.controller.OSSController;
import com.xcharge.charger.core.type.CHARGE_FSM_STATUS;
import com.xcharge.charger.core.type.CHARGE_REFUSE_CAUSE;
import com.xcharge.charger.core.type.FIN_MODE;
import com.xcharge.charger.data.bean.ContentItem;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.FeeRate;
import com.xcharge.charger.data.bean.PortFeeRate;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.setting.FeeRateSetting;
import com.xcharge.charger.data.bean.setting.PortSetting;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.ADVERT_POLICY;
import com.xcharge.charger.data.bean.type.CHARGE_INIT_TYPE;
import com.xcharge.charger.data.bean.type.CHARGE_MODE;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.CHARGE_STATUS;
import com.xcharge.charger.data.bean.type.CHARGE_STOP_CAUSE;
import com.xcharge.charger.data.bean.type.CHARGE_USER_STATUS;
import com.xcharge.charger.data.bean.type.CHARGE_USER_TYPE;
import com.xcharge.charger.data.bean.type.DELAY_PRICE_UNIT;
import com.xcharge.charger.data.bean.type.DEVICE_STATUS;
import com.xcharge.charger.data.bean.type.GUN_LOCK_MODE;
import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.bean.type.OCPP_CHARGE_STATUS;
import com.xcharge.charger.data.bean.type.SERVICE_PRICE_UNIT;
import com.xcharge.charger.data.bean.type.SWITCH_STATUS;
import com.xcharge.charger.data.bean.type.USER_TC_TYPE;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.LocalSettingCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.data.proxy.PortStatusObserver;
import com.xcharge.charger.device.adpter.DeviceProxy;
import com.xcharge.charger.device.c2.bean.PortRuntimeData;
import com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.charger.ui.adapter.api.UIServiceProxy;
import com.xcharge.charger.ui.adapter.type.CHARGE_UI_STAGE;
import com.xcharge.charger.ui.adapter.type.UI_MODE;
import com.xcharge.charger.ui.c2.activity.BaseActivity;
import com.xcharge.charger.utils.BillUtils;
import com.xcharge.common.utils.HandlerTimer;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.TimeUtils;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* loaded from: classes.dex */
public class ChargeHandler {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$core$type$CHARGE_FSM_STATUS = null;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$core$type$FIN_MODE = null;
    public static final long INTERVAL_CHECK_PLUGIN_TIMING = 5000;
    public static final long INTERVAL_CONDITION_TIMING = 1000;
    public static final long INTERVAL_DELAY_TIMING = 1000;
    public static final int MSG_BILL_PAID_EVENT = 32833;
    public static final int MSG_CHARGE_REFUSE_EVENT = 32776;
    public static final int MSG_DUMMY = 32769;
    public static final int MSG_INTERVAL_CONDITION_TIMING = 32824;
    public static final int MSG_INTERVAL_DELAY_TIMING = 32823;
    public static final int MSG_PLUGIN_CHECK_EVENT = 32834;
    public static final int MSG_PLUGIN_CHECK_TIMING = 32835;
    public static final int MSG_PORT_AUTH_INVALID = 32792;
    public static final int MSG_PORT_AUTH_VALID = 32791;
    public static final int MSG_PORT_CHARGE_FULL = 32788;
    public static final int MSG_PORT_CHARGE_RESUME = 32790;
    public static final int MSG_PORT_CHARGE_STARTED = 32786;
    public static final int MSG_PORT_CHARGE_STOPPED = 32787;
    public static final int MSG_PORT_CHARGE_SUSPEND = 32789;
    public static final int MSG_PORT_LOCK_MODE_CHANGED = 32832;
    public static final int MSG_PORT_PARK_STATUS = 32802;
    public static final int MSG_PORT_PLUGIN = 32793;
    public static final int MSG_PORT_PLUGOUT = 32800;
    public static final int MSG_PORT_RADAR_CALIBRATION = 32801;
    public static final int MSG_PORT_UPDATE = 32803;
    public static final int MSG_PORT_WARN = 32804;
    public static final int MSG_REQUEST_AUTH = 32770;
    public static final int MSG_REQUEST_CONDITION = 32774;
    public static final int MSG_REQUEST_FIN = 32772;
    public static final int MSG_REQUEST_INIT = 32771;
    public static final int MSG_REQUEST_START = 32773;
    public static final int MSG_REQUEST_STOP = 32775;
    public static final int MSG_RESPONSE_ACK = 32784;
    public static final int MSG_RESPONSE_NACK = 32785;
    public static final int MSG_SCAN_ADVERT_FIN_EVENT = 32777;
    public static final int MSG_TIMEOUT_AUTHED = 32805;
    public static final int MSG_TIMEOUT_AUTH_SENDED = 32817;
    public static final int MSG_TIMEOUT_CHARGE_FIN = 32838;
    public static final int MSG_TIMEOUT_CHARGE_STOP_DELAY = 32836;
    public static final int MSG_TIMEOUT_FIN_SENDED = 32816;
    public static final int MSG_TIMEOUT_INITED = 32807;
    public static final int MSG_TIMEOUT_INIT_ACK_SENDED = 32806;
    public static final int MSG_TIMEOUT_INIT_ADVERT = 32825;
    public static final int MSG_TIMEOUT_PLUGIN = 32818;
    public static final int MSG_TIMEOUT_PLUGOUT = 32819;
    public static final int MSG_TIMEOUT_PRESTOP_CHECK = 32837;
    public static final int MSG_TIMEOUT_PRE_STOP = 32822;
    public static final int MSG_TIMEOUT_RESERVE_CHECK = 32820;
    public static final int MSG_TIMEOUT_RESERVE_WAIT = 32821;
    public static final int MSG_TIMEOUT_STOPPED = 32809;
    public static final int MSG_TIMEOUT_STOP_SENDED = 32808;
    public static final int MSG_TIMEOUT_USER_RESERVED = 32839;
    public static final int MSG_TIMEOUT_USER_RESERVE_WAIT_PLUGIN = 32840;
    public static final long TIMEOUT_AUTHED = 10000;
    public static final long TIMEOUT_AUTH_SENDED = 10000;
    public static final long TIMEOUT_CHARGE_FIN = 4000;
    public static final long TIMEOUT_CHARGE_STOP_DELAY = 1000;
    public static final long TIMEOUT_FIN_SENDED = 10000;
    public static final long TIMEOUT_INIT_ACK_SENDED = 10000;
    public static final long TIMEOUT_PLUGOUT = 2000;
    public static final long TIMEOUT_PRESTOP_CHECK = 2000;
    public static final long TIMEOUT_PRE_STOP = 30000;
    public static final long TIMEOUT_RESERVE_CHECK = 2000;
    public static final long TIMEOUT_RESERVE_WAIT = 60000;
    public static final long TIMEOUT_STOP_SENDED = 5000;
    private Context context = null;
    private String port = null;
    private HandlerThread thread = null;
    private MsgHandler handler = null;
    private HandlerTimer handlerTimer = null;
    private ChargeSession chargeSession = null;
    private CHARGE_FSM_STATUS status = null;
    private OCPP_CHARGE_STATUS ocppChargeStatus = null;
    private double startPower = 0.0d;
    private double ammeter = 0.0d;
    private PortStatusObserver portStatusObserver = null;
    private PortLockModeObserver portLockModeObserver = null;
    private BillPayObserver billPayObserver = null;

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$core$type$CHARGE_FSM_STATUS() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$core$type$CHARGE_FSM_STATUS;
        if (iArr == null) {
            iArr = new int[CHARGE_FSM_STATUS.valuesCustom().length];
            try {
                iArr[CHARGE_FSM_STATUS.auth_sended.ordinal()] = 2;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.authed.ordinal()] = 3;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.charging.ordinal()] = 11;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.fin_sended.ordinal()] = 16;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.idle.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.init_ack_sended.ordinal()] = 4;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.init_advert.ordinal()] = 5;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.inited.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.paused.ordinal()] = 13;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.plugin.ordinal()] = 9;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.pre_stop.ordinal()] = 12;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.reserve_wait.ordinal()] = 10;
            } catch (NoSuchFieldError e12) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.stop_sended.ordinal()] = 14;
            } catch (NoSuchFieldError e13) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.stopped.ordinal()] = 15;
            } catch (NoSuchFieldError e14) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.user_reserve_wait_plugin.ordinal()] = 7;
            } catch (NoSuchFieldError e15) {
            }
            try {
                iArr[CHARGE_FSM_STATUS.user_reserved.ordinal()] = 6;
            } catch (NoSuchFieldError e16) {
            }
            $SWITCH_TABLE$com$xcharge$charger$core$type$CHARGE_FSM_STATUS = iArr;
        }
        return iArr;
    }

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$core$type$FIN_MODE() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$core$type$FIN_MODE;
        if (iArr == null) {
            iArr = new int[FIN_MODE.valuesCustom().length];
            try {
                iArr[FIN_MODE.busy.ordinal()] = 5;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[FIN_MODE.cancel.ordinal()] = 3;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[FIN_MODE.car.ordinal()] = 8;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[FIN_MODE.error.ordinal()] = 9;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[FIN_MODE.nfc.ordinal()] = 12;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[FIN_MODE.no_feerate.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[FIN_MODE.normal.ordinal()] = 1;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[FIN_MODE.plugin_timeout.ordinal()] = 7;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[FIN_MODE.port_forbiden.ordinal()] = 4;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[FIN_MODE.refuse.ordinal()] = 13;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[FIN_MODE.remote.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[FIN_MODE.reserve_error.ordinal()] = 10;
            } catch (NoSuchFieldError e12) {
            }
            try {
                iArr[FIN_MODE.timeout.ordinal()] = 2;
            } catch (NoSuchFieldError e13) {
            }
            $SWITCH_TABLE$com$xcharge$charger$core$type$FIN_MODE = iArr;
        }
        return iArr;
    }

    /* loaded from: classes.dex */
    private static class PortLockModeObserver extends ContentObserver {
        private Handler handler;

        public PortLockModeObserver(Handler handler) {
            super(handler);
            this.handler = null;
            this.handler = handler;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            Log.d("ChargeHandler.PortLockModeObserver.onChange", "selfChange: " + selfChange + ", uri: " + uri.toString());
            super.onChange(selfChange, uri);
            this.handler.obtainMessage(ChargeHandler.MSG_PORT_LOCK_MODE_CHANGED, uri).sendToTarget();
        }
    }

    /* loaded from: classes.dex */
    private static class BillPayObserver extends ContentObserver {
        private Handler handler;

        public BillPayObserver(Handler handler) {
            super(handler);
            this.handler = null;
            this.handler = handler;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            Log.d("ChargeHandler.BillPayObserver.onChange", "selfChange: " + selfChange + ", uri: " + uri.toString());
            super.onChange(selfChange, uri);
            this.handler.obtainMessage(ChargeHandler.MSG_BILL_PAID_EVENT, uri).sendToTarget();
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
                    case ChargeHandler.MSG_REQUEST_AUTH /* 32770 */:
                        RequestSession requestSession = (RequestSession) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "port: " + ChargeHandler.this.port + ", receive auth request: " + requestSession.getRequest().toJson());
                        ChargeHandler.this.handleAuthRequest(requestSession.getRequest(), requestSession.getConfirm());
                        break;
                    case ChargeHandler.MSG_REQUEST_INIT /* 32771 */:
                        RequestSession requestSession2 = (RequestSession) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "port: " + ChargeHandler.this.port + ", receive init request: " + requestSession2.getRequest().toJson());
                        ChargeHandler.this.handleInitRequest(requestSession2.getRequest(), requestSession2.getConfirm());
                        break;
                    case ChargeHandler.MSG_REQUEST_FIN /* 32772 */:
                        RequestSession requestSession3 = (RequestSession) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "port: " + ChargeHandler.this.port + ", receive fin request: " + requestSession3.getRequest().toJson());
                        ChargeHandler.this.handleFinRequest(requestSession3.getRequest(), requestSession3.getConfirm());
                        break;
                    case ChargeHandler.MSG_REQUEST_START /* 32773 */:
                        RequestSession requestSession4 = (RequestSession) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "port: " + ChargeHandler.this.port + ", receive start request: " + requestSession4.getRequest().toJson());
                        ChargeHandler.this.handleStartRequest(requestSession4.getRequest(), requestSession4.getConfirm());
                        break;
                    case ChargeHandler.MSG_REQUEST_CONDITION /* 32774 */:
                        RequestSession requestSession5 = (RequestSession) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "port: " + ChargeHandler.this.port + ", receive condition request: " + requestSession5.getRequest().toJson());
                        ChargeHandler.this.handleConditionRequest(requestSession5.getRequest(), requestSession5.getConfirm());
                        break;
                    case ChargeHandler.MSG_REQUEST_STOP /* 32775 */:
                        RequestSession requestSession6 = (RequestSession) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "port: " + ChargeHandler.this.port + ", receive stop request: " + requestSession6.getRequest().toJson());
                        ChargeHandler.this.handleStopRequest(requestSession6.getRequest(), requestSession6.getConfirm());
                        break;
                    case ChargeHandler.MSG_CHARGE_REFUSE_EVENT /* 32776 */:
                        RequestSession requestSession7 = (RequestSession) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "port: " + ChargeHandler.this.port + ", receive charge refuse event: " + requestSession7.getRequest().toJson());
                        ChargeHandler.this.handleChargeRefuseEvent(requestSession7.getRequest(), requestSession7.getConfirm());
                        break;
                    case ChargeHandler.MSG_SCAN_ADVERT_FIN_EVENT /* 32777 */:
                        RequestSession requestSession8 = (RequestSession) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "port: " + ChargeHandler.this.port + ", receive scan advert finished event: " + requestSession8.getRequest().toJson());
                        ChargeHandler.this.handleScanAdvertFinishedEvent(requestSession8.getRequest(), requestSession8.getConfirm());
                        break;
                    case ChargeHandler.MSG_RESPONSE_ACK /* 32784 */:
                        DCAPMessage response = (DCAPMessage) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "port: " + ChargeHandler.this.port + ", receive ack response: " + response.toJson());
                        ChargeHandler.this.handleAckResponse(response);
                        break;
                    case ChargeHandler.MSG_RESPONSE_NACK /* 32785 */:
                        DCAPMessage response2 = (DCAPMessage) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "port: " + ChargeHandler.this.port + ", receive nack response: " + response2.toJson());
                        ChargeHandler.this.handleNackResponse(response2);
                        break;
                    case ChargeHandler.MSG_PORT_CHARGE_STARTED /* 32786 */:
                        PortStatus portStatus = (PortStatus) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "receive charge start event !!! port: " + ChargeHandler.this.port + ", status: " + portStatus.toJson());
                        ChargeHandler.this.handleChargeStarted(true, portStatus);
                        break;
                    case ChargeHandler.MSG_PORT_CHARGE_STOPPED /* 32787 */:
                        PortStatus portStatus2 = (PortStatus) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "receive charge stop event, delay it here!!! port: " + ChargeHandler.this.port + ", status: " + portStatus2.toJson());
                        ChargeHandler.this.handleChargeStopEvent(portStatus2);
                        break;
                    case ChargeHandler.MSG_PORT_CHARGE_FULL /* 32788 */:
                        PortStatus portStatus3 = (PortStatus) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "receive charge full event !!! port: " + ChargeHandler.this.port + ", status: " + portStatus3.toJson());
                        ChargeHandler.this.handleChargeFull(portStatus3);
                        break;
                    case ChargeHandler.MSG_PORT_CHARGE_SUSPEND /* 32789 */:
                        PortStatus portStatus4 = (PortStatus) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "receive charge suspend event !!! port: " + ChargeHandler.this.port + ", status: " + portStatus4.toJson());
                        ChargeHandler.this.handleSuspend(portStatus4);
                        break;
                    case ChargeHandler.MSG_PORT_CHARGE_RESUME /* 32790 */:
                        PortStatus portStatus5 = (PortStatus) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "receive charge resume event !!! port: " + ChargeHandler.this.port + ", status: " + portStatus5.toJson());
                        ChargeHandler.this.handleResume(portStatus5);
                        break;
                    case ChargeHandler.MSG_PORT_AUTH_VALID /* 32791 */:
                        PortStatus portStatus6 = (PortStatus) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "receive auth valid event !!! port: " + ChargeHandler.this.port + ", status: " + portStatus6.toJson());
                        ChargeHandler.this.handleAuthValid(portStatus6);
                        break;
                    case ChargeHandler.MSG_PORT_AUTH_INVALID /* 32792 */:
                        PortStatus portStatus7 = (PortStatus) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "receive auth invalid event !!! port: " + ChargeHandler.this.port + ", status: " + portStatus7.toJson());
                        ChargeHandler.this.handleAuthInvalid(portStatus7);
                        break;
                    case ChargeHandler.MSG_PORT_PLUGIN /* 32793 */:
                        PortStatus portStatus8 = (PortStatus) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "receive plugin event !!! port: " + ChargeHandler.this.port + ", status: " + portStatus8.toJson());
                        ChargeHandler.this.handlePlugin(portStatus8);
                        break;
                    case ChargeHandler.MSG_PORT_PLUGOUT /* 32800 */:
                        PortStatus portStatus9 = (PortStatus) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "receive plugout event !!! port: " + ChargeHandler.this.port + ", status: " + portStatus9.toJson());
                        ChargeHandler.this.handlePlugout(portStatus9);
                        break;
                    case ChargeHandler.MSG_PORT_UPDATE /* 32803 */:
                        PortStatus portStatus10 = (PortStatus) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "receive charge update event !!! port: " + ChargeHandler.this.port + ", status: " + portStatus10.toJson());
                        ChargeHandler.this.handleUpdate(portStatus10);
                        break;
                    case ChargeHandler.MSG_TIMEOUT_AUTHED /* 32805 */:
                        Log.i("ChargeHandler.handleMessage", CHARGE_FSM_STATUS.authed + " state timeout !!! port: " + ChargeHandler.this.port + ", charge session: " + ChargeHandler.this.getChargeSession().toJson());
                        ChargeHandler.this.handlerTimer.stopTimer(msg.what);
                        ChargeHandler.this.clearChargeSession();
                        ChargeHandler.this.status = CHARGE_FSM_STATUS.idle;
                        ChargeHandler.this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_ENDED;
                        ChargeHandler.this.notifyChargeEnded2OSS();
                        Bundle data = new Bundle();
                        data.putString("stage", CHARGE_UI_STAGE.refuse.getStage());
                        data.putString("cause", CHARGE_REFUSE_CAUSE.AUTH_TIMEOUT.getCause());
                        UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
                        break;
                    case ChargeHandler.MSG_TIMEOUT_INIT_ACK_SENDED /* 32806 */:
                        RequestSession requestSession9 = (RequestSession) msg.obj;
                        ChargeSession chargeSession = ChargeHandler.this.getChargeSession();
                        Log.i("ChargeHandler.handleMessage", CHARGE_FSM_STATUS.init_ack_sended + " state timeout !!! port: " + ChargeHandler.this.port + ", charge session: " + chargeSession.toJson());
                        ChargeHandler.this.handlerTimer.stopTimer(msg.what);
                        ChargeHandler.this.finIndicate(FIN_MODE.timeout, null, chargeSession.getChargeBill().getUser_type(), chargeSession.getChargeBill().getUser_code(), chargeSession.getChargeBill().getCharge_id(), Long.valueOf(requestSession9.getRequest().getSeq()));
                        break;
                    case ChargeHandler.MSG_TIMEOUT_INITED /* 32807 */:
                        ChargeSession chargeSession2 = ChargeHandler.this.getChargeSession();
                        Log.i("ChargeHandler.handleMessage", CHARGE_FSM_STATUS.inited + " state timeout !!! port: " + ChargeHandler.this.port + ", charge session: " + chargeSession2.toJson());
                        ChargeHandler.this.handlerTimer.stopTimer(msg.what);
                        ChargeHandler.this.cancelPortChargeAuth();
                        ChargeHandler.this.finIndicate(FIN_MODE.plugin_timeout, null, chargeSession2.getChargeBill().getUser_type(), chargeSession2.getChargeBill().getUser_code(), chargeSession2.getChargeBill().getCharge_id(), null);
                        break;
                    case ChargeHandler.MSG_TIMEOUT_STOP_SENDED /* 32808 */:
                        RequestSession requestSession10 = (RequestSession) msg.obj;
                        Log.i("ChargeHandler.handleMessage", CHARGE_FSM_STATUS.stop_sended + " state timeout !!! port: " + ChargeHandler.this.port + ", charge session: " + ChargeHandler.this.getChargeSession().toJson());
                        ChargeHandler.this.handlerTimer.stopTimer(msg.what);
                        int stopRetry = ChargeHandler.this.getChargeSession().getStop_retry();
                        if (stopRetry == 1) {
                            Log.w("ChargeHandler.handleMessage", "failed to retry to stop charge !!! port: " + ChargeHandler.this.port);
                            ChargeController.nackConfirm(requestSession10.getConfirm(), 10000, "timeout", null);
                            DeviceProxy.getInstance().beep(3);
                            break;
                        } else if (stopRetry == 0) {
                            ChargeHandler.this.retryStopCharge(requestSession10.getRequest(), requestSession10.getConfirm());
                            break;
                        }
                        break;
                    case ChargeHandler.MSG_TIMEOUT_STOPPED /* 32809 */:
                        ChargeSession chargeSession3 = ChargeHandler.this.getChargeSession();
                        Log.i("ChargeHandler.handleMessage", CHARGE_FSM_STATUS.stopped + " state timeout !!! port: " + ChargeHandler.this.port + ", charge session: " + chargeSession3.toJson());
                        ChargeHandler.this.handlerTimer.stopTimer(msg.what);
                        if (chargeSession3.isDelayWaitStarted()) {
                            chargeSession3.setDelayStarted(true);
                            ChargeHandler.this.handleDelayStarted();
                        }
                        ChargeHandler.this.handlerTimer.startTimer(1000L, ChargeHandler.MSG_INTERVAL_DELAY_TIMING, null);
                        ChargeHandler.this.handleDelayTiming();
                        break;
                    case ChargeHandler.MSG_TIMEOUT_FIN_SENDED /* 32816 */:
                        Log.i("ChargeHandler.handleMessage", CHARGE_FSM_STATUS.fin_sended + " state timeout !!! port: " + ChargeHandler.this.port + ", charge session: " + ChargeHandler.this.getChargeSession().toJson());
                        ChargeHandler.this.handlerTimer.stopTimer(msg.what);
                        ChargeHandler.this.status = CHARGE_FSM_STATUS.idle;
                        ChargeHandler.this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_ENDED;
                        ChargeHandler.this.updateChargeData(false, null);
                        ChargeHandler.this.notifyChargeSessionFinished();
                        ChargeHandler.this.clearChargeSession();
                        ChargeHandler.this.notifyChargeEnded2OSS();
                        break;
                    case ChargeHandler.MSG_TIMEOUT_AUTH_SENDED /* 32817 */:
                        DCAPMessage confirm4Auth = (DCAPMessage) msg.obj;
                        Log.i("ChargeHandler.handleMessage", CHARGE_FSM_STATUS.auth_sended + " state timeout !!! port: " + ChargeHandler.this.port + ", charge session: " + ChargeHandler.this.getChargeSession().toJson());
                        ChargeHandler.this.handlerTimer.stopTimer(msg.what);
                        ChargeController.nackConfirm(confirm4Auth, 10000, "auth to server timeout", null);
                        ChargeHandler.this.clearChargeSession();
                        ChargeHandler.this.status = CHARGE_FSM_STATUS.idle;
                        ChargeHandler.this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_ENDED;
                        ChargeHandler.this.notifyChargeEnded2OSS();
                        break;
                    case ChargeHandler.MSG_TIMEOUT_PLUGIN /* 32818 */:
                        Log.i("ChargeHandler.handleMessage", CHARGE_FSM_STATUS.plugin + " state timeout !!! port: " + ChargeHandler.this.port + ", charge session: " + ChargeHandler.this.getChargeSession().toJson());
                        ChargeHandler.this.handlerTimer.stopTimer(msg.what);
                        ChargeHandler.this.startCharge();
                        ChargeHandler.this.handlerTimer.stopTimer(ChargeHandler.MSG_TIMEOUT_RESERVE_CHECK);
                        ChargeHandler.this.handlerTimer.startTimer(2000L, ChargeHandler.MSG_TIMEOUT_RESERVE_CHECK, null);
                        break;
                    case ChargeHandler.MSG_TIMEOUT_PLUGOUT /* 32819 */:
                        Log.i("ChargeHandler.handleMessage", "plugout timeout, consider charge ended indeed !!! charge status: " + ChargeHandler.this.status + ", port: " + ChargeHandler.this.port + ", charge session: " + ChargeHandler.this.getChargeSession().toJson());
                        ChargeHandler.this.handlerTimer.stopTimer(msg.what);
                        ChargeHandler.this.handlePlugoutIndeed();
                        break;
                    case ChargeHandler.MSG_TIMEOUT_RESERVE_CHECK /* 32820 */:
                        Log.i("ChargeHandler.handleMessage", "check reserve charge timeout !!! port: " + ChargeHandler.this.port + ", charge session: " + ChargeHandler.this.getChargeSession().toJson());
                        ChargeHandler.this.handlerTimer.stopTimer(msg.what);
                        ChargeHandler.this.handleReserveCheckTimeout();
                        break;
                    case ChargeHandler.MSG_TIMEOUT_RESERVE_WAIT /* 32821 */:
                        ChargeSession chargeSession4 = ChargeHandler.this.getChargeSession();
                        Log.i("ChargeHandler.handleMessage", CHARGE_FSM_STATUS.reserve_wait + " state timeout !!! port: " + ChargeHandler.this.port + ", charge session: " + ChargeHandler.this.getChargeSession().toJson());
                        ChargeHandler.this.handlerTimer.stopTimer(msg.what);
                        chargeSession4.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.car);
                        ChargeHandler.this.finIndicate(FIN_MODE.car, null, chargeSession4.getChargeBill().getUser_type(), chargeSession4.getChargeBill().getUser_code(), chargeSession4.getChargeBill().getCharge_id(), null);
                        ChargeHandler.this.cancelPortChargeAuth();
                        break;
                    case ChargeHandler.MSG_TIMEOUT_PRE_STOP /* 32822 */:
                        ChargeSession chargeSession5 = ChargeHandler.this.getChargeSession();
                        Log.i("ChargeHandler.handleMessage", CHARGE_FSM_STATUS.pre_stop + " state timeout !!! port: " + ChargeHandler.this.port + ", charge session: " + ChargeHandler.this.getChargeSession().toJson());
                        ChargeHandler.this.handlerTimer.stopTimer(msg.what);
                        ChargeHandler.this.stopCharge();
                        chargeSession5.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.car);
                        break;
                    case ChargeHandler.MSG_INTERVAL_DELAY_TIMING /* 32823 */:
                        try {
                            ChargeHandler.this.handleDelayTiming();
                        } catch (Exception e) {
                        }
                        ChargeHandler.this.handlerTimer.startTimer(1000L, ChargeHandler.MSG_INTERVAL_DELAY_TIMING, null);
                        break;
                    case ChargeHandler.MSG_INTERVAL_CONDITION_TIMING /* 32824 */:
                        ChargeHandler.this.handleConditionTiming();
                        break;
                    case ChargeHandler.MSG_TIMEOUT_INIT_ADVERT /* 32825 */:
                        Log.i("ChargeHandler.handleMessage", CHARGE_FSM_STATUS.init_advert + " state timeout !!! port: " + ChargeHandler.this.port + ", charge session: " + ChargeHandler.this.getChargeSession().toJson());
                        ChargeHandler.this.handlerTimer.stopTimer(msg.what);
                        ChargeHandler.this.handleInitAdvertTimeout();
                        break;
                    case ChargeHandler.MSG_PORT_LOCK_MODE_CHANGED /* 32832 */:
                        Uri uri = (Uri) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "lock mode changed !!! port: " + ChargeHandler.this.port + ",  charge status: " + ChargeHandler.this.status);
                        ChargeHandler.this.handlePortLockModeChanged(uri);
                        break;
                    case ChargeHandler.MSG_BILL_PAID_EVENT /* 32833 */:
                        ChargeHandler.this.handleBillPaidEvent((Uri) msg.obj);
                        break;
                    case ChargeHandler.MSG_PLUGIN_CHECK_EVENT /* 32834 */:
                        ChargeHandler.this.checkPlugin();
                        break;
                    case ChargeHandler.MSG_PLUGIN_CHECK_TIMING /* 32835 */:
                        try {
                            ChargeHandler.this.checkPlugin();
                        } catch (Exception e2) {
                        }
                        ChargeHandler.this.handlerTimer.startTimer(5000L, ChargeHandler.MSG_PLUGIN_CHECK_TIMING, null);
                        break;
                    case ChargeHandler.MSG_TIMEOUT_CHARGE_STOP_DELAY /* 32836 */:
                        PortStatus portStatus11 = (PortStatus) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "charge stop delay timeout, handle it now !!! port: " + ChargeHandler.this.port + ", status: " + portStatus11.toJson());
                        ChargeHandler.this.handlerTimer.stopTimer(msg.what);
                        ChargeHandler.this.handleChargePreStop(portStatus11);
                        break;
                    case ChargeHandler.MSG_TIMEOUT_PRESTOP_CHECK /* 32837 */:
                        PortStatus portStatus12 = (PortStatus) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "charge prestop check timeout !!! port: " + ChargeHandler.this.port + ", status: " + portStatus12.toJson());
                        ChargeHandler.this.handlerTimer.stopTimer(msg.what);
                        ChargeHandler.this.handlePrestopCheckTimeout(portStatus12);
                        break;
                    case ChargeHandler.MSG_TIMEOUT_CHARGE_FIN /* 32838 */:
                        FinDirective fin = (FinDirective) msg.obj;
                        Log.i("ChargeHandler.handleMessage", "charge fin timeout, also consider charge ended !!! charge status: " + ChargeHandler.this.status + ", port: " + ChargeHandler.this.port + ", charge session: " + ChargeHandler.this.getChargeSession().toJson() + ", fin directive: " + fin.toJson());
                        ChargeHandler.this.handlerTimer.stopTimer(msg.what);
                        ChargeHandler.this.handleChargeFinIndeed(fin);
                        break;
                    case ChargeHandler.MSG_TIMEOUT_USER_RESERVED /* 32839 */:
                        Log.i("ChargeHandler.handleMessage", CHARGE_FSM_STATUS.user_reserved + " state timeout !!! port: " + ChargeHandler.this.port + ", charge session: " + ChargeHandler.this.getChargeSession().toJson());
                        ChargeHandler.this.handlerTimer.stopTimer(msg.what);
                        ChargeHandler.this.handleUserReservedTimeout();
                        break;
                    case ChargeHandler.MSG_TIMEOUT_USER_RESERVE_WAIT_PLUGIN /* 32840 */:
                        ChargeSession chargeSession6 = ChargeHandler.this.getChargeSession();
                        Log.i("ChargeHandler.handleMessage", CHARGE_FSM_STATUS.user_reserve_wait_plugin + " state timeout !!! port: " + ChargeHandler.this.port + ", charge session: " + chargeSession6.toJson());
                        ChargeHandler.this.handlerTimer.stopTimer(msg.what);
                        DeviceProxy.getInstance().closeGunLed(ChargeHandler.this.port);
                        ChargeHandler.this.finIndicate(FIN_MODE.plugin_timeout, null, chargeSession6.getChargeBill().getUser_type(), chargeSession6.getChargeBill().getUser_code(), chargeSession6.getChargeBill().getCharge_id(), null);
                        break;
                    case PortStatusObserver.MSG_PORT_STATUS_CHANGE /* 139265 */:
                        Uri uri2 = (Uri) msg.obj;
                        if (uri2.getPath().contains("ports/" + ChargeHandler.this.port + "/plugin")) {
                            Log.d("ChargeHandler.handleMessage", "port plugin status changed, port: " + ChargeHandler.this.port + ", uri: " + uri2.toString());
                            ChargeHandler.this.handlePortPluginStatusChanged();
                            break;
                        }
                        break;
                }
            } catch (Exception e3) {
                Log.e("ChargeHandler.handleMessage", "except: " + Log.getStackTraceString(e3));
                LogUtils.syslog("ChargeHandler handleMessage exception: " + Log.getStackTraceString(e3));
            }
            super.handleMessage(msg);
        }
    }

    public void init(Context context, String port) {
        this.context = context;
        this.port = port;
        this.status = CHARGE_FSM_STATUS.idle;
        this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_ENDED;
        this.thread = new HandlerThread("ChargeHandler#" + this.port, 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(context);
        this.handlerTimer.startTimer(5000L, MSG_PLUGIN_CHECK_TIMING, null);
        this.portLockModeObserver = new PortLockModeObserver(this.handler);
        this.context.getContentResolver().registerContentObserver(ChargeStatusCacheProvider.getInstance().getUriFor("ports/lock/mode/" + this.port), true, this.portLockModeObserver);
        this.portStatusObserver = new PortStatusObserver(this.context, this.port, this.handler);
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor("ports/" + this.port), true, this.portStatusObserver);
        this.billPayObserver = new BillPayObserver(this.handler);
        this.context.getContentResolver().registerContentObserver(ChargeContentProxy.getInstance().getUriFor("pay"), true, this.billPayObserver);
    }

    public void destroy() {
        this.context.getContentResolver().unregisterContentObserver(this.portStatusObserver);
        this.context.getContentResolver().unregisterContentObserver(this.portLockModeObserver);
        this.context.getContentResolver().unregisterContentObserver(this.billPayObserver);
        this.handlerTimer.destroy();
        this.handler.removeMessages(MSG_REQUEST_AUTH);
        this.handler.removeMessages(MSG_REQUEST_INIT);
        this.handler.removeMessages(MSG_REQUEST_FIN);
        this.handler.removeMessages(MSG_REQUEST_START);
        this.handler.removeMessages(MSG_REQUEST_CONDITION);
        this.handler.removeMessages(MSG_REQUEST_STOP);
        this.handler.removeMessages(MSG_CHARGE_REFUSE_EVENT);
        this.handler.removeMessages(MSG_SCAN_ADVERT_FIN_EVENT);
        this.handler.removeMessages(MSG_RESPONSE_ACK);
        this.handler.removeMessages(MSG_RESPONSE_NACK);
        this.handler.removeMessages(MSG_PORT_CHARGE_STARTED);
        this.handler.removeMessages(MSG_PORT_CHARGE_STOPPED);
        this.handler.removeMessages(MSG_PORT_CHARGE_FULL);
        this.handler.removeMessages(MSG_PORT_CHARGE_SUSPEND);
        this.handler.removeMessages(MSG_PORT_CHARGE_RESUME);
        this.handler.removeMessages(MSG_PORT_AUTH_VALID);
        this.handler.removeMessages(MSG_PORT_AUTH_INVALID);
        this.handler.removeMessages(MSG_PORT_PLUGIN);
        this.handler.removeMessages(MSG_PORT_PLUGOUT);
        this.handler.removeMessages(MSG_PORT_RADAR_CALIBRATION);
        this.handler.removeMessages(MSG_PORT_PARK_STATUS);
        this.handler.removeMessages(MSG_PORT_UPDATE);
        this.handler.removeMessages(MSG_PORT_WARN);
        this.handler.removeMessages(MSG_TIMEOUT_AUTHED);
        this.handler.removeMessages(MSG_TIMEOUT_INIT_ACK_SENDED);
        this.handler.removeMessages(MSG_TIMEOUT_INITED);
        this.handler.removeMessages(MSG_TIMEOUT_STOP_SENDED);
        this.handler.removeMessages(MSG_TIMEOUT_STOPPED);
        this.handler.removeMessages(MSG_TIMEOUT_FIN_SENDED);
        this.handler.removeMessages(MSG_TIMEOUT_AUTH_SENDED);
        this.handler.removeMessages(MSG_TIMEOUT_PLUGIN);
        this.handler.removeMessages(MSG_TIMEOUT_PLUGOUT);
        this.handler.removeMessages(MSG_TIMEOUT_RESERVE_CHECK);
        this.handler.removeMessages(MSG_TIMEOUT_RESERVE_WAIT);
        this.handler.removeMessages(MSG_TIMEOUT_PRE_STOP);
        this.handler.removeMessages(MSG_INTERVAL_DELAY_TIMING);
        this.handler.removeMessages(MSG_INTERVAL_CONDITION_TIMING);
        this.handler.removeMessages(MSG_TIMEOUT_INIT_ADVERT);
        this.handler.removeMessages(MSG_PORT_LOCK_MODE_CHANGED);
        this.handler.removeMessages(MSG_BILL_PAID_EVENT);
        this.handler.removeMessages(MSG_PLUGIN_CHECK_EVENT);
        this.handler.removeMessages(MSG_PLUGIN_CHECK_TIMING);
        this.handler.removeMessages(MSG_TIMEOUT_CHARGE_STOP_DELAY);
        this.handler.removeMessages(MSG_TIMEOUT_PRESTOP_CHECK);
        this.handler.removeMessages(MSG_TIMEOUT_CHARGE_FIN);
        this.handler.removeMessages(MSG_TIMEOUT_USER_RESERVED);
        this.handler.removeMessages(MSG_TIMEOUT_USER_RESERVE_WAIT_PLUGIN);
        this.handler.removeMessages(PortStatusObserver.MSG_PORT_STATUS_CHANGE);
        this.thread.quit();
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

    public boolean sendEmptyMessage(int what) {
        return this.handler.sendEmptyMessage(what);
    }

    public boolean hasCharge(String chargeId) {
        if (TextUtils.isEmpty(chargeId)) {
            return false;
        }
        String charge = getChargeSession().getChargeBill().getCharge_id();
        return chargeId.equals(charge);
    }

    public boolean isIdle(CHARGE_INIT_TYPE initType, boolean isLocal) {
        if (initType == null) {
            return CHARGE_FSM_STATUS.idle.equals(this.status);
        }
        ChargeSession chargeSession = getChargeSession();
        if (!initType.equals(chargeSession.getChargeBill().getInit_type()) || CHARGE_FSM_STATUS.idle.equals(this.status)) {
            return true;
        }
        if (CHARGE_INIT_TYPE.nfc.equals(initType)) {
            NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(chargeSession.getChargeBill().getUser_type());
            return isLocal ? (NFC_CARD_TYPE.U1.equals(nfcCardType) || NFC_CARD_TYPE.U2.equals(nfcCardType) || NFC_CARD_TYPE.anyo_svw.equals(nfcCardType) || NFC_CARD_TYPE.CT_DEMO.equals(nfcCardType)) ? false : true : NFC_CARD_TYPE.U1.equals(nfcCardType) || NFC_CARD_TYPE.U2.equals(nfcCardType) || NFC_CARD_TYPE.anyo_svw.equals(nfcCardType) || NFC_CARD_TYPE.CT_DEMO.equals(nfcCardType);
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public ChargeSession getChargeSession() {
        if (this.chargeSession == null) {
            this.chargeSession = new ChargeSession();
        }
        if (this.chargeSession.getChargeBill() == null) {
            this.chargeSession.setChargeBill(new ChargeBill());
        }
        return this.chargeSession;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void clearChargeSession() {
        this.chargeSession = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePortLockModeChanged(Uri uri) {
    }

    private void setPortLockMode(String userType, CHARGE_INIT_TYPE initType) {
        GUN_LOCK_MODE mode;
        boolean usingRemote = false;
        boolean usingDefault = false;
        if (CHARGE_INIT_TYPE.nfc.equals(initType)) {
            NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(userType);
            if (NFC_CARD_TYPE.anyo1.equals(nfcCardType) || NFC_CARD_TYPE.U3.equals(nfcCardType) || NFC_CARD_TYPE.cddz_1.equals(nfcCardType) || NFC_CARD_TYPE.cddz_2.equals(nfcCardType) || NFC_CARD_TYPE.ocpp.equals(nfcCardType)) {
                usingRemote = true;
            }
        } else {
            usingRemote = true;
        }
        PortSetting portSetting = null;
        if (usingRemote) {
            if (!RemoteSettingCacheProvider.getInstance().hasRemoteSetting()) {
                usingDefault = true;
            } else {
                portSetting = RemoteSettingCacheProvider.getInstance().getChargePortSetting(this.port);
            }
        } else if (!LocalSettingCacheProvider.getInstance().hasLocalSetting()) {
            usingDefault = true;
        } else {
            portSetting = LocalSettingCacheProvider.getInstance().getChargePortSetting(this.port);
        }
        if (usingDefault) {
            mode = ChargeStatusCacheProvider.getInstance().getPortLockMode(this.port);
        } else if (portSetting != null) {
            mode = portSetting.getGunLockSetting().getMode();
        } else {
            mode = ChargeStatusCacheProvider.getInstance().getPortLockMode(this.port);
        }
        LOCK_STATUS lockStatus = ChargeStatusCacheProvider.getInstance().getPortLockStatus(this.port);
        Log.d("ChargeHandler.setPortLockMode", "port: " + this.port + ", mode: " + mode + ", lockStatus: " + lockStatus + ", usingDefault: " + usingDefault + ", usingRemote: " + usingRemote);
        if (GUN_LOCK_MODE.disable.equals(mode)) {
            if (!LOCK_STATUS.disable.equals(lockStatus)) {
                DeviceProxy.getInstance().unlockGun(this.port);
                DeviceProxy.getInstance().disableGunLock(this.port);
                ChargeStatusCacheProvider.getInstance().updatePortLockStatus(this.port, LOCK_STATUS.disable);
            }
        } else if (LOCK_STATUS.disable.equals(lockStatus)) {
            DeviceProxy.getInstance().enableGunLock(this.port);
            DeviceProxy.getInstance().unlockGun(this.port);
            ChargeStatusCacheProvider.getInstance().updatePortLockStatus(this.port, LOCK_STATUS.unlock);
        }
        ChargeSession chargeSession = getChargeSession();
        chargeSession.setGunMode(mode);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleBillPaidEvent(Uri uri) {
        ChargeSession chargeSession = getChargeSession();
        String billId = uri.getLastPathSegment();
        if (billId.equals(chargeSession.getChargeBill().getCharge_id())) {
            chargeSession.getChargeBill().setUser_balance(chargeSession.getChargeBill().getUser_balance() - chargeSession.getChargeBill().getTotal_fee());
            GUN_LOCK_MODE gunMode = chargeSession.getGunMode();
            if (GUN_LOCK_MODE.unlock_after_pay.equals(gunMode)) {
                LOCK_STATUS lockStatus = ChargeStatusCacheProvider.getInstance().getPortLockStatus(this.port);
                if (!LOCK_STATUS.disable.equals(lockStatus)) {
                    DeviceProxy.getInstance().unlockGun(this.port);
                    ChargeStatusCacheProvider.getInstance().updatePortLockStatus(this.port, LOCK_STATUS.unlock);
                }
            }
            Bundle data = new Bundle();
            data.putString("stage", CHARGE_UI_STAGE.paid.getStage());
            data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession.getChargeBill().getInit_type()));
            data.putString("chargeId", billId);
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleAuthRequest(DCAPMessage request, DCAPMessage confirm) {
        if (!isIdle(null, false)) {
            Log.w("ChargeHandler.handleAuthRequest", "busy now ! port: " + this.port);
            ChargeController.nackConfirm(confirm, 10000, "port busy", null);
            Bundle data = new Bundle();
            data.putString("stage", CHARGE_UI_STAGE.refuse.getStage());
            data.putString("cause", CHARGE_REFUSE_CAUSE.BUSY.getCause());
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
            return;
        }
        ErrorCode error = HardwareStatusCacheProvider.getInstance().getPortFault(this.port);
        if (error != null && error.getCode() != 200) {
            Log.w("ChargeHandler.handleAuthRequest", "port is except now ! port: " + this.port + ", error: " + error.toJson());
            ChargeController.nackConfirm(confirm, 10000, "port except", null);
            Bundle data2 = new Bundle();
            data2.putString("stage", CHARGE_UI_STAGE.refuse.getStage());
            data2.putString("cause", CHARGE_REFUSE_CAUSE.EXCEPT.getCause());
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data2);
        } else if (!ChargeStatusCacheProvider.getInstance().getPortSwitch(this.port)) {
            Log.w("ChargeHandler.handleAuthRequest", "port is forbidened now ! port: " + this.port);
            ChargeController.nackConfirm(confirm, 10000, "port forbidened", null);
            Bundle data3 = new Bundle();
            data3.putString("stage", CHARGE_UI_STAGE.refuse.getStage());
            data3.putString("cause", CHARGE_REFUSE_CAUSE.PORT_FORBIDEN.getCause());
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data3);
        } else {
            CAPMessage cap = (CAPMessage) request.getData();
            CAPDirectiveOption opt = cap.getOpt();
            AuthDirective auth = (AuthDirective) cap.getData();
            if (!"init".equals(opt.getAuth_id())) {
                Log.e("ChargeHandler.handleAuthRequest", "illegal auth id: " + opt.getAuth_id());
                ChargeController.nackConfirm(confirm, 10000, "illegal auth id", null);
                return;
            }
            CHARGE_INIT_TYPE initType = auth.getInit_type();
            String userType = auth.getUser_type();
            ChargeSession chargeSession = getChargeSession();
            chargeSession.getChargeBill().setInit_type(initType);
            chargeSession.getChargeBill().setUser_type(userType);
            chargeSession.getChargeBill().setUser_code(auth.getUser_code());
            chargeSession.setDevice_id(auth.getDevice_id());
            chargeSession.getChargeBill().setPort(auth.getPort());
            chargeSession.setConfirm4Auth(confirm);
            chargeSession.setPlugined(HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port));
            if (initType.equals(CHARGE_INIT_TYPE.nfc)) {
                NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(userType);
                if (nfcCardType == null) {
                    Log.w("ChargeHandler.handleAuthRequest", "illegal NFC type: " + nfcCardType);
                    ChargeController.nackConfirm(confirm, 10000, "illegal nfc card type", null);
                    clearChargeSession();
                    return;
                } else if (nfcCardType.equals(NFC_CARD_TYPE.U1)) {
                    ChargeController.ackConfirm(confirm, null);
                    this.status = CHARGE_FSM_STATUS.authed;
                    this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_STARTED;
                    this.handlerTimer.startTimer(10000L, MSG_TIMEOUT_AUTHED, null);
                    return;
                } else if (nfcCardType.equals(NFC_CARD_TYPE.U2)) {
                    handleU2CardAuthRequest();
                    return;
                } else if (nfcCardType.equals(NFC_CARD_TYPE.U3)) {
                    authIndicate(CHARGE_PLATFORM.xcharge, auth.getUser_data());
                    return;
                } else if (nfcCardType.equals(NFC_CARD_TYPE.CT_DEMO)) {
                    ChargeController.ackConfirm(confirm, null);
                    this.status = CHARGE_FSM_STATUS.authed;
                    this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_STARTED;
                    this.handlerTimer.startTimer(10000L, MSG_TIMEOUT_AUTHED, null);
                    return;
                } else if (nfcCardType.equals(NFC_CARD_TYPE.anyo1)) {
                    authIndicate(CHARGE_PLATFORM.anyo, null);
                    return;
                } else if (nfcCardType.equals(NFC_CARD_TYPE.anyo_svw)) {
                    ChargeController.ackConfirm(confirm, null);
                    this.status = CHARGE_FSM_STATUS.authed;
                    this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_STARTED;
                    this.handlerTimer.startTimer(10000L, MSG_TIMEOUT_AUTHED, null);
                    return;
                } else if (nfcCardType.equals(NFC_CARD_TYPE.cddz_1)) {
                    authIndicate(CHARGE_PLATFORM.cddz, null);
                    return;
                } else if (nfcCardType.equals(NFC_CARD_TYPE.ocpp)) {
                    authIndicate(CHARGE_PLATFORM.ocpp, null);
                    return;
                } else {
                    Log.w("ChargeHandler.handleAuthRequest", "not impleted NFC card type: " + nfcCardType);
                    ChargeController.nackConfirm(confirm, 10000, "not impleted NFC card type", null);
                    clearChargeSession();
                    return;
                }
            }
            Log.w("ChargeHandler.handleAuthRequest", "init type is not nfc, no auth progress !!!");
        }
    }

    private void handleU2CardAuthRequest() {
        ChargeSession chargeSession = getChargeSession();
        String userType = chargeSession.getChargeBill().getUser_type();
        String userCode = chargeSession.getChargeBill().getUser_code();
        DCAPMessage confirm = chargeSession.getConfirm4Auth();
        ArrayList<ChargeBill> unpaidBills = ChargeContentProxy.getInstance().getUnpaidBills(userType, userCode);
        if (unpaidBills != null && unpaidBills.size() > 0) {
            HashMap<String, Object> attach = new HashMap<>();
            attach.put("user_status", CHARGE_USER_STATUS.need_pay.getStatus());
            attach.put("bill_id", unpaidBills.get(0).getCharge_id());
            attach.put(ChargeStopCondition.TYPE_FEE, String.valueOf(unpaidBills.get(0).getTotal_fee()));
            ChargeController.nackConfirm(confirm, 10000, "unpaid user bill", attach);
            clearChargeSession();
            return;
        }
        ChargeController.ackConfirm(confirm, null);
        this.status = CHARGE_FSM_STATUS.authed;
        this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_STARTED;
        this.handlerTimer.startTimer(10000L, MSG_TIMEOUT_AUTHED, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleInitRequest(DCAPMessage request, DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) request.getData();
        CAPDirectiveOption opt = cap.getOpt();
        InitDirective init = (InitDirective) cap.getData();
        CHARGE_INIT_TYPE initType = init.getInit_type();
        String port = init.getPort();
        String userType = init.getUser_type();
        String userCode = init.getUser_code();
        String chargeId = opt.getCharge_id();
        CHARGE_PLATFORM platform = init.getCharge_platform();
        if (CHARGE_INIT_TYPE.nfc.equals(initType)) {
            if (!CHARGE_FSM_STATUS.authed.equals(this.status)) {
                Log.w("ChargeHandler.handleInitRequest", "refuse nfc init request, port: " + port + ", status: " + this.status.getStatus());
                refuseInit(FIN_MODE.busy, null, userType, userCode, chargeId, Long.valueOf(request.getSeq()));
                return;
            }
        } else if (!CHARGE_FSM_STATUS.idle.equals(this.status)) {
            Log.w("ChargeHandler.handleInitRequest", "refuse init request, port: " + port + ", status: " + this.status.getStatus());
            refuseInit(FIN_MODE.busy, null, userType, userCode, chargeId, Long.valueOf(request.getSeq()));
            return;
        } else {
            ErrorCode error = HardwareStatusCacheProvider.getInstance().getPortFault(this.port);
            if (error != null && error.getCode() != 200) {
                Log.w("ChargeHandler.handleInitRequest", "refuse init request for except now ! port: " + port + ", error: " + error.toJson());
                refuseInit(FIN_MODE.error, error, userType, userCode, chargeId, Long.valueOf(request.getSeq()));
                return;
            }
        }
        if (CHARGE_FSM_STATUS.idle.equals(this.status) && !ChargeStatusCacheProvider.getInstance().getPortSwitch(this.port)) {
            Log.w("ChargeHandler.handleInitRequest", "port is forbidened now ! port: " + port);
            refuseInit(FIN_MODE.port_forbiden, null, userType, userCode, chargeId, Long.valueOf(request.getSeq()));
            return;
        }
        ChargeSession chargeSession = getChargeSession();
        chargeSession.getChargeBill().setUser_type(userType);
        chargeSession.getChargeBill().setUser_code(userCode);
        chargeSession.setDevice_id(init.getDevice_id());
        chargeSession.getChargeBill().setPort(port);
        chargeSession.getChargeBill().setInit_type(initType);
        chargeSession.getChargeBill().setCharge_id(chargeId);
        chargeSession.getChargeBill().setUser_tc_type(init.getUser_tc_type());
        chargeSession.getChargeBill().setUser_tc_value(init.getUser_tc_value());
        chargeSession.getChargeBill().setUser_balance(init.getUser_balance());
        chargeSession.getChargeBill().setIs_free(init.getIs_free());
        chargeSession.getChargeBill().setBinded_user(init.getBinded_user());
        chargeSession.getChargeBill().setCharge_platform(platform);
        chargeSession.setPlugined(HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port));
        NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(userType);
        if (needFeeRata(platform, nfcCardType)) {
            FeeRate feeRate = agreeChargeFeeRate(initType, nfcCardType, port, init.getFee_rate());
            if (feeRate == null) {
                Log.w("ChargeHandler.handleInitRequest", "refuse init request for unavailable fee rate, port: " + port);
                refuseInit(FIN_MODE.no_feerate, null, userType, userCode, chargeId, Long.valueOf(request.getSeq()));
                if (platform.equals(CHARGE_PLATFORM.xcharge) && NFC_CARD_TYPE.U3.equals(nfcCardType)) {
                    Log.w("ChargeHandler.handleInitRequest", "no fee rate for U3, keep this status: " + this.status.getStatus() + ", port: " + this.port);
                    if (CHARGE_FSM_STATUS.authed.equals(this.status)) {
                        stopTimer(this.status);
                        this.handlerTimer.startTimer(10000L, MSG_TIMEOUT_AUTHED, null);
                        return;
                    }
                    return;
                } else if (CHARGE_FSM_STATUS.authed.equals(this.status)) {
                    stopTimer(this.status);
                    this.status = CHARGE_FSM_STATUS.idle;
                    this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_ENDED;
                    notifyChargeEnded2OSS();
                    return;
                } else {
                    return;
                }
            }
            chargeSession.getChargeBill().setFee_rate_id(feeRate.getFeeRateId());
            chargeSession.getChargeBill().setFee_rate(feeRate);
        }
        stopTimer(this.status);
        int timeoutPlugin = init.getTimeout_plugin();
        int timeoutChargeStart = init.getTimeout_start();
        int timeoutPlugout = init.getTimeout_plugout();
        if (timeoutPlugin > 0) {
            chargeSession.setTimeout_plugin(timeoutPlugin);
        }
        if (timeoutChargeStart > 0) {
            chargeSession.setTimeout_start(timeoutChargeStart);
        }
        if (timeoutPlugout > 0) {
            chargeSession.setTimeout_plugout(timeoutPlugout);
        }
        chargeSession.setUserReservedTime(init.getReserve_time());
        setPortLockMode(userType, initType);
        initAckIndicate(request.getSeq());
        this.status = CHARGE_FSM_STATUS.init_ack_sended;
        this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_STARTED;
        RequestSession requestSession = new RequestSession();
        requestSession.setRequest(request);
        requestSession.setConfirm(confirm);
        this.handlerTimer.startTimer(10000L, MSG_TIMEOUT_INIT_ACK_SENDED, requestSession);
    }

    private boolean needFeeRata(CHARGE_PLATFORM platform, NFC_CARD_TYPE cardType) {
        if (CHARGE_PLATFORM.xcharge.equals(platform)) {
            return (NFC_CARD_TYPE.U1.equals(cardType) || NFC_CARD_TYPE.CT_DEMO.equals(cardType)) ? false : true;
        } else if (CHARGE_PLATFORM.anyo.equals(platform)) {
            return false;
        } else {
            if (CHARGE_PLATFORM.cddz.equals(platform)) {
                return true;
            }
            return CHARGE_PLATFORM.ocpp.equals(platform) ? false : false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFinRequest(DCAPMessage request, DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) request.getData();
        FinDirective fin = (FinDirective) cap.getData();
        if (CHARGE_FSM_STATUS.init_ack_sended.equals(this.status) || CHARGE_FSM_STATUS.init_advert.equals(this.status) || CHARGE_FSM_STATUS.user_reserved.equals(this.status) || CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status) || CHARGE_FSM_STATUS.inited.equals(this.status) || CHARGE_FSM_STATUS.plugin.equals(this.status) || CHARGE_FSM_STATUS.reserve_wait.equals(this.status)) {
            stopTimer(this.status);
            this.handlerTimer.stopTimer(MSG_TIMEOUT_RESERVE_CHECK);
            cancelPortChargeAuth();
            if (CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status)) {
                DeviceProxy.getInstance().closeGunLed(this.port);
            }
            HashMap<String, Object> attach = new HashMap<>();
            attach.put("fin_mode", fin.getFin_mode().getMode());
            ErrorCode error = fin.getError();
            if (error != null) {
                attach.put("error", error.toJson());
            }
            ChargeController.ackConfirm(confirm, attach);
            this.status = CHARGE_FSM_STATUS.idle;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_ENDED;
            updateChargeData(false, null);
            notifyChargeSessionFinished();
            clearChargeSession();
            notifyChargeEnded2OSS();
            return;
        }
        ChargeSession chargeSession = getChargeSession();
        if ((CHARGE_INIT_TYPE.nfc.equals(chargeSession.getChargeBill().getInit_type()) || FIN_MODE.remote.equals(fin.getFin_mode()) || FIN_MODE.nfc.equals(fin.getFin_mode())) && (CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.paused.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status) || CHARGE_FSM_STATUS.stopped.equals(this.status) || CHARGE_FSM_STATUS.stop_sended.equals(this.status) || CHARGE_FSM_STATUS.fin_sended.equals(this.status))) {
            stopTimer(this.status);
            this.handlerTimer.stopTimer(MSG_TIMEOUT_CHARGE_STOP_DELAY);
            this.handlerTimer.stopTimer(MSG_TIMEOUT_PRESTOP_CHECK);
            this.handlerTimer.stopTimer(MSG_INTERVAL_DELAY_TIMING);
            this.handlerTimer.stopTimer(MSG_INTERVAL_CONDITION_TIMING);
            if (CHARGE_FSM_STATUS.stop_sended.equals(this.status) || CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status) || CHARGE_FSM_STATUS.paused.equals(this.status)) {
                chargeSession.getChargeBill().setStop_time(System.currentTimeMillis());
                int totalTime = new BigDecimal((chargeSession.getChargeBill().getStop_time() - chargeSession.getChargeBill().getStart_time()) / 1000).setScale(0, 4).intValue();
                chargeSession.getChargeBill().setTotal_time(totalTime);
            }
            cancelPortChargeAuth();
            HashMap<String, Object> attach2 = new HashMap<>();
            attach2.put("fin_mode", fin.getFin_mode().getMode());
            ErrorCode error2 = fin.getError();
            if (error2 != null) {
                attach2.put("error", error2.toJson());
            }
            ChargeController.ackConfirm(confirm, attach2);
            if (CHARGE_FSM_STATUS.fin_sended.equals(this.status)) {
                if (chargeSession.getChargeBill().getStop_cause() == null) {
                    if (FIN_MODE.remote.equals(fin.getFin_mode())) {
                        chargeSession.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.remote_user);
                    } else {
                        chargeSession.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.local_user);
                    }
                }
                this.status = CHARGE_FSM_STATUS.idle;
                this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_ENDED;
                updateChargeData(false, null);
                notifyChargeSessionFinished();
                clearChargeSession();
                notifyChargeEnded2OSS();
                return;
            }
            this.handlerTimer.stopTimer(MSG_TIMEOUT_CHARGE_FIN);
            this.handlerTimer.startTimer(TIMEOUT_CHARGE_FIN, MSG_TIMEOUT_CHARGE_FIN, fin);
            return;
        }
        Log.w("ChargeHandler.handleFinRequest", "ignore fin request, port: " + this.port + ", status: " + this.status.getStatus());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleStartRequest(DCAPMessage request, DCAPMessage confirm) {
        ChargeSession chargeSession = getChargeSession();
        if (CHARGE_FSM_STATUS.plugin.equals(this.status)) {
            stopTimer(this.status);
            CAPMessage cap = (CAPMessage) request.getData();
            StartDirective start = (StartDirective) cap.getData();
            chargeSession.getChargeBill().setUser_tc_type(start.getUser_tc_type());
            chargeSession.getChargeBill().setUser_tc_value(start.getUser_tc_value());
            startCharge();
            ChargeController.ackConfirm(confirm, null);
            this.handlerTimer.stopTimer(MSG_TIMEOUT_RESERVE_CHECK);
            this.handlerTimer.startTimer(2000L, MSG_TIMEOUT_RESERVE_CHECK, null);
        } else if (CHARGE_FSM_STATUS.inited.equals(this.status)) {
            if (HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port)) {
                ChargeController.ackConfirm(confirm, null);
            } else {
                ChargeController.nackConfirm(confirm, 10000, "port not plugin", null);
            }
        } else if (CHARGE_FSM_STATUS.user_reserved.equals(this.status)) {
            requestPortChargeAuth();
        } else {
            Log.w("ChargeHandler.handleStartRequest", "ignore start request, port: " + this.port + ", status: " + this.status.getStatus());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleConditionRequest(DCAPMessage request, DCAPMessage confirm) {
        if (CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status) || CHARGE_FSM_STATUS.paused.equals(this.status)) {
            ChargeSession chargeSession = getChargeSession();
            CAPMessage cap = (CAPMessage) request.getData();
            CAPDirectiveOption opt = cap.getOpt();
            String chargeId = opt.getCharge_id();
            if (chargeSession.getChargeBill().getCharge_id().equals(chargeId)) {
                ConditionDirective condition = (ConditionDirective) cap.getData();
                String conditionId = opt.getCondition_id();
                if (ConditionDirective.CONDITION_USER_STOP.equals(conditionId)) {
                    if (condition.getUserTcType() == null) {
                        Log.i("ChargeHandler.handleConditionRequest", "clear erver setted condition: " + chargeSession.getChargeBill().getUser_tc_type() + ", " + chargeSession.getChargeBill().getUser_tc_value());
                        chargeSession.getChargeBill().setUser_tc_type(null);
                        chargeSession.getChargeBill().setUser_tc_value(null);
                        ChargeController.ackConfirm(confirm, null);
                        return;
                    }
                    chargeSession.getChargeBill().setUser_tc_type(condition.getUserTcType());
                    chargeSession.getChargeBill().setUser_tc_value(condition.getUserTcValue());
                    if (chargeSession.isAnyErrorExist()) {
                        Log.i("ChargeHandler.handleConditionRequest", "in error, and only set condition: " + chargeSession.getChargeBill().getUser_tc_type() + ", " + chargeSession.getChargeBill().getUser_tc_value());
                        if (USER_TC_TYPE.time.equals(chargeSession.getChargeBill().getUser_tc_type())) {
                            this.handlerTimer.startTimer(1000L, MSG_INTERVAL_CONDITION_TIMING, null);
                        }
                        ChargeController.ackConfirm(confirm, null);
                        return;
                    }
                    USER_TC_TYPE utct = chargeSession.getChargeBill().getUser_tc_type();
                    if (USER_TC_TYPE.time.equals(utct)) {
                        handleConditionTiming();
                    } else if (USER_TC_TYPE.fee.equals(utct)) {
                        try {
                            int feeConditon = Integer.parseInt(chargeSession.getChargeBill().getUser_tc_value());
                            if (chargeSession.getChargeBill().getTotal_fee() >= feeConditon) {
                                Log.w("ChargeHandler.handleConditionRequest", "fee is more than user setted: " + feeConditon);
                                stopCharge();
                                chargeSession.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.user_set);
                            }
                        } catch (Exception e) {
                            Log.w("ChargeHandler.handleConditionRequest", Log.getStackTraceString(e));
                        }
                    } else if (USER_TC_TYPE.power.equals(utct)) {
                        try {
                            double powerConditon = Double.parseDouble(chargeSession.getChargeBill().getUser_tc_value());
                            if (chargeSession.getChargeBill().getTotal_power() >= powerConditon) {
                                Log.w("ChargeHandler.handleConditionRequest", "power is more than user setted: " + powerConditon);
                                stopCharge();
                                chargeSession.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.user_set);
                            }
                        } catch (Exception e2) {
                            Log.w("ChargeHandler.handleConditionRequest", Log.getStackTraceString(e2));
                        }
                    }
                    ChargeController.ackConfirm(confirm, null);
                    return;
                }
                Log.w("ChargeHandler.handleConditionRequest", "not supported condition: " + conditionId);
                ChargeController.nackConfirm(confirm, 10000, "not support", null);
                return;
            }
            Log.w("ChargeHandler.handleConditionRequest", "not current charge: " + chargeSession.getChargeBill().getCharge_id() + ", port: " + this.port);
        } else if (CHARGE_FSM_STATUS.user_reserved.equals(this.status)) {
            ChargeSession chargeSession2 = getChargeSession();
            CAPMessage cap2 = (CAPMessage) request.getData();
            CAPDirectiveOption opt2 = cap2.getOpt();
            String chargeId2 = opt2.getCharge_id();
            if (chargeSession2.getChargeBill().getCharge_id().equals(chargeId2)) {
                ConditionDirective condition2 = (ConditionDirective) cap2.getData();
                String conditionId2 = opt2.getCondition_id();
                if (ConditionDirective.CONDITION_USER_RESERVE.equals(conditionId2)) {
                    chargeSession2.setUserReservedTime(condition2.getReserveTime());
                    stopTimer(this.status);
                    Bundle data = new Bundle();
                    data.putString("stage", CHARGE_UI_STAGE.user_reserved.getStage());
                    data.putLong(ChargeStopCondition.TYPE_TIME, chargeSession2.getUserReservedTime().longValue());
                    data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession2.getChargeBill().getInit_type()));
                    data.putString("chargeId", chargeSession2.getChargeBill().getCharge_id());
                    UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
                    this.handlerTimer.startTimer(chargeSession2.getUserReservedTime().longValue() - System.currentTimeMillis(), MSG_TIMEOUT_USER_RESERVED, null);
                    ChargeController.ackConfirm(confirm, null);
                    return;
                }
                Log.w("ChargeHandler.handleConditionRequest", "not supported condition: " + conditionId2);
                ChargeController.nackConfirm(confirm, 10000, "not support", null);
                return;
            }
        } else {
            Log.w("ChargeHandler.handleConditionRequest", "invalid condition request, port: " + this.port + ", status: " + this.status.getStatus());
        }
        ChargeController.nackConfirm(confirm, 10000, "not found", null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleStopRequest(DCAPMessage request, DCAPMessage confirm) {
        ChargeSession chargeSession = getChargeSession();
        if (CHARGE_FSM_STATUS.charging.equals(this.status) || (CHARGE_FSM_STATUS.pre_stop.equals(this.status) && !chargeSession.isAnyErrorExist())) {
            stopCharge();
            chargeSession.setStop_request_seq(Long.valueOf(request.getSeq()));
            CHARGE_STOP_CAUSE stopCause = CHARGE_STOP_CAUSE.user;
            String from = request.getFrom();
            if (from.startsWith("user:" + CHARGE_USER_TYPE.nfc)) {
                stopCause = CHARGE_STOP_CAUSE.local_user;
            } else if (from.startsWith("user:")) {
                stopCause = CHARGE_STOP_CAUSE.remote_user;
            } else if (from.startsWith("server:")) {
                stopCause = CHARGE_STOP_CAUSE.system_user;
            }
            chargeSession.getChargeBill().setStop_cause(stopCause);
            this.status = CHARGE_FSM_STATUS.stop_sended;
            updateChargeData(false, null);
            RequestSession requestSession = new RequestSession();
            requestSession.setRequest(request);
            requestSession.setConfirm(confirm);
            this.handlerTimer.startTimer(5000L, MSG_TIMEOUT_STOP_SENDED, requestSession);
            return;
        }
        Log.w("ChargeHandler.handleStopRequest", "ignore stop request, port: " + this.port + ", status: " + this.status.getStatus());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleChargeRefuseEvent(DCAPMessage request, DCAPMessage confirm) {
        if (CHARGE_FSM_STATUS.idle.equals(this.status)) {
            CAPMessage cap = (CAPMessage) request.getData();
            EventDirective event = (EventDirective) cap.getData();
            CHARGE_REFUSE_CAUSE cause = event.getRefuse_cause();
            HashMap<String, Object> attch = event.getAttach();
            Bundle data = new Bundle();
            data.putString("stage", CHARGE_UI_STAGE.refuse.getStage());
            data.putString("cause", cause.getCause());
            if (attch != null) {
                for (String key : attch.keySet()) {
                    data.putString(key, attch.get(key).toString());
                }
            }
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleScanAdvertFinishedEvent(DCAPMessage request, DCAPMessage confirm) {
        handleInitAdertFinished();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void retryStopCharge(DCAPMessage request, DCAPMessage confirm) {
        stopCharge();
        getChargeSession().incStop_retry();
        RequestSession requestSession = new RequestSession();
        requestSession.setRequest(request);
        requestSession.setConfirm(confirm);
        this.handlerTimer.startTimer(5000L, MSG_TIMEOUT_STOP_SENDED, requestSession);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleAckResponse(DCAPMessage response) {
        CAPMessage cap = (CAPMessage) response.getData();
        CAPDirectiveOption opt = cap.getOpt();
        String peerOp = opt.getOp();
        if (CAPMessage.DIRECTIVE_INIT_ACK.equals(peerOp)) {
            handleInitAckResponse(response);
        } else if ("fin".equals(peerOp)) {
            handleFinResponse(response);
        } else if ("auth".equals(peerOp)) {
            handleAuthResponse(response, true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleNackResponse(DCAPMessage response) {
        CAPMessage cap = (CAPMessage) response.getData();
        CAPDirectiveOption opt = cap.getOpt();
        String peerOp = opt.getOp();
        if ("auth".equals(peerOp)) {
            handleAuthResponse(response, false);
        }
    }

    private void handleAuthResponse(DCAPMessage response, boolean isAck) {
        if (CHARGE_FSM_STATUS.auth_sended.equals(this.status)) {
            CAPMessage cap = (CAPMessage) response.getData();
            CAPDirectiveOption opt = cap.getOpt();
            ChargeSession chargeSession = getChargeSession();
            if (chargeSession.getExpected_resopnse() != null && opt.getSeq().equals(chargeSession.getExpected_resopnse())) {
                stopTimer(this.status);
                chargeSession.setExpected_resopnse(null);
                if (isAck) {
                    AckDirective ack = (AckDirective) cap.getData();
                    ChargeController.ackConfirm(chargeSession.getConfirm4Auth(), ack.getAttach());
                    this.status = CHARGE_FSM_STATUS.authed;
                    if (chargeSession.isPlugined()) {
                        this.ocppChargeStatus = OCPP_CHARGE_STATUS.TRANSACTION_STARTED;
                    }
                    this.handlerTimer.startTimer(10000L, MSG_TIMEOUT_AUTHED, null);
                    return;
                }
                NackDirective nack = (NackDirective) cap.getData();
                ChargeController.nackConfirm(chargeSession.getConfirm4Auth(), 10000, "server refused auth", nack.getAttach());
                Bundle data = new Bundle();
                data.putString("stage", CHARGE_UI_STAGE.refuse.getStage());
                HashMap<String, Object> attach = nack.getAttach();
                if (attach != null) {
                    String cause = (String) attach.get("cause");
                    if (!TextUtils.isEmpty(cause)) {
                        data.putString("cause", cause);
                        for (String key : attach.keySet()) {
                            if (!key.equals("cause")) {
                                data.putString(key, attach.get(key).toString());
                            }
                        }
                    } else {
                        data.putString("cause", CHARGE_REFUSE_CAUSE.AUTH_REFUSE.getCause());
                    }
                } else if (nack.getError() == 10004) {
                    data.putString("cause", CHARGE_REFUSE_CAUSE.NOT_PLUGGED.getCause());
                } else {
                    data.putString("cause", CHARGE_REFUSE_CAUSE.AUTH_REFUSE.getCause());
                }
                UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
                clearChargeSession();
                this.status = CHARGE_FSM_STATUS.idle;
                this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_ENDED;
                notifyChargeEnded2OSS();
                return;
            }
            Log.w("ChargeHandler.handleAuthResponse", "unhandle auth response, charge session: " + chargeSession.toJson() + ", response: " + response.toJson());
            return;
        }
        Log.w("ChargeHandler.handleAuthResponse", "ignore auth response, port: " + this.port + ", status: " + this.status.getStatus());
    }

    private void handleInitAckResponse(DCAPMessage response) {
        if (CHARGE_FSM_STATUS.init_ack_sended.equals(this.status)) {
            CAPMessage cap = (CAPMessage) response.getData();
            CAPDirectiveOption opt = cap.getOpt();
            ChargeSession chargeSession = getChargeSession();
            if (chargeSession.getExpected_resopnse() != null && opt.getSeq().equals(chargeSession.getExpected_resopnse()) && chargeSession.getChargeBill().getCharge_id().equals(opt.getCharge_id())) {
                if (chargeSession.isPlugined()) {
                    this.ocppChargeStatus = OCPP_CHARGE_STATUS.TRANSACTION_STARTED;
                }
                chargeSession.setExpected_resopnse(null);
                long initAdvertTime = getInitAdvertTime();
                if (initAdvertTime > 0) {
                    stopTimer(this.status);
                    enterInitAdvertStatus(5 + initAdvertTime);
                    return;
                } else if (chargeSession.getUserReservedTime() != null) {
                    stopTimer(this.status);
                    if (HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port)) {
                        enterUserReservedStatus();
                        return;
                    }
                    CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
                    if (CHARGE_PLATFORM.ocpp.equals(platform)) {
                        enterUserReservedStatus();
                        return;
                    } else {
                        enterUserReservedWaitPluginStatus();
                        return;
                    }
                } else {
                    chargeSession.getTimeout_start();
                    requestPortChargeAuth();
                    return;
                }
            }
            Log.w("ChargeHandler.handleInitAckResponse", "unhandle init_ack response, charge session: " + chargeSession.toJson() + ", response: " + response.toJson());
            return;
        }
        Log.w("ChargeHandler.handleInitAckResponse", "ignore init_ack response, port: " + this.port + ", status: " + this.status.getStatus());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleInitAdvertTimeout() {
        handleInitAdertFinished();
    }

    private void handleInitAdertFinished() {
        if (CHARGE_FSM_STATUS.init_advert.equals(this.status)) {
            stopTimer(this.status);
            ChargeSession chargeSession = getChargeSession();
            if (HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port) && chargeSession.getTimeout_start() > 0) {
                enterPluginStatus();
                return;
            } else if (chargeSession.getUserReservedTime() != null) {
                if (HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port)) {
                    enterUserReservedStatus();
                    return;
                }
                CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
                if (CHARGE_PLATFORM.ocpp.equals(platform)) {
                    enterUserReservedStatus();
                    return;
                } else {
                    enterUserReservedWaitPluginStatus();
                    return;
                }
            } else {
                requestPortChargeAuth();
                return;
            }
        }
        Log.w("ChargeHandler.handleInitAdertFinished", "unhandled init advert finished, port: " + this.port + ", status: " + this.status.getStatus());
    }

    private void enterInitAdvertStatus(long timeout) {
        ChargeSession chargeSession = getChargeSession();
        this.status = CHARGE_FSM_STATUS.init_advert;
        chargeSession.setTimeout_init_advert(timeout);
        updateChargeData(false, null);
        Bundle data = new Bundle();
        data.putString("stage", CHARGE_UI_STAGE.scan_advert.getStage());
        data.putLong(ChargeStopCondition.TYPE_TIME, timeout);
        data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession.getChargeBill().getInit_type()));
        data.putString("chargeId", chargeSession.getChargeBill().getCharge_id());
        UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
        this.handlerTimer.startTimer(1000 * timeout, MSG_TIMEOUT_INIT_ADVERT, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleUserReservedTimeout() {
        if (CHARGE_FSM_STATUS.user_reserved.equals(this.status)) {
            stopTimer(this.status);
            requestPortChargeAuth();
            return;
        }
        Log.w("ChargeHandler.handleUserReservedTimeout", "ignore user reserved timeout, port: " + this.port + ", status: " + this.status.getStatus());
    }

    private void enterUserReservedStatus() {
        DeviceProxy.getInstance().closeGunLed(this.port);
        ChargeSession chargeSession = getChargeSession();
        this.status = CHARGE_FSM_STATUS.user_reserved;
        updateChargeData(false, null);
        Bundle data = new Bundle();
        data.putString("stage", CHARGE_UI_STAGE.user_reserved.getStage());
        data.putLong(ChargeStopCondition.TYPE_TIME, chargeSession.getUserReservedTime().longValue());
        data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession.getChargeBill().getInit_type()));
        data.putString("chargeId", chargeSession.getChargeBill().getCharge_id());
        UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
        this.handlerTimer.startTimer(chargeSession.getUserReservedTime().longValue() - System.currentTimeMillis(), MSG_TIMEOUT_USER_RESERVED, null);
    }

    private void enterUserReservedWaitPluginStatus() {
        DeviceProxy.getInstance().openGunLed(this.port);
        ChargeSession chargeSession = getChargeSession();
        this.status = CHARGE_FSM_STATUS.user_reserve_wait_plugin;
        updateChargeData(false, null);
        Bundle data = new Bundle();
        data.putString("stage", CHARGE_UI_STAGE.inited.getStage());
        data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession.getChargeBill().getInit_type()));
        data.putString("chargeId", chargeSession.getChargeBill().getCharge_id());
        int timeout = chargeSession.getTimeout_plugin();
        if (timeout > 0) {
            data.putInt("waitPlugin", timeout);
            this.handlerTimer.startTimer(timeout * 1000, MSG_TIMEOUT_USER_RESERVE_WAIT_PLUGIN, null);
        }
        UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
    }

    private void enterInitedStatus() {
        ChargeSession chargeSession = getChargeSession();
        this.status = CHARGE_FSM_STATUS.inited;
        updateChargeData(false, null);
        Bundle data = new Bundle();
        data.putString("stage", CHARGE_UI_STAGE.inited.getStage());
        data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession.getChargeBill().getInit_type()));
        data.putString("chargeId", chargeSession.getChargeBill().getCharge_id());
        int timeout = chargeSession.getTimeout_plugin();
        if (timeout > 0) {
            data.putInt("waitPlugin", timeout);
            this.handlerTimer.startTimer(timeout * 1000, MSG_TIMEOUT_INITED, null);
        }
        UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
    }

    private long getInitAdvertTime() {
        ArrayList<ContentItem> scanAdvertSite;
        long totalTime = 0;
        try {
            if (ChargeStatusCacheProvider.getInstance().isAdvertEnabled() && (scanAdvertSite = RemoteSettingCacheProvider.getInstance().getAdvertContent(ADVERT_POLICY.scanAdvsite)) != null) {
                Iterator<ContentItem> it2 = scanAdvertSite.iterator();
                while (it2.hasNext()) {
                    ContentItem item = it2.next();
                    String localFile = item.getLocalPath();
                    if (!TextUtils.isEmpty(localFile) && new File(localFile).exists()) {
                        totalTime += item.getDuration();
                    }
                }
            }
            return totalTime;
        } catch (Exception e) {
            Log.w("ChargeHandler.getInitAdvertTime", Log.getStackTraceString(e));
            return 0L;
        }
    }

    private void handleFinResponse(DCAPMessage response) {
        if (CHARGE_FSM_STATUS.fin_sended.equals(this.status)) {
            CAPMessage cap = (CAPMessage) response.getData();
            CAPDirectiveOption opt = cap.getOpt();
            ChargeSession chargeSession = getChargeSession();
            if (chargeSession.getExpected_resopnse() != null && opt.getSeq().equals(chargeSession.getExpected_resopnse()) && ((chargeSession.getChargeBill().getCharge_id() == null && opt.getCharge_id() == null) || (chargeSession.getChargeBill().getCharge_id() != null && chargeSession.getChargeBill().getCharge_id().equals(opt.getCharge_id())))) {
                stopTimer(this.status);
                chargeSession.setExpected_resopnse(null);
                this.status = CHARGE_FSM_STATUS.idle;
                this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_ENDED;
                updateChargeData(false, null);
                notifyChargeSessionFinished();
                clearChargeSession();
                notifyChargeEnded2OSS();
                return;
            }
            Log.w("ChargeHandler.handleFinResponse", "unhandle fin response, charge session: " + chargeSession.toJson() + ", response: " + response.toJson());
            return;
        }
        Log.w("ChargeHandler.handleFinResponse", "ignore fin response, port: " + this.port + ", status: " + this.status.getStatus());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleAuthValid(PortStatus portStatus) {
        ChargeSession chargeSession = getChargeSession();
        chargeSession.setDeviceStatus(portStatus.getPortRuntimeStatus());
        if (CHARGE_FSM_STATUS.init_ack_sended.equals(this.status) || CHARGE_FSM_STATUS.init_advert.equals(this.status) || CHARGE_FSM_STATUS.user_reserved.equals(this.status)) {
            stopTimer(this.status);
            chargeSession.getChargeBill().setInit_time(System.currentTimeMillis());
            enterInitedStatus();
            if (HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port)) {
                this.handlerTimer.stopTimer(MSG_TIMEOUT_RESERVE_CHECK);
                this.handlerTimer.startTimer(2000L, MSG_TIMEOUT_RESERVE_CHECK, null);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleAuthInvalid(PortStatus portStatus) {
        this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGOUT);
        ChargeSession chargeSession = getChargeSession();
        DEVICE_STATUS deviceStatus = chargeSession.getDeviceStatus();
        if (deviceStatus != null && DEVICE_STATUS.idle.getStatus() != deviceStatus.getStatus()) {
            chargeSession.setDeviceStatus(portStatus.getPortRuntimeStatus());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePortPluginStatusChanged() {
        boolean isPlugin = HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port);
        ChargeSession chargeSession = getChargeSession();
        if (!chargeSession.isDeviceAuth()) {
            if (isPlugin) {
                this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGOUT);
                chargeSession.setPlugined(true);
                if (CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status)) {
                    stopTimer(this.status);
                    updateChargeData(false, null);
                    enterUserReservedStatus();
                    return;
                }
                return;
            }
            this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGOUT);
            CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
            if (CHARGE_PLATFORM.ocpp.equals(platform)) {
                if (!CHARGE_FSM_STATUS.user_reserved.equals(this.status) && chargeSession.isPlugined()) {
                    this.handlerTimer.startTimer(2000L, MSG_TIMEOUT_PLUGOUT, null);
                }
            } else if (chargeSession.isPlugined()) {
                this.handlerTimer.startTimer(2000L, MSG_TIMEOUT_PLUGOUT, null);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePlugin(PortStatus portStatus) {
        this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGOUT);
        ChargeSession chargeSession = getChargeSession();
        chargeSession.setDeviceStatus(portStatus.getPortRuntimeStatus());
        chargeSession.setPlugined(true);
        HardwareStatusCacheProvider.getInstance().updatePortPluginStatus(this.port, true);
        if (CHARGE_FSM_STATUS.init_advert.equals(this.status) || CHARGE_FSM_STATUS.user_reserved.equals(this.status) || CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status) || CHARGE_FSM_STATUS.inited.equals(this.status)) {
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.TRANSACTION_STARTED;
        }
        int startTimeout = chargeSession.getTimeout_start();
        if (chargeSession.isDeviceAuth()) {
            if (CHARGE_FSM_STATUS.inited.equals(this.status)) {
                if (startTimeout > 0) {
                    enterPluginStatus();
                    return;
                }
                stopTimer(this.status);
                updateChargeData(false, null);
                this.handlerTimer.stopTimer(MSG_TIMEOUT_RESERVE_CHECK);
                this.handlerTimer.startTimer(2000L, MSG_TIMEOUT_RESERVE_CHECK, null);
            } else if (CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status) || CHARGE_FSM_STATUS.paused.equals(this.status)) {
                this.handlerTimer.stopTimer(MSG_TIMEOUT_PRESTOP_CHECK);
                this.handlerTimer.startTimer(2000L, MSG_TIMEOUT_PRESTOP_CHECK, portStatus);
            } else {
                Log.w("ChargeHandler.handlePlugin", "ignore plugin event, port: " + this.port + ", status: " + this.status.getStatus());
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePrestopCheckTimeout(PortStatus portStatus) {
        ChargeSession chargeSession = getChargeSession();
        if (CHARGE_FSM_STATUS.charging.equals(this.status)) {
            LOCK_STATUS lockStatus = ChargeStatusCacheProvider.getInstance().getPortLockStatus(this.port);
            if (!LOCK_STATUS.disable.equals(lockStatus)) {
                DeviceProxy.getInstance().unlockGun(this.port);
                ChargeStatusCacheProvider.getInstance().updatePortLockStatus(this.port, LOCK_STATUS.unlock);
            }
            chargeSession.setEnteredNormalCharging(false);
            this.status = CHARGE_FSM_STATUS.pre_stop;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.ENERGY_OFFER_SUSPEND;
            updateChargeData(false, null);
            HashMap<String, Object> attach = new HashMap<>();
            attach.put("error", new ErrorCode(ErrorCode.EC_CAR_STOP_CHARGE).toJson());
            attach.put(ChargeStopCondition.TYPE_TIME, String.valueOf(System.currentTimeMillis()));
            eventIndicate(EventDirective.EVENT_CHARGE_PAUSE, attach);
        } else if (CHARGE_FSM_STATUS.pre_stop.equals(this.status)) {
            updateChargeData(false, null);
        } else if (CHARGE_FSM_STATUS.paused.equals(this.status)) {
            this.status = CHARGE_FSM_STATUS.pre_stop;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.ENERGY_OFFER_SUSPEND;
            updateChargeData(false, null);
            chargeSession.isAnyErrorExist();
        } else {
            Log.w("ChargeHandler.handlePrestopCheckTimeout", "ignore prestop check timeout, port: " + this.port + ", status: " + this.status.getStatus());
        }
    }

    private void enterPluginStatus() {
        ChargeSession chargeSession = getChargeSession();
        int startTimeout = chargeSession.getTimeout_start();
        stopTimer(this.status);
        this.status = CHARGE_FSM_STATUS.plugin;
        updateChargeData(false, null);
        eventIndicate(EventDirective.EVENT_PLUGIN, null);
        Bundle data = new Bundle();
        data.putString("stage", CHARGE_UI_STAGE.plugin.getStage());
        data.putInt("waitStart", startTimeout);
        data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession.getChargeBill().getInit_type()));
        data.putString("chargeId", chargeSession.getChargeBill().getCharge_id());
        UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
        this.handlerTimer.startTimer(startTimeout * 1000, MSG_TIMEOUT_PLUGIN, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleReserveCheckTimeout() {
        if (CHARGE_FSM_STATUS.inited.equals(this.status) || CHARGE_FSM_STATUS.plugin.equals(this.status)) {
            this.status = CHARGE_FSM_STATUS.reserve_wait;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.TRANSACTION_STARTED;
            this.handlerTimer.startTimer(60000L, MSG_TIMEOUT_RESERVE_WAIT, null);
            Bundle data = new Bundle();
            data.putString("stage", CHARGE_UI_STAGE.reserve.getStage());
            data.putBoolean("isClean", false);
            data.putInt("waitStart", 60);
            data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(this.chargeSession.getChargeBill().getInit_type()));
            data.putString("chargeId", this.chargeSession.getChargeBill().getCharge_id());
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
            return;
        }
        Log.w("ChargeHandler.handleReserveCheckTimeout", "ignore reserve check timeout, port: " + this.port + ", status: " + this.status.getStatus());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePlugout(PortStatus portStatus) {
        ChargeSession chargeSession = getChargeSession();
        chargeSession.setDeviceStatus(portStatus.getPortRuntimeStatus());
        this.handlerTimer.stopTimer(MSG_TIMEOUT_CHARGE_STOP_DELAY);
        this.handlerTimer.stopTimer(MSG_TIMEOUT_PRESTOP_CHECK);
        this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGOUT);
        if (chargeSession.isPlugined()) {
            this.handlerTimer.startTimer(2000L, MSG_TIMEOUT_PLUGOUT, null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePlugoutIndeed() {
        ChargeSession chargeSession = getChargeSession();
        chargeSession.setPlugined(false);
        HardwareStatusCacheProvider.getInstance().updatePortPluginStatus(this.port, false);
        if (CHARGE_FSM_STATUS.init_advert.equals(this.status) || CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status) || CHARGE_FSM_STATUS.user_reserved.equals(this.status) || CHARGE_FSM_STATUS.plugin.equals(this.status) || CHARGE_FSM_STATUS.reserve_wait.equals(this.status) || CHARGE_FSM_STATUS.inited.equals(this.status) || CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.paused.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status) || CHARGE_FSM_STATUS.stopped.equals(this.status) || CHARGE_FSM_STATUS.stop_sended.equals(this.status)) {
            stopTimer(this.status);
            this.handlerTimer.stopTimer(MSG_TIMEOUT_CHARGE_FIN);
            this.handlerTimer.stopTimer(MSG_TIMEOUT_RESERVE_CHECK);
            this.handlerTimer.stopTimer(MSG_INTERVAL_DELAY_TIMING);
            this.handlerTimer.stopTimer(MSG_INTERVAL_CONDITION_TIMING);
            if (CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status)) {
                DeviceProxy.getInstance().closeGunLed(this.port);
            }
            if (!CHARGE_FSM_STATUS.init_advert.equals(this.status) && !CHARGE_FSM_STATUS.user_reserved.equals(this.status) && !CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status) && !CHARGE_FSM_STATUS.inited.equals(this.status) && !CHARGE_FSM_STATUS.plugin.equals(this.status) && !CHARGE_FSM_STATUS.reserve_wait.equals(this.status) && chargeSession.getChargeBill().getStop_cause() == null) {
                chargeSession.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.plugout);
            }
            boolean isStopAmmeter = false;
            if (CHARGE_FSM_STATUS.stop_sended.equals(this.status) || CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status) || CHARGE_FSM_STATUS.paused.equals(this.status)) {
                chargeSession.getChargeBill().setStop_time(System.currentTimeMillis());
                int totalTime = new BigDecimal((chargeSession.getChargeBill().getStop_time() - chargeSession.getChargeBill().getStart_time()) / 1000).setScale(0, 4).intValue();
                chargeSession.getChargeBill().setTotal_time(totalTime);
                handleStopAmmeter(true);
                isStopAmmeter = true;
            } else if (CHARGE_FSM_STATUS.stopped.equals(this.status)) {
                handleStopAmmeter(true);
                isStopAmmeter = true;
            }
            if (chargeSession.getChargeBill() != null && CHARGE_INIT_TYPE.nfc.equals(chargeSession.getChargeBill().getInit_type()) && !chargeSession.isDeviceAuth() && (CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.paused.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status) || CHARGE_FSM_STATUS.stopped.equals(this.status) || CHARGE_FSM_STATUS.stop_sended.equals(this.status))) {
                if (chargeSession.getChargeBill().getDelay_start() > 0) {
                    calcDelayFee(true);
                }
                this.status = CHARGE_FSM_STATUS.idle;
                this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_ENDED;
                updateChargeData(isStopAmmeter, null);
                notifyChargeSessionFinished();
                clearChargeSession();
                notifyChargeEnded2OSS();
                return;
            }
            updateChargeData(isStopAmmeter, null);
            FIN_MODE finMode = FIN_MODE.normal;
            if (CHARGE_FSM_STATUS.init_advert.equals(this.status) || CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status) || CHARGE_FSM_STATUS.user_reserved.equals(this.status) || CHARGE_FSM_STATUS.inited.equals(this.status) || CHARGE_FSM_STATUS.plugin.equals(this.status) || CHARGE_FSM_STATUS.reserve_wait.equals(this.status)) {
                finMode = FIN_MODE.cancel;
            }
            finIndicate(finMode, null, chargeSession.getChargeBill().getUser_type(), chargeSession.getChargeBill().getUser_code(), chargeSession.getChargeBill().getCharge_id(), null);
            cancelPortChargeAuth();
            if (chargeSession.getChargeBill().getDelay_start() > 0) {
                calcDelayFee(true);
                return;
            }
            return;
        }
        Log.w("ChargeHandler.handlePlugout", "ignore indeeded plugout event, port: " + this.port + ", status: " + this.status.getStatus());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleChargeFinIndeed(FinDirective fin) {
        ChargeSession chargeSession = getChargeSession();
        if (chargeSession.getChargeBill() == null) {
            Log.w("ChargeHandler.handleChargeFinIndeed", "ignore charge fin request for charge session has been finished, port: " + this.port + ", status: " + this.status.getStatus());
        } else if ((CHARGE_INIT_TYPE.nfc.equals(chargeSession.getChargeBill().getInit_type()) || FIN_MODE.remote.equals(fin.getFin_mode()) || FIN_MODE.nfc.equals(fin.getFin_mode())) && !chargeSession.isDeviceAuth() && (CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.paused.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status) || CHARGE_FSM_STATUS.stopped.equals(this.status) || CHARGE_FSM_STATUS.stop_sended.equals(this.status))) {
            if (chargeSession.getChargeBill().getStop_cause() == null) {
                if (FIN_MODE.remote.equals(fin.getFin_mode())) {
                    chargeSession.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.remote_user);
                } else {
                    chargeSession.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.local_user);
                }
            }
            if (CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.paused.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status) || CHARGE_FSM_STATUS.stop_sended.equals(this.status)) {
                long now = System.currentTimeMillis();
                chargeSession.getChargeBill().setStop_time(now);
                int totalTime = new BigDecimal((chargeSession.getChargeBill().getStop_time() - chargeSession.getChargeBill().getStart_time()) / 1000).setScale(0, 4).intValue();
                chargeSession.getChargeBill().setTotal_time(totalTime);
                chargeSession.getChargeBill().setFin_time(now);
            }
            handleStopAmmeter(false);
            if (chargeSession.getChargeBill().getDelay_start() > 0) {
                calcDelayFee(true);
            }
            this.status = CHARGE_FSM_STATUS.idle;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_ENDED;
            updateChargeData(true, null);
            notifyChargeSessionFinished();
            clearChargeSession();
            notifyChargeEnded2OSS();
        } else {
            Log.w("ChargeHandler.handleChargeFinIndeed", "ignore charge fin request, port: " + this.port + ", status: " + this.status.getStatus());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleChargeStarted(boolean isChargeStartedEvent, PortStatus ps) {
        ChargeSession chargeSession = getChargeSession();
        chargeSession.setDeviceStatus(ps.getPortRuntimeStatus());
        this.handlerTimer.stopTimer(MSG_TIMEOUT_CHARGE_STOP_DELAY);
        this.handlerTimer.stopTimer(MSG_TIMEOUT_PRESTOP_CHECK);
        this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGOUT);
        if (CHARGE_FSM_STATUS.inited.equals(this.status) || CHARGE_FSM_STATUS.plugin.equals(this.status) || CHARGE_FSM_STATUS.reserve_wait.equals(this.status)) {
            PortStatus portChargeStatus = ps;
            if ((!isChargeStartedEvent || (isChargeStartedEvent && ps.getPower() == null)) && (portChargeStatus = DeviceProxy.getInstance().getPortChargeStatus(this.port)) == null) {
                Log.w("ChargeHandler.handleChargeStarted", "failed to get info from driver, port: " + this.port + ", status: " + this.status.getStatus());
            } else if (portChargeStatus.getPower() == null) {
                Log.w("ChargeHandler.handleChargeStarted", "failed to get ammeter, port: " + this.port + ", status: " + this.status.getStatus() + ", data: " + portChargeStatus.toJson());
            } else {
                stopTimer(this.status);
                this.handlerTimer.stopTimer(MSG_TIMEOUT_RESERVE_CHECK);
                this.startPower = portChargeStatus.getPower().doubleValue();
                Log.i("ChargeHandler.handleChargeStarted", "charge " + chargeSession.getChargeBill().getCharge_id() + " on port " + this.port + " start power: " + this.startPower);
                chargeSession.getChargeBill().setStart_ammeter(this.startPower);
                chargeSession.getChargeBill().setStart_time(System.currentTimeMillis());
                chargeSession.setLatestPowerMeterTimestamp(chargeSession.getChargeBill().getStart_time());
                this.ammeter = this.startPower;
                FeeRate feeRate = chargeSession.getChargeBill().getFee_rate();
                if (feeRate != null) {
                    if (feeRate.getPowerPrice() != null) {
                        chargeSession.getChargeBill().setPower_info(new ArrayList<>());
                    }
                    if (SERVICE_PRICE_UNIT.order.equals(feeRate.getServiceUnit())) {
                        double serviceFee = ((Double) feeRate.getServicePrice().get(0).get("price")).doubleValue();
                        chargeSession.getChargeBill().setService_fee(new BigDecimal(100.0d * serviceFee).setScale(0, 4).intValue());
                    } else if (SERVICE_PRICE_UNIT.degree.equals(feeRate.getServiceUnit()) && feeRate.getServicePrice() != null) {
                        chargeSession.getChargeBill().setService_info(new ArrayList<>());
                    }
                }
                this.status = CHARGE_FSM_STATUS.charging;
                this.ocppChargeStatus = OCPP_CHARGE_STATUS.ENERGY_OFFER;
                updateChargeData(false, null);
                eventIndicate(EventDirective.EVENT_CHARGE_START, null);
                USER_TC_TYPE utct = chargeSession.getChargeBill().getUser_tc_type();
                if (USER_TC_TYPE.time.equals(utct)) {
                    this.handlerTimer.startTimer(1000L, MSG_INTERVAL_CONDITION_TIMING, null);
                }
                Bundle data = new Bundle();
                data.putString("stage", CHARGE_UI_STAGE.charging.getStage());
                data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession.getChargeBill().getInit_type()));
                data.putString("chargeId", chargeSession.getChargeBill().getCharge_id());
                UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
            }
        } else if (CHARGE_FSM_STATUS.pre_stop.equals(this.status)) {
            stopTimer(this.status);
            this.status = CHARGE_FSM_STATUS.charging;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.ENERGY_OFFER;
            updateChargeData(false, null);
            HashMap<String, Object> attach = new HashMap<>();
            attach.put("error", String.valueOf((int) ErrorCode.EC_CAR_STOP_CHARGE));
            attach.put(ChargeStopCondition.TYPE_TIME, String.valueOf(System.currentTimeMillis()));
            eventIndicate(EventDirective.EVENT_CHARGE_RESUME, attach);
            Bundle data2 = new Bundle();
            data2.putString("stage", CHARGE_UI_STAGE.charging.getStage());
            data2.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession.getChargeBill().getInit_type()));
            data2.putString("chargeId", chargeSession.getChargeBill().getCharge_id());
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data2);
        } else if (CHARGE_FSM_STATUS.paused.equals(this.status)) {
            this.status = CHARGE_FSM_STATUS.charging;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.ENERGY_OFFER;
            HashMap<String, Object> attach2 = new HashMap<>();
            if (chargeSession.getLatestResumedError() != null) {
                attach2.put("error", String.valueOf(chargeSession.getLatestResumedError().getCode()));
            }
            attach2.put(ChargeStopCondition.TYPE_TIME, String.valueOf(System.currentTimeMillis()));
            eventIndicate(EventDirective.EVENT_CHARGE_RESUME, attach2);
        } else {
            Log.w("ChargeHandler.handleChargeStarted", "ignore charge start event, port: " + this.port + ", status: " + this.status.getStatus());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleChargeFull(PortStatus portStatus) {
        ChargeSession chargeSession = getChargeSession();
        chargeSession.setDeviceStatus(portStatus.getPortRuntimeStatus());
        this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGOUT);
        if (CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status)) {
            chargeSession.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.full);
            handleChargeStopped(true, portStatus);
            return;
        }
        Log.w("ChargeHandler.handleChargeStarted", "ignore charge full event, port: " + this.port + ", status: " + this.status.getStatus());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleSuspend(PortStatus portStatus) {
        this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGOUT);
        ChargeSession chargeSession = getChargeSession();
        chargeSession.setDeviceStatus(portStatus.getPortRuntimeStatus());
        DEVICE_STATUS status = portStatus.getPortRuntimeStatus();
        if (DEVICE_STATUS.emergencyStop.equals(status)) {
            chargeSession.setEmergencyStopped(true);
        }
        updateChargeData(false, null);
        if (DEVICE_STATUS.notInited.getStatus() <= status.getStatus()) {
            chargeSession.setAnyErrorExist(true);
            if (CHARGE_FSM_STATUS.reserve_wait.equals(this.status)) {
                this.handlerTimer.stopTimer(MSG_TIMEOUT_RESERVE_WAIT);
                Bundle data = new Bundle();
                data.putString("stage", CHARGE_UI_STAGE.reserve.getStage());
                data.putBoolean("isClean", true);
                data.putInt("waitStart", 60);
                data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession.getChargeBill().getInit_type()));
                data.putString("chargeId", chargeSession.getChargeBill().getCharge_id());
                UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
            } else if (CHARGE_FSM_STATUS.charging.equals(this.status)) {
                this.status = CHARGE_FSM_STATUS.paused;
                this.ocppChargeStatus = OCPP_CHARGE_STATUS.ENERGY_OFFER_SUSPEND;
                chargeSession.setEnteredNormalCharging(false);
                updateChargeData(false, null);
                HashMap<String, Object> attach = new HashMap<>();
                ErrorCode error = new ErrorCode(status.getStatus() + 30000);
                HashMap<String, Object> errData = new HashMap<>();
                errData.put("portChargeStatus", portStatus.toJson());
                error.setData(errData);
                attach.put("error", error.toJson());
                attach.put(ChargeStopCondition.TYPE_TIME, String.valueOf(System.currentTimeMillis()));
                eventIndicate(EventDirective.EVENT_CHARGE_PAUSE, attach);
            } else {
                CHARGE_FSM_STATUS.pre_stop.equals(this.status);
            }
        } else if (DEVICE_STATUS.chargeFull.equals(status) || DEVICE_STATUS.stopped.equals(status)) {
            handleChargeStopped(DEVICE_STATUS.chargeFull.equals(status), portStatus);
        } else {
            Log.w("ChargeHandler.handleSuspend", "ignore suspend event, port: " + this.port + ", charge status: " + this.status + ", suspend status: " + portStatus.toJson());
        }
    }

    private void handleErrorTolerance(PortStatus portStatus) {
        ArrayList<HashMap<String, Integer>> errorCnt = portStatus.getErrorCnt();
        boolean isToleranced = false;
        int errorCode = 200;
        int cnt = 0;
        if (errorCnt != null) {
            Iterator<HashMap<String, Integer>> it2 = errorCnt.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                HashMap<String, Integer> error = it2.next();
                if (error.containsKey("EmergencyStop")) {
                    if (error.get("EmergencyStop").intValue() >= 7) {
                        isToleranced = true;
                        errorCode = ErrorCode.EC_DEVICE_EMERGENCY_STOP;
                        cnt = error.get("EmergencyStop").intValue();
                        break;
                    }
                } else if (error.containsKey(PortStatus.KEY_ERROR_NO_GROUD)) {
                    if (error.get(PortStatus.KEY_ERROR_NO_GROUD).intValue() >= 1) {
                        isToleranced = true;
                        errorCode = ErrorCode.EC_DEVICE_NO_GROUND;
                        cnt = error.get(PortStatus.KEY_ERROR_NO_GROUD).intValue();
                        break;
                    }
                } else if (error.containsKey("OverVoltage")) {
                    if (error.get("OverVoltage").intValue() >= 7) {
                        isToleranced = true;
                        errorCode = ErrorCode.EC_DEVICE_VOLT_ERROR;
                        cnt = error.get("OverVoltage").intValue();
                        break;
                    }
                } else if (error.containsKey(PortStatus.KEY_ERROR_OVER_CURRENT)) {
                    if (error.get(PortStatus.KEY_ERROR_OVER_CURRENT).intValue() >= 3) {
                        isToleranced = true;
                        errorCode = ErrorCode.EC_DEVICE_AMP_ERROR;
                        cnt = error.get(PortStatus.KEY_ERROR_OVER_CURRENT).intValue();
                        break;
                    }
                } else if (error.containsKey(PortStatus.KEY_ERROR_OVER_TEMPERATURE)) {
                    if (error.get(PortStatus.KEY_ERROR_OVER_TEMPERATURE).intValue() >= 3) {
                        isToleranced = true;
                        errorCode = ErrorCode.EC_DEVICE_TEMP_ERROR;
                        cnt = error.get(PortStatus.KEY_ERROR_OVER_TEMPERATURE).intValue();
                        break;
                    }
                } else if (error.containsKey(PortStatus.KEY_ERROR_LEAKAGE_CURRENT)) {
                    if (error.get(PortStatus.KEY_ERROR_LEAKAGE_CURRENT).intValue() >= 1) {
                        isToleranced = true;
                        errorCode = ErrorCode.EC_DEVICE_POWER_LEAK;
                        cnt = error.get(PortStatus.KEY_ERROR_LEAKAGE_CURRENT).intValue();
                        break;
                    }
                } else if (error.containsKey(PortStatus.KEY_ERROR_CP_EXCEPT) && error.get(PortStatus.KEY_ERROR_CP_EXCEPT).intValue() >= 7) {
                    isToleranced = true;
                    errorCode = ErrorCode.EC_DEVICE_COMM_ERROR;
                    cnt = error.get(PortStatus.KEY_ERROR_CP_EXCEPT).intValue();
                    break;
                }
            }
        }
        ChargeSession chargeSession = getChargeSession();
        if (chargeSession.isDeviceAuth() && isToleranced) {
            Bundle data = new Bundle();
            data.putString("stage", CHARGE_UI_STAGE.error_stop.getStage());
            data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession.getChargeBill().getInit_type()));
            data.putInt("error", errorCode);
            data.putInt("cnt", cnt);
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
        }
    }

    private void handleStopAmmeter(boolean isPlugout) {
        ChargeSession chargeSession = getChargeSession();
        PortStatus fullPortChargeStatus = DeviceProxy.getInstance().getPortChargeStatus(this.port);
        if (fullPortChargeStatus == null) {
            Log.e("ChargeHandler.handleStopAmmeter", "failed to get info from driver, port: " + this.port + ", status: " + this.status.getStatus());
        } else if (fullPortChargeStatus.getPower() == null) {
            Log.e("ChargeHandler.handleStopAmmeter", "failed to get ammeter, port: " + this.port + ", status: " + this.status.getStatus() + ", data: " + fullPortChargeStatus.toJson());
        } else {
            double deltaPower = new BigDecimal(fullPortChargeStatus.getPower().doubleValue() - this.ammeter).setScale(2, 4).doubleValue();
            this.ammeter = fullPortChargeStatus.getPower().doubleValue();
            HardwareStatusCacheProvider.getInstance().updatePortAmmeter(this.port, this.ammeter);
            chargeSession.getChargeBill().setStop_ammeter(this.ammeter);
            if (deltaPower > 0.0d) {
                double newPower = chargeSession.getChargeBill().getTotal_power() + deltaPower;
                chargeSession.getChargeBill().setTotal_power(newPower);
                calcPowerAndServiceFee(true, deltaPower);
                PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port);
                portStatus.setAmmeter(Double.valueOf(this.ammeter));
                portStatus.setPower(Double.valueOf(newPower));
                int totalFee = chargeSession.getChargeBill().getTotal_fee();
                portStatus.setTotalFee(new BigDecimal(totalFee / 100.0f).setScale(2, 4).doubleValue());
                ChargeStatusCacheProvider.getInstance().updatePortStatus(this.port, portStatus);
            }
            LogUtils.applog("charge: " + chargeSession.getChargeBill().getCharge_id() + (isPlugout ? " plugout" : " stopped") + ", start ammeter: " + String.format(BaseActivity.TWODP, Double.valueOf(this.startPower)) + ", stop ammeter: " + String.format(BaseActivity.TWODP, Double.valueOf(this.ammeter)) + ", total power: " + String.format(BaseActivity.TWODP, Double.valueOf(chargeSession.getChargeBill().getTotal_power())));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleUpdate(PortStatus portStatus) {
        DEVICE_STATUS status;
        PortStatus fullPortChargeStatus = portStatus;
        if (portStatus.getPower() == null || portStatus.getVolts().get(0) == null || portStatus.getAmps().get(1) == null) {
            fullPortChargeStatus = DeviceProxy.getInstance().getPortChargeStatus(this.port);
            if (fullPortChargeStatus == null) {
                Log.w("ChargeHandler.handleUpdate", "failed to get info from driver, but use: " + portStatus.toJson() + " to continue, port: " + this.port + ", status: " + this.status.getStatus());
                fullPortChargeStatus = portStatus;
            } else {
                fullPortChargeStatus.getAmps().set(0, portStatus.getAmps().get(0));
                CHARGE_MODE chargeMode = portStatus.getChargeMode();
                fullPortChargeStatus.setChargeMode(chargeMode);
            }
        }
        if ((CHARGE_FSM_STATUS.paused.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status)) && (status = fullPortChargeStatus.getPortRuntimeStatus()) != null && DEVICE_STATUS.charging.getStatus() == status.getStatus()) {
            handleChargeStarted(false, fullPortChargeStatus);
        }
        ChargeSession chargeSession = getChargeSession();
        chargeSession.setDeviceStatus(fullPortChargeStatus.getPortRuntimeStatus());
        CHARGE_MODE chargeMode2 = fullPortChargeStatus.getChargeMode();
        if (CHARGE_MODE.normal_charge.equals(chargeMode2) && !chargeSession.isEnteredNormalCharging()) {
            chargeSession.setEnteredNormalCharging(true);
            LOCK_STATUS lockStatus = ChargeStatusCacheProvider.getInstance().getPortLockStatus(this.port);
            if (!LOCK_STATUS.disable.equals(lockStatus)) {
                DeviceProxy.getInstance().lockGun(this.port);
                ChargeStatusCacheProvider.getInstance().updatePortLockStatus(this.port, LOCK_STATUS.lock);
            }
        }
        if (CHARGE_FSM_STATUS.charging.equals(this.status)) {
            if (fullPortChargeStatus.getAmps().get(0) != null && fullPortChargeStatus.getAmps().get(0).doubleValue() > 0.0d) {
                this.ocppChargeStatus = OCPP_CHARGE_STATUS.ENERGY_TRANSFER;
            } else {
                this.ocppChargeStatus = OCPP_CHARGE_STATUS.ENERGY_OFFER;
            }
            double deltaPower = new BigDecimal(fullPortChargeStatus.getPower().doubleValue() - this.ammeter).setScale(2, 4).doubleValue();
            this.ammeter = fullPortChargeStatus.getPower().doubleValue();
            if (deltaPower > 0.0d) {
                double newPower = chargeSession.getChargeBill().getTotal_power() + deltaPower;
                fullPortChargeStatus.setPower(Double.valueOf(newPower));
                chargeSession.getChargeBill().setTotal_power(newPower);
                calcPowerAndServiceFee(false, deltaPower);
            } else {
                fullPortChargeStatus.setPower(Double.valueOf(chargeSession.getChargeBill().getTotal_power()));
            }
            USER_TC_TYPE utct = chargeSession.getChargeBill().getUser_tc_type();
            if (USER_TC_TYPE.power.equals(utct)) {
                try {
                    double powerConditon = Double.parseDouble(chargeSession.getChargeBill().getUser_tc_value());
                    if (chargeSession.getChargeBill().getTotal_power() >= powerConditon) {
                        Log.w("ChargeHandler.handleUpdate", "power is more than user setted: " + powerConditon);
                        stopCharge();
                        chargeSession.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.user_set);
                    }
                } catch (Exception e) {
                    Log.w("ChargeHandler.handleUpdate", Log.getStackTraceString(e));
                }
            }
        } else if (fullPortChargeStatus.getPower() != null) {
            this.ammeter = fullPortChargeStatus.getPower().doubleValue();
        }
        if (CHARGE_FSM_STATUS.idle.equals(this.status) || CHARGE_FSM_STATUS.auth_sended.equals(this.status) || CHARGE_FSM_STATUS.authed.equals(this.status) || CHARGE_FSM_STATUS.init_ack_sended.equals(this.status) || CHARGE_FSM_STATUS.init_advert.equals(this.status) || CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status) || CHARGE_FSM_STATUS.user_reserved.equals(this.status) || CHARGE_FSM_STATUS.plugin.equals(this.status) || CHARGE_FSM_STATUS.inited.equals(this.status) || CHARGE_FSM_STATUS.reserve_wait.equals(this.status)) {
            fullPortChargeStatus.setPower(Double.valueOf(0.0d));
        }
        updateChargeData(false, fullPortChargeStatus);
    }

    private void calcPowerAndServiceFee(boolean isStopAmmeter, double deltaPower) {
        ArrayList<HashMap<String, Object>> servicePriceSections;
        ChargeSession chargeSession = getChargeSession();
        FeeRate feeRate = chargeSession.getChargeBill().getFee_rate();
        if (feeRate != null) {
            long nowTimestamp = System.currentTimeMillis();
            HashMap<String, Object> deltaPowerMeter = new HashMap<>();
            deltaPowerMeter.put("begin", Long.valueOf(chargeSession.getLatestPowerMeterTimestamp()));
            deltaPowerMeter.put("end", Long.valueOf(nowTimestamp));
            deltaPowerMeter.put("meter", Double.valueOf(deltaPower));
            chargeSession.setLatestPowerMeterTimestamp(nowTimestamp);
            ArrayList<HashMap<String, Object>> powerPriceSections = feeRate.getPowerPrice();
            if (powerPriceSections != null) {
                BillUtils.updateMeterSections(deltaPowerMeter, chargeSession.getChargeBill().getPower_info(), powerPriceSections);
                double powerFee = BillUtils.calcIntervalCost(powerPriceSections, chargeSession.getChargeBill().getPower_info());
                chargeSession.getChargeBill().setPower_fee(new BigDecimal(100.0d * powerFee).setScale(0, 4).intValue());
            }
            if (SERVICE_PRICE_UNIT.degree.equals(feeRate.getServiceUnit()) && (servicePriceSections = feeRate.getServicePrice()) != null) {
                BillUtils.updateMeterSections(deltaPowerMeter, chargeSession.getChargeBill().getService_info(), servicePriceSections);
                double serviceFee = BillUtils.calcIntervalCost(servicePriceSections, chargeSession.getChargeBill().getService_info());
                chargeSession.getChargeBill().setService_fee(new BigDecimal(100.0d * serviceFee).setScale(0, 4).intValue());
            }
            chargeSession.getChargeBill().setTotal_fee(chargeSession.getChargeBill().getPower_fee() + chargeSession.getChargeBill().getService_fee() + chargeSession.getChargeBill().getDelay_fee());
            if (!isStopAmmeter) {
                NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(chargeSession.getChargeBill().getUser_type());
                if (NFC_CARD_TYPE.U2.equals(nfcCardType) && chargeSession.getChargeBill().getIs_free() != 1 && chargeSession.getChargeBill().getUser_balance() - chargeSession.getChargeBill().getTotal_fee() < 100) {
                    Log.w("ChargeHandler.calcPowerAneServiceFee", "if balanced for this charge, balance on card is less than 1 yuan !!!");
                    stopCharge();
                    chargeSession.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.no_balance);
                    return;
                }
                USER_TC_TYPE utct = chargeSession.getChargeBill().getUser_tc_type();
                if (USER_TC_TYPE.fee.equals(utct)) {
                    try {
                        int feeConditon = Integer.parseInt(chargeSession.getChargeBill().getUser_tc_value());
                        if (chargeSession.getChargeBill().getTotal_fee() >= feeConditon) {
                            Log.w("ChargeHandler.calcPowerAneServiceFee", "fee is more than user setted: " + feeConditon);
                            stopCharge();
                            chargeSession.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.user_set);
                        }
                    } catch (Exception e) {
                        Log.w("ChargeHandler.calcPowerAneServiceFee", Log.getStackTraceString(e));
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleResume(PortStatus portStatus) {
        this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGOUT);
        ChargeSession chargeSession = getChargeSession();
        chargeSession.setDeviceStatus(portStatus.getPortRuntimeStatus());
        DEVICE_STATUS status = portStatus.getPortRuntimeStatus();
        if (DEVICE_STATUS.notInited.getStatus() <= status.getStatus()) {
            chargeSession.setEmergencyStopped(false);
            chargeSession.setAnyErrorExist(false);
        }
        updateChargeData(false, null);
        if (DEVICE_STATUS.notInited.getStatus() <= status.getStatus()) {
            if (CHARGE_FSM_STATUS.reserve_wait.equals(this.status)) {
                this.handlerTimer.startTimer(60000L, MSG_TIMEOUT_RESERVE_WAIT, null);
                Bundle data = new Bundle();
                data.putString("stage", CHARGE_UI_STAGE.reserve.getStage());
                data.putBoolean("isClean", false);
                data.putInt("waitStart", 60);
                data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession.getChargeBill().getInit_type()));
                data.putString("chargeId", chargeSession.getChargeBill().getCharge_id());
                UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
            } else {
                CHARGE_FSM_STATUS.pre_stop.equals(this.status);
            }
            chargeSession.setLatestResumedError(new ErrorCode(status.getStatus() + 30000));
        } else if (DEVICE_STATUS.charging.getStatus() == status.getStatus()) {
            handleChargeStarted(false, portStatus);
        } else {
            Log.w("ChargeHandler.handleResume", "ignore resume event, port: " + this.port + ", charge status: " + this.status + ", resume status: " + portStatus.toJson());
        }
    }

    private boolean isPlugoutCp(int cp) {
        return cp == 12000;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleChargeStopEvent(PortStatus portStatus) {
        ChargeSession chargeSession = getChargeSession();
        chargeSession.setDeviceStatus(portStatus.getPortRuntimeStatus());
        if (chargeSession.isAnyErrorExist()) {
            this.handlerTimer.stopTimer(MSG_TIMEOUT_CHARGE_STOP_DELAY);
            handleChargePreStop(portStatus);
            return;
        }
        this.handlerTimer.stopTimer(MSG_TIMEOUT_CHARGE_STOP_DELAY);
        this.handlerTimer.startTimer(1000L, MSG_TIMEOUT_CHARGE_STOP_DELAY, portStatus);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleChargePreStop(PortStatus portStatus) {
        ChargeSession chargeSession = getChargeSession();
        chargeSession.setDeviceStatus(portStatus.getPortRuntimeStatus());
        this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGOUT);
        if (CHARGE_FSM_STATUS.charging.equals(this.status)) {
            LOCK_STATUS lockStatus = ChargeStatusCacheProvider.getInstance().getPortLockStatus(this.port);
            if (!LOCK_STATUS.disable.equals(lockStatus)) {
                DeviceProxy.getInstance().unlockGun(this.port);
                ChargeStatusCacheProvider.getInstance().updatePortLockStatus(this.port, LOCK_STATUS.unlock);
            }
            chargeSession.setEnteredNormalCharging(false);
            this.status = CHARGE_FSM_STATUS.pre_stop;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.ENERGY_OFFER_SUSPEND;
            updateChargeData(false, null);
            HashMap<String, Object> attach = new HashMap<>();
            attach.put("error", new ErrorCode(ErrorCode.EC_CAR_STOP_CHARGE).toJson());
            attach.put(ChargeStopCondition.TYPE_TIME, String.valueOf(System.currentTimeMillis()));
            eventIndicate(EventDirective.EVENT_CHARGE_PAUSE, attach);
        } else if (CHARGE_FSM_STATUS.stop_sended.equals(this.status)) {
            handleChargeStopped(false, portStatus);
        } else if (CHARGE_FSM_STATUS.pre_stop.equals(this.status)) {
            updateChargeData(false, null);
            chargeSession.isAnyErrorExist();
        } else if (CHARGE_FSM_STATUS.paused.equals(this.status)) {
            this.status = CHARGE_FSM_STATUS.pre_stop;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.ENERGY_OFFER_SUSPEND;
            updateChargeData(false, null);
            chargeSession.isAnyErrorExist();
        } else {
            Log.w("ChargeHandler.handleChargePreStop", "ignore charge stop event, port: " + this.port + ", status: " + this.status.getStatus());
        }
    }

    private void handleChargeStopped(boolean isFull, PortStatus portStatus) {
        Double price;
        ChargeSession chargeSession = getChargeSession();
        chargeSession.setDeviceStatus(portStatus.getPortRuntimeStatus());
        this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGOUT);
        this.handlerTimer.stopTimer(MSG_INTERVAL_CONDITION_TIMING);
        if (CHARGE_FSM_STATUS.stop_sended.equals(this.status) || CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status)) {
            stopTimer(this.status);
            boolean isUserStopped = false;
            if (CHARGE_FSM_STATUS.stop_sended.equals(this.status)) {
                isUserStopped = true;
            } else if (!isFull && chargeSession.getChargeBill().getStop_cause() == null) {
                chargeSession.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.car);
            }
            chargeSession.getChargeBill().setStop_time(System.currentTimeMillis());
            int totalTime = new BigDecimal((chargeSession.getChargeBill().getStop_time() - chargeSession.getChargeBill().getStart_time()) / 1000).setScale(0, 4).intValue();
            chargeSession.getChargeBill().setTotal_time(totalTime);
            handleStopAmmeter(false);
            this.status = CHARGE_FSM_STATUS.stopped;
            this.ocppChargeStatus = OCPP_CHARGE_STATUS.TRANSACTION_ENDED;
            updateChargeData(true, null);
            if (isUserStopped) {
                stopConfirm();
            } else {
                eventIndicate(EventDirective.EVENT_CHARGE_STOP, null);
            }
            NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(chargeSession.getChargeBill().getUser_type());
            LOCK_STATUS lockStatus = ChargeStatusCacheProvider.getInstance().getPortLockStatus(this.port);
            GUN_LOCK_MODE gunMode = chargeSession.getGunMode();
            if (GUN_LOCK_MODE.unlock_before_pay.equals(gunMode)) {
                if (!LOCK_STATUS.disable.equals(lockStatus)) {
                    DeviceProxy.getInstance().unlockGun(this.port);
                    ChargeStatusCacheProvider.getInstance().updatePortLockStatus(this.port, LOCK_STATUS.unlock);
                }
            } else if (GUN_LOCK_MODE.unlock_after_pay.equals(gunMode)) {
                if ((!NFC_CARD_TYPE.U2.equals(nfcCardType) || chargeSession.getChargeBill().getTotal_fee() <= 0 || chargeSession.getChargeBill().getIs_free() == 1) && !LOCK_STATUS.disable.equals(lockStatus)) {
                    DeviceProxy.getInstance().unlockGun(this.port);
                    ChargeStatusCacheProvider.getInstance().updatePortLockStatus(this.port, LOCK_STATUS.unlock);
                }
            } else if (GUN_LOCK_MODE.auto.equals(gunMode) && !LOCK_STATUS.disable.equals(lockStatus)) {
                DeviceProxy.getInstance().unlockGun(this.port);
                ChargeStatusCacheProvider.getInstance().updatePortLockStatus(this.port, LOCK_STATUS.unlock);
            }
            long nowTime = System.currentTimeMillis();
            double nowDelayPrice = getDelayPrice(nowTime);
            boolean willDelayHandleNow = nowDelayPrice > 0.0d;
            if (willDelayHandleNow) {
                chargeSession.setDelayWaitStarted(true);
                eventIndicate(EventDirective.EVENT_DEALY_WAIT_START, null);
                chargeSession.setDelayPrice(nowDelayPrice);
                updateChargeData(false, null);
            }
            Bundle data = new Bundle();
            data.putString("stage", CHARGE_UI_STAGE.stopped.getStage());
            data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession.getChargeBill().getInit_type()));
            data.putString("chargeId", chargeSession.getChargeBill().getCharge_id());
            data.putBoolean("willDelayHandleNow", willDelayHandleNow);
            int timeout = chargeSession.getTimeout_plugout();
            if (timeout > 0) {
                if (!willDelayHandleNow) {
                    long timeoutAt = nowTime + (timeout * 1000);
                    HashMap<String, Object> delayPriceSection = getDelayPriceSection(timeoutAt);
                    if (delayPriceSection != null && (price = (Double) delayPriceSection.get("price")) != null && price.doubleValue() > 0.0d) {
                        String begin = (String) delayPriceSection.get("begin");
                        long beginTs = TimeUtils.getDataTime(nowTime, begin);
                        if (beginTs < nowTime) {
                            beginTs += DateUtils.MILLIS_PER_DAY;
                        }
                        timeout = (int) (((beginTs - nowTime) / 1000) & XMSZHead.ID_BROADCAST);
                    }
                }
                data.putInt("waitPlugout", timeout);
                this.handlerTimer.startTimer(timeout * 1000, MSG_TIMEOUT_STOPPED, null);
            }
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
            return;
        }
        Log.w("ChargeHandler.handleChargeStopped", "ignore charge stop event, port: " + this.port + ", status: " + this.status.getStatus());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleDelayStarted() {
        ChargeSession chargeSession = getChargeSession();
        chargeSession.getChargeBill().setDelay_start(System.currentTimeMillis());
        FeeRate feeRate = chargeSession.getChargeBill().getFee_rate();
        if (feeRate != null && feeRate.getDelayPrice() != null) {
            chargeSession.setLatestDelayMeterTimestamp(chargeSession.getChargeBill().getDelay_start());
            chargeSession.getChargeBill().setDelay_info(new ArrayList<>());
            chargeSession.setDelayPrice(getDelayPrice(chargeSession.getChargeBill().getDelay_start()));
        }
        updateChargeData(false, null);
        eventIndicate("delay_start", null);
        Bundle data = new Bundle();
        data.putString("stage", CHARGE_UI_STAGE.delay.getStage());
        data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession.getChargeBill().getInit_type()));
        data.putString("chargeId", chargeSession.getChargeBill().getCharge_id());
        UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
        this.handler.sendEmptyMessageDelayed(MSG_INTERVAL_DELAY_TIMING, 1000L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleConditionTiming() {
        ChargeSession chargeSession = getChargeSession();
        USER_TC_TYPE utct = chargeSession.getChargeBill().getUser_tc_type();
        if (USER_TC_TYPE.time.equals(utct)) {
            if (CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.paused.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status)) {
                try {
                    if (CHARGE_FSM_STATUS.charging.equals(this.status) || (CHARGE_FSM_STATUS.pre_stop.equals(this.status) && !chargeSession.isAnyErrorExist())) {
                        long chargeTime = System.currentTimeMillis() - chargeSession.getChargeBill().getStart_time();
                        long conditionTime = (Integer.parseInt(chargeSession.getChargeBill().getUser_tc_value()) & (-1)) * 1000;
                        if (chargeTime >= conditionTime) {
                            Log.w("ChargeHandler.handleConditionTiming", "time is more than user setted: " + (conditionTime / 1000));
                            stopCharge();
                            chargeSession.getChargeBill().setStop_cause(CHARGE_STOP_CAUSE.user_set);
                            this.handlerTimer.stopTimer(MSG_INTERVAL_CONDITION_TIMING);
                            return;
                        }
                    }
                } catch (Exception e) {
                    Log.w("ChargeHandler.handleConditionTiming", Log.getStackTraceString(e));
                }
                this.handlerTimer.startTimer(1000L, MSG_INTERVAL_CONDITION_TIMING, null);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleDelayTiming() {
        ChargeSession chargeSession = getChargeSession();
        if (!chargeSession.isDelayWaitStarted()) {
            double nowDelayPrice = getDelayPrice(System.currentTimeMillis());
            if (nowDelayPrice > 0.0d) {
                this.handlerTimer.stopTimer(MSG_INTERVAL_DELAY_TIMING);
                eventIndicate(EventDirective.EVENT_DEALY_WAIT_START, null);
                chargeSession.setDelayWaitStarted(true);
                int waitPlugoutTimeout = chargeSession.getTimeout_plugout();
                this.handlerTimer.startTimer(waitPlugoutTimeout * 1000, MSG_TIMEOUT_STOPPED, null);
                chargeSession.setDelayPrice(nowDelayPrice);
                updateChargeData(false, null);
                Bundle data = new Bundle();
                data.putString("stage", CHARGE_UI_STAGE.delay_wait.getStage());
                data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession.getChargeBill().getInit_type()));
                data.putString("chargeId", chargeSession.getChargeBill().getCharge_id());
                data.putInt("waitPlugout", waitPlugoutTimeout);
                UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
            }
        } else if (chargeSession.isDelayStarted()) {
            calcDelayFee(false);
            if (chargeSession.getChargeBill().getTotal_delay() % 6 == 0) {
                updateChargeData(false, null);
            }
        }
    }

    private void calcDelayFee(boolean isEnd) {
        try {
            ChargeSession chargeSession = getChargeSession();
            long nowTimestamp = System.currentTimeMillis();
            long delayTime = nowTimestamp - chargeSession.getChargeBill().getDelay_start();
            if (isEnd && chargeSession.getChargeBill().getFin_time() > 0) {
                delayTime = chargeSession.getChargeBill().getFin_time() - chargeSession.getChargeBill().getDelay_start();
            }
            chargeSession.getChargeBill().setTotal_delay(new BigDecimal(((float) delayTime) / 1000.0f).setScale(0, 3).intValue());
            FeeRate feeRate = chargeSession.getChargeBill().getFee_rate();
            if (feeRate != null && DELAY_PRICE_UNIT.minute.equals(feeRate.getDelayUnit()) && feeRate.getDelayPrice() != null) {
                if ((chargeSession.getChargeBill().getTotal_delay() > 0 && isEnd) || (chargeSession.getChargeBill().getTotal_delay() > 0 && chargeSession.getChargeBill().getTotal_delay() % 60 == 0)) {
                    int latestCostDelay = chargeSession.getLatestCostDelay();
                    int minutes = new BigDecimal((chargeSession.getChargeBill().getTotal_delay() - latestCostDelay) / 60.0f).setScale(0, 3).intValue();
                    if (isEnd) {
                        minutes = new BigDecimal((chargeSession.getChargeBill().getTotal_delay() - latestCostDelay) / 60.0f).setScale(0, 4).intValue();
                    }
                    for (int i = 0; i < minutes; i++) {
                        HashMap<String, Object> deltaDelayMeter = new HashMap<>();
                        deltaDelayMeter.put("begin", Long.valueOf(chargeSession.getLatestDelayMeterTimestamp() + (i * 60 * 1000)));
                        deltaDelayMeter.put("end", Long.valueOf(chargeSession.getLatestDelayMeterTimestamp() + ((i + 1) * 60 * 1000)));
                        if (isEnd && i == minutes - 1) {
                            deltaDelayMeter.put("end", Long.valueOf(chargeSession.getChargeBill().getFin_time() > 0 ? chargeSession.getChargeBill().getFin_time() : nowTimestamp));
                        }
                        deltaDelayMeter.put("meter", Double.valueOf(1.0d));
                        BillUtils.updateMeterSections(deltaDelayMeter, chargeSession.getChargeBill().getDelay_info(), feeRate.getDelayPrice());
                    }
                    if (minutes > 0) {
                        chargeSession.setLatestDelayMeterTimestamp(nowTimestamp);
                        chargeSession.setLatestCostDelay(chargeSession.getChargeBill().getTotal_delay());
                        double delayFee = BillUtils.calcIntervalCost(feeRate.getDelayPrice(), chargeSession.getChargeBill().getDelay_info());
                        chargeSession.getChargeBill().setDelay_fee(new BigDecimal(100.0d * delayFee).setScale(0, 4).intValue());
                        chargeSession.getChargeBill().setTotal_fee(chargeSession.getChargeBill().getPower_fee() + chargeSession.getChargeBill().getService_fee() + chargeSession.getChargeBill().getDelay_fee());
                        chargeSession.setDelayPrice(getDelayPrice(nowTimestamp));
                    }
                }
            }
        } catch (Exception e) {
            Log.e("ChargeHandler.calcDelayFee", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkPlugin() {
        ChargeSession chargeSession = getChargeSession();
        if (!chargeSession.isDeviceAuth()) {
            Port runtimePortChargeStatus = DeviceProxy.getInstance().getPortRuntimeStatus(this.port);
            int standardCp = runtimePortChargeStatus.getCpVoltage().intValue();
            if (isPlugoutCp(standardCp)) {
                HardwareStatusCacheProvider.getInstance().updatePortPluginStatus(this.port, false);
            } else {
                HardwareStatusCacheProvider.getInstance().updatePortPluginStatus(this.port, true);
                if (CHARGE_FSM_STATUS.init_advert.equals(this.status) || CHARGE_FSM_STATUS.user_reserved.equals(this.status) || CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status) || CHARGE_FSM_STATUS.inited.equals(this.status)) {
                    this.ocppChargeStatus = OCPP_CHARGE_STATUS.TRANSACTION_STARTED;
                }
            }
            DeviceProxy.getInstance().notifyPortStatusUpdatedByCmd(runtimePortChargeStatus);
            DEVICE_STATUS status = runtimePortChargeStatus.getPortRuntimeStatus();
            if (chargeSession.getDeviceStatus() != null && chargeSession.getDeviceStatus().getStatus() != status.getStatus() && chargeSession.getDeviceStatus().getStatus() >= DEVICE_STATUS.notInited.getStatus() && status.getStatus() < DEVICE_STATUS.notInited.getStatus()) {
                Log.w("ChargeHandler.checkPlugin", "error: " + chargeSession.getDeviceStatus().getStatus() + " -> normal: " + status.getStatus());
                chargeSession.setDeviceStatus(status);
                chargeSession.setEmergencyStopped(false);
                chargeSession.setAnyErrorExist(false);
                PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port);
                portStatus.setPortRuntimeStatus(status);
                portStatus.setEmergencyStopStatus(SWITCH_STATUS.off);
                ChargeStatusCacheProvider.getInstance().updatePortStatus(this.port, portStatus);
            }
        }
    }

    private FeeRate agreeChargeFeeRate(CHARGE_INIT_TYPE initType, NFC_CARD_TYPE nfcType, String port, String expected) {
        HashMap<String, PortFeeRate> portsFeeRate;
        String localFeeRate = null;
        PortFeeRate LocalportFeeRate = null;
        if (initType.equals(CHARGE_INIT_TYPE.nfc) && NFC_CARD_TYPE.U2.equals(nfcType)) {
            FeeRateSetting feeRateSetting = LocalSettingCacheProvider.getInstance().getFeeRateSetting();
            if (feeRateSetting != null && (portsFeeRate = feeRateSetting.getPortsFeeRate()) != null && (LocalportFeeRate = portsFeeRate.get(port)) != null) {
                localFeeRate = LocalportFeeRate.getActiveFeeRateId();
            }
        } else {
            LocalportFeeRate = RemoteSettingCacheProvider.getInstance().getPortFeeRate(port);
            if (LocalportFeeRate != null) {
                localFeeRate = LocalportFeeRate.getActiveFeeRateId();
            }
        }
        String feeRate = expected;
        if (!TextUtils.isEmpty(feeRate)) {
            if (!feeRate.equals(localFeeRate)) {
                if (LocalportFeeRate != null) {
                    if (LocalportFeeRate.getFeeRates() != null) {
                        if (!LocalportFeeRate.getFeeRates().containsKey(feeRate)) {
                            feeRate = null;
                        }
                    } else {
                        feeRate = null;
                    }
                } else {
                    feeRate = null;
                }
                TextUtils.isEmpty(feeRate);
            }
        } else {
            feeRate = localFeeRate;
        }
        if (!TextUtils.isEmpty(feeRate)) {
            return LocalportFeeRate.getFeeRates().get(feeRate);
        }
        return null;
    }

    private NFC_CARD_TYPE getNFCTypeFromUserType(String userType) {
        if (TextUtils.isEmpty(userType)) {
            return null;
        }
        String[] userTypeSplit = userType.split("\\.");
        if (userTypeSplit.length == 2 && CHARGE_USER_TYPE.nfc.getUserType().equals(userTypeSplit[0])) {
            return NFC_CARD_TYPE.valueOf(userTypeSplit[1]);
        }
        return null;
    }

    private void authIndicate(CHARGE_PLATFORM platform, HashMap<String, Object> userData) {
        AuthDirective auth = new AuthDirective();
        ChargeSession chargeSession = getChargeSession();
        auth.setInit_type(chargeSession.getChargeBill().getInit_type());
        auth.setUser_type(chargeSession.getChargeBill().getUser_type());
        auth.setUser_code(chargeSession.getChargeBill().getUser_code());
        auth.setDevice_id(chargeSession.getDevice_id());
        auth.setPort(chargeSession.getChargeBill().getPort());
        auth.setUser_data(userData);
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setAuth_id("init");
        opt.setPort_id(chargeSession.getChargeBill().getPort());
        String to = "server:" + platform.getPlatform();
        DCAPMessage indicate = ChargeController.createIndicate(to, "auth", opt, auth);
        chargeSession.setExpected_resopnse(Long.valueOf(indicate.getSeq()));
        DCAPProxy.getInstance().sendIndicate(indicate);
        this.status = CHARGE_FSM_STATUS.auth_sended;
        this.ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_STARTED;
        this.handlerTimer.startTimer(10000L, MSG_TIMEOUT_AUTH_SENDED, chargeSession.getConfirm4Auth());
    }

    private boolean initAckIndicate(long ackInitRequestSeq) {
        InitAckDirective initAck = new InitAckDirective();
        ChargeSession chargeSession = getChargeSession();
        String userType = chargeSession.getChargeBill().getUser_type();
        String userCode = chargeSession.getChargeBill().getUser_code();
        initAck.setUser_type(userType);
        initAck.setUser_code(userCode);
        initAck.setDevice_id(chargeSession.getDevice_id());
        initAck.setPort(chargeSession.getChargeBill().getPort());
        initAck.setFee_rate(chargeSession.getChargeBill().getFee_rate_id());
        initAck.setUser_tc_type(chargeSession.getChargeBill().getUser_tc_type());
        initAck.setUser_tc_value(chargeSession.getChargeBill().getUser_tc_value());
        initAck.setUser_balance(chargeSession.getChargeBill().getUser_balance());
        initAck.setIs_free(chargeSession.getChargeBill().getIs_free());
        initAck.setBinded_user(chargeSession.getChargeBill().getBinded_user());
        initAck.setCharge_platform(chargeSession.getChargeBill().getCharge_platform());
        initAck.setTimeout_plugin(chargeSession.getTimeout_plugin());
        initAck.setTimeout_start(chargeSession.getTimeout_start());
        initAck.setTimeout_plugout(chargeSession.getTimeout_plugout());
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(getChargeSession().getChargeBill().getCharge_id());
        opt.setOp("init");
        opt.setSeq(Long.valueOf(ackInitRequestSeq));
        String to = "user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode;
        DCAPMessage indicate = ChargeController.createIndicate(to, CAPMessage.DIRECTIVE_INIT_ACK, opt, initAck);
        chargeSession.setExpected_resopnse(Long.valueOf(indicate.getSeq()));
        return DCAPProxy.getInstance().sendIndicate(indicate);
    }

    private boolean eventIndicate(String eventId, HashMap<String, Object> attach) {
        ChargeSession chargeSession = getChargeSession();
        EventDirective event = new EventDirective();
        event.setCharge_status(getChargeStatus(this.status));
        event.setStart_time(chargeSession.getChargeBill().getStart_time());
        event.setStop_time(chargeSession.getChargeBill().getStop_time());
        event.setTotal_power(chargeSession.getChargeBill().getTotal_power());
        event.setDelay_start(chargeSession.getChargeBill().getDelay_start());
        event.setTotal_delay(chargeSession.getChargeBill().getTotal_delay());
        event.setDelay_fee(chargeSession.getChargeBill().getDelay_fee());
        event.setAttach(attach);
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(chargeSession.getChargeBill().getCharge_id());
        opt.setEvent_id(eventId);
        String to = "user:" + chargeSession.getChargeBill().getUser_type() + MqttTopic.TOPIC_LEVEL_SEPARATOR + chargeSession.getChargeBill().getUser_code();
        DCAPMessage indicate = ChargeController.createIndicate(to, "event", opt, event);
        return DCAPProxy.getInstance().sendIndicate(indicate);
    }

    private boolean stopConfirm() {
        ChargeSession chargeSession = getChargeSession();
        Long stopRequestSeq = chargeSession.getStop_request_seq();
        if (stopRequestSeq != null) {
            StopDirective stop = new StopDirective();
            CAPDirectiveOption opt = new CAPDirectiveOption();
            opt.setCharge_id(chargeSession.getChargeBill().getCharge_id());
            opt.setOp("stop");
            opt.setSeq(chargeSession.getStop_request_seq());
            DCAPMessage confirm = createConfirmBySession("ack", opt, stop);
            boolean isOk = DCAPProxy.getInstance().sendConfirm(confirm);
            chargeSession.setStop_request_seq(null);
            return isOk;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startCharge() {
        DeviceProxy.getInstance().startCharge(this.port);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopCharge() {
        DeviceProxy.getInstance().stopCharge(this.port);
    }

    private void refuseInit(FIN_MODE mode, ErrorCode error, String userType, String userCode, String chargeId, Long nackInitRequestSeq) {
        FinDirective fin = new FinDirective();
        fin.setFin_mode(mode);
        fin.setError(error);
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(chargeId);
        if (nackInitRequestSeq != null) {
            opt.setOp("init");
            opt.setSeq(nackInitRequestSeq);
        }
        String to = "user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode;
        DCAPMessage indicate = ChargeController.createIndicate(to, "fin", opt, fin);
        DCAPProxy.getInstance().sendIndicate(indicate);
        String refuseCause = null;
        switch ($SWITCH_TABLE$com$xcharge$charger$core$type$FIN_MODE()[mode.ordinal()]) {
            case 4:
                refuseCause = CHARGE_REFUSE_CAUSE.PORT_FORBIDEN.getCause();
                break;
            case 5:
                refuseCause = CHARGE_REFUSE_CAUSE.BUSY.getCause();
                break;
            case 6:
                refuseCause = CHARGE_REFUSE_CAUSE.NO_FEERATE.getCause();
                break;
        }
        if (!TextUtils.isEmpty(refuseCause)) {
            Bundle data = new Bundle();
            data.putString("stage", CHARGE_UI_STAGE.refuse.getStage());
            data.putString("cause", refuseCause);
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void finIndicate(FIN_MODE mode, ErrorCode error, String userType, String userCode, String chargeId, Long nackInitRequestSeq) {
        FinDirective fin = new FinDirective();
        fin.setFin_mode(mode);
        fin.setError(error);
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(chargeId);
        if (nackInitRequestSeq != null) {
            opt.setOp("init");
            opt.setSeq(nackInitRequestSeq);
        }
        String to = "user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode;
        DCAPMessage indicate = ChargeController.createIndicate(to, "fin", opt, fin);
        DCAPProxy.getInstance().sendIndicate(indicate);
        getChargeSession().setExpected_resopnse(Long.valueOf(indicate.getSeq()));
        this.status = CHARGE_FSM_STATUS.fin_sended;
        this.handlerTimer.startTimer(10000L, MSG_TIMEOUT_FIN_SENDED, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateChargeData(boolean isStopAmmeter, PortStatus status) {
        ChargeSession chargeSession = getChargeSession();
        PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port);
        portStatus.setCharge_id(chargeSession.getChargeBill().getCharge_id());
        portStatus.setChargeStatus(getChargeStatus(this.status));
        portStatus.setOcppChargeStatus(this.ocppChargeStatus);
        portStatus.setAmmeter(Double.valueOf(this.ammeter));
        int totalFee = chargeSession.getChargeBill().getTotal_fee();
        portStatus.setTotalFee(new BigDecimal(totalFee / 100.0f).setScale(2, 4).doubleValue());
        if (status == null) {
            if (chargeSession.getDeviceStatus() != null) {
                portStatus.setPortRuntimeStatus(chargeSession.getDeviceStatus());
            }
            portStatus.setEmergencyStopStatus(chargeSession.isEmergencyStopped() ? SWITCH_STATUS.on : SWITCH_STATUS.off);
            portStatus.setPlugin(HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port));
            portStatus.setWaitPluginTimeout(chargeSession.getTimeout_plugin());
            portStatus.setWaitPlugoutTimeout(chargeSession.getTimeout_plugout());
            portStatus.setPower(Double.valueOf(chargeSession.getChargeBill().getTotal_power()));
            portStatus.setDelayPrice(chargeSession.getDelayPrice());
            portStatus.setDelayStartTime(chargeSession.getChargeBill().getDelay_start());
            portStatus.setTotalDelayFee(chargeSession.getChargeBill().getDelay_fee());
            portStatus.setChargeStartTime(chargeSession.getChargeBill().getStart_time());
            portStatus.setChargeStopTime(chargeSession.getChargeBill().getStop_time());
            portStatus.setChargeStopCause(chargeSession.getChargeBill().getStop_cause());
        } else {
            portStatus.setPortRuntimeStatus(status.getPortRuntimeStatus());
            portStatus.setAmps(status.getAmps());
            portStatus.setVolts(status.getVolts());
            portStatus.setKwatt(status.getKwatt());
            portStatus.setTemprature(status.getTemprature());
            portStatus.setCp(status.getCp());
            if (status.getErrorCnt() != null) {
                portStatus.setErrorCnt(status.getErrorCnt());
            }
            portStatus.setPower(status.getPower());
            portStatus.setChargeMode(status.getChargeMode());
        }
        if (CHARGE_FSM_STATUS.paused.equals(this.status) || CHARGE_FSM_STATUS.pre_stop.equals(this.status)) {
            PortStatus fullPortChargeStatus = DeviceProxy.getInstance().getPortChargeStatus(this.port);
            portStatus.setPortRuntimeStatus(fullPortChargeStatus.getPortRuntimeStatus());
            portStatus.setAmps(fullPortChargeStatus.getAmps());
            portStatus.setVolts(fullPortChargeStatus.getVolts());
            portStatus.setKwatt(Double.valueOf(0.0d));
            portStatus.setChargeMode(CHARGE_MODE.paused);
        }
        if (isStopAmmeter || CHARGE_FSM_STATUS.idle.equals(this.status) || CHARGE_FSM_STATUS.init_advert.equals(this.status) || CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status) || CHARGE_FSM_STATUS.user_reserved.equals(this.status) || CHARGE_FSM_STATUS.inited.equals(this.status) || CHARGE_FSM_STATUS.charging.equals(this.status) || CHARGE_FSM_STATUS.stopped.equals(this.status) || CHARGE_FSM_STATUS.fin_sended.equals(this.status)) {
            ChargeBill chargeBill = chargeSession.getChargeBill();
            if (chargeBill.getInit_time() > 0) {
                if (chargeBill.getStart_time() > 0) {
                    int totalTime = new BigDecimal((System.currentTimeMillis() - chargeBill.getStart_time()) / 1000).setScale(0, 4).intValue();
                    chargeBill.setTotal_time(totalTime);
                }
                Log.i("ChargeHandler.updateChargeData", String.valueOf(this.status.getStatus()) + " save charge and bill info: " + chargeBill.toJson());
                ChargeContentProxy.getInstance().saveChargeBill(chargeBill);
            } else if (CHARGE_FSM_STATUS.init_advert.equals(this.status) || CHARGE_FSM_STATUS.user_reserve_wait_plugin.equals(this.status) || CHARGE_FSM_STATUS.user_reserved.equals(this.status)) {
                Log.i("ChargeHandler.updateChargeData", String.valueOf(this.status.getStatus()) + " save charge and bill info: " + chargeBill.toJson());
                ChargeContentProxy.getInstance().saveChargeBill(chargeBill);
            } else if (CHARGE_FSM_STATUS.idle.equals(this.status) && !TextUtils.isEmpty(chargeBill.getCharge_id())) {
                Log.i("ChargeHandler.updateChargeData", String.valueOf(this.status.getStatus()) + " save charge and bill info: " + chargeBill.toJson());
                ChargeContentProxy.getInstance().saveChargeBill(chargeBill);
            }
        }
        ChargeStatusCacheProvider.getInstance().updatePortStatus(this.port, portStatus);
        if (CHARGE_FSM_STATUS.idle.equals(this.status)) {
            clearChargeStatusCache();
        }
    }

    private CHARGE_STATUS getChargeStatus(CHARGE_FSM_STATUS status) {
        switch ($SWITCH_TABLE$com$xcharge$charger$core$type$CHARGE_FSM_STATUS()[status.ordinal()]) {
            case 1:
                return CHARGE_STATUS.IDLE;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                return CHARGE_STATUS.CHARGE_START_WAITTING;
            case PortRuntimeData.STATUS_EX_11 /* 11 */:
            case PortRuntimeData.STATUS_EX_12 /* 12 */:
            case 13:
            case 14:
                return CHARGE_STATUS.CHARGING;
            case 15:
            case 16:
                return CHARGE_STATUS.CHARGE_STOP_WAITTING;
            default:
                return CHARGE_STATUS.IDLE;
        }
    }

    private void requestPortChargeAuth() {
        ChargeSession chargeSession = getChargeSession();
        if (!chargeSession.isDeviceAuth()) {
            if (chargeSession.getChargeBill().getInit_type().equals(CHARGE_INIT_TYPE.nfc)) {
                NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(chargeSession.getChargeBill().getUser_type());
                DeviceProxy.getInstance().authValid(this.port, nfcCardType.getType(), chargeSession.getChargeBill().getUser_code());
            } else {
                DeviceProxy.getInstance().authValid(this.port, chargeSession.getChargeBill().getUser_type(), chargeSession.getChargeBill().getUser_code());
            }
            LOCK_STATUS lockStatus = ChargeStatusCacheProvider.getInstance().getPortLockStatus(this.port);
            if (!LOCK_STATUS.disable.equals(lockStatus)) {
                DeviceProxy.getInstance().unlockGun(this.port);
                ChargeStatusCacheProvider.getInstance().updatePortLockStatus(this.port, LOCK_STATUS.unlock);
            }
            chargeSession.setDeviceAuth(true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void cancelPortChargeAuth() {
        ChargeSession chargeSession = getChargeSession();
        if (chargeSession.isDeviceAuth()) {
            GUN_LOCK_MODE gunMode = chargeSession.getGunMode();
            NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(chargeSession.getChargeBill().getUser_type());
            if (!NFC_CARD_TYPE.U2.equals(nfcCardType) || chargeSession.getChargeBill().getTotal_fee() <= 0 || chargeSession.getChargeBill().getIs_free() == 1 || !GUN_LOCK_MODE.unlock_after_pay.equals(gunMode)) {
                LOCK_STATUS lockStatus = ChargeStatusCacheProvider.getInstance().getPortLockStatus(this.port);
                if (!LOCK_STATUS.disable.equals(lockStatus)) {
                    DeviceProxy.getInstance().unlockGun(this.port);
                    ChargeStatusCacheProvider.getInstance().updatePortLockStatus(this.port, LOCK_STATUS.unlock);
                }
            }
            if (chargeSession.getChargeBill().getInit_type().equals(CHARGE_INIT_TYPE.nfc)) {
                DeviceProxy.getInstance().authInValid(this.port, nfcCardType.getType(), chargeSession.getChargeBill().getUser_code());
            } else {
                DeviceProxy.getInstance().authInValid(this.port, chargeSession.getChargeBill().getUser_type(), chargeSession.getChargeBill().getUser_code());
            }
            chargeSession.setDeviceAuth(false);
            chargeSession.getChargeBill().setFin_time(System.currentTimeMillis());
            chargeSession.getChargeBill().setBalance_flag(1);
            chargeSession.setDeviceStatus(DEVICE_STATUS.idle);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyChargeSessionFinished() {
        ChargeSession chargeSession = getChargeSession();
        ChargeBill chargeBill = chargeSession.getChargeBill();
        if (chargeBill.getStop_time() > 0) {
            Bundle data = new Bundle();
            data.putString("stage", CHARGE_UI_STAGE.billed.getStage());
            data.putBoolean("isNFC", CHARGE_INIT_TYPE.nfc.equals(chargeSession.getChargeBill().getInit_type()));
            data.putString("chargeId", chargeSession.getChargeBill().getCharge_id());
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data);
            return;
        }
        Bundle data2 = new Bundle();
        data2.putString("stage", CHARGE_UI_STAGE.ready.getStage());
        UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.charge, data2);
    }

    public DCAPMessage createConfirmBySession(String op, CAPDirectiveOption opt, Object data) {
        DCAPMessage confirm = new DCAPMessage();
        CAPMessage cap = new CAPMessage();
        ChargeSession chargeSession = getChargeSession();
        String from = "device:" + chargeSession.getDevice_id();
        String to = "user:" + chargeSession.getChargeBill().getUser_type() + MqttTopic.TOPIC_LEVEL_SEPARATOR + chargeSession.getChargeBill().getUser_code();
        confirm.setFrom(from);
        confirm.setTo(to);
        confirm.setType("cap");
        confirm.setCtime(System.currentTimeMillis());
        confirm.setSeq(Sequence.getCoreDCAPSequence());
        cap.setOp(op);
        cap.setOpt(opt);
        cap.setData(data);
        confirm.setData(cap);
        return confirm;
    }

    private void stopTimer(CHARGE_FSM_STATUS status) {
        switch ($SWITCH_TABLE$com$xcharge$charger$core$type$CHARGE_FSM_STATUS()[status.ordinal()]) {
            case 2:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_AUTH_SENDED);
                return;
            case 3:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_AUTHED);
                return;
            case 4:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_INIT_ACK_SENDED);
                return;
            case 5:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_INIT_ADVERT);
                return;
            case 6:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_USER_RESERVED);
                return;
            case 7:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_USER_RESERVE_WAIT_PLUGIN);
                return;
            case 8:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_INITED);
                return;
            case 9:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_PLUGIN);
                return;
            case 10:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_RESERVE_WAIT);
                return;
            case PortRuntimeData.STATUS_EX_11 /* 11 */:
            case 13:
            default:
                return;
            case PortRuntimeData.STATUS_EX_12 /* 12 */:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_PRE_STOP);
                return;
            case 14:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_STOP_SENDED);
                return;
            case 15:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_STOPPED);
                return;
            case 16:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_FIN_SENDED);
                return;
        }
    }

    private void clearChargeStatusCache() {
        PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port);
        portStatus.setCharge_id(null);
        portStatus.setChargeMode(null);
        portStatus.setChargeStartTime(0L);
        portStatus.setChargeStopTime(0L);
        portStatus.setChargeStopCause(null);
        portStatus.setChargeStatus(CHARGE_STATUS.IDLE);
        portStatus.setOcppChargeStatus(OCPP_CHARGE_STATUS.SESSION_ENDED);
        portStatus.setKwatt(null);
        portStatus.setTemprature(null);
        portStatus.setPower(Double.valueOf(0.0d));
        portStatus.setDelayPrice(0.0d);
        portStatus.setTotalFee(0.0d);
        portStatus.setWaitPluginTimeout(0);
        portStatus.setWaitPlugoutTimeout(0);
        ChargeStatusCacheProvider.getInstance().updatePortStatus(this.port, portStatus);
    }

    private double getDelayPrice(long time) {
        HashMap<String, Object> delayPriceSection;
        Double price;
        ChargeSession chargeSession = getChargeSession();
        FeeRate feeRate = chargeSession.getChargeBill().getFee_rate();
        if (feeRate == null || feeRate.getDelayPrice() == null || (delayPriceSection = BillUtils.getPriceSection(time, feeRate.getDelayPrice())) == null || (price = (Double) delayPriceSection.get("price")) == null || price.doubleValue() <= 0.0d) {
            return 0.0d;
        }
        return price.doubleValue();
    }

    private HashMap<String, Object> getDelayPriceSection(long time) {
        ChargeSession chargeSession = getChargeSession();
        FeeRate feeRate = chargeSession.getChargeBill().getFee_rate();
        if (feeRate == null || feeRate.getDelayPrice() == null) {
            return null;
        }
        return BillUtils.getPriceSection(time, feeRate.getDelayPrice());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyChargeEnded2OSS() {
        OSSController.getInstance().sendMessage(OSSController.getInstance().obtainMessage(OSSController.MSG_CHARGE_TO_IDLE, this.port));
    }
}