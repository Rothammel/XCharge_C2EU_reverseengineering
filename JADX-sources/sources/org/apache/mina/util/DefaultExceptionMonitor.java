package org.apache.mina.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public class DefaultExceptionMonitor extends ExceptionMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionMonitor.class);

    @Override // org.apache.mina.util.ExceptionMonitor
    public void exceptionCaught(Throwable cause) {
        if (cause instanceof Error) {
            throw ((Error) cause);
        }
        LOGGER.warn("Unexpected exception.", cause);
    }
}
