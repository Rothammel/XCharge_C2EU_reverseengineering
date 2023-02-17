package com.xcharge.charger.data.proxy;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.xcharge.charger.data.db.ContentDB;

/* loaded from: classes.dex */
public class IDGeneratorContentProxy {
    private static IDGeneratorContentProxy instance = null;
    private Context context = null;
    private ContentResolver resolver = null;
    private Uri tbUri = null;

    public static IDGeneratorContentProxy getInstance() {
        if (instance == null) {
            instance = new IDGeneratorContentProxy();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        this.resolver = this.context.getContentResolver();
        this.tbUri = Uri.parse("content://com.xcharge.charger.data.provider.content/tb_id_generator");
        initChargeIdSeq();
    }

    public void destroy() {
    }

    public boolean initChargeIdSeq() {
        String[] rslt = getChargeIdSeq();
        if (rslt == null) {
            ContentValues cv = new ContentValues();
            cv.put(ContentDB.IDGeneratorTable.CHARGE_ID_SEQ, String.valueOf(0));
            Uri uri = this.resolver.insert(this.tbUri, cv);
            if (uri != null) {
                return true;
            }
            Log.e("IDGeneratorContentProxy.initChargeIdSeq", "failed to init charge seq !!!");
            return false;
        }
        return true;
    }

    public String[] getChargeIdSeq() {
        Cursor cursor = null;
        try {
            try {
                cursor = this.resolver.query(this.tbUri, null, null, null, null);
            } catch (Exception e) {
                Log.e("IDGeneratorContentProxy.getChargeIdSeq", Log.getStackTraceString(e));
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
            String[] rslt = {cursor.getString(cursor.getColumnIndexOrThrow("_id")), cursor.getString(cursor.getColumnIndexOrThrow(ContentDB.IDGeneratorTable.CHARGE_ID_SEQ))};
            if (cursor != null) {
                cursor.close();
                return rslt;
            }
            return rslt;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public boolean updateChargeIdSeq(String id, long seq) {
        Uri uri = ContentUris.withAppendedId(this.tbUri, Long.parseLong(id));
        ContentValues cv = new ContentValues();
        cv.put(ContentDB.IDGeneratorTable.CHARGE_ID_SEQ, String.valueOf(seq));
        int count = this.resolver.update(uri, cv, null, null);
        if (count > 0) {
            return true;
        }
        Log.e("IDGeneratorContentProxy.updateChargeIdSeq", "failed to update charge seq to: " + seq);
        return false;
    }
}
