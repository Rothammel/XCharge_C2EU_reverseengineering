package com.xcharge.charger.protocol.monitor.session;

import com.xcharge.charger.data.bean.type.CHARGE_INIT_TYPE;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.USER_TC_TYPE;
import com.xcharge.common.bean.JsonBean;

public class MonitorChargeSession extends JsonBean<MonitorChargeSession> {
    private String binded_user = null;
    private long chargeReportCnt = 0;
    private long chargeStartTime = 0;
    private long chargeStopTime = 0;
    private String charge_id = null;
    private CHARGE_PLATFORM charge_platform = null;
    private String device_id = null;
    private String fee_rate = null;
    private CHARGE_INIT_TYPE init_type = null;
    private int intervalChargeReport = 5000;
    private int is_free = -1;
    private String local_charge_id = null;
    private String port = null;
    private int timeout_plugin = 0;
    private int timeout_plugout = 0;
    private long user_balance = 0;
    private String user_code = null;
    private USER_TC_TYPE user_tc_type = null;
    private String user_tc_value = null;
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

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id2) {
        this.charge_id = charge_id2;
    }

    public String getFee_rate() {
        return this.fee_rate;
    }

    public void setFee_rate(String fee_rate2) {
        this.fee_rate = fee_rate2;
    }

    public CHARGE_INIT_TYPE getInit_type() {
        return this.init_type;
    }

    public void setInit_type(CHARGE_INIT_TYPE init_type2) {
        this.init_type = init_type2;
    }

    public String getLocal_charge_id() {
        return this.local_charge_id;
    }

    public void setLocal_charge_id(String local_charge_id2) {
        this.local_charge_id = local_charge_id2;
    }

    public int getTimeout_plugin() {
        return this.timeout_plugin;
    }

    public void setTimeout_plugin(int timeout_plugin2) {
        this.timeout_plugin = timeout_plugin2;
    }

    public int getTimeout_plugout() {
        return this.timeout_plugout;
    }

    public void setTimeout_plugout(int timeout_plugout2) {
        this.timeout_plugout = timeout_plugout2;
    }

    public USER_TC_TYPE getUser_tc_type() {
        return this.user_tc_type;
    }

    public void setUser_tc_type(USER_TC_TYPE user_tc_type2) {
        this.user_tc_type = user_tc_type2;
    }

    public String getUser_tc_value() {
        return this.user_tc_value;
    }

    public void setUser_tc_value(String user_tc_value2) {
        this.user_tc_value = user_tc_value2;
    }

    public long getUser_balance() {
        return this.user_balance;
    }

    public void setUser_balance(long user_balance2) {
        this.user_balance = user_balance2;
    }

    public int getIs_free() {
        return this.is_free;
    }

    public void setIs_free(int is_free2) {
        this.is_free = is_free2;
    }

    public CHARGE_PLATFORM getCharge_platform() {
        return this.charge_platform;
    }

    public void setCharge_platform(CHARGE_PLATFORM charge_platform2) {
        this.charge_platform = charge_platform2;
    }

    public String getBinded_user() {
        return this.binded_user;
    }

    public void setBinded_user(String binded_user2) {
        this.binded_user = binded_user2;
    }

    public long getChargeStartTime() {
        return this.chargeStartTime;
    }

    public void setChargeStartTime(long chargeStartTime2) {
        this.chargeStartTime = chargeStartTime2;
    }

    public long getChargeStopTime() {
        return this.chargeStopTime;
    }

    public void setChargeStopTime(long chargeStopTime2) {
        this.chargeStopTime = chargeStopTime2;
    }

    public int getIntervalChargeReport() {
        return this.intervalChargeReport;
    }

    public void setIntervalChargeReport(int intervalChargeReport2) {
        this.intervalChargeReport = intervalChargeReport2;
    }

    public long getChargeReportCnt() {
        return this.chargeReportCnt;
    }

    public void setChargeReportCnt(long chargeReportCnt2) {
        this.chargeReportCnt = chargeReportCnt2;
    }

    public void incChargeReportCnt(int delta) {
        this.chargeReportCnt += (long) delta;
    }
}
