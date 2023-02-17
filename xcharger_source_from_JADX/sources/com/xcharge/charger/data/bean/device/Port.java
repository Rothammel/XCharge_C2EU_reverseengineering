package com.xcharge.charger.data.bean.device;

import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.status.ParkStatus;
import com.xcharge.charger.data.bean.type.DEVICE_STATUS;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;
import java.util.HashMap;

public class Port extends JsonBean<Port> {
    private Double LeakAmp = null;
    private Integer adjustAmp = null;
    private HashMap<String, ErrorCode> allDeviceErrorCache = new HashMap<>();
    private ArrayList<Double> amps = null;
    private Double avgAmp = null;
    private Double chipTemp = null;
    private Integer cpVoltage = null;
    private ErrorCode deviceError = new ErrorCode(200);
    private EmergencyStop emergencyStop = new EmergencyStop();
    private GunLock gunLock = new GunLock();
    private boolean isPlugin = false;
    private Double kwatt = null;
    private Double meter = null;
    private NFC nfcStatus = new NFC();
    private ParkStatus parkStatus = new ParkStatus();
    private String port = null;
    private DEVICE_STATUS portRuntimeStatus = null;
    private Radar radar = new Radar();
    private ArrayList<Double> volts = null;

    public String getPort() {
        return this.port;
    }

    public void setPort(String port2) {
        this.port = port2;
    }

    public NFC getNfcStatus() {
        return this.nfcStatus;
    }

    public void setNfcStatus(NFC nfcStatus2) {
        this.nfcStatus = nfcStatus2;
    }

    public GunLock getGunLock() {
        return this.gunLock;
    }

    public void setGunLock(GunLock gunLock2) {
        this.gunLock = gunLock2;
    }

    public Radar getRadar() {
        return this.radar;
    }

    public void setRadar(Radar radar2) {
        this.radar = radar2;
    }

    public EmergencyStop getEmergencyStop() {
        return this.emergencyStop;
    }

    public void setEmergencyStop(EmergencyStop emergencyStop2) {
        this.emergencyStop = emergencyStop2;
    }

    public boolean isPlugin() {
        return this.isPlugin;
    }

    public void setPlugin(boolean isPlugin2) {
        this.isPlugin = isPlugin2;
    }

    public ParkStatus getParkStatus() {
        return this.parkStatus;
    }

    public void setParkStatus(ParkStatus parkStatus2) {
        this.parkStatus = parkStatus2;
    }

    public ErrorCode getDeviceError() {
        return this.deviceError;
    }

    public void setDeviceError(ErrorCode deviceError2) {
        this.deviceError = deviceError2;
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

    public Double getChipTemp() {
        return this.chipTemp;
    }

    public void setChipTemp(Double chipTemp2) {
        this.chipTemp = chipTemp2;
    }

    public DEVICE_STATUS getPortRuntimeStatus() {
        return this.portRuntimeStatus;
    }

    public void setPortRuntimeStatus(DEVICE_STATUS portRuntimeStatus2) {
        this.portRuntimeStatus = portRuntimeStatus2;
    }

    public Double getAvgAmp() {
        return this.avgAmp;
    }

    public void setAvgAmp(Double avgAmp2) {
        this.avgAmp = avgAmp2;
    }

    public Double getKwatt() {
        return this.kwatt;
    }

    public void setKwatt(Double kwatt2) {
        this.kwatt = kwatt2;
    }

    public Double getMeter() {
        return this.meter;
    }

    public void setMeter(Double meter2) {
        this.meter = meter2;
    }

    public Integer getCpVoltage() {
        return this.cpVoltage;
    }

    public void setCpVoltage(Integer cpVoltage2) {
        this.cpVoltage = cpVoltage2;
    }

    public HashMap<String, ErrorCode> getAllDeviceErrorCache() {
        return this.allDeviceErrorCache;
    }

    public void setAllDeviceErrorCache(HashMap<String, ErrorCode> allDeviceErrorCache2) {
        this.allDeviceErrorCache = allDeviceErrorCache2;
    }

    public Integer getAdjustAmp() {
        return this.adjustAmp;
    }

    public void setAdjustAmp(Integer adjustAmp2) {
        this.adjustAmp = adjustAmp2;
    }

    public Port clone() {
        return (Port) deepClone();
    }
}
