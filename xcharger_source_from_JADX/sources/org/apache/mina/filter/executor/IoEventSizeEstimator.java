package org.apache.mina.filter.executor;

import org.apache.mina.core.session.IoEvent;

public interface IoEventSizeEstimator {
    int estimateSize(IoEvent ioEvent);
}
