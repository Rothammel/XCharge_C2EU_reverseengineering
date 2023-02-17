package com.xcharge.charger.data.proxy;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

/* loaded from: classes.dex */
public class PortStatusObserver extends ContentObserver {
    public static final int MSG_PORT_STATUS_CHANGE = 139265;
    private Context context;
    private Handler handler;
    private String port;

    public PortStatusObserver(Context context, String port, Handler handler) {
        super(handler);
        this.context = null;
        this.handler = null;
        this.port = null;
        this.context = context;
        this.handler = handler;
        this.port = port;
    }

    @Override // android.database.ContentObserver
    public boolean deliverSelfNotifications() {
        Log.d("PortStatusObserver", "deliverSelfNotifications");
        return super.deliverSelfNotifications();
    }

    @Override // android.database.ContentObserver
    public void onChange(boolean selfChange, Uri uri) {
        Log.d("PortStatusObserver.onChange", "selfChange: " + selfChange + ", uri: " + uri.toString());
        super.onChange(selfChange, uri);
        this.handler.obtainMessage(MSG_PORT_STATUS_CHANGE, uri).sendToTarget();
    }

    @Override // android.database.ContentObserver
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
    }
}