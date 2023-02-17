package com.xcharge.charger.p006ui.adapter.type;

import com.xcharge.charger.data.bean.UpgradeProgress;

/* renamed from: com.xcharge.charger.ui.adapter.type.UPGRADE_UI_SATAGE */
public enum UPGRADE_UI_SATAGE {
    download(UpgradeProgress.STAGE_DOWNLOAD),
    check("check"),
    update("update"),
    reboot("reboot");
    
    private String stage;

    private UPGRADE_UI_SATAGE(String stage2) {
        this.stage = null;
        this.stage = stage2;
    }

    public String getStage() {
        return this.stage;
    }
}
