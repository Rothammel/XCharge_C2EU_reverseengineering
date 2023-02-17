package com.xcharge.charger.data.bean.status;

import com.xcharge.charger.data.bean.FeeRate;
import com.xcharge.charger.data.bean.setting.ChargeFullRequirement;
import com.xcharge.charger.data.bean.setting.TimerSetting;
import com.xcharge.charger.data.bean.type.AMP_DISTR_POLICY;
import com.xcharge.charger.data.bean.type.WORK_MODE;
import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

/* loaded from: classes.dex */
public class ChargeStatus extends JsonBean<ChargeStatus> {
    private WORK_MODE workMode = WORK_MODE.Public;
    private int ampCapacity = 32;
    private int adjustAmp = 32;
    private int cpRange = 5;
    private int voltageRange = 15;
    private Integer leakageTolerance = null;
    private Boolean earthDisable = null;
    private double powerFactor = 1.0d;
    private AMP_DISTR_POLICY ampDistrPolicy = AMP_DISTR_POLICY.auto;
    private ChargeFullRequirement chargeFullRequirement = new ChargeFullRequirement();
    private TimerSetting timerSetting = new TimerSetting();
    private HashMap<String, PortStatus> portsStatus = null;
    private HashMap<String, FeeRate> portsFeeRate = null;
    private boolean cloudConnected = false;
    private boolean cloudTimeSynch = false;
    private boolean advertEnable = true;

    public HashMap<String, PortStatus> getPortsStatus() {
        return this.portsStatus;
    }

    public void setPortsStatus(HashMap<String, PortStatus> portsStatus) {
        this.portsStatus = portsStatus;
    }

    public WORK_MODE getWorkMode() {
        return this.workMode;
    }

    public void setWorkMode(WORK_MODE workMode) {
        this.workMode = workMode;
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

    public AMP_DISTR_POLICY getAmpDistrPolicy() {
        return this.ampDistrPolicy;
    }

    public void setAmpDistrPolicy(AMP_DISTR_POLICY ampDistrPolicy) {
        this.ampDistrPolicy = ampDistrPolicy;
    }

    public ChargeFullRequirement getChargeFullRequirement() {
        return this.chargeFullRequirement;
    }

    public void setChargeFullRequirement(ChargeFullRequirement chargeFullRequirement) {
        this.chargeFullRequirement = chargeFullRequirement;
    }

    public TimerSetting getTimerSetting() {
        return this.timerSetting;
    }

    public void setTimerSetting(TimerSetting timerSetting) {
        this.timerSetting = timerSetting;
    }

    public HashMap<String, FeeRate> getPortsFeeRate() {
        return this.portsFeeRate;
    }

    public void setPortsFeeRate(HashMap<String, FeeRate> portsFeeRate) {
        this.portsFeeRate = portsFeeRate;
    }

    public boolean isCloudConnected() {
        return this.cloudConnected;
    }

    public void setCloudConnected(boolean cloudConnected) {
        this.cloudConnected = cloudConnected;
    }

    public boolean isCloudTimeSynch() {
        return this.cloudTimeSynch;
    }

    public void setCloudTimeSynch(boolean cloudTimeSynch) {
        this.cloudTimeSynch = cloudTimeSynch;
    }

    public boolean isAdvertEnable() {
        return this.advertEnable;
    }

    public void setAdvertEnable(boolean advertEnable) {
        this.advertEnable = advertEnable;
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
}
