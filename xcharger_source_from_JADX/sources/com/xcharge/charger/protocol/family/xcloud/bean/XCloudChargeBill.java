package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;
import java.util.List;

public class XCloudChargeBill extends JsonBean<XCloudChargeBill> {
    private Double ammeterEnd = null;
    private Double ammeterStart = null;
    private Integer batteryCharged = 0;
    private String batteryId = null;
    private Integer cardBalance = null;
    private Long cardId = null;
    private DeviceError cause = null;
    private List<ChargeDetail> chargeDetail = null;
    private Long chargeEndTime = null;
    private Integer chargeInterval = null;
    private Long chargeStartTime = null;
    private Long chargeStopTime = null;
    private Long createTime = null;
    private Long delayFeeStartTime = null;
    private Integer delayInterval = null;
    private Long deviceId = null;
    private Integer devicePort = null;
    private Integer feeDelay = null;
    private Integer feePaid = null;
    private Integer feePark = null;
    private Long feePolicyId;
    private Integer feePower = null;
    private Integer feeService = null;
    private Integer feeTotal = null;

    /* renamed from: id */
    private Long f84id = null;
    private Integer leftTimeEstimated = null;
    private String localCard = null;
    private Boolean notPaid = null;
    private Long oaId = null;
    private Long paidTime = null;
    private String payMethod = null;
    private PaymentDetail paymentDetail = null;
    private Double powerCharged = null;
    private Double powerCurrent = null;
    private Integer powerSupply = null;
    String source = null;
    private String status = null;
    private Long updateTime = null;
    private Long userGroupId = null;
    private Long userId = null;
    private String vehicleId = null;
    private Long wechatId = null;

    public Long getId() {
        return this.f84id;
    }

    public void setId(Long id) {
        this.f84id = id;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source2) {
        this.source = source2;
    }

    public Long getFeePolicyId() {
        return this.feePolicyId;
    }

    public void setFeePolicyId(Long feePolicyId2) {
        this.feePolicyId = feePolicyId2;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId2) {
        this.userId = userId2;
    }

    public Long getUserGroupId() {
        return this.userGroupId;
    }

    public void setUserGroupId(Long userGroupId2) {
        this.userGroupId = userGroupId2;
    }

    public Long getOaId() {
        return this.oaId;
    }

    public void setOaId(Long oaId2) {
        this.oaId = oaId2;
    }

    public Long getWechatId() {
        return this.wechatId;
    }

    public void setWechatId(Long wechatId2) {
        this.wechatId = wechatId2;
    }

    public Long getCardId() {
        return this.cardId;
    }

    public void setCardId(Long cardId2) {
        this.cardId = cardId2;
    }

    public String getLocalCard() {
        return this.localCard;
    }

    public void setLocalCard(String localCard2) {
        this.localCard = localCard2;
    }

    public Long getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(Long deviceId2) {
        this.deviceId = deviceId2;
    }

    public Integer getDevicePort() {
        return this.devicePort;
    }

    public void setDevicePort(Integer devicePort2) {
        this.devicePort = devicePort2;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status2) {
        this.status = status2;
    }

    public Long getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Long createTime2) {
        this.createTime = createTime2;
    }

    public Long getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Long updateTime2) {
        this.updateTime = updateTime2;
    }

    public String getVehicleId() {
        return this.vehicleId;
    }

    public void setVehicleId(String vehicleId2) {
        this.vehicleId = vehicleId2;
    }

    public String getBatteryId() {
        return this.batteryId;
    }

    public void setBatteryId(String batteryId2) {
        this.batteryId = batteryId2;
    }

    public Long getChargeStartTime() {
        return this.chargeStartTime;
    }

    public void setChargeStartTime(Long chargeStartTime2) {
        this.chargeStartTime = chargeStartTime2;
    }

    public Integer getPowerSupply() {
        return this.powerSupply;
    }

    public void setPowerSupply(Integer powerSupply2) {
        this.powerSupply = powerSupply2;
    }

    public Double getPowerCurrent() {
        return this.powerCurrent;
    }

    public void setPowerCurrent(Double powerCurrent2) {
        this.powerCurrent = powerCurrent2;
    }

    public Integer getLeftTimeEstimated() {
        return this.leftTimeEstimated;
    }

    public void setLeftTimeEstimated(Integer leftTimeEstimated2) {
        this.leftTimeEstimated = leftTimeEstimated2;
    }

    public DeviceError getCause() {
        return this.cause;
    }

    public void setCause(DeviceError cause2) {
        this.cause = cause2;
    }

    public Long getDelayFeeStartTime() {
        return this.delayFeeStartTime;
    }

    public void setDelayFeeStartTime(Long delayFeeStartTime2) {
        this.delayFeeStartTime = delayFeeStartTime2;
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

    public Integer getFeePark() {
        return this.feePark;
    }

    public void setFeePark(Integer feePark2) {
        this.feePark = feePark2;
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

    public Long getChargeEndTime() {
        return this.chargeEndTime;
    }

    public void setChargeEndTime(Long chargeEndTime2) {
        this.chargeEndTime = chargeEndTime2;
    }

    public Integer getFeeDelay() {
        return this.feeDelay;
    }

    public void setFeeDelay(Integer feeDelay2) {
        this.feeDelay = feeDelay2;
    }

    public Integer getDelayInterval() {
        return this.delayInterval;
    }

    public void setDelayInterval(Integer delayInterval2) {
        this.delayInterval = delayInterval2;
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

    public Integer getFeePaid() {
        return this.feePaid;
    }

    public void setFeePaid(Integer feePaid2) {
        this.feePaid = feePaid2;
    }

    public String getPayMethod() {
        return this.payMethod;
    }

    public void setPayMethod(String payMethod2) {
        this.payMethod = payMethod2;
    }

    public Long getPaidTime() {
        return this.paidTime;
    }

    public void setPaidTime(Long paidTime2) {
        this.paidTime = paidTime2;
    }

    public PaymentDetail getPaymentDetail() {
        return this.paymentDetail;
    }

    public void setPaymentDetail(PaymentDetail paymentDetail2) {
        this.paymentDetail = paymentDetail2;
    }
}
