package com.xcharge.charger.protocol.monitor.bean.ddap;

import com.xcharge.common.bean.JsonBean;

public class DDAPSubnode extends JsonBean<DDAPSubnode> {
    private String app = null;
    private String device_id = null;
    private String firmware = null;
    private Boolean online = null;

    /* renamed from: os */
    private String f105os = null;
    private String pid = null;

    /* renamed from: sn */
    private String f106sn = null;
    private String uuid = null;
    private String vid = null;

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
        return this.f106sn;
    }

    public void setSn(String sn) {
        this.f106sn = sn;
    }

    public Boolean getOnline() {
        return this.online;
    }

    public void setOnline(Boolean online2) {
        this.online = online2;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid2) {
        this.uuid = uuid2;
    }

    public String getOs() {
        return this.f105os;
    }

    public void setOs(String os) {
        this.f105os = os;
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

    public String getDevice_id() {
        return this.device_id;
    }

    public void setDevice_id(String device_id2) {
        this.device_id = device_id2;
    }
}
