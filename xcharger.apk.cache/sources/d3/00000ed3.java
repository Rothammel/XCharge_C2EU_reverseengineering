package org.eclipse.paho.client.mqttv3.internal.websocket;

import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

/* loaded from: classes.dex */
public class Base64 {
    private static final Base64Encoder encoder;
    private static final Base64 instance = new Base64();

    static {
        Base64 base64 = instance;
        base64.getClass();
        encoder = new Base64Encoder();
    }

    public static String encode(String s) {
        encoder.putByteArray("akey", s.getBytes());
        String result = encoder.getBase64String();
        return result;
    }

    public static String encodeBytes(byte[] b) {
        encoder.putByteArray("aKey", b);
        String result = encoder.getBase64String();
        return result;
    }

    /* loaded from: classes.dex */
    public class Base64Encoder extends AbstractPreferences {
        private String base64String;

        public Base64Encoder() {
            super(null, "");
            this.base64String = null;
        }

        @Override // java.util.prefs.AbstractPreferences
        protected void putSpi(String key, String value) {
            this.base64String = value;
        }

        public String getBase64String() {
            return this.base64String;
        }

        @Override // java.util.prefs.AbstractPreferences
        protected String getSpi(String key) {
            return null;
        }

        @Override // java.util.prefs.AbstractPreferences
        protected void removeSpi(String key) {
        }

        @Override // java.util.prefs.AbstractPreferences
        protected void removeNodeSpi() throws BackingStoreException {
        }

        @Override // java.util.prefs.AbstractPreferences
        protected String[] keysSpi() throws BackingStoreException {
            return null;
        }

        @Override // java.util.prefs.AbstractPreferences
        protected String[] childrenNamesSpi() throws BackingStoreException {
            return null;
        }

        @Override // java.util.prefs.AbstractPreferences
        protected AbstractPreferences childSpi(String name) {
            return null;
        }

        @Override // java.util.prefs.AbstractPreferences
        protected void syncSpi() throws BackingStoreException {
        }

        @Override // java.util.prefs.AbstractPreferences
        protected void flushSpi() throws BackingStoreException {
        }
    }
}