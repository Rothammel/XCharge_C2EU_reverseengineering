package com.xcharge.charger.core.api.bean;

import com.xcharge.common.bean.JsonBean;

public class DCAPMessage extends JsonBean<DCAPMessage> {
    public static final String ACTION_DCAP_CONFIRM = "com.xcharge.charger.service.core.ACTION_DCAP_CONFIRM";
    public static final String ACTION_DCAP_INDICATE = "com.xcharge.charger.service.core.ACTION_DCAP_INDICATE";
    public static final String ACTION_DCAP_REQUEST = "com.xcharge.charger.service.core.ACTION_DCAP_REQUEST";
    public static final String ACTION_DCAP_RESPONSE = "com.xcharge.charger.service.core.ACTION_DCAP_RESPONSE";
    public static final String TYPE_CAP = "cap";
    public static final String TYPE_DDCP = "ddcp";
    public static final String VERSION = "1.0";
    private long ctime = System.currentTimeMillis();
    private Object data = null;
    private int encrypt = 0;
    private String from = null;
    private long seq = 0;
    private String sign = null;

    /* renamed from: to */
    private String f44to = null;
    private String type = null;
    private String ver = "1.0";
    private String xid = null;

    public String getVer() {
        return this.ver;
    }

    public void setVer(String ver2) {
        this.ver = ver2;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from2) {
        this.from = from2;
    }

    public String getTo() {
        return this.f44to;
    }

    public void setTo(String to) {
        this.f44to = to;
    }

    public long getCtime() {
        return this.ctime;
    }

    public void setCtime(long ctime2) {
        this.ctime = ctime2;
    }

    public long getSeq() {
        return this.seq;
    }

    public void setSeq(long seq2) {
        this.seq = seq2;
    }

    public String getXid() {
        return this.xid;
    }

    public void setXid(String xid2) {
        this.xid = xid2;
    }

    public String getSign() {
        return this.sign;
    }

    public void setSign(String sign2) {
        this.sign = sign2;
    }

    public int getEncrypt() {
        return this.encrypt;
    }

    public void setEncrypt(int encrypt2) {
        this.encrypt = encrypt2;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type2) {
        this.type = type2;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data2) {
        this.data = data2;
    }
}
