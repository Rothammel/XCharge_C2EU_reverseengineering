package com.google.zxing.client.result;

import android.support.v4.util.TimeUtils;
import com.alibaba.sdk.android.oss_android_sdk.BuildConfig;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.aztec.encoder.Encoder;
import com.xcharge.charger.device.c2.bean.PortRuntimeData;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.conn.ssl.TokenParser;

/* loaded from: classes.dex */
public final class ExpandedProductResultParser extends ResultParser {
    @Override // com.google.zxing.client.result.ResultParser
    public ExpandedProductParsedResult parse(Result result) {
        BarcodeFormat format = result.getBarcodeFormat();
        if (format != BarcodeFormat.RSS_EXPANDED) {
            return null;
        }
        String rawText = getMassagedText(result);
        String productID = null;
        String sscc = null;
        String lotNumber = null;
        String productionDate = null;
        String packagingDate = null;
        String bestBeforeDate = null;
        String expirationDate = null;
        String weight = null;
        String weightType = null;
        String weightIncrement = null;
        String price = null;
        String priceIncrement = null;
        String priceCurrency = null;
        Map<String, String> uncommonAIs = new HashMap<>();
        int i = 0;
        while (i < rawText.length()) {
            String ai = findAIvalue(i, rawText);
            if (ai == null) {
                return null;
            }
            int i2 = i + ai.length() + 2;
            String value = findValue(i2, rawText);
            i = i2 + value.length();
            char c = 65535;
            switch (ai.hashCode()) {
                case 1536:
                    if (ai.equals("00")) {
                        c = 0;
                        break;
                    }
                    break;
                case 1537:
                    if (ai.equals("01")) {
                        c = 1;
                        break;
                    }
                    break;
                case 1567:
                    if (ai.equals("10")) {
                        c = 2;
                        break;
                    }
                    break;
                case 1568:
                    if (ai.equals("11")) {
                        c = 3;
                        break;
                    }
                    break;
                case 1570:
                    if (ai.equals("13")) {
                        c = 4;
                        break;
                    }
                    break;
                case 1572:
                    if (ai.equals("15")) {
                        c = 5;
                        break;
                    }
                    break;
                case 1574:
                    if (ai.equals("17")) {
                        c = 6;
                        break;
                    }
                    break;
                case 1567966:
                    if (ai.equals("3100")) {
                        c = 7;
                        break;
                    }
                    break;
                case 1567967:
                    if (ai.equals("3101")) {
                        c = '\b';
                        break;
                    }
                    break;
                case 1567968:
                    if (ai.equals("3102")) {
                        c = '\t';
                        break;
                    }
                    break;
                case 1567969:
                    if (ai.equals("3103")) {
                        c = '\n';
                        break;
                    }
                    break;
                case 1567970:
                    if (ai.equals("3104")) {
                        c = 11;
                        break;
                    }
                    break;
                case 1567971:
                    if (ai.equals("3105")) {
                        c = '\f';
                        break;
                    }
                    break;
                case 1567972:
                    if (ai.equals("3106")) {
                        c = '\r';
                        break;
                    }
                    break;
                case 1567973:
                    if (ai.equals("3107")) {
                        c = 14;
                        break;
                    }
                    break;
                case 1567974:
                    if (ai.equals("3108")) {
                        c = 15;
                        break;
                    }
                    break;
                case 1567975:
                    if (ai.equals("3109")) {
                        c = 16;
                        break;
                    }
                    break;
                case 1568927:
                    if (ai.equals("3200")) {
                        c = 17;
                        break;
                    }
                    break;
                case 1568928:
                    if (ai.equals("3201")) {
                        c = 18;
                        break;
                    }
                    break;
                case 1568929:
                    if (ai.equals("3202")) {
                        c = 19;
                        break;
                    }
                    break;
                case 1568930:
                    if (ai.equals("3203")) {
                        c = 20;
                        break;
                    }
                    break;
                case 1568931:
                    if (ai.equals("3204")) {
                        c = 21;
                        break;
                    }
                    break;
                case 1568932:
                    if (ai.equals("3205")) {
                        c = 22;
                        break;
                    }
                    break;
                case 1568933:
                    if (ai.equals("3206")) {
                        c = 23;
                        break;
                    }
                    break;
                case 1568934:
                    if (ai.equals("3207")) {
                        c = 24;
                        break;
                    }
                    break;
                case 1568935:
                    if (ai.equals("3208")) {
                        c = 25;
                        break;
                    }
                    break;
                case 1568936:
                    if (ai.equals("3209")) {
                        c = 26;
                        break;
                    }
                    break;
                case 1575716:
                    if (ai.equals("3920")) {
                        c = 27;
                        break;
                    }
                    break;
                case 1575717:
                    if (ai.equals("3921")) {
                        c = 28;
                        break;
                    }
                    break;
                case 1575718:
                    if (ai.equals("3922")) {
                        c = 29;
                        break;
                    }
                    break;
                case 1575719:
                    if (ai.equals("3923")) {
                        c = 30;
                        break;
                    }
                    break;
                case 1575747:
                    if (ai.equals("3930")) {
                        c = 31;
                        break;
                    }
                    break;
                case 1575748:
                    if (ai.equals("3931")) {
                        c = TokenParser.SP;
                        break;
                    }
                    break;
                case 1575749:
                    if (ai.equals("3932")) {
                        c = '!';
                        break;
                    }
                    break;
                case 1575750:
                    if (ai.equals("3933")) {
                        c = TokenParser.DQUOTE;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    sscc = value;
                    break;
                case 1:
                    productID = value;
                    break;
                case 2:
                    lotNumber = value;
                    break;
                case 3:
                    productionDate = value;
                    break;
                case 4:
                    packagingDate = value;
                    break;
                case 5:
                    bestBeforeDate = value;
                    break;
                case 6:
                    expirationDate = value;
                    break;
                case 7:
                case '\b':
                case '\t':
                case '\n':
                case PortRuntimeData.STATUS_EX_11 /* 11 */:
                case PortRuntimeData.STATUS_EX_12 /* 12 */:
                case '\r':
                case 14:
                case 15:
                case 16:
                    weight = value;
                    weightType = ExpandedProductParsedResult.KILOGRAM;
                    weightIncrement = ai.substring(3);
                    break;
                case 17:
                case 18:
                case TimeUtils.HUNDRED_DAY_FIELD_LEN /* 19 */:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case BuildConfig.VERSION_CODE /* 26 */:
                    weight = value;
                    weightType = ExpandedProductParsedResult.POUND;
                    weightIncrement = ai.substring(3);
                    break;
                case 27:
                case 28:
                case 29:
                case 30:
                    price = value;
                    priceIncrement = ai.substring(3);
                    break;
                case 31:
                case ' ':
                case Encoder.DEFAULT_EC_PERCENT /* 33 */:
                case '\"':
                    if (value.length() < 4) {
                        return null;
                    }
                    price = value.substring(3);
                    priceCurrency = value.substring(0, 3);
                    priceIncrement = ai.substring(3);
                    break;
                default:
                    uncommonAIs.put(ai, value);
                    break;
            }
        }
        return new ExpandedProductParsedResult(rawText, productID, sscc, lotNumber, productionDate, packagingDate, bestBeforeDate, expirationDate, weight, weightType, weightIncrement, price, priceIncrement, priceCurrency, uncommonAIs);
    }

    private static String findAIvalue(int i, String rawText) {
        char c = rawText.charAt(i);
        if (c != '(') {
            return null;
        }
        CharSequence rawTextAux = rawText.substring(i + 1);
        StringBuilder buf = new StringBuilder();
        for (int index = 0; index < rawTextAux.length(); index++) {
            char currentChar = rawTextAux.charAt(index);
            if (currentChar == ')') {
                return buf.toString();
            }
            if (currentChar < '0' || currentChar > '9') {
                return null;
            }
            buf.append(currentChar);
        }
        return buf.toString();
    }

    private static String findValue(int i, String rawText) {
        StringBuilder buf = new StringBuilder();
        String rawTextAux = rawText.substring(i);
        for (int index = 0; index < rawTextAux.length(); index++) {
            char c = rawTextAux.charAt(index);
            if (c == '(') {
                if (findAIvalue(index, rawTextAux) != null) {
                    break;
                }
                buf.append('(');
            } else {
                buf.append(c);
            }
        }
        return buf.toString();
    }
}