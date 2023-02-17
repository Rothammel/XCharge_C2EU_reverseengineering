package org.apache.commons.lang3.builder;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

public class EqualsBuilder implements Builder<Boolean> {
    private static final ThreadLocal<Set<Pair<IDKey, IDKey>>> REGISTRY = new ThreadLocal<>();
    private boolean isEquals = true;

    static Set<Pair<IDKey, IDKey>> getRegistry() {
        return REGISTRY.get();
    }

    static Pair<IDKey, IDKey> getRegisterPair(Object lhs, Object rhs) {
        return Pair.m53of(new IDKey(lhs), new IDKey(rhs));
    }

    static boolean isRegistered(Object lhs, Object rhs) {
        Set<Pair<IDKey, IDKey>> registry = getRegistry();
        Pair<IDKey, IDKey> pair = getRegisterPair(lhs, rhs);
        return registry != null && (registry.contains(pair) || registry.contains(Pair.m53of(pair.getLeft(), pair.getRight())));
    }

    static void register(Object lhs, Object rhs) {
        synchronized (EqualsBuilder.class) {
            if (getRegistry() == null) {
                REGISTRY.set(new HashSet());
            }
        }
        getRegistry().add(getRegisterPair(lhs, rhs));
    }

    static void unregister(Object lhs, Object rhs) {
        Set<Pair<IDKey, IDKey>> registry = getRegistry();
        if (registry != null) {
            registry.remove(getRegisterPair(lhs, rhs));
            synchronized (EqualsBuilder.class) {
                Set<Pair<IDKey, IDKey>> registry2 = getRegistry();
                if (registry2 != null && registry2.isEmpty()) {
                    REGISTRY.remove();
                }
            }
        }
    }

    public static boolean reflectionEquals(Object lhs, Object rhs, Collection<String> excludeFields) {
        return reflectionEquals(lhs, rhs, ReflectionToStringBuilder.toNoNullStringArray(excludeFields));
    }

    public static boolean reflectionEquals(Object lhs, Object rhs, String... excludeFields) {
        return reflectionEquals(lhs, rhs, false, (Class<?>) null, excludeFields);
    }

    public static boolean reflectionEquals(Object lhs, Object rhs, boolean testTransients) {
        return reflectionEquals(lhs, rhs, testTransients, (Class<?>) null, new String[0]);
    }

    public static boolean reflectionEquals(Object lhs, Object rhs, boolean testTransients, Class<?> reflectUpToClass, String... excludeFields) {
        Class<?> testClass;
        if (lhs == rhs) {
            return true;
        }
        if (lhs == null || rhs == null) {
            return false;
        }
        Class<?> lhsClass = lhs.getClass();
        Class<?> rhsClass = rhs.getClass();
        if (lhsClass.isInstance(rhs)) {
            testClass = lhsClass;
            if (!rhsClass.isInstance(lhs)) {
                testClass = rhsClass;
            }
        } else if (!rhsClass.isInstance(lhs)) {
            return false;
        } else {
            testClass = rhsClass;
            if (!lhsClass.isInstance(rhs)) {
                testClass = lhsClass;
            }
        }
        EqualsBuilder equalsBuilder = new EqualsBuilder();
        try {
            if (testClass.isArray()) {
                equalsBuilder.append(lhs, rhs);
            } else {
                reflectionAppend(lhs, rhs, testClass, equalsBuilder, testTransients, excludeFields);
                while (testClass.getSuperclass() != null && testClass != reflectUpToClass) {
                    testClass = testClass.getSuperclass();
                    reflectionAppend(lhs, rhs, testClass, equalsBuilder, testTransients, excludeFields);
                }
            }
            return equalsBuilder.isEquals();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static void reflectionAppend(Object lhs, Object rhs, Class<?> clazz, EqualsBuilder builder, boolean useTransients, String[] excludeFields) {
        if (!isRegistered(lhs, rhs)) {
            try {
                register(lhs, rhs);
                Field[] fields = clazz.getDeclaredFields();
                AccessibleObject.setAccessible(fields, true);
                for (int i = 0; i < fields.length && builder.isEquals; i++) {
                    Field f = fields[i];
                    if (!ArrayUtils.contains((Object[]) excludeFields, (Object) f.getName()) && f.getName().indexOf(36) == -1 && ((useTransients || !Modifier.isTransient(f.getModifiers())) && !Modifier.isStatic(f.getModifiers()))) {
                        builder.append(f.get(lhs), f.get(rhs));
                    }
                }
                unregister(lhs, rhs);
            } catch (IllegalAccessException e) {
                throw new InternalError("Unexpected IllegalAccessException");
            } catch (Throwable th) {
                unregister(lhs, rhs);
                throw th;
            }
        }
    }

    public EqualsBuilder appendSuper(boolean superEquals) {
        if (this.isEquals) {
            this.isEquals = superEquals;
        }
        return this;
    }

    public EqualsBuilder append(Object lhs, Object rhs) {
        if (this.isEquals && lhs != rhs) {
            if (lhs == null || rhs == null) {
                setEquals(false);
            } else if (!lhs.getClass().isArray()) {
                this.isEquals = lhs.equals(rhs);
            } else if (lhs.getClass() != rhs.getClass()) {
                setEquals(false);
            } else if (lhs instanceof long[]) {
                append((long[]) (long[]) lhs, (long[]) (long[]) rhs);
            } else if (lhs instanceof int[]) {
                append((int[]) (int[]) lhs, (int[]) (int[]) rhs);
            } else if (lhs instanceof short[]) {
                append((short[]) (short[]) lhs, (short[]) (short[]) rhs);
            } else if (lhs instanceof char[]) {
                append((char[]) (char[]) lhs, (char[]) (char[]) rhs);
            } else if (lhs instanceof byte[]) {
                append((byte[]) (byte[]) lhs, (byte[]) (byte[]) rhs);
            } else if (lhs instanceof double[]) {
                append((double[]) (double[]) lhs, (double[]) (double[]) rhs);
            } else if (lhs instanceof float[]) {
                append((float[]) (float[]) lhs, (float[]) (float[]) rhs);
            } else if (lhs instanceof boolean[]) {
                append((boolean[]) (boolean[]) lhs, (boolean[]) (boolean[]) rhs);
            } else {
                append((Object[]) (Object[]) lhs, (Object[]) (Object[]) rhs);
            }
        }
        return this;
    }

    public EqualsBuilder append(long lhs, long rhs) {
        if (this.isEquals) {
            this.isEquals = lhs == rhs;
        }
        return this;
    }

    public EqualsBuilder append(int lhs, int rhs) {
        if (this.isEquals) {
            this.isEquals = lhs == rhs;
        }
        return this;
    }

    public EqualsBuilder append(short lhs, short rhs) {
        if (this.isEquals) {
            this.isEquals = lhs == rhs;
        }
        return this;
    }

    public EqualsBuilder append(char lhs, char rhs) {
        if (this.isEquals) {
            this.isEquals = lhs == rhs;
        }
        return this;
    }

    public EqualsBuilder append(byte lhs, byte rhs) {
        if (this.isEquals) {
            this.isEquals = lhs == rhs;
        }
        return this;
    }

    /* Debug info: failed to restart local var, previous not found, register: 5 */
    public EqualsBuilder append(double lhs, double rhs) {
        return !this.isEquals ? this : append(Double.doubleToLongBits(lhs), Double.doubleToLongBits(rhs));
    }

    /* Debug info: failed to restart local var, previous not found, register: 2 */
    public EqualsBuilder append(float lhs, float rhs) {
        return !this.isEquals ? this : append(Float.floatToIntBits(lhs), Float.floatToIntBits(rhs));
    }

    public EqualsBuilder append(boolean lhs, boolean rhs) {
        if (this.isEquals) {
            this.isEquals = lhs == rhs;
        }
        return this;
    }

    public EqualsBuilder append(Object[] lhs, Object[] rhs) {
        if (this.isEquals && lhs != rhs) {
            if (lhs == null || rhs == null) {
                setEquals(false);
            } else if (lhs.length != rhs.length) {
                setEquals(false);
            } else {
                for (int i = 0; i < lhs.length && this.isEquals; i++) {
                    append(lhs[i], rhs[i]);
                }
            }
        }
        return this;
    }

    public EqualsBuilder append(long[] lhs, long[] rhs) {
        if (this.isEquals && lhs != rhs) {
            if (lhs == null || rhs == null) {
                setEquals(false);
            } else if (lhs.length != rhs.length) {
                setEquals(false);
            } else {
                for (int i = 0; i < lhs.length && this.isEquals; i++) {
                    append(lhs[i], rhs[i]);
                }
            }
        }
        return this;
    }

    public EqualsBuilder append(int[] lhs, int[] rhs) {
        if (this.isEquals && lhs != rhs) {
            if (lhs == null || rhs == null) {
                setEquals(false);
            } else if (lhs.length != rhs.length) {
                setEquals(false);
            } else {
                for (int i = 0; i < lhs.length && this.isEquals; i++) {
                    append(lhs[i], rhs[i]);
                }
            }
        }
        return this;
    }

    public EqualsBuilder append(short[] lhs, short[] rhs) {
        if (this.isEquals && lhs != rhs) {
            if (lhs == null || rhs == null) {
                setEquals(false);
            } else if (lhs.length != rhs.length) {
                setEquals(false);
            } else {
                for (int i = 0; i < lhs.length && this.isEquals; i++) {
                    append(lhs[i], rhs[i]);
                }
            }
        }
        return this;
    }

    public EqualsBuilder append(char[] lhs, char[] rhs) {
        if (this.isEquals && lhs != rhs) {
            if (lhs == null || rhs == null) {
                setEquals(false);
            } else if (lhs.length != rhs.length) {
                setEquals(false);
            } else {
                for (int i = 0; i < lhs.length && this.isEquals; i++) {
                    append(lhs[i], rhs[i]);
                }
            }
        }
        return this;
    }

    public EqualsBuilder append(byte[] lhs, byte[] rhs) {
        if (this.isEquals && lhs != rhs) {
            if (lhs == null || rhs == null) {
                setEquals(false);
            } else if (lhs.length != rhs.length) {
                setEquals(false);
            } else {
                for (int i = 0; i < lhs.length && this.isEquals; i++) {
                    append(lhs[i], rhs[i]);
                }
            }
        }
        return this;
    }

    public EqualsBuilder append(double[] lhs, double[] rhs) {
        if (this.isEquals && lhs != rhs) {
            if (lhs == null || rhs == null) {
                setEquals(false);
            } else if (lhs.length != rhs.length) {
                setEquals(false);
            } else {
                for (int i = 0; i < lhs.length && this.isEquals; i++) {
                    append(lhs[i], rhs[i]);
                }
            }
        }
        return this;
    }

    public EqualsBuilder append(float[] lhs, float[] rhs) {
        if (this.isEquals && lhs != rhs) {
            if (lhs == null || rhs == null) {
                setEquals(false);
            } else if (lhs.length != rhs.length) {
                setEquals(false);
            } else {
                for (int i = 0; i < lhs.length && this.isEquals; i++) {
                    append(lhs[i], rhs[i]);
                }
            }
        }
        return this;
    }

    public EqualsBuilder append(boolean[] lhs, boolean[] rhs) {
        if (this.isEquals && lhs != rhs) {
            if (lhs == null || rhs == null) {
                setEquals(false);
            } else if (lhs.length != rhs.length) {
                setEquals(false);
            } else {
                for (int i = 0; i < lhs.length && this.isEquals; i++) {
                    append(lhs[i], rhs[i]);
                }
            }
        }
        return this;
    }

    public boolean isEquals() {
        return this.isEquals;
    }

    public Boolean build() {
        return Boolean.valueOf(isEquals());
    }

    /* access modifiers changed from: protected */
    public void setEquals(boolean isEquals2) {
        this.isEquals = isEquals2;
    }

    public void reset() {
        this.isEquals = true;
    }
}
