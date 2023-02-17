package org.apache.mina.core.write;

import java.util.Collection;

/* loaded from: classes.dex */
public class NothingWrittenException extends WriteException {
    private static final long serialVersionUID = -6331979307737691005L;

    public NothingWrittenException(Collection<WriteRequest> requests, String message, Throwable cause) {
        super(requests, message, cause);
    }

    public NothingWrittenException(Collection<WriteRequest> requests, String s) {
        super(requests, s);
    }

    public NothingWrittenException(Collection<WriteRequest> requests, Throwable cause) {
        super(requests, cause);
    }

    public NothingWrittenException(Collection<WriteRequest> requests) {
        super(requests);
    }

    public NothingWrittenException(WriteRequest request, String message, Throwable cause) {
        super(request, message, cause);
    }

    public NothingWrittenException(WriteRequest request, String s) {
        super(request, s);
    }

    public NothingWrittenException(WriteRequest request, Throwable cause) {
        super(request, cause);
    }

    public NothingWrittenException(WriteRequest request) {
        super(request);
    }
}