package org.apache.mina.core.write;

import java.net.SocketAddress;
import org.apache.mina.core.future.WriteFuture;

public class WriteRequestWrapper implements WriteRequest {
    private final WriteRequest parentRequest;

    public WriteRequestWrapper(WriteRequest parentRequest2) {
        if (parentRequest2 == null) {
            throw new IllegalArgumentException("parentRequest");
        }
        this.parentRequest = parentRequest2;
    }

    public SocketAddress getDestination() {
        return this.parentRequest.getDestination();
    }

    public WriteFuture getFuture() {
        return this.parentRequest.getFuture();
    }

    public Object getMessage() {
        return this.parentRequest.getMessage();
    }

    public WriteRequest getOriginalRequest() {
        return this.parentRequest.getOriginalRequest();
    }

    public WriteRequest getParentRequest() {
        return this.parentRequest;
    }

    public String toString() {
        return "WR Wrapper" + this.parentRequest.toString();
    }

    public boolean isEncoded() {
        return false;
    }
}
