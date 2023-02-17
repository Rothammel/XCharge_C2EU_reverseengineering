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

/* loaded from: classes.dex */
public class PortStatus extends JsonBean<PortStatus> {
    public static final String KEY_ERROR_CP_EXCEPT = "CPVoltage";
    public static final String KEY_ERROR_EMERGENCY_STOP = "EmergencyStop";
    public static final String KEY_ERROR_LEAKAGE_CURRENT = "LeakageCurrent";
    public static final String KEY_ERROR_NO_GROUD = "EarthWire";
    public static final String KEY_ERROR_OVER_CURRENT = "OverCurrent";
    public static final String KEY_ERROR_OVER_TEMPERATURE = "OverTemperature";
    public static final String KEY_ERROR_OVER_VOLTAGE = "OverVoltage";
    private boolean enable = true;
    private LOCK_STATUS gunLockStatus = LOCK_STATUS.disable;
    private GUN_LOCK_MODE gunLockMode = GUN_LOCK_MODE.disable;
    private SWITCH_STATUS emergencyStopStatus = SWITCH_STATUS.off;
    private DEVICE_STATUS portRuntimeStatus = null;
    private Double cp = null;
    private ArrayList<HashMap<String, Integer>> errorCnt = null;
    private boolean isPlugin = false;
    private String qrcodeContent = null;
    private ArrayList<Double> amps = null;
    private ArrayList<Double> volts = null;
    private Double LeakAmp = null;
    private Integer cpVoltage = null;
    private Double ammeter = null;
    private int ampPercent = 0;
    private CHARGE_STATUS chargeStatus = CHARGE_STATUS.IDLE;
    private OCPP_CHARGE_STATUS ocppChargeStatus = OCPP_CHARGE_STATUS.SESSION_ENDED;
    private CHARGE_MODE chargeMode = null;
    private String charge_id = null;
    private int waitPluginTimeout = 0;
    private int waitPlugoutTimeout = 0;
    private Double kwatt = null;
    private Double temprature = null;
    private Double power = null;
    private double totalFee = 0.0d;
    private double delayPrice = 0.0d;
    private long delayStartTime = 0;
    private int totalDelayFee = 0;
    private long chargeStartTime = 0;
    private long chargeStopTime = 0;
    private CHARGE_STOP_CAUSE chargeStopCause = null;

    public boolean isEnable() {
        return this.enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public LOCK_STATUS getGunLockStatus() {
        return this.gunLockStatus;
    }

    public void setGunLockStatus(LOCK_STATUS gunLockStatus) {
        this.gunLockStatus = gunLockStatus;
    }

    public GUN_LOCK_MODE getGunLockMode() {
        return this.gunLockMode;
    }

    public void setGunLockMode(GUN_LOCK_MODE gunLockMode) {
        this.gunLockMode = gunLockMode;
    }

    public SWITCH_STATUS getEmergencyStopStatus() {
        return this.emergencyStopStatus;
    }

    public DEVICE_STATUS getPortRuntimeStatus() {
        return this.portRuntimeStatus;
    }

    public void setPortRuntimeStatus(DEVICE_STATUS portRuntimeStatus) {
        this.portRuntimeStatus = portRuntimeStatus;
    }

    public void setEmergencyStopStatus(SWITCH_STATUS emergencyStopStatus) {
        this.emergencyStopStatus = emergencyStopStatus;
    }

    public ArrayList<Double> getAmps() {
        return this.amps;
    }

    public void setAmps(ArrayList<Double> amps) {
        this.amps = amps;
    }

    public ArrayList<Double> getVolts() {
        return this.volts;
    }

    public void setVolts(ArrayList<Double> volts) {
        this.volts = volts;
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

    public void setCpVoltage(Integer cpVoltage) {
        this.cpVoltage = cpVoltage;
    }

    public Double getAmmeter() {
        return this.ammeter;
    }

    public void setAmmeter(Double ammeter) {
        this.ammeter = ammeter;
    }

    public int getAmpPercent() {
        return this.ampPercent;
    }

    public void setAmpPercent(int ampPercent) {
        this.ampPercent = ampPercent;
    }

    public CHARGE_STATUS getChargeStatus() {
        return this.chargeStatus;
    }

    public void setChargeStatus(CHARGE_STATUS chargeStatus) {
        this.chargeStatus = chargeStatus;
    }

    public OCPP_CHARGE_STATUS getOcppChargeStatus() {
        return this.ocppChargeStatus;
    }

    public void setOcppChargeStatus(OCPP_CHARGE_STATUS ocppChargeStatus) {
        this.ocppChargeStatus = ocppChargeStatus;
    }

    public CHARGE_MODE getChargeMode() {
        return this.chargeMode;
    }

    public void setChargeMode(CHARGE_MODE chargeMode) {
        this.chargeMode = chargeMode;
    }

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id) {
        this.charge_id = charge_id;
    }

    public Double getKwatt() {
        return this.kwatt;
    }

    public void setKwatt(Double kwatt) {
        this.kwatt = kwatt;
    }

    public Double getTemprature() {
        return this.temprature;
    }

    public void setTemprature(Double temprature) {
        this.temprature = temprature;
    }

    public Double getPower() {
        return this.power;
    }

    public void setPower(Double power) {
        this.power = power;
    }

    public boolean isPlugin() {
        return this.isPlugin;
    }

    public void setPlugin(boolean isPlugin) {
        this.isPlugin = isPlugin;
    }

    public double getTotalFee() {
        return this.totalFee;
    }

    public void setTotalFee(double totalFee) {
        this.totalFee = totalFee;
    }

    public double getDelayPrice() {
        return this.delayPrice;
    }

    public void setDelayPrice(double delayPrice) {
        this.delayPrice = delayPrice;
    }

    public long getDelayStartTime() {
        return this.delayStartTime;
    }

    public void setDelayStartTime(long delayStartTime) {
        this.delayStartTime = delayStartTime;
    }

    public int getTotalDelayFee() {
        return this.totalDelayFee;
    }

    public void setTotalDelayFee(int totalDelayFee) {
        this.totalDelayFee = totalDelayFee;
    }

    public long getChargeStartTime() {
        return this.chargeStartTime;
    }

    public void setChargeStartTime(long chargeStartTime) {
        this.chargeStartTime = chargeStartTime;
    }

    public long getChargeStopTime() {
        return this.chargeStopTime;
    }

    public void setChargeStopTime(long chargeStopTime) {
        this.chargeStopTime = chargeStopTime;
    }

    public CHARGE_STOP_CAUSE getChargeStopCause() {
        return this.chargeStopCause;
    }

    public void setChargeStopCause(CHARGE_STOP_CAUSE chargeStopCause) {
        this.chargeStopCause = chargeStopCause;
    }

    public String getQrcodeContent() {
        return this.qrcodeContent;
    }

    public void setQrcodeContent(String qrcodeContent) {
        this.qrcodeContent = qrcodeContent;
    }

    public int getWaitPluginTimeout() {
        return this.waitPluginTimeout;
    }

    public void setWaitPluginTimeout(int waitPluginTimeout) {
        this.waitPluginTimeout = waitPluginTimeout;
    }

    public int getWaitPlugoutTimeout() {
        return this.waitPlugoutTimeout;
    }

    public void setWaitPlugoutTimeout(int waitPlugoutTimeout) {
        this.waitPlugoutTimeout = waitPlugoutTimeout;
    }

    public Double getCp() {
        return this.cp;
    }

    public void setCp(Double cp) {
        this.cp = cp;
    }

    public ArrayList<HashMap<String, Integer>> getErrorCnt() {
        return this.errorCnt;
    }

    public void setErrorCnt(ArrayList<HashMap<String, Integer>> errorCnt) {
        this.errorCnt = errorCnt;
    }

    /* renamed from: clone */
    public PortStatus m11clone() {
        return deepClone();
    }
}
