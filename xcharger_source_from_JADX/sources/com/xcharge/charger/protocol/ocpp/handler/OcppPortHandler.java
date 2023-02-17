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
import com.xcharge.charger.data.p004db.ContentDB;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.data.proxy.PortChargeStatusObserver;
import com.xcharge.charger.data.proxy.PortStatusObserver;
import com.xcharge.charger.device.p005c2.bean.PortRuntimeData;
import com.xcharge.charger.device.p005c2.service.C2DeviceProxy;
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
import com.xcharge.common.utils.TimeUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.json.JSONArray;
import org.json.JSONObject;
import p010it.sauronsoftware.ftp4j.FTPCodes;

public class OcppPortHandler {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STATUS = null;

    /* renamed from: $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE */
    private static /* synthetic */ int[] f126x26790b25 = null;
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
    private OcppChargeSession chargeSession = null;
    private Context context = null;
    private String currentError = null;
    private String currentStatus = null;
    private MsgHandler handler = null;
    /* access modifiers changed from: private */
    public HandlerTimer handlerTimer = null;
    /* access modifiers changed from: private */
    public String port = null;
    private PortChargeStatusObserver portChargeStatusObserver = null;
    private PortStatusObserver portRecoverableFaultObserver = null;
    private PortStatusObserver portStatusObserver = null;
    private CHARGE_STATUS status = CHARGE_STATUS.IDLE;
    private HandlerThread thread = null;

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STATUS() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STATUS;
        if (iArr == null) {
            iArr = new int[CHARGE_STATUS.values().length];
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

    /* renamed from: $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE */
    static /* synthetic */ int[] m35x26790b25() {
        int[] iArr = f126x26790b25;
        if (iArr == null) {
            iArr = new int[CHARGE_STOP_CAUSE.values().length];
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
            f126x26790b25 = iArr;
        }
        return iArr;
    }

    private class MsgHandler extends Handler {
        public MsgHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(android.os.Message r17) {
            /*
                r16 = this;
                r5 = 0
                r10 = 0
                r0 = r17
                int r11 = r0.what     // Catch:{ Exception -> 0x003c }
                switch(r11) {
                    case 73729: goto L_0x000d;
                    case 73730: goto L_0x006c;
                    case 73731: goto L_0x0091;
                    case 73732: goto L_0x009b;
                    case 73733: goto L_0x00ac;
                    case 73734: goto L_0x00d1;
                    case 73735: goto L_0x017b;
                    case 73736: goto L_0x018c;
                    case 73737: goto L_0x00db;
                    case 73744: goto L_0x011d;
                    case 73745: goto L_0x0146;
                    case 73746: goto L_0x0161;
                    case 131073: goto L_0x01a3;
                    case 139265: goto L_0x01de;
                    default: goto L_0x0009;
                }
            L_0x0009:
                super.handleMessage(r17)
                return
            L_0x000d:
                java.lang.String r11 = "OcppPortHandler.handleMessage"
                java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x003c }
                java.lang.String r13 = "service actived !!! port: "
                r12.<init>(r13)     // Catch:{ Exception -> 0x003c }
                r0 = r16
                com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r13 = com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.this     // Catch:{ Exception -> 0x003c }
                java.lang.String r13 = r13.port     // Catch:{ Exception -> 0x003c }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x003c }
                java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x003c }
                android.util.Log.i(r11, r12)     // Catch:{ Exception -> 0x003c }
                r0 = r16
                com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r11 = com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.this     // Catch:{ Exception -> 0x003c }
                com.xcharge.common.utils.HandlerTimer r11 = r11.handlerTimer     // Catch:{ Exception -> 0x003c }
                r12 = 60000(0xea60, double:2.9644E-319)
                r14 = 73745(0x12011, float:1.03339E-40)
                r15 = 0
                r11.startTimer(r12, r14, r15)     // Catch:{ Exception -> 0x003c }
                goto L_0x0009
            L_0x003c:
                r3 = move-exception
                java.lang.String r11 = "OcppPortHandler.handleMessage"
                java.lang.StringBuilder r12 = new java.lang.StringBuilder
                java.lang.String r13 = "except: "
                r12.<init>(r13)
                java.lang.String r13 = android.util.Log.getStackTraceString(r3)
                java.lang.StringBuilder r12 = r12.append(r13)
                java.lang.String r12 = r12.toString()
                android.util.Log.e(r11, r12)
                java.lang.StringBuilder r11 = new java.lang.StringBuilder
                java.lang.String r12 = "OcppPortHandler handleMessage exception: "
                r11.<init>(r12)
                java.lang.String r12 = android.util.Log.getStackTraceString(r3)
                java.lang.StringBuilder r11 = r11.append(r12)
                java.lang.String r11 = r11.toString()
                com.xcharge.common.utils.LogUtils.syslog(r11)
                goto L_0x0009
            L_0x006c:
                java.lang.String r11 = "OcppPortHandler.handleMessage"
                java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x003c }
                java.lang.String r13 = "service deactived !!! port: "
                r12.<init>(r13)     // Catch:{ Exception -> 0x003c }
                r0 = r16
                com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r13 = com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.this     // Catch:{ Exception -> 0x003c }
                java.lang.String r13 = r13.port     // Catch:{ Exception -> 0x003c }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x003c }
                java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x003c }
                android.util.Log.i(r11, r12)     // Catch:{ Exception -> 0x003c }
                r0 = r16
                com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r11 = com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.this     // Catch:{ Exception -> 0x003c }
                r11.clearPortActiveStatus()     // Catch:{ Exception -> 0x003c }
                goto L_0x0009
            L_0x0091:
                r0 = r17
                java.lang.Object r11 = r0.obj     // Catch:{ Exception -> 0x003c }
                r0 = r11
                org.json.JSONArray r0 = (org.json.JSONArray) r0     // Catch:{ Exception -> 0x003c }
                r5 = r0
                goto L_0x0009
            L_0x009b:
                r0 = r17
                java.lang.Object r11 = r0.obj     // Catch:{ Exception -> 0x003c }
                r0 = r11
                org.json.JSONArray r0 = (org.json.JSONArray) r0     // Catch:{ Exception -> 0x003c }
                r5 = r0
                r0 = r16
                com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r11 = com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.this     // Catch:{ Exception -> 0x003c }
                r11.handleFailedRequest(r5)     // Catch:{ Exception -> 0x003c }
                goto L_0x0009
            L_0x00ac:
                r0 = r17
                java.lang.Object r11 = r0.obj     // Catch:{ Exception -> 0x003c }
                r0 = r11
                org.json.JSONArray r0 = (org.json.JSONArray) r0     // Catch:{ Exception -> 0x003c }
                r5 = r0
                java.lang.String r11 = "OcppPortHandler.handleMessage"
                java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x003c }
                java.lang.String r13 = "send ocpp request timeout: "
                r12.<init>(r13)     // Catch:{ Exception -> 0x003c }
                java.lang.StringBuilder r12 = r12.append(r5)     // Catch:{ Exception -> 0x003c }
                java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x003c }
                android.util.Log.w(r11, r12)     // Catch:{ Exception -> 0x003c }
                r0 = r16
                com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r11 = com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.this     // Catch:{ Exception -> 0x003c }
                r11.handleFailedRequest(r5)     // Catch:{ Exception -> 0x003c }
                goto L_0x0009
            L_0x00d1:
                r0 = r17
                java.lang.Object r11 = r0.obj     // Catch:{ Exception -> 0x003c }
                r0 = r11
                org.json.JSONArray r0 = (org.json.JSONArray) r0     // Catch:{ Exception -> 0x003c }
                r5 = r0
                goto L_0x0009
            L_0x00db:
                r0 = r16
                com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r11 = com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.this     // Catch:{ Exception -> 0x003c }
                r12 = 0
                r11.sendMeterValuesReq(r12)     // Catch:{ Exception -> 0x003c }
                r0 = r16
                com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r11 = com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.this     // Catch:{ Exception -> 0x003c }
                com.xcharge.charger.protocol.ocpp.bean.OcppConfig r11 = r11.getOcppConfig()     // Catch:{ Exception -> 0x003c }
                java.util.HashMap r11 = r11.getMaps()     // Catch:{ Exception -> 0x003c }
                java.lang.String r12 = "MeterValueSampleInterval"
                java.lang.Object r6 = r11.get(r12)     // Catch:{ Exception -> 0x003c }
                java.lang.String r6 = (java.lang.String) r6     // Catch:{ Exception -> 0x003c }
                boolean r11 = android.text.TextUtils.isEmpty(r6)     // Catch:{ Exception -> 0x003c }
                if (r11 != 0) goto L_0x0009
                boolean r11 = android.text.TextUtils.isDigitsOnly(r6)     // Catch:{ Exception -> 0x003c }
                if (r11 == 0) goto L_0x0009
                int r4 = java.lang.Integer.parseInt(r6)     // Catch:{ Exception -> 0x003c }
                if (r4 <= 0) goto L_0x0009
                r0 = r16
                com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r11 = com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.this     // Catch:{ Exception -> 0x003c }
                com.xcharge.common.utils.HandlerTimer r11 = r11.handlerTimer     // Catch:{ Exception -> 0x003c }
                int r12 = r4 * 1000
                long r12 = (long) r12     // Catch:{ Exception -> 0x003c }
                r14 = 73737(0x12009, float:1.03328E-40)
                r15 = 0
                r11.startTimer(r12, r14, r15)     // Catch:{ Exception -> 0x003c }
                goto L_0x0009
            L_0x011d:
                r0 = r17
                java.lang.Object r11 = r0.obj     // Catch:{ Exception -> 0x003c }
                java.lang.Integer r11 = (java.lang.Integer) r11     // Catch:{ Exception -> 0x003c }
                int r2 = r11.intValue()     // Catch:{ Exception -> 0x003c }
                r0 = r16
                com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r11 = com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.this     // Catch:{ Exception -> 0x003c }
                r12 = 1
                r11.sendMeterValuesReq(r12)     // Catch:{ Exception -> 0x003c }
                r0 = r16
                com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r11 = com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.this     // Catch:{ Exception -> 0x003c }
                com.xcharge.common.utils.HandlerTimer r11 = r11.handlerTimer     // Catch:{ Exception -> 0x003c }
                int r12 = r2 * 1000
                long r12 = (long) r12     // Catch:{ Exception -> 0x003c }
                r14 = 73744(0x12010, float:1.03337E-40)
                java.lang.Integer r15 = java.lang.Integer.valueOf(r2)     // Catch:{ Exception -> 0x003c }
                r11.startTimer(r12, r14, r15)     // Catch:{ Exception -> 0x003c }
                goto L_0x0009
            L_0x0146:
                r0 = r16
                com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r11 = com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.this     // Catch:{ Exception -> 0x003c }
                r11.reportHistoryChargeRequest()     // Catch:{ Exception -> 0x003c }
                r0 = r16
                com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r11 = com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.this     // Catch:{ Exception -> 0x003c }
                com.xcharge.common.utils.HandlerTimer r11 = r11.handlerTimer     // Catch:{ Exception -> 0x003c }
                r12 = 60000(0xea60, double:2.9644E-319)
                r14 = 73745(0x12011, float:1.03339E-40)
                r15 = 0
                r11.startTimer(r12, r14, r15)     // Catch:{ Exception -> 0x003c }
                goto L_0x0009
            L_0x0161:
                r0 = r17
                java.lang.Object r11 = r0.obj     // Catch:{ Exception -> 0x003c }
                java.lang.Long r11 = (java.lang.Long) r11     // Catch:{ Exception -> 0x003c }
                long r8 = r11.longValue()     // Catch:{ Exception -> 0x003c }
                r0 = r16
                com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r11 = com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.this     // Catch:{ Exception -> 0x003c }
                long r12 = java.lang.System.currentTimeMillis()     // Catch:{ Exception -> 0x003c }
                r14 = 1000(0x3e8, double:4.94E-321)
                long r12 = r12 / r14
                r11.executeChargingProfile(r8, r12)     // Catch:{ Exception -> 0x003c }
                goto L_0x0009
            L_0x017b:
                r0 = r17
                java.lang.Object r11 = r0.obj     // Catch:{ Exception -> 0x003c }
                r0 = r11
                org.json.JSONArray r0 = (org.json.JSONArray) r0     // Catch:{ Exception -> 0x003c }
                r5 = r0
                r0 = r16
                com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r11 = com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.this     // Catch:{ Exception -> 0x003c }
                r11.handleRequest(r5)     // Catch:{ Exception -> 0x003c }
                goto L_0x0009
            L_0x018c:
                r0 = r17
                java.lang.Object r7 = r0.obj     // Catch:{ Exception -> 0x003c }
                com.xcharge.charger.protocol.ocpp.session.OcppRequestSession r7 = (com.xcharge.charger.protocol.ocpp.session.OcppRequestSession) r7     // Catch:{ Exception -> 0x003c }
                r0 = r16
                com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r11 = com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.this     // Catch:{ Exception -> 0x003c }
                org.json.JSONArray r12 = r7.getRequest()     // Catch:{ Exception -> 0x003c }
                org.json.JSONArray r13 = r7.getResponse()     // Catch:{ Exception -> 0x003c }
                r11.handleResponse(r12, r13)     // Catch:{ Exception -> 0x003c }
                goto L_0x0009
            L_0x01a3:
                r0 = r17
                java.lang.Object r11 = r0.obj     // Catch:{ Exception -> 0x003c }
                r0 = r11
                android.net.Uri r0 = (android.net.Uri) r0     // Catch:{ Exception -> 0x003c }
                r10 = r0
                java.lang.String r11 = "OcppPortHandler.handleMessage"
                java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x003c }
                java.lang.String r13 = "port charge status changed, port: "
                r12.<init>(r13)     // Catch:{ Exception -> 0x003c }
                r0 = r16
                com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r13 = com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.this     // Catch:{ Exception -> 0x003c }
                java.lang.String r13 = r13.port     // Catch:{ Exception -> 0x003c }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x003c }
                java.lang.String r13 = ", uri: "
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x003c }
                java.lang.String r13 = r10.toString()     // Catch:{ Exception -> 0x003c }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x003c }
                java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x003c }
                android.util.Log.i(r11, r12)     // Catch:{ Exception -> 0x003c }
                r0 = r16
                com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r11 = com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.this     // Catch:{ Exception -> 0x003c }
                r11.handlePortChargeStatusChanged(r10)     // Catch:{ Exception -> 0x003c }
                goto L_0x0009
            L_0x01de:
                r0 = r17
                java.lang.Object r11 = r0.obj     // Catch:{ Exception -> 0x003c }
                r0 = r11
                android.net.Uri r0 = (android.net.Uri) r0     // Catch:{ Exception -> 0x003c }
                r10 = r0
                java.lang.String r11 = r10.getPath()     // Catch:{ Exception -> 0x003c }
                java.lang.String r12 = "ports/fault/"
                boolean r11 = r11.contains(r12)     // Catch:{ Exception -> 0x003c }
                if (r11 == 0) goto L_0x0225
                java.lang.String r11 = "OcppPortHandler.handleMessage"
                java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x003c }
                java.lang.String r13 = "port recoverable faults changed, port: "
                r12.<init>(r13)     // Catch:{ Exception -> 0x003c }
                r0 = r16
                com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r13 = com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.this     // Catch:{ Exception -> 0x003c }
                java.lang.String r13 = r13.port     // Catch:{ Exception -> 0x003c }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x003c }
                java.lang.String r13 = ", uri: "
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x003c }
                java.lang.String r13 = r10.toString()     // Catch:{ Exception -> 0x003c }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x003c }
                java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x003c }
                android.util.Log.d(r11, r12)     // Catch:{ Exception -> 0x003c }
                r0 = r16
                com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r11 = com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.this     // Catch:{ Exception -> 0x003c }
                r11.handlePortRecoverableFaultChanged(r10)     // Catch:{ Exception -> 0x003c }
                goto L_0x0009
            L_0x0225:
                java.lang.String r11 = "OcppPortHandler.handleMessage"
                java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x003c }
                java.lang.String r13 = "port status changed, port: "
                r12.<init>(r13)     // Catch:{ Exception -> 0x003c }
                r0 = r16
                com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r13 = com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.this     // Catch:{ Exception -> 0x003c }
                java.lang.String r13 = r13.port     // Catch:{ Exception -> 0x003c }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x003c }
                java.lang.String r13 = ", uri: "
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x003c }
                java.lang.String r13 = r10.toString()     // Catch:{ Exception -> 0x003c }
                java.lang.StringBuilder r12 = r12.append(r13)     // Catch:{ Exception -> 0x003c }
                java.lang.String r12 = r12.toString()     // Catch:{ Exception -> 0x003c }
                android.util.Log.d(r11, r12)     // Catch:{ Exception -> 0x003c }
                r0 = r16
                com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler r11 = com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.this     // Catch:{ Exception -> 0x003c }
                r11.handlePortStatusChanged(r10)     // Catch:{ Exception -> 0x003c }
                goto L_0x0009
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.MsgHandler.handleMessage(android.os.Message):void");
        }
    }

    public void init(Context context2, String port2, OcppProtocolAgent protocolHandler) {
        this.context = context2;
        this.port = port2;
        this.thread = new HandlerThread("OcppPortHandler#" + this.port, 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(context2);
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

    /* access modifiers changed from: private */
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
        if (chargeId.equals(getChargeSession().getCharge_id())) {
            return this.port;
        }
        return null;
    }

    public boolean hasCharge(String chargeId) {
        return chargeId.equals(getChargeSession().getCharge_id());
    }

    /* access modifiers changed from: private */
    public void handleFailedRequest(JSONArray request) {
        try {
            String string = request.getString(2);
            switch (string.hashCode()) {
                case 77777212:
                    if (string.equals(OcppMessage.StartTransaction)) {
                        JSONObject attach = request.getJSONObject(4);
                        ChargeContentProxy.getInstance().setChargeStartReportedFlag(attach.getString("value"), 0);
                        int interval = 60;
                        String transactionMessageRetryInterval = getOcppConfig().getMaps().get(OcppMessage.TransactionMessageRetryInterval);
                        if (!TextUtils.isEmpty(transactionMessageRetryInterval) && TextUtils.isDigitsOnly(transactionMessageRetryInterval) && Integer.parseInt(transactionMessageRetryInterval) > 0) {
                            interval = Integer.parseInt(transactionMessageRetryInterval);
                        }
                        if (!attach.getBoolean("isHist") && CHARGE_STATUS.CHARGING.equals(this.status)) {
                            request.put(1, OcppProtocolAgent.getInstance().genSeq());
                            OcppProtocolAgent.getInstance().sendMessage(request, interval * 1000);
                            return;
                        }
                        return;
                    }
                    return;
                case 641037660:
                    if (string.equals(OcppMessage.StopTransaction)) {
                        ChargeContentProxy.getInstance().setChargeStopReportedFlag(request.getJSONObject(4).getString("value"), 0);
                        return;
                    }
                    return;
                default:
                    return;
            }
        } catch (Exception e) {
            Log.w("OcppPortHandler.handleFailedRequest", Log.getStackTraceString(e));
        }
        Log.w("OcppPortHandler.handleFailedRequest", Log.getStackTraceString(e));
    }

    /* access modifiers changed from: private */
    public void handlePortRecoverableFaultChanged(Uri uri) {
        List<String> segments = uri.getPathSegments();
        int size = segments.size();
        String last = segments.get(size - 1);
        if (TextUtils.isDigitsOnly(last)) {
            String status2 = segments.get(size - 2);
            if ("new".equals(status2)) {
                sendStatusNotificationReq(false, last);
            } else if (!"remove".equals(status2)) {
            } else {
                if (Integer.parseInt(last) == 200) {
                    sendStatusNotificationReq(false, last);
                } else {
                    sendStatusNotificationReq(false, (String) null);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handlePortStatusChanged(Uri uri) {
        if (uri.getPath().contains("ports/" + this.port + "/plugin")) {
            sendStatusNotificationReq(false, (String) null);
        }
    }

    /* access modifiers changed from: private */
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
                OcppChargeSession chargeSession2 = getChargeSession();
                String chargeId2 = portStatus.getCharge_id();
                ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(chargeId2);
                if (chargeBill != null) {
                    chargeSession2.setCharge_id(chargeId2);
                    chargeSession2.setUser_type(chargeBill.getUser_type());
                    chargeSession2.setUser_code(chargeBill.getUser_code());
                    chargeSession2.setInit_type(chargeBill.getInit_type());
                    chargeSession2.setUser_tc_type(chargeBill.getUser_tc_type());
                    chargeSession2.setUser_tc_value(chargeBill.getUser_tc_value());
                    chargeSession2.setUser_balance(chargeBill.getUser_balance());
                    chargeSession2.setIs_free(chargeBill.getIs_free());
                    chargeSession2.setBinded_user(chargeBill.getBinded_user());
                    chargeSession2.setCharge_platform(chargeBill.getCharge_platform());
                } else {
                    Log.w("OcppPortHandler.handlePortChargeStatusChanged", "failed to query info for charge: " + chargeId2);
                }
            } else if (chargeStatus.equals(CHARGE_STATUS.CHARGING)) {
                Log.i("OcppPortHandler.handlePortChargeStatusChanged", "enter charging status !!!");
                OcppChargeSession chargeSession3 = getChargeSession();
                String chargeId3 = chargeSession3.getCharge_id();
                if (TextUtils.isEmpty(chargeId3)) {
                    chargeId3 = portStatus.getCharge_id();
                    ChargeBill chargeBill2 = ChargeContentProxy.getInstance().getChargeBill(chargeId3);
                    if (chargeBill2 != null || TextUtils.isEmpty(chargeSession3.getUser_type())) {
                        chargeSession3.setCharge_id(chargeId3);
                        chargeSession3.setUser_type(chargeBill2.getUser_type());
                        chargeSession3.setUser_code(chargeBill2.getUser_code());
                        chargeSession3.setInit_type(chargeBill2.getInit_type());
                        chargeSession3.setUser_tc_type(chargeBill2.getUser_tc_type());
                        chargeSession3.setUser_tc_value(chargeBill2.getUser_tc_value());
                        chargeSession3.setUser_balance(chargeBill2.getUser_balance());
                        chargeSession3.setIs_free(chargeBill2.getIs_free());
                        chargeSession3.setBinded_user(chargeBill2.getBinded_user());
                        chargeSession3.setCharge_platform(chargeBill2.getCharge_platform());
                    } else {
                        Log.w("OcppPortHandler.handlePortChargeStatusChanged", "failed to query info for charge: " + chargeId3);
                    }
                }
                ChargeBill cb = ChargeContentProxy.getInstance().getChargeBill(chargeId3);
                sendStartTransactionReq(chargeId3, false);
                String meterValueSampleInterval = getOcppConfig().getMaps().get(OcppMessage.MeterValueSampleInterval);
                if (!TextUtils.isEmpty(meterValueSampleInterval) && TextUtils.isDigitsOnly(meterValueSampleInterval) && (interval2 = Integer.parseInt(meterValueSampleInterval)) > 0) {
                    this.handlerTimer.startTimer((long) (interval2 * 1000), 73737, (Object) null);
                }
                String clockAlignedDataInterval = getOcppConfig().getMaps().get(OcppMessage.ClockAlignedDataInterval);
                if (!TextUtils.isEmpty(clockAlignedDataInterval) && TextUtils.isDigitsOnly(clockAlignedDataInterval) && (interval = Integer.parseInt(clockAlignedDataInterval)) > 0) {
                    this.handlerTimer.startTimer((((long) interval) - ((System.currentTimeMillis() / 1000) % ((long) interval))) * 1000, 73744, Integer.valueOf(interval));
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
            sendStatusNotificationReq(false, (String) null);
        }
    }

    /* access modifiers changed from: private */
    public void executeChargingProfile(long startTime, long nowTime) {
        try {
            NowLimitContext nowLimitContext = getLimitFromChargingProfiles(startTime, nowTime);
            C2DeviceProxy.getInstance().ajustChargeAmp(this.port, (int) nowLimitContext.getLimit());
            Long nextTime = nowLimitContext.getNextTime();
            if (nextTime != null) {
                this.handlerTimer.startTimer(1000 * (nextTime.longValue() - nowTime), 73746, Long.valueOf(startTime));
            }
        } catch (Exception e) {
            Log.w("OcppPortHandler.executeChargingProfile", Log.getStackTraceString(e));
        }
    }

    private NowLimitContext getLimitFromChargingProfiles(long startTime, long nowTime) {
        OcppChargeSession chargeSession2 = getChargeSession();
        NowLimitContext nowLimitContext = new NowLimitContext();
        nowLimitContext.setLimit((double) ChargeStatusCacheProvider.getInstance().getAdjustAmp());
        ChargingProfile maxChargingProfile = getMaxStackLevel(OcppProtocolAgent.getInstance().getOcppConfig().getMaxChargingProfiles().get("0"), nowTime, (Long) null);
        long j = nowTime;
        ChargingProfile defChargingProfile = getMaxStackLevel(OcppProtocolAgent.getInstance().getOcppConfig().getDefChargingProfiles().get(this.port), j, Long.valueOf(startTime));
        long j2 = nowTime;
        ChargingProfile txChargingProfile = getMaxStackLevel(chargeSession2.getTxChargingProfiles().get(this.port), j2, Long.valueOf(startTime));
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
        int offset = (int) (nowTime - ((long) Integer.parseInt(startSchedule)));
        NowLimitContext nowLimitContext = new NowLimitContext();
        nowLimitContext.setLimit((double) ChargeStatusCacheProvider.getInstance().getAdjustAmp());
        int i = 0;
        while (true) {
            if (i >= chargingSchedulePeriod.size()) {
                break;
            }
            if (i != chargingSchedulePeriod.size() - 1) {
                if (chargingSchedulePeriod.get(i).getStartPeriod() <= offset && offset < chargingSchedulePeriod.get(i + 1).getStartPeriod()) {
                    nowLimitContext.setLimit(getMinLimit(chargingSchedulePeriod.get(i).getLimit()));
                    nowLimitContext.setNextTime(Long.valueOf(Long.parseLong(chargingSchedule.getStartSchedule()) + ((long) chargingSchedulePeriod.get(i + 1).getStartPeriod())));
                    break;
                }
            } else if (duration == null) {
                if (chargingSchedulePeriod.get(i).getStartPeriod() <= offset) {
                    nowLimitContext.setLimit(getMinLimit(chargingSchedulePeriod.get(i).getLimit()));
                    break;
                }
            } else if (chargingSchedulePeriod.get(i).getStartPeriod() <= offset && offset < duration.intValue()) {
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
        return nowLimitContext;
    }

    private NowLimitContext twoChargingProfile(ChargingProfile maxChargingProfile, ChargingProfile txChargingProfile, long nowTime, long startTime) {
        String chargingProfileKind = maxChargingProfile.getChargingProfileKind();
        String recurrencyKind = maxChargingProfile.getRecurrencyKind();
        ChargingSchedule maxChargingSchedule = maxChargingProfile.getChargingSchedule();
        Integer duration = maxChargingSchedule.getDuration();
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
        nowLimitContext.setLimit((double) ChargeStatusCacheProvider.getInstance().getAdjustAmp());
        int i = 0;
        loop0:
        while (true) {
            if (i >= maxChargingSchedulePeriod.size()) {
                break;
            }
            for (int j = 0; j < txChargingSchedulePeriod.size(); j++) {
                if (i == maxChargingSchedulePeriod.size() - 1 && j != txChargingSchedulePeriod.size() - 1) {
                    nowLimitContext.setLimit(getMinLimit(maxChargingSchedulePeriod.get(i).getLimit(), txChargingSchedulePeriod.get(j).getLimit()));
                    nowLimitContext.setNextTime(Long.valueOf(Long.parseLong(txStartSchedule) + ((long) txChargingSchedulePeriod.get(j + 1).getStartPeriod())));
                } else if ((i == maxChargingSchedulePeriod.size() - 1 || j != txChargingSchedulePeriod.size() - 1) && !(i == maxChargingSchedulePeriod.size() - 1 && j == txChargingSchedulePeriod.size() - 1)) {
                    if (((long) maxChargingSchedulePeriod.get(i).getStartPeriod()) <= maxOffset && maxOffset < ((long) maxChargingSchedulePeriod.get(i + 1).getStartPeriod()) && ((long) txChargingSchedulePeriod.get(j).getStartPeriod()) <= txOffset && txOffset < ((long) txChargingSchedulePeriod.get(j + 1).getStartPeriod())) {
                        nowLimitContext.setLimit(getMinLimit(maxChargingSchedulePeriod.get(i).getLimit(), txChargingSchedulePeriod.get(j).getLimit()));
                        long maxNextTime = Long.parseLong(maxStartSchedule) + ((long) maxChargingSchedulePeriod.get(i + 1).getStartPeriod());
                        long txNextTime = Long.parseLong(txStartSchedule) + ((long) txChargingSchedulePeriod.get(j + 1).getStartPeriod());
                        if (maxNextTime < txNextTime) {
                            nowLimitContext.setNextTime(Long.valueOf(maxNextTime));
                        } else {
                            nowLimitContext.setNextTime(Long.valueOf(txNextTime));
                        }
                    }
                } else if (txDuration == null) {
                    if (((long) txChargingSchedulePeriod.get(i).getStartPeriod()) <= txOffset) {
                        nowLimitContext.setLimit(getMinLimit(maxChargingSchedulePeriod.get(i).getLimit(), txChargingSchedulePeriod.get(j).getLimit()));
                        break loop0;
                    }
                } else if (((long) txChargingSchedulePeriod.get(i).getStartPeriod()) <= txOffset && txOffset < ((long) txDuration.intValue())) {
                    nowLimitContext.setLimit(getMinLimit(maxChargingSchedulePeriod.get(i).getLimit(), txChargingSchedulePeriod.get(j).getLimit()));
                    if (ChargingProfileKindType.Recurring.equals(txChargingProfileKind)) {
                        if (RecurrencyKindType.Daily.equals(txRecurrencyKind)) {
                            nowLimitContext.setNextTime(Long.valueOf(Long.parseLong(txStartSchedule) + 86400));
                        } else if (RecurrencyKindType.Weekly.equals(txRecurrencyKind)) {
                            nowLimitContext.setNextTime(Long.valueOf(Long.parseLong(txStartSchedule) + 604800));
                        }
                    }
                }
            }
            i++;
        }
        return nowLimitContext;
    }

    private double getMinLimit(double limit) {
        int adjustAmp = ChargeStatusCacheProvider.getInstance().getAdjustAmp();
        return limit < ((double) adjustAmp) ? limit : (double) adjustAmp;
    }

    private double getMinLimit(double maxLimit, double txLimit) {
        int adjustAmp = ChargeStatusCacheProvider.getInstance().getAdjustAmp();
        if (txLimit < maxLimit && maxLimit < ((double) adjustAmp)) {
            return txLimit;
        }
        if (txLimit <= maxLimit || maxLimit >= ((double) adjustAmp)) {
            return (double) adjustAmp;
        }
        return maxLimit;
    }

    private ChargingProfile getMaxStackLevel(ArrayList<ChargingProfile> chargingProfiles, long nowTime, Long startTime) {
        Integer duration;
        if (chargingProfiles == null || chargingProfiles.size() == 0) {
            return null;
        }
        ArrayList<ChargingProfile> clonedChargingProfiles = new ArrayList<>();
        Iterator<ChargingProfile> it = chargingProfiles.iterator();
        while (it.hasNext()) {
            clonedChargingProfiles.add((ChargingProfile) it.next().deepClone());
        }
        ArrayList<ChargingProfile> validChargingProfile = new ArrayList<>();
        int i = 0;
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
                i++;
            }
            String chargingProfileKind = tmp.getChargingProfileKind();
            ChargingSchedule chargingSchedule = tmp.getChargingSchedule();
            String startSchedule = chargingSchedule.getStartSchedule();
            if (!TextUtils.isEmpty(startSchedule)) {
                chargingSchedule.setStartSchedule(String.valueOf(TimeUtils.getTsFromISO8601Format(startSchedule) / 1000));
            } else if (!ChargingProfileKindType.Relative.equals(chargingProfileKind)) {
                if (ChargingProfileKindType.Recurring.equals(chargingProfileKind)) {
                    String recurrencyKind = tmp.getRecurrencyKind();
                    if (!TextUtils.isEmpty(recurrencyKind)) {
                        if (RecurrencyKindType.Daily.equals(recurrencyKind)) {
                            chargingSchedule.setDuration(86400);
                            chargingSchedule.setStartSchedule(String.valueOf((TimeUtils.getTomorrowAt(0, 0, 0) / 1000) - 86400));
                        } else if (RecurrencyKindType.Weekly.equals(recurrencyKind)) {
                            chargingSchedule.setDuration(604800);
                            chargingSchedule.setStartSchedule(String.valueOf(((TimeUtils.getTomorrowAt(0, 0, 0) / 1000) - 86400) - ((long) ((convertWeek() - 1) * 86400))));
                        }
                    }
                }
                i++;
            } else if (startTime != null) {
                chargingSchedule.setStartSchedule(String.valueOf(startTime));
            } else {
                i++;
            }
            if ((ChargingProfileKindType.Absolute.equals(chargingProfileKind) || ChargingProfileKindType.Relative.equals(chargingProfileKind)) && (duration = chargingSchedule.getDuration()) != null && nowTime - ((long) Integer.parseInt(chargingSchedule.getStartSchedule())) > ((long) duration.intValue())) {
                i++;
            } else {
                validChargingProfile.add(tmp);
                i++;
            }
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
        if (!"W".equals(schedule.getChargingRateUnit())) {
            return chargingProfile;
        }
        schedule.setChargingRateUnit("A");
        Iterator<ChargingSchedulePeriod> it2 = chargingProfile.getChargingSchedule().getChargingSchedulePeriod().iterator();
        while (it2.hasNext()) {
            ChargingSchedulePeriod period = it2.next();
            period.setLimit(period.getLimit() / ((double) (getPhase() * FTPCodes.SERVICE_READY_FOR_NEW_USER)));
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
                startTransactionReq.setReservationId(Integer.valueOf(((ReserveNowReq) new ReserveNowReq().fromJson(reserve.getJSONObject(3).toString())).getReservationId()));
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
                    switch (m35x26790b25()[stop_cause.ordinal()]) {
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
                        case PortRuntimeData.STATUS_EX_11:
                            reason = Reason.Reboot;
                            break;
                    }
                    stopTransactionReq.setReason(reason);
                }
                new ArrayList();
                if (isHist) {
                    stopTxnMeterValues = (ArrayList) JsonBean.getGsonBuilder().create().fromJson(chargeBill.getAttach_data(), new TypeToken<List<MeterValue>>() {
                    }.getType());
                } else {
                    stopTxnMeterValues = getChargeSession().getStopTxnMeterValues();
                    ChargeContentProxy.getInstance().setChargeAttchData(chargeId, JsonBean.getGsonBuilder().create().toJson((Object) stopTxnMeterValues));
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
        String ocppErrorCode;
        Double volt2;
        try {
            StatusNotificationReq statusNotificationReq = new StatusNotificationReq();
            statusNotificationReq.setConnectorId(Integer.parseInt(this.port));
            ErrorCode error = HardwareStatusCacheProvider.getInstance().getPort(this.port).getDeviceError();
            String ocppErrorCode2 = ChargePointErrorCode.OtherError;
            String vendorErrorCode = null;
            Port errPortStatus = null;
            HashMap<String, Object> data = error.getData();
            if (data != null) {
                errPortStatus = (Port) new Port().fromJson((String) data.get("portStatus"));
            }
            if (!TextUtils.isEmpty(code)) {
                int errCode = Integer.parseInt(code);
                if (errCode == 200 || errCode == 50019) {
                    statusNotificationReq.setErrorCode(ChargePointErrorCode.NoError);
                } else {
                    switch (errCode) {
                        case ErrorCode.EC_DEVICE_NOT_INIT:
                            vendorErrorCode = "EC_DEVICE_NOT_INIT";
                            break;
                        case ErrorCode.EC_DEVICE_NO_GROUND:
                            ocppErrorCode2 = ChargePointErrorCode.GroundFailure;
                            break;
                        case ErrorCode.EC_DEVICE_LOST_PHASE:
                            vendorErrorCode = "EC_DEVICE_LOST_PHASE";
                            break;
                        case ErrorCode.EC_DEVICE_EMERGENCY_STOP:
                            vendorErrorCode = "EC_DEVICE_EMERGENCY_STOP";
                            break;
                        case ErrorCode.EC_DEVICE_VOLT_ERROR:
                            vendorErrorCode = "EC_DEVICE_VOLT_ERROR";
                            if (errPortStatus != null) {
                                ArrayList<Double> volts2 = errPortStatus.getVolts();
                                if (!(volts2 == null || (volt2 = volts2.get(0)) == null)) {
                                    if (volt2.doubleValue() > 220.0d) {
                                        ocppErrorCode2 = "OverVoltage";
                                    } else {
                                        ocppErrorCode2 = ChargePointErrorCode.UnderVoltage;
                                    }
                                    vendorErrorCode = null;
                                    break;
                                }
                            }
                            break;
                        case ErrorCode.EC_DEVICE_AMP_ERROR:
                            ocppErrorCode2 = ChargePointErrorCode.OverCurrentFailure;
                            break;
                        case ErrorCode.EC_DEVICE_TEMP_ERROR:
                            ocppErrorCode2 = ChargePointErrorCode.HighTemperature;
                            break;
                        case ErrorCode.EC_DEVICE_POWER_LEAK:
                            vendorErrorCode = "EC_DEVICE_POWER_LEAK";
                            break;
                        case ErrorCode.EC_DEVICE_COMM_ERROR:
                            ocppErrorCode2 = ChargePointErrorCode.EVCommunicationError;
                            break;
                    }
                    if (ChargePointErrorCode.OtherError.equals(ocppErrorCode2)) {
                        statusNotificationReq.setErrorCode(ocppErrorCode2);
                        statusNotificationReq.setVendorErrorCode(vendorErrorCode);
                    } else {
                        statusNotificationReq.setErrorCode(ocppErrorCode2);
                    }
                }
            } else if (200 != error.getCode()) {
                switch (error.getCode()) {
                    case ErrorCode.EC_DEVICE_NOT_INIT:
                        vendorErrorCode = "EC_DEVICE_NOT_INIT";
                        break;
                    case ErrorCode.EC_DEVICE_NO_GROUND:
                        ocppErrorCode2 = ChargePointErrorCode.GroundFailure;
                        break;
                    case ErrorCode.EC_DEVICE_LOST_PHASE:
                        vendorErrorCode = "EC_DEVICE_LOST_PHASE";
                        break;
                    case ErrorCode.EC_DEVICE_EMERGENCY_STOP:
                        vendorErrorCode = "EC_DEVICE_EMERGENCY_STOP";
                        break;
                    case ErrorCode.EC_DEVICE_VOLT_ERROR:
                        vendorErrorCode = "EC_DEVICE_VOLT_ERROR";
                        if (!(errPortStatus == null || (volts = errPortStatus.getVolts()) == null || (volt = volts.get(0)) == null)) {
                            if (volt.doubleValue() > 220.0d) {
                                ocppErrorCode = "OverVoltage";
                            } else {
                                ocppErrorCode = ChargePointErrorCode.UnderVoltage;
                            }
                            vendorErrorCode = null;
                            break;
                        }
                    case ErrorCode.EC_DEVICE_AMP_ERROR:
                        ocppErrorCode2 = ChargePointErrorCode.OverCurrentFailure;
                        break;
                    case ErrorCode.EC_DEVICE_TEMP_ERROR:
                        ocppErrorCode2 = ChargePointErrorCode.HighTemperature;
                        break;
                    case ErrorCode.EC_DEVICE_POWER_LEAK:
                        vendorErrorCode = "EC_DEVICE_POWER_LEAK";
                        break;
                    case ErrorCode.EC_DEVICE_COMM_ERROR:
                        ocppErrorCode2 = ChargePointErrorCode.EVCommunicationError;
                        break;
                }
                if (ChargePointErrorCode.OtherError.equals(ocppErrorCode2)) {
                    statusNotificationReq.setErrorCode(ocppErrorCode2);
                    statusNotificationReq.setVendorErrorCode(vendorErrorCode);
                } else {
                    statusNotificationReq.setErrorCode(ocppErrorCode2);
                }
            } else {
                statusNotificationReq.setErrorCode(ChargePointErrorCode.NoError);
            }
            statusNotificationReq.setTimestamp(TimeUtils.getISO8601Format(System.currentTimeMillis(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone()));
            String status2 = null;
            if (!ChargeStatusCacheProvider.getInstance().getPortSwitch(this.port)) {
                status2 = "Unavailable";
            } else if (error.getCode() == 200) {
                switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STATUS()[this.status.ordinal()]) {
                    case 1:
                        if (!HardwareStatusCacheProvider.getInstance().getPortPluginStatus(this.port)) {
                            status2 = ChargePointStatus.Available;
                            break;
                        } else {
                            status2 = ChargePointStatus.Preparing;
                            break;
                        }
                    case 2:
                        HashMap<Integer, JSONArray> reserveNow = OcppProtocolAgent.getInstance().getReserveNow();
                        if (reserveNow != null && reserveNow.size() > 0) {
                            status2 = ChargePointStatus.Reserved;
                            break;
                        } else {
                            status2 = ChargePointStatus.Preparing;
                            break;
                        }
                        break;
                    case 3:
                        if (!getChargeSession().isEvSuspendStatus()) {
                            status2 = ChargePointStatus.Charging;
                            break;
                        } else {
                            status2 = ChargePointStatus.SuspendedEV;
                            break;
                        }
                    case 4:
                        status2 = ChargePointStatus.Finishing;
                        break;
                }
            } else {
                status2 = "Faulted";
            }
            statusNotificationReq.setStatus(status2);
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

    /* access modifiers changed from: private */
    public void handleRequest(JSONArray jsonArray) {
        try {
            String string = jsonArray.getString(2);
            switch (string.hashCode()) {
                case -1013156522:
                    if (string.equals(OcppMessage.SetChargingProfile)) {
                        handleSetChargingProfileReq(jsonArray);
                        return;
                    }
                    return;
                case -977722974:
                    if (string.equals(OcppMessage.RemoteStartTransaction)) {
                        OcppDCAPGateway.getInstance().sendMessage(OcppDCAPGateway.getInstance().obtainMessage(77827, jsonArray));
                        return;
                    }
                    return;
                case -964284085:
                    if (string.equals(OcppMessage.ChangeAvailability)) {
                        handleChangeAvailabilityReq(jsonArray);
                        return;
                    }
                    return;
                case -362842058:
                    if (!string.equals(OcppMessage.RemoteStopTransaction)) {
                        return;
                    }
                    if (String.valueOf(((RemoteStopTransactionReq) new RemoteStopTransactionReq().fromJson(jsonArray.getJSONObject(3).toString())).getTransactionId()).equals(getChargeSession().getTransactionId())) {
                        OcppDCAPGateway.getInstance().sendMessage(OcppDCAPGateway.getInstance().obtainMessage(77827, jsonArray));
                        return;
                    } else {
                        OcppProtocolAgent.getInstance().sendRemoteStopTransactionConf(jsonArray.getString(1), "Rejected");
                        return;
                    }
                default:
                    return;
            }
        } catch (Exception e) {
            Log.w("OcppPortHandler.handleRequest", Log.getStackTraceString(e));
        }
        Log.w("OcppPortHandler.handleRequest", Log.getStackTraceString(e));
    }

    private void handleChangeAvailabilityReq(JSONArray jsonArray) {
        String status2;
        try {
            String type = ((ChangeAvailabilityReq) new ChangeAvailabilityReq().fromJson(jsonArray.getJSONObject(3).toString())).getType();
            boolean enable = true;
            if (!TextUtils.isEmpty(type)) {
                if (AvailabilityType.Inoperative.equals(type)) {
                    enable = false;
                } else if (AvailabilityType.Operative.equals(type)) {
                    enable = true;
                }
            }
            if (!this.status.equals(CHARGE_STATUS.IDLE)) {
                status2 = AvailabilityStatus.Scheduled;
            } else {
                status2 = "Accepted";
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
            changeAvailabilityConf.setStatus(status2);
            sendMessage(createConfMessage(jsonArray.getString(1), OcppMessage.ChangeAvailability, changeAvailabilityConf.toJson()));
            sendStatusNotificationReq(false, (String) null);
        } catch (Exception e) {
            Log.w("OcppPortHandler.handleChangeAvailabilityReq", Log.getStackTraceString(e));
        }
    }

    private void handleSetChargingProfileReq(JSONArray jsonArray) {
        try {
            OcppChargeSession chargeSession2 = getChargeSession();
            SetChargingProfileReq setChargingProfileReq = (SetChargingProfileReq) new SetChargingProfileReq().fromJson(jsonArray.getJSONObject(3).toString());
            int connectorId = setChargingProfileReq.getConnectorId();
            ChargingProfile csChargingProfiles = setChargingProfileReq.getCsChargingProfiles();
            HashMap<String, ArrayList<ChargingProfile>> txChargingProfiles = chargeSession2.getTxChargingProfiles();
            ArrayList<ChargingProfile> chargingProfiles = new ArrayList<>();
            if (txChargingProfiles.size() > 0) {
                chargingProfiles = txChargingProfiles.get(this.port);
            }
            String status2 = "Rejected";
            if (CHARGE_STATUS.CHARGING.equals(this.status) && this.port.equals(String.valueOf(connectorId)) && ChargingProfilePurposeType.TxProfile.equals(csChargingProfiles.getChargingProfilePurpose())) {
                String chargeId = chargeSession2.getCharge_id();
                if (!TextUtils.isEmpty(chargeId)) {
                    ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(chargeId);
                    String transactionId = chargeBill.getCloud_charge_id();
                    if (!TextUtils.isEmpty(transactionId) && transactionId.equals(Integer.valueOf(csChargingProfiles.getTransactionId()))) {
                        status2 = "Accepted";
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
                        chargeSession2.setTxChargingProfiles(txChargingProfiles);
                        executeChargingProfile(chargeBill.getStart_time() / 1000, System.currentTimeMillis() / 1000);
                    }
                }
            }
            OcppProtocolAgent.getInstance().sendSetChargingProfileConf(jsonArray.getString(1), status2);
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
            OcppChargeSession chargeSession2 = getChargeSession();
            String chargeId = chargeSession2.getCharge_id();
            String userType = chargeSession2.getUser_type();
            String userCode = chargeSession2.getUser_code();
            if (!TextUtils.isEmpty(chargeId)) {
                CAPDirectiveOption opt = new CAPDirectiveOption();
                opt.setCharge_id(chargeId);
                DCAPProxy.getInstance().sendRequest(DCAPAdapter.createRequest("user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode, "stop", opt, new StopDirective()));
            }
        } catch (Exception e) {
            Log.w("OcppPortHandler.stopCharge", Log.getStackTraceString(e));
        }
    }

    /* access modifiers changed from: private */
    public void handleResponse(JSONArray request, JSONArray response) {
        try {
            String string = request.getString(2);
            switch (string.hashCode()) {
                case 77777212:
                    if (string.equals(OcppMessage.StartTransaction)) {
                        StartTransactionConf startTransactionConf = (StartTransactionConf) new StartTransactionConf().fromJson(response.getJSONObject(2).toString());
                        OcppProtocolAgent.getInstance().addIdTagInfoCache(((StartTransactionReq) new StartTransactionReq().fromJson(request.getJSONObject(3).toString())).getIdTag(), startTransactionConf.getIdTagInfo());
                        getChargeSession().setTransactionId(String.valueOf(startTransactionConf.getTransactionId()));
                        JSONObject attach = request.getJSONObject(4);
                        String chargeId = attach.getString("value");
                        boolean isHist = attach.getBoolean("isHist");
                        ChargeContentProxy.getInstance().setCloudChargeId(chargeId, String.valueOf(startTransactionConf.getTransactionId()));
                        ChargeContentProxy.getInstance().setChargeStartReportedFlag(chargeId, 1);
                        if (!"Accepted".equals(startTransactionConf.getIdTagInfo().getStatus()) && !isHist) {
                            String stopTransactionOnInvalidId = getOcppConfig().getMaps().get(OcppMessage.StopTransactionOnInvalidId);
                            if (TextUtils.isEmpty(stopTransactionOnInvalidId) || !Boolean.valueOf(stopTransactionOnInvalidId).booleanValue()) {
                                String maxEnergyOnInvalidId = getOcppConfig().getMaps().get(OcppMessage.MaxEnergyOnInvalidId);
                                if (!TextUtils.isEmpty(maxEnergyOnInvalidId) && TextUtils.isDigitsOnly(maxEnergyOnInvalidId)) {
                                    int maxEnergy = Integer.parseInt(maxEnergyOnInvalidId);
                                    if (maxEnergy > 0) {
                                        OcppRequestSession ocppRequestSession = new OcppRequestSession();
                                        ocppRequestSession.setRequest(request);
                                        ocppRequestSession.setResponse(response);
                                        OcppDCAPGateway.getInstance().sendMessage(OcppDCAPGateway.getInstance().obtainMessage(77828, ocppRequestSession));
                                        return;
                                    } else if (maxEnergy == 0) {
                                        stopCharge();
                                        return;
                                    } else {
                                        return;
                                    }
                                } else {
                                    return;
                                }
                            } else {
                                stopCharge();
                                return;
                            }
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                case 641037660:
                    if (string.equals(OcppMessage.StopTransaction)) {
                        OcppProtocolAgent.getInstance().addIdTagInfoCache(((StopTransactionReq) new StopTransactionReq().fromJson(request.getJSONObject(3).toString())).getIdTag(), ((StopTransactionConf) new StopTransactionConf().fromJson(response.getJSONObject(2).toString())).getIdTagInfo());
                        ChargeContentProxy.getInstance().setChargeStopReportedFlag(request.getJSONObject(4).getString("value"), 1);
                        if (OcppProtocolAgent.getInstance().isResetOnTxStopped()) {
                            OcppProtocolAgent.getInstance().setResetOnTxStopped(false);
                            DCAPAdapter.reset();
                            return;
                        }
                        return;
                    }
                    return;
                default:
                    return;
            }
        } catch (Exception e) {
            Log.w("OcppPortHandler.handleResponse", Log.getStackTraceString(e));
        }
        Log.w("OcppPortHandler.handleResponse", Log.getStackTraceString(e));
    }

    public void sendMeterValuesReq(boolean isClockAligned) {
        try {
            MeterValuesReq meterValuesReq = new MeterValuesReq();
            meterValuesReq.setConnectorId(Integer.parseInt(this.port));
            ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(getChargeSession().getCharge_id());
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
            sendMessage(createReqMessage(OcppProtocolAgent.getInstance().genSeq(), "MeterValues", meterValuesReq.toJson()));
        } catch (Exception e) {
            Log.w("OcppPortHandler.sendMeterValuesReq", Log.getStackTraceString(e));
        }
    }

    /* access modifiers changed from: private */
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
                    ArrayList<MeterValue> stopTxnMeterValues = getChargeSession().getStopTxnMeterValues();
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
            int remainder = (int) (TimeUtils.getHHmmSeconds(System.currentTimeMillis()) % ((long) interval));
            currentTime = remainder < interval / 2 ? currentTime - ((long) (remainder * 1000)) : currentTime + ((long) ((interval - remainder) * 1000));
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
                    OcppChargeSession chargeSession2 = getChargeSession();
                    String chargeId = chargeSession2.getCharge_id();
                    ArrayList<MeterValue> stopTxnMeterValues = chargeSession2.getStopTxnMeterValues();
                    if (maxLength2 > 0) {
                        stopTxnAlignedMeterValue.setSampledValue(getSampledValuesDataMaxLength(portStatus, stopTxnAlignedDatas, maxLength2, ReadingContext.SampleClock));
                        stopTxnMeterValues.add(stopTxnAlignedMeterValue);
                    } else {
                        stopTxnAlignedMeterValue.setSampledValue(getSampledValues(portStatus, stopTxnAlignedDatas, ReadingContext.SampleClock));
                        stopTxnMeterValues.add(stopTxnAlignedMeterValue);
                    }
                    if (!TextUtils.isEmpty(chargeId)) {
                        ChargeContentProxy.getInstance().setChargeAttchData(chargeId, JsonBean.getGsonBuilder().create().toJson((Object) stopTxnMeterValues));
                    }
                }
            }
        }
        return meterValues;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0031, code lost:
        continue;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.util.ArrayList<com.xcharge.charger.protocol.ocpp.bean.types.SampledValue> getSampledValuesDataMaxLength(com.xcharge.charger.data.bean.status.PortStatus r17, java.lang.String[] r18, int r19, java.lang.String r20) {
        /*
            r16 = this;
            java.util.ArrayList r8 = new java.util.ArrayList
            r8.<init>()
            java.lang.Double r9 = r17.getTemprature()
            java.util.ArrayList r3 = r17.getAmps()
            java.util.ArrayList r10 = r17.getVolts()
            java.lang.Double r7 = r17.getKwatt()
            java.lang.Double r2 = r17.getAmmeter()
            r4 = 0
        L_0x001a:
            r0 = r18
            int r11 = r0.length
            if (r4 < r11) goto L_0x0020
        L_0x001f:
            return r8
        L_0x0020:
            int r11 = r8.size()
            r0 = r19
            if (r11 >= r0) goto L_0x0031
            r11 = r18[r4]
            int r12 = r11.hashCode()
            switch(r12) {
                case -1997933762: goto L_0x0034;
                case -210256374: goto L_0x007b;
                case 107981306: goto L_0x00b2;
                case 567329348: goto L_0x00fb;
                case 1989569876: goto L_0x0125;
                default: goto L_0x0031;
            }
        L_0x0031:
            int r4 = r4 + 1
            goto L_0x001a
        L_0x0034:
            java.lang.String r12 = "Voltage"
            boolean r11 = r11.equals(r12)
            if (r11 == 0) goto L_0x0031
            if (r10 == 0) goto L_0x0031
            r6 = 0
        L_0x003f:
            int r11 = r16.getPhase()
            if (r6 >= r11) goto L_0x0031
            java.lang.Object r11 = r10.get(r6)
            if (r11 == 0) goto L_0x0078
            com.xcharge.charger.protocol.ocpp.bean.types.SampledValue r5 = new com.xcharge.charger.protocol.ocpp.bean.types.SampledValue
            r5.<init>()
            java.lang.Object r11 = r10.get(r6)
            java.lang.String r11 = java.lang.String.valueOf(r11)
            r5.setValue(r11)
            r0 = r20
            r5.setContext(r0)
            java.lang.String r11 = "Voltage"
            r5.setMeasurand(r11)
            switch(r6) {
                case 0: goto L_0x016e;
                case 1: goto L_0x0175;
                case 2: goto L_0x017c;
                default: goto L_0x0068;
            }
        L_0x0068:
            java.lang.String r11 = "V"
            r5.setUnit(r11)
            r8.add(r5)
            int r11 = r8.size()
            r0 = r19
            if (r11 == r0) goto L_0x001f
        L_0x0078:
            int r6 = r6 + 1
            goto L_0x003f
        L_0x007b:
            java.lang.String r12 = "Energy.Active.Import.Register"
            boolean r11 = r11.equals(r12)
            if (r11 == 0) goto L_0x0031
            if (r2 == 0) goto L_0x0031
            com.xcharge.charger.protocol.ocpp.bean.types.SampledValue r5 = new com.xcharge.charger.protocol.ocpp.bean.types.SampledValue
            r5.<init>()
            double r12 = r2.doubleValue()
            r14 = 4652007308841189376(0x408f400000000000, double:1000.0)
            double r12 = r12 * r14
            long r12 = java.lang.Math.round(r12)
            java.lang.String r11 = java.lang.String.valueOf(r12)
            r5.setValue(r11)
            r0 = r20
            r5.setContext(r0)
            java.lang.String r11 = "Energy.Active.Import.Register"
            r5.setMeasurand(r11)
            java.lang.String r11 = "Wh"
            r5.setUnit(r11)
            r8.add(r5)
            goto L_0x0031
        L_0x00b2:
            java.lang.String r12 = "Current.Import"
            boolean r11 = r11.equals(r12)
            if (r11 == 0) goto L_0x0031
            if (r3 == 0) goto L_0x0031
            r6 = 1
        L_0x00bd:
            int r11 = r16.getPhase()
            int r11 = r11 + 1
            if (r6 >= r11) goto L_0x0031
            java.lang.Object r11 = r3.get(r6)
            if (r11 == 0) goto L_0x00f8
            com.xcharge.charger.protocol.ocpp.bean.types.SampledValue r5 = new com.xcharge.charger.protocol.ocpp.bean.types.SampledValue
            r5.<init>()
            java.lang.Object r11 = r3.get(r6)
            java.lang.String r11 = java.lang.String.valueOf(r11)
            r5.setValue(r11)
            r0 = r20
            r5.setContext(r0)
            java.lang.String r11 = "Current.Import"
            r5.setMeasurand(r11)
            switch(r6) {
                case 1: goto L_0x015b;
                case 2: goto L_0x0161;
                case 3: goto L_0x0167;
                default: goto L_0x00e8;
            }
        L_0x00e8:
            java.lang.String r11 = "A"
            r5.setUnit(r11)
            r8.add(r5)
            int r11 = r8.size()
            r0 = r19
            if (r11 == r0) goto L_0x001f
        L_0x00f8:
            int r6 = r6 + 1
            goto L_0x00bd
        L_0x00fb:
            java.lang.String r12 = "Power.Active.Import"
            boolean r11 = r11.equals(r12)
            if (r11 == 0) goto L_0x0031
            if (r7 == 0) goto L_0x0031
            com.xcharge.charger.protocol.ocpp.bean.types.SampledValue r5 = new com.xcharge.charger.protocol.ocpp.bean.types.SampledValue
            r5.<init>()
            java.lang.String r11 = java.lang.String.valueOf(r7)
            r5.setValue(r11)
            r0 = r20
            r5.setContext(r0)
            java.lang.String r11 = "Power.Active.Import"
            r5.setMeasurand(r11)
            java.lang.String r11 = "kW"
            r5.setUnit(r11)
            r8.add(r5)
            goto L_0x0031
        L_0x0125:
            java.lang.String r12 = "Temperature"
            boolean r11 = r11.equals(r12)
            if (r11 == 0) goto L_0x0031
            if (r9 == 0) goto L_0x0031
            com.xcharge.charger.protocol.ocpp.bean.types.SampledValue r5 = new com.xcharge.charger.protocol.ocpp.bean.types.SampledValue
            r5.<init>()
            double r12 = r9.doubleValue()
            r14 = 4621819117588971520(0x4024000000000000, double:10.0)
            double r12 = r12 / r14
            java.lang.String r11 = java.lang.String.valueOf(r12)
            r5.setValue(r11)
            r0 = r20
            r5.setContext(r0)
            java.lang.String r11 = "Temperature"
            r5.setMeasurand(r11)
            java.lang.String r11 = "Body"
            r5.setLocation(r11)
            java.lang.String r11 = "Celsius"
            r5.setUnit(r11)
            r8.add(r5)
            goto L_0x0031
        L_0x015b:
            java.lang.String r11 = "L1"
            r5.setPhase(r11)
            goto L_0x00e8
        L_0x0161:
            java.lang.String r11 = "L2"
            r5.setPhase(r11)
            goto L_0x00e8
        L_0x0167:
            java.lang.String r11 = "L3"
            r5.setPhase(r11)
            goto L_0x00e8
        L_0x016e:
            java.lang.String r11 = "L1"
            r5.setPhase(r11)
            goto L_0x0068
        L_0x0175:
            java.lang.String r11 = "L2"
            r5.setPhase(r11)
            goto L_0x0068
        L_0x017c:
            java.lang.String r11 = "L3"
            r5.setPhase(r11)
            goto L_0x0068
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.protocol.ocpp.handler.OcppPortHandler.getSampledValuesDataMaxLength(com.xcharge.charger.data.bean.status.PortStatus, java.lang.String[], int, java.lang.String):java.util.ArrayList");
    }

    private ArrayList<SampledValue> getSampledValues(PortStatus portStatus, String[] sampledDatas, String context2) {
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
                                item.setContext(context2);
                                item.setMeasurand(Measurand.Voltage);
                                switch (j) {
                                    case 0:
                                        item.setPhase(Phase.f115L1);
                                        break;
                                    case 1:
                                        item.setPhase(Phase.f116L2);
                                        break;
                                    case 2:
                                        item.setPhase(Phase.f117L3);
                                        break;
                                }
                                item.setUnit(UnitOfMeasure.f121V);
                                sampledValue.add(item);
                            }
                        }
                        break;
                    }
                case -210256374:
                    if (str.equals(Measurand.EnergyActiveImportRegister) && portStatus.getAmmeter() != null) {
                        SampledValue item2 = new SampledValue();
                        item2.setValue(String.valueOf(Math.round(portStatus.getAmmeter().doubleValue() * 1000.0d)));
                        item2.setContext(context2);
                        item2.setMeasurand(Measurand.EnergyActiveImportRegister);
                        item2.setUnit(UnitOfMeasure.f124Wh);
                        sampledValue.add(item2);
                        break;
                    }
                case 107981306:
                    if (str.equals(Measurand.CurrentImport) && (amps = portStatus.getAmps()) != null) {
                        for (int j2 = 1; j2 < getPhase() + 1; j2++) {
                            if (amps.get(j2) != null) {
                                SampledValue item3 = new SampledValue();
                                item3.setValue(String.valueOf(amps.get(j2)));
                                item3.setContext(context2);
                                item3.setMeasurand(Measurand.CurrentImport);
                                switch (j2) {
                                    case 1:
                                        item3.setPhase(Phase.f115L1);
                                        break;
                                    case 2:
                                        item3.setPhase(Phase.f116L2);
                                        break;
                                    case 3:
                                        item3.setPhase(Phase.f117L3);
                                        break;
                                }
                                item3.setUnit("A");
                                sampledValue.add(item3);
                            }
                        }
                        break;
                    }
                case 567329348:
                    if (str.equals(Measurand.PowerActiveImport) && portStatus.getKwatt() != null) {
                        SampledValue item4 = new SampledValue();
                        item4.setValue(String.valueOf(portStatus.getKwatt()));
                        item4.setContext(context2);
                        item4.setMeasurand(Measurand.PowerActiveImport);
                        item4.setUnit(UnitOfMeasure.f125kW);
                        sampledValue.add(item4);
                        break;
                    }
                case 1989569876:
                    if (str.equals(Measurand.Temperature) && portStatus.getTemprature() != null) {
                        SampledValue item5 = new SampledValue();
                        item5.setValue(String.valueOf(portStatus.getTemprature().doubleValue() / 10.0d));
                        item5.setContext(context2);
                        item5.setMeasurand(Measurand.Temperature);
                        item5.setLocation(Location.Body);
                        item5.setUnit(UnitOfMeasure.Celsius);
                        sampledValue.add(item5);
                        break;
                    }
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

    /* access modifiers changed from: private */
    public OcppConfig getOcppConfig() {
        return OcppProtocolAgent.getInstance().getOcppConfig();
    }
}
