package org.slf4j.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/* loaded from: classes.dex */
class AndroidLoggerFactory implements ILoggerFactory {
    static final String ANONYMOUS_TAG = "null";
    static final int TAG_MAX_LENGTH = 23;
    private final ConcurrentMap<String, Logger> loggerMap = new ConcurrentHashMap();

    @Override // org.slf4j.ILoggerFactory
    public Logger getLogger(String name) {
        String tag = loggerNameToTag(name);
        Logger logger = this.loggerMap.get(tag);
        if (logger == null) {
            Logger newInstance = new AndroidLoggerAdapter(tag);
            Logger oldInstance = this.loggerMap.putIfAbsent(tag, newInstance);
            return oldInstance == null ? newInstance : oldInstance;
        }
        return logger;
    }

    static String loggerNameToTag(String loggerName) {
        if (loggerName == null) {
            return ANONYMOUS_TAG;
        }
        int length = loggerName.length();
        if (length > TAG_MAX_LENGTH) {
            int tagLength = 0;
            int lastTokenIndex = 0;
            StringBuilder tagName = new StringBuilder(26);
            do {
                int lastPeriodIndex = loggerName.indexOf(46, lastTokenIndex);
                if (lastPeriodIndex != -1) {
                    tagName.append(loggerName.charAt(lastTokenIndex));
                    int tokenLength = lastPeriodIndex - lastTokenIndex;
                    if (tokenLength > 1) {
                        tagName.append('*');
                    }
                    tagName.append(ClassUtils.PACKAGE_SEPARATOR_CHAR);
                    lastTokenIndex = lastPeriodIndex + 1;
                    tagLength = tagName.length();
                } else {
                    int tokenLength2 = length - lastTokenIndex;
                    if (tagLength == 0 || tagLength + tokenLength2 > TAG_MAX_LENGTH) {
                        return getSimpleName(loggerName);
                    }
                    tagName.append((CharSequence) loggerName, lastTokenIndex, length);
                    return tagName.toString();
                }
            } while (tagLength <= TAG_MAX_LENGTH);
            return getSimpleName(loggerName);
        }
        return loggerName;
    }

    private static String getSimpleName(String loggerName) {
        int length = loggerName.length();
        int lastPeriodIndex = loggerName.lastIndexOf(46);
        return (lastPeriodIndex == -1 || length - (lastPeriodIndex + 1) > TAG_MAX_LENGTH) ? '*' + loggerName.substring((length - 23) + 1) : loggerName.substring(lastPeriodIndex + 1);
    }
}