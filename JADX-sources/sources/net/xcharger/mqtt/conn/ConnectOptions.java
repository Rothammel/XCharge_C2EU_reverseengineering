package net.xcharger.mqtt.conn;

import android.util.Log;
import com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.xcharger.mqtt.MqttKit;
import net.xcharger.mqtt.core.Const;
import net.xcharger.util.CodecUtil;
import org.apache.http.util.TextUtils;
import org.apache.mina.proxy.handlers.http.ntlm.NTLMConstants;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/* loaded from: classes.dex */
public class ConnectOptions {
    private static Logger logger = Logger.getLogger(MqttConnect.class.getName());
    private String version;

    public static MqttConnectOptions connectOptions(String clientid, String credential) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        if (TextUtils.isEmpty(Const.userName)) {
            options.setUserName(clientid);
        } else {
            options.setUserName(Const.userName);
        }
        long timestamp = new Date().getTime();
        String nonce = Integer.valueOf(new Random().nextInt(Math.abs((int) NTLMConstants.FLAG_NEGOTIATE_KEY_EXCHANGE))).toString();
        String sign = CodecUtil.Md5.encode(String.valueOf(clientid) + Long.valueOf(timestamp).toString() + nonce + credential);
        logger.log(Level.INFO, "clientid :===" + clientid);
        logger.log(Level.INFO, "pa=== :" + timestamp + "_" + nonce + "_" + sign);
        if (TextUtils.isEmpty(Const.password)) {
            options.setPassword((String.valueOf(timestamp) + "_" + nonce + "_" + sign).toCharArray());
        } else {
            options.setPassword(Const.password.toCharArray());
        }
        options.setKeepAliveInterval(Const.keepAlive);
        options.setConnectionTimeout(Const.timeOut);
        String msg = "ReportNetworkStatus " + Const.VERSION + "\n{online:false}";
        if (Const.binaryMode) {
            try {
                byte[] bytes = MqttKit.encoder.encode(clientid, XCloudMessage.ReportNetworkStatus, Const.VERSION, "{online:false}");
                if (TextUtils.isEmpty(Const.upTopic)) {
                    options.setWill(String.valueOf(Const.device_message_from) + clientid, bytes, 1, false);
                } else {
                    options.setWill(Const.upTopic, bytes, 1, false);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "MqttConnectOptions.connectOptions except: " + Log.getStackTraceString(e));
            }
        } else if (TextUtils.isEmpty(Const.upTopic)) {
            options.setWill(String.valueOf(Const.device_message_from) + clientid, msg.getBytes(), 1, false);
        } else {
            options.setWill(Const.upTopic, msg.getBytes(), 1, false);
        }
        return options;
    }

    public static MqttMessage msg() {
        MqttMessage msg = new MqttMessage();
        msg.setQos(Const.Qos);
        msg.setRetained(false);
        return msg;
    }
}
