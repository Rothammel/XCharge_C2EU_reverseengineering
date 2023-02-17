package com.xcharge.charger.device.api;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/* loaded from: classes.dex */
public class DeviceServiceProxy {
    public static final String ACTION_DEVICE_SERIVCE_EVENT = "com.xcharge.charger.device.api.ACTION_DEVICE_SERIVCE_EVENT";
    public static final String DEVICE_SERIVCE_EVENT_CREATED = "created";
    public static final String DEVICE_SERIVCE_EVENT_DESTROYED = "destroyed";
    private static DeviceServiceProxy instance = null;
    protected Context context = null;

    public static DeviceServiceProxy getInstance() {
        if (instance == null) {
            instance = new DeviceServiceProxy();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
    }

    public void destroy() {
    }

    public void sendDeviceServiceEvent(String event) {
        Intent intent = new Intent(ACTION_DEVICE_SERIVCE_EVENT);
        intent.putExtra("event", event);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }
}
