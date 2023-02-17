package net.xcharge.sdk.server.coder.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public class JsonUtil {
    private static Logger logger = LoggerFactory.getLogger(JsonUtil.class);
    public static Gson GSON = new GsonBuilder().registerTypeAdapter(Double.class, new JsonSerializer<Double>() { // from class: net.xcharge.sdk.server.coder.utils.JsonUtil.1
        @Override // com.google.gson.JsonSerializer
        public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
            return src.doubleValue() == ((double) src.longValue()) ? new JsonPrimitive((Number) Long.valueOf(src.longValue())) : new JsonPrimitive((Number) src);
        }
    }).create();

    public static JsonObject string2JsonObject(String jsonString) {
        return (JsonObject) GSON.fromJson(jsonString, (Class<Object>) JsonObject.class);
    }

    public static boolean isJson(String jsonString) {
        try {
            return GSON.fromJson(jsonString, (Class<Object>) JsonObject.class) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static JsonElement find(JsonObject json, String... keys) {
        JsonElement element;
        JsonElement element2 = json;
        int length = keys.length;
        int i = 0;
        while (i < length) {
            String key = keys[i];
            if (element2 != null) {
                if (element2.isJsonObject() && element2.getAsJsonObject().has(key)) {
                    element = element2.getAsJsonObject().get(key);
                } else if (element2.isJsonArray() && StringUtils.isNumeric(key) && element2.getAsJsonArray().size() > NumberUtils.toInt(key)) {
                    element = element2.getAsJsonArray().get(NumberUtils.toInt(key));
                }
                i++;
                element2 = element;
            }
            return null;
        }
        return element2;
    }

    public static void clearJsonArray(JsonArray array) {
        int size = array.size();
        for (int i = 0; i < size; i++) {
            array.remove(0);
        }
    }

    public static void listAdd2jsonArray(List<JsonElement> list, JsonArray array) {
        for (JsonElement o : list) {
            array.add(o);
        }
    }

    public static void append(JsonObject json, JsonElement element, String... keys) {
        append(json, element, 0, keys);
    }

    public static void append(JsonObject json, JsonElement element, int size, String... keys) {
        if (keys.length == 1) {
            json.add(keys[0], element);
        } else if (keys.length == 2 && keys[0].equals("data") && StringUtils.isNumeric(keys[1])) {
            if (!json.has(keys[0])) {
                List list = new ArrayList();
                for (int i = 0; i < size; i++) {
                    list.add(0);
                }
                json.add(keys[0], GSON.toJsonTree(list));
            }
            JsonArray a = json.get(keys[0]).getAsJsonArray();
            Iterator iterator = json.get(keys[0]).getAsJsonArray().iterator();
            List list2 = ListUtil.copyIterator(iterator);
            list2.remove(NumberUtils.toInt(keys[1]));
            list2.add(NumberUtils.toInt(keys[1]), element);
            clearJsonArray(a);
            listAdd2jsonArray(list2, a);
        } else {
            JsonObject j = json;
            for (int i2 = 0; i2 < keys.length - 1; i2++) {
                if (j.get(keys[i2]) == null || !json.get(keys[i2]).isJsonObject()) {
                    JsonObject e = new JsonObject();
                    j.add(keys[i2], e);
                    j = e;
                } else {
                    j = j.get(keys[i2]).getAsJsonObject();
                }
            }
            j.add(keys[keys.length - 1], element);
        }
    }
}
