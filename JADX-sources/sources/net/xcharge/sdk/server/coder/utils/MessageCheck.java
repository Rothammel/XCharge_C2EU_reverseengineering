package net.xcharge.sdk.server.coder.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.xcharge.sdk.server.coder.constant.GlobalConstant;
import org.apache.commons.lang3.StringUtils;

/* loaded from: classes.dex */
public class MessageCheck {
    public static String check(String message) {
        JsonObject json = JsonUtil.string2JsonObject(message);
        if (json != null && json.has(GlobalConstant.cause)) {
            JsonElement c = json.get(GlobalConstant.cause);
            if (c.isJsonObject()) {
                String code = null;
                JsonObject cause = c.getAsJsonObject();
                if (cause.has(GlobalConstant.code) && StringUtils.endsWithIgnoreCase("ERROR", cause.get(GlobalConstant.code).getAsString())) {
                    if (cause.has(GlobalConstant.data) && cause.get(GlobalConstant.data).isJsonObject()) {
                        JsonObject data = cause.getAsJsonObject(GlobalConstant.data);
                        if (data.has(GlobalConstant.code)) {
                            code = data.get(GlobalConstant.code).getAsString();
                        }
                    }
                    if (StringUtils.isNoneBlank(code)) {
                        cause.addProperty(GlobalConstant.data, code);
                        return json.toString();
                    }
                    return message;
                }
                return message;
            }
            return message;
        }
        return message;
    }
}
