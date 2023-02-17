package com.xcharge.charger.data.bean.device;

import com.xcharge.common.bean.JsonBean;

public class Network extends JsonBean<Network> {
    public static String ACTION_CONNECTION_CHANGED = "com.xcharge.charger.data.bean.status.ACTION_CONNECTION_CHANGED";
    public static String ACTION_NETWORK_CHANGED = "com.xcharge.charger.data.bean.status.ACTION_NETWORK_CHANGED";
    public static final String NETWORK_TYPE_ETHERNET = "ethernet";
    public static final String NETWORK_TYPE_MOBILE = "mobile";
    public static final String NETWORK_TYPE_MOBILE_2G = "2G";
    public static final String NETWORK_TYPE_MOBILE_3G = "3G";
    public static final String NETWORK_TYPE_MOBILE_4G = "4G";
    public static final String NETWORK_TYPE_NONE = "none";
    public static final String NETWORK_TYPE_WIFI = "wifi";
    public static final String NETWORK_TYPE_ZIGBEE = "zigbee";
    private String Preference = "none";
    private String active = "none";
    private boolean connected = false;
    private Ethernet ethernet = null;
    private MobileNet mobile = null;
    private Wifi wifi = null;
    private ZigBee zigbee = null;

    public Ethernet getEthernet() {
        return this.ethernet;
    }

    public void setEthernet(Ethernet ethernet2) {
        this.ethernet = ethernet2;
    }

    public MobileNet getMobile() {
        return this.mobile;
    }

    public void setMobile(MobileNet mobile2) {
        this.mobile = mobile2;
    }

    public Wifi getWifi() {
        return this.wifi;
    }

    public void setWifi(Wifi wifi2) {
        this.wifi = wifi2;
    }

    public ZigBee getZigbee() {
        return this.zigbee;
    }

    public void setZigbee(ZigBee zigbee2) {
        this.zigbee = zigbee2;
    }

    public String getPreference() {
        return this.Preference;
    }

    public void setPreference(String preference) {
        this.Preference = preference;
    }

    public String getActive() {
        return this.active;
    }

    public void setActive(String active2) {
        this.active = active2;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void setConnected(boolean connected2) {
        this.connected = connected2;
    }
}
