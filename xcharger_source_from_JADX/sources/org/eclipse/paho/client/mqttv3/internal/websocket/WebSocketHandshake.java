package org.eclipse.paho.client.mqttv3.internal.websocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WebSocketHandshake {
    private static final String ACCEPT_SALT = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private static final String EMPTY = "";
    private static final String HTTP_HEADER_CONNECTION = "connection";
    private static final String HTTP_HEADER_CONNECTION_VALUE = "upgrade";
    private static final String HTTP_HEADER_SEC_WEBSOCKET_ACCEPT = "sec-websocket-accept";
    private static final String HTTP_HEADER_SEC_WEBSOCKET_PROTOCOL = "sec-websocket-protocol";
    private static final String HTTP_HEADER_UPGRADE = "upgrade";
    private static final String HTTP_HEADER_UPGRADE_WEBSOCKET = "websocket";
    private static final String LINE_SEPARATOR = "\r\n";
    private static final String SHA1_PROTOCOL = "SHA1";
    String host;
    InputStream input;
    OutputStream output;
    int port;
    String uri;

    public WebSocketHandshake(InputStream input2, OutputStream output2, String uri2, String host2, int port2) {
        this.input = input2;
        this.output = output2;
        this.uri = uri2;
        this.host = host2;
        this.port = port2;
    }

    public void execute() throws IOException {
        byte[] key = new byte[16];
        System.arraycopy(UUID.randomUUID().toString().getBytes(), 0, key, 0, 16);
        String b64Key = Base64.encodeBytes(key);
        sendHandshakeRequest(b64Key);
        receiveHandshakeResponse(b64Key);
    }

    private void sendHandshakeRequest(String key) throws IOException {
        String path = "/mqtt";
        try {
            URI srvUri = new URI(this.uri);
            if (srvUri.getRawPath() != null && !srvUri.getRawPath().isEmpty()) {
                path = srvUri.getRawPath();
                if (srvUri.getRawQuery() != null && !srvUri.getRawQuery().isEmpty()) {
                    path = String.valueOf(path) + "?" + srvUri.getRawQuery();
                }
            }
            PrintWriter pw = new PrintWriter(this.output);
            pw.print("GET " + path + " HTTP/1.1" + "\r\n");
            if (this.port == 80 || this.port == 443) {
                pw.print("Host: " + this.host + "\r\n");
            } else {
                pw.print("Host: " + this.host + ":" + this.port + "\r\n");
            }
            pw.print("Upgrade: websocket\r\n");
            pw.print("Connection: Upgrade\r\n");
            pw.print("Sec-WebSocket-Key: " + key + "\r\n");
            pw.print("Sec-WebSocket-Protocol: mqtt\r\n");
            pw.print("Sec-WebSocket-Version: 13\r\n");
            String userInfo = srvUri.getUserInfo();
            if (userInfo != null) {
                pw.print("Authorization: Basic " + Base64.encode(userInfo) + "\r\n");
            }
            pw.print("\r\n");
            pw.flush();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    private void receiveHandshakeResponse(String key) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(this.input));
        ArrayList responseLines = new ArrayList();
        String line = in.readLine();
        if (line == null) {
            throw new IOException("WebSocket Response header: Invalid response from Server, It may not support WebSockets.");
        }
        while (!line.equals("")) {
            responseLines.add(line);
            line = in.readLine();
        }
        Map headerMap = getHeaders(responseLines);
        String connectionHeader = (String) headerMap.get(HTTP_HEADER_CONNECTION);
        if (connectionHeader == null || connectionHeader.equalsIgnoreCase("upgrade")) {
            throw new IOException("WebSocket Response header: Incorrect connection header");
        }
        String upgradeHeader = (String) headerMap.get("upgrade");
        if (upgradeHeader == null || !upgradeHeader.toLowerCase().contains(HTTP_HEADER_UPGRADE_WEBSOCKET)) {
            throw new IOException("WebSocket Response header: Incorrect upgrade.");
        } else if (((String) headerMap.get(HTTP_HEADER_SEC_WEBSOCKET_PROTOCOL)) == null) {
            throw new IOException("WebSocket Response header: empty sec-websocket-protocol");
        } else if (!headerMap.containsKey(HTTP_HEADER_SEC_WEBSOCKET_ACCEPT)) {
            throw new IOException("WebSocket Response header: Missing Sec-WebSocket-Accept");
        } else {
            try {
                verifyWebSocketKey(key, (String) headerMap.get(HTTP_HEADER_SEC_WEBSOCKET_ACCEPT));
            } catch (NoSuchAlgorithmException e) {
                throw new IOException(e.getMessage());
            } catch (HandshakeFailedException e2) {
                throw new IOException("WebSocket Response header: Incorrect Sec-WebSocket-Key");
            }
        }
    }

    private Map getHeaders(ArrayList headers) {
        Map headerMap = new HashMap();
        for (int i = 1; i < headers.size(); i++) {
            String[] header = ((String) headers.get(i)).split(":");
            headerMap.put(header[0].toLowerCase(), header[1]);
        }
        return headerMap;
    }

    private void verifyWebSocketKey(String key, String accept) throws NoSuchAlgorithmException, HandshakeFailedException {
        if (!Base64.encodeBytes(sha1(String.valueOf(key) + ACCEPT_SALT)).trim().equals(accept.trim())) {
            throw new HandshakeFailedException();
        }
    }

    private byte[] sha1(String input2) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(SHA1_PROTOCOL).digest(input2.getBytes());
    }
}
