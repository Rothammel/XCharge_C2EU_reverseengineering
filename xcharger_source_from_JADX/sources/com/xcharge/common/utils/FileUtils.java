package com.xcharge.common.utils;

import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.protocol.anyo.bean.request.HeartBeatRequest;
import com.xcharge.common.utils.command.Command;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.zip.CRC32;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

public class FileUtils {
    private static char[] hexdigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static byte[] readByteArrayFromFile(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return null;
            }
            FileInputStream fin = new FileInputStream(file);
            buffer = new byte[fin.available()];
            fin.read(buffer);
            fin.close();
            return buffer;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteFiles(String rootPath) {
        File[] files;
        File[] fs;
        try {
            File file = new File(rootPath);
            if (file.exists() && (files = file.listFiles()) != null && files.length > 0) {
                for (File f : files) {
                    if (f.isFile()) {
                        f.delete();
                    } else if (f.isDirectory() && (fs = f.listFiles()) != null && fs.length > 0) {
                        deleteFiles(f.getPath());
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteFile(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0044 A[SYNTHETIC, Splitter:B:23:0x0044] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0049 A[Catch:{ Exception -> 0x0058 }] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x004e A[Catch:{ Exception -> 0x0058 }] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0053 A[Catch:{ Exception -> 0x0058 }] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0060 A[SYNTHETIC, Splitter:B:36:0x0060] */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0065 A[Catch:{ Exception -> 0x0073 }] */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x006a A[Catch:{ Exception -> 0x0073 }] */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x006f A[Catch:{ Exception -> 0x0073 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long fileChannelCopy(java.io.File r14, java.io.File r15) {
        /*
            r12 = 0
            r7 = 0
            r9 = 0
            r1 = 0
            r6 = 0
            java.io.FileInputStream r8 = new java.io.FileInputStream     // Catch:{ IOException -> 0x003e }
            r8.<init>(r14)     // Catch:{ IOException -> 0x003e }
            java.io.FileOutputStream r10 = new java.io.FileOutputStream     // Catch:{ IOException -> 0x007f, all -> 0x0078 }
            r10.<init>(r15)     // Catch:{ IOException -> 0x007f, all -> 0x0078 }
            java.nio.channels.FileChannel r1 = r8.getChannel()     // Catch:{ IOException -> 0x0082, all -> 0x007b }
            java.nio.channels.FileChannel r6 = r10.getChannel()     // Catch:{ IOException -> 0x0082, all -> 0x007b }
            r2 = 0
            long r4 = r1.size()     // Catch:{ IOException -> 0x0082, all -> 0x007b }
            long r2 = r1.transferTo(r2, r4, r6)     // Catch:{ IOException -> 0x0082, all -> 0x007b }
            if (r8 == 0) goto L_0x0027
            r8.close()     // Catch:{ Exception -> 0x0039 }
        L_0x0027:
            if (r1 == 0) goto L_0x002c
            r1.close()     // Catch:{ Exception -> 0x0039 }
        L_0x002c:
            if (r10 == 0) goto L_0x0031
            r10.close()     // Catch:{ Exception -> 0x0039 }
        L_0x0031:
            if (r6 == 0) goto L_0x0036
            r6.close()     // Catch:{ Exception -> 0x0039 }
        L_0x0036:
            r9 = r10
            r7 = r8
        L_0x0038:
            return r2
        L_0x0039:
            r0 = move-exception
            r0.printStackTrace()
            goto L_0x0036
        L_0x003e:
            r0 = move-exception
        L_0x003f:
            r0.printStackTrace()     // Catch:{ all -> 0x005d }
            if (r7 == 0) goto L_0x0047
            r7.close()     // Catch:{ Exception -> 0x0058 }
        L_0x0047:
            if (r1 == 0) goto L_0x004c
            r1.close()     // Catch:{ Exception -> 0x0058 }
        L_0x004c:
            if (r9 == 0) goto L_0x0051
            r9.close()     // Catch:{ Exception -> 0x0058 }
        L_0x0051:
            if (r6 == 0) goto L_0x0056
            r6.close()     // Catch:{ Exception -> 0x0058 }
        L_0x0056:
            r2 = r12
            goto L_0x0038
        L_0x0058:
            r0 = move-exception
            r0.printStackTrace()
            goto L_0x0056
        L_0x005d:
            r2 = move-exception
        L_0x005e:
            if (r7 == 0) goto L_0x0063
            r7.close()     // Catch:{ Exception -> 0x0073 }
        L_0x0063:
            if (r1 == 0) goto L_0x0068
            r1.close()     // Catch:{ Exception -> 0x0073 }
        L_0x0068:
            if (r9 == 0) goto L_0x006d
            r9.close()     // Catch:{ Exception -> 0x0073 }
        L_0x006d:
            if (r6 == 0) goto L_0x0072
            r6.close()     // Catch:{ Exception -> 0x0073 }
        L_0x0072:
            throw r2
        L_0x0073:
            r0 = move-exception
            r0.printStackTrace()
            goto L_0x0072
        L_0x0078:
            r2 = move-exception
            r7 = r8
            goto L_0x005e
        L_0x007b:
            r2 = move-exception
            r9 = r10
            r7 = r8
            goto L_0x005e
        L_0x007f:
            r0 = move-exception
            r7 = r8
            goto L_0x003f
        L_0x0082:
            r0 = move-exception
            r9 = r10
            r7 = r8
            goto L_0x003f
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.common.utils.FileUtils.fileChannelCopy(java.io.File, java.io.File):long");
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0031 A[SYNTHETIC, Splitter:B:15:0x0031] */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x003d A[SYNTHETIC, Splitter:B:21:0x003d] */
    /* JADX WARNING: Removed duplicated region for block: B:33:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean saveStringToFile(java.lang.String r6, java.lang.String r7, boolean r8) {
        /*
            r4 = 0
            boolean r5 = android.text.TextUtils.isEmpty(r7)
            if (r5 == 0) goto L_0x0008
        L_0x0007:
            return r4
        L_0x0008:
            java.io.File r1 = new java.io.File
            r1.<init>(r7)
            java.io.File r5 = r1.getParentFile()
            r5.mkdirs()
            r2 = 0
            java.io.FileOutputStream r3 = new java.io.FileOutputStream     // Catch:{ IOException -> 0x002b }
            r3.<init>(r1, r8)     // Catch:{ IOException -> 0x002b }
            byte[] r5 = r6.getBytes()     // Catch:{ IOException -> 0x004e, all -> 0x004b }
            r3.write(r5)     // Catch:{ IOException -> 0x004e, all -> 0x004b }
            r3.flush()     // Catch:{ IOException -> 0x004e, all -> 0x004b }
            if (r3 == 0) goto L_0x0029
            r3.close()     // Catch:{ IOException -> 0x0046 }
        L_0x0029:
            r4 = 1
            goto L_0x0007
        L_0x002b:
            r0 = move-exception
        L_0x002c:
            r0.printStackTrace()     // Catch:{ all -> 0x003a }
            if (r2 == 0) goto L_0x0007
            r2.close()     // Catch:{ IOException -> 0x0035 }
            goto L_0x0007
        L_0x0035:
            r0 = move-exception
            r0.printStackTrace()
            goto L_0x0007
        L_0x003a:
            r4 = move-exception
        L_0x003b:
            if (r2 == 0) goto L_0x0040
            r2.close()     // Catch:{ IOException -> 0x0041 }
        L_0x0040:
            throw r4
        L_0x0041:
            r0 = move-exception
            r0.printStackTrace()
            goto L_0x0040
        L_0x0046:
            r0 = move-exception
            r0.printStackTrace()
            goto L_0x0029
        L_0x004b:
            r4 = move-exception
            r2 = r3
            goto L_0x003b
        L_0x004e:
            r0 = move-exception
            r2 = r3
            goto L_0x002c
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.common.utils.FileUtils.saveStringToFile(java.lang.String, java.lang.String, boolean):boolean");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x009a, code lost:
        r12 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        r12.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x009e, code lost:
        r7 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:?, code lost:
        r17.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:?, code lost:
        r13.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00bc, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00bd, code lost:
        r8.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00c1, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00c2, code lost:
        r8.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:?, code lost:
        r17.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:?, code lost:
        r13.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00d2, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00d3, code lost:
        r8.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00d7, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00d8, code lost:
        r8.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x00df, code lost:
        r19 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00e0, code lost:
        r17 = r18;
        r13 = r14;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x00ed, code lost:
        r4 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x00ee, code lost:
        r17 = r18;
        r13 = r14;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x00f2, code lost:
        r12 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x00f3, code lost:
        r6 = r7;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00b0 A[SYNTHETIC, Splitter:B:43:0x00b0] */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00b5 A[SYNTHETIC, Splitter:B:46:0x00b5] */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00c9 A[SYNTHETIC, Splitter:B:55:0x00c9] */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00ce A[SYNTHETIC, Splitter:B:58:0x00ce] */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x00df A[ExcHandler: all (th java.lang.Throwable), Splitter:B:9:0x0024] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean Unzip(java.lang.String r21, java.lang.String r22) {
        /*
            if (r21 == 0) goto L_0x0004
            if (r22 != 0) goto L_0x0007
        L_0x0004:
            r19 = 0
        L_0x0006:
            return r19
        L_0x0007:
            r2 = 4096(0x1000, float:5.74E-42)
            r16 = 0
            r13 = 0
            r17 = 0
            r6 = 0
            java.io.FileInputStream r14 = new java.io.FileInputStream     // Catch:{ Exception -> 0x00aa }
            r0 = r21
            r14.<init>(r0)     // Catch:{ Exception -> 0x00aa }
            java.util.zip.ZipInputStream r18 = new java.util.zip.ZipInputStream     // Catch:{ Exception -> 0x00e4, all -> 0x00dc }
            java.io.BufferedInputStream r19 = new java.io.BufferedInputStream     // Catch:{ Exception -> 0x00e4, all -> 0x00dc }
            r0 = r19
            r0.<init>(r14)     // Catch:{ Exception -> 0x00e4, all -> 0x00dc }
            r18.<init>(r19)     // Catch:{ Exception -> 0x00e4, all -> 0x00dc }
            r9 = 0
            r7 = r6
        L_0x0024:
            java.util.zip.ZipEntry r9 = r18.getNextEntry()     // Catch:{ Exception -> 0x00e7, all -> 0x00df }
            if (r9 != 0) goto L_0x003a
            r18.close()     // Catch:{ Exception -> 0x00e7, all -> 0x00df }
            if (r18 == 0) goto L_0x0032
            r18.close()     // Catch:{ IOException -> 0x00a0 }
        L_0x0032:
            if (r14 == 0) goto L_0x0037
            r14.close()     // Catch:{ IOException -> 0x00a5 }
        L_0x0037:
            r19 = 1
            goto L_0x0006
        L_0x003a:
            byte[] r5 = new byte[r2]     // Catch:{ Exception -> 0x00f2, all -> 0x00df }
            java.lang.String r16 = r9.getName()     // Catch:{ Exception -> 0x00f2, all -> 0x00df }
            java.io.File r11 = new java.io.File     // Catch:{ Exception -> 0x00f2, all -> 0x00df }
            java.lang.StringBuilder r19 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00f2, all -> 0x00df }
            java.lang.String r20 = java.lang.String.valueOf(r22)     // Catch:{ Exception -> 0x00f2, all -> 0x00df }
            r19.<init>(r20)     // Catch:{ Exception -> 0x00f2, all -> 0x00df }
            r0 = r19
            r1 = r16
            java.lang.StringBuilder r19 = r0.append(r1)     // Catch:{ Exception -> 0x00f2, all -> 0x00df }
            java.lang.String r19 = r19.toString()     // Catch:{ Exception -> 0x00f2, all -> 0x00df }
            r0 = r19
            r11.<init>(r0)     // Catch:{ Exception -> 0x00f2, all -> 0x00df }
            java.io.File r10 = new java.io.File     // Catch:{ Exception -> 0x00f2, all -> 0x00df }
            java.lang.String r19 = r11.getParent()     // Catch:{ Exception -> 0x00f2, all -> 0x00df }
            r0 = r19
            r10.<init>(r0)     // Catch:{ Exception -> 0x00f2, all -> 0x00df }
            boolean r19 = r10.exists()     // Catch:{ Exception -> 0x00f2, all -> 0x00df }
            if (r19 != 0) goto L_0x0070
            r10.mkdirs()     // Catch:{ Exception -> 0x00f2, all -> 0x00df }
        L_0x0070:
            java.io.FileOutputStream r15 = new java.io.FileOutputStream     // Catch:{ Exception -> 0x00f2, all -> 0x00df }
            r15.<init>(r11)     // Catch:{ Exception -> 0x00f2, all -> 0x00df }
            java.io.BufferedOutputStream r6 = new java.io.BufferedOutputStream     // Catch:{ Exception -> 0x00f2, all -> 0x00df }
            r6.<init>(r15, r2)     // Catch:{ Exception -> 0x00f2, all -> 0x00df }
        L_0x007a:
            r19 = 0
            r0 = r18
            r1 = r19
            int r3 = r0.read(r5, r1, r2)     // Catch:{ Exception -> 0x009a, all -> 0x00df }
            r19 = -1
            r0 = r19
            if (r3 != r0) goto L_0x0092
            r6.flush()     // Catch:{ Exception -> 0x009a, all -> 0x00df }
            r6.close()     // Catch:{ Exception -> 0x009a, all -> 0x00df }
            r7 = r6
            goto L_0x0024
        L_0x0092:
            r19 = 0
            r0 = r19
            r6.write(r5, r0, r3)     // Catch:{ Exception -> 0x009a, all -> 0x00df }
            goto L_0x007a
        L_0x009a:
            r12 = move-exception
        L_0x009b:
            r12.printStackTrace()     // Catch:{ Exception -> 0x00ed, all -> 0x00df }
            r7 = r6
            goto L_0x0024
        L_0x00a0:
            r8 = move-exception
            r8.printStackTrace()
            goto L_0x0032
        L_0x00a5:
            r8 = move-exception
            r8.printStackTrace()
            goto L_0x0037
        L_0x00aa:
            r4 = move-exception
        L_0x00ab:
            r4.printStackTrace()     // Catch:{ all -> 0x00c6 }
            if (r17 == 0) goto L_0x00b3
            r17.close()     // Catch:{ IOException -> 0x00bc }
        L_0x00b3:
            if (r13 == 0) goto L_0x00b8
            r13.close()     // Catch:{ IOException -> 0x00c1 }
        L_0x00b8:
            r19 = 0
            goto L_0x0006
        L_0x00bc:
            r8 = move-exception
            r8.printStackTrace()
            goto L_0x00b3
        L_0x00c1:
            r8 = move-exception
            r8.printStackTrace()
            goto L_0x00b8
        L_0x00c6:
            r19 = move-exception
        L_0x00c7:
            if (r17 == 0) goto L_0x00cc
            r17.close()     // Catch:{ IOException -> 0x00d2 }
        L_0x00cc:
            if (r13 == 0) goto L_0x00d1
            r13.close()     // Catch:{ IOException -> 0x00d7 }
        L_0x00d1:
            throw r19
        L_0x00d2:
            r8 = move-exception
            r8.printStackTrace()
            goto L_0x00cc
        L_0x00d7:
            r8 = move-exception
            r8.printStackTrace()
            goto L_0x00d1
        L_0x00dc:
            r19 = move-exception
            r13 = r14
            goto L_0x00c7
        L_0x00df:
            r19 = move-exception
            r17 = r18
            r13 = r14
            goto L_0x00c7
        L_0x00e4:
            r4 = move-exception
            r13 = r14
            goto L_0x00ab
        L_0x00e7:
            r4 = move-exception
            r6 = r7
            r17 = r18
            r13 = r14
            goto L_0x00ab
        L_0x00ed:
            r4 = move-exception
            r17 = r18
            r13 = r14
            goto L_0x00ab
        L_0x00f2:
            r12 = move-exception
            r6 = r7
            goto L_0x009b
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.common.utils.FileUtils.Unzip(java.lang.String, java.lang.String):boolean");
    }

    public static String getMD5(File file) {
        FileInputStream fis = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis2 = new FileInputStream(file);
            try {
                byte[] buffer = new byte[2048];
                while (true) {
                    int length = fis2.read(buffer);
                    if (length == -1) {
                        break;
                    }
                    md.update(buffer, 0, length);
                }
                String byteToHexString = byteToHexString(md.digest());
                try {
                    fis2.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                FileInputStream fileInputStream = fis2;
                return byteToHexString;
            } catch (Exception e) {
                ex = e;
                fis = fis2;
            } catch (Throwable th) {
                th = th;
                fis = fis2;
                try {
                    fis.close();
                } catch (IOException ex2) {
                    ex2.printStackTrace();
                }
                throw th;
            }
        } catch (Exception e2) {
            ex = e2;
            try {
                ex.printStackTrace();
                try {
                    fis.close();
                } catch (IOException ex3) {
                    ex3.printStackTrace();
                }
                return null;
            } catch (Throwable th2) {
                th = th2;
                fis.close();
                throw th;
            }
        }
    }

    private static String byteToHexString(byte[] tmp) {
        char[] str = new char[32];
        int k = 0;
        for (int i = 0; i < 16; i++) {
            byte byte0 = tmp[i];
            int k2 = k + 1;
            str[k] = hexdigits[(byte0 >>> 4) & 15];
            k = k2 + 1;
            str[k2] = hexdigits[byte0 & HeartBeatRequest.PORT_STATUS_FAULT];
        }
        return new String(str);
    }

    public static Long getCRC32(File file) {
        FileInputStream fis = null;
        try {
            CRC32 snCrc32 = new CRC32();
            FileInputStream fis2 = new FileInputStream(file);
            try {
                byte[] buffer = new byte[2048];
                while (true) {
                    int length = fis2.read(buffer);
                    if (length == -1) {
                        break;
                    }
                    snCrc32.update(buffer, 0, length);
                }
                Long valueOf = Long.valueOf(snCrc32.getValue());
                try {
                    fis2.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                FileInputStream fileInputStream = fis2;
                return valueOf;
            } catch (Exception e) {
                ex = e;
                fis = fis2;
            } catch (Throwable th) {
                th = th;
                fis = fis2;
                try {
                    fis.close();
                } catch (IOException ex2) {
                    ex2.printStackTrace();
                }
                throw th;
            }
        } catch (Exception e2) {
            ex = e2;
            try {
                ex.printStackTrace();
                try {
                    fis.close();
                } catch (IOException ex3) {
                    ex3.printStackTrace();
                }
                return null;
            } catch (Throwable th2) {
                th = th2;
                fis.close();
                throw th;
            }
        }
    }

    public static int execShell(String cmd) {
        try {
            Object[] ret = execShellAndOutput(cmd, 0);
            if (ret == null) {
                return -1;
            }
            return ((Integer) ret[0]).intValue();
        } catch (Exception e) {
            return -1;
        }
    }

    public static int execShell(String cmd, int timeout) {
        try {
            Object[] ret = execShellAndOutput(cmd, timeout);
            if (ret == null) {
                return -1;
            }
            return ((Integer) ret[0]).intValue();
        } catch (Exception e) {
            return -1;
        }
    }

    public static Object[] execShellAndOutput(String cmd) throws Exception {
        String output;
        int status = -1;
        Process pcs = Runtime.getRuntime().exec(new String[]{"/system/bin/sh", "-c", cmd});
        InputStream stdout = pcs.getInputStream();
        InputStream stderr = pcs.getErrorStream();
        final BufferedReader outReader = new BufferedReader(new InputStreamReader(stdout, CharEncoding.UTF_8));
        final BufferedReader errReader = new BufferedReader(new InputStreamReader(stderr, CharEncoding.UTF_8));
        final StringBuilder out = new StringBuilder("");
        final StringBuilder err = new StringBuilder("");
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        String line = errReader.readLine();
                        if (line != null) {
                            err.append(line).append(StringUtils.f146LF);
                        } else {
                            return;
                        }
                    } catch (Exception e) {
                        Log.e("FileUtils.execShellAndOutput", Log.getStackTraceString(e));
                        return;
                    }
                }
            }
        }).start();
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        String line = outReader.readLine();
                        if (line != null) {
                            out.append(line).append(StringUtils.f146LF);
                        } else {
                            return;
                        }
                    } catch (Exception e) {
                        Log.e("FileUtils.execShellAndOutput", Log.getStackTraceString(e));
                        return;
                    }
                }
            }
        }).start();
        try {
            status = pcs.waitFor();
            Log.d("FileUtils.execShellAndOutput", "cmd: " + cmd + ", status: " + status);
        } catch (Exception e) {
            Log.e("FileUtils.execShellAndOutput", Log.getStackTraceString(e));
        }
        if (status == 0) {
            output = out.toString();
        } else {
            output = err.toString();
        }
        Object[] ret = {Integer.valueOf(status), null};
        if (!TextUtils.isEmpty(output)) {
            Log.d("FileUtils.execShellAndOutput", "output:\n" + output);
            ret[1] = output;
        }
        return ret;
    }

    public static Object[] execShellAndOutput(String cmd, int timeout) throws Exception {
        try {
            List<String> cmdOut = new Command("/system/bin/sh", "-c", cmd).executeWithTimeout(timeout);
            Object[] ret = {Integer.valueOf(Integer.parseInt(cmdOut.get(0))), null};
            int outLen = cmdOut.size();
            if (outLen > 1) {
                StringBuilder message = new StringBuilder();
                for (int i = 1; i < outLen; i++) {
                    message.append(cmdOut.get(i));
                    if (i != outLen - 1) {
                        message.append(StringUtils.f146LF);
                    }
                }
                ret[1] = message.toString();
                Log.d("FileUtils.execShellAndOutput", "output:\n" + ret[1]);
            }
            return ret;
        } catch (TimeoutException e) {
            Log.w("FileUtils.execShellAndOutput", "timeout on cmd: " + cmd + ", timeout: " + timeout);
            throw e;
        }
    }

    private static void closeQuietly(Object object) {
        if (object instanceof InputStream) {
            try {
                ((InputStream) object).close();
            } catch (Exception e) {
                Log.e("FileUtils.closeQuietly", Log.getStackTraceString(e));
            }
        }
        if (object instanceof OutputStream) {
            try {
                ((OutputStream) object).close();
            } catch (Exception e2) {
                Log.e("FileUtils.closeQuietly", Log.getStackTraceString(e2));
            }
        }
    }

    public static void killProcess(String like) {
        try {
            Object[] outputs = execShellAndOutput("ps | grep " + like, 4);
            if (outputs != null) {
                String output = (String) outputs[1];
                if (!TextUtils.isEmpty(output)) {
                    String[] splits = output.split("\\s+");
                    if (splits.length > 2) {
                        execShell("adb remount;adb shell kill -9 " + Integer.valueOf(Integer.parseInt(splits[1])));
                    }
                }
            }
        } catch (Exception e) {
            Log.e("FileUtils.killProcess", Log.getStackTraceString(e));
        }
    }
}
