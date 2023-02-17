package com.xcharge.charger.protocol.monitor.bean.ddap;

import com.xcharge.common.bean.JsonBean;

public class DDAPEthernetAccess extends JsonBean<DDAPEthernetAccess> {
    private String dns = null;

    /* renamed from: gw */
    private String f101gw = null;

    /* renamed from: ip */
    private String f102ip = null;
    private String mac = null;
    private String mask = null;

    public String getIp() {
        return this.f102ip;
    }

    public void setIp(String ip) {
        this.f102ip = ip;
    }

    public String getMask() {
        return this.mask;
    }

    public void setMask(String mask2) {
        this.mask = mask2;
    }

    public String getGw() {
        return this.f101gw;
    }

    public void setGw(String gw) {
        this.f101gw = gw;
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
