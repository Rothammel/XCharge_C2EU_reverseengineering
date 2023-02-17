package com.xcharge.charger.protocol.ocpp.session;

import com.xcharge.charger.protocol.ocpp.bean.cloud.UpdateFirmwareReq;
import com.xcharge.common.bean.JsonBean;

public class OcppUpgradeSession extends JsonBean<OcppUpgradeSession> {
    private Long UpgradeDCAPRequestSeq = null;
    private String downloadFile = null;
    private String stage = null;
    private UpdateFirmwareReq updateFirmwareReq = null;

    public String getStage() {
        return this.stage;
    }

    public void setStage(String stage2) {
        this.stage = stage2;
    }

    public UpdateFirmwareReq getUpdateFirmwareReq() {
        return this.updateFirmwareReq;
    }

    public void setUpdateFirmwareReq(UpdateFirmwareReq updateFirmwareReq2) {
        this.updateFirmwareReq = updateFirmwareReq2;
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
