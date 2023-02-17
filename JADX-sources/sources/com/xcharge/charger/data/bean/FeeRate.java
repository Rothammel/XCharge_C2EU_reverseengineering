package com.xcharge.charger.data.bean;

import com.xcharge.charger.data.bean.type.DELAY_PRICE_UNIT;
import com.xcharge.charger.data.bean.type.SERVICE_PRICE_UNIT;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;
import java.util.HashMap;

/* loaded from: classes.dex */
public class FeeRate extends JsonBean<FeeRate> {
    public static final String CNY = "CNY";
    private String feeRateId = null;
    private ArrayList<HashMap<String, Object>> powerPrice = null;
    private ArrayList<HashMap<String, Object>> servicePrice = null;
    private ArrayList<HashMap<String, Object>> delayPrice = null;
    private SERVICE_PRICE_UNIT serviceUnit = SERVICE_PRICE_UNIT.degree;
    private DELAY_PRICE_UNIT delayUnit = DELAY_PRICE_UNIT.minute;
    private String currency = null;

    public ArrayList<HashMap<String, Object>> getPowerPrice() {
        return this.powerPrice;
    }

    public void setPowerPrice(ArrayList<HashMap<String, Object>> powerPrice) {
        this.powerPrice = powerPrice;
    }

    public String getFeeRateId() {
        return this.feeRateId;
    }

    public void setFeeRateId(String feeRateId) {
        this.feeRateId = feeRateId;
    }

    public ArrayList<HashMap<String, Object>> getServicePrice() {
        return this.servicePrice;
    }

    public void setServicePrice(ArrayList<HashMap<String, Object>> servicePrice) {
        this.servicePrice = servicePrice;
    }

    public ArrayList<HashMap<String, Object>> getDelayPrice() {
        return this.delayPrice;
    }

    public void setDelayPrice(ArrayList<HashMap<String, Object>> delayPrice) {
        this.delayPrice = delayPrice;
    }

    public SERVICE_PRICE_UNIT getServiceUnit() {
        return this.serviceUnit;
    }

    public void setServiceUnit(SERVICE_PRICE_UNIT serviceUnit) {
        this.serviceUnit = serviceUnit;
    }

    public DELAY_PRICE_UNIT getDelayUnit() {
        return this.delayUnit;
    }

    public void setDelayUnit(DELAY_PRICE_UNIT delayUnit) {
        this.delayUnit = delayUnit;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
