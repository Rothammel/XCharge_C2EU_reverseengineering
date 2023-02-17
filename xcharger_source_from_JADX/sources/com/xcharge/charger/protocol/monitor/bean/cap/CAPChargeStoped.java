package com.xcharge.charger.protocol.monitor.bean.cap;

import com.xcharge.charger.protocol.monitor.bean.YZXDCAPError;
import com.xcharge.common.bean.JsonBean;

public class CAPChargeStoped extends JsonBean<CAPChargeStoped> {
    private String charge_id;
    private double start_ammeter;
    private long start_time;
    private double stop_ammeter;
    private YZXDCAPError stop_cause;
    private long stop_time;
    private double total_power;

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id2) {
        this.charge_id = charge_id2;
    }

    public long getStop_time() {
        return this.stop_time;
    }

    public void setStop_time(long stop_time2) {
        this.stop_time = stop_time2;
    }

    public YZXDCAPError getStop_cause() {
        return this.stop_cause;
    }

    public void setStop_cause(YZXDCAPError stop_cause2) {
        this.stop_cause = stop_cause2;
    }

    public long getStart_time() {
        return this.start_time;
    }

    public void setStart_time(long start_time2) {
        this.start_time = start_time2;
    }

    public double getStart_ammeter() {
        return this.start_ammeter;
    }

    public void setStart_ammeter(double start_ammeter2) {
        this.start_ammeter = start_ammeter2;
    }

    public double getStop_ammeter() {
        return this.stop_ammeter;
    }

    public void setStop_ammeter(double stop_ammeter2) {
        this.stop_ammeter = stop_ammeter2;
    }

    public double getTotal_power() {
        return this.total_power;
    }

    public void setTotal_power(double total_power2) {
        this.total_power = total_power2;
    }
}
