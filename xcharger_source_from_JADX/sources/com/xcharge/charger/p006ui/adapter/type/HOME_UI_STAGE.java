package com.xcharge.charger.p006ui.adapter.type;

/* renamed from: com.xcharge.charger.ui.adapter.type.HOME_UI_STAGE */
public enum HOME_UI_STAGE {
    booting("booting"),
    boot_error("boot_error"),
    normal("normal");
    
    private String stage;

    private HOME_UI_STAGE(String stage2) {
        this.stage = null;
        this.stage = stage2;
    }

    public String getStage() {
        return this.stage;
    }
}
