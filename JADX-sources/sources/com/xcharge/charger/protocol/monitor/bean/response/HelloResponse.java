package com.xcharge.charger.protocol.monitor.bean.response;

import com.xcharge.charger.protocol.monitor.bean.ddap.DDAPIOTAccess;
import com.xcharge.charger.protocol.monitor.bean.ddap.DDAPSubnode;
import com.xcharge.common.bean.JsonBean;
import java.util.List;

/* loaded from: classes.dex */
public class HelloResponse extends JsonBean<HelloResponse> {
    private DDAPIOTAccess iot_access = null;
    private List<DDAPSubnode> subset = null;

    public DDAPIOTAccess getIot_access() {
        return this.iot_access;
    }

    public void setIot_access(DDAPIOTAccess iot_access) {
        this.iot_access = iot_access;
    }

    public List<DDAPSubnode> getSubset() {
        return this.subset;
    }

    public void setSubset(List<DDAPSubnode> subset) {
        this.subset = subset;
    }
}
