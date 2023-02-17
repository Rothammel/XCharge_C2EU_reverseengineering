package com.xcharge.charger.device.p005c2.bean;

import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.CHARGE_MODE;
import com.xcharge.charger.data.bean.type.DEVICE_STATUS;
import com.xcharge.charger.data.bean.type.PARK_STATUS;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/* renamed from: com.xcharge.charger.device.c2.bean.PortRuntimeData */
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

    /* renamed from: CP */
    private Double f71CP = null;
    private String SnapShot = null;
    private Double adcVoltage = null;
    private Double chipTemp = null;
    private Integer cpVoltage = null;
    private Double current = null;
    private Double currentA = null;
    private Double currentB = null;
    private Double currentC = null;
    private Integer currentMax = null;
    private Double currentN = null;
    private Integer currentUsed = null;
    private Double energy = null;
    private ArrayList<HashMap<String, Integer>> errorStop = null;
    private Boolean isRadarCalibrated = null;
    private Integer mode = null;
    private String msg = null;
    private PARK_STATUS parkStatus = null;
    private Integer pileType = null;
    private String port = "1";
    private Double power = null;
    private Integer rader = null;
    private Integer raderCalibration = null;
    private Integer runstatus = null;
    private Integer status = null;
    private Double voltageA = null;
    private Double voltageB = null;
    private Double voltageC = null;

    public Integer getRunstatus() {
        return this.runstatus;
    }

    public void setRunstatus(Integer runstatus2) {
        this.runstatus = runstatus2;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status2) {
        this.status = status2;
    }

    public Double getCurrent() {
        return this.current;
    }

    public void setCurrent(Double current2) {
        this.current = current2;
    }

    public Double getPower() {
        return this.power;
    }

    public void setPower(Double power2) {
        this.power = power2;
    }

    public Double getEnergy() {
        return this.energy;
    }

    public void setEnergy(Double energy2) {
        this.energy = energy2;
    }

    public Double getCP() {
        return this.f71CP;
    }

    public void setCP(Double cP) {
        this.f71CP = cP;
    }

    public Integer getRader() {
        return this.rader;
    }

    public void setRader(Integer rader2) {
        this.rader = rader2;
    }

    public Boolean getIsRadarCalibrated() {
        return this.isRadarCalibrated;
    }

    public void setIsRadarCalibrated(Boolean isRadarCalibrated2) {
        this.isRadarCalibrated = isRadarCalibrated2;
    }

    public Integer getPileType() {
        return this.pileType;
    }

    public void setPileType(Integer pileType2) {
        this.pileType = pileType2;
    }

    public Double getVoltageA() {
        return this.voltageA;
    }

    public void setVoltageA(Double voltageA2) {
        this.voltageA = voltageA2;
    }

    public Double getVoltageB() {
        return this.voltageB;
    }

    public void setVoltageB(Double voltageB2) {
        this.voltageB = voltageB2;
    }

    public Double getVoltageC() {
        return this.voltageC;
    }

    public void setVoltageC(Double voltageC2) {
        this.voltageC = voltageC2;
    }

    public Double getCurrentA() {
        return this.currentA;
    }

    public void setCurrentA(Double currentA2) {
        this.currentA = currentA2;
    }

    public Double getCurrentB() {
        return this.currentB;
    }

    public void setCurrentB(Double currentB2) {
        this.currentB = currentB2;
    }

    public Double getCurrentC() {
        return this.currentC;
    }

    public void setCurrentC(Double currentC2) {
        this.currentC = currentC2;
    }

    public Double getCurrentN() {
        return this.currentN;
    }

    public void setCurrentN(Double currentN2) {
        this.currentN = currentN2;
    }

    public Double getAdcVoltage() {
        return this.adcVoltage;
    }

    public void setAdcVoltage(Double adcVoltage2) {
        this.adcVoltage = adcVoltage2;
    }

    public Integer getRaderCalibration() {
        return this.raderCalibration;
    }

    public void setRaderCalibration(Integer raderCalibration2) {
        this.raderCalibration = raderCalibration2;
    }

    public Integer getCurrentMax() {
        return this.currentMax;
    }

    public void setCurrentMax(Integer currentMax2) {
        this.currentMax = currentMax2;
    }

    public Integer getCurrentUsed() {
        return this.currentUsed;
    }

    public void setCurrentUsed(Integer currentUsed2) {
        this.currentUsed = currentUsed2;
    }

    public Integer getCpVoltage() {
        return this.cpVoltage;
    }

    public void setCpVoltage(Integer cpVoltage2) {
        this.cpVoltage = cpVoltage2;
    }

    public Double getChipTemp() {
        return this.chipTemp;
    }

    public void setChipTemp(Double chipTemp2) {
        this.chipTemp = chipTemp2;
    }

    public Integer getMode() {
        return this.mode;
    }

    public void setMode(Integer mode2) {
        this.mode = mode2;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg2) {
        this.msg = msg2;
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

    public void setErrorStop(ArrayList<HashMap<String, Integer>> errorStop2) {
        this.errorStop = errorStop2;
    }

    public PARK_STATUS getParkStatus() {
        return this.parkStatus;
    }

    public void setParkStatus(PARK_STATUS parkStatus2) {
        this.parkStatus = parkStatus2;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port2) {
        this.port = port2;
    }

    public PortStatus toPortStatus() {
        CHARGE_MODE charge_mode = null;
        PortStatus portStatus = new PortStatus();
        portStatus.setPortRuntimeStatus(getStatus() == null ? null : DEVICE_STATUS.valueBy(getStatus().intValue()));
        portStatus.setCp(getCP());
        portStatus.setErrorCnt(getErrorStop());
        portStatus.setAmps(new ArrayList(Arrays.asList(new Double[]{getCurrent(), getCurrentA(), getCurrentB(), getCurrentC()})));
        portStatus.setVolts(new ArrayList(Arrays.asList(new Double[]{getVoltageA(), getVoltageB(), getVoltageC()})));
        if (getMode() != null) {
            charge_mode = CHARGE_MODE.valueBy(getMode().intValue());
        }
        portStatus.setChargeMode(charge_mode);
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
        portStatus.setAmps(new ArrayList(Arrays.asList(new Double[]{getCurrentA(), getCurrentB(), getCurrentC()})));
        portStatus.setVolts(new ArrayList(Arrays.asList(new Double[]{getVoltageA(), getVoltageB(), getVoltageC()})));
        portStatus.setCpVoltage(getCpVoltage());
        portStatus.setMeter(getEnergy());
        portStatus.setKwatt(getPower());
        portStatus.setAdjustAmp(getCurrentUsed());
        return portStatus;
    }
}
