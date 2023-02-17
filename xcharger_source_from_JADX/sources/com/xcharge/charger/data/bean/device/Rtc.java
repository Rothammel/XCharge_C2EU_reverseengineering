package com.xcharge.charger.data.bean.device;

import com.xcharge.charger.data.bean.type.SWITCH_STATUS;
import com.xcharge.common.bean.JsonBean;

public class Rtc extends JsonBean<Rtc> {
    private SWITCH_STATUS status = SWITCH_STATUS.on;

    public SWITCH_STATUS getStatus() {
        return this.status;
    }

    public void setStatus(SWITCH_STATUS status2) {
        this.status = status2;
    }
}
