package org.apache.mina.proxy.handlers.http.ntlm;

import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.anyo.bean.request.HeartBeatRequest;

/* loaded from: classes.dex */
public interface NTLMConstants {
    public static final int DEFAULT_FLAGS = 12291;
    public static final int FLAG_NEGOTIATE_128_BIT_ENCRYPTION = 536870912;
    public static final int FLAG_NEGOTIATE_56_BIT_ENCRYPTION = Integer.MIN_VALUE;
    public static final int FLAG_NEGOTIATE_ALWAYS_SIGN = 32768;
    public static final int FLAG_NEGOTIATE_ANONYMOUS = 2048;
    public static final int FLAG_NEGOTIATE_DATAGRAM_STYLE = 64;
    public static final int FLAG_NEGOTIATE_DOMAIN_SUPPLIED = 4096;
    public static final int FLAG_NEGOTIATE_KEY_EXCHANGE = 1073741824;
    public static final int FLAG_NEGOTIATE_LAN_MANAGER_KEY = 128;
    public static final int FLAG_NEGOTIATE_LOCAL_CALL = 16384;
    public static final int FLAG_NEGOTIATE_NTLM = 512;
    public static final int FLAG_NEGOTIATE_NTLM2 = 524288;
    public static final int FLAG_NEGOTIATE_OEM = 2;
    public static final int FLAG_NEGOTIATE_SEAL = 32;
    public static final int FLAG_NEGOTIATE_SIGN = 16;
    public static final int FLAG_NEGOTIATE_TARGET_INFO = 8388608;
    public static final int FLAG_NEGOTIATE_UNICODE = 1;
    public static final int FLAG_NEGOTIATE_WORKSTATION_SUPPLIED = 8192;
    public static final int FLAG_REQUEST_SERVER_AUTH_REALM = 4;
    public static final int FLAG_TARGET_TYPE_DOMAIN = 65536;
    public static final int FLAG_TARGET_TYPE_SERVER = 131072;
    public static final int FLAG_TARGET_TYPE_SHARE = 262144;
    public static final int FLAG_UNIDENTIFIED_1 = 8;
    public static final int FLAG_UNIDENTIFIED_10 = 134217728;
    public static final int FLAG_UNIDENTIFIED_11 = 268435456;
    public static final int FLAG_UNIDENTIFIED_2 = 256;
    public static final int FLAG_UNIDENTIFIED_3 = 1024;
    public static final int FLAG_UNIDENTIFIED_4 = 1048576;
    public static final int FLAG_UNIDENTIFIED_5 = 2097152;
    public static final int FLAG_UNIDENTIFIED_6 = 4194304;
    public static final int FLAG_UNIDENTIFIED_7 = 16777216;
    public static final int FLAG_UNIDENTIFIED_8 = 33554432;
    public static final int FLAG_UNIDENTIFIED_9 = 67108864;
    public static final int MESSAGE_TYPE_1 = 1;
    public static final int MESSAGE_TYPE_2 = 2;
    public static final int MESSAGE_TYPE_3 = 3;
    public static final short TARGET_INFORMATION_SUBBLOCK_DNS_DOMAIN_NAME_TYPE = 1024;
    public static final short TARGET_INFORMATION_SUBBLOCK_DOMAIN_TYPE = 512;
    public static final short TARGET_INFORMATION_SUBBLOCK_FQDNS_HOSTNAME_TYPE = 768;
    public static final short TARGET_INFORMATION_SUBBLOCK_PARENT_DNS_DOMAIN_NAME_TYPE = 1280;
    public static final short TARGET_INFORMATION_SUBBLOCK_SERVER_TYPE = 256;
    public static final short TARGET_INFORMATION_SUBBLOCK_TERMINATOR_TYPE = 0;
    public static final byte[] NTLM_SIGNATURE = {78, 84, 76, 77, AnyoMessage.CMD_CANCEL_RESERVE_PORT, AnyoMessage.CMD_CANCEL_RESERVE_PORT, AnyoMessage.STATUS_CODE_INTERNAL_ERROR, 0};
    public static final byte[] DEFAULT_OS_VERSION = {5, 1, 40, 10, 0, 0, 0, HeartBeatRequest.PORT_STATUS_FAULT};
}
