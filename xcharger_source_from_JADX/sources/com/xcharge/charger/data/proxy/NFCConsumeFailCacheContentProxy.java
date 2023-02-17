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

    public void init(Context context2) {
        this.context = context2;
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
        Cursor cursor = null;
        try {
            Cursor cursor2 = this.resolver.query(this.tbUri, (String[]) null, "nfc_type=? and uuid=? and card_no=?", new String[]{type.getType(), uuid, cardNo}, (String) null);
            if (cursor2 == null || !cursor2.moveToFirst()) {
                if (cursor2 != null) {
                    cursor2.close();
                }
                return null;
            }
            ConsumeFailCache fromDbLine = new ConsumeFailCache().fromDbLine(cursor2);
            if (cursor2 == null) {
                return fromDbLine;
            }
            cursor2.close();
            return fromDbLine;
        } catch (Exception e) {
            Log.e("NFCConsumeFailCacheContentProxy.getConsumeFailCache", Log.getStackTraceString(e));
            if (cursor != null) {
                cursor.close();
            }
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
        if (cfc != null) {
            if (this.resolver.update(ContentUris.withAppendedId(this.tbUri, Long.parseLong(cfc.getId())), values, (String) null, (String[]) null) > 0) {
                return true;
            }
        } else if (this.resolver.insert(this.tbUri, values) != null) {
            return true;
        }
        Log.e("NFCConsumeFailCacheContentProxy.saveConsumeFailCache", "failed to save consume fail data: " + data.toJson());
        return false;
    }

    public int removeConsumeFailCache(NFC_CARD_TYPE type, String uuid, String cardNo) {
        if (type == null || TextUtils.isEmpty(uuid) || TextUtils.isEmpty(cardNo)) {
            Log.w("NFCConsumeFailCacheContentProxy.removeConsumeFailCache", "params must not be null !!!");
            return -1;
        }
        return this.resolver.delete(this.tbUri, "nfc_type=? and uuid=? and card_no=?", new String[]{type.getType(), uuid, cardNo});
    }
}
