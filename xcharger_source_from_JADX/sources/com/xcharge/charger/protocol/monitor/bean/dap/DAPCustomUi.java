package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.common.bean.JsonBean;

public class DAPCustomUi extends JsonBean<DAPCustomUi> {
    private DAPLogo logo;
    private String scan_hint_text;
    private String scan_hint_title;

    public DAPLogo getLogo() {
        return this.logo;
    }

    public void setLogo(DAPLogo logo2) {
        this.logo = logo2;
    }

    public String getScan_hint_title() {
        return this.scan_hint_title;
    }

    public void setScan_hint_title(String scan_hint_title2) {
        this.scan_hint_title = scan_hint_title2;
    }

    public String getScan_hint_text() {
        return this.scan_hint_text;
    }

    public void setScan_hint_text(String scan_hint_text2) {
        this.scan_hint_text = scan_hint_text2;
    }
}
