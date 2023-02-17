package com.xcharge.charger.data.bean.status;

import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.bean.type.PARK_STATUS;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ParkStatus extends JsonBean<ParkStatus> {
    private PARK_STATUS parkStatus = PARK_STATUS.idle;
    private LOCK_STATUS parkLockStatus = LOCK_STATUS.disable;

    public PARK_STATUS getParkStatus() {
        return this.parkStatus;
    }

    public void setParkStatus(PARK_STATUS parkStatus) {
        this.parkStatus = parkStatus;
    }

    public LOCK_STATUS getParkLockStatus() {
        return this.parkLockStatus;
    }

    public void setParkLockStatus(LOCK_STATUS parkLockStatus) {
        this.parkLockStatus = parkLockStatus;
    }
}
