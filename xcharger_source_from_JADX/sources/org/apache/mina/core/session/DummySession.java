package org.apache.mina.core.session;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import org.apache.mina.core.file.FileRegion;
import org.apache.mina.core.filterchain.DefaultIoFilterChain;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.service.AbstractIoAcceptor;
import org.apache.mina.core.service.DefaultTransportMetadata;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.TransportMetadata;
import org.apache.mina.core.write.WriteRequest;

public class DummySession extends AbstractIoSession {
    private static final SocketAddress ANONYMOUS_ADDRESS = new SocketAddress() {
        private static final long serialVersionUID = -496112902353454179L;

        public String toString() {
            return "?";
        }
    };
    /* access modifiers changed from: private */
    public static final TransportMetadata TRANSPORT_METADATA = new DefaultTransportMetadata("mina", "dummy", false, false, SocketAddress.class, IoSessionConfig.class, Object.class);
    private volatile IoSessionConfig config = new AbstractIoSessionConfig() {
    };
    private final IoFilterChain filterChain = new DefaultIoFilterChain(this);
    private volatile IoHandler handler = new IoHandlerAdapter();
    private volatile SocketAddress localAddress = ANONYMOUS_ADDRESS;
    private final IoProcessor<IoSession> processor = new IoProcessor<IoSession>() {
        public void add(IoSession session) {
        }

        public void flush(IoSession session) {
            DummySession s = (DummySession) session;
            WriteRequest req = s.getWriteRequestQueue().poll(session);
            if (req != null) {
                Object m = req.getMessage();
                if (m instanceof FileRegion) {
                    FileRegion file = (FileRegion) m;
                    try {
                        file.getFileChannel().position(file.getPosition() + file.getRemainingBytes());
                        file.update(file.getRemainingBytes());
                    } catch (IOException e) {
                        s.getFilterChain().fireExceptionCaught(e);
                    }
                }
                DummySession.this.getFilterChain().fireMessageSent(req);
            }
        }

        public void write(IoSession session, WriteRequest writeRequest) {
            session.getWriteRequestQueue().offer(session, writeRequest);
            if (!session.isWriteSuspended()) {
                flush(session);
            }
        }

        public void remove(IoSession session) {
            if (!session.getCloseFuture().isClosed()) {
                session.getFilterChain().fireSessionClosed();
            }
        }

        public void updateTrafficControl(IoSession session) {
        }

        public void dispose() {
        }

        public boolean isDisposed() {
            return false;
        }

        public boolean isDisposing() {
            return false;
        }
    };
    private volatile SocketAddress remoteAddress = ANONYMOUS_ADDRESS;
    private volatile IoService service = super.getService();
    private volatile TransportMetadata transportMetadata = TRANSPORT_METADATA;

    public DummySession() {
        super(new AbstractIoAcceptor(new AbstractIoSessionConfig() {
        }, new Executor() {
            public void execute(Runnable command) {
            }
        }) {
            /* access modifiers changed from: protected */
            public Set<SocketAddress> bindInternal(List<? extends SocketAddress> list) throws Exception {
                throw new UnsupportedOperationException();
            }

            /* access modifiers changed from: protected */
            public void unbind0(List<? extends SocketAddress> list) throws Exception {
                throw new UnsupportedOperationException();
            }

            public IoSession newSession(SocketAddress remoteAddress, SocketAddress localAddress) {
                throw new UnsupportedOperationException();
            }

            public TransportMetadata getTransportMetadata() {
                return DummySession.TRANSPORT_METADATA;
            }

            /* access modifiers changed from: protected */
            public void dispose0() throws Exception {
            }

            public IoSessionConfig getSessionConfig() {
                return this.sessionConfig;
            }
        });
        try {
            IoSessionDataStructureFactory factory = new DefaultIoSessionDataStructureFactory();
            setAttributeMap(factory.getAttributeMap(this));
            setWriteRequestQueue(factory.getWriteRequestQueue(this));
        } catch (Exception e) {
            throw new InternalError();
        }
    }

    public IoSessionConfig getConfig() {
        return this.config;
    }

    public void setConfig(IoSessionConfig config2) {
        if (config2 == null) {
            throw new IllegalArgumentException("config");
        }
        this.config = config2;
    }

    public IoFilterChain getFilterChain() {
        return this.filterChain;
    }

    public IoHandler getHandler() {
        return this.handler;
    }

    public void setHandler(IoHandler handler2) {
        if (handler2 == null) {
            throw new IllegalArgumentException("handler");
        }
        this.handler = handler2;
    }

    public SocketAddress getLocalAddress() {
        return this.localAddress;
    }

    public SocketAddress getRemoteAddress() {
        return this.remoteAddress;
    }

    public void setLocalAddress(SocketAddress localAddress2) {
        if (localAddress2 == null) {
            throw new IllegalArgumentException("localAddress");
        }
        this.localAddress = localAddress2;
    }

    public void setRemoteAddress(SocketAddress remoteAddress2) {
        if (remoteAddress2 == null) {
            throw new IllegalArgumentException("remoteAddress");
        }
        this.remoteAddress = remoteAddress2;
    }

    public IoService getService() {
        return this.service;
    }

    public void setService(IoService service2) {
        if (service2 == null) {
            throw new IllegalArgumentException("service");
        }
        this.service = service2;
    }

    public final IoProcessor<IoSession> getProcessor() {
        return this.processor;
    }

    public TransportMetadata getTransportMetadata() {
        return this.transportMetadata;
    }

    public void setTransportMetadata(TransportMetadata transportMetadata2) {
        if (transportMetadata2 == null) {
            throw new IllegalArgumentException("transportMetadata");
        }
        this.transportMetadata = transportMetadata2;
    }

    public void setScheduledWriteBytes(int byteCount) {
        super.setScheduledWriteBytes(byteCount);
    }

    public void setScheduledWriteMessages(int messages) {
        super.setScheduledWriteMessages(messages);
    }

    public void updateThroughput(boolean force) {
        super.updateThroughput(System.currentTimeMillis(), force);
    }
}
