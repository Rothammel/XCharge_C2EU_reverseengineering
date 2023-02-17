package com.xcharge.charger.device.p005c2.nfc.charge.type;

import com.xcharge.charger.protocol.monitor.bean.YZXProperty;

/* renamed from: com.xcharge.charger.device.c2.nfc.charge.type.NFC_CHARGE_STATUS */
public enum NFC_CHARGE_STATUS {
    idle(YZXProperty.CHARGE_STATUS_IDLE),
    auth_sended("auth_sended"),
    init_sended("init_sended"),
    inited("inited"),
    charging(YZXProperty.CHARGE_STATUS_CHARGING),
    stop_sended("stop_sended"),
    stopped(YZXProperty.CHARGE_STATUS_STOPPED),
    fin_sended("fin_sended");
    
    private String status;

    private NFC_CHARGE_STATUS(String status2) {
        this.status = YZXProperty.CHARGE_STATUS_IDLE;
        this.status = status2;
    }

    public String getStatus() {
        return this.status;
    }
}
