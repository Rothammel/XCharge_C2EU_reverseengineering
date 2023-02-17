package com.xcharge.charger.data.bean;

import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

public class PortFeeRate extends JsonBean<PortFeeRate> {
    private String activeFeeRateId = null;
    private HashMap<String, FeeRate> feeRates = null;

    public HashMap<String, FeeRate> getFeeRates() {
        return this.feeRates;
    }

    public void setFeeRates(HashMap<String, FeeRate> feeRates2) {
        this.feeRates = feeRates2;
    }

    public String getActiveFeeRateId() {
        return this.activeFeeRateId;
    }

    public void setActiveFeeRateId(String activeFeeRateId2) {
        this.activeFeeRateId = activeFeeRateId2;
    }
}
