package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class PaymentDetail extends JsonBean<PaymentDetail> {
    private long id = 0;
    private int paidAmount = 0;
    private String payTime = null;
    private String method = null;
    private long channel = 0;
    private String paymentOrderId = null;
    private long accountId = 0;
    private boolean succeeded = false;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPaidAmount() {
        return this.paidAmount;
    }

    public void setPaidAmount(int paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getPayTime() {
        return this.payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public long getChannel() {
        return this.channel;
    }

    public void setChannel(long channel) {
        this.channel = channel;
    }

    public String getPaymentOrderId() {
        return this.paymentOrderId;
    }

    public void setPaymentOrderId(String paymentOrderId) {
        this.paymentOrderId = paymentOrderId;
    }

    public long getAccountId() {
        return this.accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public boolean isSucceeded() {
        return this.succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }
}
