package p015pl.droidsonroids.gif;

import java.io.IOException;

/* renamed from: pl.droidsonroids.gif.GifIOException */
public class GifIOException extends IOException {
    private static final long serialVersionUID = 13038402904505L;
    public final GifError reason;

    GifIOException(GifError reason2) {
        super(reason2.getFormattedDescription());
        this.reason = reason2;
    }

    GifIOException(int errorCode) {
        this(GifError.fromCode(errorCode));
    }
}
