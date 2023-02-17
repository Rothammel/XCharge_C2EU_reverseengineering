package org.apache.http.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.ClassUtils;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* loaded from: classes.dex */
public class VersionInfoHC4 {
    public static final String PROPERTY_MODULE = "info.module";
    public static final String PROPERTY_RELEASE = "info.release";
    public static final String PROPERTY_TIMESTAMP = "info.timestamp";
    public static final String UNAVAILABLE = "UNAVAILABLE";
    public static final String VERSION_PROPERTY_FILE = "version.properties";
    private final String infoClassloader;
    private final String infoModule;
    private final String infoPackage;
    private final String infoRelease;
    private final String infoTimestamp;

    protected VersionInfoHC4(String pckg, String module, String release, String time, String clsldr) {
        Args.notNull(pckg, "Package identifier");
        this.infoPackage = pckg;
        this.infoModule = module == null ? UNAVAILABLE : module;
        this.infoRelease = release == null ? UNAVAILABLE : release;
        this.infoTimestamp = time == null ? UNAVAILABLE : time;
        this.infoClassloader = clsldr == null ? UNAVAILABLE : clsldr;
    }

    public final String getPackage() {
        return this.infoPackage;
    }

    public final String getModule() {
        return this.infoModule;
    }

    public final String getRelease() {
        return this.infoRelease;
    }

    public final String getTimestamp() {
        return this.infoTimestamp;
    }

    public final String getClassloader() {
        return this.infoClassloader;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(this.infoPackage.length() + 20 + this.infoModule.length() + this.infoRelease.length() + this.infoTimestamp.length() + this.infoClassloader.length());
        sb.append("VersionInfo(").append(this.infoPackage).append(':').append(this.infoModule);
        if (!UNAVAILABLE.equals(this.infoRelease)) {
            sb.append(':').append(this.infoRelease);
        }
        if (!UNAVAILABLE.equals(this.infoTimestamp)) {
            sb.append(':').append(this.infoTimestamp);
        }
        sb.append(')');
        if (!UNAVAILABLE.equals(this.infoClassloader)) {
            sb.append('@').append(this.infoClassloader);
        }
        return sb.toString();
    }

    public static VersionInfo[] loadVersionInfo(String[] pckgs, ClassLoader clsldr) {
        Args.notNull(pckgs, "Package identifier array");
        List<VersionInfoHC4> vil = new ArrayList<>(pckgs.length);
        for (String pckg : pckgs) {
            VersionInfoHC4 vi = loadVersionInfo(pckg, clsldr);
            if (vi != null) {
                vil.add(vi);
            }
        }
        return (VersionInfo[]) vil.toArray(new VersionInfo[vil.size()]);
    }

    public static VersionInfoHC4 loadVersionInfo(String pckg, ClassLoader clsldr) {
        Args.notNull(pckg, "Package identifier");
        ClassLoader cl = clsldr != null ? clsldr : Thread.currentThread().getContextClassLoader();
        Properties vip = null;
        try {
            InputStream is = cl.getResourceAsStream(String.valueOf(pckg.replace(ClassUtils.PACKAGE_SEPARATOR_CHAR, '/')) + MqttTopic.TOPIC_LEVEL_SEPARATOR + VERSION_PROPERTY_FILE);
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                vip = props;
                is.close();
            }
        } catch (IOException e) {
        }
        if (vip == null) {
            return null;
        }
        VersionInfoHC4 result = fromMap(pckg, vip, cl);
        return result;
    }

    protected static VersionInfoHC4 fromMap(String pckg, Map<?, ?> info, ClassLoader clsldr) {
        Args.notNull(pckg, "Package identifier");
        String module = null;
        String release = null;
        String timestamp = null;
        if (info != null) {
            module = (String) info.get(PROPERTY_MODULE);
            if (module != null && module.length() < 1) {
                module = null;
            }
            release = (String) info.get(PROPERTY_RELEASE);
            if (release != null && (release.length() < 1 || release.equals("${pom.version}"))) {
                release = null;
            }
            timestamp = (String) info.get(PROPERTY_TIMESTAMP);
            if (timestamp != null && (timestamp.length() < 1 || timestamp.equals("${mvn.timestamp}"))) {
                timestamp = null;
            }
        }
        String clsldrstr = null;
        if (clsldr != null) {
            clsldrstr = clsldr.toString();
        }
        return new VersionInfoHC4(pckg, module, release, timestamp, clsldrstr);
    }

    public static String getUserAgent(String name, String pkg, Class<?> cls) {
        VersionInfoHC4 vi = loadVersionInfo(pkg, cls.getClassLoader());
        String release = vi != null ? vi.getRelease() : UNAVAILABLE;
        String javaVersion = System.getProperty("java.version");
        return String.valueOf(name) + MqttTopic.TOPIC_LEVEL_SEPARATOR + release + " (Java 1.5 minimum; Java/" + javaVersion + ")";
    }
}