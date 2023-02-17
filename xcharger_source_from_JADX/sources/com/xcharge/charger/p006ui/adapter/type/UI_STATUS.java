package com.xcharge.charger.p006ui.adapter.type;

/* renamed from: com.xcharge.charger.ui.adapter.type.UI_STATUS */
public enum UI_STATUS {
    asleep("asleep"),
    home_boot("home_boot"),
    home_present("home_present"),
    wait_home("wait_home"),
    home_create("home_create");
    
    private String status;

    private UI_STATUS(String status2) {
        this.status = null;
        this.status = status2;
    }

    public String getStatus() {
        return this.status;
    }
}
