package com.xcharge.charger.protocol.xmsz.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class XMSZPileStatus extends JsonBean<XMSZPileStatus> {
    private byte pileStatus = 0;
    private byte pileError = 4;

    public byte getPileStatus() {
        return this.pileStatus;
    }

    public void setPileStatus(byte pileStatus) {
        this.pileStatus = pileStatus;
    }

    public byte getPileError() {
        return this.pileError;
    }

    public void setPileError(byte pileError) {
        this.pileError = pileError;
    }
}