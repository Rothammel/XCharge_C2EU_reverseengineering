package com.xcharge.charger.data.bean.setting;

import com.xcharge.common.bean.JsonBean;

public class APNSetting extends JsonBean<APNSetting> {
    private String apn = null;
    private String carrier = null;
    private String mcc = null;
    private String mmsc = null;
    private String mmsport = null;
    private String mmsproxy = null;
    private String mnc = null;
    private String password = null;
    private String port = null;
    private String proxy = null;
    private String server = null;
    private String type = null;
    private String user = null;

    public String getCarrier() {
        return this.carrier;
    }

    public void setCarrier(String carrier2) {
        this.carrier = carrier2;
    }

    public String getApn() {
        return this.apn;
    }

    public void setApn(String apn2) {
        this.apn = apn2;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user2) {
        this.user = user2;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password2) {
        this.password = password2;
    }

    public String getMcc() {
        return this.mcc;
    }

    public void setMcc(String mcc2) {
        this.mcc = mcc2;
    }

    public String getMnc() {
        return this.mnc;
    }

    public void setMnc(String mnc2) {
        this.mnc = mnc2;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type2) {
        this.type = type2;
    }

    public String getServer() {
        return this.server;
    }

    public void setServer(String server2) {
        this.server = server2;
    }

    public String getProxy() {
        return this.proxy;
    }

    public void setProxy(String proxy2) {
        this.proxy = proxy2;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port2) {
        this.port = port2;
    }

    public String getMmsc() {
        return this.mmsc;
    }

    public void setMmsc(String mmsc2) {
        this.mmsc = mmsc2;
    }

    public String getMmsproxy() {
        return this.mmsproxy;
    }

    public void setMmsproxy(String mmsproxy2) {
        this.mmsproxy = mmsproxy2;
    }

    public String getMmsport() {
        return this.mmsport;
    }

    public void setMmsport(String mmsport2) {
        this.mmsport = mmsport2;
    }
}
