package com.nostra13.universalimageloader.cache.disc.impl.ext;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import org.apache.commons.lang3.CharEncoding;

/* loaded from: classes.dex */
final class Util {
    static final Charset US_ASCII = Charset.forName(CharEncoding.US_ASCII);
    static final Charset UTF_8 = Charset.forName(CharEncoding.UTF_8);

    private Util() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static String readFully(Reader reader) throws IOException {
        try {
            StringWriter writer = new StringWriter();
            char[] buffer = new char[1024];
            while (true) {
                int count = reader.read(buffer);
                if (count != -1) {
                    writer.write(buffer, 0, count);
                } else {
                    return writer.toString();
                }
            }
        } finally {
            reader.close();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void deleteContents(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            throw new IOException("not a readable directory: " + dir);
        }
        for (File file : files) {
            if (file.isDirectory()) {
                deleteContents(file);
            }
            if (!file.delete()) {
                throw new IOException("failed to delete file: " + file);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception e) {
            }
        }
    }
}