package org.apache.commons.lang3.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

/* loaded from: classes.dex */
public class DiffBuilder implements Builder<DiffResult> {
    private final List<Diff<?>> diffs;
    private final Object left;
    private final boolean objectsTriviallyEqual;
    private final Object right;
    private final ToStringStyle style;

    public DiffBuilder(Object lhs, Object rhs, ToStringStyle style, boolean testTriviallyEqual) {
        if (lhs == null) {
            throw new IllegalArgumentException("lhs cannot be null");
        }
        if (rhs == null) {
            throw new IllegalArgumentException("rhs cannot be null");
        }
        this.diffs = new ArrayList();
        this.left = lhs;
        this.right = rhs;
        this.style = style;
        this.objectsTriviallyEqual = testTriviallyEqual && (lhs == rhs || lhs.equals(rhs));
    }

    public DiffBuilder(Object lhs, Object rhs, ToStringStyle style) {
        this(lhs, rhs, style, true);
    }

    public DiffBuilder append(String fieldName, final boolean lhs, final boolean rhs) {
        if (fieldName == null) {
            throw new IllegalArgumentException("Field name cannot be null");
        }
        if (!this.objectsTriviallyEqual && lhs != rhs) {
            this.diffs.add(new Diff<Boolean>(fieldName) { // from class: org.apache.commons.lang3.builder.DiffBuilder.1
                private static final long serialVersionUID = 1;

                @Override // org.apache.commons.lang3.tuple.Pair
                public Boolean getLeft() {
                    return Boolean.valueOf(lhs);
                }

                @Override // org.apache.commons.lang3.tuple.Pair
                public Boolean getRight() {
                    return Boolean.valueOf(rhs);
                }
            });
        }
        return this;
    }

    public DiffBuilder append(String fieldName, final boolean[] lhs, final boolean[] rhs) {
        if (fieldName == null) {
            throw new IllegalArgumentException("Field name cannot be null");
        }
        if (!this.objectsTriviallyEqual && !Arrays.equals(lhs, rhs)) {
            this.diffs.add(new Diff<Boolean[]>(fieldName) { // from class: org.apache.commons.lang3.builder.DiffBuilder.2
                private static final long serialVersionUID = 1;

                @Override // org.apache.commons.lang3.tuple.Pair
                public Boolean[] getLeft() {
                    return ArrayUtils.toObject(lhs);
                }

                @Override // org.apache.commons.lang3.tuple.Pair
                public Boolean[] getRight() {
                    return ArrayUtils.toObject(rhs);
                }
            });
        }
        return this;
    }

    public DiffBuilder append(String fieldName, final byte lhs, final byte rhs) {
        if (fieldName == null) {
            throw new IllegalArgumentException("Field name cannot be null");
        }
        if (!this.objectsTriviallyEqual && lhs != rhs) {
            this.diffs.add(new Diff<Byte>(fieldName) { // from class: org.apache.commons.lang3.builder.DiffBuilder.3
                private static final long serialVersionUID = 1;

                @Override // org.apache.commons.lang3.tuple.Pair
                public Byte getLeft() {
                    return Byte.valueOf(lhs);
                }

                @Override // org.apache.commons.lang3.tuple.Pair
                public Byte getRight() {
                    return Byte.valueOf(rhs);
                }
            });
        }
        return this;
    }

    public DiffBuilder append(String fieldName, final byte[] lhs, final byte[] rhs) {
        if (fieldName == null) {
            throw new IllegalArgumentException("Field name cannot be null");
        }
        if (!this.objectsTriviallyEqual && !Arrays.equals(lhs, rhs)) {
            this.diffs.add(new Diff<Byte[]>(fieldName) { // from class: org.apache.commons.lang3.builder.DiffBuilder.4
                private static final long serialVersionUID = 1;

                @Override // org.apache.commons.lang3.tuple.Pair
                public Byte[] getLeft() {
                    return ArrayUtils.toObject(lhs);
                }

                @Override // org.apache.commons.lang3.tuple.Pair
                public Byte[] getRight() {
                    return ArrayUtils.toObject(rhs);
                }
            });
        }
        return this;
    }

    public DiffBuilder append(String fieldName, final char lhs, final char rhs) {
        if (fieldName == null) {
            throw new IllegalArgumentException("Field name cannot be null");
        }
        if (!this.objectsTriviallyEqual && lhs != rhs) {
            this.diffs.add(new Diff<Character>(fieldName) { // from class: org.apache.commons.lang3.builder.DiffBuilder.5
                private static final long serialVersionUID = 1;

                @Override // org.apache.commons.lang3.tuple.Pair
                public Character getLeft() {
                    return Character.valueOf(lhs);
                }

                @Override // org.apache.commons.lang3.tuple.Pair
                public Character getRight() {
                    return Character.valueOf(rhs);
                }
            });
        }
        return this;
    }

    public DiffBuilder append(String fieldName, final char[] lhs, final char[] rhs) {
        if (fieldName == null) {
            throw new IllegalArgumentException("Field name cannot be null");
        }
        if (!this.objectsTriviallyEqual && !Arrays.equals(lhs, rhs)) {
            this.diffs.add(new Diff<Character[]>(fieldName) { // from class: org.apache.commons.lang3.builder.DiffBuilder.6
                private static final long serialVersionUID = 1;

                @Override // org.apache.commons.lang3.tuple.Pair
                public Character[] getLeft() {
                    return ArrayUtils.toObject(lhs);
                }

                @Override // org.apache.commons.lang3.tuple.Pair
                public Character[] getRight() {
                    return ArrayUtils.toObject(rhs);
                }
            });
        }
        return this;
    }

    public DiffBuilder append(String fieldName, final double lhs, final double rhs) {
        if (fieldName == null) {
            throw new IllegalArgumentException("Field name cannot be null");
        }
        if (!this.objectsTriviallyEqual && Double.doubleToLongBits(lhs) != Double.doubleToLongBits(rhs)) {
            this.diffs.add(new Diff<Double>(fieldName) { // from class: org.apache.commons.lang3.builder.DiffBuilder.7
                private static final long serialVersionUID = 1;

                @Override // org.apache.commons.lang3.tuple.Pair
                public Double getLeft() {
                    return Double.valueOf(lhs);
                }

                @Override // org.apache.commons.lang3.tuple.Pair
                public Double getRight() {
                    return Double.valueOf(rhs);
                }
            });
        }
        return this;
    }

    public DiffBuilder append(String fieldName, final double[] lhs, final double[] rhs) {
        if (fieldName == null) {
            throw new IllegalArgumentException("Field name cannot be null");
        }
        if (!this.objectsTriviallyEqual && !Arrays.equals(lhs, rhs)) {
            this.diffs.add(new Diff<Double[]>(fieldName) { // from class: org.apache.commons.lang3.builder.DiffBuilder.8
                private static final long serialVersionUID = 1;

                @Override // org.apache.commons.lang3.tuple.Pair
                public Double[] getLeft() {
                    return ArrayUtils.toObject(lhs);
                }

                @Override // org.apache.commons.lang3.tuple.Pair
                public Double[] getRight() {
                    return ArrayUtils.toObject(rhs);
                }
            });
        }
        return this;
    }

    public DiffBuilder append(String fieldName, final float lhs, final float rhs) {
        if (fieldName == null) {
            throw new IllegalArgumentException("Field name cannot be null");
        }
        if (!this.objectsTriviallyEqual && Float.floatToIntBits(lhs) != Float.floatToIntBits(rhs)) {
            this.diffs.add(new Diff<Float>(fieldName) { // from class: org.apache.commons.lang3.builder.DiffBuilder.9
                private static final long serialVersionUID = 1;

                @Override // org.apache.commons.lang3.tuple.Pair
                public Float getLeft() {
                    return Float.valueOf(lhs);
                }

                @Override // org.apache.commons.lang3.tuple.Pair
                public Float getRight() {
                    return Float.valueOf(rhs);
                }
            });
        }
        return this;
    }

    public DiffBuilder append(String fieldName, final float[] lhs, final float[] rhs) {
        if (fieldName == null) {
            throw new IllegalArgumentException("Field name cannot be null");
        }
        if (!this.objectsTriviallyEqual && !Arrays.equals(lhs, rhs)) {
            this.diffs.add(new Diff<Float[]>(fieldName) { // from class: org.apache.commons.lang3.builder.DiffBuilder.10
                private static final long serialVersionUID = 1;

                @Override // org.apache.commons.lang3.tuple.Pair
                public Float[] getLeft() {
                    return ArrayUtils.toObject(lhs);
                }

                @Override // org.apache.commons.lang3.tuple.Pair
                public Float[] getRight() {
                    return ArrayUtils.toObject(rhs);
                }
            });
        }
        return this;
    }

    public DiffBuilder append(String fieldName, final int lhs, final int rhs) {
        if (fieldName == null) {
            throw new IllegalArgumentException("Field name cannot be null");
        }
        if (!this.objectsTriviallyEqual && lhs != rhs) {
            this.diffs.add(new Diff<Integer>(fieldName) { // from class: org.apache.commons.lang3.builder.DiffBuilder.11
                private static final long serialVersionUID = 1;

                @Override // org.apache.commons.lang3.tuple.Pair
                public Integer getLeft() {
                    return Integer.valueOf(lhs);
                }

                @Override // org.apache.commons.lang3.tuple.Pair
                public Integer getRight() {
                    return Integer.valueOf(rhs);
                }
            });
        }
        return this;
    }

    public DiffBuilder append(String fieldName, final int[] lhs, final int[] rhs) {
        if (fieldName == null) {
            throw new IllegalArgumentException("Field name cannot be null");
        }
        if (!this.objectsTriviallyEqual && !Arrays.equals(lhs, rhs)) {
            this.diffs.add(new Diff<Integer[]>(fieldName) { // from class: org.apache.commons.lang3.builder.DiffBuilder.12
                private static final long serialVersionUID = 1;

                @Override // org.apache.commons.lang3.tuple.Pair
                public Integer[] getLeft() {
                    return ArrayUtils.toObject(lhs);
                }

                @Override // org.apache.commons.lang3.tuple.Pair
                public Integer[] getRight() {
                    return ArrayUtils.toObject(rhs);
                }
            });
        }
        return this;
    }

    public DiffBuilder append(String fieldName, final long lhs, final long rhs) {
        if (fieldName == null) {
            throw new IllegalArgumentException("Field name cannot be null");
        }
        if (!this.objectsTriviallyEqual && lhs != rhs) {
            this.diffs.add(new Diff<Long>(fieldName) { // from class: org.apache.commons.lang3.builder.DiffBuilder.13
                private static final long serialVersionUID = 1;

                @Override // org.apache.commons.lang3.tuple.Pair
                public Long getLeft() {
                    return Long.valueOf(lhs);
                }

                @Override // org.apache.commons.lang3.tuple.Pair
                public Long getRight() {
                    return Long.valueOf(rhs);
                }
            });
        }
        return this;
    }

    public DiffBuilder append(String fieldName, final long[] lhs, final long[] rhs) {
        if (fieldName == null) {
            throw new IllegalArgumentException("Field name cannot be null");
        }
        if (!this.objectsTriviallyEqual && !Arrays.equals(lhs, rhs)) {
            this.diffs.add(new Diff<Long[]>(fieldName) { // from class: org.apache.commons.lang3.builder.DiffBuilder.14
                private static final long serialVersionUID = 1;

                @Override // org.apache.commons.lang3.tuple.Pair
                public Long[] getLeft() {
                    return ArrayUtils.toObject(lhs);
                }

                @Override // org.apache.commons.lang3.tuple.Pair
                public Long[] getRight() {
                    return ArrayUtils.toObject(rhs);
                }
            });
        }
        return this;
    }

    public DiffBuilder append(String fieldName, final short lhs, final short rhs) {
        if (fieldName == null) {
            throw new IllegalArgumentException("Field name cannot be null");
        }
        if (!this.objectsTriviallyEqual && lhs != rhs) {
            this.diffs.add(new Diff<Short>(fieldName) { // from class: org.apache.commons.lang3.builder.DiffBuilder.15
                private static final long serialVersionUID = 1;

                @Override // org.apache.commons.lang3.tuple.Pair
                public Short getLeft() {
                    return Short.valueOf(lhs);
                }

                @Override // org.apache.commons.lang3.tuple.Pair
                public Short getRight() {
                    return Short.valueOf(rhs);
                }
            });
        }
        return this;
    }

    public DiffBuilder append(String fieldName, final short[] lhs, final short[] rhs) {
        if (fieldName == null) {
            throw new IllegalArgumentException("Field name cannot be null");
        }
        if (!this.objectsTriviallyEqual && !Arrays.equals(lhs, rhs)) {
            this.diffs.add(new Diff<Short[]>(fieldName) { // from class: org.apache.commons.lang3.builder.DiffBuilder.16
                private static final long serialVersionUID = 1;

                @Override // org.apache.commons.lang3.tuple.Pair
                public Short[] getLeft() {
                    return ArrayUtils.toObject(lhs);
                }

                @Override // org.apache.commons.lang3.tuple.Pair
                public Short[] getRight() {
                    return ArrayUtils.toObject(rhs);
                }
            });
        }
        return this;
    }

    public DiffBuilder append(String fieldName, final Object lhs, final Object rhs) {
        Object objectToTest;
        if (!this.objectsTriviallyEqual && lhs != rhs) {
            if (lhs != null) {
                objectToTest = lhs;
            } else {
                objectToTest = rhs;
            }
            if (objectToTest.getClass().isArray()) {
                if (objectToTest instanceof boolean[]) {
                    return append(fieldName, (boolean[]) lhs, (boolean[]) rhs);
                }
                if (objectToTest instanceof byte[]) {
                    return append(fieldName, (byte[]) lhs, (byte[]) rhs);
                }
                if (objectToTest instanceof char[]) {
                    return append(fieldName, (char[]) lhs, (char[]) rhs);
                }
                if (objectToTest instanceof double[]) {
                    return append(fieldName, (double[]) lhs, (double[]) rhs);
                }
                if (objectToTest instanceof float[]) {
                    return append(fieldName, (float[]) lhs, (float[]) rhs);
                }
                if (objectToTest instanceof int[]) {
                    return append(fieldName, (int[]) lhs, (int[]) rhs);
                }
                if (objectToTest instanceof long[]) {
                    return append(fieldName, (long[]) lhs, (long[]) rhs);
                }
                if (objectToTest instanceof short[]) {
                    return append(fieldName, (short[]) lhs, (short[]) rhs);
                }
                return append(fieldName, (Object[]) lhs, (Object[]) rhs);
            } else if (lhs == null || !lhs.equals(rhs)) {
                this.diffs.add(new Diff<Object>(fieldName) { // from class: org.apache.commons.lang3.builder.DiffBuilder.17
                    private static final long serialVersionUID = 1;

                    @Override // org.apache.commons.lang3.tuple.Pair
                    public Object getLeft() {
                        return lhs;
                    }

                    @Override // org.apache.commons.lang3.tuple.Pair
                    public Object getRight() {
                        return rhs;
                    }
                });
                return this;
            } else {
                return this;
            }
        }
        return this;
    }

    public DiffBuilder append(String fieldName, final Object[] lhs, final Object[] rhs) {
        if (!this.objectsTriviallyEqual && !Arrays.equals(lhs, rhs)) {
            this.diffs.add(new Diff<Object[]>(fieldName) { // from class: org.apache.commons.lang3.builder.DiffBuilder.18
                private static final long serialVersionUID = 1;

                @Override // org.apache.commons.lang3.tuple.Pair
                public Object[] getLeft() {
                    return lhs;
                }

                @Override // org.apache.commons.lang3.tuple.Pair
                public Object[] getRight() {
                    return rhs;
                }
            });
        }
        return this;
    }

    @Override // org.apache.commons.lang3.builder.Builder
    public DiffResult build() {
        return new DiffResult(this.left, this.right, this.diffs, this.style);
    }
}