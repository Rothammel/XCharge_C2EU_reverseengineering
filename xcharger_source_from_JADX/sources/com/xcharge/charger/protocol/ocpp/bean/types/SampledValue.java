package com.xcharge.charger.protocol.ocpp.bean.types;

import com.xcharge.common.bean.JsonBean;

public class SampledValue extends JsonBean<SampledValue> {
    private String context = ReadingContext.SamplePeriodic;
    private String format = ValueFormat.Raw;
    private String location = Location.Outlet;
    private String measurand = Measurand.EnergyActiveImportRegister;
    private String phase;
    private String unit = UnitOfMeasure.f124Wh;
    private String value;

    public String getValue() {
        return this.value;
    }

    public void setValue(String value2) {
        this.value = value2;
    }

    public String getContext() {
        return this.context;
    }

    public void setContext(String context2) {
        this.context = context2;
    }

    public String getFormat() {
        return this.format;
    }

    public void setFormat(String format2) {
        this.format = format2;
    }

    public String getMeasurand() {
        return this.measurand;
    }

    public void setMeasurand(String measurand2) {
        this.measurand = measurand2;
    }

    public String getPhase() {
        return this.phase;
    }

    public void setPhase(String phase2) {
        this.phase = phase2;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location2) {
        this.location = location2;
    }

    public String getUnit() {
        return this.unit;
    }

    public void setUnit(String unit2) {
        this.unit = unit2;
    }
}
