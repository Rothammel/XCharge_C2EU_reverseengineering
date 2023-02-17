package org.apache.commons.lang3.text.translate;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

/* loaded from: classes.dex */
public class NumericEntityUnescaper extends CharSequenceTranslator {
    private final EnumSet<OPTION> options;

    /* loaded from: classes.dex */
    public enum OPTION {
        semiColonRequired,
        semiColonOptional,
        errorIfNoSemiColon
    }

    public NumericEntityUnescaper(OPTION... options) {
        if (options.length > 0) {
            this.options = EnumSet.copyOf((Collection) Arrays.asList(options));
        } else {
            this.options = EnumSet.copyOf((Collection) Arrays.asList(OPTION.semiColonRequired));
        }
    }

    public boolean isSet(OPTION option) {
        if (this.options == null) {
            return false;
        }
        return this.options.contains(option);
    }

    /* JADX WARN: Code restructure failed: missing block: B:33:0x006e, code lost:
        if (r12.charAt(r1) != ';') goto L63;
     */
    /* JADX WARN: Code restructure failed: missing block: B:34:0x0070, code lost:
        r6 = true;
     */
    @Override // org.apache.commons.lang3.text.translate.CharSequenceTranslator
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public int translate(java.lang.CharSequence r12, int r13, java.io.Writer r14) throws java.io.IOException {
        /*
            Method dump skipped, instructions count: 224
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.lang3.text.translate.NumericEntityUnescaper.translate(java.lang.CharSequence, int, java.io.Writer):int");
    }
}
