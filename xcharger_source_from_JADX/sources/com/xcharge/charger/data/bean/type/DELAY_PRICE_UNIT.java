package com.xcharge.charger.data.bean.type;

public enum DELAY_PRICE_UNIT {
    minute(1);
    
    private int unit;

    private DELAY_PRICE_UNIT(int unit2) {
        this.unit = 1;
        this.unit = unit2;
    }

    public static DELAY_PRICE_UNIT valueBy(int unit2) {
        switch (unit2) {
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
