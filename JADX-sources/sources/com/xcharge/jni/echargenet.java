package com.xcharge.jni;

/* loaded from: classes.dex */
public class echargenet {
    public static native synchronized byte[] aesdecrypt(byte[] bArr, byte[] bArr2, int i);

    public static native synchronized byte[] aesencrypt(byte[] bArr, byte[] bArr2, int i);

    public static native synchronized byte[] packhead(int i, int i2, int i3, int i4, int i5);

    public static native synchronized byte[] unpackhead(byte[] bArr);
}
