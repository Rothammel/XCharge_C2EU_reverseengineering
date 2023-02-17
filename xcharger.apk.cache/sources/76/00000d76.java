package org.apache.mina.core.write;

import java.net.SocketAddress;
import org.apache.mina.core.future.WriteFuture;

/* loaded from: classes.dex */
public class WriteRequestWrapper implements WriteRequest {
    private final WriteRequest parentRequest;

    public WriteRequestWrapper(WriteRequest parentRequest) {
        if (parentRequest == null) {
            throw new IllegalArgumentException("parentRequest");
        }
        this.parentRequest = parentRequest;
    }

    @Override // org.apache.mina.core.write.WriteRequest
    public SocketAddress getDestination() {
        return this.parentRequest.getDestination();
    }

    @Override // org.apache.mina.core.write.WriteRequest
    public WriteFuture getFuture() {
        return this.parentRequest.getFuture();
    }

    @Override // org.apache.mina.core.write.WriteRequest
    public Object getMessage() {
        return this.parentRequest.getMessage();
    }

    @Override // org.apache.mina.core.write.WriteRequest
    public WriteRequest getOriginalRequest() {
        return this.parentRequest.getOriginalRequest();
    }

    public WriteRequest getParentRequest() {
        return this.parentRequest;
    }

    public String toString() {
        return "WR Wrapper" + this.parentRequest.toString();
    }

    @Override // org.apache.mina.core.write.WriteRequest
    public boolean isEncoded() {
        return false;
    }
}