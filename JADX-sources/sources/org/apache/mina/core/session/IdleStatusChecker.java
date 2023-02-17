package org.apache.mina.core.session;

import java.util.Set;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.util.ConcurrentHashSet;

/* loaded from: classes.dex */
public class IdleStatusChecker {
    private final Set<AbstractIoSession> sessions = new ConcurrentHashSet();
    private final NotifyingTask notifyingTask = new NotifyingTask();
    private final IoFutureListener<IoFuture> sessionCloseListener = new SessionCloseListener();

    public void addSession(AbstractIoSession session) {
        this.sessions.add(session);
        CloseFuture closeFuture = session.getCloseFuture();
        closeFuture.addListener((IoFutureListener<?>) this.sessionCloseListener);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeSession(AbstractIoSession session) {
        this.sessions.remove(session);
    }

    public NotifyingTask getNotifyingTask() {
        return this.notifyingTask;
    }

    /* loaded from: classes.dex */
    public class NotifyingTask implements Runnable {
        private volatile boolean cancelled;
        private volatile Thread thread;

        NotifyingTask() {
        }

        @Override // java.lang.Runnable
        public void run() {
            this.thread = Thread.currentThread();
            while (!this.cancelled) {
                try {
                    long currentTime = System.currentTimeMillis();
                    notifySessions(currentTime);
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                    }
                } finally {
                    this.thread = null;
                }
            }
        }

        public void cancel() {
            this.cancelled = true;
            Thread thread = this.thread;
            if (thread != null) {
                thread.interrupt();
            }
        }

        private void notifySessions(long currentTime) {
            for (AbstractIoSession session : IdleStatusChecker.this.sessions) {
                if (session.isConnected()) {
                    AbstractIoSession.notifyIdleSession(session, currentTime);
                }
            }
        }
    }

    /* loaded from: classes.dex */
    private class SessionCloseListener implements IoFutureListener<IoFuture> {
        public SessionCloseListener() {
        }

        @Override // org.apache.mina.core.future.IoFutureListener
        public void operationComplete(IoFuture future) {
            IdleStatusChecker.this.removeSession((AbstractIoSession) future.getSession());
        }
    }
}
