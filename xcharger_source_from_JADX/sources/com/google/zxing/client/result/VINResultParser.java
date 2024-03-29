package com.google.zxing.client.result;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import java.util.regex.Pattern;

public final class VINResultParser extends ResultParser {
    private static final Pattern AZ09 = Pattern.compile("[A-Z0-9]{17}");
    private static final Pattern IOQ = Pattern.compile("[IOQ]");

    public VINParsedResult parse(Result result) {
        if (result.getBarcodeFormat() != BarcodeFormat.CODE_39) {
            return null;
        }
        String rawText = IOQ.matcher(result.getText()).replaceAll("").trim();
        if (!AZ09.matcher(rawText).matches()) {
            return null;
        }
        try {
            if (!checkChecksum(rawText)) {
                return null;
            }
            String wmi = rawText.substring(0, 3);
            return new VINParsedResult(rawText, wmi, rawText.substring(3, 9), rawText.substring(9, 17), countryCode(wmi), rawText.substring(3, 8), modelYear(rawText.charAt(9)), rawText.charAt(10), rawText.substring(11));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static boolean checkChecksum(CharSequence vin) {
        int sum = 0;
        for (int i = 0; i < vin.length(); i++) {
            sum += vinPositionWeight(i + 1) * vinCharValue(vin.charAt(i));
        }
        return vin.charAt(8) == checkChar(sum % 11);
    }

    private static int vinCharValue(char c) {
        if (c >= 'A' && c <= 'I') {
            return (c - 'A') + 1;
        }
        if (c >= 'J' && c <= 'R') {
            return (c - 'J') + 1;
        }
        if (c >= 'S' && c <= 'Z') {
            return (c - 'S') + 2;
        }
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        throw new IllegalArgumentException();
    }

    private static int vinPositionWeight(int position) {
        if (position >= 1 && position <= 7) {
            return 9 - position;
        }
        if (position == 8) {
            return 10;
        }
        if (position == 9) {
            return 0;
        }
        if (position >= 10 && position <= 17) {
            return 19 - position;
        }
        throw new IllegalArgumentException();
    }

    private static char checkChar(int remainder) {
        if (remainder < 10) {
            return (char) (remainder + 48);
        }
        if (remainder == 10) {
            return 'X';
        }
        throw new IllegalArgumentException();
    }

    private static int modelYear(char c) {
        if (c >= 'E' && c <= 'H') {
            return (c - 'E') + 1984;
        }
        if (c >= 'J' && c <= 'N') {
            return (c - 'J') + 1988;
        }
        if (c == 'P') {
            return 1993;
        }
        if (c >= 'R' && c <= 'T') {
            return (c - 'R') + 1994;
        }
        if (c >= 'V' && c <= 'Y') {
            return (c - 'V') + 1997;
        }
        if (c >= '1' && c <= '9') {
            return (c - '1') + 2001;
        }
        if (c >= 'A' && c <= 'D') {
            return (c - 'A') + 2010;
        }
        throw new IllegalArgumentException();
    }

    /* JADX WARNING: Removed duplicated region for block: B:2:0x0017 A[ORIG_RETURN, RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static java.lang.String countryCode(java.lang.CharSequence r8) {
        /*
            r7 = 69
            r6 = 57
            r5 = 51
            r4 = 82
            r3 = 65
            r2 = 0
            char r0 = r8.charAt(r2)
            r2 = 1
            char r1 = r8.charAt(r2)
            switch(r0) {
                case 49: goto L_0x0019;
                case 50: goto L_0x001c;
                case 51: goto L_0x001f;
                case 52: goto L_0x0019;
                case 53: goto L_0x0019;
                case 57: goto L_0x0028;
                case 74: goto L_0x0033;
                case 75: goto L_0x003c;
                case 76: goto L_0x0045;
                case 77: goto L_0x0048;
                case 83: goto L_0x004f;
                case 86: goto L_0x0063;
                case 87: goto L_0x0077;
                case 88: goto L_0x007a;
                case 90: goto L_0x0085;
                default: goto L_0x0017;
            }
        L_0x0017:
            r2 = 0
        L_0x0018:
            return r2
        L_0x0019:
            java.lang.String r2 = "US"
            goto L_0x0018
        L_0x001c:
            java.lang.String r2 = "CA"
            goto L_0x0018
        L_0x001f:
            if (r1 < r3) goto L_0x0017
            r2 = 87
            if (r1 > r2) goto L_0x0017
            java.lang.String r2 = "MX"
            goto L_0x0018
        L_0x0028:
            if (r1 < r3) goto L_0x002c
            if (r1 <= r7) goto L_0x0030
        L_0x002c:
            if (r1 < r5) goto L_0x0017
            if (r1 > r6) goto L_0x0017
        L_0x0030:
            java.lang.String r2 = "BR"
            goto L_0x0018
        L_0x0033:
            if (r1 < r3) goto L_0x0017
            r2 = 84
            if (r1 > r2) goto L_0x0017
            java.lang.String r2 = "JP"
            goto L_0x0018
        L_0x003c:
            r2 = 76
            if (r1 < r2) goto L_0x0017
            if (r1 > r4) goto L_0x0017
            java.lang.String r2 = "KO"
            goto L_0x0018
        L_0x0045:
            java.lang.String r2 = "CN"
            goto L_0x0018
        L_0x0048:
            if (r1 < r3) goto L_0x0017
            if (r1 > r7) goto L_0x0017
            java.lang.String r2 = "IN"
            goto L_0x0018
        L_0x004f:
            if (r1 < r3) goto L_0x0058
            r2 = 77
            if (r1 > r2) goto L_0x0058
            java.lang.String r2 = "UK"
            goto L_0x0018
        L_0x0058:
            r2 = 78
            if (r1 < r2) goto L_0x0017
            r2 = 84
            if (r1 > r2) goto L_0x0017
            java.lang.String r2 = "DE"
            goto L_0x0018
        L_0x0063:
            r2 = 70
            if (r1 < r2) goto L_0x006c
            if (r1 > r4) goto L_0x006c
            java.lang.String r2 = "FR"
            goto L_0x0018
        L_0x006c:
            r2 = 83
            if (r1 < r2) goto L_0x0017
            r2 = 87
            if (r1 > r2) goto L_0x0017
            java.lang.String r2 = "ES"
            goto L_0x0018
        L_0x0077:
            java.lang.String r2 = "DE"
            goto L_0x0018
        L_0x007a:
            r2 = 48
            if (r1 == r2) goto L_0x0082
            if (r1 < r5) goto L_0x0017
            if (r1 > r6) goto L_0x0017
        L_0x0082:
            java.lang.String r2 = "RU"
            goto L_0x0018
        L_0x0085:
            if (r1 < r3) goto L_0x0017
            if (r1 > r4) goto L_0x0017
            java.lang.String r2 = "IT"
            goto L_0x0018
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.zxing.client.result.VINResultParser.countryCode(java.lang.CharSequence):java.lang.String");
    }
}
