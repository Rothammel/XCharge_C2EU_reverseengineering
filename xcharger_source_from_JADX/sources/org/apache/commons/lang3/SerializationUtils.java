package org.apache.commons.lang3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SerializationUtils {
    /* JADX WARNING: Removed duplicated region for block: B:19:0x003c A[SYNTHETIC, Splitter:B:19:0x003c] */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:14:0x0031=Splitter:B:14:0x0031, B:23:0x0041=Splitter:B:23:0x0041} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static <T extends java.io.Serializable> T clone(T r8) {
        /*
            if (r8 != 0) goto L_0x0004
            r5 = 0
        L_0x0003:
            return r5
        L_0x0004:
            byte[] r4 = serialize(r8)
            java.io.ByteArrayInputStream r0 = new java.io.ByteArrayInputStream
            r0.<init>(r4)
            r2 = 0
            org.apache.commons.lang3.SerializationUtils$ClassLoaderAwareObjectInputStream r3 = new org.apache.commons.lang3.SerializationUtils$ClassLoaderAwareObjectInputStream     // Catch:{ ClassNotFoundException -> 0x0030, IOException -> 0x0040 }
            java.lang.Class r6 = r8.getClass()     // Catch:{ ClassNotFoundException -> 0x0030, IOException -> 0x0040 }
            java.lang.ClassLoader r6 = r6.getClassLoader()     // Catch:{ ClassNotFoundException -> 0x0030, IOException -> 0x0040 }
            r3.<init>(r0, r6)     // Catch:{ ClassNotFoundException -> 0x0030, IOException -> 0x0040 }
            java.lang.Object r5 = r3.readObject()     // Catch:{ ClassNotFoundException -> 0x0058, IOException -> 0x0055, all -> 0x0052 }
            java.io.Serializable r5 = (java.io.Serializable) r5     // Catch:{ ClassNotFoundException -> 0x0058, IOException -> 0x0055, all -> 0x0052 }
            if (r3 == 0) goto L_0x0003
            r3.close()     // Catch:{ IOException -> 0x0027 }
            goto L_0x0003
        L_0x0027:
            r1 = move-exception
            org.apache.commons.lang3.SerializationException r6 = new org.apache.commons.lang3.SerializationException
            java.lang.String r7 = "IOException on closing cloned object data InputStream."
            r6.<init>(r7, r1)
            throw r6
        L_0x0030:
            r1 = move-exception
        L_0x0031:
            org.apache.commons.lang3.SerializationException r6 = new org.apache.commons.lang3.SerializationException     // Catch:{ all -> 0x0039 }
            java.lang.String r7 = "ClassNotFoundException while reading cloned object data"
            r6.<init>(r7, r1)     // Catch:{ all -> 0x0039 }
            throw r6     // Catch:{ all -> 0x0039 }
        L_0x0039:
            r6 = move-exception
        L_0x003a:
            if (r2 == 0) goto L_0x003f
            r2.close()     // Catch:{ IOException -> 0x0049 }
        L_0x003f:
            throw r6
        L_0x0040:
            r1 = move-exception
        L_0x0041:
            org.apache.commons.lang3.SerializationException r6 = new org.apache.commons.lang3.SerializationException     // Catch:{ all -> 0x0039 }
            java.lang.String r7 = "IOException while reading cloned object data"
            r6.<init>(r7, r1)     // Catch:{ all -> 0x0039 }
            throw r6     // Catch:{ all -> 0x0039 }
        L_0x0049:
            r1 = move-exception
            org.apache.commons.lang3.SerializationException r6 = new org.apache.commons.lang3.SerializationException
            java.lang.String r7 = "IOException on closing cloned object data InputStream."
            r6.<init>(r7, r1)
            throw r6
        L_0x0052:
            r6 = move-exception
            r2 = r3
            goto L_0x003a
        L_0x0055:
            r1 = move-exception
            r2 = r3
            goto L_0x0041
        L_0x0058:
            r1 = move-exception
            r2 = r3
            goto L_0x0031
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.lang3.SerializationUtils.clone(java.io.Serializable):java.io.Serializable");
    }

    public static <T extends Serializable> T roundtrip(T msg) {
        return (Serializable) deserialize(serialize(msg));
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0023 A[SYNTHETIC, Splitter:B:17:0x0023] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void serialize(java.io.Serializable r5, java.io.OutputStream r6) {
        /*
            if (r6 != 0) goto L_0x000a
            java.lang.IllegalArgumentException r3 = new java.lang.IllegalArgumentException
            java.lang.String r4 = "The OutputStream must not be null"
            r3.<init>(r4)
            throw r3
        L_0x000a:
            r1 = 0
            java.io.ObjectOutputStream r2 = new java.io.ObjectOutputStream     // Catch:{ IOException -> 0x0019 }
            r2.<init>(r6)     // Catch:{ IOException -> 0x0019 }
            r2.writeObject(r5)     // Catch:{ IOException -> 0x002e, all -> 0x002b }
            if (r2 == 0) goto L_0x0018
            r2.close()     // Catch:{ IOException -> 0x0027 }
        L_0x0018:
            return
        L_0x0019:
            r0 = move-exception
        L_0x001a:
            org.apache.commons.lang3.SerializationException r3 = new org.apache.commons.lang3.SerializationException     // Catch:{ all -> 0x0020 }
            r3.<init>((java.lang.Throwable) r0)     // Catch:{ all -> 0x0020 }
            throw r3     // Catch:{ all -> 0x0020 }
        L_0x0020:
            r3 = move-exception
        L_0x0021:
            if (r1 == 0) goto L_0x0026
            r1.close()     // Catch:{ IOException -> 0x0029 }
        L_0x0026:
            throw r3
        L_0x0027:
            r3 = move-exception
            goto L_0x0018
        L_0x0029:
            r4 = move-exception
            goto L_0x0026
        L_0x002b:
            r3 = move-exception
            r1 = r2
            goto L_0x0021
        L_0x002e:
            r0 = move-exception
            r1 = r2
            goto L_0x001a
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.lang3.SerializationUtils.serialize(java.io.Serializable, java.io.OutputStream):void");
    }

    public static byte[] serialize(Serializable obj) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        serialize(obj, baos);
        return baos.toByteArray();
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0024 A[SYNTHETIC, Splitter:B:18:0x0024] */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:22:0x0029=Splitter:B:22:0x0029, B:13:0x001b=Splitter:B:13:0x001b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static <T> T deserialize(java.io.InputStream r6) {
        /*
            if (r6 != 0) goto L_0x000a
            java.lang.IllegalArgumentException r4 = new java.lang.IllegalArgumentException
            java.lang.String r5 = "The InputStream must not be null"
            r4.<init>(r5)
            throw r4
        L_0x000a:
            r1 = 0
            java.io.ObjectInputStream r2 = new java.io.ObjectInputStream     // Catch:{ ClassCastException -> 0x001a, ClassNotFoundException -> 0x0028, IOException -> 0x002f }
            r2.<init>(r6)     // Catch:{ ClassCastException -> 0x001a, ClassNotFoundException -> 0x0028, IOException -> 0x002f }
            java.lang.Object r3 = r2.readObject()     // Catch:{ ClassCastException -> 0x0043, ClassNotFoundException -> 0x0040, IOException -> 0x003d, all -> 0x003a }
            if (r2 == 0) goto L_0x0019
            r2.close()     // Catch:{ IOException -> 0x0036 }
        L_0x0019:
            return r3
        L_0x001a:
            r0 = move-exception
        L_0x001b:
            org.apache.commons.lang3.SerializationException r4 = new org.apache.commons.lang3.SerializationException     // Catch:{ all -> 0x0021 }
            r4.<init>((java.lang.Throwable) r0)     // Catch:{ all -> 0x0021 }
            throw r4     // Catch:{ all -> 0x0021 }
        L_0x0021:
            r4 = move-exception
        L_0x0022:
            if (r1 == 0) goto L_0x0027
            r1.close()     // Catch:{ IOException -> 0x0038 }
        L_0x0027:
            throw r4
        L_0x0028:
            r0 = move-exception
        L_0x0029:
            org.apache.commons.lang3.SerializationException r4 = new org.apache.commons.lang3.SerializationException     // Catch:{ all -> 0x0021 }
            r4.<init>((java.lang.Throwable) r0)     // Catch:{ all -> 0x0021 }
            throw r4     // Catch:{ all -> 0x0021 }
        L_0x002f:
            r0 = move-exception
        L_0x0030:
            org.apache.commons.lang3.SerializationException r4 = new org.apache.commons.lang3.SerializationException     // Catch:{ all -> 0x0021 }
            r4.<init>((java.lang.Throwable) r0)     // Catch:{ all -> 0x0021 }
            throw r4     // Catch:{ all -> 0x0021 }
        L_0x0036:
            r4 = move-exception
            goto L_0x0019
        L_0x0038:
            r5 = move-exception
            goto L_0x0027
        L_0x003a:
            r4 = move-exception
            r1 = r2
            goto L_0x0022
        L_0x003d:
            r0 = move-exception
            r1 = r2
            goto L_0x0030
        L_0x0040:
            r0 = move-exception
            r1 = r2
            goto L_0x0029
        L_0x0043:
            r0 = move-exception
            r1 = r2
            goto L_0x001b
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.lang3.SerializationUtils.deserialize(java.io.InputStream):java.lang.Object");
    }

    public static <T> T deserialize(byte[] objectData) {
        if (objectData != null) {
            return deserialize((InputStream) new ByteArrayInputStream(objectData));
        }
        throw new IllegalArgumentException("The byte[] must not be null");
    }

    static class ClassLoaderAwareObjectInputStream extends ObjectInputStream {
        private static final Map<String, Class<?>> primitiveTypes = new HashMap();
        private final ClassLoader classLoader;

        public ClassLoaderAwareObjectInputStream(InputStream in, ClassLoader classLoader2) throws IOException {
            super(in);
            this.classLoader = classLoader2;
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

        /* access modifiers changed from: protected */
        public Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
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
