package com.xcharge.charger.core.type;

import com.xcharge.charger.core.api.bean.cap.EventDirective;
import com.xcharge.charger.protocol.monitor.bean.YZXProperty;

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

    private CHARGE_FSM_STATUS(String status2) {
        this.status = YZXProperty.CHARGE_STATUS_IDLE;
        this.status = status2;
    }

    public String getStatus() {
        return this.status;
    }
}
