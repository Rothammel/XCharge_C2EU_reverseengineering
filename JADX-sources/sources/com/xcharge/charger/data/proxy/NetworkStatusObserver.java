package com.xcharge.charger.data.proxy;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

/* loaded from: classes.dex */
public class NetworkStatusObserver extends ContentObserver {
    public static final int MSG_NETWORK_STATUS_CHANGE = 135169;
    private Context context;
    private Handler handler;

    public NetworkStatusObserver(Context context, Handler handler) {
        super(handler);
        this.context = null;
        this.handler = null;
        this.context = context;
        this.handler = handler;
    }

    @Override // android.database.ContentObserver
    public boolean deliverSelfNotifications() {
        Log.d("NetworkStatusObserver", "deliverSelfNotifications");
        return super.deliverSelfNotifications();
    }

    @Override // android.database.ContentObserver
    public void onChange(boolean selfChange, Uri uri) {
        Log.d("NetworkStatusObserver.onChange", "selfChange: " + selfChange + ", uri: " + uri.toString());
        super.onChange(selfChange, uri);
        this.handler.obtainMessage(135169, uri).sendToTarget();
    }

    @Override // android.database.ContentObserver
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
    }
}
