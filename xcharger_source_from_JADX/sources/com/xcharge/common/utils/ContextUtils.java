package com.xcharge.common.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.p000v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import com.xcharge.charger.data.bean.device.Network;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Iterator;
import org.apache.commons.lang3.CharEncoding;
import org.apache.http.util.EncodingUtils;

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
        Iterator<PackageInfo> iter = context.getPackageManager().getInstalledPackages(64).iterator();
        while (true) {
            if (iter.hasNext()) {
                PackageInfo packageinfo = iter.next();
                if (packageinfo.packageName.equals(context.getPackageName())) {
                    signature = packageinfo.signatures[0].toCharsString();
                    break;
                }
            } else {
                break;
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
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
            if (connectivityManager != null) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                if (activeNetwork == null || !activeNetwork.isConnected()) {
                    return false;
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e("ContextUtils.hasActiveNetwork", Log.getStackTraceString(e));
        }
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
                        while (true) {
                            if (enumIpAddr.hasMoreElements()) {
                                InetAddress inetAddress = enumIpAddr.nextElement();
                                if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                                    return new String[]{inetAddress.getHostAddress(), intf.getName()};
                                }
                            }
                        }
                    }
                } catch (SocketException e) {
                    Log.e("ContextUtils.getWierlessIPInfo", Log.getStackTraceString(e));
                }
            } else if (info.getType() == 1) {
                WifiInfo wifiInfo = ((WifiManager) context.getSystemService(Network.NETWORK_TYPE_WIFI)).getConnectionInfo();
                return new String[]{intIP2StringIP(wifiInfo.getIpAddress()), wifiInfo.getSSID()};
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
            info = context.getPackageManager().getPackageArchiveInfo(apkPath, 1);
        } catch (Exception e) {
            Log.e("ContextUtils.verifyApkAvailability", Log.getStackTraceString(e));
        }
        if (info != null) {
            return true;
        }
        return false;
    }

    public static String getRawFileToString(Context context, int id) {
        InputStream is = null;
        String str = null;
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
        return str;
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

    public static boolean writeFileData(String fileName, String message, Context context) {
        FileOutputStream fout = null;
        try {
            fout = context.openFileOutput(fileName, 0);
            fout.write(message.getBytes());
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    Log.e("ContextUtils.writeFileData", Log.getStackTraceString(e));
                }
            }
            return true;
        } catch (Exception e2) {
            Log.e("ContextUtils.writeFileData", Log.getStackTraceString(e2));
            if (fout == null) {
                return false;
            }
            try {
                fout.close();
                return false;
            } catch (IOException e3) {
                Log.e("ContextUtils.writeFileData", Log.getStackTraceString(e3));
                return false;
            }
        } catch (Throwable th) {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e4) {
                    Log.e("ContextUtils.writeFileData", Log.getStackTraceString(e4));
                }
            }
            throw th;
        }
    }

    public static String readFileData(String fileName, Context context) {
        String res = null;
        FileInputStream fin = null;
        try {
            fin = context.openFileInput(fileName);
            byte[] buffer = new byte[fin.available()];
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
        return res;
    }

    public static boolean isExistFile(String fileName, Context context) {
        FileInputStream fin = null;
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
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e3) {
                    Log.e("ContextUtils.isExistFile", Log.getStackTraceString(e3));
                }
            }
            return false;
        } catch (Throwable th) {
            if (fin != null) {
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
        InputStream is;
        FileOutputStream fos;
        boolean isOk = false;
        try {
            is = context.getAssets().open(from);
            fos = new FileOutputStream(new File(to));
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
        } catch (Throwable th) {
            fos.close();
            is.close();
            throw th;
        }
        return isOk;
    }

    public static void setMobileDataStatus(Context context, boolean enabled) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService("connectivity");
        try {
            Field iConMgrField = Class.forName(conMgr.getClass().getName()).getDeclaredField("mService");
            iConMgrField.setAccessible(true);
            Object iConMgr = iConMgrField.get(conMgr);
            Method setMobileDataEnabledMethod = Class.forName(iConMgr.getClass().getName()).getDeclaredMethod("setMobileDataEnabled", new Class[]{Boolean.TYPE});
            setMobileDataEnabledMethod.setAccessible(true);
            setMobileDataEnabledMethod.invoke(iConMgr, new Object[]{Boolean.valueOf(enabled)});
        } catch (Exception e) {
            Log.e("ContextUtils.setMobileDataStatus", Log.getStackTraceString(e));
        }
    }
}
