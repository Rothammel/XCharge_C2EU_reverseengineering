package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;

public class PaymentDetail extends JsonBean<PaymentDetail> {
    private long accountId = 0;
    private long channel = 0;

    /* renamed from: id */
    private long f83id = 0;
    private String method = null;
    private int paidAmount = 0;
    private String payTime = null;
    private String paymentOrderId = null;
    private boolean succeeded = false;

    public long getId() {
        return this.f83id;
    }

    public void setId(long id) {
        this.f83id = id;
    }

    public int getPaidAmount() {
        return this.paidAmount;
    }

    public void setPaidAmount(int paidAmount2) {
        this.paidAmount = paidAmount2;
    }

    public String getPayTime() {
        return this.payTime;
    }

    public void setPayTime(String payTime2) {
        this.payTime = payTime2;
    }

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method2) {
        this.method = method2;
    }

    public long getChannel() {
        return this.channel;
    }

    public void setChannel(long channel2) {
        this.channel = channel2;
    }

    public String getPaymentOrderId() {
        return this.paymentOrderId;
    }

    public void setPaymentOrderId(String paymentOrderId2) {
        this.paymentOrderId = paymentOrderId2;
    }

    public long getAccountId() {
        return this.accountId;
    }

    public void setAccountId(long accountId2) {
        this.accountId = accountId2;
    }

    public boolean isSucceeded() {
        return this.succeeded;
    }

    public void setSucceeded(boolean succeeded2) {
        this.succeeded = succeeded2;
    }
}
