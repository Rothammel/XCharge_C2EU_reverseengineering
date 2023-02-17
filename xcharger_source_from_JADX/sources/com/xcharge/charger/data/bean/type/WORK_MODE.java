package com.xcharge.charger.data.bean.type;

public enum WORK_MODE {
    personal("personal"),
    group("group"),
    Public("public");
    
    private String mode;

    private WORK_MODE(String mode2) {
        this.mode = null;
        this.mode = mode2;
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
