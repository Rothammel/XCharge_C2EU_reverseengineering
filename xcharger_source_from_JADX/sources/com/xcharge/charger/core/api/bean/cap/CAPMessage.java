package com.xcharge.charger.core.api.bean.cap;

import com.xcharge.common.bean.JsonBean;

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
    private Object data = null;

    /* renamed from: op */
    private String f46op = null;
    private CAPDirectiveOption opt = new CAPDirectiveOption();
    private String ver = "1.0";

    public String getVer() {
        return this.ver;
    }

    public void setVer(String ver2) {
        this.ver = ver2;
    }

    public String getOp() {
        return this.f46op;
    }

    public void setOp(String op) {
        this.f46op = op;
    }

    public CAPDirectiveOption getOpt() {
        return this.opt;
    }

    public void setOpt(CAPDirectiveOption opt2) {
        this.opt = opt2;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data2) {
        this.data = data2;
    }
}
