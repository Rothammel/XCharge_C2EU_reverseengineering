package com.xcharge.charger.data.bean.setting;

import com.xcharge.charger.data.bean.type.AMP_DISTR_POLICY;
import com.xcharge.charger.data.bean.type.WORK_MODE;
import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

/* loaded from: classes.dex */
public class ChargeSetting extends JsonBean<ChargeSetting> {
    private WORK_MODE workMode = WORK_MODE.Public;
    private int manufactoryAmpCapacity = 32;
    private int ampCapacity = 32;
    private int adjustAmp = 32;
    private int cpRange = 5;
    private int voltageRange = 15;
    private Integer leakageTolerance = null;
    private Boolean earthDisable = null;
    private double powerFactor = 1.0d;
    private AMP_DISTR_POLICY currentDistrPolicy = AMP_DISTR_POLICY.auto;
    private ChargeFullRequirement chargeFullRequirement = new ChargeFullRequirement();
    private TimerSetting timerSetting = new TimerSetting();
    private HashMap<String, PortSetting> portsSetting = null;
    private String operatorId = null;

    public int getManufactoryAmpCapacity() {
        return this.manufactoryAmpCapacity;
    }

    public void setManufactoryAmpCapacity(int manufactoryAmpCapacity) {
        this.manufactoryAmpCapacity = manufactoryAmpCapacity;
    }

    public int getAmpCapacity() {
        return this.ampCapacity;
    }

    public void setAmpCapacity(int ampCapacity) {
        this.ampCapacity = ampCapacity;
    }

    public int getAdjustAmp() {
        return this.adjustAmp;
    }

    public void setAdjustAmp(int adjustAmp) {
        this.adjustAmp = adjustAmp;
    }

    public double getPowerFactor() {
        return this.powerFactor;
    }

    public void setPowerFactor(double powerFactor) {
        this.powerFactor = powerFactor;
    }

    public AMP_DISTR_POLICY getCurrentDistrPolicy() {
        return this.currentDistrPolicy;
    }

    public void setCurrentDistrPolicy(AMP_DISTR_POLICY currentDistrPolicy) {
        this.currentDistrPolicy = currentDistrPolicy;
    }

    public ChargeFullRequirement getChargeFullRequirement() {
        return this.chargeFullRequirement;
    }

    public void setChargeFullRequirement(ChargeFullRequirement chargeFullRequirement) {
        this.chargeFullRequirement = chargeFullRequirement;
    }

    public WORK_MODE getWorkMode() {
        return this.workMode;
    }

    public void setWorkMode(WORK_MODE workMode) {
        this.workMode = workMode;
    }

    public TimerSetting getTimerSetting() {
        return this.timerSetting;
    }

    public void setTimerSetting(TimerSetting timerSetting) {
        this.timerSetting = timerSetting;
    }

    public HashMap<String, PortSetting> getPortsSetting() {
        return this.portsSetting;
    }

    public void setPortsSetting(HashMap<String, PortSetting> portsSetting) {
        this.portsSetting = portsSetting;
    }

    public int getCpRange() {
        return this.cpRange;
    }

    public void setCpRange(int cpRange) {
        this.cpRange = cpRange;
    }

    public int getVoltageRange() {
        return this.voltageRange;
    }

    public void setVoltageRange(int voltageRange) {
        this.voltageRange = voltageRange;
    }

    public Integer getLeakageTolerance() {
        return this.leakageTolerance;
    }

    public void setLeakageTolerance(Integer leakageTolerance) {
        this.leakageTolerance = leakageTolerance;
    }

    public Boolean isEarthDisable() {
        return this.earthDisable;
    }

    public void setEarthDisable(Boolean earthDisable) {
        this.earthDisable = earthDisable;
    }

    public String getOperatorId() {
        return this.operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }
}