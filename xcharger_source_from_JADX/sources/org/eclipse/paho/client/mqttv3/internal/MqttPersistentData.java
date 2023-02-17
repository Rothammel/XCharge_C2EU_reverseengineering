package org.eclipse.paho.client.mqttv3.internal;

import org.eclipse.paho.client.mqttv3.MqttPersistable;

public class MqttPersistentData implements MqttPersistable {
    private int hLength = 0;
    private int hOffset = 0;
    private byte[] header = null;
    private String key = null;
    private int pLength = 0;
    private int pOffset = 0;
    private byte[] payload = null;

    public MqttPersistentData(String key2, byte[] header2, int hOffset2, int hLength2, byte[] payload2, int pOffset2, int pLength2) {
        this.key = key2;
        this.header = header2;
        this.hOffset = hOffset2;
        this.hLength = hLength2;
        this.payload = payload2;
        this.pOffset = pOffset2;
        this.pLength = pLength2;
    }

    public String getKey() {
        return this.key;
    }

    public byte[] getHeaderBytes() {
        return this.header;
    }

    public int getHeaderLength() {
        return this.hLength;
    }

    public int getHeaderOffset() {
        return this.hOffset;
    }

    public byte[] getPayloadBytes() {
        return this.payload;
    }

    public int getPayloadLength() {
        if (this.payload == null) {
            return 0;
        }
        return this.pLength;
    }

    public int getPayloadOffset() {
        return this.pOffset;
    }
}
