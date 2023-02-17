package com.xcharge.charger.core.bean;

import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.type.DEVICE_STATUS;
import com.xcharge.charger.data.bean.type.GUN_LOCK_MODE;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ChargeSession extends JsonBean<ChargeSession> {
    private ChargeBill chargeBill = null;
    private String device_id = null;
    private DEVICE_STATUS deviceStatus = null;
    private boolean deviceAuth = false;
    private boolean isEnteredNormalCharging = false;
    private ErrorCode latestResumedError = null;
    private boolean isAnyErrorExist = false;
    private int timeout_plugin = -1;
    private int timeout_plugout = -1;
    private int timeout_start = -1;
    private long timeout_init_advert = 0;
    private Long stop_request_seq = null;
    private Long expected_resopnse = null;
    private DCAPMessage confirm4Auth = null;
    private int stop_retry = 0;
    private long latestPowerMeterTimestamp = 0;
    private long latestDelayMeterTimestamp = 0;
    private boolean isDelayWaitStarted = false;
    private boolean isDelayStarted = false;
    private boolean plugined = false;
    private GUN_LOCK_MODE gunMode = null;
    private boolean emergencyStopped = false;
    private boolean parklocked = false;
    private double delayPrice = 0.0d;
    private Long userReservedTime = null;
    private int latestCostDelay = 0;

    public ChargeBill getChargeBill() {
        return this.chargeBill;
    }

    public void setChargeBill(ChargeBill chargeBill) {
        this.chargeBill = chargeBill;
    }

    public String getDevice_id() {
        return this.device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public Long getExpected_resopnse() {
        return this.expected_resopnse;
    }

    public void setExpected_resopnse(Long expected_resopnse) {
        this.expected_resopnse = expected_resopnse;
    }

    public boolean isParklocked() {
        return this.parklocked;
    }

    public void setParklocked(boolean parklocked) {
        this.parklocked = parklocked;
    }

    public int getStop_retry() {
        return this.stop_retry;
    }

    public void setStop_retry(int stop_retry) {
        this.stop_retry = stop_retry;
    }

    public void incStop_retry() {
        this.stop_retry++;
    }

    public int getTimeout_start() {
        return this.timeout_start;
    }

    public void setTimeout_start(int timeout_start) {
        this.timeout_start = timeout_start;
    }

    public int getTimeout_plugout() {
        return this.timeout_plugout;
    }

    public void setTimeout_plugout(int timeout_plugout) {
        this.timeout_plugout = timeout_plugout;
    }

    public long getTimeout_init_advert() {
        return this.timeout_init_advert;
    }

    public void setTimeout_init_advert(long timeout_init_advert) {
        this.timeout_init_advert = timeout_init_advert;
    }

    public Long getStop_request_seq() {
        return this.stop_request_seq;
    }

    public void setStop_request_seq(Long stop_request_seq) {
        this.stop_request_seq = stop_request_seq;
    }

    public int getTimeout_plugin() {
        return this.timeout_plugin;
    }

    public void setTimeout_plugin(int timeout_plugin) {
        this.timeout_plugin = timeout_plugin;
    }

    public DCAPMessage getConfirm4Auth() {
        return this.confirm4Auth;
    }

    public void setConfirm4Auth(DCAPMessage confirm4Auth) {
        this.confirm4Auth = confirm4Auth;
    }

    public boolean isPlugined() {
        return this.plugined;
    }

    public void setPlugined(boolean plugined) {
        this.plugined = plugined;
    }

    public boolean isEmergencyStopped() {
        return this.emergencyStopped;
    }

    public void setEmergencyStopped(boolean emergencyStopped) {
        this.emergencyStopped = emergencyStopped;
    }

    public DEVICE_STATUS getDeviceStatus() {
        return this.deviceStatus;
    }

    public void setDeviceStatus(DEVICE_STATUS deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public boolean isDeviceAuth() {
        return this.deviceAuth;
    }

    public void setDeviceAuth(boolean deviceAuth) {
        this.deviceAuth = deviceAuth;
    }

    public boolean isEnteredNormalCharging() {
        return this.isEnteredNormalCharging;
    }

    public void setEnteredNormalCharging(boolean isEnteredNormalCharging) {
        this.isEnteredNormalCharging = isEnteredNormalCharging;
    }

    public ErrorCode getLatestResumedError() {
        return this.latestResumedError;
    }

    public void setLatestResumedError(ErrorCode latestResumedError) {
        this.latestResumedError = latestResumedError;
    }

    public boolean isAnyErrorExist() {
        return this.isAnyErrorExist;
    }

    public void setAnyErrorExist(boolean isAnyErrorExist) {
        this.isAnyErrorExist = isAnyErrorExist;
    }

    public long getLatestPowerMeterTimestamp() {
        return this.latestPowerMeterTimestamp;
    }

    public void setLatestPowerMeterTimestamp(long latestPowerMeterTimestamp) {
        this.latestPowerMeterTimestamp = latestPowerMeterTimestamp;
    }

    public long getLatestDelayMeterTimestamp() {
        return this.latestDelayMeterTimestamp;
    }

    public void setLatestDelayMeterTimestamp(long latestDelayMeterTimestamp) {
        this.latestDelayMeterTimestamp = latestDelayMeterTimestamp;
    }

    public boolean isDelayWaitStarted() {
        return this.isDelayWaitStarted;
    }

    public void setDelayWaitStarted(boolean isDelayWaitStarted) {
        this.isDelayWaitStarted = isDelayWaitStarted;
    }

    public boolean isDelayStarted() {
        return this.isDelayStarted;
    }

    public void setDelayStarted(boolean isDelayStarted) {
        this.isDelayStarted = isDelayStarted;
    }

    public GUN_LOCK_MODE getGunMode() {
        return this.gunMode;
    }

    public void setGunMode(GUN_LOCK_MODE gunMode) {
        this.gunMode = gunMode;
    }

    public double getDelayPrice() {
        return this.delayPrice;
    }

    public void setDelayPrice(double delayPrice) {
        this.delayPrice = delayPrice;
    }

    public Long getUserReservedTime() {
        return this.userReservedTime;
    }

    public void setUserReservedTime(Long userReservedTime) {
        this.userReservedTime = userReservedTime;
    }

    public int getLatestCostDelay() {
        return this.latestCostDelay;
    }

    public void setLatestCostDelay(int latestCostDelay) {
        this.latestCostDelay = latestCostDelay;
    }
}
