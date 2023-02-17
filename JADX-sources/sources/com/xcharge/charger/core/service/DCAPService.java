package com.xcharge.charger.core.service;

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
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.device.adpter.DeviceProxy;
import com.xcharge.common.utils.LogUtils;

/* loaded from: classes.dex */
public class DCAPService extends Service {
    public static final int MSG_DCAP_REQUEST_PRIMITIVE = 4097;
    public static final int MSG_DCAP_RESPONSE_PRIMITIVE = 4098;
    private DCAPHandler handler = null;
    private DCAPMessageReceiver dcapMessageReceiver = null;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class DCAPMessageReceiver extends BroadcastReceiver {
        private DCAPMessageReceiver() {
        }

        /* synthetic */ DCAPMessageReceiver(DCAPService dCAPService, DCAPMessageReceiver dCAPMessageReceiver) {
            this();
        }

        @Override // android.content.BroadcastReceiver
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

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class DCAPHandler extends Handler {
        private DCAPHandler() {
        }

        /* synthetic */ DCAPHandler(DCAPService dCAPService, DCAPHandler dCAPHandler) {
            this();
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case 4097:
                        DCAPDispatcher.getInstance().dispatchRequest(this, (String) msg.obj);
                        break;
                    case 4098:
                        DCAPDispatcher.getInstance().dispatchResponse(this, (String) msg.obj);
                        break;
                }
            } catch (Exception e) {
                Log.e("DCAPService.handleMessage", "except: " + Log.getStackTraceString(e));
                LogUtils.syslog("DCAPService handleMessage exception: " + Log.getStackTraceString(e));
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
        DCAPProxy.getInstance().sendDCAPServiceEvent("created");
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override // android.app.Service
    public void onDestroy() {
        destroy();
        DCAPProxy.getInstance().sendDCAPServiceEvent("destroyed");
        super.onDestroy();
    }

    private void init() {
        Context context = getApplicationContext();
        DeviceProxy.getInstance().init(context);
        DCAPDispatcher.getInstance().init(context);
        this.handler = new DCAPHandler(this, null);
        this.dcapMessageReceiver = new DCAPMessageReceiver(this, null);
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
