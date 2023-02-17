package com.xcharge.charger.protocol.monitor.bean.request;

import com.xcharge.charger.protocol.monitor.bean.cap.CAPAuthInfo;
import com.xcharge.common.bean.JsonBean;

public class AuthRequest extends JsonBean<AuthRequest> {
    private CAPAuthInfo auth_info = null;
    private Integer port = null;
    private Long swipe_time = null;
    private String user_code = null;
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

    public Integer getPort() {
        return this.port;
    }

    public void setPort(Integer port2) {
        this.port = port2;
    }

    public Long getSwipe_time() {
        return this.swipe_time;
    }

    public void setSwipe_time(Long swipe_time2) {
        this.swipe_time = swipe_time2;
    }

    public CAPAuthInfo getAuth_info() {
        return this.auth_info;
    }

    public void setAuth_info(CAPAuthInfo auth_info2) {
        this.auth_info = auth_info2;
    }
}
