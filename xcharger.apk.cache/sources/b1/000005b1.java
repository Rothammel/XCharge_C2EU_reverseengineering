package com.xcharge.charger.data.bean.type;

/* loaded from: classes.dex */
public enum GUN_LOCK_MODE {
    disable(0),
    unlock_before_pay(1),
    unlock_after_pay(2),
    auto(3);
    
    private int mode;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static GUN_LOCK_MODE[] valuesCustom() {
        GUN_LOCK_MODE[] valuesCustom = values();
        int length = valuesCustom.length;
        GUN_LOCK_MODE[] gun_lock_modeArr = new GUN_LOCK_MODE[length];
        System.arraycopy(valuesCustom, 0, gun_lock_modeArr, 0, length);
        return gun_lock_modeArr;
    }

    GUN_LOCK_MODE(int mode) {
        this.mode = 3;
        this.mode = mode;
    }

    public static GUN_LOCK_MODE valueBy(int mode) {
        switch (mode) {
            case 0:
                return disable;
            case 1:
                return unlock_before_pay;
            case 2:
                return unlock_after_pay;
            case 3:
                return auto;
            default:
                return disable;
        }
    }

    public int getMode() {
        return this.mode;
    }
}