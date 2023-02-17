package org.apache.mina.filter.codec.serialization;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class ObjectSerializationDecoder extends CumulativeProtocolDecoder {
    private final ClassLoader classLoader;
    private int maxObjectSize;

    public ObjectSerializationDecoder() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public ObjectSerializationDecoder(ClassLoader classLoader2) {
        this.maxObjectSize = 1048576;
        if (classLoader2 == null) {
            throw new IllegalArgumentException("classLoader");
        }
        this.classLoader = classLoader2;
    }

    public int getMaxObjectSize() {
        return this.maxObjectSize;
    }

    public void setMaxObjectSize(int maxObjectSize2) {
        if (maxObjectSize2 <= 0) {
            throw new IllegalArgumentException("maxObjectSize: " + maxObjectSize2);
        }
        this.maxObjectSize = maxObjectSize2;
    }

    /* access modifiers changed from: protected */
    public boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        if (!in.prefixedDataAvailable(4, this.maxObjectSize)) {
            return false;
        }
        out.write(in.getObject(this.classLoader));
        return true;
    }
}
