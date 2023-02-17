package com.xcharge.charger.device.c2.bean;

import android.text.TextUtils;
import com.xcharge.charger.device.c2.nfc.NFCUtils;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.FormatUtils;
import java.nio.charset.Charset;
import org.apache.commons.lang3.CharEncoding;

/* loaded from: classes.dex */
public class AuthSign extends JsonBean<AuthSign> {
    private long time = 0;
    private String rand = null;
    private String sign = null;
    private String data = null;

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getRand() {
        return this.rand;
    }

    public void setRand(String rand) {
        this.rand = rand;
    }

    public String getSign() {
        return this.sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public static AuthSign parse(NFCSign nfcSign) {
        if (nfcSign == null) {
            return null;
        }
        AuthSign authSign = new AuthSign();
        String data = nfcSign.getData();
        if (TextUtils.isEmpty(data) || data.length() != 32) {
            return null;
        }
        byte[] dataBytes = FormatUtils.hexStringToBytes(data);
        byte[] time = new byte[8];
        byte[] rand = new byte[8];
        System.arraycopy(dataBytes, 0, time, 0, time.length);
        System.arraycopy(dataBytes, time.length, rand, 0, rand.length);
        int validBytesInRand = rand.length;
        for (int i = rand.length; i > 0 && rand[i - 1] == 0; i--) {
            validBytesInRand--;
        }
        byte[] validRand = new byte[validBytesInRand];
        System.arraycopy(rand, 0, validRand, 0, validBytesInRand);
        authSign.setTime(NFCUtils.bytesToLong(time));
        authSign.setRand(new String(validRand, Charset.forName(CharEncoding.UTF_8)));
        authSign.setData(data);
        String sign = nfcSign.getSign();
        if (TextUtils.isEmpty(sign)) {
            return null;
        }
        byte[] signBytes = FormatUtils.hexStringToBytes(sign);
        authSign.setSign(new String(signBytes, Charset.forName(CharEncoding.UTF_8)));
        return authSign;
    }
}
