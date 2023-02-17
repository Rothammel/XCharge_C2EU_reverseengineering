package com.xcharge.charger.protocol.anyo.bean.request;

import android.support.p000v4.view.MotionEventCompat;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.common.utils.EndianUtils;
import java.nio.charset.Charset;
import java.util.InputMismatchException;
import org.apache.commons.lang3.CharEncoding;

public class AuthRequest extends AnyoMessage {
    private long balance = 0;
    private String cardNo = null;
    private int cardNoLength = 0;
    private byte cardType = 0;
    private int unbalancedCount = 0;

    public byte getCardType() {
        return this.cardType;
    }

    public void setCardType(byte cardType2) {
        this.cardType = cardType2;
    }

    public int getCardNoLength() {
        return this.cardNoLength;
    }

    public void setCardNoLength(int cardNoLength2) {
        this.cardNoLength = cardNoLength2;
    }

    public String getCardNo() {
        return this.cardNo;
    }

    public void setCardNo(String cardNo2) {
        this.cardNo = cardNo2;
    }

    public long getBalance() {
        return this.balance;
    }

    public void setBalance(long balance2) {
        this.balance = balance2;
    }

    public int getUnbalancedCount() {
        return this.unbalancedCount;
    }

    public void setUnbalancedCount(int unbalancedCount2) {
        this.unbalancedCount = unbalancedCount2;
    }

    public byte[] bodyToBytes() throws Exception {
        if (TextUtils.isEmpty(this.cardNo)) {
            Log.e("AuthRequest.bodyToBytes", "no card no in message: " + toJson());
            throw new InputMismatchException();
        }
        byte[] cardNoBytes = this.cardNo.getBytes(Charset.forName(CharEncoding.UTF_8));
        if (cardNoBytes.length > 20) {
            Log.e("AuthRequest.bodyToBytes", "illegal card no length in message: " + toJson());
            throw new InputMismatchException();
        }
        this.cardNoLength = cardNoBytes.length;
        byte[] bytes = new byte[(cardNoBytes.length + 7)];
        bytes[0] = this.cardType;
        bytes[1] = (byte) (this.cardNoLength & MotionEventCompat.ACTION_MASK);
        System.arraycopy(cardNoBytes, 0, bytes, 2, this.cardNoLength);
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.balance & XMSZHead.ID_BROADCAST)), 0, bytes, this.cardNoLength + 2, 4);
        bytes[this.cardNoLength + 6] = (byte) (this.unbalancedCount & MotionEventCompat.ACTION_MASK);
        return bytes;
    }

    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
