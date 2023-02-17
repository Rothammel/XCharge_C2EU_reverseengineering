package com.xcharge.charger.data.bean.type;

public enum PARK_LOCK_MODE {
    disable(0),
    init_unlock(1),
    auto(2);
    
    private int mode;

    private PARK_LOCK_MODE(int mode2) {
        this.mode = 2;
        this.mode = mode2;
    }

    public static PARK_LOCK_MODE valueBy(int mode2) {
        switch (mode2) {
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
