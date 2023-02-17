package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

/* loaded from: classes.dex */
public class DAPFeeRates extends JsonBean<DAPFeeRates> {
    private HashMap<String, DAPFeeRate> fee_rates = null;
    private String default_fee_rate_id = null;

    public HashMap<String, DAPFeeRate> getFee_rates() {
        return this.fee_rates;
    }

    public void setFee_rates(HashMap<String, DAPFeeRate> fee_rates) {
        this.fee_rates = fee_rates;
    }

    public String getDefault_fee_rate_id() {
        return this.default_fee_rate_id;
    }

    public void setDefault_fee_rate_id(String default_fee_rate_id) {
        this.default_fee_rate_id = default_fee_rate_id;
    }
}