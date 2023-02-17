package p010it.sauronsoftware.ftp4j;

/* renamed from: it.sauronsoftware.ftp4j.FTPListParser */
public interface FTPListParser {
    FTPFile[] parse(String[] strArr) throws FTPListParseException;
}
