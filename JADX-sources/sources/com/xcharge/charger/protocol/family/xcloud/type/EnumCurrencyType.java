package com.xcharge.charger.protocol.family.xcloud.type;

/* loaded from: classes.dex */
public enum EnumCurrencyType {
    cny("cny"),
    usd("usd");
    
    private String type;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static EnumCurrencyType[] valuesCustom() {
        EnumCurrencyType[] valuesCustom = values();
        int length = valuesCustom.length;
        EnumCurrencyType[] enumCurrencyTypeArr = new EnumCurrencyType[length];
        System.arraycopy(valuesCustom, 0, enumCurrencyTypeArr, 0, length);
        return enumCurrencyTypeArr;
    }

    EnumCurrencyType(String type) {
        this.type = null;
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
