package com.xcharge.charger.protocol.xmsz.bean.device;

import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import org.apache.commons.lang3.CharEncoding;

public class AuthorizeIDRequest extends XMSZMessage {
    private String soleCardId = "";
    private String userIdTag = "";
    private String userPasswordMd5 = "";

    public String getUserIdTag() {
        return this.userIdTag;
    }

    public void setUserIdTag(String userIdTag2) {
        this.userIdTag = userIdTag2;
    }

    public String getUserPasswordMd5() {
        return this.userPasswordMd5;
    }

    public void setUserPasswordMd5(String userPasswordMd52) {
        this.userPasswordMd5 = userPasswordMd52;
    }

    public String getSoleCardId() {
        return this.soleCardId;
    }

    public void setSoleCardId(String soleCardId2) {
        this.soleCardId = soleCardId2;
    }

    public byte[] bodyToBytes() throws Exception {
        byte[] bytes = new byte[64];
        byte[] userIdTagBytes = this.userIdTag.getBytes(XMSZMessage.GBK_NAME);
        System.arraycopy(userIdTagBytes, 0, bytes, 0, userIdTagBytes.length > 16 ? 16 : userIdTagBytes.length);
        byte[] userPasswordMd5Bytes = this.userPasswordMd5.getBytes(CharEncoding.UTF_8);
        System.arraycopy(userPasswordMd5Bytes, 0, bytes, 16, userPasswordMd5Bytes.length > 32 ? 32 : userPasswordMd5Bytes.length);
        byte[] soleCardIdBytes = this.soleCardId.getBytes(XMSZMessage.GBK_NAME);
        System.arraycopy(soleCardIdBytes, 0, bytes, 48, soleCardIdBytes.length > 32 ? 32 : soleCardIdBytes.length);
        return bytes;
    }

    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
