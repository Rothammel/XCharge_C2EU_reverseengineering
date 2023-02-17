package com.xcharge.charger.data.bean.setting;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class APNSetting extends JsonBean<APNSetting> {
    private String carrier = null;
    private String apn = null;
    private String user = null;
    private String password = null;
    private String mcc = null;
    private String mnc = null;
    private String type = null;
    private String server = null;
    private String proxy = null;
    private String port = null;
    private String mmsc = null;
    private String mmsproxy = null;
    private String mmsport = null;

    public String getCarrier() {
        return this.carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getApn() {
        return this.apn;
    }

    public void setApn(String apn) {
        this.apn = apn;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMcc() {
        return this.mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getMnc() {
        return this.mnc;
    }

    public void setMnc(String mnc) {
        this.mnc = mnc;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getServer() {
        return this.server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getProxy() {
        return this.proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getMmsc() {
        return this.mmsc;
    }

    public void setMmsc(String mmsc) {
        this.mmsc = mmsc;
    }

    public String getMmsproxy() {
        return this.mmsproxy;
    }

    public void setMmsproxy(String mmsproxy) {
        this.mmsproxy = mmsproxy;
    }

    public String getMmsport() {
        return this.mmsport;
    }

    public void setMmsport(String mmsport) {
        this.mmsport = mmsport;
    }
}
