package com.xcharge.charger.ui.adapter.type;

/* loaded from: classes.dex */
public enum UI_STATUS {
    asleep("asleep"),
    home_boot("home_boot"),
    home_present("home_present"),
    wait_home("wait_home"),
    home_create("home_create");
    
    private String status;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static UI_STATUS[] valuesCustom() {
        UI_STATUS[] valuesCustom = values();
        int length = valuesCustom.length;
        UI_STATUS[] ui_statusArr = new UI_STATUS[length];
        System.arraycopy(valuesCustom, 0, ui_statusArr, 0, length);
        return ui_statusArr;
    }

    UI_STATUS(String status) {
        this.status = null;
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}
