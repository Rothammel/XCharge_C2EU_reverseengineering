package com.xcharge.common.utils;

import android.util.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class Threads {
    private Threads() {
    }

    public static ThreadFactory daemonThreadFactory(final String name) {
        return new ThreadFactory() {
            private int nextId = 0;

            public synchronized Thread newThread(Runnable r) {
                Thread thread;
                StringBuilder append = new StringBuilder(String.valueOf(name)).append("-");
                int i = this.nextId;
                this.nextId = i + 1;
                thread = new Thread(r, append.append(i).toString());
                thread.setDaemon(true);
                return thread;
            }
        };
    }

    public static ExecutorService threadPerCpuExecutor(String name) {
        return fixedThreadsExecutor(name, Runtime.getRuntime().availableProcessors());
    }

    public static ExecutorService fixedThreadsExecutor(String name, int count) {
        return new ThreadPoolExecutor(count, count, 10, TimeUnit.SECONDS, new LinkedBlockingQueue(Integer.MAX_VALUE), daemonThreadFactory(name)) {
            /* access modifiers changed from: protected */
            public void afterExecute(Runnable runnable, Throwable throwable) {
                if (throwable != null) {
                    Log.i("Threads.fixedThreadsExecutor", "Unexpected failure from " + runnable, throwable);
                }
            }
        };
    }
}
