package com.xcharge.charger.protocol.monitor.bean.dap;

import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;
import java.util.HashMap;

public class DAPFeeRate extends JsonBean<DAPFeeRate> {
    public static final String CNY = "CNY";
    public static final String DELAY_PRICE_UNIT_MINUTE = "minute";
    public static final String PARK_PRICE_UNIT_DAY = "day";
    public static final String PARK_PRICE_UNIT_HOUR = "hour";
    public static final String PARK_PRICE_UNIT_ORDER = "order";
    public static final String SERVICE_PRICE_UNIT_DEGREE = "degree";
    public static final String SERVICE_PRICE_UNIT_ORDER = "order";
    private String currency = "CNY";
    private ArrayList<HashMap<String, Object>> delay_price = null;
    private String delay_unit = DELAY_PRICE_UNIT_MINUTE;
    private String fee_rate_id = null;
    private ArrayList<HashMap<String, Object>> park_price = null;
    private String park_unit = PARK_PRICE_UNIT_HOUR;
    private ArrayList<HashMap<String, Object>> power_price = null;
    private ArrayList<HashMap<String, Object>> service_price = null;
    private String service_unit = SERVICE_PRICE_UNIT_DEGREE;

    public String getFee_rate_id() {
        return this.fee_rate_id;
    }

    public void setFee_rate_id(String fee_rate_id2) {
        this.fee_rate_id = fee_rate_id2;
    }

    public ArrayList<HashMap<String, Object>> getPower_price() {
        return this.power_price;
    }

    public void setPower_price(ArrayList<HashMap<String, Object>> power_price2) {
        this.power_price = power_price2;
    }

    public ArrayList<HashMap<String, Object>> getService_price() {
        return this.service_price;
    }

    public void setService_price(ArrayList<HashMap<String, Object>> service_price2) {
        this.service_price = service_price2;
    }

    public ArrayList<HashMap<String, Object>> getDelay_price() {
        return this.delay_price;
    }

    public void setDelay_price(ArrayList<HashMap<String, Object>> delay_price2) {
        this.delay_price = delay_price2;
    }

    public ArrayList<HashMap<String, Object>> getPark_price() {
        return this.park_price;
    }

    public void setPark_price(ArrayList<HashMap<String, Object>> park_price2) {
        this.park_price = park_price2;
    }

    public String getService_unit() {
        return this.service_unit;
    }

    public void setService_unit(String service_unit2) {
        this.service_unit = service_unit2;
    }

    public String getDelay_unit() {
        return this.delay_unit;
    }

    public void setDelay_unit(String delay_unit2) {
        this.delay_unit = delay_unit2;
    }

    public String getPark_unit() {
        return this.park_unit;
    }

    public void setPark_unit(String park_unit2) {
        this.park_unit = park_unit2;
    }
}
