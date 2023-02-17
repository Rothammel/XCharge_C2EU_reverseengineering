package it.sauronsoftware.ftp4j;

/* loaded from: classes.dex */
public interface FTPListParser {
    FTPFile[] parse(String[] strArr) throws FTPListParseException;
}