package com.xcharge.charger.data.bean.device;

import com.xcharge.common.bean.JsonBean;

public class Ethernet extends JsonBean<Ethernet> {
    private String dns = null;
    private boolean fault = false;

    /* renamed from: gw */
    private String f49gw = null;
    private String ifName = null;

    /* renamed from: ip */
    private String f50ip = null;
    private String mac = null;
    private String mask = null;
    private String type = Network.NETWORK_TYPE_ETHERNET;

    public boolean isFault() {
        return this.fault;
    }

    public void setFault(boolean fault2) {
        this.fault = fault2;
    }

    public String getIp() {
        return this.f50ip;
    }

    public void setIp(String ip) {
        this.f50ip = ip;
    }

    public String getGw() {
        return this.f49gw;
    }

    public void setGw(String gw) {
        this.f49gw = gw;
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

    public String getIfName() {
        return this.ifName;
    }

    public void setIfName(String ifName2) {
        this.ifName = ifName2;
    }
}
