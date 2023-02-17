package com.xcharge.charger.data.bean.device;

import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ParkLock extends JsonBean<ParkLock> {
    private LOCK_STATUS status = LOCK_STATUS.disable;

    public LOCK_STATUS getStatus() {
        return this.status;
    }

    public void setStatus(LOCK_STATUS status) {
        this.status = status;
    }
}
