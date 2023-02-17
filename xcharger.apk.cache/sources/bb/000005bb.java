package com.xcharge.charger.data.bean.type;

/* loaded from: classes.dex */
public enum SERVICE_REGION {
    China("China"),
    Europe("Europe");
    
    private String region;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static SERVICE_REGION[] valuesCustom() {
        SERVICE_REGION[] valuesCustom = values();
        int length = valuesCustom.length;
        SERVICE_REGION[] service_regionArr = new SERVICE_REGION[length];
        System.arraycopy(valuesCustom, 0, service_regionArr, 0, length);
        return service_regionArr;
    }

    SERVICE_REGION(String region) {
        this.region = null;
        this.region = region;
    }

    public String getRegion() {
        return this.region;
    }
}