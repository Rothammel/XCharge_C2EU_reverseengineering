package org.apache.commons.lang3.tuple;

public final class ImmutablePair<L, R> extends Pair<L, R> {
    private static final long serialVersionUID = 4954918890077093841L;
    public final L left;
    public final R right;

    /* renamed from: of */
    public static <L, R> ImmutablePair<L, R> m49of(L left2, R right2) {
        return new ImmutablePair<>(left2, right2);
    }

    public ImmutablePair(L left2, R right2) {
        this.left = left2;
        this.right = right2;
    }

    public L getLeft() {
        return this.left;
    }

    public R getRight() {
        return this.right;
    }

    public R setValue(R r) {
        throw new UnsupportedOperationException();
    }
}
