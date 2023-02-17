package org.apache.mina.util;

public abstract class ExceptionMonitor {
    private static ExceptionMonitor instance = new DefaultExceptionMonitor();

    public abstract void exceptionCaught(Throwable th);

    public static ExceptionMonitor getInstance() {
        return instance;
    }

    public static void setInstance(ExceptionMonitor monitor) {
        if (monitor == null) {
            monitor = new DefaultExceptionMonitor();
        }
        instance = monitor;
    }
}
