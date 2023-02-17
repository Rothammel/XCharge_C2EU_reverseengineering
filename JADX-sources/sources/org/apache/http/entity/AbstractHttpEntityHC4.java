package org.apache.http.entity;

import java.io.IOException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.message.BasicHeader;

@NotThreadSafe
/* loaded from: classes.dex */
public abstract class AbstractHttpEntityHC4 implements HttpEntity {
    protected static final int OUTPUT_BUFFER_SIZE = 4096;
    protected boolean chunked;
    protected Header contentEncoding;
    protected Header contentType;

    public Header getContentType() {
        return this.contentType;
    }

    public Header getContentEncoding() {
        return this.contentEncoding;
    }

    public boolean isChunked() {
        return this.chunked;
    }

    public void setContentType(Header contentType) {
        this.contentType = contentType;
    }

    public void setContentType(String ctString) {
        BasicHeader basicHeader = null;
        if (ctString != null) {
            basicHeader = new BasicHeader("Content-Type", ctString);
        }
        setContentType((Header) basicHeader);
    }

    public void setContentEncoding(Header contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public void setContentEncoding(String ceString) {
        BasicHeader basicHeader = null;
        if (ceString != null) {
            basicHeader = new BasicHeader("Content-Encoding", ceString);
        }
        setContentEncoding((Header) basicHeader);
    }

    public void setChunked(boolean b) {
        this.chunked = b;
    }

    @Deprecated
    public void consumeContent() throws IOException {
    }
}
