package com.xcharge.charger.core.api.bean;

import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

public class AckDirective extends JsonBean<AckDirective> {
    private HashMap<String, Object> attach = null;

    public HashMap<String, Object> getAttach() {
        return this.attach;
    }

    public void setAttach(HashMap<String, Object> attach2) {
        this.attach = attach2;
    }
}
