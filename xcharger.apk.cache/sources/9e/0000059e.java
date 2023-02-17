package com.xcharge.charger.data.bean.setting;

import com.xcharge.charger.data.bean.ContentItem;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class UserDefineUISetting extends JsonBean<UserDefineUISetting> {
    private String welcome = null;
    private String scanHintTitle = null;
    private String scanHintDesc = null;
    private ContentItem logo = null;
    private ContentItem company = null;
    private String deviceCode = null;

    public String getWelcome() {
        return this.welcome;
    }

    public void setWelcome(String welcome) {
        this.welcome = welcome;
    }

    public String getScanHintTitle() {
        return this.scanHintTitle;
    }

    public void setScanHintTitle(String scanHintTitle) {
        this.scanHintTitle = scanHintTitle;
    }

    public String getScanHintDesc() {
        return this.scanHintDesc;
    }

    public void setScanHintDesc(String scanHintDesc) {
        this.scanHintDesc = scanHintDesc;
    }

    public ContentItem getLogo() {
        return this.logo;
    }

    public void setLogo(ContentItem logo) {
        this.logo = logo;
    }

    public ContentItem getCompany() {
        return this.company;
    }

    public void setCompany(ContentItem company) {
        this.company = company;
    }

    public String getDeviceCode() {
        return this.deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }
}