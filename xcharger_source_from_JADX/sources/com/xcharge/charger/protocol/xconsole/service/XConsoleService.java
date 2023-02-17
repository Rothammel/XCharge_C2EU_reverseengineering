package com.xcharge.charger.protocol.xconsole.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.p000v4.content.LocalBroadcastManager;
import android.util.Log;
import com.xcharge.charger.protocol.api.ProtocolServiceProxy;

public class XConsoleService extends Service {
    private XConsoleHandler handler = null;
    private XConsoleMsgReceiver receiver = null;

    private class XConsoleMsgReceiver extends BroadcastReceiver {
        private XConsoleMsgReceiver() {
        }

        /* synthetic */ XConsoleMsgReceiver(XConsoleService xConsoleService, XConsoleMsgReceiver xConsoleMsgReceiver) {
            this();
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
        }
    }

    private class XConsoleHandler extends Handler {
        private XConsoleHandler() {
        }

        /* synthetic */ XConsoleHandler(XConsoleService xConsoleService, XConsoleHandler xConsoleHandler) {
            this();
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            super.handleMessage(msg);
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        Log.i("XConsoleService", "onCreate");
        init();
        ProtocolServiceProxy.getInstance().sendProtocolServiceEvent("created");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        Log.i("XConsoleService", "onDestroy");
        destroy();
        ProtocolServiceProxy.getInstance().sendProtocolServiceEvent("destroyed");
        super.onDestroy();
    }

    private void init() {
        Context context = getApplicationContext();
        this.handler = new XConsoleHandler(this, (XConsoleHandler) null);
        this.receiver = new XConsoleMsgReceiver(this, (XConsoleMsgReceiver) null);
        LocalBroadcastManager.getInstance(context).registerReceiver(this.receiver, new IntentFilter());
        ProtocolServiceProxy.getInstance().init(context);
    }

    private void destroy() {
        ProtocolServiceProxy.getInstance().destroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(this.receiver);
    }
}
