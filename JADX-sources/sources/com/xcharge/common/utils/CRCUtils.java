package com.xcharge.common.utils;

import android.support.v4.internal.view.SupportMenu;
import android.support.v4.view.MotionEventCompat;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.anyo.bean.request.HeartBeatRequest;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import org.apache.mina.proxy.handlers.socks.SocksProxyConstants;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;

/* loaded from: classes.dex */
public class CRCUtils {
    static byte[] crc16_tab_h = {0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 1, -64, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, 0, -63, -127, AnyoMessage.CMD_QUERY_CHARGE_SETTING};
    static byte[] crc16_tab_l = {0, -64, -63, 1, -61, 3, 2, -62, -58, 6, 7, -57, 5, -59, -60, 4, -52, 12, MqttWireMessage.MESSAGE_TYPE_PINGRESP, -51, HeartBeatRequest.PORT_STATUS_FAULT, -49, -50, MqttWireMessage.MESSAGE_TYPE_DISCONNECT, 10, -54, -53, MqttWireMessage.MESSAGE_TYPE_UNSUBACK, -55, 9, 8, -56, -40, AnyoMessage.CMD_REPORT_CHARGE_STOPPED, AnyoMessage.CMD_REPORT_EVENT, -39, 27, -37, -38, 26, 30, -34, -33, 31, -35, 29, 28, -36, 20, -44, -43, 21, -41, 23, 22, -42, -46, 18, 19, -45, 17, -47, -48, 16, -16, AnyoMessage.CMD_SYNC_TIME, AnyoMessage.CMD_RESET_SYSTEM, -15, 51, -13, -14, 50, AnyoMessage.CMD_QUERY_SYS_INFO, -10, -9, AnyoMessage.CMD_UPDATE_SYS_INFO, -11, AnyoMessage.CMD_BIND_USER, 52, -12, AnyoMessage.CMD_START_CHARGE, -4, -3, AnyoMessage.CMD_STOP_CHARGE, -1, 63, AnyoMessage.CMD_START_UPGRADE, -2, -6, AnyoMessage.CMD_SET_TIMING_CHARGE, AnyoMessage.CMD_CANCEL_TIMING_CHARGE, -5, AnyoMessage.CMD_UPDATE_FEE_POLICY, -7, -8, AnyoMessage.CMD_QUERY_FEE_POLICY, 40, -24, -23, 41, -21, 43, 42, -22, -18, 46, 47, -17, 45, -19, -20, 44, -28, 36, 37, -27, 39, -25, -26, 38, AnyoMessage.CMD_REPORT_NETWORK_INFO, -30, -29, 35, -31, AnyoMessage.CMD_REPORT_UPGRADE_DOWNLOAD_COMPLETE, 32, -32, -96, 96, 97, -95, 99, -93, -94, 98, 102, -90, -89, 103, -91, 101, 100, -92, 108, -84, -83, 109, -81, 111, 110, -82, AnyoMessage.START_CODE_RESPONSE, 106, 107, -85, 105, -87, -88, AnyoMessage.START_CODE_REQUEST, 120, -72, -71, 121, -69, 123, 122, -70, -66, 126, Byte.MAX_VALUE, -65, 125, -67, -68, 124, -76, 116, 117, -75, 119, -73, -74, 118, 114, -78, -77, 115, -79, 113, 112, -80, AnyoMessage.STATUS_CODE_INTERNAL_ERROR, -112, XMSZMessage.BootNotificationResponse, 81, XMSZMessage.StartTransactionResponse, AnyoMessage.CMD_CANCEL_RESERVE_PORT, AnyoMessage.CMD_RESERVE_PORT, XMSZMessage.AuthorizeIDResponse, XMSZMessage.StatusNotificationResponse, 86, 87, XMSZMessage.FirmwareUpdateInformResponse, 85, XMSZMessage.HeartBeatResponse, XMSZMessage.StopTransactionResponse, 84, -100, SocksProxyConstants.V4_REPLY_REQUEST_FAILED_NO_IDENTD, SocksProxyConstants.V4_REPLY_REQUEST_FAILED_ID_NOT_CONFIRMED, -99, 95, -97, -98, 94, SocksProxyConstants.V4_REPLY_REQUEST_GRANTED, -102, -101, SocksProxyConstants.V4_REPLY_REQUEST_REJECTED_OR_FAILED, -103, 89, 88, -104, XMSZMessage.ResetResponse, 72, 73, XMSZMessage.UpdateFirmwareResponse, 75, -117, XMSZMessage.ChangeAvailabilityResponse, 74, 78, -114, -113, 79, -115, 77, 76, XMSZMessage.GetChargeInfoResponse, 68, -124, -123, 69, -121, 71, 70, -122, XMSZMessage.RemoteStartChargingResponse, AnyoMessage.CMD_QUERY_DEVICE_FAULT, AnyoMessage.CMD_QUERY_BATTERY_CHARGE_INFO, XMSZMessage.RemoteStopChargingResponse, AnyoMessage.CMD_UPDATE_CHARGE_SETTING, -127, AnyoMessage.CMD_RESET_CHARGE, AnyoMessage.CMD_QUERY_CHARGE_SETTING};

    public static int calcCrc16(byte[] data) {
        return calcCrc16(data, 0, data.length);
    }

    public static int calcCrc16(byte[] data, int offset, int len) {
        return calcCrc16(data, offset, len, SupportMenu.USER_MASK);
    }

    public static int calcCrc16(byte[] data, int offset, int len, int preval) {
        int ucCRCHi = (65280 & preval) >> 8;
        int ucCRCLo = preval & MotionEventCompat.ACTION_MASK;
        for (int i = 0; i < len; i++) {
            int iIndex = (data[offset + i] ^ ucCRCLo) & MotionEventCompat.ACTION_MASK;
            ucCRCLo = ucCRCHi ^ crc16_tab_h[iIndex];
            ucCRCHi = crc16_tab_l[iIndex];
        }
        return ((ucCRCHi & MotionEventCompat.ACTION_MASK) << 8) | (ucCRCLo & MotionEventCompat.ACTION_MASK & SupportMenu.USER_MASK);
    }

    public static void main(String[] args) {
        byte[] p = new byte[24];
        p[0] = 71;
        p[17] = 2;
        p[18] = 68;
        p[19] = 31;
        p[20] = 32;
        p[22] = 1;
        p[23] = 2;
        int crc = calcCrc16(p);
        System.out.println(crc);
    }
}
