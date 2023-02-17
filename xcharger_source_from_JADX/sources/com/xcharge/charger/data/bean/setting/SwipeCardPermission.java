package com.xcharge.charger.data.bean.setting;

import com.xcharge.common.bean.JsonBean;

public class SwipeCardPermission extends JsonBean<SwipeCardPermission> {
    private boolean permitBinding = true;
    private boolean permitChargeCtrl = true;
    private boolean permitSetting = true;
    private boolean permitTest = false;

    public boolean isPermitChargeCtrl() {
        return this.permitChargeCtrl;
    }

    public void setPermitChargeCtrl(boolean permitChargeCtrl2) {
        this.permitChargeCtrl = permitChargeCtrl2;
    }

    public boolean isPermitSetting() {
        return this.permitSetting;
    }

    public void setPermitSetting(boolean permitSetting2) {
        this.permitSetting = permitSetting2;
    }

    public boolean isPermitBinding() {
        return this.permitBinding;
    }

    public void setPermitBinding(boolean permitBinding2) {
        this.permitBinding = permitBinding2;
    }

    public boolean isPermitTest() {
        return this.permitTest;
    }

    public void setPermitTest(boolean permitTest2) {
        this.permitTest = permitTest2;
    }
}
