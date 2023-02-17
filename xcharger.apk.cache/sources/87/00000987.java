package net.xcharger.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/* loaded from: classes.dex */
public class GetAddr {
    private static String param;
    private static String url;
    private static String result = null;
    private static final Class<?> cclass = GetAddr.class;
    private static final String className = cclass.getName();
    private static Logger logger = Logger.getLogger(className);

    public static String getConfUrl(String url2, String param2, String host) {
        try {
            result = HttpRequest.sendPost(url2, param2, host);
        } catch (Exception e) {
            result = "";
            logger.log(Level.WARNING, "addr  处理失败：" + e.getMessage());
        }
        return result;
    }
}