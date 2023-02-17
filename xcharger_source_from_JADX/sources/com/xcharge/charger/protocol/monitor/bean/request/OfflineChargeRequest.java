package com.xcharge.charger.protocol.monitor.bean.request;

import com.xcharge.common.bean.JsonBean;

public class OfflineChargeRequest extends JsonBean<OfflineChargeRequest> {
    private long init_time;
    private String local_charge_id;
    private int port;
    private long swipe_time;
    private String user_code;
    private String user_type;

    public String getLocal_charge_id() {
        return this.local_charge_id;
    }

    public void setLocal_charge_id(String local_charge_id2) {
        this.local_charge_id = local_charge_id2;
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

    public int getPort() {
        return this.port;
    }

    public void setPort(int port2) {
        this.port = port2;
    }

    public long getSwipe_time() {
        return this.swipe_time;
    }

    public void setSwipe_time(long swipe_time2) {
        this.swipe_time = swipe_time2;
    }

    public long getInit_time() {
        return this.init_time;
    }

    public void setInit_time(long init_time2) {
        this.init_time = init_time2;
    }
}
