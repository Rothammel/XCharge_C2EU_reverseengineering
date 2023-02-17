package org.eclipse.paho.client.mqttv3.internal.wire;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.internal.ClientState;
import org.eclipse.paho.client.mqttv3.internal.ExceptionHelper;
import org.eclipse.paho.client.mqttv3.logging.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;

/* loaded from: classes.dex */
public class MqttInputStream extends InputStream {
    private static final String CLASS_NAME = MqttInputStream.class.getName();
    private static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
    private ClientState clientState;
    private DataInputStream in;
    private byte[] packet;
    private long packetLen;
    private ByteArrayOutputStream bais = new ByteArrayOutputStream();
    private long remLen = -1;

    public MqttInputStream(ClientState clientState, InputStream in) {
        this.clientState = null;
        this.clientState = clientState;
        this.in = new DataInputStream(in);
    }

    @Override // java.io.InputStream
    public int read() throws IOException {
        return this.in.read();
    }

    @Override // java.io.InputStream
    public int available() throws IOException {
        return this.in.available();
    }

    @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.in.close();
    }

    public MqttWireMessage readMqttWireMessage() throws IOException, MqttException {
        try {
            if (this.remLen < 0) {
                this.bais.reset();
                byte first = this.in.readByte();
                this.clientState.notifyReceivedBytes(1);
                byte type = (byte) ((first >>> 4) & 15);
                if (type < 1 || type > 14) {
                    throw ExceptionHelper.createMqttException(32108);
                }
                this.remLen = MqttWireMessage.readMBI(this.in).getValue();
                this.bais.write(first);
                this.bais.write(MqttWireMessage.encodeMBI(this.remLen));
                this.packet = new byte[(int) (this.bais.size() + this.remLen)];
                this.packetLen = 0L;
            }
            if (this.remLen < 0) {
                return null;
            }
            readFully();
            this.remLen = -1L;
            byte[] header = this.bais.toByteArray();
            System.arraycopy(header, 0, this.packet, 0, header.length);
            MqttWireMessage message = MqttWireMessage.createWireMessage(this.packet);
            log.fine(CLASS_NAME, "readMqttWireMessage", "501", new Object[]{message});
            return message;
        } catch (SocketTimeoutException e) {
            return null;
        }
    }

    private void readFully() throws IOException {
        int off = this.bais.size() + ((int) this.packetLen);
        int len = (int) (this.remLen - this.packetLen);
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        int n = 0;
        while (n < len) {
            try {
                int count = this.in.read(this.packet, off + n, len - n);
                this.clientState.notifyReceivedBytes(count);
                if (count < 0) {
                    throw new EOFException();
                }
                n += count;
            } catch (SocketTimeoutException e) {
                this.packetLen += n;
                throw e;
            }
        }
    }
}