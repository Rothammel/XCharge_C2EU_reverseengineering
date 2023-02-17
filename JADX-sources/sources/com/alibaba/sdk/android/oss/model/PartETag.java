package com.alibaba.sdk.android.oss.model;

/* loaded from: classes.dex */
public class PartETag {
    private long crc64;
    private String eTag;
    private int partNumber;
    private long partSize;

    public PartETag(int partNumber, String eTag) {
        setPartNumber(partNumber);
        setETag(eTag);
    }

    public int getPartNumber() {
        return this.partNumber;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    public String getETag() {
        return this.eTag;
    }

    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    public long getPartSize() {
        return this.partSize;
    }

    public void setPartSize(long partSize) {
        this.partSize = partSize;
    }

    public long getCRC64() {
        return this.crc64;
    }

    public void setCRC64(long crc64) {
        this.crc64 = crc64;
    }
}
