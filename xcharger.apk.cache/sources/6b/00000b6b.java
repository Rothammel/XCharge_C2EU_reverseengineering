package org.apache.commons.logging.impl;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

/* loaded from: classes.dex */
public final class Log4jFactory extends LogFactory {
    private Hashtable attributes = new Hashtable();
    private Hashtable instances = new Hashtable();

    @Override // org.apache.commons.logging.LogFactory
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    @Override // org.apache.commons.logging.LogFactory
    public String[] getAttributeNames() {
        Vector names = new Vector();
        Enumeration keys = this.attributes.keys();
        while (keys.hasMoreElements()) {
            names.addElement((String) keys.nextElement());
        }
        String[] results = new String[names.size()];
        for (int i = 0; i < results.length; i++) {
            results[i] = (String) names.elementAt(i);
        }
        return results;
    }

    @Override // org.apache.commons.logging.LogFactory
    public Log getInstance(Class clazz) throws LogConfigurationException {
        Log instance = (Log) this.instances.get(clazz);
        if (instance != null) {
            return instance;
        }
        Log4JLogger log4JLogger = new Log4JLogger(Logger.getLogger(clazz));
        this.instances.put(clazz, log4JLogger);
        return log4JLogger;
    }

    @Override // org.apache.commons.logging.LogFactory
    public Log getInstance(String name) throws LogConfigurationException {
        Log instance = (Log) this.instances.get(name);
        if (instance != null) {
            return instance;
        }
        Log4JLogger log4JLogger = new Log4JLogger(Logger.getLogger(name));
        this.instances.put(name, log4JLogger);
        return log4JLogger;
    }

    @Override // org.apache.commons.logging.LogFactory
    public void release() {
        this.instances.clear();
    }

    @Override // org.apache.commons.logging.LogFactory
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    @Override // org.apache.commons.logging.LogFactory
    public void setAttribute(String name, Object value) {
        if (value == null) {
            this.attributes.remove(name);
        } else {
            this.attributes.put(name, value);
        }
    }
}