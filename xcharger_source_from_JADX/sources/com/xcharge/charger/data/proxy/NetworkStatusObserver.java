package com.xcharge.charger.data.proxy;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class NetworkStatusObserver extends ContentObserver {
    public static final int MSG_NETWORK_STATUS_CHANGE = 135169;
    private Context context = null;
    private Handler handler = null;

    public NetworkStatusObserver(Context context2, Handler handler2) {
        super(handler2);
        this.context = context2;
        this.handler = handler2;
    }

    public boolean deliverSelfNotifications() {
        Log.d("NetworkStatusObserver", "deliverSelfNotifications");
        return super.deliverSelfNotifications();
    }

    public void onChange(boolean selfChange, Uri uri) {
        Log.d("NetworkStatusObserver.onChange", "selfChange: " + selfChange + ", uri: " + uri.toString());
        super.onChange(selfChange, uri);
        this.handler.obtainMessage(135169, uri).sendToTarget();
    }

    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
    }
}
