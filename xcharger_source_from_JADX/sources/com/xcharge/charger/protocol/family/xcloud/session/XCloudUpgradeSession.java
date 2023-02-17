package com.xcharge.charger.protocol.family.xcloud.session;

import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestUpgrade;
import com.xcharge.common.bean.JsonBean;

public class XCloudUpgradeSession extends JsonBean<XCloudUpgradeSession> {
    private Long UpgradeDCAPRequestSeq = null;
    private String downloadFile = null;
    private RequestUpgrade requestUpgrade = null;
    private String stage = null;

    public String getStage() {
        return this.stage;
    }

    public void setStage(String stage2) {
        this.stage = stage2;
    }

    public RequestUpgrade getRequestUpgrade() {
        return this.requestUpgrade;
    }

    public void setRequestUpgrade(RequestUpgrade requestUpgrade2) {
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
