package com.xcharge.charger.protocol.anyo.session;

import com.xcharge.charger.protocol.anyo.bean.request.StartUpgradeRequest;
import com.xcharge.common.bean.JsonBean;

public class AnyoUpgradeSession extends JsonBean<AnyoUpgradeSession> {
    private Long UpgradeDCAPRequestSeq = null;
    private String downloadFile = null;
    private StartUpgradeRequest requestUpgrade = null;
    private String stage = null;

    public String getStage() {
        return this.stage;
    }

    public void setStage(String stage2) {
        this.stage = stage2;
    }

    public StartUpgradeRequest getRequestUpgrade() {
        return this.requestUpgrade;
    }

    public void setRequestUpgrade(StartUpgradeRequest requestUpgrade2) {
        this.requestUpgrade = requestUpgrade2;
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

    public void setDownloadFile(String downloadFile2) {
        this.downloadFile = downloadFile2;
    }
}
