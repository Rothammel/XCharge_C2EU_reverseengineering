package net.xcharge.sdk.server.coder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import net.xcharge.sdk.server.coder.constant.GlobalConstant;
import net.xcharge.sdk.server.coder.exceptions.MessageEncoderException;
import net.xcharge.sdk.server.coder.model.rule.subitem.Field;
import net.xcharge.sdk.server.coder.model.rule.subitem.MessageType;
import net.xcharge.sdk.server.coder.model.rule.subitem.SubField;
import net.xcharge.sdk.server.coder.model.rule.subitem.ValueMap;
import net.xcharge.sdk.server.coder.utils.CodeUtil;
import net.xcharge.sdk.server.coder.utils.DateUtil;
import net.xcharge.sdk.server.coder.utils.JsonUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public class Encoder {
    private static Logger logger = LoggerFactory.getLogger(Encoder.class);

    private static byte[] convert2Header(MessageType messageType, String version) {
        String versionNum = version.substring(1);
        String[] versionChars = versionNum.split("\\.");
        char[] versionBinaryA = CodeUtil.int2BinaryChars(NumberUtils.toInt(versionChars[0]), 4);
        char[] versionBinaryB = CodeUtil.int2BinaryChars(NumberUtils.toInt(versionChars[1]), 4);
        String versionBinaryString = new String(versionBinaryA) + new String(versionBinaryB);
        byte[] header = {1, (byte) messageType.getId(), CodeUtil.binaryString2Integer(versionBinaryString).byteValue()};
        return header;
    }

    private static short cloudId2localId(String subtype, String deviceSourceId, long id) {
        return (short) id;
    }

    private static byte[] convertByField(String deviceSourceId, JsonElement element, SubField field, ValueMap valueMap, SubField[] subFields) throws MessageEncoderException {
        int value;
        byte value2;
        byte fieldId = (byte) field.getId();
        byte[] fieldBody = new byte[0];
        String fieldType = CodeUtil.int2BinaryString(fieldId, 8).substring(0, 3);
        String valueMapName = field.getValueMap();
        String productRelate = field.isProductFamilyRelated() ? deviceSourceId.substring(0, 2) : "";
        if (StringUtils.isNotBlank(productRelate)) {
            valueMapName = valueMapName + productRelate;
        }
        int scaleFactor = field.getScaleFactor() == 0 ? 1 : field.getScaleFactor();
        if ("000".equals(fieldType)) {
            if (StringUtils.isNotBlank(field.getValueMap())) {
                value2 = CodeUtil.getValueFromValueMap(valueMap, valueMapName, element.getAsString()).byteValue();
            } else if (StringUtils.equalsIgnoreCase(GlobalConstant.Boolean, field.getType())) {
                value2 = (byte) (element.getAsBoolean() ? 1 : 0);
            } else {
                value2 = element.getAsByte();
            }
            fieldBody = new byte[]{(byte) (value2 * scaleFactor)};
        }
        if ("001".equals(fieldType)) {
            Short value3 = null;
            if (StringUtils.isNotBlank(field.getValueMap())) {
                if (!element.isJsonObject()) {
                    value3 = Short.valueOf(CodeUtil.getValueFromValueMap(valueMap, valueMapName, element.getAsString()).shortValue());
                }
            } else if (StringUtils.equals(GlobalConstant.localId, field.getType())) {
                value3 = Short.valueOf(cloudId2localId(field.getSubtype(), deviceSourceId, element.getAsLong()));
            } else {
                value3 = Short.valueOf((short) (element.getAsDouble() * scaleFactor));
            }
            if (value3 != null) {
                fieldBody = CodeUtil.short2Bytes(value3.shortValue());
            }
        }
        if ("010".equals(fieldType)) {
            if (StringUtils.equals(GlobalConstant.timestamp, field.getType())) {
                Date date = DateUtil.parse_yyyyMMddHHmmss(element.getAsString());
                if (date == null) {
                    throw new MessageEncoderException("date error");
                }
                long time = date.getTime();
                value = (int) (time / 1000);
            } else if (StringUtils.equals(GlobalConstant.color, field.getType())) {
                String color = element.getAsString().replaceAll(MqttTopic.MULTI_LEVEL_WILDCARD, "00");
                value = CodeUtil.hexString2int(color);
            } else {
                value = (int) (element.getAsDouble() * scaleFactor);
            }
            fieldBody = CodeUtil.int2Bytes(value);
        }
        if ("011".equals(fieldType)) {
            byte[] bytes = new String(element.getAsString() + "\\0").getBytes(Charset.forName(CharEncoding.UTF_8));
            fieldBody = ArrayUtils.add(bytes, 0, (byte) bytes.length);
        }
        if ("101".equals(fieldType)) {
            JsonArray value4 = element.getAsJsonArray();
            if (StringUtils.equals(GlobalConstant.timedPrice, field.getSubtype())) {
                int size = 0;
                byte[] bytes2 = new byte[0];
                Iterator<JsonElement> it2 = value4.iterator();
                while (it2.hasNext()) {
                    JsonElement e = it2.next();
                    Iterator<JsonElement> it3 = e.getAsJsonArray().iterator();
                    while (it3.hasNext()) {
                        short v = it3.next().getAsShort();
                        bytes2 = ArrayUtils.addAll(bytes2, CodeUtil.short2Bytes(v));
                        size++;
                    }
                }
                fieldBody = ArrayUtils.add((byte[]) bytes2.clone(), 0, (byte) size);
            } else {
                byte[] bytes3 = {(byte) value4.size()};
                Iterator<JsonElement> it4 = value4.iterator();
                while (it4.hasNext()) {
                    JsonElement e2 = it4.next();
                    short v2 = e2.getAsShort();
                    bytes3 = ArrayUtils.addAll(bytes3, CodeUtil.short2Bytes(v2));
                }
                fieldBody = bytes3;
            }
        }
        if ("111".equals(fieldType)) {
            if (StringUtils.equals(GlobalConstant.port, field.getSubtype())) {
                JsonObject value5 = element.getAsJsonObject();
                byte[] bytes4 = {(byte) value5.entrySet().size()};
                for (Map.Entry<String, JsonElement> entry : value5.entrySet()) {
                    JsonObject v3 = entry.getValue().getAsJsonObject();
                    byte[] bs = new byte[0];
                    int length = subFields.length;
                    int i = 0;
                    while (true) {
                        int i2 = i;
                        if (i2 < length) {
                            SubField subField = subFields[i2];
                            JsonElement ee = JsonUtil.find(v3, subField.getName());
                            if (StringUtils.equals(GlobalConstant.port, subField.getName())) {
                                ee = new JsonPrimitive(entry.getKey());
                            }
                            if (ee != null) {
                                bs = ArrayUtils.addAll(bs, convertByField(deviceSourceId, ee, subField, valueMap, null));
                            }
                            i = i2 + 1;
                        }
                    }
                    bytes4 = ArrayUtils.addAll(bytes4, ArrayUtils.add(bs, 0, (byte) bs.length));
                }
                fieldBody = bytes4;
            } else if (StringUtils.equals(GlobalConstant.position, field.getSubtype())) {
                byte[] bytes5 = new byte[0];
                int sizeO = 0;
                for (Map.Entry<String, JsonElement> entry2 : element.getAsJsonObject().entrySet()) {
                    JsonArray vv = entry2.getValue().getAsJsonArray();
                    Iterator<JsonElement> it5 = vv.iterator();
                    while (it5.hasNext()) {
                        JsonElement _v = it5.next();
                        JsonObject v4 = _v.getAsJsonObject();
                        byte[] bs2 = new byte[0];
                        int length2 = subFields.length;
                        int i3 = 0;
                        while (true) {
                            int i4 = i3;
                            if (i4 < length2) {
                                SubField subField2 = subFields[i4];
                                JsonElement ee2 = JsonUtil.find(v4, subField2.getName());
                                if (StringUtils.equals(GlobalConstant.position, subField2.getName())) {
                                    ee2 = new JsonPrimitive(entry2.getKey());
                                }
                                if (ee2 != null) {
                                    bs2 = ArrayUtils.addAll(bs2, convertByField(deviceSourceId, ee2, subField2, valueMap, null));
                                }
                                i3 = i4 + 1;
                            }
                        }
                        bytes5 = ArrayUtils.addAll(bytes5, ArrayUtils.add(bs2, 0, (byte) bs2.length));
                        sizeO++;
                    }
                }
                fieldBody = ArrayUtils.add(bytes5, 0, (byte) sizeO);
            } else {
                JsonArray value6 = element.getAsJsonArray();
                byte[] bytes6 = {(byte) value6.size()};
                Iterator<JsonElement> it6 = value6.iterator();
                while (it6.hasNext()) {
                    JsonElement e3 = it6.next();
                    JsonObject v5 = e3.getAsJsonObject();
                    byte[] bs3 = new byte[0];
                    for (SubField subField3 : subFields) {
                        JsonElement ee3 = JsonUtil.find(v5, subField3.getName());
                        if (ee3 != null) {
                            bs3 = ArrayUtils.addAll(bs3, convertByField(deviceSourceId, ee3, subField3, valueMap, null));
                        }
                    }
                    bytes6 = ArrayUtils.addAll(bytes6, ArrayUtils.add(bs3, 0, (byte) bs3.length));
                }
                fieldBody = bytes6;
            }
        }
        byte[] result = {fieldId};
        if (fieldBody != null && fieldBody.length > 0) {
            return ArrayUtils.addAll(result, fieldBody);
        }
        return new byte[0];
    }

    public static byte[] encode(String deviceSourceId, MessageType messageType, ValueMap valueMap, Collection<Field> fields, String version, String data) throws MessageEncoderException {
        byte[] header = convert2Header(messageType, version);
        JsonObject json = JsonUtil.string2JsonObject(data);
        byte[] body = new byte[0];
        for (Field field : fields) {
            JsonElement element = JsonUtil.find(json, field.getName().split("\\."));
            if (element != null) {
                byte[] bytes = convertByField(deviceSourceId, element, field, valueMap, field.getSubfields());
                body = ArrayUtils.addAll(body, bytes);
            }
        }
        return ArrayUtils.addAll(header, body);
    }
}