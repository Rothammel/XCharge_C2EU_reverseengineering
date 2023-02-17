package com.xcharge.charger.data.bean.type;

/* loaded from: classes.dex */
public enum PHASE {
    UNKOWN_PHASE(0),
    SINGLE_PHASE(1),
    THREE_PHASE(2),
    DC_PHASE(3);
    
    private int phase;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static PHASE[] valuesCustom() {
        PHASE[] valuesCustom = values();
        int length = valuesCustom.length;
        PHASE[] phaseArr = new PHASE[length];
        System.arraycopy(valuesCustom, 0, phaseArr, 0, length);
        return phaseArr;
    }

    PHASE(int phase) {
        this.phase = 0;
        this.phase = phase;
    }

    public static PHASE valueBy(int phase) {
        switch (phase) {
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