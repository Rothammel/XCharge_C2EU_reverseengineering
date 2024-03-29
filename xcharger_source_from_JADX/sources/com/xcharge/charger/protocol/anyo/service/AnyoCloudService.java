package com.xcharge.charger.protocol.anyo.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.support.p000v4.content.LocalBroadcastManager;
import android.util.Log;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.CHARGE_USER_TYPE;
import com.xcharge.charger.data.p004db.ContentDB;
import com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent;
import com.xcharge.charger.protocol.anyo.router.AnyoDCAPGateway;
import com.xcharge.charger.protocol.api.ProtocolServiceProxy;
import com.xcharge.common.bean.JsonBean;

public class AnyoCloudService extends Service {
    public static final int MSG_DCAP_CONFIRM = 65537;
    public static final int MSG_DCAP_INDICATE = 65538;
    public static final int MSG_UPDATE_QRCODE_REQUEST = 65552;
    /* access modifiers changed from: private */
    public AnyoCloudServiceHandler handler = null;
    private AnyoCloudServiceMsgReceiver receiver = null;

    private class AnyoCloudServiceMsgReceiver extends BroadcastReceiver {
        private AnyoCloudServiceMsgReceiver() {
        }

        /* synthetic */ AnyoCloudServiceMsgReceiver(AnyoCloudService anyoCloudService, AnyoCloudServiceMsgReceiver anyoCloudServiceMsgReceiver) {
            this();
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(DCAPMessage.ACTION_DCAP_CONFIRM)) {
                AnyoCloudService.this.handler.sendMessage(AnyoCloudService.this.handler.obtainMessage(65537, intent.getStringExtra("body")));
            } else if (action.equals(DCAPMessage.ACTION_DCAP_INDICATE)) {
                AnyoCloudService.this.handler.sendMessage(AnyoCloudService.this.handler.obtainMessage(65538, intent.getStringExtra("body")));
            } else if (action.equals(ProtocolServiceProxy.ACTION_REQUEST_UPDATE_QRCODE_EVENT)) {
                AnyoCloudService.this.handler.sendMessage(AnyoCloudService.this.handler.obtainMessage(65552, intent.getStringExtra(ContentDB.ChargeTable.PORT)));
            }
        }
    }

    private class AnyoCloudServiceHandler extends Handler {
        private AnyoCloudServiceHandler() {
        }

        /* synthetic */ AnyoCloudServiceHandler(AnyoCloudService anyoCloudService, AnyoCloudServiceHandler anyoCloudServiceHandler) {
            this();
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(android.os.Message r6) {
            /*
                r5 = this;
                int r2 = r6.what     // Catch:{ Exception -> 0x0013 }
                switch(r2) {
                    case 65537: goto L_0x0009;
                    case 65538: goto L_0x0043;
                    case 65552: goto L_0x004d;
                    default: goto L_0x0005;
                }
            L_0x0005:
                super.handleMessage(r6)
                return
            L_0x0009:
                com.xcharge.charger.protocol.anyo.service.AnyoCloudService r3 = com.xcharge.charger.protocol.anyo.service.AnyoCloudService.this     // Catch:{ Exception -> 0x0013 }
                java.lang.Object r2 = r6.obj     // Catch:{ Exception -> 0x0013 }
                java.lang.String r2 = (java.lang.String) r2     // Catch:{ Exception -> 0x0013 }
                r3.handleDCAPConfirm(r2)     // Catch:{ Exception -> 0x0013 }
                goto L_0x0005
            L_0x0013:
                r0 = move-exception
                java.lang.String r2 = "AnyoCloudService.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder
                java.lang.String r4 = "except: "
                r3.<init>(r4)
                java.lang.String r4 = android.util.Log.getStackTraceString(r0)
                java.lang.StringBuilder r3 = r3.append(r4)
                java.lang.String r3 = r3.toString()
                android.util.Log.e(r2, r3)
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                java.lang.String r3 = "AnyoCloudService handleMessage exception: "
                r2.<init>(r3)
                java.lang.String r3 = android.util.Log.getStackTraceString(r0)
                java.lang.StringBuilder r2 = r2.append(r3)
                java.lang.String r2 = r2.toString()
                com.xcharge.common.utils.LogUtils.syslog(r2)
                goto L_0x0005
            L_0x0043:
                com.xcharge.charger.protocol.anyo.service.AnyoCloudService r3 = com.xcharge.charger.protocol.anyo.service.AnyoCloudService.this     // Catch:{ Exception -> 0x0013 }
                java.lang.Object r2 = r6.obj     // Catch:{ Exception -> 0x0013 }
                java.lang.String r2 = (java.lang.String) r2     // Catch:{ Exception -> 0x0013 }
                r3.handleDCAPIndicate(r2)     // Catch:{ Exception -> 0x0013 }
                goto L_0x0005
            L_0x004d:
                java.lang.Object r1 = r6.obj     // Catch:{ Exception -> 0x0013 }
                java.lang.String r1 = (java.lang.String) r1     // Catch:{ Exception -> 0x0013 }
                java.lang.String r2 = "AnyoCloudService.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0013 }
                java.lang.String r4 = "receive qrcode update request for port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0013 }
                java.lang.StringBuilder r3 = r3.append(r1)     // Catch:{ Exception -> 0x0013 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0013 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0013 }
                com.xcharge.charger.protocol.anyo.service.AnyoCloudService r2 = com.xcharge.charger.protocol.anyo.service.AnyoCloudService.this     // Catch:{ Exception -> 0x0013 }
                r2.handleQrcodeUpdateRequest(r1)     // Catch:{ Exception -> 0x0013 }
                goto L_0x0005
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.protocol.anyo.service.AnyoCloudService.AnyoCloudServiceHandler.handleMessage(android.os.Message):void");
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        init();
        ProtocolServiceProxy.getInstance().sendProtocolServiceEvent("created");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        destroy();
        ProtocolServiceProxy.getInstance().sendProtocolServiceEvent("destroyed");
        super.onDestroy();
    }

    private void init() {
        Context context = getApplicationContext();
        AnyoProtocolAgent.getInstance().init(context);
        AnyoDCAPGateway.getInstance().init(context);
        this.handler = new AnyoCloudServiceHandler(this, (AnyoCloudServiceHandler) null);
        this.receiver = new AnyoCloudServiceMsgReceiver(this, (AnyoCloudServiceMsgReceiver) null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(DCAPMessage.ACTION_DCAP_INDICATE);
        filter.addAction(DCAPMessage.ACTION_DCAP_CONFIRM);
        filter.addAction(ProtocolServiceProxy.ACTION_REQUEST_UPDATE_QRCODE_EVENT);
        LocalBroadcastManager.getInstance(context).registerReceiver(this.receiver, filter);
        ProtocolServiceProxy.getInstance().init(context);
        AnyoProtocolAgent.getInstance().initConnection();
    }

    private void destroy() {
        ProtocolServiceProxy.getInstance().destroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(this.receiver);
        this.handler.removeMessages(65537);
        this.handler.removeMessages(65538);
        this.handler.removeMessages(65552);
        AnyoDCAPGateway.getInstance().destroy();
        AnyoProtocolAgent.getInstance().destroy();
    }

    /* access modifiers changed from: private */
    public void handleDCAPIndicate(String msg) {
        try {
            DCAPMessage indicate = (DCAPMessage) new DCAPMessage().fromJson(msg);
            if (!checkDCAPMessageRoute(indicate)) {
                Log.w("AnyoCloudService.handleDCAPIndicate", "route is not to me, ignore it !!!");
                return;
            }
            CAPMessage cap = (CAPMessage) new CAPMessage().fromJson(JsonBean.ObjectToJson(indicate.getData()));
            indicate.setData(cap);
            String op = cap.getOp();
            if ("auth".equals(op) || CAPMessage.DIRECTIVE_INIT_ACK.equals(op) || "fin".equals(op)) {
                AnyoDCAPGateway.getInstance().sendMessage(AnyoDCAPGateway.getInstance().obtainMessage(77825, indicate));
            } else {
                Log.w("AnyoCloudService.handleDCAPIndicate", "need not to handle this DCAP indicate: " + op);
            }
        } catch (Exception e) {
            Log.w("AnyoCloudService.handleDCAPIndicate", Log.getStackTraceString(e));
        }
    }

    /* access modifiers changed from: private */
    public void handleDCAPConfirm(String msg) {
        try {
            DCAPMessage confirm = (DCAPMessage) new DCAPMessage().fromJson(msg);
            if (!checkDCAPMessageRoute(confirm)) {
                Log.w("AnyoCloudService.handleDCAPConfirm", "route is not to me, ignore it !!!");
                return;
            }
            CAPMessage cap = (CAPMessage) new CAPMessage().fromJson(JsonBean.ObjectToJson(confirm.getData()));
            confirm.setData(cap);
            String op = cap.getOp();
            if ("ack".equals(op) || CAPMessage.DIRECTIVE_NACK.equals(op)) {
                AnyoDCAPGateway.getInstance().sendMessage(AnyoDCAPGateway.getInstance().obtainMessage(77826, confirm));
            } else {
                Log.w("AnyoCloudService.handleDCAPConfirm", "need not to handle this DCAP confirm: " + op);
            }
        } catch (Exception e) {
            Log.w("AnyoCloudService.handleDCAPConfirm", Log.getStackTraceString(e));
        }
    }

    private boolean checkDCAPMessageRoute(DCAPMessage msg) {
        String from = msg.getFrom();
        String to = msg.getTo();
        if (!from.startsWith("device:") || (!to.startsWith("user:" + CHARGE_USER_TYPE.anyo) && !to.startsWith("server:" + CHARGE_PLATFORM.anyo.getPlatform()))) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void handleQrcodeUpdateRequest(String localPort) {
        AnyoProtocolAgent.getInstance().handleUpdateQrcodeRequest(localPort);
    }
}
