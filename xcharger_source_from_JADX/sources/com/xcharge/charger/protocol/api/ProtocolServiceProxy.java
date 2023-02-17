package com.xcharge.charger.protocol.api;

import android.content.Context;
import android.content.Intent;
import android.support.p000v4.content.LocalBroadcastManager;
import com.xcharge.charger.data.p004db.ContentDB;

public class ProtocolServiceProxy {
    public static final String ACTION_PROTOCOL_SERIVCE_EVENT = "com.xcharge.charger.protocol.api.ACTION_PROTOCOL_SERIVCE_EVENT";
    public static final String ACTION_REQUEST_UPDATE_QRCODE_EVENT = "com.xcharge.charger.protocol.api.ACTION_REQUEST_UPDATE_QRCODE_EVENT";
    public static final String PROTOCOL_MONITOR_SERIVCE_EVENT_CREATED = "monitor_created";
    public static final String PROTOCOL_MONITOR_SERIVCE_EVENT_DESTROYED = "monitor_destroyed";
    public static final String PROTOCOL_SERIVCE_EVENT_CREATED = "created";
    public static final String PROTOCOL_SERIVCE_EVENT_DESTROYED = "destroyed";
    private static ProtocolServiceProxy instance = null;
    protected Context context = null;

    public static ProtocolServiceProxy getInstance() {
        if (instance == null) {
            instance = new ProtocolServiceProxy();
        }
        return instance;
    }

    public void init(Context context2) {
        this.context = context2;
    }

    public void destroy() {
    }

    public void sendProtocolServiceEvent(String event) {
        Intent intent = new Intent(ACTION_PROTOCOL_SERIVCE_EVENT);
        intent.putExtra("event", event);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    public void sentUpdateQrcodeRequestEvent(String port) {
        Intent intent = new Intent(ACTION_REQUEST_UPDATE_QRCODE_EVENT);
        intent.putExtra(ContentDB.ChargeTable.PORT, port);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }
}
