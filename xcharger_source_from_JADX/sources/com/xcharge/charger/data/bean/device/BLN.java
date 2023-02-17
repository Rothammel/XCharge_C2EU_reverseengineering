package com.xcharge.charger.data.bean.device;

import com.xcharge.charger.data.bean.type.BLN_MODE;
import com.xcharge.charger.data.bean.type.SWITCH_STATUS;
import com.xcharge.common.bean.JsonBean;

public class BLN extends JsonBean<BLN> {
    public static final String DEFAULT_COLOR_STR = "#00ff00";
    public static final int DEFAULT_COLOR_VAL = 65280;
    private int color = 65280;
    private int defaultColor = 65280;
    private BLN_MODE mode = BLN_MODE.on_off;
    private SWITCH_STATUS status = SWITCH_STATUS.on;

    public SWITCH_STATUS getStatus() {
        return this.status;
    }

    public void setStatus(SWITCH_STATUS status2) {
        this.status = status2;
    }

    public int getDefaultColor() {
        return this.defaultColor;
    }

    public void setDefaultColor(int defaultColor2) {
        this.defaultColor = defaultColor2;
    }

    public int getColor() {
        return this.color;
    }

    public void setColor(int color2) {
        this.color = color2;
    }

    public BLN_MODE getMode() {
        return this.mode;
    }

    public void setMode(BLN_MODE mode2) {
        this.mode = mode2;
    }
}
