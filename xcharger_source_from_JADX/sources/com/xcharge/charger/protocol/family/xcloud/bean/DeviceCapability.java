package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;

public class DeviceCapability extends JsonBean<DeviceCapability> {
    public static final String TYPE_CURRENT_AC = "ac";
    public static final String TYPE_CURRENT_DC = "dc";
    public static final String TYPE_SCREEN_C2 = "9.7";
    public static final String TYPE_SCREEN_NONE = "none";
    private String currentType = null;
    private Boolean hasRadar = null;
    private Double maxCurrent = null;
    private Double maxPower = null;
    private Integer phases = null;
    private Integer ports = null;
    private String screen = null;

    public String getCurrentType() {
        return this.currentType;
    }

    public void setCurrentType(String currentType2) {
        this.currentType = currentType2;
    }

    public Integer getPhases() {
        return this.phases;
    }

    public void setPhases(Integer phases2) {
        this.phases = phases2;
    }

    public Double getMaxPower() {
        return this.maxPower;
    }

    public void setMaxPower(Double maxPower2) {
        this.maxPower = maxPower2;
    }

    public Double getMaxCurrent() {
        return this.maxCurrent;
    }

    public void setMaxCurrent(Double maxCurrent2) {
        this.maxCurrent = maxCurrent2;
    }

    public String getScreen() {
        return this.screen;
    }

    public void setScreen(String screen2) {
        this.screen = screen2;
    }

    public Boolean getHasRadar() {
        return this.hasRadar;
    }

    public void setHasRadar(Boolean hasRadar2) {
        this.hasRadar = hasRadar2;
    }

    public Integer getPorts() {
        return this.ports;
    }

    public void setPorts(Integer ports2) {
        this.ports = ports2;
    }
}
