package p010it.sauronsoftware.ftp4j;

/* renamed from: it.sauronsoftware.ftp4j.FTPException */
public class FTPException extends Exception {
    private static final long serialVersionUID = 1;
    private int code;
    private String message;

    public FTPException(int code2) {
        this.code = code2;
    }

    public FTPException(int code2, String message2) {
        this.code = code2;
        this.message = message2;
    }

    public FTPException(FTPReply reply) {
        StringBuffer message2 = new StringBuffer();
        String[] lines = reply.getMessages();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                message2.append(System.getProperty("line.separator"));
            }
            message2.append(lines[i]);
        }
        this.code = reply.getCode();
        this.message = message2.toString();
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public String toString() {
        return new StringBuffer().append(getClass().getName()).append(" [code=").append(this.code).append(", message= ").append(this.message).append("]").toString();
    }
}
