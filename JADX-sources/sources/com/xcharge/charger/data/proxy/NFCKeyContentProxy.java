package com.xcharge.charger.data.proxy;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.data.bean.XKeyseed;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.common.utils.ContextUtils;
import com.xcharge.common.utils.DESUtils;
import com.xcharge.common.utils.MD5Utils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class NFCKeyContentProxy {
    private static NFCKeyContentProxy instance = null;
    private Context context = null;
    private String DESKey = null;
    private ContentResolver resolver = null;
    private Uri tbUri = null;

    public static NFCKeyContentProxy getInstance() {
        if (instance == null) {
            instance = new NFCKeyContentProxy();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        this.DESKey = ContextUtils.getAPPSignature(this.context);
        this.resolver = this.context.getContentResolver();
        this.tbUri = Uri.parse("content://com.xcharge.charger.data.provider.content/tb_nfc_key");
    }

    public void destroy() {
    }

    public List<XKeyseed> getAllKeyseed() {
        Cursor cursor = null;
        try {
            try {
                List<XKeyseed> all = new ArrayList<>();
                cursor = this.resolver.query(this.tbUri, null, null, null, null);
                while (cursor.moveToNext()) {
                    String seed = cursor.getString(cursor.getColumnIndexOrThrow("key"));
                    String seed2 = DESUtils.decrypt(seed, this.DESKey);
                    String groupId = cursor.getString(cursor.getColumnIndexOrThrow(ContentDB.NFCKeyTable.GROUP_ID));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow("nfc_type"));
                    XKeyseed xKeyseed = new XKeyseed(groupId, seed2);
                    xKeyseed.setId(cursor.getString(cursor.getColumnIndexOrThrow("_id")));
                    xKeyseed.setType(type);
                    all.add(xKeyseed);
                }
                if (cursor != null) {
                    cursor.close();
                    return all;
                }
                return all;
            } catch (Exception e) {
                Log.e("NFCKeyContentProxy.getAllKeyseed", Log.getStackTraceString(e));
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public XKeyseed getKeyseed(String groupId, String type) {
        String[] selectionArgs = {groupId, type};
        Cursor cursor = null;
        try {
            try {
                cursor = this.resolver.query(this.tbUri, null, "group_id=? and nfc_type=?", selectionArgs, null);
            } catch (Exception e) {
                Log.e("NFCKeyContentProxy.getKeyseed", Log.getStackTraceString(e));
                if (cursor != null) {
                    cursor.close();
                }
            }
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            String seed = cursor.getString(cursor.getColumnIndexOrThrow("key"));
            XKeyseed xKeyseed = new XKeyseed(groupId, DESUtils.decrypt(seed, this.DESKey));
            xKeyseed.setId(cursor.getString(cursor.getColumnIndexOrThrow("_id")));
            xKeyseed.setType(type);
            if (cursor != null) {
                cursor.close();
                return xKeyseed;
            }
            return xKeyseed;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public boolean saveKeyseed(String groupId, String seed, String type) {
        try {
            String encryptSeed = DESUtils.encrypt(seed, this.DESKey);
            ContentValues values = new ContentValues();
            values.put(ContentDB.NFCKeyTable.GROUP_ID, groupId);
            values.put("key", encryptSeed);
            values.put("nfc_type", type);
            XKeyseed xKeyseed = getKeyseed(groupId, type);
            if (xKeyseed != null) {
                Uri uri = ContentUris.withAppendedId(this.tbUri, Long.parseLong(xKeyseed.getId()));
                int count = this.resolver.update(uri, values, null, null);
                if (count > 0) {
                    return true;
                }
            } else {
                Uri uri2 = this.resolver.insert(this.tbUri, values);
                if (uri2 != null) {
                    return true;
                }
            }
            Log.e("NFCKeyContentProxy.saveKeyseed", "failed to save keyseed: " + seed + ", type: " + type + ", group: " + groupId);
            return false;
        } catch (Exception e) {
            Log.e("NFCKeyContentProxy.saveKeyseed", Log.getStackTraceString(e));
            return false;
        }
    }

    public boolean clearAllKeyseed() {
        int count = this.resolver.delete(this.tbUri, null, null);
        return count > 0;
    }

    public boolean clearKeyseed(String groupId, String type) {
        String selection = "group_id=?";
        String[] selectionArgs = {groupId};
        if (!TextUtils.isEmpty(type)) {
            selection = String.valueOf("group_id=?") + " and nfc_type=?";
            selectionArgs = new String[]{groupId, type};
        }
        int count = this.resolver.delete(this.tbUri, selection, selectionArgs);
        return count > 0;
    }

    public boolean updateKeyseedFromOldVerDb() {
        String cipher = MD5Utils.MD5("308204a830820390a003020102020900b3998086d056cffa300d06092a864886f70d0101040500308194310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e20566965773110300e060355040a1307416e64726f69643110300e060355040b1307416e64726f69643110300e06035504031307416e64726f69643122302006092a864886f70d0109011613616e64726f696440616e64726f69642e636f6d301e170d3038303431353232343035305a170d3335303930313232343035305a308194310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e20566965773110300e060355040a1307416e64726f69643110300e060355040b1307416e64726f69643110300e06035504031307416e64726f69643122302006092a864886f70d0109011613616e64726f696440616e64726f69642e636f6d30820120300d06092a864886f70d01010105000382010d003082010802820101009c780592ac0d5d381cdeaa65ecc8a6006e36480c6d7207b12011be50863aabe2b55d009adf7146d6f2202280c7cd4d7bdb26243b8a806c26b34b137523a49268224904dc01493e7c0acf1a05c874f69b037b60309d9074d24280e16bad2a8734361951eaf72a482d09b204b1875e12ac98c1aa773d6800b9eafde56d58bed8e8da16f9a360099c37a834a6dfedb7b6b44a049e07a269fccf2c5496f2cf36d64df90a3b8d8f34a3baab4cf53371ab27719b3ba58754ad0c53fc14e1db45d51e234fbbe93c9ba4edf9ce54261350ec535607bf69a2ff4aa07db5f7ea200d09a6c1b49e21402f89ed1190893aab5a9180f152e82f85a45753cf5fc19071c5eec827020103a381fc3081f9301d0603551d0e041604144fe4a0b3dd9cba29f71d7287c4e7c38f2086c2993081c90603551d230481c13081be80144fe4a0b3dd9cba29f71d7287c4e7c38f2086c299a1819aa48197308194310b3009060355040613025553311330110603550408130a43616c69666f726e6961311630140603550407130d4d6f756e7461696e20566965773110300e060355040a1307416e64726f69643110300e060355040b1307416e64726f69643110300e06035504031307416e64726f69643122302006092a864886f70d0109011613616e64726f696440616e64726f69642e636f6d820900b3998086d056cffa300c0603551d13040530030101ff300d06092a864886f70d01010405000382010100572551b8d93a1f73de0f6d469f86dad6701400293c88a0cd7cd778b73dafcc197fab76e6212e56c1c761cfc42fd733de52c50ae08814cefc0a3b5a1a4346054d829f1d82b42b2048bf88b5d14929ef85f60edd12d72d55657e22e3e85d04c831d613d19938bb8982247fa321256ba12d1d6a8f92ea1db1c373317ba0c037f0d1aff645aef224979fba6e7a14bc025c71b98138cef3ddfc059617cf24845cf7b40d6382f7275ed738495ab6e5931b9421765c491b72fb68e080dbdb58c2029d347c8b328ce43ef6a8b15533edfbe989bd6a48dd4b202eda94c6ab8dd5b8399203daae2ed446232e4fe9bd961394c6300e5138e3cfd285e6e4e483538cb8b1b357");
        Cursor cursor = null;
        ArrayList<XKeyseed> keyseedList = new ArrayList<>();
        try {
            try {
                cursor = this.resolver.query(this.tbUri, null, null, null, null);
                while (cursor != null) {
                    if (!cursor.moveToNext()) {
                        break;
                    }
                    String seed = cursor.getString(cursor.getColumnIndexOrThrow("key"));
                    XKeyseed xKeyseed = new XKeyseed(cursor.getString(cursor.getColumnIndexOrThrow(ContentDB.NFCKeyTable.GROUP_ID)), DESUtils.decrypt(seed, cipher));
                    xKeyseed.setId(cursor.getString(cursor.getColumnIndexOrThrow("_id")));
                    xKeyseed.setType(cursor.getString(cursor.getColumnIndexOrThrow("nfc_type")));
                    keyseedList.add(xKeyseed);
                }
                if (cursor != null) {
                    cursor.close();
                }
                Log.i("NFCKeyContentProxy.updateKeyseedFromOldVerDb", "update item num: " + keyseedList.size());
                Iterator<XKeyseed> it2 = keyseedList.iterator();
                while (it2.hasNext()) {
                    XKeyseed item = it2.next();
                    boolean isOk = updateKeyseed(item);
                    if (!isOk) {
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                Log.e("NFCKeyContentProxy.updateKeyseedFromOldVerDb", Log.getStackTraceString(e));
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public boolean updateKeyseed(XKeyseed xKeyseed) {
        try {
            String encryptSeed = DESUtils.encrypt(xKeyseed.getSeed(), this.DESKey);
            ContentValues values = new ContentValues();
            values.put(ContentDB.NFCKeyTable.GROUP_ID, xKeyseed.getGroup());
            values.put("key", encryptSeed);
            values.put("nfc_type", xKeyseed.getType());
            Uri uri = ContentUris.withAppendedId(this.tbUri, Long.parseLong(xKeyseed.getId()));
            int count = this.resolver.update(uri, values, null, null);
            if (count > 0) {
                return true;
            }
            Log.e("NFCKeyContentProxy.updateKeyseed", "failed to update keyseed: " + xKeyseed.toJson());
            return false;
        } catch (Exception e) {
            Log.e("NFCKeyContentProxy.updateKeyseed", Log.getStackTraceString(e));
            return false;
        }
    }
}
