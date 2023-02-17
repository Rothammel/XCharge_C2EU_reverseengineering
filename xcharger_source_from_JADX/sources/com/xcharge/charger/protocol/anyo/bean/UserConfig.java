package com.xcharge.charger.protocol.anyo.bean;

import com.xcharge.common.bean.JsonBean;

public class UserConfig extends JsonBean<UserConfig> {
    private String cloudHost = null;
    private Integer cloudPort = null;
    private Long magicNumber = null;
    private Integer provider = null;
    private String qrcode = null;

    public Integer getProvider() {
        return this.provider;
    }

    public void setProvider(Integer provider2) {
        this.provider = provider2;
    }

    public Long getMagicNumber() {
        return this.magicNumber;
    }

    public void setMagicNumber(Long magicNumber2) {
        this.magicNumber = magicNumber2;
    }

    public String getCloudHost() {
        return this.cloudHost;
    }

    public void setCloudHost(String cloudHost2) {
        this.cloudHost = cloudHost2;
    }

    public Integer getCloudPort() {
        return this.cloudPort;
    }

    public void setCloudPort(Integer cloudPort2) {
        this.cloudPort = cloudPort2;
    }

    public String getQrcode() {
        return this.qrcode;
    }

    public void setQrcode(String qrcode2) {
        this.qrcode = qrcode2;
    }
}
