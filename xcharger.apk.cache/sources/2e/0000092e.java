package com.xcharge.common.utils;

import android.util.Log;
import com.google.zxing.common.StringUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/* loaded from: classes.dex */
public class ZipUtils {
    private static final int BUFF_SIZE = 1024;

    public static void zipFiles(Collection<File> resFileList, File zipFile) throws Exception {
        ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(zipFile));
        for (File resFile : resFileList) {
            try {
                zipFile(resFile, zipout, "");
            } catch (Exception e) {
                Log.e("ZipUtils.zipFiles", Log.getStackTraceString(e));
                LogUtils.cloudlog("failed to zip file: " + resFile);
            }
        }
        zipout.close();
    }

    public static void zipFiles(Collection<File> resFileList, File zipFile, String comment) throws Exception {
        ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(zipFile));
        for (File resFile : resFileList) {
            zipFile(resFile, zipout, "");
        }
        zipout.setComment(comment);
        zipout.close();
    }

    public static void upZipFile(File zipFile, String folderPath) throws Exception {
        File desDir = new File(folderPath);
        if (!desDir.exists()) {
            desDir.mkdirs();
        }
        ZipFile zf = new ZipFile(zipFile);
        Enumeration<?> entries = zf.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            InputStream in = zf.getInputStream(entry);
            String str = String.valueOf(folderPath) + File.separator + entry.getName();
            File desFile = new File(new String(str.getBytes("8859_1"), StringUtils.GB2312));
            if (!desFile.exists()) {
                File fileParentDir = desFile.getParentFile();
                if (!fileParentDir.exists()) {
                    fileParentDir.mkdirs();
                }
                desFile.createNewFile();
            }
            OutputStream out = new FileOutputStream(desFile);
            byte[] buffer = new byte[1024];
            while (true) {
                int realLength = in.read(buffer);
                if (realLength == -1) {
                    break;
                }
                out.write(buffer, 0, realLength);
            }
            in.close();
            out.close();
        }
    }

    public static ArrayList<File> upZipSelectedFile(File zipFile, String folderPath, String nameContains) throws Exception {
        ArrayList<File> fileList = new ArrayList<>();
        File desDir = new File(folderPath);
        if (!desDir.exists()) {
            desDir.mkdir();
        }
        ZipFile zf = new ZipFile(zipFile);
        Enumeration<?> entries = zf.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().contains(nameContains)) {
                InputStream in = zf.getInputStream(entry);
                String str = String.valueOf(folderPath) + File.separator + entry.getName();
                File desFile = new File(new String(str.getBytes("8859_1"), StringUtils.GB2312));
                if (!desFile.exists()) {
                    File fileParentDir = desFile.getParentFile();
                    if (!fileParentDir.exists()) {
                        fileParentDir.mkdirs();
                    }
                    desFile.createNewFile();
                }
                OutputStream out = new FileOutputStream(desFile);
                byte[] buffer = new byte[1024];
                while (true) {
                    int realLength = in.read(buffer);
                    if (realLength == -1) {
                        break;
                    }
                    out.write(buffer, 0, realLength);
                }
                in.close();
                out.close();
                fileList.add(desFile);
            }
        }
        return fileList;
    }

    public static ArrayList<String> getEntriesNames(File zipFile) throws Exception {
        ArrayList<String> entryNames = new ArrayList<>();
        Enumeration<?> entries = getEntriesEnumeration(zipFile);
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            entryNames.add(new String(getEntryName(entry).getBytes(StringUtils.GB2312), "8859_1"));
        }
        return entryNames;
    }

    public static Enumeration<?> getEntriesEnumeration(File zipFile) throws Exception {
        ZipFile zf = new ZipFile(zipFile);
        return zf.entries();
    }

    public static String getEntryComment(ZipEntry entry) throws Exception {
        return new String(entry.getComment().getBytes(StringUtils.GB2312), "8859_1");
    }

    public static String getEntryName(ZipEntry entry) throws Exception {
        return new String(entry.getName().getBytes(StringUtils.GB2312), "8859_1");
    }

    private static void zipFile(File resFile, ZipOutputStream zipout, String rootpath) throws Exception {
        String rootpath2 = new String((String.valueOf(rootpath) + (rootpath.trim().length() == 0 ? "" : File.separator) + resFile.getName()).getBytes("8859_1"), StringUtils.GB2312);
        if (resFile.isDirectory()) {
            File[] fileList = resFile.listFiles();
            for (File file : fileList) {
                zipFile(file, zipout, rootpath2);
            }
            return;
        }
        byte[] buffer = new byte[1024];
        FileInputStream in = new FileInputStream(resFile);
        zipout.putNextEntry(new ZipEntry(rootpath2));
        while (true) {
            int realLength = in.read(buffer);
            if (realLength != -1) {
                zipout.write(buffer, 0, realLength);
            } else {
                in.close();
                zipout.flush();
                zipout.closeEntry();
                return;
            }
        }
    }
}