package com.xcharge.charger.protocol.monitor.bean;

import com.xcharge.common.bean.JsonBean;
import java.util.List;

public class YZXDCAPOption extends JsonBean<YZXDCAPOption> {
    public static final String ALERT_ISSUE = "alert_issue";
    public static final String ALERT_REMOVE = "alert_remove";
    public static final String AMMETER = "ammeter";
    public static final String AMP_PWM = "amp_pwm";
    public static final String BILL = "bill";
    public static final String CAPABILITY = "capability";
    public static final String CHARGE = "charge";
    public static final String CHARGE_REFUSED = "charge_refused";
    public static final String CHARGE_STARTED = "charge_started";
    public static final String CHARGE_STATUS = "charge_status";
    public static final String CHARGE_STOPPED = "charge_stopped";
    public static final String CP_RANGE = "cp_range";
    public static final String CUSTOM_UI = "custom_ui";
    public static final String DELAY = "delay";
    public static final String DELAY_STARTED = "delay_started";
    public static final String DELAY_WAIT_STARTED = "delay_wait_started";
    public static final String ERROR = "error";
    public static final String FEE_POLICY = "fee_policy";
    public static final String GUN_CONNECT = "gun_connect";
    public static final String GUN_LOCK_STATUS = "gun_lock_status";
    public static final String NETWORK = "network";
    public static final String PORT_ENABLE = "port_enable";
    public static final String PULL_ADVSITE = "pull_advsite";
    public static final String QRCODE = "qrcode";
    public static final String RADAR_PARAM = "radar_param";
    public static final String RADAR_STATUS = "radar_status";
    public static final String SCAN_ADVSITE = "scan_advsite";
    public static final String STOP_CONDITION = "stop_condition";
    public static final String SYSTEM_CONTROL = "system_control";
    public static final String TIMING_PARAM = "timing_param";
    public static final String UPGRADE_STATUS = "upgrade_status";
    public static final String VERSION = "version";
    public static final String VOLT_RANGE = "volt_range";
    public static final String WAKEUP_ADVSITE = "wakeup_advsite";

    /* renamed from: op */
    private String f94op = null;
    private List<String> prop_id = null;
    private Long seq = null;
    private List<String> subnode = null;

    public String getOp() {
        return this.f94op;
    }

    public void setOp(String op) {
        this.f94op = op;
    }

    public Long getSeq() {
        return this.seq;
    }

    public void setSeq(Long seq2) {
        this.seq = seq2;
    }

    public List<String> getSubnode() {
        return this.subnode;
    }

    public void setSubnode(List<String> subnode2) {
        this.subnode = subnode2;
    }

    public List<String> getProp_id() {
        return this.prop_id;
    }

    public void setProp_id(List<String> prop_id2) {
        this.prop_id = prop_id2;
    }
}
