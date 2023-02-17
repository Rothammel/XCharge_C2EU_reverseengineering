package com.xcharge.charger.core.api.bean.cap;

import com.xcharge.charger.data.bean.type.CHARGE_INIT_TYPE;
import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

/* loaded from: classes.dex */
public class AuthDirective extends JsonBean<AuthDirective> {
    private String user_type = null;
    private String user_code = null;
    private String device_id = null;
    private String port = null;
    private CHARGE_INIT_TYPE init_type = null;
    private HashMap<String, Object> user_data = null;

    public String getUser_type() {
        return this.user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    public String getUser_code() {
        return this.user_code;
    }

    public void setUser_code(String user_code) {
        this.user_code = user_code;
    }

    public String getDevice_id() {
        return this.device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public CHARGE_INIT_TYPE getInit_type() {
        return this.init_type;
    }

    public void setInit_type(CHARGE_INIT_TYPE init_type) {
        this.init_type = init_type;
    }

    public HashMap<String, Object> getUser_data() {
        return this.user_data;
    }

    public void setUser_data(HashMap<String, Object> user_data) {
        this.user_data = user_data;
    }
}