package org.apache.commons.lang3.text;

import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

/* loaded from: classes.dex */
public abstract class StrLookup<V> {
    private static final StrLookup<String> NONE_LOOKUP = new MapStrLookup(null);

    public abstract String lookup(String str);

    public static StrLookup<?> noneLookup() {
        return NONE_LOOKUP;
    }

    private static Properties copyProperties(Properties input) {
        if (input == null) {
            return null;
        }
        Properties output = new Properties();
        Enumeration<?> propertyNames = input.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String propertyName = (String) propertyNames.nextElement();
            output.setProperty(propertyName, input.getProperty(propertyName));
        }
        return output;
    }

    public static StrLookup<String> systemPropertiesLookup() {
        Properties systemProperties = null;
        try {
            systemProperties = System.getProperties();
        } catch (SecurityException e) {
        }
        Properties properties = copyProperties(systemProperties);
        return new MapStrLookup(properties);
    }

    public static <V> StrLookup<V> mapLookup(Map<String, V> map) {
        return new MapStrLookup(map);
    }

    protected StrLookup() {
    }

    /* loaded from: classes.dex */
    static class MapStrLookup<V> extends StrLookup<V> {
        private final Map<String, V> map;

        MapStrLookup(Map<String, V> map) {
            this.map = map;
        }

        @Override // org.apache.commons.lang3.text.StrLookup
        public String lookup(String key) {
            Object obj;
            if (this.map == null || (obj = this.map.get(key)) == null) {
                return null;
            }
            return obj.toString();
        }
    }
}