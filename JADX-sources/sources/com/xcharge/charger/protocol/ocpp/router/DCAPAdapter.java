package com.xcharge.charger.protocol.ocpp.router;

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
import com.xcharge.charger.core.api.bean.cap.FinDirective;
import com.xcharge.charger.core.api.bean.cap.InitDirective;
import com.xcharge.charger.core.api.bean.cap.SetDirective;
import com.xcharge.charger.core.api.bean.cap.StopDirective;
import com.xcharge.charger.core.type.FIN_MODE;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.CHARGE_INIT_TYPE;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.CHARGE_STATUS;
import com.xcharge.charger.data.bean.type.CHARGE_USER_TYPE;
import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.bean.type.USER_TC_TYPE;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.protocol.ocpp.bean.OcppMessage;
import com.xcharge.charger.protocol.ocpp.bean.cloud.CancelReservationReq;
import com.xcharge.charger.protocol.ocpp.bean.cloud.RemoteStartTransactionReq;
import com.xcharge.charger.protocol.ocpp.bean.cloud.RemoteStopTransactionReq;
import com.xcharge.charger.protocol.ocpp.bean.cloud.ReserveNowReq;
import com.xcharge.charger.protocol.ocpp.bean.cloud.ResetReq;
import com.xcharge.charger.protocol.ocpp.bean.cloud.UnlockConnectorReq;
import com.xcharge.charger.protocol.ocpp.bean.device.StartTransactionReq;
import com.xcharge.charger.protocol.ocpp.bean.types.ChargingProfile;
import com.xcharge.charger.protocol.ocpp.bean.types.ResetType;
import com.xcharge.charger.protocol.ocpp.bean.types.UnlockStatus;
import com.xcharge.charger.protocol.ocpp.handler.OcppProtocolAgent;
import com.xcharge.charger.protocol.ocpp.session.OcppChargeSession;
import com.xcharge.charger.ui.c2.activity.BaseActivity;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.TimeUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class DCAPAdapter {
    public static final long DEFAULT_SESSION_TIMEOUT = 15000;
    private static String sn = null;
    private Context context = null;
    private HashMap<String, DCAPAdapterSession> dcapAdapterSessions = null;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class DCAPAdapterSession extends JsonBean<DCAPAdapterSession> {
        DCAPMessage dcapMsg;
        String directiveType;
        JSONArray ocppMsg;
        long ocppTimestamp;

        private DCAPAdapterSession() {
            this.ocppMsg = null;
            this.dcapMsg = null;
            this.directiveType = null;
            this.ocppTimestamp = 0L;
        }

        /* synthetic */ DCAPAdapterSession(DCAPAdapterSession dCAPAdapterSession) {
            this();
        }

        public JSONArray getOcppMsg() {
            return this.ocppMsg;
        }

        public void setOcppMsg(JSONArray ocppMsg) {
            this.ocppMsg = ocppMsg;
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

        public long getOcppTimestamp() {
            return this.ocppTimestamp;
        }

        public void setOcppTimestamp(long ocppTimestamp) {
            this.ocppTimestamp = ocppTimestamp;
        }
    }

    public void init(Context context) {
        this.context = context;
        sn = HardwareStatusCacheProvider.getInstance().getSn();
        this.dcapAdapterSessions = new HashMap<>();
    }

    public void destroy() {
        this.dcapAdapterSessions.clear();
    }

    public void maintainSession() {
        try {
            if (this.dcapAdapterSessions.size() == 0) {
                return;
            }
            long nowTimestamp = System.currentTimeMillis();
            Iterator<Map.Entry<String, DCAPAdapterSession>> it2 = this.dcapAdapterSessions.entrySet().iterator();
            while (it2.hasNext()) {
                DCAPAdapterSession session = it2.next().getValue();
                if (nowTimestamp - session.ocppTimestamp > 15000) {
                    Log.w("DCAPAdapter.maintainSession", "destroy an timeout DCAP adapter session: " + session.toJson());
                    JSONArray request = session.ocppMsg;
                    String string = request.getString(2);
                    switch (string.hashCode()) {
                        case -977722974:
                            if (string.equals(OcppMessage.RemoteStartTransaction)) {
                                OcppProtocolAgent.getInstance().sendRemoteStartTransactionConf(request.getString(1), "Rejected");
                                break;
                            }
                            break;
                        case -362842058:
                            if (string.equals(OcppMessage.RemoteStopTransaction)) {
                                OcppProtocolAgent.getInstance().sendRemoteStopTransactionConf(request.getString(1), "Rejected");
                                break;
                            }
                            break;
                        case 280557722:
                            if (string.equals(OcppMessage.ReserveNow)) {
                                OcppProtocolAgent.getInstance().sendReserveNowConf(request.getString(1), "Rejected");
                                break;
                            }
                            break;
                    }
                    it2.remove();
                }
            }
        } catch (Exception e) {
            Log.w("DCAPAdapter.maintainSession", Log.getStackTraceString(e));
        }
    }

    public void handleRemoteStartTransactionReq(JSONArray jsonArray) {
        try {
            RemoteStartTransactionReq remoteStartTransactionReq = new RemoteStartTransactionReq().fromJson(jsonArray.getJSONObject(3).toString());
            String port = String.valueOf(remoteStartTransactionReq.getConnectorId());
            ChargingProfile chargingProfile = remoteStartTransactionReq.getChargingProfile();
            if (chargingProfile != null) {
                OcppChargeSession chargeSession = OcppProtocolAgent.getInstance().getChargeSession(port);
                HashMap<String, ArrayList<ChargingProfile>> txChargingProfiles = chargeSession.getTxChargingProfiles();
                ArrayList<ChargingProfile> chargingProfiles = txChargingProfiles.get(port);
                if (chargingProfiles == null) {
                    chargingProfiles = new ArrayList<>();
                }
                chargingProfiles.add(chargingProfile);
                txChargingProfiles.put(port, chargingProfiles);
            }
            String chargeId = String.valueOf(System.currentTimeMillis() / 1000);
            String userType = CHARGE_USER_TYPE.ocpp.getUserType();
            String userCode = remoteStartTransactionReq.getIdTag();
            CHARGE_INIT_TYPE initType = CHARGE_INIT_TYPE.cloud;
            CAPDirectiveOption opt = new CAPDirectiveOption();
            opt.setCharge_id(chargeId);
            InitDirective init = new InitDirective();
            init.setInit_type(initType);
            init.setUser_type(userType);
            init.setUser_code(userCode);
            init.setDevice_id("sn/" + sn);
            init.setPort(port);
            init.setCharge_platform(CHARGE_PLATFORM.ocpp);
            String connectionTimeOut = OcppProtocolAgent.getInstance().getOcppConfig().getMaps().get(OcppMessage.ConnectionTimeOut);
            if (!TextUtils.isEmpty(connectionTimeOut) && TextUtils.isDigitsOnly(connectionTimeOut) && Integer.parseInt(connectionTimeOut) > 0) {
                init.setTimeout_plugin(Integer.parseInt(connectionTimeOut));
            } else {
                init.setTimeout_plugin(RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalChargeCancel());
            }
            init.setTimeout_start(-1);
            init.setTimeout_plugout(RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalDelayStart());
            String from = "user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode;
            DCAPMessage initRequest = createRequest(from, "init", opt, init);
            DCAPProxy.getInstance().sendRequest(initRequest);
            DCAPAdapterSession dcapAdapterSession = new DCAPAdapterSession(null);
            dcapAdapterSession.ocppMsg = jsonArray;
            dcapAdapterSession.ocppTimestamp = System.currentTimeMillis();
            dcapAdapterSession.dcapMsg = initRequest;
            dcapAdapterSession.directiveType = "init";
            Log.i("DCAPAdapter.handleRemoteStartTransactionReq", "create an DCAP adapter session: " + dcapAdapterSession.toJson());
            this.dcapAdapterSessions.put(String.valueOf(initRequest.getSeq()), dcapAdapterSession);
        } catch (Exception e) {
            Log.w("DCAPAdapter.handleRemoteStartTransactionReq", Log.getStackTraceString(e));
        }
    }

    public void handleRemoteStopTransactionReq(JSONArray jsonArray) {
        try {
            RemoteStopTransactionReq remoteStopTransactionReq = new RemoteStopTransactionReq().fromJson(jsonArray.getJSONObject(3).toString());
            String transactionId = String.valueOf(remoteStopTransactionReq.getTransactionId());
            ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBillcloud(transactionId);
            if (!TextUtils.isEmpty(transactionId) && chargeBill != null) {
                stopCharge(chargeBill.getPort(), jsonArray);
            }
        } catch (Exception e) {
            Log.w("DCAPAdapter.handleRemoteStopTransactionReq", Log.getStackTraceString(e));
        }
    }

    public void handleReserveNowReq(JSONArray jsonArray) {
        try {
            ReserveNowReq reserveNowReq = new ReserveNowReq().fromJson(jsonArray.getJSONObject(3).toString());
            String port = String.valueOf(reserveNowReq.getConnectorId());
            String reserveConnectorZeroSupported = OcppProtocolAgent.getInstance().getOcppConfig().getMaps().get(OcppMessage.ReserveConnectorZeroSupported);
            if (!TextUtils.isEmpty(reserveConnectorZeroSupported)) {
                boolean isSupported = Boolean.parseBoolean(reserveConnectorZeroSupported);
                if (port.equals("0") && isSupported) {
                    port = "1";
                }
            }
            String chargeId = String.valueOf(reserveNowReq.getReservationId());
            String userType = CHARGE_USER_TYPE.ocpp.getUserType();
            String userCode = reserveNowReq.getIdTag();
            CHARGE_INIT_TYPE initType = CHARGE_INIT_TYPE.cloud;
            CAPDirectiveOption opt = new CAPDirectiveOption();
            opt.setCharge_id(chargeId);
            InitDirective init = new InitDirective();
            init.setInit_type(initType);
            init.setUser_type(userType);
            init.setUser_code(userCode);
            init.setDevice_id("sn/" + sn);
            init.setPort(port);
            init.setCharge_platform(CHARGE_PLATFORM.ocpp);
            init.setTimeout_plugin(RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalChargeCancel());
            init.setTimeout_start(-1);
            init.setTimeout_plugout(RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalDelayStart());
            long reserveTime = TimeUtils.getTsFromISO8601Format(reserveNowReq.getExpiryDate());
            if (reserveTime > 0) {
                init.setReserve_time(Long.valueOf(reserveTime));
                LogUtils.cloudlog("receive ocpp handleReserveNowReq with timing charge at " + reserveTime);
            }
            String from = "user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode;
            DCAPMessage initRequest = createRequest(from, "init", opt, init);
            DCAPProxy.getInstance().sendRequest(initRequest);
            DCAPAdapterSession dcapAdapterSession = new DCAPAdapterSession(null);
            dcapAdapterSession.ocppMsg = jsonArray;
            dcapAdapterSession.ocppTimestamp = System.currentTimeMillis();
            dcapAdapterSession.dcapMsg = initRequest;
            dcapAdapterSession.directiveType = "init";
            Log.i("DCAPAdapter.handleReserveNowReq", "create DCAP adapter session: " + dcapAdapterSession.toJson());
            this.dcapAdapterSessions.put(String.valueOf(initRequest.getSeq()), dcapAdapterSession);
        } catch (Exception e) {
            Log.w("DCAPAdapter.handleReserveNowReq", Log.getStackTraceString(e));
        }
    }

    public void handleCancelReservationReq(JSONArray jsonArray) {
        try {
            CancelReservationReq cancelReservationReq = new CancelReservationReq().fromJson(jsonArray.getJSONObject(3).toString());
            String chargeId = String.valueOf(cancelReservationReq.getReservationId());
            String port = OcppProtocolAgent.getInstance().getPort(chargeId);
            OcppChargeSession chargeSession = OcppProtocolAgent.getInstance().getChargeSession(port);
            if (chargeSession != null && chargeId.equals(chargeSession.getCharge_id())) {
                CAPDirectiveOption opt = new CAPDirectiveOption();
                opt.setCharge_id(String.valueOf(cancelReservationReq.getReservationId()));
                FinDirective fin = new FinDirective();
                fin.setFin_mode(FIN_MODE.cancel);
                fin.setError(null);
                String userType = chargeSession.getUser_type();
                String userCode = chargeSession.getUser_code();
                String from = "user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode;
                DCAPMessage finRequest = createRequest(from, "fin", opt, fin);
                DCAPProxy.getInstance().sendRequest(finRequest);
            }
        } catch (Exception e) {
            Log.w("DCAPAdapter.handleCancelReservationReq", Log.getStackTraceString(e));
        }
    }

    public void setMaxChargeEnergy(JSONArray request) {
        try {
            StartTransactionReq startTransactionReq = new StartTransactionReq().fromJson(request.getJSONObject(3).toString());
            JSONObject attach = request.getJSONObject(4);
            String chargeId = attach.getString("value");
            String port = attach.getString(ContentDB.ChargeTable.PORT);
            String maxEnergyOnInvalidId = OcppProtocolAgent.getInstance().getOcppConfig().getMaps().get(OcppMessage.MaxEnergyOnInvalidId);
            double power = new BigDecimal(Integer.parseInt(maxEnergyOnInvalidId) / 1000.0d).setScale(2, 4).doubleValue();
            ConditionDirective condition = new ConditionDirective();
            condition.setUserTcType(USER_TC_TYPE.power);
            condition.setUserTcValue(String.format(BaseActivity.TWODP, Double.valueOf(power)));
            CAPDirectiveOption opt = new CAPDirectiveOption();
            opt.setCharge_id(chargeId);
            opt.setCondition_id(ConditionDirective.CONDITION_USER_STOP);
            opt.setPort_id(port);
            String userType = CHARGE_USER_TYPE.ocpp.getUserType();
            String userCode = startTransactionReq.getIdTag();
            String from = "user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode;
            DCAPMessage conditionRequest = createRequest(from, CAPMessage.DIRECTIVE_CONDITION, opt, condition);
            DCAPProxy.getInstance().sendRequest(conditionRequest);
            DCAPAdapterSession dcapAdapterSession = new DCAPAdapterSession(null);
            dcapAdapterSession.ocppMsg = request;
            dcapAdapterSession.ocppTimestamp = System.currentTimeMillis();
            dcapAdapterSession.dcapMsg = conditionRequest;
            dcapAdapterSession.directiveType = CAPMessage.DIRECTIVE_CONDITION;
            Log.i("DCAPAdapter.setMaxChargeEnergy", "create DCAP adapter session: " + dcapAdapterSession.toJson());
            this.dcapAdapterSessions.put(String.valueOf(conditionRequest.getSeq()), dcapAdapterSession);
        } catch (Exception e) {
            Log.w("DCAPAdapter.setMaxChargeEnergy", Log.getStackTraceString(e));
        }
    }

    public void handleInitAckIndicate(DCAPMessage indicate) {
        try {
            CAPMessage cap = (CAPMessage) indicate.getData();
            CAPDirectiveOption opt = cap.getOpt();
            String ackInitSeq = String.valueOf(opt.getSeq());
            DCAPAdapterSession dcapAdapterSession = this.dcapAdapterSessions.get(ackInitSeq);
            if (dcapAdapterSession == null) {
                Log.w("DCAPAdapter.handleInitAckIndicate", "not found DCAP adapter session for indicate: " + indicate.toJson());
            } else if (dcapAdapterSession.directiveType.equals("init")) {
                ackResponse(indicate);
                if (dcapAdapterSession.ocppMsg.getString(2).equals(OcppMessage.RemoteStartTransaction)) {
                    OcppProtocolAgent.getInstance().sendRemoteStartTransactionConf(dcapAdapterSession.ocppMsg.getString(1), "Accepted");
                } else if (dcapAdapterSession.ocppMsg.getString(2).equals(OcppMessage.ReserveNow)) {
                    OcppProtocolAgent.getInstance().sendReserveNowConf(dcapAdapterSession.ocppMsg.getString(1), "Accepted");
                }
                Log.i("DCAPAdapter.handleInitAckIndicate", "destroy an DCAP adapter session: " + dcapAdapterSession.toJson());
                this.dcapAdapterSessions.remove(ackInitSeq);
            } else {
                Log.w("DCAPAdapter.handleInitAckIndicate", "not matched DCAP adapter session: " + dcapAdapterSession.toJson() + " and indicate: " + indicate.toJson());
            }
        } catch (Exception e) {
            Log.w("DCAPAdapter.handleInitAckIndicate", Log.getStackTraceString(e));
        }
    }

    public void handleFinIndicate(DCAPMessage indicate) {
        try {
            ackResponse(indicate);
            CAPMessage cap = (CAPMessage) indicate.getData();
            CAPDirectiveOption opt = cap.getOpt();
            if ("init".equals(opt.getOp())) {
                String nackInitSeq = String.valueOf(opt.getSeq());
                DCAPAdapterSession dcapAdapterSession = this.dcapAdapterSessions.get(nackInitSeq);
                if (dcapAdapterSession == null) {
                    Log.w("DCAPAdapter.handleFinIndicate", "not found DCAP adapter session for indicate: " + indicate.toJson());
                } else if (dcapAdapterSession.directiveType.equals("init")) {
                    if (dcapAdapterSession.ocppMsg.getString(2).equals(OcppMessage.RemoteStartTransaction)) {
                        OcppProtocolAgent.getInstance().sendRemoteStartTransactionConf(dcapAdapterSession.ocppMsg.getString(1), "Rejected");
                    } else if (dcapAdapterSession.ocppMsg.getString(2).equals(OcppMessage.ReserveNow)) {
                        OcppProtocolAgent.getInstance().sendReserveNowConf(dcapAdapterSession.ocppMsg.getString(1), "Rejected");
                    }
                    Log.i("DCAPAdapter.handleFinIndicate", "destroy an DCAP adapter session: " + dcapAdapterSession.toJson());
                    this.dcapAdapterSessions.remove(nackInitSeq);
                } else {
                    Log.w("DCAPAdapter.handleFinIndicate", "not matched DCAP adapter session: " + dcapAdapterSession.toJson() + " and indicate: " + indicate.toJson());
                }
            }
        } catch (Exception e) {
            Log.w("DCAPAdapter.handleFinIndicate", Log.getStackTraceString(e));
        }
    }

    public void handleStopConfirm(DCAPMessage confirm) {
        try {
            CAPMessage cap = (CAPMessage) confirm.getData();
            CAPDirectiveOption opt = cap.getOpt();
            String peerSeq = String.valueOf(opt.getSeq());
            DCAPAdapterSession dcapAdapterSession = this.dcapAdapterSessions.get(peerSeq);
            if (dcapAdapterSession == null) {
                Log.w("DCAPAdapter.handleStopComfirm", "not found DCAP adapter session for confirm: " + confirm.toJson());
                return;
            }
            JSONArray jsonArray = dcapAdapterSession.ocppMsg;
            String op = cap.getOp();
            String action = jsonArray.getString(2);
            if ("ack".equals(op)) {
                if (OcppMessage.RemoteStopTransaction.equals(action)) {
                    OcppProtocolAgent.getInstance().sendRemoteStopTransactionConf(jsonArray.getString(1), "Accepted");
                } else if (OcppMessage.Reset.equals(action)) {
                    OcppProtocolAgent.getInstance().sendResetConf(jsonArray.getString(1), "Accepted");
                    OcppProtocolAgent.getInstance().setResetOnTxStopped(true);
                } else if (OcppMessage.UnlockConnector.equals(action)) {
                    unlockConnector(jsonArray);
                }
                Log.i("DCAPAdapter.handleStopComfirm", "destroy an DCAP adapter session: " + dcapAdapterSession.toJson());
                this.dcapAdapterSessions.remove(peerSeq);
            } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
                if (OcppMessage.RemoteStopTransaction.equals(action)) {
                    OcppProtocolAgent.getInstance().sendRemoteStopTransactionConf(jsonArray.getString(1), "Rejected");
                } else if (OcppMessage.Reset.equals(action)) {
                    OcppProtocolAgent.getInstance().sendResetConf(jsonArray.getString(1), "Rejected");
                } else if (OcppMessage.UnlockConnector.equals(action)) {
                    OcppProtocolAgent.getInstance().sendUnlockConnectorConf(jsonArray.getString(1), UnlockStatus.UnlockFailed);
                }
                Log.i("DCAPAdapter.handleStopComfirm", "destroy an DCAP adapter session: " + dcapAdapterSession.toJson());
                this.dcapAdapterSessions.remove(peerSeq);
            }
        } catch (Exception e) {
            Log.w("DCAPAdapter.handleStopConfirm", Log.getStackTraceString(e));
        }
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
        CAPMessage requstCap = (CAPMessage) dcapAdapterSession.dcapMsg.getData();
        String conditionId = requstCap.getOpt().getCondition_id();
        if ("ack".equals(op)) {
            ConditionDirective.CONDITION_USER_STOP.equals(conditionId);
        } else if (CAPMessage.DIRECTIVE_NACK.equals(op) && ConditionDirective.CONDITION_USER_STOP.equals(conditionId)) {
            JSONArray ocppMsg = dcapAdapterSession.getOcppMsg();
            if (OcppMessage.StartTransaction.equals(ocppMsg.optString(2, null))) {
                DCAPMessage conditionRequest = dcapAdapterSession.getDcapMsg();
                String port = ((CAPMessage) conditionRequest.getData()).getOpt().getPort_id();
                if (TextUtils.isEmpty(port)) {
                    port = "1";
                }
                stopCharge(port, dcapAdapterSession.ocppMsg);
            }
        }
        Log.i("DCAPAdapter.handleConditionConfirm", "destroy DCAP adapter session: " + dcapAdapterSession.toJson());
        this.dcapAdapterSessions.remove(peerSeq);
    }

    public void handleSetConfirm(DCAPMessage confirm) {
        try {
            CAPMessage cap = (CAPMessage) confirm.getData();
            CAPDirectiveOption opt = cap.getOpt();
            String peerSeq = String.valueOf(opt.getSeq());
            DCAPAdapterSession dcapAdapterSession = this.dcapAdapterSessions.get(peerSeq);
            if (dcapAdapterSession == null) {
                Log.w("DCAPAdapter.handleSetConfirm", "not found DCAP adapter session for confirm: " + confirm.toJson());
                return;
            }
            JSONArray jsonArray = dcapAdapterSession.ocppMsg;
            String op = cap.getOp();
            String action = dcapAdapterSession.ocppMsg.getString(2);
            if ("ack".equals(op)) {
                if (OcppMessage.UnlockConnector.equals(action)) {
                    OcppProtocolAgent.getInstance().sendUnlockConnectorConf(jsonArray.getString(1), UnlockStatus.Unlocked);
                }
            } else if (CAPMessage.DIRECTIVE_NACK.equals(op) && OcppMessage.UnlockConnector.equals(action)) {
                OcppProtocolAgent.getInstance().sendUnlockConnectorConf(jsonArray.getString(1), UnlockStatus.UnlockFailed);
            }
            Log.i("DCAPAdapter.handleSetConfirm", "destroy DCAP adapter session: " + dcapAdapterSession.toJson());
            this.dcapAdapterSessions.remove(peerSeq);
        } catch (Exception e) {
            Log.w("DCAPAdapter.handleSetConfirm", Log.getStackTraceString(e));
        }
    }

    public void handleResetReq(JSONArray jsonArray) {
        try {
            ResetReq restarReq = new ResetReq().fromJson(jsonArray.getJSONObject(3).toString());
            String type = restarReq.getType();
            if (ResetType.Soft.equals(type)) {
                PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus("1");
                if (portStatus != null && CHARGE_STATUS.CHARGING.equals(portStatus.getChargeStatus())) {
                    stopCharge("1", jsonArray);
                } else {
                    OcppProtocolAgent.getInstance().sendResetConf(jsonArray.getString(1), "Accepted");
                    reset();
                }
            } else if (ResetType.Hard.equals(type)) {
                OcppProtocolAgent.getInstance().sendResetConf(jsonArray.getString(1), "Accepted");
                reset();
            }
        } catch (Exception e) {
            Log.w("DCAPAdapter.handleRestartReq", Log.getStackTraceString(e));
        }
    }

    public void handleUnlockConnectorReq(JSONArray jsonArray) {
        try {
            UnlockConnectorReq unlockConnectorConf = new UnlockConnectorReq().fromJson(jsonArray.getJSONObject(3).toString());
            String port = String.valueOf(unlockConnectorConf.getConnectorId());
            PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(port);
            if (CHARGE_STATUS.CHARGING.equals(portStatus.getChargeStatus())) {
                stopCharge(port, jsonArray);
            } else {
                unlockConnector(jsonArray);
            }
        } catch (Exception e) {
            Log.w("DCAPAdapter.handleUnlockConnectorReq", Log.getStackTraceString(e));
        }
    }

    private void stopCharge(String port, JSONArray jsonArray) {
        try {
            OcppChargeSession chargeSession = OcppProtocolAgent.getInstance().getChargeSession(port);
            if (chargeSession != null) {
                Log.i("DCAPAdapter.stopCharge", "found charge session: " + chargeSession.toJson());
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
                dcapAdapterSession.ocppMsg = jsonArray;
                dcapAdapterSession.ocppTimestamp = System.currentTimeMillis();
                dcapAdapterSession.dcapMsg = stopRequest;
                dcapAdapterSession.directiveType = "stop";
                Log.i("DCAPAdapter.stopCharge", "create DCAP adapter session: " + dcapAdapterSession.toJson());
                this.dcapAdapterSessions.put(String.valueOf(stopRequest.getSeq()), dcapAdapterSession);
            } else {
                Log.w("DCAPAdapter.stopCharge", "chargeSession is null");
            }
        } catch (Exception e) {
            Log.w("DCAPAdapter.stopCharge", Log.getStackTraceString(e));
        }
    }

    public static void reset() {
        try {
            OcppProtocolAgent.getInstance().webSocketClient.close();
            HashMap<String, Object> values = new HashMap<>();
            values.put("opr", "reboot");
            CAPDirectiveOption opt = new CAPDirectiveOption();
            opt.setSet_id(SetDirective.SET_ID_DEVICE);
            SetDirective set = new SetDirective();
            set.setValues(values);
            String from = "server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform();
            DCAPMessage setRequest = createRequest(from, "set", opt, set);
            DCAPProxy.getInstance().sendRequest(setRequest);
        } catch (Exception e) {
            Log.w("DCAPAdapter.reset", Log.getStackTraceString(e));
        }
    }

    private void unlockConnector(JSONArray jsonArray) {
        try {
            UnlockConnectorReq unlockConnectorConf = new UnlockConnectorReq().fromJson(jsonArray.getJSONObject(3).toString());
            String port = String.valueOf(unlockConnectorConf.getConnectorId());
            HashMap<String, Object> values = new HashMap<>();
            LOCK_STATUS status = ChargeStatusCacheProvider.getInstance().getPortLockStatus(port);
            if (!LOCK_STATUS.fault.equals(status)) {
                values.put(ContentDB.ChargeTable.PORT, port);
                values.put("opr", SetDirective.OPR_UNLOCK);
                CAPDirectiveOption opt = new CAPDirectiveOption();
                opt.setSet_id(SetDirective.SET_ID_PORT_GUNLOCK);
                SetDirective set = new SetDirective();
                set.setValues(values);
                String from = "server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform();
                DCAPMessage setRequest = createRequest(from, "set", opt, set);
                DCAPProxy.getInstance().sendRequest(setRequest);
                DCAPAdapterSession dcapAdapterSession = new DCAPAdapterSession(null);
                dcapAdapterSession.ocppMsg = jsonArray;
                dcapAdapterSession.ocppTimestamp = System.currentTimeMillis();
                dcapAdapterSession.dcapMsg = setRequest;
                dcapAdapterSession.directiveType = "set";
                Log.i("DCAPAdapter.unlockConnector", "create DCAP adapter session: " + dcapAdapterSession.toJson());
                this.dcapAdapterSessions.put(String.valueOf(setRequest.getSeq()), dcapAdapterSession);
            } else {
                OcppProtocolAgent.getInstance().sendUnlockConnectorConf(jsonArray.getString(1), UnlockStatus.UnlockFailed);
            }
        } catch (Exception e) {
            Log.w("DCAPAdapter.unlockConnector", Log.getStackTraceString(e));
        }
    }

    public static DCAPMessage createRequest(String from, String op, CAPDirectiveOption opt, Object directive) {
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

    private boolean ackResponse(DCAPMessage indicate) {
        AckDirective ack = new AckDirective();
        return DCAPProxy.getInstance().sendResponse(DCAPProxy.getInstance().createCAPResponseByIndcate(indicate), "cap", "ack", ack);
    }
}
