package com.xcharge.charger.data.bean.type;

public enum BLN_MODE {
    on_off(0),
    bln(1);
    
    private int mode;

    private BLN_MODE(int mode2) {
        this.mode = 0;
        this.mode = mode2;
    }

    public static BLN_MODE valueBy(int mode2) {
        switch (mode2) {
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
