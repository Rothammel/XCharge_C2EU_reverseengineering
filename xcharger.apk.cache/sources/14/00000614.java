package com.xcharge.charger.device.network;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.ethernet.EthernetDevInfo;
import android.net.ethernet.EthernetManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.support.v4.view.MotionEventCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.data.bean.device.Ethernet;
import com.xcharge.charger.data.bean.device.MobileNet;
import com.xcharge.charger.data.bean.device.Network;
import com.xcharge.charger.data.bean.device.Wifi;
import com.xcharge.charger.data.bean.setting.APNSetting;
import com.xcharge.charger.device.c2.bean.PortRuntimeData;
import com.xcharge.common.utils.ContextUtils;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.SystemPropertiesProxy;
import java.lang.reflect.Method;

/* loaded from: classes.dex */
public class NetworkProxy {
    protected Context context = null;
    private static final Uri APN_TABLE_URI = Uri.parse("content://telephony/carriers");
    private static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
    private static final Uri CURRENT_APN_URI = Uri.parse("content://telephony/carriers/current");
    private static final Uri PREFERAPN_NO_UPDATE_URI = Uri.parse("content://telephony/carriers/preferapn_no_update");
    private static NetworkProxy instance = null;

    public static NetworkProxy getInstance() {
        if (instance == null) {
            instance = new NetworkProxy();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
    }

    public void destroy() {
    }

    public Network getNetworkStatus() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.context.getSystemService("connectivity");
        Network network = new Network();
        network.setEthernet(getEthernetStatus());
        network.setMobile(getMobileNetStatus());
        network.setWifi(getWifiStatus());
        network.setPreference(getNetworkType(connectivityManager.getNetworkPreference(), false));
        return network;
    }

    public Ethernet getEthernetStatus() {
        EthernetManager ethernetManager = EthernetManager.getInstance();
        EthernetDevInfo ethernetInfo = ethernetManager.getSavedConfig();
        if (ethernetInfo != null) {
            Ethernet ethernet = new Ethernet();
            String ifName = ethernetInfo.getIfName();
            ethernet.setIfName(ifName);
            ethernet.setIp(SystemProperties.get("dhcp." + ifName + ".ipaddress"));
            ethernet.setMask(SystemProperties.get("dhcp." + ifName + ".mask"));
            ethernet.setGw(SystemProperties.get("dhcp." + ifName + ".gateway"));
            ethernet.setDns(SystemProperties.get("dhcp." + ifName + ".dns1"));
            ethernet.setMac(ethernetInfo.getHwaddr());
            return ethernet;
        }
        return new Ethernet();
    }

    public MobileNet getMobileNetStatus() {
        NetworkInfo networkInfo;
        TelephonyManager mobileManager = (TelephonyManager) this.context.getSystemService("phone");
        if (mobileManager == null) {
            return new MobileNet();
        }
        MobileNet mobileNet = new MobileNet();
        mobileNet.setPreferApn(getPreferApn());
        String simOperator = mobileManager.getSimOperator();
        if (!TextUtils.isEmpty(simOperator) && simOperator.length() > 3) {
            mobileNet.setSimMCC(simOperator.substring(0, 3));
            mobileNet.setSimMNC(simOperator.substring(3));
        }
        mobileNet.setType(getNetworkType(true));
        ConnectivityManager connectivityManager = (ConnectivityManager) this.context.getSystemService("connectivity");
        if (connectivityManager != null && (networkInfo = connectivityManager.getActiveNetworkInfo()) != null) {
            mobileNet.setSubtype(Integer.valueOf(networkInfo.getSubtype()));
            mobileNet.setSubtypeName(networkInfo.getSubtypeName());
        }
        mobileNet.setOprator(getNetworkOperator());
        mobileNet.setPLMN(mobileManager.getNetworkOperator());
        mobileNet.setICCID(mobileManager.getSimSerialNumber());
        mobileNet.setIMSI(mobileManager.getSubscriberId());
        mobileNet.setIMEI(mobileManager.getDeviceId());
        mobileNet.setMSISDN(mobileManager.getLine1Number());
        String basebandVersion = SystemPropertiesProxy.get(this.context, "gsm.version.baseband");
        mobileNet.setBasebandSV(basebandVersion);
        String[] ipInfo = ContextUtils.getWierlessIPInfo(this.context.getApplicationContext());
        if (ipInfo != null) {
            String ip = ipInfo[0];
            String ifName = ipInfo[1];
            mobileNet.setIfName(ifName);
            mobileNet.setIp(ip);
            String dns = SystemProperties.get("net.dns1");
            mobileNet.setDns(dns);
            return mobileNet;
        }
        return mobileNet;
    }

    public Wifi getWifiStatus() {
        WifiManager wifiManager = (WifiManager) this.context.getSystemService(Network.NETWORK_TYPE_WIFI);
        if (wifiManager == null) {
            return new Wifi();
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            Wifi wifi = new Wifi();
            wifi.setMac(wifiInfo.getMacAddress());
            wifi.setSsid(wifiInfo.getSSID());
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
            if (dhcpInfo != null) {
                wifi.setIp(getWifiAddress(dhcpInfo.ipAddress));
                wifi.setMask(getWifiAddress(dhcpInfo.netmask));
                wifi.setGw(getWifiAddress(dhcpInfo.gateway));
                wifi.setDns(getWifiAddress(dhcpInfo.dns1));
                return wifi;
            }
            return wifi;
        }
        return new Wifi();
    }

    public String getNetworkType(boolean needMobileDetail) {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.context.getSystemService("connectivity");
        if (connectivityManager == null) {
            return "none";
        }
        String strNetworkType = "none";
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == 1) {
                strNetworkType = Network.NETWORK_TYPE_WIFI;
            } else if (networkInfo.getType() == 0) {
                if (needMobileDetail) {
                    String _strSubTypeName = networkInfo.getSubtypeName();
                    Log.d("NetworkProxy.getNetworkType", "Network getSubtypeName : " + _strSubTypeName);
                    int networkType = networkInfo.getSubtype();
                    switch (networkType) {
                        case 1:
                        case 2:
                        case 4:
                        case 7:
                        case PortRuntimeData.STATUS_EX_11 /* 11 */:
                            strNetworkType = "2G";
                            break;
                        case 3:
                        case 5:
                        case 6:
                        case 8:
                        case 9:
                        case 10:
                        case PortRuntimeData.STATUS_EX_12 /* 12 */:
                        case 14:
                        case 15:
                            String basebandVersion = SystemPropertiesProxy.get(this.context, "gsm.version.baseband");
                            if ("0".equals(basebandVersion) || "rv2".equals(basebandVersion) || "EC122".equals(basebandVersion)) {
                                strNetworkType = "4G";
                                break;
                            } else {
                                strNetworkType = "3G";
                                break;
                            }
                            break;
                        case 13:
                            strNetworkType = "4G";
                            break;
                        default:
                            if (_strSubTypeName.equalsIgnoreCase(MobileNet.NET_TDSCDMA) || _strSubTypeName.equalsIgnoreCase(MobileNet.NET_WCDMA) || _strSubTypeName.equalsIgnoreCase(MobileNet.NET_CDMA2000)) {
                                strNetworkType = "3G";
                                break;
                            } else {
                                strNetworkType = _strSubTypeName;
                                LogUtils.applog("maybe unrecognized mobile network type: " + strNetworkType);
                                break;
                            }
                            break;
                    }
                    Log.d("NetworkProxy.getNetworkType", "Network getSubtype : " + Integer.valueOf(networkType).toString());
                } else {
                    strNetworkType = Network.NETWORK_TYPE_MOBILE;
                }
            } else if (networkInfo.getType() == 9) {
                strNetworkType = Network.NETWORK_TYPE_ETHERNET;
            } else {
                LogUtils.applog("unhandled network type: " + networkInfo.getType());
            }
        }
        Log.d("NetworkProxy.getNetworkType", "Network Type : " + strNetworkType);
        return strNetworkType;
    }

    public String getNetworkOperator() {
        TelephonyManager mobileManager = (TelephonyManager) this.context.getSystemService("phone");
        if (mobileManager == null) {
            return MobileNet.OPR_UNKNOWN;
        }
        String plmn = mobileManager.getNetworkOperator();
        if ("46000".equals(plmn) || "46002".equals(plmn) || MobileNet.PLMN_46004.equals(plmn) || "46007".equals(plmn)) {
            return MobileNet.OPR_CMCC;
        }
        if ("46001".equals(plmn) || "46006".equals(plmn)) {
            return MobileNet.OPR_CUCC;
        }
        if ("46003".equals(plmn) || MobileNet.PLMN_46005.equals(plmn) || "46011".equals(plmn)) {
            return MobileNet.OPR_CT;
        }
        String oprName = mobileManager.getNetworkOperatorName();
        if (TextUtils.isEmpty(oprName)) {
            return MobileNet.OPR_UNKNOWN;
        }
        return oprName;
    }

    public String getMobileNetworkType(int type) {
        int mobileClass = TelephonyManager.getNetworkClass(type);
        switch (mobileClass) {
            case 1:
                return "2G";
            case 2:
                String basebandVersion = SystemPropertiesProxy.get(this.context, "gsm.version.baseband");
                if ("0".equals(basebandVersion) || "rv2".equals(basebandVersion) || "EC122".equals(basebandVersion)) {
                    return "4G";
                }
                return "3G";
            case 3:
                return "4G";
            default:
                return Network.NETWORK_TYPE_MOBILE;
        }
    }

    public String getWifiAddress(int address) {
        if (address == 0) {
            return null;
        }
        String str = String.valueOf(address & MotionEventCompat.ACTION_MASK) + "." + ((address >> 8) & MotionEventCompat.ACTION_MASK) + "." + ((address >> 16) & MotionEventCompat.ACTION_MASK) + "." + ((address >> 24) & MotionEventCompat.ACTION_MASK);
        return str;
    }

    public String getNetworkType(int type, boolean needMobileDetail) {
        if (ConnectivityManager.isNetworkTypeMobile(type)) {
            if (needMobileDetail) {
                String sType = getNetworkType(needMobileDetail);
                return sType;
            }
            return Network.NETWORK_TYPE_MOBILE;
        } else if (ConnectivityManager.isNetworkTypeWifi(type)) {
            return Network.NETWORK_TYPE_WIFI;
        } else {
            if (9 != type) {
                return "none";
            }
            return Network.NETWORK_TYPE_ETHERNET;
        }
    }

    public boolean isNetworkConnected() {
        NetworkInfo ni;
        ConnectivityManager connectivityManager = (ConnectivityManager) this.context.getSystemService("connectivity");
        return (connectivityManager == null || (ni = connectivityManager.getActiveNetworkInfo()) == null || !ni.isConnected()) ? false : true;
    }

    public String getNetworkType() {
        NetworkInfo networkInfo;
        ConnectivityManager connectivityManager = (ConnectivityManager) this.context.getSystemService("connectivity");
        if (connectivityManager == null || (networkInfo = connectivityManager.getActiveNetworkInfo()) == null) {
            return "none";
        }
        networkInfo.getType();
        String type = getNetworkType(true);
        return type;
    }

    public int calcSignalLevel(int dbm) {
        String operator = getNetworkOperator();
        if (MobileNet.OPR_CT.equals(operator)) {
            if (dbm >= -91) {
                return 0;
            }
            if (dbm >= -101) {
                return 1;
            }
            if (dbm >= -103) {
                return 2;
            }
            if (dbm >= -107) {
                return 3;
            }
            if (dbm < -113) {
                return -1;
            }
            return 4;
        } else if (MobileNet.OPR_CUCC.equals(operator)) {
            if (dbm >= -91) {
                return 0;
            }
            if (dbm >= -101) {
                return 1;
            }
            if (dbm >= -103) {
                return 2;
            }
            if (dbm >= -107) {
                return 3;
            }
            if (dbm < -113) {
                return -1;
            }
            return 4;
        } else if (MobileNet.OPR_CMCC.equals(operator)) {
            int asu = (dbm + 113) / 2;
            if (asu <= 2 || asu == 99) {
                return -1;
            }
            if (asu >= 12) {
                return 0;
            }
            if (asu >= 10) {
                return 1;
            }
            if (asu >= 8) {
                return 2;
            }
            if (asu >= 5) {
                return 3;
            }
            return 4;
        } else {
            int asu2 = (dbm + 113) / 2;
            if (asu2 <= 2 || asu2 == 99) {
                return -1;
            }
            if (asu2 >= 12) {
                return 0;
            }
            if (asu2 >= 10) {
                return 1;
            }
            if (asu2 >= 8) {
                return 2;
            }
            if (asu2 >= 5) {
                return 3;
            }
            return 4;
        }
    }

    public String getSimState() {
        TelephonyManager mobileManager = (TelephonyManager) this.context.getSystemService("phone");
        int state = mobileManager.getSimState();
        switch (state) {
            case 1:
                return MobileNet.SIM_STATE_ABSENT;
            case 2:
                return MobileNet.SIM_STATE_NEED_PIN;
            case 3:
                return MobileNet.SIM_STATE_NEED_PUK;
            case 4:
                return "locked";
            case 5:
                return MobileNet.SIM_STATE_OK;
            default:
                return "unknown";
        }
    }

    public boolean isWifiConnected() {
        ConnectivityManager connManager = (ConnectivityManager) this.context.getSystemService("connectivity");
        NetworkInfo mWifi = connManager.getNetworkInfo(1);
        if (mWifi != null) {
            return mWifi.isConnected();
        }
        return false;
    }

    public boolean isMobileConnected() {
        ConnectivityManager connManager = (ConnectivityManager) this.context.getSystemService("connectivity");
        NetworkInfo mMobile = connManager.getNetworkInfo(0);
        if (mMobile != null) {
            return mMobile.isConnected();
        }
        return false;
    }

    public void toggleMobileData(boolean isEnable) {
        try {
            ConnectivityManager connManager = (ConnectivityManager) this.context.getSystemService("connectivity");
            Class<?> cmClass = connManager.getClass();
            Class[] argClasses = {Boolean.TYPE};
            Method method = cmClass.getMethod("setMobileDataEnabled", argClasses);
            method.invoke(connManager, Boolean.valueOf(isEnable));
        } catch (Exception e) {
            Log.e("NetworkProxy.toggleMobileData", Log.getStackTraceString(e));
        }
    }

    public boolean toggleWiFi(boolean enabled) {
        WifiManager wm = (WifiManager) this.context.getSystemService(Network.NETWORK_TYPE_WIFI);
        return wm.setWifiEnabled(enabled);
    }

    public boolean isAirplaneModeOn() {
        int modeIdx = Settings.System.getInt(this.context.getContentResolver(), "airplane_mode_on", 0);
        return modeIdx == 1;
    }

    public void toggleAirplaneMode(boolean setAirPlane) {
        Settings.System.putInt(this.context.getContentResolver(), "airplane_mode_on", setAirPlane ? 1 : 0);
        Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
        intent.putExtra("state", setAirPlane);
        this.context.sendBroadcast(intent);
    }

    public APNSetting getPreferApn() {
        APNSetting apn = null;
        Cursor cursor = null;
        try {
            try {
                ContentResolver resolver = this.context.getContentResolver();
                cursor = resolver.query(PREFERRED_APN_URI, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    APNSetting apn2 = new APNSetting();
                    try {
                        apn2.setCarrier(cursor.getString(cursor.getColumnIndex("name")));
                        apn2.setApn(cursor.getString(cursor.getColumnIndex("apn")));
                        apn2.setUser(cursor.getString(cursor.getColumnIndex("user")));
                        apn2.setPassword(cursor.getString(cursor.getColumnIndex("password")));
                        apn2.setMcc(cursor.getString(cursor.getColumnIndex("mcc")));
                        apn2.setMnc(cursor.getString(cursor.getColumnIndex("mnc")));
                        apn2.setType(cursor.getString(cursor.getColumnIndex("type")));
                        apn = apn2;
                    } catch (Exception e) {
                        e = e;
                        apn = apn2;
                        Log.e("NetworkProxy.getPreferApn", Log.getStackTraceString(e));
                        if (cursor != null) {
                            cursor.close();
                        }
                        return apn;
                    } catch (Throwable th) {
                        th = th;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e2) {
                e = e2;
            }
            return apn;
        } catch (Throwable th2) {
            th = th2;
        }
    }

    public boolean setPreferredApn(long id) {
        Uri uri;
        try {
            ContentResolver resolver = this.context.getContentResolver();
            resolver.delete(PREFERAPN_NO_UPDATE_URI, null, null);
            ContentValues values = new ContentValues();
            values.put("apn_id", Long.valueOf(id));
            uri = resolver.insert(PREFERAPN_NO_UPDATE_URI, values);
        } catch (Exception e) {
            Log.e("NetworkProxy.setPreferredApn", Log.getStackTraceString(e));
        }
        if (uri != null) {
            return true;
        }
        return false;
    }

    public APNSetting getCurrentApn() {
        APNSetting apn = null;
        Cursor cursor = null;
        try {
            try {
                ContentResolver resolver = this.context.getContentResolver();
                cursor = resolver.query(CURRENT_APN_URI, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    APNSetting apn2 = new APNSetting();
                    try {
                        apn2.setCarrier(cursor.getString(cursor.getColumnIndex("name")));
                        apn2.setApn(cursor.getString(cursor.getColumnIndex("apn")));
                        apn2.setUser(cursor.getString(cursor.getColumnIndex("user")));
                        apn2.setPassword(cursor.getString(cursor.getColumnIndex("password")));
                        apn2.setMcc(cursor.getString(cursor.getColumnIndex("mcc")));
                        apn2.setMnc(cursor.getString(cursor.getColumnIndex("mnc")));
                        apn2.setType(cursor.getString(cursor.getColumnIndex("type")));
                        apn = apn2;
                    } catch (Exception e) {
                        e = e;
                        apn = apn2;
                        Log.e("NetworkProxy.getCurrentApn", Log.getStackTraceString(e));
                        if (cursor != null) {
                            cursor.close();
                        }
                        return apn;
                    } catch (Throwable th) {
                        th = th;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e2) {
                e = e2;
            }
            return apn;
        } catch (Throwable th2) {
            th = th2;
        }
    }

    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:22:0x0183 -> B:12:0x00b8). Please submit an issue!!! */
    public Long setApn(APNSetting apn) {
        ContentResolver resolver = this.context.getContentResolver();
        Cursor cursor = null;
        try {
            try {
                String[] selectionArgs = {apn.getApn(), apn.getMcc(), apn.getMnc()};
                cursor = resolver.query(APN_TABLE_URI, null, "apn=? and mcc=? and mnc=?", selectionArgs, null);
                if (cursor == null || !cursor.moveToFirst()) {
                    ContentValues values = new ContentValues();
                    values.put("name", apn.getCarrier());
                    values.put("mcc", apn.getMcc());
                    values.put("mnc", apn.getMnc());
                    values.put("numeric", String.valueOf(apn.getMcc()) + apn.getMnc());
                    values.put("apn", apn.getApn());
                    values.put("user", apn.getUser());
                    values.put("password", apn.getPassword());
                    values.put("type", apn.getType());
                    Uri insertUri = resolver.insert(APN_TABLE_URI, values);
                    if (insertUri != null) {
                        long id = ContentUris.parseId(insertUri);
                        Log.d("NetworkProxy.setApn", "insert apn: " + apn.toJson() + ", id: " + id);
                        LogUtils.syslog("insert apn: " + apn.toJson() + ", id: " + id);
                        Long valueOf = Long.valueOf(id);
                        if (cursor != null) {
                            cursor.close();
                            return valueOf;
                        }
                        return valueOf;
                    }
                } else {
                    ContentValues values2 = new ContentValues();
                    values2.put("user", apn.getUser());
                    values2.put("password", apn.getPassword());
                    values2.put("type", apn.getType());
                    long id2 = Long.parseLong(cursor.getString(cursor.getColumnIndex("_id")));
                    Uri updateUri = ContentUris.withAppendedId(APN_TABLE_URI, id2);
                    int count = resolver.update(updateUri, values2, null, null);
                    if (count > 0) {
                        Log.d("NetworkProxy.setApn", "update apn: " + apn.toJson() + ", id: " + id2);
                        LogUtils.syslog("update apn: " + apn.toJson() + ", id: " + id2);
                        Long valueOf2 = Long.valueOf(id2);
                        if (cursor != null) {
                            cursor.close();
                            return valueOf2;
                        }
                        return valueOf2;
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                Log.e("NetworkProxy.setApn", Log.getStackTraceString(e));
                if (cursor != null) {
                    cursor.close();
                }
            }
            return null;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }
}