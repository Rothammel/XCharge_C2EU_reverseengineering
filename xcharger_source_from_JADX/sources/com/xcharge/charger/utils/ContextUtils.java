package com.xcharge.charger.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContextUtils {
    public static String getAPKSignature(String apkPath) {
        String signature = null;
        try {
            Class pkgParserCls = Class.forName("android.content.pm.PackageParser");
            Object pkgParser = pkgParserCls.getConstructor(new Class[]{String.class}).newInstance(new Object[]{apkPath});
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();
            Object pkgParserPkg = pkgParserCls.getDeclaredMethod("parsePackage", new Class[]{File.class, String.class, DisplayMetrics.class, Integer.TYPE}).invoke(pkgParser, new Object[]{new File(apkPath), apkPath, metrics, 64});
            pkgParserCls.getDeclaredMethod("collectCertificates", new Class[]{pkgParserPkg.getClass(), Integer.TYPE}).invoke(pkgParser, new Object[]{pkgParserPkg, 64});
            Signature[] info = (Signature[]) pkgParserPkg.getClass().getDeclaredField("mSignatures").get(pkgParserPkg);
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
        for (PackageInfo packageinfo : context.getPackageManager().getInstalledPackages(64)) {
            if (packageinfo.packageName.equals(context.getPackageName())) {
                signature = packageinfo.signatures[0].toCharsString();
            }
        }
        Log.d("ContextUtils.getAPPSignature", "signature[0]: " + signature);
        return signature;
    }

    public static boolean checkPermission(Context context, String permissionName) {
        try {
            if (context.getPackageManager().checkPermission(permissionName, context.getPackageName()) == 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e("ContextUtils.checkPermission", Log.getStackTraceString(e));
            return false;
        }
    }

    public static boolean hasActiveNetwork(Context context) {
        try {
            if (!checkPermission(context, "android.permission.ACCESS_NETWORK_STATE")) {
                return true;
            }
            NetworkInfo activeNetwork = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
            if (activeNetwork == null || !activeNetwork.isConnected()) {
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
            LinkProperties properties = ((ConnectivityManager) context.getSystemService("connectivity")).getLinkProperties(9);
            if (properties != null) {
                Matcher matcher = Pattern.compile("\\d+.\\d+.\\d+.\\d+").matcher(properties.getAddresses().toString());
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
            info = context.getPackageManager().getPackageArchiveInfo(apkPath, 1);
        } catch (Exception e) {
            Log.e("ContextUtils.verifyApkAvailability", Log.getStackTraceString(e));
        }
        if (info != null) {
            return true;
        }
        return false;
    }

    public static boolean getRawFileToContextPath(Context context, int id, String fileName) {
        InputStream is = null;
        FileOutputStream fos = null;
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
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e("ContextUtils.getRawFileToFile", Log.getStackTraceString(e));
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e2) {
                    Log.e("ContextUtils.getRawFileToFile", Log.getStackTraceString(e2));
                }
            }
            return true;
        } catch (IOException e3) {
            Log.e("ContextUtils.getRawFileToFile", Log.getStackTraceString(e3));
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e4) {
                    Log.e("ContextUtils.getRawFileToFile", Log.getStackTraceString(e4));
                }
            }
            if (fos == null) {
                return false;
            }
            try {
                fos.close();
                return false;
            } catch (IOException e5) {
                Log.e("ContextUtils.getRawFileToFile", Log.getStackTraceString(e5));
                return false;
            }
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e6) {
                    Log.e("ContextUtils.getRawFileToFile", Log.getStackTraceString(e6));
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e7) {
                    Log.e("ContextUtils.getRawFileToFile", Log.getStackTraceString(e7));
                }
            }
            throw th;
        }
    }
}
