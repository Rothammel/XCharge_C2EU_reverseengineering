package com.xcharge.charger.protocol.monitor.bean.ddap;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class DDAPWifiAccess extends JsonBean<DDAPWifiAccess> {
    private String ssid = null;
    private String ip = null;
    private String mask = null;
    private String gw = null;
    private String dns = null;
    private String mac = null;

    public String getSsid() {
        return this.ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMask() {
        return this.mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public String getGw() {
        return this.gw;
    }

    public void setGw(String gw) {
        this.gw = gw;
    }

    public String getDns() {
        return this.dns;
    }

    public void setDns(String dns) {
        this.dns = dns;
    }

    public String getMac() {
        return this.mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
