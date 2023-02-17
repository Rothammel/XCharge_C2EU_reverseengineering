package com.xcharge.charger.device.p005c2.bean;

import com.xcharge.common.bean.JsonBean;

/* renamed from: com.xcharge.charger.device.c2.bean.NFCSignData */
public class NFCSignData extends JsonBean<NFCSignData> {
    private String data = null;
    private String sign = null;

    public String getData() {
        return this.data;
    }

    public void setData(String data2) {
        this.data = data2;
    }

    public String getSign() {
        return this.sign;
    }

    public void setSign(String sign2) {
        this.sign = sign2;
    }
}
