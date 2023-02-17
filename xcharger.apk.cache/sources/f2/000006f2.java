package com.xcharge.charger.protocol.monitor.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class DDAPMessage extends JsonBean<DDAPMessage> {
    private int code;
    private YZXDCAPMessage data;
    private String msg;

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public YZXDCAPMessage getData() {
        return this.data;
    }

    public void setData(YZXDCAPMessage data) {
        this.data = data;
    }
}