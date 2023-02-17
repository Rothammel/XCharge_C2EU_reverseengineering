package com.xcharge.charger.protocol.xmsz.bean.device;

import android.support.v4.internal.view.SupportMenu;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.common.utils.EndianUtils;

/* loaded from: classes.dex */
public class BootNotificationRequest extends XMSZMessage {
    private String SN = "";
    private String model = "";
    private byte vendorId = 0;
    private String firmwareVersion = "";
    private byte connectorCount = 1;
    private int powerRated = 0;
    private String iccid = "";
    private String imsi = "";

    public String getSN() {
        return this.SN;
    }

    public void setSN(String sN) {
        this.SN = sN;
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public byte getVendorId() {
        return this.vendorId;
    }

    public void setVendorId(byte vendorId) {
        this.vendorId = vendorId;
    }

    public String getFirmwareVersion() {
        return this.firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public byte getConnectorCount() {
        return this.connectorCount;
    }

    public void setConnectorCount(byte connectorCount) {
        this.connectorCount = connectorCount;
    }

    public int getPowerRated() {
        return this.powerRated;
    }

    public void setPowerRated(int powerRated) {
        this.powerRated = powerRated;
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

    @Override // com.xcharge.charger.protocol.xmsz.bean.XMSZMessage
    public byte[] bodyToBytes() throws Exception {
        byte[] bytes = new byte[144];
        byte[] snBytes = this.SN.getBytes(XMSZMessage.GBK_NAME);
        int snLength = snBytes.length > 25 ? 25 : snBytes.length;
        System.arraycopy(snBytes, 0, bytes, 0, snLength);
        byte[] modelBytes = this.model.getBytes(XMSZMessage.GBK_NAME);
        int modelLength = modelBytes.length > 20 ? 20 : modelBytes.length;
        System.arraycopy(modelBytes, 0, bytes, 26, modelLength);
        bytes[47] = this.vendorId;
        byte[] firmwareVersionBytes = this.firmwareVersion.getBytes(XMSZMessage.GBK_NAME);
        int firmwareVersionLength = firmwareVersionBytes.length > 50 ? 50 : firmwareVersionBytes.length;
        System.arraycopy(firmwareVersionBytes, 0, bytes, 48, firmwareVersionLength);
        bytes[99] = this.connectorCount;
        System.arraycopy(EndianUtils.shortToLittleBytes((short) (this.powerRated & SupportMenu.USER_MASK)), 0, bytes, 100, 2);
        byte[] iccidBytes = this.iccid.getBytes(XMSZMessage.GBK_NAME);
        int iccidLength = iccidBytes.length > 20 ? 20 : iccidBytes.length;
        System.arraycopy(iccidBytes, 0, bytes, 102, iccidLength);
        byte[] imsiBytes = this.imsi.getBytes(XMSZMessage.GBK_NAME);
        int imsiLength = imsiBytes.length > 20 ? 20 : imsiBytes.length;
        System.arraycopy(imsiBytes, 0, bytes, 123, imsiLength);
        return bytes;
    }

    @Override // com.xcharge.charger.protocol.xmsz.bean.XMSZMessage
    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}