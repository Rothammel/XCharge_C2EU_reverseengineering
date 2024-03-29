package org.apache.http.p014io;

import org.apache.http.HttpMessage;
import org.apache.http.config.MessageConstraints;

/* renamed from: org.apache.http.io.HttpMessageParserFactory */
public interface HttpMessageParserFactory<T extends HttpMessage> {
    HttpMessageParser create(SessionInputBuffer sessionInputBuffer, MessageConstraints messageConstraints);
}
