package com.xcharge.charger.data.bean.type;

/* loaded from: classes.dex */
public enum WORK_MODE {
    personal("personal"),
    group("group"),
    Public("public");
    
    private String mode;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static WORK_MODE[] valuesCustom() {
        WORK_MODE[] valuesCustom = values();
        int length = valuesCustom.length;
        WORK_MODE[] work_modeArr = new WORK_MODE[length];
        System.arraycopy(valuesCustom, 0, work_modeArr, 0, length);
        return work_modeArr;
    }

    WORK_MODE(String mode) {
        this.mode = null;
        this.mode = mode;
    }

    public String getMode() {
        return this.mode;
    }

    public static final WORK_MODE valueBy(String value) {
        if ("personal".equals(value)) {
            return personal;
        }
        if ("group".equals(value)) {
            return group;
        }
        if ("public".equals(value)) {
            return Public;
        }
        return null;
    }
}
