package com.xcharge.charger.protocol.monitor.bean;

import com.xcharge.common.bean.JsonBean;

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
    private boolean isBill = false;
    private String nonce = null;

    /* renamed from: op */
    private String f92op = null;
    private YZXDCAPOption opt = null;
    private String port = null;
    private String prot_type = null;
    private String prot_ver = null;
    private Long seq = null;
    private String sign = null;

    /* renamed from: ts */
    private Long f93ts = null;
    private String ver = "1.00";
    private String xid = null;

    public String getVer() {
        return this.ver;
    }

    public void setVer(String ver2) {
        this.ver = ver2;
    }

    public Long getTs() {
        return this.f93ts;
    }

    public void setTs(Long ts) {
        this.f93ts = ts;
    }

    public Long getSeq() {
        return this.seq;
    }

    public void setSeq(Long seq2) {
        this.seq = seq2;
    }

    public String getXid() {
        return this.xid;
    }

    public void setXid(String xid2) {
        this.xid = xid2;
    }

    public String getNonce() {
        return this.nonce;
    }

    public void setNonce(String nonce2) {
        this.nonce = nonce2;
    }

    public String getSign() {
        return this.sign;
    }

    public void setSign(String sign2) {
        this.sign = sign2;
    }

    public String getProt_type() {
        return this.prot_type;
    }

    public void setProt_type(String prot_type2) {
        this.prot_type = prot_type2;
    }

    public String getProt_ver() {
        return this.prot_ver;
    }

    public void setProt_ver(String prot_ver2) {
        this.prot_ver = prot_ver2;
    }

    public String getOp() {
        return this.f92op;
    }

    public void setOp(String op) {
        this.f92op = op;
    }

    public YZXDCAPOption getOpt() {
        return this.opt;
    }

    public void setOpt(YZXDCAPOption opt2) {
        this.opt = opt2;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port2) {
        this.port = port2;
    }

    public boolean isBill() {
        return this.isBill;
    }

    public void setBill(boolean isBill2) {
        this.isBill = isBill2;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data2) {
        this.data = data2;
    }

    public YZXDCAPError getError() {
        return this.error;
    }

    public void setError(YZXDCAPError error2) {
        this.error = error2;
    }

    public int getFlag() {
        return this.flag;
    }

    public void setFlag(int flag2) {
        this.flag = flag2;
    }
}
