package com.xcharge.charger.data.bean;

import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

/* loaded from: classes.dex */
public class PortFeeRate extends JsonBean<PortFeeRate> {
    private HashMap<String, FeeRate> feeRates = null;
    private String activeFeeRateId = null;

    public HashMap<String, FeeRate> getFeeRates() {
        return this.feeRates;
    }

    public void setFeeRates(HashMap<String, FeeRate> feeRates) {
        this.feeRates = feeRates;
    }

    public String getActiveFeeRateId() {
        return this.activeFeeRateId;
    }

    public void setActiveFeeRateId(String activeFeeRateId) {
        this.activeFeeRateId = activeFeeRateId;
    }
}