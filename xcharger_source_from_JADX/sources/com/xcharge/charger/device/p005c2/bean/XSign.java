package com.xcharge.charger.device.p005c2.bean;

import android.text.TextUtils;
import com.xcharge.charger.device.p005c2.nfc.NFCUtils;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.FormatUtils;

/* renamed from: com.xcharge.charger.device.c2.bean.XSign */
public class XSign extends JsonBean<XSign> {
    private int count = 0;
    private String data = null;
    private int rand = 0;
    private String sign = null;
    private long time = 0;

    public long getTime() {
        return this.time;
    }

    public void setTime(long time2) {
        this.time = time2;
    }

    public int getRand() {
        return this.rand;
    }

    public void setRand(int rand2) {
        this.rand = rand2;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count2) {
        this.count = count2;
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

    public static XSign parse(NFCSign nfcSign) {
        if (nfcSign == null) {
            return null;
        }
        XSign xSign = new XSign();
        String data2 = nfcSign.getData();
        if (TextUtils.isEmpty(data2) || data2.length() != 32) {
            return null;
        }
        byte[] dataBytes = FormatUtils.hexStringToBytes(data2);
        byte[] time2 = new byte[4];
        byte[] rand2 = new byte[4];
        System.arraycopy(dataBytes, 0, time2, 0, time2.length);
        System.arraycopy(dataBytes, time2.length, rand2, 0, rand2.length);
        byte count2 = dataBytes[time2.length + rand2.length];
        xSign.setTime((long) NFCUtils.bytesToInt(time2));
        xSign.setRand(NFCUtils.bytesToInt(rand2));
        xSign.setCount(count2 & 255);
        xSign.setData(data2);
        String sign2 = nfcSign.getSign();
        if (TextUtils.isEmpty(sign2)) {
            return null;
        }
        xSign.setSign(sign2);
        return xSign;
    }
}
