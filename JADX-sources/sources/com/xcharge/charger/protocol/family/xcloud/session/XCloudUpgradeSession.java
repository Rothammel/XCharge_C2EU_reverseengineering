package com.xcharge.charger.protocol.family.xcloud.session;

import com.xcharge.charger.protocol.family.xcloud.bean.cloud.RequestUpgrade;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class XCloudUpgradeSession extends JsonBean<XCloudUpgradeSession> {
    private String stage = null;
    private RequestUpgrade requestUpgrade = null;
    private String downloadFile = null;
    private Long UpgradeDCAPRequestSeq = null;

    public String getStage() {
        return this.stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public RequestUpgrade getRequestUpgrade() {
        return this.requestUpgrade;
    }

    public void setRequestUpgrade(RequestUpgrade requestUpgrade) {
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
