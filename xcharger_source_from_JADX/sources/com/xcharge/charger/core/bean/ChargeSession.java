package com.xcharge.charger.core.bean;

import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.type.DEVICE_STATUS;
import com.xcharge.charger.data.bean.type.GUN_LOCK_MODE;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.common.bean.JsonBean;

public class ChargeSession extends JsonBean<ChargeSession> {
    private ChargeBill chargeBill = null;
    private DCAPMessage confirm4Auth = null;
    private double delayPrice = 0.0d;
    private boolean deviceAuth = false;
    private DEVICE_STATUS deviceStatus = null;
    private String device_id = null;
    private boolean emergencyStopped = false;
    private Long expected_resopnse = null;
    private GUN_LOCK_MODE gunMode = null;
    private boolean isAnyErrorExist = false;
    private boolean isDelayStarted = false;
    private boolean isDelayWaitStarted = false;
    private boolean isEnteredNormalCharging = false;
    private int latestCostDelay = 0;
    private long latestDelayMeterTimestamp = 0;
    private long latestPowerMeterTimestamp = 0;
    private ErrorCode latestResumedError = null;
    private boolean parklocked = false;
    private boolean plugined = false;
    private Long stop_request_seq = null;
    private int stop_retry = 0;
    private long timeout_init_advert = 0;
    private int timeout_plugin = -1;
    private int timeout_plugout = -1;
    private int timeout_start = -1;
    private Long userReservedTime = null;

    public ChargeBill getChargeBill() {
        return this.chargeBill;
    }

    public void setChargeBill(ChargeBill chargeBill2) {
        this.chargeBill = chargeBill2;
    }

    public String getDevice_id() {
        return this.device_id;
    }

    public void setDevice_id(String device_id2) {
        this.device_id = device_id2;
    }

    public Long getExpected_resopnse() {
        return this.expected_resopnse;
    }

    public void setExpected_resopnse(Long expected_resopnse2) {
        this.expected_resopnse = expected_resopnse2;
    }

    public boolean isParklocked() {
        return this.parklocked;
    }

    public void setParklocked(boolean parklocked2) {
        this.parklocked = parklocked2;
    }

    public int getStop_retry() {
        return this.stop_retry;
    }

    public void setStop_retry(int stop_retry2) {
        this.stop_retry = stop_retry2;
    }

    public void incStop_retry() {
        this.stop_retry++;
    }

    public int getTimeout_start() {
        return this.timeout_start;
    }

    public void setTimeout_start(int timeout_start2) {
        this.timeout_start = timeout_start2;
    }

    public int getTimeout_plugout() {
        return this.timeout_plugout;
    }

    public void setTimeout_plugout(int timeout_plugout2) {
        this.timeout_plugout = timeout_plugout2;
    }

    public long getTimeout_init_advert() {
        return this.timeout_init_advert;
    }

    public void setTimeout_init_advert(long timeout_init_advert2) {
        this.timeout_init_advert = timeout_init_advert2;
    }

    public Long getStop_request_seq() {
        return this.stop_request_seq;
    }

    public void setStop_request_seq(Long stop_request_seq2) {
        this.stop_request_seq = stop_request_seq2;
    }

    public int getTimeout_plugin() {
        return this.timeout_plugin;
    }

    public void setTimeout_plugin(int timeout_plugin2) {
        this.timeout_plugin = timeout_plugin2;
    }

    public DCAPMessage getConfirm4Auth() {
        return this.confirm4Auth;
    }

    public void setConfirm4Auth(DCAPMessage confirm4Auth2) {
        this.confirm4Auth = confirm4Auth2;
    }

    public boolean isPlugined() {
        return this.plugined;
    }

    public void setPlugined(boolean plugined2) {
        this.plugined = plugined2;
    }

    public boolean isEmergencyStopped() {
        return this.emergencyStopped;
    }

    public void setEmergencyStopped(boolean emergencyStopped2) {
        this.emergencyStopped = emergencyStopped2;
    }

    public DEVICE_STATUS getDeviceStatus() {
        return this.deviceStatus;
    }

    public void setDeviceStatus(DEVICE_STATUS deviceStatus2) {
        this.deviceStatus = deviceStatus2;
    }

    public boolean isDeviceAuth() {
        return this.deviceAuth;
    }

    public void setDeviceAuth(boolean deviceAuth2) {
        this.deviceAuth = deviceAuth2;
    }

    public boolean isEnteredNormalCharging() {
        return this.isEnteredNormalCharging;
    }

    public void setEnteredNormalCharging(boolean isEnteredNormalCharging2) {
        this.isEnteredNormalCharging = isEnteredNormalCharging2;
    }

    public ErrorCode getLatestResumedError() {
        return this.latestResumedError;
    }

    public void setLatestResumedError(ErrorCode latestResumedError2) {
        this.latestResumedError = latestResumedError2;
    }

    public boolean isAnyErrorExist() {
        return this.isAnyErrorExist;
    }

    public void setAnyErrorExist(boolean isAnyErrorExist2) {
        this.isAnyErrorExist = isAnyErrorExist2;
    }

    public long getLatestPowerMeterTimestamp() {
        return this.latestPowerMeterTimestamp;
    }

    public void setLatestPowerMeterTimestamp(long latestPowerMeterTimestamp2) {
        this.latestPowerMeterTimestamp = latestPowerMeterTimestamp2;
    }

    public long getLatestDelayMeterTimestamp() {
        return this.latestDelayMeterTimestamp;
    }

    public void setLatestDelayMeterTimestamp(long latestDelayMeterTimestamp2) {
        this.latestDelayMeterTimestamp = latestDelayMeterTimestamp2;
    }

    public boolean isDelayWaitStarted() {
        return this.isDelayWaitStarted;
    }

    public void setDelayWaitStarted(boolean isDelayWaitStarted2) {
        this.isDelayWaitStarted = isDelayWaitStarted2;
    }

    public boolean isDelayStarted() {
        return this.isDelayStarted;
    }

    public void setDelayStarted(boolean isDelayStarted2) {
        this.isDelayStarted = isDelayStarted2;
    }

    public GUN_LOCK_MODE getGunMode() {
        return this.gunMode;
    }

    public void setGunMode(GUN_LOCK_MODE gunMode2) {
        this.gunMode = gunMode2;
    }

    public double getDelayPrice() {
        return this.delayPrice;
    }

    public void setDelayPrice(double delayPrice2) {
        this.delayPrice = delayPrice2;
    }

    public Long getUserReservedTime() {
        return this.userReservedTime;
    }

    public void setUserReservedTime(Long userReservedTime2) {
        this.userReservedTime = userReservedTime2;
    }

    public int getLatestCostDelay() {
        return this.latestCostDelay;
    }

    public void setLatestCostDelay(int latestCostDelay2) {
        this.latestCostDelay = latestCostDelay2;
    }
}
