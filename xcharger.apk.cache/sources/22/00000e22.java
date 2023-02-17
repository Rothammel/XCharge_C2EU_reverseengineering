package org.apache.mina.proxy.filter;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.IoBufferWrapper;

/* loaded from: classes.dex */
public class ProxyHandshakeIoBuffer extends IoBufferWrapper {
    public ProxyHandshakeIoBuffer(IoBuffer buf) {
        super(buf);
    }
}