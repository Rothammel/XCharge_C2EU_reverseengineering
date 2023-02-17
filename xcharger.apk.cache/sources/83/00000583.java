package com.xcharge.charger.data.bean.device;

import com.xcharge.charger.data.bean.setting.APNSetting;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class MobileNet extends JsonBean<MobileNet> {
    public static final String ACTION_SIGNAL_CHANGED = "com.xcharge.charger.data.bean.status.ACTION_SIGNAL_CHANGED";
    public static final String IF_NAME_UNKNOWN = "unknown";
    public static final String NET_CDMA2000 = "CDMA2000";
    public static final String NET_GSM = "GSM";
    public static final String NET_LTEFDD = "LTE—FDD";
    public static final String NET_LTETDD = "LTE—TDD";
    public static final String NET_TDSCDMA = "TD-SCDMA";
    public static final String NET_WCDMA = "WCDMA";
    public static final String OPR_CMCC = "CMCC";
    public static final String OPR_CT = "CT";
    public static final String OPR_CUCC = "CUCC";
    public static final String OPR_UNKNOWN = "UNKNOWN";
    public static final String PLMN_46000 = "46000";
    public static final String PLMN_46001 = "46001";
    public static final String PLMN_46002 = "46002";
    public static final String PLMN_46003 = "46003";
    public static final String PLMN_46004 = "46004";
    public static final String PLMN_46005 = "46005";
    public static final String PLMN_46006 = "46006";
    public static final String PLMN_46007 = "46007";
    public static final String PLMN_46011 = "46011";
    public static final String SIM_STATE_ABSENT = "absent";
    public static final String SIM_STATE_LOCKED = "locked";
    public static final String SIM_STATE_NEED_PIN = "pin";
    public static final String SIM_STATE_NEED_PUK = "puk";
    public static final String SIM_STATE_OK = "ok";
    public static final String SIM_STATE_UNKNOWN = "unknown";
    private String type = "none";
    private Integer subtype = null;
    private String subtypeName = null;
    private String ifName = "unknown";
    private String oprator = null;
    private String PLMN = null;
    private int signalDbm = -1000;
    private int asu = 99;
    private int defaultSignalLevel = -1;
    private String ip = null;
    private String gw = null;
    private String dns = null;
    private String IMEI = null;
    private String MSISDN = null;
    private String ICCID = null;
    private String IMSI = null;
    private String basebandSV = null;
    private String simState = "unknown";
    private APNSetting preferApn = null;
    private String simMCC = null;
    private String simMNC = null;
    private boolean fault = false;

    public boolean isFault() {
        return this.fault;
    }

    public void setFault(boolean fault) {
        this.fault = fault;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getSubtype() {
        return this.subtype;
    }

    public void setSubtype(Integer subtype) {
        this.subtype = subtype;
    }

    public String getSubtypeName() {
        return this.subtypeName;
    }

    public void setSubtypeName(String subtypeName) {
        this.subtypeName = subtypeName;
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

    public String getOprator() {
        return this.oprator;
    }

    public void setOprator(String oprator) {
        this.oprator = oprator;
    }

    public String getPLMN() {
        return this.PLMN;
    }

    public void setPLMN(String pLMN) {
        this.PLMN = pLMN;
    }

    public int getAsu() {
        return this.asu;
    }

    public void setAsu(int asu) {
        this.asu = asu;
    }

    public int getSignalDbm() {
        return this.signalDbm;
    }

    public void setSignalDbm(int signalDbm) {
        this.signalDbm = signalDbm;
    }

    public int getDefaultSignalLevel() {
        return this.defaultSignalLevel;
    }

    public void setDefaultSignalLevel(int defaultSignalLevel) {
        this.defaultSignalLevel = defaultSignalLevel;
    }

    public String getIfName() {
        return this.ifName;
    }

    public void setIfName(String ifName) {
        this.ifName = ifName;
    }

    public String getIMEI() {
        return this.IMEI;
    }

    public void setIMEI(String iMEI) {
        this.IMEI = iMEI;
    }

    public String getMSISDN() {
        return this.MSISDN;
    }

    public void setMSISDN(String mSISDN) {
        this.MSISDN = mSISDN;
    }

    public String getICCID() {
        return this.ICCID;
    }

    public void setICCID(String iCCID) {
        this.ICCID = iCCID;
    }

    public String getIMSI() {
        return this.IMSI;
    }

    public void setIMSI(String iMSI) {
        this.IMSI = iMSI;
    }

    public String getBasebandSV() {
        return this.basebandSV;
    }

    public void setBasebandSV(String basebandSV) {
        this.basebandSV = basebandSV;
    }

    public String getSimState() {
        return this.simState;
    }

    public void setSimState(String simState) {
        this.simState = simState;
    }

    public APNSetting getPreferApn() {
        return this.preferApn;
    }

    public void setPreferApn(APNSetting preferApn) {
        this.preferApn = preferApn;
    }

    public String getSimMCC() {
        return this.simMCC;
    }

    public void setSimMCC(String simMCC) {
        this.simMCC = simMCC;
    }

    public String getSimMNC() {
        return this.simMNC;
    }

    public void setSimMNC(String simMNC) {
        this.simMNC = simMNC;
    }
}