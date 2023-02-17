package com.xcharge.charger.protocol.monitor.bean.response;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class AuthResponse extends JsonBean<AuthResponse> {
    private String user_type = null;
    private String user_code = null;
    private Integer port = null;
    private String charge_id = null;
    private String fee_rate_id = null;
    private Long balance = 0L;

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

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id) {
        this.charge_id = charge_id;
    }

    public String getFee_rate_id() {
        return this.fee_rate_id;
    }

    public void setFee_rate_id(String fee_rate_id) {
        this.fee_rate_id = fee_rate_id;
    }

    public Long getBalance() {
        return this.balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }
}
