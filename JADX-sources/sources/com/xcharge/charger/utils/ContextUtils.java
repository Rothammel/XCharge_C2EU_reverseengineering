package com.xcharge.charger.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* loaded from: classes.dex */
public class ContextUtils {
    public static String getAPKSignature(String apkPath) {
        String signature = null;
        try {
            Class pkgParserCls = Class.forName("android.content.pm.PackageParser");
            Class[] typeArgs = {String.class};
            Constructor pkgParserCt = pkgParserCls.getConstructor(typeArgs);
            Object[] valueArgs = {apkPath};
            Object pkgParser = pkgParserCt.newInstance(valueArgs);
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();
            Class[] typeArgs2 = {File.class, String.class, DisplayMetrics.class, Integer.TYPE};
            Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs2);
            Object[] valueArgs2 = {new File(apkPath), apkPath, metrics, 64};
            Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs2);
            Class[] typeArgs3 = {pkgParserPkg.getClass(), Integer.TYPE};
            Method pkgParser_collectCertificatesMtd = pkgParserCls.getDeclaredMethod("collectCertificates", typeArgs3);
            Object[] valueArgs3 = {pkgParserPkg, 64};
            pkgParser_collectCertificatesMtd.invoke(pkgParser, valueArgs3);
            Field packageInfoFld = pkgParserPkg.getClass().getDeclaredField("mSignatures");
            Signature[] info = (Signature[]) packageInfoFld.get(pkgParserPkg);
            signature = info[0].toCharsString();
            Log.d("ContextUtils.getAPKSignature", "num:" + info.length + ", signature[0]: " + signature);
            return signature;
        } catch (Exception e) {
            Log.e("ContextUtils.getAPKSignature", Log.getStackTraceString(e));
            return signature;
        }
    }

    public static String getAPPSignature(Context context) {
        String signature = null;
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> apps = pm.getInstalledPackages(64);
        for (PackageInfo packageinfo : apps) {
            String packageName = packageinfo.packageName;
            if (packageName.equals(context.getPackageName())) {
                signature = packageinfo.signatures[0].toCharsString();
            }
        }
        Log.d("ContextUtils.getAPPSignature", "signature[0]: " + signature);
        return signature;
    }

    public static boolean checkPermission(Context context, String permissionName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            String pkgName = context.getPackageName();
            return packageManager.checkPermission(permissionName, pkgName) == 0;
        } catch (Exception e) {
            Log.e("ContextUtils.checkPermission", Log.getStackTraceString(e));
            return false;
        }
    }

    public static boolean hasActiveNetwork(Context context) {
        try {
            if (checkPermission(context, "android.permission.ACCESS_NETWORK_STATE")) {
                ConnectivityManager conMan = (ConnectivityManager) context.getSystemService("connectivity");
                NetworkInfo activeNetwork = conMan.getActiveNetworkInfo();
                if (activeNetwork != null) {
                    if (activeNetwork.isConnected()) {
                        return true;
                    }
                }
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.e("ContextUtils.hasActiveNetwork", Log.getStackTraceString(e));
            return false;
        }
    }

    public static boolean isNetworkConnected(Context context) {
        return hasActiveNetwork(context);
    }

    public static String getEthernetIpAddress(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
            LinkProperties properties = connectivityManager.getLinkProperties(9);
            if (properties != null) {
                String ipString = properties.getAddresses().toString();
                Pattern pattern = Pattern.compile("\\d+.\\d+.\\d+.\\d+");
                Matcher matcher = pattern.matcher(ipString);
                if (matcher.find()) {
                    return matcher.group();
                }
            }
        } catch (Exception e) {
            Log.e("ContextUtils.getEthernetIpAddress", Log.getStackTraceString(e));
        }
        return null;
    }

    public static boolean verifyApkAvailability(Context context, String apkPath) {
        PackageInfo info = null;
        try {
            PackageManager pm = context.getPackageManager();
            info = pm.getPackageArchiveInfo(apkPath, 1);
        } catch (Exception e) {
            Log.e("ContextUtils.verifyApkAvailability", Log.getStackTraceString(e));
        }
        return info != null;
    }

    public static boolean getRawFileToContextPath(Context context, int id, String fileName) {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            try {
                is = context.getResources().openRawResource(id);
                fos = context.openFileOutput(fileName, 0);
                byte[] buffer = new byte[512];
                while (true) {
                    int count = is.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    fos.write(buffer, 0, count);
                }
                return true;
            } catch (IOException e) {
                Log.e("ContextUtils.getRawFileToFile", Log.getStackTraceString(e));
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e2) {
                        Log.e("ContextUtils.getRawFileToFile", Log.getStackTraceString(e2));
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                        return false;
                    } catch (IOException e3) {
                        Log.e("ContextUtils.getRawFileToFile", Log.getStackTraceString(e3));
                        return false;
                    }
                }
                return false;
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e4) {
                    Log.e("ContextUtils.getRawFileToFile", Log.getStackTraceString(e4));
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e5) {
                    Log.e("ContextUtils.getRawFileToFile", Log.getStackTraceString(e5));
                }
            }
        }
    }
}
