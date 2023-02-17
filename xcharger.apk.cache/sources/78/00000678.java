package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class DeviceError extends JsonBean<DeviceError> {
    public static final String ADMIN_REMOTE = "ADMIN_REMOTE";
    public static final String AUTO_STOP = "AUTO";
    public static final String BAD_IDCARD = "BAD_IDCARD";
    public static final String BAD_QRCODE = "BAD_QRCODE";
    public static final String BALANCE_INSUFFICIENT = "BALANCE_INSUFFICIENT";
    public static final String BILL_UNPAID = "BILL_UNPAID";
    public static final String BUSY = "BUSY";
    public static final String CHARGE_NOT_FOUND = "CHARGE_NOT_FOUND";
    public static final String CHARGE_UNFINISHED = "CHARGE_UNFINISHED";
    public static final String CHECK_TIMEOUT = "CHECK_TIMEOUT";
    public static final String DISABLED = "DISABLED";
    public static final String DOWNLOAD_FAILED = "DOWNLOAD_FAILED";
    public static final String ERROR_STOP = "ERROR";
    public static final String JSON_ERROR = "JSON_ERROR";
    public static final String NOT_IDLE = "NOT_IDLE";
    public static final String NOT_PLUGGED = "NOT_PLUGGED";
    public static final String NOT_QUEUED = "NOT_QUEUED";
    public static final String NOT_RESERVED = "NOT_RESERVED";
    public static final String NO_FEEPOLICY = "NO_FEEPOLICY";
    public static final String NO_FUND = "NO_FUND";
    public static final String OCCUPIED = "OCCUPIED";
    public static final String OTHER = "OTHER";
    public static final String PARK_OCCUPY = "4001";
    public static final String PLUG_TIMEOUT = "PLUG_TIMEOUT";
    public static final String QUEUE_TIMEOUT = "QUEUE_TIMEOUT";
    public static final String QUEUE_UNDUE = "QUEUE_UNDUE";
    public static final String RESERVE_TIMEOUT = "RESERVE_TIMEOUT";
    public static final String RESERVE_UNDUE = "RESERVE_UNDUE";
    public static final String SYSTEM_ERROR = "ERROR";
    public static final String TIMED_CHARGE_CANCEL = "TIMED_CHARGE_CANCEL";
    public static final String TIMED_CHARGE_ERROR = "TIMED_CHARGE_ERROR";
    public static final String UPLOAD_LOG_URL_ERROR = "10001";
    public static final String USERGROUP_FORBIDDEN = "USERGROUP_FORBIDDEN";
    public static final String USER_CANCEL = "USER_CANCEL";
    public static final String USER_LOCAL = "USER_LOCAL";
    public static final String USER_REMOTE = "USER_REMOTE";
    public static final String WRONG_VERSION = "WRONG_VERSION";
    private String code;
    private Object data;
    private String msg;

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public DeviceError(String code, String msg, Object data) {
        this.code = null;
        this.msg = null;
        this.data = null;
        this.code = code;
        this.data = data;
        this.msg = msg;
    }
}