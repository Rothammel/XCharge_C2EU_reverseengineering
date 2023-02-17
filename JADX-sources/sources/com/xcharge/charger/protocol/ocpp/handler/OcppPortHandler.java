package com.xcharge.charger.protocol.ocpp.handler;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.reflect.TypeToken;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.cap.CAPDirectiveOption;
import com.xcharge.charger.core.api.bean.cap.StopDirective;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.setting.PortSetting;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.CHARGE_STATUS;
import com.xcharge.charger.data.bean.type.CHARGE_STOP_CAUSE;
import com.xcharge.charger.data.bean.type.CHARGE_USER_TYPE;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.data.proxy.PortChargeStatusObserver;
import com.xcharge.charger.data.proxy.PortStatusObserver;
import com.xcharge.charger.device.c2.bean.PortRuntimeData;
import com.xcharge.charger.device.c2.service.C2DeviceProxy;
import com.xcharge.charger.protocol.ocpp.bean.NowLimitContext;
import com.xcharge.charger.protocol.ocpp.bean.OcppConfig;
import com.xcharge.charger.protocol.ocpp.bean.OcppMessage;
import com.xcharge.charger.protocol.ocpp.bean.cloud.ChangeAvailabilityReq;
import com.xcharge.charger.protocol.ocpp.bean.cloud.RemoteStopTransactionReq;
import com.xcharge.charger.protocol.ocpp.bean.cloud.ReserveNowReq;
import com.xcharge.charger.protocol.ocpp.bean.cloud.SetChargingProfileReq;
import com.xcharge.charger.protocol.ocpp.bean.cloud.StartTransactionConf;
import com.xcharge.charger.protocol.ocpp.bean.cloud.StopTransactionConf;
import com.xcharge.charger.protocol.ocpp.bean.device.ChangeAvailabilityConf;
import com.xcharge.charger.protocol.ocpp.bean.device.MeterValuesReq;
import com.xcharge.charger.protocol.ocpp.bean.device.StartTransactionReq;
import com.xcharge.charger.protocol.ocpp.bean.device.StatusNotificationReq;
import com.xcharge.charger.protocol.ocpp.bean.device.StopTransactionReq;
import com.xcharge.charger.protocol.ocpp.bean.types.AvailabilityStatus;
import com.xcharge.charger.protocol.ocpp.bean.types.AvailabilityType;
import com.xcharge.charger.protocol.ocpp.bean.types.ChargePointErrorCode;
import com.xcharge.charger.protocol.ocpp.bean.types.ChargePointStatus;
import com.xcharge.charger.protocol.ocpp.bean.types.ChargingProfile;
import com.xcharge.charger.protocol.ocpp.bean.types.ChargingProfileKindType;
import com.xcharge.charger.protocol.ocpp.bean.types.ChargingProfilePurposeType;
import com.xcharge.charger.protocol.ocpp.bean.types.ChargingSchedule;
import com.xcharge.charger.protocol.ocpp.bean.types.ChargingSchedulePeriod;
import com.xcharge.charger.protocol.ocpp.bean.types.Location;
import com.xcharge.charger.protocol.ocpp.bean.types.Measurand;
import com.xcharge.charger.protocol.ocpp.bean.types.MeterValue;
import com.xcharge.charger.protocol.ocpp.bean.types.Phase;
import com.xcharge.charger.protocol.ocpp.bean.types.ReadingContext;
import com.xcharge.charger.protocol.ocpp.bean.types.Reason;
import com.xcharge.charger.protocol.ocpp.bean.types.RecurrencyKindType;
import com.xcharge.charger.protocol.ocpp.bean.types.SampledValue;
import com.xcharge.charger.protocol.ocpp.bean.types.UnitOfMeasure;
import com.xcharge.charger.protocol.ocpp.router.DCAPAdapter;
import com.xcharge.charger.protocol.ocpp.router.OcppDCAPGateway;
import com.xcharge.charger.protocol.ocpp.session.OcppChargeSession;
import com.xcharge.charger.protocol.ocpp.session.OcppRequestSession;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.HandlerTimer;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.TimeUtils;
import it.sauronsoftware.ftp4j.FTPCodes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class OcppPortHandler {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STATUS = null;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE = null;
    public static final int MSG_CHARGING_PROFILE_TIMER = 73746;
    public static final int MSG_CLOCK_ALIGNED_TIMER = 73744;
    public static final int MSG_CLOUD_REQUEST = 73735;
    public static final int MSG_CLOUD_RESPONSE = 73736;
    public static final int MSG_METER_VALUES_TIMER = 73737;
    public static final int MSG_REPORT_HISTORY_CHARGE_TIMER = 73745;
    public static final int MSG_REQUEST_SEND_FAIL = 73732;
    public static final int MSG_REQUEST_SEND_OK = 73731;
    public static final int MSG_REQUEST_TIMEOUT = 73733;
    public static final int MSG_REQUSET_RESEND = 73734;
    public static final int MSG_SERVICE_ACTIVE = 73729;
    public static final int MSG_SERVICE_DEACTIVE = 73730;
    public static final int REPORTED_COMPLETE = 1;
    public static final int REPORTED_NOT = 0;
    public static final int REPORTED_PROCESS = 2;
    public static final int TIMEOUT_REPORT_HISTORY_CHARGE = 60;
    public static final int TINE_INTERVAL = 5;
    private HandlerThread thread = null;
    private MsgHandler handler = null;
    private HandlerTimer handlerTimer = null;
    private Context context = null;
    private String port = null;
    private CHARGE_STATUS status = CHARGE_STATUS.IDLE;
    private OcppChargeSession chargeSession = null;
    private PortStatusObserver portStatusObserver = null;
    private PortChargeStatusObserver portChargeStatusObserver = null;
    private PortStatusObserver portRecoverableFaultObserver = null;
    private String currentError = null;
    private String currentStatus = null;

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STATUS() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STATUS;
        if (iArr == null) {
            iArr = new int[CHARGE_STATUS.valuesCustom().length];
            try {
                iArr[CHARGE_STATUS.CHARGE_START_WAITTING.ordinal()] = 2;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[CHARGE_STATUS.CHARGE_STOP_WAITTING.ordinal()] = 4;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[CHARGE_STATUS.CHARGING.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[CHARGE_STATUS.IDLE.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STATUS = iArr;
        }
        return iArr;
    }

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE;
        if (iArr == null) {
            iArr = new int[CHARGE_STOP_CAUSE.valuesCustom().length];
            try {
                iArr[CHARGE_STOP_CAUSE.car.ordinal()] = 7;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.fault.ordinal()] = 12;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.full.ordinal()] = 8;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.local_user.ordinal()] = 3;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.no_balance.ordinal()] = 9;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.plugout.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.reboot.ordinal()] = 11;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.remote_user.ordinal()] = 4;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.system_user.ordinal()] = 5;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.undefined.ordinal()] = 1;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.user.ordinal()] = 2;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.user_set.ordinal()] = 10;
            } catch (NoSuchFieldError e12) {
            }
            $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE = iArr;
        }
        return iArr;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class MsgHandler extends Handler {
        public MsgHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int interval;
            try {
                switch (msg.what) {
                    case 73729:
                        Log.i("OcppPortHandler.handleMessage", "service actived !!! port: " + OcppPortHandler.this.port);
                        OcppPortHandler.this.handlerTimer.startTimer(60000L, 73745, null);
                        break;
                    case 73730:
                        Log.i("OcppPortHandler.handleMessage", "service deactived !!! port: " + OcppPortHandler.this.port);
                        OcppPortHandler.this.clearPortActiveStatus();
                        break;
                    case 73731:
                        JSONArray jSONArray = (JSONArray) msg.obj;
                        break;
                    case 73732:
                        OcppPortHandler.this.handleFailedRequest((JSONArray) msg.obj);
                        break;
                    case 73733:
                        JSONArray jsonArray = (JSONArray) msg.obj;
                        Log.w("OcppPortHandler.handleMessage", "send ocpp request timeout: " + jsonArray);
                        OcppPortHandler.this.handleFailedRequest(jsonArray);
                        break;
                    case 73734:
                        JSONArray jSONArray2 = (JSONArray) msg.obj;
                        break;
                    case 73735:
                        OcppPortHandler.this.handleRequest((JSONArray) msg.obj);
                        break;
                    case 73736:
                        OcppRequestSession ocppRequestSession = (OcppRequestSession) msg.obj;
                        OcppPortHandler.this.handleResponse(ocppRequestSession.getRequest(), ocppRequestSession.getResponse());
                        break;
                    case 73737:
                        OcppPortHandler.this.sendMeterValuesReq(false);
                        String meterValueSampleInterval = OcppPortHandler.this.getOcppConfig().getMaps().get(OcppMessage.MeterValueSampleInterval);
                        if (!TextUtils.isEmpty(meterValueSampleInterval) && TextUtils.isDigitsOnly(meterValueSampleInterval) && (interval = Integer.parseInt(meterValueSampleInterval)) > 0) {
                            OcppPortHandler.this.handlerTimer.startTimer(interval * 1000, 73737, null);
                            break;
                        }
                        break;
                    case 73744:
                        int clovkAlignedInterval = ((Integer) msg.obj).intValue();
                        OcppPortHandler.this.sendMeterValuesReq(true);
                        OcppPortHandler.this.handlerTimer.startTimer(clovkAlignedInterval * 1000, 73744, Integer.valueOf(clovkAlignedInterval));
                        break;
                    case 73745:
                        OcppPortHandler.this.reportHistoryChargeRequest();
                        OcppPortHandler.this.handlerTimer.startTimer(60000L, 73745, null);
                        break;
                    case 73746:
                        long startTime = ((Long) msg.obj).longValue();
                        OcppPortHandler.this.executeChargingProfile(startTime, System.currentTimeMillis() / 1000);
                        break;
                    case PortChargeStatusObserver.MSG_PORT_CHARGE_STATUS_CHANGE /* 131073 */:
                        Uri uri = (Uri) msg.obj;
                        Log.i("OcppPortHandler.handleMessage", "port charge status changed, port: " + OcppPortHandler.this.port + ", uri: " + uri.toString());
                        OcppPortHandler.this.handlePortChargeStatusChanged(uri);
                        break;
                    case PortStatusObserver.MSG_PORT_STATUS_CHANGE /* 139265 */:
                        Uri uri2 = (Uri) msg.obj;
                        if (uri2.getPath().contains("ports/fault/")) {
                            Log.d("OcppPortHandler.handleMessage", "port recoverable faults changed, port: " + OcppPortHandler.this.port + ", uri: " + uri2.toString());
                            OcppPortHandler.this.handlePortRecoverableFaultChanged(uri2);
                            break;
                        } else {
                            Log.d("OcppPortHandler.handleMessage", "port status changed, port: " + OcppPortHandler.this.port + ", uri: " + uri2.toString());
                            OcppPortHandler.this.handlePortStatusChanged(uri2);
                            break;
                        }
                }
            } catch (Exception e) {
                Log.e("OcppPortHandler.handleMessage", "except: " + Log.getStackTraceString(e));
                LogUtils.syslog("OcppPortHandler handleMessage exception: " + Log.getStackTraceString(e));
            }
            super.handleMessage(msg);
        }
    }

    public void init(Context context, String port, OcppProtocolAgent protocolHandler) {
        this.context = context;
        this.port = port;
        this.thread = new HandlerThread("OcppPortHandler#" + this.port, 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(context);
        this.portRecoverableFaultObserver = new PortStatusObserver(this.context, this.port, this.handler);
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor("ports/fault/" + this.port), true, this.portRecoverableFaultObserver);
        this.portStatusObserver = new PortStatusObserver(this.context, this.port, this.handler);
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor("ports/" + this.port), true, this.portStatusObserver);
        this.portChargeStatusObserver = new PortChargeStatusObserver(this.context, this.port, this.handler);
        this.context.getContentResolver().registerContentObserver(ChargeStatusCacheProvider.getInstance().getUriFor("ports/" + this.port), true, this.portChargeStatusObserver);
    }

    public void destroy() {
        this.context.getContentResolver().unregisterContentObserver(this.portChargeStatusObserver);
        this.context.getContentResolver().unregisterContentObserver(this.portStatusObserver);
        this.context.getContentResolver().unregisterContentObserver(this.portRecoverableFaultObserver);
        this.handlerTimer.destroy();
        this.handler.removeMessages(73729);
        this.handler.removeMessages(73730);
        this.handler.removeMessages(73731);
        this.handler.removeMessages(73732);
        this.handler.removeMessages(73733);
        this.handler.removeMessages(73734);
        this.handler.removeMessages(73735);
        this.handler.removeMessages(73736);
        this.handler.removeMessages(73737);
        this.handler.removeMessages(73744);
        this.handler.removeMessages(73745);
        this.handler.removeMessages(73746);
        this.handler.removeMessages(PortChargeStatusObserver.MSG_PORT_CHARGE_STATUS_CHANGE);
        this.handler.removeMessages(PortStatusObserver.MSG_PORT_STATUS_CHANGE);
        this.thread.quit();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void clearPortActiveStatus() {
        this.currentError = null;
        this.currentStatus = null;
        this.handlerTimer.stopTimer(73745);
        this.handler.removeMessages(73731);
        this.handler.removeMessages(73732);
        this.handler.removeMessages(73733);
        this.handler.removeMessages(73734);
        this.handler.removeMessages(73735);
        this.handler.removeMessages(73736);
    }

    public Message obtainMessage(int what) {
        return this.handler.obtainMessage(what);
    }

    public Message obtainMessage(int what, Object obj) {
        return this.handler.obtainMessage(what, obj);
    }

    public boolean sendMessage(Message msg) {
        return this.handler.sendMessage(msg);
    }

    public OcppChargeSession getChargeSession() {
        if (this.chargeSession == null) {
            this.chargeSession = new OcppChargeSession();
        }
        return this.chargeSession;
    }

    private void clearChargeSession() {
        this.chargeSession = null;
    }

    public String getPort(String chargeId) {
        OcppChargeSession chargeSession = getChargeSession();
        if (chargeId.equals(chargeSession.getCharge_id())) {
            return this.port;
        }
        return null;
    }

    public boolean hasCharge(String chargeId) {
        String charge = getChargeSession().getCharge_id();
        return chargeId.equals(charge);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleFailedRequest(JSONArray request) {
        try {
            String string = request.getString(2);
            switch (string.hashCode()) {
                case 77777212:
                    if (string.equals(OcppMessage.StartTransaction)) {
                        JSONObject attach = request.getJSONObject(4);
                        String chargeId = attach.getString("value");
                        ChargeContentProxy.getInstance().setChargeStartReportedFlag(chargeId, 0);
                        int interval = 60;
                        String transactionMessageRetryInterval = getOcppConfig().getMaps().get(OcppMessage.TransactionMessageRetryInterval);
                        if (!TextUtils.isEmpty(transactionMessageRetryInterval) && TextUtils.isDigitsOnly(transactionMessageRetryInterval) && Integer.parseInt(transactionMessageRetryInterval) > 0) {
                            interval = Integer.parseInt(transactionMessageRetryInterval);
                        }
                        if (attach.getBoolean("isHist") || !CHARGE_STATUS.CHARGING.equals(this.status)) {
                            return;
                        }
                        request.put(1, OcppProtocolAgent.getInstance().genSeq());
                        OcppProtocolAgent.getInstance().sendMessage(request, interval * 1000);
                        return;
                    }
                    return;
                case 641037660:
                    if (string.equals(OcppMessage.StopTransaction)) {
                        String chargeId2 = request.getJSONObject(4).getString("value");
                        ChargeContentProxy.getInstance().setChargeStopReportedFlag(chargeId2, 0);
                        return;
                    }
                    return;
                default:
                    return;
            }
        } catch (Exception e) {
            Log.w("OcppPortHandler.handleFailedRequest", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePortRecoverableFaultChanged(Uri uri) {
        List<String> segments = uri.getPathSegments();
        int size = segments.size();
        String last = segments.get(size - 1);
        if (TextUtils.isDigitsOnly(last)) {
            String status = segments.get(size - 2);
            if ("new".equals(status)) {
                sendStatusNotificationReq(false, last);
            } else if ("remove".equals(status)) {
                int errorCode = Integer.parseInt(last);
                if (errorCode == 200) {
                    sendStatusNotificationReq(false, last);
                } else {
                    sendStatusNotificationReq(false, null);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePortStatusChanged(Uri uri) {
        if (uri.getPath().contains("ports/" + this.port + "/plugin")) {
            sendStatusNotificationReq(false, null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePortChargeStatusChanged(Uri uri) {
        int interval;
        int interval2;
        PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port);
        Log.d("OcppPortHandler.handlePortChargeStatusChanged", "now port status: " + portStatus.toJson());
        CHARGE_STATUS chargeStatus = portStatus.getChargeStatus();
        String chargeId = getChargeSession().getCharge_id();
        if (!this.status.equals(chargeStatus)) {
            if (chargeStatus.equals(CHARGE_STATUS.CHARGE_START_WAITTING)) {
                Log.i("OcppPortHandler.handlePortChargeStatusChanged", "enter wait charge status !!!");
                OcppChargeSession chargeSession = getChargeSession();
                String chargeId2 = portStatus.getCharge_id();
                ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(chargeId2);
                if (chargeBill != null) {
                    chargeSession.setCharge_id(chargeId2);
                    chargeSession.setUser_type(chargeBill.getUser_type());
                    chargeSession.setUser_code(chargeBill.getUser_code());
                    chargeSession.setInit_type(chargeBill.getInit_type());
                    chargeSession.setUser_tc_type(chargeBill.getUser_tc_type());
                    chargeSession.setUser_tc_value(chargeBill.getUser_tc_value());
                    chargeSession.setUser_balance(chargeBill.getUser_balance());
                    chargeSession.setIs_free(chargeBill.getIs_free());
                    chargeSession.setBinded_user(chargeBill.getBinded_user());
                    chargeSession.setCharge_platform(chargeBill.getCharge_platform());
                } else {
                    Log.w("OcppPortHandler.handlePortChargeStatusChanged", "failed to query info for charge: " + chargeId2);
                }
            } else if (chargeStatus.equals(CHARGE_STATUS.CHARGING)) {
                Log.i("OcppPortHandler.handlePortChargeStatusChanged", "enter charging status !!!");
                OcppChargeSession chargeSession2 = getChargeSession();
                String chargeId3 = chargeSession2.getCharge_id();
                if (TextUtils.isEmpty(chargeId3)) {
                    chargeId3 = portStatus.getCharge_id();
                    ChargeBill chargeBill2 = ChargeContentProxy.getInstance().getChargeBill(chargeId3);
                    if (chargeBill2 != null || TextUtils.isEmpty(chargeSession2.getUser_type())) {
                        chargeSession2.setCharge_id(chargeId3);
                        chargeSession2.setUser_type(chargeBill2.getUser_type());
                        chargeSession2.setUser_code(chargeBill2.getUser_code());
                        chargeSession2.setInit_type(chargeBill2.getInit_type());
                        chargeSession2.setUser_tc_type(chargeBill2.getUser_tc_type());
                        chargeSession2.setUser_tc_value(chargeBill2.getUser_tc_value());
                        chargeSession2.setUser_balance(chargeBill2.getUser_balance());
                        chargeSession2.setIs_free(chargeBill2.getIs_free());
                        chargeSession2.setBinded_user(chargeBill2.getBinded_user());
                        chargeSession2.setCharge_platform(chargeBill2.getCharge_platform());
                    } else {
                        Log.w("OcppPortHandler.handlePortChargeStatusChanged", "failed to query info for charge: " + chargeId3);
                    }
                }
                ChargeBill cb = ChargeContentProxy.getInstance().getChargeBill(chargeId3);
                sendStartTransactionReq(chargeId3, false);
                String meterValueSampleInterval = getOcppConfig().getMaps().get(OcppMessage.MeterValueSampleInterval);
                if (!TextUtils.isEmpty(meterValueSampleInterval) && TextUtils.isDigitsOnly(meterValueSampleInterval) && (interval2 = Integer.parseInt(meterValueSampleInterval)) > 0) {
                    this.handlerTimer.startTimer(interval2 * 1000, 73737, null);
                }
                String clockAlignedDataInterval = getOcppConfig().getMaps().get(OcppMessage.ClockAlignedDataInterval);
                if (!TextUtils.isEmpty(clockAlignedDataInterval) && TextUtils.isDigitsOnly(clockAlignedDataInterval) && (interval = Integer.parseInt(clockAlignedDataInterval)) > 0) {
                    long current = System.currentTimeMillis() / 1000;
                    this.handlerTimer.startTimer((interval - (current % interval)) * 1000, 73744, Integer.valueOf(interval));
                }
                executeChargingProfile(cb.getStart_time() / 1000, System.currentTimeMillis() / 1000);
            } else if (chargeStatus.equals(CHARGE_STATUS.CHARGE_STOP_WAITTING)) {
                Log.i("OcppPortHandler.getChangedYZXStatus", "enter charge stopped status !!!");
                this.handlerTimer.stopTimer(73737);
                this.handlerTimer.stopTimer(73744);
                this.handlerTimer.stopTimer(73746);
                sendStopTransactionReq(chargeId, false);
            } else if (chargeStatus.equals(CHARGE_STATUS.IDLE)) {
                Log.i("OcppPortHandler.getChangedYZXStatus", "enter idle status !!!");
                this.handlerTimer.stopTimer(73737);
                this.handlerTimer.stopTimer(73744);
                this.handlerTimer.stopTimer(73746);
                if (CHARGE_STATUS.CHARGING.equals(this.status)) {
                    sendStopTransactionReq(chargeId, false);
                }
                clearChargeSession();
            }
            this.status = chargeStatus;
            sendStatusNotificationReq(false, null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void executeChargingProfile(long startTime, long nowTime) {
        try {
            NowLimitContext nowLimitContext = getLimitFromChargingProfiles(startTime, nowTime);
            C2DeviceProxy.getInstance().ajustChargeAmp(this.port, (int) nowLimitContext.getLimit());
            Long nextTime = nowLimitContext.getNextTime();
            if (nextTime != null) {
                long interval = nextTime.longValue() - nowTime;
                this.handlerTimer.startTimer(1000 * interval, 73746, Long.valueOf(startTime));
            }
        } catch (Exception e) {
            Log.w("OcppPortHandler.executeChargingProfile", Log.getStackTraceString(e));
        }
    }

    private NowLimitContext getLimitFromChargingProfiles(long startTime, long nowTime) {
        OcppChargeSession chargeSession = getChargeSession();
        NowLimitContext nowLimitContext = new NowLimitContext();
        nowLimitContext.setLimit(ChargeStatusCacheProvider.getInstance().getAdjustAmp());
        HashMap<String, ArrayList<ChargingProfile>> maxChargingProfiles = OcppProtocolAgent.getInstance().getOcppConfig().getMaxChargingProfiles();
        ChargingProfile maxChargingProfile = getMaxStackLevel(maxChargingProfiles.get("0"), nowTime, null);
        HashMap<String, ArrayList<ChargingProfile>> defChargingProfiles = OcppProtocolAgent.getInstance().getOcppConfig().getDefChargingProfiles();
        ChargingProfile defChargingProfile = getMaxStackLevel(defChargingProfiles.get(this.port), nowTime, Long.valueOf(startTime));
        HashMap<String, ArrayList<ChargingProfile>> txChargingProfiles = chargeSession.getTxChargingProfiles();
        ChargingProfile txChargingProfile = getMaxStackLevel(txChargingProfiles.get(this.port), nowTime, Long.valueOf(startTime));
        if (txChargingProfile == null) {
            if (defChargingProfile == null) {
                if (maxChargingProfile != null) {
                    return oneChargingProfile(maxChargingProfile, nowTime, startTime);
                }
                return nowLimitContext;
            } else if (maxChargingProfile == null) {
                return oneChargingProfile(defChargingProfile, nowTime, startTime);
            } else {
                return twoChargingProfile(maxChargingProfile, defChargingProfile, nowTime, startTime);
            }
        } else if (maxChargingProfile == null) {
            return oneChargingProfile(txChargingProfile, nowTime, startTime);
        } else {
            return twoChargingProfile(maxChargingProfile, txChargingProfile, nowTime, startTime);
        }
    }

    private NowLimitContext oneChargingProfile(ChargingProfile chargingProfile, long nowTime, long startTime) {
        String chargingProfileKind = chargingProfile.getChargingProfileKind();
        String recurrencyKind = chargingProfile.getRecurrencyKind();
        ChargingSchedule chargingSchedule = chargingProfile.getChargingSchedule();
        Integer duration = chargingSchedule.getDuration();
        String startSchedule = chargingSchedule.getStartSchedule();
        ArrayList<ChargingSchedulePeriod> chargingSchedulePeriod = chargingSchedule.getChargingSchedulePeriod();
        int offset = (int) (nowTime - Integer.parseInt(startSchedule));
        NowLimitContext nowLimitContext = new NowLimitContext();
        nowLimitContext.setLimit(ChargeStatusCacheProvider.getInstance().getAdjustAmp());
        int i = 0;
        while (true) {
            if (i >= chargingSchedulePeriod.size()) {
                break;
            } else if (i == chargingSchedulePeriod.size() - 1) {
                if (duration == null) {
                    if (chargingSchedulePeriod.get(i).getStartPeriod() > offset) {
                        i++;
                    } else {
                        nowLimitContext.setLimit(getMinLimit(chargingSchedulePeriod.get(i).getLimit()));
                        break;
                    }
                } else {
                    if (chargingSchedulePeriod.get(i).getStartPeriod() <= offset && offset < duration.intValue()) {
                        nowLimitContext.setLimit(getMinLimit(chargingSchedulePeriod.get(i).getLimit()));
                        if (ChargingProfileKindType.Recurring.equals(chargingProfileKind)) {
                            if (RecurrencyKindType.Daily.equals(recurrencyKind)) {
                                nowLimitContext.setNextTime(Long.valueOf(Long.parseLong(chargingSchedule.getStartSchedule()) + 86400));
                            } else if (RecurrencyKindType.Weekly.equals(recurrencyKind)) {
                                nowLimitContext.setNextTime(Long.valueOf(Long.parseLong(chargingSchedule.getStartSchedule()) + 604800));
                            }
                        }
                    }
                    i++;
                }
            } else {
                if (chargingSchedulePeriod.get(i).getStartPeriod() <= offset && offset < chargingSchedulePeriod.get(i + 1).getStartPeriod()) {
                    nowLimitContext.setLimit(getMinLimit(chargingSchedulePeriod.get(i).getLimit()));
                    nowLimitContext.setNextTime(Long.valueOf(Long.parseLong(chargingSchedule.getStartSchedule()) + chargingSchedulePeriod.get(i + 1).getStartPeriod()));
                    break;
                }
                i++;
            }
        }
        return nowLimitContext;
    }

    private NowLimitContext twoChargingProfile(ChargingProfile maxChargingProfile, ChargingProfile txChargingProfile, long nowTime, long startTime) {
        maxChargingProfile.getChargingProfileKind();
        maxChargingProfile.getRecurrencyKind();
        ChargingSchedule maxChargingSchedule = maxChargingProfile.getChargingSchedule();
        maxChargingSchedule.getDuration();
        String maxStartSchedule = maxChargingSchedule.getStartSchedule();
        String txChargingProfileKind = txChargingProfile.getChargingProfileKind();
        String txRecurrencyKind = txChargingProfile.getRecurrencyKind();
        ChargingSchedule txChargingSchedule = txChargingProfile.getChargingSchedule();
        Integer txDuration = txChargingSchedule.getDuration();
        String txStartSchedule = txChargingSchedule.getStartSchedule();
        ArrayList<ChargingSchedulePeriod> maxChargingSchedulePeriod = maxChargingSchedule.getChargingSchedulePeriod();
        ArrayList<ChargingSchedulePeriod> txChargingSchedulePeriod = txChargingSchedule.getChargingSchedulePeriod();
        long maxOffset = nowTime - Long.parseLong(maxStartSchedule);
        long txOffset = nowTime - Long.parseLong(txStartSchedule);
        NowLimitContext nowLimitContext = new NowLimitContext();
        nowLimitContext.setLimit(ChargeStatusCacheProvider.getInstance().getAdjustAmp());
        int i = 0;
        loop0: while (true) {
            if (i >= maxChargingSchedulePeriod.size()) {
                break;
            }
            for (int j = 0; j < txChargingSchedulePeriod.size(); j++) {
                if (i == maxChargingSchedulePeriod.size() - 1 && j != txChargingSchedulePeriod.size() - 1) {
                    nowLimitContext.setLimit(getMinLimit(maxChargingSchedulePeriod.get(i).getLimit(), txChargingSchedulePeriod.get(j).getLimit()));
                    nowLimitContext.setNextTime(Long.valueOf(Long.parseLong(txStartSchedule) + txChargingSchedulePeriod.get(j + 1).getStartPeriod()));
                } else if ((i != maxChargingSchedulePeriod.size() - 1 && j == txChargingSchedulePeriod.size() - 1) || (i == maxChargingSchedulePeriod.size() - 1 && j == txChargingSchedulePeriod.size() - 1)) {
                    if (txDuration == null) {
                        if (txChargingSchedulePeriod.get(i).getStartPeriod() <= txOffset) {
                            nowLimitContext.setLimit(getMinLimit(maxChargingSchedulePeriod.get(i).getLimit(), txChargingSchedulePeriod.get(j).getLimit()));
                            break loop0;
                        }
                    } else if (txChargingSchedulePeriod.get(i).getStartPeriod() <= txOffset && txOffset < txDuration.intValue()) {
                        nowLimitContext.setLimit(getMinLimit(maxChargingSchedulePeriod.get(i).getLimit(), txChargingSchedulePeriod.get(j).getLimit()));
                        if (ChargingProfileKindType.Recurring.equals(txChargingProfileKind)) {
                            if (RecurrencyKindType.Daily.equals(txRecurrencyKind)) {
                                nowLimitContext.setNextTime(Long.valueOf(Long.parseLong(txStartSchedule) + 86400));
                            } else if (RecurrencyKindType.Weekly.equals(txRecurrencyKind)) {
                                nowLimitContext.setNextTime(Long.valueOf(Long.parseLong(txStartSchedule) + 604800));
                            }
                        }
                    }
                } else if (maxChargingSchedulePeriod.get(i).getStartPeriod() <= maxOffset && maxOffset < maxChargingSchedulePeriod.get(i + 1).getStartPeriod() && txChargingSchedulePeriod.get(j).getStartPeriod() <= txOffset && txOffset < txChargingSchedulePeriod.get(j + 1).getStartPeriod()) {
                    nowLimitContext.setLimit(getMinLimit(maxChargingSchedulePeriod.get(i).getLimit(), txChargingSchedulePeriod.get(j).getLimit()));
                    long maxNextTime = Long.parseLong(maxStartSchedule) + maxChargingSchedulePeriod.get(i + 1).getStartPeriod();
                    long txNextTime = Long.parseLong(txStartSchedule) + txChargingSchedulePeriod.get(j + 1).getStartPeriod();
                    if (maxNextTime < txNextTime) {
                        nowLimitContext.setNextTime(Long.valueOf(maxNextTime));
                    } else {
                        nowLimitContext.setNextTime(Long.valueOf(txNextTime));
                    }
                }
            }
            i++;
        }
        return nowLimitContext;
    }

    private double getMinLimit(double limit) {
        int adjustAmp = ChargeStatusCacheProvider.getInstance().getAdjustAmp();
        return limit < ((double) adjustAmp) ? limit : adjustAmp;
    }

    private double getMinLimit(double maxLimit, double txLimit) {
        int adjustAmp = ChargeStatusCacheProvider.getInstance().getAdjustAmp();
        if (txLimit >= maxLimit || maxLimit >= adjustAmp) {
            return (txLimit <= maxLimit || maxLimit >= ((double) adjustAmp)) ? adjustAmp : maxLimit;
        }
        return txLimit;
    }

    private ChargingProfile getMaxStackLevel(ArrayList<ChargingProfile> chargingProfiles, long nowTime, Long startTime) {
        int i;
        Integer duration;
        if (chargingProfiles == null || chargingProfiles.size() == 0) {
            return null;
        }
        ArrayList<ChargingProfile> clonedChargingProfiles = new ArrayList<>();
        Iterator<ChargingProfile> it2 = chargingProfiles.iterator();
        while (it2.hasNext()) {
            ChargingProfile profile = it2.next();
            clonedChargingProfiles.add(profile.deepClone());
        }
        ArrayList<ChargingProfile> validChargingProfile = new ArrayList<>();
        while (i < clonedChargingProfiles.size()) {
            ChargingProfile tmp = clonedChargingProfiles.get(i);
            String validFrom = tmp.getValidFrom();
            String validTo = tmp.getValidTo();
            if (!TextUtils.isEmpty(validFrom) && !TextUtils.isEmpty(validTo)) {
                long from = TimeUtils.getTsFromISO8601Format(validFrom);
                long to = TimeUtils.getTsFromISO8601Format(validTo);
                if (nowTime >= from && nowTime < to) {
                    tmp.setValidFrom(String.valueOf(from / 1000));
                    tmp.setValidTo(String.valueOf(to / 1000));
                }
            }
            String chargingProfileKind = tmp.getChargingProfileKind();
            ChargingSchedule chargingSchedule = tmp.getChargingSchedule();
            String startSchedule = chargingSchedule.getStartSchedule();
            if (TextUtils.isEmpty(startSchedule)) {
                if (ChargingProfileKindType.Relative.equals(chargingProfileKind)) {
                    if (startTime != null) {
                        chargingSchedule.setStartSchedule(String.valueOf(startTime));
                    }
                } else if (ChargingProfileKindType.Recurring.equals(chargingProfileKind)) {
                    String recurrencyKind = tmp.getRecurrencyKind();
                    if (!TextUtils.isEmpty(recurrencyKind)) {
                        if (RecurrencyKindType.Daily.equals(recurrencyKind)) {
                            chargingSchedule.setDuration(86400);
                            chargingSchedule.setStartSchedule(String.valueOf((TimeUtils.getTomorrowAt(0, 0, 0) / 1000) - 86400));
                        } else if (RecurrencyKindType.Weekly.equals(recurrencyKind)) {
                            chargingSchedule.setDuration(604800);
                            chargingSchedule.setStartSchedule(String.valueOf(((TimeUtils.getTomorrowAt(0, 0, 0) / 1000) - 86400) - ((convertWeek() - 1) * 86400)));
                        }
                    }
                }
            } else {
                chargingSchedule.setStartSchedule(String.valueOf(TimeUtils.getTsFromISO8601Format(startSchedule) / 1000));
            }
            if ((ChargingProfileKindType.Absolute.equals(chargingProfileKind) || ChargingProfileKindType.Relative.equals(chargingProfileKind)) && (duration = chargingSchedule.getDuration()) != null) {
                long start = Integer.parseInt(chargingSchedule.getStartSchedule());
                i = nowTime - start > ((long) duration.intValue()) ? i + 1 : 0;
            }
            validChargingProfile.add(tmp);
        }
        if (validChargingProfile.size() <= 0) {
            return null;
        }
        ChargingProfile chargingProfile = validChargingProfile.get(0);
        int stackLevel = chargingProfile.getStackLevel();
        for (int i2 = 1; i2 < validChargingProfile.size(); i2++) {
            if (validChargingProfile.get(i2).getStackLevel() > stackLevel) {
                chargingProfile = validChargingProfile.get(i2);
                stackLevel = chargingProfile.getStackLevel();
            }
        }
        ChargingSchedule schedule = chargingProfile.getChargingSchedule();
        String chargingRateUnit = schedule.getChargingRateUnit();
        if ("W".equals(chargingRateUnit)) {
            schedule.setChargingRateUnit("A");
            ArrayList<ChargingSchedulePeriod> chargingSchedulePeriods = chargingProfile.getChargingSchedule().getChargingSchedulePeriod();
            Iterator<ChargingSchedulePeriod> it3 = chargingSchedulePeriods.iterator();
            while (it3.hasNext()) {
                ChargingSchedulePeriod period = it3.next();
                period.setLimit(period.getLimit() / (getPhase() * FTPCodes.SERVICE_READY_FOR_NEW_USER));
            }
            return chargingProfile;
        }
        return chargingProfile;
    }

    private void sendStartTransactionReq(String chargeId, boolean isHist) {
        try {
            ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(chargeId);
            if (chargeBill == null) {
                Log.e("sendStartTransactionReq", "chargeBill == null");
                return;
            }
            StartTransactionReq startTransactionReq = new StartTransactionReq();
            startTransactionReq.setConnectorId(Integer.parseInt(chargeBill.getPort()));
            startTransactionReq.setIdTag(chargeBill.getUser_code());
            startTransactionReq.setMeterStart((int) Math.round(chargeBill.getStart_ammeter() * 1000.0d));
            startTransactionReq.setTimestamp(TimeUtils.getISO8601Format(chargeBill.getStart_time(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
            JSONArray reserve = OcppProtocolAgent.getInstance().getReserveNow(startTransactionReq.getIdTag());
            if (reserve != null) {
                ReserveNowReq reserveNowReq = new ReserveNowReq().fromJson(reserve.getJSONObject(3).toString());
                startTransactionReq.setReservationId(Integer.valueOf(reserveNowReq.getReservationId()));
            }
            JSONArray jsonArray = createReqMessage(OcppProtocolAgent.getInstance().genSeq(), OcppMessage.StartTransaction, startTransactionReq.toJson());
            JSONObject attach = new JSONObject();
            attach.put("type", "chargeId");
            attach.put("value", chargeBill.getCharge_id());
            attach.put(ContentDB.ChargeTable.PORT, this.port);
            attach.put("isHist", isHist);
            jsonArray.put(attach);
            sendMessage(jsonArray);
            ChargeContentProxy.getInstance().setChargeStartReportedFlag(chargeId, 2);
        } catch (Exception e) {
            Log.w("OcppPortHandler.sendStartTransactionReq", Log.getStackTraceString(e));
        }
    }

    private void sendStopTransactionReq(String chargeId, boolean isHist) {
        ArrayList<MeterValue> stopTxnMeterValues;
        try {
            ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(chargeId);
            if (chargeBill != null) {
                StopTransactionReq stopTransactionReq = new StopTransactionReq();
                stopTransactionReq.setIdTag(chargeBill.getUser_code());
                stopTransactionReq.setMeterStop((int) Math.round(chargeBill.getStop_ammeter() * 1000.0d));
                stopTransactionReq.setTimestamp(TimeUtils.getISO8601Format(chargeBill.getStop_time(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
                CHARGE_STOP_CAUSE stop_cause = chargeBill.getStop_cause();
                String reason = null;
                if (stop_cause != null) {
                    switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE()[stop_cause.ordinal()]) {
                        case 3:
                            reason = Reason.Local;
                            break;
                        case 4:
                            reason = Reason.Remote;
                            break;
                        case 6:
                            reason = Reason.EVDisconnected;
                            break;
                        case 7:
                        case 8:
                            reason = Reason.PowerLoss;
                            break;
                        case PortRuntimeData.STATUS_EX_11 /* 11 */:
                            reason = Reason.Reboot;
                            break;
                    }
                    stopTransactionReq.setReason(reason);
                }
                new ArrayList();
                if (isHist) {
                    stopTxnMeterValues = (ArrayList) JsonBean.getGsonBuilder().create().fromJson(chargeBill.getAttach_data(), new TypeToken<List<MeterValue>>() { // from class: com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.1
                    }.getType());
                } else {
                    stopTxnMeterValues = getChargeSession().getStopTxnMeterValues();
                    ChargeContentProxy.getInstance().setChargeAttchData(chargeId, JsonBean.getGsonBuilder().create().toJson(stopTxnMeterValues));
                }
                stopTransactionReq.setTransactionData(stopTxnMeterValues);
                String cloud_charge_id = chargeBill.getCloud_charge_id();
                if (!TextUtils.isEmpty(cloud_charge_id)) {
                    stopTransactionReq.setTransactionId(Integer.parseInt(cloud_charge_id));
                    JSONArray jsonArray = createReqMessage(OcppProtocolAgent.getInstance().genSeq(), OcppMessage.StopTransaction, stopTransactionReq.toJson());
                    JSONObject attach = new JSONObject();
                    attach.put("type", "chargeId");
                    attach.put("value", chargeBill.getCharge_id());
                    attach.put(ContentDB.ChargeTable.PORT, this.port);
                    jsonArray.put(attach);
                    if (chargeBill.getStop_report_flag() == 0) {
                        sendMessage(jsonArray);
                        ChargeContentProxy.getInstance().setChargeStopReportedFlag(chargeId, 2);
                    }
                }
            }
        } catch (Exception e) {
            Log.w("OcppPortHandler.sendStopTransactionReq", Log.getStackTraceString(e));
        }
    }

    public void sendStatusNotificationReq(boolean isTrigger, String code) {
        ArrayList<Double> volts;
        Double volt;
        ArrayList<Double> volts2;
        Double volt2;
        try {
            StatusNotificationReq statusNotificationReq = new StatusNotificationReq();
            statusNotificationReq.setConnectorId(Integer.parseInt(this.port));
            ErrorCode error = HardwareStatusCacheProvider.getInstance().getPort(this.port).getDeviceError();
            String ocppErrorCode = ChargePointErrorCode.OtherError;
            String vendorErrorCode = null;
            Port errPortStatus = null;
            HashMap<String, Object> data = error.getData();
            if (data != null) {
                Port errPortStatus2 = new Port().fromJson((String) data.get("portStatus"));
                errPortStatus = errPortStatus2;
            }
            if (!TextUtils.isEmpty(code)) {
                int errCode = Integer.parseInt(code);
                if (errCode == 200 || errCode == 50019) {
                    statusNotificationReq.setErrorCode(ChargePointErrorCode.NoError);
                } else {
                    switch (errCode) {
                        case ErrorCode.EC_DEVICE_NOT_INIT /* 30010 */:
                            vendorErrorCode = "EC_DEVICE_NOT_INIT";
                            break;
                        case ErrorCode.EC_DEVICE_NO_GROUND /* 30011 */:
                            ocppErrorCode = ChargePointErrorCode.GroundFailure;
                            break;
                        case ErrorCode.EC_DEVICE_LOST_PHASE /* 30012 */:
                            vendorErrorCode = "EC_DEVICE_LOST_PHASE";
                            break;
                        case ErrorCode.EC_DEVICE_EMERGENCY_STOP /* 30013 */:
                            vendorErrorCode = "EC_DEVICE_EMERGENCY_STOP";
                            break;
                        case ErrorCode.EC_DEVICE_VOLT_ERROR /* 30014 */:
                            vendorErrorCode = "EC_DEVICE_VOLT_ERROR";
                            if (errPortStatus != null && (volts2 = errPortStatus.getVolts()) != null && (volt2 = volts2.get(0)) != null) {
                                if (volt2.doubleValue() > 220.0d) {
                                    ocppErrorCode = "OverVoltage";
                                } else {
                                    ocppErrorCode = ChargePointErrorCode.UnderVoltage;
                                }
                                vendorErrorCode = null;
                                break;
                            }
                            break;
                        case ErrorCode.EC_DEVICE_AMP_ERROR /* 30015 */:
                            ocppErrorCode = ChargePointErrorCode.OverCurrentFailure;
                            break;
                        case ErrorCode.EC_DEVICE_TEMP_ERROR /* 30016 */:
                            ocppErrorCode = ChargePointErrorCode.HighTemperature;
                            break;
                        case ErrorCode.EC_DEVICE_POWER_LEAK /* 30017 */:
                            vendorErrorCode = "EC_DEVICE_POWER_LEAK";
                            break;
                        case ErrorCode.EC_DEVICE_COMM_ERROR /* 30018 */:
                            ocppErrorCode = ChargePointErrorCode.EVCommunicationError;
                            break;
                    }
                    if (ChargePointErrorCode.OtherError.equals(ocppErrorCode)) {
                        statusNotificationReq.setErrorCode(ocppErrorCode);
                        statusNotificationReq.setVendorErrorCode(vendorErrorCode);
                    } else {
                        statusNotificationReq.setErrorCode(ocppErrorCode);
                    }
                }
            } else if (200 != error.getCode()) {
                switch (error.getCode()) {
                    case ErrorCode.EC_DEVICE_NOT_INIT /* 30010 */:
                        vendorErrorCode = "EC_DEVICE_NOT_INIT";
                        break;
                    case ErrorCode.EC_DEVICE_NO_GROUND /* 30011 */:
                        ocppErrorCode = ChargePointErrorCode.GroundFailure;
                        break;
                    case ErrorCode.EC_DEVICE_LOST_PHASE /* 30012 */:
                        vendorErrorCode = "EC_DEVICE_LOST_PHASE";
                        break;
                    case ErrorCode.EC_DEVICE_EMERGENCY_STOP /* 30013 */:
                        vendorErrorCode = "EC_DEVICE_EMERGENCY_STOP";
                        break;
                    case ErrorCode.EC_DEVICE_VOLT_ERROR /* 30014 */:
                        vendorErrorCode = "EC_DEVICE_VOLT_ERROR";
                        if (errPortStatus != null && (volts = errPortStatus.getVolts()) != null && (volt = volts.get(0)) != null) {
                            if (volt.doubleValue() > 220.0d) {
                                ocppErrorCode = "OverVoltage";
                            } else {
                                ocppErrorCode = ChargePointErrorCode.UnderVoltage;
                            }
                            vendorErrorCode = null;
                            break;
                        }
                        break;
                    case ErrorCode.EC_DEVICE_AMP_ERROR /* 30015 */:
                        ocppErrorCode = ChargePointErrorCode.OverCurrentFailure;
                        break;
                    case ErrorCode.EC_DEVICE_TEMP_ERROR /* 30016 */:
                        ocppErrorCode = ChargePointErrorCode.HighTemperature;
                        break;
                    case ErrorCode.EC_DEVICE_POWER_LEAK /* 30017 */:
                        vendorErrorCode = "EC_DEVICE_POWER_LEAK";
                        break;
                    case ErrorCode.EC_DEVICE_COMM_ERROR /* 30018 */:
                        ocppErrorCode = ChargePointErrorCode.EVCommunicationError;
                        break;
                }
                if (ChargePointErrorCode.OtherError.equals(ocppErrorCode)) {
                    statusNotificationReq.setErrorCode(ocppErrorCode);
                    statusNotificationReq.setVendorErrorCode(vendorErrorCode);
                } else {
                    statusNotificationReq.setErrorCode(ocppErrorCode);
                }
            } else {
                statusNotificationReq.setErrorCode(ChargePointErrorCode.NoError);
            }
            statusNotificationReq.setTimestamp(TimeUtils.getISO8601Format(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
            String status = null;
            if (ChargeStatusCacheProvider.getInstance().getPortSwitch(this.port)) {
                if (error.getCode() == 200) {
                    switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STATUS()[this.status.ordinal()]) {
                        case 1:
                            if (HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port)) {
                                status = ChargePointStatus.Preparing;
                                break;
                            } else {
                                status = ChargePointStatus.Available;
                                break;
                            }
                        case 2:
                            HashMap<Integer, JSONArray> reserveNow = OcppProtocolAgent.getInstance().getReserveNow();
                            if (reserveNow != null && reserveNow.size() > 0) {
                                status = ChargePointStatus.Reserved;
                                break;
                            } else {
                                status = ChargePointStatus.Preparing;
                                break;
                            }
                            break;
                        case 3:
                            if (getChargeSession().isEvSuspendStatus()) {
                                status = ChargePointStatus.SuspendedEV;
                                break;
                            } else {
                                status = ChargePointStatus.Charging;
                                break;
                            }
                        case 4:
                            status = ChargePointStatus.Finishing;
                            break;
                    }
                } else {
                    status = "Faulted";
                }
            } else {
                status = "Unavailable";
            }
            statusNotificationReq.setStatus(status);
            JSONArray jsonArray = createReqMessage(OcppProtocolAgent.getInstance().genSeq(), "StatusNotification", statusNotificationReq.toJson());
            if (isTrigger) {
                JSONObject attach = new JSONObject();
                attach.put("type", OcppMessage.TriggerMessage);
                jsonArray.put(attach);
                sendMessage(jsonArray);
                return;
            }
            String newError = statusNotificationReq.getErrorCode();
            String newStatus = statusNotificationReq.getStatus();
            if (TextUtils.isEmpty(this.currentError) || TextUtils.isEmpty(this.currentStatus) || !newError.equals(this.currentError) || !newStatus.equals(this.currentStatus)) {
                this.currentError = newError;
                this.currentStatus = newStatus;
                sendMessage(jsonArray);
            }
        } catch (Exception e) {
            Log.w("OcppPortHandler.sendStatusNotificationReq", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleRequest(JSONArray jsonArray) {
        try {
            String string = jsonArray.getString(2);
            switch (string.hashCode()) {
                case -1013156522:
                    if (string.equals(OcppMessage.SetChargingProfile)) {
                        handleSetChargingProfileReq(jsonArray);
                        break;
                    }
                    break;
                case -977722974:
                    if (string.equals(OcppMessage.RemoteStartTransaction)) {
                        OcppDCAPGateway.getInstance().sendMessage(OcppDCAPGateway.getInstance().obtainMessage(77827, jsonArray));
                        break;
                    }
                    break;
                case -964284085:
                    if (string.equals(OcppMessage.ChangeAvailability)) {
                        handleChangeAvailabilityReq(jsonArray);
                        break;
                    }
                    break;
                case -362842058:
                    if (string.equals(OcppMessage.RemoteStopTransaction)) {
                        RemoteStopTransactionReq remoteStopTransactionReq = new RemoteStopTransactionReq().fromJson(jsonArray.getJSONObject(3).toString());
                        String transactionId = String.valueOf(remoteStopTransactionReq.getTransactionId());
                        if (!transactionId.equals(getChargeSession().getTransactionId())) {
                            OcppProtocolAgent.getInstance().sendRemoteStopTransactionConf(jsonArray.getString(1), "Rejected");
                            break;
                        } else {
                            OcppDCAPGateway.getInstance().sendMessage(OcppDCAPGateway.getInstance().obtainMessage(77827, jsonArray));
                            break;
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            Log.w("OcppPortHandler.handleRequest", Log.getStackTraceString(e));
        }
    }

    private void handleChangeAvailabilityReq(JSONArray jsonArray) {
        String status;
        try {
            ChangeAvailabilityReq changeAvailabilityReq = new ChangeAvailabilityReq().fromJson(jsonArray.getJSONObject(3).toString());
            String type = changeAvailabilityReq.getType();
            boolean enable = true;
            if (!TextUtils.isEmpty(type)) {
                if (AvailabilityType.Inoperative.equals(type)) {
                    enable = false;
                } else if (AvailabilityType.Operative.equals(type)) {
                    enable = true;
                }
            }
            if (!this.status.equals(CHARGE_STATUS.IDLE)) {
                status = AvailabilityStatus.Scheduled;
            } else {
                status = "Accepted";
            }
            ChargeStatusCacheProvider.getInstance().updatePortSwitch(this.port, enable);
            PortSetting portSetting = RemoteSettingCacheProvider.getInstance().getChargePortSetting(this.port);
            if (portSetting == null) {
                portSetting = new PortSetting();
                portSetting.setAmpPercent(Integer.valueOf(10000 / HardwareStatusCacheProvider.getInstance().getPorts().size()));
            }
            portSetting.setEnable(enable);
            RemoteSettingCacheProvider.getInstance().updateChargePortSetting(this.port, portSetting);
            RemoteSettingCacheProvider.getInstance().persist();
            ChangeAvailabilityConf changeAvailabilityConf = new ChangeAvailabilityConf();
            changeAvailabilityConf.setStatus(status);
            JSONArray jsonArrayConf = createConfMessage(jsonArray.getString(1), OcppMessage.ChangeAvailability, changeAvailabilityConf.toJson());
            sendMessage(jsonArrayConf);
            sendStatusNotificationReq(false, null);
        } catch (Exception e) {
            Log.w("OcppPortHandler.handleChangeAvailabilityReq", Log.getStackTraceString(e));
        }
    }

    private void handleSetChargingProfileReq(JSONArray jsonArray) {
        try {
            OcppChargeSession chargeSession = getChargeSession();
            SetChargingProfileReq setChargingProfileReq = new SetChargingProfileReq().fromJson(jsonArray.getJSONObject(3).toString());
            int connectorId = setChargingProfileReq.getConnectorId();
            ChargingProfile csChargingProfiles = setChargingProfileReq.getCsChargingProfiles();
            HashMap<String, ArrayList<ChargingProfile>> txChargingProfiles = chargeSession.getTxChargingProfiles();
            ArrayList<ChargingProfile> chargingProfiles = new ArrayList<>();
            if (txChargingProfiles.size() > 0) {
                ArrayList<ChargingProfile> chargingProfiles2 = txChargingProfiles.get(this.port);
                chargingProfiles = chargingProfiles2;
            }
            String status = "Rejected";
            if (CHARGE_STATUS.CHARGING.equals(this.status) && this.port.equals(String.valueOf(connectorId)) && ChargingProfilePurposeType.TxProfile.equals(csChargingProfiles.getChargingProfilePurpose())) {
                String chargeId = chargeSession.getCharge_id();
                if (!TextUtils.isEmpty(chargeId)) {
                    ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(chargeId);
                    String transactionId = chargeBill.getCloud_charge_id();
                    if (!TextUtils.isEmpty(transactionId) && transactionId.equals(Integer.valueOf(csChargingProfiles.getTransactionId()))) {
                        status = "Accepted";
                        if (chargingProfiles.size() > 0) {
                            Integer flag = isExist(chargingProfiles, csChargingProfiles);
                            if (flag != null) {
                                chargingProfiles.set(flag.intValue(), csChargingProfiles);
                            } else {
                                chargingProfiles.add(csChargingProfiles);
                            }
                        } else {
                            chargingProfiles.add(csChargingProfiles);
                        }
                        txChargingProfiles.put(this.port, chargingProfiles);
                        chargeSession.setTxChargingProfiles(txChargingProfiles);
                        executeChargingProfile(chargeBill.getStart_time() / 1000, System.currentTimeMillis() / 1000);
                    }
                }
            }
            OcppProtocolAgent.getInstance().sendSetChargingProfileConf(jsonArray.getString(1), status);
        } catch (Exception e) {
            Log.w("OcppPortHandler.handleSetChargingProfileReq", Log.getStackTraceString(e));
        }
    }

    private Integer isExist(ArrayList<ChargingProfile> chargingProfiles, ChargingProfile csChargingProfiles) {
        for (int i = 0; i < chargingProfiles.size(); i++) {
            if (chargingProfiles.get(i).getStackLevel() == csChargingProfiles.getStackLevel()) {
                return Integer.valueOf(i);
            }
        }
        return null;
    }

    private void stopCharge() {
        try {
            OcppChargeSession chargeSession = getChargeSession();
            String chargeId = chargeSession.getCharge_id();
            String userType = chargeSession.getUser_type();
            String userCode = chargeSession.getUser_code();
            if (!TextUtils.isEmpty(chargeId)) {
                CAPDirectiveOption opt = new CAPDirectiveOption();
                opt.setCharge_id(chargeId);
                StopDirective stop = new StopDirective();
                String from = "user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode;
                DCAPMessage stopRequest = DCAPAdapter.createRequest(from, "stop", opt, stop);
                DCAPProxy.getInstance().sendRequest(stopRequest);
            }
        } catch (Exception e) {
            Log.w("OcppPortHandler.stopCharge", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleResponse(JSONArray request, JSONArray response) {
        try {
            String string = request.getString(2);
            switch (string.hashCode()) {
                case 77777212:
                    if (string.equals(OcppMessage.StartTransaction)) {
                        StartTransactionReq startTransactionReq = new StartTransactionReq().fromJson(request.getJSONObject(3).toString());
                        StartTransactionConf startTransactionConf = new StartTransactionConf().fromJson(response.getJSONObject(2).toString());
                        OcppProtocolAgent.getInstance().addIdTagInfoCache(startTransactionReq.getIdTag(), startTransactionConf.getIdTagInfo());
                        getChargeSession().setTransactionId(String.valueOf(startTransactionConf.getTransactionId()));
                        JSONObject attach = request.getJSONObject(4);
                        String chargeId = attach.getString("value");
                        boolean isHist = attach.getBoolean("isHist");
                        ChargeContentProxy.getInstance().setCloudChargeId(chargeId, String.valueOf(startTransactionConf.getTransactionId()));
                        ChargeContentProxy.getInstance().setChargeStartReportedFlag(chargeId, 1);
                        if (!"Accepted".equals(startTransactionConf.getIdTagInfo().getStatus()) && !isHist) {
                            String stopTransactionOnInvalidId = getOcppConfig().getMaps().get(OcppMessage.StopTransactionOnInvalidId);
                            if (!TextUtils.isEmpty(stopTransactionOnInvalidId) && Boolean.valueOf(stopTransactionOnInvalidId).booleanValue()) {
                                stopCharge();
                                break;
                            } else {
                                String maxEnergyOnInvalidId = getOcppConfig().getMaps().get(OcppMessage.MaxEnergyOnInvalidId);
                                if (!TextUtils.isEmpty(maxEnergyOnInvalidId) && TextUtils.isDigitsOnly(maxEnergyOnInvalidId)) {
                                    int maxEnergy = Integer.parseInt(maxEnergyOnInvalidId);
                                    if (maxEnergy > 0) {
                                        OcppRequestSession ocppRequestSession = new OcppRequestSession();
                                        ocppRequestSession.setRequest(request);
                                        ocppRequestSession.setResponse(response);
                                        OcppDCAPGateway.getInstance().sendMessage(OcppDCAPGateway.getInstance().obtainMessage(77828, ocppRequestSession));
                                        break;
                                    } else if (maxEnergy == 0) {
                                        stopCharge();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    break;
                case 641037660:
                    if (string.equals(OcppMessage.StopTransaction)) {
                        StopTransactionReq stopTransactionReq = new StopTransactionReq().fromJson(request.getJSONObject(3).toString());
                        StopTransactionConf stopTransactionConf = new StopTransactionConf().fromJson(response.getJSONObject(2).toString());
                        OcppProtocolAgent.getInstance().addIdTagInfoCache(stopTransactionReq.getIdTag(), stopTransactionConf.getIdTagInfo());
                        ChargeContentProxy.getInstance().setChargeStopReportedFlag(request.getJSONObject(4).getString("value"), 1);
                        if (OcppProtocolAgent.getInstance().isResetOnTxStopped()) {
                            OcppProtocolAgent.getInstance().setResetOnTxStopped(false);
                            DCAPAdapter.reset();
                            break;
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            Log.w("OcppPortHandler.handleResponse", Log.getStackTraceString(e));
        }
    }

    public void sendMeterValuesReq(boolean isClockAligned) {
        try {
            MeterValuesReq meterValuesReq = new MeterValuesReq();
            meterValuesReq.setConnectorId(Integer.parseInt(this.port));
            OcppChargeSession chargeSession = getChargeSession();
            ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(chargeSession.getCharge_id());
            if (chargeBill != null) {
                String cloud_charge_id = chargeBill.getCloud_charge_id();
                if (!TextUtils.isEmpty(cloud_charge_id)) {
                    meterValuesReq.setTransactionId(Integer.valueOf(Integer.parseInt(cloud_charge_id)));
                }
            }
            if (isClockAligned) {
                meterValuesReq.setMeterValue(createClockAlignedMeterValue());
            } else {
                meterValuesReq.setMeterValue(createIntervalMeterValue());
            }
            JSONArray jsonArray = createReqMessage(OcppProtocolAgent.getInstance().genSeq(), "MeterValues", meterValuesReq.toJson());
            sendMessage(jsonArray);
        } catch (Exception e) {
            Log.w("OcppPortHandler.sendMeterValuesReq", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reportHistoryChargeRequest() {
        try {
            ChargeBill startChargeBill = getHistoryChargeBill(true);
            if (startChargeBill == null) {
                Log.w("OcppPortHandler.reportHistoryChargeRequest", "HistoryStartChargeBill: null");
            } else {
                sendStartTransactionReq(startChargeBill.getCharge_id(), true);
            }
            ChargeBill stopChargeBill = getHistoryChargeBill(false);
            if (stopChargeBill == null) {
                Log.w("OcppPortHandler.reportHistoryChargeRequest", "HistoryStopChargeBill: null");
            } else {
                sendStopTransactionReq(stopChargeBill.getCharge_id(), true);
            }
        } catch (Exception e) {
            Log.w("OcppPortHandler.reportHistoryChargeRequest", Log.getStackTraceString(e));
        }
    }

    private ChargeBill getHistoryChargeBill(boolean isStart) {
        ArrayList<ChargeBill> chargeBills;
        String[] ocppUserTypes = {CHARGE_USER_TYPE.ocpp.toString(), String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.ocpp};
        if (isStart) {
            chargeBills = ChargeContentProxy.getInstance().getUnReportedStartCharges(ocppUserTypes, this.port);
        } else {
            chargeBills = ChargeContentProxy.getInstance().getUnReportedStopCharges(ocppUserTypes, this.port);
        }
        if (chargeBills == null || chargeBills.size() == 0) {
            return null;
        }
        return chargeBills.get(0);
    }

    private JSONArray createReqMessage(String uid, String action, String load) {
        return OcppProtocolAgent.getInstance().createReqMessage(uid, action, load);
    }

    private JSONArray createConfMessage(String uid, String action, String load) {
        return OcppProtocolAgent.getInstance().createConfMessage(uid, action, load);
    }

    private void sendMessage(JSONArray jsonArray) {
        OcppProtocolAgent.getInstance().sendMessage(jsonArray);
    }

    private ArrayList<MeterValue> createIntervalMeterValue() {
        HashMap<String, String> maps = getOcppConfig().getMaps();
        ArrayList<MeterValue> meterValues = new ArrayList<>();
        MeterValue meterValue = new MeterValue();
        MeterValue stopTxnMeterValue = new MeterValue();
        String timestamp = TimeUtils.getISO8601Format(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone());
        meterValue.setTimestamp(timestamp);
        stopTxnMeterValue.setTimestamp(timestamp);
        PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port);
        if (portStatus != null) {
            String meterValuesSampledData = maps.get(OcppMessage.MeterValuesSampledData);
            if (!TextUtils.isEmpty(meterValuesSampledData)) {
                String[] meterValuesSampledDatas = meterValuesSampledData.split(",");
                int maxLength = 0;
                String meterValuesSampledDataMaxLength = maps.get(OcppMessage.MeterValuesSampledDataMaxLength);
                if (!TextUtils.isEmpty(meterValuesSampledDataMaxLength) && TextUtils.isDigitsOnly(meterValuesSampledDataMaxLength)) {
                    maxLength = Integer.parseInt(meterValuesSampledDataMaxLength);
                }
                if (meterValuesSampledDatas.length > 0) {
                    if (maxLength > 0) {
                        meterValue.setSampledValue(getSampledValuesDataMaxLength(portStatus, meterValuesSampledDatas, maxLength, ReadingContext.SamplePeriodic));
                        meterValues.add(meterValue);
                    } else {
                        meterValue.setSampledValue(getSampledValues(portStatus, meterValuesSampledDatas, ReadingContext.SamplePeriodic));
                        meterValues.add(meterValue);
                    }
                }
            }
            String stopTxnSampledData = maps.get(OcppMessage.StopTxnSampledData);
            if (!TextUtils.isEmpty(stopTxnSampledData)) {
                String[] stopTxnSampledDatas = stopTxnSampledData.split(",");
                int maxLength2 = 0;
                String stopTxnSampledDataMaxLength = maps.get(OcppMessage.StopTxnSampledDataMaxLength);
                if (!TextUtils.isEmpty(stopTxnSampledDataMaxLength) && TextUtils.isDigitsOnly(stopTxnSampledDataMaxLength)) {
                    maxLength2 = Integer.parseInt(stopTxnSampledDataMaxLength);
                }
                if (stopTxnSampledDatas.length > 0) {
                    OcppChargeSession chargeSession = getChargeSession();
                    ArrayList<MeterValue> stopTxnMeterValues = chargeSession.getStopTxnMeterValues();
                    if (maxLength2 > 0) {
                        stopTxnMeterValue.setSampledValue(getSampledValuesDataMaxLength(portStatus, stopTxnSampledDatas, maxLength2, ReadingContext.SamplePeriodic));
                        stopTxnMeterValues.add(stopTxnMeterValue);
                    } else {
                        stopTxnMeterValue.setSampledValue(getSampledValues(portStatus, stopTxnSampledDatas, ReadingContext.SamplePeriodic));
                        stopTxnMeterValues.add(stopTxnMeterValue);
                    }
                }
            }
        }
        return meterValues;
    }

    private ArrayList<MeterValue> createClockAlignedMeterValue() {
        int interval;
        HashMap<String, String> maps = getOcppConfig().getMaps();
        ArrayList<MeterValue> meterValues = new ArrayList<>();
        MeterValue meterValue = new MeterValue();
        MeterValue stopTxnAlignedMeterValue = new MeterValue();
        long currentTime = System.currentTimeMillis();
        String clockAlignedDataInterval = maps.get(OcppMessage.ClockAlignedDataInterval);
        if (!TextUtils.isEmpty(clockAlignedDataInterval) && TextUtils.isDigitsOnly(clockAlignedDataInterval) && (interval = Integer.parseInt(clockAlignedDataInterval)) > 0) {
            long currentSecond = TimeUtils.getHHmmSeconds(System.currentTimeMillis());
            int remainder = (int) (currentSecond % interval);
            currentTime = remainder < interval / 2 ? currentTime - (remainder * 1000) : currentTime + ((interval - remainder) * 1000);
        }
        String timestamp = TimeUtils.getISO8601Format(currentTime, RemoteSettingCacheProvider.getInstance().getProtocolTimezone());
        meterValue.setTimestamp(timestamp);
        stopTxnAlignedMeterValue.setTimestamp(timestamp);
        PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port);
        if (portStatus != null) {
            String meterValuesAlignedData = maps.get(OcppMessage.MeterValuesAlignedData);
            if (!TextUtils.isEmpty(meterValuesAlignedData)) {
                String[] meterValuesAlignedDatas = meterValuesAlignedData.split(",");
                int maxLength = 0;
                String meterValuesAlignedDataMaxLength = maps.get(OcppMessage.MeterValuesAlignedDataMaxLength);
                if (!TextUtils.isEmpty(meterValuesAlignedDataMaxLength) && TextUtils.isDigitsOnly(meterValuesAlignedDataMaxLength)) {
                    maxLength = Integer.parseInt(meterValuesAlignedDataMaxLength);
                }
                if (meterValuesAlignedDatas.length > 0) {
                    if (maxLength > 0) {
                        meterValue.setSampledValue(getSampledValuesDataMaxLength(portStatus, meterValuesAlignedDatas, maxLength, ReadingContext.SampleClock));
                        meterValues.add(meterValue);
                    } else {
                        meterValue.setSampledValue(getSampledValues(portStatus, meterValuesAlignedDatas, ReadingContext.SampleClock));
                        meterValues.add(meterValue);
                    }
                }
            }
            String stopTxnAlignedData = maps.get(OcppMessage.StopTxnAlignedData);
            if (!TextUtils.isEmpty(stopTxnAlignedData)) {
                String[] stopTxnAlignedDatas = stopTxnAlignedData.split(",");
                int maxLength2 = 0;
                String stopTxnAlignedDataMaxLength = maps.get(OcppMessage.StopTxnAlignedDataMaxLength);
                if (!TextUtils.isEmpty(stopTxnAlignedDataMaxLength) && TextUtils.isDigitsOnly(stopTxnAlignedDataMaxLength)) {
                    maxLength2 = Integer.parseInt(stopTxnAlignedDataMaxLength);
                }
                if (stopTxnAlignedDatas.length > 0) {
                    OcppChargeSession chargeSession = getChargeSession();
                    String chargeId = chargeSession.getCharge_id();
                    ArrayList<MeterValue> stopTxnMeterValues = chargeSession.getStopTxnMeterValues();
                    if (maxLength2 > 0) {
                        stopTxnAlignedMeterValue.setSampledValue(getSampledValuesDataMaxLength(portStatus, stopTxnAlignedDatas, maxLength2, ReadingContext.SampleClock));
                        stopTxnMeterValues.add(stopTxnAlignedMeterValue);
                    } else {
                        stopTxnAlignedMeterValue.setSampledValue(getSampledValues(portStatus, stopTxnAlignedDatas, ReadingContext.SampleClock));
                        stopTxnMeterValues.add(stopTxnAlignedMeterValue);
                    }
                    if (!TextUtils.isEmpty(chargeId)) {
                        ChargeContentProxy.getInstance().setChargeAttchData(chargeId, JsonBean.getGsonBuilder().create().toJson(stopTxnMeterValues));
                    }
                }
            }
        }
        return meterValues;
    }

    /* JADX WARN: Code restructure failed: missing block: B:59:0x0031, code lost:
        continue;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private java.util.ArrayList<com.xcharge.charger.protocol.ocpp.bean.types.SampledValue> getSampledValuesDataMaxLength(com.xcharge.charger.data.bean.status.PortStatus r17, java.lang.String[] r18, int r19, java.lang.String r20) {
        /*
            Method dump skipped, instructions count: 430
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.getSampledValuesDataMaxLength(com.xcharge.charger.data.bean.status.PortStatus, java.lang.String[], int, java.lang.String):java.util.ArrayList");
    }

    private ArrayList<SampledValue> getSampledValues(PortStatus portStatus, String[] sampledDatas, String context) {
        ArrayList<Double> amps;
        ArrayList<Double> volts;
        ArrayList<SampledValue> sampledValue = new ArrayList<>();
        for (String str : sampledDatas) {
            switch (str.hashCode()) {
                case -1997933762:
                    if (str.equals(Measurand.Voltage) && (volts = portStatus.getVolts()) != null) {
                        for (int j = 0; j < getPhase(); j++) {
                            if (volts.get(j) != null) {
                                SampledValue item = new SampledValue();
                                item.setValue(String.valueOf(volts.get(j)));
                                item.setContext(context);
                                item.setMeasurand(Measurand.Voltage);
                                switch (j) {
                                    case 0:
                                        item.setPhase(Phase.L1);
                                        break;
                                    case 1:
                                        item.setPhase(Phase.L2);
                                        break;
                                    case 2:
                                        item.setPhase(Phase.L3);
                                        break;
                                }
                                item.setUnit(UnitOfMeasure.V);
                                sampledValue.add(item);
                            }
                        }
                        break;
                    }
                    break;
                case -210256374:
                    if (str.equals(Measurand.EnergyActiveImportRegister) && portStatus.getAmmeter() != null) {
                        SampledValue item2 = new SampledValue();
                        item2.setValue(String.valueOf(Math.round(portStatus.getAmmeter().doubleValue() * 1000.0d)));
                        item2.setContext(context);
                        item2.setMeasurand(Measurand.EnergyActiveImportRegister);
                        item2.setUnit(UnitOfMeasure.Wh);
                        sampledValue.add(item2);
                        break;
                    }
                    break;
                case 107981306:
                    if (str.equals(Measurand.CurrentImport) && (amps = portStatus.getAmps()) != null) {
                        for (int j2 = 1; j2 < getPhase() + 1; j2++) {
                            if (amps.get(j2) != null) {
                                SampledValue item3 = new SampledValue();
                                item3.setValue(String.valueOf(amps.get(j2)));
                                item3.setContext(context);
                                item3.setMeasurand(Measurand.CurrentImport);
                                switch (j2) {
                                    case 1:
                                        item3.setPhase(Phase.L1);
                                        break;
                                    case 2:
                                        item3.setPhase(Phase.L2);
                                        break;
                                    case 3:
                                        item3.setPhase(Phase.L3);
                                        break;
                                }
                                item3.setUnit("A");
                                sampledValue.add(item3);
                            }
                        }
                        break;
                    }
                    break;
                case 567329348:
                    if (str.equals(Measurand.PowerActiveImport) && portStatus.getKwatt() != null) {
                        SampledValue item4 = new SampledValue();
                        item4.setValue(String.valueOf(portStatus.getKwatt()));
                        item4.setContext(context);
                        item4.setMeasurand(Measurand.PowerActiveImport);
                        item4.setUnit(UnitOfMeasure.kW);
                        sampledValue.add(item4);
                        break;
                    }
                    break;
                case 1989569876:
                    if (str.equals(Measurand.Temperature) && portStatus.getTemprature() != null) {
                        SampledValue item5 = new SampledValue();
                        item5.setValue(String.valueOf(portStatus.getTemprature().doubleValue() / 10.0d));
                        item5.setContext(context);
                        item5.setMeasurand(Measurand.Temperature);
                        item5.setLocation(Location.Body);
                        item5.setUnit(UnitOfMeasure.Celsius);
                        sampledValue.add(item5);
                        break;
                    }
                    break;
            }
        }
        return sampledValue;
    }

    private int getPhase() {
        return OcppProtocolAgent.getInstance().getPhase();
    }

    private int convertWeek() {
        return OcppProtocolAgent.getInstance().convertWeek();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public OcppConfig getOcppConfig() {
        return OcppProtocolAgent.getInstance().getOcppConfig();
    }
}
