package com.xcharge.charger.device.p005c2.bean;

import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.common.bean.JsonBean;

/* renamed from: com.xcharge.charger.device.c2.bean.NFCCardIDData */
public class NFCCardIDData extends JsonBean<NFCCardIDData> {
    private String cardNo = null;
    private NFC_CARD_TYPE cardType = null;
    private int uuid = 0;

    public int getUuid() {
        return this.uuid;
    }

    public void setUuid(int uuid2) {
        this.uuid = uuid2;
    }

    public String getCardNo() {
        return this.cardNo;
    }

    public void setCardNo(String cardNo2) {
        this.cardNo = cardNo2;
    }

    public NFC_CARD_TYPE getCardType() {
        return this.cardType;
    }

    public void setCardType(NFC_CARD_TYPE cardType2) {
        this.cardType = cardType2;
    }
}
