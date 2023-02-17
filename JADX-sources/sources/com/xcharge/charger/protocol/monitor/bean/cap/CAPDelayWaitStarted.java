package com.xcharge.charger.protocol.monitor.bean.cap;

import com.xcharge.charger.protocol.monitor.bean.YZXDCAPError;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class CAPDelayWaitStarted extends JsonBean<CAPDelayWaitStarted> {
    private String charge_id;
    private long delay_wait_start_time;
    private int park_fee;
    private int power_fee;
    private int service_fee;
    private double start_ammeter;
    private long start_time;
    private double stop_ammeter;
    private YZXDCAPError stop_cause;
    private long stop_time;
    private int total_fee;
    private double total_power;

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id) {
        this.charge_id = charge_id;
    }

    public long getDelay_wait_start_time() {
        return this.delay_wait_start_time;
    }

    public void setDelay_wait_start_time(long delay_wait_start_time) {
        this.delay_wait_start_time = delay_wait_start_time;
    }

    public long getStop_time() {
        return this.stop_time;
    }

    public void setStop_time(long stop_time) {
        this.stop_time = stop_time;
    }

    public YZXDCAPError getStop_cause() {
        return this.stop_cause;
    }

    public void setStop_cause(YZXDCAPError stop_cause) {
        this.stop_cause = stop_cause;
    }

    public long getStart_time() {
        return this.start_time;
    }

    public void setStart_time(long start_time) {
        this.start_time = start_time;
    }

    public double getStart_ammeter() {
        return this.start_ammeter;
    }

    public void setStart_ammeter(double start_ammeter) {
        this.start_ammeter = start_ammeter;
    }

    public double getStop_ammeter() {
        return this.stop_ammeter;
    }

    public void setStop_ammeter(double stop_ammeter) {
        this.stop_ammeter = stop_ammeter;
    }

    public double getTotal_power() {
        return this.total_power;
    }

    public void setTotal_power(double total_power) {
        this.total_power = total_power;
    }

    public int getPower_fee() {
        return this.power_fee;
    }

    public void setPower_fee(int power_fee) {
        this.power_fee = power_fee;
    }

    public int getService_fee() {
        return this.service_fee;
    }

    public void setService_fee(int service_fee) {
        this.service_fee = service_fee;
    }

    public int getPark_fee() {
        return this.park_fee;
    }

    public void setPark_fee(int park_fee) {
        this.park_fee = park_fee;
    }

    public int getTotal_fee() {
        return this.total_fee;
    }

    public void setTotal_fee(int total_fee) {
        this.total_fee = total_fee;
    }
}
