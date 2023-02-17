package com.xcharge.charger.ui.adapter.type;

import com.xcharge.charger.ui.api.bean.UIEventMessage;

/* loaded from: classes.dex */
public enum UI_MODE {
    home(UIEventMessage.KEY_HOME),
    charge("charge"),
    upgrade("upgrade"),
    test("test"),
    advert("advert"),
    alert("alert"),
    challenge("challenge");
    
    private String type;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static UI_MODE[] valuesCustom() {
        UI_MODE[] valuesCustom = values();
        int length = valuesCustom.length;
        UI_MODE[] ui_modeArr = new UI_MODE[length];
        System.arraycopy(valuesCustom, 0, ui_modeArr, 0, length);
        return ui_modeArr;
    }

    UI_MODE(String type) {
        this.type = null;
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
