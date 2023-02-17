package com.xcharge.charger.device.c2.nfc.charge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.api.LocalIdGenerator;
import com.xcharge.charger.core.api.Sequence;
import com.xcharge.charger.core.api.bean.AckDirective;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.NackDirective;
import com.xcharge.charger.core.api.bean.cap.AuthDirective;
import com.xcharge.charger.core.api.bean.cap.CAPDirectiveOption;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.core.api.bean.cap.EventDirective;
import com.xcharge.charger.core.api.bean.cap.FinDirective;
import com.xcharge.charger.core.api.bean.cap.InitAckDirective;
import com.xcharge.charger.core.api.bean.cap.InitDirective;
import com.xcharge.charger.core.api.bean.cap.QueryDirective;
import com.xcharge.charger.core.api.bean.cap.StopDirective;
import com.xcharge.charger.core.type.FIN_MODE;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.CHARGE_INIT_TYPE;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.CHARGE_STATUS;
import com.xcharge.charger.data.bean.type.CHARGE_USER_STATUS;
import com.xcharge.charger.data.bean.type.CHARGE_USER_TYPE;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.bean.type.USER_TC_TYPE;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.LocalSettingCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.data.proxy.PortStatusObserver;
import com.xcharge.charger.device.c2.bean.AuthSign;
import com.xcharge.charger.device.c2.bean.NFCEventData;
import com.xcharge.charger.device.c2.bean.XSign;
import com.xcharge.charger.device.c2.nfc.C2NFCAgent;
import com.xcharge.charger.device.c2.nfc.NFCUtils;
import com.xcharge.charger.device.c2.nfc.charge.type.NFC_CHARGE_STATUS;
import com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.HandlerTimer;
import com.xcharge.common.utils.LogUtils;
import java.util.HashMap;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* loaded from: classes.dex */
public class C2NFCChargeHandler {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_USER_STATUS = null;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$device$c2$nfc$charge$type$NFC_CHARGE_STATUS = null;
    public static final int MSG_ANYO1_CHARGE = 28678;
    public static final int MSG_ANYO_SVW_CHARGE = 28680;
    public static final int MSG_CDDZ1_CHARGE = 28679;
    public static final int MSG_CT_DEMO_CHARGE = 28681;
    public static final int MSG_DCAP_CONFIRM = 28673;
    public static final int MSG_DCAP_INDICATE = 28674;
    public static final int MSG_OCPP_CHARGE = 28688;
    public static final int MSG_TIMEOUT_AUTH_SENDED = 28704;
    public static final int MSG_TIMEOUT_DCAP_REQUEST = 28709;
    public static final int MSG_TIMEOUT_FIN_SENDED = 28708;
    public static final int MSG_TIMEOUT_INIT_SENDED = 28705;
    public static final int MSG_TIMEOUT_STOP_SENDED = 28707;
    public static final int MSG_U1_CHARGE = 28675;
    public static final int MSG_U2_CHARGE = 28676;
    public static final int MSG_U3_CHARGE = 28677;
    public static final long TIMEOUT_AUTH_SENDED = 10000;
    public static final long TIMEOUT_DEFAULT_DCAP_REQUEST = 30000;
    public static final long TIMEOUT_FIN_SENDED = 10000;
    public static final long TIMEOUT_INIT_SENDED = 10000;
    public static final long TIMEOUT_STOP_SENDED = 10000;
    private Context context = null;
    private String port = null;
    private DCAPMessageReceiver dcapMessageReceiver = null;
    private HashMap<String, DCAPMessage> sendDCAPReqestState = null;
    private HandlerThread thread = null;
    private MsgHandler handler = null;
    private NFC_CHARGE_STATUS status = null;
    private NFCChargeSession chargeSession = null;
    private HandlerTimer handlerTimer = null;
    private PortStatusObserver portStatusObserver = null;

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_USER_STATUS() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_USER_STATUS;
        if (iArr == null) {
            iArr = new int[CHARGE_USER_STATUS.valuesCustom().length];
            try {
                iArr[CHARGE_USER_STATUS.illegal.ordinal()] = 5;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[CHARGE_USER_STATUS.need_pay.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[CHARGE_USER_STATUS.need_queue.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[CHARGE_USER_STATUS.need_rsrv.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[CHARGE_USER_STATUS.normal.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_USER_STATUS = iArr;
        }
        return iArr;
    }

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$device$c2$nfc$charge$type$NFC_CHARGE_STATUS() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$device$c2$nfc$charge$type$NFC_CHARGE_STATUS;
        if (iArr == null) {
            iArr = new int[NFC_CHARGE_STATUS.valuesCustom().length];
            try {
                iArr[NFC_CHARGE_STATUS.auth_sended.ordinal()] = 2;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[NFC_CHARGE_STATUS.charging.ordinal()] = 5;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[NFC_CHARGE_STATUS.fin_sended.ordinal()] = 8;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[NFC_CHARGE_STATUS.idle.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[NFC_CHARGE_STATUS.init_sended.ordinal()] = 3;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[NFC_CHARGE_STATUS.inited.ordinal()] = 4;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[NFC_CHARGE_STATUS.stop_sended.ordinal()] = 6;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[NFC_CHARGE_STATUS.stopped.ordinal()] = 7;
            } catch (NoSuchFieldError e8) {
            }
            $SWITCH_TABLE$com$xcharge$charger$device$c2$nfc$charge$type$NFC_CHARGE_STATUS = iArr;
        }
        return iArr;
    }

    /* loaded from: classes.dex */
    private class DCAPMessageReceiver extends BroadcastReceiver {
        private DCAPMessageReceiver() {
        }

        /* synthetic */ DCAPMessageReceiver(C2NFCChargeHandler c2NFCChargeHandler, DCAPMessageReceiver dCAPMessageReceiver) {
            this();
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(DCAPMessage.ACTION_DCAP_CONFIRM)) {
                String body = intent.getStringExtra("body");
                C2NFCChargeHandler.this.sendMessage(C2NFCChargeHandler.this.handler.obtainMessage(C2NFCChargeHandler.MSG_DCAP_CONFIRM, body));
            } else if (action.equals(DCAPMessage.ACTION_DCAP_INDICATE)) {
                String body2 = intent.getStringExtra("body");
                C2NFCChargeHandler.this.sendMessage(C2NFCChargeHandler.this.handler.obtainMessage(C2NFCChargeHandler.MSG_DCAP_INDICATE, body2));
            }
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
                    case C2NFCChargeHandler.MSG_DCAP_CONFIRM /* 28673 */:
                        Log.i("C2NFCChargeHandler.handleMessage", "receive DCAP Confirm: " + ((String) msg.obj));
                        C2NFCChargeHandler.this.handleConfirm((String) msg.obj);
                        break;
                    case C2NFCChargeHandler.MSG_DCAP_INDICATE /* 28674 */:
                        Log.i("C2NFCChargeHandler.handleMessage", "receive DCAP Indicate: " + ((String) msg.obj));
                        C2NFCChargeHandler.this.handleIndicate((String) msg.obj);
                        break;
                    case C2NFCChargeHandler.MSG_U1_CHARGE /* 28675 */:
                        Bundle data = msg.getData();
                        Log.i("C2NFCChargeHandler.handleMessage", "enter U1 charge: " + data.toString());
                        C2NFCChargeHandler.this.handleU1ChargeMsg(data.getString(ContentDB.NFCConsumeFailCacheTable.UUID), data.getString("cardno"));
                        break;
                    case C2NFCChargeHandler.MSG_U2_CHARGE /* 28676 */:
                        Bundle data2 = msg.getData();
                        Log.i("C2NFCChargeHandler.handleMessage", "enter U2 charge: " + data2.toString());
                        XSign sign = new XSign().fromJson(data2.getString("xsign"));
                        C2NFCChargeHandler.this.handleU2ChargeMsg(data2.getString(ContentDB.NFCConsumeFailCacheTable.UUID), data2.getString("cardno"), data2.getInt(ContentDB.NFCConsumeFailCacheTable.BALANCE), data2.getByteArray("key"), sign);
                        break;
                    case C2NFCChargeHandler.MSG_U3_CHARGE /* 28677 */:
                        Bundle data3 = msg.getData();
                        Log.i("C2NFCChargeHandler.handleMessage", "enter U3 charge: " + data3.toString());
                        C2NFCChargeHandler.this.handleU3ChargeMsg(data3.getString(ContentDB.NFCConsumeFailCacheTable.UUID), data3.getString("cardno"), new AuthSign().fromJson(data3.getString("sign")));
                        break;
                    case C2NFCChargeHandler.MSG_ANYO1_CHARGE /* 28678 */:
                        Bundle data4 = msg.getData();
                        Log.i("C2NFCChargeHandler.handleMessage", "enter anyo1 charge: " + data4.toString());
                        C2NFCChargeHandler.this.handleAnyo1ChargeMsg(data4.getString(ContentDB.NFCConsumeFailCacheTable.UUID), data4.getString("cardno"));
                        break;
                    case C2NFCChargeHandler.MSG_CDDZ1_CHARGE /* 28679 */:
                        Bundle data5 = msg.getData();
                        Log.i("C2NFCChargeHandler.handleMessage", "enter cddz jianquan card charge: " + data5.toString());
                        C2NFCChargeHandler.this.handleCDDZJianQuanChargeMsg(data5.getString(ContentDB.NFCConsumeFailCacheTable.UUID), data5.getString("cardno"));
                        break;
                    case C2NFCChargeHandler.MSG_ANYO_SVW_CHARGE /* 28680 */:
                        Bundle data6 = msg.getData();
                        Log.i("C2NFCChargeHandler.handleMessage", "enter anyo svw charge: " + data6.toString());
                        C2NFCChargeHandler.this.handleAnyoSVWChargeMsg(data6.getString(ContentDB.NFCConsumeFailCacheTable.UUID), data6.getString("cardno"));
                        break;
                    case C2NFCChargeHandler.MSG_CT_DEMO_CHARGE /* 28681 */:
                        Bundle data7 = msg.getData();
                        Log.i("C2NFCChargeHandler.handleMessage", "enter ct demo charge: " + data7.toString());
                        C2NFCChargeHandler.this.handleCTDemoChargeMsg(data7.getString(ContentDB.NFCConsumeFailCacheTable.UUID), data7.getString("cardno"));
                        break;
                    case C2NFCChargeHandler.MSG_OCPP_CHARGE /* 28688 */:
                        Bundle data8 = msg.getData();
                        Log.i("C2NFCChargeHandler.handleMessage", "enter ocpp card charge: " + data8.toString());
                        C2NFCChargeHandler.this.handleOCPPChargeMsg(data8.getString(ContentDB.NFCConsumeFailCacheTable.UUID), data8.getString("cardno"));
                        break;
                    case C2NFCChargeHandler.MSG_TIMEOUT_AUTH_SENDED /* 28704 */:
                        Log.i("C2NFCChargeHandler.handleMessage", NFC_CHARGE_STATUS.auth_sended + " state timeout !!! port: " + C2NFCChargeHandler.this.port + ", charge session: " + C2NFCChargeHandler.this.getChargeSession().toJson());
                        C2NFCChargeHandler.this.handlerTimer.stopTimer(msg.what);
                        C2NFCAgent.getInstance(C2NFCChargeHandler.this.port).sendMessage(C2NFCAgent.getInstance(C2NFCChargeHandler.this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_CARD_AUTH_FAIL).toJson()));
                        C2NFCChargeHandler.this.clearChargeSession();
                        C2NFCChargeHandler.this.status = NFC_CHARGE_STATUS.idle;
                        break;
                    case C2NFCChargeHandler.MSG_TIMEOUT_INIT_SENDED /* 28705 */:
                        Log.i("C2NFCChargeHandler.handleMessage", NFC_CHARGE_STATUS.init_sended + " state timeout !!! port: " + C2NFCChargeHandler.this.port + ", charge session: " + C2NFCChargeHandler.this.getChargeSession().toJson());
                        C2NFCChargeHandler.this.handlerTimer.stopTimer(msg.what);
                        C2NFCAgent.getInstance(C2NFCChargeHandler.this.port).sendMessage(C2NFCAgent.getInstance(C2NFCChargeHandler.this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_INIT_FAIL).toJson()));
                        C2NFCChargeHandler.this.finRequest(FIN_MODE.timeout, null);
                        break;
                    case C2NFCChargeHandler.MSG_TIMEOUT_STOP_SENDED /* 28707 */:
                        Log.i("C2NFCChargeHandler.handleMessage", NFC_CHARGE_STATUS.stop_sended + " state timeout !!! port: " + C2NFCChargeHandler.this.port + ", charge session: " + C2NFCChargeHandler.this.getChargeSession().toJson());
                        C2NFCChargeHandler.this.handlerTimer.stopTimer(msg.what);
                        if (NFC_CHARGE_STATUS.stop_sended.equals(C2NFCChargeHandler.this.status)) {
                            C2NFCChargeHandler.this.status = NFC_CHARGE_STATUS.charging;
                            break;
                        }
                        break;
                    case C2NFCChargeHandler.MSG_TIMEOUT_FIN_SENDED /* 28708 */:
                        Log.i("C2NFCChargeHandler.handleMessage", NFC_CHARGE_STATUS.fin_sended + " state timeout !!! port: " + C2NFCChargeHandler.this.port + ", charge session: " + C2NFCChargeHandler.this.getChargeSession().toJson());
                        C2NFCChargeHandler.this.handlerTimer.stopTimer(msg.what);
                        C2NFCChargeHandler.this.clearChargeSession();
                        C2NFCChargeHandler.this.status = NFC_CHARGE_STATUS.idle;
                        break;
                    case C2NFCChargeHandler.MSG_TIMEOUT_DCAP_REQUEST /* 28709 */:
                        DCAPMessage request = (DCAPMessage) msg.obj;
                        if (C2NFCChargeHandler.this.sendDCAPReqestState.containsKey(String.valueOf(request.getSeq()))) {
                            Log.i("C2NFCChargeHandler.handleMessage", "timeout to send DCAP request: " + request.toJson() + " !!! port: " + C2NFCChargeHandler.this.port + ", charge session: " + C2NFCChargeHandler.this.getChargeSession().toJson());
                            C2NFCChargeHandler.this.sendDCAPReqestState.remove(String.valueOf(request.getSeq()));
                            CAPMessage cap = (CAPMessage) request.getData();
                            if ("query".equals(cap.getOp())) {
                                QueryDirective query = (QueryDirective) cap.getData();
                                CAPDirectiveOption opt = cap.getOpt();
                                if (QueryDirective.QUERY_ID_CARD_STATUS.equals(opt.getQuery_id())) {
                                    HashMap<String, Object> params = query.getParams();
                                    String action = (String) params.get("action");
                                    NFC_CARD_TYPE cardType = (NFC_CARD_TYPE) params.get("cardType");
                                    String str = (String) params.get("cardNo");
                                    NFCChargeSession nfcChargeSession = C2NFCChargeHandler.this.getChargeSession();
                                    if ("end_charging".equals(action) && NFC_CHARGE_STATUS.charging.equals(C2NFCChargeHandler.this.status) && cardType.equals(nfcChargeSession.getCardType())) {
                                        C2NFCAgent.getInstance(C2NFCChargeHandler.this.port).sendMessage(C2NFCAgent.getInstance(C2NFCChargeHandler.this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_SCAN_REFUSE).toJson()));
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    case PortStatusObserver.MSG_PORT_STATUS_CHANGE /* 139265 */:
                        Uri uri = (Uri) msg.obj;
                        Log.d("C2NFCChargeHandler.handleMessage", "port status changed, port: " + C2NFCChargeHandler.this.port + ", uri: " + uri.toString());
                        C2NFCChargeHandler.this.handlePortStatusChanged(uri);
                        break;
                }
            } catch (Exception e) {
                Log.e("C2NFCChargeHandler.handleMessage", "except: " + Log.getStackTraceString(e));
                LogUtils.syslog("C2NFCChargeHandler handleMessage exception: " + Log.getStackTraceString(e));
            }
            super.handleMessage(msg);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePortStatusChanged(Uri uri) {
        if (uri.getPath().contains("ports/" + this.port + "/plugin")) {
            boolean isConnected = HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port);
            if (SystemSettingCacheProvider.getInstance().isPlug2Charge() && isConnected) {
                NFCEventData nfcEventData = new NFCEventData();
                nfcEventData.setPort(this.port);
                nfcEventData.setPresent(true);
                nfcEventData.setUuid(0);
                C2NFCAgent.getInstance(this.port).handleEvent(nfcEventData);
            }
        }
    }

    public void init(Context context, String port) {
        this.context = context;
        this.port = port;
        this.status = NFC_CHARGE_STATUS.idle;
        this.sendDCAPReqestState = new HashMap<>();
        this.thread = new HandlerThread("C2NFCChargeHandler#" + this.port, 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(this.context);
        this.dcapMessageReceiver = new DCAPMessageReceiver(this, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(DCAPMessage.ACTION_DCAP_CONFIRM);
        filter.addAction(DCAPMessage.ACTION_DCAP_INDICATE);
        LocalBroadcastManager.getInstance(context).registerReceiver(this.dcapMessageReceiver, filter);
        this.portStatusObserver = new PortStatusObserver(this.context, this.port, this.handler);
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor("ports/" + this.port), true, this.portStatusObserver);
    }

    public void destroy() {
        this.context.getContentResolver().unregisterContentObserver(this.portStatusObserver);
        LocalBroadcastManager.getInstance(this.context).unregisterReceiver(this.dcapMessageReceiver);
        this.handlerTimer.destroy();
        this.handler.removeMessages(MSG_DCAP_CONFIRM);
        this.handler.removeMessages(MSG_DCAP_INDICATE);
        this.handler.removeMessages(MSG_U1_CHARGE);
        this.handler.removeMessages(MSG_U2_CHARGE);
        this.handler.removeMessages(MSG_U3_CHARGE);
        this.handler.removeMessages(MSG_ANYO1_CHARGE);
        this.handler.removeMessages(MSG_CDDZ1_CHARGE);
        this.handler.removeMessages(MSG_ANYO_SVW_CHARGE);
        this.handler.removeMessages(MSG_CT_DEMO_CHARGE);
        this.handler.removeMessages(MSG_OCPP_CHARGE);
        this.handler.removeMessages(MSG_TIMEOUT_AUTH_SENDED);
        this.handler.removeMessages(MSG_TIMEOUT_INIT_SENDED);
        this.handler.removeMessages(MSG_TIMEOUT_STOP_SENDED);
        this.handler.removeMessages(MSG_TIMEOUT_FIN_SENDED);
        this.handler.removeMessages(MSG_TIMEOUT_DCAP_REQUEST);
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

    /* JADX INFO: Access modifiers changed from: private */
    public NFCChargeSession getChargeSession() {
        if (this.chargeSession == null) {
            this.chargeSession = new NFCChargeSession();
        }
        return this.chargeSession;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void clearChargeSession() {
        this.chargeSession = null;
    }

    public boolean isCharging() {
        return !this.status.equals(NFC_CHARGE_STATUS.idle);
    }

    private boolean isChargeUser(String uuid, String cardNo, NFC_CARD_TYPE cardType) {
        NFCChargeSession nfcChargeSession = getChargeSession();
        if (NFC_CARD_TYPE.U1.equals(cardType)) {
            if (NFC_CARD_TYPE.U1.equals(nfcChargeSession.getCardType())) {
                return true;
            }
        } else if (NFC_CARD_TYPE.ocpp.equals(cardType) && NFC_CHARGE_STATUS.charging.equals(this.status)) {
            if (NFC_CARD_TYPE.ocpp.equals(nfcChargeSession.getCardType())) {
                return true;
            }
        } else if (uuid.equals(nfcChargeSession.getCardUUID()) && cardNo.equals(nfcChargeSession.getCardNo())) {
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleConfirm(String json) {
        try {
            DCAPMessage confirm = new DCAPMessage().fromJson(json);
            if (!checkDCAPMessageRoute(confirm)) {
                Log.w("C2NFCChargeHandler.handleConfirm", "route is not to me, ignore it !!!");
                return;
            }
            CAPMessage cap = new CAPMessage().fromJson(JsonBean.ObjectToJson(confirm.getData()));
            confirm.setData(cap);
            CAPDirectiveOption opt = cap.getOpt();
            String peerOp = opt.getOp();
            String op = cap.getOp();
            if ("query".equals(peerOp)) {
                if (this.sendDCAPReqestState.containsKey(String.valueOf(opt.getSeq()))) {
                    DCAPMessage queryRequest = this.sendDCAPReqestState.remove(String.valueOf(opt.getSeq()));
                    if ("ack".equals(op)) {
                        AckDirective ack = new AckDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                        cap.setData(ack);
                        handleQueryConfirm(true, opt.getQuery_id(), ack, queryRequest);
                        return;
                    } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
                        NackDirective nack = new NackDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                        cap.setData(nack);
                        handleQueryConfirm(false, opt.getQuery_id(), nack, queryRequest);
                        return;
                    } else {
                        return;
                    }
                }
                return;
            }
            if (!this.sendDCAPReqestState.containsKey(String.valueOf(opt.getSeq()))) {
                NFCChargeSession session = getChargeSession();
                if ("fin".equals(peerOp)) {
                    session.setIs3rdPartFin(true);
                    Log.w("C2NFCChargeHandler.handleConfirm", "server or other module fin charge session, we support it !!!");
                } else if ("stop".equals(peerOp)) {
                    session.setIs3rdPartStop(true);
                    Log.w("C2NFCChargeHandler.handleConfirm", "server or other module stop charge, we support it !!!");
                } else {
                    Log.w("C2NFCChargeHandler.handleConfirm", "not expected seq, ignore it !!!");
                    return;
                }
            } else {
                this.sendDCAPReqestState.remove(String.valueOf(opt.getSeq()));
            }
            if ("ack".equals(op)) {
                AckDirective ack2 = new AckDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                cap.setData(ack2);
                if ("auth".equals(peerOp)) {
                    parseAckAuth(ack2);
                    handleAuthConfirm(true, null);
                } else if ("fin".equals(peerOp)) {
                    handleFinConfirm();
                } else if ("stop".equals(peerOp)) {
                    handleStopConfirm(true);
                }
            } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
                NackDirective nack2 = new NackDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                cap.setData(nack2);
                if ("auth".equals(peerOp)) {
                    ErrorCode error = parseNackAuth(nack2);
                    handleAuthConfirm(false, error);
                } else if ("stop".equals(peerOp)) {
                    handleStopConfirm(false);
                }
            }
        } catch (Exception e) {
            Log.e("C2NFCChargeHandler.handleConfirm", "confirm: " + json + ", exception: " + Log.getStackTraceString(e));
        }
    }

    private void parseAckAuth(AckDirective ack) {
        HashMap<String, Object> attach;
        NFCChargeSession chargeSession = getChargeSession();
        NFC_CARD_TYPE cardType = chargeSession.getCardType();
        if (NFC_CARD_TYPE.anyo1.equals(cardType)) {
            HashMap<String, Object> attach2 = ack.getAttach();
            if (attach2 != null) {
                chargeSession.setCharge_id((String) attach2.get("bill_id"));
                chargeSession.setBinded_user((String) attach2.get("user_id"));
                chargeSession.setUser_balance(Long.valueOf((String) attach2.get(ContentDB.NFCConsumeFailCacheTable.BALANCE)).longValue());
            }
        } else if (NFC_CARD_TYPE.U3.equals(cardType)) {
            HashMap<String, Object> attach3 = ack.getAttach();
            if (attach3 != null) {
                chargeSession.setCharge_id((String) attach3.get("bill_id"));
                chargeSession.setFee_rate((String) attach3.get(ContentDB.ChargeTable.FEE_RATE_ID));
                chargeSession.setUser_balance(Integer.valueOf((String) attach3.get(ContentDB.NFCConsumeFailCacheTable.BALANCE)).intValue());
                Object userTcType = attach3.get(ContentDB.ChargeTable.USER_TC_TYPE);
                if (userTcType != null) {
                    chargeSession.setUser_tc_type(USER_TC_TYPE.valueOf((String) userTcType));
                }
                Object userTcValue = attach3.get(ContentDB.ChargeTable.USER_TC_VALUE);
                if (userTcValue != null) {
                    chargeSession.setUser_tc_value((String) userTcValue);
                }
            }
        } else if (NFC_CARD_TYPE.cddz_1.equals(cardType) && (attach = ack.getAttach()) != null) {
            chargeSession.setCharge_id((String) attach.get("bill_id"));
            chargeSession.setUser_balance(Long.valueOf((String) attach.get(ContentDB.NFCConsumeFailCacheTable.BALANCE)).longValue());
        }
    }

    private ErrorCode parseNackAuth(NackDirective nack) {
        NFCChargeSession chargeSession = getChargeSession();
        NFC_CARD_TYPE cardType = chargeSession.getCardType();
        ErrorCode error = new ErrorCode(ErrorCode.EC_NFC_CARD_AUTH_FAIL);
        if (NFC_CARD_TYPE.U2.equals(cardType)) {
            HashMap<String, Object> attach = nack.getAttach();
            if (attach != null) {
                CHARGE_USER_STATUS user_status = CHARGE_USER_STATUS.valueOf((String) attach.get("user_status"));
                switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_USER_STATUS()[user_status.ordinal()]) {
                    case 2:
                        String bill_id = (String) attach.get("bill_id");
                        int fee = Integer.parseInt((String) attach.get(ChargeStopCondition.TYPE_FEE));
                        int cardBalance = (int) (chargeSession.getUser_balance() & XMSZHead.ID_BROADCAST);
                        if (fee <= cardBalance) {
                            if (NFCUtils.consumeU2(cardBalance, fee, chargeSession.getKey(), chargeSession.getCardUUID(), chargeSession.getCardNo(), 0)) {
                                Log.i("C2NFCChargeHandler.parseNackAuth", "succeeded to consume unpaid bill: " + bill_id + ", card: " + chargeSession.getCardNo() + ", bill fee: " + fee + ", balance: " + cardBalance);
                                error.setCode(ErrorCode.EC_NFC_CARD_UNPAID_CONSUME_OK);
                                ChargeContentProxy.getInstance().setUserBalance(bill_id, cardBalance - fee);
                                ChargeContentProxy.getInstance().setPaidFlag(bill_id, 1);
                                chargeSession.setUser_balance(cardBalance - fee);
                            } else {
                                error.setCode(ErrorCode.EC_NFC_CARD_UNPAID_CONSUME_FAIL);
                            }
                        } else {
                            error.setCode(ErrorCode.EC_NFC_CARD_UNPAID_BALANCE_NOT_ENOUGH);
                        }
                        HashMap<String, Object> errorData = new HashMap<>();
                        errorData.put("billId", bill_id);
                        errorData.put(ContentDB.NFCConsumeFailCacheTable.BALANCE, String.valueOf(cardBalance));
                        errorData.put(ChargeStopCondition.TYPE_FEE, String.valueOf(fee));
                        error.setData(errorData);
                        break;
                }
            }
        } else if (NFC_CARD_TYPE.U3.equals(cardType)) {
            error.setCode(ErrorCode.EC_UI_OUT_OF_DISTURB);
        } else {
            NFC_CARD_TYPE.anyo1.equals(cardType);
        }
        return error;
    }

    private void handleAuthConfirm(boolean isAck, ErrorCode error) {
        if (NFC_CHARGE_STATUS.auth_sended.equals(this.status)) {
            stopTimer(this.status);
            if (isAck) {
                initRequest();
                return;
            }
            C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, error.toJson()));
            clearChargeSession();
            this.status = NFC_CHARGE_STATUS.idle;
            return;
        }
        Log.w("C2NFCChargeHandler.handleAuthConfirm", "ignore auth confirm, port: " + this.port + ", status: " + this.status.getStatus());
    }

    private void handleStopConfirm(boolean isAck) {
        NFCChargeSession chargeSession = getChargeSession();
        if (NFC_CHARGE_STATUS.stop_sended.equals(this.status)) {
            if (chargeSession.isIs3rdPartStop() && !isAck) {
                Log.w("C2NFCChargeHandler.handleStopConfirm", "nack confirm for 3rd part stop, ignore it !!!");
                return;
            }
            stopTimer(this.status);
            if (isAck) {
                this.status = NFC_CHARGE_STATUS.stopped;
            } else {
                this.status = NFC_CHARGE_STATUS.charging;
            }
        } else if (NFC_CHARGE_STATUS.charging.equals(this.status) && chargeSession.isIs3rdPartStop() && isAck) {
            Log.w("C2NFCChargeHandler.handleStopConfirm", "3rd part succeed to stop charge session: " + chargeSession.toJson());
            this.status = NFC_CHARGE_STATUS.stopped;
        } else {
            Log.w("C2NFCChargeHandler.handleStopConfirm", "ignore stop confirm, port: " + this.port + ", status: " + this.status.getStatus());
        }
    }

    private void handleFinConfirm() {
        NFCChargeSession chargeSession = getChargeSession();
        if (NFC_CHARGE_STATUS.fin_sended.equals(this.status) || ((NFC_CHARGE_STATUS.inited.equals(this.status) || NFC_CHARGE_STATUS.charging.equals(this.status)) && chargeSession.isIs3rdPartFin())) {
            if (chargeSession.isIs3rdPartFin()) {
                Log.w("C2NFCChargeHandler.handleFinConfirm", "3rd part succeed to fin charge session: " + chargeSession.toJson());
            }
            stopTimer(this.status);
            clearChargeSession();
            this.status = NFC_CHARGE_STATUS.idle;
            return;
        }
        Log.w("C2NFCChargeHandler.handleFinConfirm", "ignore fin confirm, port: " + this.port + ", status: " + this.status.getStatus());
    }

    private void handleQueryConfirm(boolean isAck, String queryId, Object confirmDirective, DCAPMessage queryRequest) {
        CAPMessage cap = (CAPMessage) queryRequest.getData();
        QueryDirective query = (QueryDirective) cap.getData();
        NFCChargeSession nfcChargeSession = getChargeSession();
        if (isAck) {
            AckDirective ack = (AckDirective) confirmDirective;
            HashMap<String, Object> data = ack.getAttach();
            if (QueryDirective.QUERY_ID_CARD_STATUS.equals(queryId)) {
                HashMap<String, Object> params = query.getParams();
                String action = (String) params.get("action");
                NFC_CARD_TYPE cardType = (NFC_CARD_TYPE) params.get("cardType");
                String cardNo = (String) params.get("cardNo");
                if (data != null) {
                    if ("end_charging".equals(action)) {
                        String status = (String) data.get(ContentDB.AuthInfoTable.STATUS);
                        PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port);
                        if (NFC_CHARGE_STATUS.charging.equals(this.status) && cardType.equals(nfcChargeSession.getCardType())) {
                            if ("Accepted".equals(status)) {
                                C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK));
                                stopTimer(this.status);
                                finRequest(FIN_MODE.normal, null);
                                return;
                            }
                            C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_SCAN_REFUSE).toJson()));
                            return;
                        } else if (NFC_CHARGE_STATUS.idle.equals(this.status) && CHARGE_STATUS.CHARGING.equals(portStatus.getChargeStatus())) {
                            if ("Accepted".equals(status)) {
                                C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK));
                                String from = "user:" + CHARGE_USER_TYPE.nfc.getUserType() + "." + NFC_CARD_TYPE.ocpp.getType() + MqttTopic.TOPIC_LEVEL_SEPARATOR + cardNo;
                                fin3rdCharge(portStatus.getCharge_id(), from, FIN_MODE.nfc, null);
                                return;
                            }
                            C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_SCAN_REFUSE).toJson()));
                            return;
                        } else {
                            return;
                        }
                    }
                    return;
                } else if ("end_charging".equals(action) && NFC_CHARGE_STATUS.charging.equals(this.status) && cardType.equals(nfcChargeSession.getCardType())) {
                    C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_SCAN_REFUSE).toJson()));
                    return;
                } else {
                    return;
                }
            }
            return;
        }
        NackDirective nackDirective = (NackDirective) confirmDirective;
        if (QueryDirective.QUERY_ID_CARD_STATUS.equals(queryId)) {
            HashMap<String, Object> params2 = query.getParams();
            NFC_CARD_TYPE cardType2 = (NFC_CARD_TYPE) params2.get("cardType");
            String str = (String) params2.get("cardNo");
            if ("end_charging".equals((String) params2.get("action")) && NFC_CHARGE_STATUS.charging.equals(this.status) && cardType2.equals(nfcChargeSession.getCardType())) {
                C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_SCAN_REFUSE).toJson()));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleIndicate(String json) {
        try {
            DCAPMessage indicate = new DCAPMessage().fromJson(json);
            if (!checkDCAPMessageRoute(indicate)) {
                Log.w("C2NFCChargeHandler.handleIndicate", "route is not to me, ignore it !!!");
            } else {
                CAPMessage cap = new CAPMessage().fromJson(JsonBean.ObjectToJson(indicate.getData()));
                indicate.setData(cap);
                String op = cap.getOp();
                if (CAPMessage.DIRECTIVE_INIT_ACK.equals(op)) {
                    handleInitAckIndicate(indicate);
                } else if ("fin".equals(op)) {
                    handleFinIndicate(indicate);
                } else if ("event".equals(op)) {
                    EventDirective event = new EventDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                    cap.setData(event);
                    handleEventIndicate(indicate);
                }
            }
        } catch (Exception e) {
            Log.e("C2NFCChargeHandler.handleIndicate", "indicate: " + json + ", exception: " + Log.getStackTraceString(e));
        }
    }

    private void handleInitAckIndicate(DCAPMessage indicate) {
        try {
            if (NFC_CHARGE_STATUS.init_sended.equals(this.status)) {
                CAPMessage cap = (CAPMessage) indicate.getData();
                CAPDirectiveOption opt = cap.getOpt();
                InitAckDirective initAck = new InitAckDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                cap.setData(initAck);
                stopTimer(this.status);
                NFCChargeSession chargeSession = getChargeSession();
                chargeSession.setCharge_id(opt.getCharge_id());
                chargeSession.setFee_rate(initAck.getFee_rate());
                chargeSession.setTimeout_plugin(initAck.getTimeout_plugin());
                chargeSession.setTimeout_start(initAck.getTimeout_start());
                chargeSession.setTimeout_plugout(initAck.getTimeout_plugout());
                if (NFC_CARD_TYPE.U2.equals(chargeSession.getCardType()) && !reserveU2((int) (chargeSession.getUser_balance() & XMSZHead.ID_BROADCAST), chargeSession.getKey())) {
                    Log.w("C2NFCChargeHandler.handleInitAckIndicate", "failed to reserve card: " + chargeSession.getCardNo() + ", port: " + this.port + ", status: " + this.status.getStatus());
                    C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_CARD_RESERVE_FAIL).toJson()));
                    finRequest(FIN_MODE.error, new ErrorCode(ErrorCode.EC_NFC_CARD_RESERVE_FAIL));
                } else {
                    ackResponse(DCAPProxy.getInstance().createCAPResponseByIndcate(indicate));
                    this.status = NFC_CHARGE_STATUS.inited;
                    C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK));
                }
            } else {
                Log.w("C2NFCChargeHandler.handleInitAckIndicate", "ignore init ack indicate, port: " + this.port + ", status: " + this.status.getStatus());
            }
        } catch (Exception e) {
            Log.e("C2NFCChargeHandler.handleInitAckIndicate", "indicate: " + indicate.toJson() + ", exception: " + Log.getStackTraceString(e));
        }
    }

    private void handleFinIndicate(DCAPMessage indicate) {
        try {
            CAPMessage cap = (CAPMessage) indicate.getData();
            CAPDirectiveOption opt = cap.getOpt();
            String chargeId = opt.getCharge_id();
            NFCChargeSession chargeSession = getChargeSession();
            if (!chargeId.equals(chargeSession.getCharge_id())) {
                Log.w("C2NFCChargeHandler.handleFinIndicate", "ignore fin indicate, port: " + this.port + ", peer charge id: " + chargeId + ", local charge id: " + chargeSession.getCharge_id());
            } else if (NFC_CHARGE_STATUS.init_sended.equals(this.status) || NFC_CHARGE_STATUS.inited.equals(this.status) || NFC_CHARGE_STATUS.charging.equals(this.status) || NFC_CHARGE_STATUS.stop_sended.equals(this.status) || NFC_CHARGE_STATUS.stopped.equals(this.status) || NFC_CHARGE_STATUS.fin_sended.equals(this.status)) {
                FinDirective fin = new FinDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                cap.setData(fin);
                stopTimer(this.status);
                if (NFC_CHARGE_STATUS.init_sended.equals(this.status)) {
                    FIN_MODE finMode = fin.getFin_mode();
                    if (FIN_MODE.no_feerate.equals(finMode) && CHARGE_PLATFORM.xcharge.equals(chargeSession.getCharge_platform()) && NFC_CARD_TYPE.U3.equals(chargeSession.getCardType())) {
                        Log.w("C2NFCChargeHandler.handleFinIndicate", "no fee rate for U3, keep this status: " + this.status.getStatus() + ", port: " + this.port);
                        this.handlerTimer.startTimer(10000L, MSG_TIMEOUT_INIT_SENDED, null);
                        return;
                    }
                    C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_UI_OUT_OF_DISTURB).toJson()));
                }
                ackResponse(DCAPProxy.getInstance().createCAPResponseByIndcate(indicate));
                clearChargeSession();
                this.status = NFC_CHARGE_STATUS.idle;
            } else {
                Log.w("C2NFCChargeHandler.handleFinIndicate", "ignore fin indicate, port: " + this.port + ", status: " + this.status.getStatus());
            }
        } catch (Exception e) {
            Log.e("C2NFCChargeHandler.handleFinIndicate", "indicate: " + indicate.toJson() + ", exception: " + Log.getStackTraceString(e));
        }
    }

    private void handleEventIndicate(DCAPMessage event) {
        try {
            CAPMessage cap = (CAPMessage) event.getData();
            CAPDirectiveOption opt = cap.getOpt();
            String eventId = opt.getEvent_id();
            String chargeId = opt.getCharge_id();
            NFCChargeSession chargeSession = getChargeSession();
            if (!chargeId.equals(chargeSession.getCharge_id())) {
                Log.w("C2NFCChargeHandler.handleEventIndicate", "ignore event indicate, port: " + this.port + ", peer charge id: " + chargeId + ", local charge id: " + chargeSession.getCharge_id());
            } else if (EventDirective.EVENT_CHARGE_START.equals(eventId)) {
                if (NFC_CHARGE_STATUS.inited.equals(this.status)) {
                    stopTimer(this.status);
                    this.status = NFC_CHARGE_STATUS.charging;
                } else {
                    Log.w("C2NFCChargeHandler.handleEventIndicate", "ignore charge_start event indicate, port: " + this.port + ", status: " + this.status.getStatus());
                }
            } else if (EventDirective.EVENT_CHARGE_STOP.equals(eventId)) {
                if (NFC_CHARGE_STATUS.charging.equals(this.status) || NFC_CHARGE_STATUS.stop_sended.equals(this.status)) {
                    stopTimer(this.status);
                    this.status = NFC_CHARGE_STATUS.stopped;
                } else {
                    Log.w("C2NFCChargeHandler.handleEventIndicate", "ignore charge_stop event indicate, port: " + this.port + ", status: " + this.status.getStatus());
                }
            }
        } catch (Exception e) {
            Log.e("C2NFCChargeHandler.handleEventIndicate", "event: " + event.toJson() + ", exception: " + Log.getStackTraceString(e));
        }
    }

    private boolean checkDCAPMessageRoute(DCAPMessage msg) {
        String from = msg.getFrom();
        String to = msg.getTo();
        return from.startsWith("device:") && to.startsWith("user:nfc.");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleU1ChargeMsg(String cardUUID, String cardNo) {
        if (NFC_CHARGE_STATUS.idle.equals(this.status)) {
            Log.i("C2NFCChargeHandler.handleU1ChargeMsg", "charge begin ...");
            NFCChargeSession nfcChargeSession = getChargeSession();
            String sn = HardwareStatusCacheProvider.getInstance().getSn();
            String userType = String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.U1.getType();
            String deviceId = "sn/" + sn;
            nfcChargeSession.setCardNo(cardNo);
            nfcChargeSession.setCardUUID(cardUUID);
            nfcChargeSession.setSn(sn);
            nfcChargeSession.setCardType(NFC_CARD_TYPE.U1);
            nfcChargeSession.setUser_type(userType);
            nfcChargeSession.setUser_code(cardNo);
            nfcChargeSession.setDevice_id(deviceId);
            nfcChargeSession.setPort(this.port);
            CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
            nfcChargeSession.setCharge_platform(platform);
            int waitPluginChargeTime = LocalSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalChargeCancel();
            nfcChargeSession.setTimeout_plugin(waitPluginChargeTime);
            authRequest(null);
            return;
        }
        endNFCCharge(cardUUID, cardNo, NFC_CARD_TYPE.U1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleU2ChargeMsg(String cardUUID, String cardNo, int balance, byte[] key, XSign sign) {
        if (NFC_CHARGE_STATUS.idle.equals(this.status)) {
            if (!permitU2StartCharge(cardNo, balance, key, sign)) {
                Log.w("C2NFCChargeHandler.handleU2ChargeMsg", "not permit to start charge using card: " + cardNo + ", port: " + this.port);
                return;
            } else if (balance <= 0) {
                Log.w("C2NFCChargeHandler.handleU2ChargeMsg", "not permit to start charge for no balance !!! card no: " + cardNo + ", balance: " + balance + ", port: " + this.port);
                C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_CARD_BALANCE_NOT_ENOUGH).toJson()));
                return;
            } else {
                Log.i("C2NFCChargeHandler.handleU2ChargeMsg", "charge begin ..., port: " + this.port);
                NFCChargeSession nfcChargeSession = getChargeSession();
                String sn = HardwareStatusCacheProvider.getInstance().getSn();
                String userType = String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.U2.getType();
                String deviceId = "sn/" + sn;
                nfcChargeSession.setCardNo(cardNo);
                nfcChargeSession.setCardUUID(cardUUID);
                nfcChargeSession.setKey(key);
                nfcChargeSession.setUser_balance(balance);
                nfcChargeSession.setSn(sn);
                nfcChargeSession.setXsign(sign);
                nfcChargeSession.setCardType(NFC_CARD_TYPE.U2);
                nfcChargeSession.setUser_type(userType);
                nfcChargeSession.setUser_code(cardNo);
                nfcChargeSession.setDevice_id(deviceId);
                nfcChargeSession.setPort(this.port);
                CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
                nfcChargeSession.setCharge_platform(platform);
                int waitPluginChargeTime = LocalSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalChargeCancel();
                nfcChargeSession.setTimeout_plugin(waitPluginChargeTime);
                int waitPlugoutChargeTime = LocalSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalDelayStart();
                nfcChargeSession.setTimeout_plugout(waitPlugoutChargeTime);
                authRequest(null);
                return;
            }
        }
        NFCChargeSession nfcChargeSession2 = getChargeSession();
        nfcChargeSession2.setUser_balance(balance);
        nfcChargeSession2.setXsign(sign);
        endNFCCharge(cardUUID, cardNo, NFC_CARD_TYPE.U2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleU3ChargeMsg(String cardUUID, String cardNo, AuthSign sign) {
        if (NFC_CHARGE_STATUS.idle.equals(this.status)) {
            Log.i("C2NFCChargeHandler.handleU3ChargeMsg", "charge begin ..., port: " + this.port);
            NFCChargeSession nfcChargeSession = getChargeSession();
            String sn = HardwareStatusCacheProvider.getInstance().getSn();
            String userType = String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.U3.getType();
            String deviceId = "sn/" + sn;
            nfcChargeSession.setCardNo(cardNo);
            nfcChargeSession.setCardUUID(cardUUID);
            nfcChargeSession.setSn(sn);
            nfcChargeSession.setCardType(NFC_CARD_TYPE.U3);
            nfcChargeSession.setUser_type(userType);
            nfcChargeSession.setUser_code(cardNo);
            nfcChargeSession.setDevice_id(deviceId);
            nfcChargeSession.setPort(this.port);
            CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
            nfcChargeSession.setCharge_platform(platform);
            int waitPluginChargeTime = RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalChargeCancel();
            nfcChargeSession.setTimeout_plugin(waitPluginChargeTime);
            int waitPlugoutChargeTime = RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalDelayStart();
            nfcChargeSession.setTimeout_plugout(waitPlugoutChargeTime);
            authRequest(sign);
            return;
        }
        endNFCCharge(cardUUID, cardNo, NFC_CARD_TYPE.U3);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleCTDemoChargeMsg(String cardUUID, String cardNo) {
        if (NFC_CHARGE_STATUS.idle.equals(this.status)) {
            Log.i("C2NFCChargeHandler.handleCTDemoChargeMsg", "charge begin ..., port: " + this.port);
            NFCChargeSession nfcChargeSession = getChargeSession();
            String sn = HardwareStatusCacheProvider.getInstance().getSn();
            String userType = String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.CT_DEMO.getType();
            String deviceId = "sn/" + sn;
            nfcChargeSession.setCardNo(cardNo);
            nfcChargeSession.setCardUUID(cardUUID);
            nfcChargeSession.setSn(sn);
            nfcChargeSession.setCardType(NFC_CARD_TYPE.CT_DEMO);
            nfcChargeSession.setUser_type(userType);
            nfcChargeSession.setUser_code(cardNo);
            nfcChargeSession.setDevice_id(deviceId);
            nfcChargeSession.setPort(this.port);
            nfcChargeSession.setCharge_platform(CHARGE_PLATFORM.xcharge);
            int waitPluginChargeTime = LocalSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalChargeCancel();
            nfcChargeSession.setTimeout_plugin(waitPluginChargeTime);
            authRequest(null);
            return;
        }
        endNFCCharge(cardUUID, cardNo, NFC_CARD_TYPE.CT_DEMO);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleAnyo1ChargeMsg(String cardUUID, String cardNo) {
        if (NFC_CHARGE_STATUS.idle.equals(this.status)) {
            Log.i("C2NFCChargeHandler.handleAnyo1ChargeMsg", "charge begin ..., port: " + this.port);
            NFCChargeSession nfcChargeSession = getChargeSession();
            String sn = HardwareStatusCacheProvider.getInstance().getSn();
            String userType = String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.anyo1.getType();
            String deviceId = "sn/" + sn;
            nfcChargeSession.setCardNo(cardNo);
            nfcChargeSession.setCardUUID(cardUUID);
            nfcChargeSession.setSn(sn);
            nfcChargeSession.setCardType(NFC_CARD_TYPE.anyo1);
            nfcChargeSession.setUser_type(userType);
            nfcChargeSession.setUser_code(cardNo);
            nfcChargeSession.setDevice_id(deviceId);
            nfcChargeSession.setPort(this.port);
            nfcChargeSession.setCharge_platform(CHARGE_PLATFORM.anyo);
            nfcChargeSession.setTimeout_plugin(60);
            authRequest(null);
            return;
        }
        endNFCCharge(cardUUID, cardNo, NFC_CARD_TYPE.anyo1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleAnyoSVWChargeMsg(String cardUUID, String cardNo) {
        if (NFC_CHARGE_STATUS.idle.equals(this.status)) {
            Log.i("C2NFCChargeHandler.handleAnyoSVWChargeMsg", "charge begin ..., port: " + this.port);
            NFCChargeSession nfcChargeSession = getChargeSession();
            String sn = HardwareStatusCacheProvider.getInstance().getSn();
            String userType = String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.anyo_svw.getType();
            String deviceId = "sn/" + sn;
            nfcChargeSession.setCardNo(cardNo);
            nfcChargeSession.setCardUUID(cardUUID);
            nfcChargeSession.setSn(sn);
            nfcChargeSession.setCardType(NFC_CARD_TYPE.anyo_svw);
            nfcChargeSession.setUser_type(userType);
            nfcChargeSession.setUser_code(cardNo);
            nfcChargeSession.setDevice_id(deviceId);
            nfcChargeSession.setPort(this.port);
            nfcChargeSession.setCharge_platform(CHARGE_PLATFORM.anyo);
            nfcChargeSession.setTimeout_plugin(60);
            authRequest(null);
            return;
        }
        endNFCCharge(cardUUID, cardNo, NFC_CARD_TYPE.anyo_svw);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleCDDZJianQuanChargeMsg(String cardUUID, String cardNo) {
        if (NFC_CHARGE_STATUS.idle.equals(this.status)) {
            Log.i("C2NFCChargeHandler.handleCDDZJianQuanChargeMsg", "charge begin ..., port: " + this.port);
            NFCChargeSession nfcChargeSession = getChargeSession();
            String sn = HardwareStatusCacheProvider.getInstance().getSn();
            String userType = String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.cddz_1.getType();
            String deviceId = "sn/" + sn;
            nfcChargeSession.setCardNo(cardNo);
            nfcChargeSession.setCardUUID(cardUUID);
            nfcChargeSession.setSn(sn);
            nfcChargeSession.setCardType(NFC_CARD_TYPE.cddz_1);
            nfcChargeSession.setUser_type(userType);
            nfcChargeSession.setUser_code(cardNo);
            nfcChargeSession.setDevice_id(deviceId);
            nfcChargeSession.setPort(this.port);
            nfcChargeSession.setCharge_platform(CHARGE_PLATFORM.cddz);
            nfcChargeSession.setTimeout_plugin(60);
            authRequest(null);
            return;
        }
        endNFCCharge(cardUUID, cardNo, NFC_CARD_TYPE.cddz_1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleOCPPChargeMsg(String cardUUID, String cardNo) {
        if (NFC_CHARGE_STATUS.idle.equals(this.status)) {
            if (CHARGE_STATUS.CHARGING.equals(ChargeStatusCacheProvider.getInstance().getPortStatus(this.port).getChargeStatus())) {
                HashMap<String, Object> params = new HashMap<>();
                params.put("action", "end_charging");
                params.put("cardNo", cardNo);
                params.put("cardType", NFC_CARD_TYPE.ocpp);
                queryRequest(QueryDirective.QUERY_ID_CARD_STATUS, params);
                return;
            }
            Log.i("C2NFCChargeHandler.handleOCPPChargeMsg", "charge begin ..., port: " + this.port);
            NFCChargeSession nfcChargeSession = getChargeSession();
            String sn = HardwareStatusCacheProvider.getInstance().getSn();
            String userType = String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.ocpp.getType();
            String deviceId = "sn/" + sn;
            nfcChargeSession.setCardNo(cardNo);
            nfcChargeSession.setCardUUID(cardUUID);
            nfcChargeSession.setSn(sn);
            nfcChargeSession.setCardType(NFC_CARD_TYPE.ocpp);
            nfcChargeSession.setUser_type(userType);
            nfcChargeSession.setUser_code(cardNo);
            nfcChargeSession.setDevice_id(deviceId);
            nfcChargeSession.setPort(this.port);
            nfcChargeSession.setCharge_platform(CHARGE_PLATFORM.ocpp);
            int waitPluginChargeTime = RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalChargeCancel();
            nfcChargeSession.setTimeout_plugin(waitPluginChargeTime);
            authRequest(null);
            return;
        }
        endNFCCharge(cardUUID, cardNo, NFC_CARD_TYPE.ocpp);
    }

    private void endNFCCharge(String cardUUID, String cardNo, NFC_CARD_TYPE cardType) {
        Log.i("C2NFCChargeHandler.endNFCCharge", "charge end ..., status: " + this.status.getStatus());
        NFCChargeSession nfcChargeSession = getChargeSession();
        if (isChargeUser(cardUUID, cardNo, cardType)) {
            if (NFC_CARD_TYPE.U2.equals(nfcChargeSession.getCardType())) {
                if (!handlerU2ReleaseAndPay()) {
                    return;
                }
            } else if (!NFC_CARD_TYPE.ocpp.equals(cardType) || !NFC_CHARGE_STATUS.charging.equals(this.status)) {
                C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK));
            }
            if (NFC_CHARGE_STATUS.charging.equals(this.status)) {
                if (NFC_CARD_TYPE.ocpp.equals(cardType)) {
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("action", "end_charging");
                    params.put("cardNo", cardNo);
                    params.put("cardType", NFC_CARD_TYPE.ocpp);
                    queryRequest(QueryDirective.QUERY_ID_CARD_STATUS, params);
                    return;
                }
                stopTimer(this.status);
                finRequest(FIN_MODE.normal, null);
                return;
            } else if (NFC_CHARGE_STATUS.init_sended.equals(this.status) || NFC_CHARGE_STATUS.inited.equals(this.status)) {
                stopTimer(this.status);
                finRequest(FIN_MODE.cancel, null);
                return;
            } else if (NFC_CHARGE_STATUS.auth_sended.equals(this.status)) {
                stopTimer(this.status);
                clearChargeSession();
                this.status = NFC_CHARGE_STATUS.idle;
                return;
            } else if (NFC_CHARGE_STATUS.stopped.equals(this.status)) {
                stopTimer(this.status);
                finRequest(FIN_MODE.normal, null);
                return;
            } else {
                return;
            }
        }
        Log.w("C2NFCChargeHandler.endNFCCharge", "not charge card !!! charge card no: " + nfcChargeSession.getCardNo() + ", but scan card no: " + cardNo);
        C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_NOT_INIT_CHARGE_CARD).toJson()));
    }

    private boolean handlerU2ReleaseAndPay() {
        NFCChargeSession nfcChargeSession = getChargeSession();
        String cardNo = nfcChargeSession.getCardNo();
        if (NFC_CHARGE_STATUS.inited.equals(this.status) || NFC_CHARGE_STATUS.charging.equals(this.status)) {
            if (isU2Reserved(cardNo, (int) (nfcChargeSession.getUser_balance() & XMSZHead.ID_BROADCAST), nfcChargeSession.getKey(), nfcChargeSession.getXsign())) {
                if (NFC_CHARGE_STATUS.inited.equals(this.status)) {
                    if (!releaseU2((int) (nfcChargeSession.getUser_balance() & XMSZHead.ID_BROADCAST), nfcChargeSession.getKey())) {
                        Log.w("C2NFCChargeHandler.handlerU2ReleaseAndPay", "failed to release u2 card: " + cardNo + ", port: " + this.port + ", status: " + this.status.getStatus());
                        C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_CARD_RELEASE_FAIL).toJson()));
                        return false;
                    }
                    C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK));
                    return true;
                } else if (NFC_CHARGE_STATUS.charging.equals(this.status)) {
                    return pay4NowBill(true);
                }
            } else {
                Log.w("C2NFCChargeHandler.handlerU2ReleaseAndPay", "not start charge on this pile using u2 card: " + cardNo + ", port: " + this.port + ", status: " + this.status.getStatus());
                if (NFC_CHARGE_STATUS.charging.equals(this.status)) {
                    return pay4NowBill(false);
                }
                C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK));
                return true;
            }
        } else if (NFC_CHARGE_STATUS.stopped.equals(this.status)) {
            if (isU2Reserved(cardNo, (int) (nfcChargeSession.getUser_balance() & XMSZHead.ID_BROADCAST), nfcChargeSession.getKey(), nfcChargeSession.getXsign())) {
                return pay4NowBill(true);
            }
            Log.w("C2NFCChargeHandler.handlerU2ReleaseAndPay", "not start charge on this pile using u2 card: " + cardNo + ", port: " + this.port + ", status: " + this.status.getStatus());
            return pay4NowBill(false);
        }
        C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK));
        return true;
    }

    private boolean pay4NowBill(boolean isReserved) {
        NFCChargeSession nfcChargeSession = getChargeSession();
        String cardNo = nfcChargeSession.getCardNo();
        String billId = nfcChargeSession.getCharge_id();
        if (nfcChargeSession.isPaid()) {
            Log.i("C2NFCChargeHandler.pay4NowBill", "bill has been paid !!! card: " + cardNo + ", bill: " + billId);
            C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK));
            return true;
        }
        int cardBalance = (int) (nfcChargeSession.getUser_balance() & XMSZHead.ID_BROADCAST);
        ChargeBill bill = ChargeContentProxy.getInstance().getChargeBill(billId);
        if (bill != null) {
            int billFee = bill.getTotal_fee();
            if (billFee > 0) {
                if (cardBalance >= billFee) {
                    if (NFCUtils.consumeU2(cardBalance, billFee, nfcChargeSession.getKey(), nfcChargeSession.getCardUUID(), nfcChargeSession.getCardNo(), 0)) {
                        Log.i("C2NFCChargeHandler.pay4NowBill", "succeeded to consume !!! card: " + cardNo + ", bill fee: " + billFee + ", balance: " + nfcChargeSession.getUser_balance());
                        ChargeContentProxy.getInstance().setPaidFlag(billId, 1);
                        nfcChargeSession.setUser_balance(cardBalance - billFee);
                        nfcChargeSession.setPaid(true);
                        C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK));
                        return true;
                    }
                    Log.w("C2NFCChargeHandler.pay4NowBill", "failed to consume !!! card: " + cardNo + ", bill fee: " + billFee + ", balance: " + cardBalance);
                    ErrorCode error = new ErrorCode(ErrorCode.EC_NFC_CARD_CONSUME_FAIL);
                    HashMap<String, String> attachData = new HashMap<>();
                    attachData.put(ContentDB.NFCConsumeFailCacheTable.BALANCE, String.valueOf(cardBalance));
                    attachData.put(ChargeStopCondition.TYPE_FEE, String.valueOf(billFee));
                    error.setData(attachData);
                    C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, error.toJson()));
                    return false;
                }
                Log.w("C2NFCChargeHandler.pay4NowBill", "not enough balance!!! card: " + cardNo + ", bill fee: " + billFee + ", balance: " + cardBalance);
                C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_CARD_BALANCE_NOT_ENOUGH).toJson()));
                return true;
            } else if (isReserved) {
                Log.i("C2NFCChargeHandler.pay4NowBill", "bill fee is zero, release u2 card: " + cardNo + ", bill: " + billId);
                if (!releaseU2(cardBalance, nfcChargeSession.getKey())) {
                    Log.w("C2NFCChargeHandler.pay4NowBill", "failed to release u2 card: " + cardNo + ", port: " + this.port + ", status: " + this.status.getStatus());
                    C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_CARD_RELEASE_FAIL).toJson()));
                    return false;
                }
                nfcChargeSession.setPaid(true);
                C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK));
                return true;
            } else {
                nfcChargeSession.setPaid(true);
                C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_OK));
                return true;
            }
        }
        Log.w("C2NFCChargeHandler.pay4NowBill", "no bill !!! card: " + cardNo + ", bill: " + billId);
        C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_ERROR).toJson()));
        return false;
    }

    public DCAPMessage createRequest(String from, String op, CAPDirectiveOption opt, Object directive) {
        String sn = HardwareStatusCacheProvider.getInstance().getSn();
        CAPMessage requestCap = new CAPMessage();
        DCAPMessage request = new DCAPMessage();
        request.setFrom(from);
        request.setTo("device:sn/" + sn);
        request.setType("cap");
        request.setCtime(System.currentTimeMillis());
        request.setSeq(Sequence.getAgentDCAPSequence());
        requestCap.setOp(op);
        requestCap.setOpt(opt);
        requestCap.setData(directive);
        request.setData(requestCap);
        return request;
    }

    private void authRequest(AuthSign cardSign) {
        NFCChargeSession session = getChargeSession();
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setAuth_id("init");
        opt.setPort_id(session.getPort());
        AuthDirective auth = new AuthDirective();
        auth.setInit_type(CHARGE_INIT_TYPE.nfc);
        auth.setUser_type(session.getUser_type());
        auth.setUser_code(session.getUser_code());
        auth.setDevice_id(session.getDevice_id());
        auth.setPort(session.getPort());
        if (cardSign != null) {
            HashMap<String, Object> userData = new HashMap<>();
            userData.put("timestamp", String.valueOf(cardSign.getTime()));
            userData.put("nonce", cardSign.getRand());
            userData.put("sign", cardSign.getSign());
            auth.setUser_data(userData);
        }
        String from = "user:" + session.getUser_type() + MqttTopic.TOPIC_LEVEL_SEPARATOR + session.getUser_code();
        DCAPMessage request = createRequest(from, "auth", opt, auth);
        DCAPProxy.getInstance().sendRequest(request);
        this.sendDCAPReqestState.put(String.valueOf(request.getSeq()), request);
        this.handlerTimer.startTimer(30000L, MSG_TIMEOUT_DCAP_REQUEST, request);
        this.status = NFC_CHARGE_STATUS.auth_sended;
        this.handlerTimer.startTimer(10000L, MSG_TIMEOUT_AUTH_SENDED, null);
    }

    private void initRequest() {
        NFCChargeSession session = getChargeSession();
        String chargeId = session.getCharge_id();
        if (TextUtils.isEmpty(chargeId)) {
            chargeId = LocalIdGenerator.getChargeId();
        }
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(chargeId);
        InitDirective init = new InitDirective();
        init.setInit_type(CHARGE_INIT_TYPE.nfc);
        init.setUser_type(session.getUser_type());
        init.setUser_code(session.getUser_code());
        init.setDevice_id(session.getDevice_id());
        init.setPort(session.getPort());
        init.setFee_rate(session.getFee_rate());
        init.setUser_tc_type(session.getUser_tc_type());
        init.setUser_tc_value(session.getUser_tc_value());
        init.setUser_balance(session.getUser_balance());
        init.setIs_free(session.getIs_free());
        init.setBinded_user(session.getBinded_user());
        init.setCharge_platform(session.getCharge_platform());
        init.setTimeout_plugin(session.getTimeout_plugin());
        init.setTimeout_start(session.getTimeout_start());
        init.setTimeout_plugout(session.getTimeout_plugout());
        String from = "user:" + session.getUser_type() + MqttTopic.TOPIC_LEVEL_SEPARATOR + session.getUser_code();
        DCAPMessage request = createRequest(from, "init", opt, init);
        DCAPProxy.getInstance().sendRequest(request);
        this.status = NFC_CHARGE_STATUS.init_sended;
        this.handlerTimer.startTimer(10000L, MSG_TIMEOUT_INIT_SENDED, null);
    }

    private void stopRequest() {
        NFCChargeSession session = getChargeSession();
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(session.getCharge_id());
        StopDirective stop = new StopDirective();
        String from = "user:" + session.getUser_type() + MqttTopic.TOPIC_LEVEL_SEPARATOR + session.getUser_code();
        DCAPMessage request = createRequest(from, "stop", opt, stop);
        DCAPProxy.getInstance().sendRequest(request);
        this.sendDCAPReqestState.put(String.valueOf(request.getSeq()), request);
        this.handlerTimer.startTimer(30000L, MSG_TIMEOUT_DCAP_REQUEST, request);
        this.status = NFC_CHARGE_STATUS.stop_sended;
        this.handlerTimer.startTimer(10000L, MSG_TIMEOUT_STOP_SENDED, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void finRequest(FIN_MODE mode, ErrorCode error) {
        NFCChargeSession session = getChargeSession();
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(session.getCharge_id());
        FinDirective fin = new FinDirective();
        fin.setFin_mode(mode);
        fin.setError(error);
        String from = "user:" + session.getUser_type() + MqttTopic.TOPIC_LEVEL_SEPARATOR + session.getUser_code();
        DCAPMessage request = createRequest(from, "fin", opt, fin);
        DCAPProxy.getInstance().sendRequest(request);
        this.sendDCAPReqestState.put(String.valueOf(request.getSeq()), request);
        this.handlerTimer.startTimer(30000L, MSG_TIMEOUT_DCAP_REQUEST, request);
        this.status = NFC_CHARGE_STATUS.fin_sended;
        this.handlerTimer.startTimer(10000L, MSG_TIMEOUT_FIN_SENDED, null);
    }

    private void fin3rdCharge(String chargeId, String from, FIN_MODE mode, ErrorCode error) {
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(chargeId);
        FinDirective fin = new FinDirective();
        fin.setFin_mode(mode);
        fin.setError(error);
        DCAPMessage request = createRequest(from, "fin", opt, fin);
        DCAPProxy.getInstance().sendRequest(request);
    }

    private void queryRequest(String queryId, HashMap<String, Object> params) {
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setQuery_id(queryId);
        QueryDirective query = new QueryDirective();
        query.setParams(params);
        DCAPMessage request = createRequest("user:nfc.", "query", opt, query);
        DCAPProxy.getInstance().sendRequest(request);
        this.sendDCAPReqestState.put(String.valueOf(request.getSeq()), request);
        this.handlerTimer.startTimer(30000L, MSG_TIMEOUT_DCAP_REQUEST, request);
    }

    public boolean ackResponse(DCAPMessage response) {
        AckDirective ack = new AckDirective();
        return DCAPProxy.getInstance().sendResponse(response, "cap", "ack", ack);
    }

    private void stopTimer(NFC_CHARGE_STATUS status) {
        switch ($SWITCH_TABLE$com$xcharge$charger$device$c2$nfc$charge$type$NFC_CHARGE_STATUS()[status.ordinal()]) {
            case 2:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_AUTH_SENDED);
                return;
            case 3:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_INIT_SENDED);
                return;
            case 4:
            case 5:
            case 7:
            default:
                return;
            case 6:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_STOP_SENDED);
                return;
            case 8:
                this.handlerTimer.stopTimer(MSG_TIMEOUT_FIN_SENDED);
                return;
        }
    }

    private boolean permitU2StartCharge(String cardNo, int balance, byte[] key, XSign sign) {
        boolean isOk = NFCUtils.checkU2Sign(NFCUtils.intToBytes(balance), key, sign, null);
        if (isOk) {
            return true;
        }
        String sn = HardwareStatusCacheProvider.getInstance().getSn();
        if (NFCUtils.checkU2Sign(NFCUtils.intToBytes(balance), key, sign, sn.getBytes())) {
            Log.w("C2NFCChargeHandler.permitU2StartCharge", "maybe not release on this pile !!! card: " + cardNo);
            if (releaseU2(balance, key)) {
                return true;
            }
            Log.w("C2NFCChargeHandler.permitU2StartCharge", "failed to release card: " + cardNo);
            C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_CARD_RESERVE_FAIL).toJson()));
            return false;
        }
        Log.w("C2NFCChargeHandler.permitU2StartCharge", "maybe reserved by other pile !!! card: " + cardNo);
        C2NFCAgent.getInstance(this.port).sendMessage(C2NFCAgent.getInstance(this.port).obtainMessage(C2NFCAgent.MSG_NFC_SWIPE_CARD_ERROR, new ErrorCode(ErrorCode.EC_NFC_CARD_RESERVED).toJson()));
        return false;
    }

    private boolean reserveU2(int balance, byte[] key) {
        String sn = HardwareStatusCacheProvider.getInstance().getSn();
        boolean isOK = NFCUtils.setUserCardSign(key, NFCUtils.signU2(NFCUtils.intToBytes(balance), key, 0, sn.getBytes()));
        if (isOK) {
            Log.d("C2NFCChargeHandler.reserveU2", "succeed to reserve card, balance: " + balance + ", sn: " + sn);
        }
        return isOK;
    }

    private boolean isU2Reserved(String cardNo, int balance, byte[] key, XSign sign) {
        String sn = HardwareStatusCacheProvider.getInstance().getSn();
        boolean isOk = NFCUtils.checkU2Sign(NFCUtils.intToBytes(balance), key, sign, sn.getBytes());
        if (isOk) {
            return true;
        }
        Log.w("C2NFCChargeHandler.isU2Reserved", "not reserved by this pile !!! card: " + cardNo);
        return false;
    }

    private boolean releaseU2(int balance, byte[] key) {
        boolean isOk = NFCUtils.setUserCardSign(key, NFCUtils.signU2(NFCUtils.intToBytes(balance), key, 0, null));
        if (isOk) {
            Log.d("C2NFCChargeHandler.reserveU2", "succeed to release card, balance: " + balance);
        }
        return isOk;
    }
}
