package com.xcharge.charger.protocol.ocpp.bean.cloud;

import com.xcharge.common.bean.JsonBean;

public class CancelReservationReq extends JsonBean<CancelReservationReq> {
    private int reservationId;

    public int getReservationId() {
        return this.reservationId;
    }

    public void setReservationId(int reservationId2) {
        this.reservationId = reservationId2;
    }
}
