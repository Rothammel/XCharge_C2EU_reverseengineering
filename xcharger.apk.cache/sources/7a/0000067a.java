package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class FeePolicy extends JsonBean<FeePolicy> {
    public static final int TIME_PRICE_SIZE = 6;
    private Long id = null;
    private Long deviceGroupId = null;
    private String title = null;
    private String currencyType = null;
    private FeeRange minFee = null;
    private FeeRange maxFee = null;
    private Price fixedPrice = null;
    private ArrayList<ArrayList<Integer>> timedPrice = null;
    private Long createTime = null;
    private Long updateTime = null;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeviceGroupId() {
        return this.deviceGroupId;
    }

    public void setDeviceGroupId(Long deviceGroupId) {
        this.deviceGroupId = deviceGroupId;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCurrencyType() {
        return this.currencyType;
    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }

    public FeeRange getMinFee() {
        return this.minFee;
    }

    public void setMinFee(FeeRange minFee) {
        this.minFee = minFee;
    }

    public FeeRange getMaxFee() {
        return this.maxFee;
    }

    public void setMaxFee(FeeRange maxFee) {
        this.maxFee = maxFee;
    }

    public Price getFixedPrice() {
        return this.fixedPrice;
    }

    public void setFixedPrice(Price fixedPrice) {
        this.fixedPrice = fixedPrice;
    }

    public ArrayList<ArrayList<Integer>> getTimedPrice() {
        return this.timedPrice;
    }

    public void setTimedPrice(ArrayList<ArrayList<Integer>> timedPrice) {
        this.timedPrice = timedPrice;
    }

    public Long getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}