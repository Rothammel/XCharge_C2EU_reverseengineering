package org.apache.mina.filter.codec.demux;

public interface MessageDecoderFactory {
    MessageDecoder getDecoder() throws Exception;
}
