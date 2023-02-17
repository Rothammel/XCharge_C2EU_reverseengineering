package com.xcharge.charger.protocol.monitor.util;

import android.support.v4.media.TransportMediator;
import android.support.v4.util.TimeUtils;
import com.alibaba.sdk.android.oss_android_sdk.BuildConfig;
import com.google.zxing.aztec.encoder.Encoder;
import com.google.zxing.pdf417.PDF417Common;
import com.nostra13.universalimageloader.utils.IoUtils;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.device.c2.bean.PortRuntimeData;
import com.xcharge.charger.protocol.monitor.bean.ErrorCodeMapping;
import com.xcharge.charger.protocol.monitor.bean.YZXProperty;
import it.sauronsoftware.ftp4j.FTPCodes;
import org.java_websocket.WebSocket;

/* loaded from: classes.dex */
public class FieldConfigUtils {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$protocol$monitor$bean$ErrorCodeMapping;

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$protocol$monitor$bean$ErrorCodeMapping() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$protocol$monitor$bean$ErrorCodeMapping;
        if (iArr == null) {
            iArr = new int[ErrorCodeMapping.valuesCustom().length];
            try {
                iArr[ErrorCodeMapping.E_ACCOUNT_CREATE_FAIL.ordinal()] = 63;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[ErrorCodeMapping.E_ACCOUNT_EMAIL_SMS.ordinal()] = 62;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[ErrorCodeMapping.E_ACCOUNT_EMAIL_SMS_INVALID.ordinal()] = 71;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[ErrorCodeMapping.E_AC_CHARGER_ERROR_AMP.ordinal()] = 130;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[ErrorCodeMapping.E_AC_CHARGER_ERROR_COMM.ordinal()] = 133;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[ErrorCodeMapping.E_AC_CHARGER_ERROR_EMERGENCY_STOP.ordinal()] = 128;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[ErrorCodeMapping.E_AC_CHARGER_ERROR_LEAK_AMP.ordinal()] = 132;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[ErrorCodeMapping.E_AC_CHARGER_ERROR_NOT_INIT.ordinal()] = 125;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[ErrorCodeMapping.E_AC_CHARGER_ERROR_NO_GROUND.ordinal()] = 126;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[ErrorCodeMapping.E_AC_CHARGER_ERROR_PHASE_LOST.ordinal()] = 127;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[ErrorCodeMapping.E_AC_CHARGER_ERROR_TEMPRATURE.ordinal()] = 131;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[ErrorCodeMapping.E_AC_CHARGER_ERROR_VOLT.ordinal()] = 129;
            } catch (NoSuchFieldError e12) {
            }
            try {
                iArr[ErrorCodeMapping.E_AMBIGUOUS_REQUEST.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                iArr[ErrorCodeMapping.E_API_NOT_EXIST.ordinal()] = 6;
            } catch (NoSuchFieldError e14) {
            }
            try {
                iArr[ErrorCodeMapping.E_APPKEY_NULL.ordinal()] = 14;
            } catch (NoSuchFieldError e15) {
            }
            try {
                iArr[ErrorCodeMapping.E_APPSECRET_NULL.ordinal()] = 15;
            } catch (NoSuchFieldError e16) {
            }
            try {
                iArr[ErrorCodeMapping.E_APP_AUTH_ERROR.ordinal()] = 9;
            } catch (NoSuchFieldError e17) {
            }
            try {
                iArr[ErrorCodeMapping.E_CAP_ENCAP_INVALID.ordinal()] = 91;
            } catch (NoSuchFieldError e18) {
            }
            try {
                iArr[ErrorCodeMapping.E_CAP_VER_INVALID.ordinal()] = 90;
            } catch (NoSuchFieldError e19) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_FIN_OTHER.ordinal()] = 121;
            } catch (NoSuchFieldError e20) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_FIN_PLUGIN_TIMEOUT.ordinal()] = 123;
            } catch (NoSuchFieldError e21) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_FIN_USER_CANCEL.ordinal()] = 122;
            } catch (NoSuchFieldError e22) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_FIN_VEHICLE.ordinal()] = 124;
            } catch (NoSuchFieldError e23) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_NOT_EXIST.ordinal()] = 92;
            } catch (NoSuchFieldError e24) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_REFUSE_BAD_IDCARD.ordinal()] = 105;
            } catch (NoSuchFieldError e25) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_REFUSE_BAD_QRCODE.ordinal()] = 94;
            } catch (NoSuchFieldError e26) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_REFUSE_BALANCE_INSUFFICIENT.ordinal()] = 104;
            } catch (NoSuchFieldError e27) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_REFUSE_BILL_UNPAID.ordinal()] = 96;
            } catch (NoSuchFieldError e28) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_REFUSE_CHARGE_UNFINISHED.ordinal()] = 97;
            } catch (NoSuchFieldError e29) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_REFUSE_DEVICE_ERROR.ordinal()] = 107;
            } catch (NoSuchFieldError e30) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_REFUSE_IN_USE.ordinal()] = 108;
            } catch (NoSuchFieldError e31) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_REFUSE_NOT_QUEUED.ordinal()] = 101;
            } catch (NoSuchFieldError e32) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_REFUSE_NOT_RESERVED.ordinal()] = 98;
            } catch (NoSuchFieldError e33) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_REFUSE_OTHER.ordinal()] = 93;
            } catch (NoSuchFieldError e34) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_REFUSE_PORT_DISABLED.ordinal()] = 106;
            } catch (NoSuchFieldError e35) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_REFUSE_QUEUE_TIMEOUT.ordinal()] = 103;
            } catch (NoSuchFieldError e36) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_REFUSE_QUEUE_UNDUE.ordinal()] = 102;
            } catch (NoSuchFieldError e37) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_REFUSE_RESERVE_TIMEOUT.ordinal()] = 100;
            } catch (NoSuchFieldError e38) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_REFUSE_RESERVE_UNDUE.ordinal()] = 99;
            } catch (NoSuchFieldError e39) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_REFUSE_USE_FORBIDDEN.ordinal()] = 95;
            } catch (NoSuchFieldError e40) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_STOP_ADMIN_REMOTE.ordinal()] = 113;
            } catch (NoSuchFieldError e41) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_STOP_DEVICE_ERROR.ordinal()] = 119;
            } catch (NoSuchFieldError e42) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_STOP_FEERATE_NOT_EXIST.ordinal()] = 109;
            } catch (NoSuchFieldError e43) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_STOP_FULL.ordinal()] = 115;
            } catch (NoSuchFieldError e44) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_STOP_NO_FUND.ordinal()] = 120;
            } catch (NoSuchFieldError e45) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_STOP_OTHER.ordinal()] = 110;
            } catch (NoSuchFieldError e46) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_STOP_PLUGOUT.ordinal()] = 117;
            } catch (NoSuchFieldError e47) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_STOP_REBOOT.ordinal()] = 118;
            } catch (NoSuchFieldError e48) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_STOP_USER_LOCAL.ordinal()] = 112;
            } catch (NoSuchFieldError e49) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_STOP_USER_REMOTE.ordinal()] = 111;
            } catch (NoSuchFieldError e50) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_STOP_USER_SET.ordinal()] = 114;
            } catch (NoSuchFieldError e51) {
            }
            try {
                iArr[ErrorCodeMapping.E_CHARGE_STOP_VEHICLE.ordinal()] = 116;
            } catch (NoSuchFieldError e52) {
            }
            try {
                iArr[ErrorCodeMapping.E_CMD_ERROR.ordinal()] = 33;
            } catch (NoSuchFieldError e53) {
            }
            try {
                iArr[ErrorCodeMapping.E_CONENT_TYPE_ERROR.ordinal()] = 12;
            } catch (NoSuchFieldError e54) {
            }
            try {
                iArr[ErrorCodeMapping.E_DAP_ENCAP_INVALID.ordinal()] = 86;
            } catch (NoSuchFieldError e55) {
            }
            try {
                iArr[ErrorCodeMapping.E_DAP_VER_INVALID.ordinal()] = 85;
            } catch (NoSuchFieldError e56) {
            }
            try {
                iArr[ErrorCodeMapping.E_DCAP_ENCAP_INVALID.ordinal()] = 74;
            } catch (NoSuchFieldError e57) {
            }
            try {
                iArr[ErrorCodeMapping.E_DCAP_PROT_INVALID.ordinal()] = 77;
            } catch (NoSuchFieldError e58) {
            }
            try {
                iArr[ErrorCodeMapping.E_DCAP_SEQ_DUPLICATE.ordinal()] = 76;
            } catch (NoSuchFieldError e59) {
            }
            try {
                iArr[ErrorCodeMapping.E_DCAP_SIGN_INVALID.ordinal()] = 75;
            } catch (NoSuchFieldError e60) {
            }
            try {
                iArr[ErrorCodeMapping.E_DCAP_VER_INVALID.ordinal()] = 73;
            } catch (NoSuchFieldError e61) {
            }
            try {
                iArr[ErrorCodeMapping.E_DDAP_ENCAP_INVALID.ordinal()] = 79;
            } catch (NoSuchFieldError e62) {
            }
            try {
                iArr[ErrorCodeMapping.E_DDAP_VER_INVALID.ordinal()] = 78;
            } catch (NoSuchFieldError e63) {
            }
            try {
                iArr[ErrorCodeMapping.E_DEVICE_AUTH_ERROR.ordinal()] = 8;
            } catch (NoSuchFieldError e64) {
            }
            try {
                iArr[ErrorCodeMapping.E_DEVICE_KEY_TYPE.ordinal()] = 41;
            } catch (NoSuchFieldError e65) {
            }
            try {
                iArr[ErrorCodeMapping.E_DEVICE_MAC_ERROR.ordinal()] = 42;
            } catch (NoSuchFieldError e66) {
            }
            try {
                iArr[ErrorCodeMapping.E_DEVICE_NOT_EXIST.ordinal()] = 10;
            } catch (NoSuchFieldError e67) {
            }
            try {
                iArr[ErrorCodeMapping.E_DEVICE_REGISTER_FAIL.ordinal()] = 82;
            } catch (NoSuchFieldError e68) {
            }
            try {
                iArr[ErrorCodeMapping.E_DID_NULL.ordinal()] = 7;
            } catch (NoSuchFieldError e69) {
            }
            try {
                iArr[ErrorCodeMapping.E_END_OF_JSON.ordinal()] = 59;
            } catch (NoSuchFieldError e70) {
            }
            try {
                iArr[ErrorCodeMapping.E_EXEC_ID_NULL.ordinal()] = 55;
            } catch (NoSuchFieldError e71) {
            }
            try {
                iArr[ErrorCodeMapping.E_GEN_VERIFICATION_CODE.ordinal()] = 64;
            } catch (NoSuchFieldError e72) {
            }
            try {
                iArr[ErrorCodeMapping.E_HEARTBEAT_TIME_NULL.ordinal()] = 18;
            } catch (NoSuchFieldError e73) {
            }
            try {
                iArr[ErrorCodeMapping.E_INVALID_METHOD.ordinal()] = 16;
            } catch (NoSuchFieldError e74) {
            }
            try {
                iArr[ErrorCodeMapping.E_IP_NULL.ordinal()] = 47;
            } catch (NoSuchFieldError e75) {
            }
            try {
                iArr[ErrorCodeMapping.E_JSON_ERROR.ordinal()] = 5;
            } catch (NoSuchFieldError e76) {
            }
            try {
                iArr[ErrorCodeMapping.E_JSON_MAPPING_ERROR.ordinal()] = 23;
            } catch (NoSuchFieldError e77) {
            }
            try {
                iArr[ErrorCodeMapping.E_LATITUDE_NULL.ordinal()] = 26;
            } catch (NoSuchFieldError e78) {
            }
            try {
                iArr[ErrorCodeMapping.E_LOGIN_ACCOUNT_NULL.ordinal()] = 67;
            } catch (NoSuchFieldError e79) {
            }
            try {
                iArr[ErrorCodeMapping.E_LOGIN_PASSWORD_NULL.ordinal()] = 68;
            } catch (NoSuchFieldError e80) {
            }
            try {
                iArr[ErrorCodeMapping.E_LONGITUDE_NULL.ordinal()] = 25;
            } catch (NoSuchFieldError e81) {
            }
            try {
                iArr[ErrorCodeMapping.E_MAGIC_ERROR.ordinal()] = 30;
            } catch (NoSuchFieldError e82) {
            }
            try {
                iArr[ErrorCodeMapping.E_MAGIC_NULL.ordinal()] = 29;
            } catch (NoSuchFieldError e83) {
            }
            try {
                iArr[ErrorCodeMapping.E_MAIL_ACTIVE_URL_INVALID.ordinal()] = 69;
            } catch (NoSuchFieldError e84) {
            }
            try {
                iArr[ErrorCodeMapping.E_MQ_TOKEN_NULL.ordinal()] = 19;
            } catch (NoSuchFieldError e85) {
            }
            try {
                iArr[ErrorCodeMapping.E_NODE_NULL.ordinal()] = 39;
            } catch (NoSuchFieldError e86) {
            }
            try {
                iArr[ErrorCodeMapping.E_NOT_UPLOAD_FILE_ITEM.ordinal()] = 52;
            } catch (NoSuchFieldError e87) {
            }
            try {
                iArr[ErrorCodeMapping.E_OID_NULL.ordinal()] = 17;
            } catch (NoSuchFieldError e88) {
            }
            try {
                iArr[ErrorCodeMapping.E_OK.ordinal()] = 1;
            } catch (NoSuchFieldError e89) {
            }
            try {
                iArr[ErrorCodeMapping.E_OSS_UPLOAD_FAILED.ordinal()] = 53;
            } catch (NoSuchFieldError e90) {
            }
            try {
                iArr[ErrorCodeMapping.E_PARAM_ID_NOT_EXIST.ordinal()] = 24;
            } catch (NoSuchFieldError e91) {
            }
            try {
                iArr[ErrorCodeMapping.E_PARAM_INVALID.ordinal()] = 54;
            } catch (NoSuchFieldError e92) {
            }
            try {
                iArr[ErrorCodeMapping.E_PASSWORD_INVALID.ordinal()] = 61;
            } catch (NoSuchFieldError e93) {
            }
            try {
                iArr[ErrorCodeMapping.E_PID_NOT_EXIST.ordinal()] = 35;
            } catch (NoSuchFieldError e94) {
            }
            try {
                iArr[ErrorCodeMapping.E_PID_NULL.ordinal()] = 32;
            } catch (NoSuchFieldError e95) {
            }
            try {
                iArr[ErrorCodeMapping.E_PROP_ID_NOT_EXIST.ordinal()] = 88;
            } catch (NoSuchFieldError e96) {
            }
            try {
                iArr[ErrorCodeMapping.E_PROP_VALUE_NULL.ordinal()] = 89;
            } catch (NoSuchFieldError e97) {
            }
            try {
                iArr[ErrorCodeMapping.E_QRCODE_DEVICE_NOT_EXIST.ordinal()] = 28;
            } catch (NoSuchFieldError e98) {
            }
            try {
                iArr[ErrorCodeMapping.E_QRCODE_FORMAT_UNSUPPORTED.ordinal()] = 37;
            } catch (NoSuchFieldError e99) {
            }
            try {
                iArr[ErrorCodeMapping.E_QRCODE_SIZE_ERROR.ordinal()] = 38;
            } catch (NoSuchFieldError e100) {
            }
            try {
                iArr[ErrorCodeMapping.E_QRCODE_TEMP_INVALID.ordinal()] = 22;
            } catch (NoSuchFieldError e101) {
            }
            try {
                iArr[ErrorCodeMapping.E_QUERY_END_NULL.ordinal()] = 57;
            } catch (NoSuchFieldError e102) {
            }
            try {
                iArr[ErrorCodeMapping.E_QUERY_START_NULL.ordinal()] = 56;
            } catch (NoSuchFieldError e103) {
            }
            try {
                iArr[ErrorCodeMapping.E_RESET_PASSWORD.ordinal()] = 70;
            } catch (NoSuchFieldError e104) {
            }
            try {
                iArr[ErrorCodeMapping.E_RESOURCE_MEDIA_NULL.ordinal()] = 48;
            } catch (NoSuchFieldError e105) {
            }
            try {
                iArr[ErrorCodeMapping.E_RESOURCE_OID_NULL.ordinal()] = 36;
            } catch (NoSuchFieldError e106) {
            }
            try {
                iArr[ErrorCodeMapping.E_RESOURCE_SUFFIX_NULL.ordinal()] = 50;
            } catch (NoSuchFieldError e107) {
            }
            try {
                iArr[ErrorCodeMapping.E_RESOURCE_TYPE.ordinal()] = 40;
            } catch (NoSuchFieldError e108) {
            }
            try {
                iArr[ErrorCodeMapping.E_RESOURCE_TYPE_NULL.ordinal()] = 49;
            } catch (NoSuchFieldError e109) {
            }
            try {
                iArr[ErrorCodeMapping.E_SN_QUERY_LOCKED.ordinal()] = 45;
            } catch (NoSuchFieldError e110) {
            }
            try {
                iArr[ErrorCodeMapping.E_SUB_DEVICE_JOIN_FAIL.ordinal()] = 83;
            } catch (NoSuchFieldError e111) {
            }
            try {
                iArr[ErrorCodeMapping.E_SUB_DEVICE_LEAVE_FAIL.ordinal()] = 84;
            } catch (NoSuchFieldError e112) {
            }
            try {
                iArr[ErrorCodeMapping.E_SUB_DEVICE_NOT_EXIST.ordinal()] = 87;
            } catch (NoSuchFieldError e113) {
            }
            try {
                iArr[ErrorCodeMapping.E_SUB_DID_NULL.ordinal()] = 31;
            } catch (NoSuchFieldError e114) {
            }
            try {
                iArr[ErrorCodeMapping.E_SYSTEM_ERROR.ordinal()] = 2;
            } catch (NoSuchFieldError e115) {
            }
            try {
                iArr[ErrorCodeMapping.E_TOKEN_ACCESS_PID.ordinal()] = 58;
            } catch (NoSuchFieldError e116) {
            }
            try {
                iArr[ErrorCodeMapping.E_TOKEN_ERROR.ordinal()] = 4;
            } catch (NoSuchFieldError e117) {
            }
            try {
                iArr[ErrorCodeMapping.E_TOKEN_INVALID.ordinal()] = 72;
            } catch (NoSuchFieldError e118) {
            }
            try {
                iArr[ErrorCodeMapping.E_TOKEN_NULL.ordinal()] = 3;
            } catch (NoSuchFieldError e119) {
            }
            try {
                iArr[ErrorCodeMapping.E_TRIGGER_ID_ERROR.ordinal()] = 34;
            } catch (NoSuchFieldError e120) {
            }
            try {
                iArr[ErrorCodeMapping.E_TRIGGER_ID_NULL.ordinal()] = 20;
            } catch (NoSuchFieldError e121) {
            }
            try {
                iArr[ErrorCodeMapping.E_UID_NULL.ordinal()] = 43;
            } catch (NoSuchFieldError e122) {
            }
            try {
                iArr[ErrorCodeMapping.E_USER_NOT_BOUND.ordinal()] = 11;
            } catch (NoSuchFieldError e123) {
            }
            try {
                iArr[ErrorCodeMapping.E_USER_NOT_EXIST.ordinal()] = 21;
            } catch (NoSuchFieldError e124) {
            }
            try {
                iArr[ErrorCodeMapping.E_UTYPE_NULL.ordinal()] = 60;
            } catch (NoSuchFieldError e125) {
            }
            try {
                iArr[ErrorCodeMapping.E_VERIFICATION_CODE_INVALID.ordinal()] = 65;
            } catch (NoSuchFieldError e126) {
            }
            try {
                iArr[ErrorCodeMapping.E_VERIFICATION_CODE_NULL.ordinal()] = 66;
            } catch (NoSuchFieldError e127) {
            }
            try {
                iArr[ErrorCodeMapping.E_VID_NOT_EXIST.ordinal()] = 81;
            } catch (NoSuchFieldError e128) {
            }
            try {
                iArr[ErrorCodeMapping.E_VID_NULL.ordinal()] = 80;
            } catch (NoSuchFieldError e129) {
            }
            try {
                iArr[ErrorCodeMapping.E_WECHAT_AUTH_ERROR.ordinal()] = 27;
            } catch (NoSuchFieldError e130) {
            }
            try {
                iArr[ErrorCodeMapping.E_WECHAT_PUSH_FAILED.ordinal()] = 44;
            } catch (NoSuchFieldError e131) {
            }
            try {
                iArr[ErrorCodeMapping.E_WECHAT_PUSH_LOCKED.ordinal()] = 46;
            } catch (NoSuchFieldError e132) {
            }
            try {
                iArr[ErrorCodeMapping.E_WECHAT_UPLOAD_FAILED.ordinal()] = 51;
            } catch (NoSuchFieldError e133) {
            }
            $SWITCH_TABLE$com$xcharge$charger$protocol$monitor$bean$ErrorCodeMapping = iArr;
        }
        return iArr;
    }

    public static int getCode(ErrorCodeMapping codeMapping) {
        switch ($SWITCH_TABLE$com$xcharge$charger$protocol$monitor$bean$ErrorCodeMapping()[codeMapping.ordinal()]) {
            case 1:
                return 200;
            case 2:
                return -1;
            case 3:
                return ErrorCode.EC_NFC_UNRECOGNIZED_CARD;
            case 4:
                return ErrorCode.EC_NFC_UNAVAILABLE_KEYSEED;
            case 5:
                return ErrorCode.EC_NFC_INVALID_MANAGE_CARD_DATA;
            case 6:
                return ErrorCode.EC_NFC_SET_FAIL;
            case 7:
                return ErrorCode.EC_NFC_SIGN_ERROR;
            case 8:
                return ErrorCode.EC_NFC_BIND_CARD_FAIL;
            case 9:
                return ErrorCode.EC_NFC_SET_REFUSE;
            case 10:
                return ErrorCode.EC_NFC_CHARGE_REFUSE;
            case PortRuntimeData.STATUS_EX_11 /* 11 */:
                return ErrorCode.EC_NFC_SCAN_REFUSE;
            case PortRuntimeData.STATUS_EX_12 /* 12 */:
                return ErrorCode.EC_NFC_INVALID_PORT;
            case 13:
                return ErrorCode.EC_NFC_UNPAID_BILL;
            case 14:
                return ErrorCode.EC_NFC_CARD_RESERVED;
            case 15:
                return ErrorCode.EC_NFC_CARD_RESERVE_FAIL;
            case 16:
                return ErrorCode.EC_NFC_CARD_RELEASE_FAIL;
            case 17:
                return ErrorCode.EC_NFC_CARD_AUTH_FAIL;
            case 18:
                return ErrorCode.EC_NFC_CARD_UNPAID_CONSUME_OK;
            case TimeUtils.HUNDRED_DAY_FIELD_LEN /* 19 */:
                return ErrorCode.EC_NFC_CARD_UNPAID_CONSUME_FAIL;
            case 20:
                return ErrorCode.EC_NFC_CARD_UNPAID_BALANCE_NOT_ENOUGH;
            case 21:
                return ErrorCode.EC_NFC_CARD_CONSUME_FAIL;
            case 22:
                return ErrorCode.EC_NFC_CARD_BALANCE_NOT_ENOUGH;
            case 23:
                return ErrorCode.EC_NFC_RECOVERY_SIGN_FAIL;
            case 24:
                return ErrorCode.EC_NFC_REWRITED_BALANCE;
            case 25:
                return ErrorCode.EC_NFC_NOT_GROUP_MODE;
            case BuildConfig.VERSION_CODE /* 26 */:
                return ErrorCode.EC_NFC_NOT_PERSONAL_MODE;
            case 27:
                return ErrorCode.EC_NFC_UNAVAILABLE_CLOUD;
            case 28:
                return ErrorCode.EC_NFC_NOT_PERMIT_SWIPE;
            case 29:
                return ErrorCode.EC_NFC_READ_FAILED;
            case 30:
                return ErrorCode.EC_NFC_NOT_INIT_CHARGE_CARD;
            case 31:
                return ErrorCode.EC_NFC_INIT_FAIL;
            case 32:
                return ErrorCode.EC_NFC_SWIPE_PROCESSING;
            case Encoder.DEFAULT_EC_PERCENT /* 33 */:
                return 40031;
            case 34:
                return 40032;
            case 35:
                return 40033;
            case 36:
                return 40034;
            case 37:
                return 40035;
            case 38:
                return 40036;
            case 39:
                return 40037;
            case 40:
                return 40038;
            case 41:
                return 40039;
            case 42:
                return 40040;
            case 43:
                return 40041;
            case 44:
                return 40042;
            case 45:
                return 40043;
            case 46:
                return 40044;
            case 47:
                return 40045;
            case 48:
                return 40046;
            case 49:
                return 40047;
            case 50:
                return 40048;
            case 51:
                return 40049;
            case 52:
                return 40050;
            case 53:
                return 40051;
            case 54:
                return 40052;
            case 55:
                return 40053;
            case 56:
                return 40054;
            case 57:
                return 40055;
            case 58:
                return 40056;
            case 59:
                return 40057;
            case 60:
                return 40058;
            case 61:
                return 40059;
            case 62:
                return 40060;
            case 63:
                return 40061;
            case 64:
                return 40062;
            case 65:
                return 40063;
            case 66:
                return 40064;
            case 67:
                return 40065;
            case 68:
                return 40066;
            case 69:
                return 40067;
            case 70:
                return 40068;
            case 71:
                return 40069;
            case 72:
                return 40070;
            case 73:
                return ErrorCode.EC_CAR_ERROR;
            case 74:
                return ErrorCode.EC_CAR_STOP_CHARGE;
            case IoUtils.CONTINUE_LOADING_PERCENTAGE /* 75 */:
                return 50003;
            case 76:
                return 50004;
            case 77:
                return 50005;
            case 78:
                return 50100;
            case 79:
                return 50101;
            case WebSocket.DEFAULT_PORT /* 80 */:
                return 50103;
            case 81:
                return 50104;
            case 82:
                return 50105;
            case 83:
                return 50106;
            case 84:
                return 50107;
            case 85:
            case PDF417Common.MAX_ROWS_IN_BARCODE /* 90 */:
                return 50200;
            case 86:
                return 50201;
            case 87:
                return 50203;
            case 88:
                return 50204;
            case 89:
                return 50205;
            case 91:
                return 50201;
            case 92:
                return 50202;
            case 93:
                return 50300;
            case 94:
                return 50301;
            case 95:
                return 50302;
            case 96:
                return 50303;
            case 97:
                return 50304;
            case 98:
                return 50305;
            case 99:
                return 50306;
            case 100:
                return 50307;
            case 101:
                return 50308;
            case 102:
                return 50309;
            case 103:
                return 50310;
            case 104:
                return 50311;
            case 105:
                return 50312;
            case 106:
                return 50313;
            case 107:
                return 50314;
            case 108:
                return 50315;
            case 109:
                return 50316;
            case FTPCodes.RESTART_MARKER /* 110 */:
                return 50400;
            case 111:
                return 50401;
            case 112:
                return 50402;
            case 113:
                return 50403;
            case 114:
                return 50404;
            case 115:
                return 50405;
            case 116:
                return 50406;
            case 117:
                return 50407;
            case 118:
                return 50408;
            case 119:
                return 50409;
            case 120:
                return 50410;
            case 121:
                return 50500;
            case 122:
                return 50501;
            case 123:
                return 50502;
            case 124:
                return 50503;
            case FTPCodes.DATA_CONNECTION_ALREADY_OPEN /* 125 */:
                return 50600;
            case TransportMediator.KEYCODE_MEDIA_PLAY /* 126 */:
                return 50601;
            case TransportMediator.KEYCODE_MEDIA_PAUSE /* 127 */:
                return 50602;
            case 128:
                return 50603;
            case 129:
                return 50604;
            case TransportMediator.KEYCODE_MEDIA_RECORD /* 130 */:
                return 50605;
            case 131:
                return 50606;
            case 132:
                return 50607;
            case 133:
                return 50608;
            default:
                return 0;
        }
    }

    public static String getMsg(ErrorCodeMapping codeMapping) {
        switch ($SWITCH_TABLE$com$xcharge$charger$protocol$monitor$bean$ErrorCodeMapping()[codeMapping.ordinal()]) {
            case 1:
                return "OK";
            case 2:
                return "System Error";
            case 3:
                return "Access Token Null or Not Exist";
            case 4:
                return "Access Token Invalid";
            case 5:
                return "Request Json Error";
            case 6:
                return "Request API Not Exist";
            case 7:
                return "Did Null";
            case 8:
                return "Device Authority Failed";
            case 9:
                return "App Authority Failed";
            case 10:
                return "Request Device Not Exist";
            case PortRuntimeData.STATUS_EX_11 /* 11 */:
                return "User and Device Not Bound";
            case PortRuntimeData.STATUS_EX_12 /* 12 */:
                return "Request Content-Type Not Support";
            case 13:
                return "Request Param Condition Invalid";
            case 14:
                return "Token Appkey Null";
            case 15:
                return "Token Appsecret Null";
            case 16:
                return "Request Method Invalid";
            case 17:
                return "OID Null";
            case 18:
                return "Heartbeat Time Null";
            case TimeUtils.HUNDRED_DAY_FIELD_LEN /* 19 */:
                return "MQ Push Token Null";
            case 20:
                return "Trigger ID Null";
            case 21:
                return "Request User Not Exist";
            case 22:
                return "Temp QRCode Invalid";
            case 23:
                return "Json Mapping Error";
            case 24:
                return "Request Param Json id Not Exist";
            case 25:
                return "Longitude Null";
            case BuildConfig.VERSION_CODE /* 26 */:
                return "Latitude Null";
            case 27:
                return "Wechat Authority Failed";
            case 28:
                return "Device QRCode Not Exist";
            case 29:
                return "Magic Null";
            case 30:
                return "Magic Error";
            case 31:
                return "SubDevice Id Null";
            case 32:
                return "PID Null";
            case Encoder.DEFAULT_EC_PERCENT /* 33 */:
                return "Request Cmd Error";
            case 34:
                return "Trigger Id Error";
            case 35:
                return "PID not Exist";
            case 36:
                return "Resource Push OID Null";
            case 37:
                return "QRCode Image Type Unsupport";
            case 38:
                return "Image Size Less than 64";
            case 39:
                return "Node ID Null";
            case 40:
                return "Upload Resource Type Error";
            case 41:
                return "Query Device KeyType Error";
            case 42:
                return "Mac Address Error";
            case 43:
                return "UserId Null";
            case 44:
                return "Wechat Push Error";
            case 45:
                return "SN Query Locked";
            case 46:
                return "Wechat Push Locked";
            case 47:
                return "IP Null";
            case 48:
                return "Wechat Media id Null";
            case 49:
                return "Upload Resource Type Null";
            case 50:
                return "Upload Resource Suffix Null";
            case 51:
                return "Wechat Upload Failed";
            case 52:
                return "File upload Item is Null";
            case 53:
                return "Oss Upload Failed";
            case 54:
                return "Param Invalid";
            case 55:
                return "Exec Param ID Null";
            case 56:
                return "Limit Start Param Null";
            case 57:
                return "Limit End Param Null";
            case 58:
                return "Token no Authority Access PID";
            case 59:
                return "Json End OF";
            case 60:
                return "utype null";
            case 61:
                return "user password invalid";
            case 62:
                return "email or sms exist";
            case 63:
                return "create user info fail";
            case 64:
                return "generate verification code error";
            case 65:
                return "verification code is invalid";
            case 66:
                return "verification code is null";
            case 67:
                return "account is null";
            case 68:
                return "password is null";
            case 69:
                return "email active invalid";
            case 70:
                return "user update password error";
            case 71:
                return "email or sms not exist";
            case 72:
                return "token invalid";
            case 73:
                return "DCAP version invalid";
            case 74:
                return "DCAP encapsulation invalid";
            case IoUtils.CONTINUE_LOADING_PERCENTAGE /* 75 */:
                return "DCAP signature invalid";
            case 76:
                return "DCAP duplicate sequence";
            case 77:
                return "DCAP protocol invalid";
            case 78:
                return "DDAP version invalid";
            case 79:
                return "DDAP encapsulation invalid";
            case WebSocket.DEFAULT_PORT /* 80 */:
                return "VID null";
            case 81:
                return "VID not exist";
            case 82:
                return "register device fail";
            case 83:
                return "join sub-device fail";
            case 84:
                return "leave sub-device fail";
            case 85:
                return "DAP version invalid";
            case 86:
                return "DAP encapsulation invalid";
            case 87:
                return "sub-device not exist";
            case 88:
                return "property id not exist";
            case 89:
                return "property value is null";
            case PDF417Common.MAX_ROWS_IN_BARCODE /* 90 */:
                return "CAP version invalid";
            case 91:
                return "CAP encapsulation invalid";
            case 92:
                return "charge not exist";
            case 93:
                return "unknown";
            case 94:
                return "bad qrcode";
            case 95:
                return "user forbidden";
            case 96:
                return "bill unpaid";
            case 97:
                return "charge unfinished";
            case 98:
                return "not reserved";
            case 99:
                return "reserve undue";
            case 100:
                return "reserve timeout";
            case 101:
                return "not queued";
            case 102:
                return "queue undue";
            case 103:
                return "queue timeout";
            case 104:
                return "balance insufficient";
            case 105:
                return "bad id-card";
            case 106:
                return "port disabled";
            case 107:
                return "device error";
            case 108:
                return "in using";
            case 109:
                return "fee rate not exist";
            case FTPCodes.RESTART_MARKER /* 110 */:
                return "unknown";
            case 111:
                return "remote user";
            case 112:
                return "local user";
            case 113:
                return "admin user";
            case 114:
                return "user setted condition";
            case 115:
                return "full";
            case 116:
                return YZXProperty.GUN_CONNECT_VEHICLE;
            case 117:
                return "plugout";
            case 118:
                return "reboot";
            case 119:
                return "device error";
            case 120:
                return "not fund";
            case 121:
                return "unknown";
            case 122:
                return "user cancel";
            case 123:
                return "plugin timout";
            case 124:
                return YZXProperty.GUN_CONNECT_VEHICLE;
            case FTPCodes.DATA_CONNECTION_ALREADY_OPEN /* 125 */:
                return "not init";
            case TransportMediator.KEYCODE_MEDIA_PLAY /* 126 */:
                return "ground except";
            case TransportMediator.KEYCODE_MEDIA_PAUSE /* 127 */:
                return "lost phase";
            case 128:
                return "emergency stop";
            case 129:
                return "voltage except";
            case TransportMediator.KEYCODE_MEDIA_RECORD /* 130 */:
                return "current except";
            case 131:
                return "temprature except";
            case 132:
                return "leak current";
            case 133:
                return "communication except";
            default:
                return null;
        }
    }
}