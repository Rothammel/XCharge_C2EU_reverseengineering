package com.xcharge.charger.protocol.xmsz.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.CHARGE_USER_TYPE;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.charger.protocol.api.ProtocolServiceProxy;
import com.xcharge.charger.protocol.xmsz.handler.XMSZProtocolAgent;
import com.xcharge.charger.protocol.xmsz.router.XMSZDCAPGateway;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class XMSZCloudService extends Service {
    public static final int MSG_DCAP_CONFIRM = 65537;
    public static final int MSG_DCAP_INDICATE = 65538;
    public static final int MSG_UPDATE_QRCODE_REQUEST = 65552;
    private XMSZCloudServiceHandler handler = null;
    private XMSZCloudServiceMsgReceiver receiver = null;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class XMSZCloudServiceMsgReceiver extends BroadcastReceiver {
        private XMSZCloudServiceMsgReceiver() {
        }

        /* synthetic */ XMSZCloudServiceMsgReceiver(XMSZCloudService xMSZCloudService, XMSZCloudServiceMsgReceiver xMSZCloudServiceMsgReceiver) {
            this();
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(DCAPMessage.ACTION_DCAP_CONFIRM)) {
                String body = intent.getStringExtra("body");
                XMSZCloudService.this.handler.sendMessage(XMSZCloudService.this.handler.obtainMessage(65537, body));
            } else if (action.equals(DCAPMessage.ACTION_DCAP_INDICATE)) {
                String body2 = intent.getStringExtra("body");
                XMSZCloudService.this.handler.sendMessage(XMSZCloudService.this.handler.obtainMessage(65538, body2));
            } else if (action.equals(ProtocolServiceProxy.ACTION_REQUEST_UPDATE_QRCODE_EVENT)) {
                String port = intent.getStringExtra(ContentDB.ChargeTable.PORT);
                XMSZCloudService.this.handler.sendMessage(XMSZCloudService.this.handler.obtainMessage(65552, port));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class XMSZCloudServiceHandler extends Handler {
        private XMSZCloudServiceHandler() {
        }

        /* synthetic */ XMSZCloudServiceHandler(XMSZCloudService xMSZCloudService, XMSZCloudServiceHandler xMSZCloudServiceHandler) {
            this();
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 65537:
                    XMSZCloudService.this.handleDCAPConfirm((String) msg.obj);
                    break;
                case 65538:
                    XMSZCloudService.this.handleDCAPIndicate((String) msg.obj);
                    break;
                case 65552:
                    String port = (String) msg.obj;
                    Log.i("XMSZCloudServiceHandler.handleMessage", "receive qrcode update request for port: " + port);
                    XMSZCloudService.this.handleQrcodeUpdateRequest(port);
                    break;
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
        Log.d("XMSZCloudService.onCreate", "onCreate !!!");
        super.onCreate();
        init();
        ProtocolServiceProxy.getInstance().sendProtocolServiceEvent("created");
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override // android.app.Service
    public void onDestroy() {
        destroy();
        ProtocolServiceProxy.getInstance().sendProtocolServiceEvent("destroyed");
        super.onDestroy();
    }

    private void init() {
        Context context = getApplicationContext();
        XMSZProtocolAgent.getInstance().init(context);
        XMSZDCAPGateway.getInstance().init(context);
        this.handler = new XMSZCloudServiceHandler(this, null);
        this.receiver = new XMSZCloudServiceMsgReceiver(this, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(DCAPMessage.ACTION_DCAP_INDICATE);
        filter.addAction(DCAPMessage.ACTION_DCAP_CONFIRM);
        filter.addAction(ProtocolServiceProxy.ACTION_REQUEST_UPDATE_QRCODE_EVENT);
        LocalBroadcastManager.getInstance(context).registerReceiver(this.receiver, filter);
        ProtocolServiceProxy.getInstance().init(context);
        XMSZProtocolAgent.getInstance().initConnection();
    }

    private void destroy() {
        ProtocolServiceProxy.getInstance().destroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(this.receiver);
        this.handler.removeMessages(65537);
        this.handler.removeMessages(65538);
        this.handler.removeMessages(65552);
        XMSZDCAPGateway.getInstance().destroy();
        XMSZProtocolAgent.getInstance().destroy();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleDCAPIndicate(String msg) {
        try {
            DCAPMessage indicate = new DCAPMessage().fromJson(msg);
            if (!checkDCAPMessageRoute(indicate)) {
                Log.w("XMSZCloudService.handleDCAPIndicate", "route is not to me, ignore it !!!");
            } else {
                CAPMessage cap = new CAPMessage().fromJson(JsonBean.ObjectToJson(indicate.getData()));
                indicate.setData(cap);
                String op = cap.getOp();
                if ("auth".equals(op) || CAPMessage.DIRECTIVE_INIT_ACK.equals(op) || "fin".equals(op)) {
                    XMSZDCAPGateway.getInstance().sendMessage(XMSZDCAPGateway.getInstance().obtainMessage(77825, indicate));
                } else {
                    Log.w("XMSZCloudService.handleDCAPIndicate", "need not to handle this DCAP indicate: " + op);
                }
            }
        } catch (Exception e) {
            Log.w("XMSZCloudService.handleDCAPIndicate", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleDCAPConfirm(String msg) {
        try {
            DCAPMessage confirm = new DCAPMessage().fromJson(msg);
            if (!checkDCAPMessageRoute(confirm)) {
                Log.w("XMSZCloudService.handleDCAPConfirm", "route is not to me, ignore it !!!");
            } else {
                CAPMessage cap = new CAPMessage().fromJson(JsonBean.ObjectToJson(confirm.getData()));
                confirm.setData(cap);
                String op = cap.getOp();
                if ("ack".equals(op) || CAPMessage.DIRECTIVE_NACK.equals(op)) {
                    XMSZDCAPGateway.getInstance().sendMessage(XMSZDCAPGateway.getInstance().obtainMessage(77826, confirm));
                } else {
                    Log.w("XMSZCloudService.handleDCAPConfirm", "need not to handle this DCAP confirm: " + op);
                }
            }
        } catch (Exception e) {
            Log.w("XMSZCloudService.handleDCAPConfirm", Log.getStackTraceString(e));
        }
    }

    private boolean checkDCAPMessageRoute(DCAPMessage msg) {
        String from = msg.getFrom();
        String to = msg.getTo();
        return from.startsWith("device:") && (to.startsWith(new StringBuilder("user:").append(CHARGE_USER_TYPE.xmsz).toString()) || to.startsWith(new StringBuilder("server:").append(CHARGE_PLATFORM.xmsz.getPlatform()).toString()));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleQrcodeUpdateRequest(String localPort) {
        XMSZProtocolAgent.getInstance().handleUpdateQrcodeRequest(localPort);
    }
}
