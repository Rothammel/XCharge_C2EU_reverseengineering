package com.xcharge.charger.data.proxy;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.xcharge.charger.data.p004db.ContentDB;

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

    public void init(Context context2) {
        this.context = context2;
        this.resolver = this.context.getContentResolver();
        this.tbUri = Uri.parse("content://com.xcharge.charger.data.provider.content/tb_id_generator");
        initChargeIdSeq();
    }

    public void destroy() {
    }

    public boolean initChargeIdSeq() {
        if (getChargeIdSeq() != null) {
            return true;
        }
        ContentValues cv = new ContentValues();
        cv.put(ContentDB.IDGeneratorTable.CHARGE_ID_SEQ, String.valueOf(0));
        if (this.resolver.insert(this.tbUri, cv) != null) {
            return true;
        }
        Log.e("IDGeneratorContentProxy.initChargeIdSeq", "failed to init charge seq !!!");
        return false;
    }

    public String[] getChargeIdSeq() {
        Cursor cursor = null;
        try {
            Cursor cursor2 = this.resolver.query(this.tbUri, (String[]) null, (String) null, (String[]) null, (String) null);
            if (cursor2 == null || !cursor2.moveToFirst()) {
                if (cursor2 != null) {
                    cursor2.close();
                }
                return null;
            }
            String[] rslt = {cursor2.getString(cursor2.getColumnIndexOrThrow("_id")), cursor2.getString(cursor2.getColumnIndexOrThrow(ContentDB.IDGeneratorTable.CHARGE_ID_SEQ))};
            if (cursor2 == null) {
                return rslt;
            }
            cursor2.close();
            return rslt;
        } catch (Exception e) {
            Log.e("IDGeneratorContentProxy.getChargeIdSeq", Log.getStackTraceString(e));
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

    public boolean updateChargeIdSeq(String id, long seq) {
        Uri uri = ContentUris.withAppendedId(this.tbUri, Long.parseLong(id));
        ContentValues cv = new ContentValues();
        cv.put(ContentDB.IDGeneratorTable.CHARGE_ID_SEQ, String.valueOf(seq));
        if (this.resolver.update(uri, cv, (String) null, (String[]) null) > 0) {
            return true;
        }
        Log.e("IDGeneratorContentProxy.updateChargeIdSeq", "failed to update charge seq to: " + seq);
        return false;
    }
}
