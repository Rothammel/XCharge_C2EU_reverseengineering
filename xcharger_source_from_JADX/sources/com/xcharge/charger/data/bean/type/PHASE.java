package com.xcharge.charger.data.bean.type;

public enum PHASE {
    UNKOWN_PHASE(0),
    SINGLE_PHASE(1),
    THREE_PHASE(2),
    DC_PHASE(3);
    
    private int phase;

    private PHASE(int phase2) {
        this.phase = 0;
        this.phase = phase2;
    }

    public static PHASE valueBy(int phase2) {
        switch (phase2) {
            case 0:
                return UNKOWN_PHASE;
            case 1:
                return SINGLE_PHASE;
            case 2:
                return THREE_PHASE;
            case 3:
                return DC_PHASE;
            default:
                return UNKOWN_PHASE;
        }
    }

    public int getPhase() {
        return this.phase;
    }
}
