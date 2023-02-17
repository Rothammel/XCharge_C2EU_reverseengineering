package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class LocalChargeBill extends JsonBean<LocalChargeBill> {
    private long id = 0;
    private String sourceId = null;
    private String cardSourceId = null;
    private long cardId = 0;
    private int balance = 0;
    private String deviceSourceId = null;
    private String deviceId = null;
    private int devicePort = 1;
    private String vehicleId = null;
    private String batteryId = null;
    private long chargeStartTime = 0;
    private long chargeStopTime = 0;
    private long delayFeeStartTime = 0;
    private long chargeEndTime = 0;
    private long paidTime = 0;
    private double powerCharged = 0.0d;
    private int batteryCharged = 0;
    private int chargeInterval = 0;
    private int delayInterval = 0;
    private int feeTotal = 0;
    private int feePower = 0;
    private int feeService = 0;
    private int feeDelay = 0;
    private int feePark = 0;
    private int feePaid = 0;
    private ArrayList<ChargeDetail> chargeDetail = new ArrayList<>();
    private String currencyType = null;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSourceId() {
        return this.sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getCardSourceId() {
        return this.cardSourceId;
    }

    public void setCardSourceId(String cardSourceId) {
        this.cardSourceId = cardSourceId;
    }

    public long getCardId() {
        return this.cardId;
    }

    public void setCardId(long cardId) {
        this.cardId = cardId;
    }

    public int getBalance() {
        return this.balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public String getDeviceSourceId() {
        return this.deviceSourceId;
    }

    public void setDeviceSourceId(String deviceSourceId) {
        this.deviceSourceId = deviceSourceId;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getDevicePort() {
        return this.devicePort;
    }

    public void setDevicePort(int devicePort) {
        this.devicePort = devicePort;
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

    public long getChargeStartTime() {
        return this.chargeStartTime;
    }

    public void setChargeStartTime(long chargeStartTime) {
        this.chargeStartTime = chargeStartTime;
    }

    public long getChargeStopTime() {
        return this.chargeStopTime;
    }

    public void setChargeStopTime(long chargeStopTime) {
        this.chargeStopTime = chargeStopTime;
    }

    public long getDelayFeeStartTime() {
        return this.delayFeeStartTime;
    }

    public void setDelayFeeStartTime(long delayFeeStartTime) {
        this.delayFeeStartTime = delayFeeStartTime;
    }

    public long getChargeEndTime() {
        return this.chargeEndTime;
    }

    public void setChargeEndTime(long chargeEndTime) {
        this.chargeEndTime = chargeEndTime;
    }

    public long getPaidTime() {
        return this.paidTime;
    }

    public void setPaidTime(long paidTime) {
        this.paidTime = paidTime;
    }

    public double getPowerCharged() {
        return this.powerCharged;
    }

    public void setPowerCharged(double powerCharged) {
        this.powerCharged = powerCharged;
    }

    public int getBatteryCharged() {
        return this.batteryCharged;
    }

    public void setBatteryCharged(int batteryCharged) {
        this.batteryCharged = batteryCharged;
    }

    public int getChargeInterval() {
        return this.chargeInterval;
    }

    public void setChargeInterval(int chargeInterval) {
        this.chargeInterval = chargeInterval;
    }

    public int getDelayInterval() {
        return this.delayInterval;
    }

    public void setDelayInterval(int delayInterval) {
        this.delayInterval = delayInterval;
    }

    public int getFeeTotal() {
        return this.feeTotal;
    }

    public void setFeeTotal(int feeTotal) {
        this.feeTotal = feeTotal;
    }

    public int getFeePower() {
        return this.feePower;
    }

    public void setFeePower(int feePower) {
        this.feePower = feePower;
    }

    public int getFeeService() {
        return this.feeService;
    }

    public void setFeeService(int feeService) {
        this.feeService = feeService;
    }

    public int getFeeDelay() {
        return this.feeDelay;
    }

    public void setFeeDelay(int feeDelay) {
        this.feeDelay = feeDelay;
    }

    public int getFeePark() {
        return this.feePark;
    }

    public void setFeePark(int feePark) {
        this.feePark = feePark;
    }

    public int getFeePaid() {
        return this.feePaid;
    }

    public void setFeePaid(int feePaid) {
        this.feePaid = feePaid;
    }

    public ArrayList<ChargeDetail> getChargeDetail() {
        return this.chargeDetail;
    }

    public void setChargeDetail(ArrayList<ChargeDetail> chargeDetail) {
        this.chargeDetail = chargeDetail;
    }

    public String getCurrencyType() {
        return this.currencyType;
    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }
}