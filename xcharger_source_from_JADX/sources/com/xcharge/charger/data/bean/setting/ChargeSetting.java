package com.xcharge.charger.data.bean.setting;

import com.xcharge.charger.data.bean.type.AMP_DISTR_POLICY;
import com.xcharge.charger.data.bean.type.WORK_MODE;
import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

public class ChargeSetting extends JsonBean<ChargeSetting> {
    private int adjustAmp = 32;
    private int ampCapacity = 32;
    private ChargeFullRequirement chargeFullRequirement = new ChargeFullRequirement();
    private int cpRange = 5;
    private AMP_DISTR_POLICY currentDistrPolicy = AMP_DISTR_POLICY.auto;
    private Boolean earthDisable = null;
    private Integer leakageTolerance = null;
    private int manufactoryAmpCapacity = 32;
    private String operatorId = null;
    private HashMap<String, PortSetting> portsSetting = null;
    private double powerFactor = 1.0d;
    private TimerSetting timerSetting = new TimerSetting();
    private int voltageRange = 15;
    private WORK_MODE workMode = WORK_MODE.Public;

    public int getManufactoryAmpCapacity() {
        return this.manufactoryAmpCapacity;
    }

    public void setManufactoryAmpCapacity(int manufactoryAmpCapacity2) {
        this.manufactoryAmpCapacity = manufactoryAmpCapacity2;
    }

    public int getAmpCapacity() {
        return this.ampCapacity;
    }

    public void setAmpCapacity(int ampCapacity2) {
        this.ampCapacity = ampCapacity2;
    }

    public int getAdjustAmp() {
        return this.adjustAmp;
    }

    public void setAdjustAmp(int adjustAmp2) {
        this.adjustAmp = adjustAmp2;
    }

    public double getPowerFactor() {
        return this.powerFactor;
    }

    public void setPowerFactor(double powerFactor2) {
        this.powerFactor = powerFactor2;
    }

    public AMP_DISTR_POLICY getCurrentDistrPolicy() {
        return this.currentDistrPolicy;
    }

    public void setCurrentDistrPolicy(AMP_DISTR_POLICY currentDistrPolicy2) {
        this.currentDistrPolicy = currentDistrPolicy2;
    }

    public ChargeFullRequirement getChargeFullRequirement() {
        return this.chargeFullRequirement;
    }

    public void setChargeFullRequirement(ChargeFullRequirement chargeFullRequirement2) {
        this.chargeFullRequirement = chargeFullRequirement2;
    }

    public WORK_MODE getWorkMode() {
        return this.workMode;
    }

    public void setWorkMode(WORK_MODE workMode2) {
        this.workMode = workMode2;
    }

    public TimerSetting getTimerSetting() {
        return this.timerSetting;
    }

    public void setTimerSetting(TimerSetting timerSetting2) {
        this.timerSetting = timerSetting2;
    }

    public HashMap<String, PortSetting> getPortsSetting() {
        return this.portsSetting;
    }

    public void setPortsSetting(HashMap<String, PortSetting> portsSetting2) {
        this.portsSetting = portsSetting2;
    }

    public int getCpRange() {
        return this.cpRange;
    }

    public void setCpRange(int cpRange2) {
        this.cpRange = cpRange2;
    }

    public int getVoltageRange() {
        return this.voltageRange;
    }

    public void setVoltageRange(int voltageRange2) {
        this.voltageRange = voltageRange2;
    }

    public Integer getLeakageTolerance() {
        return this.leakageTolerance;
    }

    public void setLeakageTolerance(Integer leakageTolerance2) {
        this.leakageTolerance = leakageTolerance2;
    }

    public Boolean isEarthDisable() {
        return this.earthDisable;
    }

    public void setEarthDisable(Boolean earthDisable2) {
        this.earthDisable = earthDisable2;
    }

    public String getOperatorId() {
        return this.operatorId;
    }

    public void setOperatorId(String operatorId2) {
        this.operatorId = operatorId2;
    }
}
