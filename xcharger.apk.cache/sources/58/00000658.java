package com.xcharge.charger.protocol.anyo.router;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.api.Sequence;
import com.xcharge.charger.core.api.bean.AckDirective;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.cap.CAPDirectiveOption;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.core.api.bean.cap.FinDirective;
import com.xcharge.charger.core.api.bean.cap.InitDirective;
import com.xcharge.charger.core.api.bean.cap.SetDirective;
import com.xcharge.charger.core.api.bean.cap.StartDirective;
import com.xcharge.charger.core.api.bean.cap.StopDirective;
import com.xcharge.charger.core.type.FIN_MODE;
import com.xcharge.charger.data.bean.type.CHARGE_INIT_TYPE;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.CHARGE_USER_TYPE;
import com.xcharge.charger.data.bean.type.USER_TC_TYPE;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.anyo.bean.request.ResetChargeRequest;
import com.xcharge.charger.protocol.anyo.bean.request.StartChargeRequest;
import com.xcharge.charger.protocol.anyo.bean.request.StopChargeRequest;
import com.xcharge.charger.protocol.anyo.bean.request.UnlockPortRequest;
import com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent;
import com.xcharge.charger.protocol.anyo.session.AnyoChargeSession;
import com.xcharge.common.bean.JsonBean;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* loaded from: classes.dex */
public class DCAPAdapter {
    public static final long DEFAULT_SESSION_TIMEOUT = 15000;
    private Context context = null;
    private String sn = null;
    private HashMap<String, DCAPAdapterSession> dcapAdapterSessions = null;

    /* loaded from: classes.dex */
    private static class DCAPAdapterSession extends JsonBean<DCAPAdapterSession> {
        AnyoMessage anyoMsg;
        long anyoTimestamp;
        DCAPMessage dcapMsg;
        String directiveType;

        private DCAPAdapterSession() {
            this.anyoMsg = null;
            this.dcapMsg = null;
            this.directiveType = null;
            this.anyoTimestamp = 0L;
        }

        /* synthetic */ DCAPAdapterSession(DCAPAdapterSession dCAPAdapterSession) {
            this();
        }

        public AnyoMessage getAnyoMsg() {
            return this.anyoMsg;
        }

        public void setAnyoMsg(AnyoMessage anyoMsg) {
            this.anyoMsg = anyoMsg;
        }

        public DCAPMessage getDcapMsg() {
            return this.dcapMsg;
        }

        public void setDcapMsg(DCAPMessage dcapMsg) {
            this.dcapMsg = dcapMsg;
        }

        public String getDirectiveType() {
            return this.directiveType;
        }

        public void setDirectiveType(String directiveType) {
            this.directiveType = directiveType;
        }

        public long getAnyoTimestamp() {
            return this.anyoTimestamp;
        }

        public void setAnyoTimestamp(long anyoTimestamp) {
            this.anyoTimestamp = anyoTimestamp;
        }
    }

    public void init(Context context) {
        this.context = context;
        this.sn = HardwareStatusCacheProvider.getInstance().getSn();
        this.dcapAdapterSessions = new HashMap<>();
    }

    public void destroy() {
        this.dcapAdapterSessions.clear();
    }

    public void maintainSession() {
        if (this.dcapAdapterSessions.size() != 0) {
            long nowTimestamp = System.currentTimeMillis();
            Iterator<Map.Entry<String, DCAPAdapterSession>> it2 = this.dcapAdapterSessions.entrySet().iterator();
            while (it2.hasNext()) {
                DCAPAdapterSession session = it2.next().getValue();
                if (nowTimestamp - session.anyoTimestamp > 15000) {
                    Log.w("DCAPAdapter.maintainSession", "destroy an timeout DCAP adapter session: " + session.toJson());
                    AnyoMessage request = session.anyoMsg;
                    byte cmd = request.getHead().getCmdCode();
                    switch (cmd) {
                        case 60:
                            AnyoProtocolAgent.getInstance().sendStartChargeResponse((StartChargeRequest) request, AnyoMessage.STATUS_CODE_INTERNAL_ERROR);
                            break;
                        case 61:
                            AnyoProtocolAgent.getInstance().sendStopChargeResponse((StopChargeRequest) request, AnyoMessage.STATUS_CODE_INTERNAL_ERROR);
                            break;
                        case 81:
                            finRequest(FIN_MODE.timeout, session.dcapMsg);
                            AnyoProtocolAgent.getInstance().sendUnlockPortResponse((UnlockPortRequest) request, AnyoMessage.STATUS_CODE_INTERNAL_ERROR);
                            break;
                    }
                    it2.remove();
                }
            }
        }
    }

    public void handleUnlockPortRequest(AnyoMessage request) {
        UnlockPortRequest unlockPortRequest = (UnlockPortRequest) request;
        String chargeId = String.valueOf(unlockPortRequest.getBillId());
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(chargeId);
        String userType = CHARGE_USER_TYPE.anyo.getUserType();
        String userCode = String.valueOf(unlockPortRequest.getUserId());
        InitDirective init = new InitDirective();
        init.setInit_type(CHARGE_INIT_TYPE.native_qrcode);
        init.setUser_type(userType);
        init.setUser_code(userCode);
        init.setDevice_id("sn/" + this.sn);
        init.setPort(AnyoProtocolAgent.getInstance().getLocalPort(unlockPortRequest.getPort()));
        init.setCharge_platform(CHARGE_PLATFORM.anyo);
        init.setTimeout_plugin(60);
        init.setTimeout_start(-1);
        init.setTimeout_plugout(-1);
        String from = "user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode;
        DCAPMessage initRequest = createRequest(from, "init", opt, init);
        DCAPProxy.getInstance().sendRequest(initRequest);
        DCAPAdapterSession dcapAdapterSession = new DCAPAdapterSession(null);
        dcapAdapterSession.anyoMsg = unlockPortRequest;
        dcapAdapterSession.anyoTimestamp = System.currentTimeMillis();
        dcapAdapterSession.dcapMsg = initRequest;
        dcapAdapterSession.directiveType = "init";
        Log.i("DCAPAdapter.handleUnlockPortRequest", "create an DCAP adapter session: " + dcapAdapterSession.toJson());
        this.dcapAdapterSessions.put(String.valueOf(initRequest.getSeq()), dcapAdapterSession);
    }

    public void handleStartChargeRequest(AnyoMessage request) {
        StartChargeRequest startChargeRequest = (StartChargeRequest) request;
        AnyoChargeSession chargeSession = AnyoProtocolAgent.getInstance().getChargeSession(startChargeRequest.getPort());
        if (chargeSession == null) {
            Log.w("DCAPAdapter.handleStartChargeRequest", "not found charge session on port: " + startChargeRequest.getPort());
            AnyoProtocolAgent.getInstance().sendStartChargeResponse(startChargeRequest, AnyoMessage.STATUS_CODE_INTERNAL_ERROR);
            return;
        }
        Log.i("DCAPAdapter.handleStartChargeRequest", "found charge session: " + chargeSession.toJson());
        String chargeId = chargeSession.getCharge_id();
        String userType = chargeSession.getUser_type();
        String userCode = chargeSession.getUser_code();
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(chargeId);
        StartDirective start = new StartDirective();
        byte chargePolicyKey = startChargeRequest.getChargePolicyKey();
        long chargePolicyValue = startChargeRequest.getChargePolicyValue();
        USER_TC_TYPE UserTcType = USER_TC_TYPE.auto;
        String UserTcValue = null;
        switch (chargePolicyKey) {
            case 1:
                UserTcType = USER_TC_TYPE.power;
                double power = new BigDecimal(chargePolicyValue / 100).setScale(2, 4).doubleValue();
                UserTcValue = String.format("%2f", Double.valueOf(power));
                break;
            case 2:
                UserTcType = USER_TC_TYPE.fee;
                UserTcValue = String.valueOf(chargePolicyValue);
                break;
            case 3:
                UserTcType = USER_TC_TYPE.time;
                UserTcValue = String.valueOf(chargePolicyValue);
                break;
        }
        start.setUser_tc_type(UserTcType);
        start.setUser_tc_value(UserTcValue);
        String from = "user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode;
        DCAPMessage startRequest = createRequest(from, "start", opt, start);
        DCAPProxy.getInstance().sendRequest(startRequest);
        DCAPAdapterSession dcapAdapterSession = new DCAPAdapterSession(null);
        dcapAdapterSession.anyoMsg = startChargeRequest;
        dcapAdapterSession.anyoTimestamp = System.currentTimeMillis();
        dcapAdapterSession.dcapMsg = startRequest;
        dcapAdapterSession.directiveType = "start";
        Log.i("DCAPAdapter.handleStartChargeRequest", "create an DCAP adapter session: " + dcapAdapterSession.toJson());
        this.dcapAdapterSessions.put(String.valueOf(startRequest.getSeq()), dcapAdapterSession);
    }

    public void handleStopChargeRequest(AnyoMessage request) {
        StopChargeRequest stopChargeRequest = (StopChargeRequest) request;
        AnyoChargeSession chargeSession = AnyoProtocolAgent.getInstance().getChargeSession(stopChargeRequest.getPort());
        if (chargeSession == null) {
            Log.w("DCAPAdapter.handleStopChargeRequest", "not found charge session on port: " + stopChargeRequest.getPort());
            AnyoProtocolAgent.getInstance().sendStopChargeResponse(stopChargeRequest, AnyoMessage.STATUS_CODE_INTERNAL_ERROR);
            return;
        }
        Log.i("DCAPAdapter.handleStopChargeRequest", "found charge session: " + chargeSession.toJson());
        String chargeId = chargeSession.getCharge_id();
        String userType = chargeSession.getUser_type();
        String userCode = chargeSession.getUser_code();
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(chargeId);
        StopDirective stop = new StopDirective();
        String from = "user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode;
        DCAPMessage stopRequest = createRequest(from, "stop", opt, stop);
        DCAPProxy.getInstance().sendRequest(stopRequest);
        DCAPAdapterSession dcapAdapterSession = new DCAPAdapterSession(null);
        dcapAdapterSession.anyoMsg = stopChargeRequest;
        dcapAdapterSession.anyoTimestamp = System.currentTimeMillis();
        dcapAdapterSession.dcapMsg = stopRequest;
        dcapAdapterSession.directiveType = "stop";
        Log.i("DCAPAdapter.handleStopChargeRequest", "create an DCAP adapter session: " + dcapAdapterSession.toJson());
        this.dcapAdapterSessions.put(String.valueOf(stopRequest.getSeq()), dcapAdapterSession);
    }

    public void handleResetChargeRequest(AnyoMessage request) {
        ResetChargeRequest resetChargeRequest = (ResetChargeRequest) request;
        AnyoChargeSession chargeSession = AnyoProtocolAgent.getInstance().getChargeSession(resetChargeRequest.getPort());
        if (chargeSession == null) {
            Log.w("DCAPAdapter.handleResetChargeRequest", "not found charge session on port: " + resetChargeRequest.getPort());
            AnyoProtocolAgent.getInstance().sendResetChargeResponse(resetChargeRequest, AnyoMessage.STATUS_CODE_INTERNAL_ERROR);
            return;
        }
        Log.i("DCAPAdapter.handleResetChargeRequest", "found charge session: " + chargeSession.toJson());
        String chargeId = chargeSession.getCharge_id();
        if (TextUtils.isEmpty(chargeId)) {
            Log.w("DCAPAdapter.handleResetChargeRequest", "no charge on port: : " + resetChargeRequest.getPort());
            AnyoProtocolAgent.getInstance().sendResetChargeResponse(resetChargeRequest, (byte) 19);
            return;
        }
        DCAPMessage finRequest = finChargeByRemote(chargeId);
        if (finRequest == null) {
            Log.w("DCAPAdapter.handleResetChargeRequest", "failed to send fin requst to charge: " + chargeId);
            AnyoProtocolAgent.getInstance().sendResetChargeResponse(resetChargeRequest, (byte) 19);
            return;
        }
        chargeSession.setResetChargeRequest(resetChargeRequest);
    }

    public void handleRebootRequest(AnyoMessage request) {
        HashMap<String, Object> values = new HashMap<>();
        values.put("opr", "reboot");
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setSet_id(SetDirective.SET_ID_DEVICE);
        SetDirective set = new SetDirective();
        set.setValues(values);
        String from = "server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform();
        DCAPMessage setRequest = createRequest(from, "set", opt, set);
        DCAPProxy.getInstance().sendRequest(setRequest);
        DCAPAdapterSession dcapAdapterSession = new DCAPAdapterSession(null);
        dcapAdapterSession.anyoMsg = request;
        dcapAdapterSession.anyoTimestamp = System.currentTimeMillis();
        dcapAdapterSession.dcapMsg = setRequest;
        dcapAdapterSession.directiveType = "set";
        Log.i("DCAPAdapter.handleRebootRequest", "create DCAP adapter session: " + dcapAdapterSession.toJson());
        this.dcapAdapterSessions.put(String.valueOf(setRequest.getSeq()), dcapAdapterSession);
    }

    public void handleSetConfirm(DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) confirm.getData();
        CAPDirectiveOption opt = cap.getOpt();
        String peerSeq = String.valueOf(opt.getSeq());
        DCAPAdapterSession dcapAdapterSession = this.dcapAdapterSessions.get(peerSeq);
        if (dcapAdapterSession == null) {
            Log.w("DCAPAdapter.handleSetConfirm", "not found DCAP adapter session for confirm: " + confirm.toJson());
            return;
        }
        Log.i("DCAPAdapter.handleSetConfirm", "destroy DCAP adapter session: " + dcapAdapterSession.toJson());
        this.dcapAdapterSessions.remove(peerSeq);
    }

    public void handleInitAckIndicate(DCAPMessage indicate) {
        CAPMessage cap = (CAPMessage) indicate.getData();
        CAPDirectiveOption opt = cap.getOpt();
        String ackInitSeq = String.valueOf(opt.getSeq());
        DCAPAdapterSession dcapAdapterSession = this.dcapAdapterSessions.get(ackInitSeq);
        if (dcapAdapterSession == null) {
            Log.w("DCAPAdapter.handleInitAckIndicate", "not found DCAP adapter session for indicate: " + indicate.toJson());
        } else if (dcapAdapterSession.directiveType.equals("init") && dcapAdapterSession.anyoMsg.getHead().getCmdCode() == 81) {
            ackResponse(indicate);
            AnyoProtocolAgent.getInstance().sendUnlockPortResponse((UnlockPortRequest) dcapAdapterSession.anyoMsg, (byte) 0);
            Log.i("DCAPAdapter.handleInitAckIndicate", "destroy an DCAP adapter session: " + dcapAdapterSession.toJson());
            this.dcapAdapterSessions.remove(ackInitSeq);
        } else {
            Log.w("DCAPAdapter.handleInitAckIndicate", "not matched DCAP adapter session: " + dcapAdapterSession.toJson() + " and indicate: " + indicate.toJson());
        }
    }

    public void handleFinIndicate(DCAPMessage indicate) {
        ackResponse(indicate);
        CAPMessage cap = (CAPMessage) indicate.getData();
        CAPDirectiveOption opt = cap.getOpt();
        if ("init".equals(opt.getOp())) {
            String nackInitSeq = String.valueOf(opt.getSeq());
            DCAPAdapterSession dcapAdapterSession = this.dcapAdapterSessions.get(nackInitSeq);
            if (dcapAdapterSession == null) {
                Log.w("DCAPAdapter.handleFinIndicate", "not found DCAP adapter session for indicate: " + indicate.toJson());
            } else if (dcapAdapterSession.directiveType.equals("init") && dcapAdapterSession.anyoMsg.getHead().getCmdCode() == 81) {
                byte anyoStatusCode = AnyoMessage.STATUS_CODE_INTERNAL_ERROR;
                FIN_MODE finMode = ((FinDirective) cap.getData()).getFin_mode();
                anyoStatusCode = (FIN_MODE.refuse.equals(finMode) || FIN_MODE.busy.equals(finMode) || FIN_MODE.cancel.equals(finMode) || FIN_MODE.plugin_timeout.equals(finMode) || FIN_MODE.port_forbiden.equals(finMode) || FIN_MODE.no_feerate.equals(finMode) || FIN_MODE.car.equals(finMode)) ? (byte) 19 : (byte) 19;
                AnyoProtocolAgent.getInstance().sendUnlockPortResponse((UnlockPortRequest) dcapAdapterSession.anyoMsg, anyoStatusCode);
                Log.i("DCAPAdapter.handleFinIndicate", "destroy an DCAP adapter session: " + dcapAdapterSession.toJson());
                this.dcapAdapterSessions.remove(nackInitSeq);
            } else {
                Log.w("DCAPAdapter.handleFinIndicate", "not matched DCAP adapter session: " + dcapAdapterSession.toJson() + " and indicate: " + indicate.toJson());
            }
        }
    }

    public void handleStartComfirm(DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) confirm.getData();
        CAPDirectiveOption opt = cap.getOpt();
        String peerSeq = String.valueOf(opt.getSeq());
        DCAPAdapterSession dcapAdapterSession = this.dcapAdapterSessions.get(peerSeq);
        if (dcapAdapterSession == null) {
            Log.w("DCAPAdapter.handleStartComfirm", "not found DCAP adapter session for confirm: " + confirm.toJson());
            return;
        }
        String op = cap.getOp();
        if ("ack".equals(op)) {
            AnyoProtocolAgent.getInstance().sendStartChargeResponse((StartChargeRequest) dcapAdapterSession.anyoMsg, (byte) 0);
            Log.i("DCAPAdapter.handleStartComfirm", "destroy an DCAP adapter session: " + dcapAdapterSession.toJson());
            this.dcapAdapterSessions.remove(peerSeq);
        } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
            AnyoProtocolAgent.getInstance().sendStartChargeResponse((StartChargeRequest) dcapAdapterSession.anyoMsg, AnyoMessage.STATUS_CODE_INTERNAL_ERROR);
            Log.i("DCAPAdapter.handleStartComfirm", "destroy an DCAP adapter session: " + dcapAdapterSession.toJson());
            this.dcapAdapterSessions.remove(peerSeq);
        }
    }

    public void handleStopConfirm(DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) confirm.getData();
        CAPDirectiveOption opt = cap.getOpt();
        String peerSeq = String.valueOf(opt.getSeq());
        DCAPAdapterSession dcapAdapterSession = this.dcapAdapterSessions.get(peerSeq);
        if (dcapAdapterSession == null) {
            Log.w("DCAPAdapter.handleStopComfirm", "not found DCAP adapter session for confirm: " + confirm.toJson());
            return;
        }
        String op = cap.getOp();
        if ("ack".equals(op)) {
            AnyoProtocolAgent.getInstance().sendStopChargeResponse((StopChargeRequest) dcapAdapterSession.anyoMsg, (byte) 0);
            Log.i("DCAPAdapter.handleStopComfirm", "destroy an DCAP adapter session: " + dcapAdapterSession.toJson());
            this.dcapAdapterSessions.remove(peerSeq);
        } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
            AnyoProtocolAgent.getInstance().sendStopChargeResponse((StopChargeRequest) dcapAdapterSession.anyoMsg, AnyoMessage.STATUS_CODE_INTERNAL_ERROR);
            Log.i("DCAPAdapter.handleStopComfirm", "destroy an DCAP adapter session: " + dcapAdapterSession.toJson());
            this.dcapAdapterSessions.remove(peerSeq);
        }
    }

    private DCAPMessage createRequest(String from, String op, CAPDirectiveOption opt, Object directive) {
        CAPMessage requestCap = new CAPMessage();
        DCAPMessage request = new DCAPMessage();
        request.setFrom(from);
        request.setTo("device:sn/" + this.sn);
        request.setType("cap");
        request.setCtime(System.currentTimeMillis());
        request.setSeq(Sequence.getAgentDCAPSequence());
        requestCap.setOp(op);
        requestCap.setOpt(opt);
        requestCap.setData(directive);
        request.setData(requestCap);
        return request;
    }

    private DCAPMessage finChargeByRemote(String chargeId) {
        ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(chargeId);
        if (chargeBill != null) {
            String userType = chargeBill.getUser_type();
            String userCode = chargeBill.getUser_code();
            if ((chargeBill.getStart_time() > 0 && chargeBill.getStop_time() == 0) || (chargeBill.getStop_time() > 0 && chargeBill.getFin_time() == 0)) {
                CAPDirectiveOption opt = new CAPDirectiveOption();
                opt.setCharge_id(chargeId);
                FinDirective fin = new FinDirective();
                fin.setFin_mode(FIN_MODE.remote);
                String from = "user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode;
                DCAPMessage request = createRequest(from, "fin", opt, fin);
                if (DCAPProxy.getInstance().sendRequest(request)) {
                    return request;
                }
            } else {
                Log.w("DCAPAdapter.finChargeByRemote", "charge: " + chargeId + " is not in CHARGING state, refuse to finish by remote");
            }
        } else {
            Log.w("DCAPAdapter.finChargeByRemote", "charge: " + chargeId + " is not exist, refuse to finish by remote");
        }
        return null;
    }

    private void finRequest(FIN_MODE finMode, DCAPMessage init) {
        CAPMessage initCap = (CAPMessage) init.getData();
        InitDirective initDirective = (InitDirective) initCap.getData();
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(initCap.getOpt().getCharge_id());
        FinDirective fin = new FinDirective();
        fin.setFin_mode(finMode);
        String from = "user:" + initDirective.getUser_type() + MqttTopic.TOPIC_LEVEL_SEPARATOR + initDirective.getUser_code();
        DCAPMessage request = createRequest(from, "fin", opt, fin);
        DCAPProxy.getInstance().sendRequest(request);
    }

    private boolean ackResponse(DCAPMessage indicate) {
        AckDirective ack = new AckDirective();
        return DCAPProxy.getInstance().sendResponse(DCAPProxy.getInstance().createCAPResponseByIndcate(indicate), "cap", "ack", ack);
    }
}