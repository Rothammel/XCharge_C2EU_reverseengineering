package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;

public class LocaleOption extends JsonBean<LocaleOption> {
    private String currency = null;
    private String lang = null;
    private String timezone = null;
    private String timezoneDisp = null;
    private Boolean useDST = null;

    public String getTimezone() {
        return this.timezone;
    }

    public void setTimezone(String timezone2) {
        this.timezone = timezone2;
    }

    public String getTimezoneDisp() {
        return this.timezoneDisp;
    }

    public void setTimezoneDisp(String timezoneDisp2) {
        this.timezoneDisp = timezoneDisp2;
    }

    public Boolean getUseDST() {
        return this.useDST;
    }

    public void setUseDST(Boolean useDST2) {
        this.useDST = useDST2;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency2) {
        this.currency = currency2;
    }

    public String getLang() {
        return this.lang;
    }

    public void setLang(String lang2) {
        this.lang = lang2;
    }
}
