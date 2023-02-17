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
        Enumeration<? extends ZipEntry> entries = zf.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            InputStream in = zf.getInputStream(entry);
            File desFile = new File(new String((String.valueOf(folderPath) + File.separator + entry.getName()).getBytes("8859_1"), StringUtils.GB2312));
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
        Enumeration<? extends ZipEntry> entries = zf.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (entry.getName().contains(nameContains)) {
                InputStream in = zf.getInputStream(entry);
                File desFile = new File(new String((String.valueOf(folderPath) + File.separator + entry.getName()).getBytes("8859_1"), StringUtils.GB2312));
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
            entryNames.add(new String(getEntryName((ZipEntry) entries.nextElement()).getBytes(StringUtils.GB2312), "8859_1"));
        }
        return entryNames;
    }

    public static Enumeration<?> getEntriesEnumeration(File zipFile) throws Exception {
        return new ZipFile(zipFile).entries();
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
            for (File file : resFile.listFiles()) {
                zipFile(file, zipout, rootpath2);
            }
            return;
        }
        byte[] buffer = new byte[1024];
        FileInputStream in = new FileInputStream(resFile);
        zipout.putNextEntry(new ZipEntry(rootpath2));
        while (true) {
            int realLength = in.read(buffer);
            if (realLength == -1) {
                in.close();
                zipout.flush();
                zipout.closeEntry();
                return;
            }
            zipout.write(buffer, 0, realLength);
        }
    }
}
