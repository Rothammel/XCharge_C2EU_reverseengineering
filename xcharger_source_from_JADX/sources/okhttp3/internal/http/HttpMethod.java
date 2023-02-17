package okhttp3.internal.http;

import org.apache.http.client.methods.HttpDeleteHC4;
import org.apache.http.client.methods.HttpHeadHC4;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPostHC4;

public final class HttpMethod {
    public static boolean invalidatesCache(String method) {
        return method.equals(HttpPostHC4.METHOD_NAME) || method.equals(HttpPatch.METHOD_NAME) || method.equals("PUT") || method.equals(HttpDeleteHC4.METHOD_NAME) || method.equals("MOVE");
    }

    public static boolean requiresRequestBody(String method) {
        return method.equals(HttpPostHC4.METHOD_NAME) || method.equals("PUT") || method.equals(HttpPatch.METHOD_NAME) || method.equals("PROPPATCH") || method.equals("REPORT");
    }

    public static boolean permitsRequestBody(String method) {
        return !method.equals("GET") && !method.equals(HttpHeadHC4.METHOD_NAME);
    }

    public static boolean redirectsWithBody(String method) {
        return method.equals("PROPFIND");
    }

    public static boolean redirectsToGet(String method) {
        return !method.equals("PROPFIND");
    }

    private HttpMethod() {
    }
}
