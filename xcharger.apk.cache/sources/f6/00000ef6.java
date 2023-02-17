package org.eclipse.paho.client.mqttv3.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.TokenParser;

/* loaded from: classes.dex */
public class SimpleLogFormatter extends Formatter {
    private static final String LS = System.getProperty("line.separator");

    @Override // java.util.logging.Formatter
    public String format(LogRecord r) {
        StringWriter sw;
        PrintWriter pw;
        StringBuffer sb = new StringBuffer();
        sb.append(r.getLevel().getName()).append("\t");
        sb.append(String.valueOf(MessageFormat.format("{0, date, yy-MM-dd} {0, time, kk:mm:ss.SSSS} ", new Date(r.getMillis()))) + "\t");
        String cnm = r.getSourceClassName();
        String cn = "";
        if (cnm != null) {
            int cnl = cnm.length();
            if (cnl > 20) {
                cn = r.getSourceClassName().substring(cnl - 19);
            } else {
                char[] sp = {TokenParser.SP};
                StringBuffer sb1 = new StringBuffer().append(cnm);
                cn = sb1.append(sp, 0, 1).toString();
            }
        }
        sb.append(cn).append("\t").append(StringUtils.SPACE);
        sb.append(left(r.getSourceMethodName(), 23, TokenParser.SP)).append("\t");
        sb.append(r.getThreadID()).append("\t");
        sb.append(formatMessage(r)).append(LS);
        if (r.getThrown() != null) {
            sb.append("Throwable occurred: ");
            Throwable t = r.getThrown();
            PrintWriter pw2 = null;
            try {
                sw = new StringWriter();
                pw = new PrintWriter(sw);
            } catch (Throwable th) {
                th = th;
            }
            try {
                t.printStackTrace(pw);
                sb.append(sw.toString());
                if (pw != null) {
                    try {
                        pw.close();
                    } catch (Exception e) {
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                pw2 = pw;
                if (pw2 != null) {
                    try {
                        pw2.close();
                    } catch (Exception e2) {
                    }
                }
                throw th;
            }
        }
        return sb.toString();
    }

    public static String left(String s, int width, char fillChar) {
        if (s.length() < width) {
            StringBuffer sb = new StringBuffer(width);
            sb.append(s);
            int i = width - s.length();
            while (true) {
                i--;
                if (i >= 0) {
                    sb.append(fillChar);
                } else {
                    return sb.toString();
                }
            }
        } else {
            return s;
        }
    }
}