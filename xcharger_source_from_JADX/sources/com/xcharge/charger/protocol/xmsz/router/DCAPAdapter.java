package com.xcharge.charger.protocol.xmsz.router;

import android.content.Context;
import android.util.Log;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.api.Sequence;
import com.xcharge.charger.core.api.bean.AckDirective;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.cap.CAPDirectiveOption;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.core.api.bean.cap.FinDirective;
import com.xcharge.charger.core.api.bean.cap.InitDirective;
import com.xcharge.charger.core.api.bean.cap.StopDirective;
import com.xcharge.charger.core.type.FIN_MODE;
import com.xcharge.charger.data.bean.type.CHARGE_INIT_TYPE;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.CHARGE_USER_TYPE;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.charger.protocol.xmsz.bean.cloud.RemoteStartChargingRequest;
import com.xcharge.charger.protocol.xmsz.bean.cloud.RemoteStopChargingRequest;
import com.xcharge.charger.protocol.xmsz.handler.XMSZProtocolAgent;
import com.xcharge.charger.protocol.xmsz.session.XMSZChargeSession;
import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.paho.client.mqttv3.MqttTopic;

public class DCAPAdapter {
    public static final long DEFAULT_SESSION_TIMEOUT = 10000;
    private Context context = null;
    private HashMap<String, DCAPAdapterSession> dcapAdapterSessions = null;

    /* renamed from: sn */
    private String f129sn = null;

    private static class DCAPAdapterSession extends JsonBean<DCAPAdapterSession> {
        DCAPMessage dcapMsg;
        String directiveType;
        XMSZMessage xmszMsg;
        long xmszTimestamp;

        private DCAPAdapterSession() {
            this.xmszMsg = null;
            this.dcapMsg = null;
            this.directiveType = null;
            this.xmszTimestamp = 0;
        }

        /* synthetic */ DCAPAdapterSession(DCAPAdapterSession dCAPAdapterSession) {
            this();
        }

        public XMSZMessage getXmszMsg() {
            return this.xmszMsg;
        }

        public void setXmszMsg(XMSZMessage xmszMsg2) {
            this.xmszMsg = xmszMsg2;
        }

        public DCAPMessage getDcapMsg() {
            return this.dcapMsg;
        }

        public void setDcapMsg(DCAPMessage dcapMsg2) {
            this.dcapMsg = dcapMsg2;
        }

        public String getDirectiveType() {
            return this.directiveType;
        }

        public void setDirectiveType(String directiveType2) {
            this.directiveType = directiveType2;
        }

        public long getXmszTimestamp() {
            return this.xmszTimestamp;
        }

        public void setXmszTimestamp(long xmszTimestamp2) {
            this.xmszTimestamp = xmszTimestamp2;
        }
    }

    public void init(Context context2) {
        this.context = context2;
        this.f129sn = HardwareStatusCacheProvider.getInstance().getSn();
        this.dcapAdapterSessions = new HashMap<>();
    }

    public void destroy() {
        this.dcapAdapterSessions.clear();
    }

    public void maintainSession() {
        if (this.dcapAdapterSessions.size() != 0) {
            long nowTimestamp = System.currentTimeMillis();
            Iterator<Map.Entry<String, DCAPAdapterSession>> it = this.dcapAdapterSessions.entrySet().iterator();
            while (it.hasNext()) {
                DCAPAdapterSession session = (DCAPAdapterSession) it.next().getValue();
                if (nowTimestamp - session.xmszTimestamp > 10000) {
                    Log.w("DCAPAdapter.maintainSession", "destroy an timeout DCAP adapter session: " + session.toJson());
                    XMSZMessage request = session.xmszMsg;
                    switch (request.getHead().getFunctionCode()) {
                        case 2:
                            finRequest(FIN_MODE.timeout, session.dcapMsg);
                            XMSZProtocolAgent.getInstance().sendRemoteStartChargingResponse((RemoteStartChargingRequest) request, (byte) 0);
                            break;
                        case 3:
                            XMSZProtocolAgent.getInstance().sendRemoteStopChargingResponse((RemoteStopChargingRequest) request, (byte) 0);
                            break;
                    }
                    it.remove();
                }
            }
        }
    }

    public void handleRemoteStartChargingRequest(XMSZMessage request) {
        RemoteStartChargingRequest remoteStartChargingRequest = (RemoteStartChargingRequest) request;
        String chargeId = String.valueOf(System.currentTimeMillis() / 1000);
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(chargeId);
        String userType = CHARGE_USER_TYPE.xmsz.getUserType();
        String userCode = String.valueOf(remoteStartChargingRequest.getUserIdTag());
        InitDirective init = new InitDirective();
        init.setInit_type(CHARGE_INIT_TYPE.native_qrcode);
        init.setUser_type(userType);
        init.setUser_code(userCode);
        init.setDevice_id("sn/" + this.f129sn);
        init.setPort(String.valueOf(remoteStartChargingRequest.getConnectorId() & 255));
        init.setCharge_platform(CHARGE_PLATFORM.xmsz);
        init.setTimeout_plugin(60);
        init.setTimeout_start(-1);
        init.setTimeout_plugout(-1);
        DCAPMessage initRequest = createRequest("user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode, "init", opt, init);
        DCAPProxy.getInstance().sendRequest(initRequest);
        DCAPAdapterSession dcapAdapterSession = new DCAPAdapterSession((DCAPAdapterSession) null);
        dcapAdapterSession.xmszMsg = remoteStartChargingRequest;
        dcapAdapterSession.xmszTimestamp = System.currentTimeMillis();
        dcapAdapterSession.dcapMsg = initRequest;
        dcapAdapterSession.directiveType = "init";
        Log.i("DCAPAdapter.handleRemoteStartChargingRequest", "create an DCAP adapter session: " + dcapAdapterSession.toJson());
        this.dcapAdapterSessions.put(String.valueOf(initRequest.getSeq()), dcapAdapterSession);
    }

    public void handleRemoteStopChargingRequest(XMSZMessage request) {
        RemoteStopChargingRequest remoteStopChargingRequest = (RemoteStopChargingRequest) request;
        XMSZChargeSession chargeSession = XMSZProtocolAgent.getInstance().getChargeSessionByChargeId(String.valueOf(remoteStopChargingRequest.getTransactionId()));
        if (chargeSession == null) {
            Log.w("DCAPAdapter.handleRemoteStopChargingRequest", "not found charge session for charge: " + remoteStopChargingRequest.getTransactionId());
            XMSZProtocolAgent.getInstance().sendRemoteStopChargingResponse(remoteStopChargingRequest, (byte) 0);
            return;
        }
        request.setPort(chargeSession.getPort());
        Log.i("DCAPAdapter.handleRemoteStopChargingRequest", "found charge session: " + chargeSession.toJson());
        String chargeId = chargeSession.getCharge_id();
        String userType = chargeSession.getUser_type();
        String userCode = chargeSession.getUser_code();
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(chargeId);
        DCAPMessage stopRequest = createRequest("user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode, "stop", opt, new StopDirective());
        DCAPProxy.getInstance().sendRequest(stopRequest);
        DCAPAdapterSession dcapAdapterSession = new DCAPAdapterSession((DCAPAdapterSession) null);
        dcapAdapterSession.xmszMsg = remoteStopChargingRequest;
        dcapAdapterSession.xmszTimestamp = System.currentTimeMillis();
        dcapAdapterSession.dcapMsg = stopRequest;
        dcapAdapterSession.directiveType = "stop";
        Log.i("DCAPAdapter.handleRemoteStopChargingRequest", "create an DCAP adapter session: " + dcapAdapterSession.toJson());
        this.dcapAdapterSessions.put(String.valueOf(stopRequest.getSeq()), dcapAdapterSession);
    }

    public void handleInitAckIndicate(DCAPMessage indicate) {
        String ackInitSeq = String.valueOf(((CAPMessage) indicate.getData()).getOpt().getSeq());
        DCAPAdapterSession dcapAdapterSession = this.dcapAdapterSessions.get(ackInitSeq);
        if (dcapAdapterSession == null) {
            Log.w("DCAPAdapter.handleInitAckIndicate", "not found DCAP adapter session for indicate: " + indicate.toJson());
        } else if (!dcapAdapterSession.directiveType.equals("init") || dcapAdapterSession.xmszMsg.getHead().getFunctionCode() != 2) {
            Log.w("DCAPAdapter.handleInitAckIndicate", "not matched DCAP adapter session: " + dcapAdapterSession.toJson() + " and indicate: " + indicate.toJson());
        } else {
            ackResponse(indicate);
            XMSZProtocolAgent.getInstance().sendRemoteStartChargingResponse((RemoteStartChargingRequest) dcapAdapterSession.xmszMsg, (byte) 1);
            Log.i("DCAPAdapter.handleInitAckIndicate", "destroy an DCAP adapter session: " + dcapAdapterSession.toJson());
            this.dcapAdapterSessions.remove(ackInitSeq);
        }
    }

    public void handleFinIndicate(DCAPMessage indicate) {
        ackResponse(indicate);
        CAPDirectiveOption opt = ((CAPMessage) indicate.getData()).getOpt();
        if ("init".equals(opt.getOp())) {
            String nackInitSeq = String.valueOf(opt.getSeq());
            DCAPAdapterSession dcapAdapterSession = this.dcapAdapterSessions.get(nackInitSeq);
            if (dcapAdapterSession == null) {
                Log.w("DCAPAdapter.handleFinIndicate", "not found DCAP adapter session for indicate: " + indicate.toJson());
            } else if (!dcapAdapterSession.directiveType.equals("init") || dcapAdapterSession.xmszMsg.getHead().getFunctionCode() != 2) {
                Log.w("DCAPAdapter.handleFinIndicate", "not matched DCAP adapter session: " + dcapAdapterSession.toJson() + " and indicate: " + indicate.toJson());
            } else {
                XMSZProtocolAgent.getInstance().sendRemoteStartChargingResponse((RemoteStartChargingRequest) dcapAdapterSession.xmszMsg, (byte) 0);
                Log.i("DCAPAdapter.handleFinIndicate", "destroy an DCAP adapter session: " + dcapAdapterSession.toJson());
                this.dcapAdapterSessions.remove(nackInitSeq);
            }
        }
    }

    public void handleStopConfirm(DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) confirm.getData();
        String peerSeq = String.valueOf(cap.getOpt().getSeq());
        DCAPAdapterSession dcapAdapterSession = this.dcapAdapterSessions.get(peerSeq);
        if (dcapAdapterSession == null) {
            Log.w("DCAPAdapter.handleStopComfirm", "not found DCAP adapter session for confirm: " + confirm.toJson());
            return;
        }
        String op = cap.getOp();
        if ("ack".equals(op)) {
            XMSZProtocolAgent.getInstance().sendRemoteStopChargingResponse((RemoteStopChargingRequest) dcapAdapterSession.xmszMsg, (byte) 1);
            Log.i("DCAPAdapter.handleStopComfirm", "destroy an DCAP adapter session: " + dcapAdapterSession.toJson());
            this.dcapAdapterSessions.remove(peerSeq);
        } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
            XMSZProtocolAgent.getInstance().sendRemoteStopChargingResponse((RemoteStopChargingRequest) dcapAdapterSession.xmszMsg, (byte) 0);
            Log.i("DCAPAdapter.handleStopComfirm", "destroy an DCAP adapter session: " + dcapAdapterSession.toJson());
            this.dcapAdapterSessions.remove(peerSeq);
        }
    }

    private DCAPMessage createRequest(String from, String op, CAPDirectiveOption opt, Object directive) {
        CAPMessage requestCap = new CAPMessage();
        DCAPMessage request = new DCAPMessage();
        request.setFrom(from);
        request.setTo("device:sn/" + this.f129sn);
        request.setType("cap");
        request.setCtime(System.currentTimeMillis());
        request.setSeq(Sequence.getAgentDCAPSequence());
        requestCap.setOp(op);
        requestCap.setOpt(opt);
        requestCap.setData(directive);
        request.setData(requestCap);
        return request;
    }

    private void finRequest(FIN_MODE finMode, DCAPMessage init) {
        CAPMessage initCap = (CAPMessage) init.getData();
        InitDirective initDirective = (InitDirective) initCap.getData();
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(initCap.getOpt().getCharge_id());
        FinDirective fin = new FinDirective();
        fin.setFin_mode(finMode);
        DCAPProxy.getInstance().sendRequest(createRequest("user:" + initDirective.getUser_type() + MqttTopic.TOPIC_LEVEL_SEPARATOR + initDirective.getUser_code(), "fin", opt, fin));
    }

    private boolean ackResponse(DCAPMessage indicate) {
        return DCAPProxy.getInstance().sendResponse(DCAPProxy.getInstance().createCAPResponseByIndcate(indicate), "cap", "ack", new AckDirective());
    }
}
