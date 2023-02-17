package com.xcharge.charger.data.bean.setting;

import com.xcharge.charger.data.bean.ContentItem;
import com.xcharge.common.bean.JsonBean;

public class UserDefineUISetting extends JsonBean<UserDefineUISetting> {
    private ContentItem company = null;
    private String deviceCode = null;
    private ContentItem logo = null;
    private String scanHintDesc = null;
    private String scanHintTitle = null;
    private String welcome = null;

    public String getWelcome() {
        return this.welcome;
    }

    public void setWelcome(String welcome2) {
        this.welcome = welcome2;
    }

    public String getScanHintTitle() {
        return this.scanHintTitle;
    }

    public void setScanHintTitle(String scanHintTitle2) {
        this.scanHintTitle = scanHintTitle2;
    }

    public String getScanHintDesc() {
        return this.scanHintDesc;
    }

    public void setScanHintDesc(String scanHintDesc2) {
        this.scanHintDesc = scanHintDesc2;
    }

    public ContentItem getLogo() {
        return this.logo;
    }

    public void setLogo(ContentItem logo2) {
        this.logo = logo2;
    }

    public ContentItem getCompany() {
        return this.company;
    }

    public void setCompany(ContentItem company2) {
        this.company = company2;
    }

    public String getDeviceCode() {
        return this.deviceCode;
    }

    public void setDeviceCode(String deviceCode2) {
        this.deviceCode = deviceCode2;
    }
}
