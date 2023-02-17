package com.xcharge.charger.core.controller;

import android.content.Context;
import android.util.Log;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.api.Sequence;
import com.xcharge.charger.core.api.bean.AckDirective;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.NackDirective;
import com.xcharge.charger.core.api.bean.cap.AuthDirective;
import com.xcharge.charger.core.api.bean.cap.CAPDirectiveOption;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.core.api.bean.cap.ConditionDirective;
import com.xcharge.charger.core.api.bean.cap.EventDirective;
import com.xcharge.charger.core.api.bean.cap.FinDirective;
import com.xcharge.charger.core.api.bean.cap.InitDirective;
import com.xcharge.charger.core.api.bean.cap.StartDirective;
import com.xcharge.charger.core.api.bean.cap.StopDirective;
import com.xcharge.charger.core.bean.RequestSession;
import com.xcharge.charger.core.handler.ChargeHandler;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.CHARGE_INIT_TYPE;
import com.xcharge.charger.data.bean.type.PARK_STATUS;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.device.api.PortStatusListener;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.LogUtils;
import java.util.HashMap;
import java.util.Map;

/* loaded from: classes.dex */
public class ChargeController implements PortStatusListener {
    private static ChargeController instance = null;
    private Context context = null;
    private HashMap<String, ChargeHandler> chargeHandlers = new HashMap<>();

    public static ChargeController getInstance() {
        if (instance == null) {
            instance = new ChargeController();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        HashMap<String, Port> ports = HardwareStatusCacheProvider.getInstance().getPorts();
        if (ports != null) {
            for (String port : ports.keySet()) {
                ChargeHandler chargeHandler = new ChargeHandler();
                chargeHandler.init(this.context, port);
                this.chargeHandlers.put(port, chargeHandler);
            }
        }
    }

    public void destroy() {
        for (ChargeHandler chargeHandler : this.chargeHandlers.values()) {
            chargeHandler.destroy();
        }
        this.chargeHandlers.clear();
    }

    public void handleRequest(DCAPMessage request, DCAPMessage confirm) {
        String port;
        int handlerMsg;
        CAPMessage cap = (CAPMessage) request.getData();
        String op = cap.getOp();
        try {
            if ("auth".equals(op)) {
                AuthDirective auth = new AuthDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                cap.setData(auth);
                port = auth.getPort();
                handlerMsg = ChargeHandler.MSG_REQUEST_AUTH;
            } else if ("init".equals(op)) {
                InitDirective init = new InitDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                cap.setData(init);
                port = init.getPort();
                handlerMsg = ChargeHandler.MSG_REQUEST_INIT;
            } else if ("fin".equals(op)) {
                FinDirective fin = new FinDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                cap.setData(fin);
                String chargeId = cap.getOpt().getCharge_id();
                port = getPortByChargeId(chargeId);
                handlerMsg = ChargeHandler.MSG_REQUEST_FIN;
            } else if ("start".equals(op)) {
                StartDirective start = new StartDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                cap.setData(start);
                String chargeId2 = cap.getOpt().getCharge_id();
                port = getPortByChargeId(chargeId2);
                handlerMsg = ChargeHandler.MSG_REQUEST_START;
            } else if ("stop".equals(op)) {
                StopDirective stop = new StopDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                cap.setData(stop);
                String chargeId3 = cap.getOpt().getCharge_id();
                port = getPortByChargeId(chargeId3);
                handlerMsg = ChargeHandler.MSG_REQUEST_STOP;
            } else if ("event".equals(op)) {
                CAPDirectiveOption opt = cap.getOpt();
                String eventId = opt.getEvent_id();
                if (EventDirective.EVENT_CHARGE_REFUSE.equals(eventId)) {
                    EventDirective event = new EventDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                    cap.setData(event);
                    port = opt.getPort_id();
                    handlerMsg = ChargeHandler.MSG_CHARGE_REFUSE_EVENT;
                } else if (EventDirective.EVENT_SCAN_ADVERT_FIN.equals(eventId)) {
                    EventDirective event2 = new EventDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                    cap.setData(event2);
                    String chargeId4 = cap.getOpt().getCharge_id();
                    port = getPortByChargeId(chargeId4);
                    handlerMsg = ChargeHandler.MSG_SCAN_ADVERT_FIN_EVENT;
                } else {
                    return;
                }
            } else if (CAPMessage.DIRECTIVE_CONDITION.equals(op)) {
                ConditionDirective condition = new ConditionDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                cap.setData(condition);
                port = cap.getOpt().getPort_id();
                handlerMsg = ChargeHandler.MSG_REQUEST_CONDITION;
            } else {
                return;
            }
            ChargeHandler portHandler = this.chargeHandlers.get(port);
            if (portHandler == null) {
                Log.e("ChargeController.handleRequest", "your request related port is not exist !!! port: " + port);
                if (!"init".equals(op) && !"fin".equals(op)) {
                    nackConfirm(confirm, 10000, "invail port", null);
                }
            } else if (handlerMsg != 32769) {
                RequestSession requestSession = new RequestSession();
                requestSession.setRequest(request);
                requestSession.setConfirm(confirm);
                portHandler.sendMessage(portHandler.obtainMessage(handlerMsg, requestSession));
            }
        } catch (Exception e) {
            Log.e("ChargeController.handleRequest", "request: " + request.toJson() + ", exception: " + Log.getStackTraceString(e));
            LogUtils.syslog("ChargeController handleRequest: " + request.toJson() + ", exception: " + Log.getStackTraceString(e));
            nackConfirm(confirm, 10000, e.toString(), null);
        }
    }

    public void handleResponse(DCAPMessage response) {
        int handlerMsg;
        String port;
        CAPMessage cap = (CAPMessage) response.getData();
        String op = cap.getOp();
        try {
            if ("ack".equals(op)) {
                AckDirective ack = new AckDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                cap.setData(ack);
                handlerMsg = ChargeHandler.MSG_RESPONSE_ACK;
            } else if (CAPMessage.DIRECTIVE_NACK.equals(op)) {
                NackDirective nack = new NackDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                cap.setData(nack);
                handlerMsg = ChargeHandler.MSG_RESPONSE_NACK;
            } else {
                return;
            }
            String peerOp = cap.getOpt().getOp();
            if ("auth".equals(peerOp)) {
                port = cap.getOpt().getPort_id();
            } else {
                String chargeId = cap.getOpt().getCharge_id();
                port = getPortByChargeId(chargeId);
            }
            ChargeHandler portHandler = this.chargeHandlers.get(port);
            if (portHandler == null) {
                Log.e("ChargeController.handleResponse", "your reponse related port is not exist !!! port: " + port);
            } else if (handlerMsg != 32769) {
                portHandler.sendMessage(portHandler.obtainMessage(handlerMsg, response));
            }
        } catch (Exception e) {
            Log.e("ChargeController.handleResponse", "response: " + response.toJson() + ", exception: " + Log.getStackTraceString(e));
        }
    }

    public ChargeHandler getChargeHandler(String port) {
        return this.chargeHandlers.get(port);
    }

    private String getPortByChargeId(String chargeId) {
        for (Map.Entry<String, ChargeHandler> entry : this.chargeHandlers.entrySet()) {
            ChargeHandler chargeHandler = entry.getValue();
            if (chargeHandler.hasCharge(chargeId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public boolean hasCharge(CHARGE_INIT_TYPE initType, boolean isLocal) {
        for (Map.Entry<String, ChargeHandler> entry : this.chargeHandlers.entrySet()) {
            ChargeHandler chargeHandler = entry.getValue();
            if (!chargeHandler.isIdle(initType, isLocal)) {
                return true;
            }
        }
        return false;
    }

    public static boolean nackConfirm(DCAPMessage confirm, int error, String msg, HashMap<String, Object> attach) {
        NackDirective nack = new NackDirective();
        nack.setError(error);
        nack.setMsg(msg);
        nack.setAttach(attach);
        return DCAPProxy.getInstance().sendConfirm(confirm, "cap", CAPMessage.DIRECTIVE_NACK, nack);
    }

    public static boolean ackConfirm(DCAPMessage confirm, HashMap<String, Object> attach) {
        AckDirective ack = new AckDirective();
        ack.setAttach(attach);
        return DCAPProxy.getInstance().sendConfirm(confirm, "cap", "ack", ack);
    }

    public static DCAPMessage createIndicate(String to, String op, CAPDirectiveOption opt, Object directive) {
        CAPMessage indicateCap = new CAPMessage();
        DCAPMessage indicate = new DCAPMessage();
        indicate.setFrom(DCAPProxy.getInstance().getCoreSendDCAPFrom(to));
        indicate.setTo(to);
        indicate.setType("cap");
        indicate.setCtime(System.currentTimeMillis());
        indicate.setSeq(Sequence.getCoreDCAPSequence());
        indicateCap.setOp(op);
        indicateCap.setOpt(opt);
        indicateCap.setData(directive);
        indicate.setData(indicateCap);
        return indicate;
    }

    @Override // com.xcharge.charger.device.api.PortStatusListener
    public void onPlugin(String port, PortStatus data) {
        ChargeHandler portHandler = this.chargeHandlers.get(port);
        portHandler.sendMessage(portHandler.obtainMessage(ChargeHandler.MSG_PORT_PLUGIN, data));
    }

    @Override // com.xcharge.charger.device.api.PortStatusListener
    public void onPlugout(String port, PortStatus data) {
        ChargeHandler portHandler = this.chargeHandlers.get(port);
        portHandler.sendMessage(portHandler.obtainMessage(ChargeHandler.MSG_PORT_PLUGOUT, data));
    }

    @Override // com.xcharge.charger.device.api.PortStatusListener
    public void onChargeStart(String port, PortStatus data) {
        ChargeHandler portHandler = this.chargeHandlers.get(port);
        portHandler.sendMessage(portHandler.obtainMessage(ChargeHandler.MSG_PORT_CHARGE_STARTED, data));
    }

    @Override // com.xcharge.charger.device.api.PortStatusListener
    public void onChargeFull(String port, PortStatus data) {
        ChargeHandler portHandler = this.chargeHandlers.get(port);
        portHandler.sendMessage(portHandler.obtainMessage(ChargeHandler.MSG_PORT_CHARGE_FULL, data));
    }

    @Override // com.xcharge.charger.device.api.PortStatusListener
    public void onChargeStop(String port, PortStatus data) {
        ChargeHandler portHandler = this.chargeHandlers.get(port);
        portHandler.sendMessage(portHandler.obtainMessage(ChargeHandler.MSG_PORT_CHARGE_STOPPED, data));
    }

    @Override // com.xcharge.charger.device.api.PortStatusListener
    public void onSuspend(String port, PortStatus data) {
        ChargeHandler portHandler = this.chargeHandlers.get(port);
        portHandler.sendMessage(portHandler.obtainMessage(ChargeHandler.MSG_PORT_CHARGE_SUSPEND, data));
    }

    @Override // com.xcharge.charger.device.api.PortStatusListener
    public void onResume(String port, PortStatus data) {
        ChargeHandler portHandler = this.chargeHandlers.get(port);
        portHandler.sendMessage(portHandler.obtainMessage(ChargeHandler.MSG_PORT_CHARGE_RESUME, data));
    }

    @Override // com.xcharge.charger.device.api.PortStatusListener
    public void onWarning(String port) {
        ChargeHandler portHandler = this.chargeHandlers.get(port);
        portHandler.sendMessage(portHandler.obtainMessage(ChargeHandler.MSG_PORT_WARN));
    }

    @Override // com.xcharge.charger.device.api.PortStatusListener
    public void onUpdate(String port, PortStatus data) {
        ChargeHandler portHandler = this.chargeHandlers.get(port);
        portHandler.sendMessage(portHandler.obtainMessage(ChargeHandler.MSG_PORT_UPDATE, data));
    }

    @Override // com.xcharge.charger.device.api.PortStatusListener
    public void onParkBusy(String port) {
        ChargeHandler portHandler = this.chargeHandlers.get(port);
        portHandler.sendMessage(portHandler.obtainMessage(ChargeHandler.MSG_PORT_PARK_STATUS, PARK_STATUS.occupied));
    }

    @Override // com.xcharge.charger.device.api.PortStatusListener
    public void onParkIdle(String port) {
        ChargeHandler portHandler = this.chargeHandlers.get(port);
        portHandler.sendMessage(portHandler.obtainMessage(ChargeHandler.MSG_PORT_PARK_STATUS, PARK_STATUS.idle));
    }

    @Override // com.xcharge.charger.device.api.PortStatusListener
    public void onParkUnkow(String port) {
        ChargeHandler portHandler = this.chargeHandlers.get(port);
        portHandler.sendMessage(portHandler.obtainMessage(ChargeHandler.MSG_PORT_PARK_STATUS, PARK_STATUS.unknown));
    }

    @Override // com.xcharge.charger.device.api.PortStatusListener
    public void onRadarCalibration(String port, boolean isSuccess) {
        ChargeHandler portHandler = this.chargeHandlers.get(port);
        portHandler.sendMessage(portHandler.obtainMessage(ChargeHandler.MSG_PORT_RADAR_CALIBRATION, Boolean.valueOf(isSuccess)));
    }

    @Override // com.xcharge.charger.device.api.PortStatusListener
    public void onAuthValid(String port, PortStatus data) {
        ChargeHandler portHandler = this.chargeHandlers.get(port);
        portHandler.sendMessage(portHandler.obtainMessage(ChargeHandler.MSG_PORT_AUTH_VALID, data));
    }

    @Override // com.xcharge.charger.device.api.PortStatusListener
    public void onAuthInvalid(String port, PortStatus data) {
        ChargeHandler portHandler = this.chargeHandlers.get(port);
        portHandler.sendMessage(portHandler.obtainMessage(ChargeHandler.MSG_PORT_AUTH_INVALID, data));
    }
}
