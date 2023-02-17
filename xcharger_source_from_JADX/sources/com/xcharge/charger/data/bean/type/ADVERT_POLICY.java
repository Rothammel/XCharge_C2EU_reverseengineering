package com.xcharge.charger.data.bean.type;

public enum ADVERT_POLICY {
    scanAdvsite("scanAdvsite"),
    pullAdvsite("pullAdvsite"),
    wakeUpAdvsite("wakeUpAdvsite"),
    idleAdvsite("idleAdvsite"),
    chargingAdvsite("chargingAdvsite");
    
    private String policy;

    private ADVERT_POLICY(String policy2) {
        this.policy = null;
        this.policy = policy2;
    }

    public String getPolicy() {
        return this.policy;
    }
}
