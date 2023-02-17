package com.xcharge.charger.device.p005c2.bean;

import com.xcharge.common.bean.JsonBean;

/* renamed from: com.xcharge.charger.device.c2.bean.IntValue */
public class IntValue extends JsonBean<IntValue> {
    private int value = 0;

    public int getValue() {
        return this.value;
    }

    public void setValue(int value2) {
        this.value = value2;
    }
}
