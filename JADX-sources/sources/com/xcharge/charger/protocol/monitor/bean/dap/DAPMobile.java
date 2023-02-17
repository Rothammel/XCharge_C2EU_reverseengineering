package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.charger.protocol.monitor.bean.ddap.DDAPMobileAccess;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class DAPMobile extends JsonBean<DAPMobile> {
    private DDAPMobileAccess moblie = null;

    public DDAPMobileAccess getMoblie() {
        return this.moblie;
    }

    public void setMoblie(DDAPMobileAccess moblie) {
        this.moblie = moblie;
    }
}
