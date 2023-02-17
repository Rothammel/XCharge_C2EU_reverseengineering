package com.xcharge.charger.p006ui.adapter.type;

import com.xcharge.charger.core.api.bean.cap.EventDirective;
import com.xcharge.charger.protocol.monitor.bean.YZXDCAPOption;
import com.xcharge.charger.protocol.monitor.bean.YZXProperty;

/* renamed from: com.xcharge.charger.ui.adapter.type.CHARGE_UI_STAGE */
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

    private CHARGE_UI_STAGE(String stage2) {
        this.stage = null;
        this.stage = stage2;
    }

    public String getStage() {
        return this.stage;
    }
}
