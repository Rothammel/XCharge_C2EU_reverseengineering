package it.sauronsoftware.ftp4j;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.StringTokenizer;

/* loaded from: classes.dex */
class NVTASCIIWriter extends Writer {
    private static final String LINE_SEPARATOR = "\r\n";
    private OutputStream stream;
    private Writer writer;

    public NVTASCIIWriter(OutputStream stream, String charsetName) throws IOException {
        this.stream = stream;
        this.writer = new OutputStreamWriter(stream, charsetName);
    }

    @Override // java.io.Writer, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        synchronized (this) {
            this.writer.close();
        }
    }

    @Override // java.io.Writer, java.io.Flushable
    public void flush() throws IOException {
        synchronized (this) {
            this.writer.flush();
        }
    }

    @Override // java.io.Writer
    public void write(char[] cbuf, int off, int len) throws IOException {
        synchronized (this) {
            this.writer.write(cbuf, off, len);
        }
    }

    public void changeCharset(String charsetName) throws IOException {
        synchronized (this) {
            this.writer = new OutputStreamWriter(this.stream, charsetName);
        }
    }

    public void writeLine(String str) throws IOException {
        StringBuffer buffer = new StringBuffer();
        boolean atLeastOne = false;
        StringTokenizer st = new StringTokenizer(str, "\r\n");
        int count = st.countTokens();
        for (int i = 0; i < count; i++) {
            String line = st.nextToken();
            if (line.length() > 0) {
                if (atLeastOne) {
                    buffer.append('\r');
                    buffer.append((char) 0);
                }
                buffer.append(line);
                atLeastOne = true;
            }
        }
        if (buffer.length() > 0) {
            String statement = buffer.toString();
            this.writer.write(statement);
            this.writer.write("\r\n");
            this.writer.flush();
        }
    }
}
