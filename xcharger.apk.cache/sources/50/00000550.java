package com.xcharge.charger.core.api.bean.cap;

import com.xcharge.charger.core.type.CHARGE_REFUSE_CAUSE;
import com.xcharge.charger.data.bean.type.CHARGE_STATUS;
import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

/* loaded from: classes.dex */
public class EventDirective extends JsonBean<EventDirective> {
    public static final String EVENT_CHARGE_PAUSE = "charge_pause";
    public static final String EVENT_CHARGE_REFUSE = "charge_refuse";
    public static final String EVENT_CHARGE_RESUME = "charge_resume";
    public static final String EVENT_CHARGE_START = "charge_start";
    public static final String EVENT_CHARGE_STOP = "charge_stop";
    public static final String EVENT_DEALY_START = "delay_start";
    public static final String EVENT_DEALY_WAIT_START = "delay_wait_start";
    public static final String EVENT_PLUGIN = "plugin";
    public static final String EVENT_SCAN_ADVERT_FIN = "scan_advert_fin";
    private CHARGE_STATUS charge_status = null;
    private long start_time = 0;
    private long stop_time = 0;
    private double total_power = 0.0d;
    private long delay_start = 0;
    private int total_delay = 0;
    private int delay_fee = 0;
    private CHARGE_REFUSE_CAUSE refuse_cause = null;
    private HashMap<String, Object> attach = null;

    public CHARGE_STATUS getCharge_status() {
        return this.charge_status;
    }

    public void setCharge_status(CHARGE_STATUS charge_status) {
        this.charge_status = charge_status;
    }

    public double getTotal_power() {
        return this.total_power;
    }

    public void setTotal_power(double total_power) {
        this.total_power = total_power;
    }

    public int getTotal_delay() {
        return this.total_delay;
    }

    public void setTotal_delay(int total_delay) {
        this.total_delay = total_delay;
    }

    public long getStart_time() {
        return this.start_time;
    }

    public void setStart_time(long start_time) {
        this.start_time = start_time;
    }

    public long getStop_time() {
        return this.stop_time;
    }

    public void setStop_time(long stop_time) {
        this.stop_time = stop_time;
    }

    public long getDelay_start() {
        return this.delay_start;
    }

    public void setDelay_start(long delay_start) {
        this.delay_start = delay_start;
    }

    public int getDelay_fee() {
        return this.delay_fee;
    }

    public void setDelay_fee(int delay_fee) {
        this.delay_fee = delay_fee;
    }

    public CHARGE_REFUSE_CAUSE getRefuse_cause() {
        return this.refuse_cause;
    }

    public void setRefuse_cause(CHARGE_REFUSE_CAUSE refuse_cause) {
        this.refuse_cause = refuse_cause;
    }

    public HashMap<String, Object> getAttach() {
        return this.attach;
    }

    public void setAttach(HashMap<String, Object> attach) {
        this.attach = attach;
    }
}