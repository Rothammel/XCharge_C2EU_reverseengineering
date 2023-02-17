package com.xcharge.charger.data.bean.setting;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class CountrySetting extends JsonBean<CountrySetting> {
    private String zone = "+08:00";
    private boolean useDaylightTime = false;
    private String lang = "en";
    private String money = "CNY";
    private String moneyDisp = "元";

    public String getZone() {
        return this.zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public boolean isUseDaylightTime() {
        return this.useDaylightTime;
    }

    public void setUseDaylightTime(boolean useDaylightTime) {
        this.useDaylightTime = useDaylightTime;
    }

    public String getLang() {
        return this.lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getMoney() {
        return this.money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getMoneyDisp() {
        return this.moneyDisp;
    }

    public void setMoneyDisp(String moneyDisp) {
        this.moneyDisp = moneyDisp;
    }

    /* renamed from: clone */
    public CountrySetting m10clone() {
        return deepClone();
    }
}
