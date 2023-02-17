package com.xcharge.charger.device.p005c2.bean;

import android.text.TextUtils;
import com.xcharge.charger.device.p005c2.nfc.NFCUtils;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.FormatUtils;
import java.nio.charset.Charset;
import org.apache.commons.lang3.CharEncoding;

/* renamed from: com.xcharge.charger.device.c2.bean.AuthSign */
public class AuthSign extends JsonBean<AuthSign> {
    private String data = null;
    private String rand = null;
    private String sign = null;
    private long time = 0;

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }

    public String getRand() {
        return this.rand;
    }

    public void setRand(String rand2) {
        this.rand = rand2;
    }

    public String getSign() {
        return this.sign;
    }

    public void setSign(String sign2) {
        this.sign = sign2;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data2) {
        this.data = data2;
    }

    public static AuthSign parse(NFCSign nfcSign) {
        if (nfcSign == null) {
            return null;
        }
        AuthSign authSign = new AuthSign();
        String data2 = nfcSign.getData();
        if (TextUtils.isEmpty(data2) || data2.length() != 32) {
            return null;
        }
        byte[] dataBytes = FormatUtils.hexStringToBytes(data2);
        byte[] time2 = new byte[8];
        byte[] rand2 = new byte[8];
        System.arraycopy(dataBytes, 0, time2, 0, time2.length);
        System.arraycopy(dataBytes, time2.length, rand2, 0, rand2.length);
        int validBytesInRand = rand2.length;
        int i = rand2.length;
        while (i > 0 && rand2[i - 1] == 0) {
            validBytesInRand--;
            i--;
        }
        byte[] validRand = new byte[validBytesInRand];
        System.arraycopy(rand2, 0, validRand, 0, validBytesInRand);
        authSign.setTime(NFCUtils.bytesToLong(time2));
        authSign.setRand(new String(validRand, Charset.forName(CharEncoding.UTF_8)));
        authSign.setData(data2);
        String sign2 = nfcSign.getSign();
        if (TextUtils.isEmpty(sign2)) {
            return null;
        }
        authSign.setSign(new String(FormatUtils.hexStringToBytes(sign2), Charset.forName(CharEncoding.UTF_8)));
        return authSign;
    }
}
