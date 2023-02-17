package com.xcharge.charger.data.bean.status;

import com.xcharge.charger.data.bean.type.CHARGE_MODE;
import com.xcharge.charger.data.bean.type.CHARGE_STATUS;
import com.xcharge.charger.data.bean.type.CHARGE_STOP_CAUSE;
import com.xcharge.charger.data.bean.type.DEVICE_STATUS;
import com.xcharge.charger.data.bean.type.GUN_LOCK_MODE;
import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.bean.type.OCPP_CHARGE_STATUS;
import com.xcharge.charger.data.bean.type.SWITCH_STATUS;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;
import java.util.HashMap;

public class PortStatus extends JsonBean<PortStatus> {
    public static final String KEY_ERROR_CP_EXCEPT = "CPVoltage";
    public static final String KEY_ERROR_EMERGENCY_STOP = "EmergencyStop";
    public static final String KEY_ERROR_LEAKAGE_CURRENT = "LeakageCurrent";
    public static final String KEY_ERROR_NO_GROUD = "EarthWire";
    public static final String KEY_ERROR_OVER_CURRENT = "OverCurrent";
    public static final String KEY_ERROR_OVER_TEMPERATURE = "OverTemperature";
    public static final String KEY_ERROR_OVER_VOLTAGE = "OverVoltage";
    private Double LeakAmp = null;
    private Double ammeter = null;
    private int ampPercent = 0;
    private ArrayList<Double> amps = null;
    private CHARGE_MODE chargeMode = null;
    private long chargeStartTime = 0;
    private CHARGE_STATUS chargeStatus = CHARGE_STATUS.IDLE;
    private CHARGE_STOP_CAUSE chargeStopCause = null;
    private long chargeStopTime = 0;
    private String charge_id = null;

    /* renamed from: cp */
    private Double f59cp = null;
    private Integer cpVoltage = null;
    private double delayPrice = 0.0d;
    private long delayStartTime = 0;
    private SWITCH_STATUS emergencyStopStatus = SWITCH_STATUS.off;
    private boolean enable = true;
    private ArrayList<HashMap<String, Integer>> errorCnt = null;
    private GUN_LOCK_MODE gunLockMode = GUN_LOCK_MODE.disable;
    private LOCK_STATUS gunLockStatus = LOCK_STATUS.disable;
    private boolean isPlugin = false;
    private Double kwatt = null;
    private OCPP_CHARGE_STATUS ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_ENDED;
    private DEVICE_STATUS portRuntimeStatus = null;
    private Double power = null;
    private String qrcodeContent = null;
    private Double temprature = null;
    private int totalDelayFee = 0;
    private double totalFee = 0.0d;
    private ArrayList<Double> volts = null;
    private int waitPluginTimeout = 0;
    private int waitPlugoutTimeout = 0;

    public boolean isEnable() {
        return this.enable;
    }

    public void setEnable(boolean enable2) {
        this.enable = enable2;
    }

    public LOCK_STATUS getGunLockStatus() {
        return this.gunLockStatus;
    }

    public void setGunLockStatus(LOCK_STATUS gunLockStatus2) {
        this.gunLockStatus = gunLockStatus2;
    }

    public GUN_LOCK_MODE getGunLockMode() {
        return this.gunLockMode;
    }

    public void setGunLockMode(GUN_LOCK_MODE gunLockMode2) {
        this.gunLockMode = gunLockMode2;
    }

    public SWITCH_STATUS getEmergencyStopStatus() {
        return this.emergencyStopStatus;
    }

    public DEVICE_STATUS getPortRuntimeStatus() {
        return this.portRuntimeStatus;
    }

    public void setPortRuntimeStatus(DEVICE_STATUS portRuntimeStatus2) {
        this.portRuntimeStatus = portRuntimeStatus2;
    }

    public void setEmergencyStopStatus(SWITCH_STATUS emergencyStopStatus2) {
        this.emergencyStopStatus = emergencyStopStatus2;
    }

    public ArrayList<Double> getAmps() {
        return this.amps;
    }

    public void setAmps(ArrayList<Double> amps2) {
        this.amps = amps2;
    }

    public ArrayList<Double> getVolts() {
        return this.volts;
    }

    public void setVolts(ArrayList<Double> volts2) {
        this.volts = volts2;
    }

    public Double getLeakAmp() {
        return this.LeakAmp;
    }

    public void setLeakAmp(Double leakAmp) {
        this.LeakAmp = leakAmp;
    }

    public Integer getCpVoltage() {
        return this.cpVoltage;
    }

    public void setCpVoltage(Integer cpVoltage2) {
        this.cpVoltage = cpVoltage2;
    }

    public Double getAmmeter() {
        return this.ammeter;
    }

    public void setAmmeter(Double ammeter2) {
        this.ammeter = ammeter2;
    }

    public int getAmpPercent() {
        return this.ampPercent;
    }

    public void setAmpPercent(int ampPercent2) {
        this.ampPercent = ampPercent2;
    }

    public CHARGE_STATUS getChargeStatus() {
        return this.chargeStatus;
    }

    public void setChargeStatus(CHARGE_STATUS chargeStatus2) {
        this.chargeStatus = chargeStatus2;
    }

    public OCPP_CHARGE_STATUS getOcppChargeStatus() {
        return this.ocppChargeStatus;
    }

    public void setOcppChargeStatus(OCPP_CHARGE_STATUS ocppChargeStatus2) {
        this.ocppChargeStatus = ocppChargeStatus2;
    }

    public CHARGE_MODE getChargeMode() {
        return this.chargeMode;
    }

    public void setChargeMode(CHARGE_MODE chargeMode2) {
        this.chargeMode = chargeMode2;
    }

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id2) {
        this.charge_id = charge_id2;
    }

    public Double getKwatt() {
        return this.kwatt;
    }

    public void setKwatt(Double kwatt2) {
        this.kwatt = kwatt2;
    }

    public Double getTemprature() {
        return this.temprature;
    }

    public void setTemprature(Double temprature2) {
        this.temprature = temprature2;
    }

    public Double getPower() {
        return this.power;
    }

    public void setPower(Double power2) {
        this.power = power2;
    }

    public boolean isPlugin() {
        return this.isPlugin;
    }

    public void setPlugin(boolean isPlugin2) {
        this.isPlugin = isPlugin2;
    }

    public double getTotalFee() {
        return this.totalFee;
    }

    public void setTotalFee(double totalFee2) {
        this.totalFee = totalFee2;
    }

    public double getDelayPrice() {
        return this.delayPrice;
    }

    public void setDelayPrice(double delayPrice2) {
        this.delayPrice = delayPrice2;
    }

    public long getDelayStartTime() {
        return this.delayStartTime;
    }

    public void setDelayStartTime(long delayStartTime2) {
        this.delayStartTime = delayStartTime2;
    }

    public int getTotalDelayFee() {
        return this.totalDelayFee;
    }

    public void setTotalDelayFee(int totalDelayFee2) {
        this.totalDelayFee = totalDelayFee2;
    }

    public long getChargeStartTime() {
        return this.chargeStartTime;
    }

    public void setChargeStartTime(long chargeStartTime2) {
        this.chargeStartTime = chargeStartTime2;
    }

    public long getChargeStopTime() {
        return this.chargeStopTime;
    }

    public void setChargeStopTime(long chargeStopTime2) {
        this.chargeStopTime = chargeStopTime2;
    }

    public CHARGE_STOP_CAUSE getChargeStopCause() {
        return this.chargeStopCause;
    }

    public void setChargeStopCause(CHARGE_STOP_CAUSE chargeStopCause2) {
        this.chargeStopCause = chargeStopCause2;
    }

    public String getQrcodeContent() {
        return this.qrcodeContent;
    }

    public void setQrcodeContent(String qrcodeContent2) {
        this.qrcodeContent = qrcodeContent2;
    }

    public int getWaitPluginTimeout() {
        return this.waitPluginTimeout;
    }

    public void setWaitPluginTimeout(int waitPluginTimeout2) {
        this.waitPluginTimeout = waitPluginTimeout2;
    }

    public int getWaitPlugoutTimeout() {
        return this.waitPlugoutTimeout;
    }

    public void setWaitPlugoutTimeout(int waitPlugoutTimeout2) {
        this.waitPlugoutTimeout = waitPlugoutTimeout2;
    }

    public Double getCp() {
        return this.f59cp;
    }

    public void setCp(Double cp) {
        this.f59cp = cp;
    }

    public ArrayList<HashMap<String, Integer>> getErrorCnt() {
        return this.errorCnt;
    }

    public void setErrorCnt(ArrayList<HashMap<String, Integer>> errorCnt2) {
        this.errorCnt = errorCnt2;
    }

    public PortStatus clone() {
        return (PortStatus) deepClone();
    }
}
