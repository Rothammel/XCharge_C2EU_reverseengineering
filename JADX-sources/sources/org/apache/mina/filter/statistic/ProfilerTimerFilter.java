package org.apache.mina.filter.statistic;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoEventType;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

/* loaded from: classes.dex */
public class ProfilerTimerFilter extends IoFilterAdapter {
    private TimerWorker messageReceivedTimerWorker;
    private TimerWorker messageSentTimerWorker;
    private boolean profileMessageReceived;
    private boolean profileMessageSent;
    private boolean profileSessionClosed;
    private boolean profileSessionCreated;
    private boolean profileSessionIdle;
    private boolean profileSessionOpened;
    private TimerWorker sessionClosedTimerWorker;
    private TimerWorker sessionCreatedTimerWorker;
    private TimerWorker sessionIdleTimerWorker;
    private TimerWorker sessionOpenedTimerWorker;
    private volatile TimeUnit timeUnit;

    public ProfilerTimerFilter() {
        this(TimeUnit.MILLISECONDS, IoEventType.MESSAGE_RECEIVED, IoEventType.MESSAGE_SENT);
    }

    public ProfilerTimerFilter(TimeUnit timeUnit) {
        this(timeUnit, IoEventType.MESSAGE_RECEIVED, IoEventType.MESSAGE_SENT);
    }

    public ProfilerTimerFilter(TimeUnit timeUnit, IoEventType... eventTypes) {
        this.profileMessageReceived = false;
        this.profileMessageSent = false;
        this.profileSessionCreated = false;
        this.profileSessionOpened = false;
        this.profileSessionIdle = false;
        this.profileSessionClosed = false;
        this.timeUnit = timeUnit;
        setProfilers(eventTypes);
    }

    private void setProfilers(IoEventType... eventTypes) {
        for (IoEventType type : eventTypes) {
            switch (type) {
                case MESSAGE_RECEIVED:
                    this.messageReceivedTimerWorker = new TimerWorker();
                    this.profileMessageReceived = true;
                    break;
                case MESSAGE_SENT:
                    this.messageSentTimerWorker = new TimerWorker();
                    this.profileMessageSent = true;
                    break;
                case SESSION_CREATED:
                    this.sessionCreatedTimerWorker = new TimerWorker();
                    this.profileSessionCreated = true;
                    break;
                case SESSION_OPENED:
                    this.sessionOpenedTimerWorker = new TimerWorker();
                    this.profileSessionOpened = true;
                    break;
                case SESSION_IDLE:
                    this.sessionIdleTimerWorker = new TimerWorker();
                    this.profileSessionIdle = true;
                    break;
                case SESSION_CLOSED:
                    this.sessionClosedTimerWorker = new TimerWorker();
                    this.profileSessionClosed = true;
                    break;
            }
        }
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public void profile(IoEventType type) {
        switch (type) {
            case MESSAGE_RECEIVED:
                this.profileMessageReceived = true;
                if (this.messageReceivedTimerWorker == null) {
                    this.messageReceivedTimerWorker = new TimerWorker();
                    return;
                }
                return;
            case MESSAGE_SENT:
                this.profileMessageSent = true;
                if (this.messageSentTimerWorker == null) {
                    this.messageSentTimerWorker = new TimerWorker();
                    return;
                }
                return;
            case SESSION_CREATED:
                this.profileSessionCreated = true;
                if (this.sessionCreatedTimerWorker == null) {
                    this.sessionCreatedTimerWorker = new TimerWorker();
                    return;
                }
                return;
            case SESSION_OPENED:
                this.profileSessionOpened = true;
                if (this.sessionOpenedTimerWorker == null) {
                    this.sessionOpenedTimerWorker = new TimerWorker();
                    return;
                }
                return;
            case SESSION_IDLE:
                this.profileSessionIdle = true;
                if (this.sessionIdleTimerWorker == null) {
                    this.sessionIdleTimerWorker = new TimerWorker();
                    return;
                }
                return;
            case SESSION_CLOSED:
                this.profileSessionClosed = true;
                if (this.sessionClosedTimerWorker == null) {
                    this.sessionClosedTimerWorker = new TimerWorker();
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void stopProfile(IoEventType type) {
        switch (type) {
            case MESSAGE_RECEIVED:
                this.profileMessageReceived = false;
                return;
            case MESSAGE_SENT:
                this.profileMessageSent = false;
                return;
            case SESSION_CREATED:
                this.profileSessionCreated = false;
                return;
            case SESSION_OPENED:
                this.profileSessionOpened = false;
                return;
            case SESSION_IDLE:
                this.profileSessionIdle = false;
                return;
            case SESSION_CLOSED:
                this.profileSessionClosed = false;
                return;
            default:
                return;
        }
    }

    public Set<IoEventType> getEventsToProfile() {
        Set<IoEventType> set = new HashSet<>();
        if (this.profileMessageReceived) {
            set.add(IoEventType.MESSAGE_RECEIVED);
        }
        if (this.profileMessageSent) {
            set.add(IoEventType.MESSAGE_SENT);
        }
        if (this.profileSessionCreated) {
            set.add(IoEventType.SESSION_CREATED);
        }
        if (this.profileSessionOpened) {
            set.add(IoEventType.SESSION_OPENED);
        }
        if (this.profileSessionIdle) {
            set.add(IoEventType.SESSION_IDLE);
        }
        if (this.profileSessionClosed) {
            set.add(IoEventType.SESSION_CLOSED);
        }
        return set;
    }

    public void setEventsToProfile(IoEventType... eventTypes) {
        setProfilers(eventTypes);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void messageReceived(IoFilter.NextFilter nextFilter, IoSession session, Object message) throws Exception {
        if (this.profileMessageReceived) {
            long start = timeNow();
            nextFilter.messageReceived(session, message);
            long end = timeNow();
            this.messageReceivedTimerWorker.addNewDuration(end - start);
            return;
        }
        nextFilter.messageReceived(session, message);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void messageSent(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        if (this.profileMessageSent) {
            long start = timeNow();
            nextFilter.messageSent(session, writeRequest);
            long end = timeNow();
            this.messageSentTimerWorker.addNewDuration(end - start);
            return;
        }
        nextFilter.messageSent(session, writeRequest);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void sessionCreated(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        if (this.profileSessionCreated) {
            long start = timeNow();
            nextFilter.sessionCreated(session);
            long end = timeNow();
            this.sessionCreatedTimerWorker.addNewDuration(end - start);
            return;
        }
        nextFilter.sessionCreated(session);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void sessionOpened(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        if (this.profileSessionOpened) {
            long start = timeNow();
            nextFilter.sessionOpened(session);
            long end = timeNow();
            this.sessionOpenedTimerWorker.addNewDuration(end - start);
            return;
        }
        nextFilter.sessionOpened(session);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void sessionIdle(IoFilter.NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
        if (this.profileSessionIdle) {
            long start = timeNow();
            nextFilter.sessionIdle(session, status);
            long end = timeNow();
            this.sessionIdleTimerWorker.addNewDuration(end - start);
            return;
        }
        nextFilter.sessionIdle(session, status);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void sessionClosed(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        if (this.profileSessionClosed) {
            long start = timeNow();
            nextFilter.sessionClosed(session);
            long end = timeNow();
            this.sessionClosedTimerWorker.addNewDuration(end - start);
            return;
        }
        nextFilter.sessionClosed(session);
    }

    public double getAverageTime(IoEventType type) {
        switch (type) {
            case MESSAGE_RECEIVED:
                if (this.profileMessageReceived) {
                    return this.messageReceivedTimerWorker.getAverage();
                }
                break;
            case MESSAGE_SENT:
                if (this.profileMessageSent) {
                    return this.messageSentTimerWorker.getAverage();
                }
                break;
            case SESSION_CREATED:
                if (this.profileSessionCreated) {
                    return this.sessionCreatedTimerWorker.getAverage();
                }
                break;
            case SESSION_OPENED:
                if (this.profileSessionOpened) {
                    return this.sessionOpenedTimerWorker.getAverage();
                }
                break;
            case SESSION_IDLE:
                if (this.profileSessionIdle) {
                    return this.sessionIdleTimerWorker.getAverage();
                }
                break;
            case SESSION_CLOSED:
                if (this.profileSessionClosed) {
                    return this.sessionClosedTimerWorker.getAverage();
                }
                break;
        }
        throw new IllegalArgumentException("You are not monitoring this event.  Please add this event first.");
    }

    public long getTotalCalls(IoEventType type) {
        switch (type) {
            case MESSAGE_RECEIVED:
                if (this.profileMessageReceived) {
                    return this.messageReceivedTimerWorker.getCallsNumber();
                }
                break;
            case MESSAGE_SENT:
                if (this.profileMessageSent) {
                    return this.messageSentTimerWorker.getCallsNumber();
                }
                break;
            case SESSION_CREATED:
                if (this.profileSessionCreated) {
                    return this.sessionCreatedTimerWorker.getCallsNumber();
                }
                break;
            case SESSION_OPENED:
                if (this.profileSessionOpened) {
                    return this.sessionOpenedTimerWorker.getCallsNumber();
                }
                break;
            case SESSION_IDLE:
                if (this.profileSessionIdle) {
                    return this.sessionIdleTimerWorker.getCallsNumber();
                }
                break;
            case SESSION_CLOSED:
                if (this.profileSessionClosed) {
                    return this.sessionClosedTimerWorker.getCallsNumber();
                }
                break;
        }
        throw new IllegalArgumentException("You are not monitoring this event.  Please add this event first.");
    }

    public long getTotalTime(IoEventType type) {
        switch (type) {
            case MESSAGE_RECEIVED:
                if (this.profileMessageReceived) {
                    return this.messageReceivedTimerWorker.getTotal();
                }
                break;
            case MESSAGE_SENT:
                if (this.profileMessageSent) {
                    return this.messageSentTimerWorker.getTotal();
                }
                break;
            case SESSION_CREATED:
                if (this.profileSessionCreated) {
                    return this.sessionCreatedTimerWorker.getTotal();
                }
                break;
            case SESSION_OPENED:
                if (this.profileSessionOpened) {
                    return this.sessionOpenedTimerWorker.getTotal();
                }
                break;
            case SESSION_IDLE:
                if (this.profileSessionIdle) {
                    return this.sessionIdleTimerWorker.getTotal();
                }
                break;
            case SESSION_CLOSED:
                if (this.profileSessionClosed) {
                    return this.sessionClosedTimerWorker.getTotal();
                }
                break;
        }
        throw new IllegalArgumentException("You are not monitoring this event.  Please add this event first.");
    }

    public long getMinimumTime(IoEventType type) {
        switch (type) {
            case MESSAGE_RECEIVED:
                if (this.profileMessageReceived) {
                    return this.messageReceivedTimerWorker.getMinimum();
                }
                break;
            case MESSAGE_SENT:
                if (this.profileMessageSent) {
                    return this.messageSentTimerWorker.getMinimum();
                }
                break;
            case SESSION_CREATED:
                if (this.profileSessionCreated) {
                    return this.sessionCreatedTimerWorker.getMinimum();
                }
                break;
            case SESSION_OPENED:
                if (this.profileSessionOpened) {
                    return this.sessionOpenedTimerWorker.getMinimum();
                }
                break;
            case SESSION_IDLE:
                if (this.profileSessionIdle) {
                    return this.sessionIdleTimerWorker.getMinimum();
                }
                break;
            case SESSION_CLOSED:
                if (this.profileSessionClosed) {
                    return this.sessionClosedTimerWorker.getMinimum();
                }
                break;
        }
        throw new IllegalArgumentException("You are not monitoring this event.  Please add this event first.");
    }

    public long getMaximumTime(IoEventType type) {
        switch (type) {
            case MESSAGE_RECEIVED:
                if (this.profileMessageReceived) {
                    return this.messageReceivedTimerWorker.getMaximum();
                }
                break;
            case MESSAGE_SENT:
                if (this.profileMessageSent) {
                    return this.messageSentTimerWorker.getMaximum();
                }
                break;
            case SESSION_CREATED:
                if (this.profileSessionCreated) {
                    return this.sessionCreatedTimerWorker.getMaximum();
                }
                break;
            case SESSION_OPENED:
                if (this.profileSessionOpened) {
                    return this.sessionOpenedTimerWorker.getMaximum();
                }
                break;
            case SESSION_IDLE:
                if (this.profileSessionIdle) {
                    return this.sessionIdleTimerWorker.getMaximum();
                }
                break;
            case SESSION_CLOSED:
                if (this.profileSessionClosed) {
                    return this.sessionClosedTimerWorker.getMaximum();
                }
                break;
        }
        throw new IllegalArgumentException("You are not monitoring this event.  Please add this event first.");
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class TimerWorker {
        private final Object lock = new Object();
        private final AtomicLong total = new AtomicLong();
        private final AtomicLong callsNumber = new AtomicLong();
        private final AtomicLong minimum = new AtomicLong();
        private final AtomicLong maximum = new AtomicLong();

        public TimerWorker() {
        }

        public void addNewDuration(long duration) {
            this.callsNumber.incrementAndGet();
            this.total.addAndGet(duration);
            synchronized (this.lock) {
                if (duration < this.minimum.longValue()) {
                    this.minimum.set(duration);
                }
                if (duration > this.maximum.longValue()) {
                    this.maximum.set(duration);
                }
            }
        }

        public double getAverage() {
            double longValue;
            synchronized (this.lock) {
                longValue = this.total.longValue() / this.callsNumber.longValue();
            }
            return longValue;
        }

        public long getCallsNumber() {
            return this.callsNumber.longValue();
        }

        public long getTotal() {
            return this.total.longValue();
        }

        public long getMinimum() {
            return this.minimum.longValue();
        }

        public long getMaximum() {
            return this.maximum.longValue();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: org.apache.mina.filter.statistic.ProfilerTimerFilter$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$java$util$concurrent$TimeUnit = new int[TimeUnit.values().length];

        static {
            try {
                $SwitchMap$java$util$concurrent$TimeUnit[TimeUnit.SECONDS.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$java$util$concurrent$TimeUnit[TimeUnit.MICROSECONDS.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$java$util$concurrent$TimeUnit[TimeUnit.NANOSECONDS.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            $SwitchMap$org$apache$mina$core$session$IoEventType = new int[IoEventType.values().length];
            try {
                $SwitchMap$org$apache$mina$core$session$IoEventType[IoEventType.MESSAGE_RECEIVED.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$org$apache$mina$core$session$IoEventType[IoEventType.MESSAGE_SENT.ordinal()] = 2;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$org$apache$mina$core$session$IoEventType[IoEventType.SESSION_CREATED.ordinal()] = 3;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$org$apache$mina$core$session$IoEventType[IoEventType.SESSION_OPENED.ordinal()] = 4;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$org$apache$mina$core$session$IoEventType[IoEventType.SESSION_IDLE.ordinal()] = 5;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$org$apache$mina$core$session$IoEventType[IoEventType.SESSION_CLOSED.ordinal()] = 6;
            } catch (NoSuchFieldError e9) {
            }
        }
    }

    private long timeNow() {
        switch (AnonymousClass1.$SwitchMap$java$util$concurrent$TimeUnit[this.timeUnit.ordinal()]) {
            case 1:
                return System.currentTimeMillis() / 1000;
            case 2:
                return System.nanoTime() / 1000;
            case 3:
                return System.nanoTime();
            default:
                return System.currentTimeMillis();
        }
    }
}
