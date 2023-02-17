package com.xcharge.charger.data.bean.type;

public enum CHARGE_MODE {
    unknow(0),
    pre_charge(1),
    normal_charge(2),
    trickle_charge(3),
    full(4),
    paused(10);
    
    private int mode;

    private CHARGE_MODE(int mode2) {
        this.mode = 0;
        this.mode = mode2;
    }

    public static CHARGE_MODE valueBy(int mode2) {
        switch (mode2) {
            case 0:
                return unknow;
            case 1:
                return pre_charge;
            case 2:
                return normal_charge;
            case 3:
                return trickle_charge;
            case 4:
                return full;
            case 10:
                return paused;
            default:
                return unknow;
        }
    }

    public int getMode() {
        return this.mode;
    }
}
