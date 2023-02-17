package com.xcharge.charger.data.bean.device;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class Wifi extends JsonBean<Wifi> {
    private String type = Network.NETWORK_TYPE_WIFI;
    private String ifName = null;
    private String ssid = null;
    private int signalDbm = -1000;
    private String ip = null;
    private String mask = null;
    private String gw = null;
    private String dns = null;
    private String mac = null;
    private boolean fault = false;

    public boolean isFault() {
        return this.fault;
    }

    public void setFault(boolean fault) {
        this.fault = fault;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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

    public String getSsid() {
        return this.ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getMac() {
        return this.mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getMask() {
        return this.mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSignalDbm() {
        return this.signalDbm;
    }

    public void setSignalDbm(int signalDbm) {
        this.signalDbm = signalDbm;
    }

    public String getIfName() {
        return this.ifName;
    }

    public void setIfName(String ifName) {
        this.ifName = ifName;
    }
}
