package org.slf4j.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

class AndroidLoggerFactory implements ILoggerFactory {
    static final String ANONYMOUS_TAG = "null";
    static final int TAG_MAX_LENGTH = 23;
    private final ConcurrentMap<String, Logger> loggerMap = new ConcurrentHashMap();

    AndroidLoggerFactory() {
    }

    public Logger getLogger(String name) {
        String tag = loggerNameToTag(name);
        Logger logger = (Logger) this.loggerMap.get(tag);
        if (logger != null) {
            return logger;
        }
        Logger newInstance = new AndroidLoggerAdapter(tag);
        Logger oldInstance = this.loggerMap.putIfAbsent(tag, newInstance);
        if (oldInstance == null) {
            return newInstance;
        }
        return oldInstance;
    }

    static String loggerNameToTag(String loggerName) {
        if (loggerName == null) {
            return ANONYMOUS_TAG;
        }
        int length = loggerName.length();
        if (length <= TAG_MAX_LENGTH) {
            return loggerName;
        }
        int tagLength = 0;
        int lastTokenIndex = 0;
        StringBuilder tagName = new StringBuilder(26);
        do {
            int lastPeriodIndex = loggerName.indexOf(46, lastTokenIndex);
            if (lastPeriodIndex != -1) {
                tagName.append(loggerName.charAt(lastTokenIndex));
                if (lastPeriodIndex - lastTokenIndex > 1) {
                    tagName.append('*');
                }
                tagName.append(ClassUtils.PACKAGE_SEPARATOR_CHAR);
                lastTokenIndex = lastPeriodIndex + 1;
                tagLength = tagName.length();
            } else {
                int tokenLength = length - lastTokenIndex;
                if (tagLength == 0 || tagLength + tokenLength > TAG_MAX_LENGTH) {
                    return getSimpleName(loggerName);
                }
                tagName.append(loggerName, lastTokenIndex, length);
                return tagName.toString();
            }
        } while (tagLength <= TAG_MAX_LENGTH);
        return getSimpleName(loggerName);
    }

    private static String getSimpleName(String loggerName) {
        int length = loggerName.length();
        int lastPeriodIndex = loggerName.lastIndexOf(46);
        return (lastPeriodIndex == -1 || length - (lastPeriodIndex + 1) > TAG_MAX_LENGTH) ? '*' + loggerName.substring((length - 23) + 1) : loggerName.substring(lastPeriodIndex + 1);
    }
}
