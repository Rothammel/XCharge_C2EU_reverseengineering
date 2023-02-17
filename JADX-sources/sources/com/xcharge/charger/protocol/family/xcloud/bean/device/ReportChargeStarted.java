package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ReportChargeStarted extends JsonBean<ReportChargeStarted> {
    private Long sid = null;
    private long billId = 0;
    private String vId = null;
    private String bId = null;
    private Double bCap = null;
    private Double bVol = null;
    private String commVer = null;
    private long time = 0;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }

    public long getBillId() {
        return this.billId;
    }

    public void setBillId(long billId) {
        this.billId = billId;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getvId() {
        return this.vId;
    }

    public void setvId(String vId) {
        this.vId = vId;
    }

    public String getbId() {
        return this.bId;
    }

    public void setbId(String bId) {
        this.bId = bId;
    }

    public Double getbCap() {
        return this.bCap;
    }

    public void setbCap(Double bCap) {
        this.bCap = bCap;
    }

    public Double getbVol() {
        return this.bVol;
    }

    public void setbVol(Double bVol) {
        this.bVol = bVol;
    }

    public String getCommVer() {
        return this.commVer;
    }

    public void setCommVer(String commVer) {
        this.commVer = commVer;
    }
}
