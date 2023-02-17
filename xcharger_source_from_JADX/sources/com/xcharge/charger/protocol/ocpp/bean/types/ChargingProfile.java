package com.xcharge.charger.protocol.ocpp.bean.types;

import com.xcharge.common.bean.JsonBean;

public class ChargingProfile extends JsonBean<ChargingProfile> {
    private int chargingProfileId;
    private String chargingProfileKind;
    private String chargingProfilePurpose;
    private ChargingSchedule chargingSchedule;
    private String recurrencyKind;
    private int stackLevel;
    private int transactionId;
    private String validFrom;
    private String validTo;

    public int getChargingProfileId() {
        return this.chargingProfileId;
    }

    public void setChargingProfileId(int chargingProfileId2) {
        this.chargingProfileId = chargingProfileId2;
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(int transactionId2) {
        this.transactionId = transactionId2;
    }

    public int getStackLevel() {
        return this.stackLevel;
    }

    public void setStackLevel(int stackLevel2) {
        this.stackLevel = stackLevel2;
    }

    public String getChargingProfilePurpose() {
        return this.chargingProfilePurpose;
    }

    public void setChargingProfilePurpose(String chargingProfilePurpose2) {
        this.chargingProfilePurpose = chargingProfilePurpose2;
    }

    public String getChargingProfileKind() {
        return this.chargingProfileKind;
    }

    public void setChargingProfileKind(String chargingProfileKind2) {
        this.chargingProfileKind = chargingProfileKind2;
    }

    public String getRecurrencyKind() {
        return this.recurrencyKind;
    }

    public void setRecurrencyKind(String recurrencyKind2) {
        this.recurrencyKind = recurrencyKind2;
    }

    public String getValidFrom() {
        return this.validFrom;
    }

    public void setValidFrom(String validFrom2) {
        this.validFrom = validFrom2;
    }

    public String getValidTo() {
        return this.validTo;
    }

    public void setValidTo(String validTo2) {
        this.validTo = validTo2;
    }

    public ChargingSchedule getChargingSchedule() {
        return this.chargingSchedule;
    }

    public void setChargingSchedule(ChargingSchedule chargingSchedule2) {
        this.chargingSchedule = chargingSchedule2;
    }
}
