package com.xcharge.charger.data.proxy;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import com.xcharge.charger.data.p004db.ContentDB;
import com.xcharge.common.bean.JsonBean;

public class AuthInfo extends JsonBean<AuthInfo> {
    private String expiryDate = null;

    /* renamed from: id */
    private String f68id = null;
    private String idTag = null;
    private String parentIdTag = null;
    private String status = null;

    public String getId() {
        return this.f68id;
    }

    public void setId(String id) {
        this.f68id = id;
    }

    public String getIdTag() {
        return this.idTag;
    }

    public void setIdTag(String idTag2) {
        this.idTag = idTag2;
    }

    public String getExpiryDate() {
        return this.expiryDate;
    }

    public void setExpiryDate(String expiryDate2) {
        this.expiryDate = expiryDate2;
    }

    public String getParentIdTag() {
        return this.parentIdTag;
    }

    public void setParentIdTag(String parentIdTag2) {
        this.parentIdTag = parentIdTag2;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status2) {
        this.status = status2;
    }

    public AuthInfo fromDbLine(Cursor c) throws Exception {
        this.f68id = c.getString(c.getColumnIndexOrThrow("_id"));
        this.idTag = c.getString(c.getColumnIndexOrThrow(ContentDB.AuthInfoTable.ID_TAG));
        String expiryDate2 = c.getString(c.getColumnIndexOrThrow(ContentDB.AuthInfoTable.EXPIRY_DATE));
        if (!TextUtils.isEmpty(expiryDate2)) {
            this.expiryDate = expiryDate2;
        } else {
            this.expiryDate = null;
        }
        String paren = c.getString(c.getColumnIndexOrThrow(ContentDB.AuthInfoTable.PARENT_ID_TAG));
        if (!TextUtils.isEmpty(paren)) {
            this.parentIdTag = paren;
        } else {
            this.parentIdTag = null;
        }
        this.status = c.getString(c.getColumnIndexOrThrow(ContentDB.AuthInfoTable.STATUS));
        return this;
    }

    public ContentValues toDbLine() {
        String str;
        String str2 = null;
        ContentValues cv = new ContentValues();
        cv.put(ContentDB.AuthInfoTable.ID_TAG, this.idTag);
        if (this.expiryDate != null) {
            str = this.expiryDate;
        } else {
            str = null;
        }
        cv.put(ContentDB.AuthInfoTable.EXPIRY_DATE, str);
        if (this.parentIdTag != null) {
            str2 = this.parentIdTag;
        }
        cv.put(ContentDB.AuthInfoTable.PARENT_ID_TAG, str2);
        cv.put(ContentDB.AuthInfoTable.STATUS, this.status);
        return cv;
    }
}
