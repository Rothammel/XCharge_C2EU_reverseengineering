package com.xcharge.charger.data.bean;

import com.xcharge.common.bean.JsonBean;

public class ContentItem extends JsonBean<ContentItem> {
    private long duration = 0;
    private String fileMD5 = null;
    private long fileSize = 0;
    private String fileUrl = null;
    private String localPath = null;
    private String text = null;
    private String type = null;

    public String getType() {
        return this.type;
    }

    public void setType(String type2) {
        this.type = type2;
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

    public String getLocalPath() {
        return this.localPath;
    }

    public void setLocalPath(String localPath2) {
        this.localPath = localPath2;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text2) {
        this.text = text2;
    }

    public long getDuration() {
        return this.duration;
    }

    public void setDuration(long duration2) {
        this.duration = duration2;
    }

    public ContentItem clone() {
        return (ContentItem) deepClone();
    }
}
