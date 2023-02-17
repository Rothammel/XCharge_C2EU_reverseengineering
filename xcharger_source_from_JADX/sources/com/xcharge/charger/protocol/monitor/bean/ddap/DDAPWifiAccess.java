package com.xcharge.charger.protocol.monitor.bean.ddap;

import com.xcharge.common.bean.JsonBean;

public class DDAPWifiAccess extends JsonBean<DDAPWifiAccess> {
    private String dns = null;

    /* renamed from: gw */
    private String f107gw = null;

    /* renamed from: ip */
    private String f108ip = null;
    private String mac = null;
    private String mask = null;
    private String ssid = null;

    public String getSsid() {
        return this.ssid;
    }

    public void setSsid(String ssid2) {
        this.ssid = ssid2;
    }

    public String getIp() {
        return this.f108ip;
    }

    public void setIp(String ip) {
        this.f108ip = ip;
    }

    public String getMask() {
        return this.mask;
    }

    public void setMask(String mask2) {
        this.mask = mask2;
    }

    public String getGw() {
        return this.f107gw;
    }

    public void setGw(String gw) {
        this.f107gw = gw;
    }

    public String getDns() {
        return this.dns;
    }

    public void setDns(String dns2) {
        this.dns = dns2;
    }

    public String getMac() {
        return this.mac;
    }

    public void setMac(String mac2) {
        this.mac = mac2;
    }
}
