package com.xcharge.charger.data.proxy;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import com.google.gson.reflect.TypeToken;
import com.xcharge.charger.data.bean.FeeRate;
import com.xcharge.charger.data.bean.type.CHARGE_INIT_TYPE;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.CHARGE_STOP_CAUSE;
import com.xcharge.charger.data.bean.type.USER_TC_TYPE;
import com.xcharge.charger.data.p004db.ContentDB;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;
import java.util.HashMap;

public class ChargeBill extends JsonBean<ChargeBill> {
    private String attach_data = null;
    private int balance_flag = 0;
    private long balance_time = 0;
    private String binded_user = null;
    private String charge_id = null;
    private CHARGE_PLATFORM charge_platform = null;
    private String cloud_charge_id = null;
    private int delay_fee = 0;
    private ArrayList<HashMap<String, Object>> delay_info = null;
    private long delay_start = 0;
    private FeeRate fee_rate = null;
    private String fee_rate_id = null;
    private long fin_time = 0;

    /* renamed from: id */
    private String f69id = null;
    private long init_time = 0;
    private CHARGE_INIT_TYPE init_type = null;
    private int is_free = -1;
    private int monitor_flag = 0;
    private int park_fee = 0;
    private ArrayList<HashMap<String, Object>> park_info = null;
    private int pay_flag = -1;
    private long pay_time = 0;
    private int pay_type = -1;
    private String port = null;
    private int power_fee = 0;
    private ArrayList<HashMap<String, Object>> power_info = null;
    private int report_flag = 0;
    private int service_fee = 0;
    private ArrayList<HashMap<String, Object>> service_info = null;
    private double start_ammeter = -1.0d;
    private int start_report_flag = 0;
    private long start_time = 0;
    private double stop_ammeter = -1.0d;
    private CHARGE_STOP_CAUSE stop_cause = null;
    private int stop_report_flag = 0;
    private long stop_time = 0;
    private int total_delay = 0;
    private int total_fee = 0;
    private int total_park = 0;
    private double total_power = 0.0d;
    private int total_time = 0;
    private long update_time = 0;
    private long user_balance = 0;
    private String user_code = null;
    private USER_TC_TYPE user_tc_type = null;
    private String user_tc_value = null;
    private String user_type = null;

    public String getId() {
        return this.f69id;
    }

    public void setId(String id) {
        this.f69id = id;
    }

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id2) {
        this.charge_id = charge_id2;
    }

    public String getUser_type() {
        return this.user_type;
    }

    public void setUser_type(String user_type2) {
        this.user_type = user_type2;
    }

    public String getUser_code() {
        return this.user_code;
    }

    public void setUser_code(String user_code2) {
        this.user_code = user_code2;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port2) {
        this.port = port2;
    }

    public CHARGE_INIT_TYPE getInit_type() {
        return this.init_type;
    }

    public void setInit_type(CHARGE_INIT_TYPE init_type2) {
        this.init_type = init_type2;
    }

    public USER_TC_TYPE getUser_tc_type() {
        return this.user_tc_type;
    }

    public void setUser_tc_type(USER_TC_TYPE user_tc_type2) {
        this.user_tc_type = user_tc_type2;
    }

    public String getUser_tc_value() {
        return this.user_tc_value;
    }

    public void setUser_tc_value(String user_tc_value2) {
        this.user_tc_value = user_tc_value2;
    }

    public long getUser_balance() {
        return this.user_balance;
    }

    public void setUser_balance(long user_balance2) {
        this.user_balance = user_balance2;
    }

    public int getIs_free() {
        return this.is_free;
    }

    public void setIs_free(int is_free2) {
        this.is_free = is_free2;
    }

    public CHARGE_PLATFORM getCharge_platform() {
        return this.charge_platform;
    }

    public void setCharge_platform(CHARGE_PLATFORM charge_platform2) {
        this.charge_platform = charge_platform2;
    }

    public String getBinded_user() {
        return this.binded_user;
    }

    public void setBinded_user(String binded_user2) {
        this.binded_user = binded_user2;
    }

    public String getCloud_charge_id() {
        return this.cloud_charge_id;
    }

    public void setCloud_charge_id(String cloud_charge_id2) {
        this.cloud_charge_id = cloud_charge_id2;
    }

    public int getTotal_time() {
        return this.total_time;
    }

    public void setTotal_time(int total_time2) {
        this.total_time = total_time2;
    }

    public long getInit_time() {
        return this.init_time;
    }

    public void setInit_time(long init_time2) {
        this.init_time = init_time2;
    }

    public long getFin_time() {
        return this.fin_time;
    }

    public void setFin_time(long fin_time2) {
        this.fin_time = fin_time2;
    }

    public long getStart_time() {
        return this.start_time;
    }

    public void setStart_time(long start_time2) {
        this.start_time = start_time2;
    }

    public long getStop_time() {
        return this.stop_time;
    }

    public void setStop_time(long stop_time2) {
        this.stop_time = stop_time2;
    }

    public long getDelay_start() {
        return this.delay_start;
    }

    public void setDelay_start(long delay_start2) {
        this.delay_start = delay_start2;
    }

    public int getTotal_delay() {
        return this.total_delay;
    }

    public void setTotal_delay(int total_delay2) {
        this.total_delay = total_delay2;
    }

    public ArrayList<HashMap<String, Object>> getDelay_info() {
        return this.delay_info;
    }

    public void setDelay_info(ArrayList<HashMap<String, Object>> delay_info2) {
        this.delay_info = delay_info2;
    }

    public CHARGE_STOP_CAUSE getStop_cause() {
        return this.stop_cause;
    }

    public void setStop_cause(CHARGE_STOP_CAUSE stop_cause2) {
        this.stop_cause = stop_cause2;
    }

    public double getStart_ammeter() {
        return this.start_ammeter;
    }

    public void setStart_ammeter(double start_ammeter2) {
        this.start_ammeter = start_ammeter2;
    }

    public double getStop_ammeter() {
        return this.stop_ammeter;
    }

    public void setStop_ammeter(double stop_ammeter2) {
        this.stop_ammeter = stop_ammeter2;
    }

    public double getTotal_power() {
        return this.total_power;
    }

    public void setTotal_power(double total_power2) {
        this.total_power = total_power2;
    }

    public ArrayList<HashMap<String, Object>> getPower_info() {
        return this.power_info;
    }

    public void setPower_info(ArrayList<HashMap<String, Object>> power_info2) {
        this.power_info = power_info2;
    }

    public String getFee_rate_id() {
        return this.fee_rate_id;
    }

    public void setFee_rate_id(String fee_rate_id2) {
        this.fee_rate_id = fee_rate_id2;
    }

    public FeeRate getFee_rate() {
        return this.fee_rate;
    }

    public void setFee_rate(FeeRate fee_rate2) {
        this.fee_rate = fee_rate2;
    }

    public int getTotal_fee() {
        return this.total_fee;
    }

    public void setTotal_fee(int total_fee2) {
        this.total_fee = total_fee2;
    }

    public int getPower_fee() {
        return this.power_fee;
    }

    public void setPower_fee(int power_fee2) {
        this.power_fee = power_fee2;
    }

    public int getService_fee() {
        return this.service_fee;
    }

    public void setService_fee(int service_fee2) {
        this.service_fee = service_fee2;
    }

    public int getDelay_fee() {
        return this.delay_fee;
    }

    public void setDelay_fee(int delay_fee2) {
        this.delay_fee = delay_fee2;
    }

    public int getBalance_flag() {
        return this.balance_flag;
    }

    public void setBalance_flag(int balance_flag2) {
        this.balance_flag = balance_flag2;
    }

    public long getBalance_time() {
        return this.balance_time;
    }

    public void setBalance_time(long balance_time2) {
        this.balance_time = balance_time2;
    }

    public int getPay_flag() {
        return this.pay_flag;
    }

    public void setPay_flag(int pay_flag2) {
        this.pay_flag = pay_flag2;
    }

    public int getPay_type() {
        return this.pay_type;
    }

    public void setPay_type(int pay_type2) {
        this.pay_type = pay_type2;
    }

    public long getPay_time() {
        return this.pay_time;
    }

    public void setPay_time(long pay_time2) {
        this.pay_time = pay_time2;
    }

    public int getReport_flag() {
        return this.report_flag;
    }

    public void setReport_flag(int report_flag2) {
        this.report_flag = report_flag2;
    }

    public int getMonitor_flag() {
        return this.monitor_flag;
    }

    public void setMonitor_flag(int monitor_flag2) {
        this.monitor_flag = monitor_flag2;
    }

    public ArrayList<HashMap<String, Object>> getService_info() {
        return this.service_info;
    }

    public void setService_info(ArrayList<HashMap<String, Object>> service_info2) {
        this.service_info = service_info2;
    }

    public int getTotal_park() {
        return this.total_park;
    }

    public void setTotal_park(int total_park2) {
        this.total_park = total_park2;
    }

    public int getPark_fee() {
        return this.park_fee;
    }

    public void setPark_fee(int park_fee2) {
        this.park_fee = park_fee2;
    }

    public ArrayList<HashMap<String, Object>> getPark_info() {
        return this.park_info;
    }

    public void setPark_info(ArrayList<HashMap<String, Object>> park_info2) {
        this.park_info = park_info2;
    }

    public long getUpdate_time() {
        return this.update_time;
    }

    public void setUpdate_time(long update_time2) {
        this.update_time = update_time2;
    }

    public int getStart_report_flag() {
        return this.start_report_flag;
    }

    public void setStart_report_flag(int start_report_flag2) {
        this.start_report_flag = start_report_flag2;
    }

    public int getStop_report_flag() {
        return this.stop_report_flag;
    }

    public void setStop_report_flag(int stop_report_flag2) {
        this.stop_report_flag = stop_report_flag2;
    }

    public String getAttach_data() {
        return this.attach_data;
    }

    public void setAttach_data(String attach_data2) {
        this.attach_data = attach_data2;
    }

    public ChargeBill fromDbLine(Cursor c) throws Exception {
        this.f69id = c.getString(c.getColumnIndexOrThrow("_id"));
        this.charge_id = c.getString(c.getColumnIndexOrThrow(ContentDB.ChargeTable.CHARGE_ID));
        this.user_type = c.getString(c.getColumnIndexOrThrow(ContentDB.ChargeTable.USER_TYPE));
        this.user_code = c.getString(c.getColumnIndexOrThrow(ContentDB.ChargeTable.USER_CODE));
        this.port = c.getString(c.getColumnIndexOrThrow(ContentDB.ChargeTable.PORT));
        String initType = c.getString(c.getColumnIndexOrThrow(ContentDB.ChargeTable.INIT_TYPE));
        if (!TextUtils.isEmpty(initType)) {
            this.init_type = CHARGE_INIT_TYPE.valueOf(initType);
        } else {
            this.init_type = null;
        }
        String userTcType = c.getString(c.getColumnIndexOrThrow(ContentDB.ChargeTable.USER_TC_TYPE));
        if (!TextUtils.isEmpty(userTcType)) {
            this.user_tc_type = USER_TC_TYPE.valueOf(userTcType);
        } else {
            this.user_tc_type = null;
        }
        this.user_tc_value = c.getString(c.getColumnIndexOrThrow(ContentDB.ChargeTable.USER_TC_VALUE));
        this.user_balance = c.getLong(c.getColumnIndexOrThrow(ContentDB.ChargeTable.USER_BALANCE));
        this.is_free = c.getInt(c.getColumnIndexOrThrow(ContentDB.ChargeTable.IS_FREE));
        this.binded_user = c.getString(c.getColumnIndexOrThrow(ContentDB.ChargeTable.BINDED_USER));
        this.cloud_charge_id = c.getString(c.getColumnIndexOrThrow(ContentDB.ChargeTable.CLOUD_CHARGE_ID));
        String chargePlatform = c.getString(c.getColumnIndexOrThrow(ContentDB.ChargeTable.CHARGE_PLATFORM));
        if (!TextUtils.isEmpty(chargePlatform)) {
            this.charge_platform = CHARGE_PLATFORM.valueOf(chargePlatform);
        } else {
            this.charge_platform = null;
        }
        this.total_time = c.getInt(c.getColumnIndexOrThrow(ContentDB.ChargeTable.TOTAL_TIME));
        this.init_time = c.getLong(c.getColumnIndexOrThrow(ContentDB.ChargeTable.INIT_TIME));
        this.fin_time = c.getLong(c.getColumnIndexOrThrow(ContentDB.ChargeTable.FIN_TIME));
        this.start_time = c.getLong(c.getColumnIndexOrThrow(ContentDB.ChargeTable.START_TIME));
        this.stop_time = c.getLong(c.getColumnIndexOrThrow(ContentDB.ChargeTable.STOP_TIME));
        this.delay_start = c.getLong(c.getColumnIndexOrThrow("delay_start"));
        this.total_delay = c.getInt(c.getColumnIndexOrThrow(ContentDB.ChargeTable.TOTAL_DELAY));
        this.update_time = c.getLong(c.getColumnIndexOrThrow("update_time"));
        String delayInfo = c.getString(c.getColumnIndexOrThrow(ContentDB.ChargeTable.DELAY_INFO));
        if (!TextUtils.isEmpty(delayInfo)) {
            this.delay_info = (ArrayList) JsonBean.getGsonBuilder().create().fromJson(delayInfo, new TypeToken<ArrayList<HashMap<String, Object>>>() {
            }.getType());
        } else {
            this.delay_info = null;
        }
        String stopCause = c.getString(c.getColumnIndexOrThrow(ContentDB.ChargeTable.STOP_CAUSE));
        if (!TextUtils.isEmpty(stopCause)) {
            this.stop_cause = CHARGE_STOP_CAUSE.valueOf(stopCause);
        } else {
            this.stop_cause = null;
        }
        this.start_ammeter = c.getDouble(c.getColumnIndexOrThrow(ContentDB.ChargeTable.START_AMMETER));
        this.stop_ammeter = c.getDouble(c.getColumnIndexOrThrow(ContentDB.ChargeTable.STOP_AMMETER));
        this.total_power = c.getDouble(c.getColumnIndexOrThrow(ContentDB.ChargeTable.TOTAL_POWER));
        String powerInfo = c.getString(c.getColumnIndexOrThrow(ContentDB.ChargeTable.POWER_INFO));
        if (!TextUtils.isEmpty(powerInfo)) {
            this.power_info = (ArrayList) JsonBean.getGsonBuilder().create().fromJson(powerInfo, new TypeToken<ArrayList<HashMap<String, Object>>>() {
            }.getType());
        } else {
            this.power_info = null;
        }
        String serviceInfo = c.getString(c.getColumnIndexOrThrow(ContentDB.ChargeTable.SERVICE_INFO));
        if (!TextUtils.isEmpty(serviceInfo)) {
            this.service_info = (ArrayList) JsonBean.getGsonBuilder().create().fromJson(serviceInfo, new TypeToken<ArrayList<HashMap<String, Object>>>() {
            }.getType());
        } else {
            this.service_info = null;
        }
        this.fee_rate_id = c.getString(c.getColumnIndexOrThrow(ContentDB.ChargeTable.FEE_RATE_ID));
        String feeRate = c.getString(c.getColumnIndexOrThrow(ContentDB.ChargeTable.FEE_RATE));
        if (!TextUtils.isEmpty(feeRate)) {
            this.fee_rate = (FeeRate) new FeeRate().fromJson(feeRate);
        } else {
            this.fee_rate = null;
        }
        this.total_fee = c.getInt(c.getColumnIndexOrThrow(ContentDB.ChargeTable.TOTAL_FEE));
        this.power_fee = c.getInt(c.getColumnIndexOrThrow(ContentDB.ChargeTable.POWER_FEE));
        this.service_fee = c.getInt(c.getColumnIndexOrThrow(ContentDB.ChargeTable.SERVICE_FEE));
        this.delay_fee = c.getInt(c.getColumnIndexOrThrow(ContentDB.ChargeTable.DELAY_FEE));
        this.total_park = c.getInt(c.getColumnIndexOrThrow(ContentDB.ChargeTable.TOTAL_PARK));
        this.park_fee = c.getInt(c.getColumnIndexOrThrow(ContentDB.ChargeTable.PARK_FEE));
        String parkInfo = c.getString(c.getColumnIndexOrThrow(ContentDB.ChargeTable.PARK_INFO));
        if (!TextUtils.isEmpty(parkInfo)) {
            this.park_info = (ArrayList) JsonBean.getGsonBuilder().create().fromJson(parkInfo, new TypeToken<ArrayList<HashMap<String, Object>>>() {
            }.getType());
        } else {
            this.park_info = null;
        }
        this.balance_flag = c.getInt(c.getColumnIndexOrThrow(ContentDB.ChargeTable.BALANCE_FLAG));
        this.balance_time = c.getLong(c.getColumnIndexOrThrow(ContentDB.ChargeTable.BALANCE_TIME));
        this.pay_flag = c.getInt(c.getColumnIndexOrThrow(ContentDB.ChargeTable.PAY_FLAG));
        this.pay_type = c.getInt(c.getColumnIndexOrThrow(ContentDB.ChargeTable.PAY_TYPE));
        this.pay_time = c.getLong(c.getColumnIndexOrThrow(ContentDB.ChargeTable.PAY_TIME));
        this.report_flag = c.getInt(c.getColumnIndexOrThrow(ContentDB.ChargeTable.REPORT_FLAG));
        this.monitor_flag = c.getInt(c.getColumnIndexOrThrow(ContentDB.ChargeTable.MONITOR_FLAG));
        this.start_report_flag = c.getInt(c.getColumnIndexOrThrow(ContentDB.ChargeTable.START_REPORT_FLAG));
        this.stop_report_flag = c.getInt(c.getColumnIndexOrThrow(ContentDB.ChargeTable.STOP_REPORT_FLAG));
        this.attach_data = c.getString(c.getColumnIndexOrThrow(ContentDB.ChargeTable.ATTACH_DATA));
        return this;
    }

    public ContentValues toDbLine() {
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        String str6;
        String str7;
        String str8 = null;
        ContentValues cv = new ContentValues();
        cv.put(ContentDB.ChargeTable.CHARGE_ID, this.charge_id);
        cv.put(ContentDB.ChargeTable.USER_TYPE, this.user_type);
        cv.put(ContentDB.ChargeTable.USER_CODE, this.user_code);
        cv.put(ContentDB.ChargeTable.PORT, this.port);
        cv.put(ContentDB.ChargeTable.INIT_TYPE, this.init_type != null ? this.init_type.getType() : null);
        if (this.user_tc_type != null) {
            str = this.user_tc_type.getType();
        } else {
            str = null;
        }
        cv.put(ContentDB.ChargeTable.USER_TC_TYPE, str);
        cv.put(ContentDB.ChargeTable.USER_TC_VALUE, this.user_tc_value);
        cv.put(ContentDB.ChargeTable.USER_BALANCE, Long.valueOf(this.user_balance));
        cv.put(ContentDB.ChargeTable.IS_FREE, Integer.valueOf(this.is_free));
        cv.put(ContentDB.ChargeTable.BINDED_USER, this.binded_user);
        if (this.charge_platform != null) {
            str2 = this.charge_platform.getPlatform();
        } else {
            str2 = null;
        }
        cv.put(ContentDB.ChargeTable.CHARGE_PLATFORM, str2);
        cv.put(ContentDB.ChargeTable.TOTAL_TIME, Integer.valueOf(this.total_time));
        cv.put(ContentDB.ChargeTable.INIT_TIME, Long.valueOf(this.init_time));
        cv.put(ContentDB.ChargeTable.FIN_TIME, Long.valueOf(this.fin_time));
        cv.put(ContentDB.ChargeTable.START_TIME, Long.valueOf(this.start_time));
        cv.put(ContentDB.ChargeTable.STOP_TIME, Long.valueOf(this.stop_time));
        cv.put("delay_start", Long.valueOf(this.delay_start));
        cv.put(ContentDB.ChargeTable.TOTAL_DELAY, Integer.valueOf(this.total_delay));
        if (this.delay_info != null) {
            str3 = JsonBean.getGsonBuilder().create().toJson((Object) this.delay_info);
        } else {
            str3 = null;
        }
        cv.put(ContentDB.ChargeTable.DELAY_INFO, str3);
        if (this.stop_cause != null) {
            str4 = this.stop_cause.getCause();
        } else {
            str4 = null;
        }
        cv.put(ContentDB.ChargeTable.STOP_CAUSE, str4);
        cv.put("update_time", Long.valueOf(this.update_time));
        cv.put(ContentDB.ChargeTable.START_AMMETER, Double.valueOf(this.start_ammeter));
        cv.put(ContentDB.ChargeTable.STOP_AMMETER, Double.valueOf(this.stop_ammeter));
        cv.put(ContentDB.ChargeTable.TOTAL_POWER, Double.valueOf(this.total_power));
        if (this.power_info != null) {
            str5 = JsonBean.getGsonBuilder().create().toJson((Object) this.power_info);
        } else {
            str5 = null;
        }
        cv.put(ContentDB.ChargeTable.POWER_INFO, str5);
        if (this.service_info != null) {
            str6 = JsonBean.getGsonBuilder().create().toJson((Object) this.service_info);
        } else {
            str6 = null;
        }
        cv.put(ContentDB.ChargeTable.SERVICE_INFO, str6);
        cv.put(ContentDB.ChargeTable.FEE_RATE_ID, this.fee_rate_id);
        if (this.fee_rate != null) {
            str7 = this.fee_rate.toJson();
        } else {
            str7 = null;
        }
        cv.put(ContentDB.ChargeTable.FEE_RATE, str7);
        cv.put(ContentDB.ChargeTable.TOTAL_FEE, Integer.valueOf(this.total_fee));
        cv.put(ContentDB.ChargeTable.POWER_FEE, Integer.valueOf(this.power_fee));
        cv.put(ContentDB.ChargeTable.SERVICE_FEE, Integer.valueOf(this.service_fee));
        cv.put(ContentDB.ChargeTable.DELAY_FEE, Integer.valueOf(this.delay_fee));
        cv.put(ContentDB.ChargeTable.TOTAL_PARK, Integer.valueOf(this.total_park));
        cv.put(ContentDB.ChargeTable.PARK_FEE, Integer.valueOf(this.park_fee));
        if (this.park_info != null) {
            str8 = JsonBean.getGsonBuilder().create().toJson((Object) this.park_info);
        }
        cv.put(ContentDB.ChargeTable.PARK_INFO, str8);
        cv.put(ContentDB.ChargeTable.BALANCE_FLAG, Integer.valueOf(this.balance_flag));
        return cv;
    }
}
