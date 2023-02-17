package com.xcharge.charger.protocol.xmsz.bean.device;

import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import org.apache.commons.lang3.CharEncoding;

/* loaded from: classes.dex */
public class AuthorizeIDRequest extends XMSZMessage {
    private String userIdTag = "";
    private String userPasswordMd5 = "";
    private String soleCardId = "";

    public String getUserIdTag() {
        return this.userIdTag;
    }

    public void setUserIdTag(String userIdTag) {
        this.userIdTag = userIdTag;
    }

    public String getUserPasswordMd5() {
        return this.userPasswordMd5;
    }

    public void setUserPasswordMd5(String userPasswordMd5) {
        this.userPasswordMd5 = userPasswordMd5;
    }

    public String getSoleCardId() {
        return this.soleCardId;
    }

    public void setSoleCardId(String soleCardId) {
        this.soleCardId = soleCardId;
    }

    @Override // com.xcharge.charger.protocol.xmsz.bean.XMSZMessage
    public byte[] bodyToBytes() throws Exception {
        byte[] bytes = new byte[64];
        byte[] userIdTagBytes = this.userIdTag.getBytes(XMSZMessage.GBK_NAME);
        int userIdTagLength = userIdTagBytes.length > 16 ? 16 : userIdTagBytes.length;
        System.arraycopy(userIdTagBytes, 0, bytes, 0, userIdTagLength);
        byte[] userPasswordMd5Bytes = this.userPasswordMd5.getBytes(CharEncoding.UTF_8);
        int userPasswordMd5Length = userPasswordMd5Bytes.length > 32 ? 32 : userPasswordMd5Bytes.length;
        System.arraycopy(userPasswordMd5Bytes, 0, bytes, 16, userPasswordMd5Length);
        byte[] soleCardIdBytes = this.soleCardId.getBytes(XMSZMessage.GBK_NAME);
        int soleCardIdLength = soleCardIdBytes.length > 32 ? 32 : soleCardIdBytes.length;
        System.arraycopy(soleCardIdBytes, 0, bytes, 48, soleCardIdLength);
        return bytes;
    }

    @Override // com.xcharge.charger.protocol.xmsz.bean.XMSZMessage
    public XMSZMessage bodyFromBytes(byte[] bytes) throws Exception {
        return null;
    }
}
