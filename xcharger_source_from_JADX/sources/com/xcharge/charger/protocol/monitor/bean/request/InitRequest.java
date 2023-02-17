package com.xcharge.charger.protocol.monitor.bean.request;

import com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition;
import com.xcharge.common.bean.JsonBean;

public class InitRequest extends JsonBean<InitRequest> {
    public static final String UTYPE_FACEBOOK = "facebook";
    public static final String UTYPE_NFC = "nfc";
    public static final String UTYPE_QQ = "qq";
    public static final String UTYPE_TWITTER = "twitter";
    public static final String UTYPE_WECHAT = "wechat";
    public static final String UTYPE_WEIBO = "weibo";
    public static final String UTYPE_WXMP = "wxmp";
    private Long balance = null;
    private String charge_id = null;
    private String fee_rate_id = null;
    private String init_type = null;
    private Integer port = null;
    private ChargeStopCondition stop_condition = null;
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

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id2) {
        this.charge_id = charge_id2;
    }

    public String getFee_rate_id() {
        return this.fee_rate_id;
    }

    public void setFee_rate_id(String fee_rate_id2) {
        this.fee_rate_id = fee_rate_id2;
    }

    public ChargeStopCondition getStop_condition() {
        return this.stop_condition;
    }

    public void setStop_condition(ChargeStopCondition stop_condition2) {
        this.stop_condition = stop_condition2;
    }

    public Long getBalance() {
        return this.balance;
    }

    public void setBalance(Long balance2) {
        this.balance = balance2;
    }

    public String getInit_type() {
        return this.init_type;
    }

    public void setInit_type(String init_type2) {
        this.init_type = init_type2;
    }
}
