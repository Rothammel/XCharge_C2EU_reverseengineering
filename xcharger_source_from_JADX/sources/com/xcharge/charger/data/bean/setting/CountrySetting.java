package com.xcharge.charger.data.bean.setting;

import com.xcharge.common.bean.JsonBean;

public class CountrySetting extends JsonBean<CountrySetting> {
    private String lang = "en";
    private String money = "CNY";
    private String moneyDisp = "元";
    private boolean useDaylightTime = false;
    private String zone = "+08:00";

    public String getZone() {
        return this.zone;
    }

    public void setZone(String zone2) {
        this.zone = zone2;
    }

    public boolean isUseDaylightTime() {
        return this.useDaylightTime;
    }

    public void setUseDaylightTime(boolean useDaylightTime2) {
        this.useDaylightTime = useDaylightTime2;
    }

    public String getLang() {
        return this.lang;
    }

    public void setLang(String lang2) {
        this.lang = lang2;
    }

    public String getMoney() {
        return this.money;
    }

    public void setMoney(String money2) {
        this.money = money2;
    }

    public String getMoneyDisp() {
        return this.moneyDisp;
    }

    public void setMoneyDisp(String moneyDisp2) {
        this.moneyDisp = moneyDisp2;
    }

    public CountrySetting clone() {
        return (CountrySetting) deepClone();
    }
}
