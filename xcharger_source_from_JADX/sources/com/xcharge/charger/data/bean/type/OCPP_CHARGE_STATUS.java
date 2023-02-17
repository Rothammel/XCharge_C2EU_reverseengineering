package com.xcharge.charger.data.bean.type;

public enum OCPP_CHARGE_STATUS {
    SESSION_STARTED("SESSION_STARTED"),
    TRANSACTION_STARTED("TRANSACTION_STARTED"),
    ENERGY_OFFER("ENERGY_OFFER"),
    ENERGY_TRANSFER("ENERGY_TRANSFER"),
    ENERGY_OFFER_SUSPEND("ENERGY_OFFER_SUSPEND"),
    TRANSACTION_ENDED("TRANSACTION_ENDED"),
    SESSION_ENDED("\tSESSION_ENDED");
    
    private String status;

    private OCPP_CHARGE_STATUS(String status2) {
        this.status = null;
        this.status = status2;
    }

    public String getStatus() {
        return this.status;
    }
}
