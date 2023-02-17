package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;

public class XCloudRadarSetting extends JsonBean<XCloudRadarSetting> {
    private boolean enabled;

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled2) {
        this.enabled = enabled2;
    }
}
