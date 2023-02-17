package com.xcharge.charger.protocol.anyo.session;

import com.xcharge.charger.protocol.anyo.bean.request.StartUpgradeRequest;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class AnyoUpgradeSession extends JsonBean<AnyoUpgradeSession> {
    private String stage = null;
    private StartUpgradeRequest requestUpgrade = null;
    private String downloadFile = null;
    private Long UpgradeDCAPRequestSeq = null;

    public String getStage() {
        return this.stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public StartUpgradeRequest getRequestUpgrade() {
        return this.requestUpgrade;
    }

    public void setRequestUpgrade(StartUpgradeRequest requestUpgrade) {
        this.requestUpgrade = requestUpgrade;
    }

    public Long getUpgradeDCAPRequestSeq() {
        return this.UpgradeDCAPRequestSeq;
    }

    public void setUpgradeDCAPRequestSeq(Long upgradeDCAPRequestSeq) {
        this.UpgradeDCAPRequestSeq = upgradeDCAPRequestSeq;
    }

    public String getDownloadFile() {
        return this.downloadFile;
    }

    public void setDownloadFile(String downloadFile) {
        this.downloadFile = downloadFile;
    }
}
