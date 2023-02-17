package com.xcharge.charger.core.type;

import com.xcharge.charger.core.api.bean.cap.EventDirective;
import com.xcharge.charger.protocol.monitor.bean.YZXProperty;

/* loaded from: classes.dex */
public enum CHARGE_FSM_STATUS {
    idle(YZXProperty.CHARGE_STATUS_IDLE),
    auth_sended("auth_sended"),
    authed("authed"),
    init_ack_sended("init_ack_sended"),
    init_advert("init_advert"),
    user_reserved("user_reserved"),
    user_reserve_wait_plugin("user_reserve_wait_plugin"),
    inited("inited"),
    plugin(EventDirective.EVENT_PLUGIN),
    reserve_wait("reserve_wait"),
    charging(YZXProperty.CHARGE_STATUS_CHARGING),
    pre_stop("pre_stop"),
    paused("paused"),
    stop_sended("stop_sended"),
    stopped(YZXProperty.CHARGE_STATUS_STOPPED),
    fin_sended("fin_sended");
    
    private String status;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static CHARGE_FSM_STATUS[] valuesCustom() {
        CHARGE_FSM_STATUS[] valuesCustom = values();
        int length = valuesCustom.length;
        CHARGE_FSM_STATUS[] charge_fsm_statusArr = new CHARGE_FSM_STATUS[length];
        System.arraycopy(valuesCustom, 0, charge_fsm_statusArr, 0, length);
        return charge_fsm_statusArr;
    }

    CHARGE_FSM_STATUS(String status) {
        this.status = YZXProperty.CHARGE_STATUS_IDLE;
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}
