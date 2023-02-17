package com.xcharge.charger.core.api.bean.cap;

import android.util.Log;
import com.xcharge.charger.data.bean.UpgradeData;

/* loaded from: classes.dex */
public class UpgradeDirective extends UpgradeData {
    @Override // com.xcharge.common.bean.JsonBean
    /* renamed from: fromJson */
    public UpgradeData fromJson2(String json) {
        try {
            return (UpgradeDirective) getGsonBuilder().create().fromJson(json, (Class<Object>) Class.forName(getClass().getName()));
        } catch (Exception e) {
            Log.e("UpgradeDirective.fromJson", "json: " + json + ", exception: " + Log.getStackTraceString(e));
            return null;
        }
    }

    @Override // com.xcharge.common.bean.JsonBean
    public String toJson() {
        return getGsonBuilder().create().toJson(this);
    }
}
