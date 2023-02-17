package it.sauronsoftware.ftp4j;

import org.apache.commons.lang3.StringUtils;

/* loaded from: classes.dex */
public class FTPReply {
    private int code;
    private String[] messages;

    /* JADX INFO: Access modifiers changed from: package-private */
    public FTPReply(int code, String[] messages) {
        this.code = 0;
        this.code = code;
        this.messages = messages;
    }

    public int getCode() {
        return this.code;
    }

    public boolean isSuccessCode() {
        int aux = this.code - 200;
        return aux >= 0 && aux < 100;
    }

    public String[] getMessages() {
        return this.messages;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getClass().getName());
        buffer.append(" [code=");
        buffer.append(this.code);
        buffer.append(", message=");
        for (int i = 0; i < this.messages.length; i++) {
            if (i > 0) {
                buffer.append(StringUtils.SPACE);
            }
            buffer.append(this.messages[i]);
        }
        buffer.append("]");
        return buffer.toString();
    }
}