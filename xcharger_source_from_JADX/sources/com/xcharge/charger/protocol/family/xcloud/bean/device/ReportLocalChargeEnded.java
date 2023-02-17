package com.xcharge.charger.protocol.family.xcloud.bean.device;

import com.xcharge.charger.protocol.family.xcloud.bean.ChargeDetail;
import com.xcharge.common.bean.JsonBean;
import java.util.List;

public class ReportLocalChargeEnded extends JsonBean<ReportLocalChargeEnded> {
    private Double ammeterEnd = null;
    private Double ammeterStart = null;
    private Double bCap = null;
    private String bId = null;
    private String bType = null;
    private Double bVol = null;
    private Integer batteryCharged = null;
    private Integer cardBalance = null;
    private String cardSourceId = null;
    private List<ChargeDetail> chargeDetail = null;
    private Long chargeEndTime = null;
    private Integer chargeInterval = null;
    private Long chargeStartTime = null;
    private Long chargeStopTime = null;
    private String commVer = null;
    private Integer feePower = null;
    private Integer feeService = null;
    private Integer feeTotal = null;
    private Boolean notPaid = null;
    private int port = 1;
    private Double powerCharged = null;
    private Long sid = null;
    private String subnode = null;
    private String vId = null;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid2) {
        this.sid = sid2;
    }

    public String getSubnode() {
        return this.subnode;
    }

    public void setSubnode(String subnode2) {
        this.subnode = subnode2;
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

    public String getbType() {
        return this.bType;
    }

    public void setbType(String bType2) {
        this.bType = bType2;
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

    public Long getChargeStartTime() {
        return this.chargeStartTime;
    }

    public void setChargeStartTime(Long chargeStartTime2) {
        this.chargeStartTime = chargeStartTime2;
    }

    public Long getChargeStopTime() {
        return this.chargeStopTime;
    }

    public void setChargeStopTime(Long chargeStopTime2) {
        this.chargeStopTime = chargeStopTime2;
    }

    public Integer getChargeInterval() {
        return this.chargeInterval;
    }

    public void setChargeInterval(Integer chargeInterval2) {
        this.chargeInterval = chargeInterval2;
    }

    public Long getChargeEndTime() {
        return this.chargeEndTime;
    }

    public void setChargeEndTime(Long chargeEndTime2) {
        this.chargeEndTime = chargeEndTime2;
    }

    public Double getPowerCharged() {
        return this.powerCharged;
    }

    public void setPowerCharged(Double powerCharged2) {
        this.powerCharged = powerCharged2;
    }

    public Integer getBatteryCharged() {
        return this.batteryCharged;
    }

    public void setBatteryCharged(Integer batteryCharged2) {
        this.batteryCharged = batteryCharged2;
    }

    public Double getAmmeterStart() {
        return this.ammeterStart;
    }

    public void setAmmeterStart(Double ammeterStart2) {
        this.ammeterStart = ammeterStart2;
    }

    public Double getAmmeterEnd() {
        return this.ammeterEnd;
    }

    public void setAmmeterEnd(Double ammeterEnd2) {
        this.ammeterEnd = ammeterEnd2;
    }

    public Integer getFeeTotal() {
        return this.feeTotal;
    }

    public void setFeeTotal(Integer feeTotal2) {
        this.feeTotal = feeTotal2;
    }

    public Integer getFeePower() {
        return this.feePower;
    }

    public void setFeePower(Integer feePower2) {
        this.feePower = feePower2;
    }

    public Integer getFeeService() {
        return this.feeService;
    }

    public void setFeeService(Integer feeService2) {
        this.feeService = feeService2;
    }

    public List<ChargeDetail> getChargeDetail() {
        return this.chargeDetail;
    }

    public void setChargeDetail(List<ChargeDetail> chargeDetail2) {
        this.chargeDetail = chargeDetail2;
    }

    public Integer getCardBalance() {
        return this.cardBalance;
    }

    public void setCardBalance(Integer cardBalance2) {
        this.cardBalance = cardBalance2;
    }

    public Boolean getNotPaid() {
        return this.notPaid;
    }

    public void setNotPaid(Boolean notPaid2) {
        this.notPaid = notPaid2;
    }
}
