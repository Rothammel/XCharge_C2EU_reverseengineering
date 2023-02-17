package com.xcharge.charger.protocol.monitor.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class YZXDCAPMessage extends JsonBean<YZXDCAPMessage> {
    public static final String DCAP_VER = "1.00";
    public static final String OP_ACK = "ack";
    public static final String OP_ALERT = "alert";
    public static final String OP_AUTH = "auth";
    public static final String OP_DISCOVERY = "discovery";
    public static final String OP_EVENT = "event";
    public static final String OP_FIN = "fin";
    public static final String OP_HELLO = "hello";
    public static final String OP_INIT = "init";
    public static final String OP_JOIN = "join";
    public static final String OP_LEAVE = "leave";
    public static final String OP_LOG = "log";
    public static final String OP_OFFER = "offer";
    public static final String OP_OFFLINE_CHARGE = "offline_charge";
    public static final String OP_ONLINE = "online";
    public static final String OP_QUERY = "query";
    public static final String OP_REPORT = "report";
    public static final String OP_SECURITY = "security";
    public static final String OP_SET = "set";
    public static final String OP_STOP = "stop";
    public static final String OP_THIRD_CHARGE = "third_charge";
    public static final String OP_UPGRADE = "upgrade";
    public static final String PROT_TYPE_CAP = "cap";
    public static final String PROT_TYPE_DAP = "dap";
    public static final String PROT_TYPE_DDAP = "ddap";
    public static final String PROT_VER_CAP = "1.00";
    public static final String PROT_VER_DAP = "1.00";
    public static final String PROT_VER_DDAP = "1.00";
    private Object data;
    private YZXDCAPError error;
    private int flag;
    private String ver = "1.00";
    private Long ts = null;
    private Long seq = null;
    private String xid = null;
    private String nonce = null;
    private String sign = null;
    private String prot_type = null;
    private String prot_ver = null;
    private String op = null;
    private YZXDCAPOption opt = null;
    private String port = null;
    private boolean isBill = false;

    public String getVer() {
        return this.ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public Long getTs() {
        return this.ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public Long getSeq() {
        return this.seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    public String getXid() {
        return this.xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }

    public String getNonce() {
        return this.nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getSign() {
        return this.sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getProt_type() {
        return this.prot_type;
    }

    public void setProt_type(String prot_type) {
        this.prot_type = prot_type;
    }

    public String getProt_ver() {
        return this.prot_ver;
    }

    public void setProt_ver(String prot_ver) {
        this.prot_ver = prot_ver;
    }

    public String getOp() {
        return this.op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public YZXDCAPOption getOpt() {
        return this.opt;
    }

    public void setOpt(YZXDCAPOption opt) {
        this.opt = opt;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public boolean isBill() {
        return this.isBill;
    }

    public void setBill(boolean isBill) {
        this.isBill = isBill;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public YZXDCAPError getError() {
        return this.error;
    }

    public void setError(YZXDCAPError error) {
        this.error = error;
    }

    public int getFlag() {
        return this.flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
