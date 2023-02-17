package org.apache.mina.filter.codec;

import org.apache.mina.core.session.IoSession;

public interface ProtocolCodecFactory {
    ProtocolDecoder getDecoder(IoSession ioSession) throws Exception;

    ProtocolEncoder getEncoder(IoSession ioSession) throws Exception;
}
