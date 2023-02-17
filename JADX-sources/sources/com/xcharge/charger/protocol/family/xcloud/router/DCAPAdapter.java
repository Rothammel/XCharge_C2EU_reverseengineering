package com.xcharge.charger.protocol.family.xcloud.router;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.api.Sequence;
import com.xcharge.charger.core.api.bean.AckDirective;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.cap.CAPDirectiveOption;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.core.api.bean.cap.ConditionDirective;
import com.xcharge.charger.core.api.bean.cap.EventDirective;
import com.xcharge.charger.core.api.bean.cap.FinDirective;
import com.xcharge.charger.core.api.bean.cap.InitDirective;
import com.xcharge.charger.core.api.bean.cap.QueryDirective;
import com.xcharge.charger.core.api.bean.cap.SetDirective;
import com.xcharge.charger.core.api.bean.cap.StartDirective;
import com.xcharge.charger.core.api.bean.cap.StopDirective;
import com.xcharge.charger.core.type.CHARGE_REFUSE_CAUSE;
import com.xcharge.charger.core.type.FIN_MODE;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.type.CHARGE_INIT_TYPE;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.CHARGE_USER_TYPE;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.bean.type.USER_TC_TYPE;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.protocol.family.xcloud.bean.ChargeStopCondition;
import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.CancelAutoStop;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestAction;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestAutoStop;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestEndCharge;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestRefuseCharge;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestStartCharge;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestStopCharge;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestUpdateStartTime;
import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestVerification;
import com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent;
import com.xcharge.charger.protocol.family.xcloud.session.XCloudChargeSession;
import com.xcharge.charger.protocol.family.xcloud.type.EnumActionStatus;
import com.xcharge.charger.protocol.family.xcloud.type.EnumDeviceAction;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.TimeUtils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class DCAPAdapter {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$core$type$FIN_MODE = null;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$protocol$family$xcloud$type$EnumDeviceAction = null;
    public static final long DEFAULT_SESSION_TIMEOUT = 15000;
    private Context context = null;
    private String sn = null;
    private HashMap<String, DCAPAdapterSession> dcapAdapterSessions = null;

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

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$protocol$family$xcloud$type$EnumDeviceAction() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$protocol$family$xcloud$type$EnumDeviceAction;
        if (iArr == null) {
            iArr = new int[EnumDeviceAction.valuesCustom().length];
            try {
                iArr[EnumDeviceAction.lockPlug.ordinal()] = 2;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[EnumDeviceAction.restart.ordinal()] = 1;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[EnumDeviceAction.unlockPlug.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            $SWITCH_TABLE$com$xcharge$charger$protocol$family$xcloud$type$EnumDeviceAction = iArr;
        }
        return iArr;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class DCAPAdapterSession extends JsonBean<DCAPAdapterSession> {
        DCAPMessage dcapMsg;
        String directiveType;
        long expired;
        XCloudMessage xcloudMsg;
        long xcloudTimestamp;

        private DCAPAdapterSession() {
            this.xcloudMsg = null;
            this.dcapMsg = null;
            this.directiveType = null;
            this.xcloudTimestamp = 0L;
            this.expired = 15000L;
        }

        /* synthetic */ DCAPAdapterSession(DCAPAdapterSession dCAPAdapterSession) {
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

        public long getXcloudTimestamp() {
            return this.xcloudTimestamp;
        }

        public void setXcloudTimestamp(long xcloudTimestamp) {
            this.xcloudTimestamp = xcloudTimestamp;
        }

        public long getExpired() {
            return this.expired;
        }

        public void setExpired(long expired) {
            this.expired = expired;
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

    public void handleRequestStartCharge(XCloudMessage request) {
        RequestStartCharge requestStartCharge = (RequestStartCharge) request.getBody();
        String chargeId = String.valueOf(requestStartCharge.getBillId());
        String userType = CHARGE_USER_TYPE.xcharge.getUserType();
        String userCode = String.valueOf(requestStartCharge.getBillId());
        CHARGE_INIT_TYPE initType = CHARGE_INIT_TYPE.cloud;
        XCloudChargeSession chargeSession = XCloudProtocolAgent.getInstance().getChargeSession(request.getPort());
        NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(chargeSession.getUser_type());
        boolean isU3Restarted = false;
        if (chargeId.equals(chargeSession.getCharge_id()) && NFC_CARD_TYPE.U3.equals(nfcCardType)) {
            isU3Restarted = true;
            userType = chargeSession.getUser_type();
            userCode = chargeSession.getUser_code();
            initType = CHARGE_INIT_TYPE.nfc;
        }
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(chargeId);
        InitDirective init = new InitDirective();
        init.setInit_type(initType);
        init.setUser_type(userType);
        init.setUser_code(userCode);
        init.setDevice_id("sn/" + this.sn);
        init.setPort(request.getPort());
        init.setUser_balance(requestStartCharge.getBalance());
        Long feePolicyId = requestStartCharge.getFeePolicyId();
        if (feePolicyId != null) {
            init.setFee_rate(String.valueOf(feePolicyId));
        }
        ChargeStopCondition chargeStopCondition = requestStartCharge.getAutoStopAt();
        if (chargeStopCondition != null) {
            LogUtils.cloudlog("receive xcloud RequestStartCharge with StopCondition: " + chargeStopCondition.toJson());
            USER_TC_TYPE userTcType = USER_TC_TYPE.auto;
            String userTcValue = null;
            if (chargeStopCondition.getFee() != null) {
                userTcType = USER_TC_TYPE.fee;
                userTcValue = String.valueOf(chargeStopCondition.getFee());
            } else if (chargeStopCondition.getInterval() != null) {
                userTcType = USER_TC_TYPE.time;
                userTcValue = String.valueOf(chargeStopCondition.getInterval().intValue() * 60);
            } else if (chargeStopCondition.getPower() != null) {
                userTcType = USER_TC_TYPE.power;
                userTcValue = String.valueOf(chargeStopCondition.getPower());
            }
            init.setUser_tc_type(userTcType);
            init.setUser_tc_value(userTcValue);
        }
        init.setCharge_platform(CHARGE_PLATFORM.xcharge);
        init.setTimeout_plugin(RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalChargeCancel());
        init.setTimeout_start(-1);
        init.setTimeout_plugout(RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalDelayStart());
        Long reserveTime = requestStartCharge.getStartTime();
        if (reserveTime != null) {
            init.setReserve_time(Long.valueOf(TimeUtils.getTsFromXCloudFormat(String.valueOf(reserveTime), RemoteSettingCacheProvider.getInstance().getProtocolTimezone())));
            LogUtils.cloudlog("receive xcloud RequestStartCharge with timing charge at " + reserveTime);
        }
        String from = "user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode;
        DCAPMessage initRequest = createRequest(from, "init", opt, init);
        DCAPProxy.getInstance().sendRequest(initRequest);
        if (!isU3Restarted) {
            DCAPAdapterSession dcapAdapterSession = new DCAPAdapterSession(null);
            dcapAdapterSession.xcloudMsg = request;
            dcapAdapterSession.xcloudTimestamp = System.currentTimeMillis();
            dcapAdapterSession.dcapMsg = initRequest;
            dcapAdapterSession.directiveType = "init";
            Log.i("DCAPAdapter.handleRequestStartCharge", "create DCAP adapter session: " + dcapAdapterSession.toJson());
            this.dcapAdapterSessions.put(String.valueOf(initRequest.getSeq()), dcapAdapterSession);
        }
    }

    public void handleRequestRefuseCharge(XCloudMessage request) {
        RequestRefuseCharge requestRefuseCharge = (RequestRefuseCharge) request.getBody();
        String port = String.valueOf(requestRefuseCharge.getPort());
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
                    Log.w("DCAPAdapter.handleRequestRefuseCharge", Log.getStackTraceString(e));
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
                    Log.w("DCAPAdapter.handleRequestRefuseCharge", Log.getStackTraceString(e2));
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
        if (attach.size() == 0) {
            attach = null;
        }
        eventRefuseChargeRequest(port, refuseCause, attach);
    }

    public void handleRequestAction(XCloudMessage request) {
        RequestAction requestAction = (RequestAction) request.getBody();
        String setId = null;
        HashMap<String, Object> values = new HashMap<>();
        EnumDeviceAction action = EnumDeviceAction.valueOf(requestAction.getAction());
        switch ($SWITCH_TABLE$com$xcharge$charger$protocol$family$xcloud$type$EnumDeviceAction()[action.ordinal()]) {
            case 1:
                setId = SetDirective.SET_ID_DEVICE;
                values.put("opr", "reboot");
                break;
            case 2:
                setId = SetDirective.SET_ID_PORT_GUNLOCK;
                values.put(ContentDB.ChargeTable.PORT, String.valueOf(requestAction.getPort()));
                values.put("opr", SetDirective.OPR_LOCK);
                break;
            case 3:
                setId = SetDirective.SET_ID_PORT_GUNLOCK;
                values.put(ContentDB.ChargeTable.PORT, String.valueOf(requestAction.getPort()));
                values.put("opr", SetDirective.OPR_UNLOCK);
                break;
        }
        if (!TextUtils.isEmpty(setId)) {
            CAPDirectiveOption opt = new CAPDirectiveOption();
            opt.setSet_id(setId);
            SetDirective set = new SetDirective();
            set.setValues(values);
            String from = "server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform();
            DCAPMessage setRequest = createRequest(from, "set", opt, set);
            DCAPProxy.getInstance().sendRequest(setRequest);
            DCAPAdapterSession dcapAdapterSession = new DCAPAdapterSession(null);
            dcapAdapterSession.xcloudMsg = request;
            dcapAdapterSession.xcloudTimestamp = System.currentTimeMillis();
            dcapAdapterSession.dcapMsg = setRequest;
            dcapAdapterSession.directiveType = "set";
            Log.i("DCAPAdapter.handleRequestAction", "create DCAP adapter session: " + dcapAdapterSession.toJson());
            this.dcapAdapterSessions.put(String.valueOf(setRequest.getSeq()), dcapAdapterSession);
            return;
        }
        Log.w("DCAPAdapter.handleRequestAction", "unsuported action: " + action);
        XCloudProtocolAgent.getInstance().sendReportActionResult(request, EnumActionStatus.failed, new DeviceError(DeviceError.JSON_ERROR, "unsuported action", null));
    }

    public void handleRequestVerification(XCloudMessage request) {
        RequestVerification requestVerification = (RequestVerification) request.getBody();
        String customer = requestVerification.getCustomer();
        int expired = requestVerification.getExpireInterval();
        if (expired <= 0) {
            Log.w("DCAPAdapter.handleRequestVerification", "illegal ExpireInterval in request: " + request.toJson());
            return;
        }
        QueryDirective query = new QueryDirective();
        HashMap<String, Object> params = new HashMap<>();
        params.put("customer", customer);
        params.put("expired", String.valueOf(expired));
        query.setParams(params);
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setQuery_id("device.verification");
        String from = "server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform();
        DCAPMessage queryRequest = createRequest(from, "query", opt, query);
        DCAPProxy.getInstance().sendRequest(queryRequest);
        DCAPAdapterSession dcapAdapterSession = new DCAPAdapterSession(null);
        dcapAdapterSession.xcloudMsg = request;
        dcapAdapterSession.xcloudTimestamp = System.currentTimeMillis();
        dcapAdapterSession.expired = (expired + 5) * 1000;
        dcapAdapterSession.dcapMsg = queryRequest;
        dcapAdapterSession.directiveType = "query";
        Log.i("DCAPAdapter.handleRequestVerification", "create DCAP adapter session: " + dcapAdapterSession.toJson());
        this.dcapAdapterSessions.put(String.valueOf(queryRequest.getSeq()), dcapAdapterSession);
    }

    public void handleRequestAutoStop(XCloudMessage request) {
        RequestAutoStop requestAutoStop = (RequestAutoStop) request.getBody();
        ChargeStopCondition chargeStopCondition = requestAutoStop.getAutoStopAt();
        if (chargeStopCondition != null) {
            USER_TC_TYPE userTcType = USER_TC_TYPE.auto;
            String userTcValue = null;
            if (chargeStopCondition.getFee() != null) {
                userTcType = USER_TC_TYPE.fee;
                userTcValue = String.valueOf(chargeStopCondition.getFee());
            } else if (chargeStopCondition.getInterval() != null) {
                userTcType = USER_TC_TYPE.time;
                userTcValue = String.valueOf(chargeStopCondition.getInterval().intValue() * 60);
            } else if (chargeStopCondition.getPower() != null) {
                userTcType = USER_TC_TYPE.power;
                userTcValue = String.valueOf(Double.valueOf(chargeStopCondition.getPower().intValue() * 1.0d));
            }
            String port = request.getPort();
            String chargeId = String.valueOf(requestAutoStop.getBillId());
            XCloudChargeSession chargeSession = XCloudProtocolAgent.getInstance().getChargeSession(port);
            if (chargeSession != null && chargeId.equals(chargeSession.getCharge_id())) {
                Log.i("DCAPAdapter.handleRequestAutoStop", "charge session: " + chargeSession.toJson());
                ConditionDirective condition = new ConditionDirective();
                condition.setUserTcType(userTcType);
                condition.setUserTcValue(userTcValue);
                CAPDirectiveOption opt = new CAPDirectiveOption();
                opt.setCharge_id(chargeId);
                opt.setCondition_id(ConditionDirective.CONDITION_USER_STOP);
                opt.setPort_id(port);
                String userType = chargeSession.getUser_type();
                String userCode = chargeSession.getUser_code();
                String from = "user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode;
                DCAPMessage conditionRequest = createRequest(from, CAPMessage.DIRECTIVE_CONDITION, opt, condition);
                DCAPProxy.getInstance().sendRequest(conditionRequest);
                DCAPAdapterSession dcapAdapterSession = new DCAPAdapterSession(null);
                dcapAdapterSession.xcloudMsg = request;
                dcapAdapterSession.xcloudTimestamp = System.currentTimeMillis();
                dcapAdapterSession.dcapMsg = conditionRequest;
                dcapAdapterSession.directiveType = CAPMessage.DIRECTIVE_CONDITION;
                Log.i("DCAPAdapter.handleRequestAutoStop", "create DCAP adapter session: " + dcapAdapterSession.toJson());
                this.dcapAdapterSessions.put(String.valueOf(conditionRequest.getSeq()), dcapAdapterSession);
                return;
            }
            Log.w("DCAPAdapter.handleRequestAutoStop", "failed to find charge session for charge: " + chargeId);
            XCloudProtocolAgent.getInstance().sendReportAutoStopResult(request, new DeviceError("ERROR", null, null));
            return;
        }
        Log.w("DCAPAdapter.handleRequestAutoStop", "no charge stop condition in request: " + request.toJson());
        XCloudProtocolAgent.getInstance().sendReportAutoStopResult(request, new DeviceError("ERROR", null, null));
    }

    public void handleRequestUpdateStartTime(XCloudMessage request) {
        RequestUpdateStartTime requestUpdateStartTime = (RequestUpdateStartTime) request.getBody();
        String port = request.getPort();
        String chargeId = String.valueOf(requestUpdateStartTime.getBillId());
        XCloudChargeSession chargeSession = XCloudProtocolAgent.getInstance().getChargeSession(port);
        if (chargeSession != null && chargeId.equals(chargeSession.getCharge_id())) {
            Log.i("DCAPAdapter.handleRequestUpdateStartTime", "charge session: " + chargeSession.toJson());
            Long startTime = requestUpdateStartTime.getStartTime();
            if (startTime == null) {
                String userType = chargeSession.getUser_type();
                String userCode = chargeSession.getUser_code();
                CAPDirectiveOption opt = new CAPDirectiveOption();
                opt.setCharge_id(chargeId);
                StartDirective start = new StartDirective();
                USER_TC_TYPE UserTcType = USER_TC_TYPE.auto;
                start.setUser_tc_type(UserTcType);
                start.setUser_tc_value(null);
                String from = "user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode;
                DCAPMessage startRequest = createRequest(from, "start", opt, start);
                DCAPProxy.getInstance().sendRequest(startRequest);
                DCAPAdapterSession dcapAdapterSession = new DCAPAdapterSession(null);
                dcapAdapterSession.xcloudMsg = request;
                dcapAdapterSession.xcloudTimestamp = System.currentTimeMillis();
                dcapAdapterSession.dcapMsg = startRequest;
                dcapAdapterSession.directiveType = "start";
                Log.i("DCAPAdapter.handleRequestUpdateStartTime", "create DCAP adapter session: " + dcapAdapterSession.toJson());
                this.dcapAdapterSessions.put(String.valueOf(startRequest.getSeq()), dcapAdapterSession);
                return;
            } else if (startTime.longValue() == 0) {
                CAPDirectiveOption opt2 = new CAPDirectiveOption();
                opt2.setCharge_id(chargeId);
                FinDirective fin = new FinDirective();
                fin.setFin_mode(FIN_MODE.cancel);
                fin.setError(null);
                String userType2 = chargeSession.getUser_type();
                String userCode2 = chargeSession.getUser_code();
                String from2 = "user:" + userType2 + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode2;
                DCAPMessage finRequest = createRequest(from2, "fin", opt2, fin);
                DCAPProxy.getInstance().sendRequest(finRequest);
                return;
            } else {
                long reserveTs = TimeUtils.getTsFromXCloudFormat(String.valueOf(startTime), RemoteSettingCacheProvider.getInstance().getProtocolTimezone());
                ConditionDirective condition = new ConditionDirective();
                condition.setReserveTime(Long.valueOf(reserveTs));
                CAPDirectiveOption opt3 = new CAPDirectiveOption();
                opt3.setCharge_id(chargeId);
                opt3.setCondition_id(ConditionDirective.CONDITION_USER_RESERVE);
                opt3.setPort_id(port);
                String userType3 = chargeSession.getUser_type();
                String userCode3 = chargeSession.getUser_code();
                String from3 = "user:" + userType3 + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode3;
                DCAPMessage conditionRequest = createRequest(from3, CAPMessage.DIRECTIVE_CONDITION, opt3, condition);
                DCAPProxy.getInstance().sendRequest(conditionRequest);
                DCAPAdapterSession dcapAdapterSession2 = new DCAPAdapterSession(null);
                dcapAdapterSession2.xcloudMsg = request;
                dcapAdapterSession2.xcloudTimestamp = System.currentTimeMillis();
                dcapAdapterSession2.dcapMsg = conditionRequest;
                dcapAdapterSession2.directiveType = CAPMessage.DIRECTIVE_CONDITION;
                Log.i("DCAPAdapter.handleRequestUpdateStartTime", "create DCAP adapter session: " + dcapAdapterSession2.toJson());
                this.dcapAdapterSessions.put(String.valueOf(conditionRequest.getSeq()), dcapAdapterSession2);
                return;
            }
        }
        Log.w("DCAPAdapter.handleRequestUpdateStartTime", "failed to find charge session for charge: " + chargeId);
    }

    public void handleCancelAutoStop(XCloudMessage request) {
        CancelAutoStop cancelAutoStop = (CancelAutoStop) request.getBody();
        ChargeStopCondition chargeStopCondition = cancelAutoStop.getAutoStopAt();
        if (chargeStopCondition != null) {
            String port = request.getPort();
            String chargeId = String.valueOf(cancelAutoStop.getBillId());
            XCloudChargeSession chargeSession = XCloudProtocolAgent.getInstance().getChargeSession(port);
            if (chargeSession != null && chargeId.equals(chargeSession.getCharge_id())) {
                Log.i("DCAPAdapter.handleCancelAutoStop", "charge session: " + chargeSession.toJson());
                ChargeStopCondition chargeStopConditionInSession = chargeSession.getChargeStopCondition();
                if (chargeStopConditionInSession != null && ((chargeStopConditionInSession.getFee() != null && chargeStopCondition.getFee() != null) || ((chargeStopConditionInSession.getInterval() != null && chargeStopCondition.getInterval() != null) || (chargeStopConditionInSession.getPower() != null && chargeStopCondition.getPower() != null)))) {
                    ConditionDirective condition = new ConditionDirective();
                    CAPDirectiveOption opt = new CAPDirectiveOption();
                    opt.setCharge_id(chargeId);
                    opt.setCondition_id(ConditionDirective.CONDITION_USER_STOP);
                    opt.setPort_id(port);
                    String userType = chargeSession.getUser_type();
                    String userCode = chargeSession.getUser_code();
                    String from = "user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode;
                    DCAPMessage conditionRequest = createRequest(from, CAPMessage.DIRECTIVE_CONDITION, opt, condition);
                    DCAPProxy.getInstance().sendRequest(conditionRequest);
                    DCAPAdapterSession dcapAdapterSession = new DCAPAdapterSession(null);
                    dcapAdapterSession.xcloudMsg = request;
                    dcapAdapterSession.xcloudTimestamp = System.currentTimeMillis();
                    dcapAdapterSession.dcapMsg = conditionRequest;
                    dcapAdapterSession.directiveType = CAPMessage.DIRECTIVE_CONDITION;
                    Log.i("DCAPAdapter.handleCancelAutoStop", "create DCAP adapter session: " + dcapAdapterSession.toJson());
                    this.dcapAdapterSessions.put(String.valueOf(conditionRequest.getSeq()), dcapAdapterSession);
                    return;
                }
                Log.w("DCAPAdapter.handleCancelAutoStop", "charge stop condition to cancel in request: " + request.toJson() + " is not in erver setted: " + chargeStopConditionInSession.toJson());
                XCloudProtocolAgent.getInstance().sendReportAutoStopResult(request, new DeviceError("ERROR", null, null));
                return;
            }
            Log.w("DCAPAdapter.handleCancelAutoStop", "failed to find charge session for charge: " + chargeId);
            XCloudProtocolAgent.getInstance().sendReportAutoStopResult(request, new DeviceError("ERROR", null, null));
            return;
        }
        Log.w("DCAPAdapter.handleCancelAutoStop", "no charge stop condition in request: " + request.toJson());
        XCloudProtocolAgent.getInstance().sendReportAutoStopResult(request, new DeviceError("ERROR", null, null));
    }

    public void handleRequestStopCharge(XCloudMessage request) {
        RequestStopCharge requestStopCharge = (RequestStopCharge) request.getBody();
        String billId = String.valueOf(requestStopCharge.getBillId());
        String port = XCloudProtocolAgent.getInstance().getPort(billId);
        if (!TextUtils.isEmpty(port)) {
            XCloudChargeSession chargeSession = XCloudProtocolAgent.getInstance().getChargeSession(port);
            Log.i("DCAPAdapter.handleRequestStopCharge", "found charge session: " + chargeSession.toJson());
            String userType = chargeSession.getUser_type();
            String userCode = chargeSession.getUser_code();
            CAPDirectiveOption opt = new CAPDirectiveOption();
            opt.setCharge_id(billId);
            StopDirective stop = new StopDirective();
            String from = "server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform();
            DeviceError stopCause = requestStopCharge.getCause();
            if (stopCause != null) {
                if (DeviceError.USER_REMOTE.equals(stopCause.getCode())) {
                    from = "user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode;
                }
            } else {
                from = "user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode;
            }
            DCAPMessage stopRequest = createRequest(from, "stop", opt, stop);
            DCAPProxy.getInstance().sendRequest(stopRequest);
            DCAPAdapterSession dcapAdapterSession = new DCAPAdapterSession(null);
            dcapAdapterSession.xcloudMsg = request;
            dcapAdapterSession.xcloudTimestamp = System.currentTimeMillis();
            dcapAdapterSession.dcapMsg = stopRequest;
            dcapAdapterSession.directiveType = "stop";
            Log.i("DCAPAdapter.handleRequestStopCharge", "create DCAP adapter session: " + dcapAdapterSession.toJson());
            this.dcapAdapterSessions.put(String.valueOf(stopRequest.getSeq()), dcapAdapterSession);
            return;
        }
        Log.w("DCAPAdapter.handleRequestStopCharge", "failed to find related port for charge: " + billId);
    }

    public void handleRequestEndCharge(XCloudMessage request) {
        RequestEndCharge requestEndCharge = (RequestEndCharge) request.getBody();
        String billId = String.valueOf(requestEndCharge.getBillId());
        finChargeByRemote(billId);
    }

    public void handleInitAckIndicate(DCAPMessage indicate) {
        CAPMessage cap = (CAPMessage) indicate.getData();
        CAPDirectiveOption opt = cap.getOpt();
        String ackInitSeq = String.valueOf(opt.getSeq());
        DCAPAdapterSession dcapAdapterSession = this.dcapAdapterSessions.get(ackInitSeq);
        if (dcapAdapterSession == null) {
            Log.w("DCAPAdapter.handleInitAckIndicate", "not found DCAP adapter session for indicate: " + indicate.toJson());
        } else if (dcapAdapterSession.directiveType.equals("init") && dcapAdapterSession.xcloudMsg.getMessageName().equals(XCloudMessage.RequestStartCharge)) {
            ackResponse(indicate);
            Log.i("DCAPAdapter.handleInitAckIndicate", "destroy DCAP adapter session: " + dcapAdapterSession.toJson());
            this.dcapAdapterSessions.remove(ackInitSeq);
        } else {
            Log.w("DCAPAdapter.handleInitAckIndicate", "not matched DCAP adapter session: " + dcapAdapterSession.toJson() + " and indicate: " + indicate.toJson());
        }
    }

    public void handleFinIndicate(DCAPMessage indicate) {
        DeviceError deviceError;
        ackResponse(indicate);
        CAPMessage cap = (CAPMessage) indicate.getData();
        FinDirective fin = (FinDirective) cap.getData();
        FIN_MODE finMode = fin.getFin_mode();
        ErrorCode error = fin.getError();
        String code = DeviceError.OTHER;
        switch ($SWITCH_TABLE$com$xcharge$charger$core$type$FIN_MODE()[finMode.ordinal()]) {
            case 3:
                code = DeviceError.USER_CANCEL;
                break;
            case 4:
                code = DeviceError.DISABLED;
                break;
            case 5:
                code = DeviceError.BUSY;
                break;
            case 6:
                code = DeviceError.NO_FEEPOLICY;
                break;
            case 7:
                code = DeviceError.PLUG_TIMEOUT;
                break;
            case 8:
            case 9:
                code = "ERROR";
                break;
            case 10:
                code = DeviceError.TIMED_CHARGE_ERROR;
                break;
        }
        if (error != null) {
            if (error.getCode() >= 30010 && error.getCode() <= 30018) {
                deviceError = new DeviceError(code, null, String.valueOf((error.getCode() + 5000) - 30000));
            } else {
                deviceError = new DeviceError(code, null, null);
            }
        } else if (FIN_MODE.car.equals(finMode)) {
            deviceError = new DeviceError(code, null, "5019");
        } else {
            deviceError = new DeviceError(code, null, null);
        }
        CAPDirectiveOption opt = cap.getOpt();
        if ("init".equals(opt.getOp())) {
            if (indicate.getTo().startsWith("user:nfc." + NFC_CARD_TYPE.U3)) {
                XCloudProtocolAgent.getInstance().sendReportChargeCancelled(null, opt.getCharge_id(), deviceError);
                return;
            }
            String nackInitSeq = String.valueOf(opt.getSeq());
            DCAPAdapterSession dcapAdapterSession = this.dcapAdapterSessions.get(nackInitSeq);
            if (dcapAdapterSession == null) {
                Log.w("DCAPAdapter.handleFinIndicate", "not found DCAP adapter session for indicate: " + indicate.toJson());
                XCloudChargeSession chargeSession = XCloudProtocolAgent.getInstance().getChargeSessionById(opt.getCharge_id());
                if (chargeSession != null) {
                    XCloudProtocolAgent.getInstance().sendReportChargeCancelled(chargeSession.getRequestStartCharge(), opt.getCharge_id(), deviceError);
                }
            } else if (dcapAdapterSession.directiveType.equals("init") && dcapAdapterSession.xcloudMsg.getMessageName().equals(XCloudMessage.RequestStartCharge)) {
                XCloudProtocolAgent.getInstance().sendReportChargeCancelled(dcapAdapterSession.xcloudMsg, null, deviceError);
                Log.i("DCAPAdapter.handleFinIndicate", "destroy DCAP adapter session: " + dcapAdapterSession.toJson());
                this.dcapAdapterSessions.remove(nackInitSeq);
            } else {
                Log.w("DCAPAdapter.handleFinIndicate", "not matched DCAP adapter session: " + dcapAdapterSession.toJson() + " and indicate: " + indicate.toJson());
            }
        } else if (DeviceError.TIMED_CHARGE_ERROR.equals(code)) {
            String chargeId = opt.getCharge_id();
            XCloudChargeSession chargeSession2 = XCloudProtocolAgent.getInstance().getChargeSessionById(chargeId);
            if (chargeSession2 != null) {
                XCloudProtocolAgent.getInstance().sendReportChargeCancelled(chargeSession2.getRequestStartCharge(), chargeId, deviceError);
            }
        } else {
            XCloudProtocolAgent.getInstance().sendReportChargeCancelled(null, opt.getCharge_id(), deviceError);
        }
    }

    public void handleQueryConfirm(DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) confirm.getData();
        CAPDirectiveOption opt = cap.getOpt();
        String peerSeq = String.valueOf(opt.getSeq());
        DCAPAdapterSession dcapAdapterSession = this.dcapAdapterSessions.get(peerSeq);
        if (dcapAdapterSession == null) {
            Log.w("DCAPAdapter.handleQueryConfirm", "not found DCAP adapter session for confirm: " + confirm.toJson());
            return;
        }
        XCloudMessage cloudRequest = dcapAdapterSession.xcloudMsg;
        String name = cloudRequest.getMessageName();
        String op = cap.getOp();
        if ("ack".equals(op)) {
            if (XCloudMessage.RequestVerification.equals(name)) {
                XCloudProtocolAgent.getInstance().sendReportVerification(dcapAdapterSession.xcloudMsg);
            }
        } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
            XCloudMessage.RequestVerification.equals(name);
        }
        Log.i("DCAPAdapter.handleQueryConfirm", "destroy DCAP adapter session: " + dcapAdapterSession.toJson());
        this.dcapAdapterSessions.remove(peerSeq);
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
        XCloudMessage cloudRequest = dcapAdapterSession.xcloudMsg;
        String name = cloudRequest.getMessageName();
        String op = cap.getOp();
        if ("ack".equals(op)) {
            if (XCloudMessage.RequestAction.equals(name)) {
                XCloudProtocolAgent.getInstance().sendReportActionResult(dcapAdapterSession.xcloudMsg, EnumActionStatus.success, null);
            }
        } else if (CAPMessage.DIRECTIVE_NACK.equals(op) && XCloudMessage.RequestAction.equals(name)) {
            XCloudProtocolAgent.getInstance().sendReportActionResult(dcapAdapterSession.xcloudMsg, EnumActionStatus.failed, new DeviceError("ERROR", null, null));
        }
        Log.i("DCAPAdapter.handleSetConfirm", "destroy DCAP adapter session: " + dcapAdapterSession.toJson());
        this.dcapAdapterSessions.remove(peerSeq);
    }

    public void handleConditionConfirm(DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) confirm.getData();
        CAPDirectiveOption opt = cap.getOpt();
        String peerSeq = String.valueOf(opt.getSeq());
        DCAPAdapterSession dcapAdapterSession = this.dcapAdapterSessions.get(peerSeq);
        if (dcapAdapterSession == null) {
            Log.w("DCAPAdapter.handleConditionConfirm", "not found DCAP adapter session for confirm: " + confirm.toJson());
            return;
        }
        String op = cap.getOp();
        if ("ack".equals(op)) {
            CAPMessage requstCap = (CAPMessage) dcapAdapterSession.dcapMsg.getData();
            String conditionId = requstCap.getOpt().getCondition_id();
            if (ConditionDirective.CONDITION_USER_STOP.equals(conditionId)) {
                ConditionDirective condition = (ConditionDirective) requstCap.getData();
                if (condition.getUserTcType() == null) {
                    String port = dcapAdapterSession.xcloudMsg.getPort();
                    CancelAutoStop cancelAutoStop = (CancelAutoStop) dcapAdapterSession.xcloudMsg.getBody();
                    XCloudChargeSession chargeSession = XCloudProtocolAgent.getInstance().getChargeSession(port);
                    if (chargeSession != null) {
                        Log.i("DCAPAdapter.handleConditionConfirm", "clear charge stop condition in charge: " + cancelAutoStop.getBillId());
                        chargeSession.setChargeStopCondition(null);
                    }
                }
                XCloudProtocolAgent.getInstance().sendReportAutoStopResult(dcapAdapterSession.xcloudMsg, null);
            } else {
                ConditionDirective.CONDITION_USER_RESERVE.equals(conditionId);
            }
            Log.i("DCAPAdapter.handleConditionConfirm", "destroy DCAP adapter session: " + dcapAdapterSession.toJson());
        } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
            String conditionId2 = ((CAPMessage) dcapAdapterSession.dcapMsg.getData()).getOpt().getCondition_id();
            if (ConditionDirective.CONDITION_USER_STOP.equals(conditionId2)) {
                XCloudProtocolAgent.getInstance().sendReportAutoStopResult(dcapAdapterSession.xcloudMsg, new DeviceError("ERROR", null, null));
            } else {
                ConditionDirective.CONDITION_USER_RESERVE.equals(conditionId2);
            }
            Log.i("DCAPAdapter.handleConditionConfirm", "destroy DCAP adapter session: " + dcapAdapterSession.toJson());
        }
        this.dcapAdapterSessions.remove(peerSeq);
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
            Log.i("DCAPAdapter.handleStartComfirm", "destroy an DCAP adapter session: " + dcapAdapterSession.toJson());
        } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
            Log.i("DCAPAdapter.handleStartComfirm", "destroy an DCAP adapter session: " + dcapAdapterSession.toJson());
        }
        this.dcapAdapterSessions.remove(peerSeq);
    }

    public void handleStopConfirm(DCAPMessage confirm) {
        CAPMessage cap = (CAPMessage) confirm.getData();
        CAPDirectiveOption opt = cap.getOpt();
        String peerSeq = String.valueOf(opt.getSeq());
        DCAPAdapterSession dcapAdapterSession = this.dcapAdapterSessions.get(peerSeq);
        if (dcapAdapterSession == null) {
            Log.w("DCAPAdapter.handleStopConfirm", "not found DCAP adapter session for confirm: " + confirm.toJson());
            return;
        }
        String op = cap.getOp();
        if ("ack".equals(op)) {
            Log.i("DCAPAdapter.handleStopConfirm", "destroy DCAP adapter session: " + dcapAdapterSession.toJson());
            this.dcapAdapterSessions.remove(peerSeq);
        } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
            Log.i("DCAPAdapter.handleStopConfirm", "destroy DCAP adapter session: " + dcapAdapterSession.toJson());
            this.dcapAdapterSessions.remove(peerSeq);
        }
    }

    public void maintainSession() {
        if (this.dcapAdapterSessions.size() != 0) {
            long nowTimestamp = System.currentTimeMillis();
            Iterator<Map.Entry<String, DCAPAdapterSession>> it2 = this.dcapAdapterSessions.entrySet().iterator();
            while (it2.hasNext()) {
                DCAPAdapterSession session = it2.next().getValue();
                if (nowTimestamp - session.xcloudTimestamp > session.getExpired()) {
                    Log.w("DCAPAdapter.maintainSession", "destroy timeout DCAP adapter session: " + session.toJson());
                    XCloudMessage request = session.xcloudMsg;
                    String name = request.getMessageName();
                    if (XCloudMessage.RequestStartCharge.equals(name)) {
                        finRequest(FIN_MODE.timeout, null, session.dcapMsg);
                        XCloudProtocolAgent.getInstance().sendReportChargeCancelled(request, null, new DeviceError(DeviceError.OTHER, null, null));
                        return;
                    }
                    if (!XCloudMessage.RequestStopCharge.equals(name)) {
                        if (XCloudMessage.RequestAutoStop.equals(name)) {
                            XCloudProtocolAgent.getInstance().sendReportAutoStopResult(request, new DeviceError("ERROR", null, null));
                        } else if (XCloudMessage.CancelAutoStop.equals(name)) {
                            XCloudProtocolAgent.getInstance().sendReportAutoStopResult(request, new DeviceError("ERROR", null, null));
                        } else if (XCloudMessage.RequestAction.equals(name)) {
                            XCloudProtocolAgent.getInstance().sendReportActionResult(request, EnumActionStatus.failed, new DeviceError("ERROR", null, null));
                        }
                    }
                    it2.remove();
                }
            }
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

    private void finChargeByRemote(String chargeId) {
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
                DCAPProxy.getInstance().sendRequest(request);
                return;
            }
            Log.w("DCAPAdapter.finChargeByRemote", "charge: " + chargeId + " is not in CHARGING state, refuse to finish by remote");
            return;
        }
        Log.w("DCAPAdapter.finChargeByRemote", "charge: " + chargeId + " is not exist, refuse to finish by remote");
    }

    private void finRequest(FIN_MODE finMode, ErrorCode error, DCAPMessage init) {
        CAPMessage initCap = (CAPMessage) init.getData();
        InitDirective initDirective = (InitDirective) initCap.getData();
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(initCap.getOpt().getCharge_id());
        FinDirective fin = new FinDirective();
        fin.setFin_mode(finMode);
        fin.setError(error);
        String from = "user:" + initDirective.getUser_type() + MqttTopic.TOPIC_LEVEL_SEPARATOR + initDirective.getUser_code();
        DCAPMessage request = createRequest(from, "fin", opt, fin);
        DCAPProxy.getInstance().sendRequest(request);
    }

    private void eventRefuseChargeRequest(String port, CHARGE_REFUSE_CAUSE cause, HashMap<String, Object> attach) {
        EventDirective event = new EventDirective();
        event.setRefuse_cause(cause);
        event.setAttach(attach);
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setPort_id(port);
        opt.setEvent_id(EventDirective.EVENT_CHARGE_REFUSE);
        String from = "server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform();
        DCAPMessage request = createRequest(from, "event", opt, event);
        DCAPProxy.getInstance().sendRequest(request);
    }

    private boolean ackResponse(DCAPMessage indicate) {
        AckDirective ack = new AckDirective();
        return DCAPProxy.getInstance().sendResponse(DCAPProxy.getInstance().createCAPResponseByIndcate(indicate), "cap", "ack", ack);
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
}
