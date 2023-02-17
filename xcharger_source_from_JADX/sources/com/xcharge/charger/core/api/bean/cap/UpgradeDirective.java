package com.xcharge.charger.core.api.bean.cap;

import android.util.Log;
import com.xcharge.charger.data.bean.UpgradeData;

public class UpgradeDirective extends UpgradeData {
    public UpgradeDirective fromJson(String json) {
        try {
            return (UpgradeDirective) getGsonBuilder().create().fromJson(json, Class.forName(getClass().getName()));
        } catch (Exception e) {
            Log.e("UpgradeDirective.fromJson", "json: " + json + ", exception: " + Log.getStackTraceString(e));
            return null;
        }
    }

    public String toJson() {
        return getGsonBuilder().create().toJson((Object) this);
    }
}
