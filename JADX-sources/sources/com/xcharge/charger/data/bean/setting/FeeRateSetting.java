package com.xcharge.charger.data.bean.setting;

import com.xcharge.charger.data.bean.PortFeeRate;
import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

/* loaded from: classes.dex */
public class FeeRateSetting extends JsonBean<FeeRateSetting> {
    private HashMap<String, PortFeeRate> portsFeeRate = null;

    public HashMap<String, PortFeeRate> getPortsFeeRate() {
        return this.portsFeeRate;
    }

    public void setPortsFeeRate(HashMap<String, PortFeeRate> portsFeeRate) {
        this.portsFeeRate = portsFeeRate;
    }
}
