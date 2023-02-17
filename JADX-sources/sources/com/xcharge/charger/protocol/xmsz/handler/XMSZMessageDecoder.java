package com.xcharge.charger.protocol.xmsz.handler;

import android.util.Log;
import com.xcharge.charger.device.c2.bean.PortRuntimeData;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.charger.protocol.xmsz.bean.cloud.AuthorizeIDResponse;
import com.xcharge.charger.protocol.xmsz.bean.cloud.BootNotificationResponse;
import com.xcharge.charger.protocol.xmsz.bean.cloud.ChangeAvailabilityRequest;
import com.xcharge.charger.protocol.xmsz.bean.cloud.GetChargeInfoRequest;
import com.xcharge.charger.protocol.xmsz.bean.cloud.HeartBeatResponse;
import com.xcharge.charger.protocol.xmsz.bean.cloud.RemoteStartChargingRequest;
import com.xcharge.charger.protocol.xmsz.bean.cloud.RemoteStopChargingRequest;
import com.xcharge.charger.protocol.xmsz.bean.cloud.ResetRequest;
import com.xcharge.charger.protocol.xmsz.bean.cloud.StartTransactionResponse;
import com.xcharge.charger.protocol.xmsz.bean.cloud.StatusNotificationResponse;
import com.xcharge.charger.protocol.xmsz.bean.cloud.StopTransactionResponse;
import com.xcharge.common.utils.EndianUtils;
import com.xcharge.common.utils.FormatUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/* loaded from: classes.dex */
public class XMSZMessageDecoder extends CumulativeProtocolDecoder {
    private static final String DECODER_STATE_KEY = String.valueOf(XMSZMessageDecoder.class.getName()) + ".STATE";
    public static final int HEAD_LENTH = 14;

    /* loaded from: classes.dex */
    private static class DecoderState {
        XMSZHead head;

        private DecoderState() {
            this.head = null;
        }

        /* synthetic */ DecoderState(DecoderState decoderState) {
            this();
        }
    }

    @Override // org.apache.mina.filter.codec.CumulativeProtocolDecoder
    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        int bodyLength;
        DecoderState decoderState = (DecoderState) session.getAttribute(DECODER_STATE_KEY);
        if (decoderState == null) {
            decoderState = new DecoderState(null);
            session.setAttribute(DECODER_STATE_KEY, decoderState);
        }
        if (decoderState.head == null) {
            if (in.remaining() < 14) {
                return false;
            }
            XMSZHead head = decodeHead(in);
            decoderState.head = head;
        }
        if (decoderState.head == null || in.remaining() < decoderState.head.getPacketLength() - 10) {
            return false;
        }
        byte[] bodyBytes = new byte[bodyLength];
        in.get(bodyBytes);
        Log.d("XMSZMessageDecoder.doDecode", "receive package body: " + FormatUtils.bytesToHexString(bodyBytes));
        byte functionCode = decoderState.head.getFunctionCode();
        XMSZMessage message = decodeXMSZMessage(functionCode, bodyBytes);
        if (message != null) {
            message.setHead(decoderState.head);
            out.write(message);
        } else {
            Log.w("XMSZMessageDecoder.doDecode", "unsupported msg now, discard it, package head: " + decoderState.head.toJson() + ", package body: " + FormatUtils.bytesToHexString(bodyBytes));
        }
        decoderState.head = null;
        return true;
    }

    private XMSZHead decodeHead(IoBuffer in) throws Exception {
        byte[] headBytes = new byte[14];
        in.get(headBytes);
        Log.d("XMSZMessageDecoder.decodeHead", "receive package head: " + FormatUtils.bytesToHexString(headBytes));
        return new XMSZHead().fromBytes(headBytes);
    }

    private XMSZMessage decodeXMSZMessage(byte functionCode, byte[] body) throws Exception {
        byte[] msgContent;
        XMSZMessage decodedMsg = null;
        int msgContentLength = body.length - 2;
        if (msgContentLength > 0) {
            msgContent = new byte[msgContentLength];
            System.arraycopy(body, 0, msgContent, 0, msgContentLength);
        } else {
            msgContent = new byte[0];
        }
        switch (functionCode) {
            case -111:
                decodedMsg = new BootNotificationResponse().bodyFromBytes(msgContent);
                break;
            case -110:
                decodedMsg = new AuthorizeIDResponse().bodyFromBytes(msgContent);
                break;
            case -109:
                decodedMsg = new StartTransactionResponse().bodyFromBytes(msgContent);
                break;
            case -108:
                decodedMsg = new StopTransactionResponse().bodyFromBytes(msgContent);
                break;
            case -107:
                decodedMsg = new HeartBeatResponse().bodyFromBytes(msgContent);
                break;
            case -106:
                decodedMsg = new StatusNotificationResponse().bodyFromBytes(msgContent);
                break;
            case 2:
                decodedMsg = new RemoteStartChargingRequest().bodyFromBytes(msgContent);
                break;
            case 3:
                decodedMsg = new RemoteStopChargingRequest().bodyFromBytes(msgContent);
                break;
            case 8:
                decodedMsg = new ResetRequest().bodyFromBytes(msgContent);
                break;
            case 10:
                decodedMsg = new ChangeAvailabilityRequest().bodyFromBytes(msgContent);
                break;
            case PortRuntimeData.STATUS_EX_12 /* 12 */:
                decodedMsg = new GetChargeInfoRequest().bodyFromBytes(msgContent);
                break;
        }
        if (decodedMsg != null) {
            decodedMsg.setCrc16(EndianUtils.littleBytesToShort(new byte[]{body[msgContentLength], body[msgContentLength + 1]}) & 65535);
        }
        return decodedMsg;
    }
}
