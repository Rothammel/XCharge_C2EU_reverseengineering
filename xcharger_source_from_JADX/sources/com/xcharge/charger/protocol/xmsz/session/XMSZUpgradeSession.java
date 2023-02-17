package com.xcharge.charger.protocol.xmsz.session;

import com.xcharge.charger.protocol.xmsz.bean.cloud.UpdateFirmwareRequest;
import com.xcharge.common.bean.JsonBean;

public class XMSZUpgradeSession extends JsonBean<XMSZUpgradeSession> {
    private Long UpgradeDCAPRequestSeq = null;
    private String downloadFile = null;
    private UpdateFirmwareRequest requestUpgrade = null;
    private String stage = null;

    public String getStage() {
        return this.stage;
    }

    public void setStage(String stage2) {
        this.stage = stage2;
    }

    public UpdateFirmwareRequest getRequestUpgrade() {
        return this.requestUpgrade;
    }

    public void setRequestUpgrade(UpdateFirmwareRequest requestUpgrade2) {
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
