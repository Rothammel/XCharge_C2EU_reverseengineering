package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class DeviceCapability extends JsonBean<DeviceCapability> {
    public static final String TYPE_CURRENT_AC = "ac";
    public static final String TYPE_CURRENT_DC = "dc";
    public static final String TYPE_SCREEN_C2 = "9.7";
    public static final String TYPE_SCREEN_NONE = "none";
    private String currentType = null;
    private Integer phases = null;
    private Double maxPower = null;
    private Double maxCurrent = null;
    private String screen = null;
    private Boolean hasRadar = null;
    private Integer ports = null;

    public String getCurrentType() {
        return this.currentType;
    }

    public void setCurrentType(String currentType) {
        this.currentType = currentType;
    }

    public Integer getPhases() {
        return this.phases;
    }

    public void setPhases(Integer phases) {
        this.phases = phases;
    }

    public Double getMaxPower() {
        return this.maxPower;
    }

    public void setMaxPower(Double maxPower) {
        this.maxPower = maxPower;
    }

    public Double getMaxCurrent() {
        return this.maxCurrent;
    }

    public void setMaxCurrent(Double maxCurrent) {
        this.maxCurrent = maxCurrent;
    }

    public String getScreen() {
        return this.screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public Boolean getHasRadar() {
        return this.hasRadar;
    }

    public void setHasRadar(Boolean hasRadar) {
        this.hasRadar = hasRadar;
    }

    public Integer getPorts() {
        return this.ports;
    }

    public void setPorts(Integer ports) {
        this.ports = ports;
    }
}
