package com.alibaba.sdk.android.oss.model;

import com.alibaba.sdk.android.oss.internal.CheckCRC64DownloadInputStream;
import java.io.InputStream;

/* loaded from: classes.dex */
public class GetObjectResult extends OSSResult {
    private long contentLength;
    private ObjectMetadata metadata = new ObjectMetadata();
    private InputStream objectContent;

    public ObjectMetadata getMetadata() {
        return this.metadata;
    }

    public void setMetadata(ObjectMetadata metadata) {
        this.metadata = metadata;
    }

    public InputStream getObjectContent() {
        return this.objectContent;
    }

    public void setObjectContent(InputStream objectContent) {
        this.objectContent = objectContent;
    }

    public long getContentLength() {
        return this.contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    @Override // com.alibaba.sdk.android.oss.model.OSSResult
    public Long getClientCRC() {
        return (this.objectContent == null || !(this.objectContent instanceof CheckCRC64DownloadInputStream)) ? super.getClientCRC() : Long.valueOf(((CheckCRC64DownloadInputStream) this.objectContent).getClientCRC64());
    }
}