package com.xcharge.charger.device.c2.bean;

import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.CHARGE_MODE;
import com.xcharge.charger.data.bean.type.DEVICE_STATUS;
import com.xcharge.charger.data.bean.type.PARK_STATUS;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/* loaded from: classes.dex */
public class PortRuntimeData extends JsonBean<PortRuntimeData> {
    public static final String EVENT_AUTH_INVALID = "E@AuthInvalid";
    public static final String EVENT_AUTH_VALID = "E@AuthValid";
    public static final String EVENT_CHARGING_FULL = "E@ChargingFull";
    public static final String EVENT_CHARGING_START = "E@ChargingStart";
    public static final String EVENT_CHARGING_STOP = "E@ChargingStop";
    public static final String EVENT_PLUG_IN = "E@PlugIn";
    public static final String EVENT_PLUG_OUT = "E@PlugOut";
    public static final String EVENT_RESUME = "E@Resume";
    public static final String EVENT_SUSPEND = "E@Suspend";
    public static final String EVENT_UPDATE = "E@Update";
    public static final int RUNSTATUS_IDLE = 0;
    public static final int RUNSTATUS_USING = 1;
    public static final int STATUS_CHARGING = 4;
    public static final int STATUS_CHARGING_FULL = 5;
    public static final int STATUS_EX_10 = 10;
    public static final int STATUS_EX_11 = 11;
    public static final int STATUS_EX_12 = 12;
    public static final int STATUS_EX_13 = 13;
    public static final int STATUS_EX_14 = 14;
    public static final int STATUS_EX_15 = 15;
    public static final int STATUS_EX_16 = 16;
    public static final int STATUS_EX_17 = 17;
    public static final int STATUS_EX_18 = 18;
    public static final int STATUS_EX_CHARGING_MODE_ERROR = 1001;
    public static final int STATUS_EX_WARNING = 1000;
    public static final int STATUS_FREE = 0;
    public static final int STATUS_MAX = 18;
    public static final int STATUS_PLUG_OUT = 2;
    public static final int STATUS_PREPARE = 3;
    public static final int STATUS_SELF_CHECK = 1;
    public static final int STATUS_USER_STOP = 6;
    private Integer runstatus = null;
    private Integer status = null;
    private Double current = null;
    private Double power = null;
    private Double energy = null;
    private Double CP = null;
    private Integer rader = null;
    private Boolean isRadarCalibrated = null;
    private Integer pileType = null;
    private Double voltageA = null;
    private Double voltageB = null;
    private Double voltageC = null;
    private Double currentA = null;
    private Double currentB = null;
    private Double currentC = null;
    private Double currentN = null;
    private Double adcVoltage = null;
    private Integer raderCalibration = null;
    private Integer currentMax = null;
    private Integer currentUsed = null;
    private Integer cpVoltage = null;
    private Double chipTemp = null;
    private Integer mode = null;
    private String msg = null;
    private String SnapShot = null;
    private ArrayList<HashMap<String, Integer>> errorStop = null;
    private PARK_STATUS parkStatus = null;
    private String port = "1";

    public Integer getRunstatus() {
        return this.runstatus;
    }

    public void setRunstatus(Integer runstatus) {
        this.runstatus = runstatus;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Double getCurrent() {
        return this.current;
    }

    public void setCurrent(Double current) {
        this.current = current;
    }

    public Double getPower() {
        return this.power;
    }

    public void setPower(Double power) {
        this.power = power;
    }

    public Double getEnergy() {
        return this.energy;
    }

    public void setEnergy(Double energy) {
        this.energy = energy;
    }

    public Double getCP() {
        return this.CP;
    }

    public void setCP(Double cP) {
        this.CP = cP;
    }

    public Integer getRader() {
        return this.rader;
    }

    public void setRader(Integer rader) {
        this.rader = rader;
    }

    public Boolean getIsRadarCalibrated() {
        return this.isRadarCalibrated;
    }

    public void setIsRadarCalibrated(Boolean isRadarCalibrated) {
        this.isRadarCalibrated = isRadarCalibrated;
    }

    public Integer getPileType() {
        return this.pileType;
    }

    public void setPileType(Integer pileType) {
        this.pileType = pileType;
    }

    public Double getVoltageA() {
        return this.voltageA;
    }

    public void setVoltageA(Double voltageA) {
        this.voltageA = voltageA;
    }

    public Double getVoltageB() {
        return this.voltageB;
    }

    public void setVoltageB(Double voltageB) {
        this.voltageB = voltageB;
    }

    public Double getVoltageC() {
        return this.voltageC;
    }

    public void setVoltageC(Double voltageC) {
        this.voltageC = voltageC;
    }

    public Double getCurrentA() {
        return this.currentA;
    }

    public void setCurrentA(Double currentA) {
        this.currentA = currentA;
    }

    public Double getCurrentB() {
        return this.currentB;
    }

    public void setCurrentB(Double currentB) {
        this.currentB = currentB;
    }

    public Double getCurrentC() {
        return this.currentC;
    }

    public void setCurrentC(Double currentC) {
        this.currentC = currentC;
    }

    public Double getCurrentN() {
        return this.currentN;
    }

    public void setCurrentN(Double currentN) {
        this.currentN = currentN;
    }

    public Double getAdcVoltage() {
        return this.adcVoltage;
    }

    public void setAdcVoltage(Double adcVoltage) {
        this.adcVoltage = adcVoltage;
    }

    public Integer getRaderCalibration() {
        return this.raderCalibration;
    }

    public void setRaderCalibration(Integer raderCalibration) {
        this.raderCalibration = raderCalibration;
    }

    public Integer getCurrentMax() {
        return this.currentMax;
    }

    public void setCurrentMax(Integer currentMax) {
        this.currentMax = currentMax;
    }

    public Integer getCurrentUsed() {
        return this.currentUsed;
    }

    public void setCurrentUsed(Integer currentUsed) {
        this.currentUsed = currentUsed;
    }

    public Integer getCpVoltage() {
        return this.cpVoltage;
    }

    public void setCpVoltage(Integer cpVoltage) {
        this.cpVoltage = cpVoltage;
    }

    public Double getChipTemp() {
        return this.chipTemp;
    }

    public void setChipTemp(Double chipTemp) {
        this.chipTemp = chipTemp;
    }

    public Integer getMode() {
        return this.mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSnapShot() {
        return this.SnapShot;
    }

    public void setSnapShot(String snapShot) {
        this.SnapShot = snapShot;
    }

    public ArrayList<HashMap<String, Integer>> getErrorStop() {
        return this.errorStop;
    }

    public void setErrorStop(ArrayList<HashMap<String, Integer>> errorStop) {
        this.errorStop = errorStop;
    }

    public PARK_STATUS getParkStatus() {
        return this.parkStatus;
    }

    public void setParkStatus(PARK_STATUS parkStatus) {
        this.parkStatus = parkStatus;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public PortStatus toPortStatus() {
        PortStatus portStatus = new PortStatus();
        portStatus.setPortRuntimeStatus(getStatus() == null ? null : DEVICE_STATUS.valueBy(getStatus().intValue()));
        portStatus.setCp(getCP());
        portStatus.setErrorCnt(getErrorStop());
        portStatus.setAmps(new ArrayList<>(Arrays.asList(getCurrent(), getCurrentA(), getCurrentB(), getCurrentC())));
        portStatus.setVolts(new ArrayList<>(Arrays.asList(getVoltageA(), getVoltageB(), getVoltageC())));
        portStatus.setChargeMode(getMode() != null ? CHARGE_MODE.valueBy(getMode().intValue()) : null);
        portStatus.setPower(getEnergy());
        portStatus.setKwatt(getPower());
        portStatus.setTemprature(getChipTemp());
        portStatus.setLeakAmp(getCurrentN());
        portStatus.setCpVoltage(getCpVoltage());
        return portStatus;
    }

    public Port toPort() {
        Port portStatus = new Port();
        portStatus.setPort(getPort());
        portStatus.setPortRuntimeStatus(getStatus() == null ? null : DEVICE_STATUS.valueBy(getStatus().intValue()));
        portStatus.setLeakAmp(getCurrentN());
        portStatus.setChipTemp(getChipTemp());
        portStatus.getRadar().setDetectDist(getRader());
        portStatus.getRadar().setCalibrateDist(getRaderCalibration());
        portStatus.setAvgAmp(getCurrent());
        portStatus.setAmps(new ArrayList<>(Arrays.asList(getCurrentA(), getCurrentB(), getCurrentC())));
        portStatus.setVolts(new ArrayList<>(Arrays.asList(getVoltageA(), getVoltageB(), getVoltageC())));
        portStatus.setCpVoltage(getCpVoltage());
        portStatus.setMeter(getEnergy());
        portStatus.setKwatt(getPower());
        portStatus.setAdjustAmp(getCurrentUsed());
        return portStatus;
    }
}
