package com.xcharge.charger.protocol.monitor.bean;

import com.xcharge.common.bean.JsonBean;

public class DDAPMessage extends JsonBean<DDAPMessage> {
    private int code;
    private YZXDCAPMessage data;
    private String msg;

    public int getCode() {
        return this.code;
    }

    public void setCode(int code2) {
        this.code = code2;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg2) {
        this.msg = msg2;
    }

    public YZXDCAPMessage getData() {
        return this.data;
    }

    public void setData(YZXDCAPMessage data2) {
        this.data = data2;
    }
}
