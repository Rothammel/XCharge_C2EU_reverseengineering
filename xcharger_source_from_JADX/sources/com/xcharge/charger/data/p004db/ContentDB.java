package com.xcharge.charger.data.p004db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/* renamed from: com.xcharge.charger.data.db.ContentDB */
public class ContentDB extends SQLiteOpenHelper {
    private static final String DB_NAME = "content.db";
    private static final int DB_VERSION = 11;
    private static final String TB_AUTH_INFO = "tb_auth_info";
    private static final String TB_CHARGE = "tb_charge";
    private static final String TB_ID_GENERATOR = "tb_id_generator";
    private static final String TB_NFC_CONSUME_FAIL_CACHE = "tb_nfc_consume_fail_cache";
    private static final String TB_NFC_KEY = "tb_nfc_key";
    private String createAuthInfoTableSql;
    private String createChargeTableSql;
    private String createIDGeneratorTableSql;
    private String createNFCConsumeFailCacheTableSql;
    private String createNFCKeyTableSql;

    /* renamed from: com.xcharge.charger.data.db.ContentDB$AuthInfoTable */
    public static class AuthInfoTable implements BaseColumns {
        public static final String EXPIRY_DATE = "expiryDate";
        public static final String ID_TAG = "idTag";
        public static final String PARENT_ID_TAG = "parenIdTag";
        public static final String STATUS = "status";
        public static final String TABLE_NAME = "tb_auth_info";
        public static final String _ID = "_id";
    }

    /* renamed from: com.xcharge.charger.data.db.ContentDB$ChargeTable */
    public static class ChargeTable implements BaseColumns {
        public static final String ATTACH_DATA = "attach_data";
        public static final String BALANCE_FLAG = "balance_flag";
        public static final String BALANCE_TIME = "balance_time";
        public static final String BINDED_USER = "binded_user";
        public static final String CHARGE_ID = "charge_id";
        public static final String CHARGE_PLATFORM = "charge_platform";
        public static final String CLOUD_CHARGE_ID = "cloud_charge_id";
        public static final String DELAY_FEE = "delay_fee";
        public static final String DELAY_INFO = "delay_info";
        public static final String DELAY_START = "delay_start";
        public static final String FEE_RATE = "fee_rate";
        public static final String FEE_RATE_ID = "fee_rate_id";
        public static final String FIN_TIME = "fin_time";
        public static final String INIT_TIME = "init_time";
        public static final String INIT_TYPE = "init_type";
        public static final String IS_FREE = "is_free";
        public static final String MONITOR_FLAG = "monitor_flag";
        public static final String PARK_FEE = "park_fee";
        public static final String PARK_INFO = "park_info";
        public static final String PAY_FLAG = "pay_flag";
        public static final String PAY_TIME = "pay_time";
        public static final String PAY_TYPE = "pay_type";
        public static final String PORT = "port";
        public static final String POWER_FEE = "power_fee";
        public static final String POWER_INFO = "power_info";
        public static final String REPORT_FLAG = "report_flag";
        public static final String SERVICE_FEE = "service_fee";
        public static final String SERVICE_INFO = "service_info";
        public static final String START_AMMETER = "start_ammeter";
        public static final String START_REPORT_FLAG = "start_report_flag";
        public static final String START_TIME = "start_time";
        public static final String STOP_AMMETER = "stop_ammeter";
        public static final String STOP_CAUSE = "stop_cause";
        public static final String STOP_REPORT_FLAG = "stop_report_flag";
        public static final String STOP_TIME = "stop_time";
        public static final String TABLE_NAME = "tb_charge";
        public static final String TOTAL_DELAY = "total_delay";
        public static final String TOTAL_FEE = "total_fee";
        public static final String TOTAL_PARK = "total_park";
        public static final String TOTAL_POWER = "total_power";
        public static final String TOTAL_TIME = "total_time";
        public static final String UPDATE_TIME = "update_time";
        public static final String USER_BALANCE = "user_balance";
        public static final String USER_CODE = "user_code";
        public static final String USER_TC_TYPE = "user_tc_type";
        public static final String USER_TC_VALUE = "user_tc_value";
        public static final String USER_TYPE = "user_type";
        public static final String _ID = "_id";
    }

    /* renamed from: com.xcharge.charger.data.db.ContentDB$IDGeneratorTable */
    public static class IDGeneratorTable implements BaseColumns {
        public static final String CHARGE_ID_SEQ = "charge_id_seq";
        public static final String TABLE_NAME = "tb_id_generator";
        public static final String _ID = "_id";
    }

    /* renamed from: com.xcharge.charger.data.db.ContentDB$NFCConsumeFailCacheTable */
    public static class NFCConsumeFailCacheTable implements BaseColumns {
        public static final String BALANCE = "balance";
        public static final String CARD_NO = "card_no";
        public static final String COUNT = "count";
        public static final String NFC_TYPE = "nfc_type";
        public static final String TABLE_NAME = "tb_nfc_consume_fail_cache";
        public static final String UPDATE_TIME = "update_time";
        public static final String UUID = "uuid";
        public static final String _ID = "_id";
    }

    /* renamed from: com.xcharge.charger.data.db.ContentDB$NFCKeyTable */
    public static class NFCKeyTable implements BaseColumns {
        public static final String GROUP_ID = "group_id";
        public static final String KEY = "key";
        public static final String NFC_TYPE = "nfc_type";
        public static final String TABLE_NAME = "tb_nfc_key";
        public static final String _ID = "_id";
    }

    public ContentDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
        this.createChargeTableSql = "create table if not exists tb_charge ( _id integer PRIMARY KEY, charge_id text, user_type text, user_code text, port text DEFAULT 1, init_type text, user_tc_type text, user_tc_value text, user_balance int DEFAULT 0, is_free int DEFAULT -1, charge_platform text, binded_user text, cloud_charge_id text, total_time int DEFAULT 0, init_time text, fin_time text, start_time text, stop_time text, delay_start text, total_delay int DEFAULT 0, delay_info text, stop_cause text, update_time text, start_ammeter double DEFAULT -1.0, stop_ammeter double DEFAULT -1.0, total_power double DEFAULT 0.0, power_info text, service_info text, fee_rate_id text, fee_rate text, total_fee int DEFAULT 0, power_fee int DEFAULT 0, service_fee int DEFAULT 0, delay_fee int DEFAULT 0, total_park int DEFAULT 0, park_fee int DEFAULT 0, park_info text, balance_flag int DEFAULT 0, balance_time text, pay_flag int DEFAULT -1, pay_type int DEFAULT -1, pay_time text, report_flag int DEFAULT 0, monitor_flag int DEFAULT 0, start_report_flag int DEFAULT 0, stop_report_flag int DEFAULT 0, attach_data text  )";
        this.createNFCKeyTableSql = "create table if not exists tb_nfc_key ( _id integer PRIMARY KEY, group_id text NOT NULL, nfc_type text NOT NULL, key  text NOT NULL )";
        this.createNFCConsumeFailCacheTableSql = "create table if not exists tb_nfc_consume_fail_cache ( _id integer PRIMARY KEY, nfc_type text NOT NULL, uuid text NOT NULL, card_no  text NOT NULL, balance int DEFAULT 0, count int DEFAULT 0, update_time text )";
        this.createIDGeneratorTableSql = "create table if not exists tb_id_generator ( _id integer PRIMARY KEY, charge_id_seq  text DEFAULT '0' )";
        this.createAuthInfoTableSql = "create table if not exists tb_auth_info ( _id integer PRIMARY KEY, idTag text , expiryDate text, parenIdTag text, status text  )";
    }

    public ContentDB(Context context) {
        this(context, DB_NAME, (SQLiteDatabase.CursorFactory) null, 11, (DatabaseErrorHandler) null);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(this.createChargeTableSql);
        db.execSQL(this.createNFCKeyTableSql);
        db.execSQL(this.createNFCConsumeFailCacheTableSql);
        db.execSQL(this.createIDGeneratorTableSql);
        db.execSQL(this.createAuthInfoTableSql);
        Log.i("ContentDB.onCreate", "db tables created !!!");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 1 && newVersion > 1) {
            db.execSQL("alter table tb_charge add column stop_cause text");
        }
        if (oldVersion <= 2 && newVersion > 2) {
            db.execSQL("alter table tb_charge add column user_tc_type text");
            db.execSQL("alter table tb_charge add column user_tc_value text");
            db.execSQL("alter table tb_charge add column user_balance int DEFAULT 0");
            db.execSQL("alter table tb_charge add column is_free int DEFAULT -1");
            db.execSQL("alter table tb_charge add column charge_platform text");
            db.execSQL("alter table tb_charge add column binded_user text");
        }
        if (oldVersion <= 3 && newVersion > 3) {
            db.execSQL(this.createNFCConsumeFailCacheTableSql);
        }
        if (oldVersion <= 4 && newVersion > 4) {
            db.execSQL("alter table tb_charge add column service_info text");
            db.execSQL("alter table tb_charge add column total_park int DEFAULT 0");
            db.execSQL("alter table tb_charge add column park_fee int DEFAULT 0");
            db.execSQL("alter table tb_charge add column park_info text");
        }
        if (oldVersion <= 5 && newVersion > 5) {
            db.execSQL("alter table tb_charge add column cloud_charge_id text");
        }
        if (oldVersion <= 6 && newVersion > 6) {
            db.execSQL("alter table tb_charge add column update_time text");
            db.execSQL("alter table tb_charge add column start_report_flag int DEFAULT 0");
            db.execSQL("alter table tb_charge add column stop_report_flag int DEFAULT 0");
        }
        if (oldVersion <= 7 && newVersion > 7) {
            db.execSQL(this.createIDGeneratorTableSql);
        }
        if (oldVersion <= 8 && newVersion > 8) {
            db.execSQL("alter table tb_charge add column start_ammeter double DEFAULT 0.0");
            db.execSQL("alter table tb_charge add column stop_ammeter double DEFAULT 0.0");
            db.execSQL(this.createAuthInfoTableSql);
        }
        if (oldVersion <= 9 && newVersion > 9) {
            db.execSQL("alter table tb_charge add column attach_data text");
        }
        if (oldVersion <= 10 && newVersion > 10) {
            db.execSQL("alter table tb_charge add column balance_time text");
            db.execSQL("alter table tb_charge add column pay_time text");
            db.execSQL("alter table tb_charge add column monitor_flag int DEFAULT 0");
        }
        Log.i("ContentDB.onUpgrade", "db tables upgraded !!!");
        onCreate(db);
    }
}
