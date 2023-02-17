package org.apache.mina.filter.codec;

import org.apache.mina.core.future.WriteFuture;

public interface ProtocolEncoderOutput {
    WriteFuture flush();

    void mergeAll();

    void write(Object obj);
}
