package com.xcharge.charger.protocol.ocpp.bean.types;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class SampledValue extends JsonBean<SampledValue> {
    private String phase;
    private String value;
    private String context = ReadingContext.SamplePeriodic;
    private String format = ValueFormat.Raw;
    private String measurand = Measurand.EnergyActiveImportRegister;
    private String location = Location.Outlet;
    private String unit = UnitOfMeasure.Wh;

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getContext() {
        return this.context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getFormat() {
        return this.format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getMeasurand() {
        return this.measurand;
    }

    public void setMeasurand(String measurand) {
        this.measurand = measurand;
    }

    public String getPhase() {
        return this.phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUnit() {
        return this.unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}