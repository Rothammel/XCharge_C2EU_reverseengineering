package com.xcharge.charger.protocol.anyo.bean.request;

import android.util.Log;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.common.utils.EndianUtils;
import java.nio.charset.Charset;
import java.util.InputMismatchException;
import org.apache.commons.lang3.CharEncoding;

public class LoginRequest extends AnyoMessage {
    public static final String DEFAULT_STR = "";
    private int magicNum = 0;
    private String operatorInfo = "";
    private String pileInfo = "";
    private String pileNo = "";
    private byte pileType = 0;
    private byte provider = 0;
    private byte rand = 0;

    public String getPileNo() {
        return this.pileNo;
    }

    public void setPileNo(String pileNo2) {
        this.pileNo = pileNo2;
    }

    public byte getPileType() {
        return this.pileType;
    }

    public void setPileType(byte pileType2) {
        this.pileType = pileType2;
    }

    public byte getProvider() {
        return this.provider;
    }

    public void setProvider(byte provider2) {
        this.provider = provider2;
    }

    public int getMagicNum() {
        return this.magicNum;
    }

    public void setMagicNum(int magicNum2) {
        this.magicNum = magicNum2;
    }

    public String getPileInfo() {
        return this.pileInfo;
    }

    public void setPileInfo(String pileInfo2) {
        this.pileInfo = pileInfo2;
    }

    public String getOperatorInfo() {
        return this.operatorInfo;
    }

    public void setOperatorInfo(String operatorInfo2) {
        this.operatorInfo = operatorInfo2;
    }

    public byte getRand() {
        return this.rand;
    }

    public void setRand(byte rand2) {
        this.rand = rand2;
    }

    public byte[] bodyToBytes() throws Exception {
        Charset utf8 = Charset.forName(CharEncoding.UTF_8);
        byte[] pileNoBytes = this.pileNo.getBytes(utf8);
        if (pileNoBytes.length > 20) {
            Log.e("LoginRequest.bodyToBytes", "illegal pile no length in message: " + toJson());
            throw new InputMismatchException();
        }
        byte[] bytes = new byte[67];
        System.arraycopy(pileNoBytes, 0, bytes, 0, pileNoBytes.length);
        bytes[20] = this.pileType;
        bytes[21] = this.provider;
        System.arraycopy(EndianUtils.intToLittleBytes(this.magicNum), 0, bytes, 22, 4);
        byte[] pileInfoBytes = this.pileInfo.getBytes(utf8);
        System.arraycopy(pileInfoBytes, 0, bytes, 26, pileInfoBytes.length > 20 ? 20 : pileInfoBytes.length);
        byte[] operatorInfoBytes = this.operatorInfo.getBytes(utf8);
        System.arraycopy(operatorInfoBytes, 0, bytes, 46, operatorInfoBytes.length > 20 ? 20 : operatorInfoBytes.length);
        bytes[66] = this.rand;
        return bytes;
    }

    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
