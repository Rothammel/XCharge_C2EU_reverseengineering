package com.xcharge.charger.protocol.family.xcloud.router;

import android.content.Context;
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
import com.xcharge.charger.core.type.CHARGE_REFUSE_CAUSE;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.type.USER_TC_TYPE;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.protocol.family.xcloud.bean.ChargeStopCondition;
import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestRefuseCharge;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestStartCharge;
import com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent;
import com.xcharge.charger.protocol.family.xcloud.session.XCloudRequestSession;
import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class XCloudAdapter {
    public static final long DEFAULT_SESSION_TIMEOUT = 30000;
    private Context context = null;
    private String sn = null;
    private HashMap<String, XCloudAdapterSession> xcloudAdapterSessions = null;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class XCloudAdapterSession extends JsonBean<XCloudAdapterSession> {
        DCAPMessage dcapMsg;
        long dcapTimestamp;
        String directiveType;
        XCloudMessage xcloudMsg;

        private XCloudAdapterSession() {
            this.dcapMsg = null;
            this.directiveType = null;
            this.xcloudMsg = null;
            this.dcapTimestamp = 0L;
        }

        /* synthetic */ XCloudAdapterSession(XCloudAdapterSession xCloudAdapterSession) {
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

        public XCloudMessage getXcloudMsg() {
            return this.xcloudMsg;
        }

        public void setXcloudMsg(XCloudMessage xcloudMsg) {
            this.xcloudMsg = xcloudMsg;
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
        this.sn = HardwareStatusCacheProvider.getInstance().getSn();
        this.xcloudAdapterSessions = new HashMap<>();
    }

    public void destroy() {
        this.xcloudAdapterSessions.clear();
    }

    public void handleAuthIndicate(DCAPMessage indicate) {
        if (!ChargeStatusCacheProvider.getInstance().isCloudTimeSynch()) {
            Log.w("XCloudAdapter.handleAuthIndicate", "not time synchnized from cloud, refuse to card auth indicate: " + indicate.toJson());
            DCAPMessage dcapAuthResponse = DCAPProxy.getInstance().createCAPResponseByIndcate(indicate);
            NackDirective nack = new NackDirective();
            nack.setError(ErrorCode.EC_INTERNAL_ERROR);
            DCAPProxy.getInstance().sendResponse(dcapAuthResponse, "cap", CAPMessage.DIRECTIVE_NACK, nack);
            return;
        }
        CAPMessage cap = (CAPMessage) indicate.getData();
        AuthDirective auth = (AuthDirective) cap.getData();
        String port = auth.getPort();
        String cardNo = auth.getUser_code();
        HashMap<String, Object> userData = auth.getUser_data();
        String timestamp = (String) userData.get("timestamp");
        String nonce = (String) userData.get("nonce");
        String signature = (String) userData.get("sign");
        XCloudMessage requestChargeWithIDCard = XCloudProtocolAgent.getInstance().sendRequestChargeWithIDCard(port, cardNo, timestamp, nonce, signature);
        if (requestChargeWithIDCard != null) {
            XCloudAdapterSession xcloudAdapterSession = new XCloudAdapterSession(null);
            xcloudAdapterSession.dcapMsg = indicate;
            xcloudAdapterSession.directiveType = "auth";
            xcloudAdapterSession.dcapTimestamp = System.currentTimeMillis();
            xcloudAdapterSession.xcloudMsg = requestChargeWithIDCard;
            Log.i("XCloudAdapter.handleAuthIndicate", "create an XCloud adapter session: " + xcloudAdapterSession.toJson());
            this.xcloudAdapterSessions.put(requestChargeWithIDCard.getSessionId(), xcloudAdapterSession);
            return;
        }
        Log.w("XCloudAdapter.handleAuthIndicate", "failed to send RequestChargeWithIDCard for indicate: " + indicate.toJson());
        DCAPMessage dcapAuthResponse2 = DCAPProxy.getInstance().createCAPResponseByIndcate(indicate);
        NackDirective nack2 = new NackDirective();
        nack2.setError(ErrorCode.EC_INTERNAL_ERROR);
        DCAPProxy.getInstance().sendResponse(dcapAuthResponse2, "cap", CAPMessage.DIRECTIVE_NACK, nack2);
    }

    public void handU3AuthRefused(XCloudRequestSession xcloudRequestSession) {
        XCloudMessage requestChargeWithIDCard = xcloudRequestSession.getRequest();
        String sessionId = requestChargeWithIDCard.getSessionId();
        XCloudAdapterSession xcloudAdapterSession = this.xcloudAdapterSessions.get(sessionId);
        if (xcloudAdapterSession == null) {
            Log.w("XCloudAdapter.handU3AuthRefused", "related XCloud adapter session is not exist !!! XCloud session id: " + sessionId);
            return;
        }
        nackResponse(xcloudAdapterSession.dcapMsg, xcloudRequestSession.getError());
        Log.i("XCloudAdapter.handU3AuthRefused", "destroy an XCloud adapter session: " + xcloudAdapterSession.toJson());
        this.xcloudAdapterSessions.remove(sessionId);
    }

    public void handleRequestStartCharge(XCloudMessage response) {
        String sessionId = response.getSessionId();
        XCloudAdapterSession xcloudAdapterSession = this.xcloudAdapterSessions.get(sessionId);
        if (xcloudAdapterSession == null) {
            Log.w("XCloudAdapter.handleRequestStartCharge", "related XCloud adapter session is not exist !!! XCloud session id: " + sessionId);
            return;
        }
        RequestStartCharge requestStartCharge = (RequestStartCharge) response.getBody();
        DCAPMessage dcapAuthResponse = DCAPProxy.getInstance().createCAPResponseByIndcate(xcloudAdapterSession.dcapMsg);
        HashMap<String, Object> attach = new HashMap<>();
        attach.put("bill_id", String.valueOf(requestStartCharge.getBillId()));
        attach.put(ContentDB.ChargeTable.FEE_RATE_ID, String.valueOf(requestStartCharge.getFeePolicyId()));
        attach.put(ContentDB.NFCConsumeFailCacheTable.BALANCE, String.valueOf(requestStartCharge.getBalance()));
        ChargeStopCondition chargeStopCondition = requestStartCharge.getAutoStopAt();
        if (chargeStopCondition != null) {
            USER_TC_TYPE userTcType = USER_TC_TYPE.auto;
            String userTcValue = null;
            if (chargeStopCondition.getFee().intValue() > 0) {
                userTcType = USER_TC_TYPE.fee;
                userTcValue = String.valueOf(chargeStopCondition.getFee());
            } else if (chargeStopCondition.getInterval().intValue() > 0) {
                userTcType = USER_TC_TYPE.time;
                userTcValue = String.valueOf(chargeStopCondition.getInterval().intValue() * 60);
            } else if (chargeStopCondition.getPower().intValue() > 0) {
                userTcType = USER_TC_TYPE.power;
                userTcValue = String.valueOf(chargeStopCondition.getPower());
            }
            attach.put(ContentDB.ChargeTable.USER_TC_TYPE, userTcType.getType());
            if (!TextUtils.isEmpty(userTcValue)) {
                attach.put(ContentDB.ChargeTable.USER_TC_VALUE, userTcValue);
            }
        }
        AckDirective ack = new AckDirective();
        ack.setAttach(attach);
        DCAPProxy.getInstance().sendResponse(dcapAuthResponse, "cap", "ack", ack);
        Log.i("XCloudAdapter.handleRequestStartCharge", "destroy an XCloud adapter session: " + xcloudAdapterSession.toJson());
        this.xcloudAdapterSessions.remove(sessionId);
    }

    public void handleRequestRefuseCharge(XCloudMessage response) {
        String sessionId = response.getSessionId();
        XCloudAdapterSession xcloudAdapterSession = this.xcloudAdapterSessions.get(sessionId);
        if (xcloudAdapterSession == null) {
            Log.w("XCloudAdapter.handleRequestRefuseCharge", "related XCloud adapter session is not exist !!! XCloud session id: " + sessionId);
            return;
        }
        RequestRefuseCharge requestRefuseCharge = (RequestRefuseCharge) response.getBody();
        DCAPMessage dcapAuthResponse = DCAPProxy.getInstance().createCAPResponseByIndcate(xcloudAdapterSession.dcapMsg);
        CHARGE_REFUSE_CAUSE refuseCause = CHARGE_REFUSE_CAUSE.UNDEFINED;
        HashMap<String, Object> attach = new HashMap<>();
        DeviceError error = requestRefuseCharge.getCause();
        String errorCode = error.getCode();
        if (DeviceError.BAD_QRCODE.equals(errorCode)) {
            refuseCause = CHARGE_REFUSE_CAUSE.BAD_QRCODE;
        } else if (DeviceError.USERGROUP_FORBIDDEN.equals(errorCode)) {
            refuseCause = CHARGE_REFUSE_CAUSE.USERGROUP_FORBIDDEN;
        } else if (DeviceError.BILL_UNPAID.equals(errorCode)) {
            refuseCause = CHARGE_REFUSE_CAUSE.BILL_UNPAID;
            String errorData = JsonBean.ObjectToJson(error.getData());
            if (!TextUtils.isEmpty(errorData)) {
                try {
                    JSONObject errorDataJson = new JSONObject(errorData);
                    if (errorDataJson.has("billId")) {
                        long billId = errorDataJson.getLong("billId");
                        attach.put("bill_id", String.valueOf(billId));
                    }
                    if (errorDataJson.has("total")) {
                        int total = errorDataJson.getInt("total");
                        attach.put(com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition.TYPE_FEE, String.valueOf(total));
                    }
                } catch (JSONException e) {
                    Log.w("XCloudAdapter.handleRequestRefuseCharge", Log.getStackTraceString(e));
                }
            }
        } else if (DeviceError.CHARGE_UNFINISHED.equals(errorCode)) {
            refuseCause = CHARGE_REFUSE_CAUSE.CHARGE_UNFINISHED;
        } else if (DeviceError.NOT_RESERVED.equals(errorCode)) {
            refuseCause = CHARGE_REFUSE_CAUSE.NOT_RESERVED;
        } else if (DeviceError.NOT_QUEUED.equals(errorCode)) {
            refuseCause = CHARGE_REFUSE_CAUSE.NOT_QUEUED;
        } else if (DeviceError.RESERVE_UNDUE.equals(errorCode)) {
            refuseCause = CHARGE_REFUSE_CAUSE.RESERVE_UNDUE;
        } else if (DeviceError.QUEUE_UNDUE.equals(errorCode)) {
            refuseCause = CHARGE_REFUSE_CAUSE.QUEUE_UNDUE;
            String errorData2 = (String) error.getData();
            if (!TextUtils.isEmpty(errorData2)) {
                try {
                    int order = new JSONObject(errorData2).getInt("order");
                    attach.put("queue_order", String.valueOf(order));
                } catch (JSONException e2) {
                    Log.w("XCloudAdapter.handleRequestRefuseCharge", Log.getStackTraceString(e2));
                }
            }
        } else if (DeviceError.RESERVE_TIMEOUT.equals(errorCode)) {
            refuseCause = CHARGE_REFUSE_CAUSE.RESERVE_TIMEOUT;
        } else if (DeviceError.QUEUE_TIMEOUT.equals(errorCode)) {
            refuseCause = CHARGE_REFUSE_CAUSE.QUEUE_TIMEOUT;
        } else if (DeviceError.BALANCE_INSUFFICIENT.equals(errorCode)) {
            refuseCause = CHARGE_REFUSE_CAUSE.BALANCE_INSUFFICIENT;
        } else if (DeviceError.BAD_IDCARD.equals(errorCode)) {
            refuseCause = CHARGE_REFUSE_CAUSE.BAD_IDCARD;
        }
        attach.put("cause", refuseCause.getCause());
        NackDirective nack = new NackDirective();
        nack.setError(ErrorCode.EC_CLOUD_AUTH_REFUSED);
        nack.setAttach(attach);
        DCAPProxy.getInstance().sendResponse(dcapAuthResponse, "cap", CAPMessage.DIRECTIVE_NACK, nack);
        Log.i("XCloudAdapter.handleRequestRefuseCharge", "destroy an XCloud adapter session: " + xcloudAdapterSession.toJson());
        this.xcloudAdapterSessions.remove(sessionId);
    }

    public void handleSendXCloudRequestFail(XCloudMessage request) {
        String sessionId = request.getSessionId();
        XCloudAdapterSession xcloudAdapterSession = this.xcloudAdapterSessions.get(sessionId);
        if (xcloudAdapterSession != null) {
            Log.w("XCloudAdapter.handleSendXCloudRequestFail", "failed to send XCloud request, destroy related XCloud adapter session: " + xcloudAdapterSession.toJson());
            nackResponse(xcloudAdapterSession.dcapMsg, null);
            this.xcloudAdapterSessions.remove(sessionId);
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

    public void maintainSession() {
        if (this.xcloudAdapterSessions.size() != 0) {
            long nowTimestamp = System.currentTimeMillis();
            Iterator<Map.Entry<String, XCloudAdapterSession>> it2 = this.xcloudAdapterSessions.entrySet().iterator();
            while (it2.hasNext()) {
                XCloudAdapterSession session = it2.next().getValue();
                if (nowTimestamp - session.dcapTimestamp > 30000) {
                    Log.w("XCloudAdapter.maintainSession", "destroy an timeout XCloud adapter session: " + session.toJson());
                    nackResponse(session.dcapMsg, null);
                    it2.remove();
                }
            }
        }
    }

    private boolean nackResponse(DCAPMessage indicate, ErrorCode err) {
        NackDirective nack = new NackDirective();
        if (err != null) {
            nack.setError(err.getCode());
            nack.setAttach(err.getData());
        }
        return DCAPProxy.getInstance().sendResponse(DCAPProxy.getInstance().createCAPResponseByIndcate(indicate), "cap", CAPMessage.DIRECTIVE_NACK, nack);
    }
}
