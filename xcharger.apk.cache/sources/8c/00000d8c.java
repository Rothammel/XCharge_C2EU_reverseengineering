package org.apache.mina.filter.codec;

import org.apache.commons.lang3.StringUtils;

/* loaded from: classes.dex */
public class ProtocolDecoderException extends ProtocolCodecException {
    private static final long serialVersionUID = 3545799879533408565L;
    private String hexdump;

    public ProtocolDecoderException() {
    }

    public ProtocolDecoderException(String message) {
        super(message);
    }

    public ProtocolDecoderException(Throwable cause) {
        super(cause);
    }

    public ProtocolDecoderException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override // java.lang.Throwable
    public String getMessage() {
        String message = super.getMessage();
        if (message == null) {
            message = "";
        }
        if (this.hexdump != null) {
            return message + (message.length() > 0 ? StringUtils.SPACE : "") + "(Hexdump: " + this.hexdump + ')';
        }
        return message;
    }

    public String getHexdump() {
        return this.hexdump;
    }

    public void setHexdump(String hexdump) {
        if (this.hexdump != null) {
            throw new IllegalStateException("Hexdump cannot be set more than once.");
        }
        this.hexdump = hexdump;
    }
}