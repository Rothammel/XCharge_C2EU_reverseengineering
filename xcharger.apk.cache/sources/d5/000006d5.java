package com.xcharge.charger.protocol.family.xcloud.service;

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
import com.xcharge.charger.core.api.bean.cap.CAPDirectiveOption;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.charger.protocol.api.ProtocolServiceProxy;
import com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent;
import com.xcharge.charger.protocol.family.xcloud.router.XCloudDCAPGateway;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.LogUtils;

/* loaded from: classes.dex */
public class XCloudService extends Service {
    public static final int MSG_DCAP_CONFIRM = 65537;
    public static final int MSG_DCAP_INDICATE = 65538;
    public static final int MSG_UPDATE_QRCODE_REQUEST = 65552;
    private XCloudServiceHandler handler = null;
    private XCloudServiceMsgReceiver receiver = null;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class XCloudServiceMsgReceiver extends BroadcastReceiver {
        private XCloudServiceMsgReceiver() {
        }

        /* synthetic */ XCloudServiceMsgReceiver(XCloudService xCloudService, XCloudServiceMsgReceiver xCloudServiceMsgReceiver) {
            this();
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(DCAPMessage.ACTION_DCAP_CONFIRM)) {
                String body = intent.getStringExtra("body");
                XCloudService.this.handler.sendMessage(XCloudService.this.handler.obtainMessage(65537, body));
            } else if (action.equals(DCAPMessage.ACTION_DCAP_INDICATE)) {
                String body2 = intent.getStringExtra("body");
                XCloudService.this.handler.sendMessage(XCloudService.this.handler.obtainMessage(65538, body2));
            } else if (action.equals(ProtocolServiceProxy.ACTION_REQUEST_UPDATE_QRCODE_EVENT)) {
                String port = intent.getStringExtra(ContentDB.ChargeTable.PORT);
                XCloudService.this.handler.sendMessage(XCloudService.this.handler.obtainMessage(65552, port));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class XCloudServiceHandler extends Handler {
        private XCloudServiceHandler() {
        }

        /* synthetic */ XCloudServiceHandler(XCloudService xCloudService, XCloudServiceHandler xCloudServiceHandler) {
            this();
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case 65537:
                        XCloudService.this.handleDCAPConfirm((String) msg.obj);
                        break;
                    case 65538:
                        XCloudService.this.handleDCAPIndicate((String) msg.obj);
                        break;
                    case 65552:
                        String port = (String) msg.obj;
                        Log.i("XCloudService.handleMessage", "receive qrcode update request for port: " + port);
                        XCloudService.this.handleQrcodeUpdateRequest(port);
                        break;
                }
            } catch (Exception e) {
                Log.e("XCloudService.handleMessage", "except: " + Log.getStackTraceString(e));
                LogUtils.syslog("XCloudService handleMessage exception: " + Log.getStackTraceString(e));
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
        XCloudProtocolAgent.getInstance().init(context);
        XCloudDCAPGateway.getInstance().init(context);
        this.handler = new XCloudServiceHandler(this, null);
        this.receiver = new XCloudServiceMsgReceiver(this, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(DCAPMessage.ACTION_DCAP_INDICATE);
        filter.addAction(DCAPMessage.ACTION_DCAP_CONFIRM);
        filter.addAction(ProtocolServiceProxy.ACTION_REQUEST_UPDATE_QRCODE_EVENT);
        LocalBroadcastManager.getInstance(context).registerReceiver(this.receiver, filter);
        ProtocolServiceProxy.getInstance().init(context);
        XCloudProtocolAgent.getInstance().initServerTimeSync();
        XCloudProtocolAgent.getInstance().initConnection();
    }

    private void destroy() {
        ProtocolServiceProxy.getInstance().destroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(this.receiver);
        this.handler.removeMessages(65537);
        this.handler.removeMessages(65538);
        this.handler.removeMessages(65552);
        XCloudDCAPGateway.getInstance().destroy();
        XCloudProtocolAgent.getInstance().destroy();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleDCAPIndicate(String msg) {
        try {
            DCAPMessage indicate = new DCAPMessage().fromJson(msg);
            if (!checkDCAPMessageRoute(indicate)) {
                Log.w("XCloudService.handleDCAPIndicate", "route is not to me, ignore it !!!");
            } else {
                CAPMessage cap = new CAPMessage().fromJson(JsonBean.ObjectToJson(indicate.getData()));
                indicate.setData(cap);
                String op = cap.getOp();
                if ("auth".equals(op) || CAPMessage.DIRECTIVE_INIT_ACK.equals(op) || "fin".equals(op) || "event".equals(op)) {
                    if (indicate.getTo().startsWith("user:nfc." + NFC_CARD_TYPE.U3)) {
                        if ("fin".equals(op)) {
                            XCloudDCAPGateway.getInstance().sendMessage(XCloudDCAPGateway.getInstance().obtainMessage(77825, indicate));
                        } else {
                            Log.w("XCloudService.handleDCAPIndicate", "need not handle U3 indicate msg except for FIN indicate !!!");
                        }
                    } else {
                        XCloudDCAPGateway.getInstance().sendMessage(XCloudDCAPGateway.getInstance().obtainMessage(77825, indicate));
                    }
                } else {
                    Log.w("XCloudService.handleDCAPIndicate", "need not to handle this DCAP indicate: " + op);
                }
            }
        } catch (Exception e) {
            Log.w("XCloudService.handleDCAPIndicate", Log.getStackTraceString(e));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleDCAPConfirm(String msg) {
        try {
            DCAPMessage confirm = new DCAPMessage().fromJson(msg);
            if (!checkDCAPMessageRoute(confirm)) {
                Log.w("XCloudService.handleDCAPConfirm", "route is not to me, ignore it !!!");
            } else {
                CAPMessage cap = new CAPMessage().fromJson(JsonBean.ObjectToJson(confirm.getData()));
                confirm.setData(cap);
                String op = cap.getOp();
                if ("ack".equals(op) || CAPMessage.DIRECTIVE_NACK.equals(op)) {
                    if (confirm.getTo().startsWith("user:nfc." + NFC_CARD_TYPE.U3)) {
                        if ("ack".equals(op)) {
                            CAPDirectiveOption opt = cap.getOpt();
                            String peerOp = opt.getOp();
                            if ("fin".equals(peerOp)) {
                                XCloudDCAPGateway.getInstance().sendMessage(XCloudDCAPGateway.getInstance().obtainMessage(77826, confirm));
                            } else {
                                Log.w("XCloudService.handleDCAPConfirm", "need not handle U3 confirm msg except for ACK FIN confirm !!!");
                            }
                        } else {
                            Log.w("XCloudService.handleDCAPConfirm", "need not handle U3 confirm msg except for ACK FIN confirm !!!");
                        }
                    } else {
                        XCloudDCAPGateway.getInstance().sendMessage(XCloudDCAPGateway.getInstance().obtainMessage(77826, confirm));
                    }
                } else {
                    Log.w("XCloudService.handleDCAPConfirm", "need not to handle this DCAP confirm: " + op);
                }
            }
        } catch (Exception e) {
            Log.w("XCloudService.handleDCAPConfirm", Log.getStackTraceString(e));
        }
    }

    private boolean checkDCAPMessageRoute(DCAPMessage msg) {
        String from = msg.getFrom();
        String to = msg.getTo();
        if (to.startsWith("user:nfc." + NFC_CARD_TYPE.U3)) {
            return true;
        }
        return from.startsWith("device:") && !to.startsWith("user:nfc") && (!to.startsWith("server:") || to.startsWith(new StringBuilder("server:").append(CHARGE_PLATFORM.xcharge.getPlatform()).toString()));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleQrcodeUpdateRequest(String port) {
        XCloudProtocolAgent.getInstance().handleUpdateQrcodeRequest(port);
    }
}