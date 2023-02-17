package com.xcharge.charger.protocol.ocpp.session;

import com.xcharge.charger.data.bean.type.CHARGE_INIT_TYPE;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.CHARGE_STOP_CAUSE;
import com.xcharge.charger.data.bean.type.USER_TC_TYPE;
import com.xcharge.charger.protocol.ocpp.bean.types.ChargingProfile;
import com.xcharge.charger.protocol.ocpp.bean.types.MeterValue;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;
import java.util.HashMap;

/* loaded from: classes.dex */
public class OcppChargeSession extends JsonBean<OcppChargeSession> {
    private String user_type = null;
    private String user_code = null;
    private String device_id = null;
    private String port = null;
    private String charge_id = null;
    private String fee_rate = null;
    private CHARGE_INIT_TYPE init_type = null;
    private USER_TC_TYPE user_tc_type = null;
    private String user_tc_value = null;
    private long user_balance = 0;
    private int is_free = -1;
    private CHARGE_PLATFORM charge_platform = null;
    private String binded_user = null;
    private long chargeStartTime = 0;
    private long chargeStopTime = 0;
    private double power = 0.0d;
    private CHARGE_STOP_CAUSE chargeStopCause = null;
    private HashMap<String, ArrayList<ChargingProfile>> txChargingProfiles = new HashMap<>();
    private String transactionId = null;
    private boolean evSuspendStatus = false;
    private ArrayList<MeterValue> stopTxnMeterValues = new ArrayList<>();

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

    public String getDevice_id() {
        return this.device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id) {
        this.charge_id = charge_id;
    }

    public String getFee_rate() {
        return this.fee_rate;
    }

    public void setFee_rate(String fee_rate) {
        this.fee_rate = fee_rate;
    }

    public CHARGE_INIT_TYPE getInit_type() {
        return this.init_type;
    }

    public void setInit_type(CHARGE_INIT_TYPE init_type) {
        this.init_type = init_type;
    }

    public USER_TC_TYPE getUser_tc_type() {
        return this.user_tc_type;
    }

    public void setUser_tc_type(USER_TC_TYPE user_tc_type) {
        this.user_tc_type = user_tc_type;
    }

    public String getUser_tc_value() {
        return this.user_tc_value;
    }

    public void setUser_tc_value(String user_tc_value) {
        this.user_tc_value = user_tc_value;
    }

    public long getUser_balance() {
        return this.user_balance;
    }

    public void setUser_balance(long user_balance) {
        this.user_balance = user_balance;
    }

    public int getIs_free() {
        return this.is_free;
    }

    public void setIs_free(int is_free) {
        this.is_free = is_free;
    }

    public CHARGE_PLATFORM getCharge_platform() {
        return this.charge_platform;
    }

    public void setCharge_platform(CHARGE_PLATFORM charge_platform) {
        this.charge_platform = charge_platform;
    }

    public String getBinded_user() {
        return this.binded_user;
    }

    public void setBinded_user(String binded_user) {
        this.binded_user = binded_user;
    }

    public long getChargeStartTime() {
        return this.chargeStartTime;
    }

    public void setChargeStartTime(long chargeStartTime) {
        this.chargeStartTime = chargeStartTime;
    }

    public long getChargeStopTime() {
        return this.chargeStopTime;
    }

    public void setChargeStopTime(long chargeStopTime) {
        this.chargeStopTime = chargeStopTime;
    }

    public double getPower() {
        return this.power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    public CHARGE_STOP_CAUSE getChargeStopCause() {
        return this.chargeStopCause;
    }

    public void setChargeStopCause(CHARGE_STOP_CAUSE chargeStopCause) {
        this.chargeStopCause = chargeStopCause;
    }

    public HashMap<String, ArrayList<ChargingProfile>> getTxChargingProfiles() {
        return this.txChargingProfiles;
    }

    public void setTxChargingProfiles(HashMap<String, ArrayList<ChargingProfile>> txChargingProfiles) {
        this.txChargingProfiles = txChargingProfiles;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public boolean isEvSuspendStatus() {
        return this.evSuspendStatus;
    }

    public void setEvSuspendStatus(boolean evSuspendStatus) {
        this.evSuspendStatus = evSuspendStatus;
    }

    public ArrayList<MeterValue> getStopTxnMeterValues() {
        return this.stopTxnMeterValues;
    }

    public void setStopTxnMeterValues(ArrayList<MeterValue> stopTxnMeterValues) {
        this.stopTxnMeterValues = stopTxnMeterValues;
    }
}