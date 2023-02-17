package com.xcharge.charger.protocol.ocpp.session;

import com.xcharge.charger.protocol.ocpp.bean.cloud.UpdateFirmwareReq;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class OcppUpgradeSession extends JsonBean<OcppUpgradeSession> {
    private String stage = null;
    private UpdateFirmwareReq updateFirmwareReq = null;
    private String downloadFile = null;
    private Long UpgradeDCAPRequestSeq = null;

    public String getStage() {
        return this.stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public UpdateFirmwareReq getUpdateFirmwareReq() {
        return this.updateFirmwareReq;
    }

    public void setUpdateFirmwareReq(UpdateFirmwareReq updateFirmwareReq) {
        this.updateFirmwareReq = updateFirmwareReq;
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
