package com.xcharge.charger.data.bean.type;

/* loaded from: classes.dex */
public enum DELAY_PRICE_UNIT {
    minute(1);
    
    private int unit;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static DELAY_PRICE_UNIT[] valuesCustom() {
        DELAY_PRICE_UNIT[] valuesCustom = values();
        int length = valuesCustom.length;
        DELAY_PRICE_UNIT[] delay_price_unitArr = new DELAY_PRICE_UNIT[length];
        System.arraycopy(valuesCustom, 0, delay_price_unitArr, 0, length);
        return delay_price_unitArr;
    }

    DELAY_PRICE_UNIT(int unit) {
        this.unit = 1;
        this.unit = unit;
    }

    public static DELAY_PRICE_UNIT valueBy(int unit) {
        switch (unit) {
            case 1:
                return minute;
            default:
                return minute;
        }
    }

    public int getUnit() {
        return this.unit;
    }
}