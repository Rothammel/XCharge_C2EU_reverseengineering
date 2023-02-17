package com.xcharge.charger.data.bean.type;

public enum SWITCH_STATUS {
    disable("disable"),
    on("on"),
    off("off"),
    fault("fault");
    
    private String status;

    private SWITCH_STATUS(String status2) {
        this.status = null;
        this.status = status2;
    }

    public String getStatus() {
        return this.status;
    }
}
