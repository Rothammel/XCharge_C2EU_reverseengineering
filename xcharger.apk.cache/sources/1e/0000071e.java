package com.xcharge.charger.protocol.monitor.bean.request;

import com.xcharge.charger.protocol.monitor.bean.cap.CAPAuthInfo;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class AuthRequest extends JsonBean<AuthRequest> {
    private String user_type = null;
    private String user_code = null;
    private Integer port = null;
    private Long swipe_time = null;
    private CAPAuthInfo auth_info = null;

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

    public Integer getPort() {
        return this.port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Long getSwipe_time() {
        return this.swipe_time;
    }

    public void setSwipe_time(Long swipe_time) {
        this.swipe_time = swipe_time;
    }

    public CAPAuthInfo getAuth_info() {
        return this.auth_info;
    }

    public void setAuth_info(CAPAuthInfo auth_info) {
        this.auth_info = auth_info;
    }
}