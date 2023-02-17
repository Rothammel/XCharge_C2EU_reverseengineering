package org.apache.commons.lang3;

/* loaded from: classes.dex */
public enum JavaVersion {
    JAVA_0_9(1.5f, "0.9"),
    JAVA_1_1(1.1f, "1.1"),
    JAVA_1_2(1.2f, "1.2"),
    JAVA_1_3(1.3f, "1.3"),
    JAVA_1_4(1.4f, "1.4"),
    JAVA_1_5(1.5f, "1.5"),
    JAVA_1_6(1.6f, "1.6"),
    JAVA_1_7(1.7f, "1.7"),
    JAVA_1_8(1.8f, "1.8"),
    JAVA_1_9(1.9f, "1.9"),
    JAVA_RECENT(maxVersion(), Float.toString(maxVersion()));
    
    private final String name;
    private final float value;

    JavaVersion(float value, String name) {
        this.value = value;
        this.name = name;
    }

    public boolean atLeast(JavaVersion requiredVersion) {
        return this.value >= requiredVersion.value;
    }

    static JavaVersion getJavaVersion(String nom) {
        return get(nom);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static JavaVersion get(String nom) {
        if ("0.9".equals(nom)) {
            return JAVA_0_9;
        }
        if ("1.1".equals(nom)) {
            return JAVA_1_1;
        }
        if ("1.2".equals(nom)) {
            return JAVA_1_2;
        }
        if ("1.3".equals(nom)) {
            return JAVA_1_3;
        }
        if ("1.4".equals(nom)) {
            return JAVA_1_4;
        }
        if ("1.5".equals(nom)) {
            return JAVA_1_5;
        }
        if ("1.6".equals(nom)) {
            return JAVA_1_6;
        }
        if ("1.7".equals(nom)) {
            return JAVA_1_7;
        }
        if ("1.8".equals(nom)) {
            return JAVA_1_8;
        }
        if ("1.9".equals(nom)) {
            return JAVA_1_9;
        }
        if (nom != null) {
            float v = toFloatVersion(nom);
            if (v - 1.0d < 1.0d) {
                int firstComma = Math.max(nom.indexOf(46), nom.indexOf(44));
                int end = Math.max(nom.length(), nom.indexOf(44, firstComma));
                if (Float.parseFloat(nom.substring(firstComma + 1, end)) > 0.9f) {
                    return JAVA_RECENT;
                }
                return null;
            }
            return null;
        }
        return null;
    }

    @Override // java.lang.Enum
    public String toString() {
        return this.name;
    }

    private static float maxVersion() {
        float v = toFloatVersion(System.getProperty("java.version", "2.0"));
        if (v > 0.0f) {
            return v;
        }
        return 2.0f;
    }

    private static float toFloatVersion(String value) {
        String[] toParse = value.split("\\.");
        if (toParse.length >= 2) {
            try {
                return Float.parseFloat(toParse[0] + ClassUtils.PACKAGE_SEPARATOR_CHAR + toParse[1]);
            } catch (NumberFormatException e) {
            }
        }
        return -1.0f;
    }
}
