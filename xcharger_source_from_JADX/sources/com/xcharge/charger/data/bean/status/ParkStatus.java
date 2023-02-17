package com.xcharge.charger.data.bean.status;

import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.bean.type.PARK_STATUS;
import com.xcharge.common.bean.JsonBean;

public class ParkStatus extends JsonBean<ParkStatus> {
    private LOCK_STATUS parkLockStatus = LOCK_STATUS.disable;
    private PARK_STATUS parkStatus = PARK_STATUS.idle;

    public PARK_STATUS getParkStatus() {
        return this.parkStatus;
    }

    public void setParkStatus(PARK_STATUS parkStatus2) {
        this.parkStatus = parkStatus2;
    }

    public LOCK_STATUS getParkLockStatus() {
        return this.parkLockStatus;
    }

    public void setParkLockStatus(LOCK_STATUS parkLockStatus2) {
        this.parkLockStatus = parkLockStatus2;
    }
}
