package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class DAPRadarParam extends JsonBean<DAPRadarParam> {
    private double dist;
    private String work_time;

    public double getDist() {
        return this.dist;
    }

    public void setDist(double dist) {
        this.dist = dist;
    }

    public String getWork_time() {
        return this.work_time;
    }

    public void setWork_time(String work_time) {
        this.work_time = work_time;
    }
}