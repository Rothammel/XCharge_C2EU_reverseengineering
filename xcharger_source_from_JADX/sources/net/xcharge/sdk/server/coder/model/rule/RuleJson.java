package net.xcharge.sdk.server.coder.model.rule;

import net.xcharge.sdk.server.coder.model.rule.subitem.MessageDefinition;
import net.xcharge.sdk.server.coder.model.rule.subitem.ValueMap;

public class RuleJson {
    private MessageDefinition[] messageDefinitions;
    private String protocol;
    private ValueMap valueMaps;

    public String getProtocol() {
        return this.protocol;
    }

    /* access modifiers changed from: package-private */
    public void setProtocol(String protocol2) {
        this.protocol = protocol2;
    }

    public MessageDefinition[] getMessageDefinitions() {
        return this.messageDefinitions;
    }

    /* access modifiers changed from: package-private */
    public void setMessageDefinitions(MessageDefinition[] messageDefinitions2) {
        this.messageDefinitions = messageDefinitions2;
    }

    public ValueMap getValueMaps() {
        return this.valueMaps;
    }

    /* access modifiers changed from: package-private */
    public void setValueMaps(ValueMap valueMaps2) {
        this.valueMaps = valueMaps2;
    }
}
