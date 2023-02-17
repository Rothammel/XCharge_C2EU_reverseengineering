package org.eclipse.paho.client.mqttv3.internal.wire;

import android.support.v4.view.MotionEventCompat;
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

/* loaded from: classes.dex */
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
    protected int msgId = 0;
    private byte type;

    protected abstract byte getMessageInfo();

    protected abstract byte[] getVariableHeader() throws MqttException;

    public MqttWireMessage(byte type) {
        this.type = type;
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

    public void setMessageId(int msgId) {
        this.msgId = msgId;
    }

    public String getKey() {
        return new Integer(getMessageId()).toString();
    }

    public byte[] getHeader() throws MqttException {
        try {
            int first = ((getType() & HeartBeatRequest.PORT_STATUS_FAULT) << 4) ^ (getMessageInfo() & HeartBeatRequest.PORT_STATUS_FAULT);
            byte[] varHeader = getVariableHeader();
            int remLen = varHeader.length + getPayload().length;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(first);
            dos.write(encodeMBI(remLen));
            dos.write(varHeader);
            dos.flush();
            return baos.toByteArray();
        } catch (IOException ioe) {
            throw new MqttException(ioe);
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
        MultiByteArrayInputStream mbais = new MultiByteArrayInputStream(data.getHeaderBytes(), data.getHeaderOffset(), data.getHeaderLength(), payload, data.getPayloadOffset(), data.getPayloadLength());
        return createWireMessage(mbais);
    }

    public static MqttWireMessage createWireMessage(byte[] bytes) throws MqttException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        return createWireMessage(bais);
    }

    private static MqttWireMessage createWireMessage(InputStream inputStream) throws MqttException {
        try {
            CountingInputStream counter = new CountingInputStream(inputStream);
            DataInputStream in = new DataInputStream(counter);
            int first = in.readUnsignedByte();
            byte type = (byte) (first >> 4);
            byte info = (byte) (first & 15);
            long remLen = readMBI(in).getValue();
            long totalToRead = counter.getCounter() + remLen;
            long remainder = totalToRead - counter.getCounter();
            byte[] data = new byte[0];
            if (remainder > 0) {
                data = new byte[(int) remainder];
                in.readFully(data, 0, data.length);
            }
            if (type == 1) {
                MqttWireMessage result = new MqttConnect(info, data);
                return result;
            } else if (type == 3) {
                MqttWireMessage result2 = new MqttPublish(info, data);
                return result2;
            } else if (type == 4) {
                MqttWireMessage result3 = new MqttPubAck(info, data);
                return result3;
            } else if (type == 7) {
                MqttWireMessage result4 = new MqttPubComp(info, data);
                return result4;
            } else if (type == 2) {
                MqttWireMessage result5 = new MqttConnack(info, data);
                return result5;
            } else if (type == 12) {
                MqttWireMessage result6 = new MqttPingReq(info, data);
                return result6;
            } else if (type == 13) {
                MqttWireMessage result7 = new MqttPingResp(info, data);
                return result7;
            } else if (type == 8) {
                MqttWireMessage result8 = new MqttSubscribe(info, data);
                return result8;
            } else if (type == 9) {
                MqttWireMessage result9 = new MqttSuback(info, data);
                return result9;
            } else if (type == 10) {
                MqttWireMessage result10 = new MqttUnsubscribe(info, data);
                return result10;
            } else if (type == 11) {
                MqttWireMessage result11 = new MqttUnsubAck(info, data);
                return result11;
            } else if (type == 6) {
                MqttWireMessage result12 = new MqttPubRel(info, data);
                return result12;
            } else if (type == 5) {
                MqttWireMessage result13 = new MqttPubRec(info, data);
                return result13;
            } else if (type == 14) {
                MqttWireMessage result14 = new MqttDisconnect(info, data);
                return result14;
            } else {
                throw ExceptionHelper.createMqttException(6);
            }
        } catch (IOException io) {
            throw new MqttException(io);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public static byte[] encodeMBI(long number) {
        int numBytes = 0;
        long no = number;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        do {
            byte digit = (byte) (no % 128);
            no /= 128;
            if (no > 0) {
                digit = (byte) (digit | AnyoMessage.CMD_RESET_CHARGE);
            }
            bos.write(digit);
            numBytes++;
            if (no <= 0) {
                break;
            }
        } while (numBytes < 4);
        return bos.toByteArray();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public static MultiByteInteger readMBI(DataInputStream in) throws IOException {
        byte digit;
        long msgLength = 0;
        int multiplier = 1;
        int count = 0;
        do {
            digit = in.readByte();
            count++;
            msgLength += (digit & Byte.MAX_VALUE) * multiplier;
            multiplier *= 128;
        } while ((digit & AnyoMessage.CMD_RESET_CHARGE) != 0);
        return new MultiByteInteger(msgLength, count);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public byte[] encodeMessageId() throws MqttException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeShort(this.msgId);
            dos.flush();
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new MqttException(ex);
        }
    }

    public boolean isRetryable() {
        return false;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void encodeUTF8(DataOutputStream dos, String stringToEncode) throws MqttException {
        try {
            byte[] encodedString = stringToEncode.getBytes("UTF-8");
            byte byte1 = (byte) ((encodedString.length >>> 8) & MotionEventCompat.ACTION_MASK);
            byte byte2 = (byte) ((encodedString.length >>> 0) & MotionEventCompat.ACTION_MASK);
            dos.write(byte1);
            dos.write(byte2);
            dos.write(encodedString);
        } catch (UnsupportedEncodingException ex) {
            throw new MqttException(ex);
        } catch (IOException ex2) {
            throw new MqttException(ex2);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String decodeUTF8(DataInputStream input) throws MqttException {
        try {
            int encodedLength = input.readUnsignedShort();
            byte[] encodedString = new byte[encodedLength];
            input.readFully(encodedString);
            return new String(encodedString, "UTF-8");
        } catch (IOException ex) {
            throw new MqttException(ex);
        }
    }

    public String toString() {
        return PACKET_NAMES[this.type];
    }
}