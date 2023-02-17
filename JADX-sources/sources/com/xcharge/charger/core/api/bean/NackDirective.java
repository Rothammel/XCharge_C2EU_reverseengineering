package com.xcharge.charger.core.api.bean;

import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

/* loaded from: classes.dex */
public class NackDirective extends JsonBean<NackDirective> {
    private int error = 0;
    private String msg = null;
    private HashMap<String, Object> attach = null;

    public int getError() {
        return this.error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public HashMap<String, Object> getAttach() {
        return this.attach;
    }

    public void setAttach(HashMap<String, Object> attach) {
        this.attach = attach;
    }
}
