package com.xcharge.charger.protocol.monitor.bean.ddap;

/* loaded from: classes.dex */
public class DDAPMobileAccess {
    public static final String PLMN_46000 = "46000";
    public static final String PLMN_46001 = "46001";
    public static final String PLMN_46002 = "46002";
    public static final String PLMN_46003 = "46003";
    public static final String PLMN_46006 = "46006";
    public static final String PLMN_46007 = "46007";
    public static final String PLMN_46011 = "46011";
    public static final String TYPE_MOBILE_2G = "2G";
    public static final String TYPE_MOBILE_3G = "3G";
    public static final String TYPE_MOBILE_4G = "4G";
    public static final String TYPE_MOBILE_5G = "5G";
    private String type = null;
    private String PLMN = null;
    private String ip = null;
    private String gw = null;
    private String dns = null;
    private String IMEI = null;
    private String IMSI = null;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPLMN() {
        return this.PLMN;
    }

    public void setPLMN(String pLMN) {
        this.PLMN = pLMN;
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

    public String getIMEI() {
        return this.IMEI;
    }

    public void setIMEI(String iMEI) {
        this.IMEI = iMEI;
    }

    public String getIMSI() {
        return this.IMSI;
    }

    public void setIMSI(String iMSI) {
        this.IMSI = iMSI;
    }
}
