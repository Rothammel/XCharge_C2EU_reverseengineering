package com.google.zxing.client.result;

import com.google.zxing.Result;

/* loaded from: classes.dex */
public final class WifiResultParser extends ResultParser {
    @Override // com.google.zxing.client.result.ResultParser
    public WifiParsedResult parse(Result result) {
        String ssid;
        String rawText = getMassagedText(result);
        if (!rawText.startsWith("WIFI:") || (ssid = matchSinglePrefixedField("S:", rawText, ';', false)) == null || ssid.isEmpty()) {
            return null;
        }
        String pass = matchSinglePrefixedField("P:", rawText, ';', false);
        String type = matchSinglePrefixedField("T:", rawText, ';', false);
        if (type == null) {
            type = "nopass";
        }
        boolean hidden = Boolean.parseBoolean(matchSinglePrefixedField("H:", rawText, ';', false));
        return new WifiParsedResult(type, ssid, pass, hidden);
    }
}
