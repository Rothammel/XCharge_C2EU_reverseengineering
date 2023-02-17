package org.apache.commons.lang3.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

public class DiffBuilder implements Builder<DiffResult> {
    private final List<Diff<?>> diffs;
    private final Object left;
    private final boolean objectsTriviallyEqual;
    private final Object right;
    private final ToStringStyle style;

    public DiffBuilder(Object lhs, Object rhs, ToStringStyle style2, boolean testTriviallyEqual) {
        if (lhs == null) {
            throw new IllegalArgumentException("lhs cannot be null");
        } else if (rhs == null) {
            throw new IllegalArgumentException("rhs cannot be null");
        } else {
            this.diffs = new ArrayList();
            this.left = lhs;
            this.right = rhs;
            this.style = style2;
            this.objectsTriviallyEqual = testTriviallyEqual && (lhs == rhs || lhs.equals(rhs));
        }
    }

    public DiffBuilder(Object lhs, Object rhs, ToStringStyle style2) {
        this(lhs, rhs, style2, true);
    }

    public DiffBuilder append(String fieldName, final boolean lhs, final boolean rhs) {
        if (fieldName == null) {
            throw new IllegalArgumentException("Field name cannot be null");
        }
        if (!this.objectsTriviallyEqual && lhs != rhs) {
            this.diffs.add(new Diff<Boolean>(fieldName) {
                private static final long serialVersionUID = 1;

                public Boolean getLeft() {
                    return Boolean.valueOf(lhs);
                }

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
            this.diffs.add(new Diff<Boolean[]>(fieldName) {
                private static final long serialVersionUID = 1;

                public Boolean[] getLeft() {
                    return ArrayUtils.toObject(lhs);
                }

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
            this.diffs.add(new Diff<Byte>(fieldName) {
                private static final long serialVersionUID = 1;

                public Byte getLeft() {
                    return Byte.valueOf(lhs);
                }

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
            this.diffs.add(new Diff<Byte[]>(fieldName) {
                private static final long serialVersionUID = 1;

                public Byte[] getLeft() {
                    return ArrayUtils.toObject(lhs);
                }

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
            this.diffs.add(new Diff<Character>(fieldName) {
                private static final long serialVersionUID = 1;

                public Character getLeft() {
                    return Character.valueOf(lhs);
                }

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
            this.diffs.add(new Diff<Character[]>(fieldName) {
                private static final long serialVersionUID = 1;

                public Character[] getLeft() {
                    return ArrayUtils.toObject(lhs);
                }

                public Character[] getRight() {
                    return ArrayUtils.toObject(rhs);
                }
            });
        }
        return this;
    }

    public DiffBuilder append(String fieldName, double lhs, double rhs) {
        if (fieldName == null) {
            throw new IllegalArgumentException("Field name cannot be null");
        }
        if (!this.objectsTriviallyEqual && Double.doubleToLongBits(lhs) != Double.doubleToLongBits(rhs)) {
            final double d = lhs;
            final double d2 = rhs;
            this.diffs.add(new Diff<Double>(fieldName) {
                private static final long serialVersionUID = 1;

                public Double getLeft() {
                    return Double.valueOf(d);
                }

                public Double getRight() {
                    return Double.valueOf(d2);
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
            this.diffs.add(new Diff<Double[]>(fieldName) {
                private static final long serialVersionUID = 1;

                public Double[] getLeft() {
                    return ArrayUtils.toObject(lhs);
                }

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
            this.diffs.add(new Diff<Float>(fieldName) {
                private static final long serialVersionUID = 1;

                public Float getLeft() {
                    return Float.valueOf(lhs);
                }

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
            this.diffs.add(new Diff<Float[]>(fieldName) {
                private static final long serialVersionUID = 1;

                public Float[] getLeft() {
                    return ArrayUtils.toObject(lhs);
                }

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
            this.diffs.add(new Diff<Integer>(fieldName) {
                private static final long serialVersionUID = 1;

                public Integer getLeft() {
                    return Integer.valueOf(lhs);
                }

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
            this.diffs.add(new Diff<Integer[]>(fieldName) {
                private static final long serialVersionUID = 1;

                public Integer[] getLeft() {
                    return ArrayUtils.toObject(lhs);
                }

                public Integer[] getRight() {
                    return ArrayUtils.toObject(rhs);
                }
            });
        }
        return this;
    }

    public DiffBuilder append(String fieldName, long lhs, long rhs) {
        if (fieldName == null) {
            throw new IllegalArgumentException("Field name cannot be null");
        }
        if (!this.objectsTriviallyEqual && lhs != rhs) {
            final long j = lhs;
            final long j2 = rhs;
            this.diffs.add(new Diff<Long>(fieldName) {
                private static final long serialVersionUID = 1;

                public Long getLeft() {
                    return Long.valueOf(j);
                }

                public Long getRight() {
                    return Long.valueOf(j2);
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
            this.diffs.add(new Diff<Long[]>(fieldName) {
                private static final long serialVersionUID = 1;

                public Long[] getLeft() {
                    return ArrayUtils.toObject(lhs);
                }

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
            this.diffs.add(new Diff<Short>(fieldName) {
                private static final long serialVersionUID = 1;

                public Short getLeft() {
                    return Short.valueOf(lhs);
                }

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
            this.diffs.add(new Diff<Short[]>(fieldName) {
                private static final long serialVersionUID = 1;

                public Short[] getLeft() {
                    return ArrayUtils.toObject(lhs);
                }

                public Short[] getRight() {
                    return ArrayUtils.toObject(rhs);
                }
            });
        }
        return this;
    }

    /* Debug info: failed to restart local var, previous not found, register: 3 */
    public DiffBuilder append(String fieldName, final Object lhs, final Object rhs) {
        Object objectToTest;
        if (this.objectsTriviallyEqual || lhs == rhs) {
            return this;
        }
        if (lhs != null) {
            objectToTest = lhs;
        } else {
            objectToTest = rhs;
        }
        if (objectToTest.getClass().isArray()) {
            if (objectToTest instanceof boolean[]) {
                return append(fieldName, (boolean[]) (boolean[]) lhs, (boolean[]) (boolean[]) rhs);
            }
            if (objectToTest instanceof byte[]) {
                return append(fieldName, (byte[]) (byte[]) lhs, (byte[]) (byte[]) rhs);
            }
            if (objectToTest instanceof char[]) {
                return append(fieldName, (char[]) (char[]) lhs, (char[]) (char[]) rhs);
            }
            if (objectToTest instanceof double[]) {
                return append(fieldName, (double[]) (double[]) lhs, (double[]) (double[]) rhs);
            }
            if (objectToTest instanceof float[]) {
                return append(fieldName, (float[]) (float[]) lhs, (float[]) (float[]) rhs);
            }
            if (objectToTest instanceof int[]) {
                return append(fieldName, (int[]) (int[]) lhs, (int[]) (int[]) rhs);
            }
            if (objectToTest instanceof long[]) {
                return append(fieldName, (long[]) (long[]) lhs, (long[]) (long[]) rhs);
            }
            if (objectToTest instanceof short[]) {
                return append(fieldName, (short[]) (short[]) lhs, (short[]) (short[]) rhs);
            }
            return append(fieldName, (Object[]) (Object[]) lhs, (Object[]) (Object[]) rhs);
        } else if (lhs != null && lhs.equals(rhs)) {
            return this;
        } else {
            this.diffs.add(new Diff<Object>(fieldName) {
                private static final long serialVersionUID = 1;

                public Object getLeft() {
                    return lhs;
                }

                public Object getRight() {
                    return rhs;
                }
            });
            return this;
        }
    }

    public DiffBuilder append(String fieldName, final Object[] lhs, final Object[] rhs) {
        if (!this.objectsTriviallyEqual && !Arrays.equals(lhs, rhs)) {
            this.diffs.add(new Diff<Object[]>(fieldName) {
                private static final long serialVersionUID = 1;

                public Object[] getLeft() {
                    return lhs;
                }

                public Object[] getRight() {
                    return rhs;
                }
            });
        }
        return this;
    }

    public DiffResult build() {
        return new DiffResult(this.left, this.right, this.diffs, this.style);
    }
}
