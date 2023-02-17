package com.xcharge.charger.protocol.xmsz.bean.device;

import android.support.p000v4.internal.view.SupportMenu;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.common.utils.EndianUtils;

public class BootNotificationRequest extends XMSZMessage {

    /* renamed from: SN */
    private String f128SN = "";
    private byte connectorCount = 1;
    private String firmwareVersion = "";
    private String iccid = "";
    private String imsi = "";
    private String model = "";
    private int powerRated = 0;
    private byte vendorId = 0;

    public String getSN() {
        return this.f128SN;
    }

    public void setSN(String sN) {
        this.f128SN = sN;
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(String model2) {
        this.model = model2;
    }

    public byte getVendorId() {
        return this.vendorId;
    }

    public void setVendorId(byte vendorId2) {
        this.vendorId = vendorId2;
    }

    public String getFirmwareVersion() {
        return this.firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion2) {
        this.firmwareVersion = firmwareVersion2;
    }

    public byte getConnectorCount() {
        return this.connectorCount;
    }

    public void setConnectorCount(byte connectorCount2) {
        this.connectorCount = connectorCount2;
    }

    public int getPowerRated() {
        return this.powerRated;
    }

    public void setPowerRated(int powerRated2) {
        this.powerRated = powerRated2;
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
        byte[] bytes = new byte[144];
        byte[] snBytes = this.f128SN.getBytes(XMSZMessage.GBK_NAME);
        System.arraycopy(snBytes, 0, bytes, 0, snBytes.length > 25 ? 25 : snBytes.length);
        byte[] modelBytes = this.model.getBytes(XMSZMessage.GBK_NAME);
        System.arraycopy(modelBytes, 0, bytes, 26, modelBytes.length > 20 ? 20 : modelBytes.length);
        bytes[47] = this.vendorId;
        byte[] firmwareVersionBytes = this.firmwareVersion.getBytes(XMSZMessage.GBK_NAME);
        System.arraycopy(firmwareVersionBytes, 0, bytes, 48, firmwareVersionBytes.length > 50 ? 50 : firmwareVersionBytes.length);
        bytes[99] = this.connectorCount;
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.powerRated & SupportMenu.USER_MASK)), 0, bytes, 100, 2);
        byte[] iccidBytes = this.iccid.getBytes(XMSZMessage.GBK_NAME);
        System.arraycopy(iccidBytes, 0, bytes, 102, iccidBytes.length > 20 ? 20 : iccidBytes.length);
        byte[] imsiBytes = this.imsi.getBytes(XMSZMessage.GBK_NAME);
        System.arraycopy(imsiBytes, 0, bytes, 123, imsiBytes.length > 20 ? 20 : imsiBytes.length);
        return bytes;
    }

    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
