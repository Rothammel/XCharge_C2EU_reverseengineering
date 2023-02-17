package com.xcharge.charger.protocol.ocpp.bean.types;

import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

public class MeterValue extends JsonBean<MeterValue> {
    private ArrayList<SampledValue> sampledValue = new ArrayList<>();
    private String timestamp;

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp2) {
        this.timestamp = timestamp2;
    }

    public ArrayList<SampledValue> getSampledValue() {
        return this.sampledValue;
    }

    public void setSampledValue(ArrayList<SampledValue> sampledValue2) {
        this.sampledValue = sampledValue2;
    }
}
