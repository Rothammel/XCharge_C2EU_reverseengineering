package com.xcharge.charger.protocol.ocpp.bean.types;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
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

    public void setChargingProfileId(int chargingProfileId) {
        this.chargingProfileId = chargingProfileId;
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getStackLevel() {
        return this.stackLevel;
    }

    public void setStackLevel(int stackLevel) {
        this.stackLevel = stackLevel;
    }

    public String getChargingProfilePurpose() {
        return this.chargingProfilePurpose;
    }

    public void setChargingProfilePurpose(String chargingProfilePurpose) {
        this.chargingProfilePurpose = chargingProfilePurpose;
    }

    public String getChargingProfileKind() {
        return this.chargingProfileKind;
    }

    public void setChargingProfileKind(String chargingProfileKind) {
        this.chargingProfileKind = chargingProfileKind;
    }

    public String getRecurrencyKind() {
        return this.recurrencyKind;
    }

    public void setRecurrencyKind(String recurrencyKind) {
        this.recurrencyKind = recurrencyKind;
    }

    public String getValidFrom() {
        return this.validFrom;
    }

    public void setValidFrom(String validFrom) {
        this.validFrom = validFrom;
    }

    public String getValidTo() {
        return this.validTo;
    }

    public void setValidTo(String validTo) {
        this.validTo = validTo;
    }

    public ChargingSchedule getChargingSchedule() {
        return this.chargingSchedule;
    }

    public void setChargingSchedule(ChargingSchedule chargingSchedule) {
        this.chargingSchedule = chargingSchedule;
    }
}