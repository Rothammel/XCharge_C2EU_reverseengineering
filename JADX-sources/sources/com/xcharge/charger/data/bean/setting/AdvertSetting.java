package com.xcharge.charger.data.bean.setting;

import com.xcharge.charger.data.bean.ContentItem;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;
import java.util.HashMap;

/* loaded from: classes.dex */
public class AdvertSetting extends JsonBean<AdvertSetting> {
    private HashMap<String, ArrayList<ContentItem>> content = null;

    public HashMap<String, ArrayList<ContentItem>> getContent() {
        return this.content;
    }

    public void setContent(HashMap<String, ArrayList<ContentItem>> content) {
        this.content = content;
    }
}
