package com.xcharge.charger.data.proxy;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

/* loaded from: classes.dex */
public class CloudTimeSynchObserver extends ContentObserver {
    public static final int MSG_CLOUD_TIME_SYNCHED = 143361;
    private Context context;
    private Handler handler;

    public CloudTimeSynchObserver(Context context, Handler handler) {
        super(handler);
        this.context = null;
        this.handler = null;
        this.context = context;
        this.handler = handler;
    }

    @Override // android.database.ContentObserver
    public boolean deliverSelfNotifications() {
        Log.d("CloudTimeSynchObserver", "deliverSelfNotifications");
        return super.deliverSelfNotifications();
    }

    @Override // android.database.ContentObserver
    public void onChange(boolean selfChange, Uri uri) {
        Log.d("CloudTimeSynchObserver.onChange", "selfChange: " + selfChange + ", uri: " + uri.toString());
        super.onChange(selfChange, uri);
        this.handler.obtainMessage(MSG_CLOUD_TIME_SYNCHED, uri).sendToTarget();
    }

    @Override // android.database.ContentObserver
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
    }
}