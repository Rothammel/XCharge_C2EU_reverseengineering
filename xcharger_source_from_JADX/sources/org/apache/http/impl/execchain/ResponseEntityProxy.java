package org.apache.http.impl.execchain;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.conn.EofSensorInputStreamHC4;
import org.apache.http.conn.EofSensorWatcher;
import org.apache.http.entity.HttpEntityWrapperHC4;

@NotThreadSafe
class ResponseEntityProxy extends HttpEntityWrapperHC4 implements EofSensorWatcher {
    private final ConnectionHolder connHolder;

    public static void enchance(HttpResponse response, ConnectionHolder connHolder2) {
        HttpEntity entity = response.getEntity();
        if (entity != null && entity.isStreaming() && connHolder2 != null) {
            response.setEntity(new ResponseEntityProxy(entity, connHolder2));
        }
    }

    ResponseEntityProxy(HttpEntity entity, ConnectionHolder connHolder2) {
        super(entity);
        this.connHolder = connHolder2;
    }

    private void cleanup() {
        if (this.connHolder != null) {
            this.connHolder.abortConnection();
        }
    }

    public void releaseConnection() throws IOException {
        if (this.connHolder != null) {
            try {
                if (this.connHolder.isReusable()) {
                    this.connHolder.releaseConnection();
                }
            } finally {
                cleanup();
            }
        }
    }

    public boolean isRepeatable() {
        return false;
    }

    public InputStream getContent() throws IOException {
        return new EofSensorInputStreamHC4(this.wrappedEntity.getContent(), this);
    }

    @Deprecated
    public void consumeContent() throws IOException {
        releaseConnection();
    }

    public void writeTo(OutputStream outstream) throws IOException {
        try {
            this.wrappedEntity.writeTo(outstream);
            releaseConnection();
        } finally {
            cleanup();
        }
    }

    /* JADX INFO: finally extract failed */
    public boolean eofDetected(InputStream wrapped) throws IOException {
        try {
            wrapped.close();
            releaseConnection();
            cleanup();
            return false;
        } catch (Throwable th) {
            cleanup();
            throw th;
        }
    }

    public boolean streamClosed(InputStream wrapped) throws IOException {
        boolean open;
        try {
            if (this.connHolder == null || this.connHolder.isReleased()) {
                open = false;
            } else {
                open = true;
            }
            wrapped.close();
            releaseConnection();
        } catch (SocketException ex) {
            if (open) {
                throw ex;
            }
        } catch (Throwable th) {
            cleanup();
            throw th;
        }
        cleanup();
        return false;
    }

    public boolean streamAbort(InputStream wrapped) throws IOException {
        cleanup();
        return false;
    }

    public String toString() {
        return "ResponseEntityProxy{" + this.wrappedEntity + '}';
    }
}
