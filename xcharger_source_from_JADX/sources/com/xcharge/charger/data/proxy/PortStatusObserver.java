package com.xcharge.charger.data.proxy;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class PortStatusObserver extends ContentObserver {
    public static final int MSG_PORT_STATUS_CHANGE = 139265;
    private Context context = null;
    private Handler handler = null;
    private String port = null;

    public PortStatusObserver(Context context2, String port2, Handler handler2) {
        super(handler2);
        this.context = context2;
        this.handler = handler2;
        this.port = port2;
    }

    public boolean deliverSelfNotifications() {
        Log.d("PortStatusObserver", "deliverSelfNotifications");
        return super.deliverSelfNotifications();
    }

    public void onChange(boolean selfChange, Uri uri) {
        Log.d("PortStatusObserver.onChange", "selfChange: " + selfChange + ", uri: " + uri.toString());
        super.onChange(selfChange, uri);
        this.handler.obtainMessage(MSG_PORT_STATUS_CHANGE, uri).sendToTarget();
    }

    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
    }
}
