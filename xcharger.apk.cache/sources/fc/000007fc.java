package com.xcharge.charger.protocol.xmsz.bean.device;

import android.util.Log;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.common.utils.EndianUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Iterator;

/* loaded from: classes.dex */
public class StopTransactionRequest extends XMSZMessage {
    public static final String KEY_POWER = "meterValue";
    public static final String KEY_TIMESTAMP = "timeStamp";
    private long transactionId = 0;
    private String userIdTag = "";
    private byte meterValueCount = 0;
    private ArrayList<HashMap<String, Long>> timePowerValues = new ArrayList<>();

    @Override // com.xcharge.charger.protocol.xmsz.bean.XMSZMessage
    public byte[] bodyToBytes() throws Exception {
        int meterCnt = this.meterValueCount & 255;
        if (this.timePowerValues.size() != meterCnt) {
            Log.e("StopTransactionRequest.bodyToBytes", "illegal meterValue in message: " + toJson());
            throw new InputMismatchException();
        }
        byte[] bytes = new byte[(meterCnt * 8) + 21];
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.transactionId & XMSZHead.ID_BROADCAST)), 0, bytes, 0, 4);
        byte[] userIdTagBytes = this.userIdTag.getBytes(XMSZMessage.GBK_NAME);
        int userIdTagLength = userIdTagBytes.length > 16 ? 16 : userIdTagBytes.length;
        System.arraycopy(userIdTagBytes, 0, bytes, 4, userIdTagLength);
        bytes[20] = this.meterValueCount;
        if (meterCnt > 0) {
            int pos = 21;
            Iterator<HashMap<String, Long>> it2 = this.timePowerValues.iterator();
            while (it2.hasNext()) {
                HashMap<String, Long> element = it2.next();
                Long timeStamp = element.get(KEY_TIMESTAMP);
                Long power = element.get(KEY_POWER);
                if (timeStamp == null || power == null) {
                    Log.e("StopTransactionRequest.bodyToBytes", "illegal meterValue in message: " + toJson());
                    throw new InputMismatchException();
                }
                System.arraycopy(EndianUtils.intToLittleBytes((int) (timeStamp.longValue() & XMSZHead.ID_BROADCAST)), 0, bytes, pos, 4);
                System.arraycopy(EndianUtils.intToLittleBytes((int) (power.longValue() & XMSZHead.ID_BROADCAST)), 0, bytes, pos + 4, 4);
                pos += 8;
            }
        }
        return bytes;
    }

    public long getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public String getUserIdTag() {
        return this.userIdTag;
    }

    public void setUserIdTag(String userIdTag) {
        this.userIdTag = userIdTag;
    }

    public byte getMeterValueCount() {
        return this.meterValueCount;
    }

    public void setMeterValueCount(byte meterValueCount) {
        this.meterValueCount = meterValueCount;
    }

    public ArrayList<HashMap<String, Long>> getTimePowerValues() {
        return this.timePowerValues;
    }

    public void setTimePowerValues(ArrayList<HashMap<String, Long>> timePowerValues) {
        this.timePowerValues = timePowerValues;
    }

    @Override // com.xcharge.charger.protocol.xmsz.bean.XMSZMessage
    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}