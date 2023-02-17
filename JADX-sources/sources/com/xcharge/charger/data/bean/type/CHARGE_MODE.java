package com.xcharge.charger.data.bean.type;

/* loaded from: classes.dex */
public enum CHARGE_MODE {
    unknow(0),
    pre_charge(1),
    normal_charge(2),
    trickle_charge(3),
    full(4),
    paused(10);
    
    private int mode;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static CHARGE_MODE[] valuesCustom() {
        CHARGE_MODE[] valuesCustom = values();
        int length = valuesCustom.length;
        CHARGE_MODE[] charge_modeArr = new CHARGE_MODE[length];
        System.arraycopy(valuesCustom, 0, charge_modeArr, 0, length);
        return charge_modeArr;
    }

    CHARGE_MODE(int mode) {
        this.mode = 0;
        this.mode = mode;
    }

    public static CHARGE_MODE valueBy(int mode) {
        switch (mode) {
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
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            default:
                return unknow;
            case 10:
                return paused;
        }
    }

    public int getMode() {
        return this.mode;
    }
}
