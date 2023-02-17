package com.xcharge.charger.protocol.monitor.bean.ddap;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class DDAPDeviceSystem extends JsonBean<DDAPDeviceSystem> {
    public static final String PID_C2 = "C2";
    public static final String TYPE_GATEWAY = "gateway";
    public static final String TYPE_REPEATER = "repeater";
    public static final String TYPE_TERMINAL = "terminal";
    public static final String VID_YZXTECH = "net.yzxtech";
    private String type = null;
    private String vid = null;
    private String pid = null;
    private String sn = null;
    private String uuid = null;
    private String os = null;
    private String firmware = null;
    private String app = null;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVid() {
        return this.vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public String getPid() {
        return this.pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getSn() {
        return this.sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getOs() {
        return this.os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getFirmware() {
        return this.firmware;
    }

    public void setFirmware(String firmware) {
        this.firmware = firmware;
    }

    public String getApp() {
        return this.app;
    }

    public void setApp(String app) {
        this.app = app;
    }
}
