package net.xcharger.mqtt.core;

import org.apache.commons.lang3.CharEncoding;

public class Const {
    public static String DEFAULT_ENCODING = CharEncoding.UTF_8;
    public static int Qos = 1;
    public static String VERSION = "v1.0";
    public static boolean binaryMode = false;
    public static String broker = null;
    public static String clientId = null;
    public static String cloud_message_to = "cmf/";
    public static boolean devMode = false;
    public static String device_message_from = "VirtualTopic/dmf/";
    public static String downTopic = null;
    public static String getAddrUrl = "";
    public static int keepAlive = 60;
    public static int maxMemorySize = 0;
    public static String password = null;
    public static boolean ssl = false;
    public static int timeOut = 40;
    public static String upTopic = null;
    public static String userName = null;
}
