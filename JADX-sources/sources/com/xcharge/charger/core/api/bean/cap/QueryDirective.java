package com.xcharge.charger.core.api.bean.cap;

import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

/* loaded from: classes.dex */
public class QueryDirective extends JsonBean<QueryDirective> {
    public static final String QUERY_ID_CARD_STATUS = "card.status";
    public static final String QUERY_ID_DEVICE_VERIFICATION = "device.verification";
    public static final String QUERY_ID_PORT_PLUGIN_UPDATE = "plugin.update.port";
    private HashMap<String, Object> params = null;

    public HashMap<String, Object> getParams() {
        return this.params;
    }

    public void setParams(HashMap<String, Object> params) {
        this.params = params;
    }
}
