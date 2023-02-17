package com.xcharge.common.bean;

import android.util.Log;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.reflect.TypeToken;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/* loaded from: classes.dex */
public class JsonBean<T> implements Serializable {
    private static final GsonBuilder gsonBuilder = new GsonBuilder();

    static {
        gsonBuilder.registerTypeAdapter(Long.class, new JsonSerializer<Long>() { // from class: com.xcharge.common.bean.JsonBean.1LongSerializer
            @Override // com.google.gson.JsonSerializer
            public JsonElement serialize(Long src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(String.valueOf(src));
            }
        }).setLongSerializationPolicy(LongSerializationPolicy.STRING);
        gsonBuilder.registerTypeAdapter(Long.class, new JsonDeserializer<Long>() { // from class: com.xcharge.common.bean.JsonBean.1LongDeserializer
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // com.google.gson.JsonDeserializer
            public Long deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return Long.valueOf(Long.parseLong(json.getAsJsonPrimitive().getAsString()));
            }
        }).setLongSerializationPolicy(LongSerializationPolicy.STRING);
        gsonBuilder.disableHtmlEscaping();
    }

    public static GsonBuilder getGsonBuilder() {
        return gsonBuilder;
    }

    public T fromJson(String json) {
        try {
            return (T) gsonBuilder.create().fromJson(json, (Class<Object>) Class.forName(getClass().getName()));
        } catch (Exception e) {
            Log.e("JsonBean.fromJson", "json: " + json + ", exception: " + Log.getStackTraceString(e));
            return null;
        }
    }

    public String toJson() {
        return gsonBuilder.create().toJson(this);
    }

    public T deepClone() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            oos.close();
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            ois.close();
            return (T) ois.readObject();
        } catch (Exception e) {
            Log.e("JsonBean.deepClone", "exception: " + Log.getStackTraceString(e));
            return null;
        }
    }

    public static Object deepClone(Object src) {
        Object o = null;
        if (src == null) {
            return null;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(src);
            oos.close();
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            o = ois.readObject();
            ois.close();
            return o;
        } catch (Exception e) {
            Log.e("JsonBean.deepClone", "exception: " + Log.getStackTraceString(e));
            return o;
        }
    }

    public static String ObjectToJson(Object o) {
        return gsonBuilder.create().toJson(o, new TypeToken<Map>() { // from class: com.xcharge.common.bean.JsonBean.1
        }.getType());
    }

    public static String mapToJson(Map map) {
        return gsonBuilder.create().toJson(map, new TypeToken<Map>() { // from class: com.xcharge.common.bean.JsonBean.2
        }.getType());
    }

    public static Map jsonToMap(String json) {
        try {
            return (Map) gsonBuilder.create().fromJson(json, new TypeToken<Map>() { // from class: com.xcharge.common.bean.JsonBean.3
            }.getType());
        } catch (Exception e) {
            Log.e("JsonBean.jsonToMap", "json: " + json + ", exception: " + Log.getStackTraceString(e));
            return null;
        }
    }

    public static String listToJson(List list) {
        return gsonBuilder.create().toJson(list, new TypeToken<List>() { // from class: com.xcharge.common.bean.JsonBean.4
        }.getType());
    }

    public static List jsonToList(String json) {
        try {
            return (List) gsonBuilder.create().fromJson(json, new TypeToken<List>() { // from class: com.xcharge.common.bean.JsonBean.5
            }.getType());
        } catch (Exception e) {
            Log.e("JsonBean.jsonToList", "json: " + json + ", exception: " + Log.getStackTraceString(e));
            return null;
        }
    }
}