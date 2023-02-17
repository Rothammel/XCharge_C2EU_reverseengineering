package com.xcharge.charger.data.proxy;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class AuthInfo extends JsonBean<AuthInfo> {
    private String id = null;
    private String idTag = null;
    private String expiryDate = null;
    private String parentIdTag = null;
    private String status = null;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdTag() {
        return this.idTag;
    }

    public void setIdTag(String idTag) {
        this.idTag = idTag;
    }

    public String getExpiryDate() {
        return this.expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getParentIdTag() {
        return this.parentIdTag;
    }

    public void setParentIdTag(String parentIdTag) {
        this.parentIdTag = parentIdTag;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public AuthInfo fromDbLine(Cursor c) throws Exception {
        this.id = c.getString(c.getColumnIndexOrThrow("_id"));
        this.idTag = c.getString(c.getColumnIndexOrThrow(ContentDB.AuthInfoTable.ID_TAG));
        String expiryDate = c.getString(c.getColumnIndexOrThrow(ContentDB.AuthInfoTable.EXPIRY_DATE));
        if (!TextUtils.isEmpty(expiryDate)) {
            this.expiryDate = expiryDate;
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
        ContentValues cv = new ContentValues();
        cv.put(ContentDB.AuthInfoTable.ID_TAG, this.idTag);
        cv.put(ContentDB.AuthInfoTable.EXPIRY_DATE, this.expiryDate != null ? this.expiryDate : null);
        cv.put(ContentDB.AuthInfoTable.PARENT_ID_TAG, this.parentIdTag != null ? this.parentIdTag : null);
        cv.put(ContentDB.AuthInfoTable.STATUS, this.status);
        return cv;
    }
}