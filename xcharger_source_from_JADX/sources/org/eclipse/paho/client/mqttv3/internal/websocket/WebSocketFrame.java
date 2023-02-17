package org.eclipse.paho.client.mqttv3.internal.websocket;

import android.support.p000v4.view.MotionEventCompat;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.anyo.bean.request.HeartBeatRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.SecureRandom;

public class WebSocketFrame {
    public static final int frameLengthOverhead = 6;
    private boolean closeFlag = false;
    private boolean fin;
    private byte opcode;
    private byte[] payload;

    public byte getOpcode() {
        return this.opcode;
    }

    public boolean isFin() {
        return this.fin;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public boolean isCloseFlag() {
        return this.closeFlag;
    }

    public WebSocketFrame(byte opcode2, boolean fin2, byte[] payload2) {
        this.opcode = opcode2;
        this.fin = fin2;
        this.payload = payload2;
    }

    public WebSocketFrame(byte[] rawFrame) {
        boolean masked;
        ByteBuffer buffer = ByteBuffer.wrap(rawFrame);
        setFinAndOpCode(buffer.get());
        byte b = buffer.get();
        if ((b & AnyoMessage.CMD_RESET_CHARGE) != 0) {
            masked = true;
        } else {
            masked = false;
        }
        int payloadLength = (byte) (b & Byte.MAX_VALUE);
        int byteCount = 0;
        if (payloadLength == 127) {
            byteCount = 8;
        } else if (payloadLength == 126) {
            byteCount = 2;
        }
        while (true) {
            byteCount--;
            if (byteCount <= 0) {
                break;
            }
            payloadLength |= (buffer.get() & 255) << (byteCount * 8);
        }
        byte[] maskingKey = null;
        if (masked) {
            maskingKey = new byte[4];
            buffer.get(maskingKey, 0, 4);
        }
        this.payload = new byte[payloadLength];
        buffer.get(this.payload, 0, payloadLength);
        if (masked) {
            for (int i = 0; i < this.payload.length; i++) {
                byte[] bArr = this.payload;
                bArr[i] = (byte) (bArr[i] ^ maskingKey[i % 4]);
            }
        }
    }

    private void setFinAndOpCode(byte incomingByte) {
        this.fin = (incomingByte & AnyoMessage.CMD_RESET_CHARGE) != 0;
        this.opcode = (byte) (incomingByte & HeartBeatRequest.PORT_STATUS_FAULT);
    }

    public WebSocketFrame(InputStream input) throws IOException {
        boolean masked = true;
        setFinAndOpCode((byte) input.read());
        if (this.opcode == 2) {
            byte maskLengthByte = (byte) input.read();
            masked = (maskLengthByte & AnyoMessage.CMD_RESET_CHARGE) == 0 ? false : masked;
            int payloadLength = (byte) (maskLengthByte & Byte.MAX_VALUE);
            int byteCount = 0;
            if (payloadLength == 127) {
                byteCount = 8;
            } else if (payloadLength == 126) {
                byteCount = 2;
            }
            payloadLength = byteCount > 0 ? 0 : payloadLength;
            while (true) {
                byteCount--;
                if (byteCount < 0) {
                    break;
                }
                payloadLength |= (((byte) input.read()) & 255) << (byteCount * 8);
            }
            byte[] maskingKey = null;
            if (masked) {
                maskingKey = new byte[4];
                input.read(maskingKey, 0, 4);
            }
            this.payload = new byte[payloadLength];
            int offsetIndex = 0;
            int tempLength = payloadLength;
            while (offsetIndex != payloadLength) {
                int bytesRead = input.read(this.payload, offsetIndex, tempLength);
                offsetIndex += bytesRead;
                tempLength -= bytesRead;
            }
            if (masked) {
                for (int i = 0; i < this.payload.length; i++) {
                    byte[] bArr = this.payload;
                    bArr[i] = (byte) (bArr[i] ^ maskingKey[i % 4]);
                }
            }
        } else if (this.opcode == 8) {
            this.closeFlag = true;
        } else {
            throw new IOException("Invalid Frame: Opcode: " + this.opcode);
        }
    }

    public byte[] encodeFrame() {
        int length = this.payload.length + 6;
        if (this.payload.length > 65535) {
            length += 8;
        } else if (this.payload.length >= 126) {
            length += 2;
        }
        ByteBuffer buffer = ByteBuffer.allocate(length);
        appendFinAndOpCode(buffer, this.opcode, this.fin);
        byte[] mask = generateMaskingKey();
        appendLengthAndMask(buffer, this.payload.length, mask);
        for (int i = 0; i < this.payload.length; i++) {
            byte[] bArr = this.payload;
            byte b = (byte) (bArr[i] ^ mask[i % 4]);
            bArr[i] = b;
            buffer.put(b);
        }
        buffer.flip();
        return buffer.array();
    }

    public static void appendLengthAndMask(ByteBuffer buffer, int length, byte[] mask) {
        if (mask != null) {
            appendLength(buffer, length, true);
            buffer.put(mask);
            return;
        }
        appendLength(buffer, length, false);
    }

    private static void appendLength(ByteBuffer buffer, int length, boolean masked) {
        byte b;
        if (length < 0) {
            throw new IllegalArgumentException("Length cannot be negative");
        }
        if (masked) {
            b = AnyoMessage.CMD_RESET_CHARGE;
        } else {
            b = 0;
        }
        if (length > 65535) {
            buffer.put((byte) (b | Byte.MAX_VALUE));
            buffer.put((byte) 0);
            buffer.put((byte) 0);
            buffer.put((byte) 0);
            buffer.put((byte) 0);
            buffer.put((byte) ((length >> 24) & MotionEventCompat.ACTION_MASK));
            buffer.put((byte) ((length >> 16) & MotionEventCompat.ACTION_MASK));
            buffer.put((byte) ((length >> 8) & MotionEventCompat.ACTION_MASK));
            buffer.put((byte) (length & MotionEventCompat.ACTION_MASK));
        } else if (length >= 126) {
            buffer.put((byte) (b | 126));
            buffer.put((byte) (length >> 8));
            buffer.put((byte) (length & MotionEventCompat.ACTION_MASK));
        } else {
            buffer.put((byte) (b | length));
        }
    }

    public static void appendFinAndOpCode(ByteBuffer buffer, byte opcode2, boolean fin2) {
        byte b = 0;
        if (fin2) {
            b = (byte) 128;
        }
        buffer.put((byte) ((opcode2 & HeartBeatRequest.PORT_STATUS_FAULT) | b));
    }

    public static byte[] generateMaskingKey() {
        SecureRandom secureRandomGenerator = new SecureRandom();
        return new byte[]{(byte) secureRandomGenerator.nextInt(MotionEventCompat.ACTION_MASK), (byte) secureRandomGenerator.nextInt(MotionEventCompat.ACTION_MASK), (byte) secureRandomGenerator.nextInt(MotionEventCompat.ACTION_MASK), (byte) secureRandomGenerator.nextInt(MotionEventCompat.ACTION_MASK)};
    }
}
