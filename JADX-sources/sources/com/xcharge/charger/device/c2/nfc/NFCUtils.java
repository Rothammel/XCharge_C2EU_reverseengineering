package com.xcharge.charger.device.c2.nfc;

import android.support.v4.view.MotionEventCompat;
import android.text.TextUtils;
import android.util.Log;
import com.android.chargerhd.chargerhdNative;
import com.xcharge.charger.data.bean.ConsumeFailCache;
import com.xcharge.charger.data.bean.XKeyseed;
import com.xcharge.charger.data.bean.setting.ChargeSetting;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.bean.type.PLATFORM_CUSTOMER;
import com.xcharge.charger.data.provider.LocalSettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.data.proxy.NFCConsumeFailCacheContentProxy;
import com.xcharge.charger.data.proxy.NFCKeyContentProxy;
import com.xcharge.charger.device.c2.bean.AuthSign;
import com.xcharge.charger.device.c2.bean.CDDZCardKeySeeds;
import com.xcharge.charger.device.c2.bean.ManageCardData;
import com.xcharge.charger.device.c2.bean.NFCCardIDData;
import com.xcharge.charger.device.c2.bean.NFCSign;
import com.xcharge.charger.device.c2.bean.XSign;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.common.utils.EndianUtils;
import com.xcharge.common.utils.FileUtils;
import com.xcharge.common.utils.FormatUtils;
import com.xcharge.common.utils.MD5Utils;
import com.xcharge.jni.echargenet;
import java.util.Random;
import java.util.zip.CRC32;
import org.apache.commons.lang3.CharEncoding;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class NFCUtils {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_PLATFORM = null;
    public static final String CDDZ_MCARD_KEYA = "6f7d2k";
    public static final String CDDZ_MCARD_KEYB = "d4j8k9";
    static final String KEY_SEED_PATH = "/etc/xcharger.key";

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_PLATFORM() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_PLATFORM;
        if (iArr == null) {
            iArr = new int[CHARGE_PLATFORM.valuesCustom().length];
            try {
                iArr[CHARGE_PLATFORM.anyo.ordinal()] = 3;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[CHARGE_PLATFORM.cddz.ordinal()] = 8;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[CHARGE_PLATFORM.ecw.ordinal()] = 6;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[CHARGE_PLATFORM.ocpp.ordinal()] = 9;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[CHARGE_PLATFORM.ptne.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[CHARGE_PLATFORM.xcharge.ordinal()] = 1;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[CHARGE_PLATFORM.xconsole.ordinal()] = 2;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[CHARGE_PLATFORM.xmsz.ordinal()] = 4;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[CHARGE_PLATFORM.yzx.ordinal()] = 7;
            } catch (NoSuchFieldError e9) {
            }
            $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_PLATFORM = iArr;
        }
        return iArr;
    }

    public static byte[] getPublicAreaKeySeed() {
        byte[] keyBytes = FileUtils.readByteArrayFromFile(KEY_SEED_PATH);
        if (keyBytes == null || keyBytes.length < 48) {
            Log.e("NFCUtils.getPublicAreaKeySeed", "no keyseed in /etc/xcharger.key");
            return null;
        }
        byte[] keySeed = new byte[32];
        System.arraycopy(keyBytes, 6, keySeed, 0, keySeed.length);
        return keySeed;
    }

    public static XKeyseed getM1PrivateAreaXKeySeed() {
        byte[] keyBytes = FileUtils.readByteArrayFromFile(KEY_SEED_PATH);
        if (keyBytes == null || keyBytes.length < 70) {
            Log.e("NFCUtils.getM1PrivateAreaXKeySeed", "no keyseed in /etc/xcharger.key");
            return null;
        }
        byte[] keyID = new byte[6];
        byte[] keySeed = new byte[32];
        System.arraycopy(keyBytes, 0, keyID, 0, keyID.length);
        System.arraycopy(keyBytes, 38, keySeed, 0, keySeed.length);
        return new XKeyseed(new String(keyID), FormatUtils.bytesToHexString(keySeed));
    }

    public static XKeyseed getU3PrivateAreaXKeySeed() {
        byte[] keyBytes = FileUtils.readByteArrayFromFile(KEY_SEED_PATH);
        if (keyBytes == null || keyBytes.length < 102) {
            Log.e("NFCUtils.getU3PrivateAreaXKeySeed", "no keyseed in /etc/xcharger.key");
            return null;
        }
        byte[] keyID = new byte[6];
        byte[] keySeed = new byte[32];
        System.arraycopy(keyBytes, 0, keyID, 0, keyID.length);
        System.arraycopy(keyBytes, 70, keySeed, 0, keySeed.length);
        return new XKeyseed(new String(keyID), FormatUtils.bytesToHexString(keySeed));
    }

    public static byte[] getPrivateKey(int uuid, String cardNo, String keySeed) {
        byte[] key1 = intToBytes(uuid);
        byte[] key2 = FormatUtils.hexStringToBytes(keySeed);
        byte[] key3 = cardNo.getBytes();
        byte[] key = new byte[key1.length + key2.length + key3.length];
        System.arraycopy(key1, 0, key, 0, key1.length);
        System.arraycopy(key2, 0, key, key1.length, key2.length);
        System.arraycopy(key3, 0, key, key1.length + key2.length, key3.length);
        return key;
    }

    public static byte[] intToBytes(int i) {
        return new byte[]{(byte) (i & MotionEventCompat.ACTION_MASK), (byte) ((i >> 8) & MotionEventCompat.ACTION_MASK), (byte) ((i >> 16) & MotionEventCompat.ACTION_MASK), (byte) ((i >> 24) & MotionEventCompat.ACTION_MASK)};
    }

    public static int bytesToInt(byte[] b) {
        return (b[0] & 255) | ((b[1] & 255) << 8) | ((b[2] & 255) << 16) | ((b[3] & 255) << 24);
    }

    public static byte[] longToBytes(long l) {
        long temp = l;
        byte[] b = new byte[8];
        for (int i = 0; i < b.length; i++) {
            b[i] = Long.valueOf(255 & temp).byteValue();
            temp >>= 8;
        }
        return b;
    }

    public static long bytesToLong(byte[] b) {
        return ((b[7] & 255) << 56) | ((b[6] & 255) << 48) | ((b[5] & 255) << 40) | ((b[4] & 255) << 32) | ((b[3] & 255) << 24) | ((b[2] & 255) << 16) | ((b[1] & 255) << 8) | ((b[0] & 255) << 0);
    }

    public static NFCCardIDData distinguishCard(int uuid, String szuuid) {
        CHARGE_PLATFORM chargePlatform = SystemSettingCacheProvider.getInstance().getChargePlatform();
        if (chargePlatform == null) {
            Log.w("NFCUtils.distinguishCard", "no charge paltform setting, xcharge platform used by default !!!");
            chargePlatform = CHARGE_PLATFORM.xcharge;
        }
        String cardNo = null;
        NFC_CARD_TYPE cardType = null;
        switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_PLATFORM()[chargePlatform.ordinal()]) {
            case 1:
            case 2:
                if (PLATFORM_CUSTOMER.ct_demo.equals(SystemSettingCacheProvider.getInstance().getPlatformCustomer())) {
                    if (uuid == 1542571595 || uuid == 1543050155) {
                        cardNo = String.valueOf(Long.valueOf(uuid & XMSZHead.ID_BROADCAST));
                        cardType = NFC_CARD_TYPE.CT_DEMO;
                        break;
                    }
                } else if (uuid == 0) {
                    cardNo = "U100000000000000";
                    cardType = NFC_CARD_TYPE.U1;
                    break;
                } else {
                    cardNo = getXChargePlatformCardNo(uuid);
                    if (!TextUtils.isEmpty(cardNo)) {
                        cardType = getCardType(cardNo);
                        break;
                    }
                }
                break;
            case 3:
                cardNo = getAnyoPlatformCardNo();
                if (!TextUtils.isEmpty(cardNo)) {
                    cardType = NFC_CARD_TYPE.anyo1;
                    break;
                } else {
                    cardNo = getXChargePlatformCardNo(uuid);
                    if (!TextUtils.isEmpty(cardNo)) {
                        cardType = getPermittedXChargeCardType(cardNo);
                        break;
                    }
                }
                break;
            case 4:
                cardNo = getXChargePlatformCardNo(uuid);
                if (!TextUtils.isEmpty(cardNo)) {
                    cardType = getPermittedXChargeCardType(cardNo);
                    break;
                }
                break;
            case 5:
                cardNo = getXChargePlatformCardNo(uuid);
                if (!TextUtils.isEmpty(cardNo)) {
                    cardType = getPermittedXChargeCardType(cardNo);
                    break;
                }
                break;
            case 6:
            case 7:
            default:
                Log.w("NFCUtils.distinguishCard", "unsupported charge paltform: " + chargePlatform.getPlatform());
                return null;
            case 8:
                NFCCardIDData cddzCard = distinguishCDDZCard(uuid);
                if (cddzCard == null) {
                    cardNo = getXChargePlatformCardNo(uuid);
                    if (!TextUtils.isEmpty(cardNo)) {
                        cardType = getPermittedXChargeCardType(cardNo);
                        break;
                    }
                } else {
                    return cddzCard;
                }
                break;
            case 9:
                cardNo = getOCPPCardNo(uuid, szuuid);
                if (!TextUtils.isEmpty(cardNo)) {
                    cardType = NFC_CARD_TYPE.ocpp;
                    break;
                } else {
                    cardNo = getXChargePlatformCardNo(uuid);
                    if (!TextUtils.isEmpty(cardNo)) {
                        cardType = getPermittedXChargeCardType(cardNo);
                        break;
                    }
                }
                break;
        }
        if (cardType != null) {
            NFCCardIDData cardIDData = new NFCCardIDData();
            cardIDData.setUuid(uuid);
            cardIDData.setCardNo(cardNo);
            cardIDData.setCardType(cardType);
            return cardIDData;
        }
        Log.w("NFCUtils.distinguishCard", "unrecognized card !!! uuid: " + uuid + ", card no: " + cardNo + ", charge paltform: " + chargePlatform.getPlatform());
        return null;
    }

    private static NFCCardIDData distinguishCDDZCard(int uuid) {
        NFCCardIDData cardIDData = recognizeCDDZMCard(uuid);
        if (cardIDData != null) {
            return cardIDData;
        }
        NFCCardIDData cardIDData2 = recognizeCDDZUserCard(uuid);
        if (cardIDData2 != null) {
            return cardIDData2;
        }
        Log.w("NFCUtils.distinguishCDDZCard", "not cddz card !!! uuid: " + uuid);
        return null;
    }

    private static NFCCardIDData recognizeCDDZMCard(int uuid) {
        byte[] data;
        try {
            byte[] key = CDDZ_MCARD_KEYA.getBytes(CharEncoding.UTF_8);
            Log.d("NFCUtils.recognizeCDDZMCard", "key: " + FormatUtils.bytesToHexString(key));
            data = chargerhdNative.chargerhdNFCRead(1, key);
        } catch (Exception e) {
            Log.e("NFCUtils.recognizeCDDZMCard", "except: " + Log.getStackTraceString(e));
        }
        if (data != null) {
            Log.d("NFCUtils.recognizeCDDZMCard", "block-1: " + FormatUtils.bytesToHexString(data) + ", len: " + data.length);
            byte[] cardNoBytes = new byte[8];
            System.arraycopy(data, 0, cardNoBytes, 0, 8);
            if (EndianUtils.littleBytesToLong(cardNoBytes) == 0) {
                Log.w("NFCUtils.recognizeCDDZMCard", "illegal cardNo: " + FormatUtils.bytesToHexString(cardNoBytes) + ", uuid: " + uuid);
                return null;
            }
            String cardNo = new String(cardNoBytes, CharEncoding.UTF_8);
            NFCCardIDData cardIDData = new NFCCardIDData();
            cardIDData.setUuid(uuid);
            cardIDData.setCardNo(cardNo);
            cardIDData.setCardType(NFC_CARD_TYPE.cddz_m);
            return cardIDData;
        }
        Log.w("NFCUtils.recognizeCDDZMCard", "not miyao card, uuid: " + uuid);
        return null;
    }

    private static NFCCardIDData recognizeCDDZUserCard(int uuid) {
        String operatorCode;
        NFC_CARD_TYPE cardType;
        try {
            ChargeSetting chargeSetting = LocalSettingCacheProvider.getInstance().getChargeSetting();
            operatorCode = chargeSetting.getOperatorId();
        } catch (Exception e) {
            Log.e("NFCUtils.recognizeCDDZUserCard", "except: " + Log.getStackTraceString(e));
        }
        if (TextUtils.isEmpty(operatorCode)) {
            Log.w("NFCUtils.recognizeCDDZUserCard", "not config operator, swipe miyao card first, uuid: " + uuid);
            return null;
        }
        XKeyseed xKeyseed = NFCKeyContentProxy.getInstance().getKeyseed(operatorCode, NFC_CARD_TYPE.cddz_m.getType());
        if (xKeyseed == null || TextUtils.isEmpty(xKeyseed.getSeed())) {
            Log.w("NFCUtils.recognizeCDDZUserCard", "unavailable key seed for operator: " + operatorCode);
            return null;
        }
        CDDZCardKeySeeds cddzCardKeySeeds = new CDDZCardKeySeeds().fromJson(xKeyseed.getSeed());
        if (cddzCardKeySeeds == null) {
            Log.w("NFCUtils.recognizeCDDZUserCard", "illegal key seed: " + xKeyseed.getSeed() + " for operator: " + operatorCode);
            return null;
        }
        Log.d("NFCUtils.recognizeCDDZUserCard", "key seeds: " + cddzCardKeySeeds.toJson());
        byte[] sSrc = new byte[16];
        byte[] uuidBytes = EndianUtils.intToLittleBytes(uuid);
        System.arraycopy(uuidBytes, 0, sSrc, 0, uuidBytes.length);
        byte[] sKey = FormatUtils.hexStringToBytes(cddzCardKeySeeds.getSeedA());
        byte[] rawkey = echargenet.aesencrypt(sKey, sSrc, 1);
        if (rawkey == null || rawkey.length == 0) {
            Log.w("NFCUtils.recognizeCDDZUserCard", "aes encrypt error, , sSrc: " + FormatUtils.bytesToHexString(sSrc) + ", sKey: " + FormatUtils.bytesToHexString(sKey));
            return null;
        }
        Log.d("NFCUtils.recognizeCDDZUserCard", "AES encrypted: " + FormatUtils.bytesToHexString(rawkey) + ", sSrc: " + FormatUtils.bytesToHexString(sSrc) + ", sKey: " + FormatUtils.bytesToHexString(sKey));
        byte[] decrypted = echargenet.aesdecrypt(sKey, rawkey, 1);
        Log.d("NFCUtils.recognizeCDDZUserCard", "AES decrypted: " + FormatUtils.bytesToHexString(decrypted));
        byte[] key = new byte[6];
        System.arraycopy(rawkey, 0, key, 0, 6);
        Log.d("NFCUtils.recognizeCDDZUserCard", "key: " + FormatUtils.bytesToHexString(key));
        byte[] data = chargerhdNative.chargerhdNFCRead(9, key);
        if (data != null) {
            Log.d("NFCUtils.recognizeCDDZUserCard", "block-9: " + FormatUtils.bytesToHexString(data) + ", len: " + data.length);
            int ktType = data[0];
            if (18 == ktType) {
                cardType = NFC_CARD_TYPE.cddz_1;
            } else if (17 != ktType) {
                if (19 == ktType) {
                    Log.w("NFCUtils.recognizeCDDZUserCard", "user card not enabled, uuid: " + uuid);
                    return null;
                }
                Log.w("NFCUtils.recognizeCDDZUserCard", "illegal user card type: " + ktType + ", uuid: " + uuid);
                return null;
            } else {
                cardType = NFC_CARD_TYPE.cddz_2;
            }
            byte[] cardNoBytes = new byte[8];
            System.arraycopy(data, 1, cardNoBytes, 0, 8);
            String cardNo = new String(cardNoBytes, CharEncoding.UTF_8);
            byte[] expireTimeBytes = new byte[4];
            System.arraycopy(data, 10, expireTimeBytes, 0, 4);
            long expireTime = EndianUtils.littleBytesToInt(expireTimeBytes) & XMSZHead.ID_BROADCAST;
            if (1000 * expireTime < System.currentTimeMillis()) {
                Log.w("NFCUtils.recognizeCDDZUserCard", "user card is expired, uuid: " + uuid + ", cardNo: " + cardNo);
                return null;
            }
            NFCCardIDData cardIDData = new NFCCardIDData();
            cardIDData.setUuid(uuid);
            cardIDData.setCardNo(cardNo);
            cardIDData.setCardType(cardType);
            Log.d("NFCUtils.recognizeCDDZUserCard", "NFCCardIDData: " + cardIDData.toJson());
            return cardIDData;
        }
        Log.w("NFCUtils.recognizeCDDZUserCard", "not user card, uuid: " + uuid);
        return null;
    }

    private static String getXChargePlatformCardNo(int uuid) {
        byte[] keyseed = getPublicAreaKeySeed();
        if (keyseed == null) {
            return null;
        }
        byte[] uuidBytes = intToBytes(uuid);
        byte[] key = new byte[uuidBytes.length + keyseed.length];
        System.arraycopy(uuidBytes, 0, key, 0, uuidBytes.length);
        System.arraycopy(keyseed, 0, key, uuidBytes.length, keyseed.length);
        String reply = chargerhdNative.chargerhdCardSerialNumber(key);
        Log.i("NFCUtils.getXChargePlatformCardNo", "key: " + FormatUtils.bytesToHexString(key) + ", reply: " + reply);
        if (TextUtils.isEmpty(reply)) {
            return null;
        }
        try {
            JSONObject json = new JSONObject(reply);
            if (json.has("serialNum")) {
                return json.getString("serialNum");
            }
            return null;
        } catch (Exception e) {
            Log.e("NFCUtils.getXChargePlatformCardNo", Log.getStackTraceString(e));
            return null;
        }
    }

    private static String getAnyoPlatformCardNo() {
        byte[] key = {1, MqttWireMessage.MESSAGE_TYPE_DISCONNECT, AnyoMessage.CMD_REPORT_EVENT, 35, 26, 27};
        String reply = chargerhdNative.chargerhdAnyueCardSerialNumber(key);
        Log.i("NFCUtils.getAnyoPlatformCardNo", "key: " + FormatUtils.bytesToHexString(key) + ", reply: " + reply);
        if (TextUtils.isEmpty(reply)) {
            return null;
        }
        try {
            JSONObject json = new JSONObject(reply);
            if (json.has("serialNum")) {
                return json.getString("serialNum");
            }
            return null;
        } catch (Exception e) {
            Log.e("NFCUtils.getAnyoPlatformCardNo", Log.getStackTraceString(e));
            return null;
        }
    }

    private static String getOCPPCardNo(int uuid, String szuuid) {
        return szuuid;
    }

    private static NFC_CARD_TYPE getCardType(String cardNo) {
        String type = cardNo.substring(0, 2);
        return NFC_CARD_TYPE.valueOf(type);
    }

    private static NFC_CARD_TYPE getPermittedXChargeCardType(String cardNo) {
        String type = cardNo.substring(0, 2);
        NFC_CARD_TYPE cardType = NFC_CARD_TYPE.valueOf(type);
        if (NFC_CARD_TYPE.M1.equals(cardType) || NFC_CARD_TYPE.M2.equals(cardType) || NFC_CARD_TYPE.U1.equals(cardType)) {
            return cardType;
        }
        return null;
    }

    public static ManageCardData getManageCardInfo(byte[] key, String type) {
        String reply = chargerhdNative.chargerhdMangerCardInfo(key, type);
        Log.i("NFCUtils.getManageCardInfo", "type: " + type + ", key: " + FormatUtils.bytesToHexString(key) + ", reply: " + reply);
        if (TextUtils.isEmpty(reply) || "{}".equals(reply)) {
            return null;
        }
        return new ManageCardData().fromJson(reply);
    }

    public static XSign getUserCardSign(byte[] key) {
        try {
            String reply = chargerhdNative.chargerhdGetUserCardSign(key);
            Log.i("NFCUtils.getUserCardSign", "key: " + FormatUtils.bytesToHexString(key) + ", reply: " + reply);
            if (TextUtils.isEmpty(reply) || "{}".equals(reply)) {
                return null;
            }
            NFCSign nfcSign = new NFCSign().fromJson(reply);
            return XSign.parse(nfcSign);
        } catch (Exception e) {
            Log.e("NFCUtils.getUserCardSign", Log.getStackTraceString(e));
            return null;
        }
    }

    public static AuthSign getAuthSign(byte[] key) {
        try {
            String reply = chargerhdNative.chargerhdGetUserCardSign(key);
            Log.i("NFCUtils.getU3CardSign", "key: " + FormatUtils.bytesToHexString(key) + ", reply: " + reply);
            if (TextUtils.isEmpty(reply) || "{}".equals(reply)) {
                return null;
            }
            NFCSign nfcSign = new NFCSign().fromJson(reply);
            return AuthSign.parse(nfcSign);
        } catch (Exception e) {
            Log.e("NFCUtils.getU3CardSign", Log.getStackTraceString(e));
            return null;
        }
    }

    public static boolean setUserCardSign(byte[] key, NFCSign sign) {
        return chargerhdNative.chargerhdSetUserCardSign(key, sign.toJson());
    }

    public static NFCSign signU1(byte[] sn, byte[] seed) {
        byte[] data = new byte[16];
        long timestamp = System.currentTimeMillis() / 1000;
        int rand = new Random().nextInt(9999);
        System.arraycopy(longToBytes(timestamp), 0, data, 0, 4);
        System.arraycopy(intToBytes(rand), 0, data, 4, 4);
        NFCSign nfcSign = new NFCSign();
        nfcSign.setSign(getU1Sign(sn, seed, data));
        nfcSign.setData(FormatUtils.bytesToHexString(data));
        return nfcSign;
    }

    public static boolean checkU1Sign(byte[] sn, byte[] seed, XSign sign) {
        if (sign == null || TextUtils.isEmpty(sign.getSign()) || TextUtils.isEmpty(sign.getData())) {
            return false;
        }
        return getU1Sign(sn, seed, FormatUtils.hexStringToBytes(sign.getData())).equals(sign.getSign().toUpperCase());
    }

    private static String getU1Sign(byte[] sn, byte[] seed, byte[] data) {
        byte[] sign = new byte[sn.length + seed.length + data.length];
        System.arraycopy(sn, 0, sign, 0, sn.length);
        System.arraycopy(seed, 0, sign, sn.length, seed.length);
        System.arraycopy(data, 0, sign, sn.length + seed.length, data.length);
        return MD5Utils.MD5(sign);
    }

    public static String getGroupID(String cardNo) {
        return cardNo.substring(2, 8);
    }

    public static int getU2CardBalance(byte[] key) {
        return chargerhdNative.chargerhdConsumerCardBalance(key);
    }

    public static NFCSign signU2(byte[] money, byte[] key, int count, byte[] sn) {
        byte[] data = new byte[16];
        int timestamp = (int) (System.currentTimeMillis() / 1000);
        int rand = new Random().nextInt(9999);
        System.arraycopy(intToBytes(timestamp), 0, data, 0, 4);
        System.arraycopy(intToBytes(rand), 0, data, 4, 4);
        data[8] = (byte) count;
        NFCSign nfcSign = new NFCSign();
        byte[] crcSn = null;
        if (sn != null) {
            CRC32 snCrc32 = new CRC32();
            snCrc32.update(sn);
            crcSn = longToBytes(snCrc32.getValue());
        }
        nfcSign.setSign(getU2Sign(money, key, data, crcSn));
        nfcSign.setData(FormatUtils.bytesToHexString(data));
        return nfcSign;
    }

    public static String getU2Sign(byte[] money, byte[] key, byte[] data, byte[] sn) {
        int length = money.length + key.length + data.length;
        if (sn != null) {
            length += sn.length;
        }
        byte[] sign = new byte[length];
        System.arraycopy(money, 0, sign, 0, money.length);
        System.arraycopy(key, 0, sign, money.length, key.length);
        System.arraycopy(data, 0, sign, money.length + key.length, data.length);
        if (sn != null) {
            System.arraycopy(sn, 0, sign, money.length + key.length + data.length, sn.length);
        }
        return MD5Utils.MD5(sign);
    }

    public static boolean checkU2Sign(byte[] money, byte[] key, XSign sign, byte[] sn) {
        if (sign == null || TextUtils.isEmpty(sign.getSign()) || TextUtils.isEmpty(sign.getData())) {
            return false;
        }
        byte[] data = FormatUtils.hexStringToBytes(sign.getData());
        if (sn == null) {
            String signWithoutSN = getU2Sign(money, key, data, null);
            if (signWithoutSN.equals(sign.getSign().toUpperCase())) {
                return true;
            }
        } else {
            CRC32 snCrc32 = new CRC32();
            snCrc32.update(sn);
            String signWithSN = getU2Sign(money, key, data, longToBytes(snCrc32.getValue()));
            if (signWithSN.equals(sign.getSign().toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    public static boolean consumeU2(int money, int fee, byte[] key, String uuid, String cardNo, int count) {
        int newBalance = money - fee;
        if (!setUserCardSign(key, signU2(intToBytes(newBalance), key, 1, null))) {
            Log.w("NFCUtils.consumeU2", "failed to set consume sign !!!");
            return false;
        }
        boolean isOk = chargerhdNative.chargerhdConsumerCardAmount(key, fee);
        if (!isOk) {
            Log.w("NFCUtils.consumeU2", "failed to consume !!!");
            ConsumeFailCache data = new ConsumeFailCache();
            data.setBalance(money);
            data.setNfc_type(NFC_CARD_TYPE.U2);
            data.setUuid(uuid);
            data.setCard_no(cardNo);
            data.setCount(count);
            data.setUpdate_time(System.currentTimeMillis());
            NFCConsumeFailCacheContentProxy.getInstance().saveConsumeFailCache(data);
            return isOk;
        }
        return isOk;
    }
}
