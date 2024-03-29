package org.apache.commons.lang3.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class BackgroundInitializer<T> implements ConcurrentInitializer<T> {
    private ExecutorService executor;
    private ExecutorService externalExecutor;
    private Future<T> future;

    /* access modifiers changed from: protected */
    public abstract T initialize() throws Exception;

    protected BackgroundInitializer() {
        this((ExecutorService) null);
    }

    protected BackgroundInitializer(ExecutorService exec) {
        setExternalExecutor(exec);
    }

    public final synchronized ExecutorService getExternalExecutor() {
        return this.externalExecutor;
    }

    public synchronized boolean isStarted() {
        return this.future != null;
    }

    public final synchronized void setExternalExecutor(ExecutorService externalExecutor2) {
        if (isStarted()) {
            throw new IllegalStateException("Cannot set ExecutorService after start()!");
        }
        this.externalExecutor = externalExecutor2;
    }

    public synchronized boolean start() {
        boolean z;
        ExecutorService tempExec;
        if (!isStarted()) {
            this.executor = getExternalExecutor();
            if (this.executor == null) {
                tempExec = createExecutor();
                this.executor = tempExec;
            } else {
                tempExec = null;
            }
            this.future = this.executor.submit(createTask(tempExec));
            z = true;
        } else {
            z = false;
        }
        return z;
    }

    public T get() throws ConcurrentException {
        try {
            return getFuture().get();
        } catch (ExecutionException execex) {
            ConcurrentUtils.handleCause(execex);
            return null;
        } catch (InterruptedException iex) {
            Thread.currentThread().interrupt();
            throw new ConcurrentException(iex);
        }
    }

    public synchronized Future<T> getFuture() {
        if (this.future == null) {
            throw new IllegalStateException("start() must be called first!");
        }
        return this.future;
    }

    /* access modifiers changed from: protected */
    public final synchronized ExecutorService getActiveExecutor() {
        return this.executor;
    }

    /* access modifiers changed from: protected */
    public int getTaskCount() {
        return 1;
    }

    private Callable<T> createTask(ExecutorService execDestroy) {
        return new InitializationTask(execDestroy);
    }

    private ExecutorService createExecutor() {
        return Executors.newFixedThreadPool(getTaskCount());
    }

    private class InitializationTask implements Callable<T> {
        private final ExecutorService execFinally;

        public InitializationTask(ExecutorService exec) {
            this.execFinally = exec;
        }

        public T call() throws Exception {
            try {
                return BackgroundInitializer.this.initialize();
            } finally {
                if (this.execFinally != null) {
                    this.execFinally.shutdown();
                }
            }
        }
    }
}
