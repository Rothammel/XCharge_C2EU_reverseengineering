package org.apache.mina.proxy.handlers.http.ntlm;

import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import java.security.Key;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.lang3.CharEncoding;
import org.apache.mina.proxy.handlers.socks.SocksProxyConstants;

public class NTLMResponses {
    public static final byte[] LM_HASH_MAGIC_CONSTANT = {75, 71, AnyoMessage.CMD_CANCEL_RESERVE_PORT, AnyoMessage.CMD_REPORT_UPGRADE_DOWNLOAD_COMPLETE, AnyoMessage.CMD_QUERY_CHARGE_SETTING, 35, 36, 37};

    public static byte[] getLMResponse(String password, byte[] challenge) throws Exception {
        return lmResponse(lmHash(password), challenge);
    }

    public static byte[] getNTLMResponse(String password, byte[] challenge) throws Exception {
        return lmResponse(ntlmHash(password), challenge);
    }

    public static byte[] getNTLMv2Response(String target, String user, String password, byte[] targetInformation, byte[] challenge, byte[] clientNonce) throws Exception {
        return getNTLMv2Response(target, user, password, targetInformation, challenge, clientNonce, System.currentTimeMillis());
    }

    public static byte[] getNTLMv2Response(String target, String user, String password, byte[] targetInformation, byte[] challenge, byte[] clientNonce, long time) throws Exception {
        return lmv2Response(ntlmv2Hash(target, user, password), createBlob(targetInformation, clientNonce, time), challenge);
    }

    public static byte[] getLMv2Response(String target, String user, String password, byte[] challenge, byte[] clientNonce) throws Exception {
        return lmv2Response(ntlmv2Hash(target, user, password), clientNonce, challenge);
    }

    public static byte[] getNTLM2SessionResponse(String password, byte[] challenge, byte[] clientNonce) throws Exception {
        byte[] ntlmHash = ntlmHash(password);
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(challenge);
        md5.update(clientNonce);
        byte[] sessionHash = new byte[8];
        System.arraycopy(md5.digest(), 0, sessionHash, 0, 8);
        return lmResponse(ntlmHash, sessionHash);
    }

    private static byte[] lmHash(String password) throws Exception {
        byte[] oemPassword = password.toUpperCase().getBytes(CharEncoding.US_ASCII);
        byte[] keyBytes = new byte[14];
        System.arraycopy(oemPassword, 0, keyBytes, 0, Math.min(oemPassword.length, 14));
        Key lowKey = createDESKey(keyBytes, 0);
        Key highKey = createDESKey(keyBytes, 7);
        Cipher des = Cipher.getInstance("DES/ECB/NoPadding");
        des.init(1, lowKey);
        byte[] lowHash = des.doFinal(LM_HASH_MAGIC_CONSTANT);
        des.init(1, highKey);
        byte[] highHash = des.doFinal(LM_HASH_MAGIC_CONSTANT);
        byte[] lmHash = new byte[16];
        System.arraycopy(lowHash, 0, lmHash, 0, 8);
        System.arraycopy(highHash, 0, lmHash, 8, 8);
        return lmHash;
    }

    private static byte[] ntlmHash(String password) throws Exception {
        return MessageDigest.getInstance("MD4").digest(password.getBytes("UnicodeLittleUnmarked"));
    }

    private static byte[] ntlmv2Hash(String target, String user, String password) throws Exception {
        return hmacMD5((user.toUpperCase() + target).getBytes("UnicodeLittleUnmarked"), ntlmHash(password));
    }

    private static byte[] lmResponse(byte[] hash, byte[] challenge) throws Exception {
        byte[] keyBytes = new byte[21];
        System.arraycopy(hash, 0, keyBytes, 0, 16);
        Key lowKey = createDESKey(keyBytes, 0);
        Key middleKey = createDESKey(keyBytes, 7);
        Key highKey = createDESKey(keyBytes, 14);
        Cipher des = Cipher.getInstance("DES/ECB/NoPadding");
        des.init(1, lowKey);
        byte[] lowResponse = des.doFinal(challenge);
        des.init(1, middleKey);
        byte[] middleResponse = des.doFinal(challenge);
        des.init(1, highKey);
        byte[] highResponse = des.doFinal(challenge);
        byte[] lmResponse = new byte[24];
        System.arraycopy(lowResponse, 0, lmResponse, 0, 8);
        System.arraycopy(middleResponse, 0, lmResponse, 8, 8);
        System.arraycopy(highResponse, 0, lmResponse, 16, 8);
        return lmResponse;
    }

    private static byte[] lmv2Response(byte[] hash, byte[] clientData, byte[] challenge) throws Exception {
        byte[] data = new byte[(challenge.length + clientData.length)];
        System.arraycopy(challenge, 0, data, 0, challenge.length);
        System.arraycopy(clientData, 0, data, challenge.length, clientData.length);
        byte[] mac = hmacMD5(data, hash);
        byte[] lmv2Response = new byte[(mac.length + clientData.length)];
        System.arraycopy(mac, 0, lmv2Response, 0, mac.length);
        System.arraycopy(clientData, 0, lmv2Response, mac.length, clientData.length);
        return lmv2Response;
    }

    private static byte[] createBlob(byte[] targetInformation, byte[] clientNonce, long time) {
        byte[] blobSignature = {1, 1, 0, 0};
        byte[] reserved = {0, 0, 0, 0};
        byte[] unknown1 = {0, 0, 0, 0};
        byte[] unknown2 = {0, 0, 0, 0};
        long time2 = (time + 11644473600000L) * 10000;
        byte[] timestamp = new byte[8];
        for (int i = 0; i < 8; i++) {
            timestamp[i] = (byte) ((int) time2);
            time2 >>>= 8;
        }
        byte[] blob = new byte[(blobSignature.length + reserved.length + timestamp.length + clientNonce.length + unknown1.length + targetInformation.length + unknown2.length)];
        System.arraycopy(blobSignature, 0, blob, 0, blobSignature.length);
        int offset = 0 + blobSignature.length;
        System.arraycopy(reserved, 0, blob, offset, reserved.length);
        int offset2 = offset + reserved.length;
        System.arraycopy(timestamp, 0, blob, offset2, timestamp.length);
        int offset3 = offset2 + timestamp.length;
        System.arraycopy(clientNonce, 0, blob, offset3, clientNonce.length);
        int offset4 = offset3 + clientNonce.length;
        System.arraycopy(unknown1, 0, blob, offset4, unknown1.length);
        int offset5 = offset4 + unknown1.length;
        System.arraycopy(targetInformation, 0, blob, offset5, targetInformation.length);
        System.arraycopy(unknown2, 0, blob, offset5 + targetInformation.length, unknown2.length);
        return blob;
    }

    public static byte[] hmacMD5(byte[] data, byte[] key) throws Exception {
        byte[] ipad = new byte[64];
        byte[] opad = new byte[64];
        for (int i = 0; i < 64; i++) {
            if (i < key.length) {
                ipad[i] = (byte) (key[i] ^ AnyoMessage.CMD_QUERY_SYS_INFO);
                opad[i] = (byte) (key[i] ^ SocksProxyConstants.V4_REPLY_REQUEST_FAILED_NO_IDENTD);
            } else {
                ipad[i] = AnyoMessage.CMD_QUERY_SYS_INFO;
                opad[i] = SocksProxyConstants.V4_REPLY_REQUEST_FAILED_NO_IDENTD;
            }
        }
        byte[] content = new byte[(data.length + 64)];
        System.arraycopy(ipad, 0, content, 0, 64);
        System.arraycopy(data, 0, content, 64, data.length);
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] data2 = md5.digest(content);
        byte[] content2 = new byte[(data2.length + 64)];
        System.arraycopy(opad, 0, content2, 0, 64);
        System.arraycopy(data2, 0, content2, 64, data2.length);
        return md5.digest(content2);
    }

    private static Key createDESKey(byte[] bytes, int offset) {
        byte[] keyBytes = new byte[7];
        System.arraycopy(bytes, offset, keyBytes, 0, 7);
        byte[] material = {keyBytes[0], (byte) ((keyBytes[0] << 7) | ((keyBytes[1] & 255) >>> 1)), (byte) ((keyBytes[1] << 6) | ((keyBytes[2] & 255) >>> 2)), (byte) ((keyBytes[2] << 5) | ((keyBytes[3] & 255) >>> 3)), (byte) ((keyBytes[3] << 4) | ((keyBytes[4] & 255) >>> 4)), (byte) ((keyBytes[4] << 3) | ((keyBytes[5] & 255) >>> 5)), (byte) ((keyBytes[5] << 2) | ((keyBytes[6] & 255) >>> 6)), (byte) (keyBytes[6] << 1)};
        oddParity(material);
        return new SecretKeySpec(material, "DES");
    }

    private static void oddParity(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            if (((((((((b >>> 7) ^ (b >>> 6)) ^ (b >>> 5)) ^ (b >>> 4)) ^ (b >>> 3)) ^ (b >>> 2)) ^ (b >>> 1)) & 1) == 0) {
                bytes[i] = (byte) (bytes[i] | 1);
            } else {
                bytes[i] = (byte) (bytes[i] & -2);
            }
        }
    }
}
