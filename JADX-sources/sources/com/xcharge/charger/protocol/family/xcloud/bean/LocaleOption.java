package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class LocaleOption extends JsonBean<LocaleOption> {
    private String timezone = null;
    private String timezoneDisp = null;
    private Boolean useDST = null;
    private String currency = null;
    private String lang = null;

    public String getTimezone() {
        return this.timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getTimezoneDisp() {
        return this.timezoneDisp;
    }

    public void setTimezoneDisp(String timezoneDisp) {
        this.timezoneDisp = timezoneDisp;
    }

    public Boolean getUseDST() {
        return this.useDST;
    }

    public void setUseDST(Boolean useDST) {
        this.useDST = useDST;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getLang() {
        return this.lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
