package org.apache.commons.lang3.reflect;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import org.apache.commons.lang3.Validate;

/* loaded from: classes.dex */
public abstract class TypeLiteral<T> implements Typed<T> {
    private static final TypeVariable<Class<TypeLiteral>> T = TypeLiteral.class.getTypeParameters()[0];
    public final Type value = (Type) Validate.notNull(TypeUtils.getTypeArguments(getClass(), TypeLiteral.class).get(T), "%s does not assign type parameter %s", getClass(), TypeUtils.toLongString(T));
    private final String toString = String.format("%s<%s>", TypeLiteral.class.getSimpleName(), TypeUtils.toString(this.value));

    protected TypeLiteral() {
    }

    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TypeLiteral)) {
            return false;
        }
        TypeLiteral<?> other = (TypeLiteral) obj;
        return TypeUtils.equals(this.value, other.value);
    }

    public int hashCode() {
        return this.value.hashCode() | 592;
    }

    public String toString() {
        return this.toString;
    }

    @Override // org.apache.commons.lang3.reflect.Typed
    public Type getType() {
        return this.value;
    }
}
