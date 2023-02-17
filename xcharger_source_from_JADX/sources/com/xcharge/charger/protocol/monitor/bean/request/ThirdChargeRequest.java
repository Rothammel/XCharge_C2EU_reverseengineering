package com.xcharge.charger.protocol.monitor.bean.request;

import com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition;
import com.xcharge.charger.protocol.monitor.bean.dap.DAPFeeRate;
import com.xcharge.common.bean.JsonBean;

public class ThirdChargeRequest extends JsonBean<ThirdChargeRequest> {
    private DAPFeeRate fee_rate;
    private long init_time;
    private int port;
    private ChargeStopCondition stop_condition;
    private String third_charge_id;
    private String third_platform;
    private String user_code;
    private String user_type;

    public String getThird_charge_id() {
        return this.third_charge_id;
    }

    public void setThird_charge_id(String third_charge_id2) {
        this.third_charge_id = third_charge_id2;
    }

    public String getThird_platform() {
        return this.third_platform;
    }

    public void setThird_platform(String third_platform2) {
        this.third_platform = third_platform2;
    }

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

    public int getPort() {
        return this.port;
    }

    public void setPort(int port2) {
        this.port = port2;
    }

    public long getInit_time() {
        return this.init_time;
    }

    public void setInit_time(long init_time2) {
        this.init_time = init_time2;
    }

    public ChargeStopCondition getStop_condition() {
        return this.stop_condition;
    }

    public void setStop_condition(ChargeStopCondition stop_condition2) {
        this.stop_condition = stop_condition2;
    }

    public DAPFeeRate getFee_rate() {
        return this.fee_rate;
    }

    public void setFee_rate(DAPFeeRate fee_rate2) {
        this.fee_rate = fee_rate2;
    }
}
