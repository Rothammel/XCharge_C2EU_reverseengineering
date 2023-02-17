package com.xcharge.charger.data.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.data.db.ContentDB;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* loaded from: classes.dex */
public class ChargerContentProvider extends ContentProvider {
    public static final String AUTHORITY = "com.xcharge.charger.data.provider.content";
    public static final String AUTH_INFO_TABLE = "tb_auth_info";
    public static final String CHARGE_TABLE = "tb_charge";
    public static final String CONTENT_TYPE_AUTH_INFO_ITEM = "vnd.android.cursor.item/com.xcharge.charger.data.provider.content.tb_auth_info";
    public static final String CONTENT_TYPE_AUTH_INFO_SET = "vnd.android.cursor.dir/com.xcharge.charger.data.provider.content.tb_auth_info";
    public static final String CONTENT_TYPE_CHARGE_ITEM = "vnd.android.cursor.item/com.xcharge.charger.data.provider.content.tb_charge";
    public static final String CONTENT_TYPE_CHARGE_SET = "vnd.android.cursor.dir/com.xcharge.charger.data.provider.content.tb_charge";
    public static final String CONTENT_TYPE_ID_GENERATOR_ITEM = "vnd.android.cursor.item/com.xcharge.charger.data.provider.content.tb_id_generator";
    public static final String CONTENT_TYPE_ID_GENERATOR_SET = "vnd.android.cursor.dir/com.xcharge.charger.data.provider.content.tb_id_generator";
    public static final String CONTENT_TYPE_NFC_CONSUME_FAIL_CACHE_ITEM = "vnd.android.cursor.item/com.xcharge.charger.data.provider.content.tb_nfc_consume_fail_cache";
    public static final String CONTENT_TYPE_NFC_CONSUME_FAIL_CACHE_SET = "vnd.android.cursor.dir/com.xcharge.charger.data.provider.content.tb_nfc_consume_fail_cache";
    public static final String CONTENT_TYPE_NFC_KEY_ITEM = "vnd.android.cursor.item/com.xcharge.charger.data.provider.content.tb_nfc_key";
    public static final String CONTENT_TYPE_NFC_KEY_SET = "vnd.android.cursor.dir/com.xcharge.charger.data.provider.content.tb_nfc_key";
    public static final String CONTENT_URI = "content://com.xcharge.charger.data.provider.content/";
    public static final String ID_GENERATOR_TABLE = "tb_id_generator";
    public static final int KEY_AUTH_INFO_ITEM = 10;
    public static final int KEY_AUTH_INFO_SET = 9;
    public static final int KEY_CHARGE_ITEM = 6;
    public static final int KEY_CHARGE_SET = 5;
    public static final int KEY_ID_GENERATOR_ITEM = 8;
    public static final int KEY_ID_GENERATOR_SET = 7;
    public static final int KEY_NFC_CONSUME_FAIL_CACHE_ITEM = 4;
    public static final int KEY_NFC_CONSUME_FAIL_CACHE_SET = 3;
    public static final int KEY_NFC_KEY_ITEM = 2;
    public static final int KEY_NFC_KEY_SET = 1;
    public static final String NFC_CONSUME_FAIL_CACHE_TABLE = "tb_nfc_consume_fail_cache";
    public static final String NFC_KEY_TABLE = "tb_nfc_key";
    public static final UriMatcher uriMatcher = new UriMatcher(-1);
    private ContentDB contentDB = null;

    static {
        uriMatcher.addURI(AUTHORITY, "tb_nfc_key", 1);
        uriMatcher.addURI(AUTHORITY, "tb_nfc_key/#", 2);
        uriMatcher.addURI(AUTHORITY, "tb_nfc_consume_fail_cache", 3);
        uriMatcher.addURI(AUTHORITY, "tb_nfc_consume_fail_cache/#", 4);
        uriMatcher.addURI(AUTHORITY, "tb_charge", 5);
        uriMatcher.addURI(AUTHORITY, "tb_charge/#", 6);
        uriMatcher.addURI(AUTHORITY, "tb_id_generator", 7);
        uriMatcher.addURI(AUTHORITY, "tb_id_generator/#", 8);
        uriMatcher.addURI(AUTHORITY, "tb_auth_info", 9);
        uriMatcher.addURI(AUTHORITY, "tb_auth_info/#", 10);
    }

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        String where = null;
        try {
            SQLiteDatabase db = this.contentDB.getWritableDatabase();
            int matcher = uriMatcher.match(uri);
            if (2 == matcher || 6 == matcher || 4 == matcher || 8 == matcher || 10 == matcher) {
                long id = ContentUris.parseId(uri);
                String where2 = "_ID = " + id;
                where = String.valueOf(where2) + (!TextUtils.isEmpty(selection) ? " and (" + selection + ")" : "");
            }
            switch (matcher) {
                case 1:
                    count = db.delete("tb_nfc_key", selection, selectionArgs);
                    break;
                case 2:
                    count = db.delete("tb_nfc_key", where, selectionArgs);
                    break;
                case 3:
                    count = db.delete("tb_nfc_consume_fail_cache", selection, selectionArgs);
                    break;
                case 4:
                    count = db.delete("tb_nfc_consume_fail_cache", where, selectionArgs);
                    break;
                case 5:
                    count = db.delete("tb_charge", selection, selectionArgs);
                    break;
                case 6:
                    count = db.delete("tb_charge", where, selectionArgs);
                    break;
                case 7:
                    count = db.delete("tb_id_generator", selection, selectionArgs);
                    break;
                case 8:
                    count = db.delete("tb_id_generator", where, selectionArgs);
                    break;
                case 9:
                    count = db.delete("tb_auth_info", selection, selectionArgs);
                    break;
                case 10:
                    count = db.delete("tb_auth_info", where, selectionArgs);
                    break;
                default:
                    Log.w("ChargerContentProvider.delete", "unknown uri: " + uri);
                    break;
            }
        } catch (Exception e) {
            Log.e("ChargerContentProvider.delete", Log.getStackTraceString(e));
        }
        if (count > 0) {
            String notifyUri = uri.toString();
            getContext().getContentResolver().notifyChange(Uri.parse(notifyUri.replace("_", "")), null);
        }
        return count;
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case 1:
                return CONTENT_TYPE_NFC_KEY_SET;
            case 2:
                return CONTENT_TYPE_NFC_KEY_ITEM;
            case 3:
                return CONTENT_TYPE_NFC_CONSUME_FAIL_CACHE_SET;
            case 4:
                return CONTENT_TYPE_NFC_CONSUME_FAIL_CACHE_ITEM;
            case 5:
                return CONTENT_TYPE_CHARGE_SET;
            case 6:
                return CONTENT_TYPE_CHARGE_ITEM;
            case 7:
                return CONTENT_TYPE_ID_GENERATOR_SET;
            case 8:
                return CONTENT_TYPE_ID_GENERATOR_ITEM;
            case 9:
                return CONTENT_TYPE_AUTH_INFO_SET;
            case 10:
                return CONTENT_TYPE_AUTH_INFO_ITEM;
            default:
                Log.w("ChargerContentProvider.getType", "unknown uri: " + uri);
                return null;
        }
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues values) {
        Uri rslt = null;
        try {
            SQLiteDatabase db = this.contentDB.getWritableDatabase();
            switch (uriMatcher.match(uri)) {
                case 1:
                    long id = db.insert("tb_nfc_key", null, values);
                    rslt = ContentUris.withAppendedId(uri, id);
                    break;
                case 2:
                    long id2 = db.insert("tb_nfc_key", null, values);
                    String path = uri.toString();
                    rslt = Uri.parse(String.valueOf(path.substring(0, path.lastIndexOf(MqttTopic.TOPIC_LEVEL_SEPARATOR))) + id2);
                    break;
                case 3:
                    long id3 = db.insert("tb_nfc_consume_fail_cache", null, values);
                    rslt = ContentUris.withAppendedId(uri, id3);
                    break;
                case 4:
                    long id4 = db.insert("tb_nfc_consume_fail_cache", null, values);
                    String path2 = uri.toString();
                    rslt = Uri.parse(String.valueOf(path2.substring(0, path2.lastIndexOf(MqttTopic.TOPIC_LEVEL_SEPARATOR))) + id4);
                    break;
                case 5:
                    long id5 = db.insert("tb_charge", null, values);
                    rslt = ContentUris.withAppendedId(uri, id5);
                    break;
                case 6:
                    long id6 = db.insert("tb_charge", null, values);
                    String path3 = uri.toString();
                    rslt = Uri.parse(String.valueOf(path3.substring(0, path3.lastIndexOf(MqttTopic.TOPIC_LEVEL_SEPARATOR))) + id6);
                    break;
                case 7:
                    long id7 = db.insert("tb_id_generator", null, values);
                    rslt = ContentUris.withAppendedId(uri, id7);
                    break;
                case 8:
                    long id8 = db.insert("tb_id_generator", null, values);
                    String path4 = uri.toString();
                    rslt = Uri.parse(String.valueOf(path4.substring(0, path4.lastIndexOf(MqttTopic.TOPIC_LEVEL_SEPARATOR))) + id8);
                    break;
                case 9:
                    long id9 = db.insert("tb_auth_info", null, values);
                    rslt = ContentUris.withAppendedId(uri, id9);
                    break;
                case 10:
                    long id10 = db.insert("tb_auth_info", null, values);
                    String path5 = uri.toString();
                    rslt = Uri.parse(String.valueOf(path5.substring(0, path5.lastIndexOf(MqttTopic.TOPIC_LEVEL_SEPARATOR))) + id10);
                    break;
                default:
                    Log.w("ChargerContentProvider.insert", "unknown uri: " + uri);
                    break;
            }
        } catch (Exception e) {
            Log.e("ChargerContentProvider.insert", Log.getStackTraceString(e));
        }
        if (rslt != null) {
            String notifyUri = rslt.toString();
            getContext().getContentResolver().notifyChange(Uri.parse(notifyUri.replace("_", "")), null);
        }
        return rslt;
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        this.contentDB = new ContentDB(getContext());
        Log.i("ChargerContentProvider.onCreate", "content provider created !!!");
        return this.contentDB != null;
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor rslt = null;
        String where = null;
        try {
            SQLiteDatabase db = this.contentDB.getWritableDatabase();
            int matcher = uriMatcher.match(uri);
            if (2 == matcher || 6 == matcher || 4 == matcher || 8 == matcher || 10 == matcher) {
                long id = ContentUris.parseId(uri);
                String where2 = "_ID = " + id;
                where = String.valueOf(where2) + (!TextUtils.isEmpty(selection) ? " and (" + selection + ")" : "");
            }
            switch (matcher) {
                case 1:
                    rslt = db.query("tb_nfc_key", projection, selection, selectionArgs, null, null, sortOrder);
                    break;
                case 2:
                    rslt = db.query("tb_nfc_key", projection, where, selectionArgs, null, null, sortOrder);
                    break;
                case 3:
                    rslt = db.query("tb_nfc_consume_fail_cache", projection, selection, selectionArgs, null, null, sortOrder);
                    break;
                case 4:
                    rslt = db.query("tb_nfc_consume_fail_cache", projection, where, selectionArgs, null, null, sortOrder);
                    break;
                case 5:
                    rslt = db.query("tb_charge", projection, selection, selectionArgs, null, null, sortOrder);
                    break;
                case 6:
                    db.query("tb_charge", projection, where, selectionArgs, null, null, sortOrder);
                case 7:
                    rslt = db.query("tb_id_generator", projection, selection, selectionArgs, null, null, sortOrder);
                    break;
                case 8:
                    rslt = db.query("tb_id_generator", projection, where, selectionArgs, null, null, sortOrder);
                    break;
                case 9:
                    rslt = db.query("tb_auth_info", projection, selection, selectionArgs, null, null, sortOrder);
                    break;
                case 10:
                    rslt = db.query("tb_auth_info", projection, where, selectionArgs, null, null, sortOrder);
                    break;
                default:
                    Log.w("ChargerContentProvider.query", "unknown uri: " + uri);
                    break;
            }
        } catch (Exception e) {
            Log.e("ChargerContentProvider.query", Log.getStackTraceString(e));
        }
        if (rslt != null) {
            rslt.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return rslt;
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        String where = null;
        try {
            SQLiteDatabase db = this.contentDB.getWritableDatabase();
            int matcher = uriMatcher.match(uri);
            if (2 == matcher || 6 == matcher || 4 == matcher || 8 == matcher || 10 == matcher) {
                long id = ContentUris.parseId(uri);
                String where2 = "_ID = " + id;
                where = String.valueOf(where2) + (!TextUtils.isEmpty(selection) ? " and (" + selection + ")" : "");
            }
            switch (matcher) {
                case 1:
                    count = db.update("tb_nfc_key", values, selection, selectionArgs);
                    break;
                case 2:
                    count = db.update("tb_nfc_key", values, where, selectionArgs);
                    break;
                case 3:
                    count = db.update("tb_nfc_consume_fail_cache", values, selection, selectionArgs);
                    break;
                case 4:
                    count = db.update("tb_nfc_consume_fail_cache", values, where, selectionArgs);
                    break;
                case 5:
                    count = db.update("tb_charge", values, selection, selectionArgs);
                    break;
                case 6:
                    count = db.update("tb_charge", values, where, selectionArgs);
                    break;
                case 7:
                    count = db.update("tb_id_generator", values, selection, selectionArgs);
                    break;
                case 8:
                    count = db.update("tb_id_generator", values, where, selectionArgs);
                    break;
                case 9:
                    count = db.update("tb_auth_info", values, selection, selectionArgs);
                    break;
                case 10:
                    count = db.update("tb_auth_info", values, where, selectionArgs);
                    break;
                default:
                    Log.w("ChargerContentProvider.update", "unknown uri: " + uri);
                    break;
            }
        } catch (Exception e) {
            Log.e("ChargerContentProvider.update", Log.getStackTraceString(e));
        }
        if (count > 0) {
            String notifyUri = uri.toString();
            getContext().getContentResolver().notifyChange(Uri.parse(notifyUri.replace("_", "")), null);
        }
        return count;
    }
}