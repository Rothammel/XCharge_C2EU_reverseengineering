package org.apache.mina.filter.codec.demux;

public interface MessageEncoderFactory<T> {
    MessageEncoder<T> getEncoder() throws Exception;
}
