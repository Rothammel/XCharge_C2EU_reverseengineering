package com.xcharge.charger.protocol.monitor.router;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.cap.CAPDirectiveOption;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.core.api.bean.cap.EventDirective;
import com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.HandlerTimer;

public class MonitorDCAPGateway {
    public static final int INTERVAL_ADAPTER_MAINTAIN = 1000;
    public static final int MSG_DCAP_CONFIRM = 77826;
    public static final int MSG_DCAP_INDICATE = 77825;
    public static final int MSG_FAILED_REQUEST_SERVER = 77829;
    public static final int MSG_MONITOR_REQUEST = 77827;
    public static final int MSG_MONITOR_RESPONSE = 77828;
    public static final int MSG_TIMER_MAINTAIN_ADAPTER = 77830;
    private static MonitorDCAPGateway instance = null;
    private Context context = null;
    private MsgHandler handler = null;
    private HandlerTimer handlerTimer = null;
    private HandlerThread thread = null;

    public static MonitorDCAPGateway getInstance() {
        if (instance == null) {
            instance = new MonitorDCAPGateway();
        }
        return instance;
    }

    private class MsgHandler extends Handler {
        public MsgHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(android.os.Message r6) {
            /*
                r5 = this;
                int r2 = r6.what     // Catch:{ Exception -> 0x002b }
                switch(r2) {
                    case 77825: goto L_0x0009;
                    case 77826: goto L_0x0005;
                    case 77827: goto L_0x0005;
                    case 77828: goto L_0x0005;
                    case 77829: goto L_0x0005;
                    default: goto L_0x0005;
                }
            L_0x0005:
                super.handleMessage(r6)
                return
            L_0x0009:
                java.lang.Object r0 = r6.obj     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.core.api.bean.DCAPMessage r0 = (com.xcharge.charger.core.api.bean.DCAPMessage) r0     // Catch:{ Exception -> 0x002b }
                java.lang.String r2 = "MonitorDCAPGateway.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x002b }
                java.lang.String r4 = "receive DCAP indicate: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x002b }
                java.lang.String r4 = r0.toJson()     // Catch:{ Exception -> 0x002b }
                java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x002b }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.monitor.util.LogUtils.log(r2, r3)     // Catch:{ Exception -> 0x002b }
                com.xcharge.charger.protocol.monitor.router.MonitorDCAPGateway r2 = com.xcharge.charger.protocol.monitor.router.MonitorDCAPGateway.this     // Catch:{ Exception -> 0x002b }
                r2.handleDCAPIndicate(r0)     // Catch:{ Exception -> 0x002b }
                goto L_0x0005
            L_0x002b:
                r1 = move-exception
                java.lang.String r2 = "MonitorDCAPGateway.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder
                java.lang.String r4 = "except: "
                r3.<init>(r4)
                java.lang.String r4 = android.util.Log.getStackTraceString(r1)
                java.lang.StringBuilder r3 = r3.append(r4)
                java.lang.String r3 = r3.toString()
                android.util.Log.e(r2, r3)
                goto L_0x0005
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.protocol.monitor.router.MonitorDCAPGateway.MsgHandler.handleMessage(android.os.Message):void");
        }
    }

    public void init(Context context2) {
        this.context = context2;
        this.thread = new HandlerThread("MonitorDCAPGateway", 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(context2);
        this.handlerTimer.startTimer(1000, 77830, (Object) null);
    }

    public void destroy() {
        this.handlerTimer.destroy();
        this.handler.removeMessages(77826);
        this.handler.removeMessages(77825);
        this.handler.removeMessages(77827);
        this.handler.removeMessages(77828);
        this.handler.removeMessages(77829);
        this.handler.removeMessages(77830);
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

    /* access modifiers changed from: private */
    public void handleDCAPIndicate(DCAPMessage indicate) {
        CAPMessage cap = (CAPMessage) indicate.getData();
        if ("event".equals(cap.getOp())) {
            cap.setData((EventDirective) new EventDirective().fromJson(JsonBean.ObjectToJson(cap.getData())));
            handleEventIndicate(indicate);
        }
    }

    private void handleEventIndicate(DCAPMessage indicate) {
        CAPMessage cap = (CAPMessage) indicate.getData();
        EventDirective event = (EventDirective) cap.getData();
        CAPDirectiveOption opt = cap.getOpt();
        String chargeId = opt.getCharge_id();
        String eventId = opt.getEvent_id();
        if ("delay_start".equals(eventId)) {
            MonitorProtocolAgent.getInstance().handleDelayStartedRequest(chargeId, event.getDelay_start());
        } else if (EventDirective.EVENT_DEALY_WAIT_START.equals(eventId)) {
            MonitorProtocolAgent.getInstance().handleDelayWaitStartedRequest(chargeId);
        }
    }
}
