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

/* loaded from: classes.dex */
public class XMSZAdapter {
    public static final long DEFAULT_SESSION_TIMEOUT = 30000;
    private Context context = null;
    private HashMap<String, XMSZAdapterSession> xmszAdapterSessions = null;

    /* loaded from: classes.dex */
    private static class XMSZAdapterSession extends JsonBean<XMSZAdapterSession> {
        DCAPMessage dcapMsg = null;
        String directiveType = null;
        XMSZMessage xmszMsg = null;
        long dcapTimestamp = 0;

        private XMSZAdapterSession() {
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

        public XMSZMessage getXmszMsg() {
            return this.xmszMsg;
        }

        public void setXmszMsg(XMSZMessage xmszMsg) {
            this.xmszMsg = xmszMsg;
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
        this.xmszAdapterSessions = new HashMap<>();
    }

    public void destroy() {
        this.xmszAdapterSessions.clear();
    }

    public void maintainSession() {
        if (this.xmszAdapterSessions.size() != 0) {
            long nowTimestamp = System.currentTimeMillis();
            Iterator<Map.Entry<String, XMSZAdapterSession>> it2 = this.xmszAdapterSessions.entrySet().iterator();
            while (it2.hasNext()) {
                XMSZAdapterSession session = it2.next().getValue();
                if (nowTimestamp - session.dcapTimestamp > 30000) {
                    Log.w("XMSZAdapter.maintainSession", "destroy an timeout xmsz adapter session: " + session.toJson());
                    nackResponse(session.dcapMsg);
                    it2.remove();
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
        NackDirective nack = new NackDirective();
        return DCAPProxy.getInstance().sendResponse(DCAPProxy.getInstance().createCAPResponseByIndcate(indicate), "cap", CAPMessage.DIRECTIVE_NACK, nack);
    }
}