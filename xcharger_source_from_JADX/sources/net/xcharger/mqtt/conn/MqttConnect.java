package net.xcharger.mqtt.conn;

import android.util.Log;
import com.xcharge.common.utils.LogUtils;
import java.net.InetAddress;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.xcharger.mqtt.MqttKit;
import net.xcharger.mqtt.core.Const;
import net.xcharger.sdk.device.MessageProxyException;
import net.xcharger.util.CodecUtil;
import net.xcharger.util.GetAddr;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.apache.mina.proxy.handlers.http.ntlm.NTLMConstants;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttConnect implements ConnectInterface {
    private static MqttAsyncClient asynclient = null;
    private static String cachedIpUrl = null;
    private static MqttClient client = null;
    private static long connRetryTime = 0;
    private static Logger logger = Logger.getLogger(MqttConnect.class.getName());
    private static MqttConnectOptions options;

    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0117, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0118, code lost:
        r2 = logger;
        r3 = java.util.logging.Level.INFO;
        r4 = new java.lang.StringBuilder("sdk.mqtt client:");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0125, code lost:
        if (client != null) goto L_0x0127;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0127, code lost:
        r1 = java.lang.Integer.valueOf(client.hashCode());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0131, code lost:
        r2.log(r3, r4.append(r1).append(" disconnect error ").append(r0).toString());
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized org.eclipse.paho.client.mqttv3.MqttClient getMqttConnect(java.lang.String r7, java.lang.String r8) throws java.lang.Exception {
        /*
            r6 = this;
            r1 = 0
            monitor-enter(r6)
            java.util.logging.Logger r2 = logger     // Catch:{ all -> 0x00e7 }
            java.util.logging.Level r3 = java.util.logging.Level.INFO     // Catch:{ all -> 0x00e7 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x00e7 }
            java.lang.String r5 = "=====getMqttConnect======"
            r4.<init>(r5)     // Catch:{ all -> 0x00e7 }
            org.eclipse.paho.client.mqttv3.MqttClient r5 = client     // Catch:{ all -> 0x00e7 }
            java.lang.StringBuilder r4 = r4.append(r5)     // Catch:{ all -> 0x00e7 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x00e7 }
            r2.log(r3, r4)     // Catch:{ all -> 0x00e7 }
            java.util.logging.Logger r2 = logger     // Catch:{ all -> 0x00e7 }
            java.util.logging.Level r3 = java.util.logging.Level.INFO     // Catch:{ all -> 0x00e7 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x00e7 }
            java.lang.String r5 = "=====getMqttConnect===clientid==="
            r4.<init>(r5)     // Catch:{ all -> 0x00e7 }
            java.lang.StringBuilder r4 = r4.append(r7)     // Catch:{ all -> 0x00e7 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x00e7 }
            r2.log(r3, r4)     // Catch:{ all -> 0x00e7 }
            java.util.logging.Logger r2 = logger     // Catch:{ all -> 0x00e7 }
            java.util.logging.Level r3 = java.util.logging.Level.INFO     // Catch:{ all -> 0x00e7 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x00e7 }
            java.lang.String r5 = "=====getMqttConnect==credential==="
            r4.<init>(r5)     // Catch:{ all -> 0x00e7 }
            java.lang.StringBuilder r4 = r4.append(r8)     // Catch:{ all -> 0x00e7 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x00e7 }
            r2.log(r3, r4)     // Catch:{ all -> 0x00e7 }
            java.util.logging.Logger r3 = logger     // Catch:{ all -> 0x00e7 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x00e7 }
            java.lang.String r2 = "sdk.mqtt client:"
            r4.<init>(r2)     // Catch:{ all -> 0x00e7 }
            org.eclipse.paho.client.mqttv3.MqttClient r2 = client     // Catch:{ all -> 0x00e7 }
            if (r2 == 0) goto L_0x00b3
            org.eclipse.paho.client.mqttv3.MqttClient r2 = client     // Catch:{ all -> 0x00e7 }
            int r2 = r2.hashCode()     // Catch:{ all -> 0x00e7 }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)     // Catch:{ all -> 0x00e7 }
        L_0x005d:
            java.lang.StringBuilder r2 = r4.append(r2)     // Catch:{ all -> 0x00e7 }
            java.lang.String r4 = " start close"
            java.lang.StringBuilder r2 = r2.append(r4)     // Catch:{ all -> 0x00e7 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x00e7 }
            r3.info(r2)     // Catch:{ all -> 0x00e7 }
            org.eclipse.paho.client.mqttv3.MqttClient r2 = client     // Catch:{ all -> 0x00e7 }
            if (r2 == 0) goto L_0x00ac
            org.eclipse.paho.client.mqttv3.MqttClient r2 = client     // Catch:{ all -> 0x00e7 }
            boolean r2 = r2.isConnected()     // Catch:{ all -> 0x00e7 }
            if (r2 == 0) goto L_0x00ec
            java.util.logging.Logger r3 = logger     // Catch:{ all -> 0x00e7 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x00e7 }
            java.lang.String r2 = "sdk.mqtt client:"
            r4.<init>(r2)     // Catch:{ all -> 0x00e7 }
            org.eclipse.paho.client.mqttv3.MqttClient r2 = client     // Catch:{ all -> 0x00e7 }
            if (r2 == 0) goto L_0x00b5
            org.eclipse.paho.client.mqttv3.MqttClient r2 = client     // Catch:{ all -> 0x00e7 }
            int r2 = r2.hashCode()     // Catch:{ all -> 0x00e7 }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)     // Catch:{ all -> 0x00e7 }
        L_0x0091:
            java.lang.StringBuilder r2 = r4.append(r2)     // Catch:{ all -> 0x00e7 }
            java.lang.String r4 = " client.isConnected is true and will be closed"
            java.lang.StringBuilder r2 = r2.append(r4)     // Catch:{ all -> 0x00e7 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x00e7 }
            r3.info(r2)     // Catch:{ all -> 0x00e7 }
            org.eclipse.paho.client.mqttv3.MqttClient r2 = client     // Catch:{ Exception -> 0x00b7 }
            r2.disconnect()     // Catch:{ Exception -> 0x00b7 }
        L_0x00a7:
            org.eclipse.paho.client.mqttv3.MqttClient r2 = client     // Catch:{ Exception -> 0x0117 }
            r2.close()     // Catch:{ Exception -> 0x0117 }
        L_0x00ac:
            r6.mqttConnect(r7, r8)     // Catch:{ all -> 0x00e7 }
            org.eclipse.paho.client.mqttv3.MqttClient r1 = client     // Catch:{ all -> 0x00e7 }
            monitor-exit(r6)
            return r1
        L_0x00b3:
            r2 = r1
            goto L_0x005d
        L_0x00b5:
            r2 = r1
            goto L_0x0091
        L_0x00b7:
            r0 = move-exception
            java.util.logging.Logger r3 = logger     // Catch:{ all -> 0x00e7 }
            java.util.logging.Level r4 = java.util.logging.Level.INFO     // Catch:{ all -> 0x00e7 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x00e7 }
            java.lang.String r2 = "sdk.mqtt client:"
            r5.<init>(r2)     // Catch:{ all -> 0x00e7 }
            org.eclipse.paho.client.mqttv3.MqttClient r2 = client     // Catch:{ all -> 0x00e7 }
            if (r2 == 0) goto L_0x00ea
            org.eclipse.paho.client.mqttv3.MqttClient r2 = client     // Catch:{ all -> 0x00e7 }
            int r2 = r2.hashCode()     // Catch:{ all -> 0x00e7 }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)     // Catch:{ all -> 0x00e7 }
        L_0x00d1:
            java.lang.StringBuilder r2 = r5.append(r2)     // Catch:{ all -> 0x00e7 }
            java.lang.String r5 = " disconnect error "
            java.lang.StringBuilder r2 = r2.append(r5)     // Catch:{ all -> 0x00e7 }
            java.lang.StringBuilder r2 = r2.append(r0)     // Catch:{ all -> 0x00e7 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x00e7 }
            r3.log(r4, r2)     // Catch:{ all -> 0x00e7 }
            goto L_0x00a7
        L_0x00e7:
            r1 = move-exception
            monitor-exit(r6)
            throw r1
        L_0x00ea:
            r2 = r1
            goto L_0x00d1
        L_0x00ec:
            java.util.logging.Logger r3 = logger     // Catch:{ all -> 0x00e7 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x00e7 }
            java.lang.String r2 = "sdk.mqtt client:"
            r4.<init>(r2)     // Catch:{ all -> 0x00e7 }
            org.eclipse.paho.client.mqttv3.MqttClient r2 = client     // Catch:{ all -> 0x00e7 }
            if (r2 == 0) goto L_0x0115
            org.eclipse.paho.client.mqttv3.MqttClient r2 = client     // Catch:{ all -> 0x00e7 }
            int r2 = r2.hashCode()     // Catch:{ all -> 0x00e7 }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)     // Catch:{ all -> 0x00e7 }
        L_0x0103:
            java.lang.StringBuilder r2 = r4.append(r2)     // Catch:{ all -> 0x00e7 }
            java.lang.String r4 = " client.isConnected is false"
            java.lang.StringBuilder r2 = r2.append(r4)     // Catch:{ all -> 0x00e7 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x00e7 }
            r3.info(r2)     // Catch:{ all -> 0x00e7 }
            goto L_0x00a7
        L_0x0115:
            r2 = r1
            goto L_0x0103
        L_0x0117:
            r0 = move-exception
            java.util.logging.Logger r2 = logger     // Catch:{ all -> 0x00e7 }
            java.util.logging.Level r3 = java.util.logging.Level.INFO     // Catch:{ all -> 0x00e7 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x00e7 }
            java.lang.String r5 = "sdk.mqtt client:"
            r4.<init>(r5)     // Catch:{ all -> 0x00e7 }
            org.eclipse.paho.client.mqttv3.MqttClient r5 = client     // Catch:{ all -> 0x00e7 }
            if (r5 == 0) goto L_0x0131
            org.eclipse.paho.client.mqttv3.MqttClient r1 = client     // Catch:{ all -> 0x00e7 }
            int r1 = r1.hashCode()     // Catch:{ all -> 0x00e7 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x00e7 }
        L_0x0131:
            java.lang.StringBuilder r1 = r4.append(r1)     // Catch:{ all -> 0x00e7 }
            java.lang.String r4 = " disconnect error "
            java.lang.StringBuilder r1 = r1.append(r4)     // Catch:{ all -> 0x00e7 }
            java.lang.StringBuilder r1 = r1.append(r0)     // Catch:{ all -> 0x00e7 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x00e7 }
            r2.log(r3, r1)     // Catch:{ all -> 0x00e7 }
            goto L_0x00ac
        */
        throw new UnsupportedOperationException("Method not decompiled: net.xcharger.mqtt.conn.MqttConnect.getMqttConnect(java.lang.String, java.lang.String):org.eclipse.paho.client.mqttv3.MqttClient");
    }

    public synchronized MqttAsyncClient getMqttAsyncConnect(String clientid, String credential) throws Exception {
        if (asynclient == null) {
            asynclient = new MqttConnect().mqttAsyncConnect(clientid, credential);
        }
        return asynclient;
    }

    public synchronized MqttClient mqttConnect(String clientid, String credential) throws Exception {
        Integer num;
        String host;
        try {
            logger.log(Level.INFO, "clientid:-----" + clientid + ", credential:-----" + credential);
            if (!TextUtils.isEmpty(Const.getAddrUrl)) {
                long nowTime = new Date().getTime();
                if (connRetryTime > nowTime) {
                    logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " CONN_TOO_OFTEN");
                    throw new MessageProxyException("CONN_TOO_OFTEN");
                }
                long timestamp = new Date().getTime();
                String nonce = Integer.valueOf(new Random().nextInt(Math.abs(NTLMConstants.FLAG_NEGOTIATE_KEY_EXCHANGE))).toString();
                String signature = CodecUtil.Md5.encode(String.valueOf(clientid) + Long.valueOf(timestamp).toString() + nonce + credential);
                logger.info("addrurl:==" + Const.getAddrUrl + "  {\"sourceId\":\"" + clientid + "\",\"timestamp\":\"" + timestamp + "\",\"nonce\":\"" + nonce + "\",\"signature\":\"" + signature + "\"}");
                String param = "{\"sourceId\":\"" + clientid + "\",\"timestamp\":\"" + timestamp + "\",\"nonce\":\"" + nonce + "\",\"signature\":\"" + signature + "\"}";
                String postResult = GetAddr.getConfUrl(Const.getAddrUrl, param, (String) null);
                logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " addr result : " + postResult);
                if (postResult != null && postResult.contains("result")) {
                    if (Const.devMode) {
                        String ip = parseDN("xcloud.dev.xcharger.net");
                        if (!TextUtils.isEmpty(ip)) {
                            cachedIpUrl = "http://" + ip + "/Addr/getBrokerForDevice/v1.0";
                        }
                    } else {
                        String ip2 = parseDN("addr.xcloud.xcharger.net");
                        if (!TextUtils.isEmpty(ip2)) {
                            cachedIpUrl = "http://" + ip2 + "/getBrokerForDevice/v1.0";
                        }
                    }
                    logger.info("cachedIpUrl:=====" + cachedIpUrl);
                    String result = postResult.replaceAll(".*\"result\":\"(.*?)\".*", "$1").trim();
                    logger.info("result:=====" + result);
                    if (result != null && result.contains("tcp")) {
                        logger.log(Level.INFO, result);
                        logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " start new client, ");
                        if (TextUtils.isEmpty(Const.clientId)) {
                            client = new MqttClient(result, clientid, new MemoryPersistence());
                        } else {
                            client = new MqttClient(result, Const.clientId, new MemoryPersistence());
                        }
                        logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " client is new");
                        logger.log(Level.INFO, "client ---" + client.isConnected());
                        LogUtils.cloudlog("succeed to get mqtt broker:" + result + " by url:" + Const.getAddrUrl);
                    } else if (result != null && result.matches("[0-9]+")) {
                        logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " CONN_TOO_OFTEN");
                        connRetryTime = ((long) (Integer.valueOf(result).intValue() * 1000)) + nowTime;
                        throw new MessageProxyException("CONN_TOO_OFTEN");
                    }
                } else if (!TextUtils.isEmpty(cachedIpUrl)) {
                    if (Const.devMode) {
                        host = "xcloud.dev.xcharger.net";
                    } else {
                        host = "addr.xcloud.xcharger.net";
                    }
                    if (TextUtils.isEmpty(parseDN(host))) {
                        logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + ", try to get addr config by cached ip url:" + cachedIpUrl);
                        LogUtils.cloudlog("failed to parse DN:" + host + ", try to get mqtt broker config by cached ip url:" + cachedIpUrl);
                        String postResult2 = GetAddr.getConfUrl(cachedIpUrl, param, host);
                        logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " addr result : " + postResult2);
                        if (postResult2 == null || !postResult2.contains("result")) {
                            logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " CONN_ADDR_FAILED");
                            throw new MessageProxyException("CONN_ADDR_FAILED");
                        }
                        String result2 = postResult2.replaceAll(".*\"result\":\"(.*?)\".*", "$1").trim();
                        logger.info("result:=====" + result2);
                        if (result2 != null && result2.contains("tcp")) {
                            logger.log(Level.INFO, result2);
                            logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " start new client, ");
                            if (TextUtils.isEmpty(Const.clientId)) {
                                client = new MqttClient(result2, clientid, new MemoryPersistence());
                            } else {
                                client = new MqttClient(result2, Const.clientId, new MemoryPersistence());
                            }
                            logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " client is new");
                            logger.log(Level.INFO, "client ---" + client.isConnected());
                            LogUtils.cloudlog("succeed to get mqtt broker:" + result2 + " by cached ip url:" + cachedIpUrl);
                        } else if (result2 != null && result2.matches("[0-9]+")) {
                            logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " CONN_TOO_OFTEN");
                            connRetryTime = ((long) (Integer.valueOf(result2).intValue() * 1000)) + nowTime;
                            throw new MessageProxyException("CONN_TOO_OFTEN");
                        }
                    } else {
                        logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " CONN_ADDR_FAILED");
                        throw new MessageProxyException("CONN_ADDR_FAILED");
                    }
                } else {
                    logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " CONN_ADDR_FAILED");
                    throw new MessageProxyException("CONN_ADDR_FAILED");
                }
            } else if (TextUtils.isEmpty(Const.broker)) {
                logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " NO_BROKER_CONFIG");
                throw new MessageProxyException("NO_BROKER_CONFIG");
            } else {
                logger.log(Level.INFO, Const.broker);
                logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " start new client, ");
                if (TextUtils.isEmpty(Const.clientId)) {
                    client = new MqttClient(Const.broker, clientid, new MemoryPersistence());
                } else {
                    client = new MqttClient(Const.broker, Const.clientId, new MemoryPersistence());
                }
                logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " client is new");
                logger.log(Level.INFO, "client ---" + client.isConnected());
            }
            logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " start set connect option");
            new ConnectOptions();
            options = ConnectOptions.connectOptions(clientid, credential);
            logger.log(Level.INFO, "KeepAliveInterval:-----" + options.getKeepAliveInterval());
            logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " >>>>>>>connect start >>>>>>>>>>>>>>");
            client.connect(options);
            logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " >>>>>>>connect end   >>>>>>>>>>>>>>");
            connRetryTime = 0;
            Logger logger2 = logger;
            StringBuilder sb = new StringBuilder("sdk.mqtt client:");
            if (client != null) {
                num = Integer.valueOf(client.hashCode());
            } else {
                num = null;
            }
            logger2.info(sb.append(num).append(" ------connect isConnected--").append(client.isConnected()).toString());
        } catch (Exception e) {
            logger.log(Level.INFO, "sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + StringUtils.SPACE + e);
        }
        return client;
    }

    public MqttAsyncClient mqttAsyncConnect(String clientid, String credential) throws Exception {
        if (asynclient == null) {
            try {
                asynclient = new MqttAsyncClient(Const.getAddrUrl, clientid);
                new ConnectOptions();
                options = ConnectOptions.connectOptions(clientid, credential);
                asynclient.connect(options);
            } catch (Exception e) {
                asynclient = null;
                logger.log(Level.WARNING, "mqttConnect error ");
                new MessageProxyException(e.toString());
            }
        }
        return asynclient;
    }

    public static boolean doStop() {
        logger.log(Level.INFO, "stop=====" + client.isConnected() + "=============");
        try {
            if (client != null && client.isConnected()) {
                MqttKit.disConnect();
                client.disconnect();
            }
            if (client != null) {
                client.close();
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "mqttConnect stop error " + e.getMessage());
            new MessageProxyException(e.toString());
        }
        client = null;
        return true;
    }

    public static boolean clearClient() throws Exception {
        logger.log(Level.INFO, "  clear client " + client);
        try {
            if (client != null) {
                if (client.isConnected()) {
                    client.disconnect();
                    logger.log(Level.INFO, "  clear client ok");
                }
                client.close();
            }
        } catch (MqttException e) {
            client = null;
            logger.log(Level.WARNING, "  clear client error " + e.getMessage());
        }
        client = null;
        return true;
    }

    public static void forceToClearStatus() {
        client = null;
    }

    public static final MqttClient getClient() {
        return client;
    }

    public static MqttAsyncClient getAsyncConnect() {
        return asynclient;
    }

    public static boolean clientIsClient() {
        return client.isConnected();
    }

    private String parseDN(String dn) {
        try {
            return InetAddress.getByName(dn).getHostAddress();
        } catch (Exception e) {
            logger.log(Level.WARNING, "failed to parse DN: " + dn + ", except: " + Log.getStackTraceString(e));
            return null;
        }
    }
}
