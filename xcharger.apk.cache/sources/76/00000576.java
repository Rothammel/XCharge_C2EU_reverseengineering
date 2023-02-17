package com.xcharge.charger.data.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ContentItem extends JsonBean<ContentItem> {
    private String type = null;
    private String fileUrl = null;
    private long fileSize = 0;
    private String fileMD5 = null;
    private String localPath = null;
    private String text = null;
    private long duration = 0;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getLocalPath() {
        return this.localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getDuration() {
        return this.duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    /* renamed from: clone */
    public ContentItem m7clone() {
        return deepClone();
    }
}