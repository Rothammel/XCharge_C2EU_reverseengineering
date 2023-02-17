package com.xcharge.charger.protocol.anyo.bean.request;

import android.support.p000v4.internal.view.SupportMenu;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.common.utils.EndianUtils;
import java.nio.charset.Charset;
import org.apache.commons.lang3.CharEncoding;

public class ReportNetworkInfoRequest extends AnyoMessage {
    public static final String DEFAULT_STR = "";

    /* renamed from: ci */
    private int f76ci = 0;
    private String iccid = "";
    private String imei = "";
    private String imsi = "";
    private int lac = 0;
    private long mac = 0;
    private int mcc = 0;
    private int mnc = 0;
    private byte netType = 0;
    private long ram = 0;
    private long rom = 0;

    public byte getNetType() {
        return this.netType;
    }

    public void setNetType(byte netType2) {
        this.netType = netType2;
    }

    public int getMcc() {
        return this.mcc;
    }

    public void setMcc(int mcc2) {
        this.mcc = mcc2;
    }

    public int getMnc() {
        return this.mnc;
    }

    public void setMnc(int mnc2) {
        this.mnc = mnc2;
    }

    public int getLac() {
        return this.lac;
    }

    public void setLac(int lac2) {
        this.lac = lac2;
    }

    public int getCi() {
        return this.f76ci;
    }

    public void setCi(int ci) {
        this.f76ci = ci;
    }

    public String getImei() {
        return this.imei;
    }

    public void setImei(String imei2) {
        this.imei = imei2;
    }

    public long getRam() {
        return this.ram;
    }

    public void setRam(long ram2) {
        this.ram = ram2;
    }

    public long getRom() {
        return this.rom;
    }

    public void setRom(long rom2) {
        this.rom = rom2;
    }

    public long getMac() {
        return this.mac;
    }

    public void setMac(long mac2) {
        this.mac = mac2;
    }

    public String getIccid() {
        return this.iccid;
    }

    public void setIccid(String iccid2) {
        this.iccid = iccid2;
    }

    public String getImsi() {
        return this.imsi;
    }

    public void setImsi(String imsi2) {
        this.imsi = imsi2;
    }

    public byte[] bodyToBytes() throws Exception {
        Charset utf8 = Charset.forName(CharEncoding.UTF_8);
        byte[] bytes = new byte[78];
        bytes[0] = this.netType;
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.mcc & SupportMenu.USER_MASK)), 0, bytes, 1, 2);
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.mnc & SupportMenu.USER_MASK)), 0, bytes, 3, 2);
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.lac & SupportMenu.USER_MASK)), 0, bytes, 5, 2);
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.f76ci & SupportMenu.USER_MASK)), 0, bytes, 7, 2);
        byte[] imeiBytes = this.imei.getBytes(utf8);
        System.arraycopy(imeiBytes, 0, bytes, 9, imeiBytes.length > 16 ? 16 : imeiBytes.length);
        System.arraycopy(EndianUtils.longToLittleBytes(this.ram & 281474976710655L, 6), 0, bytes, 25, 6);
        System.arraycopy(EndianUtils.longToLittleBytes(this.rom & 281474976710655L, 6), 0, bytes, 31, 6);
        System.arraycopy(EndianUtils.longToLittleBytes(this.mac & 281474976710655L, 6), 0, bytes, 37, 6);
        byte[] iccidBytes = this.iccid.getBytes(utf8);
        System.arraycopy(iccidBytes, 0, bytes, 43, iccidBytes.length > 20 ? 20 : iccidBytes.length);
        byte[] imsiBytes = this.imsi.getBytes(utf8);
        System.arraycopy(imsiBytes, 0, bytes, 63, imsiBytes.length > 15 ? 15 : imsiBytes.length);
        return bytes;
    }

    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
