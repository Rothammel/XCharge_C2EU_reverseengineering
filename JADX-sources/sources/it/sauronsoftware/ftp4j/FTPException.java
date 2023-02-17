package it.sauronsoftware.ftp4j;

/* loaded from: classes.dex */
public class FTPException extends Exception {
    private static final long serialVersionUID = 1;
    private int code;
    private String message;

    public FTPException(int code) {
        this.code = code;
    }

    public FTPException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public FTPException(FTPReply reply) {
        StringBuffer message = new StringBuffer();
        String[] lines = reply.getMessages();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                message.append(System.getProperty("line.separator"));
            }
            message.append(lines[i]);
        }
        this.code = reply.getCode();
        this.message = message.toString();
    }

    public int getCode() {
        return this.code;
    }

    @Override // java.lang.Throwable
    public String getMessage() {
        return this.message;
    }

    @Override // java.lang.Throwable
    public String toString() {
        return new StringBuffer().append(getClass().getName()).append(" [code=").append(this.code).append(", message= ").append(this.message).append("]").toString();
    }
}
