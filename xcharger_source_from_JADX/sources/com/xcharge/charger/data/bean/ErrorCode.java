package com.xcharge.charger.data.bean;

import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

public class ErrorCode extends JsonBean<ErrorCode> {
    public static final int EC_CAR_ERROR = 50000;
    public static final int EC_CAR_STOP_CHARGE = 50001;
    public static final int EC_CHARGE_ERROR = 10000;
    public static final int EC_CHARGE_NEED_PLUGIN = 10004;
    public static final int EC_CLOUD_AUTH_REFUSED = 10003;
    public static final int EC_DEVICE_AMP_ERROR = 30015;
    public static final int EC_DEVICE_BUSY = 10002;
    public static final int EC_DEVICE_COMM_ERROR = 30018;
    public static final int EC_DEVICE_EMERGENCY_STOP = 30013;
    public static final int EC_DEVICE_ERROR = 30000;
    public static final int EC_DEVICE_LOST_PHASE = 30012;
    public static final int EC_DEVICE_NOT_INIT = 30010;
    public static final int EC_DEVICE_NO_GROUND = 30011;
    public static final int EC_DEVICE_PORT_UNAVAILABLE = 30002;
    public static final int EC_DEVICE_POWER_LEAK = 30017;
    public static final int EC_DEVICE_SN_NOT_EXIST = 30001;
    public static final int EC_DEVICE_TEMP_ERROR = 30016;
    public static final int EC_DEVICE_VOLT_ERROR = 30014;
    public static final int EC_INTERNAL_ERROR = 20500;
    public static final int EC_NFC_BIND_CARD_FAIL = 40006;
    public static final int EC_NFC_CARD_AUTH_FAIL = 40015;
    public static final int EC_NFC_CARD_BALANCE_NOT_ENOUGH = 40020;
    public static final int EC_NFC_CARD_CONSUME_FAIL = 40019;
    public static final int EC_NFC_CARD_RELEASE_FAIL = 40014;
    public static final int EC_NFC_CARD_RESERVED = 40012;
    public static final int EC_NFC_CARD_RESERVE_FAIL = 40013;
    public static final int EC_NFC_CARD_UNPAID_BALANCE_NOT_ENOUGH = 40018;
    public static final int EC_NFC_CARD_UNPAID_CONSUME_FAIL = 40017;
    public static final int EC_NFC_CARD_UNPAID_CONSUME_OK = 40016;
    public static final int EC_NFC_CHARGE_REFUSE = 40008;
    public static final int EC_NFC_ERROR = 40000;
    public static final int EC_NFC_INIT_FAIL = 40029;
    public static final int EC_NFC_INVALID_MANAGE_CARD_DATA = 40003;
    public static final int EC_NFC_INVALID_PORT = 40010;
    public static final int EC_NFC_NOT_GROUP_MODE = 40023;
    public static final int EC_NFC_NOT_INIT_CHARGE_CARD = 40028;
    public static final int EC_NFC_NOT_PERMIT_SWIPE = 40026;
    public static final int EC_NFC_NOT_PERSONAL_MODE = 40024;
    public static final int EC_NFC_READ_FAILED = 40027;
    public static final int EC_NFC_RECOVERY_SIGN_FAIL = 40021;
    public static final int EC_NFC_REWRITED_BALANCE = 40022;
    public static final int EC_NFC_SCAN_REFUSE = 40009;
    public static final int EC_NFC_SET_FAIL = 40004;
    public static final int EC_NFC_SET_REFUSE = 40007;
    public static final int EC_NFC_SIGN_ERROR = 40005;
    public static final int EC_NFC_SWIPE_PROCESSING = 40030;
    public static final int EC_NFC_UNAVAILABLE_CLOUD = 40025;
    public static final int EC_NFC_UNAVAILABLE_KEYSEED = 40002;
    public static final int EC_NFC_UNPAID_BILL = 40011;
    public static final int EC_NFC_UNRECOGNIZED_CARD = 40001;
    public static final int EC_NOT_IMPLEMENT = 20501;
    public static final int EC_NOT_REPORTED_CHARGE = 10001;
    public static final int EC_OK = 200;
    public static final int EC_UI_OUT_OF_DISTURB = 20503;
    public static final int EC_UNAVAILABLE_PLATFORM_SETTING = 20502;
    public static final int EC_UPGRADE_APK_NOT_FOUND = 60005;
    public static final int EC_UPGRADE_APK_NOT_VALID = 60007;
    public static final int EC_UPGRADE_APK_SIGN_ERROR = 60006;
    public static final int EC_UPGRADE_APK_VERIFY_ERROR = 60008;
    public static final int EC_UPGRADE_CHARGE_REFUSE = 60010;
    public static final int EC_UPGRADE_DOWNLOAD_FAIL = 60002;
    public static final int EC_UPGRADE_ERROR = 60000;
    public static final int EC_UPGRADE_FIREWARE_VERIFY_ERROR = 60009;
    public static final int EC_UPGRADE_NOT_INTEGRATED = 60003;
    public static final int EC_UPGRADE_NOT_SUPPORTED_VERSION = 60001;
    public static final int EC_UPGRADE_UNZIP_FAIL = 60004;
    private int code = 200;
    private HashMap data = null;

    public ErrorCode() {
    }

    public ErrorCode(int code2) {
        this.code = code2;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code2) {
        this.code = code2;
    }

    public HashMap getData() {
        return this.data;
    }

    public void setData(HashMap data2) {
        this.data = data2;
    }
}
