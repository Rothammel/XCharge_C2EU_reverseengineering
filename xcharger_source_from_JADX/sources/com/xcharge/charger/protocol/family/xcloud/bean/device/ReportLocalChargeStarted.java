package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.common.bean.JsonBean;

public class ReportLocalChargeStarted extends JsonBean<ReportLocalChargeStarted> {
    private Double bCap = null;
    private String bId = null;
    private Double bVol = null;
    private String cardSourceId = null;
    private String commVer = null;
    private int port = 1;
    private Long sid = null;
    private long time = 0;
    private String vId = null;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid2) {
        this.sid = sid2;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port2) {
        this.port = port2;
    }

    public String getCardSourceId() {
        return this.cardSourceId;
    }

    public void setCardSourceId(String cardSourceId2) {
        this.cardSourceId = cardSourceId2;
    }

    public String getvId() {
        return this.vId;
    }

    public void setvId(String vId2) {
        this.vId = vId2;
    }

    public String getbId() {
        return this.bId;
    }

    public void setbId(String bId2) {
        this.bId = bId2;
    }

    public Double getbCap() {
        return this.bCap;
    }

    public void setbCap(Double bCap2) {
        this.bCap = bCap2;
    }

    public Double getbVol() {
        return this.bVol;
    }

    public void setbVol(Double bVol2) {
        this.bVol = bVol2;
    }

    public String getCommVer() {
        return this.commVer;
    }

    public void setCommVer(String commVer2) {
        this.commVer = commVer2;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }
}
