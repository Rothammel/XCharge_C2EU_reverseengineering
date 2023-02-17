package com.xcharge.charger.data.proxy;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.CHARGE_STOP_CAUSE;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.ChargerContentProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import java.util.ArrayList;
import java.util.Iterator;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* loaded from: classes.dex */
public class ChargeContentProxy {
    private static ChargeContentProxy instance = null;
    private Context context = null;
    private ContentResolver resolver = null;
    private Uri tbUri = null;

    public static ChargeContentProxy getInstance() {
        if (instance == null) {
            instance = new ChargeContentProxy();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        this.resolver = this.context.getContentResolver();
        this.tbUri = Uri.parse("content://com.xcharge.charger.data.provider.content/tb_charge");
    }

    public void destroy() {
    }

    public Uri getUriFor(String subPath) {
        String path = ChargerContentProvider.CONTENT_URI + "tb_charge".replace("_", "");
        if (!TextUtils.isEmpty(subPath)) {
            path = String.valueOf(path) + MqttTopic.TOPIC_LEVEL_SEPARATOR + subPath;
        }
        return Uri.parse(path);
    }

    public ChargeBill getChargeBill(String chargeId) {
        if (chargeId == null) {
            Log.w("ChargeContentProxy.getChargeBill", "param chargeId must not be null !!!");
            return null;
        }
        String currentPlatform = SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform();
        String[] selectionArgs = {chargeId, currentPlatform};
        Cursor cursor = null;
        try {
            try {
                cursor = this.resolver.query(this.tbUri, null, "charge_id=? and charge_platform=?", selectionArgs, null);
            } catch (Exception e) {
                Log.e("ChargeContentProxy.getChargeBill", Log.getStackTraceString(e));
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
            ChargeBill fromDbLine = new ChargeBill().fromDbLine(cursor);
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

    public ChargeBill getChargeBillcloud(String cloudChargeId) {
        if (cloudChargeId == null) {
            Log.w("ChargeContentProxy.getChargeBill", "param cloudChargeId must not be null !!!");
            return null;
        }
        String currentPlatform = SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform();
        String[] selectionArgs = {cloudChargeId, currentPlatform};
        Cursor cursor = null;
        try {
            try {
                cursor = this.resolver.query(this.tbUri, null, "cloud_charge_id=? and charge_platform=?", selectionArgs, null);
            } catch (Exception e) {
                Log.e("ChargeContentProxy.getChargeBill", Log.getStackTraceString(e));
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
            ChargeBill fromDbLine = new ChargeBill().fromDbLine(cursor);
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

    public boolean saveChargeBill(ChargeBill chargeBill) {
        String chargeId = chargeBill.getCharge_id();
        if (chargeId == null) {
            Log.w("ChargeContentProxy.saveChargeBill", "illegal charge bill: " + chargeBill.toJson());
            return false;
        }
        chargeBill.setUpdate_time(System.currentTimeMillis());
        ContentValues values = chargeBill.toDbLine();
        ChargeBill cb = getChargeBill(chargeId);
        if (cb == null) {
            Uri uri = this.resolver.insert(this.tbUri, values);
            if (uri != null) {
                return true;
            }
        } else {
            Uri uri2 = ContentUris.withAppendedId(this.tbUri, Long.parseLong(cb.getId()));
            int count = this.resolver.update(uri2, values, null, null);
            if (count > 0) {
                return true;
            }
        }
        Log.e("ChargeContentProxy.saveChargeBill", "failed to save charge bill: " + chargeBill.toJson());
        return false;
    }

    public boolean setCloudChargeId(String localChargeId, String cloudChargeId) {
        ChargeBill cb = getChargeBill(localChargeId);
        if (cb != null) {
            ContentValues values = cb.toDbLine();
            values.put(ContentDB.ChargeTable.CLOUD_CHARGE_ID, cloudChargeId);
            Uri uri = ContentUris.withAppendedId(this.tbUri, Long.parseLong(cb.getId()));
            int count = this.resolver.update(uri, values, null, null);
            if (count > 0) {
                return true;
            }
        }
        Log.w("ChargeContentProxy.setCloudChargeId", "failed to set cloud charge id for offline card charge: " + localChargeId + ", cloud charge id: " + cloudChargeId);
        return false;
    }

    public ArrayList<ChargeBill> getUnpaidBills(String userType, String userCode) {
        String currentPlatform = SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform();
        String[] selectionArgs = {userType, userCode, currentPlatform};
        Cursor cursor = null;
        try {
            try {
                cursor = this.resolver.query(this.tbUri, null, "user_type=? and user_code=? and pay_flag<=0 and total_fee>0 and fin_time>0 and charge_platform=?", selectionArgs, null);
            } catch (Exception e) {
                Log.e("ChargeContentProxy.getUnpaidBills", Log.getStackTraceString(e));
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
            ArrayList<ChargeBill> unpaidBills = new ArrayList<>();
            while (cursor.moveToNext()) {
                unpaidBills.add(new ChargeBill().fromDbLine(cursor));
            }
            if (cursor != null) {
                cursor.close();
                return unpaidBills;
            }
            return unpaidBills;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public boolean setBalanceFlag(String chargeId) {
        ChargeBill cb = getChargeBill(chargeId);
        if (cb != null) {
            ContentValues values = cb.toDbLine();
            values.put(ContentDB.ChargeTable.BALANCE_FLAG, (Integer) 1);
            values.put(ContentDB.ChargeTable.BALANCE_TIME, String.valueOf(System.currentTimeMillis()));
            Uri uri = ContentUris.withAppendedId(this.tbUri, Long.parseLong(cb.getId()));
            int count = this.resolver.update(uri, values, null, null);
            if (count > 0) {
                return true;
            }
        }
        Log.w("ChargeContentProxy.setBalanceFlag", "failed to set balance flag for charge: " + chargeId);
        return false;
    }

    public boolean setUserBalance(String billId, long balance) {
        ChargeBill cb = getChargeBill(billId);
        if (cb != null) {
            ContentValues values = cb.toDbLine();
            values.put(ContentDB.ChargeTable.USER_BALANCE, Long.valueOf(balance));
            Uri uri = ContentUris.withAppendedId(this.tbUri, Long.parseLong(cb.getId()));
            int count = this.resolver.update(uri, values, null, null);
            if (count > 0) {
                return true;
            }
        }
        Log.w("ChargeContentProxy.setCarBalance", "failed to set user balance: " + balance + " for bill: " + billId);
        return false;
    }

    public boolean setPaidFlag(String billId, int payType) {
        ChargeBill cb = getChargeBill(billId);
        if (cb != null) {
            ContentValues values = cb.toDbLine();
            values.put(ContentDB.ChargeTable.PAY_FLAG, (Integer) 1);
            values.put(ContentDB.ChargeTable.PAY_TYPE, Integer.valueOf(payType));
            if (payType == 1) {
                values.put(ContentDB.ChargeTable.PAY_TIME, String.valueOf(System.currentTimeMillis()));
            }
            Uri uri = ContentUris.withAppendedId(this.tbUri, Long.parseLong(cb.getId()));
            int count = this.resolver.update(uri, values, null, null);
            if (count > 0) {
                this.context.getContentResolver().notifyChange(getUriFor("pay/" + billId), null);
                return true;
            }
        }
        Log.w("ChargeContentProxy.setPaidFlag", "failed to set paid flag for bill: " + billId);
        return false;
    }

    public boolean setReportedFlag(String chargeId) {
        ChargeBill cb = getChargeBill(chargeId);
        if (cb != null) {
            ContentValues values = cb.toDbLine();
            values.put(ContentDB.ChargeTable.REPORT_FLAG, (Integer) 1);
            Uri uri = ContentUris.withAppendedId(this.tbUri, Long.parseLong(cb.getId()));
            int count = this.resolver.update(uri, values, null, null);
            if (count > 0) {
                return true;
            }
        }
        Log.w("ChargeContentProxy.setReportedFlag", "failed to set reported flag for charge: " + chargeId);
        return false;
    }

    public boolean setMonitorFlag(String chargeId) {
        ChargeBill cb = getChargeBill(chargeId);
        if (cb != null) {
            ContentValues values = cb.toDbLine();
            values.put(ContentDB.ChargeTable.MONITOR_FLAG, (Integer) 1);
            Uri uri = ContentUris.withAppendedId(this.tbUri, Long.parseLong(cb.getId()));
            int count = this.resolver.update(uri, values, null, null);
            if (count > 0) {
                return true;
            }
        }
        Log.w("ChargeContentProxy.setMonitorFlag", "failed to set monitor flag for charge: " + chargeId);
        return false;
    }

    public boolean clearReportedFlag(String chargeId) {
        ChargeBill cb = getChargeBill(chargeId);
        if (cb != null) {
            ContentValues values = cb.toDbLine();
            values.put(ContentDB.ChargeTable.REPORT_FLAG, (Integer) 0);
            Uri uri = ContentUris.withAppendedId(this.tbUri, Long.parseLong(cb.getId()));
            int count = this.resolver.update(uri, values, null, null);
            if (count > 0) {
                return true;
            }
        }
        Log.w("ChargeContentProxy.clearReportedFlag", "failed to clear reported flag for charge: " + chargeId);
        return false;
    }

    public ArrayList<ChargeBill> getUnReportedBills(String[] userTypes, String port) {
        String[] selectionArgs = new String[userTypes.length + 2];
        selectionArgs[0] = userTypes[0];
        String userTypesSection = "user_type=?";
        for (int i = 1; i < userTypes.length; i++) {
            userTypesSection = String.valueOf(userTypesSection) + " or " + ContentDB.ChargeTable.USER_TYPE + "=?";
            selectionArgs[i] = userTypes[i];
        }
        if (userTypes.length > 1) {
            userTypesSection = "(" + userTypesSection + ")";
        }
        String currentPlatform = SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform();
        String selection = String.valueOf(userTypesSection) + " and " + ContentDB.ChargeTable.PORT + "=? and " + ContentDB.ChargeTable.REPORT_FLAG + "=0 and " + ContentDB.ChargeTable.FIN_TIME + ">0 and " + ContentDB.ChargeTable.CHARGE_PLATFORM + "=?";
        selectionArgs[userTypes.length] = port;
        selectionArgs[userTypes.length + 1] = currentPlatform;
        Cursor cursor = null;
        try {
            try {
                cursor = this.resolver.query(this.tbUri, null, selection, selectionArgs, "CAST( `init_time` AS UNSIGNED ) DESC, CAST( `total_fee` AS UNSIGNED ) DESC");
            } catch (Exception e) {
                Log.e("ChargeContentProxy.getUnpaidBills", Log.getStackTraceString(e));
                if (cursor != null) {
                    cursor.close();
                }
            }
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                Log.w("ChargeContentProxy.getUnReportedBills", "unfound unreported charge bill for user types: " + userTypes.toString() + ", port: " + port);
                return null;
            }
            ArrayList<ChargeBill> unreportedBills = new ArrayList<>();
            while (cursor.moveToNext()) {
                unreportedBills.add(new ChargeBill().fromDbLine(cursor));
            }
            if (cursor != null) {
                cursor.close();
                return unreportedBills;
            }
            return unreportedBills;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public ArrayList<ChargeBill> getUnMonitorBills(String[] userTypes, String port) {
        String[] selectionArgs = new String[userTypes.length + 2];
        selectionArgs[0] = userTypes[0];
        String userTypesSection = "user_type=?";
        for (int i = 1; i < userTypes.length; i++) {
            userTypesSection = String.valueOf(userTypesSection) + " or " + ContentDB.ChargeTable.USER_TYPE + "=?";
            selectionArgs[i] = userTypes[i];
        }
        if (userTypes.length > 1) {
            userTypesSection = "(" + userTypesSection + ")";
        }
        String currentPlatform = SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform();
        String selection = String.valueOf(userTypesSection) + " and " + ContentDB.ChargeTable.PORT + "=? and " + ContentDB.ChargeTable.MONITOR_FLAG + "=0 and " + ContentDB.ChargeTable.FIN_TIME + ">0 and " + ContentDB.ChargeTable.CHARGE_PLATFORM + "=?";
        selectionArgs[userTypes.length] = port;
        selectionArgs[userTypes.length + 1] = currentPlatform;
        Cursor cursor = null;
        try {
            try {
                cursor = this.resolver.query(this.tbUri, null, selection, selectionArgs, "CAST( `init_time` AS UNSIGNED ) DESC, CAST( `total_fee` AS UNSIGNED ) DESC");
            } catch (Exception e) {
                Log.e("ChargeContentProxy.getUnMonitorBills", Log.getStackTraceString(e));
                if (cursor != null) {
                    cursor.close();
                }
            }
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                Log.w("ChargeContentProxy.getUnMonitorBills", "unfound unreported charge bill for user types: " + userTypes.toString() + ", port: " + port);
                return null;
            }
            ArrayList<ChargeBill> unreportedBills = new ArrayList<>();
            while (cursor.moveToNext()) {
                unreportedBills.add(new ChargeBill().fromDbLine(cursor));
            }
            if (cursor != null) {
                cursor.close();
                return unreportedBills;
            }
            return unreportedBills;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public boolean setChargeAttchData(String chargeId, String data) {
        ChargeBill cb = getChargeBill(chargeId);
        if (cb != null) {
            ContentValues values = cb.toDbLine();
            values.put(ContentDB.ChargeTable.ATTACH_DATA, data);
            Uri uri = ContentUris.withAppendedId(this.tbUri, Long.parseLong(cb.getId()));
            int count = this.resolver.update(uri, values, null, null);
            if (count > 0) {
                return true;
            }
        }
        Log.w("ChargeContentProxy.setChargeAttchData", "failed to set charge attach data: " + data + ", charge id: " + chargeId);
        return false;
    }

    public boolean setChargeStartReportedFlag(String chargeId, int startReportFlag) {
        ChargeBill cb = getChargeBill(chargeId);
        if (cb != null) {
            ContentValues values = cb.toDbLine();
            values.put(ContentDB.ChargeTable.START_REPORT_FLAG, Integer.valueOf(startReportFlag));
            Uri uri = ContentUris.withAppendedId(this.tbUri, Long.parseLong(cb.getId()));
            int count = this.resolver.update(uri, values, null, null);
            if (count > 0) {
                return true;
            }
        }
        Log.w("ChargeContentProxy.setChargeStartReportedFlag", "failed to set charge start reported flag for charge: " + chargeId);
        return false;
    }

    public ArrayList<ChargeBill> getUnReportedStartCharges(String[] userTypes, String port) {
        String[] selectionArgs = new String[userTypes.length + 2];
        selectionArgs[0] = userTypes[0];
        String userTypesSection = "user_type=?";
        for (int i = 1; i < userTypes.length; i++) {
            userTypesSection = String.valueOf(userTypesSection) + " or " + ContentDB.ChargeTable.USER_TYPE + "=?";
            selectionArgs[i] = userTypes[i];
        }
        if (userTypes.length > 1) {
            userTypesSection = "(" + userTypesSection + ")";
        }
        String currentPlatform = SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform();
        String selection = String.valueOf(userTypesSection) + " and " + ContentDB.ChargeTable.PORT + "=? and " + ContentDB.ChargeTable.START_REPORT_FLAG + "=0 and " + ContentDB.ChargeTable.START_TIME + ">0 and " + ContentDB.ChargeTable.CHARGE_PLATFORM + "=?";
        selectionArgs[userTypes.length] = port;
        selectionArgs[userTypes.length + 1] = currentPlatform;
        Cursor cursor = null;
        try {
            try {
                cursor = this.resolver.query(this.tbUri, null, selection, selectionArgs, "CAST( `start_time` AS UNSIGNED ) DESC");
            } catch (Exception e) {
                Log.e("ChargeContentProxy.getUnReportedStartCharges", Log.getStackTraceString(e));
                if (cursor != null) {
                    cursor.close();
                }
            }
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                Log.w("ChargeContentProxy.getUnReportedStartCharges", "not found unreported start charge for user types: " + userTypes.toString() + ", port: " + port);
                return null;
            }
            ArrayList<ChargeBill> unreportedBills = new ArrayList<>();
            while (cursor.moveToNext()) {
                unreportedBills.add(new ChargeBill().fromDbLine(cursor));
            }
            if (cursor != null) {
                cursor.close();
                return unreportedBills;
            }
            return unreportedBills;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public boolean setChargeStopReportedFlag(String chargeId, int stopReportFlag) {
        ChargeBill cb = getChargeBill(chargeId);
        if (cb != null) {
            ContentValues values = cb.toDbLine();
            values.put(ContentDB.ChargeTable.STOP_REPORT_FLAG, Integer.valueOf(stopReportFlag));
            Uri uri = ContentUris.withAppendedId(this.tbUri, Long.parseLong(cb.getId()));
            int count = this.resolver.update(uri, values, null, null);
            if (count > 0) {
                return true;
            }
        }
        Log.w("ChargeContentProxy.setChargeStopReportedFlag", "failed to set charge stop reported flag for charge: " + chargeId);
        return false;
    }

    public ArrayList<ChargeBill> getUnReportedStopCharges(String[] userTypes, String port) {
        String[] selectionArgs = new String[userTypes.length + 2];
        selectionArgs[0] = userTypes[0];
        String userTypesSection = "user_type=?";
        for (int i = 1; i < userTypes.length; i++) {
            userTypesSection = String.valueOf(userTypesSection) + " or " + ContentDB.ChargeTable.USER_TYPE + "=?";
            selectionArgs[i] = userTypes[i];
        }
        if (userTypes.length > 1) {
            userTypesSection = "(" + userTypesSection + ")";
        }
        String currentPlatform = SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform();
        String selection = String.valueOf(userTypesSection) + " and " + ContentDB.ChargeTable.PORT + "=? and " + ContentDB.ChargeTable.START_REPORT_FLAG + "=1 and " + ContentDB.ChargeTable.STOP_REPORT_FLAG + "=0 and " + ContentDB.ChargeTable.STOP_TIME + ">0 and " + ContentDB.ChargeTable.CHARGE_PLATFORM + "=?";
        selectionArgs[userTypes.length] = port;
        selectionArgs[userTypes.length + 1] = currentPlatform;
        Cursor cursor = null;
        try {
            try {
                cursor = this.resolver.query(this.tbUri, null, selection, selectionArgs, "CAST( `stop_time` AS UNSIGNED ) DESC, CAST( `total_fee` AS UNSIGNED ) DESC");
            } catch (Exception e) {
                Log.e("ChargeContentProxy.getUnReportedStopCharges", Log.getStackTraceString(e));
                if (cursor != null) {
                    cursor.close();
                }
            }
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                Log.w("ChargeContentProxy.getUnReportedStopCharges", "not found unreported stop charge for user types: " + userTypes.toString() + ", port: " + port);
                return null;
            }
            ArrayList<ChargeBill> unreportedBills = new ArrayList<>();
            while (cursor.moveToNext()) {
                unreportedBills.add(new ChargeBill().fromDbLine(cursor));
            }
            if (cursor != null) {
                cursor.close();
                return unreportedBills;
            }
            return unreportedBills;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public int clearChargeBillBefore(long after, long before) {
        String[] selectionArgs = {String.valueOf(before), String.valueOf(after)};
        return this.resolver.delete(this.tbUri, "init_time<? and init_time>=?", selectionArgs);
    }

    public int endExceptionChargeBill() {
        PortStatus portStatus;
        Double ammeter;
        Cursor cursor = null;
        ArrayList<ChargeBill> exceptionBills = new ArrayList<>();
        try {
            try {
                cursor = this.resolver.query(this.tbUri, null, "(init_time>0 and fin_time=0) or (start_time>0 and stop_time=0)", null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        exceptionBills.add(new ChargeBill().fromDbLine(cursor));
                    }
                }
            } catch (Exception e) {
                Log.e("ChargeContentProxy.endExceptionChargeBill", Log.getStackTraceString(e));
                if (cursor != null) {
                    cursor.close();
                }
            }
            System.currentTimeMillis();
            Iterator<ChargeBill> it2 = exceptionBills.iterator();
            while (it2.hasNext()) {
                ChargeBill cb = it2.next();
                if (cb.getStart_time() > 0 && cb.getStop_time() == 0) {
                    if (cb.getUpdate_time() > cb.getStart_time()) {
                        cb.setStop_time(cb.getUpdate_time());
                    } else {
                        cb.setStop_time(cb.getStart_time());
                    }
                    if (cb.getStop_cause() == null) {
                        cb.setStop_cause(CHARGE_STOP_CAUSE.reboot);
                    }
                }
                if (cb.getFin_time() == 0) {
                    if (cb.getStop_time() > 0) {
                        if (cb.getUpdate_time() > cb.getStop_time()) {
                            cb.setFin_time(cb.getUpdate_time());
                        } else {
                            cb.setFin_time(cb.getStop_time());
                        }
                    } else if (cb.getUpdate_time() > 0) {
                        cb.setFin_time(cb.getUpdate_time());
                    } else {
                        cb.setFin_time(cb.getInit_time());
                    }
                    if (cb.getStop_cause() == null) {
                        cb.setStop_cause(CHARGE_STOP_CAUSE.reboot);
                    }
                }
                if (cb.getStop_ammeter() < 0.0d) {
                    String port = cb.getPort();
                    if (!TextUtils.isEmpty(port) && (portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus(port)) != null && (ammeter = portStatus.getAmmeter()) != null) {
                        cb.setStop_ammeter(ammeter.doubleValue());
                    }
                }
                cb.setBalance_flag(1);
                ContentValues values = cb.toDbLine();
                Uri uri = ContentUris.withAppendedId(this.tbUri, Long.parseLong(cb.getId()));
                int count = this.resolver.update(uri, values, null, null);
                if (count <= 0) {
                    Log.w("ChargeContentProxy.endExceptionChargeBill", "failed to end charge bill: " + cb);
                }
            }
            return exceptionBills.size();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
