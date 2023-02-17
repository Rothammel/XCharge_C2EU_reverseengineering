package it.sauronsoftware.ftp4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/* loaded from: classes.dex */
class NVTASCIIReader extends Reader {
    private static final String SYSTEM_LINE_SEPARATOR = System.getProperty("line.separator");
    private Reader reader;
    private InputStream stream;

    public NVTASCIIReader(InputStream stream, String charsetName) throws IOException {
        this.stream = stream;
        this.reader = new InputStreamReader(stream, charsetName);
    }

    @Override // java.io.Reader, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        synchronized (this) {
            this.reader.close();
        }
    }

    @Override // java.io.Reader
    public int read(char[] cbuf, int off, int len) throws IOException {
        int read;
        synchronized (this) {
            read = this.reader.read(cbuf, off, len);
        }
        return read;
    }

    public void changeCharset(String charsetName) throws IOException {
        synchronized (this) {
            this.reader = new InputStreamReader(this.stream, charsetName);
        }
    }

    public String readLine() throws IOException {
        StringBuffer buffer = new StringBuffer();
        int current = -1;
        while (true) {
            int i = this.reader.read();
            if (i == -1) {
                if (buffer.length() == 0) {
                    return null;
                }
                return buffer.toString();
            }
            int previous = current;
            current = i;
            if (current == 10) {
                return buffer.toString();
            }
            if (previous == 13 && current == 0) {
                buffer.append(SYSTEM_LINE_SEPARATOR);
            } else if (current != 0 && current != 13) {
                buffer.append((char) current);
            }
        }
    }
}
