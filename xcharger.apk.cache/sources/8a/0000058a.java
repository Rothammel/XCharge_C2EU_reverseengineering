package com.xcharge.charger.data.bean.device;

import com.xcharge.charger.data.bean.type.SWITCH_STATUS;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class Screen extends JsonBean<Screen> {
    private SWITCH_STATUS status = SWITCH_STATUS.on;

    public SWITCH_STATUS getStatus() {
        return this.status;
    }

    public void setStatus(SWITCH_STATUS status) {
        this.status = status;
    }
}