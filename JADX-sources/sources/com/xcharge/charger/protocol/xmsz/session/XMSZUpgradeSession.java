package com.xcharge.charger.protocol.xmsz.session;

import com.xcharge.charger.protocol.xmsz.bean.cloud.UpdateFirmwareRequest;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class XMSZUpgradeSession extends JsonBean<XMSZUpgradeSession> {
    private String stage = null;
    private UpdateFirmwareRequest requestUpgrade = null;
    private String downloadFile = null;
    private Long UpgradeDCAPRequestSeq = null;

    public String getStage() {
        return this.stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public UpdateFirmwareRequest getRequestUpgrade() {
        return this.requestUpgrade;
    }

    public void setRequestUpgrade(UpdateFirmwareRequest requestUpgrade) {
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
