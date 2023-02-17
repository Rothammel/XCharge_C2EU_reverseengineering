package com.xcharge.common.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import com.xcharge.charger.data.bean.device.Network;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.CharEncoding;
import org.apache.http.util.EncodingUtils;

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
        Iterator<PackageInfo> iter = apps.iterator();
        while (true) {
            if (!iter.hasNext()) {
                break;
            }
            PackageInfo packageinfo = iter.next();
            String packageName = packageinfo.packageName;
            if (packageName.equals(context.getPackageName())) {
                signature = packageinfo.signatures[0].toCharsString();
                break;
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
        } catch (Exception e) {
            Log.e("ContextUtils.hasActiveNetwork", Log.getStackTraceString(e));
        }
        if (checkPermission(context, "android.permission.ACCESS_NETWORK_STATE")) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
            if (connectivityManager != null) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                if (activeNetwork != null) {
                    if (activeNetwork.isConnected()) {
                        return true;
                    }
                }
                return false;
            }
            return false;
        }
        return true;
    }

    public static boolean isNetworkConnected(Context context) {
        return hasActiveNetwork(context);
    }

    public static String getIpAddressByInterface(String ifName) {
        try {
            NetworkInterface ni = NetworkInterface.getByName(ifName);
            if (ni != null) {
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    InetAddress ia = ias.nextElement();
                    if ((ia instanceof Inet4Address) && !ia.isLoopbackAddress()) {
                        return ia.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception e) {
            Log.e("ContextUtils.getIpAddressByInterface", Log.getStackTraceString(e));
        }
        return null;
    }

    public static String[] getWierlessIPInfo(Context context) {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == 0) {
                try {
                    Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                    while (en.hasMoreElements()) {
                        NetworkInterface intf = en.nextElement();
                        Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                        while (enumIpAddr.hasMoreElements()) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                                return new String[]{inetAddress.getHostAddress(), intf.getName()};
                            }
                        }
                    }
                } catch (SocketException e) {
                    Log.e("ContextUtils.getWierlessIPInfo", Log.getStackTraceString(e));
                }
            } else if (info.getType() == 1) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Network.NETWORK_TYPE_WIFI);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());
                return new String[]{ipAddress, wifiInfo.getSSID()};
            }
        }
        return null;
    }

    public static String intIP2StringIP(int ip) {
        return String.valueOf(ip & MotionEventCompat.ACTION_MASK) + "." + ((ip >> 8) & MotionEventCompat.ACTION_MASK) + "." + ((ip >> 16) & MotionEventCompat.ACTION_MASK) + "." + ((ip >> 24) & MotionEventCompat.ACTION_MASK);
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

    public static String getRawFileToString(Context context, int id) {
        InputStream is = null;
        String str = null;
        try {
            try {
                is = context.getResources().openRawResource(id);
                byte[] buffer = new byte[512];
                StringBuffer stringBuffer = new StringBuffer();
                while (true) {
                    int count = is.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    stringBuffer.append(new String(buffer, 0, count));
                }
                str = stringBuffer.toString();
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        Log.e("ContextUtils.getRawFileToString", Log.getStackTraceString(e));
                    }
                }
            } catch (IOException e2) {
                Log.e("ContextUtils.getRawFileToString", Log.getStackTraceString(e2));
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e3) {
                        Log.e("ContextUtils.getRawFileToString", Log.getStackTraceString(e3));
                    }
                }
            }
            return str;
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e4) {
                    Log.e("ContextUtils.getRawFileToString", Log.getStackTraceString(e4));
                }
            }
            throw th;
        }
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

    public static boolean writeFileData(String fileName, String message, Context context) {
        FileOutputStream fout = null;
        try {
            try {
                fout = context.openFileOutput(fileName, 0);
                byte[] bytes = message.getBytes();
                fout.write(bytes);
                return true;
            } finally {
                if (fout != null) {
                    try {
                        fout.close();
                    } catch (IOException e) {
                        Log.e("ContextUtils.writeFileData", Log.getStackTraceString(e));
                    }
                }
            }
        } catch (Exception e2) {
            Log.e("ContextUtils.writeFileData", Log.getStackTraceString(e2));
            if (fout != null) {
                try {
                    fout.close();
                    return false;
                } catch (IOException e3) {
                    Log.e("ContextUtils.writeFileData", Log.getStackTraceString(e3));
                    return false;
                }
            }
            return false;
        }
    }

    public static String readFileData(String fileName, Context context) {
        String res = null;
        FileInputStream fin = null;
        try {
            try {
                fin = context.openFileInput(fileName);
                int length = fin.available();
                byte[] buffer = new byte[length];
                fin.read(buffer);
                res = EncodingUtils.getString(buffer, CharEncoding.UTF_8);
                if (fin != null) {
                    try {
                        fin.close();
                    } catch (IOException e) {
                        Log.e("ContextUtils.readFileData", Log.getStackTraceString(e));
                    }
                }
            } catch (Exception e2) {
                Log.e("ContextUtils.readFileData", Log.getStackTraceString(e2));
                if (fin != null) {
                    try {
                        fin.close();
                    } catch (IOException e3) {
                        Log.e("ContextUtils.readFileData", Log.getStackTraceString(e3));
                    }
                }
            }
            return res;
        } catch (Throwable th) {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e4) {
                    Log.e("ContextUtils.readFileData", Log.getStackTraceString(e4));
                }
            }
            throw th;
        }
    }

    public static boolean isExistFile(String fileName, Context context) {
        FileInputStream fin = null;
        try {
            try {
                FileInputStream fin2 = context.openFileInput(fileName);
                if (fin2 != null) {
                    try {
                        fin2.close();
                    } catch (IOException e) {
                        Log.e("ContextUtils.isExistFile", Log.getStackTraceString(e));
                    }
                }
                return true;
            } catch (Exception e2) {
                Log.e("ContextUtils.isExistFile", Log.getStackTraceString(e2));
                if (0 != 0) {
                    try {
                        fin.close();
                    } catch (IOException e3) {
                        Log.e("ContextUtils.isExistFile", Log.getStackTraceString(e3));
                    }
                }
                return false;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fin.close();
                } catch (IOException e4) {
                    Log.e("ContextUtils.isExistFile", Log.getStackTraceString(e4));
                }
            }
            throw th;
        }
    }

    public static boolean readAssetsFileTo(Context context, String from, String to) {
        boolean isOk = false;
        try {
            AssetManager am = context.getAssets();
            InputStream is = am.open(from);
            FileOutputStream fos = new FileOutputStream(new File(to));
            byte[] buffer = new byte[1024];
            while (true) {
                int read = is.read(buffer);
                if (read == -1) {
                    break;
                }
                fos.write(buffer, 0, read);
            }
            fos.flush();
            isOk = true;
            fos.close();
            is.close();
        } catch (IOException e) {
            Log.e("ContextUtils.readAssetsFileTo", Log.getStackTraceString(e));
        }
        return isOk;
    }

    public static void setMobileDataStatus(Context context, boolean enabled) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService("connectivity");
        try {
            Class<?> conMgrClass = Class.forName(conMgr.getClass().getName());
            Field iConMgrField = conMgrClass.getDeclaredField("mService");
            iConMgrField.setAccessible(true);
            Object iConMgr = iConMgrField.get(conMgr);
            Class<?> iConMgrClass = Class.forName(iConMgr.getClass().getName());
            Method setMobileDataEnabledMethod = iConMgrClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);
            setMobileDataEnabledMethod.invoke(iConMgr, Boolean.valueOf(enabled));
        } catch (Exception e) {
            Log.e("ContextUtils.setMobileDataStatus", Log.getStackTraceString(e));
        }
    }
}
