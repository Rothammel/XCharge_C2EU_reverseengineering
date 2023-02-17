package net.xcharge.sdk.server.coder.model.rule;

import net.xcharge.sdk.server.coder.model.rule.subitem.MessageDefinition;
import net.xcharge.sdk.server.coder.model.rule.subitem.ValueMap;

/* loaded from: classes.dex */
public class RuleJson {
    private MessageDefinition[] messageDefinitions;
    private String protocol;
    private ValueMap valueMaps;

    public String getProtocol() {
        return this.protocol;
    }

    void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public MessageDefinition[] getMessageDefinitions() {
        return this.messageDefinitions;
    }

    void setMessageDefinitions(MessageDefinition[] messageDefinitions) {
        this.messageDefinitions = messageDefinitions;
    }

    public ValueMap getValueMaps() {
        return this.valueMaps;
    }

    void setValueMaps(ValueMap valueMaps) {
        this.valueMaps = valueMaps;
    }
}
