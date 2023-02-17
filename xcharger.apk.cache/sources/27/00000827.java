package com.xcharge.charger.ui.adapter.type;

/* loaded from: classes.dex */
public enum HOME_UI_STAGE {
    booting("booting"),
    boot_error("boot_error"),
    normal("normal");
    
    private String stage;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static HOME_UI_STAGE[] valuesCustom() {
        HOME_UI_STAGE[] valuesCustom = values();
        int length = valuesCustom.length;
        HOME_UI_STAGE[] home_ui_stageArr = new HOME_UI_STAGE[length];
        System.arraycopy(valuesCustom, 0, home_ui_stageArr, 0, length);
        return home_ui_stageArr;
    }

    HOME_UI_STAGE(String stage) {
        this.stage = null;
        this.stage = stage;
    }

    public String getStage() {
        return this.stage;
    }
}