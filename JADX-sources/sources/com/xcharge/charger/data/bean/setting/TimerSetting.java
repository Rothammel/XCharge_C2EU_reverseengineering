package com.xcharge.charger.data.bean.setting;

import com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class TimerSetting extends JsonBean<TimerSetting> {
    private int intervalStandby = 90;
    private int intervalChargeCancel = 90;
    private int intervalDelayStart = 1800;
    private int intervalDelayTip = XCloudProtocolAgent.TIMER_MQTT_CONNECT_BLOCKED;
    private int intervalChargeReport = 60;

    public int getIntervalStandby() {
        return this.intervalStandby;
    }

    public void setIntervalStandby(int intervalStandby) {
        this.intervalStandby = intervalStandby;
    }

    public int getIntervalChargeCancel() {
        return this.intervalChargeCancel;
    }

    public void setIntervalChargeCancel(int intervalChargeCancel) {
        this.intervalChargeCancel = intervalChargeCancel;
    }

    public int getIntervalDelayStart() {
        return this.intervalDelayStart;
    }

    public void setIntervalDelayStart(int intervalDelayStart) {
        this.intervalDelayStart = intervalDelayStart;
    }

    public int getIntervalChargeReport() {
        return this.intervalChargeReport;
    }

    public void setIntervalChargeReport(int intervalChargeReport) {
        this.intervalChargeReport = intervalChargeReport;
    }

    public int getIntervalDelayTip() {
        return this.intervalDelayTip;
    }

    public void setIntervalDelayTip(int intervalDelayTip) {
        this.intervalDelayTip = intervalDelayTip;
    }
}
