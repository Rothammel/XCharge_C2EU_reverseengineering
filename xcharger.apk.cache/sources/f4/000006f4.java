package com.xcharge.charger.protocol.monitor.bean;

import com.xcharge.charger.protocol.monitor.util.FieldConfigUtils;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class YZXDCAPError extends JsonBean<YZXDCAPError> {
    private int code = FieldConfigUtils.getCode(ErrorCodeMapping.E_SYSTEM_ERROR);
    private String msg = null;
    private Object detail = null;

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

    public Object getDetail() {
        return this.detail;
    }

    public void setDetail(Object detail) {
        this.detail = detail;
    }

    public void init(ErrorCodeMapping codeMapping) {
        this.code = FieldConfigUtils.getCode(codeMapping);
        this.msg = FieldConfigUtils.getMsg(codeMapping);
    }
}