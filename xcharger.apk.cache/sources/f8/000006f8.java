package com.xcharge.charger.protocol.monitor.bean;

import com.xcharge.common.bean.JsonBean;
import java.util.List;

/* loaded from: classes.dex */
public class YZXPropset extends JsonBean<YZXPropset> {
    private List<YZXProperty> propset = null;

    public List<YZXProperty> getPropset() {
        return this.propset;
    }

    public void setPropset(List<YZXProperty> propset) {
        this.propset = propset;
    }
}