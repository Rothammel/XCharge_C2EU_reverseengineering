package com.xcharge.charger.protocol.anyo.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class UserConfig extends JsonBean<UserConfig> {
    private Integer provider = null;
    private Long magicNumber = null;
    private String cloudHost = null;
    private Integer cloudPort = null;
    private String qrcode = null;

    public Integer getProvider() {
        return this.provider;
    }

    public void setProvider(Integer provider) {
        this.provider = provider;
    }

    public Long getMagicNumber() {
        return this.magicNumber;
    }

    public void setMagicNumber(Long magicNumber) {
        this.magicNumber = magicNumber;
    }

    public String getCloudHost() {
        return this.cloudHost;
    }

    public void setCloudHost(String cloudHost) {
        this.cloudHost = cloudHost;
    }

    public Integer getCloudPort() {
        return this.cloudPort;
    }

    public void setCloudPort(Integer cloudPort) {
        this.cloudPort = cloudPort;
    }

    public String getQrcode() {
        return this.qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }
}
