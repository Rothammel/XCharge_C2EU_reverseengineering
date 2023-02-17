package org.apache.commons.lang3.text;

import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

public abstract class StrLookup<V> {
    private static final StrLookup<String> NONE_LOOKUP = new MapStrLookup((Map) null);

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
        return new MapStrLookup(copyProperties(systemProperties));
    }

    public static <V> StrLookup<V> mapLookup(Map<String, V> map) {
        return new MapStrLookup(map);
    }

    protected StrLookup() {
    }

    static class MapStrLookup<V> extends StrLookup<V> {
        private final Map<String, V> map;

        MapStrLookup(Map<String, V> map2) {
            this.map = map2;
        }

        public String lookup(String key) {
            Object obj;
            if (this.map == null || (obj = this.map.get(key)) == null) {
                return null;
            }
            return obj.toString();
        }
    }
}
