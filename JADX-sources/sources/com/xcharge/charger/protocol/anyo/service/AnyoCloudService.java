package com.xcharge.charger.protocol.anyo.service;

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
import com.xcharge.charger.protocol.anyo.handler.AnyoProtocolAgent;
import com.xcharge.charger.protocol.anyo.router.AnyoDCAPGateway;
import com.xcharge.charger.protocol.api.ProtocolServiceProxy;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.LogUtils;

/* loaded from: classes.dex */
public class AnyoCloudService extends Service {
    public static final int MSG_DCAP_CONFIRM = 65537;
    public static final int MSG_DCAP_INDICATE = 65538;
    public static final int MSG_UPDATE_QRCODE_REQUEST = 65552;
    private AnyoCloudServiceHandler handler = null;
    private AnyoCloudServiceMsgReceiver receiver = null;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class AnyoCloudServiceMsgReceiver extends BroadcastReceiver {
        private AnyoCloudServiceMsgReceiver() {
        }

        /* synthetic */ AnyoCloudServiceMsgReceiver(AnyoCloudService anyoCloudService, AnyoCloudServiceMsgReceiver anyoCloudServiceMsgReceiver) {
            this();
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(DCAPMessage.ACTION_DCAP_CONFIRM)) {
                String body = intent.getStringExtra("body");
                AnyoCloudService.this.handler.sendMessage(AnyoCloudService.this.handler.obtainMessage(65537, body));
            } else if (action.equals(DCAPMessage.ACTION_DCAP_INDICATE)) {
                String body2 = intent.getStringExtra("body");
                AnyoCloudService.this.handler.sendMessage(AnyoCloudService.this.handler.obtainMessage(65538, body2));
            } else if (action.equals(ProtocolServiceProxy.ACTION_REQUEST_UPDATE_QRCODE_EVENT)) {
                String port = intent.getStringExtra(ContentDB.ChargeTable.PORT);
                AnyoCloudService.this.handler.sendMessage(AnyoCloudService.this.handler.obtainMessage(65552, port));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class AnyoCloudServiceHandler extends Handler {
        private AnyoCloudServiceHandler() {
        }

        /* synthetic */ AnyoCloudServiceHandler(AnyoCloudService anyoCloudService, AnyoCloudServiceHandler anyoCloudServiceHandler) {
            this();
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case 65537:
                        AnyoCloudService.this.handleDCAPConfirm((String) msg.obj);
                        break;
                    case 65538:
                        AnyoCloudService.this.handleDCAPIndicate((String) msg.obj);
                        break;
                    case 65552:
                        String port = (String) msg.obj;
                        Log.i("AnyoCloudService.handleMessage", "receive qrcode update request for port: " + port);
                        AnyoCloudService.this.handleQrcodeUpdateRequest(port);
                        break;
                }
            } catch (Exception e) {
                Log.e("AnyoCloudService.handleMessage", "except: " + Log.getStackTraceString(e));
                LogUtils.syslog("AnyoCloudService handleMessage exception: " + Log.getStackTraceString(e));
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
        AnyoProtocolAgent.getInstance().init(context);
        AnyoDCAPGateway.getInstance().init(context);
        this.handler = new AnyoCloudServiceHandler(this, null);
        this.receiver = new AnyoCloudServiceMsgReceiver(this, null);
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

    /* JADX INFO: Access modifiers changed from: private */
    public void handleDCAPIndicate(String msg) {
        try {
            DCAPMessage indicate = new DCAPMessage().fromJson(msg);
            if (!checkDCAPMessageRoute(indicate)) {
                Log.w("AnyoCloudService.handleDCAPIndicate", "route is not to me, ignore it !!!");
            } else {
                CAPMessage cap = new CAPMessage().fromJson(JsonBean.ObjectToJson(indicate.getData()));
                indicate.setData(cap);
                String op = cap.getOp();
                if ("auth".equals(op) || CAPMessage.DIRECTIVE_INIT_ACK.equals(op) || "fin".equals(op)) {
                    AnyoDCAPGateway.getInstance().sendMessage(AnyoDCAPGateway.getInstance().obtainMessage(77825, indicate));
                } else {
                    Log.w("AnyoCloudService.handleDCAPIndicate", "need not to handle this DCAP indicate: " + op);
                }
            }
        } catch (Exception e) {
            Log.w("AnyoCloudService.handleDCAPIndicate", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleDCAPConfirm(String msg) {
        try {
            DCAPMessage confirm = new DCAPMessage().fromJson(msg);
            if (!checkDCAPMessageRoute(confirm)) {
                Log.w("AnyoCloudService.handleDCAPConfirm", "route is not to me, ignore it !!!");
            } else {
                CAPMessage cap = new CAPMessage().fromJson(JsonBean.ObjectToJson(confirm.getData()));
                confirm.setData(cap);
                String op = cap.getOp();
                if ("ack".equals(op) || CAPMessage.DIRECTIVE_NACK.equals(op)) {
                    AnyoDCAPGateway.getInstance().sendMessage(AnyoDCAPGateway.getInstance().obtainMessage(77826, confirm));
                } else {
                    Log.w("AnyoCloudService.handleDCAPConfirm", "need not to handle this DCAP confirm: " + op);
                }
            }
        } catch (Exception e) {
            Log.w("AnyoCloudService.handleDCAPConfirm", Log.getStackTraceString(e));
        }
    }

    private boolean checkDCAPMessageRoute(DCAPMessage msg) {
        String from = msg.getFrom();
        String to = msg.getTo();
        return from.startsWith("device:") && (to.startsWith(new StringBuilder("user:").append(CHARGE_USER_TYPE.anyo).toString()) || to.startsWith(new StringBuilder("server:").append(CHARGE_PLATFORM.anyo.getPlatform()).toString()));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleQrcodeUpdateRequest(String localPort) {
        AnyoProtocolAgent.getInstance().handleUpdateQrcodeRequest(localPort);
    }
}
