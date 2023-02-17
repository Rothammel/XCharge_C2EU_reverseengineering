package com.xcharge.charger.data.bean.type;

/* loaded from: classes.dex */
public enum ADVERT_POLICY {
    scanAdvsite("scanAdvsite"),
    pullAdvsite("pullAdvsite"),
    wakeUpAdvsite("wakeUpAdvsite"),
    idleAdvsite("idleAdvsite"),
    chargingAdvsite("chargingAdvsite");
    
    private String policy;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static ADVERT_POLICY[] valuesCustom() {
        ADVERT_POLICY[] valuesCustom = values();
        int length = valuesCustom.length;
        ADVERT_POLICY[] advert_policyArr = new ADVERT_POLICY[length];
        System.arraycopy(valuesCustom, 0, advert_policyArr, 0, length);
        return advert_policyArr;
    }

    ADVERT_POLICY(String policy) {
        this.policy = null;
        this.policy = policy;
    }

    public String getPolicy() {
        return this.policy;
    }
}
