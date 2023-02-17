package org.eclipse.paho.client.mqttv3.internal.wire;

import android.support.p000v4.view.MotionEventCompat;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.anyo.bean.request.HeartBeatRequest;
import com.xcharge.charger.protocol.monitor.bean.YZXProperty;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.apache.mina.proxy.handlers.http.HttpProxyConstants;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistable;
import org.eclipse.paho.client.mqttv3.internal.ExceptionHelper;

public abstract class MqttWireMessage {
    public static final byte MESSAGE_TYPE_CONNACK = 2;
    public static final byte MESSAGE_TYPE_CONNECT = 1;
    public static final byte MESSAGE_TYPE_DISCONNECT = 14;
    public static final byte MESSAGE_TYPE_PINGREQ = 12;
    public static final byte MESSAGE_TYPE_PINGRESP = 13;
    public static final byte MESSAGE_TYPE_PUBACK = 4;
    public static final byte MESSAGE_TYPE_PUBCOMP = 7;
    public static final byte MESSAGE_TYPE_PUBLISH = 3;
    public static final byte MESSAGE_TYPE_PUBREC = 5;
    public static final byte MESSAGE_TYPE_PUBREL = 6;
    public static final byte MESSAGE_TYPE_SUBACK = 9;
    public static final byte MESSAGE_TYPE_SUBSCRIBE = 8;
    public static final byte MESSAGE_TYPE_UNSUBACK = 11;
    public static final byte MESSAGE_TYPE_UNSUBSCRIBE = 10;
    private static final String[] PACKET_NAMES = {YZXProperty.CHARGE_STATUS_RESERVED, HttpProxyConstants.CONNECT, "CONNACK", "PUBLISH", "PUBACK", "PUBREC", "PUBREL", "PUBCOMP", "SUBSCRIBE", "SUBACK", "UNSUBSCRIBE", "UNSUBACK", "PINGREQ", "PINGRESP", "DISCONNECT"};
    protected static final String STRING_ENCODING = "UTF-8";
    protected boolean duplicate = false;
    protected int msgId;
    private byte type;

    /* access modifiers changed from: protected */
    public abstract byte getMessageInfo();

    /* access modifiers changed from: protected */
    public abstract byte[] getVariableHeader() throws MqttException;

    public MqttWireMessage(byte type2) {
        this.type = type2;
        this.msgId = 0;
    }

    public byte[] getPayload() throws MqttException {
        return new byte[0];
    }

    public byte getType() {
        return this.type;
    }

    public int getMessageId() {
        return this.msgId;
    }

    public void setMessageId(int msgId2) {
        this.msgId = msgId2;
    }

    public String getKey() {
        return new Integer(getMessageId()).toString();
    }

    public byte[] getHeader() throws MqttException {
        try {
            int first = ((getType() & HeartBeatRequest.PORT_STATUS_FAULT) << 4) ^ (getMessageInfo() & 15);
            byte[] varHeader = getVariableHeader();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(first);
            dos.write(encodeMBI((long) (varHeader.length + getPayload().length)));
            dos.write(varHeader);
            dos.flush();
            return baos.toByteArray();
        } catch (IOException ioe) {
            throw new MqttException((Throwable) ioe);
        }
    }

    public boolean isMessageIdRequired() {
        return true;
    }

    public static MqttWireMessage createWireMessage(MqttPersistable data) throws MqttException {
        byte[] payload = data.getPayloadBytes();
        if (payload == null) {
            payload = new byte[0];
        }
        return createWireMessage((InputStream) new MultiByteArrayInputStream(data.getHeaderBytes(), data.getHeaderOffset(), data.getHeaderLength(), payload, data.getPayloadOffset(), data.getPayloadLength()));
    }

    public static MqttWireMessage createWireMessage(byte[] bytes) throws MqttException {
        return createWireMessage((InputStream) new ByteArrayInputStream(bytes));
    }

    private static MqttWireMessage createWireMessage(InputStream inputStream) throws MqttException {
        try {
            CountingInputStream counter = new CountingInputStream(inputStream);
            DataInputStream in = new DataInputStream(counter);
            int first = in.readUnsignedByte();
            byte type2 = (byte) (first >> 4);
            byte info = (byte) (first & 15);
            long remainder = (((long) counter.getCounter()) + readMBI(in).getValue()) - ((long) counter.getCounter());
            byte[] data = new byte[0];
            if (remainder > 0) {
                data = new byte[((int) remainder)];
                in.readFully(data, 0, data.length);
            }
            if (type2 == 1) {
                return new MqttConnect(info, data);
            }
            if (type2 == 3) {
                return new MqttPublish(info, data);
            }
            if (type2 == 4) {
                return new MqttPubAck(info, data);
            }
            if (type2 == 7) {
                return new MqttPubComp(info, data);
            }
            if (type2 == 2) {
                return new MqttConnack(info, data);
            }
            if (type2 == 12) {
                return new MqttPingReq(info, data);
            }
            if (type2 == 13) {
                return new MqttPingResp(info, data);
            }
            if (type2 == 8) {
                return new MqttSubscribe(info, data);
            }
            if (type2 == 9) {
                return new MqttSuback(info, data);
            }
            if (type2 == 10) {
                return new MqttUnsubscribe(info, data);
            }
            if (type2 == 11) {
                return new MqttUnsubAck(info, data);
            }
            if (type2 == 6) {
                return new MqttPubRel(info, data);
            }
            if (type2 == 5) {
                return new MqttPubRec(info, data);
            }
            if (type2 == 14) {
                return new MqttDisconnect(info, data);
            }
            throw ExceptionHelper.createMqttException(6);
        } catch (IOException io) {
            throw new MqttException((Throwable) io);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x0014  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected static byte[] encodeMBI(long r12) {
        /*
            r10 = 128(0x80, double:6.32E-322)
            r8 = 0
            r4 = 0
            r2 = r12
            java.io.ByteArrayOutputStream r0 = new java.io.ByteArrayOutputStream
            r0.<init>()
        L_0x000b:
            long r6 = r2 % r10
            int r5 = (int) r6
            byte r1 = (byte) r5
            long r2 = r2 / r10
            int r5 = (r2 > r8 ? 1 : (r2 == r8 ? 0 : -1))
            if (r5 <= 0) goto L_0x0017
            r5 = r1 | 128(0x80, float:1.794E-43)
            byte r1 = (byte) r5
        L_0x0017:
            r0.write(r1)
            int r4 = r4 + 1
            int r5 = (r2 > r8 ? 1 : (r2 == r8 ? 0 : -1))
            if (r5 <= 0) goto L_0x0023
            r5 = 4
            if (r4 < r5) goto L_0x000b
        L_0x0023:
            byte[] r5 = r0.toByteArray()
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage.encodeMBI(long):byte[]");
    }

    protected static MultiByteInteger readMBI(DataInputStream in) throws IOException {
        byte digit;
        long msgLength = 0;
        int multiplier = 1;
        int count = 0;
        do {
            digit = in.readByte();
            count++;
            msgLength += (long) ((digit & Byte.MAX_VALUE) * multiplier);
            multiplier *= 128;
        } while ((digit & AnyoMessage.CMD_RESET_CHARGE) != 0);
        return new MultiByteInteger(msgLength, count);
    }

    /* access modifiers changed from: protected */
    public byte[] encodeMessageId() throws MqttException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeShort(this.msgId);
            dos.flush();
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new MqttException((Throwable) ex);
        }
    }

    public boolean isRetryable() {
        return false;
    }

    public void setDuplicate(boolean duplicate2) {
        this.duplicate = duplicate2;
    }

    /* access modifiers changed from: protected */
    public void encodeUTF8(DataOutputStream dos, String stringToEncode) throws MqttException {
        try {
            byte[] encodedString = stringToEncode.getBytes("UTF-8");
            dos.write((byte) ((encodedString.length >>> 8) & MotionEventCompat.ACTION_MASK));
            dos.write((byte) ((encodedString.length >>> 0) & MotionEventCompat.ACTION_MASK));
            dos.write(encodedString);
        } catch (UnsupportedEncodingException ex) {
            throw new MqttException((Throwable) ex);
        } catch (IOException ex2) {
            throw new MqttException((Throwable) ex2);
        }
    }

    /* access modifiers changed from: protected */
    public String decodeUTF8(DataInputStream input) throws MqttException {
        try {
            byte[] encodedString = new byte[input.readUnsignedShort()];
            input.readFully(encodedString);
            return new String(encodedString, "UTF-8");
        } catch (IOException ex) {
            throw new MqttException((Throwable) ex);
        }
    }

    public String toString() {
        return PACKET_NAMES[this.type];
    }
}
