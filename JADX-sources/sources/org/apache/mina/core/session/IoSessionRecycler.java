package org.apache.mina.core.session;

import java.net.SocketAddress;

/* loaded from: classes.dex */
public interface IoSessionRecycler {
    public static final IoSessionRecycler NOOP = new IoSessionRecycler() { // from class: org.apache.mina.core.session.IoSessionRecycler.1
        @Override // org.apache.mina.core.session.IoSessionRecycler
        public void put(IoSession session) {
        }

        @Override // org.apache.mina.core.session.IoSessionRecycler
        public IoSession recycle(SocketAddress remoteAddress) {
            return null;
        }

        @Override // org.apache.mina.core.session.IoSessionRecycler
        public void remove(IoSession session) {
        }
    };

    void put(IoSession ioSession);

    IoSession recycle(SocketAddress socketAddress);

    void remove(IoSession ioSession);
}
