package org.apache.mina.core.session;

import java.io.Serializable;
import org.apache.commons.lang3.ClassUtils;

public final class AttributeKey implements Serializable {
    private static final long serialVersionUID = -583377473376683096L;
    private final String name;

    public AttributeKey(Class<?> source, String name2) {
        this.name = source.getName() + ClassUtils.PACKAGE_SEPARATOR_CHAR + name2 + '@' + Integer.toHexString(hashCode());
    }

    public String toString() {
        return this.name;
    }

    public int hashCode() {
        return (this.name == null ? 0 : this.name.hashCode()) + 629;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AttributeKey)) {
            return false;
        }
        return this.name.equals(((AttributeKey) obj).name);
    }
}
