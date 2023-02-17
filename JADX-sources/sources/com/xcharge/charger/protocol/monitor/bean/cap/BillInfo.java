package com.xcharge.charger.protocol.monitor.bean.cap;

import com.xcharge.charger.protocol.monitor.bean.YZXDCAPError;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;
import java.util.HashMap;

/* loaded from: classes.dex */
public class BillInfo extends JsonBean<BillInfo> {
    private String bill_id = null;
    private String user_type = null;
    private String user_code = null;
    private String port = null;
    private ChargeStopCondition stop_condition = null;
    private Long balance = null;
    private YZXDCAPError fin_cause = null;
    private String local_charge_id = null;
    private Integer total_time = null;
    private Long init_time = null;
    private Long fin_time = null;
    private Long start_time = null;
    private Long stop_time = null;
    private YZXDCAPError stop_cause = null;
    private Long delay_start = null;
    private Integer total_delay = null;
    private ArrayList<HashMap<String, Object>> delay_info = null;
    private Double start_ammeter = null;
    private Double stop_ammeter = null;
    private Double total_power = null;
    private ArrayList<HashMap<String, Object>> power_info = null;
    private ArrayList<HashMap<String, Object>> service_info = null;
    private String fee_rate_id = null;
    private Integer total_fee = null;
    private Integer power_fee = null;
    private Integer service_fee = null;
    private Integer delay_fee = null;
    private Integer park_fee = null;
    private Integer total_park = null;
    private ArrayList<HashMap<String, Object>> park_info = null;
    private boolean balance_flag = false;
    private Long balance_time = null;
    private boolean pay_flag = false;
    private Long pay_time = null;
    private Long pay_balance = null;

    public String getBill_id() {
        return this.bill_id;
    }

    public void setBill_id(String bill_id) {
        this.bill_id = bill_id;
    }

    public String getUser_type() {
        return this.user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    public String getUser_code() {
        return this.user_code;
    }

    public void setUser_code(String user_code) {
        this.user_code = user_code;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public ChargeStopCondition getStop_condition() {
        return this.stop_condition;
    }

    public void setStop_condition(ChargeStopCondition stop_condition) {
        this.stop_condition = stop_condition;
    }

    public Long getBalance() {
        return this.balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public YZXDCAPError getFin_cause() {
        return this.fin_cause;
    }

    public void setFin_cause(YZXDCAPError fin_cause) {
        this.fin_cause = fin_cause;
    }

    public String getLocal_charge_id() {
        return this.local_charge_id;
    }

    public void setLocal_charge_id(String local_charge_id) {
        this.local_charge_id = local_charge_id;
    }

    public Integer getTotal_time() {
        return this.total_time;
    }

    public void setTotal_time(Integer total_time) {
        this.total_time = total_time;
    }

    public Long getInit_time() {
        return this.init_time;
    }

    public void setInit_time(Long init_time) {
        this.init_time = init_time;
    }

    public Long getFin_time() {
        return this.fin_time;
    }

    public void setFin_time(Long fin_time) {
        this.fin_time = fin_time;
    }

    public Long getStart_time() {
        return this.start_time;
    }

    public void setStart_time(Long start_time) {
        this.start_time = start_time;
    }

    public Long getStop_time() {
        return this.stop_time;
    }

    public void setStop_time(Long stop_time) {
        this.stop_time = stop_time;
    }

    public YZXDCAPError getStop_cause() {
        return this.stop_cause;
    }

    public void setStop_cause(YZXDCAPError stop_cause) {
        this.stop_cause = stop_cause;
    }

    public Long getDelay_start() {
        return this.delay_start;
    }

    public void setDelay_start(Long delay_start) {
        this.delay_start = delay_start;
    }

    public Integer getTotal_delay() {
        return this.total_delay;
    }

    public void setTotal_delay(Integer total_delay) {
        this.total_delay = total_delay;
    }

    public ArrayList<HashMap<String, Object>> getDelay_info() {
        return this.delay_info;
    }

    public void setDelay_info(ArrayList<HashMap<String, Object>> delay_info) {
        this.delay_info = delay_info;
    }

    public Double getStart_ammeter() {
        return this.start_ammeter;
    }

    public void setStart_ammeter(Double start_ammeter) {
        this.start_ammeter = start_ammeter;
    }

    public Double getStop_ammeter() {
        return this.stop_ammeter;
    }

    public void setStop_ammeter(Double stop_ammeter) {
        this.stop_ammeter = stop_ammeter;
    }

    public Double getTotal_power() {
        return this.total_power;
    }

    public void setTotal_power(Double total_power) {
        this.total_power = total_power;
    }

    public ArrayList<HashMap<String, Object>> getPower_info() {
        return this.power_info;
    }

    public void setPower_info(ArrayList<HashMap<String, Object>> power_info) {
        this.power_info = power_info;
    }

    public ArrayList<HashMap<String, Object>> getService_info() {
        return this.service_info;
    }

    public void setService_info(ArrayList<HashMap<String, Object>> service_info) {
        this.service_info = service_info;
    }

    public String getFee_rate_id() {
        return this.fee_rate_id;
    }

    public void setFee_rate_id(String fee_rate_id) {
        this.fee_rate_id = fee_rate_id;
    }

    public Integer getTotal_fee() {
        return this.total_fee;
    }

    public void setTotal_fee(Integer total_fee) {
        this.total_fee = total_fee;
    }

    public Integer getPower_fee() {
        return this.power_fee;
    }

    public void setPower_fee(Integer power_fee) {
        this.power_fee = power_fee;
    }

    public Integer getService_fee() {
        return this.service_fee;
    }

    public void setService_fee(Integer service_fee) {
        this.service_fee = service_fee;
    }

    public Integer getDelay_fee() {
        return this.delay_fee;
    }

    public void setDelay_fee(Integer delay_fee) {
        this.delay_fee = delay_fee;
    }

    public Integer getPark_fee() {
        return this.park_fee;
    }

    public void setPark_fee(Integer park_fee) {
        this.park_fee = park_fee;
    }

    public Integer getTotal_park() {
        return this.total_park;
    }

    public void setTotal_park(Integer total_park) {
        this.total_park = total_park;
    }

    public ArrayList<HashMap<String, Object>> getPark_info() {
        return this.park_info;
    }

    public void setPark_info(ArrayList<HashMap<String, Object>> park_info) {
        this.park_info = park_info;
    }

    public boolean isBalance_flag() {
        return this.balance_flag;
    }

    public void setBalance_flag(boolean balance_flag) {
        this.balance_flag = balance_flag;
    }

    public Long getBalance_time() {
        return this.balance_time;
    }

    public void setBalance_time(Long balance_time) {
        this.balance_time = balance_time;
    }

    public boolean isPay_flag() {
        return this.pay_flag;
    }

    public void setPay_flag(boolean pay_flag) {
        this.pay_flag = pay_flag;
    }

    public Long getPay_time() {
        return this.pay_time;
    }

    public void setPay_time(Long pay_time) {
        this.pay_time = pay_time;
    }

    public Long getPay_balance() {
        return this.pay_balance;
    }

    public void setPay_balance(Long pay_balance) {
        this.pay_balance = pay_balance;
    }
}
