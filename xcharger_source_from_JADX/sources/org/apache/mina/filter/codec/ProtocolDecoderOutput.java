package org.apache.mina.filter.codec;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.session.IoSession;

public interface ProtocolDecoderOutput {
    void flush(IoFilter.NextFilter nextFilter, IoSession ioSession);

    void write(Object obj);
}
