package org.apache.http.conn.ssl;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.annotation.Immutable;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.ParserCursor;
import org.apache.http.util.CharArrayBuffer;

@Immutable
final class DistinguishedNameParser {
    private static final BitSet COMMA_OR_PLUS = TokenParser.INIT_BITSET(44, 43);
    private static final BitSet EQUAL_OR_COMMA_OR_PLUS = TokenParser.INIT_BITSET(61, 44, 43);
    public static final DistinguishedNameParser INSTANCE = new DistinguishedNameParser();
    private final TokenParser tokenParser = new InternalTokenParser();

    DistinguishedNameParser() {
    }

    /* access modifiers changed from: package-private */
    public String parseToken(CharArrayBuffer buf, ParserCursor cursor, BitSet delimiters) {
        return this.tokenParser.parseToken(buf, cursor, delimiters);
    }

    /* access modifiers changed from: package-private */
    public String parseValue(CharArrayBuffer buf, ParserCursor cursor, BitSet delimiters) {
        return this.tokenParser.parseValue(buf, cursor, delimiters);
    }

    /* access modifiers changed from: package-private */
    public NameValuePair parseParameter(CharArrayBuffer buf, ParserCursor cursor) {
        String name = parseToken(buf, cursor, EQUAL_OR_COMMA_OR_PLUS);
        if (cursor.atEnd()) {
            return new BasicNameValuePair(name, (String) null);
        }
        int delim = buf.charAt(cursor.getPos());
        cursor.updatePos(cursor.getPos() + 1);
        if (delim == 44) {
            return new BasicNameValuePair(name, (String) null);
        }
        String value = parseValue(buf, cursor, COMMA_OR_PLUS);
        if (!cursor.atEnd()) {
            cursor.updatePos(cursor.getPos() + 1);
        }
        return new BasicNameValuePair(name, value);
    }

    public List<NameValuePair> parse(CharArrayBuffer buf, ParserCursor cursor) {
        List<NameValuePair> params = new ArrayList<>();
        this.tokenParser.skipWhiteSpace(buf, cursor);
        while (!cursor.atEnd()) {
            params.add(parseParameter(buf, cursor));
        }
        return params;
    }

    public List<NameValuePair> parse(String s) {
        if (s == null) {
            return null;
        }
        CharArrayBuffer buffer = new CharArrayBuffer(s.length());
        buffer.append(s);
        return parse(buffer, new ParserCursor(0, s.length()));
    }

    static class InternalTokenParser extends TokenParser {
        InternalTokenParser() {
        }

        public void copyUnquotedContent(CharArrayBuffer buf, ParserCursor cursor, BitSet delimiters, StringBuilder dst) {
            int pos = cursor.getPos();
            int indexFrom = cursor.getPos();
            int indexTo = cursor.getUpperBound();
            boolean escaped = false;
            int i = indexFrom;
            while (i < indexTo) {
                char current = buf.charAt(i);
                if (!escaped) {
                    if ((delimiters != null && delimiters.get(current)) || TokenParser.isWhitespace(current) || current == '\"') {
                        break;
                    } else if (current == '\\') {
                        escaped = true;
                    } else {
                        dst.append(current);
                    }
                } else {
                    dst.append(current);
                    escaped = false;
                }
                i++;
                pos++;
            }
            cursor.updatePos(pos);
        }
    }
}
