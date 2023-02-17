package com.xcharge.charger.protocol.family.xcloud.bean.cloud;

import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.common.bean.JsonBean;

public class SendChargeQRCode extends JsonBean<SendChargeQRCode> {
    private DeviceError error = null;
    private int expireInterval = 0;
    private String qrCode = null;
    private Long sid = null;
    private long time = 0;

    public Long getSid() {
        return this.sid;
    }

    public void setSid(Long sid2) {
        this.sid = sid2;
    }

    public String getQrCode() {
        return this.qrCode;
    }

    public void setQrCode(String qrCode2) {
        this.qrCode = qrCode2;
    }

    public int getExpireInterval() {
        return this.expireInterval;
    }

    public void setExpireInterval(int expireInterval2) {
        this.expireInterval = expireInterval2;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }

    public DeviceError getError() {
        return this.error;
    }

    public void setError(DeviceError error2) {
        this.error = error2;
    }
}
