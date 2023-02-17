package org.apache.mina.core.write;

import java.net.SocketAddress;
import org.apache.mina.core.future.WriteFuture;

public interface WriteRequest {
    SocketAddress getDestination();

    WriteFuture getFuture();

    Object getMessage();

    WriteRequest getOriginalRequest();

    boolean isEncoded();
}
