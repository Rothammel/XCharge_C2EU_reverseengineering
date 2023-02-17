package net.xcharge.sdk.server.coder;

import java.util.HashMap;
import net.xcharge.sdk.server.coder.exceptions.MessageEncoderException;
import net.xcharge.sdk.server.coder.model.Rule;
import net.xcharge.sdk.server.coder.model.rule.subitem.Field;
import net.xcharge.sdk.server.coder.model.rule.subitem.MessageType;
import net.xcharge.sdk.server.coder.model.rule.subitem.ValueMap;
import net.xcharge.sdk.server.coder.utils.CodeUtil;
import net.xcharge.sdk.server.coder.utils.JsonUtil;
import net.xcharge.sdk.server.coder.utils.MessageCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Coder {
    private HashMap<String, HashMap<Integer, Field>> fieldSearch;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private HashMap<String, MessageType> typeSearchById;
    private HashMap<String, MessageType> typeSearchByName;
    private ValueMap valueMap;

    public Coder(Rule rule) {
        this.typeSearchByName = rule.getTypeSearchByName();
        this.typeSearchById = rule.getTypeSearchById();
        this.fieldSearch = rule.getFieldSearch();
        this.valueMap = rule.getValueMap();
    }

    public byte[] encode(String deviceSourceId, String msgName, String msgVersion, String msgData) throws MessageEncoderException {
        HashMap<Integer, Field> fields = this.fieldSearch.get(msgName + msgVersion);
        this.logger.info("message encode rule = {} ", (Object) JsonUtil.GSON.toJson((Object) fields));
        this.logger.info("message encode data = {} ", (Object) msgData);
        String messageDate = MessageCheck.check(msgData);
        this.logger.info("message encode data cover to = {} ", (Object) messageDate);
        byte[] result = Encoder.encode(deviceSourceId, this.typeSearchByName.get(msgName + msgVersion), this.valueMap, fields.values(), msgVersion, messageDate);
        this.logger.info("message encode rslt = {} ", (Object) CodeUtil.bytes2pString(result));
        this.logger.info("message encode rslt = {} ", (Object) CodeUtil.bytes2binaryString(result));
        return result;
    }

    public String[] decode(String deviceSourceId, byte[] msg) throws MessageEncoderException {
        this.logger.info("message decode data = {} ", (Object) CodeUtil.bytes2pString(msg));
        this.logger.info("message decode data = {} ", (Object) CodeUtil.bytes2binaryString(msg));
        String[] result = Decoder.decode(this.typeSearchById, this.fieldSearch, this.valueMap, deviceSourceId, msg);
        this.logger.info("message decode rslt = {} {} {}", result[0], result[1], result[2]);
        return result;
    }
}
