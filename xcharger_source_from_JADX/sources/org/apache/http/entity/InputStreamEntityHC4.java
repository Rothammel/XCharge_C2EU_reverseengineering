package org.apache.http.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.util.Args;

@NotThreadSafe
public class InputStreamEntityHC4 extends AbstractHttpEntityHC4 {
    private final InputStream content;
    private final long length;

    public InputStreamEntityHC4(InputStream instream) {
        this(instream, -1);
    }

    public InputStreamEntityHC4(InputStream instream, long length2) {
        this(instream, length2, (ContentType) null);
    }

    public InputStreamEntityHC4(InputStream instream, ContentType contentType) {
        this(instream, -1, contentType);
    }

    public InputStreamEntityHC4(InputStream instream, long length2, ContentType contentType) {
        this.content = (InputStream) Args.notNull(instream, "Source input stream");
        this.length = length2;
        if (contentType != null) {
            setContentType(contentType.toString());
        }
    }

    public boolean isRepeatable() {
        return false;
    }

    public long getContentLength() {
        return this.length;
    }

    public InputStream getContent() throws IOException {
        return this.content;
    }

    public void writeTo(OutputStream outstream) throws IOException {
        int l;
        Args.notNull(outstream, "Output stream");
        InputStream instream = this.content;
        try {
            byte[] buffer = new byte[4096];
            if (this.length < 0) {
                while (true) {
                    int l2 = instream.read(buffer);
                    if (l2 == -1) {
                        break;
                    }
                    outstream.write(buffer, 0, l2);
                }
            } else {
                long remaining = this.length;
                while (remaining > 0 && (l = instream.read(buffer, 0, (int) Math.min(4096, remaining))) != -1) {
                    outstream.write(buffer, 0, l);
                    remaining -= (long) l;
                }
            }
        } finally {
            instream.close();
        }
    }

    public boolean isStreaming() {
        return true;
    }
}
