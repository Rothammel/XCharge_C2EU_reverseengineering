package net.xcharge.sdk.server.coder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import net.xcharge.sdk.server.coder.constant.GlobalConstant;
import net.xcharge.sdk.server.coder.exceptions.MessageEncoderException;
import net.xcharge.sdk.server.coder.model.rule.subitem.Field;
import net.xcharge.sdk.server.coder.model.rule.subitem.MessageType;
import net.xcharge.sdk.server.coder.model.rule.subitem.SubField;
import net.xcharge.sdk.server.coder.model.rule.subitem.ValueMap;
import net.xcharge.sdk.server.coder.utils.CodeUtil;
import net.xcharge.sdk.server.coder.utils.DateUtil;
import net.xcharge.sdk.server.coder.utils.JsonUtil;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public class Decoder {
    private static Logger logger = LoggerFactory.getLogger(Decoder.class);

    private static boolean isUint(String type) {
        return type.startsWith("uint");
    }

    private static long localId2cloudId(String subtype, String deviceSourceId, short localId) {
        return localId;
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static void read(HashMap<Integer, Field> fields, ValueMap valueMap, InputStream inputStream, JsonObject json, String deviceSourceId) throws Exception {
        int fieldId = inputStream.read();
        if (fieldId != -1) {
            String fieldType = CodeUtil.int2BinaryString(fieldId, 8).substring(0, 3);
            Field field = fields.get(Integer.valueOf(fieldId));
            if (field == null) {
                throw new MessageEncoderException(fieldType + " not exist");
            }
            String[] fieldName = field.getName().split("\\.");
            String valueMapName = field.getValueMap();
            String productRelate = field.isProductFamilyRelated() ? deviceSourceId.substring(0, 2) : "";
            if (StringUtils.isNotBlank(productRelate)) {
                valueMapName = valueMapName + productRelate;
            }
            int scaleFactor = field.getScaleFactor() == 0 ? 1 : field.getScaleFactor();
            int arrayLength = field.getArrayLength();
            if ("000".equals(fieldType)) {
                int v = inputStream.read();
                if (StringUtils.isNotBlank(valueMapName)) {
                    String vs = CodeUtil.getKeyFromValueMap(valueMap, valueMapName, v);
                    JsonUtil.append(json, new JsonPrimitive(vs), fieldName);
                } else if (StringUtils.equalsIgnoreCase(GlobalConstant.Boolean, field.getType())) {
                    JsonUtil.append(json, new JsonPrimitive(Boolean.valueOf(v == 1)), fieldName);
                } else {
                    if (!isUint(field.getType())) {
                        v = (byte) v;
                    }
                    JsonUtil.append(json, new JsonPrimitive((Number) Integer.valueOf(v)), arrayLength, fieldName);
                }
            }
            if ("001".equals(fieldType)) {
                byte[] bytes = new byte[2];
                inputStream.read(bytes);
                int v2 = CodeUtil.bytes2Int(bytes);
                if (StringUtils.isNotBlank(valueMapName)) {
                    String vs2 = CodeUtil.getKeyFromValueMap(valueMap, valueMapName, v2);
                    JsonUtil.append(json, new JsonPrimitive(vs2), fieldName);
                } else if (StringUtils.equals(GlobalConstant.localId, field.getType())) {
                    long vs3 = localId2cloudId(field.getSubtype(), deviceSourceId, (short) v2);
                    JsonUtil.append(json, new JsonPrimitive((Number) Long.valueOf(vs3)), fieldName);
                } else if (scaleFactor > 1) {
                    double d = new BigDecimal(String.valueOf(v2)).divide(new BigDecimal(String.valueOf(scaleFactor))).doubleValue();
                    JsonUtil.append(json, new JsonPrimitive((Number) Double.valueOf(d)), arrayLength, fieldName);
                } else {
                    if (!isUint(field.getType())) {
                        v2 = (short) v2;
                    }
                    JsonUtil.append(json, new JsonPrimitive((Number) Integer.valueOf(v2)), arrayLength, fieldName);
                }
            }
            if ("010".equals(fieldType)) {
                byte[] bytes2 = new byte[4];
                inputStream.read(bytes2);
                int v3 = CodeUtil.bytes2Int(bytes2);
                if (StringUtils.isNotBlank(valueMapName)) {
                    String vs4 = CodeUtil.getKeyFromValueMap(valueMap, valueMapName, v3);
                    JsonUtil.append(json, new JsonPrimitive(vs4), fieldName);
                } else if (StringUtils.equals(GlobalConstant.timestamp, field.getType())) {
                    long time = NumberUtils.toLong(DateUtil.parse_yyyyMMddHHmmss(v3 * 1000));
                    JsonUtil.append(json, new JsonPrimitive((Number) Long.valueOf(time)), fieldName);
                } else if (StringUtils.equals(GlobalConstant.color, field.getType())) {
                    byte[] bs = CodeUtil.int2Bytes(v3);
                    JsonUtil.append(json, new JsonPrimitive(MqttTopic.MULTI_LEVEL_WILDCARD + CodeUtil.byte2hexString(bs[1]) + CodeUtil.byte2hexString(bs[2]) + CodeUtil.byte2hexString(bs[3])), fieldName);
                } else if (scaleFactor > 1) {
                    JsonUtil.append(json, new JsonPrimitive((Number) Double.valueOf(new BigDecimal(String.valueOf(v3)).divide(new BigDecimal(String.valueOf(scaleFactor))).doubleValue())), arrayLength, fieldName);
                } else {
                    JsonUtil.append(json, new JsonPrimitive((Number) Integer.valueOf(v3)), arrayLength, fieldName);
                }
            }
            if ("011".equals(fieldType)) {
                int size = inputStream.read();
                byte[] bytes3 = new byte[size];
                inputStream.read(bytes3);
                String string = new String(bytes3, CharEncoding.UTF_8);
                if (!string.endsWith("\\0")) {
                    throw new MessageEncoderException("error, string in message is not end with \\0");
                }
                JsonUtil.append(json, new JsonPrimitive(string.substring(0, string.length() - 2)), fieldName);
            }
            if ("101".equals(fieldType)) {
                int size2 = inputStream.read();
                int num = size2 * 2;
                byte[] bytes4 = new byte[num];
                inputStream.read(bytes4);
                JsonArray v4 = new JsonArray();
                if (StringUtils.equals(GlobalConstant.timedPrice, field.getSubtype())) {
                    if ((num / 2) % 6 != 0) {
                        throw new MessageEncoderException("error, timedPrice size%6!=0 ");
                    }
                    for (int i = 0; i < num / 12; i++) {
                        int mark = i * 12;
                        JsonArray vv = new JsonArray();
                        vv.add(Integer.valueOf(CodeUtil.bytes2Int(bytes4[mark + 0], bytes4[mark + 1])));
                        vv.add(Integer.valueOf(CodeUtil.bytes2Int(bytes4[mark + 2], bytes4[mark + 3])));
                        vv.add(Integer.valueOf(CodeUtil.bytes2Int(bytes4[mark + 4], bytes4[mark + 5])));
                        vv.add(Integer.valueOf(CodeUtil.bytes2Int(bytes4[mark + 6], bytes4[mark + 7])));
                        vv.add(Integer.valueOf(CodeUtil.bytes2Int(bytes4[mark + 8], bytes4[mark + 9])));
                        vv.add(Integer.valueOf(CodeUtil.bytes2Int(bytes4[mark + 10], bytes4[mark + 11])));
                        v4.add(vv);
                    }
                    JsonUtil.append(json, v4, fieldName);
                } else {
                    for (int i2 = 0; i2 < num / 2; i2++) {
                        int mark2 = i2 * 2;
                        v4.add(Integer.valueOf(CodeUtil.bytes2Int(bytes4[mark2 + 0], bytes4[mark2 + 1])));
                    }
                    JsonUtil.append(json, v4, fieldName);
                }
            }
            if ("111".equals(fieldType)) {
                int size3 = inputStream.read();
                JsonArray jsonArray = new JsonArray();
                for (int i3 = 0; i3 < size3; i3++) {
                    int length = inputStream.read();
                    byte[] bytes5 = new byte[length];
                    inputStream.read(bytes5);
                    JsonObject _json = new JsonObject();
                    InputStream _stream = new ByteArrayInputStream(bytes5);
                    read(subFields2fieldMap(field.getSubfields()), valueMap, _stream, _json, deviceSourceId);
                    jsonArray.getAsJsonArray().add(_json);
                }
                if (StringUtils.equals(GlobalConstant.port, field.getSubtype())) {
                    JsonObject j = new JsonObject();
                    Iterator<JsonElement> it2 = jsonArray.getAsJsonArray().iterator();
                    while (it2.hasNext()) {
                        JsonElement e = it2.next();
                        JsonObject _e = e.getAsJsonObject();
                        String port = _e.get(GlobalConstant.port).getAsString();
                        _e.remove(GlobalConstant.port);
                        j.add(port, _e);
                    }
                    jsonArray = j;
                }
                if (StringUtils.equals(GlobalConstant.position, field.getSubtype())) {
                    JsonObject j2 = new JsonObject();
                    Iterator<JsonElement> it3 = jsonArray.getAsJsonArray().iterator();
                    while (it3.hasNext()) {
                        JsonElement e2 = it3.next();
                        JsonObject _e2 = e2.getAsJsonObject();
                        String position = _e2.get(GlobalConstant.position).getAsString();
                        _e2.remove(GlobalConstant.position);
                        if (!j2.has(position)) {
                            j2.add(position, new JsonArray());
                        }
                        j2.get(position).getAsJsonArray().add(_e2);
                    }
                    jsonArray = j2;
                }
                JsonUtil.append(json, jsonArray, fieldName);
            }
            read(fields, valueMap, inputStream, json, deviceSourceId);
        }
    }

    private static HashMap<Integer, Field> subFields2fieldMap(SubField... subFields) {
        HashMap map = new HashMap();
        for (SubField subField : subFields) {
            String json = JsonUtil.GSON.toJson(subField);
            Field field = (Field) JsonUtil.GSON.fromJson(json, (Class<Object>) Field.class);
            map.put(Integer.valueOf(field.getId()), field);
        }
        return map;
    }

    public static String[] decode(HashMap<String, MessageType> typeSearch, HashMap<String, HashMap<Integer, Field>> fieldSearch, ValueMap valueMap, String deviceSourceId, byte[] msg) throws MessageEncoderException {
        MessageType messageType = null;
        InputStream inputStream = new ByteArrayInputStream(msg);
        try {
            byte read = (byte) inputStream.read();
            int messageTypeId = inputStream.read();
            int messageVersion = inputStream.read();
            String v = CodeUtil.int2BinaryString(messageVersion, 8);
            String version = "v" + CodeUtil.binaryString2Integer(v.substring(0, 4)) + "." + CodeUtil.binaryString2Integer(v.substring(4, 8));
            messageType = typeSearch.get(messageTypeId + version);
        } catch (Exception e) {
            logger.error("", (Throwable) e);
        }
        if (messageType == null) {
            throw new MessageEncoderException("error, because message type not exist");
        }
        HashMap<Integer, Field> fieldList = fieldSearch.get(messageType.getName() + messageType.getVersion());
        JsonObject json = new JsonObject();
        try {
            read(fieldList, valueMap, inputStream, json, deviceSourceId);
            String[] result = {messageType.getName(), messageType.getVersion(), json.toString()};
            return result;
        } catch (Exception e2) {
            logger.error("", (Throwable) e2);
            throw new MessageEncoderException("error, because there is some wrong");
        }
    }
}