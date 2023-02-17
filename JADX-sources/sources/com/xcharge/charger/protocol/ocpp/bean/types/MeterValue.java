package com.xcharge.charger.protocol.ocpp.bean.types;

import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class MeterValue extends JsonBean<MeterValue> {
    private ArrayList<SampledValue> sampledValue = new ArrayList<>();
    private String timestamp;

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public ArrayList<SampledValue> getSampledValue() {
        return this.sampledValue;
    }

    public void setSampledValue(ArrayList<SampledValue> sampledValue) {
        this.sampledValue = sampledValue;
    }
}
