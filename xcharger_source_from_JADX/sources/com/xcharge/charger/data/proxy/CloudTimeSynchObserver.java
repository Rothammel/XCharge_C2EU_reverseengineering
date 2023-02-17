package com.xcharge.charger.data.proxy;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class CloudTimeSynchObserver extends ContentObserver {
    public static final int MSG_CLOUD_TIME_SYNCHED = 143361;
    private Context context = null;
    private Handler handler = null;

    public CloudTimeSynchObserver(Context context2, Handler handler2) {
        super(handler2);
        this.context = context2;
        this.handler = handler2;
    }

    public boolean deliverSelfNotifications() {
        Log.d("CloudTimeSynchObserver", "deliverSelfNotifications");
        return super.deliverSelfNotifications();
    }

    public void onChange(boolean selfChange, Uri uri) {
        Log.d("CloudTimeSynchObserver.onChange", "selfChange: " + selfChange + ", uri: " + uri.toString());
        super.onChange(selfChange, uri);
        this.handler.obtainMessage(MSG_CLOUD_TIME_SYNCHED, uri).sendToTarget();
    }

    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
    }
}
