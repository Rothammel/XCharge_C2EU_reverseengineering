package com.nostra13.universalimageloader.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/* loaded from: classes.dex */
public final class IoUtils {
    public static final int CONTINUE_LOADING_PERCENTAGE = 75;
    public static final int DEFAULT_BUFFER_SIZE = 32768;
    public static final int DEFAULT_IMAGE_TOTAL_SIZE = 512000;

    /* loaded from: classes.dex */
    public interface CopyListener {
        boolean onBytesCopied(int i, int i2);
    }

    private IoUtils() {
    }

    public static boolean copyStream(InputStream is, OutputStream os, CopyListener listener) throws IOException {
        return copyStream(is, os, listener, 32768);
    }

    public static boolean copyStream(InputStream is, OutputStream os, CopyListener listener, int bufferSize) throws IOException {
        int current = 0;
        int total = is.available();
        if (total <= 0) {
            total = DEFAULT_IMAGE_TOTAL_SIZE;
        }
        byte[] bytes = new byte[bufferSize];
        if (shouldStopLoading(listener, 0, total)) {
            return false;
        }
        do {
            int count = is.read(bytes, 0, bufferSize);
            if (count != -1) {
                os.write(bytes, 0, count);
                current += count;
            } else {
                os.flush();
                return true;
            }
        } while (!shouldStopLoading(listener, current, total));
        return false;
    }

    private static boolean shouldStopLoading(CopyListener listener, int current, int total) {
        if (listener != null) {
            boolean shouldContinue = listener.onBytesCopied(current, total);
            if (!shouldContinue && (current * 100) / total < 75) {
                return true;
            }
        }
        return false;
    }

    public static void readAndCloseStream(InputStream is) {
        byte[] bytes = new byte[32768];
        do {
            try {
            } catch (IOException e) {
                return;
            } finally {
                closeSilently(is);
            }
        } while (is.read(bytes, 0, 32768) != -1);
    }

    public static void closeSilently(Closeable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
        }
    }
}
