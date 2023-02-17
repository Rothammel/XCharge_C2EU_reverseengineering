package org.apache.mina.filter.codec;

/* loaded from: classes.dex */
public class RecoverableProtocolDecoderException extends ProtocolDecoderException {
    private static final long serialVersionUID = -8172624045024880678L;

    public RecoverableProtocolDecoderException() {
    }

    public RecoverableProtocolDecoderException(String message) {
        super(message);
    }

    public RecoverableProtocolDecoderException(Throwable cause) {
        super(cause);
    }

    public RecoverableProtocolDecoderException(String message, Throwable cause) {
        super(message, cause);
    }
}