package com.xcharge.charger.data.bean.device;

import com.xcharge.charger.data.bean.UpgradeProgress;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class Software extends JsonBean<Software> {
    private String os = "Android";
    private String osVer = null;
    private String appVer = null;
    private String firewareVer = null;
    private UpgradeProgress upgradeProgress = new UpgradeProgress();

    public String getOs() {
        return this.os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getOsVer() {
        return this.osVer;
    }

    public void setOsVer(String osVer) {
        this.osVer = osVer;
    }

    public String getAppVer() {
        return this.appVer;
    }

    public void setAppVer(String appVer) {
        this.appVer = appVer;
    }

    public String getFirewareVer() {
        return this.firewareVer;
    }

    public void setFirewareVer(String firewareVer) {
        this.firewareVer = firewareVer;
    }

    public UpgradeProgress getUpgradeProgress() {
        return this.upgradeProgress;
    }

    public void setUpgradeProgress(UpgradeProgress upgradeProgress) {
        this.upgradeProgress = upgradeProgress;
    }
}
