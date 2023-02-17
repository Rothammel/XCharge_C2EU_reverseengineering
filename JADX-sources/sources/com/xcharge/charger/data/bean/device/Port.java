package com.xcharge.charger.data.bean.device;

import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.status.ParkStatus;
import com.xcharge.charger.data.bean.type.DEVICE_STATUS;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;
import java.util.HashMap;

/* loaded from: classes.dex */
public class Port extends JsonBean<Port> {
    private String port = null;
    private NFC nfcStatus = new NFC();
    private GunLock gunLock = new GunLock();
    private Radar radar = new Radar();
    private EmergencyStop emergencyStop = new EmergencyStop();
    private boolean isPlugin = false;
    private ParkStatus parkStatus = new ParkStatus();
    private HashMap<String, ErrorCode> allDeviceErrorCache = new HashMap<>();
    private ErrorCode deviceError = new ErrorCode(200);
    private ArrayList<Double> amps = null;
    private ArrayList<Double> volts = null;
    private Double LeakAmp = null;
    private Double chipTemp = null;
    private DEVICE_STATUS portRuntimeStatus = null;
    private Double avgAmp = null;
    private Double kwatt = null;
    private Double meter = null;
    private Integer cpVoltage = null;
    private Integer adjustAmp = null;

    public String getPort() {
        return this.port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public NFC getNfcStatus() {
        return this.nfcStatus;
    }

    public void setNfcStatus(NFC nfcStatus) {
        this.nfcStatus = nfcStatus;
    }

    public GunLock getGunLock() {
        return this.gunLock;
    }

    public void setGunLock(GunLock gunLock) {
        this.gunLock = gunLock;
    }

    public Radar getRadar() {
        return this.radar;
    }

    public void setRadar(Radar radar) {
        this.radar = radar;
    }

    public EmergencyStop getEmergencyStop() {
        return this.emergencyStop;
    }

    public void setEmergencyStop(EmergencyStop emergencyStop) {
        this.emergencyStop = emergencyStop;
    }

    public boolean isPlugin() {
        return this.isPlugin;
    }

    public void setPlugin(boolean isPlugin) {
        this.isPlugin = isPlugin;
    }

    public ParkStatus getParkStatus() {
        return this.parkStatus;
    }

    public void setParkStatus(ParkStatus parkStatus) {
        this.parkStatus = parkStatus;
    }

    public ErrorCode getDeviceError() {
        return this.deviceError;
    }

    public void setDeviceError(ErrorCode deviceError) {
        this.deviceError = deviceError;
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

    public Double getChipTemp() {
        return this.chipTemp;
    }

    public void setChipTemp(Double chipTemp) {
        this.chipTemp = chipTemp;
    }

    public DEVICE_STATUS getPortRuntimeStatus() {
        return this.portRuntimeStatus;
    }

    public void setPortRuntimeStatus(DEVICE_STATUS portRuntimeStatus) {
        this.portRuntimeStatus = portRuntimeStatus;
    }

    public Double getAvgAmp() {
        return this.avgAmp;
    }

    public void setAvgAmp(Double avgAmp) {
        this.avgAmp = avgAmp;
    }

    public Double getKwatt() {
        return this.kwatt;
    }

    public void setKwatt(Double kwatt) {
        this.kwatt = kwatt;
    }

    public Double getMeter() {
        return this.meter;
    }

    public void setMeter(Double meter) {
        this.meter = meter;
    }

    public Integer getCpVoltage() {
        return this.cpVoltage;
    }

    public void setCpVoltage(Integer cpVoltage) {
        this.cpVoltage = cpVoltage;
    }

    public HashMap<String, ErrorCode> getAllDeviceErrorCache() {
        return this.allDeviceErrorCache;
    }

    public void setAllDeviceErrorCache(HashMap<String, ErrorCode> allDeviceErrorCache) {
        this.allDeviceErrorCache = allDeviceErrorCache;
    }

    public Integer getAdjustAmp() {
        return this.adjustAmp;
    }

    public void setAdjustAmp(Integer adjustAmp) {
        this.adjustAmp = adjustAmp;
    }

    /* renamed from: clone */
    public Port m9clone() {
        return deepClone();
    }
}
