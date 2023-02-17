package it.sauronsoftware.ftp4j.connectors;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class Base64 {
    static String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    static char pad = '=';

    Base64() {
    }

    public static String encode(String str) throws RuntimeException {
        byte[] bytes = str.getBytes();
        byte[] encoded = encode(bytes);
        try {
            return new String(encoded, "ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("ASCII is not supported!", e);
        }
    }

    public static String encode(String str, String charset) throws RuntimeException {
        try {
            byte[] bytes = str.getBytes(charset);
            byte[] encoded = encode(bytes);
            try {
                return new String(encoded, "ASCII");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("ASCII is not supported!", e);
            }
        } catch (UnsupportedEncodingException e2) {
            throw new RuntimeException(new StringBuffer().append("Unsupported charset: ").append(charset).toString(), e2);
        }
    }

    public static String decode(String str) throws RuntimeException {
        try {
            byte[] bytes = str.getBytes("ASCII");
            byte[] decoded = decode(bytes);
            return new String(decoded);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("ASCII is not supported!", e);
        }
    }

    public static String decode(String str, String charset) throws RuntimeException {
        try {
            byte[] bytes = str.getBytes("ASCII");
            byte[] decoded = decode(bytes);
            try {
                return new String(decoded, charset);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(new StringBuffer().append("Unsupported charset: ").append(charset).toString(), e);
            }
        } catch (UnsupportedEncodingException e2) {
            throw new RuntimeException("ASCII is not supported!", e2);
        }
    }

    public static byte[] encode(byte[] bytes) throws RuntimeException {
        return encode(bytes, 0);
    }

    public static byte[] encode(byte[] bytes, int wrapAt) throws RuntimeException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            try {
                encode(inputStream, outputStream, wrapAt);
                return outputStream.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("Unexpected I/O error", e);
            }
        } finally {
            try {
                inputStream.close();
            } catch (Throwable th) {
            }
            try {
                outputStream.close();
            } catch (Throwable th2) {
            }
        }
    }

    public static byte[] decode(byte[] bytes) throws RuntimeException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            try {
                decode(inputStream, outputStream);
                return outputStream.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("Unexpected I/O error", e);
            }
        } finally {
            try {
                inputStream.close();
            } catch (Throwable th) {
            }
            try {
                outputStream.close();
            } catch (Throwable th2) {
            }
        }
    }

    public static void encode(InputStream inputStream, OutputStream outputStream) throws IOException {
        encode(inputStream, outputStream, 0);
    }

    public static void encode(InputStream inputStream, OutputStream outputStream, int wrapAt) throws IOException {
        Base64OutputStream aux = new Base64OutputStream(outputStream, wrapAt);
        copy(inputStream, aux);
        aux.commit();
    }

    public static void decode(InputStream inputStream, OutputStream outputStream) throws IOException {
        copy(new Base64InputStream(inputStream), outputStream);
    }

    public static void encode(File source, File target, int wrapAt) throws IOException {
        InputStream inputStream;
        OutputStream outputStream;
        InputStream inputStream2 = null;
        OutputStream outputStream2 = null;
        try {
            inputStream = new FileInputStream(source);
            try {
                outputStream = new FileOutputStream(target);
            } catch (Throwable th) {
                th = th;
                inputStream2 = inputStream;
            }
        } catch (Throwable th2) {
            th = th2;
        }
        try {
            encode(inputStream, outputStream, wrapAt);
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Throwable th3) {
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable th4) {
                }
            }
        } catch (Throwable th5) {
            th = th5;
            outputStream2 = outputStream;
            inputStream2 = inputStream;
            if (outputStream2 != null) {
                try {
                    outputStream2.close();
                } catch (Throwable th6) {
                }
            }
            if (inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (Throwable th7) {
                }
            }
            throw th;
        }
    }

    public static void encode(File source, File target) throws IOException {
        InputStream inputStream;
        OutputStream outputStream;
        InputStream inputStream2 = null;
        OutputStream outputStream2 = null;
        try {
            inputStream = new FileInputStream(source);
            try {
                outputStream = new FileOutputStream(target);
            } catch (Throwable th) {
                th = th;
                inputStream2 = inputStream;
            }
        } catch (Throwable th2) {
            th = th2;
        }
        try {
            encode(inputStream, outputStream);
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Throwable th3) {
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable th4) {
                }
            }
        } catch (Throwable th5) {
            th = th5;
            outputStream2 = outputStream;
            inputStream2 = inputStream;
            if (outputStream2 != null) {
                try {
                    outputStream2.close();
                } catch (Throwable th6) {
                }
            }
            if (inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (Throwable th7) {
                }
            }
            throw th;
        }
    }

    public static void decode(File source, File target) throws IOException {
        InputStream inputStream;
        OutputStream outputStream;
        InputStream inputStream2 = null;
        OutputStream outputStream2 = null;
        try {
            inputStream = new FileInputStream(source);
            try {
                outputStream = new FileOutputStream(target);
            } catch (Throwable th) {
                th = th;
                inputStream2 = inputStream;
            }
        } catch (Throwable th2) {
            th = th2;
        }
        try {
            decode(inputStream, outputStream);
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Throwable th3) {
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable th4) {
                }
            }
        } catch (Throwable th5) {
            th = th5;
            outputStream2 = outputStream;
            inputStream2 = inputStream;
            if (outputStream2 != null) {
                try {
                    outputStream2.close();
                } catch (Throwable th6) {
                }
            }
            if (inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (Throwable th7) {
                }
            }
            throw th;
        }
    }

    private static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] b = new byte[1024];
        while (true) {
            int len = inputStream.read(b);
            if (len != -1) {
                outputStream.write(b, 0, len);
            } else {
                return;
            }
        }
    }
}