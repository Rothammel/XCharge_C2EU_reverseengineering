package com.google.zxing.aztec;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.Writer;
import com.google.zxing.aztec.encoder.AztecCode;
import com.google.zxing.aztec.encoder.Encoder;
import com.google.zxing.common.BitMatrix;
import java.nio.charset.Charset;
import java.util.Map;
import org.apache.commons.lang3.CharEncoding;

public final class AztecWriter implements Writer {
    private static final Charset DEFAULT_CHARSET = Charset.forName(CharEncoding.ISO_8859_1);

    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height) {
        return encode(contents, format, width, height, (Map<EncodeHintType, ?>) null);
    }

    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints) {
        Charset forName;
        int intValue;
        int intValue2;
        Number layers = null;
        String charset = hints == null ? null : (String) hints.get(EncodeHintType.CHARACTER_SET);
        Number eccPercent = hints == null ? null : (Number) hints.get(EncodeHintType.ERROR_CORRECTION);
        if (hints != null) {
            layers = (Number) hints.get(EncodeHintType.AZTEC_LAYERS);
        }
        if (charset == null) {
            forName = DEFAULT_CHARSET;
        } else {
            forName = Charset.forName(charset);
        }
        if (eccPercent == null) {
            intValue = 33;
        } else {
            intValue = eccPercent.intValue();
        }
        if (layers == null) {
            intValue2 = 0;
        } else {
            intValue2 = layers.intValue();
        }
        return encode(contents, format, width, height, forName, intValue, intValue2);
    }

    private static BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Charset charset, int eccPercent, int layers) {
        if (format == BarcodeFormat.AZTEC) {
            return renderResult(Encoder.encode(contents.getBytes(charset), eccPercent, layers), width, height);
        }
        throw new IllegalArgumentException("Can only encode AZTEC, but got " + format);
    }

    private static BitMatrix renderResult(AztecCode code, int width, int height) {
        BitMatrix input = code.getMatrix();
        if (input == null) {
            throw new IllegalStateException();
        }
        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        int outputWidth = Math.max(width, inputWidth);
        int outputHeight = Math.max(height, inputHeight);
        int multiple = Math.min(outputWidth / inputWidth, outputHeight / inputHeight);
        int leftPadding = (outputWidth - (inputWidth * multiple)) / 2;
        BitMatrix output = new BitMatrix(outputWidth, outputHeight);
        int inputY = 0;
        int outputY = (outputHeight - (inputHeight * multiple)) / 2;
        while (inputY < inputHeight) {
            int inputX = 0;
            int outputX = leftPadding;
            while (inputX < inputWidth) {
                if (input.get(inputX, inputY)) {
                    output.setRegion(outputX, outputY, multiple, multiple);
                }
                inputX++;
                outputX += multiple;
            }
            inputY++;
            outputY += multiple;
        }
        return output;
    }
}
