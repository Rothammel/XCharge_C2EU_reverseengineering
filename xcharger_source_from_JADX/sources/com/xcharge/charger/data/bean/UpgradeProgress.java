package com.xcharge.charger.data.bean;

import com.xcharge.common.bean.JsonBean;

public class UpgradeProgress extends JsonBean<UpgradeProgress> {
    public static final int SC_CHECK_FIREWARE_PROGRESS = 6;
    public static final int SC_CHECK_INTEGRITY = 3;
    public static final int SC_DOWNLOAD_PROGRESS = 2;
    public static final int SC_FINISH_UPDATE = 8;
    public static final int SC_START_CHECK_FIREWARE = 5;
    public static final int SC_START_DOWNLOAD = 1;
    public static final int SC_START_UNZIP = 4;
    public static final int SC_START_UPDATE = 7;
    public static final String STAGE_DOWNLOAD = "download";
    public static final String STAGE_UPDATE = "update";
    private ErrorCode error = new ErrorCode(200);
    private int progress = 0;
    private String stage = null;
    private int status = 0;
    private UpgradeData upgradeData = null;

    public String getStage() {
        return this.stage;
    }

    public void setStage(String stage2) {
        this.stage = stage2;
    }

    public int getProgress() {
        return this.progress;
    }

    public void setProgress(int progress2) {
        this.progress = progress2;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status2) {
        this.status = status2;
    }

    public ErrorCode getError() {
        return this.error;
    }

    public void setError(ErrorCode error2) {
        this.error = error2;
    }

    public UpgradeData getUpgradeData() {
        return this.upgradeData;
    }

    public void setUpgradeData(UpgradeData upgradeData2) {
        this.upgradeData = upgradeData2;
    }
}
