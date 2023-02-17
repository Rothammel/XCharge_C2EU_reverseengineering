package it.sauronsoftware.ftp4j.listparsers;

import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPListParseException;
import it.sauronsoftware.ftp4j.FTPListParser;
import java.util.Date;
import java.util.StringTokenizer;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* loaded from: classes.dex */
public class EPLFListParser implements FTPListParser {
    @Override // it.sauronsoftware.ftp4j.FTPListParser
    public FTPFile[] parse(String[] lines) throws FTPListParseException {
        int size = lines.length;
        FTPFile[] ret = null;
        for (int i = 0; i < size; i++) {
            String l = lines[i];
            if (l.charAt(0) != '+') {
                throw new FTPListParseException();
            }
            int a = l.indexOf(9);
            if (a == -1) {
                throw new FTPListParseException();
            }
            String facts = l.substring(1, a);
            String name = l.substring(a + 1, l.length());
            Date md = null;
            boolean dir = false;
            long fileSize = 0;
            StringTokenizer st = new StringTokenizer(facts, ",");
            while (st.hasMoreTokens()) {
                String f = st.nextToken();
                int s = f.length();
                if (s > 0) {
                    if (s == 1) {
                        if (f.equals(MqttTopic.TOPIC_LEVEL_SEPARATOR)) {
                            dir = true;
                        }
                    } else {
                        char c = f.charAt(0);
                        String value = f.substring(1, s);
                        if (c == 's') {
                            try {
                                fileSize = Long.parseLong(value);
                            } catch (Throwable th) {
                            }
                        } else if (c == 'm') {
                            try {
                                long m = Long.parseLong(value);
                                Date md2 = new Date(1000 * m);
                                md = md2;
                            } catch (Throwable th2) {
                            }
                        }
                    }
                }
            }
            if (ret == null) {
                ret = new FTPFile[size];
            }
            ret[i] = new FTPFile();
            ret[i].setName(name);
            ret[i].setModifiedDate(md);
            ret[i].setSize(fileSize);
            ret[i].setType(dir ? 1 : 0);
        }
        return ret;
    }

    public static void main(String[] args) throws Throwable {
        String[] test = {"+i8388621.29609,m824255902,/,\tdev", "+i8388621.44468,m839956783,r,s10376,\tRFCEPLF"};
        EPLFListParser parser = new EPLFListParser();
        FTPFile[] f = parser.parse(test);
        for (FTPFile fTPFile : f) {
            System.out.println(fTPFile);
        }
    }
}
