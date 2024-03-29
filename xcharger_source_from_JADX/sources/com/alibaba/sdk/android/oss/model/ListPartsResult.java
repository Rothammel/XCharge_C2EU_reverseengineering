package com.alibaba.sdk.android.oss.model;

import java.util.ArrayList;
import java.util.List;

public class ListPartsResult extends OSSResult {
    private String bucketName;
    private boolean isTruncated;
    private String key;
    private Integer maxParts;
    private Integer nextPartNumberMarker;
    private Integer partNumberMarker;
    private List<PartSummary> parts = new ArrayList();
    private String storageClass;
    private String uploadId;

    public String getBucketName() {
        return this.bucketName;
    }

    public void setBucketName(String bucketName2) {
        this.bucketName = bucketName2;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key2) {
        this.key = key2;
    }

    public String getUploadId() {
        return this.uploadId;
    }

    public void setUploadId(String uploadId2) {
        this.uploadId = uploadId2;
    }

    public String getStorageClass() {
        return this.storageClass;
    }

    public void setStorageClass(String storageClass2) {
        this.storageClass = storageClass2;
    }

    public Integer getPartNumberMarker() {
        return this.partNumberMarker;
    }

    public void setPartNumberMarker(int partNumberMarker2) {
        this.partNumberMarker = Integer.valueOf(partNumberMarker2);
    }

    public Integer getNextPartNumberMarker() {
        return this.nextPartNumberMarker;
    }

    public void setNextPartNumberMarker(int nextPartNumberMarker2) {
        this.nextPartNumberMarker = Integer.valueOf(nextPartNumberMarker2);
    }

    public Integer getMaxParts() {
        return this.maxParts;
    }

    public void setMaxParts(int maxParts2) {
        this.maxParts = Integer.valueOf(maxParts2);
    }

    public boolean isTruncated() {
        return this.isTruncated;
    }

    public void setTruncated(boolean isTruncated2) {
        this.isTruncated = isTruncated2;
    }

    public List<PartSummary> getParts() {
        return this.parts;
    }

    public void setParts(List<PartSummary> parts2) {
        this.parts.clear();
        if (parts2 != null && !parts2.isEmpty()) {
            this.parts.addAll(parts2);
        }
    }
}
