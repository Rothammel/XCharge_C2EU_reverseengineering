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
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.CHARGE_INIT_TYPE;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.CHARGE_STATUS;
import com.xcharge.charger.data.bean.type.CHARGE_USER_TYPE;
import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.bean.type.USER_TC_TYPE;
import com.xcharge.charger.data.p004db.ContentDB;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.p006ui.p009c2.activity.BaseActivity;
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

public class DCAPAdapter {
    public static final long DEFAULT_SESSION_TIMEOUT = 15000;

    /* renamed from: sn */
    private static String f127sn = null;
    private Context context = null;
    private HashMap<String, DCAPAdapterSession> dcapAdapterSessions = null;

    private static class DCAPAdapterSession extends JsonBean<DCAPAdapterSession> {
        DCAPMessage dcapMsg;
        String directiveType;
        JSONArray ocppMsg;
        long ocppTimestamp;

        private DCAPAdapterSession() {
            this.ocppMsg = null;
            this.dcapMsg = null;
            this.directiveType = null;
            this.ocppTimestamp = 0;
        }

        /* synthetic */ DCAPAdapterSession(DCAPAdapterSession dCAPAdapterSession) {
            this();
        }

        public JSONArray getOcppMsg() {
            return this.ocppMsg;
        }

        public void setOcppMsg(JSONArray ocppMsg2) {
            this.ocppMsg = ocppMsg2;
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

        public long getOcppTimestamp() {
            return this.ocppTimestamp;
        }

        public void setOcppTimestamp(long ocppTimestamp2) {
            this.ocppTimestamp = ocppTimestamp2;
        }
    }

    public void init(Context context2) {
        this.context = context2;
        f127sn = HardwareStatusCacheProvider.getInstance().getSn();
        this.dcapAdapterSessions = new HashMap<>();
    }

    public void destroy() {
        this.dcapAdapterSessions.clear();
    }

    public void maintainSession() {
        try {
            if (this.dcapAdapterSessions.size() != 0) {
                long nowTimestamp = System.currentTimeMillis();
                Iterator<Map.Entry<String, DCAPAdapterSession>> it = this.dcapAdapterSessions.entrySet().iterator();
                while (it.hasNext()) {
                    DCAPAdapterSession session = (DCAPAdapterSession) it.next().getValue();
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
                        it.remove();
                    }
                }
            }
        } catch (Exception e) {
            Log.w("DCAPAdapter.maintainSession", Log.getStackTraceString(e));
        }
    }

    public void handleRemoteStartTransactionReq(JSONArray jsonArray) {
        try {
            RemoteStartTransactionReq remoteStartTransactionReq = (RemoteStartTransactionReq) new RemoteStartTransactionReq().fromJson(jsonArray.getJSONObject(3).toString());
            String port = String.valueOf(remoteStartTransactionReq.getConnectorId());
            ChargingProfile chargingProfile = remoteStartTransactionReq.getChargingProfile();
            if (chargingProfile != null) {
                HashMap<String, ArrayList<ChargingProfile>> txChargingProfiles = OcppProtocolAgent.getInstance().getChargeSession(port).getTxChargingProfiles();
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
            init.setDevice_id("sn/" + f127sn);
            init.setPort(port);
            init.setCharge_platform(CHARGE_PLATFORM.ocpp);
            String connectionTimeOut = OcppProtocolAgent.getInstance().getOcppConfig().getMaps().get(OcppMessage.ConnectionTimeOut);
            if (TextUtils.isEmpty(connectionTimeOut) || !TextUtils.isDigitsOnly(connectionTimeOut) || Integer.parseInt(connectionTimeOut) <= 0) {
                init.setTimeout_plugin(RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalChargeCancel());
            } else {
                init.setTimeout_plugin(Integer.parseInt(connectionTimeOut));
            }
            init.setTimeout_start(-1);
            init.setTimeout_plugout(RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalDelayStart());
            DCAPMessage initRequest = createRequest("user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode, "init", opt, init);
            DCAPProxy.getInstance().sendRequest(initRequest);
            DCAPAdapterSession dcapAdapterSession = new DCAPAdapterSession((DCAPAdapterSession) null);
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
            String transactionId = String.valueOf(((RemoteStopTransactionReq) new RemoteStopTransactionReq().fromJson(jsonArray.getJSONObject(3).toString())).getTransactionId());
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
            ReserveNowReq reserveNowReq = (ReserveNowReq) new ReserveNowReq().fromJson(jsonArray.getJSONObject(3).toString());
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
            init.setDevice_id("sn/" + f127sn);
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
            DCAPMessage initRequest = createRequest("user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode, "init", opt, init);
            DCAPProxy.getInstance().sendRequest(initRequest);
            DCAPAdapterSession dcapAdapterSession = new DCAPAdapterSession((DCAPAdapterSession) null);
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
            CancelReservationReq cancelReservationReq = (CancelReservationReq) new CancelReservationReq().fromJson(jsonArray.getJSONObject(3).toString());
            String chargeId = String.valueOf(cancelReservationReq.getReservationId());
            OcppChargeSession chargeSession = OcppProtocolAgent.getInstance().getChargeSession(OcppProtocolAgent.getInstance().getPort(chargeId));
            if (chargeSession != null && chargeId.equals(chargeSession.getCharge_id())) {
                CAPDirectiveOption opt = new CAPDirectiveOption();
                opt.setCharge_id(String.valueOf(cancelReservationReq.getReservationId()));
                FinDirective fin = new FinDirective();
                fin.setFin_mode(FIN_MODE.cancel);
                fin.setError((ErrorCode) null);
                String userType = chargeSession.getUser_type();
                DCAPProxy.getInstance().sendRequest(createRequest("user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + chargeSession.getUser_code(), "fin", opt, fin));
            }
        } catch (Exception e) {
            Log.w("DCAPAdapter.handleCancelReservationReq", Log.getStackTraceString(e));
        }
    }

    public void setMaxChargeEnergy(JSONArray request) {
        try {
            JSONObject attach = request.getJSONObject(4);
            String chargeId = attach.getString("value");
            String port = attach.getString(ContentDB.ChargeTable.PORT);
            double power = new BigDecimal(((double) Integer.parseInt(OcppProtocolAgent.getInstance().getOcppConfig().getMaps().get(OcppMessage.MaxEnergyOnInvalidId))) / 1000.0d).setScale(2, 4).doubleValue();
            ConditionDirective condition = new ConditionDirective();
            condition.setUserTcType(USER_TC_TYPE.power);
            condition.setUserTcValue(String.format(BaseActivity.TWODP, new Object[]{Double.valueOf(power)}));
            CAPDirectiveOption opt = new CAPDirectiveOption();
            opt.setCharge_id(chargeId);
            opt.setCondition_id(ConditionDirective.CONDITION_USER_STOP);
            opt.setPort_id(port);
            String userType = CHARGE_USER_TYPE.ocpp.getUserType();
            DCAPMessage conditionRequest = createRequest("user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + ((StartTransactionReq) new StartTransactionReq().fromJson(request.getJSONObject(3).toString())).getIdTag(), CAPMessage.DIRECTIVE_CONDITION, opt, condition);
            DCAPProxy.getInstance().sendRequest(conditionRequest);
            DCAPAdapterSession dcapAdapterSession = new DCAPAdapterSession((DCAPAdapterSession) null);
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
            String ackInitSeq = String.valueOf(((CAPMessage) indicate.getData()).getOpt().getSeq());
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
            CAPDirectiveOption opt = ((CAPMessage) indicate.getData()).getOpt();
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
            String peerSeq = String.valueOf(cap.getOpt().getSeq());
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
        String peerSeq = String.valueOf(cap.getOpt().getSeq());
        DCAPAdapterSession dcapAdapterSession = this.dcapAdapterSessions.get(peerSeq);
        if (dcapAdapterSession == null) {
            Log.w("DCAPAdapter.handleConditionConfirm", "not found DCAP adapter session for confirm: " + confirm.toJson());
            return;
        }
        String op = cap.getOp();
        String conditionId = ((CAPMessage) dcapAdapterSession.dcapMsg.getData()).getOpt().getCondition_id();
        if ("ack".equals(op)) {
            ConditionDirective.CONDITION_USER_STOP.equals(conditionId);
        } else if (CAPMessage.DIRECTIVE_NACK.equals(op) && ConditionDirective.CONDITION_USER_STOP.equals(conditionId) && OcppMessage.StartTransaction.equals(dcapAdapterSession.getOcppMsg().optString(2, (String) null))) {
            String port = ((CAPMessage) dcapAdapterSession.getDcapMsg().getData()).getOpt().getPort_id();
            if (TextUtils.isEmpty(port)) {
                port = "1";
            }
            stopCharge(port, dcapAdapterSession.ocppMsg);
        }
        Log.i("DCAPAdapter.handleConditionConfirm", "destroy DCAP adapter session: " + dcapAdapterSession.toJson());
        this.dcapAdapterSessions.remove(peerSeq);
    }

    public void handleSetConfirm(DCAPMessage confirm) {
        try {
            CAPMessage cap = (CAPMessage) confirm.getData();
            String peerSeq = String.valueOf(cap.getOpt().getSeq());
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
            String type = ((ResetReq) new ResetReq().fromJson(jsonArray.getJSONObject(3).toString())).getType();
            if (ResetType.Soft.equals(type)) {
                PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus("1");
                if (portStatus == null || !CHARGE_STATUS.CHARGING.equals(portStatus.getChargeStatus())) {
                    OcppProtocolAgent.getInstance().sendResetConf(jsonArray.getString(1), "Accepted");
                    reset();
                    return;
                }
                stopCharge("1", jsonArray);
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
            String port = String.valueOf(((UnlockConnectorReq) new UnlockConnectorReq().fromJson(jsonArray.getJSONObject(3).toString())).getConnectorId());
            if (CHARGE_STATUS.CHARGING.equals(ChargeStatusCacheProvider.getInstance().getPortStatus(port).getChargeStatus())) {
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
                DCAPMessage stopRequest = createRequest("user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode, "stop", opt, new StopDirective());
                DCAPProxy.getInstance().sendRequest(stopRequest);
                DCAPAdapterSession dcapAdapterSession = new DCAPAdapterSession((DCAPAdapterSession) null);
                dcapAdapterSession.ocppMsg = jsonArray;
                dcapAdapterSession.ocppTimestamp = System.currentTimeMillis();
                dcapAdapterSession.dcapMsg = stopRequest;
                dcapAdapterSession.directiveType = "stop";
                Log.i("DCAPAdapter.stopCharge", "create DCAP adapter session: " + dcapAdapterSession.toJson());
                this.dcapAdapterSessions.put(String.valueOf(stopRequest.getSeq()), dcapAdapterSession);
                return;
            }
            Log.w("DCAPAdapter.stopCharge", "chargeSession is null");
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
            DCAPProxy.getInstance().sendRequest(createRequest("server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform(), "set", opt, set));
        } catch (Exception e) {
            Log.w("DCAPAdapter.reset", Log.getStackTraceString(e));
        }
    }

    private void unlockConnector(JSONArray jsonArray) {
        try {
            String port = String.valueOf(((UnlockConnectorReq) new UnlockConnectorReq().fromJson(jsonArray.getJSONObject(3).toString())).getConnectorId());
            HashMap<String, Object> values = new HashMap<>();
            if (!LOCK_STATUS.fault.equals(ChargeStatusCacheProvider.getInstance().getPortLockStatus(port))) {
                values.put(ContentDB.ChargeTable.PORT, port);
                values.put("opr", SetDirective.OPR_UNLOCK);
                CAPDirectiveOption opt = new CAPDirectiveOption();
                opt.setSet_id(SetDirective.SET_ID_PORT_GUNLOCK);
                SetDirective set = new SetDirective();
                set.setValues(values);
                DCAPMessage setRequest = createRequest("server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform(), "set", opt, set);
                DCAPProxy.getInstance().sendRequest(setRequest);
                DCAPAdapterSession dcapAdapterSession = new DCAPAdapterSession((DCAPAdapterSession) null);
                dcapAdapterSession.ocppMsg = jsonArray;
                dcapAdapterSession.ocppTimestamp = System.currentTimeMillis();
                dcapAdapterSession.dcapMsg = setRequest;
                dcapAdapterSession.directiveType = "set";
                Log.i("DCAPAdapter.unlockConnector", "create DCAP adapter session: " + dcapAdapterSession.toJson());
                this.dcapAdapterSessions.put(String.valueOf(setRequest.getSeq()), dcapAdapterSession);
                return;
            }
            OcppProtocolAgent.getInstance().sendUnlockConnectorConf(jsonArray.getString(1), UnlockStatus.UnlockFailed);
        } catch (Exception e) {
            Log.w("DCAPAdapter.unlockConnector", Log.getStackTraceString(e));
        }
    }

    public static DCAPMessage createRequest(String from, String op, CAPDirectiveOption opt, Object directive) {
        CAPMessage requestCap = new CAPMessage();
        DCAPMessage request = new DCAPMessage();
        request.setFrom(from);
        request.setTo("device:sn/" + f127sn);
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
        return DCAPProxy.getInstance().sendResponse(DCAPProxy.getInstance().createCAPResponseByIndcate(indicate), "cap", "ack", new AckDirective());
    }
}
