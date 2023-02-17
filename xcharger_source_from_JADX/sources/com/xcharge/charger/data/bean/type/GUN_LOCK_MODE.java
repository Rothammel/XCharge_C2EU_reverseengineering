package com.xcharge.charger.data.bean.type;

public enum GUN_LOCK_MODE {
    disable(0),
    unlock_before_pay(1),
    unlock_after_pay(2),
    auto(3);
    
    private int mode;

    private GUN_LOCK_MODE(int mode2) {
        this.mode = 3;
        this.mode = mode2;
    }

    public static GUN_LOCK_MODE valueBy(int mode2) {
        switch (mode2) {
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
