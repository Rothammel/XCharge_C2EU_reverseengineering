package com.xcharge.charger.device.p005c2.bean;

import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

/* renamed from: com.xcharge.charger.device.c2.bean.DeviceBasicInfoData */
public class DeviceBasicInfoData extends JsonBean<DeviceBasicInfoData> {
    public static final String EVENT_GETINFO = "E@Info";
    private String CurrentMax = null;
    private String PID = null;
    private String PUK = null;
    private String PileType = null;

    /* renamed from: SN */
    private String f70SN = null;
    private ArrayList<String> ports = null;

    public String getSN() {
        return this.f70SN;
    }

    public void setSN(String sN) {
        this.f70SN = sN;
    }

    public String getPUK() {
        return this.PUK;
    }

    public void setPUK(String pUK) {
        this.PUK = pUK;
    }

    public String getPID() {
        return this.PID;
    }

    public void setPID(String pID) {
        this.PID = pID;
    }

    public String getPileType() {
        return this.PileType;
    }

    public void setPileType(String pileType) {
        this.PileType = pileType;
    }

    public String getCurrentMax() {
        return this.CurrentMax;
    }

    public void setCurrentMax(String currentMax) {
        this.CurrentMax = currentMax;
    }

    public ArrayList<String> getPorts() {
        return this.ports;
    }

    public void setPorts(ArrayList<String> ports2) {
        this.ports = ports2;
    }
}
