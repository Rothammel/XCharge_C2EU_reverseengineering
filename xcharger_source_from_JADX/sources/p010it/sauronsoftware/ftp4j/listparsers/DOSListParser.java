package p010it.sauronsoftware.ftp4j.listparsers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import p010it.sauronsoftware.ftp4j.FTPFile;
import p010it.sauronsoftware.ftp4j.FTPListParseException;
import p010it.sauronsoftware.ftp4j.FTPListParser;

/* renamed from: it.sauronsoftware.ftp4j.listparsers.DOSListParser */
public class DOSListParser implements FTPListParser {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yy hh:mm a");
    private static final Pattern PATTERN = Pattern.compile("^(\\d{2})-(\\d{2})-(\\d{2})\\s+(\\d{2}):(\\d{2})(AM|PM)\\s+(<DIR>|\\d+)\\s+([^\\\\/*?\"<>|]+)$");

    public FTPFile[] parse(String[] lines) throws FTPListParseException {
        Date md;
        int size = lines.length;
        FTPFile[] ret = new FTPFile[size];
        int i = 0;
        while (i < size) {
            Matcher m = PATTERN.matcher(lines[i]);
            if (m.matches()) {
                String month = m.group(1);
                String day = m.group(2);
                String year = m.group(3);
                String hour = m.group(4);
                String minute = m.group(5);
                String ampm = m.group(6);
                String dirOrSize = m.group(7);
                String name = m.group(8);
                ret[i] = new FTPFile();
                ret[i].setName(name);
                if (dirOrSize.equalsIgnoreCase("<DIR>")) {
                    ret[i].setType(1);
                    ret[i].setSize(0);
                } else {
                    try {
                        long fileSize = Long.parseLong(dirOrSize);
                        ret[i].setType(0);
                        ret[i].setSize(fileSize);
                    } catch (Throwable th) {
                        throw new FTPListParseException();
                    }
                }
                String mdString = new StringBuffer().append(month).append(MqttTopic.TOPIC_LEVEL_SEPARATOR).append(day).append(MqttTopic.TOPIC_LEVEL_SEPARATOR).append(year).append(StringUtils.SPACE).append(hour).append(":").append(minute).append(StringUtils.SPACE).append(ampm).toString();
                try {
                    synchronized (DATE_FORMAT) {
                        md = DATE_FORMAT.parse(mdString);
                    }
                    ret[i].setModifiedDate(md);
                    i++;
                } catch (ParseException e) {
                    throw new FTPListParseException();
                }
            } else {
                throw new FTPListParseException();
            }
        }
        return ret;
    }
}
