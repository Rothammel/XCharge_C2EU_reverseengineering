package net.xcharge.sdk.server.coder.model;

import java.util.HashMap;
import net.xcharge.sdk.server.coder.model.rule.RuleJson;
import net.xcharge.sdk.server.coder.model.rule.subitem.Field;
import net.xcharge.sdk.server.coder.model.rule.subitem.MessageDefinition;
import net.xcharge.sdk.server.coder.model.rule.subitem.MessageType;
import net.xcharge.sdk.server.coder.model.rule.subitem.ValueMap;
import net.xcharge.sdk.server.coder.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public class Rule {
    private ValueMap valueMap;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private HashMap<String, MessageType> typeSearchByName = new HashMap<>();
    private HashMap<String, MessageType> typeSearchById = new HashMap<>();
    private HashMap<String, HashMap<Integer, Field>> fieldSearch = new HashMap<>();

    public Rule(RuleJson ruleJson) {
        MessageDefinition[] messageDefinitions;
        Field[] fields;
        this.valueMap = ruleJson.getValueMaps();
        for (MessageDefinition definition : ruleJson.getMessageDefinitions()) {
            MessageType messageType = definition.getMassageType();
            HashMap<Integer, Field> fields2 = new HashMap<>();
            for (Field field : definition.getFields()) {
                fields2.put(Integer.valueOf(field.getId()), field);
            }
            this.logger.debug("encoder load {} with {}", JsonUtil.GSON.toJson(messageType), JsonUtil.GSON.toJson(fields2));
            this.fieldSearch.put(messageType.getName() + messageType.getVersion(), fields2);
            this.typeSearchByName.put(messageType.getName() + messageType.getVersion(), messageType);
            this.typeSearchById.put(messageType.getId() + messageType.getVersion(), messageType);
        }
    }

    public HashMap<String, MessageType> getTypeSearchByName() {
        return this.typeSearchByName;
    }

    public void setTypeSearchByName(HashMap<String, MessageType> typeSearchByName) {
        this.typeSearchByName = typeSearchByName;
    }

    public HashMap<String, MessageType> getTypeSearchById() {
        return this.typeSearchById;
    }

    public void setTypeSearchById(HashMap<String, MessageType> typeSearchById) {
        this.typeSearchById = typeSearchById;
    }

    public HashMap<String, HashMap<Integer, Field>> getFieldSearch() {
        return this.fieldSearch;
    }

    public void setFieldSearch(HashMap<String, HashMap<Integer, Field>> fieldSearch) {
        this.fieldSearch = fieldSearch;
    }

    public ValueMap getValueMap() {
        return this.valueMap;
    }

    public void setValueMap(ValueMap valueMap) {
        this.valueMap = valueMap;
    }
}