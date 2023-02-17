package com.xcharge.charger.core.api.bean;

import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

public class NackDirective extends JsonBean<NackDirective> {
    private HashMap<String, Object> attach = null;
    private int error = 0;
    private String msg = null;

    public int getError() {
        return this.error;
    }

    public void setError(int error2) {
        this.error = error2;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg2) {
        this.msg = msg2;
    }

    public HashMap<String, Object> getAttach() {
        return this.attach;
    }

    public void setAttach(HashMap<String, Object> attach2) {
        this.attach = attach2;
    }
}
