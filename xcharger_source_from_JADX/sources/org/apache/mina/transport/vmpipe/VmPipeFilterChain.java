package org.apache.mina.transport.vmpipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.DefaultIoFilterChain;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoEvent;
import org.apache.mina.core.session.IoEventType;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestQueue;
import org.apache.mina.core.write.WriteToClosedSessionException;

class VmPipeFilterChain extends DefaultIoFilterChain {
    private final Queue<IoEvent> eventQueue = new ConcurrentLinkedQueue();
    /* access modifiers changed from: private */
    public volatile boolean flushEnabled;
    private final IoProcessor<VmPipeSession> processor = new VmPipeIoProcessor();
    private volatile boolean sessionOpened;

    VmPipeFilterChain(AbstractIoSession session) {
        super(session);
    }

    /* access modifiers changed from: package-private */
    public IoProcessor<VmPipeSession> getProcessor() {
        return this.processor;
    }

    public void start() {
        this.flushEnabled = true;
        flushEvents();
        flushPendingDataQueues((VmPipeSession) getSession());
    }

    private void pushEvent(IoEvent e) {
        pushEvent(e, this.flushEnabled);
    }

    /* access modifiers changed from: private */
    public void pushEvent(IoEvent e, boolean flushNow) {
        this.eventQueue.add(e);
        if (flushNow) {
            flushEvents();
        }
    }

    /* access modifiers changed from: private */
    public void flushEvents() {
        while (true) {
            IoEvent e = this.eventQueue.poll();
            if (e != null) {
                fireEvent(e);
            } else {
                return;
            }
        }
    }

    private void fireEvent(IoEvent e) {
        VmPipeSession session = (VmPipeSession) getSession();
        IoEventType type = e.getType();
        Object data = e.getParameter();
        if (type == IoEventType.MESSAGE_RECEIVED) {
            if (!this.sessionOpened || session.isReadSuspended() || !session.getLock().tryLock()) {
                session.receivedMessageQueue.add(data);
                return;
            }
            try {
                if (session.isReadSuspended()) {
                    session.receivedMessageQueue.add(data);
                } else {
                    super.fireMessageReceived(data);
                }
            } finally {
                session.getLock().unlock();
            }
        } else if (type == IoEventType.WRITE) {
            super.fireFilterWrite((WriteRequest) data);
        } else if (type == IoEventType.MESSAGE_SENT) {
            super.fireMessageSent((WriteRequest) data);
        } else if (type == IoEventType.EXCEPTION_CAUGHT) {
            super.fireExceptionCaught((Throwable) data);
        } else if (type == IoEventType.SESSION_IDLE) {
            super.fireSessionIdle((IdleStatus) data);
        } else if (type == IoEventType.SESSION_OPENED) {
            super.fireSessionOpened();
            this.sessionOpened = true;
        } else if (type == IoEventType.SESSION_CREATED) {
            session.getLock().lock();
            try {
                super.fireSessionCreated();
            } finally {
                session.getLock().unlock();
            }
        } else if (type == IoEventType.SESSION_CLOSED) {
            flushPendingDataQueues(session);
            super.fireSessionClosed();
        } else if (type == IoEventType.CLOSE) {
            super.fireFilterClose();
        }
    }

    /* access modifiers changed from: private */
    public static void flushPendingDataQueues(VmPipeSession s) {
        s.getProcessor().updateTrafficControl(s);
        s.getRemoteSession().getProcessor().updateTrafficControl(s);
    }

    public void fireFilterClose() {
        pushEvent(new IoEvent(IoEventType.CLOSE, getSession(), (Object) null));
    }

    public void fireFilterWrite(WriteRequest writeRequest) {
        pushEvent(new IoEvent(IoEventType.WRITE, getSession(), writeRequest));
    }

    public void fireExceptionCaught(Throwable cause) {
        pushEvent(new IoEvent(IoEventType.EXCEPTION_CAUGHT, getSession(), cause));
    }

    public void fireMessageSent(WriteRequest request) {
        pushEvent(new IoEvent(IoEventType.MESSAGE_SENT, getSession(), request));
    }

    public void fireSessionClosed() {
        pushEvent(new IoEvent(IoEventType.SESSION_CLOSED, getSession(), (Object) null));
    }

    public void fireSessionCreated() {
        pushEvent(new IoEvent(IoEventType.SESSION_CREATED, getSession(), (Object) null));
    }

    public void fireSessionIdle(IdleStatus status) {
        pushEvent(new IoEvent(IoEventType.SESSION_IDLE, getSession(), status));
    }

    public void fireSessionOpened() {
        pushEvent(new IoEvent(IoEventType.SESSION_OPENED, getSession(), (Object) null));
    }

    public void fireMessageReceived(Object message) {
        pushEvent(new IoEvent(IoEventType.MESSAGE_RECEIVED, getSession(), message));
    }

    private class VmPipeIoProcessor implements IoProcessor<VmPipeSession> {
        private VmPipeIoProcessor() {
        }

        public void flush(VmPipeSession session) {
            WriteRequestQueue queue = session.getWriteRequestQueue0();
            if (!session.isClosing()) {
                session.getLock().lock();
                try {
                    if (!queue.isEmpty(session)) {
                        long currentTime = System.currentTimeMillis();
                        while (true) {
                            WriteRequest req = queue.poll(session);
                            if (req == null) {
                                break;
                            }
                            Object m = req.getMessage();
                            VmPipeFilterChain.this.pushEvent(new IoEvent(IoEventType.MESSAGE_SENT, session, req), false);
                            session.getRemoteSession().getFilterChain().fireMessageReceived(getMessageCopy(m));
                            if (m instanceof IoBuffer) {
                                session.increaseWrittenBytes0(((IoBuffer) m).remaining(), currentTime);
                            }
                        }
                        if (VmPipeFilterChain.this.flushEnabled) {
                            VmPipeFilterChain.this.flushEvents();
                        }
                        session.getLock().unlock();
                        VmPipeFilterChain.flushPendingDataQueues(session);
                    }
                } finally {
                    if (VmPipeFilterChain.this.flushEnabled) {
                        VmPipeFilterChain.this.flushEvents();
                    }
                    session.getLock().unlock();
                }
            } else {
                List<WriteRequest> failedRequests = new ArrayList<>();
                while (true) {
                    WriteRequest req2 = queue.poll(session);
                    if (req2 == null) {
                        break;
                    }
                    failedRequests.add(req2);
                }
                if (!failedRequests.isEmpty()) {
                    WriteToClosedSessionException cause = new WriteToClosedSessionException((Collection<WriteRequest>) failedRequests);
                    for (WriteRequest r : failedRequests) {
                        r.getFuture().setException(cause);
                    }
                    session.getFilterChain().fireExceptionCaught(cause);
                }
            }
        }

        public void write(VmPipeSession session, WriteRequest writeRequest) {
            session.getWriteRequestQueue().offer(session, writeRequest);
            if (!session.isWriteSuspended()) {
                flush(session);
            }
        }

        private Object getMessageCopy(Object message) {
            Object messageCopy = message;
            if (!(message instanceof IoBuffer)) {
                return messageCopy;
            }
            IoBuffer rb = (IoBuffer) message;
            rb.mark();
            IoBuffer wb = IoBuffer.allocate(rb.remaining());
            wb.put(rb);
            wb.flip();
            rb.reset();
            return wb;
        }

        public void remove(VmPipeSession session) {
            try {
                session.getLock().lock();
                if (!session.getCloseFuture().isClosed()) {
                    session.getServiceListeners().fireSessionDestroyed(session);
                    session.getRemoteSession().closeNow();
                }
            } finally {
                session.getLock().unlock();
            }
        }

        public void add(VmPipeSession session) {
        }

        public void updateTrafficControl(VmPipeSession session) {
            if (!session.isReadSuspended()) {
                List<Object> data = new ArrayList<>();
                session.receivedMessageQueue.drainTo(data);
                for (Object aData : data) {
                    VmPipeFilterChain.this.fireMessageReceived(aData);
                }
            }
            if (!session.isWriteSuspended()) {
                flush(session);
            }
        }

        public void dispose() {
        }

        public boolean isDisposed() {
            return false;
        }

        public boolean isDisposing() {
            return false;
        }
    }
}
