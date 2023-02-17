package com.xcharge.charger.data.proxy;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.data.bean.ConsumeFailCache;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;

/* loaded from: classes.dex */
public class NFCConsumeFailCacheContentProxy {
    private static NFCConsumeFailCacheContentProxy instance = null;
    private Context context = null;
    private ContentResolver resolver = null;
    private Uri tbUri = null;

    public static NFCConsumeFailCacheContentProxy getInstance() {
        if (instance == null) {
            instance = new NFCConsumeFailCacheContentProxy();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        this.resolver = this.context.getContentResolver();
        this.tbUri = Uri.parse("content://com.xcharge.charger.data.provider.content/tb_nfc_consume_fail_cache");
    }

    public void destroy() {
    }

    public ConsumeFailCache getConsumeFailCache(NFC_CARD_TYPE type, String uuid, String cardNo) {
        if (type == null || TextUtils.isEmpty(uuid) || TextUtils.isEmpty(cardNo)) {
            Log.w("NFCConsumeFailCacheContentProxy.getConsumeFailCache", "params must not be null !!!");
            return null;
        }
        String[] selectionArgs = {type.getType(), uuid, cardNo};
        Cursor cursor = null;
        try {
            try {
                cursor = this.resolver.query(this.tbUri, null, "nfc_type=? and uuid=? and card_no=?", selectionArgs, null);
            } catch (Exception e) {
                Log.e("NFCConsumeFailCacheContentProxy.getConsumeFailCache", Log.getStackTraceString(e));
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
            ConsumeFailCache fromDbLine = new ConsumeFailCache().fromDbLine(cursor);
            if (cursor != null) {
                cursor.close();
                return fromDbLine;
            }
            return fromDbLine;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public boolean saveConsumeFailCache(ConsumeFailCache data) {
        NFC_CARD_TYPE type = data.getNfc_type();
        String uuid = data.getUuid();
        String cardNo = data.getCard_no();
        if (type == null || TextUtils.isEmpty(uuid) || TextUtils.isEmpty(cardNo)) {
            Log.w("NFCConsumeFailCacheContentProxy.saveConsumeFailCache", "illegal data: " + data.toJson());
            return false;
        }
        ContentValues values = data.toDbLine();
        ConsumeFailCache cfc = getConsumeFailCache(type, uuid, cardNo);
        if (cfc == null) {
            Uri uri = this.resolver.insert(this.tbUri, values);
            if (uri != null) {
                return true;
            }
        } else {
            Uri uri2 = ContentUris.withAppendedId(this.tbUri, Long.parseLong(cfc.getId()));
            int count = this.resolver.update(uri2, values, null, null);
            if (count > 0) {
                return true;
            }
        }
        Log.e("NFCConsumeFailCacheContentProxy.saveConsumeFailCache", "failed to save consume fail data: " + data.toJson());
        return false;
    }

    public int removeConsumeFailCache(NFC_CARD_TYPE type, String uuid, String cardNo) {
        if (type == null || TextUtils.isEmpty(uuid) || TextUtils.isEmpty(cardNo)) {
            Log.w("NFCConsumeFailCacheContentProxy.removeConsumeFailCache", "params must not be null !!!");
            return -1;
        }
        String[] selectionArgs = {type.getType(), uuid, cardNo};
        return this.resolver.delete(this.tbUri, "nfc_type=? and uuid=? and card_no=?", selectionArgs);
    }
}