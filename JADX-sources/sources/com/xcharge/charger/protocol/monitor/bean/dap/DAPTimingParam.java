package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class DAPTimingParam extends JsonBean<DAPTimingParam> {
    private int interval_charge_cancel;
    private int interval_charge_report;
    private int interval_delay_start;
    private int interval_standby;

    public int getInterval_charge_cancel() {
        return this.interval_charge_cancel;
    }

    public void setInterval_charge_cancel(int interval_charge_cancel) {
        this.interval_charge_cancel = interval_charge_cancel;
    }

    public int getInterval_delay_start() {
        return this.interval_delay_start;
    }

    public void setInterval_delay_start(int interval_delay_start) {
        this.interval_delay_start = interval_delay_start;
    }

    public int getInterval_charge_report() {
        return this.interval_charge_report;
    }

    public void setInterval_charge_report(int interval_charge_report) {
        this.interval_charge_report = interval_charge_report;
    }

    public int getInterval_standby() {
        return this.interval_standby;
    }

    public void setInterval_standby(int interval_standby) {
        this.interval_standby = interval_standby;
    }
}
