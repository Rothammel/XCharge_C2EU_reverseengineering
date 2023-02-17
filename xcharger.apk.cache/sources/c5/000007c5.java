package com.xcharge.charger.protocol.ocpp.router;

import android.content.Context;
import android.util.Log;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.api.bean.AckDirective;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.NackDirective;
import com.xcharge.charger.core.api.bean.cap.AuthDirective;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.core.api.bean.cap.QueryDirective;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.charger.protocol.ocpp.bean.cloud.AuthorizeConf;
import com.xcharge.charger.protocol.ocpp.handler.OcppProtocolAgent;
import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;

/* loaded from: classes.dex */
public class OcppAdapter {
    public static final long DEFAULT_SESSION_TIMEOUT = 30000;
    private Context context = null;
    private HashMap<String, OcppAdapterSession> ocppAdapterSessions = null;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class OcppAdapterSession extends JsonBean<OcppAdapterSession> {
        DCAPMessage dcapMsg;
        long dcapTimestamp;
        String directiveType;
        JSONArray ocppMsg;

        private OcppAdapterSession() {
            this.dcapMsg = null;
            this.directiveType = null;
            this.ocppMsg = null;
            this.dcapTimestamp = 0L;
        }

        /* synthetic */ OcppAdapterSession(OcppAdapterSession ocppAdapterSession) {
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

        public JSONArray getOcppMsg() {
            return this.ocppMsg;
        }

        public void setOcppMsg(JSONArray ocppMsg) {
            this.ocppMsg = ocppMsg;
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
        this.ocppAdapterSessions = new HashMap<>();
    }

    public void destroy() {
        this.ocppAdapterSessions.clear();
    }

    public void maintainSession() {
        if (this.ocppAdapterSessions.size() != 0) {
            long nowTimestamp = System.currentTimeMillis();
            Iterator<Map.Entry<String, OcppAdapterSession>> it2 = this.ocppAdapterSessions.entrySet().iterator();
            while (it2.hasNext()) {
                OcppAdapterSession session = it2.next().getValue();
                if (nowTimestamp - session.dcapTimestamp > 30000) {
                    Log.w("OcppAdapter.maintainSession", "destroy an timeout ocpp adapter session: " + session.toJson());
                    nackResponse(session.dcapMsg);
                    it2.remove();
                }
            }
        }
    }

    public void handleFailedOcppRequest(JSONArray request) {
        try {
            String uid = request.getString(1);
            OcppAdapterSession session = this.ocppAdapterSessions.get(request.get(1));
            if (session != null) {
                Log.w("OcppAdapter.handleFailedOcppRequest", "ocpp request failed, destroy related ocpp adapter session: " + session.toJson());
                nackResponse(session.dcapMsg);
                this.ocppAdapterSessions.remove(uid);
            }
        } catch (Exception e) {
            Log.w("OcppAdapter.handleFailedOcppRequest", Log.getStackTraceString(e));
        }
    }

    public void handleAuthIndicate(DCAPMessage indicate) {
        try {
            CAPMessage cap = (CAPMessage) indicate.getData();
            AuthDirective auth = (AuthDirective) cap.getData();
            String cardNo = auth.getUser_code();
            JSONArray jsonArray = OcppProtocolAgent.getInstance().authorizeReq(cardNo, true);
            if (jsonArray != null) {
                OcppAdapterSession ocppAdapterSession = new OcppAdapterSession(null);
                ocppAdapterSession.dcapMsg = indicate;
                ocppAdapterSession.directiveType = "auth";
                ocppAdapterSession.dcapTimestamp = System.currentTimeMillis();
                ocppAdapterSession.ocppMsg = jsonArray;
                Log.i("OcppAdapter.handleAuthIndicate", "create an ocpp adapter session: " + ocppAdapterSession.toJson());
                this.ocppAdapterSessions.put(jsonArray.getString(1), ocppAdapterSession);
                if (jsonArray.length() > 4) {
                    OcppProtocolAgent.getInstance().sendLocalAuthorizeConf(jsonArray);
                }
            } else {
                Log.w("OcppAdapter.handleAuthIndicate", "failed to send auth request for indicate: " + indicate.toJson());
                DCAPMessage dcapAuthResponse = DCAPProxy.getInstance().createCAPResponseByIndcate(indicate);
                NackDirective nack = new NackDirective();
                nack.setError(ErrorCode.EC_INTERNAL_ERROR);
                DCAPProxy.getInstance().sendResponse(dcapAuthResponse, "cap", CAPMessage.DIRECTIVE_NACK, nack);
            }
        } catch (Exception e) {
            Log.w("OcppAdapter.handleAuthIndicate", Log.getStackTraceString(e));
        }
    }

    public void handleQueryIndicate(DCAPMessage indicate) {
        try {
            CAPMessage cap = (CAPMessage) indicate.getData();
            QueryDirective query = (QueryDirective) cap.getData();
            HashMap<String, Object> params = query.getParams();
            String cardNo = (String) params.get("cardNo");
            JSONArray jsonArray = OcppProtocolAgent.getInstance().authorizeReq(cardNo, false);
            if (jsonArray != null) {
                OcppAdapterSession ocppAdapterSession = new OcppAdapterSession(null);
                ocppAdapterSession.dcapMsg = indicate;
                ocppAdapterSession.directiveType = "query";
                ocppAdapterSession.dcapTimestamp = System.currentTimeMillis();
                ocppAdapterSession.ocppMsg = jsonArray;
                Log.i("OcppAdapter.handleQueryIndicate", "create an ocpp adapter session: " + ocppAdapterSession.toJson());
                this.ocppAdapterSessions.put(jsonArray.getString(1), ocppAdapterSession);
                if (jsonArray.length() > 4) {
                    OcppProtocolAgent.getInstance().sendLocalAuthorizeConf(jsonArray);
                }
            } else {
                Log.w("OcppAdapter.handleQueryIndicate", "failed to send auth request for indicate: " + indicate.toJson());
                DCAPMessage dcapAuthResponse = DCAPProxy.getInstance().createCAPResponseByIndcate(indicate);
                NackDirective nack = new NackDirective();
                nack.setError(ErrorCode.EC_INTERNAL_ERROR);
                DCAPProxy.getInstance().sendResponse(dcapAuthResponse, "cap", CAPMessage.DIRECTIVE_NACK, nack);
            }
        } catch (Exception e) {
            Log.w("OcppAdapter.handleQueryIndicate", Log.getStackTraceString(e));
        }
    }

    public void handleAuthResponse(JSONArray response) {
        try {
            OcppAdapterSession ocppAdapterSession = this.ocppAdapterSessions.get(response.get(1));
            if (ocppAdapterSession == null) {
                Log.w("OcppAdapter.handleAuthResponse", "related ocpp adapter session is not exist !!! ocpp sequence: " + response.get(1));
                return;
            }
            DCAPMessage dcapAuthResponse = DCAPProxy.getInstance().createCAPResponseByIndcate(ocppAdapterSession.dcapMsg);
            AuthorizeConf authorizeConf = new AuthorizeConf().fromJson(response.getJSONObject(2).toString());
            if ("Accepted".equals(authorizeConf.getIdTagInfo().getStatus())) {
                AckDirective ackDirective = new AckDirective();
                if ("query".equals(ocppAdapterSession.getDirectiveType())) {
                    HashMap<String, Object> attach = new HashMap<>();
                    attach.put(ContentDB.AuthInfoTable.STATUS, "Accepted");
                    ackDirective.setAttach(attach);
                }
                DCAPProxy.getInstance().sendResponse(dcapAuthResponse, "cap", "ack", ackDirective);
            } else {
                NackDirective nack = new NackDirective();
                nack.setError(ErrorCode.EC_CLOUD_AUTH_REFUSED);
                DCAPProxy.getInstance().sendResponse(dcapAuthResponse, "cap", CAPMessage.DIRECTIVE_NACK, nack);
            }
            Log.i("OcppAdapter.handleAuthResponse", "destroy an ocpp adapter session: " + ocppAdapterSession.toJson());
            this.ocppAdapterSessions.remove(response.get(1));
        } catch (Exception e) {
            Log.w("OcppAdapter.handleAuthResponse", Log.getStackTraceString(e));
        }
    }

    private boolean nackResponse(DCAPMessage indicate) {
        NackDirective nack = new NackDirective();
        return DCAPProxy.getInstance().sendResponse(DCAPProxy.getInstance().createCAPResponseByIndcate(indicate), "cap", CAPMessage.DIRECTIVE_NACK, nack);
    }
}