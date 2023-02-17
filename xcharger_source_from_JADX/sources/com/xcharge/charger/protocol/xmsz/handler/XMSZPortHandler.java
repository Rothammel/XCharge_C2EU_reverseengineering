package com.xcharge.charger.protocol.xmsz.handler;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.p000v4.view.MotionEventCompat;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.CHARGE_STATUS;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.data.proxy.ChargeContentProxy;
import com.xcharge.charger.data.proxy.PortChargeStatusObserver;
import com.xcharge.charger.data.proxy.PortStatusObserver;
import com.xcharge.charger.device.p005c2.bean.PortRuntimeData;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.charger.protocol.xmsz.bean.XMSZPortStatus;
import com.xcharge.charger.protocol.xmsz.bean.cloud.GetChargeInfoRequest;
import com.xcharge.charger.protocol.xmsz.bean.cloud.RemoteStartChargingRequest;
import com.xcharge.charger.protocol.xmsz.bean.cloud.RemoteStopChargingRequest;
import com.xcharge.charger.protocol.xmsz.bean.cloud.StatusNotificationResponse;
import com.xcharge.charger.protocol.xmsz.bean.device.GetChargeInfoResponse;
import com.xcharge.charger.protocol.xmsz.bean.device.RemoteStartChargingResponse;
import com.xcharge.charger.protocol.xmsz.bean.device.RemoteStopChargingResponse;
import com.xcharge.charger.protocol.xmsz.bean.device.StatusNotificationRequest;
import com.xcharge.charger.protocol.xmsz.router.XMSZDCAPGateway;
import com.xcharge.charger.protocol.xmsz.session.XMSZChargeSession;
import com.xcharge.charger.protocol.xmsz.session.XMSZRequestSession;
import com.xcharge.charger.protocol.xmsz.type.XMSZ_PILE_PRESENCE_STATE;
import com.xcharge.common.utils.HandlerTimer;
import java.math.BigDecimal;

public class XMSZPortHandler {
    public static final int MSG_CLOUD_REQUEST = 73735;
    public static final int MSG_CLOUD_RESPONSE = 73736;
    public static final int MSG_HERAT_BEAT_TIMER = 73737;
    public static final int MSG_PILE_OFFLINE = 73730;
    public static final int MSG_PILE_ONLINE = 73729;
    public static final int MSG_REPORT_CHARGE_TIMER = 73744;
    public static final int MSG_REPORT_HISTORY_CHARGE_TIMER = 73745;
    public static final int MSG_REQUEST_SEND_FAIL = 73732;
    public static final int MSG_REQUEST_SEND_OK = 73731;
    public static final int MSG_REQUEST_TIMEOUT = 73733;
    public static final int MSG_REQUSET_RESEND = 73734;
    public static final int TIMEOUT_REPORT_CHARGE = 120;
    public static final int TIMEOUT_REPORT_HISTORY_CHARGE = 120;
    private XMSZChargeSession chargeSession = null;
    private Context context = null;
    private MsgHandler handler = null;
    /* access modifiers changed from: private */
    public HandlerTimer handlerTimer = null;
    private boolean isPortPermitCharge = true;
    private XMSZPortStatus latestPortStatus = new XMSZPortStatus();
    /* access modifiers changed from: private */
    public String port = null;
    private PortChargeStatusObserver portChargeStatusObserver = null;
    private PortStatusObserver portStatusObserver = null;
    private CHARGE_STATUS status = CHARGE_STATUS.IDLE;
    private HandlerThread thread = null;
    private XMSZ_PILE_PRESENCE_STATE xmszPileStatus = XMSZ_PILE_PRESENCE_STATE.offline;

    private class MsgHandler extends Handler {
        public MsgHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 73729:
                    Log.i("XMSZPortHandler.MsgHandler", "pile online !!! port: " + XMSZPortHandler.this.port);
                    XMSZPortHandler.this.handlePileOnline(((Boolean) msg.obj).booleanValue());
                    break;
                case 73730:
                    Log.i("XMSZPortHandler.MsgHandler", "pile offline !!! port: " + XMSZPortHandler.this.port);
                    XMSZPortHandler.this.handlePileOffline();
                    break;
                case 73731:
                    XMSZMessage xmszMessage = (XMSZMessage) msg.obj;
                    break;
                case 73732:
                    XMSZPortHandler.this.handleFailedRequest((XMSZMessage) msg.obj);
                    break;
                case 73733:
                    XMSZMessage xmszMessage2 = (XMSZMessage) msg.obj;
                    Log.w("XMSZPortHandler.MsgHandler", "send xmsz request timeout: " + xmszMessage2.toJson());
                    XMSZPortHandler.this.handleFailedRequest(xmszMessage2);
                    break;
                case 73734:
                    XMSZPortHandler.this.resendRequest((XMSZMessage) msg.obj);
                    break;
                case 73735:
                    XMSZPortHandler.this.handleRequest((XMSZMessage) msg.obj);
                    break;
                case 73736:
                    XMSZRequestSession xmszRequestSession = (XMSZRequestSession) msg.obj;
                    XMSZPortHandler.this.handleResponse(xmszRequestSession.getSendedRequest(), xmszRequestSession.getResponse());
                    break;
                case 73745:
                    XMSZPortHandler.this.handlerTimer.startTimer(120000, 73745, (Object) null);
                    break;
                case PortChargeStatusObserver.MSG_PORT_CHARGE_STATUS_CHANGE:
                    Uri uri = (Uri) msg.obj;
                    Log.i("XMSZPortHandler.MsgHandler", "port charge status changed, port: " + XMSZPortHandler.this.port + ", uri: " + uri.toString());
                    XMSZPortHandler.this.handlePortChargeStatusChanged(uri);
                    break;
                case PortStatusObserver.MSG_PORT_STATUS_CHANGE:
                    Uri uri2 = (Uri) msg.obj;
                    Log.i("XMSZPortHandler.MsgHandler", "port status changed, port: " + XMSZPortHandler.this.port + ", uri: " + uri2.toString());
                    XMSZPortHandler.this.handlePortStatusChanged(uri2);
                    break;
            }
            super.handleMessage(msg);
        }
    }

    public void init(Context context2, String port2, XMSZProtocolAgent protocolHandler) {
        this.context = context2;
        this.port = port2;
        this.thread = new HandlerThread("XMSZPortHandler#" + this.port, 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(context2);
        this.portStatusObserver = new PortStatusObserver(this.context, this.port, this.handler);
        this.context.getContentResolver().registerContentObserver(HardwareStatusCacheProvider.getInstance().getUriFor("ports/" + this.port), true, this.portStatusObserver);
        this.portChargeStatusObserver = new PortChargeStatusObserver(this.context, this.port, this.handler);
        this.context.getContentResolver().registerContentObserver(ChargeStatusCacheProvider.getInstance().getUriFor("ports/" + this.port), true, this.portChargeStatusObserver);
        updatePortStatusByPriority(getPortStatusByPriority());
    }

    public void destroy() {
        this.context.getContentResolver().unregisterContentObserver(this.portChargeStatusObserver);
        this.context.getContentResolver().unregisterContentObserver(this.portStatusObserver);
        this.handlerTimer.destroy();
        this.handler.removeMessages(73729);
        this.handler.removeMessages(73730);
        this.handler.removeMessages(73731);
        this.handler.removeMessages(73732);
        this.handler.removeMessages(73733);
        this.handler.removeMessages(73734);
        this.handler.removeMessages(73735);
        this.handler.removeMessages(73736);
        this.handler.removeMessages(73744);
        this.handler.removeMessages(73745);
        this.handler.removeMessages(PortChargeStatusObserver.MSG_PORT_CHARGE_STATUS_CHANGE);
        this.handler.removeMessages(PortStatusObserver.MSG_PORT_STATUS_CHANGE);
        this.thread.quit();
    }

    /* access modifiers changed from: private */
    public void handlePileOnline(boolean isPilePermitCharge) {
        this.xmszPileStatus = XMSZ_PILE_PRESENCE_STATE.online;
        this.isPortPermitCharge = isPilePermitCharge;
        if (isPilePermitCharge) {
            this.handlerTimer.startTimer(120000, 73745, (Object) null);
            sendPortStatusNotificationRequest();
            XMSZProtocolAgent.getInstance().sendPileStatusNotificationRequest();
        }
    }

    /* access modifiers changed from: private */
    public void handlePileOffline() {
        this.handlerTimer.stopTimer(73745);
        this.handler.removeMessages(73731);
        this.handler.removeMessages(73732);
        this.handler.removeMessages(73733);
        this.handler.removeMessages(73734);
        this.handler.removeMessages(73735);
        this.handler.removeMessages(73736);
        this.xmszPileStatus = XMSZ_PILE_PRESENCE_STATE.offline;
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

    public XMSZChargeSession getChargeSession() {
        if (this.chargeSession == null) {
            this.chargeSession = new XMSZChargeSession();
        }
        return this.chargeSession;
    }

    private void clearChargeSession() {
        this.chargeSession = null;
    }

    public boolean hasCharge(String chargeId) {
        return chargeId.equals(getChargeSession().getCharge_id());
    }

    /* access modifiers changed from: private */
    public void handleFailedRequest(XMSZMessage request) {
    }

    /* access modifiers changed from: private */
    public void handlePortStatusChanged(Uri uri) {
        if (updatePortStatusByPriority(getPortStatusByPriority())) {
            sendPortStatusNotificationRequest();
            XMSZProtocolAgent.getInstance().sendPileStatusNotificationRequestByPortStatusChanged();
        }
    }

    /* access modifiers changed from: private */
    public void handlePortChargeStatusChanged(Uri uri) {
        if (updatePortStatusByPriority(getPortStatusByPriority())) {
            sendPortStatusNotificationRequest();
            XMSZProtocolAgent.getInstance().sendPileStatusNotificationRequestByPortStatusChanged();
        }
    }

    private void sendPortStatusNotificationRequest() {
        try {
            StatusNotificationRequest statusNotificationRequest = new StatusNotificationRequest();
            statusNotificationRequest.setConnectorId((byte) (Integer.parseInt(this.port) & MotionEventCompat.ACTION_MASK));
            statusNotificationRequest.setConnectorPlugStatus((byte) (this.latestPortStatus.isPortPlugin() ? 1 : 0));
            statusNotificationRequest.setPointStatusCode(this.latestPortStatus.getPortStatus());
            statusNotificationRequest.setPointErrorCode(this.latestPortStatus.getPortError());
            statusNotificationRequest.setTime(System.currentTimeMillis() / 1000);
            XMSZHead head = XMSZProtocolAgent.getInstance().createRequestHead((byte) 22);
            head.setPacketLength(statusNotificationRequest.bodyToBytes().length + 12);
            statusNotificationRequest.setHead(head);
            statusNotificationRequest.setCrc16(statusNotificationRequest.calcCheckSum());
            sendMessage((XMSZMessage) statusNotificationRequest);
            Log.i("XMSZPortHandler.sendPortStatusNotificationRequest", "send statusNotificationRequest: " + statusNotificationRequest.toJson());
        } catch (Exception e) {
            Log.w("XMSZPortHandler.sendPortStatusNotificationRequest", Log.getStackTraceString(e));
        }
    }

    /* access modifiers changed from: private */
    public void handleRequest(XMSZMessage request) {
        switch (request.getHead().getFunctionCode()) {
            case 2:
                Log.i("XMSZPortHandler.handleRequest", "receive RemoteStartChargingRequest:" + request.toJson());
                if (this.isPortPermitCharge) {
                    XMSZDCAPGateway.getInstance().sendMessage(XMSZDCAPGateway.getInstance().obtainMessage(77827, request));
                    return;
                }
                return;
            case 3:
                Log.i("XMSZPortHandler.handleRequest", "receive RemoteStopChargingRequest:" + request.toJson());
                XMSZDCAPGateway.getInstance().sendMessage(XMSZDCAPGateway.getInstance().obtainMessage(77827, request));
                return;
            case PortRuntimeData.STATUS_EX_12:
                Log.i("XMSZPortHandler.handleRequest", "receive GetChargeInfoRequest:" + request.toJson());
                XMSZDCAPGateway.getInstance().sendMessage(XMSZDCAPGateway.getInstance().obtainMessage(77827, request));
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: private */
    public void handleResponse(XMSZMessage request, XMSZMessage response) {
        switch (response.getHead().getFunctionCode()) {
            case -109:
                Log.i("XMSZPortHandler.handleResponse", "receive StartTransactionResponse:" + response.toJson());
                XMSZDCAPGateway.getInstance().sendMessage(XMSZDCAPGateway.getInstance().obtainMessage(77828, response));
                return;
            case -108:
                Log.i("XMSZPortHandler.handleResponse", "receive StopTransactionResponse:" + response.toJson());
                XMSZDCAPGateway.getInstance().sendMessage(XMSZDCAPGateway.getInstance().obtainMessage(77828, response));
                return;
            case -106:
                Log.i("XMSZPortHandler.handleResponse", "receive StatusNotificationResponse:" + response.toJson());
                handleStatusNotificationResponse(request, response);
                return;
            default:
                return;
        }
    }

    private void sendMessage(XMSZMessage msg) {
        XMSZProtocolAgent.getInstance().sendMessage(msg);
    }

    private void handleStatusNotificationResponse(XMSZMessage request, XMSZMessage response) {
        byte returnCode = ((StatusNotificationResponse) response).getReturnCode();
        if (2 == returnCode) {
            Log.e("XMSZPortHandler.handleStatusNotificationResponse", "error CRC !!!");
            if (request.getRetrySend() < 1) {
                this.handler.sendMessage(this.handler.obtainMessage(73734, request));
            }
        } else if (returnCode == 0) {
            request.setRetrySend(0);
            Log.w("XMSZPortHandler.handleStatusNotificationResponse", "failed to request StatusNotification !!!");
        } else if (3 == returnCode) {
            request.setRetrySend(0);
            Log.w("XMSZPortHandler.handleStatusNotificationResponse", "system busy, reject request StatusNotification !!!");
        } else {
            request.setRetrySend(0);
        }
    }

    /* access modifiers changed from: private */
    public void resendRequest(XMSZMessage request) {
        Log.w("XMSZPortHandler.resendRequest", "resend xmsz request: " + request.toJson());
        try {
            request.getHead().setPacketID(XMSZProtocolAgent.getInstance().getRequestSequence());
            request.setHead(request.getHead());
            request.setCrc16(request.calcCheckSum());
            sendMessage(request);
            request.setRetrySend(request.getRetrySend() + 1);
        } catch (Exception e) {
            Log.w("XMSZPortHandler.resendRequest", Log.getStackTraceString(e));
        }
    }

    private void responseGetChargeInfo(GetChargeInfoRequest request, String chargeId) {
        try {
            PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port);
            long startChargeTime = portStatus.getChargeStartTime();
            XMSZChargeSession chargeSession2 = getChargeSession();
            GetChargeInfoResponse getChargeInfoResponse = new GetChargeInfoResponse();
            if (!chargeId.equals(chargeSession2.getCharge_id()) || startChargeTime <= 0) {
                getChargeInfoResponse.setReturnCode((byte) 0);
            } else {
                int amp = new BigDecimal(portStatus.getAmps().get(0).doubleValue() * 10.0d).setScale(0, 4).intValue();
                int volt = new BigDecimal(portStatus.getVolts().get(0).doubleValue() * 10.0d).setScale(0, 4).intValue();
                int power = new BigDecimal(portStatus.getKwatt().doubleValue() * 100.0d).setScale(0, 4).intValue();
                short temprature = new BigDecimal(portStatus.getTemprature().doubleValue() * 100.0d).setScale(0, 4).shortValue();
                int chargeTime = new BigDecimal((System.currentTimeMillis() - portStatus.getChargeStartTime()) / 1000).setScale(0, 4).intValue();
                int meter = new BigDecimal(portStatus.getPower().doubleValue() * 100.0d).setScale(0, 4).intValue();
                getChargeInfoResponse.setReturnCode((byte) 1);
                getChargeInfoResponse.setTransactionId(Long.parseLong(chargeId));
                getChargeInfoResponse.setChargeTime(chargeTime);
                getChargeInfoResponse.setTemperature(temprature);
                getChargeInfoResponse.setCurrentPower(power);
                getChargeInfoResponse.setCurrentVoltage(volt);
                getChargeInfoResponse.setCurrentAvailable(amp);
                getChargeInfoResponse.setMaxVoltage(volt);
                getChargeInfoResponse.setMaxTemperature(temprature);
                getChargeInfoResponse.setMeterValue(meter);
            }
            getChargeInfoResponse.setPort(this.port);
            XMSZHead head = XMSZProtocolAgent.getInstance().createResponseHead(request.getHead());
            head.setPacketLength(getChargeInfoResponse.bodyToBytes().length + 12);
            getChargeInfoResponse.setHead(head);
            getChargeInfoResponse.setCrc16(getChargeInfoResponse.calcCheckSum());
            sendMessage((XMSZMessage) getChargeInfoResponse);
            Log.i("XMSZPortHandler.responseGetChargeInfo", "send GetChargeInfo response: " + getChargeInfoResponse.toJson());
        } catch (Exception e) {
            Log.w("XMSZPortHandler.responseGetChargeInfo", Log.getStackTraceString(e));
        }
    }

    public boolean responseRemoteStartCharging(RemoteStartChargingRequest request, byte statusCode) {
        try {
            RemoteStartChargingResponse response = new RemoteStartChargingResponse();
            response.setReturnCode(statusCode);
            XMSZHead head = XMSZProtocolAgent.getInstance().createResponseHead(request.getHead());
            head.setPacketLength(response.bodyToBytes().length + 12);
            response.setHead(head);
            response.setCrc16(response.calcCheckSum());
            response.setPort(this.port);
            sendMessage((XMSZMessage) response);
            Log.i("XMSZPortHandler.responseRemoteStartCharging", "send RemoteStartCharging response: " + response.toJson());
            return true;
        } catch (Exception e) {
            Log.w("XMSZPortHandler.responseRemoteStartCharging", Log.getStackTraceString(e));
            return false;
        }
    }

    public boolean responseRemoteStopCharging(RemoteStopChargingRequest request, byte statusCode) {
        try {
            RemoteStopChargingResponse response = new RemoteStopChargingResponse();
            response.setReturnCode(statusCode);
            XMSZHead head = XMSZProtocolAgent.getInstance().createResponseHead(request.getHead());
            head.setPacketLength(response.bodyToBytes().length + 12);
            response.setHead(head);
            response.setCrc16(response.calcCheckSum());
            response.setPort(this.port);
            sendMessage((XMSZMessage) response);
            Log.i("XMSZPortHandler.responseRemoteStopCharging", "send RemoteStopCharging response: " + response.toJson());
            return true;
        } catch (Exception e) {
            Log.w("XMSZPortHandler.responseRemoteStopCharging", Log.getStackTraceString(e));
            return false;
        }
    }

    private byte getPortErrorCode(ErrorCode error) {
        switch (error.getCode()) {
            case 200:
                return 4;
            case ErrorCode.EC_DEVICE_NO_GROUND:
                return 2;
            case ErrorCode.EC_DEVICE_VOLT_ERROR:
                return 16;
            case ErrorCode.EC_DEVICE_AMP_ERROR:
                return 18;
            case ErrorCode.EC_DEVICE_TEMP_ERROR:
                return 3;
            default:
                return -1;
        }
    }

    public XMSZPortStatus getLatestPortStatus() {
        return this.latestPortStatus;
    }

    private XMSZPortStatus getPortStatusByPriority() {
        XMSZPortStatus changedPortStatus = new XMSZPortStatus();
        PortStatus portChargeStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(this.port);
        Log.d("XMSZPortHandler.getPortStatusByPriority", "now port charge status: " + portChargeStatus.toJson());
        CHARGE_STATUS chargeStatus = portChargeStatus.getChargeStatus();
        if (!this.status.equals(chargeStatus)) {
            if (chargeStatus.equals(CHARGE_STATUS.CHARGE_START_WAITTING)) {
                Log.i("XMSZPortHandler.getPortStatusByPriority", "enter wait charge status !!!");
                XMSZChargeSession chargeSession2 = getChargeSession();
                String chargeId = portChargeStatus.getCharge_id();
                ChargeBill chargeBill = ChargeContentProxy.getInstance().getChargeBill(chargeId);
                if (chargeBill != null) {
                    chargeSession2.setCharge_id(chargeId);
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
                    Log.w("XMSZPortHandler.getPortStatusByPriority", "failed to query info for charge: " + chargeId);
                }
            } else if (chargeStatus.equals(CHARGE_STATUS.CHARGING)) {
                Log.i("XMSZPortHandler.getPortStatusByPriority", "enter charging status !!!");
                XMSZChargeSession chargeSession3 = getChargeSession();
                if (TextUtils.isEmpty(chargeSession3.getCharge_id())) {
                    String chargeId2 = portChargeStatus.getCharge_id();
                    ChargeBill chargeBill2 = ChargeContentProxy.getInstance().getChargeBill(chargeId2);
                    if (chargeBill2 != null) {
                        chargeSession3.setCharge_id(chargeId2);
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
                        Log.w("XMSZPortHandler.getPortStatusByPriority", "failed to query info for charge: " + chargeId2);
                    }
                }
            } else if (chargeStatus.equals(CHARGE_STATUS.CHARGE_STOP_WAITTING)) {
                Log.i("XMSZPortHandler.getPortStatusByPriority", "enter charge stopped status !!!");
            } else if (chargeStatus.equals(CHARGE_STATUS.IDLE)) {
                Log.i("XMSZPortHandler.getPortStatusByPriority", "enter idle status !!!");
                clearChargeSession();
            }
            this.status = chargeStatus;
        }
        Port portStatus = HardwareStatusCacheProvider.getInstance().getPort(this.port);
        changedPortStatus.setPortPlugin(portStatus.isPlugin());
        changedPortStatus.setPortError(getPortErrorCode(portStatus.getDeviceError()));
        if (changedPortStatus.getPortError() != 4) {
            changedPortStatus.setPortStatus((byte) 2);
        } else if (!this.isPortPermitCharge) {
            changedPortStatus.setPortStatus((byte) 3);
        } else if (!CHARGE_STATUS.IDLE.equals(this.status)) {
            changedPortStatus.setPortStatus((byte) 1);
        } else {
            changedPortStatus.setPortStatus((byte) 0);
        }
        return changedPortStatus;
    }

    private boolean updatePortStatusByPriority(XMSZPortStatus newPortStatus) {
        byte newPortStatusCode = newPortStatus.getPortStatus();
        byte latestPortStatusCode = this.latestPortStatus.getPortStatus();
        if (newPortStatusCode != latestPortStatusCode) {
            if (latestPortStatusCode == 3) {
                if (newPortStatusCode == 2 || newPortStatusCode == 0) {
                    this.latestPortStatus = newPortStatus;
                    return true;
                }
            } else if (latestPortStatusCode == 1) {
                if (!(newPortStatusCode == 3 || newPortStatusCode == 4)) {
                    this.latestPortStatus = newPortStatus;
                    return true;
                }
            } else if (latestPortStatusCode == 4) {
                if (newPortStatusCode != 3) {
                    this.latestPortStatus = newPortStatus;
                    return true;
                }
            } else if (latestPortStatusCode == 0) {
                this.latestPortStatus = newPortStatus;
                return true;
            }
        } else if (newPortStatusCode == 2 && newPortStatus.getPortError() != this.latestPortStatus.getPortError()) {
            this.latestPortStatus = newPortStatus;
            return true;
        }
        if (newPortStatus.isPortPlugin() == this.latestPortStatus.isPortPlugin()) {
            return false;
        }
        this.latestPortStatus = newPortStatus;
        return true;
    }
}
