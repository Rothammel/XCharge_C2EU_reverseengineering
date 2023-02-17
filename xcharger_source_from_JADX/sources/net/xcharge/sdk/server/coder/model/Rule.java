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

public class Rule {
    private HashMap<String, HashMap<Integer, Field>> fieldSearch;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private HashMap<String, MessageType> typeSearchById;
    private HashMap<String, MessageType> typeSearchByName;
    private ValueMap valueMap;

    public Rule(RuleJson ruleJson) {
        this.valueMap = ruleJson.getValueMaps();
        this.typeSearchByName = new HashMap<>();
        this.typeSearchById = new HashMap<>();
        this.fieldSearch = new HashMap<>();
        for (MessageDefinition definition : ruleJson.getMessageDefinitions()) {
            MessageType messageType = definition.getMassageType();
            HashMap<Integer, Field> fields = new HashMap<>();
            for (Field field : definition.getFields()) {
                fields.put(Integer.valueOf(field.getId()), field);
            }
            this.logger.debug("encoder load {} with {}", (Object) JsonUtil.GSON.toJson((Object) messageType), (Object) JsonUtil.GSON.toJson((Object) fields));
            this.fieldSearch.put(messageType.getName() + messageType.getVersion(), fields);
            this.typeSearchByName.put(messageType.getName() + messageType.getVersion(), messageType);
            this.typeSearchById.put(messageType.getId() + messageType.getVersion(), messageType);
        }
    }

    public HashMap<String, MessageType> getTypeSearchByName() {
        return this.typeSearchByName;
    }

    public void setTypeSearchByName(HashMap<String, MessageType> typeSearchByName2) {
        this.typeSearchByName = typeSearchByName2;
    }

    public HashMap<String, MessageType> getTypeSearchById() {
        return this.typeSearchById;
    }

    public void setTypeSearchById(HashMap<String, MessageType> typeSearchById2) {
        this.typeSearchById = typeSearchById2;
    }

    public HashMap<String, HashMap<Integer, Field>> getFieldSearch() {
        return this.fieldSearch;
    }

    public void setFieldSearch(HashMap<String, HashMap<Integer, Field>> fieldSearch2) {
        this.fieldSearch = fieldSearch2;
    }

    public ValueMap getValueMap() {
        return this.valueMap;
    }

    public void setValueMap(ValueMap valueMap2) {
        this.valueMap = valueMap2;
    }
}
