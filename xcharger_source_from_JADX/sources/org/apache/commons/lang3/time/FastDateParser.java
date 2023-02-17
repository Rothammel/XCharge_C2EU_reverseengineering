package org.apache.commons.lang3.time;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FastDateParser implements DateParser, Serializable {
    private static final Strategy ABBREVIATED_YEAR_STRATEGY = new NumberStrategy(1) {
        /* access modifiers changed from: package-private */
        public void setCalendar(FastDateParser parser, Calendar cal, String value) {
            int iValue = Integer.parseInt(value);
            if (iValue < 100) {
                iValue = parser.adjustYear(iValue);
            }
            cal.set(1, iValue);
        }
    };
    private static final Strategy DAY_OF_MONTH_STRATEGY = new NumberStrategy(5);
    private static final Strategy DAY_OF_WEEK_IN_MONTH_STRATEGY = new NumberStrategy(8);
    private static final Strategy DAY_OF_YEAR_STRATEGY = new NumberStrategy(6);
    private static final Strategy HOUR12_STRATEGY = new NumberStrategy(10) {
        /* access modifiers changed from: package-private */
        public int modify(int iValue) {
            if (iValue == 12) {
                return 0;
            }
            return iValue;
        }
    };
    private static final Strategy HOUR24_OF_DAY_STRATEGY = new NumberStrategy(11) {
        /* access modifiers changed from: package-private */
        public int modify(int iValue) {
            if (iValue == 24) {
                return 0;
            }
            return iValue;
        }
    };
    private static final Strategy HOUR_OF_DAY_STRATEGY = new NumberStrategy(11);
    private static final Strategy HOUR_STRATEGY = new NumberStrategy(10);
    private static final Strategy ISO_8601_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}(?::?\\d{2})?))");
    static final Locale JAPANESE_IMPERIAL = new Locale("ja", "JP", "JP");
    private static final Strategy LITERAL_YEAR_STRATEGY = new NumberStrategy(1);
    private static final Strategy MILLISECOND_STRATEGY = new NumberStrategy(14);
    private static final Strategy MINUTE_STRATEGY = new NumberStrategy(12);
    private static final Strategy NUMBER_MONTH_STRATEGY = new NumberStrategy(2) {
        /* access modifiers changed from: package-private */
        public int modify(int iValue) {
            return iValue - 1;
        }
    };
    private static final Strategy SECOND_STRATEGY = new NumberStrategy(13);
    private static final Strategy WEEK_OF_MONTH_STRATEGY = new NumberStrategy(4);
    private static final Strategy WEEK_OF_YEAR_STRATEGY = new NumberStrategy(3);
    private static final ConcurrentMap<Locale, Strategy>[] caches = new ConcurrentMap[17];
    private static final Pattern formatPattern = Pattern.compile("D+|E+|F+|G+|H+|K+|M+|S+|W+|X+|Z+|a+|d+|h+|k+|m+|s+|w+|y+|z+|''|'[^']++(''[^']*+)*+'|[^'A-Za-z]++");
    private static final long serialVersionUID = 2;
    private final int century;
    private transient String currentFormatField;
    private final Locale locale;
    private transient Strategy nextStrategy;
    private transient Pattern parsePattern;
    private final String pattern;
    private final int startYear;
    private transient Strategy[] strategies;
    private final TimeZone timeZone;

    protected FastDateParser(String pattern2, TimeZone timeZone2, Locale locale2) {
        this(pattern2, timeZone2, locale2, (Date) null);
    }

    protected FastDateParser(String pattern2, TimeZone timeZone2, Locale locale2, Date centuryStart) {
        int centuryStartYear;
        this.pattern = pattern2;
        this.timeZone = timeZone2;
        this.locale = locale2;
        Calendar definingCalendar = Calendar.getInstance(timeZone2, locale2);
        if (centuryStart != null) {
            definingCalendar.setTime(centuryStart);
            centuryStartYear = definingCalendar.get(1);
        } else if (locale2.equals(JAPANESE_IMPERIAL)) {
            centuryStartYear = 0;
        } else {
            definingCalendar.setTime(new Date());
            centuryStartYear = definingCalendar.get(1) - 80;
        }
        this.century = (centuryStartYear / 100) * 100;
        this.startYear = centuryStartYear - this.century;
        init(definingCalendar);
    }

    private void init(Calendar definingCalendar) {
        StringBuilder regex = new StringBuilder();
        List<Strategy> collector = new ArrayList<>();
        Matcher patternMatcher = formatPattern.matcher(this.pattern);
        if (!patternMatcher.lookingAt()) {
            throw new IllegalArgumentException("Illegal pattern character '" + this.pattern.charAt(patternMatcher.regionStart()) + "'");
        }
        this.currentFormatField = patternMatcher.group();
        Strategy currentStrategy = getStrategy(this.currentFormatField, definingCalendar);
        while (true) {
            patternMatcher.region(patternMatcher.end(), patternMatcher.regionEnd());
            if (!patternMatcher.lookingAt()) {
                break;
            }
            String nextFormatField = patternMatcher.group();
            this.nextStrategy = getStrategy(nextFormatField, definingCalendar);
            if (currentStrategy.addRegex(this, regex)) {
                collector.add(currentStrategy);
            }
            this.currentFormatField = nextFormatField;
            currentStrategy = this.nextStrategy;
        }
        this.nextStrategy = null;
        if (patternMatcher.regionStart() != patternMatcher.regionEnd()) {
            throw new IllegalArgumentException("Failed to parse \"" + this.pattern + "\" ; gave up at index " + patternMatcher.regionStart());
        }
        if (currentStrategy.addRegex(this, regex)) {
            collector.add(currentStrategy);
        }
        this.currentFormatField = null;
        this.strategies = (Strategy[]) collector.toArray(new Strategy[collector.size()]);
        this.parsePattern = Pattern.compile(regex.toString());
    }

    public String getPattern() {
        return this.pattern;
    }

    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    public Locale getLocale() {
        return this.locale;
    }

    /* access modifiers changed from: package-private */
    public Pattern getParsePattern() {
        return this.parsePattern;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof FastDateParser)) {
            return false;
        }
        FastDateParser other = (FastDateParser) obj;
        if (!this.pattern.equals(other.pattern) || !this.timeZone.equals(other.timeZone) || !this.locale.equals(other.locale)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return this.pattern.hashCode() + ((this.timeZone.hashCode() + (this.locale.hashCode() * 13)) * 13);
    }

    public String toString() {
        return "FastDateParser[" + this.pattern + "," + this.locale + "," + this.timeZone.getID() + "]";
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        init(Calendar.getInstance(this.timeZone, this.locale));
    }

    public Object parseObject(String source) throws ParseException {
        return parse(source);
    }

    public Date parse(String source) throws ParseException {
        Date date = parse(source, new ParsePosition(0));
        if (date != null) {
            return date;
        }
        if (this.locale.equals(JAPANESE_IMPERIAL)) {
            throw new ParseException("(The " + this.locale + " locale does not support dates before 1868 AD)\n" + "Unparseable date: \"" + source + "\" does not match " + this.parsePattern.pattern(), 0);
        }
        throw new ParseException("Unparseable date: \"" + source + "\" does not match " + this.parsePattern.pattern(), 0);
    }

    public Object parseObject(String source, ParsePosition pos) {
        return parse(source, pos);
    }

    public Date parse(String source, ParsePosition pos) {
        int offset = pos.getIndex();
        Matcher matcher = this.parsePattern.matcher(source.substring(offset));
        if (!matcher.lookingAt()) {
            return null;
        }
        Calendar cal = Calendar.getInstance(this.timeZone, this.locale);
        cal.clear();
        int i = 0;
        while (i < this.strategies.length) {
            int i2 = i + 1;
            this.strategies[i].setCalendar(this, cal, matcher.group(i2));
            i = i2;
        }
        pos.setIndex(matcher.end() + offset);
        return cal.getTime();
    }

    /* access modifiers changed from: private */
    public static StringBuilder escapeRegex(StringBuilder regex, String value, boolean unquote) {
        char c;
        regex.append("\\Q");
        int i = 0;
        while (true) {
            if (i < value.length()) {
                c = value.charAt(i);
                switch (c) {
                    case '\'':
                        if (unquote) {
                            i++;
                            if (i != value.length()) {
                                c = value.charAt(i);
                                break;
                            } else {
                                break;
                            }
                        } else {
                            continue;
                        }
                    case '\\':
                        i++;
                        if (i != value.length()) {
                            regex.append(c);
                            c = value.charAt(i);
                            if (c != 'E') {
                                break;
                            } else {
                                regex.append("E\\\\E\\");
                                c = 'Q';
                                break;
                            }
                        } else {
                            continue;
                        }
                }
            } else {
                regex.append("\\E");
            }
            regex.append(c);
            i++;
        }
        return regex;
    }

    /* access modifiers changed from: private */
    public static Map<String, Integer> getDisplayNames(int field, Calendar definingCalendar, Locale locale2) {
        return definingCalendar.getDisplayNames(field, 0, locale2);
    }

    /* access modifiers changed from: private */
    public int adjustYear(int twoDigitYear) {
        int trial = this.century + twoDigitYear;
        return twoDigitYear >= this.startYear ? trial : trial + 100;
    }

    /* access modifiers changed from: package-private */
    public boolean isNextNumber() {
        return this.nextStrategy != null && this.nextStrategy.isNumber();
    }

    /* access modifiers changed from: package-private */
    public int getFieldWidth() {
        return this.currentFormatField.length();
    }

    private static abstract class Strategy {
        /* access modifiers changed from: package-private */
        public abstract boolean addRegex(FastDateParser fastDateParser, StringBuilder sb);

        private Strategy() {
        }

        /* access modifiers changed from: package-private */
        public boolean isNumber() {
            return false;
        }

        /* access modifiers changed from: package-private */
        public void setCalendar(FastDateParser parser, Calendar cal, String value) {
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        return new org.apache.commons.lang3.time.FastDateParser.CopyQuotedStrategy(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:?, code lost:
        return getLocaleSpecificStrategy(15, r5);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private org.apache.commons.lang3.time.FastDateParser.Strategy getStrategy(java.lang.String r4, java.util.Calendar r5) {
        /*
            r3 = this;
            r1 = 0
            r2 = 2
            char r0 = r4.charAt(r1)
            switch(r0) {
                case 39: goto L_0x000f;
                case 68: goto L_0x0026;
                case 69: goto L_0x0029;
                case 70: goto L_0x002f;
                case 71: goto L_0x0032;
                case 72: goto L_0x0037;
                case 75: goto L_0x003a;
                case 77: goto L_0x003d;
                case 83: goto L_0x004c;
                case 87: goto L_0x004f;
                case 88: goto L_0x0077;
                case 90: goto L_0x0080;
                case 97: goto L_0x0052;
                case 100: goto L_0x0059;
                case 104: goto L_0x005c;
                case 107: goto L_0x005f;
                case 109: goto L_0x0062;
                case 115: goto L_0x0065;
                case 119: goto L_0x0068;
                case 121: goto L_0x006b;
                case 122: goto L_0x008b;
                default: goto L_0x0009;
            }
        L_0x0009:
            org.apache.commons.lang3.time.FastDateParser$CopyQuotedStrategy r0 = new org.apache.commons.lang3.time.FastDateParser$CopyQuotedStrategy
            r0.<init>(r4)
        L_0x000e:
            return r0
        L_0x000f:
            int r0 = r4.length()
            if (r0 <= r2) goto L_0x0009
            org.apache.commons.lang3.time.FastDateParser$CopyQuotedStrategy r0 = new org.apache.commons.lang3.time.FastDateParser$CopyQuotedStrategy
            r1 = 1
            int r2 = r4.length()
            int r2 = r2 + -1
            java.lang.String r1 = r4.substring(r1, r2)
            r0.<init>(r1)
            goto L_0x000e
        L_0x0026:
            org.apache.commons.lang3.time.FastDateParser$Strategy r0 = DAY_OF_YEAR_STRATEGY
            goto L_0x000e
        L_0x0029:
            r0 = 7
            org.apache.commons.lang3.time.FastDateParser$Strategy r0 = r3.getLocaleSpecificStrategy(r0, r5)
            goto L_0x000e
        L_0x002f:
            org.apache.commons.lang3.time.FastDateParser$Strategy r0 = DAY_OF_WEEK_IN_MONTH_STRATEGY
            goto L_0x000e
        L_0x0032:
            org.apache.commons.lang3.time.FastDateParser$Strategy r0 = r3.getLocaleSpecificStrategy(r1, r5)
            goto L_0x000e
        L_0x0037:
            org.apache.commons.lang3.time.FastDateParser$Strategy r0 = HOUR_OF_DAY_STRATEGY
            goto L_0x000e
        L_0x003a:
            org.apache.commons.lang3.time.FastDateParser$Strategy r0 = HOUR_STRATEGY
            goto L_0x000e
        L_0x003d:
            int r0 = r4.length()
            r1 = 3
            if (r0 < r1) goto L_0x0049
            org.apache.commons.lang3.time.FastDateParser$Strategy r0 = r3.getLocaleSpecificStrategy(r2, r5)
            goto L_0x000e
        L_0x0049:
            org.apache.commons.lang3.time.FastDateParser$Strategy r0 = NUMBER_MONTH_STRATEGY
            goto L_0x000e
        L_0x004c:
            org.apache.commons.lang3.time.FastDateParser$Strategy r0 = MILLISECOND_STRATEGY
            goto L_0x000e
        L_0x004f:
            org.apache.commons.lang3.time.FastDateParser$Strategy r0 = WEEK_OF_MONTH_STRATEGY
            goto L_0x000e
        L_0x0052:
            r0 = 9
            org.apache.commons.lang3.time.FastDateParser$Strategy r0 = r3.getLocaleSpecificStrategy(r0, r5)
            goto L_0x000e
        L_0x0059:
            org.apache.commons.lang3.time.FastDateParser$Strategy r0 = DAY_OF_MONTH_STRATEGY
            goto L_0x000e
        L_0x005c:
            org.apache.commons.lang3.time.FastDateParser$Strategy r0 = HOUR12_STRATEGY
            goto L_0x000e
        L_0x005f:
            org.apache.commons.lang3.time.FastDateParser$Strategy r0 = HOUR24_OF_DAY_STRATEGY
            goto L_0x000e
        L_0x0062:
            org.apache.commons.lang3.time.FastDateParser$Strategy r0 = MINUTE_STRATEGY
            goto L_0x000e
        L_0x0065:
            org.apache.commons.lang3.time.FastDateParser$Strategy r0 = SECOND_STRATEGY
            goto L_0x000e
        L_0x0068:
            org.apache.commons.lang3.time.FastDateParser$Strategy r0 = WEEK_OF_YEAR_STRATEGY
            goto L_0x000e
        L_0x006b:
            int r0 = r4.length()
            if (r0 <= r2) goto L_0x0074
            org.apache.commons.lang3.time.FastDateParser$Strategy r0 = LITERAL_YEAR_STRATEGY
            goto L_0x000e
        L_0x0074:
            org.apache.commons.lang3.time.FastDateParser$Strategy r0 = ABBREVIATED_YEAR_STRATEGY
            goto L_0x000e
        L_0x0077:
            int r0 = r4.length()
            org.apache.commons.lang3.time.FastDateParser$Strategy r0 = org.apache.commons.lang3.time.FastDateParser.ISO8601TimeZoneStrategy.getStrategy(r0)
            goto L_0x000e
        L_0x0080:
            java.lang.String r0 = "ZZ"
            boolean r0 = r4.equals(r0)
            if (r0 == 0) goto L_0x008b
            org.apache.commons.lang3.time.FastDateParser$Strategy r0 = ISO_8601_STRATEGY
            goto L_0x000e
        L_0x008b:
            r0 = 15
            org.apache.commons.lang3.time.FastDateParser$Strategy r0 = r3.getLocaleSpecificStrategy(r0, r5)
            goto L_0x000e
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.lang3.time.FastDateParser.getStrategy(java.lang.String, java.util.Calendar):org.apache.commons.lang3.time.FastDateParser$Strategy");
    }

    private static ConcurrentMap<Locale, Strategy> getCache(int field) {
        ConcurrentMap<Locale, Strategy> concurrentMap;
        synchronized (caches) {
            if (caches[field] == null) {
                caches[field] = new ConcurrentHashMap(3);
            }
            concurrentMap = caches[field];
        }
        return concurrentMap;
    }

    private Strategy getLocaleSpecificStrategy(int field, Calendar definingCalendar) {
        ConcurrentMap<Locale, Strategy> cache = getCache(field);
        Strategy strategy = (Strategy) cache.get(this.locale);
        if (strategy == null) {
            strategy = field == 15 ? new TimeZoneStrategy(this.locale) : new CaseInsensitiveTextStrategy(field, definingCalendar, this.locale);
            Strategy inCache = cache.putIfAbsent(this.locale, strategy);
            if (inCache != null) {
                return inCache;
            }
        }
        return strategy;
    }

    private static class CopyQuotedStrategy extends Strategy {
        private final String formatField;

        CopyQuotedStrategy(String formatField2) {
            super();
            this.formatField = formatField2;
        }

        /* access modifiers changed from: package-private */
        public boolean isNumber() {
            char c = this.formatField.charAt(0);
            if (c == '\'') {
                c = this.formatField.charAt(1);
            }
            return Character.isDigit(c);
        }

        /* access modifiers changed from: package-private */
        public boolean addRegex(FastDateParser parser, StringBuilder regex) {
            StringBuilder unused = FastDateParser.escapeRegex(regex, this.formatField, true);
            return false;
        }
    }

    private static class CaseInsensitiveTextStrategy extends Strategy {
        private final int field;
        private final Map<String, Integer> lKeyValues = new HashMap();
        private final Locale locale;

        CaseInsensitiveTextStrategy(int field2, Calendar definingCalendar, Locale locale2) {
            super();
            this.field = field2;
            this.locale = locale2;
            Map<String, Integer> keyValues = FastDateParser.getDisplayNames(field2, definingCalendar, locale2);
            for (Map.Entry<String, Integer> entry : keyValues.entrySet()) {
                this.lKeyValues.put(entry.getKey().toLowerCase(locale2), entry.getValue());
            }
        }

        /* access modifiers changed from: package-private */
        public boolean addRegex(FastDateParser parser, StringBuilder regex) {
            regex.append("((?iu)");
            for (String textKeyValue : this.lKeyValues.keySet()) {
                FastDateParser.escapeRegex(regex, textKeyValue, false).append('|');
            }
            regex.setCharAt(regex.length() - 1, ')');
            return true;
        }

        /* access modifiers changed from: package-private */
        public void setCalendar(FastDateParser parser, Calendar cal, String value) {
            Integer iVal = this.lKeyValues.get(value.toLowerCase(this.locale));
            if (iVal == null) {
                StringBuilder sb = new StringBuilder(value);
                sb.append(" not in (");
                for (String textKeyValue : this.lKeyValues.keySet()) {
                    sb.append(textKeyValue).append(TokenParser.f168SP);
                }
                sb.setCharAt(sb.length() - 1, ')');
                throw new IllegalArgumentException(sb.toString());
            }
            cal.set(this.field, iVal.intValue());
        }
    }

    private static class NumberStrategy extends Strategy {
        private final int field;

        NumberStrategy(int field2) {
            super();
            this.field = field2;
        }

        /* access modifiers changed from: package-private */
        public boolean isNumber() {
            return true;
        }

        /* access modifiers changed from: package-private */
        public boolean addRegex(FastDateParser parser, StringBuilder regex) {
            if (parser.isNextNumber()) {
                regex.append("(\\p{Nd}{").append(parser.getFieldWidth()).append("}+)");
                return true;
            }
            regex.append("(\\p{Nd}++)");
            return true;
        }

        /* access modifiers changed from: package-private */
        public void setCalendar(FastDateParser parser, Calendar cal, String value) {
            cal.set(this.field, modify(Integer.parseInt(value)));
        }

        /* access modifiers changed from: package-private */
        public int modify(int iValue) {
            return iValue;
        }
    }

    private static class TimeZoneStrategy extends Strategy {

        /* renamed from: ID */
        private static final int f157ID = 0;
        private static final int LONG_DST = 3;
        private static final int LONG_STD = 1;
        private static final int SHORT_DST = 4;
        private static final int SHORT_STD = 2;
        private final SortedMap<String, TimeZone> tzNames = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        private final String validTimeZoneChars;

        TimeZoneStrategy(Locale locale) {
            super();
            for (String[] zone : DateFormatSymbols.getInstance(locale).getZoneStrings()) {
                if (!zone[0].startsWith("GMT")) {
                    TimeZone tz = TimeZone.getTimeZone(zone[0]);
                    if (!this.tzNames.containsKey(zone[1])) {
                        this.tzNames.put(zone[1], tz);
                    }
                    if (!this.tzNames.containsKey(zone[2])) {
                        this.tzNames.put(zone[2], tz);
                    }
                    if (tz.useDaylightTime()) {
                        if (!this.tzNames.containsKey(zone[3])) {
                            this.tzNames.put(zone[3], tz);
                        }
                        if (!this.tzNames.containsKey(zone[4])) {
                            this.tzNames.put(zone[4], tz);
                        }
                    }
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("(GMT[+-]\\d{1,2}:\\d{2}").append('|');
            sb.append("[+-]\\d{4}").append('|');
            for (String id : this.tzNames.keySet()) {
                FastDateParser.escapeRegex(sb, id, false).append('|');
            }
            sb.setCharAt(sb.length() - 1, ')');
            this.validTimeZoneChars = sb.toString();
        }

        /* access modifiers changed from: package-private */
        public boolean addRegex(FastDateParser parser, StringBuilder regex) {
            regex.append(this.validTimeZoneChars);
            return true;
        }

        /* access modifiers changed from: package-private */
        public void setCalendar(FastDateParser parser, Calendar cal, String value) {
            TimeZone tz;
            if (value.charAt(0) == '+' || value.charAt(0) == '-') {
                tz = TimeZone.getTimeZone("GMT" + value);
            } else if (value.startsWith("GMT")) {
                tz = TimeZone.getTimeZone(value);
            } else {
                tz = (TimeZone) this.tzNames.get(value);
                if (tz == null) {
                    throw new IllegalArgumentException(value + " is not a supported timezone name");
                }
            }
            cal.setTimeZone(tz);
        }
    }

    private static class ISO8601TimeZoneStrategy extends Strategy {
        private static final Strategy ISO_8601_1_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}))");
        private static final Strategy ISO_8601_2_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}\\d{2}))");
        private static final Strategy ISO_8601_3_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}(?::)\\d{2}))");
        private final String pattern;

        ISO8601TimeZoneStrategy(String pattern2) {
            super();
            this.pattern = pattern2;
        }

        /* access modifiers changed from: package-private */
        public boolean addRegex(FastDateParser parser, StringBuilder regex) {
            regex.append(this.pattern);
            return true;
        }

        /* access modifiers changed from: package-private */
        public void setCalendar(FastDateParser parser, Calendar cal, String value) {
            if (value.equals("Z")) {
                cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            } else {
                cal.setTimeZone(TimeZone.getTimeZone("GMT" + value));
            }
        }

        static Strategy getStrategy(int tokenLen) {
            switch (tokenLen) {
                case 1:
                    return ISO_8601_1_STRATEGY;
                case 2:
                    return ISO_8601_2_STRATEGY;
                case 3:
                    return ISO_8601_3_STRATEGY;
                default:
                    throw new IllegalArgumentException("invalid number of X");
            }
        }
    }
}
