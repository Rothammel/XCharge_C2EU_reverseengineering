package com.xcharge.charger.protocol.family.xcloud.session;

import com.xcharge.charger.data.bean.type.CHARGE_INIT_TYPE;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.CHARGE_STOP_CAUSE;
import com.xcharge.charger.data.bean.type.USER_TC_TYPE;
import com.xcharge.charger.protocol.family.xcloud.bean.ChargeStopCondition;
import com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage;
import com.xcharge.common.bean.JsonBean;

public class XCloudChargeSession extends JsonBean<XCloudChargeSession> {
    private String binded_user = null;
    private long chargeReportCnt = 0;
    private long chargeStartTime = 0;
    private CHARGE_STOP_CAUSE chargeStopCause = null;
    private ChargeStopCondition chargeStopCondition = null;
    private long chargeStopTime = 0;
    private String charge_id = null;
    private CHARGE_PLATFORM charge_platform = null;
    private String cloud_charge_id = null;
    private String device_id = null;
    private String fee_rate = null;
    private CHARGE_INIT_TYPE init_type = null;
    private int intervalChargeReport = 5000;
    private int is_free = -1;
    private String port = null;
    private double power = 0.0d;
    XCloudMessage requestStartCharge = null;
    private Long requestStartChargeSid = null;
    private Long requestStopChargeSid = null;
    private int user_balance = 0;
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

    public int getUser_balance() {
        return this.user_balance;
    }

    public void setUser_balance(int user_balance2) {
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

    public String getCloud_charge_id() {
        return this.cloud_charge_id;
    }

    public void setCloud_charge_id(String cloud_charge_id2) {
        this.cloud_charge_id = cloud_charge_id2;
    }

    public Long getRequestStartChargeSid() {
        return this.requestStartChargeSid;
    }

    public void setRequestStartChargeSid(Long requestStartChargeSid2) {
        this.requestStartChargeSid = requestStartChargeSid2;
    }

    public Long getRequestStopChargeSid() {
        return this.requestStopChargeSid;
    }

    public void setRequestStopChargeSid(Long requestStopChargeSid2) {
        this.requestStopChargeSid = requestStopChargeSid2;
    }

    public ChargeStopCondition getChargeStopCondition() {
        return this.chargeStopCondition;
    }

    public void setChargeStopCondition(ChargeStopCondition chargeStopCondition2) {
        this.chargeStopCondition = chargeStopCondition2;
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

    public double getPower() {
        return this.power;
    }

    public void setPower(double power2) {
        this.power = power2;
    }

    public CHARGE_STOP_CAUSE getChargeStopCause() {
        return this.chargeStopCause;
    }

    public void setChargeStopCause(CHARGE_STOP_CAUSE chargeStopCause2) {
        this.chargeStopCause = chargeStopCause2;
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

    public XCloudMessage getRequestStartCharge() {
        return this.requestStartCharge;
    }

    public void setRequestStartCharge(XCloudMessage requestStartCharge2) {
        this.requestStartCharge = requestStartCharge2;
    }
}
