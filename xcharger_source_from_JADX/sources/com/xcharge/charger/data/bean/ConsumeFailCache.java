package com.xcharge.charger.data.bean;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.p004db.ContentDB;
import com.xcharge.common.bean.JsonBean;

public class ConsumeFailCache extends JsonBean<ConsumeFailCache> {
    private int balance = 0;
    private String card_no = null;
    private int count = 0;

    /* renamed from: id */
    private String f47id = null;
    private NFC_CARD_TYPE nfc_type = null;
    private long update_time = 0;
    private String uuid = null;

    public String getId() {
        return this.f47id;
    }

    public void setId(String id) {
        this.f47id = id;
    }

    public NFC_CARD_TYPE getNfc_type() {
        return this.nfc_type;
    }

    public void setNfc_type(NFC_CARD_TYPE nfc_type2) {
        this.nfc_type = nfc_type2;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid2) {
        this.uuid = uuid2;
    }

    public String getCard_no() {
        return this.card_no;
    }

    public void setCard_no(String card_no2) {
        this.card_no = card_no2;
    }

    public int getBalance() {
        return this.balance;
    }

    public void setBalance(int balance2) {
        this.balance = balance2;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count2) {
        this.count = count2;
    }

    public long getUpdate_time() {
        return this.update_time;
    }

    public void setUpdate_time(long update_time2) {
        this.update_time = update_time2;
    }

    public ConsumeFailCache fromDbLine(Cursor c) throws Exception {
        this.f47id = c.getString(c.getColumnIndexOrThrow("_id"));
        String nfcType = c.getString(c.getColumnIndexOrThrow("nfc_type"));
        if (!TextUtils.isEmpty(nfcType)) {
            this.nfc_type = NFC_CARD_TYPE.valueOf(nfcType);
        } else {
            this.nfc_type = null;
        }
        this.uuid = c.getString(c.getColumnIndexOrThrow(ContentDB.NFCConsumeFailCacheTable.UUID));
        this.card_no = c.getString(c.getColumnIndexOrThrow(ContentDB.NFCConsumeFailCacheTable.CARD_NO));
        this.balance = c.getInt(c.getColumnIndexOrThrow(ContentDB.NFCConsumeFailCacheTable.BALANCE));
        this.count = c.getInt(c.getColumnIndexOrThrow(ContentDB.NFCConsumeFailCacheTable.COUNT));
        this.update_time = c.getLong(c.getColumnIndexOrThrow("update_time"));
        return this;
    }

    public ContentValues toDbLine() {
        ContentValues cv = new ContentValues();
        cv.put("nfc_type", this.nfc_type != null ? this.nfc_type.getType() : null);
        cv.put(ContentDB.NFCConsumeFailCacheTable.UUID, this.uuid);
        cv.put(ContentDB.NFCConsumeFailCacheTable.CARD_NO, this.card_no);
        cv.put(ContentDB.NFCConsumeFailCacheTable.BALANCE, Integer.valueOf(this.balance));
        cv.put(ContentDB.NFCConsumeFailCacheTable.COUNT, Integer.valueOf(this.count));
        cv.put("update_time", Long.valueOf(this.update_time));
        return cv;
    }
}
