package com.xcharge.charger.p006ui.adapter.type;

import com.xcharge.charger.p006ui.api.bean.UIEventMessage;

/* renamed from: com.xcharge.charger.ui.adapter.type.UI_MODE */
public enum UI_MODE {
    home(UIEventMessage.KEY_HOME),
    charge("charge"),
    upgrade("upgrade"),
    test("test"),
    advert("advert"),
    alert("alert"),
    challenge("challenge");
    
    private String type;

    private UI_MODE(String type2) {
        this.type = null;
        this.type = type2;
    }

    public String getType() {
        return this.type;
    }
}
