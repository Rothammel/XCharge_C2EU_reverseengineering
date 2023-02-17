package com.xcharge.common.utils;

import android.text.TextUtils;
import android.util.Log;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.apache.commons.lang3.StringUtils;

/* loaded from: classes.dex */
public class NetworkUtils {
    public static boolean ping2ip(String ip, int timeout) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address.isReachable(timeout * 1000);
        } catch (Exception e) {
            Log.e("NetworkUtils.ping2ip", Log.getStackTraceString(e));
            return false;
        }
    }

    public static Object[] ping2dn(String dn, int timeout) {
        try {
            String ip = parseDN(dn);
            boolean isReachable = false;
            if (!TextUtils.isEmpty(ip)) {
                isReachable = ping2ip(ip, timeout);
            }
            return new Object[]{Boolean.valueOf(isReachable), ip};
        } catch (Exception e) {
            Log.e("NetworkUtils.ping2dn", Log.getStackTraceString(e));
            Object[] objArr = new Object[2];
            objArr[0] = false;
            return objArr;
        }
    }

    public static boolean checkServer(String host, int port, int timeout) {
        boolean isOk = false;
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(InetAddress.getByName(host), port), timeout * 1000);
            isOk = true;
            socket.close();
            return true;
        } catch (Exception e) {
            Log.e("NetworkUtils.checkServer", Log.getStackTraceString(e));
            return isOk;
        }
    }

    public static String parseDN(String dn) {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(dn);
            if (addresses != null && addresses.length > 0) {
                InetAddress address = addresses[0];
                return address.getHostAddress();
            }
        } catch (Exception e) {
            Log.e("NetworkUtils.parseDNS", Log.getStackTraceString(e));
        }
        return null;
    }

    public static boolean ping(String peer, int timeout) {
        String forlog;
        try {
            String cmdline = "ping -c 4 " + peer;
            Object[] ret = FileUtils.execShellAndOutput(cmdline, timeout);
            if (ret == null) {
                return false;
            }
            int status = ((Integer) ret[0]).intValue();
            String output = (String) ret[1];
            if (status == 0) {
                if (!TextUtils.isEmpty(output)) {
                    String[] splits = output.split(StringUtils.LF);
                    if (splits.length >= 3) {
                        String forlog2 = String.valueOf(cmdline) + " [ " + splits[0];
                        if (splits[splits.length - 2].contains("packets transmitted")) {
                            forlog = String.valueOf(forlog2) + StringUtils.SPACE + splits[splits.length - 2] + " ]";
                        } else {
                            forlog = String.valueOf(forlog2) + " ]";
                        }
                        LogUtils.applog(forlog);
                    }
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e("NetworkUtils.ping", Log.getStackTraceString(e));
            return false;
        }
    }
}