package com.xcharge.charger.protocol.anyo.bean.response;

import android.util.Log;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.common.utils.EndianUtils;
import com.xcharge.common.utils.FormatUtils;

/* loaded from: classes.dex */
public class ReportHistoryBillResponse extends AnyoMessage {
    private Long billId = null;

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public byte[] bodyToBytes() throws Exception {
        if (this.billId == null) {
            return new byte[0];
        }
        byte[] bytes = new byte[4];
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.billId.longValue() & XMSZHead.ID_BROADCAST)), 0, bytes, 0, 4);
        return bytes;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        if (bytes.length != 0 && bytes.length != 4) {
            Log.e("ReportHistoryBillResponse.bodyFromBytes", "body length must be 0 or 4 !!! body: " + FormatUtils.bytesToHexString(bytes));
            throw new IllegalArgumentException();
        }
        if (bytes.length == 4) {
            this.billId = Long.valueOf(EndianUtils.littleBytesToInt(new byte[]{bytes[0], bytes[1], bytes[2], bytes[3]}) & XMSZHead.ID_BROADCAST);
        }
        return this;
    }
}
