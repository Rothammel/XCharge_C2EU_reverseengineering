package com.xcharge.charger.protocol.monitor.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.protocol.api.ProtocolServiceProxy;
import com.xcharge.charger.protocol.monitor.handler.MonitorProtocolAgent;
import com.xcharge.charger.protocol.monitor.router.MonitorDCAPGateway;
import com.xcharge.charger.protocol.monitor.util.LogUtils;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class MonitorCloudService extends Service {
    public static final int MSG_DCAP_CONFIRM = 65537;
    public static final int MSG_DCAP_INDICATE = 65538;
    public static final int MSG_UPDATE_QRCODE_REQUEST = 65552;
    private MonitorCloudServiceHandler handler = null;
    private MonitorCloudServiceMsgReceiver receiver = null;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class MonitorCloudServiceMsgReceiver extends BroadcastReceiver {
        private MonitorCloudServiceMsgReceiver() {
        }

        /* synthetic */ MonitorCloudServiceMsgReceiver(MonitorCloudService monitorCloudService, MonitorCloudServiceMsgReceiver monitorCloudServiceMsgReceiver) {
            this();
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(DCAPMessage.ACTION_DCAP_CONFIRM)) {
                String body = intent.getStringExtra("body");
                MonitorCloudService.this.handler.sendMessage(MonitorCloudService.this.handler.obtainMessage(65537, body));
            } else if (action.equals(DCAPMessage.ACTION_DCAP_INDICATE)) {
                String body2 = intent.getStringExtra("body");
                MonitorCloudService.this.handler.sendMessage(MonitorCloudService.this.handler.obtainMessage(65538, body2));
            } else if (action.equals(ProtocolServiceProxy.ACTION_REQUEST_UPDATE_QRCODE_EVENT)) {
                String port = intent.getStringExtra(ContentDB.ChargeTable.PORT);
                MonitorCloudService.this.handler.sendMessage(MonitorCloudService.this.handler.obtainMessage(65552, port));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class MonitorCloudServiceHandler extends Handler {
        private MonitorCloudServiceHandler() {
        }

        /* synthetic */ MonitorCloudServiceHandler(MonitorCloudService monitorCloudService, MonitorCloudServiceHandler monitorCloudServiceHandler) {
            this();
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case 65537:
                        MonitorCloudService.this.handleDCAPConfirm((String) msg.obj);
                        break;
                    case 65538:
                        MonitorCloudService.this.handleDCAPIndicate((String) msg.obj);
                        break;
                    case 65552:
                        String port = (String) msg.obj;
                        Log.i("MonitorCloudService.handleMessage", "receive qrcode update request for port: " + port);
                        MonitorCloudService.this.handleQrcodeUpdateRequest(port);
                        break;
                }
            } catch (Exception e) {
                Log.e("MonitorCloudService.handleMessage", "except: " + Log.getStackTraceString(e));
            }
            super.handleMessage(msg);
        }
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public void onCreate() {
        LogUtils.log("MonitorCloudService.onCreate", "onCreate !!!");
        super.onCreate();
        init();
        ProtocolServiceProxy.getInstance().sendProtocolServiceEvent(ProtocolServiceProxy.PROTOCOL_MONITOR_SERIVCE_EVENT_CREATED);
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override // android.app.Service
    public void onDestroy() {
        destroy();
        ProtocolServiceProxy.getInstance().sendProtocolServiceEvent(ProtocolServiceProxy.PROTOCOL_MONITOR_SERIVCE_EVENT_DESTROYED);
        super.onDestroy();
    }

    private void init() {
        Context context = getApplicationContext();
        MonitorProtocolAgent.getInstance().init(context);
        MonitorDCAPGateway.getInstance().init(context);
        this.handler = new MonitorCloudServiceHandler(this, null);
        this.receiver = new MonitorCloudServiceMsgReceiver(this, null);
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

    /* JADX INFO: Access modifiers changed from: private */
    public void handleDCAPIndicate(String msg) {
        try {
            DCAPMessage indicate = new DCAPMessage().fromJson(msg);
            if (!checkDCAPMessageRoute(indicate)) {
                LogUtils.log("MonitorCloudService.handleDCAPIndicate", "route is not to me, ignore it !!!");
            } else {
                CAPMessage cap = new CAPMessage().fromJson(JsonBean.ObjectToJson(indicate.getData()));
                indicate.setData(cap);
                String op = cap.getOp();
                if ("auth".equals(op) || CAPMessage.DIRECTIVE_INIT_ACK.equals(op) || "fin".equals(op) || "event".equals(op)) {
                    MonitorDCAPGateway.getInstance().sendMessage(MonitorDCAPGateway.getInstance().obtainMessage(77825, indicate));
                } else {
                    LogUtils.log("MonitorCloudService.handleDCAPIndicate", "need not to handle this DCAP indicate: " + op);
                }
            }
        } catch (Exception e) {
            LogUtils.log("MonitorCloudService.handleDCAPIndicate", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleDCAPConfirm(String msg) {
        try {
            DCAPMessage confirm = new DCAPMessage().fromJson(msg);
            if (!checkDCAPMessageRoute(confirm)) {
                LogUtils.log("MonitorCloudService.handleDCAPConfirm", "route is not to me, ignore it !!!");
            } else {
                CAPMessage cap = new CAPMessage().fromJson(JsonBean.ObjectToJson(confirm.getData()));
                confirm.setData(cap);
                String op = cap.getOp();
                if ("ack".equals(op) || CAPMessage.DIRECTIVE_NACK.equals(op)) {
                    MonitorDCAPGateway.getInstance().sendMessage(MonitorDCAPGateway.getInstance().obtainMessage(77826, confirm));
                } else {
                    LogUtils.log("MonitorCloudService.handleDCAPConfirm", "need not to handle this DCAP confirm: " + op);
                }
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
        return from.startsWith("device:") && (to.startsWith(new StringBuilder("user:").append(platform).toString()) || to.startsWith(new StringBuilder("server:").append(platform.getPlatform()).toString()));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleQrcodeUpdateRequest(String localPort) {
    }
}
