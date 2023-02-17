package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.common.bean.JsonBean;

public class DAPRadarParam extends JsonBean<DAPRadarParam> {
    private double dist;
    private String work_time;

    public double getDist() {
        return this.dist;
    }

    public void setDist(double dist2) {
        this.dist = dist2;
    }

    public String getWork_time() {
        return this.work_time;
    }

    public void setWork_time(String work_time2) {
        this.work_time = work_time2;
    }
}
