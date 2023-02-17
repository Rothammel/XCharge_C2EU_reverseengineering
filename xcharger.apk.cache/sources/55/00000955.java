package it.sauronsoftware.ftp4j.listparsers;

import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPListParseException;
import it.sauronsoftware.ftp4j.FTPListParser;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* loaded from: classes.dex */
public class DOSListParser implements FTPListParser {
    private static final Pattern PATTERN = Pattern.compile("^(\\d{2})-(\\d{2})-(\\d{2})\\s+(\\d{2}):(\\d{2})(AM|PM)\\s+(<DIR>|\\d+)\\s+([^\\\\/*?\"<>|]+)$");
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yy hh:mm a");

    @Override // it.sauronsoftware.ftp4j.FTPListParser
    public FTPFile[] parse(String[] lines) throws FTPListParseException {
        Date md;
        int size = lines.length;
        FTPFile[] ret = new FTPFile[size];
        for (int i = 0; i < size; i++) {
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
                    ret[i].setSize(0L);
                } else {
                    try {
                        long fileSize = Long.parseLong(dirOrSize);
                        ret[i].setType(0);
                        ret[i].setSize(fileSize);
                    } finally {
                        FTPListParseException fTPListParseException = new FTPListParseException();
                    }
                }
                String mdString = new StringBuffer().append(month).append(MqttTopic.TOPIC_LEVEL_SEPARATOR).append(day).append(MqttTopic.TOPIC_LEVEL_SEPARATOR).append(year).append(StringUtils.SPACE).append(hour).append(":").append(minute).append(StringUtils.SPACE).append(ampm).toString();
                try {
                    synchronized (DATE_FORMAT) {
                        md = DATE_FORMAT.parse(mdString);
                    }
                    ret[i].setModifiedDate(md);
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