package com.google.zxing.qrcode.decoder;

import com.google.zxing.common.BitMatrix;

abstract class DataMask {
    private static final DataMask[] DATA_MASKS = {new DataMask000(), new DataMask001(), new DataMask010(), new DataMask011(), new DataMask100(), new DataMask101(), new DataMask110(), new DataMask111()};

    /* access modifiers changed from: package-private */
    public abstract boolean isMasked(int i, int i2);

    private DataMask() {
    }

    /* access modifiers changed from: package-private */
    public final void unmaskBitMatrix(BitMatrix bits, int dimension) {
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                if (isMasked(i, j)) {
                    bits.flip(j, i);
                }
            }
        }
    }

    static DataMask forReference(int reference) {
        if (reference >= 0 && reference <= 7) {
            return DATA_MASKS[reference];
        }
        throw new IllegalArgumentException();
    }

    private static final class DataMask000 extends DataMask {
        private DataMask000() {
            super();
        }

        /* access modifiers changed from: package-private */
        public boolean isMasked(int i, int j) {
            return ((i + j) & 1) == 0;
        }
    }

    private static final class DataMask001 extends DataMask {
        private DataMask001() {
            super();
        }

        /* access modifiers changed from: package-private */
        public boolean isMasked(int i, int j) {
            return (i & 1) == 0;
        }
    }

    private static final class DataMask010 extends DataMask {
        private DataMask010() {
            super();
        }

        /* access modifiers changed from: package-private */
        public boolean isMasked(int i, int j) {
            return j % 3 == 0;
        }
    }

    private static final class DataMask011 extends DataMask {
        private DataMask011() {
            super();
        }

        /* access modifiers changed from: package-private */
        public boolean isMasked(int i, int j) {
            return (i + j) % 3 == 0;
        }
    }

    private static final class DataMask100 extends DataMask {
        private DataMask100() {
            super();
        }

        /* access modifiers changed from: package-private */
        public boolean isMasked(int i, int j) {
            return (((i / 2) + (j / 3)) & 1) == 0;
        }
    }

    private static final class DataMask101 extends DataMask {
        private DataMask101() {
            super();
        }

        /* access modifiers changed from: package-private */
        public boolean isMasked(int i, int j) {
            int temp = i * j;
            return (temp & 1) + (temp % 3) == 0;
        }
    }

    private static final class DataMask110 extends DataMask {
        private DataMask110() {
            super();
        }

        /* access modifiers changed from: package-private */
        public boolean isMasked(int i, int j) {
            int temp = i * j;
            return (((temp & 1) + (temp % 3)) & 1) == 0;
        }
    }

    private static final class DataMask111 extends DataMask {
        private DataMask111() {
            super();
        }

        /* access modifiers changed from: package-private */
        public boolean isMasked(int i, int j) {
            return ((((i + j) & 1) + ((i * j) % 3)) & 1) == 0;
        }
    }
}
