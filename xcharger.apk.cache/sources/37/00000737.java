package com.xcharge.charger.protocol.monitor.session;

import com.xcharge.charger.data.bean.type.CHARGE_INIT_TYPE;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.USER_TC_TYPE;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class MonitorChargeSession extends JsonBean<MonitorChargeSession> {
    private String user_type = null;
    private String user_code = null;
    private String device_id = null;
    private String port = null;
    private String charge_id = null;
    private String fee_rate = null;
    private CHARGE_INIT_TYPE init_type = null;
    private String local_charge_id = null;
    private USER_TC_TYPE user_tc_type = null;
    private String user_tc_value = null;
    private long user_balance = 0;
    private int is_free = -1;
    private CHARGE_PLATFORM charge_platform = null;
    private String binded_user = null;
    private long chargeStartTime = 0;
    private long chargeStopTime = 0;
    private int timeout_plugin = 0;
    private int timeout_plugout = 0;
    private int intervalChargeReport = 5000;
    private long chargeReportCnt = 0;

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

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id) {
        this.charge_id = charge_id;
    }

    public String getFee_rate() {
        return this.fee_rate;
    }

    public void setFee_rate(String fee_rate) {
        this.fee_rate = fee_rate;
    }

    public CHARGE_INIT_TYPE getInit_type() {
        return this.init_type;
    }

    public void setInit_type(CHARGE_INIT_TYPE init_type) {
        this.init_type = init_type;
    }

    public String getLocal_charge_id() {
        return this.local_charge_id;
    }

    public void setLocal_charge_id(String local_charge_id) {
        this.local_charge_id = local_charge_id;
    }

    public int getTimeout_plugin() {
        return this.timeout_plugin;
    }

    public void setTimeout_plugin(int timeout_plugin) {
        this.timeout_plugin = timeout_plugin;
    }

    public int getTimeout_plugout() {
        return this.timeout_plugout;
    }

    public void setTimeout_plugout(int timeout_plugout) {
        this.timeout_plugout = timeout_plugout;
    }

    public USER_TC_TYPE getUser_tc_type() {
        return this.user_tc_type;
    }

    public void setUser_tc_type(USER_TC_TYPE user_tc_type) {
        this.user_tc_type = user_tc_type;
    }

    public String getUser_tc_value() {
        return this.user_tc_value;
    }

    public void setUser_tc_value(String user_tc_value) {
        this.user_tc_value = user_tc_value;
    }

    public long getUser_balance() {
        return this.user_balance;
    }

    public void setUser_balance(long user_balance) {
        this.user_balance = user_balance;
    }

    public int getIs_free() {
        return this.is_free;
    }

    public void setIs_free(int is_free) {
        this.is_free = is_free;
    }

    public CHARGE_PLATFORM getCharge_platform() {
        return this.charge_platform;
    }

    public void setCharge_platform(CHARGE_PLATFORM charge_platform) {
        this.charge_platform = charge_platform;
    }

    public String getBinded_user() {
        return this.binded_user;
    }

    public void setBinded_user(String binded_user) {
        this.binded_user = binded_user;
    }

    public long getChargeStartTime() {
        return this.chargeStartTime;
    }

    public void setChargeStartTime(long chargeStartTime) {
        this.chargeStartTime = chargeStartTime;
    }

    public long getChargeStopTime() {
        return this.chargeStopTime;
    }

    public void setChargeStopTime(long chargeStopTime) {
        this.chargeStopTime = chargeStopTime;
    }

    public int getIntervalChargeReport() {
        return this.intervalChargeReport;
    }

    public void setIntervalChargeReport(int intervalChargeReport) {
        this.intervalChargeReport = intervalChargeReport;
    }

    public long getChargeReportCnt() {
        return this.chargeReportCnt;
    }

    public void setChargeReportCnt(long chargeReportCnt) {
        this.chargeReportCnt = chargeReportCnt;
    }

    public void incChargeReportCnt(int delta) {
        this.chargeReportCnt += delta;
    }
}