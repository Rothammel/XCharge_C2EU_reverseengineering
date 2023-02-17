package com.xcharge.charger.protocol.anyo.bean.request;

import android.support.v4.internal.view.SupportMenu;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.common.utils.EndianUtils;
import java.nio.charset.Charset;
import org.apache.commons.lang3.CharEncoding;

/* loaded from: classes.dex */
public class ReportNetworkInfoRequest extends AnyoMessage {
    public static final String DEFAULT_STR = "";
    private byte netType = 0;
    private int mcc = 0;
    private int mnc = 0;
    private int lac = 0;
    private int ci = 0;
    private String imei = "";
    private long ram = 0;
    private long rom = 0;
    private long mac = 0;
    private String iccid = "";
    private String imsi = "";

    public byte getNetType() {
        return this.netType;
    }

    public void setNetType(byte netType) {
        this.netType = netType;
    }

    public int getMcc() {
        return this.mcc;
    }

    public void setMcc(int mcc) {
        this.mcc = mcc;
    }

    public int getMnc() {
        return this.mnc;
    }

    public void setMnc(int mnc) {
        this.mnc = mnc;
    }

    public int getLac() {
        return this.lac;
    }

    public void setLac(int lac) {
        this.lac = lac;
    }

    public int getCi() {
        return this.ci;
    }

    public void setCi(int ci) {
        this.ci = ci;
    }

    public String getImei() {
        return this.imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public long getRam() {
        return this.ram;
    }

    public void setRam(long ram) {
        this.ram = ram;
    }

    public long getRom() {
        return this.rom;
    }

    public void setRom(long rom) {
        this.rom = rom;
    }

    public long getMac() {
        return this.mac;
    }

    public void setMac(long mac) {
        this.mac = mac;
    }

    public String getIccid() {
        return this.iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getImsi() {
        return this.imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public byte[] bodyToBytes() throws Exception {
        Charset utf8 = Charset.forName(CharEncoding.UTF_8);
        byte[] bytes = new byte[78];
        bytes[0] = this.netType;
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.mcc & SupportMenu.USER_MASK)), 0, bytes, 1, 2);
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.mnc & SupportMenu.USER_MASK)), 0, bytes, 3, 2);
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.lac & SupportMenu.USER_MASK)), 0, bytes, 5, 2);
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.ci & SupportMenu.USER_MASK)), 0, bytes, 7, 2);
        byte[] imeiBytes = this.imei.getBytes(utf8);
        int imeiLength = imeiBytes.length > 16 ? 16 : imeiBytes.length;
        System.arraycopy(imeiBytes, 0, bytes, 9, imeiLength);
        System.arraycopy(EndianUtils.longToLittleBytes(this.ram & 281474976710655L, 6), 0, bytes, 25, 6);
        System.arraycopy(EndianUtils.longToLittleBytes(this.rom & 281474976710655L, 6), 0, bytes, 31, 6);
        System.arraycopy(EndianUtils.longToLittleBytes(this.mac & 281474976710655L, 6), 0, bytes, 37, 6);
        byte[] iccidBytes = this.iccid.getBytes(utf8);
        int iccidLength = iccidBytes.length > 20 ? 20 : iccidBytes.length;
        System.arraycopy(iccidBytes, 0, bytes, 43, iccidLength);
        byte[] imsiBytes = this.imsi.getBytes(utf8);
        int imsiLength = imsiBytes.length > 15 ? 15 : imsiBytes.length;
        System.arraycopy(imsiBytes, 0, bytes, 63, imsiLength);
        return bytes;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}