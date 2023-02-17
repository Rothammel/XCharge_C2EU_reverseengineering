package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.charger.protocol.family.xcloud.bean.ChargeDetail;
import com.xcharge.common.bean.JsonBean;
import java.util.List;

/* loaded from: classes.dex */
public class ReportLocalChargeEnded extends JsonBean<ReportLocalChargeEnded> {
    private Long sid = null;
    private String subnode = null;
    private int port = 1;
    private String cardSourceId = null;
    private String vId = null;
    private String bType = null;
    private String bId = null;
    private Double bCap = null;
    private Double bVol = null;
    private String commVer = null;
    private Long chargeStartTime = null;
    private Long chargeStopTime = null;
    private Integer chargeInterval = null;
    private Long chargeEndTime = null;
    private Double powerCharged = null;
    private Integer batteryCharged = null;
    private Double ammeterStart = null;
    private Double ammeterEnd = null;
    private Integer feeTotal = null;
    private Integer feePower = null;
    private Integer feeService = null;
    private List<ChargeDetail> chargeDetail = null;
    private Integer cardBalance = null;
    private Boolean notPaid = null;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }

    public String getSubnode() {
        return this.subnode;
    }

    public void setSubnode(String subnode) {
        this.subnode = subnode;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getCardSourceId() {
        return this.cardSourceId;
    }

    public void setCardSourceId(String cardSourceId) {
        this.cardSourceId = cardSourceId;
    }

    public String getvId() {
        return this.vId;
    }

    public void setvId(String vId) {
        this.vId = vId;
    }

    public String getbType() {
        return this.bType;
    }

    public void setbType(String bType) {
        this.bType = bType;
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

    public Long getChargeStartTime() {
        return this.chargeStartTime;
    }

    public void setChargeStartTime(Long chargeStartTime) {
        this.chargeStartTime = chargeStartTime;
    }

    public Long getChargeStopTime() {
        return this.chargeStopTime;
    }

    public void setChargeStopTime(Long chargeStopTime) {
        this.chargeStopTime = chargeStopTime;
    }

    public Integer getChargeInterval() {
        return this.chargeInterval;
    }

    public void setChargeInterval(Integer chargeInterval) {
        this.chargeInterval = chargeInterval;
    }

    public Long getChargeEndTime() {
        return this.chargeEndTime;
    }

    public void setChargeEndTime(Long chargeEndTime) {
        this.chargeEndTime = chargeEndTime;
    }

    public Double getPowerCharged() {
        return this.powerCharged;
    }

    public void setPowerCharged(Double powerCharged) {
        this.powerCharged = powerCharged;
    }

    public Integer getBatteryCharged() {
        return this.batteryCharged;
    }

    public void setBatteryCharged(Integer batteryCharged) {
        this.batteryCharged = batteryCharged;
    }

    public Double getAmmeterStart() {
        return this.ammeterStart;
    }

    public void setAmmeterStart(Double ammeterStart) {
        this.ammeterStart = ammeterStart;
    }

    public Double getAmmeterEnd() {
        return this.ammeterEnd;
    }

    public void setAmmeterEnd(Double ammeterEnd) {
        this.ammeterEnd = ammeterEnd;
    }

    public Integer getFeeTotal() {
        return this.feeTotal;
    }

    public void setFeeTotal(Integer feeTotal) {
        this.feeTotal = feeTotal;
    }

    public Integer getFeePower() {
        return this.feePower;
    }

    public void setFeePower(Integer feePower) {
        this.feePower = feePower;
    }

    public Integer getFeeService() {
        return this.feeService;
    }

    public void setFeeService(Integer feeService) {
        this.feeService = feeService;
    }

    public List<ChargeDetail> getChargeDetail() {
        return this.chargeDetail;
    }

    public void setChargeDetail(List<ChargeDetail> chargeDetail) {
        this.chargeDetail = chargeDetail;
    }

    public Integer getCardBalance() {
        return this.cardBalance;
    }

    public void setCardBalance(Integer cardBalance) {
        this.cardBalance = cardBalance;
    }

    public Boolean getNotPaid() {
        return this.notPaid;
    }

    public void setNotPaid(Boolean notPaid) {
        this.notPaid = notPaid;
    }
}