package com.xcharge.charger.data.bean;

import com.xcharge.common.bean.JsonBean;

public class UpgradeData extends JsonBean<UpgradeData> {
    public static final String COM_ALL = "all";
    public static final String COM_APP = "app";
    public static final String COM_FIREWARE = "fireware";
    public static final String COM_LAUNCHER = "launcher";
    private String component = null;
    private String dependentVersion = null;
    private String srcPath = null;
    private String version = null;

    public String getComponent() {
        return this.component;
    }

    public void setComponent(String component2) {
        this.component = component2;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version2) {
        this.version = version2;
    }

    public String getDependentVersion() {
        return this.dependentVersion;
    }

    public void setDependentVersion(String dependentVersion2) {
        this.dependentVersion = dependentVersion2;
    }

    public String getSrcPath() {
        return this.srcPath;
    }

    public void setSrcPath(String srcPath2) {
        this.srcPath = srcPath2;
    }
}
