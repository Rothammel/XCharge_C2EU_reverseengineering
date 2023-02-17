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

public class MqttInputStream extends InputStream {
    private static final String CLASS_NAME = MqttInputStream.class.getName();
    private static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
    private ByteArrayOutputStream bais;
    private ClientState clientState = null;

    /* renamed from: in */
    private DataInputStream f202in;
    private byte[] packet;
    private long packetLen;
    private long remLen;

    public MqttInputStream(ClientState clientState2, InputStream in) {
        this.clientState = clientState2;
        this.f202in = new DataInputStream(in);
        this.bais = new ByteArrayOutputStream();
        this.remLen = -1;
    }

    public int read() throws IOException {
        return this.f202in.read();
    }

    public int available() throws IOException {
        return this.f202in.available();
    }

    public void close() throws IOException {
        this.f202in.close();
    }

    public MqttWireMessage readMqttWireMessage() throws IOException, MqttException {
        try {
            if (this.remLen < 0) {
                this.bais.reset();
                byte first = this.f202in.readByte();
                this.clientState.notifyReceivedBytes(1);
                byte type = (byte) ((first >>> 4) & 15);
                if (type < 1 || type > 14) {
                    throw ExceptionHelper.createMqttException(32108);
                }
                this.remLen = MqttWireMessage.readMBI(this.f202in).getValue();
                this.bais.write(first);
                this.bais.write(MqttWireMessage.encodeMBI(this.remLen));
                this.packet = new byte[((int) (((long) this.bais.size()) + this.remLen))];
                this.packetLen = 0;
            }
            if (this.remLen < 0) {
                return null;
            }
            readFully();
            this.remLen = -1;
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
                int count = this.f202in.read(this.packet, off + n, len - n);
                this.clientState.notifyReceivedBytes(count);
                if (count < 0) {
                    throw new EOFException();
                }
                n += count;
            } catch (SocketTimeoutException e) {
                this.packetLen += (long) n;
                throw e;
            }
        }
    }
}
