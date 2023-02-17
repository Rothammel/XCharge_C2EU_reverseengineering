package com.xcharge.charger.data.provider;

import android.content.ContentProvider;
import android.content.UriMatcher;
import android.net.Uri;
import android.util.Log;
import com.xcharge.charger.data.p004db.ContentDB;

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

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int delete(android.net.Uri r12, java.lang.String r13, java.lang.String[] r14) {
        /*
            r11 = this;
            r1 = 0
            r0 = 0
            r4 = 0
            r7 = 0
            com.xcharge.charger.data.db.ContentDB r8 = r11.contentDB     // Catch:{ Exception -> 0x00e0 }
            android.database.sqlite.SQLiteDatabase r1 = r8.getWritableDatabase()     // Catch:{ Exception -> 0x00e0 }
            android.content.UriMatcher r8 = uriMatcher     // Catch:{ Exception -> 0x00e0 }
            int r3 = r8.match(r12)     // Catch:{ Exception -> 0x00e0 }
            r8 = 2
            if (r8 == r3) goto L_0x0022
            r8 = 6
            if (r8 == r3) goto L_0x0022
            r8 = 4
            if (r8 == r3) goto L_0x0022
            r8 = 8
            if (r8 == r3) goto L_0x0022
            r8 = 10
            if (r8 != r3) goto L_0x0061
        L_0x0022:
            long r4 = android.content.ContentUris.parseId(r12)     // Catch:{ Exception -> 0x00e0 }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00e0 }
            java.lang.String r9 = "_ID = "
            r8.<init>(r9)     // Catch:{ Exception -> 0x00e0 }
            java.lang.StringBuilder r8 = r8.append(r4)     // Catch:{ Exception -> 0x00e0 }
            java.lang.String r7 = r8.toString()     // Catch:{ Exception -> 0x00e0 }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00e0 }
            java.lang.String r8 = java.lang.String.valueOf(r7)     // Catch:{ Exception -> 0x00e0 }
            r9.<init>(r8)     // Catch:{ Exception -> 0x00e0 }
            boolean r8 = android.text.TextUtils.isEmpty(r13)     // Catch:{ Exception -> 0x00e0 }
            if (r8 != 0) goto L_0x0097
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00e0 }
            java.lang.String r10 = " and ("
            r8.<init>(r10)     // Catch:{ Exception -> 0x00e0 }
            java.lang.StringBuilder r8 = r8.append(r13)     // Catch:{ Exception -> 0x00e0 }
            java.lang.String r10 = ")"
            java.lang.StringBuilder r8 = r8.append(r10)     // Catch:{ Exception -> 0x00e0 }
            java.lang.String r8 = r8.toString()     // Catch:{ Exception -> 0x00e0 }
        L_0x0059:
            java.lang.StringBuilder r8 = r9.append(r8)     // Catch:{ Exception -> 0x00e0 }
            java.lang.String r7 = r8.toString()     // Catch:{ Exception -> 0x00e0 }
        L_0x0061:
            switch(r3) {
                case 1: goto L_0x009a;
                case 2: goto L_0x00a1;
                case 3: goto L_0x00a8;
                case 4: goto L_0x00af;
                case 5: goto L_0x00b6;
                case 6: goto L_0x00bd;
                case 7: goto L_0x00c4;
                case 8: goto L_0x00cb;
                case 9: goto L_0x00d2;
                case 10: goto L_0x00d9;
                default: goto L_0x0064;
            }     // Catch:{ Exception -> 0x00e0 }
        L_0x0064:
            java.lang.String r8 = "ChargerContentProvider.delete"
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00e0 }
            java.lang.String r10 = "unknown uri: "
            r9.<init>(r10)     // Catch:{ Exception -> 0x00e0 }
            java.lang.StringBuilder r9 = r9.append(r12)     // Catch:{ Exception -> 0x00e0 }
            java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x00e0 }
            android.util.Log.w(r8, r9)     // Catch:{ Exception -> 0x00e0 }
        L_0x0078:
            if (r0 <= 0) goto L_0x0096
            java.lang.String r6 = r12.toString()
            android.content.Context r8 = r11.getContext()
            android.content.ContentResolver r8 = r8.getContentResolver()
            java.lang.String r9 = "_"
            java.lang.String r10 = ""
            java.lang.String r9 = r6.replace(r9, r10)
            android.net.Uri r9 = android.net.Uri.parse(r9)
            r10 = 0
            r8.notifyChange(r9, r10)
        L_0x0096:
            return r0
        L_0x0097:
            java.lang.String r8 = ""
            goto L_0x0059
        L_0x009a:
            java.lang.String r8 = "tb_nfc_key"
            int r0 = r1.delete(r8, r13, r14)     // Catch:{ Exception -> 0x00e0 }
            goto L_0x0078
        L_0x00a1:
            java.lang.String r8 = "tb_nfc_key"
            int r0 = r1.delete(r8, r7, r14)     // Catch:{ Exception -> 0x00e0 }
            goto L_0x0078
        L_0x00a8:
            java.lang.String r8 = "tb_nfc_consume_fail_cache"
            int r0 = r1.delete(r8, r13, r14)     // Catch:{ Exception -> 0x00e0 }
            goto L_0x0078
        L_0x00af:
            java.lang.String r8 = "tb_nfc_consume_fail_cache"
            int r0 = r1.delete(r8, r7, r14)     // Catch:{ Exception -> 0x00e0 }
            goto L_0x0078
        L_0x00b6:
            java.lang.String r8 = "tb_charge"
            int r0 = r1.delete(r8, r13, r14)     // Catch:{ Exception -> 0x00e0 }
            goto L_0x0078
        L_0x00bd:
            java.lang.String r8 = "tb_charge"
            int r0 = r1.delete(r8, r7, r14)     // Catch:{ Exception -> 0x00e0 }
            goto L_0x0078
        L_0x00c4:
            java.lang.String r8 = "tb_id_generator"
            int r0 = r1.delete(r8, r13, r14)     // Catch:{ Exception -> 0x00e0 }
            goto L_0x0078
        L_0x00cb:
            java.lang.String r8 = "tb_id_generator"
            int r0 = r1.delete(r8, r7, r14)     // Catch:{ Exception -> 0x00e0 }
            goto L_0x0078
        L_0x00d2:
            java.lang.String r8 = "tb_auth_info"
            int r0 = r1.delete(r8, r13, r14)     // Catch:{ Exception -> 0x00e0 }
            goto L_0x0078
        L_0x00d9:
            java.lang.String r8 = "tb_auth_info"
            int r0 = r1.delete(r8, r7, r14)     // Catch:{ Exception -> 0x00e0 }
            goto L_0x0078
        L_0x00e0:
            r2 = move-exception
            java.lang.String r8 = "ChargerContentProvider.delete"
            java.lang.String r9 = android.util.Log.getStackTraceString(r2)
            android.util.Log.e(r8, r9)
            goto L_0x0078
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.data.provider.ChargerContentProvider.delete(android.net.Uri, java.lang.String, java.lang.String[]):int");
    }

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

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.net.Uri insert(android.net.Uri r12, android.content.ContentValues r13) {
        /*
            r11 = this;
            r10 = 0
            r0 = 0
            r2 = 0
            r5 = 0
            r6 = 0
            com.xcharge.charger.data.db.ContentDB r7 = r11.contentDB     // Catch:{ Exception -> 0x0166 }
            android.database.sqlite.SQLiteDatabase r0 = r7.getWritableDatabase()     // Catch:{ Exception -> 0x0166 }
            android.content.UriMatcher r7 = uriMatcher     // Catch:{ Exception -> 0x0166 }
            int r7 = r7.match(r12)     // Catch:{ Exception -> 0x0166 }
            switch(r7) {
                case 1: goto L_0x0047;
                case 2: goto L_0x0053;
                case 3: goto L_0x007f;
                case 4: goto L_0x008b;
                case 5: goto L_0x00b8;
                case 6: goto L_0x00c5;
                case 7: goto L_0x00f2;
                case 8: goto L_0x00ff;
                case 9: goto L_0x012c;
                case 10: goto L_0x0139;
                default: goto L_0x0015;
            }     // Catch:{ Exception -> 0x0166 }
        L_0x0015:
            java.lang.String r7 = "ChargerContentProvider.insert"
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0166 }
            java.lang.String r9 = "unknown uri: "
            r8.<init>(r9)     // Catch:{ Exception -> 0x0166 }
            java.lang.StringBuilder r8 = r8.append(r12)     // Catch:{ Exception -> 0x0166 }
            java.lang.String r8 = r8.toString()     // Catch:{ Exception -> 0x0166 }
            android.util.Log.w(r7, r8)     // Catch:{ Exception -> 0x0166 }
        L_0x0029:
            if (r6 == 0) goto L_0x0046
            java.lang.String r4 = r6.toString()
            android.content.Context r7 = r11.getContext()
            android.content.ContentResolver r7 = r7.getContentResolver()
            java.lang.String r8 = "_"
            java.lang.String r9 = ""
            java.lang.String r8 = r4.replace(r8, r9)
            android.net.Uri r8 = android.net.Uri.parse(r8)
            r7.notifyChange(r8, r10)
        L_0x0046:
            return r6
        L_0x0047:
            java.lang.String r7 = "tb_nfc_key"
            r8 = 0
            long r2 = r0.insert(r7, r8, r13)     // Catch:{ Exception -> 0x0166 }
            android.net.Uri r6 = android.content.ContentUris.withAppendedId(r12, r2)     // Catch:{ Exception -> 0x0166 }
            goto L_0x0029
        L_0x0053:
            java.lang.String r7 = "tb_nfc_key"
            r8 = 0
            long r2 = r0.insert(r7, r8, r13)     // Catch:{ Exception -> 0x0166 }
            java.lang.String r5 = r12.toString()     // Catch:{ Exception -> 0x0166 }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0166 }
            r8 = 0
            java.lang.String r9 = "/"
            int r9 = r5.lastIndexOf(r9)     // Catch:{ Exception -> 0x0166 }
            java.lang.String r8 = r5.substring(r8, r9)     // Catch:{ Exception -> 0x0166 }
            java.lang.String r8 = java.lang.String.valueOf(r8)     // Catch:{ Exception -> 0x0166 }
            r7.<init>(r8)     // Catch:{ Exception -> 0x0166 }
            java.lang.StringBuilder r7 = r7.append(r2)     // Catch:{ Exception -> 0x0166 }
            java.lang.String r7 = r7.toString()     // Catch:{ Exception -> 0x0166 }
            android.net.Uri r6 = android.net.Uri.parse(r7)     // Catch:{ Exception -> 0x0166 }
            goto L_0x0029
        L_0x007f:
            java.lang.String r7 = "tb_nfc_consume_fail_cache"
            r8 = 0
            long r2 = r0.insert(r7, r8, r13)     // Catch:{ Exception -> 0x0166 }
            android.net.Uri r6 = android.content.ContentUris.withAppendedId(r12, r2)     // Catch:{ Exception -> 0x0166 }
            goto L_0x0029
        L_0x008b:
            java.lang.String r7 = "tb_nfc_consume_fail_cache"
            r8 = 0
            long r2 = r0.insert(r7, r8, r13)     // Catch:{ Exception -> 0x0166 }
            java.lang.String r5 = r12.toString()     // Catch:{ Exception -> 0x0166 }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0166 }
            r8 = 0
            java.lang.String r9 = "/"
            int r9 = r5.lastIndexOf(r9)     // Catch:{ Exception -> 0x0166 }
            java.lang.String r8 = r5.substring(r8, r9)     // Catch:{ Exception -> 0x0166 }
            java.lang.String r8 = java.lang.String.valueOf(r8)     // Catch:{ Exception -> 0x0166 }
            r7.<init>(r8)     // Catch:{ Exception -> 0x0166 }
            java.lang.StringBuilder r7 = r7.append(r2)     // Catch:{ Exception -> 0x0166 }
            java.lang.String r7 = r7.toString()     // Catch:{ Exception -> 0x0166 }
            android.net.Uri r6 = android.net.Uri.parse(r7)     // Catch:{ Exception -> 0x0166 }
            goto L_0x0029
        L_0x00b8:
            java.lang.String r7 = "tb_charge"
            r8 = 0
            long r2 = r0.insert(r7, r8, r13)     // Catch:{ Exception -> 0x0166 }
            android.net.Uri r6 = android.content.ContentUris.withAppendedId(r12, r2)     // Catch:{ Exception -> 0x0166 }
            goto L_0x0029
        L_0x00c5:
            java.lang.String r7 = "tb_charge"
            r8 = 0
            long r2 = r0.insert(r7, r8, r13)     // Catch:{ Exception -> 0x0166 }
            java.lang.String r5 = r12.toString()     // Catch:{ Exception -> 0x0166 }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0166 }
            r8 = 0
            java.lang.String r9 = "/"
            int r9 = r5.lastIndexOf(r9)     // Catch:{ Exception -> 0x0166 }
            java.lang.String r8 = r5.substring(r8, r9)     // Catch:{ Exception -> 0x0166 }
            java.lang.String r8 = java.lang.String.valueOf(r8)     // Catch:{ Exception -> 0x0166 }
            r7.<init>(r8)     // Catch:{ Exception -> 0x0166 }
            java.lang.StringBuilder r7 = r7.append(r2)     // Catch:{ Exception -> 0x0166 }
            java.lang.String r7 = r7.toString()     // Catch:{ Exception -> 0x0166 }
            android.net.Uri r6 = android.net.Uri.parse(r7)     // Catch:{ Exception -> 0x0166 }
            goto L_0x0029
        L_0x00f2:
            java.lang.String r7 = "tb_id_generator"
            r8 = 0
            long r2 = r0.insert(r7, r8, r13)     // Catch:{ Exception -> 0x0166 }
            android.net.Uri r6 = android.content.ContentUris.withAppendedId(r12, r2)     // Catch:{ Exception -> 0x0166 }
            goto L_0x0029
        L_0x00ff:
            java.lang.String r7 = "tb_id_generator"
            r8 = 0
            long r2 = r0.insert(r7, r8, r13)     // Catch:{ Exception -> 0x0166 }
            java.lang.String r5 = r12.toString()     // Catch:{ Exception -> 0x0166 }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0166 }
            r8 = 0
            java.lang.String r9 = "/"
            int r9 = r5.lastIndexOf(r9)     // Catch:{ Exception -> 0x0166 }
            java.lang.String r8 = r5.substring(r8, r9)     // Catch:{ Exception -> 0x0166 }
            java.lang.String r8 = java.lang.String.valueOf(r8)     // Catch:{ Exception -> 0x0166 }
            r7.<init>(r8)     // Catch:{ Exception -> 0x0166 }
            java.lang.StringBuilder r7 = r7.append(r2)     // Catch:{ Exception -> 0x0166 }
            java.lang.String r7 = r7.toString()     // Catch:{ Exception -> 0x0166 }
            android.net.Uri r6 = android.net.Uri.parse(r7)     // Catch:{ Exception -> 0x0166 }
            goto L_0x0029
        L_0x012c:
            java.lang.String r7 = "tb_auth_info"
            r8 = 0
            long r2 = r0.insert(r7, r8, r13)     // Catch:{ Exception -> 0x0166 }
            android.net.Uri r6 = android.content.ContentUris.withAppendedId(r12, r2)     // Catch:{ Exception -> 0x0166 }
            goto L_0x0029
        L_0x0139:
            java.lang.String r7 = "tb_auth_info"
            r8 = 0
            long r2 = r0.insert(r7, r8, r13)     // Catch:{ Exception -> 0x0166 }
            java.lang.String r5 = r12.toString()     // Catch:{ Exception -> 0x0166 }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0166 }
            r8 = 0
            java.lang.String r9 = "/"
            int r9 = r5.lastIndexOf(r9)     // Catch:{ Exception -> 0x0166 }
            java.lang.String r8 = r5.substring(r8, r9)     // Catch:{ Exception -> 0x0166 }
            java.lang.String r8 = java.lang.String.valueOf(r8)     // Catch:{ Exception -> 0x0166 }
            r7.<init>(r8)     // Catch:{ Exception -> 0x0166 }
            java.lang.StringBuilder r7 = r7.append(r2)     // Catch:{ Exception -> 0x0166 }
            java.lang.String r7 = r7.toString()     // Catch:{ Exception -> 0x0166 }
            android.net.Uri r6 = android.net.Uri.parse(r7)     // Catch:{ Exception -> 0x0166 }
            goto L_0x0029
        L_0x0166:
            r1 = move-exception
            java.lang.String r7 = "ChargerContentProvider.insert"
            java.lang.String r8 = android.util.Log.getStackTraceString(r1)
            android.util.Log.e(r7, r8)
            goto L_0x0029
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.data.provider.ChargerContentProvider.insert(android.net.Uri, android.content.ContentValues):android.net.Uri");
    }

    public boolean onCreate() {
        this.contentDB = new ContentDB(getContext());
        Log.i("ChargerContentProvider.onCreate", "content provider created !!!");
        return this.contentDB != null;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.database.Cursor query(android.net.Uri r17, java.lang.String[] r18, java.lang.String r19, java.lang.String[] r20, java.lang.String r21) {
        /*
            r16 = this;
            r2 = 0
            r14 = 0
            r12 = 0
            r15 = 0
            r0 = r16
            com.xcharge.charger.data.db.ContentDB r3 = r0.contentDB     // Catch:{ Exception -> 0x013b }
            android.database.sqlite.SQLiteDatabase r2 = r3.getWritableDatabase()     // Catch:{ Exception -> 0x013b }
            android.content.UriMatcher r3 = uriMatcher     // Catch:{ Exception -> 0x013b }
            r0 = r17
            int r11 = r3.match(r0)     // Catch:{ Exception -> 0x013b }
            r3 = 2
            if (r3 == r11) goto L_0x0026
            r3 = 6
            if (r3 == r11) goto L_0x0026
            r3 = 4
            if (r3 == r11) goto L_0x0026
            r3 = 8
            if (r3 == r11) goto L_0x0026
            r3 = 10
            if (r3 != r11) goto L_0x0067
        L_0x0026:
            long r12 = android.content.ContentUris.parseId(r17)     // Catch:{ Exception -> 0x013b }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x013b }
            java.lang.String r4 = "_ID = "
            r3.<init>(r4)     // Catch:{ Exception -> 0x013b }
            java.lang.StringBuilder r3 = r3.append(r12)     // Catch:{ Exception -> 0x013b }
            java.lang.String r15 = r3.toString()     // Catch:{ Exception -> 0x013b }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x013b }
            java.lang.String r3 = java.lang.String.valueOf(r15)     // Catch:{ Exception -> 0x013b }
            r4.<init>(r3)     // Catch:{ Exception -> 0x013b }
            boolean r3 = android.text.TextUtils.isEmpty(r19)     // Catch:{ Exception -> 0x013b }
            if (r3 != 0) goto L_0x0090
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x013b }
            java.lang.String r5 = " and ("
            r3.<init>(r5)     // Catch:{ Exception -> 0x013b }
            r0 = r19
            java.lang.StringBuilder r3 = r3.append(r0)     // Catch:{ Exception -> 0x013b }
            java.lang.String r5 = ")"
            java.lang.StringBuilder r3 = r3.append(r5)     // Catch:{ Exception -> 0x013b }
            java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x013b }
        L_0x005f:
            java.lang.StringBuilder r3 = r4.append(r3)     // Catch:{ Exception -> 0x013b }
            java.lang.String r15 = r3.toString()     // Catch:{ Exception -> 0x013b }
        L_0x0067:
            switch(r11) {
                case 1: goto L_0x0093;
                case 2: goto L_0x00a4;
                case 3: goto L_0x00b4;
                case 4: goto L_0x00c5;
                case 5: goto L_0x00d5;
                case 6: goto L_0x00e6;
                case 7: goto L_0x00f5;
                case 8: goto L_0x0107;
                case 9: goto L_0x0118;
                case 10: goto L_0x012a;
                default: goto L_0x006a;
            }     // Catch:{ Exception -> 0x013b }
        L_0x006a:
            java.lang.String r3 = "ChargerContentProvider.query"
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x013b }
            java.lang.String r5 = "unknown uri: "
            r4.<init>(r5)     // Catch:{ Exception -> 0x013b }
            r0 = r17
            java.lang.StringBuilder r4 = r4.append(r0)     // Catch:{ Exception -> 0x013b }
            java.lang.String r4 = r4.toString()     // Catch:{ Exception -> 0x013b }
            android.util.Log.w(r3, r4)     // Catch:{ Exception -> 0x013b }
        L_0x0080:
            if (r14 == 0) goto L_0x008f
            android.content.Context r3 = r16.getContext()
            android.content.ContentResolver r3 = r3.getContentResolver()
            r0 = r17
            r14.setNotificationUri(r3, r0)
        L_0x008f:
            return r14
        L_0x0090:
            java.lang.String r3 = ""
            goto L_0x005f
        L_0x0093:
            java.lang.String r3 = "tb_nfc_key"
            r7 = 0
            r8 = 0
            r4 = r18
            r5 = r19
            r6 = r20
            r9 = r21
            android.database.Cursor r14 = r2.query(r3, r4, r5, r6, r7, r8, r9)     // Catch:{ Exception -> 0x013b }
            goto L_0x0080
        L_0x00a4:
            java.lang.String r3 = "tb_nfc_key"
            r7 = 0
            r8 = 0
            r4 = r18
            r5 = r15
            r6 = r20
            r9 = r21
            android.database.Cursor r14 = r2.query(r3, r4, r5, r6, r7, r8, r9)     // Catch:{ Exception -> 0x013b }
            goto L_0x0080
        L_0x00b4:
            java.lang.String r3 = "tb_nfc_consume_fail_cache"
            r7 = 0
            r8 = 0
            r4 = r18
            r5 = r19
            r6 = r20
            r9 = r21
            android.database.Cursor r14 = r2.query(r3, r4, r5, r6, r7, r8, r9)     // Catch:{ Exception -> 0x013b }
            goto L_0x0080
        L_0x00c5:
            java.lang.String r3 = "tb_nfc_consume_fail_cache"
            r7 = 0
            r8 = 0
            r4 = r18
            r5 = r15
            r6 = r20
            r9 = r21
            android.database.Cursor r14 = r2.query(r3, r4, r5, r6, r7, r8, r9)     // Catch:{ Exception -> 0x013b }
            goto L_0x0080
        L_0x00d5:
            java.lang.String r3 = "tb_charge"
            r7 = 0
            r8 = 0
            r4 = r18
            r5 = r19
            r6 = r20
            r9 = r21
            android.database.Cursor r14 = r2.query(r3, r4, r5, r6, r7, r8, r9)     // Catch:{ Exception -> 0x013b }
            goto L_0x0080
        L_0x00e6:
            java.lang.String r3 = "tb_charge"
            r7 = 0
            r8 = 0
            r4 = r18
            r5 = r15
            r6 = r20
            r9 = r21
            android.database.Cursor r14 = r2.query(r3, r4, r5, r6, r7, r8, r9)     // Catch:{ Exception -> 0x013b }
        L_0x00f5:
            java.lang.String r3 = "tb_id_generator"
            r7 = 0
            r8 = 0
            r4 = r18
            r5 = r19
            r6 = r20
            r9 = r21
            android.database.Cursor r14 = r2.query(r3, r4, r5, r6, r7, r8, r9)     // Catch:{ Exception -> 0x013b }
            goto L_0x0080
        L_0x0107:
            java.lang.String r3 = "tb_id_generator"
            r7 = 0
            r8 = 0
            r4 = r18
            r5 = r15
            r6 = r20
            r9 = r21
            android.database.Cursor r14 = r2.query(r3, r4, r5, r6, r7, r8, r9)     // Catch:{ Exception -> 0x013b }
            goto L_0x0080
        L_0x0118:
            java.lang.String r3 = "tb_auth_info"
            r7 = 0
            r8 = 0
            r4 = r18
            r5 = r19
            r6 = r20
            r9 = r21
            android.database.Cursor r14 = r2.query(r3, r4, r5, r6, r7, r8, r9)     // Catch:{ Exception -> 0x013b }
            goto L_0x0080
        L_0x012a:
            java.lang.String r3 = "tb_auth_info"
            r7 = 0
            r8 = 0
            r4 = r18
            r5 = r15
            r6 = r20
            r9 = r21
            android.database.Cursor r14 = r2.query(r3, r4, r5, r6, r7, r8, r9)     // Catch:{ Exception -> 0x013b }
            goto L_0x0080
        L_0x013b:
            r10 = move-exception
            java.lang.String r3 = "ChargerContentProvider.query"
            java.lang.String r4 = android.util.Log.getStackTraceString(r10)
            android.util.Log.e(r3, r4)
            goto L_0x0080
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.data.provider.ChargerContentProvider.query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String):android.database.Cursor");
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int update(android.net.Uri r12, android.content.ContentValues r13, java.lang.String r14, java.lang.String[] r15) {
        /*
            r11 = this;
            r1 = 0
            r0 = 0
            r4 = 0
            r7 = 0
            com.xcharge.charger.data.db.ContentDB r8 = r11.contentDB     // Catch:{ Exception -> 0x00e0 }
            android.database.sqlite.SQLiteDatabase r1 = r8.getWritableDatabase()     // Catch:{ Exception -> 0x00e0 }
            android.content.UriMatcher r8 = uriMatcher     // Catch:{ Exception -> 0x00e0 }
            int r3 = r8.match(r12)     // Catch:{ Exception -> 0x00e0 }
            r8 = 2
            if (r8 == r3) goto L_0x0022
            r8 = 6
            if (r8 == r3) goto L_0x0022
            r8 = 4
            if (r8 == r3) goto L_0x0022
            r8 = 8
            if (r8 == r3) goto L_0x0022
            r8 = 10
            if (r8 != r3) goto L_0x0061
        L_0x0022:
            long r4 = android.content.ContentUris.parseId(r12)     // Catch:{ Exception -> 0x00e0 }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00e0 }
            java.lang.String r9 = "_ID = "
            r8.<init>(r9)     // Catch:{ Exception -> 0x00e0 }
            java.lang.StringBuilder r8 = r8.append(r4)     // Catch:{ Exception -> 0x00e0 }
            java.lang.String r7 = r8.toString()     // Catch:{ Exception -> 0x00e0 }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00e0 }
            java.lang.String r8 = java.lang.String.valueOf(r7)     // Catch:{ Exception -> 0x00e0 }
            r9.<init>(r8)     // Catch:{ Exception -> 0x00e0 }
            boolean r8 = android.text.TextUtils.isEmpty(r14)     // Catch:{ Exception -> 0x00e0 }
            if (r8 != 0) goto L_0x0097
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00e0 }
            java.lang.String r10 = " and ("
            r8.<init>(r10)     // Catch:{ Exception -> 0x00e0 }
            java.lang.StringBuilder r8 = r8.append(r14)     // Catch:{ Exception -> 0x00e0 }
            java.lang.String r10 = ")"
            java.lang.StringBuilder r8 = r8.append(r10)     // Catch:{ Exception -> 0x00e0 }
            java.lang.String r8 = r8.toString()     // Catch:{ Exception -> 0x00e0 }
        L_0x0059:
            java.lang.StringBuilder r8 = r9.append(r8)     // Catch:{ Exception -> 0x00e0 }
            java.lang.String r7 = r8.toString()     // Catch:{ Exception -> 0x00e0 }
        L_0x0061:
            switch(r3) {
                case 1: goto L_0x009a;
                case 2: goto L_0x00a1;
                case 3: goto L_0x00a8;
                case 4: goto L_0x00af;
                case 5: goto L_0x00b6;
                case 6: goto L_0x00bd;
                case 7: goto L_0x00c4;
                case 8: goto L_0x00cb;
                case 9: goto L_0x00d2;
                case 10: goto L_0x00d9;
                default: goto L_0x0064;
            }     // Catch:{ Exception -> 0x00e0 }
        L_0x0064:
            java.lang.String r8 = "ChargerContentProvider.update"
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00e0 }
            java.lang.String r10 = "unknown uri: "
            r9.<init>(r10)     // Catch:{ Exception -> 0x00e0 }
            java.lang.StringBuilder r9 = r9.append(r12)     // Catch:{ Exception -> 0x00e0 }
            java.lang.String r9 = r9.toString()     // Catch:{ Exception -> 0x00e0 }
            android.util.Log.w(r8, r9)     // Catch:{ Exception -> 0x00e0 }
        L_0x0078:
            if (r0 <= 0) goto L_0x0096
            java.lang.String r6 = r12.toString()
            android.content.Context r8 = r11.getContext()
            android.content.ContentResolver r8 = r8.getContentResolver()
            java.lang.String r9 = "_"
            java.lang.String r10 = ""
            java.lang.String r9 = r6.replace(r9, r10)
            android.net.Uri r9 = android.net.Uri.parse(r9)
            r10 = 0
            r8.notifyChange(r9, r10)
        L_0x0096:
            return r0
        L_0x0097:
            java.lang.String r8 = ""
            goto L_0x0059
        L_0x009a:
            java.lang.String r8 = "tb_nfc_key"
            int r0 = r1.update(r8, r13, r14, r15)     // Catch:{ Exception -> 0x00e0 }
            goto L_0x0078
        L_0x00a1:
            java.lang.String r8 = "tb_nfc_key"
            int r0 = r1.update(r8, r13, r7, r15)     // Catch:{ Exception -> 0x00e0 }
            goto L_0x0078
        L_0x00a8:
            java.lang.String r8 = "tb_nfc_consume_fail_cache"
            int r0 = r1.update(r8, r13, r14, r15)     // Catch:{ Exception -> 0x00e0 }
            goto L_0x0078
        L_0x00af:
            java.lang.String r8 = "tb_nfc_consume_fail_cache"
            int r0 = r1.update(r8, r13, r7, r15)     // Catch:{ Exception -> 0x00e0 }
            goto L_0x0078
        L_0x00b6:
            java.lang.String r8 = "tb_charge"
            int r0 = r1.update(r8, r13, r14, r15)     // Catch:{ Exception -> 0x00e0 }
            goto L_0x0078
        L_0x00bd:
            java.lang.String r8 = "tb_charge"
            int r0 = r1.update(r8, r13, r7, r15)     // Catch:{ Exception -> 0x00e0 }
            goto L_0x0078
        L_0x00c4:
            java.lang.String r8 = "tb_id_generator"
            int r0 = r1.update(r8, r13, r14, r15)     // Catch:{ Exception -> 0x00e0 }
            goto L_0x0078
        L_0x00cb:
            java.lang.String r8 = "tb_id_generator"
            int r0 = r1.update(r8, r13, r7, r15)     // Catch:{ Exception -> 0x00e0 }
            goto L_0x0078
        L_0x00d2:
            java.lang.String r8 = "tb_auth_info"
            int r0 = r1.update(r8, r13, r14, r15)     // Catch:{ Exception -> 0x00e0 }
            goto L_0x0078
        L_0x00d9:
            java.lang.String r8 = "tb_auth_info"
            int r0 = r1.update(r8, r13, r7, r15)     // Catch:{ Exception -> 0x00e0 }
            goto L_0x0078
        L_0x00e0:
            r2 = move-exception
            java.lang.String r8 = "ChargerContentProvider.update"
            java.lang.String r9 = android.util.Log.getStackTraceString(r2)
            android.util.Log.e(r8, r9)
            goto L_0x0078
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.data.provider.ChargerContentProvider.update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[]):int");
    }
}
