package com.xcharge.charger.data.bean.setting;

import com.xcharge.charger.data.bean.PortFeeRate;
import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

public class FeeRateSetting extends JsonBean<FeeRateSetting> {
    private HashMap<String, PortFeeRate> portsFeeRate = null;

    public HashMap<String, PortFeeRate> getPortsFeeRate() {
        return this.portsFeeRate;
    }

    public void setPortsFeeRate(HashMap<String, PortFeeRate> portsFeeRate2) {
        this.portsFeeRate = portsFeeRate2;
    }
}
