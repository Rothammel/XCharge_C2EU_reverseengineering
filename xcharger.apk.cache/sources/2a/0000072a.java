package com.xcharge.charger.protocol.monitor.handler;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.FeeRate;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.CHARGE_STATUS;
import com.xcharge.charger.data.bean.type.CHARGE_STOP_CAUSE;
import com.xcharge.charger.data.bean.type.CHARGE_USER_TYPE;
import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.bean.type.USER_TC_TYPE;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.data.proxy.PortChargeStatusObserver;
import com.xcharge.charger.data.proxy.PortStatusObserver;
import com.xcharge.charger.device.c2.bean.PortRuntimeData;
import com.xcharge.charger.protocol.monitor.bean.ErrorCodeMapping;
import com.xcharge.charger.protocol.monitor.bean.YZXDCAPError;
import com.xcharge.charger.protocol.monitor.bean.YZXDCAPMessage;
import com.xcharge.charger.protocol.monitor.bean.YZXDCAPOption;
import com.xcharge.charger.protocol.monitor.bean.YZXProperty;
import com.xcharge.charger.protocol.monitor.bean.YZXPropset;
import com.xcharge.charger.protocol.monitor.bean.cap.BillInfo;
import com.xcharge.charger.protocol.monitor.bean.cap.CAPBillInfo;
import com.xcharge.charger.protocol.monitor.bean.cap.CAPChargeInfo;
import com.xcharge.charger.protocol.monitor.bean.cap.CAPChargeStarted;
import com.xcharge.charger.protocol.monitor.bean.cap.CAPChargeStoped;
import com.xcharge.charger.protocol.monitor.bean.cap.CAPDelayInfo;
import com.xcharge.charger.protocol.monitor.bean.cap.CAPDelayStarted;
import com.xcharge.charger.protocol.monitor.bean.cap.CAPDelayWaitStarted;
import com.xcharge.charger.protocol.monitor.bean.cap.ChargeInfo;
import com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition;
import com.xcharge.charger.protocol.monitor.bean.dap.DAPFeeRate;
import com.xcharge.charger.protocol.monitor.bean.request.FinRequest;
import com.xcharge.charger.protocol.monitor.bean.request.ThirdChargeRequest;
import com.xcharge.charger.protocol.monitor.bean.response.ThirdChargeResponse;
import com.xcharge.charger.protocol.monitor.session.MonitorChargeSession;
import com.xcharge.charger.protocol.monitor.session.MonitorRequestSession;
import com.xcharge.charger.protocol.monitor.util.LogUtils;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.HandlerTimer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* loaded from: classes.dex */
public class MonitorPortHandler {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STATUS = null;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE = null;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$LOCK_STATUS = null;
    public static final int INTERVAL_REPORT_HISTORY_BILL = 30000;
    public static final int MSG_CLOUD_REQUEST = 73735;
    public static final int MSG_CLOUD_RESPONSE = 73736;
    public static final int MSG_REPORT_CHARGE_TIMER = 73745;
    public static final int MSG_REPORT_DELAY_TIMER = 73747;
    public static final int MSG_REPORT_HISTORY_BILL_TIMER = 73746;
    public static final int MSG_REQUEST_TIMEOUT = 73733;
    public static final int MSG_REQUSET_RESEND = 73734;
    public static final int MSG_SEND_FAIL = 73732;
    public static final int MSG_SEND_OK = 73731;
    public static final int MSG_SERVICE_ACTIVE = 73729;
    public static final int MSG_SERVICE_DEACTIVE = 73730;
    private static final int TIMEOUT_REPORT_DELAY = 60000;
    private int TIMEOUT_REPORT_CHARGE = 60000;
    private HandlerThread thread = null;
    private MsgHandler handler = null;
    private HandlerTimer handlerTimer = null;
    private Context context = null;
    private String port = null;
    private boolean isActive = false;
    private CHARGE_STATUS status = CHARGE_STATUS.IDLE;
    private MonitorChargeSession chargeSession = null;
    private PortChargeStatusObserver portChargeStatusObserver = null;
    private PortStatusObserver portStatusObserver = null;
    private PortStatusObserver portRecoverableFaultObserver = null;

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

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$LOCK_STATUS() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$LOCK_STATUS;
        if (iArr == null) {
            iArr = new int[LOCK_STATUS.valuesCustom().length];
            try {
                iArr[LOCK_STATUS.disable.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[LOCK_STATUS.fault.ordinal()] = 4;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[LOCK_STATUS.lock.ordinal()] = 2;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[LOCK_STATUS.unlock.ordinal()] = 3;
            } catch (NoSuchFieldError e4) {
            }
            $SWITCH_TABLE$com$xcharge$charger$data$bean$type$LOCK_STATUS = iArr;
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
            String chargeId;
            ChargeBill chargeBill;
            try {
                switch (msg.what) {
                    case 73729:
                        LogUtils.log("MonitorPortHandler.handleMessage", "service actived !!! port: " + MonitorPortHandler.this.port);
                        MonitorPortHandler.this.isActive = true;
                        if (HardwareStatusCacheProvider.getInstance().hasDeviceErrors(MonitorPortHandler.this.port)) {
                            MonitorPortHandler.this.sendAlertIssueRequest();
                        } else {
                            MonitorPortHandler.this.sendAlertRemoveRequest(null);
                        }
                        MonitorPortHandler.this.sendGunConnectRequest("report");
                        MonitorPortHandler.this.sendChargeStatusRequest(MonitorPortHandler.this.status);
                        MonitorPortHandler.this.chargeSession = MonitorPortHandler.this.getChargeSession();
                        if (!TextUtils.isEmpty(MonitorPortHandler.this.chargeSession.getCharge_id()) && !TextUtils.isEmpty(MonitorPortHandler.this.chargeSession.getUser_type()) && CHARGE_STATUS.CHARGING.equals(MonitorPortHandler.this.status)) {
                            MonitorPortHandler.this.sendChargeInfoRequest();
                            MonitorPortHandler.this.handlerTimer.startTimer(MonitorPortHandler.this.chargeSession.getIntervalChargeReport(), 73745, null);
                        }
                        if (!TextUtils.isEmpty(MonitorPortHandler.this.chargeSession.getCharge_id()) && !TextUtils.isEmpty(MonitorPortHandler.this.chargeSession.getUser_type()) && CHARGE_STATUS.CHARGE_STOP_WAITTING.equals(MonitorPortHandler.this.status) && (chargeBill = ChargeContentProxy.getInstance().getChargeBill((chargeId = MonitorPortHandler.this.chargeSession.getCharge_id()))) != null && chargeBill.getDelay_start() > 0 && chargeBill.getFin_time() <= 0) {
                            MonitorPortHandler.this.sendDelayInfoRequest(chargeId);
                            MonitorPortHandler.this.handlerTimer.startTimer(60000L, 73747, chargeId);
                        }
                        MonitorPortHandler.this.handlerTimer.startTimer(30000L, 73746, null);
                        break;
                    case 73730:
                        LogUtils.log("MonitorPortHandler.handleMessage", "service deactived !!! port: " + MonitorPortHandler.this.port);
                        MonitorPortHandler.this.isActive = false;
                        MonitorPortHandler.this.clearPortActiveStatus();
                        break;
                    case 73731:
                        YZXDCAPMessage yZXDCAPMessage = (YZXDCAPMessage) msg.obj;
                        break;
                    case 73732:
                        YZXDCAPMessage yzxdcapMessage = (YZXDCAPMessage) msg.obj;
                        MonitorPortHandler.this.handleFailedRequest(yzxdcapMessage);
                        break;
                    case 73733:
                        YZXDCAPMessage yzxdcapMessage2 = (YZXDCAPMessage) msg.obj;
                        LogUtils.log("MonitorPortHandler.handleMessage", "send yzx request timeout: " + yzxdcapMessage2.toJson());
                        MonitorPortHandler.this.handleFailedRequest(yzxdcapMessage2);
                        break;
                    case 73734:
                        YZXDCAPMessage yzxdcapMessage3 = (YZXDCAPMessage) msg.obj;
                        MonitorPortHandler.this.resendRequest(yzxdcapMessage3);
                        break;
                    case 73735:
                        YZXDCAPMessage yzxdcapMessage4 = (YZXDCAPMessage) msg.obj;
                        MonitorPortHandler.this.handleRequest(yzxdcapMessage4);
                        break;
                    case 73736:
                        MonitorRequestSession yzxRequestSession = (MonitorRequestSession) msg.obj;
                        MonitorPortHandler.this.handleResponse(yzxRequestSession.getRequest(), yzxRequestSession.getResponse());
                        break;
                    case 73745:
                        MonitorPortHandler.this.reportChargeInfo();
                        break;
                    case 73746:
                        MonitorPortHandler.this.reportHistoryBill();
                        MonitorPortHandler.this.handlerTimer.startTimer(30000L, 73746, null);
                        break;
                    case 73747:
                        String chargeId2 = (String) msg.obj;
                        MonitorPortHandler.this.sendDelayInfoRequest(chargeId2);
                        MonitorPortHandler.this.handlerTimer.startTimer(60000L, 73747, chargeId2);
                        break;
                    case PortChargeStatusObserver.MSG_PORT_CHARGE_STATUS_CHANGE /* 131073 */:
                        MonitorPortHandler.this.handlePortChargeStatusChanged((Uri) msg.obj);
                        break;
                    case PortStatusObserver.MSG_PORT_STATUS_CHANGE /* 139265 */:
                        Uri uri = (Uri) msg.obj;
                        if (uri.getPath().contains("ports/fault/")) {
                            LogUtils.log("MonitorPortHandler.handleMessage", "port recoverable faults changed, port: " + MonitorPortHandler.this.port + ", uri: " + uri.toString());
                            MonitorPortHandler.this.handlePortRecoverableFaultChanged(uri);
                            break;
                        } else {
                            LogUtils.log("MonitorPortHandler.handleMessage", "port status changed, port: " + MonitorPortHandler.this.port + ", uri: " + uri.toString());
                            MonitorPortHandler.this.handlePortStatusChanged(uri);
                            break;
                        }
                }
            } catch (Exception e) {
                LogUtils.log("MonitorPortHandler.handleMessage", "except: " + Log.getStackTraceString(e));
            }
            super.handleMessage(msg);
        }
    }

    public void init(Context context, String port, MonitorProtocolAgent protocolHandler) {
        this.context = context;
        this.port = port;
        this.thread = new HandlerThread("MonitorPortHandler#" + this.port, 10);
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
        this.handler.removeMessages(73745);
        this.handler.removeMessages(73746);
        this.handler.removeMessages(73747);
        this.handler.removeMessages(PortChargeStatusObserver.MSG_PORT_CHARGE_STATUS_CHANGE);
        this.handler.removeMessages(PortStatusObserver.MSG_PORT_STATUS_CHANGE);
        this.thread.quit();
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

    public MonitorChargeSession getChargeSession() {
        if (this.chargeSession == null) {
            this.chargeSession = new MonitorChargeSession();
        }
        return this.chargeSession;
    }

    private void clearChargeSession() {
        this.chargeSession = null;
    }

    public String getPort(String chargeId) {
        MonitorChargeSession chargeSession = getChargeSession();
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
    public void handleFailedRequest(YZXDCAPMessage request) {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void resendRequest(YZXDCAPMessage yzxdcapMessage) {
        if (!sendMessage(yzxdcapMessage)) {
            this.handler.sendMessage(this.handler.obtainMessage(73732, yzxdcapMessage));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePortStatusChanged(Uri uri) {
        if (uri.getPath().contains("ports/" + this.port + "/plugin")) {
            sendGunConnectRequest("event");
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
                sendAlertIssueRequest();
            } else if ("remove".equals(status)) {
                int errorCode = Integer.parseInt(last);
                if (errorCode == 200) {
                    sendAlertRemoveRequest(null);
                } else {
                    sendAlertRemoveRequest(new ErrorCode(errorCode));
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendAlertIssueRequest() {
        try {
            YZXDCAPMessage request = MonitorProtocolAgent.getInstance().createDAPRequest("alert", "alert_issue/port/" + this.port);
            YZXPropset yzxPropset = new YZXPropset();
            List<YZXProperty> propset = new ArrayList<>();
            YZXProperty yzxProperty = new YZXProperty();
            yzxProperty.setId("alert_issue/port/" + this.port);
            List<YZXDCAPError> errorList = new ArrayList<>();
            HashMap<String, ErrorCode> errors = HardwareStatusCacheProvider.getInstance().getAllDeviceErrors(this.port);
            if (errors != null) {
                for (ErrorCode error : errors.values()) {
                    YZXDCAPError yzxdcapError = new YZXDCAPError();
                    switch (error.getCode()) {
                        case ErrorCode.EC_DEVICE_NOT_INIT /* 30010 */:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_NOT_INIT);
                            break;
                        case ErrorCode.EC_DEVICE_NO_GROUND /* 30011 */:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_NO_GROUND);
                            break;
                        case ErrorCode.EC_DEVICE_LOST_PHASE /* 30012 */:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_PHASE_LOST);
                            break;
                        case ErrorCode.EC_DEVICE_EMERGENCY_STOP /* 30013 */:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_EMERGENCY_STOP);
                            break;
                        case ErrorCode.EC_DEVICE_VOLT_ERROR /* 30014 */:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_VOLT);
                            break;
                        case ErrorCode.EC_DEVICE_AMP_ERROR /* 30015 */:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_AMP);
                            break;
                        case ErrorCode.EC_DEVICE_TEMP_ERROR /* 30016 */:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_TEMPRATURE);
                            break;
                        case ErrorCode.EC_DEVICE_POWER_LEAK /* 30017 */:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_LEAK_AMP);
                            break;
                        case ErrorCode.EC_DEVICE_COMM_ERROR /* 30018 */:
                            yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_COMM);
                            break;
                    }
                    errorList.add(yzxdcapError);
                }
                yzxProperty.setValue(errorList);
                propset.add(yzxProperty);
                yzxPropset.setPropset(propset);
                request.setData(yzxPropset);
                sendMessage(request);
                LogUtils.log("MonitorPortHandler.sendAlertIssueRequest", "send AlertIssueRequest:" + request.toJson());
            }
        } catch (Exception e) {
            LogUtils.log("MonitorPortHandler.sendAlertIssueRequest", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendAlertRemoveRequest(ErrorCode errorCode) {
        try {
            YZXDCAPMessage request = MonitorProtocolAgent.getInstance().createDAPRequest("alert", "alert_remove/port/" + this.port);
            YZXPropset yzxPropset = new YZXPropset();
            List<YZXProperty> propset = new ArrayList<>();
            YZXProperty yzxProperty = new YZXProperty();
            yzxProperty.setId("alert_remove/port/" + this.port);
            List<YZXDCAPError> erroeList = new ArrayList<>();
            if (errorCode != null) {
                YZXDCAPError yzxdcapError = new YZXDCAPError();
                switch (errorCode.getCode()) {
                    case ErrorCode.EC_DEVICE_NOT_INIT /* 30010 */:
                        yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_NOT_INIT);
                        break;
                    case ErrorCode.EC_DEVICE_NO_GROUND /* 30011 */:
                        yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_NO_GROUND);
                        break;
                    case ErrorCode.EC_DEVICE_LOST_PHASE /* 30012 */:
                        yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_PHASE_LOST);
                        break;
                    case ErrorCode.EC_DEVICE_EMERGENCY_STOP /* 30013 */:
                        yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_EMERGENCY_STOP);
                        break;
                    case ErrorCode.EC_DEVICE_VOLT_ERROR /* 30014 */:
                        yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_VOLT);
                        break;
                    case ErrorCode.EC_DEVICE_AMP_ERROR /* 30015 */:
                        yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_AMP);
                        break;
                    case ErrorCode.EC_DEVICE_TEMP_ERROR /* 30016 */:
                        yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_TEMPRATURE);
                        break;
                    case ErrorCode.EC_DEVICE_POWER_LEAK /* 30017 */:
                        yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_LEAK_AMP);
                        break;
                    case ErrorCode.EC_DEVICE_COMM_ERROR /* 30018 */:
                        yzxdcapError.init(ErrorCodeMapping.E_AC_CHARGER_ERROR_COMM);
                        break;
                }
                erroeList.add(yzxdcapError);
                yzxProperty.setValue(erroeList);
                propset.add(yzxProperty);
                yzxPropset.setPropset(propset);
                request.setData(yzxPropset);
            }
            sendMessage(request);
            LogUtils.log("MonitorPortHandler.sendAlertRemoveRequest", "send AlertRemoveRequest:" + request.toJson());
        } catch (Exception e) {
            LogUtils.log("MonitorPortHandler.sendAlertRemoveRequest", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendGunConnectRequest(String op) {
        try {
            YZXDCAPMessage request = MonitorProtocolAgent.getInstance().createDAPRequest(op, "gun_connect/port/" + this.port);
            YZXPropset yzxPropset = new YZXPropset();
            List<YZXProperty> propset = new ArrayList<>();
            YZXProperty yzxProperty = new YZXProperty();
            yzxProperty.setId("gun_connect/port/" + this.port);
            Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(this.port);
            if (portStatus.isPlugin()) {
                yzxProperty.setValue(YZXProperty.GUN_CONNECT_VEHICLE);
            } else {
                yzxProperty.setValue("none");
            }
            propset.add(yzxProperty);
            yzxPropset.setPropset(propset);
            request.setData(yzxPropset);
            sendMessage(request);
            LogUtils.log("MonitorPortHandler.sendGunConnectRequest", "send GunConnectRequest:" + request.toJson());
        } catch (Exception e) {
            LogUtils.log("MonitorPortHandler.sendGunConnectRequest", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handlePortChargeStatusChanged(Uri uri) {
        PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port);
        LogUtils.log("MonitorPortHandler.getChangedYZXStatus", "now port status: " + portStatus.toJson());
        CHARGE_STATUS chargeStatus = portStatus.getChargeStatus();
        String chargeId = getChargeSession().getCharge_id();
        if (!this.status.equals(chargeStatus)) {
            if (chargeStatus.equals(CHARGE_STATUS.CHARGE_START_WAITTING)) {
                LogUtils.log("MonitorPortHandler.getChangedYZXStatus", "enter wait charge status !!!");
                MonitorChargeSession chargeSession = getChargeSession();
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
                    LogUtils.log("MonitorPortHandler.getChangedYZXStatus", "failed to query info for charge: " + chargeId2);
                }
                sendThirdChargeRequest(chargeId2, false);
            } else if (chargeStatus.equals(CHARGE_STATUS.CHARGING)) {
                LogUtils.log("MonitorPortHandler.getChangedYZXStatus", "enter charging status !!!");
                MonitorChargeSession chargeSession2 = getChargeSession();
                String chargeId3 = chargeSession2.getCharge_id();
                if (TextUtils.isEmpty(chargeId3)) {
                    chargeId3 = portStatus.getCharge_id();
                    ChargeBill chargeBill2 = ChargeContentProxy.getInstance().getChargeBill(chargeId3);
                    if (chargeBill2 != null) {
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
                        LogUtils.log("MonitorPortHandler.getChangedYZXStatus", "failed to query info for charge: " + chargeId3);
                    }
                }
                sendChargeStartedRequest(chargeId3);
                this.handlerTimer.startTimer(chargeSession2.getIntervalChargeReport(), 73745, null);
            } else if (chargeStatus.equals(CHARGE_STATUS.CHARGE_STOP_WAITTING)) {
                LogUtils.log("MonitorPortHandler.getChangedYZXStatus", "enter charge stopped status !!!");
                this.handlerTimer.stopTimer(73745);
                sendChargeStopedRequest(chargeId);
            } else if (chargeStatus.equals(CHARGE_STATUS.IDLE)) {
                LogUtils.log("MonitorPortHandler.getChangedYZXStatus", "enter idle status !!!");
                this.handlerTimer.stopTimer(73745);
                this.handlerTimer.stopTimer(73747);
                if (CHARGE_STATUS.CHARGING.equals(this.status)) {
                    sendChargeStopedRequest(chargeId);
                }
                if (CHARGE_STATUS.CHARGING.equals(this.status) || CHARGE_STATUS.CHARGE_STOP_WAITTING.equals(this.status)) {
                    sendFinRequest(chargeId, null, null);
                }
                clearChargeSession();
            }
            this.status = chargeStatus;
            sendChargeStatusRequest(this.status);
        }
    }

    private void sendThirdChargeRequest(String charge_id, boolean isBill) {
        try {
            YZXDCAPMessage yzxdcapMessage = MonitorProtocolAgent.getInstance().createCAPRequest(YZXDCAPMessage.OP_THIRD_CHARGE, null);
            ThirdChargeRequest thirdChargeRequest = new ThirdChargeRequest();
            thirdChargeRequest.setThird_charge_id(charge_id);
            CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
            thirdChargeRequest.setThird_platform(platform.getPlatform());
            ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(charge_id);
            if (chargeBill != null) {
                thirdChargeRequest.setUser_type(chargeBill.getUser_type());
                thirdChargeRequest.setUser_code(chargeBill.getUser_code());
                FeeRate fee_rate = chargeBill.getFee_rate();
                if (fee_rate != null) {
                    DAPFeeRate dapFeeRate = new DAPFeeRate();
                    dapFeeRate.setFee_rate_id(fee_rate.getFeeRateId());
                    dapFeeRate.setPower_price(fee_rate.getPowerPrice());
                    dapFeeRate.setService_price(fee_rate.getServicePrice());
                    dapFeeRate.setDelay_price(fee_rate.getDelayPrice());
                    thirdChargeRequest.setFee_rate(dapFeeRate);
                }
            }
            thirdChargeRequest.setPort(Integer.parseInt(this.port));
            thirdChargeRequest.setInit_time(System.currentTimeMillis());
            ChargeStopCondition chargeStopCondition = new ChargeStopCondition();
            USER_TC_TYPE user_tc_type = chargeBill.getUser_tc_type();
            if (user_tc_type != null) {
                chargeStopCondition.setType(user_tc_type.getType());
            }
            String user_tc_value = chargeBill.getUser_tc_value();
            if (!TextUtils.isEmpty(user_tc_value)) {
                chargeStopCondition.setValue(user_tc_value);
            }
            thirdChargeRequest.setStop_condition(chargeStopCondition);
            yzxdcapMessage.setData(thirdChargeRequest);
            yzxdcapMessage.setPort(this.port);
            yzxdcapMessage.setBill(isBill);
            sendMessage(yzxdcapMessage);
            LogUtils.log("XCloudPortHandler.sendThirdChargeRequest", "send ThirdChargeRequest: " + yzxdcapMessage.toJson());
        } catch (Exception e) {
            LogUtils.log("XCloudPortHandler.sendThirdChargeRequest", Log.getStackTraceString(e));
        }
    }

    private void sendChargeStartedRequest(String charge_id) {
        try {
            ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(charge_id);
            String localChargeId = getChargeSession().getLocal_charge_id();
            if (chargeBill != null && !TextUtils.isEmpty(localChargeId)) {
                YZXDCAPMessage yzxdcapMessage = MonitorProtocolAgent.getInstance().createCAPRequest("event", "charge_started/" + localChargeId);
                YZXPropset yzxPropset = new YZXPropset();
                List<YZXProperty> propset = new ArrayList<>();
                YZXProperty yzxProperty = new YZXProperty();
                yzxProperty.setId("charge_started/" + localChargeId);
                CAPChargeStarted capChargeStarted = new CAPChargeStarted();
                capChargeStarted.setCharge_id(getChargeSession().getLocal_charge_id());
                capChargeStarted.setStart_time(chargeBill.getStart_time());
                capChargeStarted.setStart_ammeter(chargeBill.getStart_ammeter());
                yzxProperty.setValue(capChargeStarted);
                propset.add(yzxProperty);
                yzxPropset.setPropset(propset);
                yzxdcapMessage.setData(yzxPropset);
                sendMessage(yzxdcapMessage);
                LogUtils.log("MonitorPortHandler.sendChargeStartedRequest", "send ChargeStartedRequest: " + yzxdcapMessage.toJson());
            }
        } catch (Exception e) {
            LogUtils.log("MonitorPortHandler.sendChargeStartedRequest", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reportChargeInfo() {
        MonitorChargeSession chargeSession = getChargeSession();
        LogUtils.log("MonitorPortHandler.handleChargeStatusPeriodicReport", "report charge request periodically, port: " + this.port + ", interval: " + chargeSession.getIntervalChargeReport() + ", status: " + this.status);
        sendChargeInfoRequest();
        chargeSession.incChargeReportCnt(1);
        if (chargeSession.getChargeReportCnt() == 12) {
            int intervalChargeReport = RemoteSettingCacheProvider.getInstance().getChargeSetting().getTimerSetting().getIntervalChargeReport() * 1000;
            if (intervalChargeReport <= 0) {
                intervalChargeReport = this.TIMEOUT_REPORT_CHARGE;
            }
            chargeSession.setIntervalChargeReport(intervalChargeReport);
        }
        this.handlerTimer.startTimer(chargeSession.getIntervalChargeReport(), 73745, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendChargeInfoRequest() {
        try {
            PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port);
            ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(portStatus.getCharge_id());
            String localChargeId = getChargeSession().getLocal_charge_id();
            if (chargeBill != null && !TextUtils.isEmpty(localChargeId)) {
                YZXDCAPMessage yzxdcapMessage = MonitorProtocolAgent.getInstance().createCAPRequest("report", "charge/" + localChargeId);
                YZXPropset yzxPropset = new YZXPropset();
                List<YZXProperty> propset = new ArrayList<>();
                YZXProperty yzxProperty = new YZXProperty();
                yzxProperty.setId("charge/" + localChargeId);
                CAPChargeInfo capChargeInfo = new CAPChargeInfo();
                ChargeInfo chargeInfo = new ChargeInfo();
                chargeInfo.setPower(portStatus.getPower());
                chargeInfo.setKwatt(Integer.valueOf((int) (portStatus.getKwatt().doubleValue() * 1000.0d)));
                chargeInfo.setVolt(portStatus.getVolts().get(0));
                chargeInfo.setAmp(portStatus.getAmps().get(0));
                chargeInfo.setPower_fee(Integer.valueOf(chargeBill.getPower_fee()));
                chargeInfo.setService_fee(Integer.valueOf(chargeBill.getService_fee()));
                chargeInfo.setPark_fee(Integer.valueOf(chargeBill.getPark_fee()));
                capChargeInfo.setCharge_id(localChargeId);
                capChargeInfo.setCharge_info(chargeInfo);
                yzxProperty.setValue(capChargeInfo);
                propset.add(yzxProperty);
                yzxPropset.setPropset(propset);
                yzxdcapMessage.setData(yzxPropset);
                sendMessage(yzxdcapMessage);
                LogUtils.log("MonitorPortHandler.sendChargeInfoRequest", "send ChargeInfoRequest: " + yzxdcapMessage.toJson());
            }
        } catch (Exception e) {
            LogUtils.log("MonitorPortHandler.sendChargeInfoRequest", Log.getStackTraceString(e));
        }
    }

    private void sendChargeStopedRequest(String charge_id) {
        try {
            ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(charge_id);
            String localChargeId = getChargeSession().getLocal_charge_id();
            if (chargeBill != null && !TextUtils.isEmpty(localChargeId)) {
                YZXDCAPMessage yzxdcapMessage = MonitorProtocolAgent.getInstance().createCAPRequest("event", "charge_stopped/" + localChargeId);
                YZXPropset yzxPropset = new YZXPropset();
                List<YZXProperty> propset = new ArrayList<>();
                YZXProperty yzxProperty = new YZXProperty();
                yzxProperty.setId("charge_stopped/" + localChargeId);
                CAPChargeStoped capChargeStoped = new CAPChargeStoped();
                capChargeStoped.setCharge_id(localChargeId);
                capChargeStoped.setStop_time(chargeBill.getStop_time());
                YZXDCAPError stopCause = new YZXDCAPError();
                switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE()[chargeBill.getStop_cause().ordinal()]) {
                    case 1:
                        stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_OTHER);
                        break;
                    case 3:
                        stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_USER_LOCAL);
                        break;
                    case 4:
                        stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_USER_REMOTE);
                        break;
                    case 5:
                        stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_ADMIN_REMOTE);
                        break;
                    case 6:
                        stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_PLUGOUT);
                        break;
                    case 7:
                        stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_VEHICLE);
                        break;
                    case 8:
                        stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_FULL);
                        break;
                    case 9:
                        stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_NO_FUND);
                        break;
                    case 10:
                        stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_USER_SET);
                        break;
                    case PortRuntimeData.STATUS_EX_12 /* 12 */:
                        stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_DEVICE_ERROR);
                        break;
                }
                capChargeStoped.setStop_cause(stopCause);
                capChargeStoped.setStart_time(chargeBill.getStart_time());
                capChargeStoped.setStart_ammeter(chargeBill.getStart_ammeter());
                capChargeStoped.setStop_ammeter(chargeBill.getStop_ammeter());
                capChargeStoped.setTotal_power(chargeBill.getTotal_power());
                yzxProperty.setValue(capChargeStoped);
                propset.add(yzxProperty);
                yzxPropset.setPropset(propset);
                yzxdcapMessage.setData(yzxPropset);
                sendMessage(yzxdcapMessage);
                LogUtils.log("MonitorPortHandler.sendChargeStopedRequest", "send ChargeStopedRequest: " + yzxdcapMessage.toJson());
            }
        } catch (Exception e) {
            LogUtils.log("MonitorPortHandler.sendChargeStopedRequest", Log.getStackTraceString(e));
        }
    }

    public void sendFinRequest(String charge_id, YZXDCAPError finCause, String finTime) {
        try {
            ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(charge_id);
            String localChargeId = getChargeSession().getLocal_charge_id();
            if (chargeBill != null && !TextUtils.isEmpty(localChargeId)) {
                YZXDCAPMessage yzxdcapMessage = MonitorProtocolAgent.getInstance().createCAPRequest("fin", null);
                FinRequest finRequest = new FinRequest();
                finRequest.setCharge_id(localChargeId);
                BillInfo billInfo = new BillInfo();
                billInfo.setLocal_charge_id(charge_id);
                billInfo.setBill_id(localChargeId);
                billInfo.setUser_type(chargeBill.getUser_type());
                billInfo.setUser_code(chargeBill.getUser_code());
                billInfo.setPort(chargeBill.getPort());
                ChargeStopCondition chargeStopCondition = new ChargeStopCondition();
                USER_TC_TYPE user_tc_type = chargeBill.getUser_tc_type();
                if (user_tc_type != null) {
                    chargeStopCondition.setType(user_tc_type.getType());
                }
                chargeStopCondition.setValue(chargeBill.getUser_tc_value());
                billInfo.setStop_condition(chargeStopCondition);
                billInfo.setBalance(Long.valueOf(chargeBill.getUser_balance()));
                if (finCause != null) {
                    billInfo.setFin_cause(finCause);
                }
                billInfo.setLocal_charge_id(chargeBill.getCharge_id());
                billInfo.setTotal_time(Integer.valueOf((int) ((chargeBill.getStop_time() - chargeBill.getStart_time()) / 1000)));
                billInfo.setInit_time(Long.valueOf(chargeBill.getInit_time()));
                if (finTime == null) {
                    billInfo.setFin_time(Long.valueOf(chargeBill.getFin_time()));
                } else {
                    billInfo.setFin_time(Long.valueOf(finTime));
                }
                billInfo.setStart_time(Long.valueOf(chargeBill.getStart_time()));
                billInfo.setStop_time(Long.valueOf(chargeBill.getStop_time()));
                billInfo.setStart_ammeter(Double.valueOf(chargeBill.getStart_ammeter()));
                billInfo.setStop_ammeter(Double.valueOf(chargeBill.getStop_ammeter()));
                YZXDCAPError stopCause = new YZXDCAPError();
                CHARGE_STOP_CAUSE stop_cause = chargeBill.getStop_cause();
                if (stop_cause != null) {
                    switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE()[stop_cause.ordinal()]) {
                        case 1:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_OTHER);
                            break;
                        case 3:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_USER_LOCAL);
                            break;
                        case 4:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_USER_REMOTE);
                            break;
                        case 5:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_ADMIN_REMOTE);
                            break;
                        case 6:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_PLUGOUT);
                            break;
                        case 7:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_VEHICLE);
                            break;
                        case 8:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_FULL);
                            break;
                        case 9:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_NO_FUND);
                            break;
                        case 10:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_USER_SET);
                            break;
                        case PortRuntimeData.STATUS_EX_12 /* 12 */:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_DEVICE_ERROR);
                            break;
                    }
                    billInfo.setStop_cause(stopCause);
                }
                billInfo.setDelay_start(Long.valueOf(chargeBill.getDelay_start()));
                billInfo.setTotal_delay(Integer.valueOf(chargeBill.getTotal_delay()));
                billInfo.setDelay_info(chargeBill.getDelay_info());
                billInfo.setTotal_power(Double.valueOf(chargeBill.getTotal_power()));
                billInfo.setPower_info(chargeBill.getPower_info());
                billInfo.setService_info(chargeBill.getService_info());
                billInfo.setFee_rate_id(chargeBill.getFee_rate_id());
                billInfo.setTotal_fee(Integer.valueOf(chargeBill.getTotal_fee()));
                billInfo.setPower_fee(Integer.valueOf(chargeBill.getPower_fee()));
                billInfo.setService_fee(Integer.valueOf(chargeBill.getService_fee()));
                billInfo.setDelay_fee(Integer.valueOf(chargeBill.getDelay_fee()));
                billInfo.setPark_fee(Integer.valueOf(chargeBill.getPark_fee()));
                billInfo.setTotal_park(Integer.valueOf(chargeBill.getTotal_park()));
                billInfo.setPark_info(chargeBill.getPark_info());
                if (chargeBill.getBalance_flag() == 0) {
                    billInfo.setBalance_flag(false);
                } else if (chargeBill.getBalance_flag() == 1) {
                    billInfo.setBalance_flag(true);
                }
                billInfo.setBalance_time(Long.valueOf(chargeBill.getBalance_time()));
                billInfo.setPay_flag(true);
                long pay_time = chargeBill.getPay_time();
                if (pay_time == 0) {
                    billInfo.setPay_time(Long.valueOf(System.currentTimeMillis()));
                } else {
                    billInfo.setPay_time(Long.valueOf(pay_time));
                }
                finRequest.setBill_info(billInfo);
                yzxdcapMessage.setData(finRequest);
                yzxdcapMessage.setPort(this.port);
                sendMessage(yzxdcapMessage);
                LogUtils.log("MonitorPortHandler.sendFinRequest", "send FinRequest: " + yzxdcapMessage.toJson());
            }
        } catch (Exception e) {
            LogUtils.log("MonitorPortHandler.sendFinRequest", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendChargeStatusRequest(CHARGE_STATUS chargeStatus) {
        try {
            YZXDCAPMessage request = MonitorProtocolAgent.getInstance().createDAPRequest("event", "charge_status/port/" + this.port);
            YZXPropset yzxPropset = new YZXPropset();
            List<YZXProperty> propset = new ArrayList<>();
            YZXProperty yzxProperty = new YZXProperty();
            yzxProperty.setId("charge_status/port/" + this.port);
            switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STATUS()[chargeStatus.ordinal()]) {
                case 1:
                    yzxProperty.setValue(YZXProperty.CHARGE_STATUS_IDLE);
                    break;
                case 2:
                    yzxProperty.setValue(YZXProperty.CHARGE_STATUS_STARTING);
                    break;
                case 3:
                    yzxProperty.setValue(YZXProperty.CHARGE_STATUS_CHARGING);
                    break;
                case 4:
                    yzxProperty.setValue(YZXProperty.CHARGE_STATUS_STOPPED);
                    break;
            }
            propset.add(yzxProperty);
            yzxPropset.setPropset(propset);
            request.setData(yzxPropset);
            sendMessage(request);
            LogUtils.log("MonitorPortHandler.sendChargeStatusRequest", "send ChargeStatusRequest:" + request.toJson());
        } catch (Exception e) {
            LogUtils.log("MonitorPortHandler.sendChargeStatusRequest", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleRequest(YZXDCAPMessage yzxdcapMessage) {
    }

    public YZXProperty handleQueryChargeInfoRequest(YZXDCAPMessage yzxdcapMessage) {
        try {
            if (CHARGE_STATUS.CHARGING.equals(this.status)) {
                PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port);
                ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(portStatus.getCharge_id());
                String localChargeId = getChargeSession().getLocal_charge_id();
                if (chargeBill != null && !TextUtils.isEmpty(localChargeId)) {
                    YZXProperty yzxProperty = new YZXProperty();
                    yzxProperty.setId("charge/" + localChargeId);
                    CAPChargeInfo capChargeInfo = new CAPChargeInfo();
                    ChargeInfo chargeInfo = new ChargeInfo();
                    chargeInfo.setPower(portStatus.getPower());
                    chargeInfo.setKwatt(Integer.valueOf((int) (portStatus.getKwatt().doubleValue() * 1000.0d)));
                    chargeInfo.setVolt(portStatus.getVolts().get(0));
                    chargeInfo.setAmp(portStatus.getAmps().get(0));
                    chargeInfo.setPower_fee(Integer.valueOf(chargeBill.getPower_fee()));
                    chargeInfo.setService_fee(Integer.valueOf(chargeBill.getService_fee()));
                    chargeInfo.setPark_fee(Integer.valueOf(chargeBill.getPark_fee()));
                    capChargeInfo.setCharge_id(localChargeId);
                    capChargeInfo.setCharge_info(chargeInfo);
                    yzxProperty.setValue(capChargeInfo);
                    return yzxProperty;
                }
            }
        } catch (Exception e) {
            LogUtils.log("MonitorPortHandler.handleQueryChargeInfoRequest", Log.getStackTraceString(e));
        }
        return null;
    }

    public YZXProperty handleQueryGunLockStatusRequest(YZXDCAPMessage yzxdcapMessage) {
        try {
            YZXProperty yzxProperty = new YZXProperty();
            yzxProperty.setId(YZXDCAPOption.GUN_LOCK_STATUS);
            LOCK_STATUS status = ChargeStatusCacheProvider.getInstance().getPortLockStatus(this.port);
            switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$LOCK_STATUS()[status.ordinal()]) {
                case 1:
                    yzxProperty.setValue("disable");
                    break;
                case 2:
                    yzxProperty.setValue("locked");
                    break;
                case 3:
                    yzxProperty.setValue(YZXProperty.GUN_LOCK_UNLOCKED);
                    break;
            }
            return yzxProperty;
        } catch (Exception e) {
            LogUtils.log("MonitorPortHandler.handleQueryGunLockStatusRequest", Log.getStackTraceString(e));
            return null;
        }
    }

    public YZXProperty handleQueryPortEnableRequest(YZXDCAPMessage yzxdcapMessage) {
        try {
            YZXProperty yzxProperty = new YZXProperty();
            yzxProperty.setId(YZXDCAPOption.PORT_ENABLE);
            yzxProperty.setValue(Boolean.valueOf(ChargeStatusCacheProvider.getInstance().getPortSwitch(this.port)));
            return yzxProperty;
        } catch (Exception e) {
            LogUtils.log("MonitorPortHandler.handleQueryPortEnableRequest", Log.getStackTraceString(e));
            return null;
        }
    }

    public YZXProperty handleQueryRadarStatusRequest(YZXDCAPMessage yzxdcapMessage) {
        try {
            YZXProperty yzxProperty = new YZXProperty();
            yzxProperty.setId(YZXDCAPOption.RADAR_STATUS);
            if (HardwareStatusCacheProvider.getInstance().getPortRadarSwitch("1")) {
                yzxProperty.setValue("enable");
            } else {
                yzxProperty.setValue("disable");
            }
            return yzxProperty;
        } catch (Exception e) {
            LogUtils.log("YZXProtocolAgent.handleQueryRadarStatusRequest", Log.getStackTraceString(e));
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleResponse(YZXDCAPMessage request, YZXDCAPMessage response) {
        YZXDCAPOption option = response.getOpt();
        String op = option.getOp();
        switch (op.hashCode()) {
            case -934521548:
                if (op.equals("report")) {
                    String[] propId = option.getProp_id().get(0).split(MqttTopic.TOPIC_LEVEL_SEPARATOR);
                    if (propId.length == 2) {
                        if (YZXDCAPOption.DELAY.equals(propId[0])) {
                            Log.i("YZXPortHandler.handleResponse", "receive DelayInfoResponse:" + response.toJson());
                            return;
                        } else if (YZXDCAPOption.BILL.equals(propId[0])) {
                            Log.i("YZXPortHandler.handleResponse", "receive BillResponse:" + response.toJson());
                            if (response.getError() == null) {
                                YZXPropset yzxPropset = (YZXPropset) request.getData();
                                CAPBillInfo capBillInfo = (CAPBillInfo) yzxPropset.getPropset().get(0).getValue();
                                BillInfo billInfo = capBillInfo.getBill_info();
                                ChargeContentProxy.getInstance().setMonitorFlag(billInfo.getLocal_charge_id());
                                return;
                            }
                            return;
                        } else {
                            return;
                        }
                    }
                    return;
                }
                return;
            case 101387:
                if (op.equals("fin") && response.getError() == null) {
                    FinRequest finRequest = (FinRequest) request.getData();
                    ChargeContentProxy.getInstance().setMonitorFlag(finRequest.getBill_info().getLocal_charge_id());
                    return;
                }
                return;
            case 96891546:
                if (op.equals("event")) {
                    String[] propId2 = option.getProp_id().get(0).split(MqttTopic.TOPIC_LEVEL_SEPARATOR);
                    if (propId2.length == 2) {
                        if (YZXDCAPOption.DELAY_WAIT_STARTED.equals(propId2[0])) {
                            LogUtils.log("MonitorPortHandler.handleResponse", "receive DelayWaitStartedResponse:" + response.toJson());
                            return;
                        } else if (YZXDCAPOption.DELAY_STARTED.equals(propId2[0])) {
                            LogUtils.log("MonitorPortHandler.handleResponse", "receive DelayStartedResponse:" + response.toJson());
                            if (response.getError() == null) {
                                this.handlerTimer.startTimer(60000L, 73747, getChargeSession().getCharge_id());
                                return;
                            }
                            return;
                        } else {
                            return;
                        }
                    }
                    return;
                }
                return;
            case 373098636:
                if (op.equals(YZXDCAPMessage.OP_THIRD_CHARGE) && response.getError() == null) {
                    ThirdChargeRequest thirdChargeRequest = (ThirdChargeRequest) request.getData();
                    ThirdChargeResponse thirdChargeResponse = new ThirdChargeResponse().fromJson(JsonBean.ObjectToJson(response.getData()));
                    if (request.isBill()) {
                        sendBillRequest(thirdChargeRequest.getThird_charge_id(), thirdChargeResponse.getCharge_id());
                        return;
                    } else {
                        getChargeSession().setLocal_charge_id(thirdChargeResponse.getCharge_id());
                        return;
                    }
                }
                return;
            default:
                return;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reportHistoryBill() {
        CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
        String[] yzxUserTypes = {platform.toString(), String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.U1, String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.U2, String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.U3, String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.anyo1, String.valueOf(CHARGE_USER_TYPE.nfc.getUserType()) + "." + NFC_CARD_TYPE.ocpp};
        ArrayList<ChargeBill> chargeBills = ChargeContentProxy.getInstance().getUnMonitorBills(yzxUserTypes, this.port);
        if (chargeBills != null && chargeBills.size() != 0) {
            Iterator<ChargeBill> it2 = chargeBills.iterator();
            while (it2.hasNext()) {
                ChargeBill chargeBill = it2.next();
                if (chargeBill.getStart_time() > 0) {
                    sendThirdChargeRequest(chargeBill.getCharge_id(), true);
                }
            }
        }
    }

    private void sendBillRequest(String chargeId, String thirdChargeId) {
        try {
            ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(chargeId);
            if (chargeBill != null && !TextUtils.isEmpty(thirdChargeId)) {
                YZXDCAPMessage yzxdcapMessage = MonitorProtocolAgent.getInstance().createCAPRequest("report", "bill/" + thirdChargeId);
                YZXPropset yzxPropset = new YZXPropset();
                List<YZXProperty> propset = new ArrayList<>();
                YZXProperty yzxProperty = new YZXProperty();
                yzxProperty.setId("bill/" + thirdChargeId);
                CAPBillInfo capBillInfo = new CAPBillInfo();
                capBillInfo.setCharge_id(thirdChargeId);
                BillInfo billInfo = new BillInfo();
                billInfo.setLocal_charge_id(chargeId);
                billInfo.setBill_id(thirdChargeId);
                billInfo.setUser_type(chargeBill.getUser_type());
                billInfo.setUser_code(chargeBill.getUser_code());
                billInfo.setPort(chargeBill.getPort());
                ChargeStopCondition chargeStopCondition = new ChargeStopCondition();
                USER_TC_TYPE user_tc_type = chargeBill.getUser_tc_type();
                if (user_tc_type != null) {
                    chargeStopCondition.setType(user_tc_type.getType());
                }
                chargeStopCondition.setValue(chargeBill.getUser_tc_value());
                billInfo.setStop_condition(chargeStopCondition);
                billInfo.setBalance(Long.valueOf(chargeBill.getUser_balance()));
                billInfo.setLocal_charge_id(chargeBill.getCharge_id());
                billInfo.setTotal_time(Integer.valueOf((int) ((chargeBill.getStop_time() - chargeBill.getStart_time()) / 1000)));
                billInfo.setInit_time(Long.valueOf(chargeBill.getInit_time()));
                billInfo.setFin_time(Long.valueOf(chargeBill.getFin_time()));
                billInfo.setStart_time(Long.valueOf(chargeBill.getStart_time()));
                billInfo.setStop_time(Long.valueOf(chargeBill.getStop_time()));
                billInfo.setStart_ammeter(Double.valueOf(chargeBill.getStart_ammeter()));
                billInfo.setStop_ammeter(Double.valueOf(chargeBill.getStop_ammeter()));
                YZXDCAPError stopCause = new YZXDCAPError();
                CHARGE_STOP_CAUSE stop_cause = chargeBill.getStop_cause();
                if (stop_cause != null) {
                    switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE()[stop_cause.ordinal()]) {
                        case 1:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_OTHER);
                            break;
                        case 3:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_USER_LOCAL);
                            break;
                        case 4:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_USER_REMOTE);
                            break;
                        case 5:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_ADMIN_REMOTE);
                            break;
                        case 6:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_PLUGOUT);
                            break;
                        case 7:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_VEHICLE);
                            break;
                        case 8:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_FULL);
                            break;
                        case 9:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_NO_FUND);
                            break;
                        case 10:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_USER_SET);
                            break;
                        case PortRuntimeData.STATUS_EX_12 /* 12 */:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_DEVICE_ERROR);
                            break;
                    }
                    billInfo.setStop_cause(stopCause);
                }
                billInfo.setDelay_start(Long.valueOf(chargeBill.getDelay_start()));
                billInfo.setTotal_delay(Integer.valueOf(chargeBill.getTotal_delay()));
                billInfo.setDelay_info(chargeBill.getDelay_info());
                billInfo.setTotal_power(Double.valueOf(chargeBill.getTotal_power()));
                billInfo.setPower_info(chargeBill.getPower_info());
                billInfo.setService_info(chargeBill.getService_info());
                billInfo.setFee_rate_id(chargeBill.getFee_rate_id());
                billInfo.setTotal_fee(Integer.valueOf(chargeBill.getTotal_fee()));
                billInfo.setPower_fee(Integer.valueOf(chargeBill.getPower_fee()));
                billInfo.setService_fee(Integer.valueOf(chargeBill.getService_fee()));
                billInfo.setDelay_fee(Integer.valueOf(chargeBill.getDelay_fee()));
                billInfo.setPark_fee(Integer.valueOf(chargeBill.getPark_fee()));
                billInfo.setTotal_park(Integer.valueOf(chargeBill.getTotal_park()));
                billInfo.setPark_info(chargeBill.getPark_info());
                if (chargeBill.getBalance_flag() == 0) {
                    billInfo.setBalance_flag(false);
                } else if (chargeBill.getBalance_flag() == 1) {
                    billInfo.setBalance_flag(true);
                }
                billInfo.setBalance_time(Long.valueOf(chargeBill.getBalance_time()));
                billInfo.setPay_flag(true);
                long pay_time = chargeBill.getPay_time();
                if (pay_time == 0) {
                    billInfo.setPay_time(Long.valueOf(System.currentTimeMillis()));
                } else {
                    billInfo.setPay_time(Long.valueOf(pay_time));
                }
                capBillInfo.setBill_info(billInfo);
                yzxProperty.setValue(capBillInfo);
                propset.add(yzxProperty);
                yzxPropset.setPropset(propset);
                yzxdcapMessage.setData(yzxPropset);
                yzxdcapMessage.setPort(this.port);
                sendMessage(yzxdcapMessage);
                LogUtils.log("MonitorPortHandler.sendBillRequest", "send BillRequest: " + yzxdcapMessage.toJson());
            }
        } catch (Exception e) {
            LogUtils.log("MonitorPortHandler.sendBillRequest", Log.getStackTraceString(e));
        }
    }

    public void sendDelayWaitStartedRequest(String charge_id) {
        try {
            ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(charge_id);
            String localChargeId = getChargeSession().getLocal_charge_id();
            if (chargeBill != null && !TextUtils.isEmpty(localChargeId)) {
                YZXDCAPMessage yzxdcapMessage = MonitorProtocolAgent.getInstance().createCAPRequest("event", "delay_wait_started/" + localChargeId);
                YZXPropset yzxPropset = new YZXPropset();
                List<YZXProperty> propset = new ArrayList<>();
                YZXProperty yzxProperty = new YZXProperty();
                yzxProperty.setId("delay_wait_started/" + localChargeId);
                CAPDelayWaitStarted capDelayWaitStarted = new CAPDelayWaitStarted();
                capDelayWaitStarted.setCharge_id(localChargeId);
                capDelayWaitStarted.setDelay_wait_start_time(System.currentTimeMillis());
                capDelayWaitStarted.setStop_time(chargeBill.getStop_time());
                CHARGE_STOP_CAUSE stop_cause = chargeBill.getStop_cause();
                if (stop_cause != null) {
                    YZXDCAPError stopCause = new YZXDCAPError();
                    switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE()[stop_cause.ordinal()]) {
                        case 1:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_OTHER);
                            break;
                        case 3:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_USER_LOCAL);
                            break;
                        case 4:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_USER_REMOTE);
                            break;
                        case 5:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_ADMIN_REMOTE);
                            break;
                        case 6:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_PLUGOUT);
                            break;
                        case 7:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_VEHICLE);
                            break;
                        case 8:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_FULL);
                            break;
                        case 9:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_NO_FUND);
                            break;
                        case 10:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_USER_SET);
                            break;
                        case PortRuntimeData.STATUS_EX_12 /* 12 */:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_DEVICE_ERROR);
                            break;
                    }
                    capDelayWaitStarted.setStop_cause(stopCause);
                }
                capDelayWaitStarted.setStart_time(chargeBill.getStart_time());
                capDelayWaitStarted.setStart_ammeter(chargeBill.getStart_ammeter());
                capDelayWaitStarted.setStop_ammeter(chargeBill.getStop_ammeter());
                capDelayWaitStarted.setTotal_power(chargeBill.getTotal_power());
                capDelayWaitStarted.setPower_fee(chargeBill.getPower_fee());
                capDelayWaitStarted.setService_fee(chargeBill.getService_fee());
                capDelayWaitStarted.setPark_fee(chargeBill.getPark_fee());
                capDelayWaitStarted.setTotal_fee(chargeBill.getTotal_fee());
                yzxProperty.setValue(capDelayWaitStarted);
                propset.add(yzxProperty);
                yzxPropset.setPropset(propset);
                yzxdcapMessage.setData(yzxPropset);
                yzxdcapMessage.setPort(this.port);
                sendMessage(yzxdcapMessage);
                LogUtils.log("MonitorPortHandler.sendDelayWaitStartedRequest", "send DelayWaitStartedRequest:" + yzxdcapMessage.toJson());
            }
        } catch (Exception e) {
            LogUtils.log("MonitorPortHandler.sendDelayWaitStartedRequest", Log.getStackTraceString(e));
        }
    }

    public void sendDelayStartedRequest(String charge_id, long delayStart) {
        try {
            ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(charge_id);
            String localChargeId = getChargeSession().getLocal_charge_id();
            if (chargeBill != null && !TextUtils.isEmpty(localChargeId)) {
                YZXDCAPMessage yzxdcapMessage = MonitorProtocolAgent.getInstance().createCAPRequest("event", "delay_started/" + localChargeId);
                YZXPropset yzxPropset = new YZXPropset();
                List<YZXProperty> propset = new ArrayList<>();
                YZXProperty yzxProperty = new YZXProperty();
                yzxProperty.setId("delay_started/" + localChargeId);
                CAPDelayStarted capDelayStarted = new CAPDelayStarted();
                capDelayStarted.setCharge_id(localChargeId);
                capDelayStarted.setDelay_start_time(chargeBill.getDelay_start());
                capDelayStarted.setStop_time(chargeBill.getStop_time());
                CHARGE_STOP_CAUSE stop_cause = chargeBill.getStop_cause();
                if (stop_cause != null) {
                    YZXDCAPError stopCause = new YZXDCAPError();
                    switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE()[stop_cause.ordinal()]) {
                        case 1:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_OTHER);
                            break;
                        case 3:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_USER_LOCAL);
                            break;
                        case 4:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_USER_REMOTE);
                            break;
                        case 5:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_ADMIN_REMOTE);
                            break;
                        case 6:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_PLUGOUT);
                            break;
                        case 7:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_VEHICLE);
                            break;
                        case 8:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_FULL);
                            break;
                        case 9:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_NO_FUND);
                            break;
                        case 10:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_USER_SET);
                            break;
                        case PortRuntimeData.STATUS_EX_12 /* 12 */:
                            stopCause.init(ErrorCodeMapping.E_CHARGE_STOP_DEVICE_ERROR);
                            break;
                    }
                    capDelayStarted.setStop_cause(stopCause);
                }
                capDelayStarted.setStart_time(chargeBill.getStart_time());
                capDelayStarted.setStart_ammeter(chargeBill.getStart_ammeter());
                capDelayStarted.setStop_ammeter(chargeBill.getStop_ammeter());
                capDelayStarted.setTotal_power(chargeBill.getTotal_power());
                capDelayStarted.setPower_fee(chargeBill.getPower_fee());
                capDelayStarted.setService_fee(chargeBill.getService_fee());
                capDelayStarted.setPark_fee(chargeBill.getPark_fee());
                capDelayStarted.setTotal_fee(chargeBill.getTotal_fee());
                yzxProperty.setValue(capDelayStarted);
                propset.add(yzxProperty);
                yzxPropset.setPropset(propset);
                yzxdcapMessage.setData(yzxPropset);
                yzxdcapMessage.setPort(this.port);
                sendMessage(yzxdcapMessage);
                LogUtils.log("MonitorPortHandler.sendDelayStartedRequest", "send DelayStartedRequest: " + yzxdcapMessage.toJson());
            }
        } catch (Exception e) {
            LogUtils.log("MonitorPortHandler.sendDelayStartedRequest", Log.getStackTraceString(e));
        }
    }

    public void sendDelayInfoRequest(String charge_id) {
        try {
            ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(charge_id);
            String localChargeId = getChargeSession().getLocal_charge_id();
            if (chargeBill != null && !TextUtils.isEmpty(localChargeId)) {
                YZXDCAPMessage yzxdcapMessage = MonitorProtocolAgent.getInstance().createCAPRequest("report", "delay/" + localChargeId);
                YZXPropset yzxPropset = new YZXPropset();
                List<YZXProperty> propset = new ArrayList<>();
                YZXProperty yzxProperty = new YZXProperty();
                yzxProperty.setId("delay/" + localChargeId);
                CAPDelayInfo capDelayInfo = new CAPDelayInfo();
                capDelayInfo.setCharge_id(localChargeId);
                capDelayInfo.setTotal_delay(chargeBill.getTotal_delay());
                capDelayInfo.setDelay_fee(chargeBill.getDelay_fee());
                capDelayInfo.setPower_fee(chargeBill.getPower_fee());
                capDelayInfo.setService_fee(chargeBill.getService_fee());
                capDelayInfo.setPark_fee(chargeBill.getPark_fee());
                capDelayInfo.setTotal_fee(chargeBill.getTotal_fee());
                yzxProperty.setValue(capDelayInfo);
                propset.add(yzxProperty);
                yzxPropset.setPropset(propset);
                yzxdcapMessage.setData(yzxPropset);
                yzxdcapMessage.setPort(this.port);
                sendMessage(yzxdcapMessage);
                LogUtils.log("MonitorPortHandler.sendDelayInfoRequest", "send DelayInfoRequest: " + yzxdcapMessage.toJson());
            }
        } catch (Exception e) {
            LogUtils.log("MonitorPortHandler.sendDelayInfoRequest", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void clearPortActiveStatus() {
        this.handlerTimer.stopTimer(73745);
        this.handlerTimer.stopTimer(73746);
        this.handlerTimer.stopTimer(73747);
        this.handler.removeMessages(73731);
        this.handler.removeMessages(73732);
        this.handler.removeMessages(73733);
        this.handler.removeMessages(73734);
        this.handler.removeMessages(73735);
        this.handler.removeMessages(73736);
    }

    private boolean sendMessage(YZXDCAPMessage msg) {
        return MonitorProtocolAgent.getInstance().sendMessage(msg);
    }
}