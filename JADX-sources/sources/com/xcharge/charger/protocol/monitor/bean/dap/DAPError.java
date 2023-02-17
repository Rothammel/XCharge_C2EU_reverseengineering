package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.charger.protocol.monitor.bean.YZXDCAPError;
import com.xcharge.common.bean.JsonBean;
import java.util.List;

/* loaded from: classes.dex */
public class DAPError extends JsonBean<DAPError> {
    private List<YZXDCAPError> error_set;

    public List<YZXDCAPError> getError_set() {
        return this.error_set;
    }

    public void setError_set(List<YZXDCAPError> error_set) {
        this.error_set = error_set;
    }
}
