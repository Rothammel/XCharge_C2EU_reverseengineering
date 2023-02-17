package com.xcharge.charger.protocol.monitor.bean.cap;

import com.xcharge.charger.protocol.monitor.bean.YZXDCAPError;
import com.xcharge.common.bean.JsonBean;

public class CAPChargeRefused extends JsonBean<CAPChargeRefused> {
    private YZXDCAPError refuse_cause;

    public YZXDCAPError getRefuse_cause() {
        return this.refuse_cause;
    }

    public void setRefuse_cause(YZXDCAPError refuse_cause2) {
        this.refuse_cause = refuse_cause2;
    }
}
