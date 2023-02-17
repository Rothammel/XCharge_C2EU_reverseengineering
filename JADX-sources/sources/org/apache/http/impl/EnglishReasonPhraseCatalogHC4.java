package org.apache.http.impl;

import com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent;
import com.xcharge.charger.ui.adapter.api.UIServiceProxy;
import it.sauronsoftware.ftp4j.FTPCodes;
import java.util.Locale;
import okhttp3.internal.http.StatusLine;
import org.apache.http.ReasonPhraseCatalog;
import org.apache.http.annotation.Immutable;
import org.apache.http.util.Args;

@Immutable
/* loaded from: classes.dex */
public class EnglishReasonPhraseCatalogHC4 implements ReasonPhraseCatalog {
    public static final EnglishReasonPhraseCatalogHC4 INSTANCE = new EnglishReasonPhraseCatalogHC4();
    private static final String[][] REASON_PHRASES;

    static {
        String[][] strArr = new String[6];
        strArr[1] = new String[3];
        strArr[2] = new String[8];
        strArr[3] = new String[8];
        strArr[4] = new String[25];
        strArr[5] = new String[8];
        REASON_PHRASES = strArr;
        setReason(200, "OK");
        setReason(201, UIServiceProxy.UI_SERIVCE_EVENT_CREATED);
        setReason(FTPCodes.SUPERFLOUS_COMMAND, "Accepted");
        setReason(204, "No Content");
        setReason(301, "Moved Permanently");
        setReason(302, "Moved Temporarily");
        setReason(304, "Not Modified");
        setReason(400, "Bad Request");
        setReason(401, "Unauthorized");
        setReason(403, "Forbidden");
        setReason(404, "Not Found");
        setReason(FTPCodes.SYNTAX_ERROR, "Internal Server Error");
        setReason(FTPCodes.SYNTAX_ERROR_IN_PARAMETERS, "Not Implemented");
        setReason(FTPCodes.COMMAND_NOT_IMPLEMENTED, "Bad Gateway");
        setReason(FTPCodes.BAD_SEQUENCE_OF_COMMANDS, "Service Unavailable");
        setReason(100, "Continue");
        setReason(StatusLine.HTTP_TEMP_REDIRECT, "Temporary Redirect");
        setReason(405, "Method Not Allowed");
        setReason(409, "Conflict");
        setReason(412, "Precondition Failed");
        setReason(413, "Request Too Long");
        setReason(414, "Request-URI Too Long");
        setReason(415, "Unsupported Media Type");
        setReason(XCloudProtocolAgent.TIMER_MQTT_CONNECT_BLOCKED, "Multiple Choices");
        setReason(303, "See Other");
        setReason(305, "Use Proxy");
        setReason(402, "Payment Required");
        setReason(406, "Not Acceptable");
        setReason(407, "Proxy Authentication Required");
        setReason(408, "Request Timeout");
        setReason(101, "Switching Protocols");
        setReason(203, "Non Authoritative Information");
        setReason(205, "Reset Content");
        setReason(206, "Partial Content");
        setReason(FTPCodes.COMMAND_PARAMETER_NOT_IMPLEMENTED, "Gateway Timeout");
        setReason(505, "Http Version Not Supported");
        setReason(410, "Gone");
        setReason(411, "Length Required");
        setReason(416, "Requested Range Not Satisfiable");
        setReason(417, "Expectation Failed");
        setReason(102, "Processing");
        setReason(207, "Multi-Status");
        setReason(422, "Unprocessable Entity");
        setReason(419, "Insufficient Space On Resource");
        setReason(420, "Method Failure");
        setReason(423, "Locked");
        setReason(507, "Insufficient Storage");
        setReason(424, "Failed Dependency");
    }

    protected EnglishReasonPhraseCatalogHC4() {
    }

    public String getReason(int status, Locale loc) {
        Args.check(status >= 100 && status < 600, "Unknown category for status code " + status);
        int category = status / 100;
        int subcode = status - (category * 100);
        if (REASON_PHRASES[category].length <= subcode) {
            return null;
        }
        String reason = REASON_PHRASES[category][subcode];
        return reason;
    }

    private static void setReason(int status, String reason) {
        int category = status / 100;
        int subcode = status - (category * 100);
        REASON_PHRASES[category][subcode] = reason;
    }
}
