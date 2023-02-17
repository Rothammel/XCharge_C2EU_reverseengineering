package com.xcharge.charger.ui.adapter.api;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.xcharge.charger.ui.adapter.type.UI_MODE;

/* loaded from: classes.dex */
public class UIServiceProxy {
    public static final String ACTION_UI_MODE_EVENT = "com.xcharge.charger.ui.api.ACTION_UI_MODE_EVENT";
    public static final String ACTION_UI_SERIVCE_EVENT = "com.xcharge.charger.ui.api.ACTION_UI_SERIVCE_EVENT";
    public static final String UI_SERIVCE_EVENT_CREATED = "Created";
    public static final String UI_SERIVCE_EVENT_DESTROYED = "Destroyed";
    public static final String UI_SERIVCE_EVENT_UPDATE_QRCODE = "UpdateQrcode";
    private static UIServiceProxy instance = null;
    protected Context context = null;

    public static UIServiceProxy getInstance() {
        if (instance == null) {
            instance = new UIServiceProxy();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
    }

    public void destroy() {
    }

    public void sendUIServiceEvent(String event) {
        if (this.context == null) {
            Log.e("UIServiceProxy.sendUIModeEvent", "UIServiceProxy is not inited !!!");
            return;
        }
        Intent intent = new Intent(ACTION_UI_SERIVCE_EVENT);
        intent.putExtra("event", event);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    public boolean sendUIModeEvent(UI_MODE mode, Bundle data) {
        if (this.context == null) {
            Log.e("UIServiceProxy.sendUIModeEvent", "UIServiceProxy is not inited !!!");
            return false;
        }
        try {
            Intent intent = new Intent(ACTION_UI_MODE_EVENT);
            intent.putExtra("mode", mode.getType());
            intent.putExtra("data", data);
            LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
            return true;
        } catch (Exception e) {
            Log.e("UIServiceProxy.sendUIModeEvent", Log.getStackTraceString(e));
            return false;
        }
    }
}
