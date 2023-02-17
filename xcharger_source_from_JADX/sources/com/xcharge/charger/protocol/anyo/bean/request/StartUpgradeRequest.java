package com.xcharge.charger.protocol.anyo.bean.request;

import android.support.p000v4.view.MotionEventCompat;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.common.utils.EndianUtils;
import com.xcharge.common.utils.FormatUtils;
import java.nio.charset.Charset;
import java.util.InputMismatchException;
import org.apache.commons.lang3.CharEncoding;

public class StartUpgradeRequest extends AnyoMessage {
    public static final byte TYPE_UPGRADE_FTP = 2;
    public static final byte TYPE_UPGRADE_HTTP = 3;
    public static final byte TYPE_UPGRADE_TCP = 1;
    private int checksum = 0;
    private long packLength = 0;
    private byte primaryVersion = 0;
    private byte provider = 0;
    private byte reviseVersion = 0;
    private byte secondaryVersion = 0;
    private long sectionLength = 0;
    private String upgradeAddr = null;
    private int upgradeAddrLength = 0;
    private byte upgradeType = 0;

    public byte getProvider() {
        return this.provider;
    }

    public void setProvider(byte provider2) {
        this.provider = provider2;
    }

    public byte getPrimaryVersion() {
        return this.primaryVersion;
    }

    public void setPrimaryVersion(byte primaryVersion2) {
        this.primaryVersion = primaryVersion2;
    }

    public byte getSecondaryVersion() {
        return this.secondaryVersion;
    }

    public void setSecondaryVersion(byte secondaryVersion2) {
        this.secondaryVersion = secondaryVersion2;
    }

    public byte getReviseVersion() {
        return this.reviseVersion;
    }

    public void setReviseVersion(byte reviseVersion2) {
        this.reviseVersion = reviseVersion2;
    }

    public long getPackLength() {
        return this.packLength;
    }

    public void setPackLength(long packLength2) {
        this.packLength = packLength2;
    }

    public int getChecksum() {
        return this.checksum;
    }

    public void setChecksum(int checksum2) {
        this.checksum = checksum2;
    }

    public long getSectionLength() {
        return this.sectionLength;
    }

    public void setSectionLength(long sectionLength2) {
        this.sectionLength = sectionLength2;
    }

    public byte getUpgradeType() {
        return this.upgradeType;
    }

    public void setUpgradeType(byte upgradeType2) {
        this.upgradeType = upgradeType2;
    }

    public int getUpgradeAddrLength() {
        return this.upgradeAddrLength;
    }

    public void setUpgradeAddrLength(int upgradeAddrLength2) {
        this.upgradeAddrLength = upgradeAddrLength2;
    }

    public String getUpgradeAddr() {
        return this.upgradeAddr;
    }

    public void setUpgradeAddr(String upgradeAddr2) {
        this.upgradeAddr = upgradeAddr2;
    }

    public byte[] bodyToBytes() throws Exception {
        Charset utf8 = Charset.forName(CharEncoding.UTF_8);
        if (this.upgradeAddrLength == 0 || TextUtils.isEmpty(this.upgradeAddr)) {
            Log.e("StartUpgradeRequest.bodyToBytes", "illegal upgrade address in message: " + toJson());
            throw new InputMismatchException();
        }
        byte[] upgradeAddrBytes = this.upgradeAddr.getBytes(utf8);
        if (this.upgradeAddrLength != upgradeAddrBytes.length) {
            Log.e("StartUpgradeRequest.bodyToBytes", "upgrade address length is not matched in message: " + toJson());
            throw new InputMismatchException();
        }
        byte[] bytes = new byte[(this.upgradeAddrLength + 18)];
        bytes[0] = this.provider;
        bytes[1] = this.reviseVersion;
        bytes[2] = this.secondaryVersion;
        bytes[3] = this.primaryVersion;
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.packLength & XMSZHead.ID_BROADCAST)), 0, bytes, 4, 4);
        System.arraycopy(EndianUtils.intToLittleBytes(this.checksum), 0, bytes, 8, 4);
        System.arraycopy(EndianUtils.intToLittleBytes((int) (this.sectionLength & XMSZHead.ID_BROADCAST)), 0, bytes, 12, 4);
        bytes[16] = this.upgradeType;
        bytes[17] = (byte) (this.upgradeAddrLength & MotionEventCompat.ACTION_MASK);
        System.arraycopy(upgradeAddrBytes, 0, bytes, 18, this.upgradeAddrLength & MotionEventCompat.ACTION_MASK);
        return bytes;
    }

    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        Charset utf8 = Charset.forName(CharEncoding.UTF_8);
        if (bytes.length <= 18) {
            Log.e("StartUpgradeRequest.bodyFromBytes", "body length must more than 18 !!! body: " + FormatUtils.bytesToHexString(bytes));
            throw new IllegalArgumentException();
        }
        this.upgradeAddrLength = bytes[17] & 255;
        if (this.upgradeAddrLength == 0 || bytes.length != this.upgradeAddrLength + 18) {
            Log.e("StartUpgradeRequest.bodyFromBytes", "upgrade address length is not matched in body: " + FormatUtils.bytesToHexString(bytes));
            throw new IllegalArgumentException();
        }
        this.provider = bytes[0];
        this.reviseVersion = bytes[1];
        this.secondaryVersion = bytes[2];
        this.primaryVersion = bytes[3];
        this.packLength = ((long) EndianUtils.littleBytesToInt(new byte[]{bytes[4], bytes[5], bytes[6], bytes[7]})) & XMSZHead.ID_BROADCAST;
        this.checksum = EndianUtils.littleBytesToInt(new byte[]{bytes[8], bytes[9], bytes[10], bytes[11]});
        this.sectionLength = ((long) EndianUtils.littleBytesToInt(new byte[]{bytes[12], bytes[13], bytes[14], bytes[15]})) & XMSZHead.ID_BROADCAST;
        this.upgradeType = bytes[16];
        byte[] upgradeAddrBytes = new byte[this.upgradeAddrLength];
        for (int i = 0; i < this.upgradeAddrLength; i++) {
            upgradeAddrBytes[i] = bytes[i + 18];
        }
        this.upgradeAddr = new String(upgradeAddrBytes, utf8);
        return this;
    }
}
