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

public class JsonBean<T> implements Serializable {
    private static final GsonBuilder gsonBuilder = new GsonBuilder();

    static {
        gsonBuilder.registerTypeAdapter(Long.class, new JsonSerializer<Long>() {
            public JsonElement serialize(Long src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(String.valueOf(src));
            }
        }).setLongSerializationPolicy(LongSerializationPolicy.STRING);
        gsonBuilder.registerTypeAdapter(Long.class, new JsonDeserializer<Long>() {
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
            return gsonBuilder.create().fromJson(json, Class.forName(getClass().getName()));
        } catch (Exception e) {
            Log.e("JsonBean.fromJson", "json: " + json + ", exception: " + Log.getStackTraceString(e));
            return null;
        }
    }

    public String toJson() {
        return gsonBuilder.create().toJson((Object) this);
    }

    public T deepClone() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            oos.close();
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            ois.close();
            return ois.readObject();
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
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            o = ois.readObject();
            ois.close();
            return o;
        } catch (Exception e) {
            Log.e("JsonBean.deepClone", "exception: " + Log.getStackTraceString(e));
            return o;
        }
    }

    public static String ObjectToJson(Object o) {
        return gsonBuilder.create().toJson(o, new TypeToken<Map>() {
        }.getType());
    }

    public static String mapToJson(Map map) {
        return gsonBuilder.create().toJson((Object) map, new TypeToken<Map>() {
        }.getType());
    }

    public static Map jsonToMap(String json) {
        try {
            return (Map) gsonBuilder.create().fromJson(json, new TypeToken<Map>() {
            }.getType());
        } catch (Exception e) {
            Log.e("JsonBean.jsonToMap", "json: " + json + ", exception: " + Log.getStackTraceString(e));
            return null;
        }
    }

    public static String listToJson(List list) {
        return gsonBuilder.create().toJson((Object) list, new TypeToken<List>() {
        }.getType());
    }

    public static List jsonToList(String json) {
        try {
            return (List) gsonBuilder.create().fromJson(json, new TypeToken<List>() {
            }.getType());
        } catch (Exception e) {
            Log.e("JsonBean.jsonToList", "json: " + json + ", exception: " + Log.getStackTraceString(e));
            return null;
        }
    }
}
