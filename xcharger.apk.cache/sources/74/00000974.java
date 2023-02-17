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

/* loaded from: classes.dex */
public class MqttConnect implements ConnectInterface {
    private static MqttConnectOptions options;
    private static MqttClient client = null;
    private static MqttAsyncClient asynclient = null;
    private static long connRetryTime = 0;
    private static Logger logger = Logger.getLogger(MqttConnect.class.getName());
    private static String cachedIpUrl = null;

    public synchronized MqttClient getMqttConnect(String clientid, String credential) throws Exception {
        MqttClient mqttClient;
        synchronized (this) {
            logger.log(Level.INFO, "=====getMqttConnect======" + client);
            logger.log(Level.INFO, "=====getMqttConnect===clientid===" + clientid);
            logger.log(Level.INFO, "=====getMqttConnect==credential===" + credential);
            logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " start close");
            if (client != null) {
                if (client.isConnected()) {
                    logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " client.isConnected is true and will be closed");
                    try {
                        client.disconnect();
                    } catch (Exception e) {
                        logger.log(Level.INFO, "sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " disconnect error " + e);
                    }
                } else {
                    logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " client.isConnected is false");
                }
                try {
                    client.close();
                } catch (Exception e2) {
                    logger.log(Level.INFO, "sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " disconnect error " + e2);
                }
            }
            mqttConnect(clientid, credential);
            mqttClient = client;
        }
        return mqttClient;
    }

    public synchronized MqttAsyncClient getMqttAsyncConnect(String clientid, String credential) throws Exception {
        if (asynclient == null) {
            asynclient = new MqttConnect().mqttAsyncConnect(clientid, credential);
        }
        return asynclient;
    }

    @Override // net.xcharger.mqtt.conn.ConnectInterface
    public synchronized MqttClient mqttConnect(String clientid, String credential) throws Exception {
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
                String nonce = Integer.valueOf(new Random().nextInt(Math.abs((int) NTLMConstants.FLAG_NEGOTIATE_KEY_EXCHANGE))).toString();
                String signature = CodecUtil.Md5.encode(String.valueOf(clientid) + Long.valueOf(timestamp).toString() + nonce + credential);
                logger.info("addrurl:==" + Const.getAddrUrl + "  {\"sourceId\":\"" + clientid + "\",\"timestamp\":\"" + timestamp + "\",\"nonce\":\"" + nonce + "\",\"signature\":\"" + signature + "\"}");
                String param = "{\"sourceId\":\"" + clientid + "\",\"timestamp\":\"" + timestamp + "\",\"nonce\":\"" + nonce + "\",\"signature\":\"" + signature + "\"}";
                String postResult = GetAddr.getConfUrl(Const.getAddrUrl, param, null);
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
                        Integer addSeconds = Integer.valueOf(result);
                        connRetryTime = (addSeconds.intValue() * 1000) + nowTime;
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
                        if (postResult2 != null && postResult2.contains("result")) {
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
                                Integer addSeconds2 = Integer.valueOf(result2);
                                connRetryTime = (addSeconds2.intValue() * 1000) + nowTime;
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
            connRetryTime = 0L;
            logger.info("sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + " ------connect isConnected--" + client.isConnected());
        } catch (Exception e) {
            logger.log(Level.INFO, "sdk.mqtt client:" + (client != null ? Integer.valueOf(client.hashCode()) : null) + StringUtils.SPACE + e);
        }
        return client;
    }

    @Override // net.xcharger.mqtt.conn.ConnectInterface
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