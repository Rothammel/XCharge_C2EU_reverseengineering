package org.apache.commons.lang3.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class FieldUtils {
    public static Field getField(Class<?> cls, String fieldName) {
        Field field = getField(cls, fieldName, false);
        MemberUtils.setAccessibleWorkaround(field);
        return field;
    }

    public static Field getField(Class<?> cls, String fieldName, boolean forceAccess) {
        boolean z;
        boolean z2;
        if (cls != null) {
            z = true;
        } else {
            z = false;
        }
        Validate.isTrue(z, "The class must not be null", new Object[0]);
        Validate.isTrue(StringUtils.isNotBlank(fieldName), "The field name must not be blank/empty", new Object[0]);
        Class<?> acls = cls;
        while (acls != null) {
            try {
                Field field = acls.getDeclaredField(fieldName);
                if (Modifier.isPublic(field.getModifiers())) {
                    return field;
                }
                if (forceAccess) {
                    field.setAccessible(true);
                    return field;
                }
                acls = acls.getSuperclass();
            } catch (NoSuchFieldException e) {
            }
        }
        Field match = null;
        for (Class<?> class1 : ClassUtils.getAllInterfaces(cls)) {
            try {
                Field test = class1.getField(fieldName);
                if (match == null) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                Validate.isTrue(z2, "Reference to field %s is ambiguous relative to %s; a matching field exists on two or more implemented interfaces.", fieldName, cls);
                match = test;
            } catch (NoSuchFieldException e2) {
            }
        }
        return match;
    }

    public static Field getDeclaredField(Class<?> cls, String fieldName) {
        return getDeclaredField(cls, fieldName, false);
    }

    public static Field getDeclaredField(Class<?> cls, String fieldName, boolean forceAccess) {
        boolean z = true;
        if (cls == null) {
            z = false;
        }
        Validate.isTrue(z, "The class must not be null", new Object[0]);
        Validate.isTrue(StringUtils.isNotBlank(fieldName), "The field name must not be blank/empty", new Object[0]);
        try {
            Field field = cls.getDeclaredField(fieldName);
            if (MemberUtils.isAccessible(field)) {
                return field;
            }
            if (!forceAccess) {
                return null;
            }
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    public static Field[] getAllFields(Class<?> cls) {
        List<Field> allFieldsList = getAllFieldsList(cls);
        return (Field[]) allFieldsList.toArray(new Field[allFieldsList.size()]);
    }

    public static List<Field> getAllFieldsList(Class<?> cls) {
        boolean z;
        if (cls != null) {
            z = true;
        } else {
            z = false;
        }
        Validate.isTrue(z, "The class must not be null", new Object[0]);
        List<Field> allFields = new ArrayList<>();
        for (Class<?> currentClass = cls; currentClass != null; currentClass = currentClass.getSuperclass()) {
            for (Field field : currentClass.getDeclaredFields()) {
                allFields.add(field);
            }
        }
        return allFields;
    }

    public static Field[] getFieldsWithAnnotation(Class<?> cls, Class<? extends Annotation> annotationCls) {
        List<Field> annotatedFieldsList = getFieldsListWithAnnotation(cls, annotationCls);
        return (Field[]) annotatedFieldsList.toArray(new Field[annotatedFieldsList.size()]);
    }

    public static List<Field> getFieldsListWithAnnotation(Class<?> cls, Class<? extends Annotation> annotationCls) {
        boolean z;
        if (annotationCls != null) {
            z = true;
        } else {
            z = false;
        }
        Validate.isTrue(z, "The annotation class must not be null", new Object[0]);
        List<Field> allFields = getAllFieldsList(cls);
        List<Field> annotatedFields = new ArrayList<>();
        for (Field field : allFields) {
            if (field.getAnnotation(annotationCls) != null) {
                annotatedFields.add(field);
            }
        }
        return annotatedFields;
    }

    public static Object readStaticField(Field field) throws IllegalAccessException {
        return readStaticField(field, false);
    }

    public static Object readStaticField(Field field, boolean forceAccess) throws IllegalAccessException {
        boolean z;
        if (field != null) {
            z = true;
        } else {
            z = false;
        }
        Validate.isTrue(z, "The field must not be null", new Object[0]);
        Validate.isTrue(Modifier.isStatic(field.getModifiers()), "The field '%s' is not static", field.getName());
        return readField(field, (Object) null, forceAccess);
    }

    public static Object readStaticField(Class<?> cls, String fieldName) throws IllegalAccessException {
        return readStaticField(cls, fieldName, false);
    }

    public static Object readStaticField(Class<?> cls, String fieldName, boolean forceAccess) throws IllegalAccessException {
        boolean z;
        Field field = getField(cls, fieldName, forceAccess);
        if (field != null) {
            z = true;
        } else {
            z = false;
        }
        Validate.isTrue(z, "Cannot locate field '%s' on %s", fieldName, cls);
        return readStaticField(field, false);
    }

    public static Object readDeclaredStaticField(Class<?> cls, String fieldName) throws IllegalAccessException {
        return readDeclaredStaticField(cls, fieldName, false);
    }

    public static Object readDeclaredStaticField(Class<?> cls, String fieldName, boolean forceAccess) throws IllegalAccessException {
        boolean z;
        Field field = getDeclaredField(cls, fieldName, forceAccess);
        if (field != null) {
            z = true;
        } else {
            z = false;
        }
        Validate.isTrue(z, "Cannot locate declared field %s.%s", cls.getName(), fieldName);
        return readStaticField(field, false);
    }

    public static Object readField(Field field, Object target) throws IllegalAccessException {
        return readField(field, target, false);
    }

    public static Object readField(Field field, Object target, boolean forceAccess) throws IllegalAccessException {
        Validate.isTrue(field != null, "The field must not be null", new Object[0]);
        if (!forceAccess || field.isAccessible()) {
            MemberUtils.setAccessibleWorkaround(field);
        } else {
            field.setAccessible(true);
        }
        return field.get(target);
    }

    public static Object readField(Object target, String fieldName) throws IllegalAccessException {
        return readField(target, fieldName, false);
    }

    public static Object readField(Object target, String fieldName, boolean forceAccess) throws IllegalAccessException {
        boolean z;
        boolean z2;
        if (target != null) {
            z = true;
        } else {
            z = false;
        }
        Validate.isTrue(z, "target object must not be null", new Object[0]);
        Class<?> cls = target.getClass();
        Field field = getField(cls, fieldName, forceAccess);
        if (field != null) {
            z2 = true;
        } else {
            z2 = false;
        }
        Validate.isTrue(z2, "Cannot locate field %s on %s", fieldName, cls);
        return readField(field, target, false);
    }

    public static Object readDeclaredField(Object target, String fieldName) throws IllegalAccessException {
        return readDeclaredField(target, fieldName, false);
    }

    public static Object readDeclaredField(Object target, String fieldName, boolean forceAccess) throws IllegalAccessException {
        boolean z;
        boolean z2;
        if (target != null) {
            z = true;
        } else {
            z = false;
        }
        Validate.isTrue(z, "target object must not be null", new Object[0]);
        Class<?> cls = target.getClass();
        Field field = getDeclaredField(cls, fieldName, forceAccess);
        if (field != null) {
            z2 = true;
        } else {
            z2 = false;
        }
        Validate.isTrue(z2, "Cannot locate declared field %s.%s", cls, fieldName);
        return readField(field, target, false);
    }

    public static void writeStaticField(Field field, Object value) throws IllegalAccessException {
        writeStaticField(field, value, false);
    }

    public static void writeStaticField(Field field, Object value, boolean forceAccess) throws IllegalAccessException {
        boolean z;
        if (field != null) {
            z = true;
        } else {
            z = false;
        }
        Validate.isTrue(z, "The field must not be null", new Object[0]);
        Validate.isTrue(Modifier.isStatic(field.getModifiers()), "The field %s.%s is not static", field.getDeclaringClass().getName(), field.getName());
        writeField(field, (Object) null, value, forceAccess);
    }

    public static void writeStaticField(Class<?> cls, String fieldName, Object value) throws IllegalAccessException {
        writeStaticField(cls, fieldName, value, false);
    }

    public static void writeStaticField(Class<?> cls, String fieldName, Object value, boolean forceAccess) throws IllegalAccessException {
        boolean z;
        Field field = getField(cls, fieldName, forceAccess);
        if (field != null) {
            z = true;
        } else {
            z = false;
        }
        Validate.isTrue(z, "Cannot locate field %s on %s", fieldName, cls);
        writeStaticField(field, value, false);
    }

    public static void writeDeclaredStaticField(Class<?> cls, String fieldName, Object value) throws IllegalAccessException {
        writeDeclaredStaticField(cls, fieldName, value, false);
    }

    public static void writeDeclaredStaticField(Class<?> cls, String fieldName, Object value, boolean forceAccess) throws IllegalAccessException {
        boolean z;
        Field field = getDeclaredField(cls, fieldName, forceAccess);
        if (field != null) {
            z = true;
        } else {
            z = false;
        }
        Validate.isTrue(z, "Cannot locate declared field %s.%s", cls.getName(), fieldName);
        writeField(field, (Object) null, value, false);
    }

    public static void writeField(Field field, Object target, Object value) throws IllegalAccessException {
        writeField(field, target, value, false);
    }

    public static void writeField(Field field, Object target, Object value, boolean forceAccess) throws IllegalAccessException {
        Validate.isTrue(field != null, "The field must not be null", new Object[0]);
        if (!forceAccess || field.isAccessible()) {
            MemberUtils.setAccessibleWorkaround(field);
        } else {
            field.setAccessible(true);
        }
        field.set(target, value);
    }

    public static void removeFinalModifier(Field field) {
        removeFinalModifier(field, true);
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void removeFinalModifier(java.lang.reflect.Field r7, boolean r8) {
        /*
            r3 = 1
            r4 = 0
            if (r7 == 0) goto L_0x003d
            r2 = r3
        L_0x0005:
            java.lang.String r5 = "The field must not be null"
            java.lang.Object[] r6 = new java.lang.Object[r4]
            org.apache.commons.lang3.Validate.isTrue((boolean) r2, (java.lang.String) r5, (java.lang.Object[]) r6)
            int r2 = r7.getModifiers()     // Catch:{ NoSuchFieldException -> 0x0049, IllegalAccessException -> 0x004b }
            boolean r2 = java.lang.reflect.Modifier.isFinal(r2)     // Catch:{ NoSuchFieldException -> 0x0049, IllegalAccessException -> 0x004b }
            if (r2 == 0) goto L_0x003c
            java.lang.Class<java.lang.reflect.Field> r2 = java.lang.reflect.Field.class
            java.lang.String r5 = "modifiers"
            java.lang.reflect.Field r1 = r2.getDeclaredField(r5)     // Catch:{ NoSuchFieldException -> 0x0049, IllegalAccessException -> 0x004b }
            if (r8 == 0) goto L_0x003f
            boolean r2 = r1.isAccessible()     // Catch:{ NoSuchFieldException -> 0x0049, IllegalAccessException -> 0x004b }
            if (r2 != 0) goto L_0x003f
            r0 = r3
        L_0x0027:
            if (r0 == 0) goto L_0x002d
            r2 = 1
            r1.setAccessible(r2)     // Catch:{ NoSuchFieldException -> 0x0049, IllegalAccessException -> 0x004b }
        L_0x002d:
            int r2 = r7.getModifiers()     // Catch:{ all -> 0x0041 }
            r2 = r2 & -17
            r1.setInt(r7, r2)     // Catch:{ all -> 0x0041 }
            if (r0 == 0) goto L_0x003c
            r2 = 0
            r1.setAccessible(r2)     // Catch:{ NoSuchFieldException -> 0x0049, IllegalAccessException -> 0x004b }
        L_0x003c:
            return
        L_0x003d:
            r2 = r4
            goto L_0x0005
        L_0x003f:
            r0 = r4
            goto L_0x0027
        L_0x0041:
            r2 = move-exception
            if (r0 == 0) goto L_0x0048
            r3 = 0
            r1.setAccessible(r3)     // Catch:{ NoSuchFieldException -> 0x0049, IllegalAccessException -> 0x004b }
        L_0x0048:
            throw r2     // Catch:{ NoSuchFieldException -> 0x0049, IllegalAccessException -> 0x004b }
        L_0x0049:
            r2 = move-exception
            goto L_0x003c
        L_0x004b:
            r2 = move-exception
            goto L_0x003c
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.lang3.reflect.FieldUtils.removeFinalModifier(java.lang.reflect.Field, boolean):void");
    }

    public static void writeField(Object target, String fieldName, Object value) throws IllegalAccessException {
        writeField(target, fieldName, value, false);
    }

    public static void writeField(Object target, String fieldName, Object value, boolean forceAccess) throws IllegalAccessException {
        boolean z;
        boolean z2;
        if (target != null) {
            z = true;
        } else {
            z = false;
        }
        Validate.isTrue(z, "target object must not be null", new Object[0]);
        Class<?> cls = target.getClass();
        Field field = getField(cls, fieldName, forceAccess);
        if (field != null) {
            z2 = true;
        } else {
            z2 = false;
        }
        Validate.isTrue(z2, "Cannot locate declared field %s.%s", cls.getName(), fieldName);
        writeField(field, target, value, false);
    }

    public static void writeDeclaredField(Object target, String fieldName, Object value) throws IllegalAccessException {
        writeDeclaredField(target, fieldName, value, false);
    }

    public static void writeDeclaredField(Object target, String fieldName, Object value, boolean forceAccess) throws IllegalAccessException {
        boolean z;
        boolean z2;
        if (target != null) {
            z = true;
        } else {
            z = false;
        }
        Validate.isTrue(z, "target object must not be null", new Object[0]);
        Class<?> cls = target.getClass();
        Field field = getDeclaredField(cls, fieldName, forceAccess);
        if (field != null) {
            z2 = true;
        } else {
            z2 = false;
        }
        Validate.isTrue(z2, "Cannot locate declared field %s.%s", cls.getName(), fieldName);
        writeField(field, target, value, false);
    }
}
