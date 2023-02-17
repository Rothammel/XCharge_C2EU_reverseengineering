package com.xcharge.charger.ui.adapter.type;

import com.xcharge.charger.data.bean.UpgradeProgress;

/* loaded from: classes.dex */
public enum UPGRADE_UI_SATAGE {
    download(UpgradeProgress.STAGE_DOWNLOAD),
    check("check"),
    update("update"),
    reboot("reboot");
    
    private String stage;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static UPGRADE_UI_SATAGE[] valuesCustom() {
        UPGRADE_UI_SATAGE[] valuesCustom = values();
        int length = valuesCustom.length;
        UPGRADE_UI_SATAGE[] upgrade_ui_satageArr = new UPGRADE_UI_SATAGE[length];
        System.arraycopy(valuesCustom, 0, upgrade_ui_satageArr, 0, length);
        return upgrade_ui_satageArr;
    }

    UPGRADE_UI_SATAGE(String stage) {
        this.stage = null;
        this.stage = stage;
    }

    public String getStage() {
        return this.stage;
    }
}
