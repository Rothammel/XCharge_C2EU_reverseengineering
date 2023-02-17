package net.xcharge.sdk.server.coder.model.rule.subitem;

public class MessageDefinition {
    private Field[] fields;
    private MessageType messageType;

    public MessageType getMassageType() {
        return this.messageType;
    }

    /* access modifiers changed from: package-private */
    public void setMassageType(MessageType massageType) {
        this.messageType = massageType;
    }

    public Field[] getFields() {
        return this.fields;
    }

    /* access modifiers changed from: package-private */
    public void setFields(Field[] fields2) {
        this.fields = fields2;
    }
}
