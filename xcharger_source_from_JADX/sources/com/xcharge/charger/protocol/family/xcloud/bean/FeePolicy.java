package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

public class FeePolicy extends JsonBean<FeePolicy> {
    public static final int TIME_PRICE_SIZE = 6;
    private Long createTime = null;
    private String currencyType = null;
    private Long deviceGroupId = null;
    private Price fixedPrice = null;

    /* renamed from: id */
    private Long f80id = null;
    private FeeRange maxFee = null;
    private FeeRange minFee = null;
    private ArrayList<ArrayList<Integer>> timedPrice = null;
    private String title = null;
    private Long updateTime = null;

    public Long getId() {
        return this.f80id;
    }

    public void setId(Long id) {
        this.f80id = id;
    }

    public Long getDeviceGroupId() {
        return this.deviceGroupId;
    }

    public void setDeviceGroupId(Long deviceGroupId2) {
        this.deviceGroupId = deviceGroupId2;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title2) {
        this.title = title2;
    }

    public String getCurrencyType() {
        return this.currencyType;
    }

    public void setCurrencyType(String currencyType2) {
        this.currencyType = currencyType2;
    }

    public FeeRange getMinFee() {
        return this.minFee;
    }

    public void setMinFee(FeeRange minFee2) {
        this.minFee = minFee2;
    }

    public FeeRange getMaxFee() {
        return this.maxFee;
    }

    public void setMaxFee(FeeRange maxFee2) {
        this.maxFee = maxFee2;
    }

    public Price getFixedPrice() {
        return this.fixedPrice;
    }

    public void setFixedPrice(Price fixedPrice2) {
        this.fixedPrice = fixedPrice2;
    }

    public ArrayList<ArrayList<Integer>> getTimedPrice() {
        return this.timedPrice;
    }

    public void setTimedPrice(ArrayList<ArrayList<Integer>> timedPrice2) {
        this.timedPrice = timedPrice2;
    }

    public Long getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Long createTime2) {
        this.createTime = createTime2;
    }

    public Long getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Long updateTime2) {
        this.updateTime = updateTime2;
    }
}
