package org.apache.commons.lang3.builder;

final class IDKey {

    /* renamed from: id */
    private final int f147id;
    private final Object value;

    public IDKey(Object _value) {
        this.f147id = System.identityHashCode(_value);
        this.value = _value;
    }

    public int hashCode() {
        return this.f147id;
    }

    public boolean equals(Object other) {
        if (!(other instanceof IDKey)) {
            return false;
        }
        IDKey idKey = (IDKey) other;
        if (this.f147id == idKey.f147id && this.value == idKey.value) {
            return true;
        }
        return false;
    }
}
