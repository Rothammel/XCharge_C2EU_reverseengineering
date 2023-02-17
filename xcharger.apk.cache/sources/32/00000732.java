package com.xcharge.charger.protocol.monitor.router;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.cap.CAPDirectiveOption;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.core.api.bean.cap.EventDirective;
import com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent;
import com.xcharge.charger.protocol.monitor.util.LogUtils;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.HandlerTimer;

/* loaded from: classes.dex */
public class MonitorDCAPGateway {
    public static final int INTERVAL_ADAPTER_MAINTAIN = 1000;
    public static final int MSG_DCAP_CONFIRM = 77826;
    public static final int MSG_DCAP_INDICATE = 77825;
    public static final int MSG_FAILED_REQUEST_SERVER = 77829;
    public static final int MSG_MONITOR_REQUEST = 77827;
    public static final int MSG_MONITOR_RESPONSE = 77828;
    public static final int MSG_TIMER_MAINTAIN_ADAPTER = 77830;
    private static MonitorDCAPGateway instance = null;
    private HandlerThread thread = null;
    private MsgHandler handler = null;
    private HandlerTimer handlerTimer = null;
    private Context context = null;

    public static MonitorDCAPGateway getInstance() {
        if (instance == null) {
            instance = new MonitorDCAPGateway();
        }
        return instance;
    }

    /* loaded from: classes.dex */
    private class MsgHandler extends Handler {
        public MsgHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case 77825:
                        DCAPMessage dcapIndicate = (DCAPMessage) msg.obj;
                        LogUtils.log("MonitorDCAPGateway.handleMessage", "receive DCAP indicate: " + dcapIndicate.toJson());
                        MonitorDCAPGateway.this.handleDCAPIndicate(dcapIndicate);
                        break;
                }
            } catch (Exception e) {
                Log.e("MonitorDCAPGateway.handleMessage", "except: " + Log.getStackTraceString(e));
            }
            super.handleMessage(msg);
        }
    }

    public void init(Context context) {
        this.context = context;
        this.thread = new HandlerThread("MonitorDCAPGateway", 10);
        this.thread.start();
        this.handler = new MsgHandler(this.thread.getLooper());
        this.handlerTimer = new HandlerTimer(this.handler);
        this.handlerTimer.init(context);
        this.handlerTimer.startTimer(1000L, 77830, null);
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

    /* JADX INFO: Access modifiers changed from: private */
    public void handleDCAPIndicate(DCAPMessage indicate) {
        CAPMessage cap = (CAPMessage) indicate.getData();
        String op = cap.getOp();
        if ("event".equals(op)) {
            EventDirective event = new EventDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
            cap.setData(event);
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
            long delayStartTime = event.getDelay_start();
            MonitorProtocolAgent.getInstance().handleDelayStartedRequest(chargeId, delayStartTime);
        } else if (EventDirective.EVENT_DEALY_WAIT_START.equals(eventId)) {
            MonitorProtocolAgent.getInstance().handleDelayWaitStartedRequest(chargeId);
        }
    }
}