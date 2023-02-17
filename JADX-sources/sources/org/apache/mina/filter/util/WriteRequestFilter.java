package org.apache.mina.filter.util;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestWrapper;

/* loaded from: classes.dex */
public abstract class WriteRequestFilter extends IoFilterAdapter {
    protected abstract Object doFilterWrite(IoFilter.NextFilter nextFilter, IoSession ioSession, WriteRequest writeRequest) throws Exception;

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void filterWrite(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        Object filteredMessage = doFilterWrite(nextFilter, session, writeRequest);
        if (filteredMessage != null && filteredMessage != writeRequest.getMessage()) {
            nextFilter.filterWrite(session, new FilteredWriteRequest(filteredMessage, writeRequest));
        } else {
            nextFilter.filterWrite(session, writeRequest);
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
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

    /* loaded from: classes.dex */
    private class FilteredWriteRequest extends WriteRequestWrapper {
        private final Object filteredMessage;

        public FilteredWriteRequest(Object filteredMessage, WriteRequest writeRequest) {
            super(writeRequest);
            if (filteredMessage == null) {
                throw new IllegalArgumentException("filteredMessage");
            }
            this.filteredMessage = filteredMessage;
        }

        public WriteRequestFilter getParent() {
            return WriteRequestFilter.this;
        }

        @Override // org.apache.mina.core.write.WriteRequestWrapper, org.apache.mina.core.write.WriteRequest
        public Object getMessage() {
            return this.filteredMessage;
        }
    }
}
