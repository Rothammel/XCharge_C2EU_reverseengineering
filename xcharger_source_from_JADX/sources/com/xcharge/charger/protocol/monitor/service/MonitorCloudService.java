package com.xcharge.charger.protocol.monitor.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.p004db.ContentDB;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.protocol.api.ProtocolServiceProxy;
import com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent;
import com.xcharge.charger.protocol.monitor.router.MonitorDCAPGateway;
import com.xcharge.charger.protocol.monitor.util.LogUtils;
import com.xcharge.common.bean.JsonBean;

public class MonitorCloudService extends Service {
    public static final int MSG_DCAP_CONFIRM = 65537;
    public static final int MSG_DCAP_INDICATE = 65538;
    public static final int MSG_UPDATE_QRCODE_REQUEST = 65552;
    /* access modifiers changed from: private */
    public MonitorCloudServiceHandler handler = null;
    private MonitorCloudServiceMsgReceiver receiver = null;

    private class MonitorCloudServiceMsgReceiver extends BroadcastReceiver {
        private MonitorCloudServiceMsgReceiver() {
        }

        /* synthetic */ MonitorCloudServiceMsgReceiver(MonitorCloudService monitorCloudService, MonitorCloudServiceMsgReceiver monitorCloudServiceMsgReceiver) {
            this();
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(DCAPMessage.ACTION_DCAP_CONFIRM)) {
                MonitorCloudService.this.handler.sendMessage(MonitorCloudService.this.handler.obtainMessage(65537, intent.getStringExtra("body")));
            } else if (action.equals(DCAPMessage.ACTION_DCAP_INDICATE)) {
                MonitorCloudService.this.handler.sendMessage(MonitorCloudService.this.handler.obtainMessage(65538, intent.getStringExtra("body")));
            } else if (action.equals(ProtocolServiceProxy.ACTION_REQUEST_UPDATE_QRCODE_EVENT)) {
                MonitorCloudService.this.handler.sendMessage(MonitorCloudService.this.handler.obtainMessage(65552, intent.getStringExtra(ContentDB.ChargeTable.PORT)));
            }
        }
    }

    private class MonitorCloudServiceHandler extends Handler {
        private MonitorCloudServiceHandler() {
        }

        /* synthetic */ MonitorCloudServiceHandler(MonitorCloudService monitorCloudService, MonitorCloudServiceHandler monitorCloudServiceHandler) {
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
                    case 65538: goto L_0x002d;
                    case 65552: goto L_0x0037;
                    default: goto L_0x0005;
                }
            L_0x0005:
                super.handleMessage(r6)
                return
            L_0x0009:
                com.xcharge.charger.protocol.monitor.service.MonitorCloudService r3 = com.xcharge.charger.protocol.monitor.service.MonitorCloudService.this     // Catch:{ Exception -> 0x0013 }
                java.lang.Object r2 = r6.obj     // Catch:{ Exception -> 0x0013 }
                java.lang.String r2 = (java.lang.String) r2     // Catch:{ Exception -> 0x0013 }
                r3.handleDCAPConfirm(r2)     // Catch:{ Exception -> 0x0013 }
                goto L_0x0005
            L_0x0013:
                r0 = move-exception
                java.lang.String r2 = "MonitorCloudService.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder
                java.lang.String r4 = "except: "
                r3.<init>(r4)
                java.lang.String r4 = android.util.Log.getStackTraceString(r0)
                java.lang.StringBuilder r3 = r3.append(r4)
                java.lang.String r3 = r3.toString()
                android.util.Log.e(r2, r3)
                goto L_0x0005
            L_0x002d:
                com.xcharge.charger.protocol.monitor.service.MonitorCloudService r3 = com.xcharge.charger.protocol.monitor.service.MonitorCloudService.this     // Catch:{ Exception -> 0x0013 }
                java.lang.Object r2 = r6.obj     // Catch:{ Exception -> 0x0013 }
                java.lang.String r2 = (java.lang.String) r2     // Catch:{ Exception -> 0x0013 }
                r3.handleDCAPIndicate(r2)     // Catch:{ Exception -> 0x0013 }
                goto L_0x0005
            L_0x0037:
                java.lang.Object r1 = r6.obj     // Catch:{ Exception -> 0x0013 }
                java.lang.String r1 = (java.lang.String) r1     // Catch:{ Exception -> 0x0013 }
                java.lang.String r2 = "MonitorCloudService.handleMessage"
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0013 }
                java.lang.String r4 = "receive qrcode update request for port: "
                r3.<init>(r4)     // Catch:{ Exception -> 0x0013 }
                java.lang.StringBuilder r3 = r3.append(r1)     // Catch:{ Exception -> 0x0013 }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0013 }
                android.util.Log.i(r2, r3)     // Catch:{ Exception -> 0x0013 }
                com.xcharge.charger.protocol.monitor.service.MonitorCloudService r2 = com.xcharge.charger.protocol.monitor.service.MonitorCloudService.this     // Catch:{ Exception -> 0x0013 }
                r2.handleQrcodeUpdateRequest(r1)     // Catch:{ Exception -> 0x0013 }
                goto L_0x0005
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.protocol.monitor.service.MonitorCloudService.MonitorCloudServiceHandler.handleMessage(android.os.Message):void");
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        LogUtils.log("MonitorCloudService.onCreate", "onCreate !!!");
        super.onCreate();
        init();
        ProtocolServiceProxy.getInstance().sendProtocolServiceEvent(ProtocolServiceProxy.PROTOCOL_MONITOR_SERIVCE_EVENT_CREATED);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        destroy();
        ProtocolServiceProxy.getInstance().sendProtocolServiceEvent(ProtocolServiceProxy.PROTOCOL_MONITOR_SERIVCE_EVENT_DESTROYED);
        super.onDestroy();
    }

    private void init() {
        Context context = getApplicationContext();
        MonitorProtocolAgent.getInstance().init(context);
        MonitorDCAPGateway.getInstance().init(context);
        this.handler = new MonitorCloudServiceHandler(this, (MonitorCloudServiceHandler) null);
        this.receiver = new MonitorCloudServiceMsgReceiver(this, (MonitorCloudServiceMsgReceiver) null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(DCAPMessage.ACTION_DCAP_INDICATE);
        filter.addAction(DCAPMessage.ACTION_DCAP_CONFIRM);
        filter.addAction(ProtocolServiceProxy.ACTION_REQUEST_UPDATE_QRCODE_EVENT);
        registerReceiver(this.receiver, filter);
        ProtocolServiceProxy.getInstance().init(context);
        MonitorProtocolAgent.getInstance().initServerTimeSync();
    }

    private void destroy() {
        ProtocolServiceProxy.getInstance().destroy();
        unregisterReceiver(this.receiver);
        this.handler.removeMessages(65537);
        this.handler.removeMessages(65538);
        this.handler.removeMessages(65552);
        MonitorDCAPGateway.getInstance().destroy();
        MonitorProtocolAgent.getInstance().destroy();
    }

    /* access modifiers changed from: private */
    public void handleDCAPIndicate(String msg) {
        try {
            DCAPMessage indicate = (DCAPMessage) new DCAPMessage().fromJson(msg);
            if (!checkDCAPMessageRoute(indicate)) {
                LogUtils.log("MonitorCloudService.handleDCAPIndicate", "route is not to me, ignore it !!!");
                return;
            }
            CAPMessage cap = (CAPMessage) new CAPMessage().fromJson(JsonBean.ObjectToJson(indicate.getData()));
            indicate.setData(cap);
            String op = cap.getOp();
            if ("auth".equals(op) || CAPMessage.DIRECTIVE_INIT_ACK.equals(op) || "fin".equals(op) || "event".equals(op)) {
                MonitorDCAPGateway.getInstance().sendMessage(MonitorDCAPGateway.getInstance().obtainMessage(77825, indicate));
            } else {
                LogUtils.log("MonitorCloudService.handleDCAPIndicate", "need not to handle this DCAP indicate: " + op);
            }
        } catch (Exception e) {
            LogUtils.log("MonitorCloudService.handleDCAPIndicate", Log.getStackTraceString(e));
        }
    }

    /* access modifiers changed from: private */
    public void handleDCAPConfirm(String msg) {
        try {
            DCAPMessage confirm = (DCAPMessage) new DCAPMessage().fromJson(msg);
            if (!checkDCAPMessageRoute(confirm)) {
                LogUtils.log("MonitorCloudService.handleDCAPConfirm", "route is not to me, ignore it !!!");
                return;
            }
            CAPMessage cap = (CAPMessage) new CAPMessage().fromJson(JsonBean.ObjectToJson(confirm.getData()));
            confirm.setData(cap);
            String op = cap.getOp();
            if ("ack".equals(op) || CAPMessage.DIRECTIVE_NACK.equals(op)) {
                MonitorDCAPGateway.getInstance().sendMessage(MonitorDCAPGateway.getInstance().obtainMessage(77826, confirm));
            } else {
                LogUtils.log("MonitorCloudService.handleDCAPConfirm", "need not to handle this DCAP confirm: " + op);
            }
        } catch (Exception e) {
            LogUtils.log("MonitorCloudService.handleDCAPConfirm", Log.getStackTraceString(e));
        }
    }

    private boolean checkDCAPMessageRoute(DCAPMessage msg) {
        String from = msg.getFrom();
        String to = msg.getTo();
        if (to.startsWith("user:nfc.")) {
            return true;
        }
        CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
        if (!from.startsWith("device:") || (!to.startsWith("user:" + platform) && !to.startsWith("server:" + platform.getPlatform()))) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void handleQrcodeUpdateRequest(String localPort) {
    }
}
