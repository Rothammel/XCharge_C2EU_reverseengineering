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

    public void init(Context context2) {
        this.context = context2;
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
        Cursor cursor = null;
        try {
            Cursor cursor2 = this.resolver.query(this.tbUri, (String[]) null, "idTag=?", new String[]{idTag}, (String) null);
            if (cursor2 == null || !cursor2.moveToFirst()) {
                if (cursor2 != null) {
                    cursor2.close();
                }
                return null;
            }
            AuthInfo fromDbLine = new AuthInfo().fromDbLine(cursor2);
            if (cursor2 == null) {
                return fromDbLine;
            }
            cursor2.close();
            return fromDbLine;
        } catch (Exception e) {
            Log.e("AuthInfoProxy.getAuthInfo", Log.getStackTraceString(e));
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

    public AuthInfo getAuthInfo(String idTag, String parentIdTag) {
        if (TextUtils.isEmpty(idTag)) {
            Log.w("AuthInfoProxy.getAuthInfo", "param idTag must not be null !!!");
            return null;
        }
        Cursor cursor = null;
        try {
            Cursor cursor2 = this.resolver.query(this.tbUri, (String[]) null, "idTag=? and parenIdTag=?", new String[]{idTag, parentIdTag}, (String) null);
            if (cursor2 == null || !cursor2.moveToFirst()) {
                if (cursor2 != null) {
                    cursor2.close();
                }
                return null;
            }
            AuthInfo fromDbLine = new AuthInfo().fromDbLine(cursor2);
            if (cursor2 == null) {
                return fromDbLine;
            }
            cursor2.close();
            return fromDbLine;
        } catch (Exception e) {
            Log.e("AuthInfoProxy.getAuthInfo", Log.getStackTraceString(e));
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

    public void insertAllAuthInfos(List<AuthInfo> list) {
        this.resolver.delete(this.tbUri, (String) null, (String[]) null);
        for (AuthInfo authInfo : list) {
            this.resolver.insert(this.tbUri, authInfo.toDbLine());
        }
    }

    public void deleteAllAuthInfo() {
        this.resolver.delete(this.tbUri, (String) null, (String[]) null);
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
        this.resolver.update(ContentUris.withAppendedId(this.tbUri, Long.parseLong(ai.getId())), values, (String) null, (String[]) null);
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
                    this.resolver.update(ContentUris.withAppendedId(this.tbUri, Long.parseLong(ai.getId())), values, (String) null, (String[]) null);
                }
            }
        }
    }

    public List<AuthInfo> queryAuthInfo() {
        Cursor cursor = null;
        try {
            cursor = this.resolver.query(this.tbUri, (String[]) null, (String) null, (String[]) null, (String) null);
            if (cursor != null) {
                List<AuthInfo> authInfos = new ArrayList<>();
                while (cursor.moveToNext()) {
                    authInfos.add(new AuthInfo().fromDbLine(cursor));
                }
                if (cursor == null) {
                    return authInfos;
                }
                cursor.close();
                return authInfos;
            }
            if (cursor != null) {
                cursor.close();
            }
            return null;
        } catch (Exception e) {
            Log.e("AuthInfoProxy.queryAuthInfo", Log.getStackTraceString(e));
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

    public ArrayList<AuthInfo> getAllAuthInfo() {
        Cursor cursor = null;
        try {
            cursor = this.resolver.query(this.tbUri, (String[]) null, (String) null, (String[]) null, (String) null);
            if (cursor != null) {
                ArrayList<AuthInfo> authInfos = new ArrayList<>();
                while (cursor.moveToNext()) {
                    authInfos.add(new AuthInfo().fromDbLine(cursor));
                }
                if (cursor == null) {
                    return authInfos;
                }
                cursor.close();
                return authInfos;
            }
            if (cursor != null) {
                cursor.close();
            }
            return null;
        } catch (Exception e) {
            Log.e("AuthInfoProxy.getAllAuthInfo", Log.getStackTraceString(e));
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
}
