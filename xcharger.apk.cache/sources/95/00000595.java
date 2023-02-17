package com.xcharge.charger.data.bean.setting;

import com.xcharge.charger.data.bean.type.GUN_LOCK_MODE;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class GunLockSetting extends JsonBean<GunLockSetting> {
    private GUN_LOCK_MODE mode = GUN_LOCK_MODE.disable;

    public GUN_LOCK_MODE getMode() {
        return this.mode;
    }

    public void setMode(GUN_LOCK_MODE mode) {
        this.mode = mode;
    }
}