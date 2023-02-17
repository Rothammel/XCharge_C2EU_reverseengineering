package pl.droidsonroids.gif;

import java.io.IOException;

/* loaded from: classes.dex */
public class GifIOException extends IOException {
    private static final long serialVersionUID = 13038402904505L;
    public final GifError reason;

    GifIOException(GifError reason) {
        super(reason.getFormattedDescription());
        this.reason = reason;
    }

    GifIOException(int errorCode) {
        this(GifError.fromCode(errorCode));
    }
}
