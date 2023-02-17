package com.xcharge.charger.data.bean.type;

public enum SERVICE_PRICE_UNIT {
    degree(1),
    order(2);
    
    private int unit;

    private SERVICE_PRICE_UNIT(int unit2) {
        this.unit = 1;
        this.unit = unit2;
    }

    public static SERVICE_PRICE_UNIT valueBy(int unit2) {
        switch (unit2) {
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
