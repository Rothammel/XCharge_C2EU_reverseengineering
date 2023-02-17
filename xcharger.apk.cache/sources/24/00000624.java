package com.xcharge.charger.protocol.anyo.bean.request;

import android.support.v4.view.MotionEventCompat;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.common.utils.EndianUtils;
import java.nio.charset.Charset;
import java.util.InputMismatchException;
import org.apache.commons.lang3.CharEncoding;

/* loaded from: classes.dex */
public class AuthRequest extends AnyoMessage {
    private byte cardType = 0;
    private int cardNoLength = 0;
    private String cardNo = null;
    private long balance = 0;
    private int unbalancedCount = 0;

    public byte getCardType() {
        return this.cardType;
    }

    public void setCardType(byte cardType) {
        this.cardType = cardType;
    }

    public int getCardNoLength() {
        return this.cardNoLength;
    }

    public void setCardNoLength(int cardNoLength) {
        this.cardNoLength = cardNoLength;
    }

    public String getCardNo() {
        return this.cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public long getBalance() {
        return this.balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public int getUnbalancedCount() {
        return this.unbalancedCount;
    }

    public void setUnbalancedCount(int unbalancedCount) {
        this.unbalancedCount = unbalancedCount;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public byte[] bodyToBytes() throws Exception {
        if (TextUtils.isEmpty(this.cardNo)) {
            Log.e("AuthRequest.bodyToBytes", "no card no in message: " + toJson());
            throw new InputMismatchException();
        }
        Charset utf8 = Charset.forName(CharEncoding.UTF_8);
        byte[] cardNoBytes = this.cardNo.getBytes(utf8);
        if (cardNoBytes.length > 20) {
            Log.e("AuthRequest.bodyToBytes", "illegal card no length in message: " + toJson());
            throw new InputMismatchException();
        }
        this.cardNoLength = cardNoBytes.length;
        byte[] bytes = new byte[cardNoBytes.length + 7];
        bytes[0] = this.cardType;
        bytes[1] = (byte) (this.cardNoLength & MotionEventCompat.ACTION_MASK);
        System.arraycopy(cardNoBytes, 0, bytes, 2, this.cardNoLength);
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.balance & XMSZHead.ID_BROADCAST)), 0, bytes, this.cardNoLength + 2, 4);
        bytes[this.cardNoLength + 6] = (byte) (this.unbalancedCount & MotionEventCompat.ACTION_MASK);
        return bytes;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}