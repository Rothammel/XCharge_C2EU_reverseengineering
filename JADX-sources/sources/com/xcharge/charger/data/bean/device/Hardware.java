package com.xcharge.charger.data.bean.device;

import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.type.PHASE;
import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

/* loaded from: classes.dex */
public class Hardware extends JsonBean<Hardware> {
    private String hwVersion = null;
    private String signatureCode = null;
    private String pid = null;
    String sn = null;
    private PHASE phase = PHASE.UNKOWN_PHASE;
    private double ampCapacity = 32.0d;
    private Network network = new Network();
    private Screen screen = null;
    private Rtc rtc = null;
    private Beep beep = null;
    private BLN bln = null;
    private ErrorCode deviceError = new ErrorCode(200);
    private HashMap<String, Port> ports = null;
    private long bootTimestamp = 0;
    private long workTime = 0;

    public String getSn() {
        return this.sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public Network getNetwork() {
        return this.network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public Screen getScreen() {
        return this.screen;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    public HashMap<String, Port> getPorts() {
        return this.ports;
    }

    public void setPorts(HashMap<String, Port> ports) {
        this.ports = ports;
    }

    public String getHwVersion() {
        return this.hwVersion;
    }

    public void setHwVersion(String hwVersion) {
        this.hwVersion = hwVersion;
    }

    public Rtc getRtc() {
        return this.rtc;
    }

    public void setRtc(Rtc rtc) {
        this.rtc = rtc;
    }

    public Beep getBeep() {
        return this.beep;
    }

    public void setBeep(Beep beep) {
        this.beep = beep;
    }

    public BLN getBln() {
        return this.bln;
    }

    public void setBln(BLN bln) {
        this.bln = bln;
    }

    public long getBootTimestamp() {
        return this.bootTimestamp;
    }

    public void setBootTimestamp(long bootTimestamp) {
        this.bootTimestamp = bootTimestamp;
    }

    public long getWorkTime() {
        return this.workTime;
    }

    public void setWorkTime(long workTime) {
        this.workTime = workTime;
    }

    public PHASE getPhase() {
        return this.phase;
    }

    public void setPhase(PHASE phase) {
        this.phase = phase;
    }

    public double getAmpCapacity() {
        return this.ampCapacity;
    }

    public void setAmpCapacity(double ampCapacity) {
        this.ampCapacity = ampCapacity;
    }

    public String getPid() {
        return this.pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getSignatureCode() {
        return this.signatureCode;
    }

    public void setSignatureCode(String signatureCode) {
        this.signatureCode = signatureCode;
    }

    public ErrorCode getDeviceError() {
        return this.deviceError;
    }

    public void setDeviceError(ErrorCode deviceError) {
        this.deviceError = deviceError;
    }
}
