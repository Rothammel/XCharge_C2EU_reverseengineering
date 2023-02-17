package com.xcharge.charger.data.bean.type;

/* loaded from: classes.dex */
public enum BLN_MODE {
    on_off(0),
    bln(1);
    
    private int mode;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static BLN_MODE[] valuesCustom() {
        BLN_MODE[] valuesCustom = values();
        int length = valuesCustom.length;
        BLN_MODE[] bln_modeArr = new BLN_MODE[length];
        System.arraycopy(valuesCustom, 0, bln_modeArr, 0, length);
        return bln_modeArr;
    }

    BLN_MODE(int mode) {
        this.mode = 0;
        this.mode = mode;
    }

    public static BLN_MODE valueBy(int mode) {
        switch (mode) {
            case 0:
                return on_off;
            case 1:
                return bln;
            default:
                return on_off;
        }
    }

    public int getMode() {
        return this.mode;
    }
}