package org.eclipse.paho.client.mqttv3;

public class MqttDeliveryToken extends MqttToken implements IMqttDeliveryToken {
    public MqttDeliveryToken() {
    }

    public MqttDeliveryToken(String logContext) {
        super(logContext);
    }

    public MqttMessage getMessage() throws MqttException {
        return this.internalTok.getMessage();
    }

    /* access modifiers changed from: protected */
    public void setMessage(MqttMessage msg) {
        this.internalTok.setMessage(msg);
    }
}
