package org.apache.mina.filter.executor;

import java.util.EnumSet;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.filterchain.IoFilterEvent;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoEventType;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

public class ExecutorFilter extends IoFilterAdapter {
    private static final int BASE_THREAD_NUMBER = 0;
    private static final IoEventType[] DEFAULT_EVENT_SET = {IoEventType.EXCEPTION_CAUGHT, IoEventType.MESSAGE_RECEIVED, IoEventType.MESSAGE_SENT, IoEventType.SESSION_CLOSED, IoEventType.SESSION_IDLE, IoEventType.SESSION_OPENED};
    private static final long DEFAULT_KEEPALIVE_TIME = 30;
    private static final int DEFAULT_MAX_POOL_SIZE = 16;
    private static final boolean MANAGEABLE_EXECUTOR = true;
    private static final boolean NOT_MANAGEABLE_EXECUTOR = false;
    private EnumSet<IoEventType> eventTypes;
    private Executor executor;
    private boolean manageableExecutor;

    public ExecutorFilter() {
        init(createDefaultExecutor(0, 16, DEFAULT_KEEPALIVE_TIME, TimeUnit.SECONDS, Executors.defaultThreadFactory(), (IoEventQueueHandler) null), true, new IoEventType[0]);
    }

    public ExecutorFilter(int maximumPoolSize) {
        init(createDefaultExecutor(0, maximumPoolSize, DEFAULT_KEEPALIVE_TIME, TimeUnit.SECONDS, Executors.defaultThreadFactory(), (IoEventQueueHandler) null), true, new IoEventType[0]);
    }

    public ExecutorFilter(int corePoolSize, int maximumPoolSize) {
        init(createDefaultExecutor(corePoolSize, maximumPoolSize, DEFAULT_KEEPALIVE_TIME, TimeUnit.SECONDS, Executors.defaultThreadFactory(), (IoEventQueueHandler) null), true, new IoEventType[0]);
    }

    public ExecutorFilter(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        init(createDefaultExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, Executors.defaultThreadFactory(), (IoEventQueueHandler) null), true, new IoEventType[0]);
    }

    public ExecutorFilter(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, IoEventQueueHandler queueHandler) {
        init(createDefaultExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, Executors.defaultThreadFactory(), queueHandler), true, new IoEventType[0]);
    }

    public ExecutorFilter(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory) {
        init(createDefaultExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, threadFactory, (IoEventQueueHandler) null), true, new IoEventType[0]);
    }

    public ExecutorFilter(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory, IoEventQueueHandler queueHandler) {
        init(new OrderedThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, threadFactory, queueHandler), true, new IoEventType[0]);
    }

    public ExecutorFilter(IoEventType... eventTypes2) {
        init(createDefaultExecutor(0, 16, DEFAULT_KEEPALIVE_TIME, TimeUnit.SECONDS, Executors.defaultThreadFactory(), (IoEventQueueHandler) null), true, eventTypes2);
    }

    public ExecutorFilter(int maximumPoolSize, IoEventType... eventTypes2) {
        init(createDefaultExecutor(0, maximumPoolSize, DEFAULT_KEEPALIVE_TIME, TimeUnit.SECONDS, Executors.defaultThreadFactory(), (IoEventQueueHandler) null), true, eventTypes2);
    }

    public ExecutorFilter(int corePoolSize, int maximumPoolSize, IoEventType... eventTypes2) {
        init(createDefaultExecutor(corePoolSize, maximumPoolSize, DEFAULT_KEEPALIVE_TIME, TimeUnit.SECONDS, Executors.defaultThreadFactory(), (IoEventQueueHandler) null), true, eventTypes2);
    }

    public ExecutorFilter(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, IoEventType... eventTypes2) {
        init(createDefaultExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, Executors.defaultThreadFactory(), (IoEventQueueHandler) null), true, eventTypes2);
    }

    public ExecutorFilter(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, IoEventQueueHandler queueHandler, IoEventType... eventTypes2) {
        init(createDefaultExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, Executors.defaultThreadFactory(), queueHandler), true, eventTypes2);
    }

    public ExecutorFilter(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory, IoEventType... eventTypes2) {
        init(createDefaultExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, threadFactory, (IoEventQueueHandler) null), true, eventTypes2);
    }

    public ExecutorFilter(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory, IoEventQueueHandler queueHandler, IoEventType... eventTypes2) {
        init(new OrderedThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, threadFactory, queueHandler), true, eventTypes2);
    }

    public ExecutorFilter(Executor executor2) {
        init(executor2, false, new IoEventType[0]);
    }

    public ExecutorFilter(Executor executor2, IoEventType... eventTypes2) {
        init(executor2, false, eventTypes2);
    }

    private Executor createDefaultExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory, IoEventQueueHandler queueHandler) {
        return new OrderedThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, threadFactory, queueHandler);
    }

    private void initEventTypes(IoEventType... eventTypes2) {
        if (eventTypes2 == null || eventTypes2.length == 0) {
            eventTypes2 = DEFAULT_EVENT_SET;
        }
        this.eventTypes = EnumSet.of(eventTypes2[0], eventTypes2);
        if (this.eventTypes.contains(IoEventType.SESSION_CREATED)) {
            this.eventTypes = null;
            throw new IllegalArgumentException(IoEventType.SESSION_CREATED + " is not allowed.");
        }
    }

    private void init(Executor executor2, boolean manageableExecutor2, IoEventType... eventTypes2) {
        if (executor2 == null) {
            throw new IllegalArgumentException("executor");
        }
        initEventTypes(eventTypes2);
        this.executor = executor2;
        this.manageableExecutor = manageableExecutor2;
    }

    public void destroy() {
        if (this.manageableExecutor) {
            ((ExecutorService) this.executor).shutdown();
        }
    }

    public final Executor getExecutor() {
        return this.executor;
    }

    /* access modifiers changed from: protected */
    public void fireEvent(IoFilterEvent event) {
        this.executor.execute(event);
    }

    public void onPreAdd(IoFilterChain parent, String name, IoFilter.NextFilter nextFilter) throws Exception {
        if (parent.contains((IoFilter) this)) {
            throw new IllegalArgumentException("You can't add the same filter instance more than once.  Create another instance and add it.");
        }
    }

    public final void sessionOpened(IoFilter.NextFilter nextFilter, IoSession session) {
        if (this.eventTypes.contains(IoEventType.SESSION_OPENED)) {
            fireEvent(new IoFilterEvent(nextFilter, IoEventType.SESSION_OPENED, session, (Object) null));
        } else {
            nextFilter.sessionOpened(session);
        }
    }

    public final void sessionClosed(IoFilter.NextFilter nextFilter, IoSession session) {
        if (this.eventTypes.contains(IoEventType.SESSION_CLOSED)) {
            fireEvent(new IoFilterEvent(nextFilter, IoEventType.SESSION_CLOSED, session, (Object) null));
        } else {
            nextFilter.sessionClosed(session);
        }
    }

    public final void sessionIdle(IoFilter.NextFilter nextFilter, IoSession session, IdleStatus status) {
        if (this.eventTypes.contains(IoEventType.SESSION_IDLE)) {
            fireEvent(new IoFilterEvent(nextFilter, IoEventType.SESSION_IDLE, session, status));
        } else {
            nextFilter.sessionIdle(session, status);
        }
    }

    public final void exceptionCaught(IoFilter.NextFilter nextFilter, IoSession session, Throwable cause) {
        if (this.eventTypes.contains(IoEventType.EXCEPTION_CAUGHT)) {
            fireEvent(new IoFilterEvent(nextFilter, IoEventType.EXCEPTION_CAUGHT, session, cause));
        } else {
            nextFilter.exceptionCaught(session, cause);
        }
    }

    public final void messageReceived(IoFilter.NextFilter nextFilter, IoSession session, Object message) {
        if (this.eventTypes.contains(IoEventType.MESSAGE_RECEIVED)) {
            fireEvent(new IoFilterEvent(nextFilter, IoEventType.MESSAGE_RECEIVED, session, message));
        } else {
            nextFilter.messageReceived(session, message);
        }
    }

    public final void messageSent(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) {
        if (this.eventTypes.contains(IoEventType.MESSAGE_SENT)) {
            fireEvent(new IoFilterEvent(nextFilter, IoEventType.MESSAGE_SENT, session, writeRequest));
        } else {
            nextFilter.messageSent(session, writeRequest);
        }
    }

    public final void filterWrite(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) {
        if (this.eventTypes.contains(IoEventType.WRITE)) {
            fireEvent(new IoFilterEvent(nextFilter, IoEventType.WRITE, session, writeRequest));
        } else {
            nextFilter.filterWrite(session, writeRequest);
        }
    }

    public final void filterClose(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        if (this.eventTypes.contains(IoEventType.CLOSE)) {
            fireEvent(new IoFilterEvent(nextFilter, IoEventType.CLOSE, session, (Object) null));
        } else {
            nextFilter.filterClose(session);
        }
    }
}
