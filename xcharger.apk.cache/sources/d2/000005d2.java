package com.xcharge.charger.data.proxy;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.util.TextUtils;

/* loaded from: classes.dex */
public class AuthInfoProxy {
    private static AuthInfoProxy instance = null;
    private Context context = null;
    private ContentResolver resolver = null;
    private Uri tbUri = null;

    public static AuthInfoProxy getInstance() {
        if (instance == null) {
            instance = new AuthInfoProxy();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        this.resolver = this.context.getContentResolver();
        this.tbUri = Uri.parse("content://com.xcharge.charger.data.provider.content/tb_auth_info");
    }

    public void destroy() {
    }

    public AuthInfo getAuthInfo(String idTag) {
        if (TextUtils.isEmpty(idTag)) {
            Log.w("AuthInfoProxy.getAuthInfo", "param idTag must not be null !!!");
            return null;
        }
        String[] selectionArgs = {idTag};
        Cursor cursor = null;
        try {
            try {
                cursor = this.resolver.query(this.tbUri, null, "idTag=?", selectionArgs, null);
            } catch (Exception e) {
                Log.e("AuthInfoProxy.getAuthInfo", Log.getStackTraceString(e));
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
            AuthInfo fromDbLine = new AuthInfo().fromDbLine(cursor);
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

    public AuthInfo getAuthInfo(String idTag, String parentIdTag) {
        if (TextUtils.isEmpty(idTag)) {
            Log.w("AuthInfoProxy.getAuthInfo", "param idTag must not be null !!!");
            return null;
        }
        String[] selectionArgs = {idTag, parentIdTag};
        Cursor cursor = null;
        try {
            try {
                cursor = this.resolver.query(this.tbUri, null, "idTag=? and parenIdTag=?", selectionArgs, null);
            } catch (Exception e) {
                Log.e("AuthInfoProxy.getAuthInfo", Log.getStackTraceString(e));
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
            AuthInfo fromDbLine = new AuthInfo().fromDbLine(cursor);
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

    public void insertAllAuthInfos(List<AuthInfo> list) {
        this.resolver.delete(this.tbUri, null, null);
        for (AuthInfo authInfo : list) {
            ContentValues values = authInfo.toDbLine();
            this.resolver.insert(this.tbUri, values);
        }
    }

    public void deleteAllAuthInfo() {
        this.resolver.delete(this.tbUri, null, null);
    }

    public void insertAuthInfo(AuthInfo authInfo) {
        ContentValues values = authInfo.toDbLine();
        String idTag = authInfo.getIdTag();
        if (TextUtils.isEmpty(idTag)) {
            Log.w("AuthInfoProxy.insertAuthInfo", "param idTag must not be null !!!" + authInfo.toJson());
            return;
        }
        AuthInfo ai = getAuthInfo(idTag);
        if (ai == null) {
            this.resolver.insert(this.tbUri, values);
            return;
        }
        Uri uri = ContentUris.withAppendedId(this.tbUri, Long.parseLong(ai.getId()));
        this.resolver.update(uri, values, null, null);
    }

    public void insertAuthInfos(List<AuthInfo> list) {
        for (AuthInfo authInfo : list) {
            ContentValues values = authInfo.toDbLine();
            String idTag = authInfo.getIdTag();
            if (TextUtils.isEmpty(idTag)) {
                Log.w("AuthInfoProxy.insertAuthInfos", "param idTag must not be null !!!" + authInfo.toJson());
            } else {
                AuthInfo ai = getAuthInfo(idTag);
                if (ai == null) {
                    this.resolver.insert(this.tbUri, values);
                } else {
                    Uri uri = ContentUris.withAppendedId(this.tbUri, Long.parseLong(ai.getId()));
                    this.resolver.update(uri, values, null, null);
                }
            }
        }
    }

    public List<AuthInfo> queryAuthInfo() {
        Cursor cursor = null;
        try {
            try {
                cursor = this.resolver.query(this.tbUri, null, null, null, null);
            } catch (Exception e) {
                Log.e("AuthInfoProxy.queryAuthInfo", Log.getStackTraceString(e));
                if (cursor != null) {
                    cursor.close();
                }
            }
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            List<AuthInfo> authInfos = new ArrayList<>();
            while (cursor.moveToNext()) {
                authInfos.add(new AuthInfo().fromDbLine(cursor));
            }
            if (cursor != null) {
                cursor.close();
                return authInfos;
            }
            return authInfos;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public ArrayList<AuthInfo> getAllAuthInfo() {
        Cursor cursor = null;
        try {
            try {
                cursor = this.resolver.query(this.tbUri, null, null, null, null);
            } catch (Exception e) {
                Log.e("AuthInfoProxy.getAllAuthInfo", Log.getStackTraceString(e));
                if (cursor != null) {
                    cursor.close();
                }
            }
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            ArrayList<AuthInfo> authInfos = new ArrayList<>();
            while (cursor.moveToNext()) {
                authInfos.add(new AuthInfo().fromDbLine(cursor));
            }
            if (cursor != null) {
                cursor.close();
                return authInfos;
            }
            return authInfos;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }
}