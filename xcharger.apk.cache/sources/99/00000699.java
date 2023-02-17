package com.xcharge.charger.protocol.family.xcloud.bean.cloud;

import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class SendChargeQRCode extends JsonBean<SendChargeQRCode> {
    private Long sid = null;
    private String qrCode = null;
    private int expireInterval = 0;
    private long time = 0;
    private DeviceError error = null;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }

    public String getQrCode() {
        return this.qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public int getExpireInterval() {
        return this.expireInterval;
    }

    public void setExpireInterval(int expireInterval) {
        this.expireInterval = expireInterval;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public DeviceError getError() {
        return this.error;
    }

    public void setError(DeviceError error) {
        this.error = error;
    }
}