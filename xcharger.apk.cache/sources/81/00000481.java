package com.google.zxing.pdf417.decoder;

import java.util.Formatter;

/* loaded from: classes.dex */
class DetectionResultColumn {
    private static final int MAX_NEARBY_DISTANCE = 5;
    private final BoundingBox boundingBox;
    private final Codeword[] codewords;

    /* JADX INFO: Access modifiers changed from: package-private */
    public DetectionResultColumn(BoundingBox boundingBox) {
        this.boundingBox = new BoundingBox(boundingBox);
        this.codewords = new Codeword[(boundingBox.getMaxY() - boundingBox.getMinY()) + 1];
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final Codeword getCodewordNearby(int imageRow) {
        Codeword codeword;
        Codeword codeword2;
        Codeword codeword3 = getCodeword(imageRow);
        if (codeword3 != null) {
            return codeword3;
        }
        for (int i = 1; i < 5; i++) {
            int nearImageRow = imageRowToCodewordIndex(imageRow) - i;
            if (nearImageRow >= 0 && (codeword2 = this.codewords[nearImageRow]) != null) {
                return codeword2;
            }
            int nearImageRow2 = imageRowToCodewordIndex(imageRow) + i;
            if (nearImageRow2 < this.codewords.length && (codeword = this.codewords[nearImageRow2]) != null) {
                return codeword;
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final int imageRowToCodewordIndex(int imageRow) {
        return imageRow - this.boundingBox.getMinY();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void setCodeword(int imageRow, Codeword codeword) {
        this.codewords[imageRowToCodewordIndex(imageRow)] = codeword;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final Codeword getCodeword(int imageRow) {
        return this.codewords[imageRowToCodewordIndex(imageRow)];
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final Codeword[] getCodewords() {
        return this.codewords;
    }

    public String toString() {
        int row;
        Formatter formatter = new Formatter();
        Codeword[] codewordArr = this.codewords;
        int length = codewordArr.length;
        int i = 0;
        int row2 = 0;
        while (i < length) {
            Codeword codeword = codewordArr[i];
            if (codeword == null) {
                row = row2 + 1;
                formatter.format("%3d:    |   %n", Integer.valueOf(row2));
            } else {
                row = row2 + 1;
                formatter.format("%3d: %3d|%3d%n", Integer.valueOf(row2), Integer.valueOf(codeword.getRowNumber()), Integer.valueOf(codeword.getValue()));
            }
            i++;
            row2 = row;
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
}