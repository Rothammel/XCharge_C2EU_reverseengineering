package com.xcharge.charger.protocol.monitor.bean;

import com.xcharge.charger.protocol.monitor.util.FieldConfigUtils;
import com.xcharge.common.bean.JsonBean;

public class YZXDCAPError extends JsonBean<YZXDCAPError> {
    private int code = FieldConfigUtils.getCode(ErrorCodeMapping.E_SYSTEM_ERROR);
    private Object detail = null;
    private String msg = null;

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

    public Object getDetail() {
        return this.detail;
    }

    public void setDetail(Object detail2) {
        this.detail = detail2;
    }

    public void init(ErrorCodeMapping codeMapping) {
        this.code = FieldConfigUtils.getCode(codeMapping);
        this.msg = FieldConfigUtils.getMsg(codeMapping);
    }
}
