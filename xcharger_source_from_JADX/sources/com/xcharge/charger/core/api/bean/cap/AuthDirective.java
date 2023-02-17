package com.xcharge.charger.core.api.bean.cap;

import com.xcharge.charger.data.bean.type.CHARGE_INIT_TYPE;
import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

public class AuthDirective extends JsonBean<AuthDirective> {
    private String device_id = null;
    private CHARGE_INIT_TYPE init_type = null;
    private String port = null;
    private String user_code = null;
    private HashMap<String, Object> user_data = null;
    private String user_type = null;

    public String getUser_type() {
        return this.user_type;
    }

    public void setUser_type(String user_type2) {
        this.user_type = user_type2;
    }

    public String getUser_code() {
        return this.user_code;
    }

    public void setUser_code(String user_code2) {
        this.user_code = user_code2;
    }

    public String getDevice_id() {
        return this.device_id;
    }

    public void setDevice_id(String device_id2) {
        this.device_id = device_id2;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port2) {
        this.port = port2;
    }

    public CHARGE_INIT_TYPE getInit_type() {
        return this.init_type;
    }

    public void setInit_type(CHARGE_INIT_TYPE init_type2) {
        this.init_type = init_type2;
    }

    public HashMap<String, Object> getUser_data() {
        return this.user_data;
    }

    public void setUser_data(HashMap<String, Object> user_data2) {
        this.user_data = user_data2;
    }
}
