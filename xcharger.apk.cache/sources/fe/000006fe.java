package com.xcharge.charger.protocol.monitor.bean.cap;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class CAPChargeStarted extends JsonBean<CAPChargeStarted> {
    private String charge_id;
    private double start_ammeter;
    private long start_time;

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id) {
        this.charge_id = charge_id;
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
}