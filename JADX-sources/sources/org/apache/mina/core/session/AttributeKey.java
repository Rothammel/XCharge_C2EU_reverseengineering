package org.apache.mina.core.session;

import java.io.Serializable;
import org.apache.commons.lang3.ClassUtils;

/* loaded from: classes.dex */
public final class AttributeKey implements Serializable {
    private static final long serialVersionUID = -583377473376683096L;
    private final String name;

    public AttributeKey(Class<?> source, String name) {
        this.name = source.getName() + ClassUtils.PACKAGE_SEPARATOR_CHAR + name + '@' + Integer.toHexString(hashCode());
    }

    public String toString() {
        return this.name;
    }

    public int hashCode() {
        int h = (this.name == null ? 0 : this.name.hashCode()) + 629;
        return h;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AttributeKey)) {
            return false;
        }
        AttributeKey other = (AttributeKey) obj;
        return this.name.equals(other.name);
    }
}
