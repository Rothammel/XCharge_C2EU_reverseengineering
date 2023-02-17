package com.xcharge.charger.core.api.bean.cap;

import com.xcharge.charger.core.type.FIN_MODE;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.type.CHARGE_STATUS;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class FinDirective extends JsonBean<FinDirective> {
    private FIN_MODE fin_mode = null;
    private ErrorCode error = null;
    private CHARGE_STATUS charge_status = null;
    private String start_time = null;
    private String stop_time = null;
    private double total_power = 0.0d;
    private int total_delay = 0;

    public FIN_MODE getFin_mode() {
        return this.fin_mode;
    }

    public void setFin_mode(FIN_MODE fin_mode) {
        this.fin_mode = fin_mode;
    }

    public ErrorCode getError() {
        return this.error;
    }

    public void setError(ErrorCode error) {
        this.error = error;
    }

    public CHARGE_STATUS getCharge_status() {
        return this.charge_status;
    }

    public void setCharge_status(CHARGE_STATUS charge_status) {
        this.charge_status = charge_status;
    }

    public String getStart_time() {
        return this.start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getStop_time() {
        return this.stop_time;
    }

    public void setStop_time(String stop_time) {
        this.stop_time = stop_time;
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
}
