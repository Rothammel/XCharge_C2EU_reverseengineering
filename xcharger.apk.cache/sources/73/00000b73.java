package org.apache.http;

import java.io.IOException;

/* loaded from: classes.dex */
public class ContentTooLongException extends IOException {
    private static final long serialVersionUID = -924287689552495383L;

    public ContentTooLongException(String message) {
        super(message);
    }
}