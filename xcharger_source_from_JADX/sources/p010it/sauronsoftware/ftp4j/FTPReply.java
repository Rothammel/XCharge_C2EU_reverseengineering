package p010it.sauronsoftware.ftp4j;

import org.apache.commons.lang3.StringUtils;

/* renamed from: it.sauronsoftware.ftp4j.FTPReply */
public class FTPReply {
    private int code = 0;
    private String[] messages;

    FTPReply(int code2, String[] messages2) {
        this.code = code2;
        this.messages = messages2;
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
