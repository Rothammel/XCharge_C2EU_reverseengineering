package com.xcharge.charger.protocol.family.xcloud.bean.cloud;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class RequestUpgrade extends JsonBean<RequestUpgrade> {
    public static final String COM_APP = "app";
    public static final String COM_DCB = "dcb";
    public static final String COM_OS = "os";
    private Long sid = null;
    private String component = COM_OS;
    private String version = null;
    private String dependentVersion = null;
    private boolean partial = false;
    private String fileUrl = null;
    private long fileSize = 0;
    private String fileMD5 = null;
    private long time = 0;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }

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

    public boolean isPartial() {
        return this.partial;
    }

    public void setPartial(boolean partial) {
        this.partial = partial;
    }

    public String getFileUrl() {
        return this.fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public long getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileMD5() {
        return this.fileMD5;
    }

    public void setFileMD5(String fileMD5) {
        this.fileMD5 = fileMD5;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}