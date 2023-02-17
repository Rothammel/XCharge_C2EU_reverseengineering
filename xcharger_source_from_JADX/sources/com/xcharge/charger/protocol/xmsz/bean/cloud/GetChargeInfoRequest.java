package com.xcharge.charger.protocol.xmsz.bean.cloud;

import android.util.Log;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.common.utils.FormatUtils;

public class GetChargeInfoRequest extends XMSZMessage {
    private byte connectorId = 1;

    public byte getConnectorId() {
        return this.connectorId;
    }

    public void setConnectorId(byte connectorId2) {
        this.connectorId = connectorId2;
    }

    public byte[] bodyToBytes() throws Exception {
        return new byte[]{this.connectorId};
    }

    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        if (bytes.length != 1) {
            Log.e("GetChargeInfoRequest.bodyFromBytes", "body length must be 1 !!! body: " + FormatUtils.bytesToHexString(bytes));
            throw new IllegalArgumentException();
        }
        this.connectorId = bytes[0];
        return this;
    }
}
