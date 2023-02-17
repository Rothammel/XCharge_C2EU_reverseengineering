package org.apache.mina.filter.util;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestWrapper;

public abstract class WriteRequestFilter extends IoFilterAdapter {
    /* access modifiers changed from: protected */
    public abstract Object doFilterWrite(IoFilter.NextFilter nextFilter, IoSession ioSession, WriteRequest writeRequest) throws Exception;

    public void filterWrite(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        Object filteredMessage = doFilterWrite(nextFilter, session, writeRequest);
        if (filteredMessage == null || filteredMessage == writeRequest.getMessage()) {
            nextFilter.filterWrite(session, writeRequest);
        } else {
            nextFilter.filterWrite(session, new FilteredWriteRequest(filteredMessage, writeRequest));
        }
    }

    public void messageSent(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        if (writeRequest instanceof FilteredWriteRequest) {
            FilteredWriteRequest req = (FilteredWriteRequest) writeRequest;
            if (req.getParent() == this) {
                nextFilter.messageSent(session, req.getParentRequest());
                return;
            }
        }
        nextFilter.messageSent(session, writeRequest);
    }

    private class FilteredWriteRequest extends WriteRequestWrapper {
        private final Object filteredMessage;

        public FilteredWriteRequest(Object filteredMessage2, WriteRequest writeRequest) {
            super(writeRequest);
            if (filteredMessage2 == null) {
                throw new IllegalArgumentException("filteredMessage");
            }
            this.filteredMessage = filteredMessage2;
        }

        public WriteRequestFilter getParent() {
            return WriteRequestFilter.this;
        }

        public Object getMessage() {
            return this.filteredMessage;
        }
    }
}
