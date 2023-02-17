package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;
import java.util.List;

/* loaded from: classes.dex */
public class XCloudChargeBill extends JsonBean<XCloudChargeBill> {
    private Long feePolicyId;
    private Long id = null;
    String source = null;
    private Long userId = null;
    private Long userGroupId = null;
    private Long oaId = null;
    private Long wechatId = null;
    private Long cardId = null;
    private String localCard = null;
    private Long deviceId = null;
    private Integer devicePort = null;
    private String status = null;
    private Long createTime = null;
    private Long updateTime = null;
    private String vehicleId = null;
    private String batteryId = null;
    private Long chargeStartTime = null;
    private Integer powerSupply = null;
    private Double powerCurrent = null;
    private Integer leftTimeEstimated = null;
    private DeviceError cause = null;
    private Long delayFeeStartTime = null;
    private Double powerCharged = null;
    private Integer batteryCharged = 0;
    private Integer feeTotal = null;
    private Integer feePower = null;
    private Integer feeService = null;
    private Integer feePark = null;
    private Long chargeStopTime = null;
    private Integer chargeInterval = null;
    private Double ammeterStart = null;
    private Double ammeterEnd = null;
    private Long chargeEndTime = null;
    private Integer feeDelay = null;
    private Integer delayInterval = null;
    private List<ChargeDetail> chargeDetail = null;
    private Integer cardBalance = null;
    private Boolean notPaid = null;
    private Integer feePaid = null;
    private String payMethod = null;
    private Long paidTime = null;
    private PaymentDetail paymentDetail = null;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Long getFeePolicyId() {
        return this.feePolicyId;
    }

    public void setFeePolicyId(Long feePolicyId) {
        this.feePolicyId = feePolicyId;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserGroupId() {
        return this.userGroupId;
    }

    public void setUserGroupId(Long userGroupId) {
        this.userGroupId = userGroupId;
    }

    public Long getOaId() {
        return this.oaId;
    }

    public void setOaId(Long oaId) {
        this.oaId = oaId;
    }

    public Long getWechatId() {
        return this.wechatId;
    }

    public void setWechatId(Long wechatId) {
        this.wechatId = wechatId;
    }

    public Long getCardId() {
        return this.cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public String getLocalCard() {
        return this.localCard;
    }

    public void setLocalCard(String localCard) {
        this.localCard = localCard;
    }

    public Long getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getDevicePort() {
        return this.devicePort;
    }

    public void setDevicePort(Integer devicePort) {
        this.devicePort = devicePort;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getVehicleId() {
        return this.vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getBatteryId() {
        return this.batteryId;
    }

    public void setBatteryId(String batteryId) {
        this.batteryId = batteryId;
    }

    public Long getChargeStartTime() {
        return this.chargeStartTime;
    }

    public void setChargeStartTime(Long chargeStartTime) {
        this.chargeStartTime = chargeStartTime;
    }

    public Integer getPowerSupply() {
        return this.powerSupply;
    }

    public void setPowerSupply(Integer powerSupply) {
        this.powerSupply = powerSupply;
    }

    public Double getPowerCurrent() {
        return this.powerCurrent;
    }

    public void setPowerCurrent(Double powerCurrent) {
        this.powerCurrent = powerCurrent;
    }

    public Integer getLeftTimeEstimated() {
        return this.leftTimeEstimated;
    }

    public void setLeftTimeEstimated(Integer leftTimeEstimated) {
        this.leftTimeEstimated = leftTimeEstimated;
    }

    public DeviceError getCause() {
        return this.cause;
    }

    public void setCause(DeviceError cause) {
        this.cause = cause;
    }

    public Long getDelayFeeStartTime() {
        return this.delayFeeStartTime;
    }

    public void setDelayFeeStartTime(Long delayFeeStartTime) {
        this.delayFeeStartTime = delayFeeStartTime;
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

    public Integer getFeePark() {
        return this.feePark;
    }

    public void setFeePark(Integer feePark) {
        this.feePark = feePark;
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

    public Long getChargeEndTime() {
        return this.chargeEndTime;
    }

    public void setChargeEndTime(Long chargeEndTime) {
        this.chargeEndTime = chargeEndTime;
    }

    public Integer getFeeDelay() {
        return this.feeDelay;
    }

    public void setFeeDelay(Integer feeDelay) {
        this.feeDelay = feeDelay;
    }

    public Integer getDelayInterval() {
        return this.delayInterval;
    }

    public void setDelayInterval(Integer delayInterval) {
        this.delayInterval = delayInterval;
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

    public Integer getFeePaid() {
        return this.feePaid;
    }

    public void setFeePaid(Integer feePaid) {
        this.feePaid = feePaid;
    }

    public String getPayMethod() {
        return this.payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public Long getPaidTime() {
        return this.paidTime;
    }

    public void setPaidTime(Long paidTime) {
        this.paidTime = paidTime;
    }

    public PaymentDetail getPaymentDetail() {
        return this.paymentDetail;
    }

    public void setPaymentDetail(PaymentDetail paymentDetail) {
        this.paymentDetail = paymentDetail;
    }
}
