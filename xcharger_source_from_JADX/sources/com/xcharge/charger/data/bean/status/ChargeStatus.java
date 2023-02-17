package com.xcharge.charger.data.bean.status;

import com.xcharge.charger.data.bean.FeeRate;
import com.xcharge.charger.data.bean.setting.ChargeFullRequirement;
import com.xcharge.charger.data.bean.setting.TimerSetting;
import com.xcharge.charger.data.bean.type.AMP_DISTR_POLICY;
import com.xcharge.charger.data.bean.type.WORK_MODE;
import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

public class ChargeStatus extends JsonBean<ChargeStatus> {
    private int adjustAmp = 32;
    private boolean advertEnable = true;
    private int ampCapacity = 32;
    private AMP_DISTR_POLICY ampDistrPolicy = AMP_DISTR_POLICY.auto;
    private ChargeFullRequirement chargeFullRequirement = new ChargeFullRequirement();
    private boolean cloudConnected = false;
    private boolean cloudTimeSynch = false;
    private int cpRange = 5;
    private Boolean earthDisable = null;
    private Integer leakageTolerance = null;
    private HashMap<String, FeeRate> portsFeeRate = null;
    private HashMap<String, PortStatus> portsStatus = null;
    private double powerFactor = 1.0d;
    private TimerSetting timerSetting = new TimerSetting();
    private int voltageRange = 15;
    private WORK_MODE workMode = WORK_MODE.Public;

    public HashMap<String, PortStatus> getPortsStatus() {
        return this.portsStatus;
    }

    public void setPortsStatus(HashMap<String, PortStatus> portsStatus2) {
        this.portsStatus = portsStatus2;
    }

    public WORK_MODE getWorkMode() {
        return this.workMode;
    }

    public void setWorkMode(WORK_MODE workMode2) {
        this.workMode = workMode2;
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

    public AMP_DISTR_POLICY getAmpDistrPolicy() {
        return this.ampDistrPolicy;
    }

    public void setAmpDistrPolicy(AMP_DISTR_POLICY ampDistrPolicy2) {
        this.ampDistrPolicy = ampDistrPolicy2;
    }

    public ChargeFullRequirement getChargeFullRequirement() {
        return this.chargeFullRequirement;
    }

    public void setChargeFullRequirement(ChargeFullRequirement chargeFullRequirement2) {
        this.chargeFullRequirement = chargeFullRequirement2;
    }

    public TimerSetting getTimerSetting() {
        return this.timerSetting;
    }

    public void setTimerSetting(TimerSetting timerSetting2) {
        this.timerSetting = timerSetting2;
    }

    public HashMap<String, FeeRate> getPortsFeeRate() {
        return this.portsFeeRate;
    }

    public void setPortsFeeRate(HashMap<String, FeeRate> portsFeeRate2) {
        this.portsFeeRate = portsFeeRate2;
    }

    public boolean isCloudConnected() {
        return this.cloudConnected;
    }

    public void setCloudConnected(boolean cloudConnected2) {
        this.cloudConnected = cloudConnected2;
    }

    public boolean isCloudTimeSynch() {
        return this.cloudTimeSynch;
    }

    public void setCloudTimeSynch(boolean cloudTimeSynch2) {
        this.cloudTimeSynch = cloudTimeSynch2;
    }

    public boolean isAdvertEnable() {
        return this.advertEnable;
    }

    public void setAdvertEnable(boolean advertEnable2) {
        this.advertEnable = advertEnable2;
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
}
