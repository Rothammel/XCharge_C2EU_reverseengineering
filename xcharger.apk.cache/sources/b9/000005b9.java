package com.xcharge.charger.data.bean.type;

/* loaded from: classes.dex */
public enum PLATFORM_CUSTOMER {
    jsmny("jsmny"),
    anyo_private("anyo_private"),
    anyo_svw("anyo_svw"),
    ct_demo("ct_demo"),
    be_ENERGISED("be_ENERGISED"),
    emobility_partner("emobility_partner");
    
    private String customer;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static PLATFORM_CUSTOMER[] valuesCustom() {
        PLATFORM_CUSTOMER[] valuesCustom = values();
        int length = valuesCustom.length;
        PLATFORM_CUSTOMER[] platform_customerArr = new PLATFORM_CUSTOMER[length];
        System.arraycopy(valuesCustom, 0, platform_customerArr, 0, length);
        return platform_customerArr;
    }

    PLATFORM_CUSTOMER(String customer) {
        this.customer = null;
        this.customer = customer;
    }

    public String getCustomer() {
        return this.customer;
    }
}