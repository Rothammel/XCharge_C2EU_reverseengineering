package com.xcharge.charger.core.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.p000v4.content.LocalBroadcastManager;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.device.adpter.DeviceProxy;

public class DCAPService extends Service {
    public static final int MSG_DCAP_REQUEST_PRIMITIVE = 4097;
    public static final int MSG_DCAP_RESPONSE_PRIMITIVE = 4098;
    private DCAPMessageReceiver dcapMessageReceiver = null;
    /* access modifiers changed from: private */
    public DCAPHandler handler = null;

    private class DCAPMessageReceiver extends BroadcastReceiver {
        private DCAPMessageReceiver() {
        }

        /* synthetic */ DCAPMessageReceiver(DCAPService dCAPService, DCAPMessageReceiver dCAPMessageReceiver) {
            this();
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DCAPMessage.ACTION_DCAP_REQUEST)) {
                String body = intent.getStringExtra("body");
                Message msg = new Message();
                msg.what = 4097;
                msg.obj = body;
                DCAPService.this.handler.sendMessage(msg);
            } else if (intent.getAction().equals(DCAPMessage.ACTION_DCAP_RESPONSE)) {
                String body2 = intent.getStringExtra("body");
                Message msg2 = new Message();
                msg2.what = 4098;
                msg2.obj = body2;
                DCAPService.this.handler.sendMessage(msg2);
            }
        }
    }

    private class DCAPHandler extends Handler {
        private DCAPHandler() {
        }

        /* synthetic */ DCAPHandler(DCAPService dCAPService, DCAPHandler dCAPHandler) {
            this();
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(android.os.Message r5) {
            /*
                r4 = this;
                int r1 = r5.what     // Catch:{ Exception -> 0x0015 }
                switch(r1) {
                    case 4097: goto L_0x0009;
                    case 4098: goto L_0x0045;
                    default: goto L_0x0005;
                }
            L_0x0005:
                super.handleMessage(r5)
                return
            L_0x0009:
                com.xcharge.charger.core.service.DCAPDispatcher r2 = com.xcharge.charger.core.service.DCAPDispatcher.getInstance()     // Catch:{ Exception -> 0x0015 }
                java.lang.Object r1 = r5.obj     // Catch:{ Exception -> 0x0015 }
                java.lang.String r1 = (java.lang.String) r1     // Catch:{ Exception -> 0x0015 }
                r2.dispatchRequest(r4, r1)     // Catch:{ Exception -> 0x0015 }
                goto L_0x0005
            L_0x0015:
                r0 = move-exception
                java.lang.String r1 = "DCAPService.handleMessage"
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                java.lang.String r3 = "except: "
                r2.<init>(r3)
                java.lang.String r3 = android.util.Log.getStackTraceString(r0)
                java.lang.StringBuilder r2 = r2.append(r3)
                java.lang.String r2 = r2.toString()
                android.util.Log.e(r1, r2)
                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                java.lang.String r2 = "DCAPService handleMessage exception: "
                r1.<init>(r2)
                java.lang.String r2 = android.util.Log.getStackTraceString(r0)
                java.lang.StringBuilder r1 = r1.append(r2)
                java.lang.String r1 = r1.toString()
                com.xcharge.common.utils.LogUtils.syslog(r1)
                goto L_0x0005
            L_0x0045:
                com.xcharge.charger.core.service.DCAPDispatcher r2 = com.xcharge.charger.core.service.DCAPDispatcher.getInstance()     // Catch:{ Exception -> 0x0015 }
                java.lang.Object r1 = r5.obj     // Catch:{ Exception -> 0x0015 }
                java.lang.String r1 = (java.lang.String) r1     // Catch:{ Exception -> 0x0015 }
                r2.dispatchResponse(r4, r1)     // Catch:{ Exception -> 0x0015 }
                goto L_0x0005
            */
            throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.core.service.DCAPService.DCAPHandler.handleMessage(android.os.Message):void");
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        init();
        DCAPProxy.getInstance().sendDCAPServiceEvent("created");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        destroy();
        DCAPProxy.getInstance().sendDCAPServiceEvent("destroyed");
        super.onDestroy();
    }

    private void init() {
        Context context = getApplicationContext();
        DeviceProxy.getInstance().init(context);
        DCAPDispatcher.getInstance().init(context);
        this.handler = new DCAPHandler(this, (DCAPHandler) null);
        this.dcapMessageReceiver = new DCAPMessageReceiver(this, (DCAPMessageReceiver) null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(DCAPMessage.ACTION_DCAP_REQUEST);
        filter.addAction(DCAPMessage.ACTION_DCAP_RESPONSE);
        LocalBroadcastManager.getInstance(context).registerReceiver(this.dcapMessageReceiver, filter);
        DCAPProxy.getInstance().init(context);
    }

    private void destroy() {
        DCAPProxy.getInstance().destroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(this.dcapMessageReceiver);
        this.handler.removeMessages(4097);
        this.handler.removeMessages(4098);
        DCAPDispatcher.getInstance().destroy();
        DeviceProxy.getInstance().destroy();
    }
}
