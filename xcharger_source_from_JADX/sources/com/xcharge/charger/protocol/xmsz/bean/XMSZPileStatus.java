package com.xcharge.charger.protocol.xmsz.bean;

import com.xcharge.common.bean.JsonBean;

public class XMSZPileStatus extends JsonBean<XMSZPileStatus> {
    private byte pileError = 4;
    private byte pileStatus = 0;

    public byte getPileStatus() {
        return this.pileStatus;
    }

    public void setPileStatus(byte pileStatus2) {
        this.pileStatus = pileStatus2;
    }

    public byte getPileError() {
        return this.pileError;
    }

    public void setPileError(byte pileError2) {
        this.pileError = pileError2;
    }
}
