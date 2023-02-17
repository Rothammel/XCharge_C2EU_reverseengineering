package com.xcharge.charger.protocol.anyo.router;

import android.content.Context;
import android.util.Log;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.api.bean.AckDirective;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.NackDirective;
import com.xcharge.charger.core.api.bean.cap.AuthDirective;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.anyo.bean.response.AuthResponse;
import com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent;
import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/* loaded from: classes.dex */
public class AnyoAdapter {
    public static final long DEFAULT_SESSION_TIMEOUT = 30000;
    private Context context = null;
    private HashMap<String, AnyoAdapterSession> anyoAdapterSessions = null;

    /* loaded from: classes.dex */
    private static class AnyoAdapterSession extends JsonBean<AnyoAdapterSession> {
        AnyoMessage anyoMsg;
        DCAPMessage dcapMsg;
        long dcapTimestamp;
        String directiveType;

        private AnyoAdapterSession() {
            this.dcapMsg = null;
            this.directiveType = null;
            this.anyoMsg = null;
            this.dcapTimestamp = 0L;
        }

        /* synthetic */ AnyoAdapterSession(AnyoAdapterSession anyoAdapterSession) {
            this();
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

        public AnyoMessage getAnyoMsg() {
            return this.anyoMsg;
        }

        public void setAnyoMsg(AnyoMessage anyoMsg) {
            this.anyoMsg = anyoMsg;
        }

        public long getDcapTimestamp() {
            return this.dcapTimestamp;
        }

        public void setDcapTimestamp(long dcapTimestamp) {
            this.dcapTimestamp = dcapTimestamp;
        }
    }

    public void init(Context context) {
        this.context = context;
        this.anyoAdapterSessions = new HashMap<>();
    }

    public void destroy() {
        this.anyoAdapterSessions.clear();
    }

    public void maintainSession() {
        if (this.anyoAdapterSessions.size() != 0) {
            long nowTimestamp = System.currentTimeMillis();
            Iterator<Map.Entry<String, AnyoAdapterSession>> it2 = this.anyoAdapterSessions.entrySet().iterator();
            while (it2.hasNext()) {
                AnyoAdapterSession session = it2.next().getValue();
                if (nowTimestamp - session.dcapTimestamp > 30000) {
                    Log.w("AnyoAdapter.maintainSession", "destroy an timeout anyo adapter session: " + session.toJson());
                    nackResponse(session.dcapMsg);
                    it2.remove();
                }
            }
        }
    }

    public void handleFailedAnyoRequest(AnyoMessage request) {
        String anyoSeq = String.valueOf(request.getHead().getSeq() & 255);
        AnyoAdapterSession session = this.anyoAdapterSessions.get(anyoSeq);
        if (session != null) {
            Log.w("AnyoAdapter.handleFailedAnyoRequest", "anyo request failed, destroy related anyo adapter session: " + session.toJson());
            nackResponse(session.dcapMsg);
            this.anyoAdapterSessions.remove(anyoSeq);
        }
    }

    public void handleAuthIndicate(DCAPMessage indicate) {
        CAPMessage cap = (CAPMessage) indicate.getData();
        AuthDirective auth = (AuthDirective) cap.getData();
        String localPort = auth.getPort();
        int anyoPort = Integer.parseInt(localPort) + 9;
        String cardNo = auth.getUser_code();
        AnyoMessage anyoAuthRequest = AnyoProtocolAgent.getInstance().sendAuthRequest(anyoPort, 0, cardNo);
        if (anyoAuthRequest != null) {
            AnyoAdapterSession anyoAdapterSession = new AnyoAdapterSession(null);
            anyoAdapterSession.dcapMsg = indicate;
            anyoAdapterSession.directiveType = "auth";
            anyoAdapterSession.dcapTimestamp = System.currentTimeMillis();
            anyoAdapterSession.anyoMsg = anyoAuthRequest;
            Log.i("AnyoAdapter.handleAuthIndicate", "create an anyo adapter session: " + anyoAdapterSession.toJson());
            this.anyoAdapterSessions.put(String.valueOf(anyoAuthRequest.getHead().getSeq() & 255), anyoAdapterSession);
            return;
        }
        Log.w("AnyoAdapter.handleAuthIndicate", "failed to send auth request for indicate: " + indicate.toJson());
        DCAPMessage dcapAuthResponse = DCAPProxy.getInstance().createCAPResponseByIndcate(indicate);
        NackDirective nack = new NackDirective();
        nack.setError(ErrorCode.EC_INTERNAL_ERROR);
        DCAPProxy.getInstance().sendResponse(dcapAuthResponse, "cap", CAPMessage.DIRECTIVE_NACK, nack);
    }

    public void handleAuthResponse(AuthResponse response) {
        String anyoSeq = String.valueOf(response.getHead().getSeq() & 255);
        AnyoAdapterSession anyoAdapterSession = this.anyoAdapterSessions.get(anyoSeq);
        if (anyoAdapterSession == null) {
            Log.w("AnyoAdapter.handleAuthResponse", "related anyo adapter session is not exist !!! anyo sequence: " + anyoSeq);
            return;
        }
        DCAPMessage dcapAuthResponse = DCAPProxy.getInstance().createCAPResponseByIndcate(anyoAdapterSession.dcapMsg);
        if (response.getHead().getStatusCode() == 0) {
            HashMap<String, Object> attach = new HashMap<>();
            attach.put("user_id", String.valueOf(response.getUserId()));
            attach.put("bill_id", String.valueOf(response.getBillId()));
            attach.put(ContentDB.NFCConsumeFailCacheTable.BALANCE, String.valueOf(response.getBalance()));
            AckDirective ack = new AckDirective();
            ack.setAttach(attach);
            DCAPProxy.getInstance().sendResponse(dcapAuthResponse, "cap", "ack", ack);
        } else {
            NackDirective nack = new NackDirective();
            nack.setError(ErrorCode.EC_CLOUD_AUTH_REFUSED);
            DCAPProxy.getInstance().sendResponse(dcapAuthResponse, "cap", CAPMessage.DIRECTIVE_NACK, nack);
        }
        Log.i("AnyoAdapter.handleAuthResponse", "destroy an anyo adapter session: " + anyoAdapterSession.toJson());
        this.anyoAdapterSessions.remove(anyoSeq);
    }

    private boolean nackResponse(DCAPMessage indicate) {
        NackDirective nack = new NackDirective();
        return DCAPProxy.getInstance().sendResponse(DCAPProxy.getInstance().createCAPResponseByIndcate(indicate), "cap", CAPMessage.DIRECTIVE_NACK, nack);
    }
}