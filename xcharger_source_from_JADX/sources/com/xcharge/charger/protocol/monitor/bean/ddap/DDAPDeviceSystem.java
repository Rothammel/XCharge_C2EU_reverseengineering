package com.xcharge.charger.protocol.monitor.bean.ddap;

import com.xcharge.common.bean.JsonBean;

public class DDAPDeviceSystem extends JsonBean<DDAPDeviceSystem> {
    public static final String PID_C2 = "C2";
    public static final String TYPE_GATEWAY = "gateway";
    public static final String TYPE_REPEATER = "repeater";
    public static final String TYPE_TERMINAL = "terminal";
    public static final String VID_YZXTECH = "net.yzxtech";
    private String app = null;
    private String firmware = null;

    /* renamed from: os */
    private String f99os = null;
    private String pid = null;

    /* renamed from: sn */
    private String f100sn = null;
    private String type = null;
    private String uuid = null;
    private String vid = null;

    public String getType() {
        return this.type;
    }

    public void setType(String type2) {
        this.type = type2;
    }

    public String getVid() {
        return this.vid;
    }

    public void setVid(String vid2) {
        this.vid = vid2;
    }

    public String getPid() {
        return this.pid;
    }

    public void setPid(String pid2) {
        this.pid = pid2;
    }

    public String getSn() {
        return this.f100sn;
    }

    public void setSn(String sn) {
        this.f100sn = sn;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid2) {
        this.uuid = uuid2;
    }

    public String getOs() {
        return this.f99os;
    }

    public void setOs(String os) {
        this.f99os = os;
    }

    public String getFirmware() {
        return this.firmware;
    }

    public void setFirmware(String firmware2) {
        this.firmware = firmware2;
    }

    public String getApp() {
        return this.app;
    }

    public void setApp(String app2) {
        this.app = app2;
    }
}
