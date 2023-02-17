package com.xcharge.charger.protocol.ocpp.bean.device;

import com.xcharge.common.bean.JsonBean;

public class BootNotificationReq extends JsonBean<BootNotificationReq> {
    private String chargeBoxSerialNumber;
    private String chargePointModel;
    private String chargePointSerialNumber;
    private String chargePointVendor;
    private String firmwareVersion;
    private String iccid;
    private String imsi;
    private String meteType;
    private String meterSerialNumber;

    public String getChargeBoxSerialNumber() {
        return this.chargeBoxSerialNumber;
    }

    public void setChargeBoxSerialNumber(String chargeBoxSerialNumber2) {
        this.chargeBoxSerialNumber = chargeBoxSerialNumber2;
    }

    public String getChargePointModel() {
        return this.chargePointModel;
    }

    public void setChargePointModel(String chargePointModel2) {
        this.chargePointModel = chargePointModel2;
    }

    public String getChargePointSerialNumber() {
        return this.chargePointSerialNumber;
    }

    public void setChargePointSerialNumber(String chargePointSerialNumber2) {
        this.chargePointSerialNumber = chargePointSerialNumber2;
    }

    public String getChargePointVendor() {
        return this.chargePointVendor;
    }

    public void setChargePointVendor(String chargePointVendor2) {
        this.chargePointVendor = chargePointVendor2;
    }

    public String getFirmwareVersion() {
        return this.firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion2) {
        this.firmwareVersion = firmwareVersion2;
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

    public String getMeterSerialNumber() {
        return this.meterSerialNumber;
    }

    public void setMeterSerialNumber(String meterSerialNumber2) {
        this.meterSerialNumber = meterSerialNumber2;
    }

    public String getMeteType() {
        return this.meteType;
    }

    public void setMeteType(String meteType2) {
        this.meteType = meteType2;
    }
}
