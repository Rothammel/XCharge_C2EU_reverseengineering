package org.apache.commons.lang3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/* loaded from: classes.dex */
public class SerializationUtils {
    public static <T extends Serializable> T clone(T object) {
        ClassLoaderAwareObjectInputStream in;
        if (object == null) {
            return null;
        }
        byte[] objectData = serialize(object);
        ByteArrayInputStream bais = new ByteArrayInputStream(objectData);
        ClassLoaderAwareObjectInputStream in2 = null;
        try {
            try {
                in = new ClassLoaderAwareObjectInputStream(bais, object.getClass().getClassLoader());
            } catch (IOException e) {
                ex = e;
            } catch (ClassNotFoundException e2) {
                ex = e2;
            }
            try {
                T t = (T) in.readObject();
                if (in != null) {
                    try {
                        in.close();
                        return t;
                    } catch (IOException ex) {
                        throw new SerializationException("IOException on closing cloned object data InputStream.", ex);
                    }
                }
                return t;
            } catch (IOException e3) {
                ex = e3;
                throw new SerializationException("IOException while reading cloned object data", ex);
            } catch (ClassNotFoundException e4) {
                ex = e4;
                throw new SerializationException("ClassNotFoundException while reading cloned object data", ex);
            } catch (Throwable th) {
                th = th;
                in2 = in;
                if (in2 != null) {
                    try {
                        in2.close();
                    } catch (IOException ex2) {
                        throw new SerializationException("IOException on closing cloned object data InputStream.", ex2);
                    }
                }
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
        }
    }

    public static <T extends Serializable> T roundtrip(T msg) {
        return (T) deserialize(serialize(msg));
    }

    public static void serialize(Serializable obj, OutputStream outputStream) {
        if (outputStream == null) {
            throw new IllegalArgumentException("The OutputStream must not be null");
        }
        ObjectOutputStream out = null;
        try {
            try {
                ObjectOutputStream out2 = new ObjectOutputStream(outputStream);
                try {
                    out2.writeObject(obj);
                    if (out2 != null) {
                        try {
                            out2.close();
                        } catch (IOException e) {
                        }
                    }
                } catch (IOException e2) {
                    ex = e2;
                    throw new SerializationException(ex);
                } catch (Throwable th) {
                    th = th;
                    out = out2;
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e3) {
                        }
                    }
                    throw th;
                }
            } catch (IOException e4) {
                ex = e4;
            }
        } catch (Throwable th2) {
            th = th2;
        }
    }

    public static byte[] serialize(Serializable obj) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        serialize(obj, baos);
        return baos.toByteArray();
    }

    public static <T> T deserialize(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("The InputStream must not be null");
        }
        ObjectInputStream in = null;
        try {
            try {
                ObjectInputStream in2 = new ObjectInputStream(inputStream);
                try {
                    T obj = (T) in2.readObject();
                    if (in2 != null) {
                        try {
                            in2.close();
                        } catch (IOException e) {
                        }
                    }
                    return obj;
                } catch (IOException e2) {
                    ex = e2;
                    throw new SerializationException(ex);
                } catch (ClassCastException e3) {
                    ex = e3;
                    throw new SerializationException(ex);
                } catch (ClassNotFoundException e4) {
                    ex = e4;
                    throw new SerializationException(ex);
                } catch (Throwable th) {
                    th = th;
                    in = in2;
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e5) {
                        }
                    }
                    throw th;
                }
            } catch (IOException e6) {
                ex = e6;
            } catch (ClassCastException e7) {
                ex = e7;
            } catch (ClassNotFoundException e8) {
                ex = e8;
            }
        } catch (Throwable th2) {
            th = th2;
        }
    }

    public static <T> T deserialize(byte[] objectData) {
        if (objectData == null) {
            throw new IllegalArgumentException("The byte[] must not be null");
        }
        return (T) deserialize(new ByteArrayInputStream(objectData));
    }

    /* loaded from: classes.dex */
    static class ClassLoaderAwareObjectInputStream extends ObjectInputStream {
        private static final Map<String, Class<?>> primitiveTypes = new HashMap();
        private final ClassLoader classLoader;

        public ClassLoaderAwareObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException {
            super(in);
            this.classLoader = classLoader;
            primitiveTypes.put("byte", Byte.TYPE);
            primitiveTypes.put("short", Short.TYPE);
            primitiveTypes.put("int", Integer.TYPE);
            primitiveTypes.put("long", Long.TYPE);
            primitiveTypes.put("float", Float.TYPE);
            primitiveTypes.put("double", Double.TYPE);
            primitiveTypes.put("boolean", Boolean.TYPE);
            primitiveTypes.put("char", Character.TYPE);
            primitiveTypes.put("void", Void.TYPE);
        }

        @Override // java.io.ObjectInputStream
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            String name = desc.getName();
            try {
                return Class.forName(name, false, this.classLoader);
            } catch (ClassNotFoundException e) {
                try {
                    return Class.forName(name, false, Thread.currentThread().getContextClassLoader());
                } catch (ClassNotFoundException cnfe) {
                    Class<?> cls = primitiveTypes.get(name);
                    if (cls != null) {
                        return cls;
                    }
                    throw cnfe;
                }
            }
        }
    }
}