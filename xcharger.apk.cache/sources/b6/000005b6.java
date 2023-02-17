package com.xcharge.charger.data.bean.type;

/* loaded from: classes.dex */
public enum PARK_LOCK_MODE {
    disable(0),
    init_unlock(1),
    auto(2);
    
    private int mode;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static PARK_LOCK_MODE[] valuesCustom() {
        PARK_LOCK_MODE[] valuesCustom = values();
        int length = valuesCustom.length;
        PARK_LOCK_MODE[] park_lock_modeArr = new PARK_LOCK_MODE[length];
        System.arraycopy(valuesCustom, 0, park_lock_modeArr, 0, length);
        return park_lock_modeArr;
    }

    PARK_LOCK_MODE(int mode) {
        this.mode = 2;
        this.mode = mode;
    }

    public static PARK_LOCK_MODE valueBy(int mode) {
        switch (mode) {
            case 0:
                return disable;
            case 1:
                return init_unlock;
            case 2:
                return auto;
            default:
                return disable;
        }
    }

    public int getMode() {
        return this.mode;
    }
}