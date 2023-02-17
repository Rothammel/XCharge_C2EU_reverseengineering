package org.apache.mina.filter.codec;

/* loaded from: classes.dex */
public class ProtocolEncoderException extends ProtocolCodecException {
    private static final long serialVersionUID = 8752989973624459604L;

    public ProtocolEncoderException() {
    }

    public ProtocolEncoderException(String message) {
        super(message);
    }

    public ProtocolEncoderException(Throwable cause) {
        super(cause);
    }

    public ProtocolEncoderException(String message, Throwable cause) {
        super(message, cause);
    }
}
