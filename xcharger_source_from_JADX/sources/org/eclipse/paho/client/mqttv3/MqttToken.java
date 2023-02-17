package org.eclipse.paho.client.mqttv3;

import org.eclipse.paho.client.mqttv3.internal.Token;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;

public class MqttToken implements IMqttToken {
    public Token internalTok = null;

    public MqttToken() {
    }

    public MqttToken(String logContext) {
        this.internalTok = new Token(logContext);
    }

    public MqttException getException() {
        return this.internalTok.getException();
    }

    public boolean isComplete() {
        return this.internalTok.isComplete();
    }

    public void setActionCallback(IMqttActionListener listener) {
        this.internalTok.setActionCallback(listener);
    }

    public IMqttActionListener getActionCallback() {
        return this.internalTok.getActionCallback();
    }

    public void waitForCompletion() throws MqttException {
        this.internalTok.waitForCompletion(-1);
    }

    public void waitForCompletion(long timeout) throws MqttException {
        this.internalTok.waitForCompletion(timeout);
    }

    public IMqttAsyncClient getClient() {
        return this.internalTok.getClient();
    }

    public String[] getTopics() {
        return this.internalTok.getTopics();
    }

    public Object getUserContext() {
        return this.internalTok.getUserContext();
    }

    public void setUserContext(Object userContext) {
        this.internalTok.setUserContext(userContext);
    }

    public int getMessageId() {
        return this.internalTok.getMessageID();
    }

    public int[] getGrantedQos() {
        return this.internalTok.getGrantedQos();
    }

    public boolean getSessionPresent() {
        return this.internalTok.getSessionPresent();
    }

    public MqttWireMessage getResponse() {
        return this.internalTok.getResponse();
    }
}
