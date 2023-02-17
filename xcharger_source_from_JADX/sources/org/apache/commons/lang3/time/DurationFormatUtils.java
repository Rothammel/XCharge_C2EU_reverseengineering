package org.apache.commons.lang3.time;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class DurationFormatUtils {

    /* renamed from: H */
    static final Object f150H = "H";
    public static final String ISO_EXTENDED_FORMAT_PATTERN = "'P'yyyy'Y'M'M'd'DT'H'H'm'M's.SSS'S'";

    /* renamed from: M */
    static final Object f151M = "M";

    /* renamed from: S */
    static final Object f152S = "S";

    /* renamed from: d */
    static final Object f153d = "d";

    /* renamed from: m */
    static final Object f154m = "m";

    /* renamed from: s */
    static final Object f155s = "s";

    /* renamed from: y */
    static final Object f156y = "y";

    public static String formatDurationHMS(long durationMillis) {
        return formatDuration(durationMillis, "HH:mm:ss.SSS");
    }

    public static String formatDurationISO(long durationMillis) {
        return formatDuration(durationMillis, ISO_EXTENDED_FORMAT_PATTERN, false);
    }

    public static String formatDuration(long durationMillis, String format) {
        return formatDuration(durationMillis, format, true);
    }

    public static String formatDuration(long durationMillis, String format, boolean padWithZeros) {
        Validate.inclusiveBetween(0, Long.MAX_VALUE, durationMillis, "durationMillis must not be negative");
        Token[] tokens = lexx(format);
        long days = 0;
        long hours = 0;
        long minutes = 0;
        long seconds = 0;
        long milliseconds = durationMillis;
        if (Token.containsTokenWithValue(tokens, f153d)) {
            days = milliseconds / DateUtils.MILLIS_PER_DAY;
            milliseconds -= DateUtils.MILLIS_PER_DAY * days;
        }
        if (Token.containsTokenWithValue(tokens, f150H)) {
            hours = milliseconds / DateUtils.MILLIS_PER_HOUR;
            milliseconds -= DateUtils.MILLIS_PER_HOUR * hours;
        }
        if (Token.containsTokenWithValue(tokens, f154m)) {
            minutes = milliseconds / 60000;
            milliseconds -= 60000 * minutes;
        }
        if (Token.containsTokenWithValue(tokens, f155s)) {
            seconds = milliseconds / 1000;
            milliseconds -= 1000 * seconds;
        }
        return format(tokens, 0, 0, days, hours, minutes, seconds, milliseconds, padWithZeros);
    }

    public static String formatDurationWords(long durationMillis, boolean suppressLeadingZeroElements, boolean suppressTrailingZeroElements) {
        String duration = formatDuration(durationMillis, "d' days 'H' hours 'm' minutes 's' seconds'");
        if (suppressLeadingZeroElements) {
            duration = StringUtils.SPACE + duration;
            String tmp = StringUtils.replaceOnce(duration, " 0 days", "");
            if (tmp.length() != duration.length()) {
                duration = tmp;
                String tmp2 = StringUtils.replaceOnce(duration, " 0 hours", "");
                if (tmp2.length() != duration.length()) {
                    String tmp3 = StringUtils.replaceOnce(tmp2, " 0 minutes", "");
                    duration = tmp3;
                    if (tmp3.length() != duration.length()) {
                        duration = StringUtils.replaceOnce(tmp3, " 0 seconds", "");
                    }
                }
            }
            if (duration.length() != 0) {
                duration = duration.substring(1);
            }
        }
        if (suppressTrailingZeroElements) {
            String tmp4 = StringUtils.replaceOnce(duration, " 0 seconds", "");
            if (tmp4.length() != duration.length()) {
                duration = tmp4;
                String tmp5 = StringUtils.replaceOnce(duration, " 0 minutes", "");
                if (tmp5.length() != duration.length()) {
                    duration = tmp5;
                    String tmp6 = StringUtils.replaceOnce(duration, " 0 hours", "");
                    if (tmp6.length() != duration.length()) {
                        duration = StringUtils.replaceOnce(tmp6, " 0 days", "");
                    }
                }
            }
        }
        return StringUtils.replaceOnce(StringUtils.replaceOnce(StringUtils.replaceOnce(StringUtils.replaceOnce(StringUtils.SPACE + duration, " 1 seconds", " 1 second"), " 1 minutes", " 1 minute"), " 1 hours", " 1 hour"), " 1 days", " 1 day").trim();
    }

    public static String formatPeriodISO(long startMillis, long endMillis) {
        return formatPeriod(startMillis, endMillis, ISO_EXTENDED_FORMAT_PATTERN, false, TimeZone.getDefault());
    }

    public static String formatPeriod(long startMillis, long endMillis, String format) {
        return formatPeriod(startMillis, endMillis, format, true, TimeZone.getDefault());
    }

    public static String formatPeriod(long startMillis, long endMillis, String format, boolean padWithZeros, TimeZone timezone) {
        int months;
        Validate.isTrue(startMillis <= endMillis, "startMillis must not be greater than endMillis", new Object[0]);
        Token[] tokens = lexx(format);
        Calendar start = Calendar.getInstance(timezone);
        start.setTime(new Date(startMillis));
        Calendar end = Calendar.getInstance(timezone);
        end.setTime(new Date(endMillis));
        int milliseconds = end.get(14) - start.get(14);
        int seconds = end.get(13) - start.get(13);
        int minutes = end.get(12) - start.get(12);
        int hours = end.get(11) - start.get(11);
        int days = end.get(5) - start.get(5);
        int months2 = end.get(2) - start.get(2);
        int years = end.get(1) - start.get(1);
        while (milliseconds < 0) {
            milliseconds += 1000;
            seconds--;
        }
        while (seconds < 0) {
            seconds += 60;
            minutes--;
        }
        while (minutes < 0) {
            minutes += 60;
            hours--;
        }
        while (hours < 0) {
            hours += 24;
            days--;
        }
        if (Token.containsTokenWithValue(tokens, f151M)) {
            while (days < 0) {
                days += start.getActualMaximum(5);
                months2--;
                start.add(2, 1);
            }
            while (months < 0) {
                months2 = months + 12;
                years--;
            }
            if (!Token.containsTokenWithValue(tokens, f156y) && years != 0) {
                while (years != 0) {
                    months += years * 12;
                    years = 0;
                }
            }
        } else {
            if (!Token.containsTokenWithValue(tokens, f156y)) {
                int target = end.get(1);
                if (months2 < 0) {
                    target--;
                }
                while (start.get(1) != target) {
                    int days2 = days + (start.getActualMaximum(6) - start.get(6));
                    if ((start instanceof GregorianCalendar) && start.get(2) == 1 && start.get(5) == 29) {
                        days2++;
                    }
                    start.add(1, 1);
                    days = days2 + start.get(6);
                }
                years = 0;
            }
            while (start.get(2) != end.get(2)) {
                days += start.getActualMaximum(5);
                start.add(2, 1);
            }
            months = 0;
            while (days < 0) {
                days += start.getActualMaximum(5);
                months--;
                start.add(2, 1);
            }
        }
        if (!Token.containsTokenWithValue(tokens, f153d)) {
            hours += days * 24;
            days = 0;
        }
        if (!Token.containsTokenWithValue(tokens, f150H)) {
            minutes += hours * 60;
            hours = 0;
        }
        if (!Token.containsTokenWithValue(tokens, f154m)) {
            seconds += minutes * 60;
            minutes = 0;
        }
        if (!Token.containsTokenWithValue(tokens, f155s)) {
            milliseconds += seconds * 1000;
            seconds = 0;
        }
        return format(tokens, (long) years, (long) months, (long) days, (long) hours, (long) minutes, (long) seconds, (long) milliseconds, padWithZeros);
    }

    static String format(Token[] tokens, long years, long months, long days, long hours, long minutes, long seconds, long milliseconds, boolean padWithZeros) {
        StringBuilder buffer = new StringBuilder();
        boolean lastOutputSeconds = false;
        for (Token token : tokens) {
            Object value = token.getValue();
            int count = token.getCount();
            if (value instanceof StringBuilder) {
                buffer.append(value.toString());
            } else if (value == f156y) {
                buffer.append(paddedValue(years, padWithZeros, count));
                lastOutputSeconds = false;
            } else if (value == f151M) {
                buffer.append(paddedValue(months, padWithZeros, count));
                lastOutputSeconds = false;
            } else if (value == f153d) {
                buffer.append(paddedValue(days, padWithZeros, count));
                lastOutputSeconds = false;
            } else if (value == f150H) {
                buffer.append(paddedValue(hours, padWithZeros, count));
                lastOutputSeconds = false;
            } else if (value == f154m) {
                buffer.append(paddedValue(minutes, padWithZeros, count));
                lastOutputSeconds = false;
            } else if (value == f155s) {
                buffer.append(paddedValue(seconds, padWithZeros, count));
                lastOutputSeconds = true;
            } else if (value == f152S) {
                if (lastOutputSeconds) {
                    buffer.append(paddedValue(milliseconds, true, padWithZeros ? Math.max(3, count) : 3));
                } else {
                    buffer.append(paddedValue(milliseconds, padWithZeros, count));
                }
                lastOutputSeconds = false;
            }
        }
        return buffer.toString();
    }

    private static String paddedValue(long value, boolean padWithZeros, int count) {
        String longString = Long.toString(value);
        return padWithZeros ? StringUtils.leftPad(longString, count, '0') : longString;
    }

    static Token[] lexx(String format) {
        ArrayList<Token> list = new ArrayList<>(format.length());
        boolean inLiteral = false;
        StringBuilder buffer = null;
        Token previous = null;
        for (int i = 0; i < format.length(); i++) {
            char ch = format.charAt(i);
            if (!inLiteral || ch == '\'') {
                Object value = null;
                switch (ch) {
                    case '\'':
                        if (!inLiteral) {
                            buffer = new StringBuilder();
                            list.add(new Token(buffer));
                            inLiteral = true;
                            break;
                        } else {
                            buffer = null;
                            inLiteral = false;
                            break;
                        }
                    case 'H':
                        value = f150H;
                        break;
                    case 'M':
                        value = f151M;
                        break;
                    case 'S':
                        value = f152S;
                        break;
                    case 'd':
                        value = f153d;
                        break;
                    case 'm':
                        value = f154m;
                        break;
                    case 's':
                        value = f155s;
                        break;
                    case 'y':
                        value = f156y;
                        break;
                    default:
                        if (buffer == null) {
                            buffer = new StringBuilder();
                            list.add(new Token(buffer));
                        }
                        buffer.append(ch);
                        break;
                }
                if (value != null) {
                    if (previous == null || previous.getValue() != value) {
                        Token token = new Token(value);
                        list.add(token);
                        previous = token;
                    } else {
                        previous.increment();
                    }
                    buffer = null;
                }
            } else {
                buffer.append(ch);
            }
        }
        if (!inLiteral) {
            return (Token[]) list.toArray(new Token[list.size()]);
        }
        throw new IllegalArgumentException("Unmatched quote in format: " + format);
    }

    static class Token {
        private int count;
        private final Object value;

        static boolean containsTokenWithValue(Token[] tokens, Object value2) {
            for (Token token : tokens) {
                if (token.getValue() == value2) {
                    return true;
                }
            }
            return false;
        }

        Token(Object value2) {
            this.value = value2;
            this.count = 1;
        }

        Token(Object value2, int count2) {
            this.value = value2;
            this.count = count2;
        }

        /* access modifiers changed from: package-private */
        public void increment() {
            this.count++;
        }

        /* access modifiers changed from: package-private */
        public int getCount() {
            return this.count;
        }

        /* access modifiers changed from: package-private */
        public Object getValue() {
            return this.value;
        }

        public boolean equals(Object obj2) {
            if (!(obj2 instanceof Token)) {
                return false;
            }
            Token tok2 = (Token) obj2;
            if (this.value.getClass() != tok2.value.getClass() || this.count != tok2.count) {
                return false;
            }
            if (this.value instanceof StringBuilder) {
                return this.value.toString().equals(tok2.value.toString());
            }
            if (this.value instanceof Number) {
                return this.value.equals(tok2.value);
            }
            if (this.value == tok2.value) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return this.value.hashCode();
        }

        public String toString() {
            return StringUtils.repeat(this.value.toString(), this.count);
        }
    }
}
