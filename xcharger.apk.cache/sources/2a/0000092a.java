package com.xcharge.common.utils;

import android.util.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/* loaded from: classes.dex */
public final class Threads {
    private Threads() {
    }

    public static ThreadFactory daemonThreadFactory(final String name) {
        return new ThreadFactory() { // from class: com.xcharge.common.utils.Threads.1
            private int nextId = 0;

            @Override // java.util.concurrent.ThreadFactory
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
        ThreadFactory threadFactory = daemonThreadFactory(name);
        return new ThreadPoolExecutor(count, count, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue(Integer.MAX_VALUE), threadFactory) { // from class: com.xcharge.common.utils.Threads.2
            @Override // java.util.concurrent.ThreadPoolExecutor
            protected void afterExecute(Runnable runnable, Throwable throwable) {
                if (throwable != null) {
                    Log.i("Threads.fixedThreadsExecutor", "Unexpected failure from " + runnable, throwable);
                }
            }
        };
    }
}