package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

public class LocalChargeBill extends JsonBean<LocalChargeBill> {
    private int balance = 0;
    private int batteryCharged = 0;
    private String batteryId = null;
    private long cardId = 0;
    private String cardSourceId = null;
    private ArrayList<ChargeDetail> chargeDetail = new ArrayList<>();
    private long chargeEndTime = 0;
    private int chargeInterval = 0;
    private long chargeStartTime = 0;
    private long chargeStopTime = 0;
    private String currencyType = null;
    private long delayFeeStartTime = 0;
    private int delayInterval = 0;
    private String deviceId = null;
    private int devicePort = 1;
    private String deviceSourceId = null;
    private int feeDelay = 0;
    private int feePaid = 0;
    private int feePark = 0;
    private int feePower = 0;
    private int feeService = 0;
    private int feeTotal = 0;

    /* renamed from: id */
    private long f81id = 0;
    private long paidTime = 0;
    private double powerCharged = 0.0d;
    private String sourceId = null;
    private String vehicleId = null;

    public long getId() {
        return this.f81id;
    }

    public void setId(long id) {
        this.f81id = id;
    }

    public String getSourceId() {
        return this.sourceId;
    }

    public void setSourceId(String sourceId2) {
        this.sourceId = sourceId2;
    }

    public String getCardSourceId() {
        return this.cardSourceId;
    }

    public void setCardSourceId(String cardSourceId2) {
        this.cardSourceId = cardSourceId2;
    }

    public long getCardId() {
        return this.cardId;
    }

    public void setCardId(long cardId2) {
        this.cardId = cardId2;
    }

    public int getBalance() {
        return this.balance;
    }

    public void setBalance(int balance2) {
        this.balance = balance2;
    }

    public String getDeviceSourceId() {
        return this.deviceSourceId;
    }

    public void setDeviceSourceId(String deviceSourceId2) {
        this.deviceSourceId = deviceSourceId2;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId2) {
        this.deviceId = deviceId2;
    }

    public int getDevicePort() {
        return this.devicePort;
    }

    public void setDevicePort(int devicePort2) {
        this.devicePort = devicePort2;
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

    public long getChargeStartTime() {
        return this.chargeStartTime;
    }

    public void setChargeStartTime(long chargeStartTime2) {
        this.chargeStartTime = chargeStartTime2;
    }

    public long getChargeStopTime() {
        return this.chargeStopTime;
    }

    public void setChargeStopTime(long chargeStopTime2) {
        this.chargeStopTime = chargeStopTime2;
    }

    public long getDelayFeeStartTime() {
        return this.delayFeeStartTime;
    }

    public void setDelayFeeStartTime(long delayFeeStartTime2) {
        this.delayFeeStartTime = delayFeeStartTime2;
    }

    public long getChargeEndTime() {
        return this.chargeEndTime;
    }

    public void setChargeEndTime(long chargeEndTime2) {
        this.chargeEndTime = chargeEndTime2;
    }

    public long getPaidTime() {
        return this.paidTime;
    }

    public void setPaidTime(long paidTime2) {
        this.paidTime = paidTime2;
    }

    public double getPowerCharged() {
        return this.powerCharged;
    }

    public void setPowerCharged(double powerCharged2) {
        this.powerCharged = powerCharged2;
    }

    public int getBatteryCharged() {
        return this.batteryCharged;
    }

    public void setBatteryCharged(int batteryCharged2) {
        this.batteryCharged = batteryCharged2;
    }

    public int getChargeInterval() {
        return this.chargeInterval;
    }

    public void setChargeInterval(int chargeInterval2) {
        this.chargeInterval = chargeInterval2;
    }

    public int getDelayInterval() {
        return this.delayInterval;
    }

    public void setDelayInterval(int delayInterval2) {
        this.delayInterval = delayInterval2;
    }

    public int getFeeTotal() {
        return this.feeTotal;
    }

    public void setFeeTotal(int feeTotal2) {
        this.feeTotal = feeTotal2;
    }

    public int getFeePower() {
        return this.feePower;
    }

    public void setFeePower(int feePower2) {
        this.feePower = feePower2;
    }

    public int getFeeService() {
        return this.feeService;
    }

    public void setFeeService(int feeService2) {
        this.feeService = feeService2;
    }

    public int getFeeDelay() {
        return this.feeDelay;
    }

    public void setFeeDelay(int feeDelay2) {
        this.feeDelay = feeDelay2;
    }

    public int getFeePark() {
        return this.feePark;
    }

    public void setFeePark(int feePark2) {
        this.feePark = feePark2;
    }

    public int getFeePaid() {
        return this.feePaid;
    }

    public void setFeePaid(int feePaid2) {
        this.feePaid = feePaid2;
    }

    public ArrayList<ChargeDetail> getChargeDetail() {
        return this.chargeDetail;
    }

    public void setChargeDetail(ArrayList<ChargeDetail> chargeDetail2) {
        this.chargeDetail = chargeDetail2;
    }

    public String getCurrencyType() {
        return this.currencyType;
    }

    public void setCurrencyType(String currencyType2) {
        this.currencyType = currencyType2;
    }
}
