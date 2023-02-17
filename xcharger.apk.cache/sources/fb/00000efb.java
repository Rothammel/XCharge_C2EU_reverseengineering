package org.eclipse.paho.client.mqttv3.util;

import java.util.Enumeration;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.TokenParser;
import org.eclipse.paho.client.mqttv3.internal.ClientComms;
import org.eclipse.paho.client.mqttv3.logging.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;

/* loaded from: classes.dex */
public class Debug {
    private static final String separator = "==============";
    private String clientID;
    private ClientComms comms;
    private static final String CLASS_NAME = ClientComms.class.getName();
    private static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
    private static final String lineSep = System.getProperty("line.separator", StringUtils.LF);

    public Debug(String clientID, ClientComms comms) {
        this.clientID = clientID;
        this.comms = comms;
        log.setResourceName(clientID);
    }

    public void dumpClientDebug() {
        dumpClientComms();
        dumpConOptions();
        dumpClientState();
        dumpBaseDebug();
    }

    public void dumpBaseDebug() {
        dumpVersion();
        dumpSystemProperties();
        dumpMemoryTrace();
    }

    protected void dumpMemoryTrace() {
        log.dumpTrace();
    }

    protected void dumpVersion() {
        StringBuffer vInfo = new StringBuffer();
        vInfo.append(String.valueOf(lineSep) + separator + " Version Info " + separator + lineSep);
        vInfo.append(String.valueOf(left("Version", 20, TokenParser.SP)) + ":  " + ClientComms.VERSION + lineSep);
        vInfo.append(String.valueOf(left("Build Level", 20, TokenParser.SP)) + ":  " + ClientComms.BUILD_LEVEL + lineSep);
        vInfo.append("==========================================" + lineSep);
        log.fine(CLASS_NAME, "dumpVersion", vInfo.toString());
    }

    public void dumpSystemProperties() {
        Properties sysProps = System.getProperties();
        log.fine(CLASS_NAME, "dumpSystemProperties", dumpProperties(sysProps, "SystemProperties").toString());
    }

    public void dumpClientState() {
        if (this.comms != null && this.comms.getClientState() != null) {
            Properties props = this.comms.getClientState().getDebug();
            log.fine(CLASS_NAME, "dumpClientState", dumpProperties(props, String.valueOf(this.clientID) + " : ClientState").toString());
        }
    }

    public void dumpClientComms() {
        if (this.comms != null) {
            Properties props = this.comms.getDebug();
            log.fine(CLASS_NAME, "dumpClientComms", dumpProperties(props, String.valueOf(this.clientID) + " : ClientComms").toString());
        }
    }

    public void dumpConOptions() {
        if (this.comms != null) {
            Properties props = this.comms.getConOptions().getDebug();
            log.fine(CLASS_NAME, "dumpConOptions", dumpProperties(props, String.valueOf(this.clientID) + " : Connect Options").toString());
        }
    }

    public static String dumpProperties(Properties props, String name) {
        StringBuffer propStr = new StringBuffer();
        Enumeration propsE = props.propertyNames();
        propStr.append(String.valueOf(lineSep) + separator + StringUtils.SPACE + name + StringUtils.SPACE + separator + lineSep);
        while (propsE.hasMoreElements()) {
            String key = (String) propsE.nextElement();
            propStr.append(String.valueOf(left(key, 28, TokenParser.SP)) + ":  " + props.get(key) + lineSep);
        }
        propStr.append("==========================================" + lineSep);
        return propStr.toString();
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