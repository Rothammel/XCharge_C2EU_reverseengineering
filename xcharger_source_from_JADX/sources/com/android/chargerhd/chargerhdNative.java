package com.android.chargerhd;

public class chargerhdNative {
    public static native synchronized String chargerhdAnyueCardSerialNumber(byte[] bArr);

    public static native synchronized String chargerhdCardSerialNumber(byte[] bArr);

    public static native synchronized boolean chargerhdConsumerCardAmount(byte[] bArr, int i);

    public static native synchronized int chargerhdConsumerCardBalance(byte[] bArr);

    public static native synchronized String chargerhdConsumerCardInfo(byte[] bArr);

    public static native synchronized String chargerhdControl(String str);

    public static native synchronized boolean chargerhdControlAsync(String str);

    public static native synchronized String chargerhdGetUserCardSign(byte[] bArr);

    public static native synchronized String chargerhdMangerCardInfo(byte[] bArr, String str);

    public static native synchronized byte[] chargerhdNFCRead(int i, byte[] bArr);

    public static native synchronized boolean chargerhdNFCWrite(int i, byte[] bArr, byte[] bArr2);

    public static native synchronized boolean chargerhdSetUserCardSign(byte[] bArr, String str);

    public static native synchronized String chargerhdVersion();
}
