package com.xcharge.charger.device.p005c2.bean;

import com.xcharge.common.bean.JsonBean;

/* renamed from: com.xcharge.charger.device.c2.bean.BLNValue */
public class BLNValue extends JsonBean<BLNValue> {
    private int color = 0;
    private int delayOff = 0;
    private int delayOn = 0;

    public int getColor() {
        return this.color;
    }

    public void setColor(int color2) {
        this.color = color2;
    }

    public int getDelayOn() {
        return this.delayOn;
    }

    public void setDelayOn(int delayOn2) {
        this.delayOn = delayOn2;
    }

    public int getDelayOff() {
        return this.delayOff;
    }

    public void setDelayOff(int delayOff2) {
        this.delayOff = delayOff2;
    }
}
