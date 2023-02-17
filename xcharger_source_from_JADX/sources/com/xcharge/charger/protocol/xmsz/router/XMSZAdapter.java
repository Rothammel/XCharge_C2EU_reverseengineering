package com.xcharge.charger.protocol.xmsz.router;

import android.content.Context;
import android.util.Log;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.NackDirective;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.charger.protocol.xmsz.bean.cloud.AuthorizeIDResponse;
import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class XMSZAdapter {
    public static final long DEFAULT_SESSION_TIMEOUT = 30000;
    private Context context = null;
    private HashMap<String, XMSZAdapterSession> xmszAdapterSessions = null;

    private static class XMSZAdapterSession extends JsonBean<XMSZAdapterSession> {
        DCAPMessage dcapMsg = null;
        long dcapTimestamp = 0;
        String directiveType = null;
        XMSZMessage xmszMsg = null;

        private XMSZAdapterSession() {
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

        public XMSZMessage getXmszMsg() {
            return this.xmszMsg;
        }

        public void setXmszMsg(XMSZMessage xmszMsg2) {
            this.xmszMsg = xmszMsg2;
        }

        public long getDcapTimestamp() {
            return this.dcapTimestamp;
        }

        public void setDcapTimestamp(long dcapTimestamp2) {
            this.dcapTimestamp = dcapTimestamp2;
        }
    }

    public void init(Context context2) {
        this.context = context2;
        this.xmszAdapterSessions = new HashMap<>();
    }

    public void destroy() {
        this.xmszAdapterSessions.clear();
    }

    public void maintainSession() {
        if (this.xmszAdapterSessions.size() != 0) {
            long nowTimestamp = System.currentTimeMillis();
            Iterator<Map.Entry<String, XMSZAdapterSession>> it = this.xmszAdapterSessions.entrySet().iterator();
            while (it.hasNext()) {
                XMSZAdapterSession session = (XMSZAdapterSession) it.next().getValue();
                if (nowTimestamp - session.dcapTimestamp > 30000) {
                    Log.w("XMSZAdapter.maintainSession", "destroy an timeout xmsz adapter session: " + session.toJson());
                    nackResponse(session.dcapMsg);
                    it.remove();
                }
            }
        }
    }

    public void handleFailedXMSZRequest(XMSZMessage request) {
        String anyoSeq = String.valueOf(request.getHead().getPacketID() & 255);
        XMSZAdapterSession session = this.xmszAdapterSessions.get(anyoSeq);
        if (session != null) {
            Log.w("XMSZAdapter.handleFailedAnyoRequest", "xmsz request failed, destroy related xmsz adapter session: " + session.toJson());
            nackResponse(session.dcapMsg);
            this.xmszAdapterSessions.remove(anyoSeq);
        }
    }

    public void handleAuthIndicate(DCAPMessage indicate) {
    }

    public void handleAuthorizeIDResponse(AuthorizeIDResponse response) {
    }

    private boolean nackResponse(DCAPMessage indicate) {
        return DCAPProxy.getInstance().sendResponse(DCAPProxy.getInstance().createCAPResponseByIndcate(indicate), "cap", CAPMessage.DIRECTIVE_NACK, new NackDirective());
    }
}
