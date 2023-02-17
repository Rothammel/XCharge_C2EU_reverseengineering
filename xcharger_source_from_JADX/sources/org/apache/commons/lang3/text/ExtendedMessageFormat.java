package org.apache.commons.lang3.text;

import java.text.Format;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;
import p010it.sauronsoftware.ftp4j.FTPCodes;

public class ExtendedMessageFormat extends MessageFormat {
    static final /* synthetic */ boolean $assertionsDisabled = (!ExtendedMessageFormat.class.desiredAssertionStatus());
    private static final String DUMMY_PATTERN = "";
    private static final char END_FE = '}';
    private static final int HASH_SEED = 31;
    private static final char QUOTE = '\'';
    private static final char START_FE = '{';
    private static final char START_FMT = ',';
    private static final long serialVersionUID = -2362048321261811743L;
    private final Map<String, ? extends FormatFactory> registry;
    private String toPattern;

    public ExtendedMessageFormat(String pattern) {
        this(pattern, Locale.getDefault());
    }

    public ExtendedMessageFormat(String pattern, Locale locale) {
        this(pattern, locale, (Map<String, ? extends FormatFactory>) null);
    }

    public ExtendedMessageFormat(String pattern, Map<String, ? extends FormatFactory> registry2) {
        this(pattern, Locale.getDefault(), registry2);
    }

    public ExtendedMessageFormat(String pattern, Locale locale, Map<String, ? extends FormatFactory> registry2) {
        super("");
        setLocale(locale);
        this.registry = registry2;
        applyPattern(pattern);
    }

    public String toPattern() {
        return this.toPattern;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x00a6, code lost:
        r7 = parseFormatDescription(r21, next(r14));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void applyPattern(java.lang.String r21) {
        /*
            r20 = this;
            r0 = r20
            java.util.Map<java.lang.String, ? extends org.apache.commons.lang3.text.FormatFactory> r0 = r0.registry
            r17 = r0
            if (r17 != 0) goto L_0x0016
            super.applyPattern(r21)
            java.lang.String r17 = super.toPattern()
            r0 = r17
            r1 = r20
            r1.toPattern = r0
        L_0x0015:
            return
        L_0x0016:
            java.util.ArrayList r9 = new java.util.ArrayList
            r9.<init>()
            java.util.ArrayList r8 = new java.util.ArrayList
            r8.<init>()
            java.lang.StringBuilder r16 = new java.lang.StringBuilder
            int r17 = r21.length()
            r16.<init>(r17)
            java.text.ParsePosition r14 = new java.text.ParsePosition
            r17 = 0
            r0 = r17
            r14.<init>(r0)
            char[] r3 = r21.toCharArray()
            r5 = 0
        L_0x0037:
            int r17 = r14.getIndex()
            int r18 = r21.length()
            r0 = r17
            r1 = r18
            if (r0 >= r1) goto L_0x011b
            int r17 = r14.getIndex()
            char r17 = r3[r17]
            switch(r17) {
                case 39: goto L_0x005d;
                case 123: goto L_0x0067;
                default: goto L_0x004e;
            }
        L_0x004e:
            int r17 = r14.getIndex()
            char r17 = r3[r17]
            r16.append(r17)
            r0 = r20
            r0.next(r14)
            goto L_0x0037
        L_0x005d:
            r0 = r20
            r1 = r21
            r2 = r16
            r0.appendQuotedString(r1, r14, r2)
            goto L_0x0037
        L_0x0067:
            int r5 = r5 + 1
            r0 = r20
            r1 = r21
            r0.seekNonWs(r1, r14)
            int r15 = r14.getIndex()
            r0 = r20
            java.text.ParsePosition r17 = r0.next(r14)
            r0 = r20
            r1 = r21
            r2 = r17
            int r11 = r0.readArgumentIndex(r1, r2)
            r17 = 123(0x7b, float:1.72E-43)
            java.lang.StringBuilder r17 = r16.append(r17)
            r0 = r17
            r0.append(r11)
            r0 = r20
            r1 = r21
            r0.seekNonWs(r1, r14)
            r6 = 0
            r7 = 0
            int r17 = r14.getIndex()
            char r17 = r3[r17]
            r18 = 44
            r0 = r17
            r1 = r18
            if (r0 != r1) goto L_0x00c9
            r0 = r20
            java.text.ParsePosition r17 = r0.next(r14)
            r0 = r20
            r1 = r21
            r2 = r17
            java.lang.String r7 = r0.parseFormatDescription(r1, r2)
            r0 = r20
            java.text.Format r6 = r0.getFormat(r7)
            if (r6 != 0) goto L_0x00c9
            r17 = 44
            java.lang.StringBuilder r17 = r16.append(r17)
            r0 = r17
            r0.append(r7)
        L_0x00c9:
            r9.add(r6)
            if (r6 != 0) goto L_0x00cf
            r7 = 0
        L_0x00cf:
            r8.add(r7)
            int r17 = r9.size()
            r0 = r17
            if (r0 != r5) goto L_0x0115
            r17 = 1
        L_0x00dc:
            org.apache.commons.lang3.Validate.isTrue(r17)
            int r17 = r8.size()
            r0 = r17
            if (r0 != r5) goto L_0x0118
            r17 = 1
        L_0x00e9:
            org.apache.commons.lang3.Validate.isTrue(r17)
            int r17 = r14.getIndex()
            char r17 = r3[r17]
            r18 = 125(0x7d, float:1.75E-43)
            r0 = r17
            r1 = r18
            if (r0 == r1) goto L_0x004e
            java.lang.IllegalArgumentException r17 = new java.lang.IllegalArgumentException
            java.lang.StringBuilder r18 = new java.lang.StringBuilder
            r18.<init>()
            java.lang.String r19 = "Unreadable format element at position "
            java.lang.StringBuilder r18 = r18.append(r19)
            r0 = r18
            java.lang.StringBuilder r18 = r0.append(r15)
            java.lang.String r18 = r18.toString()
            r17.<init>(r18)
            throw r17
        L_0x0115:
            r17 = 0
            goto L_0x00dc
        L_0x0118:
            r17 = 0
            goto L_0x00e9
        L_0x011b:
            java.lang.String r17 = r16.toString()
            r0 = r20
            r1 = r17
            super.applyPattern(r1)
            java.lang.String r17 = super.toPattern()
            r0 = r20
            r1 = r17
            java.lang.String r17 = r0.insertFormats(r1, r8)
            r0 = r17
            r1 = r20
            r1.toPattern = r0
            r0 = r20
            boolean r17 = r0.containsElements(r9)
            if (r17 == 0) goto L_0x0015
            java.text.Format[] r13 = r20.getFormats()
            r10 = 0
            java.util.Iterator r12 = r9.iterator()
        L_0x0149:
            boolean r17 = r12.hasNext()
            if (r17 == 0) goto L_0x015c
            java.lang.Object r4 = r12.next()
            java.text.Format r4 = (java.text.Format) r4
            if (r4 == 0) goto L_0x0159
            r13[r10] = r4
        L_0x0159:
            int r10 = r10 + 1
            goto L_0x0149
        L_0x015c:
            r0 = r20
            super.setFormats(r13)
            goto L_0x0015
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.lang3.text.ExtendedMessageFormat.applyPattern(java.lang.String):void");
    }

    public void setFormat(int formatElementIndex, Format newFormat) {
        throw new UnsupportedOperationException();
    }

    public void setFormatByArgumentIndex(int argumentIndex, Format newFormat) {
        throw new UnsupportedOperationException();
    }

    public void setFormats(Format[] newFormats) {
        throw new UnsupportedOperationException();
    }

    public void setFormatsByArgumentIndex(Format[] newFormats) {
        throw new UnsupportedOperationException();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (ObjectUtils.notEqual(getClass(), obj.getClass())) {
            return false;
        }
        ExtendedMessageFormat rhs = (ExtendedMessageFormat) obj;
        if (ObjectUtils.notEqual(this.toPattern, rhs.toPattern)) {
            return false;
        }
        if (ObjectUtils.notEqual(this.registry, rhs.registry)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (((super.hashCode() * HASH_SEED) + ObjectUtils.hashCode(this.registry)) * HASH_SEED) + ObjectUtils.hashCode(this.toPattern);
    }

    private Format getFormat(String desc) {
        if (this.registry != null) {
            String name = desc;
            String args = null;
            int i = desc.indexOf(44);
            if (i > 0) {
                name = desc.substring(0, i).trim();
                args = desc.substring(i + 1).trim();
            }
            FormatFactory factory = (FormatFactory) this.registry.get(name);
            if (factory != null) {
                return factory.getFormat(name, args, getLocale());
            }
        }
        return null;
    }

    private int readArgumentIndex(String pattern, ParsePosition pos) {
        int start = pos.getIndex();
        seekNonWs(pattern, pos);
        StringBuilder result = new StringBuilder();
        boolean error = false;
        while (!error && pos.getIndex() < pattern.length()) {
            char c = pattern.charAt(pos.getIndex());
            if (Character.isWhitespace(c)) {
                seekNonWs(pattern, pos);
                c = pattern.charAt(pos.getIndex());
                if (!(c == ',' || c == '}')) {
                    error = true;
                    next(pos);
                }
            }
            if ((c == ',' || c == '}') && result.length() > 0) {
                try {
                    return Integer.parseInt(result.toString());
                } catch (NumberFormatException e) {
                }
            }
            error = !Character.isDigit(c);
            result.append(c);
            next(pos);
        }
        if (error) {
            throw new IllegalArgumentException("Invalid format argument index at position " + start + ": " + pattern.substring(start, pos.getIndex()));
        }
        throw new IllegalArgumentException("Unterminated format element at position " + start);
    }

    private String parseFormatDescription(String pattern, ParsePosition pos) {
        int start = pos.getIndex();
        seekNonWs(pattern, pos);
        int text = pos.getIndex();
        int depth = 1;
        while (pos.getIndex() < pattern.length()) {
            switch (pattern.charAt(pos.getIndex())) {
                case '\'':
                    getQuotedString(pattern, pos);
                    break;
                case '{':
                    depth++;
                    break;
                case FTPCodes.DATA_CONNECTION_ALREADY_OPEN:
                    depth--;
                    if (depth != 0) {
                        break;
                    } else {
                        return pattern.substring(text, pos.getIndex());
                    }
            }
            next(pos);
        }
        throw new IllegalArgumentException("Unterminated format element at position " + start);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0050, code lost:
        r3 = r3 + 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String insertFormats(java.lang.String r9, java.util.ArrayList<java.lang.String> r10) {
        /*
            r8 = this;
            boolean r6 = r8.containsElements(r10)
            if (r6 != 0) goto L_0x0007
        L_0x0006:
            return r9
        L_0x0007:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            int r6 = r9.length()
            int r6 = r6 * 2
            r5.<init>(r6)
            java.text.ParsePosition r4 = new java.text.ParsePosition
            r6 = 0
            r4.<init>(r6)
            r3 = -1
            r2 = 0
        L_0x001a:
            int r6 = r4.getIndex()
            int r7 = r9.length()
            if (r6 >= r7) goto L_0x0067
            int r6 = r4.getIndex()
            char r0 = r9.charAt(r6)
            switch(r0) {
                case 39: goto L_0x0036;
                case 123: goto L_0x003a;
                case 125: goto L_0x0064;
                default: goto L_0x002f;
            }
        L_0x002f:
            r5.append(r0)
            r8.next(r4)
            goto L_0x001a
        L_0x0036:
            r8.appendQuotedString(r9, r4, r5)
            goto L_0x001a
        L_0x003a:
            int r2 = r2 + 1
            r6 = 123(0x7b, float:1.72E-43)
            java.lang.StringBuilder r6 = r5.append(r6)
            java.text.ParsePosition r7 = r8.next(r4)
            int r7 = r8.readArgumentIndex(r9, r7)
            r6.append(r7)
            r6 = 1
            if (r2 != r6) goto L_0x001a
            int r3 = r3 + 1
            java.lang.Object r1 = r10.get(r3)
            java.lang.String r1 = (java.lang.String) r1
            if (r1 == 0) goto L_0x001a
            r6 = 44
            java.lang.StringBuilder r6 = r5.append(r6)
            r6.append(r1)
            goto L_0x001a
        L_0x0064:
            int r2 = r2 + -1
            goto L_0x002f
        L_0x0067:
            java.lang.String r9 = r5.toString()
            goto L_0x0006
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.lang3.text.ExtendedMessageFormat.insertFormats(java.lang.String, java.util.ArrayList):java.lang.String");
    }

    private void seekNonWs(String pattern, ParsePosition pos) {
        char[] buffer = pattern.toCharArray();
        do {
            int len = StrMatcher.splitMatcher().isMatch(buffer, pos.getIndex());
            pos.setIndex(pos.getIndex() + len);
            if (len <= 0 || pos.getIndex() >= pattern.length()) {
            }
            int len2 = StrMatcher.splitMatcher().isMatch(buffer, pos.getIndex());
            pos.setIndex(pos.getIndex() + len2);
            return;
        } while (pos.getIndex() >= pattern.length());
    }

    private ParsePosition next(ParsePosition pos) {
        pos.setIndex(pos.getIndex() + 1);
        return pos;
    }

    private StringBuilder appendQuotedString(String pattern, ParsePosition pos, StringBuilder appendTo) {
        if ($assertionsDisabled || pattern.toCharArray()[pos.getIndex()] == '\'') {
            if (appendTo != null) {
                appendTo.append(QUOTE);
            }
            next(pos);
            int start = pos.getIndex();
            char[] c = pattern.toCharArray();
            int lastHold = start;
            int i = pos.getIndex();
            while (i < pattern.length()) {
                switch (c[pos.getIndex()]) {
                    case '\'':
                        next(pos);
                        if (appendTo == null) {
                            return null;
                        }
                        return appendTo.append(c, lastHold, pos.getIndex() - lastHold);
                    default:
                        next(pos);
                        i++;
                }
            }
            throw new IllegalArgumentException("Unterminated quoted string at position " + start);
        }
        throw new AssertionError("Quoted string must start with quote character");
    }

    private void getQuotedString(String pattern, ParsePosition pos) {
        appendQuotedString(pattern, pos, (StringBuilder) null);
    }

    private boolean containsElements(Collection<?> coll) {
        if (coll == null || coll.isEmpty()) {
            return false;
        }
        for (Object name : coll) {
            if (name != null) {
                return true;
            }
        }
        return false;
    }
}
