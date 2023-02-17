package com.xcharge.charger.device.c2.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class BLNValue extends JsonBean<BLNValue> {
    private int color = 0;
    private int delayOn = 0;
    private int delayOff = 0;

    public int getColor() {
        return this.color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getDelayOn() {
        return this.delayOn;
    }

    public void setDelayOn(int delayOn) {
        this.delayOn = delayOn;
    }

    public int getDelayOff() {
        return this.delayOff;
    }

    public void setDelayOff(int delayOff) {
        this.delayOff = delayOff;
    }
}