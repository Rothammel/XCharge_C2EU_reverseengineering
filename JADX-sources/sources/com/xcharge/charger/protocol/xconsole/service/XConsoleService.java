package com.xcharge.charger.protocol.xconsole.service;

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
import com.xcharge.charger.protocol.api.ProtocolServiceProxy;

/* loaded from: classes.dex */
public class XConsoleService extends Service {
    private XConsoleHandler handler = null;
    private XConsoleMsgReceiver receiver = null;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class XConsoleMsgReceiver extends BroadcastReceiver {
        private XConsoleMsgReceiver() {
        }

        /* synthetic */ XConsoleMsgReceiver(XConsoleService xConsoleService, XConsoleMsgReceiver xConsoleMsgReceiver) {
            this();
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            intent.getAction();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class XConsoleHandler extends Handler {
        private XConsoleHandler() {
        }

        /* synthetic */ XConsoleHandler(XConsoleService xConsoleService, XConsoleHandler xConsoleHandler) {
            this();
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
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
        Log.i("XConsoleService", "onCreate");
        init();
        ProtocolServiceProxy.getInstance().sendProtocolServiceEvent("created");
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override // android.app.Service
    public void onDestroy() {
        Log.i("XConsoleService", "onDestroy");
        destroy();
        ProtocolServiceProxy.getInstance().sendProtocolServiceEvent("destroyed");
        super.onDestroy();
    }

    private void init() {
        Context context = getApplicationContext();
        this.handler = new XConsoleHandler(this, null);
        this.receiver = new XConsoleMsgReceiver(this, null);
        IntentFilter filter = new IntentFilter();
        LocalBroadcastManager.getInstance(context).registerReceiver(this.receiver, filter);
        ProtocolServiceProxy.getInstance().init(context);
    }

    private void destroy() {
        ProtocolServiceProxy.getInstance().destroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(this.receiver);
    }
}
