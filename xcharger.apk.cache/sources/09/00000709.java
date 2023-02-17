package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class DAPCustomUi extends JsonBean<DAPCustomUi> {
    private DAPLogo logo;
    private String scan_hint_text;
    private String scan_hint_title;

    public DAPLogo getLogo() {
        return this.logo;
    }

    public void setLogo(DAPLogo logo) {
        this.logo = logo;
    }

    public String getScan_hint_title() {
        return this.scan_hint_title;
    }

    public void setScan_hint_title(String scan_hint_title) {
        this.scan_hint_title = scan_hint_title;
    }

    public String getScan_hint_text() {
        return this.scan_hint_text;
    }

    public void setScan_hint_text(String scan_hint_text) {
        this.scan_hint_text = scan_hint_text;
    }
}