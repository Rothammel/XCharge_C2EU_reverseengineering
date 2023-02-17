package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;
import java.util.HashMap;

public class DeviceSetting extends JsonBean<DeviceSetting> {
    private Object anyOptions = null;
    private DeviceContent content = null;
    private Integer cpErrorRange = null;
    private Long defaultFeePolicy = null;
    private String defaultLightColor = null;
    private Boolean earthDisabled = null;
    private ArrayList<FeePolicy> feePolicy = null;
    private Integer intervalCancelCharge = null;
    private Integer intervalChargeReport = null;
    private Integer intervalStandby = null;
    private Integer intervalStartDelayFee = null;
    private LocaleOption locale = null;
    private ArrayList<NFCGroupSeed> nfcGroupSeed = null;
    private HashMap<String, XCloudPort> ports = null;
    private Double powerFactor = null;
    private Double powerSupply = null;
    private Integer powerSupplyPercent = null;
    private String qrcodeChars = null;
    private XCloudRadarSetting radar = null;
    private Integer rcErrorThreshold = null;
    private Integer vErrorRange = null;
    private Integer vSigDiffRange = null;

    public Integer getIntervalStandby() {
        return this.intervalStandby;
    }

    public void setIntervalStandby(Integer intervalStandby2) {
        this.intervalStandby = intervalStandby2;
    }

    public Integer getIntervalCancelCharge() {
        return this.intervalCancelCharge;
    }

    public void setIntervalCancelCharge(Integer intervalCancelCharge2) {
        this.intervalCancelCharge = intervalCancelCharge2;
    }

    public Integer getIntervalStartDelayFee() {
        return this.intervalStartDelayFee;
    }

    public void setIntervalStartDelayFee(Integer intervalStartDelayFee2) {
        this.intervalStartDelayFee = intervalStartDelayFee2;
    }

    public Integer getIntervalChargeReport() {
        return this.intervalChargeReport;
    }

    public void setIntervalChargeReport(Integer intervalChargeReport2) {
        this.intervalChargeReport = intervalChargeReport2;
    }

    public LocaleOption getLocale() {
        return this.locale;
    }

    public void setLocale(LocaleOption locale2) {
        this.locale = locale2;
    }

    public Integer getCpErrorRange() {
        return this.cpErrorRange;
    }

    public void setCpErrorRange(Integer cpErrorRange2) {
        this.cpErrorRange = cpErrorRange2;
    }

    public Integer getvErrorRange() {
        return this.vErrorRange;
    }

    public void setvErrorRange(Integer vErrorRange2) {
        this.vErrorRange = vErrorRange2;
    }

    public Double getPowerFactor() {
        return this.powerFactor;
    }

    public void setPowerFactor(Double powerFactor2) {
        this.powerFactor = powerFactor2;
    }

    public XCloudRadarSetting getRadar() {
        return this.radar;
    }

    public void setRadar(XCloudRadarSetting radar2) {
        this.radar = radar2;
    }

    public String getDefaultLightColor() {
        return this.defaultLightColor;
    }

    public void setDefaultLightColor(String defaultLightColor2) {
        this.defaultLightColor = defaultLightColor2;
    }

    public DeviceContent getContent() {
        return this.content;
    }

    public void setContent(DeviceContent content2) {
        this.content = content2;
    }

    public ArrayList<FeePolicy> getFeePolicy() {
        return this.feePolicy;
    }

    public void setFeePolicy(ArrayList<FeePolicy> feePolicy2) {
        this.feePolicy = feePolicy2;
    }

    public Long getDefaultFeePolicy() {
        return this.defaultFeePolicy;
    }

    public void setDefaultFeePolicy(Long defaultFeePolicy2) {
        this.defaultFeePolicy = defaultFeePolicy2;
    }

    public HashMap<String, XCloudPort> getPorts() {
        return this.ports;
    }

    public void setPorts(HashMap<String, XCloudPort> ports2) {
        this.ports = ports2;
    }

    public Integer getvSigDiffRange() {
        return this.vSigDiffRange;
    }

    public void setvSigDiffRange(Integer vSigDiffRange2) {
        this.vSigDiffRange = vSigDiffRange2;
    }

    public Integer getRcErrorThreshold() {
        return this.rcErrorThreshold;
    }

    public void setRcErrorThreshold(Integer rcErrorThreshold2) {
        this.rcErrorThreshold = rcErrorThreshold2;
    }

    public Integer getPowerSupplyPercent() {
        return this.powerSupplyPercent;
    }

    public void setPowerSupplyPercent(Integer powerSupplyPercent2) {
        this.powerSupplyPercent = powerSupplyPercent2;
    }

    public Double getPowerSupply() {
        return this.powerSupply;
    }

    public void setPowerSupply(Double powerSupply2) {
        this.powerSupply = powerSupply2;
    }

    public Boolean getEarthDisabled() {
        return this.earthDisabled;
    }

    public void setEarthDisabled(Boolean earthDisabled2) {
        this.earthDisabled = earthDisabled2;
    }

    public ArrayList<NFCGroupSeed> getNfcGroupSeed() {
        return this.nfcGroupSeed;
    }

    public void setNfcGroupSeed(ArrayList<NFCGroupSeed> nfcGroupSeed2) {
        this.nfcGroupSeed = nfcGroupSeed2;
    }

    public String getQrcodeChars() {
        return this.qrcodeChars;
    }

    public void setQrcodeChars(String qrcodeChars2) {
        this.qrcodeChars = qrcodeChars2;
    }

    public Object getAnyOptions() {
        return this.anyOptions;
    }

    public void setAnyOptions(Object anyOptions2) {
        this.anyOptions = anyOptions2;
    }
}
