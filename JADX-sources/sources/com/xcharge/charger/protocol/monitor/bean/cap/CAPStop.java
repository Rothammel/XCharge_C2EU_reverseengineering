package com.xcharge.charger.protocol.monitor.bean.cap;

import com.xcharge.charger.protocol.monitor.bean.YZXDCAPError;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class CAPStop extends JsonBean<CAPStop> {
    private String charge_id = null;
    private YZXDCAPError stop_cause = null;

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id) {
        this.charge_id = charge_id;
    }

    public YZXDCAPError getStop_cause() {
        return this.stop_cause;
    }

    public void setStop_cause(YZXDCAPError stop_cause) {
        this.stop_cause = stop_cause;
    }
}
