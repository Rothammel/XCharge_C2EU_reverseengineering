package com.xcharge.charger.data.bean.device;

import com.xcharge.charger.data.bean.UpgradeProgress;
import com.xcharge.common.bean.JsonBean;

public class Software extends JsonBean<Software> {
    private String appVer = null;
    private String firewareVer = null;

    /* renamed from: os */
    private String f55os = "Android";
    private String osVer = null;
    private UpgradeProgress upgradeProgress = new UpgradeProgress();

    public String getOs() {
        return this.f55os;
    }

    public void setOs(String os) {
        this.f55os = os;
    }

    public String getOsVer() {
        return this.osVer;
    }

    public void setOsVer(String osVer2) {
        this.osVer = osVer2;
    }

    public String getAppVer() {
        return this.appVer;
    }

    public void setAppVer(String appVer2) {
        this.appVer = appVer2;
    }

    public String getFirewareVer() {
        return this.firewareVer;
    }

    public void setFirewareVer(String firewareVer2) {
        this.firewareVer = firewareVer2;
    }

    public UpgradeProgress getUpgradeProgress() {
        return this.upgradeProgress;
    }

    public void setUpgradeProgress(UpgradeProgress upgradeProgress2) {
        this.upgradeProgress = upgradeProgress2;
    }
}
