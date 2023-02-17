package com.xcharge.charger.device.c2.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class NFCSign extends JsonBean<NFCSign> {
    private String data = null;
    private String sign = null;

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getSign() {
        return this.sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
