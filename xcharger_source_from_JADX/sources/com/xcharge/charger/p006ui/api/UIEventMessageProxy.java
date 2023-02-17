package com.xcharge.charger.p006ui.api;

import android.content.Context;
import android.content.Intent;
import android.support.p000v4.content.LocalBroadcastManager;
import android.util.Log;
import com.xcharge.charger.p006ui.api.bean.UIEventMessage;
import java.util.HashMap;

/* renamed from: com.xcharge.charger.ui.api.UIEventMessageProxy */
public class UIEventMessageProxy {
    private static UIEventMessageProxy insatnce = null;
    private Context context = null;

    public static UIEventMessageProxy getInstance() {
        if (insatnce == null) {
            insatnce = new UIEventMessageProxy();
        }
        return insatnce;
    }

    public void init(Context context2) {
        this.context = context2;
    }

    public void destroy() {
    }

    public boolean sendEvent(UIEventMessage event) {
        try {
            Intent intent = new Intent(UIEventMessage.ACTION_UI_EVENT);
            intent.putExtra("body", event.toJson());
            LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
            return true;
        } catch (Exception e) {
            Log.e("UIEventMessageProxy.sendEvent", Log.getStackTraceString(e));
            return false;
        }
    }

    public boolean sendEvent(String activity, String type, String subtype, String name, String status, HashMap data) {
        UIEventMessage event = new UIEventMessage();
        event.setActivity(activity);
        event.setType(type);
        event.setSubType(subtype);
        event.setName(name);
        event.setStatus(status);
        event.setData(data);
        return sendEvent(event);
    }
}
