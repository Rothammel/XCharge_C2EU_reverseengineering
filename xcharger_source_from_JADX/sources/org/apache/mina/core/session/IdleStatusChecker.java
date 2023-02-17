package org.apache.mina.core.session;

import java.util.Set;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.util.ConcurrentHashSet;

public class IdleStatusChecker {
    private final NotifyingTask notifyingTask = new NotifyingTask();
    private final IoFutureListener<IoFuture> sessionCloseListener = new SessionCloseListener();
    /* access modifiers changed from: private */
    public final Set<AbstractIoSession> sessions = new ConcurrentHashSet();

    public void addSession(AbstractIoSession session) {
        this.sessions.add(session);
        session.getCloseFuture().addListener(this.sessionCloseListener);
    }

    /* access modifiers changed from: private */
    public void removeSession(AbstractIoSession session) {
        this.sessions.remove(session);
    }

    public NotifyingTask getNotifyingTask() {
        return this.notifyingTask;
    }

    public class NotifyingTask implements Runnable {
        private volatile boolean cancelled;
        private volatile Thread thread;

        NotifyingTask() {
        }

        public void run() {
            this.thread = Thread.currentThread();
            while (!this.cancelled) {
                try {
                    notifySessions(System.currentTimeMillis());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                } finally {
                    this.thread = null;
                }
            }
        }

        public void cancel() {
            this.cancelled = true;
            Thread thread2 = this.thread;
            if (thread2 != null) {
                thread2.interrupt();
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

    private class SessionCloseListener implements IoFutureListener<IoFuture> {
        public SessionCloseListener() {
        }

        public void operationComplete(IoFuture future) {
            IdleStatusChecker.this.removeSession((AbstractIoSession) future.getSession());
        }
    }
}
