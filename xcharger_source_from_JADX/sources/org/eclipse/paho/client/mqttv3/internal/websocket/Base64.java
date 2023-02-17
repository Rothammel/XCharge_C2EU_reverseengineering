package org.eclipse.paho.client.mqttv3.internal.websocket;

import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

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
        return encoder.getBase64String();
    }

    public static String encodeBytes(byte[] b) {
        encoder.putByteArray("aKey", b);
        return encoder.getBase64String();
    }

    public class Base64Encoder extends AbstractPreferences {
        private String base64String = null;

        public Base64Encoder() {
            super((AbstractPreferences) null, "");
        }

        /* access modifiers changed from: protected */
        public void putSpi(String key, String value) {
            this.base64String = value;
        }

        public String getBase64String() {
            return this.base64String;
        }

        /* access modifiers changed from: protected */
        public String getSpi(String key) {
            return null;
        }

        /* access modifiers changed from: protected */
        public void removeSpi(String key) {
        }

        /* access modifiers changed from: protected */
        public void removeNodeSpi() throws BackingStoreException {
        }

        /* access modifiers changed from: protected */
        public String[] keysSpi() throws BackingStoreException {
            return null;
        }

        /* access modifiers changed from: protected */
        public String[] childrenNamesSpi() throws BackingStoreException {
            return null;
        }

        /* access modifiers changed from: protected */
        public AbstractPreferences childSpi(String name) {
            return null;
        }

        /* access modifiers changed from: protected */
        public void syncSpi() throws BackingStoreException {
        }

        /* access modifiers changed from: protected */
        public void flushSpi() throws BackingStoreException {
        }
    }
}
