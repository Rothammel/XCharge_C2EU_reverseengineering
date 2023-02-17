package net.xcharger.sdk.device;

/* loaded from: classes.dex */
public interface MessageHandler {
    void onDisconnected();

    void onMessage(String str, String str2, String str3, String str4);
}