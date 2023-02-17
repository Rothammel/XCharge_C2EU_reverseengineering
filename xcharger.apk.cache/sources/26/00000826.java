package com.xcharge.charger.ui.adapter.type;

import com.xcharge.charger.core.api.bean.cap.EventDirective;
import com.xcharge.charger.protocol.monitor.bean.YZXDCAPOption;
import com.xcharge.charger.protocol.monitor.bean.YZXProperty;

/* loaded from: classes.dex */
public enum CHARGE_UI_STAGE {
    ready("ready"),
    auth("auth"),
    scan_advert("scan_advert"),
    user_reserved("user_reserved"),
    inited("inited"),
    plugin(EventDirective.EVENT_PLUGIN),
    reserve("reserve"),
    charging(YZXProperty.CHARGE_STATUS_CHARGING),
    prestop("prestop"),
    stopped(YZXProperty.CHARGE_STATUS_STOPPED),
    paid("paid"),
    delay_wait("delay_wait"),
    delay(YZXDCAPOption.DELAY),
    billed("billed"),
    refuse("refuse"),
    error_stop("error_stop");
    
    private String stage;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static CHARGE_UI_STAGE[] valuesCustom() {
        CHARGE_UI_STAGE[] valuesCustom = values();
        int length = valuesCustom.length;
        CHARGE_UI_STAGE[] charge_ui_stageArr = new CHARGE_UI_STAGE[length];
        System.arraycopy(valuesCustom, 0, charge_ui_stageArr, 0, length);
        return charge_ui_stageArr;
    }

    CHARGE_UI_STAGE(String stage) {
        this.stage = null;
        this.stage = stage;
    }

    public String getStage() {
        return this.stage;
    }
}