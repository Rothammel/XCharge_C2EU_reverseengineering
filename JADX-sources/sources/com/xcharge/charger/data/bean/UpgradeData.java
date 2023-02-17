package com.xcharge.charger.data.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class UpgradeData extends JsonBean<UpgradeData> {
    public static final String COM_ALL = "all";
    public static final String COM_APP = "app";
    public static final String COM_FIREWARE = "fireware";
    public static final String COM_LAUNCHER = "launcher";
    private String component = null;
    private String version = null;
    private String dependentVersion = null;
    private String srcPath = null;

    public String getComponent() {
        return this.component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDependentVersion() {
        return this.dependentVersion;
    }

    public void setDependentVersion(String dependentVersion) {
        this.dependentVersion = dependentVersion;
    }

    public String getSrcPath() {
        return this.srcPath;
    }

    public void setSrcPath(String srcPath) {
        this.srcPath = srcPath;
    }
}
