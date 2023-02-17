package org.apache.mina.core.session;

import java.net.SocketAddress;

public interface IoSessionRecycler {
    public static final IoSessionRecycler NOOP = new IoSessionRecycler() {
        public void put(IoSession session) {
        }

        public IoSession recycle(SocketAddress remoteAddress) {
            return null;
        }

        public void remove(IoSession session) {
        }
    };

    void put(IoSession ioSession);

    IoSession recycle(SocketAddress socketAddress);

    void remove(IoSession ioSession);
}
