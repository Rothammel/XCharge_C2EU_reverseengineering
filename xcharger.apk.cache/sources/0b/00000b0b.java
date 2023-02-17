package org.apache.commons.lang3.text;

import it.sauronsoftware.ftp4j.FTPCodes;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;

/* loaded from: classes.dex */
public class ExtendedMessageFormat extends MessageFormat {
    static final /* synthetic */ boolean $assertionsDisabled;
    private static final String DUMMY_PATTERN = "";
    private static final char END_FE = '}';
    private static final int HASH_SEED = 31;
    private static final char QUOTE = '\'';
    private static final char START_FE = '{';
    private static final char START_FMT = ',';
    private static final long serialVersionUID = -2362048321261811743L;
    private final Map<String, ? extends FormatFactory> registry;
    private String toPattern;

    static {
        $assertionsDisabled = !ExtendedMessageFormat.class.desiredAssertionStatus();
    }

    public ExtendedMessageFormat(String pattern) {
        this(pattern, Locale.getDefault());
    }

    public ExtendedMessageFormat(String pattern, Locale locale) {
        this(pattern, locale, null);
    }

    public ExtendedMessageFormat(String pattern, Map<String, ? extends FormatFactory> registry) {
        this(pattern, Locale.getDefault(), registry);
    }

    public ExtendedMessageFormat(String pattern, Locale locale, Map<String, ? extends FormatFactory> registry) {
        super("");
        setLocale(locale);
        this.registry = registry;
        applyPattern(pattern);
    }

    @Override // java.text.MessageFormat
    public String toPattern() {
        return this.toPattern;
    }

    @Override // java.text.MessageFormat
    public final void applyPattern(String pattern) {
        if (this.registry == null) {
            super.applyPattern(pattern);
            this.toPattern = super.toPattern();
            return;
        }
        ArrayList<Format> foundFormats = new ArrayList<>();
        ArrayList<String> foundDescriptions = new ArrayList<>();
        StringBuilder stripCustom = new StringBuilder(pattern.length());
        ParsePosition pos = new ParsePosition(0);
        char[] c = pattern.toCharArray();
        int fmtCount = 0;
        while (pos.getIndex() < pattern.length()) {
            switch (c[pos.getIndex()]) {
                case '\'':
                    appendQuotedString(pattern, pos, stripCustom);
                    continue;
                case '{':
                    fmtCount++;
                    seekNonWs(pattern, pos);
                    int start = pos.getIndex();
                    int index = readArgumentIndex(pattern, next(pos));
                    stripCustom.append(START_FE).append(index);
                    seekNonWs(pattern, pos);
                    Format format = null;
                    String formatDescription = null;
                    if (c[pos.getIndex()] == ',' && (format = getFormat((formatDescription = parseFormatDescription(pattern, next(pos))))) == null) {
                        stripCustom.append(START_FMT).append(formatDescription);
                    }
                    foundFormats.add(format);
                    if (format == null) {
                        formatDescription = null;
                    }
                    foundDescriptions.add(formatDescription);
                    Validate.isTrue(foundFormats.size() == fmtCount);
                    Validate.isTrue(foundDescriptions.size() == fmtCount);
                    if (c[pos.getIndex()] != '}') {
                        throw new IllegalArgumentException("Unreadable format element at position " + start);
                    }
                    break;
            }
            stripCustom.append(c[pos.getIndex()]);
            next(pos);
        }
        super.applyPattern(stripCustom.toString());
        this.toPattern = insertFormats(super.toPattern(), foundDescriptions);
        if (containsElements(foundFormats)) {
            Format[] origFormats = getFormats();
            int i = 0;
            Iterator<Format> it2 = foundFormats.iterator();
            while (it2.hasNext()) {
                Format f = it2.next();
                if (f != null) {
                    origFormats[i] = f;
                }
                i++;
            }
            super.setFormats(origFormats);
        }
    }

    @Override // java.text.MessageFormat
    public void setFormat(int formatElementIndex, Format newFormat) {
        throw new UnsupportedOperationException();
    }

    @Override // java.text.MessageFormat
    public void setFormatByArgumentIndex(int argumentIndex, Format newFormat) {
        throw new UnsupportedOperationException();
    }

    @Override // java.text.MessageFormat
    public void setFormats(Format[] newFormats) {
        throw new UnsupportedOperationException();
    }

    @Override // java.text.MessageFormat
    public void setFormatsByArgumentIndex(Format[] newFormats) {
        throw new UnsupportedOperationException();
    }

    @Override // java.text.MessageFormat
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && super.equals(obj) && !ObjectUtils.notEqual(getClass(), obj.getClass())) {
            ExtendedMessageFormat rhs = (ExtendedMessageFormat) obj;
            return (ObjectUtils.notEqual(this.toPattern, rhs.toPattern) || ObjectUtils.notEqual(this.registry, rhs.registry)) ? false : true;
        }
        return false;
    }

    @Override // java.text.MessageFormat
    public int hashCode() {
        int result = super.hashCode();
        return (((result * HASH_SEED) + ObjectUtils.hashCode(this.registry)) * HASH_SEED) + ObjectUtils.hashCode(this.toPattern);
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
            FormatFactory factory = this.registry.get(name);
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
                if (c != ',' && c != '}') {
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
                case FTPCodes.DATA_CONNECTION_ALREADY_OPEN /* 125 */:
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

    private String insertFormats(String pattern, ArrayList<String> customPatterns) {
        if (containsElements(customPatterns)) {
            StringBuilder sb = new StringBuilder(pattern.length() * 2);
            ParsePosition pos = new ParsePosition(0);
            int fe = -1;
            int depth = 0;
            while (pos.getIndex() < pattern.length()) {
                char c = pattern.charAt(pos.getIndex());
                switch (c) {
                    case '\'':
                        appendQuotedString(pattern, pos, sb);
                        continue;
                    case '{':
                        depth++;
                        sb.append(START_FE).append(readArgumentIndex(pattern, next(pos)));
                        if (depth == 1) {
                            fe++;
                            String customPattern = customPatterns.get(fe);
                            if (customPattern != null) {
                                sb.append(START_FMT).append(customPattern);
                            }
                        } else {
                            continue;
                        }
                    case FTPCodes.DATA_CONNECTION_ALREADY_OPEN /* 125 */:
                        depth--;
                        break;
                }
                sb.append(c);
                next(pos);
            }
            return sb.toString();
        }
        return pattern;
    }

    private void seekNonWs(String pattern, ParsePosition pos) {
        char[] buffer = pattern.toCharArray();
        do {
            int len = StrMatcher.splitMatcher().isMatch(buffer, pos.getIndex());
            pos.setIndex(pos.getIndex() + len);
            if (len <= 0) {
                return;
            }
        } while (pos.getIndex() < pattern.length());
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
            for (int i = pos.getIndex(); i < pattern.length(); i++) {
                switch (c[pos.getIndex()]) {
                    case '\'':
                        next(pos);
                        if (appendTo == null) {
                            return null;
                        }
                        return appendTo.append(c, start, pos.getIndex() - start);
                    default:
                        next(pos);
                }
            }
            throw new IllegalArgumentException("Unterminated quoted string at position " + start);
        }
        throw new AssertionError("Quoted string must start with quote character");
    }

    private void getQuotedString(String pattern, ParsePosition pos) {
        appendQuotedString(pattern, pos, null);
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