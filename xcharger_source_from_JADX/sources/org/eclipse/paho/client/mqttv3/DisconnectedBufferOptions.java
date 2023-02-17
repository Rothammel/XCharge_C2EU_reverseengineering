package org.eclipse.paho.client.mqttv3;

public class DisconnectedBufferOptions {
    public static final boolean DELETE_OLDEST_MESSAGES_DEFAULT = false;
    public static final boolean DISCONNECTED_BUFFER_ENABLED_DEFAULT = false;
    public static final int DISCONNECTED_BUFFER_SIZE_DEFAULT = 5000;
    public static final boolean PERSIST_DISCONNECTED_BUFFER_DEFAULT = false;
    private boolean bufferEnabled = false;
    private int bufferSize = 5000;
    private boolean deleteOldestMessages = false;
    private boolean persistBuffer = false;

    public int getBufferSize() {
        return this.bufferSize;
    }

    public void setBufferSize(int bufferSize2) {
        if (bufferSize2 < 1) {
            throw new IllegalArgumentException();
        }
        this.bufferSize = bufferSize2;
    }

    public boolean isBufferEnabled() {
        return this.bufferEnabled;
    }

    public void setBufferEnabled(boolean bufferEnabled2) {
        this.bufferEnabled = bufferEnabled2;
    }

    public boolean isPersistBuffer() {
        return this.persistBuffer;
    }

    public void setPersistBuffer(boolean persistBuffer2) {
        this.persistBuffer = persistBuffer2;
    }

    public boolean isDeleteOldestMessages() {
        return this.deleteOldestMessages;
    }

    public void setDeleteOldestMessages(boolean deleteOldestMessages2) {
        this.deleteOldestMessages = deleteOldestMessages2;
    }
}
