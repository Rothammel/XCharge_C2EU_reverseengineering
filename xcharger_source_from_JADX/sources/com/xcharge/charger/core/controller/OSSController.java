package com.xcharge.charger.core.controller;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.p000v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.api.bean.AckDirective;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.NackDirective;
import com.xcharge.charger.core.api.bean.cap.CAPDirectiveOption;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.core.api.bean.cap.QueryDirective;
import com.xcharge.charger.core.api.bean.cap.SetDirective;
import com.xcharge.charger.core.api.bean.cap.UpgradeDirective;
import com.xcharge.charger.core.bean.GatewaySession;
import com.xcharge.charger.core.bean.IndicateSession;
import com.xcharge.charger.core.bean.RequestSession;
import com.xcharge.charger.core.handler.ChargeHandler;
import com.xcharge.charger.core.handler.UpgradeAgent;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.setting.SwipeCardPermission;
import com.xcharge.charger.data.bean.type.CHARGE_INIT_TYPE;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.p004db.ContentDB;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SoftwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.device.adpter.DeviceProxy;
import com.xcharge.charger.p006ui.adapter.api.UIServiceProxy;
import com.xcharge.charger.p006ui.adapter.type.CHALLENGE_TYPE;
import com.xcharge.charger.p006ui.adapter.type.UI_MODE;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.HandlerTimer;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.TimeUtils;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class OSSController {
    private static final long INTERVAL_UPGRADE_CHECK = 30000;
    public static final int MSG_CHARGE_TO_IDLE = 196613;
    public static final int MSG_DELAYED_UPGRADE_CHECK = 196612;
    public static final int MSG_DUMMY = 196608;
    public static final int MSG_REQUEST_QUERY = 196609;
    public static final int MSG_REQUEST_SET = 196610;
    public static final int MSG_REQUEST_UPGRADE = 196611;
    public static final int MSG_RESPONSE_QUERY = 196614;
    public static final int MSG_TIMEOUT_DCAP_GATEWAY_SESSION = 196615;
    public static final long TIMEOUT_DEFAULT_DCAP_GATEWAY_SESSION = 30000;
    private static OSSController instance = null;
    private long cloud_ts = 0;
    private Context context = null;
    /* access modifiers changed from: private */
    public HashMap<String, GatewaySession> dcapGatewaySessions = null;
    private HashMap<String, Object> delayLocaleSetting = null;
    private RequestSession delayedUpgradeRequest = null;
    private MsgHandler handler = null;
    private HandlerTimer handlerTimer = null;
    private boolean isDelayCloudTimeSynch = false;
    private boolean isDelayLocaleSetting = false;
    private long local_ts = 0;
    private HandlerThread thread = null;

    private class MsgHandler extends Handler {
        public MsgHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(android.os.Message r12) {
            /*
                r11 = this;
                r5 = 0
                int r7 = r12.what     // Catch:{ Exception -> 0x003a }
                switch(r7) {
                    case 196609: goto L_0x000a;
                    case 196610: goto L_0x006a;
                    case 196611: goto L_0x009b;
                    case 196612: goto L_0x00cc;
                    case 196613: goto L_0x00d3;
                    case 196614: goto L_0x00f2;
                    case 196615: goto L_0x0115;
                    default: goto L_0x0006;
                }
            L_0x0006:
                super.handleMessage(r12)
                return
            L_0x000a:
                java.lang.Object r7 = r12.obj     // Catch:{ Exception -> 0x003a }
                r0 = r7
                com.xcharge.charger.core.bean.RequestSession r0 = (com.xcharge.charger.core.bean.RequestSession) r0     // Catch:{ Exception -> 0x003a }
                r5 = r0
                java.lang.String r7 = "OSSController.handleMessage"
                java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x003a }
                java.lang.String r9 = "receive query request: "
                r8.<init>(r9)     // Catch:{ Exception -> 0x003a }
                com.xcharge.charger.core.api.bean.DCAPMessage r9 = r5.getRequest()     // Catch:{ Exception -> 0x003a }
                java.lang.String r9 = r9.toJson()     // Catch:{ Exception -> 0x003a }
                java.lang.StringBuilder r8 = r8.append(r9)     // Catch:{ Exception -> 0x003a }
                java.lang.String r8 = r8.toString()     // Catch:{ Exception -> 0x003a }
                android.util.Log.i(r7, r8)     // Catch:{ Exception -> 0x003a }
                com.xcharge.charger.core.controller.OSSController r7 = com.xcharge.charger.core.controller.OSSController.this     // Catch:{ Exception -> 0x003a }
                com.xcharge.charger.core.api.bean.DCAPMessage r8 = r5.getRequest()     // Catch:{ Exception -> 0x003a }
                com.xcharge.charger.core.api.bean.DCAPMessage r9 = r5.getConfirm()     // Catch:{ Exception -> 0x003a }
                r7.handleQueryRequest(r8, r9)     // Catch:{ Exception -> 0x003a }
                goto L_0x0006
            L_0x003a:
                r2 = move-exception
                java.lang.String r7 = "OSSController.handleMessage"
                java.lang.StringBuilder r8 = new java.lang.StringBuilder
                java.lang.String r9 = "except: "
                r8.<init>(r9)
                java.lang.String r9 = android.util.Log.getStackTraceString(r2)
                java.lang.StringBuilder r8 = r8.append(r9)
                java.lang.String r8 = r8.toString()
                android.util.Log.e(r7, r8)
                java.lang.StringBuilder r7 = new java.lang.StringBuilder
                java.lang.String r8 = "OSSController handleMessage exception: "
                r7.<init>(r8)
                java.lang.String r8 = android.util.Log.getStackTraceString(r2)
                java.lang.StringBuilder r7 = r7.append(r8)
                java.lang.String r7 = r7.toString()
                com.xcharge.common.utils.LogUtils.syslog(r7)
                goto L_0x0006
            L_0x006a:
                java.lang.Object r7 = r12.obj     // Catch:{ Exception -> 0x003a }
                r0 = r7
                com.xcharge.charger.core.bean.RequestSession r0 = (com.xcharge.charger.core.bean.RequestSession) r0     // Catch:{ Exception -> 0x003a }
                r5 = r0
                java.lang.String r7 = "OSSController.handleMessage"
                java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x003a }
                java.lang.String r9 = "receive set request: "
                r8.<init>(r9)     // Catch:{ Exception -> 0x003a }
                com.xcharge.charger.core.api.bean.DCAPMessage r9 = r5.getRequest()     // Catch:{ Exception -> 0x003a }
                java.lang.String r9 = r9.toJson()     // Catch:{ Exception -> 0x003a }
                java.lang.StringBuilder r8 = r8.append(r9)     // Catch:{ Exception -> 0x003a }
                java.lang.String r8 = r8.toString()     // Catch:{ Exception -> 0x003a }
                android.util.Log.i(r7, r8)     // Catch:{ Exception -> 0x003a }
                com.xcharge.charger.core.controller.OSSController r7 = com.xcharge.charger.core.controller.OSSController.this     // Catch:{ Exception -> 0x003a }
                com.xcharge.charger.core.api.bean.DCAPMessage r8 = r5.getRequest()     // Catch:{ Exception -> 0x003a }
                com.xcharge.charger.core.api.bean.DCAPMessage r9 = r5.getConfirm()     // Catch:{ Exception -> 0x003a }
                r7.handleSetRequest(r8, r9)     // Catch:{ Exception -> 0x003a }
                goto L_0x0006
            L_0x009b:
                java.lang.Object r7 = r12.obj     // Catch:{ Exception -> 0x003a }
                r0 = r7
                com.xcharge.charger.core.bean.RequestSession r0 = (com.xcharge.charger.core.bean.RequestSession) r0     // Catch:{ Exception -> 0x003a }
                r5 = r0
                java.lang.String r7 = "OSSController.handleMessage"
                java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x003a }
                java.lang.String r9 = "receive upgrade request: "
                r8.<init>(r9)     // Catch:{ Exception -> 0x003a }
                com.xcharge.charger.core.api.bean.DCAPMessage r9 = r5.getRequest()     // Catch:{ Exception -> 0x003a }
                java.lang.String r9 = r9.toJson()     // Catch:{ Exception -> 0x003a }
                java.lang.StringBuilder r8 = r8.append(r9)     // Catch:{ Exception -> 0x003a }
                java.lang.String r8 = r8.toString()     // Catch:{ Exception -> 0x003a }
                android.util.Log.i(r7, r8)     // Catch:{ Exception -> 0x003a }
                com.xcharge.charger.core.controller.OSSController r7 = com.xcharge.charger.core.controller.OSSController.this     // Catch:{ Exception -> 0x003a }
                com.xcharge.charger.core.api.bean.DCAPMessage r8 = r5.getRequest()     // Catch:{ Exception -> 0x003a }
                com.xcharge.charger.core.api.bean.DCAPMessage r9 = r5.getConfirm()     // Catch:{ Exception -> 0x003a }
                r7.handleUpgradeRequest(r8, r9)     // Catch:{ Exception -> 0x003a }
                goto L_0x0006
            L_0x00cc:
                com.xcharge.charger.core.controller.OSSController r7 = com.xcharge.charger.core.controller.OSSController.this     // Catch:{ Exception -> 0x003a }
                r7.handleDelayedUpgradeCheck()     // Catch:{ Exception -> 0x003a }
                goto L_0x0006
            L_0x00d3:
                java.lang.Object r4 = r12.obj     // Catch:{ Exception -> 0x003a }
                java.lang.String r4 = (java.lang.String) r4     // Catch:{ Exception -> 0x003a }
                java.lang.String r7 = "OSSController.handleMessage"
                java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x003a }
                java.lang.String r9 = "receive port charge to idle event, port: "
                r8.<init>(r9)     // Catch:{ Exception -> 0x003a }
                java.lang.StringBuilder r8 = r8.append(r4)     // Catch:{ Exception -> 0x003a }
                java.lang.String r8 = r8.toString()     // Catch:{ Exception -> 0x003a }
                android.util.Log.d(r7, r8)     // Catch:{ Exception -> 0x003a }
                com.xcharge.charger.core.controller.OSSController r7 = com.xcharge.charger.core.controller.OSSController.this     // Catch:{ Exception -> 0x003a }
                r7.handlePortCharge2Idle(r4)     // Catch:{ Exception -> 0x003a }
                goto L_0x0006
            L_0x00f2:
                java.lang.Object r6 = r12.obj     // Catch:{ Exception -> 0x003a }
                com.xcharge.charger.core.api.bean.DCAPMessage r6 = (com.xcharge.charger.core.api.bean.DCAPMessage) r6     // Catch:{ Exception -> 0x003a }
                java.lang.String r7 = "OSSController.handleMessage"
                java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x003a }
                java.lang.String r9 = "receive query response: "
                r8.<init>(r9)     // Catch:{ Exception -> 0x003a }
                java.lang.String r9 = r6.toJson()     // Catch:{ Exception -> 0x003a }
                java.lang.StringBuilder r8 = r8.append(r9)     // Catch:{ Exception -> 0x003a }
                java.lang.String r8 = r8.toString()     // Catch:{ Exception -> 0x003a }
                android.util.Log.i(r7, r8)     // Catch:{ Exception -> 0x003a }
                com.xcharge.charger.core.controller.OSSController r7 = com.xcharge.charger.core.controller.OSSController.this     // Catch:{ Exception -> 0x003a }
                r7.handleQueryResponse(r6)     // Catch:{ Exception -> 0x003a }
                goto L_0x0006
            L_0x0115:
                java.lang.Object r3 = r12.obj     // Catch:{ Exception -> 0x003a }
                com.xcharge.charger.core.bean.GatewaySession r3 = (com.xcharge.charger.core.bean.GatewaySession) r3     // Catch:{ Exception -> 0x003a }
                java.lang.String r7 = "OSSController.handleMessage"
                java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x003a }
                java.lang.String r9 = "timeout on gateway session: "
                r8.<init>(r9)     // Catch:{ Exception -> 0x003a }
                java.lang.String r9 = r3.toJson()     // Catch:{ Exception -> 0x003a }
                java.lang.StringBuilder r8 = r8.append(r9)     // Catch:{ Exception -> 0x003a }
                java.lang.String r8 = r8.toString()     // Catch:{ Exception -> 0x003a }
                android.util.Log.d(r7, r8)     // Catch:{ Exception -> 0x003a }
                com.xcharge.charger.core.controller.OSSController r7 = com.xcharge.charger.core.controller.OSSController.this     // Catch:{ Exception -> 0x003a }
                java.util.HashMap r7 = r7.dcapGatewaySessions     // Catch:{ Exception -> 0x003a }
                com.xcharge.charger.core.bean.IndicateSession r8 = r3.getIndicateSession()     // Catch:{ Exception -> 0x003a }
                com.xcharge.charger.core.api.bean.DCAPMessage r8 = r8.getIndicate()     // Catch:{ Exception -> 0x003a }
                long r8 = r8.getSeq()     // Catch:{ Exception -> 0x003a }
                java.lang.String r8 = java.lang.String.valueOf(r8)     // Catch:{ Exception -> 0x003a }
                r7.remove(r8)     // Catch:{ Exception -> 0x003a }
                com.xcharge.charger.core.bean.RequestSession r7 = r3.getRequestSession()     // Catch:{ Exception -> 0x003a }
                com.xcharge.charger.core.api.bean.DCAPMessage r7 = r7.getConfirm()     // Catch:{ Exception -> 0x003a }
                r8 = 20500(0x5014, float:2.8727E-41)
                java.lang.String r9 = "internal error"
                r10 = 0
                com.xcharge.charger.core.controller.ChargeController.nackConfirm(r7, r8, r9, r10)     // Catch:{ Exception -> 0x003a }
                goto L_0x0006
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.core.controller.OSSController.MsgHandler.handleMessage(android.os.Message):void");
        }
    }

    public static OSSController getInstance() {
        if (instance == null) {
            instance = new OSSController();
        }
        return instance;
    }

    public void init(Context context2) {
        this.context = context2;
        this.dcapGatewaySessions = new HashMap<>();
        UpgradeAgent.getInstance().init(this.context);
        this.thread = new HandlerThread("OSSController", 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(context2);
    }

    public void destroy() {
        this.handlerTimer.destroy();
        this.handler.removeMessages(MSG_REQUEST_QUERY);
        this.handler.removeMessages(MSG_REQUEST_SET);
        this.handler.removeMessages(MSG_REQUEST_UPGRADE);
        this.handler.removeMessages(MSG_DELAYED_UPGRADE_CHECK);
        this.handler.removeMessages(MSG_CHARGE_TO_IDLE);
        this.handler.removeMessages(MSG_RESPONSE_QUERY);
        this.handler.removeMessages(MSG_TIMEOUT_DCAP_GATEWAY_SESSION);
        this.thread.quit();
        UpgradeAgent.getInstance().destroy();
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

    public void handleRequest(DCAPMessage request, DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) request.getData();
        String op = cap.getOp();
        int msgId = MSG_DUMMY;
        try {
            if ("query".equals(op)) {
                cap.setData((QueryDirective) new QueryDirective().fromJson(JsonBean.ObjectToJson(cap.getData())));
                msgId = MSG_REQUEST_QUERY;
            } else if ("set".equals(op)) {
                cap.setData((SetDirective) new SetDirective().fromJson(JsonBean.ObjectToJson(cap.getData())));
                msgId = MSG_REQUEST_SET;
            } else if ("upgrade".equals(op)) {
                cap.setData(new UpgradeDirective().fromJson(JsonBean.ObjectToJson(cap.getData())));
                msgId = MSG_REQUEST_UPGRADE;
            }
            if (msgId != 196608) {
                RequestSession requestSession = new RequestSession();
                requestSession.setRequest(request);
                requestSession.setConfirm(confirm);
                this.handler.sendMessage(this.handler.obtainMessage(msgId, requestSession));
            }
        } catch (Exception e) {
            Log.e("OSSController.handleRequest", "request: " + request.toJson() + ", exception: " + Log.getStackTraceString(e));
            ChargeController.nackConfirm(confirm, ErrorCode.EC_INTERNAL_ERROR, e.toString(), (HashMap<String, Object>) null);
        }
    }

    public void handleResponse(DCAPMessage response) {
        CAPMessage cap = (CAPMessage) response.getData();
        String op = cap.getOp();
        String peerOp = cap.getOpt().getOp();
        int msgId = MSG_DUMMY;
        try {
            if ("ack".equals(op)) {
                cap.setData((AckDirective) new AckDirective().fromJson(JsonBean.ObjectToJson(cap.getData())));
                if ("query".equals(peerOp)) {
                    msgId = MSG_RESPONSE_QUERY;
                }
            } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
                cap.setData((NackDirective) new NackDirective().fromJson(JsonBean.ObjectToJson(cap.getData())));
                if ("query".equals(peerOp)) {
                    msgId = MSG_RESPONSE_QUERY;
                }
            } else {
                return;
            }
            if (msgId != 196608) {
                this.handler.sendMessage(this.handler.obtainMessage(msgId, response));
            }
        } catch (Exception e) {
            Log.e("OSSController.handleResponse", "response: " + response.toJson() + ", exception: " + Log.getStackTraceString(e));
        }
    }

    /* access modifiers changed from: private */
    public void handleQueryRequest(DCAPMessage request, DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) request.getData();
        String queryId = cap.getOpt().getQuery_id();
        if (queryId.startsWith("device.port.")) {
            Port portStatus = DeviceProxy.getInstance().getPortRuntimeStatus(queryId.split("\\.")[2]);
            if (portStatus != null) {
                HashMap<String, Object> attach = new HashMap<>();
                attach.put(queryId, portStatus);
                ChargeController.ackConfirm(confirm, attach);
                return;
            }
        } else if (queryId.equals("device.verification")) {
            HashMap<String, Object> params = ((QueryDirective) cap.getData()).getParams();
            String xid = String.valueOf(request.getSeq());
            Bundle data = new Bundle();
            data.putString("type", CHALLENGE_TYPE.verification.getType());
            data.putString("xid", xid);
            data.putString("customer", (String) params.get("customer"));
            data.putInt("expired", Integer.parseInt((String) params.get("expired")));
            UIServiceProxy.getInstance().sendUIModeEvent(UI_MODE.challenge, data);
            return;
        } else if (queryId.startsWith(QueryDirective.QUERY_ID_PORT_PLUGIN_UPDATE)) {
            ChargeController.getInstance().getChargeHandler(queryId.split("\\.")[3]).sendEmptyMessage(ChargeHandler.MSG_PLUGIN_CHECK_EVENT);
            return;
        } else if (queryId.equals(QueryDirective.QUERY_ID_CARD_STATUS)) {
            HashMap<String, Object> params2 = ((QueryDirective) cap.getData()).getParams();
            NFC_CARD_TYPE cardType = NFC_CARD_TYPE.valueOf((String) params2.get("cardType"));
            String cardNo = (String) params2.get("cardNo");
            CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
            if (CHARGE_PLATFORM.ocpp.equals(platform) && NFC_CARD_TYPE.ocpp.equals(cardType)) {
                CAPDirectiveOption indicateOpt = new CAPDirectiveOption();
                indicateOpt.setQuery_id(queryId);
                HashMap<String, Object> indicateParams = new HashMap<>();
                indicateParams.put("cardNo", cardNo);
                QueryDirective queryIndicate = new QueryDirective();
                queryIndicate.setParams(indicateParams);
                DCAPMessage indicate = ChargeController.createIndicate("server:" + platform, "query", indicateOpt, queryIndicate);
                DCAPProxy.getInstance().sendIndicate(indicate);
                RequestSession requestSession = new RequestSession();
                requestSession.setRequest(request);
                requestSession.setConfirm(confirm);
                IndicateSession indicateSession = new IndicateSession();
                indicateSession.setIndicate(indicate);
                GatewaySession gatewaySession = new GatewaySession();
                gatewaySession.setRequestSession(requestSession);
                gatewaySession.setIndicateSession(indicateSession);
                this.dcapGatewaySessions.put(String.valueOf(indicate.getSeq()), gatewaySession);
                this.handlerTimer.startTimer(30000, MSG_TIMEOUT_DCAP_GATEWAY_SESSION, gatewaySession);
                return;
            }
        }
        Log.w("OSSController.handleQueryRequest", "failed to handle query request: " + request.toJson());
        ChargeController.nackConfirm(confirm, ErrorCode.EC_INTERNAL_ERROR, "internal error", (HashMap<String, Object>) null);
    }

    /* access modifiers changed from: private */
    public void handleQueryResponse(DCAPMessage response) {
        GatewaySession gatewaySession = this.dcapGatewaySessions.remove(String.valueOf(((CAPMessage) response.getData()).getOpt().getSeq()));
        if (gatewaySession != null) {
            DCAPMessage confirm = gatewaySession.getRequestSession().getConfirm();
            CAPMessage responseCap = (CAPMessage) response.getData();
            String responseOp = responseCap.getOp();
            if ("ack".equals(responseOp)) {
                ChargeController.ackConfirm(confirm, ((AckDirective) responseCap.getData()).getAttach());
            } else if (CAPMessage.DIRECTIVE_NACK.equals(responseOp)) {
                NackDirective nack = (NackDirective) responseCap.getData();
                ChargeController.nackConfirm(gatewaySession.getRequestSession().getConfirm(), nack.getError(), nack.getMsg(), nack.getAttach());
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleSetRequest(DCAPMessage request, DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) request.getData();
        String setId = cap.getOpt().getSet_id();
        HashMap<String, Object> values = ((SetDirective) cap.getData()).getValues();
        if (SetDirective.SET_ID_DEVICE.equals(setId)) {
            if ("reboot".equals((String) values.get("opr"))) {
                LocalBroadcastManager.getInstance(this.context).sendBroadcast(new Intent(DCAPProxy.ACTION_DEVICE_REBOOT_EVENT));
                ChargeController.ackConfirm(confirm, (HashMap<String, Object>) null);
                return;
            }
        } else if (SetDirective.SET_ID_PORT_GUNLOCK.equals(setId)) {
            String port = (String) values.get(ContentDB.ChargeTable.PORT);
            if (TextUtils.isEmpty(port)) {
                port = "1";
            }
            String opr = (String) values.get("opr");
            if ("guest".equals(request.getFrom())) {
                LogUtils.applog("receive gunlock " + opr + " request from UI");
            }
            if (!LOCK_STATUS.disable.equals(ChargeStatusCacheProvider.getInstance().getPortLockStatus(port))) {
                if (SetDirective.OPR_LOCK.equals(opr)) {
                    DeviceProxy.getInstance().lockGun(port);
                    ChargeStatusCacheProvider.getInstance().updatePortLockStatus(port, LOCK_STATUS.lock);
                } else if (SetDirective.OPR_UNLOCK.equals(opr)) {
                    DeviceProxy.getInstance().unlockGun(port);
                    ChargeStatusCacheProvider.getInstance().updatePortLockStatus(port, LOCK_STATUS.unlock);
                } else {
                    "disable".equals(opr);
                }
            } else if (!"enable".equals(opr)) {
                if (SetDirective.OPR_LOCK.equals(opr)) {
                    DeviceProxy.getInstance().enableGunLock(port);
                    DeviceProxy.getInstance().lockGun(port);
                    DeviceProxy.getInstance().disableGunLock(port);
                } else if (SetDirective.OPR_UNLOCK.equals(opr)) {
                    DeviceProxy.getInstance().enableGunLock(port);
                    DeviceProxy.getInstance().unlockGun(port);
                    DeviceProxy.getInstance().disableGunLock(port);
                }
            }
            ChargeController.ackConfirm(confirm, (HashMap<String, Object>) null);
            return;
        } else if (SetDirective.SET_ID_PORT_AMP_WORK.equals(setId)) {
            String port2 = (String) values.get(ContentDB.ChargeTable.PORT);
            if (TextUtils.isEmpty(port2)) {
                port2 = "1";
            }
            int adjustAmp = new BigDecimal(Double.valueOf(Double.parseDouble((String) values.get("value"))).doubleValue()).setScale(0, 4).intValue();
            DeviceProxy.getInstance().ajustChargeAmp(port2, adjustAmp);
            ChargeStatusCacheProvider.getInstance().updateAdjustAmp(adjustAmp);
            ChargeController.ackConfirm(confirm, (HashMap<String, Object>) null);
            return;
        } else if ("device.verification".equals(setId)) {
            CAPDirectiveOption queryOpt = new CAPDirectiveOption();
            queryOpt.setQuery_id("device.verification");
            queryOpt.setOp("query");
            queryOpt.setSeq(Long.valueOf(Long.parseLong((String) values.get("xid"))));
            DCAPMessage verificationConfirm = DCAPProxy.getInstance().createCAPConfirm("server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform(), queryOpt);
            if ("1".equals((String) values.get("result"))) {
                ChargeController.ackConfirm(verificationConfirm, (HashMap<String, Object>) null);
                return;
            } else {
                ChargeController.nackConfirm(verificationConfirm, ErrorCode.EC_INTERNAL_ERROR, "internal error", (HashMap<String, Object>) null);
                return;
            }
        } else if (SetDirective.SET_ID_DEVICE_TIME_CLOUDSYNCH.equals(setId)) {
            String cloud_ts2 = (String) values.get("cloud_ts");
            String local_ts2 = (String) values.get("local_ts");
            if (ChargeController.getInstance().hasCharge((CHARGE_INIT_TYPE) null, false)) {
                LogUtils.syslog("charging now, delay to synch cloud time, cloud ts: " + cloud_ts2 + ", local ts: " + local_ts2);
                this.isDelayCloudTimeSynch = true;
                this.cloud_ts = Long.parseLong(cloud_ts2);
                this.local_ts = Long.parseLong(local_ts2);
            } else {
                synchCloudTime(Long.parseLong(cloud_ts2), Long.parseLong(local_ts2));
            }
            ChargeController.ackConfirm(confirm, (HashMap<String, Object>) null);
            return;
        } else if (SetDirective.SET_ID_DEVICE_CP_RANGE.equals(setId)) {
            int cpRange = Integer.parseInt((String) values.get("value"));
            DeviceProxy.getInstance().setCPRange(cpRange);
            ChargeStatusCacheProvider.getInstance().updateCPRange(cpRange);
            ChargeController.ackConfirm(confirm, (HashMap<String, Object>) null);
            return;
        } else if (SetDirective.SET_ID_DEVICE_VOLT_RANGE.equals(setId)) {
            int voltRange = Integer.parseInt((String) values.get("value"));
            DeviceProxy.getInstance().setVoltageRange(voltRange);
            ChargeStatusCacheProvider.getInstance().updateVoltageRange(voltRange);
            ChargeController.ackConfirm(confirm, (HashMap<String, Object>) null);
            return;
        } else if (SetDirective.SET_ID_DEVICE_LEAKAGE_TOLERANCE.equals(setId)) {
            int leakageTolerance = Integer.parseInt((String) values.get("value"));
            DeviceProxy.getInstance().setLeakageTolerance(leakageTolerance);
            ChargeStatusCacheProvider.getInstance().updateLeakageTolerance(Integer.valueOf(leakageTolerance));
            ChargeController.ackConfirm(confirm, (HashMap<String, Object>) null);
            return;
        } else if (SetDirective.SET_ID_DEVICE_EARTH_DISABLE.equals(setId)) {
            boolean earthDisable = "disable".equals((String) values.get("value"));
            DeviceProxy.getInstance().setEarthDisable(earthDisable);
            ChargeStatusCacheProvider.getInstance().updateEarthDisable(Boolean.valueOf(earthDisable));
            ChargeController.ackConfirm(confirm, (HashMap<String, Object>) null);
            return;
        } else if (SetDirective.SET_ID_DEVICE_LOCALE.equals(setId)) {
            if (ChargeController.getInstance().hasCharge((CHARGE_INIT_TYPE) null, false)) {
                LogUtils.syslog("charging now, delay to set locale params: " + JsonBean.mapToJson(values));
                this.delayLocaleSetting = values;
                this.isDelayLocaleSetting = true;
            } else {
                setLocaleParams(values);
            }
            ChargeController.ackConfirm(confirm, (HashMap<String, Object>) null);
            return;
        }
        Log.w("OSSController.handleSetRequest", "failed to handle set request: " + request.toJson());
        ChargeController.nackConfirm(confirm, ErrorCode.EC_INTERNAL_ERROR, "internal error", (HashMap<String, Object>) null);
    }

    private void setLocaleParams(Map localeSetting) {
        Log.i("OSSController.setLocaleParams", "local locale before set: " + CountrySettingCacheProvider.getInstance().getCountrySetting().toJson());
        if (localeSetting.containsKey("dst")) {
            CountrySettingCacheProvider.getInstance().updateUseDaylightTime(((Boolean) localeSetting.get("dst")).booleanValue());
        }
        String localTimezone = CountrySettingCacheProvider.getInstance().getZone();
        if (localeSetting.containsKey("zone")) {
            localTimezone = (String) localeSetting.get("zone");
        }
        if (localeSetting.containsKey("zone") || localeSetting.containsKey("dst")) {
            boolean useDST = CountrySettingCacheProvider.getInstance().isUseDaylightTime();
            String zoneId = TimeUtils.getTimezoneId(localTimezone, useDST);
            if (TextUtils.isEmpty(zoneId)) {
                Log.w("OSSController.setLocaleParams", "unavailable id for timezone: " + localTimezone);
            } else {
                ((AlarmManager) this.context.getSystemService("alarm")).setTimeZone(zoneId);
                if (localeSetting.containsKey("zone")) {
                    CountrySettingCacheProvider.getInstance().updateZone(localTimezone);
                }
                boolean realUseDST = TimeZone.getTimeZone(zoneId).useDaylightTime();
                if (realUseDST != useDST) {
                    CountrySettingCacheProvider.getInstance().updateUseDaylightTime(realUseDST);
                }
                Log.i("OSSController.setLocaleParams", "set timezone: " + localTimezone + " using id: " + zoneId + ", useDST: " + realUseDST);
                LogUtils.syslog("set timezone: " + localTimezone + " using id: " + zoneId + ", useDST: " + realUseDST);
            }
        }
        if (localeSetting.containsKey("lang")) {
            CountrySettingCacheProvider.getInstance().updateLang((String) localeSetting.get("lang"));
        }
        if (localeSetting.containsKey("money")) {
            CountrySettingCacheProvider.getInstance().updateMoney((String) localeSetting.get("money"));
        }
        if (localeSetting.containsKey("moneyDisp")) {
            CountrySettingCacheProvider.getInstance().updateMoneyDisp((String) localeSetting.get("moneyDisp"));
        }
        Log.i("OSSController.setLocaleParams", "local locale after set: " + CountrySettingCacheProvider.getInstance().getCountrySetting().toJson());
    }

    /* access modifiers changed from: private */
    public void handleUpgradeRequest(DCAPMessage request, DCAPMessage confirm) {
        UpgradeDirective upgrade = (UpgradeDirective) ((CAPMessage) request.getData()).getData();
        HashMap<String, SwipeCardPermission> restoreSwipeCardPermissions = new HashMap<>();
        for (String port : HardwareStatusCacheProvider.getInstance().getPorts().keySet()) {
            restoreSwipeCardPermissions.put(port, SystemSettingCacheProvider.getInstance().getPortSwipeCardPermission(port));
            SwipeCardPermission forbidenSwipeCard = new SwipeCardPermission();
            forbidenSwipeCard.setPermitSetting(false);
            forbidenSwipeCard.setPermitChargeCtrl(false);
            forbidenSwipeCard.setPermitBinding(false);
            SystemSettingCacheProvider.getInstance().updatePortSwipeCardPermission(port, forbidenSwipeCard);
        }
        this.handlerTimer.stopTimer(MSG_DELAYED_UPGRADE_CHECK);
        this.delayedUpgradeRequest = null;
        if (UpgradeAgent.getInstance().update(upgrade)) {
            ChargeController.ackConfirm(confirm, (HashMap<String, Object>) null);
        } else {
            ChargeController.nackConfirm(confirm, SoftwareStatusCacheProvider.getInstance().getUpgradeProgress().getError().getCode(), (String) null, (HashMap<String, Object>) null);
            ErrorCode error = SoftwareStatusCacheProvider.getInstance().getUpgradeProgress().getError();
            if (error != null && error.getCode() == 60010) {
                this.delayedUpgradeRequest = new RequestSession();
                this.delayedUpgradeRequest.setRequest(request);
                this.delayedUpgradeRequest.setConfirm(confirm);
                this.handlerTimer.startTimer(30000, MSG_DELAYED_UPGRADE_CHECK, (Object) null);
            }
        }
        for (Map.Entry<String, SwipeCardPermission> entry : restoreSwipeCardPermissions.entrySet()) {
            SystemSettingCacheProvider.getInstance().updatePortSwipeCardPermission(entry.getKey(), entry.getValue());
        }
    }

    /* access modifiers changed from: private */
    public void handleDelayedUpgradeCheck() {
        if (this.delayedUpgradeRequest == null) {
            return;
        }
        if (ChargeController.getInstance().hasCharge((CHARGE_INIT_TYPE) null, false)) {
            this.handlerTimer.startTimer(30000, MSG_DELAYED_UPGRADE_CHECK, (Object) null);
        } else {
            this.handler.sendMessageDelayed(this.handler.obtainMessage(MSG_REQUEST_UPGRADE, this.delayedUpgradeRequest), 120000);
        }
    }

    /* access modifiers changed from: private */
    public void handlePortCharge2Idle(String port) {
        if (!ChargeController.getInstance().hasCharge((CHARGE_INIT_TYPE) null, false) && this.isDelayCloudTimeSynch) {
            LogUtils.syslog("charge ended, try to synch cloud time, cloud ts: " + this.cloud_ts + ", local ts: " + this.local_ts);
            synchCloudTime(this.cloud_ts, this.local_ts);
        }
        if (!ChargeController.getInstance().hasCharge((CHARGE_INIT_TYPE) null, false) && this.isDelayLocaleSetting) {
            setLocaleParams(this.delayLocaleSetting);
            this.isDelayLocaleSetting = false;
        }
    }

    private void synchCloudTime(long cloudTs, long localTs) {
        long localTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (localTime >= localTs) {
            long cloudTime = (cloudTs + localTime) - localTs;
            Log.i("OSSController.synchCloudTime", "local time before synch set: " + sdf.format(new Date(localTime)));
            Log.i("OSSController.synchCloudTime", "cloud time: " + sdf.format(new Date(cloudTime)));
            SystemClock.setCurrentTimeMillis(cloudTime);
            LogUtils.syslog("synch cloud time: " + sdf.format(new Date(cloudTime)));
            Log.i("OSSController.synchCloudTime", "local time after synch setted: " + sdf.format(new Date(System.currentTimeMillis())));
            this.isDelayCloudTimeSynch = false;
            ChargeStatusCacheProvider.getInstance().updateCloudTimeSynch(true);
            return;
        }
        Log.w("OSSController.synchCloudTime", "invalid time, now time " + sdf.format(new Date(localTime)) + " is lower than local time received synch from cloud: " + sdf.format(new Date(localTs)));
        LogUtils.syslog("failed to synch cloud time, and now time " + sdf.format(new Date(localTime)) + " is lower than local time received synch from cloud: " + sdf.format(new Date(localTs)));
    }
}
