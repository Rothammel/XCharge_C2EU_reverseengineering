package com.xcharge.charger.protocol.xmsz.bean.cloud;

import android.util.Log;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.common.utils.FormatUtils;

/* loaded from: classes.dex */
public class ChangeAvailabilityRequest extends XMSZMessage {
    public static final byte WORK_DISABLE = 0;
    public static final byte WORK_ENABLE = 1;
    private byte connectorId = 1;
    private byte enable = 1;

    public byte getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(byte connectorId) {
        this.connectorId = connectorId;
    }

    public byte getEnable() {
        return this.enable;
    }

    public void setEnable(byte enable) {
        this.enable = enable;
    }

    @Override // com.xcharge.charger.protocol.xmsz.bean.XMSZMessage
    public byte[] bodyToBytes() throws Exception {
        byte[] bytes = {this.connectorId, this.enable};
        return bytes;
    }

    @Override // com.xcharge.charger.protocol.xmsz.bean.XMSZMessage
    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        if (bytes.length != 2) {
            Log.e("ChangeAvailabilityRequest.bodyFromBytes", "body length must be 2 !!! body: " + FormatUtils.bytesToHexString(bytes));
            throw new IllegalArgumentException();
        }
        this.connectorId = bytes[0];
        this.enable = bytes[1];
        return this;
    }
}
