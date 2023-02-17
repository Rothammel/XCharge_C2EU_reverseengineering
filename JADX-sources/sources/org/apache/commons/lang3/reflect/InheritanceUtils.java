package org.apache.commons.lang3.reflect;

import org.apache.commons.lang3.BooleanUtils;

/* loaded from: classes.dex */
public class InheritanceUtils {
    public static int distance(Class<?> child, Class<?> parent) {
        if (child == null || parent == null) {
            return -1;
        }
        if (child.equals(parent)) {
            return 0;
        }
        Class<?> cParent = child.getSuperclass();
        int d = BooleanUtils.toInteger(parent.equals(cParent));
        if (d == 1) {
            return d;
        }
        int d2 = d + distance(cParent, parent);
        if (d2 > 0) {
            return d2 + 1;
        }
        return -1;
    }
}
