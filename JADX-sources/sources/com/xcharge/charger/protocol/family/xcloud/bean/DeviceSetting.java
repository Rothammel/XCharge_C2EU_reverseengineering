package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;
import java.util.HashMap;

/* loaded from: classes.dex */
public class DeviceSetting extends JsonBean<DeviceSetting> {
    private Integer intervalStandby = null;
    private Integer intervalCancelCharge = null;
    private Integer intervalStartDelayFee = null;
    private Integer intervalChargeReport = null;
    private LocaleOption locale = null;
    private Integer cpErrorRange = null;
    private Integer vErrorRange = null;
    private Integer vSigDiffRange = null;
    private Integer rcErrorThreshold = null;
    private Boolean earthDisabled = null;
    private Double powerFactor = null;
    private XCloudRadarSetting radar = null;
    private String defaultLightColor = null;
    private Integer powerSupplyPercent = null;
    private Double powerSupply = null;
    private DeviceContent content = null;
    private ArrayList<FeePolicy> feePolicy = null;
    private Long defaultFeePolicy = null;
    private HashMap<String, XCloudPort> ports = null;
    private ArrayList<NFCGroupSeed> nfcGroupSeed = null;
    private String qrcodeChars = null;
    private Object anyOptions = null;

    public Integer getIntervalStandby() {
        return this.intervalStandby;
    }

    public void setIntervalStandby(Integer intervalStandby) {
        this.intervalStandby = intervalStandby;
    }

    public Integer getIntervalCancelCharge() {
        return this.intervalCancelCharge;
    }

    public void setIntervalCancelCharge(Integer intervalCancelCharge) {
        this.intervalCancelCharge = intervalCancelCharge;
    }

    public Integer getIntervalStartDelayFee() {
        return this.intervalStartDelayFee;
    }

    public void setIntervalStartDelayFee(Integer intervalStartDelayFee) {
        this.intervalStartDelayFee = intervalStartDelayFee;
    }

    public Integer getIntervalChargeReport() {
        return this.intervalChargeReport;
    }

    public void setIntervalChargeReport(Integer intervalChargeReport) {
        this.intervalChargeReport = intervalChargeReport;
    }

    public LocaleOption getLocale() {
        return this.locale;
    }

    public void setLocale(LocaleOption locale) {
        this.locale = locale;
    }

    public Integer getCpErrorRange() {
        return this.cpErrorRange;
    }

    public void setCpErrorRange(Integer cpErrorRange) {
        this.cpErrorRange = cpErrorRange;
    }

    public Integer getvErrorRange() {
        return this.vErrorRange;
    }

    public void setvErrorRange(Integer vErrorRange) {
        this.vErrorRange = vErrorRange;
    }

    public Double getPowerFactor() {
        return this.powerFactor;
    }

    public void setPowerFactor(Double powerFactor) {
        this.powerFactor = powerFactor;
    }

    public XCloudRadarSetting getRadar() {
        return this.radar;
    }

    public void setRadar(XCloudRadarSetting radar) {
        this.radar = radar;
    }

    public String getDefaultLightColor() {
        return this.defaultLightColor;
    }

    public void setDefaultLightColor(String defaultLightColor) {
        this.defaultLightColor = defaultLightColor;
    }

    public DeviceContent getContent() {
        return this.content;
    }

    public void setContent(DeviceContent content) {
        this.content = content;
    }

    public ArrayList<FeePolicy> getFeePolicy() {
        return this.feePolicy;
    }

    public void setFeePolicy(ArrayList<FeePolicy> feePolicy) {
        this.feePolicy = feePolicy;
    }

    public Long getDefaultFeePolicy() {
        return this.defaultFeePolicy;
    }

    public void setDefaultFeePolicy(Long defaultFeePolicy) {
        this.defaultFeePolicy = defaultFeePolicy;
    }

    public HashMap<String, XCloudPort> getPorts() {
        return this.ports;
    }

    public void setPorts(HashMap<String, XCloudPort> ports) {
        this.ports = ports;
    }

    public Integer getvSigDiffRange() {
        return this.vSigDiffRange;
    }

    public void setvSigDiffRange(Integer vSigDiffRange) {
        this.vSigDiffRange = vSigDiffRange;
    }

    public Integer getRcErrorThreshold() {
        return this.rcErrorThreshold;
    }

    public void setRcErrorThreshold(Integer rcErrorThreshold) {
        this.rcErrorThreshold = rcErrorThreshold;
    }

    public Integer getPowerSupplyPercent() {
        return this.powerSupplyPercent;
    }

    public void setPowerSupplyPercent(Integer powerSupplyPercent) {
        this.powerSupplyPercent = powerSupplyPercent;
    }

    public Double getPowerSupply() {
        return this.powerSupply;
    }

    public void setPowerSupply(Double powerSupply) {
        this.powerSupply = powerSupply;
    }

    public Boolean getEarthDisabled() {
        return this.earthDisabled;
    }

    public void setEarthDisabled(Boolean earthDisabled) {
        this.earthDisabled = earthDisabled;
    }

    public ArrayList<NFCGroupSeed> getNfcGroupSeed() {
        return this.nfcGroupSeed;
    }

    public void setNfcGroupSeed(ArrayList<NFCGroupSeed> nfcGroupSeed) {
        this.nfcGroupSeed = nfcGroupSeed;
    }

    public String getQrcodeChars() {
        return this.qrcodeChars;
    }

    public void setQrcodeChars(String qrcodeChars) {
        this.qrcodeChars = qrcodeChars;
    }

    public Object getAnyOptions() {
        return this.anyOptions;
    }

    public void setAnyOptions(Object anyOptions) {
        this.anyOptions = anyOptions;
    }
}
