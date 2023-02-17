package org.apache.mina.core.write;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import org.apache.mina.util.MapBackedSet;

public class WriteException extends IOException {
    private static final long serialVersionUID = -4174407422754524197L;
    private final List<WriteRequest> requests;

    public WriteException(WriteRequest request) {
        this.requests = asRequestList(request);
    }

    public WriteException(WriteRequest request, String message) {
        super(message);
        this.requests = asRequestList(request);
    }

    public WriteException(WriteRequest request, String message, Throwable cause) {
        super(message);
        initCause(cause);
        this.requests = asRequestList(request);
    }

    public WriteException(WriteRequest request, Throwable cause) {
        initCause(cause);
        this.requests = asRequestList(request);
    }

    public WriteException(Collection<WriteRequest> requests2) {
        this.requests = asRequestList(requests2);
    }

    public WriteException(Collection<WriteRequest> requests2, String message) {
        super(message);
        this.requests = asRequestList(requests2);
    }

    public WriteException(Collection<WriteRequest> requests2, String message, Throwable cause) {
        super(message);
        initCause(cause);
        this.requests = asRequestList(requests2);
    }

    public WriteException(Collection<WriteRequest> requests2, Throwable cause) {
        initCause(cause);
        this.requests = asRequestList(requests2);
    }

    public List<WriteRequest> getRequests() {
        return this.requests;
    }

    public WriteRequest getRequest() {
        return this.requests.get(0);
    }

    private static List<WriteRequest> asRequestList(Collection<WriteRequest> requests2) {
        if (requests2 == null) {
            throw new IllegalArgumentException("requests");
        } else if (requests2.isEmpty()) {
            throw new IllegalArgumentException("requests is empty.");
        } else {
            Set<WriteRequest> newRequests = new MapBackedSet<>(new LinkedHashMap());
            for (WriteRequest r : requests2) {
                newRequests.add(r.getOriginalRequest());
            }
            return Collections.unmodifiableList(new ArrayList(newRequests));
        }
    }

    private static List<WriteRequest> asRequestList(WriteRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request");
        }
        List<WriteRequest> requests2 = new ArrayList<>(1);
        requests2.add(request.getOriginalRequest());
        return Collections.unmodifiableList(requests2);
    }
}
