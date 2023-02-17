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
import android.support.p000v4.view.MotionEventCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.data.bean.device.Ethernet;
import com.xcharge.charger.data.bean.device.MobileNet;
import com.xcharge.charger.data.bean.device.Network;
import com.xcharge.charger.data.bean.device.Wifi;
import com.xcharge.charger.data.bean.setting.APNSetting;
import com.xcharge.charger.device.p005c2.bean.PortRuntimeData;
import com.xcharge.common.utils.ContextUtils;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.SystemPropertiesProxy;

public class NetworkProxy {
    private static final Uri APN_TABLE_URI = Uri.parse("content://telephony/carriers");
    private static final Uri CURRENT_APN_URI = Uri.parse("content://telephony/carriers/current");
    private static final Uri PREFERAPN_NO_UPDATE_URI = Uri.parse("content://telephony/carriers/preferapn_no_update");
    private static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
    private static NetworkProxy instance = null;
    protected Context context = null;

    public static NetworkProxy getInstance() {
        if (instance == null) {
            instance = new NetworkProxy();
        }
        return instance;
    }

    public void init(Context context2) {
        this.context = context2;
    }

    public void destroy() {
    }

    public Network getNetworkStatus() {
        Network network = new Network();
        network.setEthernet(getEthernetStatus());
        network.setMobile(getMobileNetStatus());
        network.setWifi(getWifiStatus());
        network.setPreference(getNetworkType(((ConnectivityManager) this.context.getSystemService("connectivity")).getNetworkPreference(), false));
        return network;
    }

    public Ethernet getEthernetStatus() {
        EthernetDevInfo ethernetInfo = EthernetManager.getInstance().getSavedConfig();
        if (ethernetInfo == null) {
            return new Ethernet();
        }
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
        if (!(connectivityManager == null || (networkInfo = connectivityManager.getActiveNetworkInfo()) == null)) {
            mobileNet.setSubtype(Integer.valueOf(networkInfo.getSubtype()));
            mobileNet.setSubtypeName(networkInfo.getSubtypeName());
        }
        mobileNet.setOprator(getNetworkOperator());
        mobileNet.setPLMN(mobileManager.getNetworkOperator());
        mobileNet.setICCID(mobileManager.getSimSerialNumber());
        mobileNet.setIMSI(mobileManager.getSubscriberId());
        mobileNet.setIMEI(mobileManager.getDeviceId());
        mobileNet.setMSISDN(mobileManager.getLine1Number());
        mobileNet.setBasebandSV(SystemPropertiesProxy.get(this.context, "gsm.version.baseband"));
        String[] ipInfo = ContextUtils.getWierlessIPInfo(this.context.getApplicationContext());
        if (ipInfo == null) {
            return mobileNet;
        }
        String ip = ipInfo[0];
        mobileNet.setIfName(ipInfo[1]);
        mobileNet.setIp(ip);
        mobileNet.setDns(SystemProperties.get("net.dns1"));
        return mobileNet;
    }

    public Wifi getWifiStatus() {
        WifiManager wifiManager = (WifiManager) this.context.getSystemService(Network.NETWORK_TYPE_WIFI);
        if (wifiManager == null) {
            return new Wifi();
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            return new Wifi();
        }
        Wifi wifi = new Wifi();
        wifi.setMac(wifiInfo.getMacAddress());
        wifi.setSsid(wifiInfo.getSSID());
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        if (dhcpInfo == null) {
            return wifi;
        }
        wifi.setIp(getWifiAddress(dhcpInfo.ipAddress));
        wifi.setMask(getWifiAddress(dhcpInfo.netmask));
        wifi.setGw(getWifiAddress(dhcpInfo.gateway));
        wifi.setDns(getWifiAddress(dhcpInfo.dns1));
        return wifi;
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
                        case PortRuntimeData.STATUS_EX_11:
                            strNetworkType = "2G";
                            break;
                        case 3:
                        case 5:
                        case 6:
                        case 8:
                        case 9:
                        case 10:
                        case PortRuntimeData.STATUS_EX_12:
                        case 14:
                        case 15:
                            String basebandVersion = SystemPropertiesProxy.get(this.context, "gsm.version.baseband");
                            if (!"0".equals(basebandVersion) && !"rv2".equals(basebandVersion) && !"EC122".equals(basebandVersion)) {
                                strNetworkType = "3G";
                                break;
                            } else {
                                strNetworkType = "4G";
                                break;
                            }
                            break;
                        case 13:
                            strNetworkType = "4G";
                            break;
                        default:
                            if (!_strSubTypeName.equalsIgnoreCase(MobileNet.NET_TDSCDMA) && !_strSubTypeName.equalsIgnoreCase(MobileNet.NET_WCDMA) && !_strSubTypeName.equalsIgnoreCase(MobileNet.NET_CDMA2000)) {
                                strNetworkType = _strSubTypeName;
                                LogUtils.applog("maybe unrecognized mobile network type: " + strNetworkType);
                                break;
                            } else {
                                strNetworkType = "3G";
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
        if (!TextUtils.isEmpty(oprName)) {
            return oprName;
        }
        return MobileNet.OPR_UNKNOWN;
    }

    public String getMobileNetworkType(int type) {
        switch (TelephonyManager.getNetworkClass(type)) {
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
        if (address != 0) {
            return String.valueOf(address & MotionEventCompat.ACTION_MASK) + "." + ((address >> 8) & MotionEventCompat.ACTION_MASK) + "." + ((address >> 16) & MotionEventCompat.ACTION_MASK) + "." + ((address >> 24) & MotionEventCompat.ACTION_MASK);
        }
        return null;
    }

    public String getNetworkType(int type, boolean needMobileDetail) {
        if (ConnectivityManager.isNetworkTypeMobile(type)) {
            if (needMobileDetail) {
                return getNetworkType(needMobileDetail);
            }
            return Network.NETWORK_TYPE_MOBILE;
        } else if (ConnectivityManager.isNetworkTypeWifi(type)) {
            return Network.NETWORK_TYPE_WIFI;
        } else {
            if (9 == type) {
                return Network.NETWORK_TYPE_ETHERNET;
            }
            return "none";
        }
    }

    public boolean isNetworkConnected() {
        NetworkInfo ni;
        ConnectivityManager connectivityManager = (ConnectivityManager) this.context.getSystemService("connectivity");
        if (connectivityManager == null || (ni = connectivityManager.getActiveNetworkInfo()) == null || !ni.isConnected()) {
            return false;
        }
        return true;
    }

    public String getNetworkType() {
        NetworkInfo networkInfo;
        ConnectivityManager connectivityManager = (ConnectivityManager) this.context.getSystemService("connectivity");
        if (connectivityManager == null || (networkInfo = connectivityManager.getActiveNetworkInfo()) == null) {
            return "none";
        }
        int type = networkInfo.getType();
        return getNetworkType(true);
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
            if (dbm >= -113) {
                return 4;
            }
            return -1;
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
            if (dbm >= -113) {
                return 4;
            }
            return -1;
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
        switch (((TelephonyManager) this.context.getSystemService("phone")).getSimState()) {
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
        NetworkInfo mWifi = ((ConnectivityManager) this.context.getSystemService("connectivity")).getNetworkInfo(1);
        if (mWifi != null) {
            return mWifi.isConnected();
        }
        return false;
    }

    public boolean isMobileConnected() {
        NetworkInfo mMobile = ((ConnectivityManager) this.context.getSystemService("connectivity")).getNetworkInfo(0);
        if (mMobile != null) {
            return mMobile.isConnected();
        }
        return false;
    }

    public void toggleMobileData(boolean isEnable) {
        try {
            ConnectivityManager connManager = (ConnectivityManager) this.context.getSystemService("connectivity");
            connManager.getClass().getMethod("setMobileDataEnabled", new Class[]{Boolean.TYPE}).invoke(connManager, new Object[]{Boolean.valueOf(isEnable)});
        } catch (Exception e) {
            Log.e("NetworkProxy.toggleMobileData", Log.getStackTraceString(e));
        }
    }

    public boolean toggleWiFi(boolean enabled) {
        return ((WifiManager) this.context.getSystemService(Network.NETWORK_TYPE_WIFI)).setWifiEnabled(enabled);
    }

    public boolean isAirplaneModeOn() {
        if (Settings.System.getInt(this.context.getContentResolver(), "airplane_mode_on", 0) == 1) {
            return true;
        }
        return false;
    }

    public void toggleAirplaneMode(boolean setAirPlane) {
        Settings.System.putInt(this.context.getContentResolver(), "airplane_mode_on", setAirPlane ? 1 : 0);
        Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
        intent.putExtra("state", setAirPlane);
        this.context.sendBroadcast(intent);
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x008d  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0094  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.xcharge.charger.data.bean.setting.APNSetting getPreferApn() {
        /*
            r10 = this;
            r6 = 0
            r8 = 0
            android.content.Context r1 = r10.context     // Catch:{ Exception -> 0x0081 }
            android.content.ContentResolver r0 = r1.getContentResolver()     // Catch:{ Exception -> 0x0081 }
            android.net.Uri r1 = PREFERRED_APN_URI     // Catch:{ Exception -> 0x0081 }
            r2 = 0
            r3 = 0
            r4 = 0
            r5 = 0
            android.database.Cursor r8 = r0.query(r1, r2, r3, r4, r5)     // Catch:{ Exception -> 0x0081 }
            if (r8 == 0) goto L_0x007b
            boolean r1 = r8.moveToFirst()     // Catch:{ Exception -> 0x0081 }
            if (r1 == 0) goto L_0x007b
            com.xcharge.charger.data.bean.setting.APNSetting r7 = new com.xcharge.charger.data.bean.setting.APNSetting     // Catch:{ Exception -> 0x0081 }
            r7.<init>()     // Catch:{ Exception -> 0x0081 }
            java.lang.String r1 = "name"
            int r1 = r8.getColumnIndex(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = r8.getString(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            r7.setCarrier(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = "apn"
            int r1 = r8.getColumnIndex(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = r8.getString(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            r7.setApn(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = "user"
            int r1 = r8.getColumnIndex(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = r8.getString(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            r7.setUser(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = "password"
            int r1 = r8.getColumnIndex(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = r8.getString(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            r7.setPassword(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = "mcc"
            int r1 = r8.getColumnIndex(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = r8.getString(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            r7.setMcc(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = "mnc"
            int r1 = r8.getColumnIndex(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = r8.getString(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            r7.setMnc(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = "type"
            int r1 = r8.getColumnIndex(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = r8.getString(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            r7.setType(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            r6 = r7
        L_0x007b:
            if (r8 == 0) goto L_0x0080
            r8.close()
        L_0x0080:
            return r6
        L_0x0081:
            r9 = move-exception
        L_0x0082:
            java.lang.String r1 = "NetworkProxy.getPreferApn"
            java.lang.String r2 = android.util.Log.getStackTraceString(r9)     // Catch:{ all -> 0x0091 }
            android.util.Log.e(r1, r2)     // Catch:{ all -> 0x0091 }
            if (r8 == 0) goto L_0x0080
            r8.close()
            goto L_0x0080
        L_0x0091:
            r1 = move-exception
        L_0x0092:
            if (r8 == 0) goto L_0x0097
            r8.close()
        L_0x0097:
            throw r1
        L_0x0098:
            r1 = move-exception
            r6 = r7
            goto L_0x0092
        L_0x009b:
            r9 = move-exception
            r6 = r7
            goto L_0x0082
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.device.network.NetworkProxy.getPreferApn():com.xcharge.charger.data.bean.setting.APNSetting");
    }

    public boolean setPreferredApn(long id) {
        try {
            ContentResolver resolver = this.context.getContentResolver();
            resolver.delete(PREFERAPN_NO_UPDATE_URI, (String) null, (String[]) null);
            ContentValues values = new ContentValues();
            values.put("apn_id", Long.valueOf(id));
            if (resolver.insert(PREFERAPN_NO_UPDATE_URI, values) != null) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e("NetworkProxy.setPreferredApn", Log.getStackTraceString(e));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x008d  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0094  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.xcharge.charger.data.bean.setting.APNSetting getCurrentApn() {
        /*
            r10 = this;
            r6 = 0
            r8 = 0
            android.content.Context r1 = r10.context     // Catch:{ Exception -> 0x0081 }
            android.content.ContentResolver r0 = r1.getContentResolver()     // Catch:{ Exception -> 0x0081 }
            android.net.Uri r1 = CURRENT_APN_URI     // Catch:{ Exception -> 0x0081 }
            r2 = 0
            r3 = 0
            r4 = 0
            r5 = 0
            android.database.Cursor r8 = r0.query(r1, r2, r3, r4, r5)     // Catch:{ Exception -> 0x0081 }
            if (r8 == 0) goto L_0x007b
            boolean r1 = r8.moveToFirst()     // Catch:{ Exception -> 0x0081 }
            if (r1 == 0) goto L_0x007b
            com.xcharge.charger.data.bean.setting.APNSetting r7 = new com.xcharge.charger.data.bean.setting.APNSetting     // Catch:{ Exception -> 0x0081 }
            r7.<init>()     // Catch:{ Exception -> 0x0081 }
            java.lang.String r1 = "name"
            int r1 = r8.getColumnIndex(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = r8.getString(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            r7.setCarrier(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = "apn"
            int r1 = r8.getColumnIndex(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = r8.getString(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            r7.setApn(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = "user"
            int r1 = r8.getColumnIndex(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = r8.getString(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            r7.setUser(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = "password"
            int r1 = r8.getColumnIndex(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = r8.getString(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            r7.setPassword(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = "mcc"
            int r1 = r8.getColumnIndex(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = r8.getString(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            r7.setMcc(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = "mnc"
            int r1 = r8.getColumnIndex(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = r8.getString(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            r7.setMnc(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = "type"
            int r1 = r8.getColumnIndex(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            java.lang.String r1 = r8.getString(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            r7.setType(r1)     // Catch:{ Exception -> 0x009b, all -> 0x0098 }
            r6 = r7
        L_0x007b:
            if (r8 == 0) goto L_0x0080
            r8.close()
        L_0x0080:
            return r6
        L_0x0081:
            r9 = move-exception
        L_0x0082:
            java.lang.String r1 = "NetworkProxy.getCurrentApn"
            java.lang.String r2 = android.util.Log.getStackTraceString(r9)     // Catch:{ all -> 0x0091 }
            android.util.Log.e(r1, r2)     // Catch:{ all -> 0x0091 }
            if (r8 == 0) goto L_0x0080
            r8.close()
            goto L_0x0080
        L_0x0091:
            r1 = move-exception
        L_0x0092:
            if (r8 == 0) goto L_0x0097
            r8.close()
        L_0x0097:
            throw r1
        L_0x0098:
            r1 = move-exception
            r6 = r7
            goto L_0x0092
        L_0x009b:
            r9 = move-exception
            r6 = r7
            goto L_0x0082
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.device.network.NetworkProxy.getCurrentApn():com.xcharge.charger.data.bean.setting.APNSetting");
    }

    public Long setApn(APNSetting apn) {
        ContentResolver resolver = this.context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(APN_TABLE_URI, (String[]) null, "apn=? and mcc=? and mnc=?", new String[]{apn.getApn(), apn.getMcc(), apn.getMnc()}, (String) null);
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
                    if (cursor == null) {
                        return valueOf;
                    }
                    cursor.close();
                    return valueOf;
                }
            } else {
                ContentValues values2 = new ContentValues();
                values2.put("user", apn.getUser());
                values2.put("password", apn.getPassword());
                values2.put("type", apn.getType());
                long id2 = Long.parseLong(cursor.getString(cursor.getColumnIndex("_id")));
                if (resolver.update(ContentUris.withAppendedId(APN_TABLE_URI, id2), values2, (String) null, (String[]) null) > 0) {
                    Log.d("NetworkProxy.setApn", "update apn: " + apn.toJson() + ", id: " + id2);
                    LogUtils.syslog("update apn: " + apn.toJson() + ", id: " + id2);
                    Long valueOf2 = Long.valueOf(id2);
                    if (cursor == null) {
                        return valueOf2;
                    }
                    cursor.close();
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
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
        return null;
    }
}
