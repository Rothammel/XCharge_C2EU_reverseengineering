package com.alibaba.sdk.android.oss.model;

/* loaded from: classes.dex */
public class AppendObjectResult extends OSSResult {
    private long nextPosition;
    private String objectCRC64;

    public long getNextPosition() {
        return this.nextPosition;
    }

    public void setNextPosition(Long nextPosition) {
        this.nextPosition = nextPosition.longValue();
    }

    public String getObjectCRC64() {
        return this.objectCRC64;
    }

    public void setObjectCRC64(String objectCRC64) {
        this.objectCRC64 = objectCRC64;
    }
}
