package com.xcharge.charger.data.bean.device;

import com.xcharge.common.bean.JsonBean;

public class Wifi extends JsonBean<Wifi> {
    private String dns = null;
    private boolean fault = false;

    /* renamed from: gw */
    private String f56gw = null;
    private String ifName = null;

    /* renamed from: ip */
    private String f57ip = null;
    private String mac = null;
    private String mask = null;
    private int signalDbm = -1000;
    private String ssid = null;
    private String type = Network.NETWORK_TYPE_WIFI;

    public boolean isFault() {
        return this.fault;
    }

    public void setFault(boolean fault2) {
        this.fault = fault2;
    }

    public String getIp() {
        return this.f57ip;
    }

    public void setIp(String ip) {
        this.f57ip = ip;
    }

    public String getGw() {
        return this.f56gw;
    }

    public void setGw(String gw) {
        this.f56gw = gw;
    }

    public String getDns() {
        return this.dns;
    }

    public void setDns(String dns2) {
        this.dns = dns2;
    }

    public String getSsid() {
        return this.ssid;
    }

    public void setSsid(String ssid2) {
        this.ssid = ssid2;
    }

    public String getMac() {
        return this.mac;
    }

    public void setMac(String mac2) {
        this.mac = mac2;
    }

    public String getMask() {
        return this.mask;
    }

    public void setMask(String mask2) {
        this.mask = mask2;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type2) {
        this.type = type2;
    }

    public int getSignalDbm() {
        return this.signalDbm;
    }

    public void setSignalDbm(int signalDbm2) {
        this.signalDbm = signalDbm2;
    }

    public String getIfName() {
        return this.ifName;
    }

    public void setIfName(String ifName2) {
        this.ifName = ifName2;
    }
}
