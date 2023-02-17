package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.charger.protocol.monitor.bean.ddap.DDAPEthernetAccess;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class DAPEthernet extends JsonBean<DAPEthernet> {
    private DDAPEthernetAccess ethernet = null;

    public DDAPEthernetAccess getEthernet() {
        return this.ethernet;
    }

    public void setEthernet(DDAPEthernetAccess ethernet) {
        this.ethernet = ethernet;
    }
}
