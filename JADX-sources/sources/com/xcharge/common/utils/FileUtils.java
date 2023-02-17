package com.xcharge.common.utils;

import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.protocol.anyo.bean.request.HeartBeatRequest;
import com.xcharge.common.utils.command.Command;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

/* loaded from: classes.dex */
public class FileUtils {
    private static char[] hexdigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static byte[] readByteArrayFromFile(String filePath) {
        File file;
        byte[] buffer = null;
        try {
            file = new File(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!file.exists()) {
            return null;
        }
        FileInputStream fin = new FileInputStream(file);
        int length = fin.available();
        buffer = new byte[length];
        fin.read(buffer);
        fin.close();
        return buffer;
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

    public static long fileChannelCopy(File s, File t) {
        FileInputStream fi = null;
        FileOutputStream fo = null;
        FileChannel in = null;
        FileChannel out = null;
        try {
            try {
                FileInputStream fi2 = new FileInputStream(s);
                try {
                    FileOutputStream fo2 = new FileOutputStream(t);
                    try {
                        in = fi2.getChannel();
                        out = fo2.getChannel();
                        long transferTo = in.transferTo(0L, in.size(), out);
                        if (fi2 != null) {
                            try {
                                fi2.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (in != 0) {
                            in.close();
                        }
                        if (fo2 != null) {
                            fo2.close();
                        }
                        if (out != null) {
                            out.close();
                        }
                        fo = fo2;
                        fi = fi2;
                        return transferTo;
                    } catch (IOException e2) {
                        e = e2;
                        fo = fo2;
                        fi = fi2;
                        e.printStackTrace();
                        if (fi != null) {
                            try {
                                fi.close();
                            } catch (Exception e3) {
                                e3.printStackTrace();
                                return 0L;
                            }
                        }
                        if (in != null) {
                            in.close();
                        }
                        if (fo != null) {
                            fo.close();
                        }
                        if (out != null) {
                            out.close();
                        }
                        return 0L;
                    } catch (Throwable th) {
                        th = th;
                        fo = fo2;
                        fi = fi2;
                        if (fi != null) {
                            try {
                                fi.close();
                            } catch (Exception e4) {
                                e4.printStackTrace();
                                throw th;
                            }
                        }
                        if (in != null) {
                            in.close();
                        }
                        if (fo != null) {
                            fo.close();
                        }
                        if (out != null) {
                            out.close();
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    e = e5;
                    fi = fi2;
                } catch (Throwable th2) {
                    th = th2;
                    fi = fi2;
                }
            } catch (IOException e6) {
                e = e6;
            }
        } catch (Throwable th3) {
            th = th3;
        }
    }

    public static boolean saveStringToFile(String content, String filePath, boolean append) {
        boolean z = false;
        if (!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            FileOutputStream outputStream = null;
            try {
                try {
                    FileOutputStream outputStream2 = new FileOutputStream(file, append);
                    try {
                        outputStream2.write(content.getBytes());
                        outputStream2.flush();
                        if (outputStream2 != null) {
                            try {
                                outputStream2.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        z = true;
                    } catch (IOException e2) {
                        e = e2;
                        outputStream = outputStream2;
                        e.printStackTrace();
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                        }
                        return z;
                    } catch (Throwable th) {
                        th = th;
                        outputStream = outputStream2;
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e4) {
                                e4.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    e = e5;
                }
            } catch (Throwable th2) {
                th = th2;
            }
        }
        return z;
    }

    public static boolean Unzip(String zipFile, String targetDir) {
        boolean z;
        BufferedOutputStream dest;
        if (zipFile == null || targetDir == null) {
            return false;
        }
        FileInputStream fis = null;
        ZipInputStream zis = null;
        try {
            try {
                FileInputStream fis2 = new FileInputStream(zipFile);
                try {
                    ZipInputStream zis2 = new ZipInputStream(new BufferedInputStream(fis2));
                    BufferedOutputStream dest2 = null;
                    while (true) {
                        try {
                            try {
                                ZipEntry entry = zis2.getNextEntry();
                                if (entry == null) {
                                    break;
                                }
                                try {
                                    byte[] data = new byte[4096];
                                    String strEntry = entry.getName();
                                    File entryFile = new File(String.valueOf(targetDir) + strEntry);
                                    File entryDir = new File(entryFile.getParent());
                                    if (!entryDir.exists()) {
                                        entryDir.mkdirs();
                                    }
                                    FileOutputStream fos = new FileOutputStream(entryFile);
                                    dest = new BufferedOutputStream(fos, 4096);
                                    while (true) {
                                        try {
                                            int count = zis2.read(data, 0, 4096);
                                            if (count == -1) {
                                                break;
                                            }
                                            dest.write(data, 0, count);
                                        } catch (Exception e) {
                                            ex = e;
                                            try {
                                                ex.printStackTrace();
                                                dest2 = dest;
                                            } catch (Exception e2) {
                                                cwj = e2;
                                                zis = zis2;
                                                fis = fis2;
                                                cwj.printStackTrace();
                                                if (zis != null) {
                                                    try {
                                                        zis.close();
                                                    } catch (IOException e3) {
                                                        e3.printStackTrace();
                                                    }
                                                }
                                                if (fis != null) {
                                                    try {
                                                        fis.close();
                                                    } catch (IOException e4) {
                                                        e4.printStackTrace();
                                                    }
                                                }
                                                z = false;
                                                return z;
                                            }
                                        }
                                    }
                                    dest.flush();
                                    dest.close();
                                    dest2 = dest;
                                } catch (Exception e5) {
                                    ex = e5;
                                    dest = dest2;
                                }
                            } catch (Throwable th) {
                                th = th;
                                zis = zis2;
                                fis = fis2;
                                if (zis != null) {
                                    try {
                                        zis.close();
                                    } catch (IOException e6) {
                                        e6.printStackTrace();
                                    }
                                }
                                if (fis != null) {
                                    try {
                                        fis.close();
                                    } catch (IOException e7) {
                                        e7.printStackTrace();
                                    }
                                }
                                throw th;
                            }
                        } catch (Exception e8) {
                            cwj = e8;
                            zis = zis2;
                            fis = fis2;
                        }
                    }
                    zis2.close();
                    if (zis2 != null) {
                        try {
                            zis2.close();
                        } catch (IOException e9) {
                            e9.printStackTrace();
                        }
                    }
                    if (fis2 != null) {
                        try {
                            fis2.close();
                        } catch (IOException e10) {
                            e10.printStackTrace();
                        }
                    }
                    z = true;
                } catch (Exception e11) {
                    cwj = e11;
                    fis = fis2;
                } catch (Throwable th2) {
                    th = th2;
                    fis = fis2;
                }
            } catch (Exception e12) {
                cwj = e12;
            }
            return z;
        } catch (Throwable th3) {
            th = th3;
        }
    }

    public static String getMD5(File file) {
        FileInputStream fis = null;
        try {
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
                    byte[] b = md.digest();
                    String byteToHexString = byteToHexString(b);
                    try {
                        fis2.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    return byteToHexString;
                } catch (Exception e) {
                    ex = e;
                    fis = fis2;
                    ex.printStackTrace();
                    try {
                        fis.close();
                    } catch (IOException ex2) {
                        ex2.printStackTrace();
                    }
                    return null;
                } catch (Throwable th) {
                    th = th;
                    fis = fis2;
                    try {
                        fis.close();
                    } catch (IOException ex3) {
                        ex3.printStackTrace();
                    }
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
            }
        } catch (Exception e2) {
            ex = e2;
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
        String s = new String(str);
        return s;
    }

    public static Long getCRC32(File file) {
        FileInputStream fis = null;
        try {
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
                    return valueOf;
                } catch (Exception e) {
                    ex = e;
                    fis = fis2;
                    ex.printStackTrace();
                    try {
                        fis.close();
                    } catch (IOException ex2) {
                        ex2.printStackTrace();
                    }
                    return null;
                } catch (Throwable th) {
                    th = th;
                    fis = fis2;
                    try {
                        fis.close();
                    } catch (IOException ex3) {
                        ex3.printStackTrace();
                    }
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
            }
        } catch (Exception e2) {
            ex = e2;
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
        String[] str = {"/system/bin/sh", "-c", cmd};
        Process pcs = Runtime.getRuntime().exec(str);
        InputStream stdout = pcs.getInputStream();
        InputStream stderr = pcs.getErrorStream();
        final BufferedReader outReader = new BufferedReader(new InputStreamReader(stdout, CharEncoding.UTF_8));
        final BufferedReader errReader = new BufferedReader(new InputStreamReader(stderr, CharEncoding.UTF_8));
        final StringBuilder out = new StringBuilder("");
        final StringBuilder err = new StringBuilder("");
        new Thread(new Runnable() { // from class: com.xcharge.common.utils.FileUtils.1
            @Override // java.lang.Runnable
            public void run() {
                while (true) {
                    try {
                        String line = errReader.readLine();
                        if (line != null) {
                            err.append(line).append(StringUtils.LF);
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
        new Thread(new Runnable() { // from class: com.xcharge.common.utils.FileUtils.2
            @Override // java.lang.Runnable
            public void run() {
                while (true) {
                    try {
                        String line = outReader.readLine();
                        if (line != null) {
                            out.append(line).append(StringUtils.LF);
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
        String[] cmdArr = {"/system/bin/sh", "-c", cmd};
        try {
            List<String> cmdOut = new Command(cmdArr).executeWithTimeout(timeout);
            Object[] ret = {Integer.valueOf(Integer.parseInt(cmdOut.get(0))), null};
            int outLen = cmdOut.size();
            if (outLen > 1) {
                StringBuilder message = new StringBuilder();
                for (int i = 1; i < outLen; i++) {
                    message.append(cmdOut.get(i));
                    if (i != outLen - 1) {
                        message.append(StringUtils.LF);
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
                        Integer pid = Integer.valueOf(Integer.parseInt(splits[1]));
                        execShell("adb remount;adb shell kill -9 " + pid);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("FileUtils.killProcess", Log.getStackTraceString(e));
        }
    }
}
