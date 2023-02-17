package com.xcharge.charger.core.api.bean.cap;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class CAPDirectiveOption extends JsonBean<CAPDirectiveOption> {
    private String op = null;
    private Long seq = null;
    private String charge_id = null;
    private String event_id = null;
    private String auth_id = null;
    private String query_id = null;
    private String set_id = null;
    private String report_id = null;
    private String port_id = null;
    private String condition_id = null;

    public String getOp() {
        return this.op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public Long getSeq() {
        return this.seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id) {
        this.charge_id = charge_id;
    }

    public String getEvent_id() {
        return this.event_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }

    public String getAuth_id() {
        return this.auth_id;
    }

    public void setAuth_id(String auth_id) {
        this.auth_id = auth_id;
    }

    public String getQuery_id() {
        return this.query_id;
    }

    public void setQuery_id(String query_id) {
        this.query_id = query_id;
    }

    public String getSet_id() {
        return this.set_id;
    }

    public void setSet_id(String set_id) {
        this.set_id = set_id;
    }

    public String getReport_id() {
        return this.report_id;
    }

    public void setReport_id(String report_id) {
        this.report_id = report_id;
    }

    public String getPort_id() {
        return this.port_id;
    }

    public void setPort_id(String port_id) {
        this.port_id = port_id;
    }

    public String getCondition_id() {
        return this.condition_id;
    }

    public void setCondition_id(String condition_id) {
        this.condition_id = condition_id;
    }
}