package com.xcharge.charger.core.api.bean.cap;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class CAPMessage extends JsonBean<CAPMessage> {
    public static final String DIRECTIVE_ACK = "ack";
    public static final String DIRECTIVE_ACTIVE = "active";
    public static final String DIRECTIVE_ALERT = "alert";
    public static final String DIRECTIVE_AUTH = "auth";
    public static final String DIRECTIVE_CONDITION = "condition";
    public static final String DIRECTIVE_EVENT = "event";
    public static final String DIRECTIVE_FIN = "fin";
    public static final String DIRECTIVE_INIT = "init";
    public static final String DIRECTIVE_INIT_ACK = "init_ack";
    public static final String DIRECTIVE_LOG = "log";
    public static final String DIRECTIVE_NACK = "nack";
    public static final String DIRECTIVE_QUERY = "query";
    public static final String DIRECTIVE_REPORT = "report";
    public static final String DIRECTIVE_SET = "set";
    public static final String DIRECTIVE_START = "start";
    public static final String DIRECTIVE_STOP = "stop";
    public static final String DIRECTIVE_UPGRADE = "upgrade";
    public static final String VERSION = "1.0";
    private String ver = "1.0";
    private String op = null;
    private CAPDirectiveOption opt = new CAPDirectiveOption();
    private Object data = null;

    public String getVer() {
        return this.ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getOp() {
        return this.op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public CAPDirectiveOption getOpt() {
        return this.opt;
    }

    public void setOpt(CAPDirectiveOption opt) {
        this.opt = opt;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
