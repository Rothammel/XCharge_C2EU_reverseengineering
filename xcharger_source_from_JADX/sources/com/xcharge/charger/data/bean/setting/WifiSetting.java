package com.xcharge.charger.data.bean.setting;

import com.xcharge.common.bean.JsonBean;

public class WifiSetting extends JsonBean<WifiSetting> {
    private String pwd = null;
    private String ssid = null;

    public String getSsid() {
        return this.ssid;
    }

    public void setSsid(String ssid2) {
        this.ssid = ssid2;
    }

    public String getPwd() {
        return this.pwd;
    }

    public void setPwd(String pwd2) {
        this.pwd = pwd2;
    }
}
