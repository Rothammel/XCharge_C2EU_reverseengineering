package org.apache.http.p014io;

import org.apache.http.HttpMessage;

/* renamed from: org.apache.http.io.HttpMessageWriterFactory */
public interface HttpMessageWriterFactory<T extends HttpMessage> {
    HttpMessageWriter create(SessionOutputBuffer sessionOutputBuffer);
}
