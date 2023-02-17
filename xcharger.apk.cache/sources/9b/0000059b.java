package com.xcharge.charger.data.bean.setting;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class SwipeCardPermission extends JsonBean<SwipeCardPermission> {
    private boolean permitChargeCtrl = true;
    private boolean permitSetting = true;
    private boolean permitBinding = true;
    private boolean permitTest = false;

    public boolean isPermitChargeCtrl() {
        return this.permitChargeCtrl;
    }

    public void setPermitChargeCtrl(boolean permitChargeCtrl) {
        this.permitChargeCtrl = permitChargeCtrl;
    }

    public boolean isPermitSetting() {
        return this.permitSetting;
    }

    public void setPermitSetting(boolean permitSetting) {
        this.permitSetting = permitSetting;
    }

    public boolean isPermitBinding() {
        return this.permitBinding;
    }

    public void setPermitBinding(boolean permitBinding) {
        this.permitBinding = permitBinding;
    }

    public boolean isPermitTest() {
        return this.permitTest;
    }

    public void setPermitTest(boolean permitTest) {
        this.permitTest = permitTest;
    }
}