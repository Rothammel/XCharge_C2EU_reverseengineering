package com.xcharge.charger.core.service;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.core.api.bean.cap.SetDirective;
import com.xcharge.charger.core.controller.ChargeController;
import com.xcharge.charger.core.controller.OSSController;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.device.adpter.DeviceProxy;
import com.xcharge.common.bean.JsonBean;

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

    public void init(Context context2) {
        this.context = context2;
        OSSController.getInstance().init(this.context);
        ChargeController.getInstance().init(this.context);
        DeviceProxy.getInstance().attachPortStatusListener(ChargeController.getInstance());
    }

    public void destroy() {
        DeviceProxy.getInstance().dettachPortStatusListener(ChargeController.getInstance());
        ChargeController.getInstance().destroy();
        OSSController.getInstance().destroy();
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v5, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: com.xcharge.charger.core.api.bean.DCAPMessage} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void dispatchRequest(android.os.Handler r11, java.lang.String r12) {
        /*
            r10 = this;
            r5 = 0
            r6 = 0
            com.xcharge.charger.core.api.bean.DCAPMessage r7 = new com.xcharge.charger.core.api.bean.DCAPMessage     // Catch:{ Exception -> 0x0084 }
            r7.<init>()     // Catch:{ Exception -> 0x0084 }
            java.lang.Object r7 = r7.fromJson(r12)     // Catch:{ Exception -> 0x0084 }
            r0 = r7
            com.xcharge.charger.core.api.bean.DCAPMessage r0 = (com.xcharge.charger.core.api.bean.DCAPMessage) r0     // Catch:{ Exception -> 0x0084 }
            r5 = r0
            boolean r7 = r10.checkDCAPMessageRoute(r5)     // Catch:{ Exception -> 0x0084 }
            if (r7 != 0) goto L_0x0016
        L_0x0015:
            return
        L_0x0016:
            java.lang.String r6 = r5.getType()     // Catch:{ Exception -> 0x0084 }
            java.lang.String r7 = "cap"
            boolean r7 = r7.equals(r6)     // Catch:{ Exception -> 0x0084 }
            if (r7 == 0) goto L_0x010a
            com.xcharge.charger.core.api.bean.cap.CAPMessage r7 = new com.xcharge.charger.core.api.bean.cap.CAPMessage     // Catch:{ Exception -> 0x0084 }
            r7.<init>()     // Catch:{ Exception -> 0x0084 }
            java.lang.Object r8 = r5.getData()     // Catch:{ Exception -> 0x0084 }
            java.lang.String r8 = com.xcharge.common.bean.JsonBean.ObjectToJson(r8)     // Catch:{ Exception -> 0x0084 }
            java.lang.Object r1 = r7.fromJson(r8)     // Catch:{ Exception -> 0x0084 }
            com.xcharge.charger.core.api.bean.cap.CAPMessage r1 = (com.xcharge.charger.core.api.bean.cap.CAPMessage) r1     // Catch:{ Exception -> 0x0084 }
            r5.setData(r1)     // Catch:{ Exception -> 0x0084 }
            java.lang.String r4 = r1.getOp()     // Catch:{ Exception -> 0x0084 }
            java.lang.String r7 = "auth"
            boolean r7 = r7.equals(r4)     // Catch:{ Exception -> 0x0084 }
            if (r7 != 0) goto L_0x0074
            java.lang.String r7 = "init"
            boolean r7 = r7.equals(r4)     // Catch:{ Exception -> 0x0084 }
            if (r7 != 0) goto L_0x0074
            java.lang.String r7 = "fin"
            boolean r7 = r7.equals(r4)     // Catch:{ Exception -> 0x0084 }
            if (r7 != 0) goto L_0x0074
            java.lang.String r7 = "start"
            boolean r7 = r7.equals(r4)     // Catch:{ Exception -> 0x0084 }
            if (r7 != 0) goto L_0x0074
            java.lang.String r7 = "stop"
            boolean r7 = r7.equals(r4)     // Catch:{ Exception -> 0x0084 }
            if (r7 != 0) goto L_0x0074
            java.lang.String r7 = "condition"
            boolean r7 = r7.equals(r4)     // Catch:{ Exception -> 0x0084 }
            if (r7 != 0) goto L_0x0074
            java.lang.String r7 = "event"
            boolean r7 = r7.equals(r4)     // Catch:{ Exception -> 0x0084 }
            if (r7 == 0) goto L_0x00cb
        L_0x0074:
            com.xcharge.charger.core.controller.ChargeController r7 = com.xcharge.charger.core.controller.ChargeController.getInstance()     // Catch:{ Exception -> 0x0084 }
            com.xcharge.charger.core.api.DCAPProxy r8 = com.xcharge.charger.core.api.DCAPProxy.getInstance()     // Catch:{ Exception -> 0x0084 }
            com.xcharge.charger.core.api.bean.DCAPMessage r8 = r8.createCAPConfirmByRequest(r5)     // Catch:{ Exception -> 0x0084 }
            r7.handleRequest(r5, r8)     // Catch:{ Exception -> 0x0084 }
            goto L_0x0015
        L_0x0084:
            r2 = move-exception
            java.lang.String r7 = "DCAPDispatcher.dispatchRequest"
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            java.lang.String r9 = "failed to dispatch request msg: "
            r8.<init>(r9)
            java.lang.StringBuilder r8 = r8.append(r12)
            java.lang.String r9 = ", exception: "
            java.lang.StringBuilder r8 = r8.append(r9)
            java.lang.String r9 = android.util.Log.getStackTraceString(r2)
            java.lang.StringBuilder r8 = r8.append(r9)
            java.lang.String r8 = r8.toString()
            android.util.Log.e(r7, r8)
            com.xcharge.charger.core.api.bean.NackDirective r3 = new com.xcharge.charger.core.api.bean.NackDirective
            r3.<init>()
            r7 = 10000(0x2710, float:1.4013E-41)
            r3.setError(r7)
            java.lang.String r7 = r2.toString()
            r3.setMsg(r7)
            com.xcharge.charger.core.api.DCAPProxy r7 = com.xcharge.charger.core.api.DCAPProxy.getInstance()
            com.xcharge.charger.core.api.DCAPProxy r8 = com.xcharge.charger.core.api.DCAPProxy.getInstance()
            com.xcharge.charger.core.api.bean.DCAPMessage r8 = r8.createCAPConfirmByRequest(r5)
            java.lang.String r9 = "nack"
            r7.sendConfirm(r8, r6, r9, r3)
            goto L_0x0015
        L_0x00cb:
            java.lang.String r7 = "query"
            boolean r7 = r7.equals(r4)     // Catch:{ Exception -> 0x0084 }
            if (r7 != 0) goto L_0x00e3
            java.lang.String r7 = "set"
            boolean r7 = r7.equals(r4)     // Catch:{ Exception -> 0x0084 }
            if (r7 != 0) goto L_0x00e3
            java.lang.String r7 = "upgrade"
            boolean r7 = r7.equals(r4)     // Catch:{ Exception -> 0x0084 }
            if (r7 == 0) goto L_0x00f4
        L_0x00e3:
            com.xcharge.charger.core.controller.OSSController r7 = com.xcharge.charger.core.controller.OSSController.getInstance()     // Catch:{ Exception -> 0x0084 }
            com.xcharge.charger.core.api.DCAPProxy r8 = com.xcharge.charger.core.api.DCAPProxy.getInstance()     // Catch:{ Exception -> 0x0084 }
            com.xcharge.charger.core.api.bean.DCAPMessage r8 = r8.createCAPConfirmByRequest(r5)     // Catch:{ Exception -> 0x0084 }
            r7.handleRequest(r5, r8)     // Catch:{ Exception -> 0x0084 }
            goto L_0x0015
        L_0x00f4:
            java.lang.String r7 = "DCAPDispatcher.dispatchRequest"
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0084 }
            java.lang.String r9 = "invalid DCAP CAP request op: "
            r8.<init>(r9)     // Catch:{ Exception -> 0x0084 }
            java.lang.StringBuilder r8 = r8.append(r4)     // Catch:{ Exception -> 0x0084 }
            java.lang.String r8 = r8.toString()     // Catch:{ Exception -> 0x0084 }
            android.util.Log.w(r7, r8)     // Catch:{ Exception -> 0x0084 }
            goto L_0x0015
        L_0x010a:
            java.lang.String r7 = "ddcp"
            r7.equals(r6)     // Catch:{ Exception -> 0x0084 }
            goto L_0x0015
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.core.service.DCAPDispatcher.dispatchRequest(android.os.Handler, java.lang.String):void");
    }

    public void dispatchResponse(Handler handler, String msg) {
        try {
            DCAPMessage response = (DCAPMessage) new DCAPMessage().fromJson(msg);
            if (checkDCAPMessageRoute(response)) {
                String type = response.getType();
                if ("cap".equals(type)) {
                    CAPMessage cap = (CAPMessage) new CAPMessage().fromJson(JsonBean.ObjectToJson(response.getData()));
                    response.setData(cap);
                    String op = cap.getOp();
                    if ("ack".equals(op) || CAPMessage.DIRECTIVE_NACK.equals(op)) {
                        String peerOp = cap.getOpt().getOp();
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
                if (!toSplit[1].equals("sn/" + HardwareStatusCacheProvider.getInstance().getSn())) {
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
