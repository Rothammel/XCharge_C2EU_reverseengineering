package com.xcharge.charger.protocol.xmsz.bean;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent;
import com.xcharge.charger.protocol.xmsz.R;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.ContextUtils;

/* loaded from: classes.dex */
public class XMSZConfig extends JsonBean<XMSZConfig> {
    private static final String configFileName = "xmsz_cfg.json";
    private String vendor_name = "40";
    private int vendor_id = 4;
    private long point_id = 0;
    private String center_ip_port = "10.8.0.254:8115";
    private int max_offline_time = 600;
    private int min_balance = 3000;
    private int time_for_reserve = XCloudProtocolAgent.TIMER_MQTT_CONNECT_BLOCKED;
    private String qrcode_url = "http://cdz.xmszzy.cn:6010/ChargeCenter/app.jsf";

    public synchronized String getVendor_name() {
        return this.vendor_name;
    }

    public synchronized void setVendor_name(String vendor_name) {
        this.vendor_name = vendor_name;
    }

    public synchronized int getVendor_id() {
        return this.vendor_id;
    }

    public synchronized void setVendor_id(int vendor_id) {
        this.vendor_id = vendor_id;
    }

    public synchronized long getPoint_id() {
        return this.point_id;
    }

    public synchronized void setPoint_id(long point_id) {
        this.point_id = point_id;
    }

    public synchronized String getCenter_ip_port() {
        return this.center_ip_port;
    }

    public synchronized void setCenter_ip_port(String center_ip_port) {
        this.center_ip_port = center_ip_port;
    }

    public synchronized int getMax_offline_time() {
        return this.max_offline_time;
    }

    public synchronized void setMax_offline_time(int max_offline_time) {
        this.max_offline_time = max_offline_time;
    }

    public synchronized int getMin_balance() {
        return this.min_balance;
    }

    public synchronized void setMin_balance(int min_balance) {
        this.min_balance = min_balance;
    }

    public synchronized int getTime_for_reserve() {
        return this.time_for_reserve;
    }

    public synchronized void setTime_for_reserve(int time_for_reserve) {
        this.time_for_reserve = time_for_reserve;
    }

    public synchronized String getQrcode_url() {
        return this.qrcode_url;
    }

    public synchronized void setQrcode_url(String qrcode_url) {
        this.qrcode_url = qrcode_url;
    }

    public synchronized void init(Context context) {
        try {
            String cfg = ContextUtils.getRawFileToString(context, R.raw.xmsz_cfg);
            if (!TextUtils.isEmpty(cfg)) {
                Log.d("XMSZConfig.init", "config: " + cfg);
                XMSZConfig config = new XMSZConfig().fromJson(cfg);
                this.vendor_name = config.vendor_name;
                this.vendor_id = config.vendor_id;
                this.qrcode_url = config.qrcode_url;
                if (!ContextUtils.isExistFile(configFileName, context)) {
                    this.point_id = config.point_id;
                    this.center_ip_port = config.center_ip_port;
                    this.max_offline_time = config.max_offline_time;
                    this.min_balance = config.min_balance;
                    this.time_for_reserve = config.time_for_reserve;
                } else {
                    load(context);
                }
                persist(context);
            } else {
                load(context);
            }
        } catch (Exception e) {
            Log.w("XMSZConfig.init", Log.getStackTraceString(e));
        }
    }

    public synchronized void load(Context context) {
        try {
            String cfg = ContextUtils.readFileData(configFileName, context);
            if (!TextUtils.isEmpty(cfg)) {
                Log.d("XMSZConfig.load", "config: " + cfg);
                XMSZConfig config = new XMSZConfig().fromJson(cfg);
                this.point_id = config.point_id;
                this.center_ip_port = config.center_ip_port;
                this.max_offline_time = config.max_offline_time;
                this.min_balance = config.min_balance;
                this.time_for_reserve = config.time_for_reserve;
            }
        } catch (Exception e) {
            Log.w("XMSZConfig.load", Log.getStackTraceString(e));
        }
    }

    public synchronized void persist(Context context) {
        ContextUtils.writeFileData(configFileName, toJson(), context);
    }
}