package net.xcharger.sdk.device;

import java.text.SimpleDateFormat;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import net.xcharger.mqtt.core.Const;
import net.xcharger.mqtt.device.MqttMessageProxy;
import org.apache.commons.lang3.StringUtils;

public class MessageProxyFactory {
    static {
        Logger logger = Logger.getLogger("");
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new Formatter() {
            public String format(LogRecord record) {
                if (!record.getMessage().startsWith("sdk.mqtt")) {
                    return "";
                }
                String dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Long.valueOf(record.getMillis()));
                StringBuilder stringBuilder = new StringBuilder("sdk.mqtt ");
                stringBuilder.append(dateFormat).append(StringUtils.SPACE).append(record.getSourceClassName()).append(".").append(record.getSourceMethodName()).append(": ").append(record.getMessage());
                System.out.println(stringBuilder.toString());
                return "";
            }
        });
        logger.addHandler(consoleHandler);
    }

    public static MessageProxy createInstance(MessageProxyOptions ops) {
        if (ops == null) {
            ops = new MessageProxyOptions();
        }
        Const.getAddrUrl = ops.getAddrUrl();
        Const.keepAlive = ops.getKeepAlive();
        Const.timeOut = ops.getConnectionTimeout();
        Const.maxMemorySize = ops.getMsgIdCacheSize();
        Const.binaryMode = ops.isBinaryMode();
        Const.devMode = ops.isDevMode();
        Const.broker = ops.getBroker();
        Const.userName = ops.getUserName();
        Const.password = ops.getPassword();
        Const.clientId = ops.getClientId();
        Const.upTopic = ops.getUpTopic();
        Const.downTopic = ops.getDownTopic();
        return new MqttMessageProxy();
    }

    /* access modifiers changed from: package-private */
    public String getVersion() {
        return "v1.0";
    }
}
