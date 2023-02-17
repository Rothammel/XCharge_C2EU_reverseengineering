package org.apache.commons.logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import org.apache.commons.lang3.CharEncoding;

/* loaded from: classes.dex */
public abstract class LogFactory {
    public static final String FACTORY_DEFAULT = "org.apache.commons.logging.impl.LogFactoryImpl";
    public static final String FACTORY_PROPERTIES = "commons-logging.properties";
    public static final String FACTORY_PROPERTY = "org.apache.commons.logging.LogFactory";
    protected static final String SERVICE_ID = "META-INF/services/org.apache.commons.logging.LogFactory";
    static Class class$java$lang$Thread;
    static Class class$org$apache$commons$logging$LogFactory;
    protected static Hashtable factories = new Hashtable();

    public abstract Object getAttribute(String str);

    public abstract String[] getAttributeNames();

    public abstract Log getInstance(Class cls) throws LogConfigurationException;

    public abstract Log getInstance(String str) throws LogConfigurationException;

    public abstract void release();

    public abstract void removeAttribute(String str);

    public abstract void setAttribute(String str, Object obj);

    public static LogFactory getFactory() throws LogConfigurationException {
        BufferedReader rd;
        Class cls;
        String factoryClass;
        ClassLoader contextClassLoader = (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() { // from class: org.apache.commons.logging.LogFactory.1
            @Override // java.security.PrivilegedAction
            public Object run() {
                return LogFactory.getContextClassLoader();
            }
        });
        LogFactory factory = getCachedFactory(contextClassLoader);
        if (factory != null) {
            return factory;
        }
        Properties props = null;
        try {
            InputStream stream = getResourceAsStream(contextClassLoader, FACTORY_PROPERTIES);
            if (stream != null) {
                Properties props2 = new Properties();
                try {
                    props2.load(stream);
                    stream.close();
                    props = props2;
                } catch (IOException e) {
                    props = props2;
                } catch (SecurityException e2) {
                    props = props2;
                }
            }
        } catch (IOException e3) {
        } catch (SecurityException e4) {
        }
        try {
            String factoryClass2 = System.getProperty(FACTORY_PROPERTY);
            if (factoryClass2 != null) {
                factory = newFactory(factoryClass2, contextClassLoader);
            }
        } catch (SecurityException e5) {
        }
        if (factory == null) {
            try {
                InputStream is = getResourceAsStream(contextClassLoader, SERVICE_ID);
                if (is != null) {
                    try {
                        rd = new BufferedReader(new InputStreamReader(is, CharEncoding.UTF_8));
                    } catch (UnsupportedEncodingException e6) {
                        rd = new BufferedReader(new InputStreamReader(is));
                    }
                    String factoryClassName = rd.readLine();
                    rd.close();
                    if (factoryClassName != null && !"".equals(factoryClassName)) {
                        factory = newFactory(factoryClassName, contextClassLoader);
                    }
                }
            } catch (Exception e7) {
            }
        }
        if (factory == null && props != null && (factoryClass = props.getProperty(FACTORY_PROPERTY)) != null) {
            factory = newFactory(factoryClass, contextClassLoader);
        }
        if (factory == null) {
            if (class$org$apache$commons$logging$LogFactory == null) {
                cls = class$(FACTORY_PROPERTY);
                class$org$apache$commons$logging$LogFactory = cls;
            } else {
                cls = class$org$apache$commons$logging$LogFactory;
            }
            factory = newFactory(FACTORY_DEFAULT, cls.getClassLoader());
        }
        if (factory != null) {
            cacheFactory(contextClassLoader, factory);
            if (props != null) {
                Enumeration names = props.propertyNames();
                while (names.hasMoreElements()) {
                    String name = (String) names.nextElement();
                    String value = props.getProperty(name);
                    factory.setAttribute(name, value);
                }
            }
        }
        return factory;
    }

    static Class class$(String x0) {
        try {
            return Class.forName(x0);
        } catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }

    public static Log getLog(Class clazz) throws LogConfigurationException {
        return getFactory().getInstance(clazz);
    }

    public static Log getLog(String name) throws LogConfigurationException {
        return getFactory().getInstance(name);
    }

    public static void release(ClassLoader classLoader) {
        synchronized (factories) {
            LogFactory factory = (LogFactory) factories.get(classLoader);
            if (factory != null) {
                factory.release();
                factories.remove(classLoader);
            }
        }
    }

    public static void releaseAll() {
        synchronized (factories) {
            Enumeration elements = factories.elements();
            while (elements.hasMoreElements()) {
                LogFactory element = (LogFactory) elements.nextElement();
                element.release();
            }
            factories.clear();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public static ClassLoader getContextClassLoader() throws LogConfigurationException {
        Class cls;
        Class cls2;
        try {
            if (class$java$lang$Thread == null) {
                cls2 = class$("java.lang.Thread");
                class$java$lang$Thread = cls2;
            } else {
                cls2 = class$java$lang$Thread;
            }
            Method method = cls2.getMethod("getContextClassLoader", null);
            try {
                ClassLoader classLoader = (ClassLoader) method.invoke(Thread.currentThread(), null);
                return classLoader;
            } catch (IllegalAccessException e) {
                throw new LogConfigurationException("Unexpected IllegalAccessException", e);
            } catch (InvocationTargetException e2) {
                if (e2.getTargetException() instanceof SecurityException) {
                    return null;
                }
                throw new LogConfigurationException("Unexpected InvocationTargetException", e2.getTargetException());
            }
        } catch (NoSuchMethodException e3) {
            if (class$org$apache$commons$logging$LogFactory == null) {
                cls = class$(FACTORY_PROPERTY);
                class$org$apache$commons$logging$LogFactory = cls;
            } else {
                cls = class$org$apache$commons$logging$LogFactory;
            }
            ClassLoader classLoader2 = cls.getClassLoader();
            return classLoader2;
        }
    }

    private static LogFactory getCachedFactory(ClassLoader contextClassLoader) {
        if (contextClassLoader == null) {
            return null;
        }
        LogFactory factory = (LogFactory) factories.get(contextClassLoader);
        return factory;
    }

    private static void cacheFactory(ClassLoader classLoader, LogFactory factory) {
        if (classLoader != null && factory != null) {
            factories.put(classLoader, factory);
        }
    }

    protected static LogFactory newFactory(String factoryClass, ClassLoader classLoader) throws LogConfigurationException {
        Object result = AccessController.doPrivileged(new PrivilegedAction(classLoader, factoryClass) { // from class: org.apache.commons.logging.LogFactory.2
            private final ClassLoader val$classLoader;
            private final String val$factoryClass;

            {
                this.val$classLoader = classLoader;
                this.val$factoryClass = factoryClass;
            }

            @Override // java.security.PrivilegedAction
            public Object run() {
                Class cls;
                Class cls2;
                Class cls3;
                try {
                    if (this.val$classLoader != null) {
                        try {
                            try {
                                return (LogFactory) this.val$classLoader.loadClass(this.val$factoryClass).newInstance();
                            } catch (ClassNotFoundException ex) {
                                ClassLoader classLoader2 = this.val$classLoader;
                                if (LogFactory.class$org$apache$commons$logging$LogFactory == null) {
                                    cls2 = LogFactory.class$(LogFactory.FACTORY_PROPERTY);
                                    LogFactory.class$org$apache$commons$logging$LogFactory = cls2;
                                } else {
                                    cls2 = LogFactory.class$org$apache$commons$logging$LogFactory;
                                }
                                if (classLoader2 == cls2.getClassLoader()) {
                                    throw ex;
                                }
                            } catch (NoClassDefFoundError e) {
                                ClassLoader classLoader3 = this.val$classLoader;
                                if (LogFactory.class$org$apache$commons$logging$LogFactory == null) {
                                    cls = LogFactory.class$(LogFactory.FACTORY_PROPERTY);
                                    LogFactory.class$org$apache$commons$logging$LogFactory = cls;
                                } else {
                                    cls = LogFactory.class$org$apache$commons$logging$LogFactory;
                                }
                                if (classLoader3 == cls.getClassLoader()) {
                                    throw e;
                                }
                            }
                        } catch (ClassCastException e2) {
                            ClassLoader classLoader4 = this.val$classLoader;
                            if (LogFactory.class$org$apache$commons$logging$LogFactory == null) {
                                cls3 = LogFactory.class$(LogFactory.FACTORY_PROPERTY);
                                LogFactory.class$org$apache$commons$logging$LogFactory = cls3;
                            } else {
                                cls3 = LogFactory.class$org$apache$commons$logging$LogFactory;
                            }
                            if (classLoader4 == cls3.getClassLoader()) {
                                throw e2;
                            }
                        }
                    }
                    return (LogFactory) Class.forName(this.val$factoryClass).newInstance();
                } catch (Exception e3) {
                    return new LogConfigurationException(e3);
                }
            }
        });
        if (result instanceof LogConfigurationException) {
            throw ((LogConfigurationException) result);
        }
        return (LogFactory) result;
    }

    private static InputStream getResourceAsStream(ClassLoader loader, String name) {
        return (InputStream) AccessController.doPrivileged(new PrivilegedAction(loader, name) { // from class: org.apache.commons.logging.LogFactory.3
            private final ClassLoader val$loader;
            private final String val$name;

            {
                this.val$loader = loader;
                this.val$name = name;
            }

            @Override // java.security.PrivilegedAction
            public Object run() {
                return this.val$loader != null ? this.val$loader.getResourceAsStream(this.val$name) : ClassLoader.getSystemResourceAsStream(this.val$name);
            }
        });
    }
}