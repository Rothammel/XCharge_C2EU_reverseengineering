package com.xcharge.charger.protocol.monitor.bean.ddap;

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
    private String IMEI = null;
    private String IMSI = null;
    private String PLMN = null;
    private String dns = null;

    /* renamed from: gw */
    private String f103gw = null;

    /* renamed from: ip */
    private String f104ip = null;
    private String type = null;

    public String getType() {
        return this.type;
    }

    public void setType(String type2) {
        this.type = type2;
    }

    public String getPLMN() {
        return this.PLMN;
    }

    public void setPLMN(String pLMN) {
        this.PLMN = pLMN;
    }

    public String getIp() {
        return this.f104ip;
    }

    public void setIp(String ip) {
        this.f104ip = ip;
    }

    public String getGw() {
        return this.f103gw;
    }

    public void setGw(String gw) {
        this.f103gw = gw;
    }

    public String getDns() {
        return this.dns;
    }

    public void setDns(String dns2) {
        this.dns = dns2;
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
