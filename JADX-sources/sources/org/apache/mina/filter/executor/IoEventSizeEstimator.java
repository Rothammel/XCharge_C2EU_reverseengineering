package org.apache.mina.filter.executor;

import org.apache.mina.core.session.IoEvent;

/* loaded from: classes.dex */
public interface IoEventSizeEstimator {
    int estimateSize(IoEvent ioEvent);
}
