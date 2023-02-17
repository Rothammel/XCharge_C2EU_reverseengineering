package org.apache.mina.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamePreservingRunnable implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger((Class<?>) NamePreservingRunnable.class);
    private final String newName;
    private final Runnable runnable;

    public NamePreservingRunnable(Runnable runnable2, String newName2) {
        this.runnable = runnable2;
        this.newName = newName2;
    }

    public void run() {
        Thread currentThread = Thread.currentThread();
        String oldName = currentThread.getName();
        if (this.newName != null) {
            setName(currentThread, this.newName);
        }
        try {
            this.runnable.run();
        } finally {
            setName(currentThread, oldName);
        }
    }

    private void setName(Thread thread, String name) {
        try {
            thread.setName(name);
        } catch (SecurityException se) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Failed to set the thread name.", (Throwable) se);
            }
        }
    }
}
