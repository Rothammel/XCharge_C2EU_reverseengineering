package com.xcharge.charger.data.bean.device;

import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.type.PHASE;
import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

public class Hardware extends JsonBean<Hardware> {
    private double ampCapacity = 32.0d;
    private Beep beep = null;
    private BLN bln = null;
    private long bootTimestamp = 0;
    private ErrorCode deviceError = new ErrorCode(200);
    private String hwVersion = null;
    private Network network = new Network();
    private PHASE phase = PHASE.UNKOWN_PHASE;
    private String pid = null;
    private HashMap<String, Port> ports = null;
    private Rtc rtc = null;
    private Screen screen = null;
    private String signatureCode = null;

    /* renamed from: sn */
    String f51sn = null;
    private long workTime = 0;

    public String getSn() {
        return this.f51sn;
    }

    public void setSn(String sn) {
        this.f51sn = sn;
    }

    public Network getNetwork() {
        return this.network;
    }

    public void setNetwork(Network network2) {
        this.network = network2;
    }

    public Screen getScreen() {
        return this.screen;
    }

    public void setScreen(Screen screen2) {
        this.screen = screen2;
    }

    public HashMap<String, Port> getPorts() {
        return this.ports;
    }

    public void setPorts(HashMap<String, Port> ports2) {
        this.ports = ports2;
    }

    public String getHwVersion() {
        return this.hwVersion;
    }

    public void setHwVersion(String hwVersion2) {
        this.hwVersion = hwVersion2;
    }

    public Rtc getRtc() {
        return this.rtc;
    }

    public void setRtc(Rtc rtc2) {
        this.rtc = rtc2;
    }

    public Beep getBeep() {
        return this.beep;
    }

    public void setBeep(Beep beep2) {
        this.beep = beep2;
    }

    public BLN getBln() {
        return this.bln;
    }

    public void setBln(BLN bln2) {
        this.bln = bln2;
    }

    public long getBootTimestamp() {
        return this.bootTimestamp;
    }

    public void setBootTimestamp(long bootTimestamp2) {
        this.bootTimestamp = bootTimestamp2;
    }

    public long getWorkTime() {
        return this.workTime;
    }

    public void setWorkTime(long workTime2) {
        this.workTime = workTime2;
    }

    public PHASE getPhase() {
        return this.phase;
    }

    public void setPhase(PHASE phase2) {
        this.phase = phase2;
    }

    public double getAmpCapacity() {
        return this.ampCapacity;
    }

    public void setAmpCapacity(double ampCapacity2) {
        this.ampCapacity = ampCapacity2;
    }

    public String getPid() {
        return this.pid;
    }

    public void setPid(String pid2) {
        this.pid = pid2;
    }

    public String getSignatureCode() {
        return this.signatureCode;
    }

    public void setSignatureCode(String signatureCode2) {
        this.signatureCode = signatureCode2;
    }

    public ErrorCode getDeviceError() {
        return this.deviceError;
    }

    public void setDeviceError(ErrorCode deviceError2) {
        this.deviceError = deviceError2;
    }
}
