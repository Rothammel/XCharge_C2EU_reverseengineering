package com.xcharge.charger.device.c2.bean;

import android.text.TextUtils;
import com.xcharge.charger.device.c2.nfc.NFCUtils;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.FormatUtils;

/* loaded from: classes.dex */
public class XSign extends JsonBean<XSign> {
    private long time = 0;
    private int rand = 0;
    private int count = 0;
    private String sign = null;
    private String data = null;

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getRand() {
        return this.rand;
    }

    public void setRand(int rand) {
        this.rand = rand;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
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

    public static XSign parse(NFCSign nfcSign) {
        if (nfcSign == null) {
            return null;
        }
        XSign xSign = new XSign();
        String data = nfcSign.getData();
        if (TextUtils.isEmpty(data) || data.length() != 32) {
            return null;
        }
        byte[] dataBytes = FormatUtils.hexStringToBytes(data);
        byte[] time = new byte[4];
        byte[] rand = new byte[4];
        System.arraycopy(dataBytes, 0, time, 0, time.length);
        System.arraycopy(dataBytes, time.length, rand, 0, rand.length);
        byte count = dataBytes[time.length + rand.length];
        xSign.setTime(NFCUtils.bytesToInt(time));
        xSign.setRand(NFCUtils.bytesToInt(rand));
        xSign.setCount(count & 255);
        xSign.setData(data);
        String sign = nfcSign.getSign();
        if (TextUtils.isEmpty(sign)) {
            return null;
        }
        xSign.setSign(sign);
        return xSign;
    }
}