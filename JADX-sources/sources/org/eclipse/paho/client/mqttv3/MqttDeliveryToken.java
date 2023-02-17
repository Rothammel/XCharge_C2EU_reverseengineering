package org.eclipse.paho.client.mqttv3;

/* loaded from: classes.dex */
public class MqttDeliveryToken extends MqttToken implements IMqttDeliveryToken {
    public MqttDeliveryToken() {
    }

    public MqttDeliveryToken(String logContext) {
        super(logContext);
    }

    @Override // org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
    public MqttMessage getMessage() throws MqttException {
        return this.internalTok.getMessage();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setMessage(MqttMessage msg) {
        this.internalTok.setMessage(msg);
    }
}
