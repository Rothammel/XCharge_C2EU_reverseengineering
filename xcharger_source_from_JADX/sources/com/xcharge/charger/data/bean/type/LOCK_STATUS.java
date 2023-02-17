package com.xcharge.charger.data.bean.type;

import com.xcharge.charger.core.api.bean.cap.SetDirective;

public enum LOCK_STATUS {
    disable("disable"),
    lock(SetDirective.OPR_LOCK),
    unlock(SetDirective.OPR_UNLOCK),
    fault("fault");
    
    private String status;

    private LOCK_STATUS(String status2) {
        this.status = null;
        this.status = status2;
    }

    public String getStatus() {
        return this.status;
    }
}
