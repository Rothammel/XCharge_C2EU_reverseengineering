package com.google.zxing.pdf417.decoder;

import com.google.zxing.pdf417.PDF417Common;
import java.util.Formatter;

/* loaded from: classes.dex */
final class DetectionResult {
    private static final int ADJUST_ROW_NUMBER_SKIP = 2;
    private final int barcodeColumnCount;
    private final BarcodeMetadata barcodeMetadata;
    private BoundingBox boundingBox;
    private final DetectionResultColumn[] detectionResultColumns;

    /* JADX INFO: Access modifiers changed from: package-private */
    public DetectionResult(BarcodeMetadata barcodeMetadata, BoundingBox boundingBox) {
        this.barcodeMetadata = barcodeMetadata;
        this.barcodeColumnCount = barcodeMetadata.getColumnCount();
        this.boundingBox = boundingBox;
        this.detectionResultColumns = new DetectionResultColumn[this.barcodeColumnCount + 2];
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public DetectionResultColumn[] getDetectionResultColumns() {
        int previousUnadjustedCount;
        adjustIndicatorColumnRowNumbers(this.detectionResultColumns[0]);
        adjustIndicatorColumnRowNumbers(this.detectionResultColumns[this.barcodeColumnCount + 1]);
        int unadjustedCodewordCount = PDF417Common.MAX_CODEWORDS_IN_BARCODE;
        do {
            previousUnadjustedCount = unadjustedCodewordCount;
            unadjustedCodewordCount = adjustRowNumbers();
            if (unadjustedCodewordCount <= 0) {
                break;
            }
        } while (unadjustedCodewordCount < previousUnadjustedCount);
        return this.detectionResultColumns;
    }

    private void adjustIndicatorColumnRowNumbers(DetectionResultColumn detectionResultColumn) {
        if (detectionResultColumn != null) {
            ((DetectionResultRowIndicatorColumn) detectionResultColumn).adjustCompleteIndicatorColumnRowNumbers(this.barcodeMetadata);
        }
    }

    private int adjustRowNumbers() {
        int unadjustedCount = adjustRowNumbersByRow();
        if (unadjustedCount == 0) {
            return 0;
        }
        for (int barcodeColumn = 1; barcodeColumn < this.barcodeColumnCount + 1; barcodeColumn++) {
            Codeword[] codewords = this.detectionResultColumns[barcodeColumn].getCodewords();
            for (int codewordsRow = 0; codewordsRow < codewords.length; codewordsRow++) {
                if (codewords[codewordsRow] != null && !codewords[codewordsRow].hasValidRowNumber()) {
                    adjustRowNumbers(barcodeColumn, codewordsRow, codewords);
                }
            }
        }
        return unadjustedCount;
    }

    private int adjustRowNumbersByRow() {
        adjustRowNumbersFromBothRI();
        int unadjustedCount = adjustRowNumbersFromLRI();
        return adjustRowNumbersFromRRI() + unadjustedCount;
    }

    private void adjustRowNumbersFromBothRI() {
        if (this.detectionResultColumns[0] != null && this.detectionResultColumns[this.barcodeColumnCount + 1] != null) {
            Codeword[] LRIcodewords = this.detectionResultColumns[0].getCodewords();
            Codeword[] RRIcodewords = this.detectionResultColumns[this.barcodeColumnCount + 1].getCodewords();
            for (int codewordsRow = 0; codewordsRow < LRIcodewords.length; codewordsRow++) {
                if (LRIcodewords[codewordsRow] != null && RRIcodewords[codewordsRow] != null && LRIcodewords[codewordsRow].getRowNumber() == RRIcodewords[codewordsRow].getRowNumber()) {
                    for (int barcodeColumn = 1; barcodeColumn <= this.barcodeColumnCount; barcodeColumn++) {
                        Codeword codeword = this.detectionResultColumns[barcodeColumn].getCodewords()[codewordsRow];
                        if (codeword != null) {
                            codeword.setRowNumber(LRIcodewords[codewordsRow].getRowNumber());
                            if (!codeword.hasValidRowNumber()) {
                                this.detectionResultColumns[barcodeColumn].getCodewords()[codewordsRow] = null;
                            }
                        }
                    }
                }
            }
        }
    }

    private int adjustRowNumbersFromRRI() {
        if (this.detectionResultColumns[this.barcodeColumnCount + 1] == null) {
            return 0;
        }
        int unadjustedCount = 0;
        Codeword[] codewords = this.detectionResultColumns[this.barcodeColumnCount + 1].getCodewords();
        for (int codewordsRow = 0; codewordsRow < codewords.length; codewordsRow++) {
            if (codewords[codewordsRow] != null) {
                int rowIndicatorRowNumber = codewords[codewordsRow].getRowNumber();
                int invalidRowCounts = 0;
                for (int barcodeColumn = this.barcodeColumnCount + 1; barcodeColumn > 0 && invalidRowCounts < 2; barcodeColumn--) {
                    Codeword codeword = this.detectionResultColumns[barcodeColumn].getCodewords()[codewordsRow];
                    if (codeword != null) {
                        invalidRowCounts = adjustRowNumberIfValid(rowIndicatorRowNumber, invalidRowCounts, codeword);
                        if (!codeword.hasValidRowNumber()) {
                            unadjustedCount++;
                        }
                    }
                }
            }
        }
        return unadjustedCount;
    }

    private int adjustRowNumbersFromLRI() {
        if (this.detectionResultColumns[0] == null) {
            return 0;
        }
        int unadjustedCount = 0;
        Codeword[] codewords = this.detectionResultColumns[0].getCodewords();
        for (int codewordsRow = 0; codewordsRow < codewords.length; codewordsRow++) {
            if (codewords[codewordsRow] != null) {
                int rowIndicatorRowNumber = codewords[codewordsRow].getRowNumber();
                int invalidRowCounts = 0;
                for (int barcodeColumn = 1; barcodeColumn < this.barcodeColumnCount + 1 && invalidRowCounts < 2; barcodeColumn++) {
                    Codeword codeword = this.detectionResultColumns[barcodeColumn].getCodewords()[codewordsRow];
                    if (codeword != null) {
                        invalidRowCounts = adjustRowNumberIfValid(rowIndicatorRowNumber, invalidRowCounts, codeword);
                        if (!codeword.hasValidRowNumber()) {
                            unadjustedCount++;
                        }
                    }
                }
            }
        }
        return unadjustedCount;
    }

    private static int adjustRowNumberIfValid(int rowIndicatorRowNumber, int invalidRowCounts, Codeword codeword) {
        if (codeword == null) {
            return invalidRowCounts;
        }
        if (!codeword.hasValidRowNumber()) {
            if (codeword.isValidRowNumber(rowIndicatorRowNumber)) {
                codeword.setRowNumber(rowIndicatorRowNumber);
                invalidRowCounts = 0;
            } else {
                invalidRowCounts++;
            }
        }
        return invalidRowCounts;
    }

    private void adjustRowNumbers(int barcodeColumn, int codewordsRow, Codeword[] codewords) {
        Codeword codeword = codewords[codewordsRow];
        Codeword[] previousColumnCodewords = this.detectionResultColumns[barcodeColumn - 1].getCodewords();
        Codeword[] nextColumnCodewords = previousColumnCodewords;
        if (this.detectionResultColumns[barcodeColumn + 1] != null) {
            nextColumnCodewords = this.detectionResultColumns[barcodeColumn + 1].getCodewords();
        }
        Codeword[] otherCodewords = new Codeword[14];
        otherCodewords[2] = previousColumnCodewords[codewordsRow];
        otherCodewords[3] = nextColumnCodewords[codewordsRow];
        if (codewordsRow > 0) {
            otherCodewords[0] = codewords[codewordsRow - 1];
            otherCodewords[4] = previousColumnCodewords[codewordsRow - 1];
            otherCodewords[5] = nextColumnCodewords[codewordsRow - 1];
        }
        if (codewordsRow > 1) {
            otherCodewords[8] = codewords[codewordsRow - 2];
            otherCodewords[10] = previousColumnCodewords[codewordsRow - 2];
            otherCodewords[11] = nextColumnCodewords[codewordsRow - 2];
        }
        if (codewordsRow < codewords.length - 1) {
            otherCodewords[1] = codewords[codewordsRow + 1];
            otherCodewords[6] = previousColumnCodewords[codewordsRow + 1];
            otherCodewords[7] = nextColumnCodewords[codewordsRow + 1];
        }
        if (codewordsRow < codewords.length - 2) {
            otherCodewords[9] = codewords[codewordsRow + 2];
            otherCodewords[12] = previousColumnCodewords[codewordsRow + 2];
            otherCodewords[13] = nextColumnCodewords[codewordsRow + 2];
        }
        for (Codeword otherCodeword : otherCodewords) {
            if (adjustRowNumber(codeword, otherCodeword)) {
                return;
            }
        }
    }

    private static boolean adjustRowNumber(Codeword codeword, Codeword otherCodeword) {
        if (otherCodeword != null && otherCodeword.hasValidRowNumber() && otherCodeword.getBucket() == codeword.getBucket()) {
            codeword.setRowNumber(otherCodeword.getRowNumber());
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getBarcodeColumnCount() {
        return this.barcodeColumnCount;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getBarcodeRowCount() {
        return this.barcodeMetadata.getRowCount();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int getBarcodeECLevel() {
        return this.barcodeMetadata.getErrorCorrectionLevel();
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setDetectionResultColumn(int barcodeColumn, DetectionResultColumn detectionResultColumn) {
        this.detectionResultColumns[barcodeColumn] = detectionResultColumn;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public DetectionResultColumn getDetectionResultColumn(int barcodeColumn) {
        return this.detectionResultColumns[barcodeColumn];
    }

    public String toString() {
        DetectionResultColumn rowIndicatorColumn = this.detectionResultColumns[0];
        if (rowIndicatorColumn == null) {
            rowIndicatorColumn = this.detectionResultColumns[this.barcodeColumnCount + 1];
        }
        Formatter formatter = new Formatter();
        for (int codewordsRow = 0; codewordsRow < rowIndicatorColumn.getCodewords().length; codewordsRow++) {
            formatter.format("CW %3d:", Integer.valueOf(codewordsRow));
            for (int barcodeColumn = 0; barcodeColumn < this.barcodeColumnCount + 2; barcodeColumn++) {
                if (this.detectionResultColumns[barcodeColumn] == null) {
                    formatter.format("    |   ", new Object[0]);
                } else {
                    Codeword codeword = this.detectionResultColumns[barcodeColumn].getCodewords()[codewordsRow];
                    if (codeword == null) {
                        formatter.format("    |   ", new Object[0]);
                    } else {
                        formatter.format(" %3d|%3d", Integer.valueOf(codeword.getRowNumber()), Integer.valueOf(codeword.getValue()));
                    }
                }
            }
            formatter.format("%n", new Object[0]);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
}
