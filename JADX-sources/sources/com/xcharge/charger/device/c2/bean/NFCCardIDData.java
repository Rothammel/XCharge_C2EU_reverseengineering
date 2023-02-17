package com.xcharge.charger.device.c2.bean;

import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class NFCCardIDData extends JsonBean<NFCCardIDData> {
    private int uuid = 0;
    private String cardNo = null;
    private NFC_CARD_TYPE cardType = null;

    public int getUuid() {
        return this.uuid;
    }

    public void setUuid(int uuid) {
        this.uuid = uuid;
    }

    public String getCardNo() {
        return this.cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public NFC_CARD_TYPE getCardType() {
        return this.cardType;
    }

    public void setCardType(NFC_CARD_TYPE cardType) {
        this.cardType = cardType;
    }
}
