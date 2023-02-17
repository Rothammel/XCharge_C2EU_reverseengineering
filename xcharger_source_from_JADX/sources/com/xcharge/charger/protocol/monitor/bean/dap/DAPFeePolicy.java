package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.common.bean.JsonBean;

public class DAPFeePolicy extends JsonBean<DAPFeePolicy> {
    private DAPFeeRates fee_policy;

    public DAPFeeRates getFee_policy() {
        return this.fee_policy;
    }

    public void setFee_policy(DAPFeeRates fee_policy2) {
        this.fee_policy = fee_policy2;
    }
}
