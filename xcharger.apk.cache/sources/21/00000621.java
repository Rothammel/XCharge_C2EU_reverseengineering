package com.xcharge.charger.protocol.anyo.bean;

import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public abstract class AnyoMessage extends JsonBean<AnyoMessage> {
    public static final byte CMD_AUTH = 17;
    public static final byte CMD_BIND_USER = 53;
    public static final byte CMD_CANCEL_RESERVE_PORT = 83;
    public static final byte CMD_CANCEL_TIMING_CHARGE = 59;
    public static final byte CMD_HEART_BEAT = 21;
    public static final byte CMD_LOGIN = 16;
    public static final byte CMD_QUERY_BATTERY_CHARGE_INFO = 67;
    public static final byte CMD_QUERY_CHARGE_SETTING = 64;
    public static final byte CMD_QUERY_DEVICE_FAULT = 66;
    public static final byte CMD_QUERY_FEE_POLICY = 56;
    public static final byte CMD_QUERY_SYS_INFO = 54;
    public static final byte CMD_REPORT_BATTERY = 23;
    public static final byte CMD_REPORT_CHARGE = 18;
    public static final byte CMD_REPORT_CHARGE_STOPPED = 24;
    public static final byte CMD_REPORT_EVENT = 25;
    public static final byte CMD_REPORT_HIST_BILL = 20;
    public static final byte CMD_REPORT_NETWORK_INFO = 34;
    public static final byte CMD_REPORT_NOW_BILL = 19;
    public static final byte CMD_REPORT_UPGRADE_DOWNLOAD_COMPLETE = 33;
    public static final byte CMD_REQ_UPGRADE_PACK = 22;
    public static final byte CMD_RESERVE_PORT = 82;
    public static final byte CMD_RESET_CHARGE = Byte.MIN_VALUE;
    public static final byte CMD_RESET_SYSTEM = 49;
    public static final byte CMD_SET_TIMING_CHARGE = 58;
    public static final byte CMD_START_CHARGE = 60;
    public static final byte CMD_START_UPGRADE = 62;
    public static final byte CMD_STOP_CHARGE = 61;
    public static final byte CMD_SYNC_TIME = 48;
    public static final byte CMD_UNLOCK_PORT = 81;
    public static final byte CMD_UPDATE_CHARGE_SETTING = 65;
    public static final byte CMD_UPDATE_FEE_POLICY = 57;
    public static final byte CMD_UPDATE_SYS_INFO = 55;
    public static final int MAX_BODY_LENGTH = 1024;
    public static final byte PILE_TYPE_DOUBLE = 2;
    public static final byte PILE_TYPE_DOUBLE_ADVERT = 5;
    public static final byte PILE_TYPE_MULTIPLE = 3;
    public static final byte PILE_TYPE_SINGLE = 1;
    public static final byte PILE_TYPE_SINGLE_ADVERT = 4;
    public static final byte START_CODE_REQUEST = 104;
    public static final byte START_CODE_RESPONSE = -86;
    public static final byte STATUS_CODE_CMD_ERROR = 81;
    public static final byte STATUS_CODE_CMD_NOT_EXIST = 20;
    public static final byte STATUS_CODE_CMD_NOT_SUPPORT = 21;
    public static final byte STATUS_CODE_FORMAT_ERROR = 17;
    public static final byte STATUS_CODE_INTERNAL_ERROR = 80;
    public static final byte STATUS_CODE_OK = 0;
    public static final byte STATUS_CODE_REFUSE = 19;
    public static final byte STATUS_CODE_UNAUTH = 18;
    private AnyoHead head = null;
    private String port = null;
    private int retrySend = 0;

    public abstract AnyoMessage bodyFromBytes(byte[] bArr) throws Exception;

    public abstract byte[] bodyToBytes() throws Exception;

    public AnyoHead getHead() {
        return this.head;
    }

    public void setHead(AnyoHead head) {
        this.head = head;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public int getRetrySend() {
        return this.retrySend;
    }

    public void setRetrySend(int retrySend) {
        this.retrySend = retrySend;
    }

    public byte[] toBytes() throws Exception {
        byte[] head = this.head.toBytes();
        byte[] body = bodyToBytes();
        int len = body.length + 8;
        byte[] bytes = new byte[len];
        System.arraycopy(head, 0, bytes, 0, head.length);
        System.arraycopy(body, 0, bytes, head.length, body.length);
        return bytes;
    }

    public byte calcCheckSum(byte rand) throws Exception {
        byte[] head = this.head.toBytes();
        byte[] body = bodyToBytes();
        int len = body.length + 7;
        byte[] bytes = new byte[len];
        System.arraycopy(head, 0, bytes, 0, head.length - 1);
        System.arraycopy(body, 0, bytes, head.length - 1, body.length);
        byte checkSum = rand;
        for (int i = 0; i < len; i++) {
            checkSum = (byte) (bytes[i] ^ checkSum);
        }
        return checkSum;
    }

    public boolean verifyCheckSum(byte peerRand) {
        try {
            return calcCheckSum(peerRand) == this.head.getCheckSum();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}