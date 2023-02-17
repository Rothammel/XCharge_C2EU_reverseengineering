package com.xcharge.charger.data.bean;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class ConsumeFailCache extends JsonBean<ConsumeFailCache> {
    private String id = null;
    private NFC_CARD_TYPE nfc_type = null;
    private String uuid = null;
    private String card_no = null;
    private int balance = 0;
    private int count = 0;
    private long update_time = 0;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NFC_CARD_TYPE getNfc_type() {
        return this.nfc_type;
    }

    public void setNfc_type(NFC_CARD_TYPE nfc_type) {
        this.nfc_type = nfc_type;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCard_no() {
        return this.card_no;
    }

    public void setCard_no(String card_no) {
        this.card_no = card_no;
    }

    public int getBalance() {
        return this.balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getUpdate_time() {
        return this.update_time;
    }

    public void setUpdate_time(long update_time) {
        this.update_time = update_time;
    }

    public ConsumeFailCache fromDbLine(Cursor c) throws Exception {
        this.id = c.getString(c.getColumnIndexOrThrow("_id"));
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
