package com.xcharge.charger.core.api.bean.cap;

import com.xcharge.charger.core.type.FIN_MODE;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.type.CHARGE_STATUS;
import com.xcharge.common.bean.JsonBean;

public class FinDirective extends JsonBean<FinDirective> {
    private CHARGE_STATUS charge_status = null;
    private ErrorCode error = null;
    private FIN_MODE fin_mode = null;
    private String start_time = null;
    private String stop_time = null;
    private int total_delay = 0;
    private double total_power = 0.0d;

    public FIN_MODE getFin_mode() {
        return this.fin_mode;
    }

    public void setFin_mode(FIN_MODE fin_mode2) {
        this.fin_mode = fin_mode2;
    }

    public ErrorCode getError() {
        return this.error;
    }

    public void setError(ErrorCode error2) {
        this.error = error2;
    }

    public CHARGE_STATUS getCharge_status() {
        return this.charge_status;
    }

    public void setCharge_status(CHARGE_STATUS charge_status2) {
        this.charge_status = charge_status2;
    }

    public String getStart_time() {
        return this.start_time;
    }

    public void setStart_time(String start_time2) {
        this.start_time = start_time2;
    }

    public String getStop_time() {
        return this.stop_time;
    }

    public void setStop_time(String stop_time2) {
        this.stop_time = stop_time2;
    }

    public double getTotal_power() {
        return this.total_power;
    }

    public void setTotal_power(double total_power2) {
        this.total_power = total_power2;
    }

    public int getTotal_delay() {
        return this.total_delay;
    }

    public void setTotal_delay(int total_delay2) {
        this.total_delay = total_delay2;
    }
}
