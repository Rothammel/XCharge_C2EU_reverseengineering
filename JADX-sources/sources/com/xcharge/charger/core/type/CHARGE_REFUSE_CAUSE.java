package com.xcharge.charger.core.type;

import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;

/* loaded from: classes.dex */
public enum CHARGE_REFUSE_CAUSE {
    UNDEFINED("UNDEFINED"),
    BAD_QRCODE(DeviceError.BAD_QRCODE),
    USERGROUP_FORBIDDEN(DeviceError.USERGROUP_FORBIDDEN),
    BILL_UNPAID(DeviceError.BILL_UNPAID),
    CHARGE_UNFINISHED(DeviceError.CHARGE_UNFINISHED),
    NOT_RESERVED(DeviceError.NOT_RESERVED),
    NOT_QUEUED(DeviceError.NOT_QUEUED),
    RESERVE_UNDUE(DeviceError.RESERVE_UNDUE),
    QUEUE_UNDUE(DeviceError.QUEUE_UNDUE),
    RESERVE_TIMEOUT(DeviceError.RESERVE_TIMEOUT),
    QUEUE_TIMEOUT(DeviceError.QUEUE_TIMEOUT),
    BUSY(DeviceError.BUSY),
    PORT_FORBIDEN("PORT_FORBIDEN"),
    NO_FEERATE("NO_FEERATE"),
    AUTH_TIMEOUT("AUTH_TIMEOUT"),
    AUTH_REFUSE("AUTH_REFUSE"),
    BALANCE_INSUFFICIENT(DeviceError.BALANCE_INSUFFICIENT),
    BAD_IDCARD(DeviceError.BAD_IDCARD),
    EXCEPT("EXCEPT"),
    NOT_PLUGGED(DeviceError.NOT_PLUGGED);
    
    private String cause;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static CHARGE_REFUSE_CAUSE[] valuesCustom() {
        CHARGE_REFUSE_CAUSE[] valuesCustom = values();
        int length = valuesCustom.length;
        CHARGE_REFUSE_CAUSE[] charge_refuse_causeArr = new CHARGE_REFUSE_CAUSE[length];
        System.arraycopy(valuesCustom, 0, charge_refuse_causeArr, 0, length);
        return charge_refuse_causeArr;
    }

    CHARGE_REFUSE_CAUSE(String cause) {
        this.cause = null;
        this.cause = cause;
    }

    public String getCause() {
        return this.cause;
    }
}
