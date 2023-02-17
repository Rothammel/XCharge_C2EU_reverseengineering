package com.xcharge.charger.protocol.ocpp.bean.device;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class FirmwareStatusNotificationReq extends JsonBean<FirmwareStatusNotificationReq> {
    private String status;

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}