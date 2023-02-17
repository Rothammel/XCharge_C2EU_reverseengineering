package com.xcharge.charger.protocol.anyo.handler;

import android.util.Log;
import com.google.zxing.aztec.encoder.Encoder;
import com.xcharge.charger.protocol.anyo.bean.AnyoHead;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.anyo.bean.request.QueryDeviceFaultRequest;
import com.xcharge.charger.protocol.anyo.bean.request.RebootRequest;
import com.xcharge.charger.protocol.anyo.bean.request.ResetChargeRequest;
import com.xcharge.charger.protocol.anyo.bean.request.StartChargeRequest;
import com.xcharge.charger.protocol.anyo.bean.request.StartUpgradeRequest;
import com.xcharge.charger.protocol.anyo.bean.request.StopChargeRequest;
import com.xcharge.charger.protocol.anyo.bean.request.TimeSyncRequest;
import com.xcharge.charger.protocol.anyo.bean.request.UnlockPortRequest;
import com.xcharge.charger.protocol.anyo.bean.response.AuthResponse;
import com.xcharge.charger.protocol.anyo.bean.response.HeartBeatResponse;
import com.xcharge.charger.protocol.anyo.bean.response.LoginResponse;
import com.xcharge.charger.protocol.anyo.bean.response.ReportChargeResponse;
import com.xcharge.charger.protocol.anyo.bean.response.ReportChargeStoppedResponse;
import com.xcharge.charger.protocol.anyo.bean.response.ReportEventResponse;
import com.xcharge.charger.protocol.anyo.bean.response.ReportHistoryBillResponse;
import com.xcharge.charger.protocol.anyo.bean.response.ReportNetworkInfoResponse;
import com.xcharge.charger.protocol.anyo.bean.response.UpgradeDownloadCompleteResponse;
import com.xcharge.common.utils.FormatUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class AnyoMessageDecoder extends CumulativeProtocolDecoder {
    private static final String DECODER_STATE_KEY = (String.valueOf(AnyoMessageDecoder.class.getName()) + ".STATE");
    public static final int HEAD_LENTH = 8;
    public static final int MAX_MESSAGE_LENTH = 1032;

    private static class DecoderState {
        AnyoHead head;
        byte[] remain;

        private DecoderState() {
            this.head = null;
            this.remain = null;
        }

        /* synthetic */ DecoderState(DecoderState decoderState) {
            this();
        }
    }

    /* access modifiers changed from: protected */
    public boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        DecoderState decoderState = (DecoderState) session.getAttribute(DECODER_STATE_KEY);
        if (decoderState == null) {
            decoderState = new DecoderState((DecoderState) null);
            session.setAttribute(DECODER_STATE_KEY, decoderState);
        }
        if (decoderState.head == null) {
            if (in.remaining() < 8) {
                return false;
            }
            AnyoHead head = decodeHead(in);
            if (head.getBodyLength() == 0) {
                AnyoMessage message = decodeAnyoMessage(head.getStartCode(), head.getCmdCode(), new byte[0]);
                if (message != null) {
                    message.setHead(head);
                    out.write(message);
                    return true;
                }
                Log.w("AnyoMessageDecoder.doDecode", "unsupported msg now, discard it, head: " + head.toJson());
                return true;
            }
            decoderState.head = head;
        }
        if (decoderState.head == null) {
            return false;
        }
        int bodyLength = decoderState.head.getBodyLength();
        if (in.remaining() < bodyLength) {
            return false;
        }
        byte[] bodyBytes = new byte[bodyLength];
        in.get(bodyBytes);
        Log.d("AnyoMessageDecoder.doDecode", "receive body: " + FormatUtils.bytesToHexString(bodyBytes));
        AnyoMessage message2 = decodeAnyoMessage(decoderState.head.getStartCode(), decoderState.head.getCmdCode(), bodyBytes);
        if (message2 != null) {
            message2.setHead(decoderState.head);
            out.write(message2);
        } else {
            Log.w("AnyoMessageDecoder.doDecode", "unsupported msg now, discard it, head: " + decoderState.head.toJson() + ", body: " + FormatUtils.bytesToHexString(bodyBytes));
        }
        decoderState.head = null;
        return true;
    }

    private AnyoHead decodeHead(IoBuffer in) throws Exception {
        byte[] headBytes = new byte[8];
        in.get(headBytes);
        Log.d("AnyoMessageDecoder.decodeHead", "receive head: " + FormatUtils.bytesToHexString(headBytes));
        return new AnyoHead().fromBytes(headBytes);
    }

    private AnyoMessage decodeAnyoMessage(byte start, byte cmd, byte[] bytes) throws Exception {
        if (start == -86) {
            switch (cmd) {
                case 16:
                    return new LoginResponse().bodyFromBytes(bytes);
                case 17:
                    return new AuthResponse().bodyFromBytes(bytes);
                case 18:
                    return new ReportChargeResponse().bodyFromBytes(bytes);
                case 20:
                    return new ReportHistoryBillResponse().bodyFromBytes(bytes);
                case 21:
                    return new HeartBeatResponse().bodyFromBytes(bytes);
                case 24:
                    return new ReportChargeStoppedResponse().bodyFromBytes(bytes);
                case 25:
                    return new ReportEventResponse().bodyFromBytes(bytes);
                case Encoder.DEFAULT_EC_PERCENT:
                    return new UpgradeDownloadCompleteResponse().bodyFromBytes(bytes);
                case 34:
                    return new ReportNetworkInfoResponse().bodyFromBytes(bytes);
            }
        } else if (start == 104) {
            switch (cmd) {
                case Byte.MIN_VALUE:
                    return new ResetChargeRequest().bodyFromBytes(bytes);
                case 48:
                    return new TimeSyncRequest().bodyFromBytes(bytes);
                case 49:
                    return new RebootRequest().bodyFromBytes(bytes);
                case 60:
                    return new StartChargeRequest().bodyFromBytes(bytes);
                case 61:
                    return new StopChargeRequest().bodyFromBytes(bytes);
                case 62:
                    return new StartUpgradeRequest().bodyFromBytes(bytes);
                case 66:
                    return new QueryDeviceFaultRequest().bodyFromBytes(bytes);
                case 81:
                    return new UnlockPortRequest().bodyFromBytes(bytes);
            }
        }
        return null;
    }
}
