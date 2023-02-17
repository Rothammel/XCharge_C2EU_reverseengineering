package net.xcharge.sdk.server.coder.model.rule.subitem;

/* loaded from: classes.dex */
public class MessageDefinition {
    private Field[] fields;
    private MessageType messageType;

    public MessageType getMassageType() {
        return this.messageType;
    }

    void setMassageType(MessageType massageType) {
        this.messageType = massageType;
    }

    public Field[] getFields() {
        return this.fields;
    }

    void setFields(Field[] fields) {
        this.fields = fields;
    }
}