package com.xcharge.charger.core.api.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class DCAPMessage extends JsonBean<DCAPMessage> {
    public static final String ACTION_DCAP_CONFIRM = "com.xcharge.charger.service.core.ACTION_DCAP_CONFIRM";
    public static final String ACTION_DCAP_INDICATE = "com.xcharge.charger.service.core.ACTION_DCAP_INDICATE";
    public static final String ACTION_DCAP_REQUEST = "com.xcharge.charger.service.core.ACTION_DCAP_REQUEST";
    public static final String ACTION_DCAP_RESPONSE = "com.xcharge.charger.service.core.ACTION_DCAP_RESPONSE";
    public static final String TYPE_CAP = "cap";
    public static final String TYPE_DDCP = "ddcp";
    public static final String VERSION = "1.0";
    private String ver = "1.0";
    private String from = null;
    private String to = null;
    private long ctime = System.currentTimeMillis();
    private long seq = 0;
    private String xid = null;
    private String sign = null;
    private int encrypt = 0;
    private String type = null;
    private Object data = null;

    public String getVer() {
        return this.ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return this.to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public long getCtime() {
        return this.ctime;
    }

    public void setCtime(long ctime) {
        this.ctime = ctime;
    }

    public long getSeq() {
        return this.seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public String getXid() {
        return this.xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }

    public String getSign() {
        return this.sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public int getEncrypt() {
        return this.encrypt;
    }

    public void setEncrypt(int encrypt) {
        this.encrypt = encrypt;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}