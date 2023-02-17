package com.xcharge.charger.data.bean;

import com.xcharge.charger.data.bean.type.DELAY_PRICE_UNIT;
import com.xcharge.charger.data.bean.type.SERVICE_PRICE_UNIT;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;
import java.util.HashMap;

public class FeeRate extends JsonBean<FeeRate> {
    public static final String CNY = "CNY";
    private String currency = null;
    private ArrayList<HashMap<String, Object>> delayPrice = null;
    private DELAY_PRICE_UNIT delayUnit = DELAY_PRICE_UNIT.minute;
    private String feeRateId = null;
    private ArrayList<HashMap<String, Object>> powerPrice = null;
    private ArrayList<HashMap<String, Object>> servicePrice = null;
    private SERVICE_PRICE_UNIT serviceUnit = SERVICE_PRICE_UNIT.degree;

    public ArrayList<HashMap<String, Object>> getPowerPrice() {
        return this.powerPrice;
    }

    public void setPowerPrice(ArrayList<HashMap<String, Object>> powerPrice2) {
        this.powerPrice = powerPrice2;
    }

    public String getFeeRateId() {
        return this.feeRateId;
    }

    public void setFeeRateId(String feeRateId2) {
        this.feeRateId = feeRateId2;
    }

    public ArrayList<HashMap<String, Object>> getServicePrice() {
        return this.servicePrice;
    }

    public void setServicePrice(ArrayList<HashMap<String, Object>> servicePrice2) {
        this.servicePrice = servicePrice2;
    }

    public ArrayList<HashMap<String, Object>> getDelayPrice() {
        return this.delayPrice;
    }

    public void setDelayPrice(ArrayList<HashMap<String, Object>> delayPrice2) {
        this.delayPrice = delayPrice2;
    }

    public SERVICE_PRICE_UNIT getServiceUnit() {
        return this.serviceUnit;
    }

    public void setServiceUnit(SERVICE_PRICE_UNIT serviceUnit2) {
        this.serviceUnit = serviceUnit2;
    }

    public DELAY_PRICE_UNIT getDelayUnit() {
        return this.delayUnit;
    }

    public void setDelayUnit(DELAY_PRICE_UNIT delayUnit2) {
        this.delayUnit = delayUnit2;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency2) {
        this.currency = currency2;
    }
}
