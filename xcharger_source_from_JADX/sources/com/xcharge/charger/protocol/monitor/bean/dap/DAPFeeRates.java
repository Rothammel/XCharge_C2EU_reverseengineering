package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

public class DAPFeeRates extends JsonBean<DAPFeeRates> {
    private String default_fee_rate_id = null;
    private HashMap<String, DAPFeeRate> fee_rates = null;

    public HashMap<String, DAPFeeRate> getFee_rates() {
        return this.fee_rates;
    }

    public void setFee_rates(HashMap<String, DAPFeeRate> fee_rates2) {
        this.fee_rates = fee_rates2;
    }

    public String getDefault_fee_rate_id() {
        return this.default_fee_rate_id;
    }

    public void setDefault_fee_rate_id(String default_fee_rate_id2) {
        this.default_fee_rate_id = default_fee_rate_id2;
    }
}
