package com.xcharge.charger.data.bean.setting;

import com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent;
import com.xcharge.common.bean.JsonBean;

public class TimerSetting extends JsonBean<TimerSetting> {
    private int intervalChargeCancel = 90;
    private int intervalChargeReport = 60;
    private int intervalDelayStart = 1800;
    private int intervalDelayTip = XCloudProtocolAgent.TIMER_MQTT_CONNECT_BLOCKED;
    private int intervalStandby = 90;

    public int getIntervalStandby() {
        return this.intervalStandby;
    }

    public void setIntervalStandby(int intervalStandby2) {
        this.intervalStandby = intervalStandby2;
    }

    public int getIntervalChargeCancel() {
        return this.intervalChargeCancel;
    }

    public void setIntervalChargeCancel(int intervalChargeCancel2) {
        this.intervalChargeCancel = intervalChargeCancel2;
    }

    public int getIntervalDelayStart() {
        return this.intervalDelayStart;
    }

    public void setIntervalDelayStart(int intervalDelayStart2) {
        this.intervalDelayStart = intervalDelayStart2;
    }

    public int getIntervalChargeReport() {
        return this.intervalChargeReport;
    }

    public void setIntervalChargeReport(int intervalChargeReport2) {
        this.intervalChargeReport = intervalChargeReport2;
    }

    public int getIntervalDelayTip() {
        return this.intervalDelayTip;
    }

    public void setIntervalDelayTip(int intervalDelayTip2) {
        this.intervalDelayTip = intervalDelayTip2;
    }
}
