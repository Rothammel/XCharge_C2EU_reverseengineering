package com.xcharge.charger.core.service;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.NackDirective;
import com.xcharge.charger.core.api.bean.cap.CAPDirectiveOption;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.core.api.bean.cap.SetDirective;
import com.xcharge.charger.core.controller.ChargeController;
import com.xcharge.charger.core.controller.OSSController;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.device.adpter.DeviceProxy;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class DCAPDispatcher {
    private static DCAPDispatcher instance = null;
    private Context context = null;

    public static synchronized DCAPDispatcher getInstance() {
        DCAPDispatcher dCAPDispatcher;
        synchronized (DCAPDispatcher.class) {
            if (instance == null) {
                instance = new DCAPDispatcher();
            }
            dCAPDispatcher = instance;
        }
        return dCAPDispatcher;
    }

    public void init(Context context) {
        this.context = context;
        OSSController.getInstance().init(this.context);
        ChargeController.getInstance().init(this.context);
        DeviceProxy.getInstance().attachPortStatusListener(ChargeController.getInstance());
    }

    public void destroy() {
        DeviceProxy.getInstance().dettachPortStatusListener(ChargeController.getInstance());
        ChargeController.getInstance().destroy();
        OSSController.getInstance().destroy();
    }

    public void dispatchRequest(Handler handler, String msg) {
        DCAPMessage request = null;
        String type = null;
        try {
            request = new DCAPMessage().fromJson(msg);
            if (checkDCAPMessageRoute(request)) {
                type = request.getType();
                if ("cap".equals(type)) {
                    CAPMessage cap = new CAPMessage().fromJson(JsonBean.ObjectToJson(request.getData()));
                    request.setData(cap);
                    String op = cap.getOp();
                    if ("auth".equals(op) || "init".equals(op) || "fin".equals(op) || "start".equals(op) || "stop".equals(op) || CAPMessage.DIRECTIVE_CONDITION.equals(op) || "event".equals(op)) {
                        ChargeController.getInstance().handleRequest(request, DCAPProxy.getInstance().createCAPConfirmByRequest(request));
                    } else if ("query".equals(op) || "set".equals(op) || "upgrade".equals(op)) {
                        OSSController.getInstance().handleRequest(request, DCAPProxy.getInstance().createCAPConfirmByRequest(request));
                    } else {
                        Log.w("DCAPDispatcher.dispatchRequest", "invalid DCAP CAP request op: " + op);
                    }
                } else {
                    DCAPMessage.TYPE_DDCP.equals(type);
                }
            }
        } catch (Exception e) {
            Log.e("DCAPDispatcher.dispatchRequest", "failed to dispatch request msg: " + msg + ", exception: " + Log.getStackTraceString(e));
            NackDirective nack = new NackDirective();
            nack.setError(10000);
            nack.setMsg(e.toString());
            DCAPProxy.getInstance().sendConfirm(DCAPProxy.getInstance().createCAPConfirmByRequest(request), type, CAPMessage.DIRECTIVE_NACK, nack);
        }
    }

    public void dispatchResponse(Handler handler, String msg) {
        try {
            DCAPMessage response = new DCAPMessage().fromJson(msg);
            if (checkDCAPMessageRoute(response)) {
                String type = response.getType();
                if ("cap".equals(type)) {
                    CAPMessage cap = new CAPMessage().fromJson(JsonBean.ObjectToJson(response.getData()));
                    response.setData(cap);
                    String op = cap.getOp();
                    if ("ack".equals(op) || CAPMessage.DIRECTIVE_NACK.equals(op)) {
                        CAPDirectiveOption opt = cap.getOpt();
                        String peerOp = opt.getOp();
                        if (CAPMessage.DIRECTIVE_INIT_ACK.equals(peerOp) || "fin".equals(peerOp) || "event".equals(peerOp) || "auth".equals(peerOp)) {
                            ChargeController.getInstance().handleResponse(response);
                        } else if (CAPMessage.DIRECTIVE_ACTIVE.equals(peerOp) || "alert".equals(peerOp) || "log".equals(peerOp) || "report".equals(peerOp) || "query".equals(peerOp) || "set".equals(peerOp)) {
                            OSSController.getInstance().handleResponse(response);
                        } else {
                            Log.w("DCAPDispatcher.dispatchResponse", "invalid DCAP CAP response peer op: " + peerOp);
                        }
                    }
                } else {
                    DCAPMessage.TYPE_DDCP.equals(type);
                }
            }
        } catch (Exception e) {
            Log.e("DCAPDispatcher.dispatchResponse", "failed to dispatch response msg: " + msg + ", exception: " + Log.getStackTraceString(e));
        }
    }

    private boolean checkDCAPMessageRoute(DCAPMessage msg) {
        String from = msg.getFrom();
        String to = msg.getTo();
        if ((from.startsWith("server") || from.startsWith("user") || from.startsWith(SetDirective.SET_ID_DEVICE) || from.startsWith("guest")) && (to.startsWith("broadcast") || to.startsWith(SetDirective.SET_ID_DEVICE))) {
            String[] toSplit = to.split(":");
            if (SetDirective.SET_ID_DEVICE.equals(toSplit[0]) && toSplit.length == 2) {
                String sn = HardwareStatusCacheProvider.getInstance().getSn();
                if (!toSplit[1].equals("sn/" + sn)) {
                    Log.w("DCAPDispatcher.checkDCAPMessageRoute", "not to me, msg: " + msg.toJson());
                    return false;
                }
            }
            return true;
        }
        Log.w("DCAPDispatcher.checkDCAPMessageRoute", "illegal from or to in msg: " + msg.toJson());
        return false;
    }
}