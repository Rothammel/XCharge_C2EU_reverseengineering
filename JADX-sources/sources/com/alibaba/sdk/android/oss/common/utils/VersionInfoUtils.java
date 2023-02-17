package com.alibaba.sdk.android.oss.common.utils;

import android.os.Build;
import com.alibaba.sdk.android.oss.common.OSSLog;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* loaded from: classes.dex */
public class VersionInfoUtils {
    private static String userAgent = null;

    public static String getUserAgent(String customInfo) {
        if (OSSUtils.isEmptyString(userAgent)) {
            userAgent = "aliyun-sdk-android/" + getVersion() + getSystemInfo();
        }
        return OSSUtils.isEmptyString(customInfo) ? userAgent : userAgent + MqttTopic.TOPIC_LEVEL_SEPARATOR + customInfo;
    }

    public static String getVersion() {
        return "2.5.0";
    }

    private static String getSystemInfo() {
        StringBuilder customUA = new StringBuilder();
        customUA.append("(");
        customUA.append(System.getProperty("os.name"));
        customUA.append("/Android " + Build.VERSION.RELEASE);
        customUA.append(MqttTopic.TOPIC_LEVEL_SEPARATOR);
        customUA.append(Build.MODEL + ";" + Build.ID);
        customUA.append(")");
        String ua = customUA.toString();
        OSSLog.logDebug("user agent : " + ua);
        if (OSSUtils.isEmptyString(ua)) {
            String propertyUA = System.getProperty("http.agent");
            return propertyUA.replaceAll("[^\\p{ASCII}]", "?");
        }
        return ua;
    }
}
