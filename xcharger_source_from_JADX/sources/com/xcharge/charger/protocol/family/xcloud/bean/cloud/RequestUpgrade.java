package com.xcharge.charger.protocol.family.xcloud.bean.cloud;

import com.xcharge.common.bean.JsonBean;

public class RequestUpgrade extends JsonBean<RequestUpgrade> {
    public static final String COM_APP = "app";
    public static final String COM_DCB = "dcb";
    public static final String COM_OS = "os";
    private String component = COM_OS;
    private String dependentVersion = null;
    private String fileMD5 = null;
    private long fileSize = 0;
    private String fileUrl = null;
    private boolean partial = false;
    private Long sid = null;
    private long time = 0;
    private String version = null;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid2) {
        this.sid = sid2;
    }

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

    public boolean isPartial() {
        return this.partial;
    }

    public void setPartial(boolean partial2) {
        this.partial = partial2;
    }

    public String getFileUrl() {
        return this.fileUrl;
    }

    public void setFileUrl(String fileUrl2) {
        this.fileUrl = fileUrl2;
    }

    public long getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(long fileSize2) {
        this.fileSize = fileSize2;
    }

    public String getFileMD5() {
        return this.fileMD5;
    }

    public void setFileMD5(String fileMD52) {
        this.fileMD5 = fileMD52;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }
}
