package com.xcharge.charger.data.bean.type;

/* loaded from: classes.dex */
public enum SERVICE_PRICE_UNIT {
    degree(1),
    order(2);
    
    private int unit;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static SERVICE_PRICE_UNIT[] valuesCustom() {
        SERVICE_PRICE_UNIT[] valuesCustom = values();
        int length = valuesCustom.length;
        SERVICE_PRICE_UNIT[] service_price_unitArr = new SERVICE_PRICE_UNIT[length];
        System.arraycopy(valuesCustom, 0, service_price_unitArr, 0, length);
        return service_price_unitArr;
    }

    SERVICE_PRICE_UNIT(int unit) {
        this.unit = 1;
        this.unit = unit;
    }

    public static SERVICE_PRICE_UNIT valueBy(int unit) {
        switch (unit) {
            case 1:
                return degree;
            case 2:
                return order;
            default:
                return degree;
        }
    }

    public int getUnit() {
        return this.unit;
    }
}
