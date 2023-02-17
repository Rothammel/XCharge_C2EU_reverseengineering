package org.eclipse.paho.client.mqttv3;

import java.io.UnsupportedEncodingException;
import org.apache.commons.lang3.CharEncoding;
import org.eclipse.paho.client.mqttv3.internal.ClientComms;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish;
import org.eclipse.paho.client.mqttv3.util.Strings;

/* loaded from: classes.dex */
public class MqttTopic {
    private static final int MAX_TOPIC_LEN = 65535;
    private static final int MIN_TOPIC_LEN = 1;
    public static final String MULTI_LEVEL_WILDCARD = "#";
    public static final String MULTI_LEVEL_WILDCARD_PATTERN = "/#";
    private static final char NUL = 0;
    public static final String SINGLE_LEVEL_WILDCARD = "+";
    public static final String TOPIC_LEVEL_SEPARATOR = "/";
    public static final String TOPIC_WILDCARDS = "#+";
    private ClientComms comms;
    private String name;

    public MqttTopic(String name, ClientComms comms) {
        this.comms = comms;
        this.name = name;
    }

    public MqttDeliveryToken publish(byte[] payload, int qos, boolean retained) throws MqttException, MqttPersistenceException {
        MqttMessage message = new MqttMessage(payload);
        message.setQos(qos);
        message.setRetained(retained);
        return publish(message);
    }

    public MqttDeliveryToken publish(MqttMessage message) throws MqttException, MqttPersistenceException {
        MqttDeliveryToken token = new MqttDeliveryToken(this.comms.getClient().getClientId());
        token.setMessage(message);
        this.comms.sendNoWait(createPublish(message), token);
        token.internalTok.waitUntilSent();
        return token;
    }

    public String getName() {
        return this.name;
    }

    private MqttPublish createPublish(MqttMessage message) {
        return new MqttPublish(getName(), message);
    }

    public String toString() {
        return getName();
    }

    public static void validate(String topicString, boolean wildcardAllowed) throws IllegalArgumentException {
        try {
            int topicLen = topicString.getBytes(CharEncoding.UTF_8).length;
            if (topicLen < 1 || topicLen > 65535) {
                throw new IllegalArgumentException(String.format("Invalid topic length, should be in range[%d, %d]!", new Integer(1), new Integer(65535)));
            }
            if (!wildcardAllowed) {
                if (Strings.containsAny(topicString, TOPIC_WILDCARDS)) {
                    throw new IllegalArgumentException("The topic name MUST NOT contain any wildcard characters (#+)");
                }
            } else if (!Strings.equalsAny(topicString, new String[]{MULTI_LEVEL_WILDCARD, "+"})) {
                if (Strings.countMatches(topicString, MULTI_LEVEL_WILDCARD) > 1 || (topicString.contains(MULTI_LEVEL_WILDCARD) && !topicString.endsWith(MULTI_LEVEL_WILDCARD_PATTERN))) {
                    throw new IllegalArgumentException("Invalid usage of multi-level wildcard in topic string: " + topicString);
                }
                validateSingleLevelWildcard(topicString);
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    private static void validateSingleLevelWildcard(String topicString) {
        char singleLevelWildcardChar = "+".charAt(0);
        char topicLevelSeparatorChar = TOPIC_LEVEL_SEPARATOR.charAt(0);
        char[] chars = topicString.toCharArray();
        int length = chars.length;
        for (int i = 0; i < length; i++) {
            char prev = i + (-1) >= 0 ? chars[i - 1] : (char) 0;
            char next = i + 1 < length ? chars[i + 1] : (char) 0;
            if (chars[i] == singleLevelWildcardChar && ((prev != topicLevelSeparatorChar && prev != 0) || (next != topicLevelSeparatorChar && next != 0))) {
                throw new IllegalArgumentException(String.format("Invalid usage of single-level wildcard in topic string '%s'!", topicString));
            }
        }
    }

    public static boolean isMatched(String topicFilter, String topicName) throws IllegalArgumentException {
        int curn = 0;
        int curf = 0;
        int curn_end = topicName.length();
        int curf_end = topicFilter.length();
        validate(topicFilter, true);
        validate(topicName, false);
        if (topicFilter.equals(topicName)) {
            return true;
        }
        while (curf < curf_end && curn < curn_end && ((topicName.charAt(curn) != '/' || topicFilter.charAt(curf) == '/') && (topicFilter.charAt(curf) == '+' || topicFilter.charAt(curf) == '#' || topicFilter.charAt(curf) == topicName.charAt(curn)))) {
            if (topicFilter.charAt(curf) == '+') {
                int nextpos = curn + 1;
                while (nextpos < curn_end && topicName.charAt(nextpos) != '/') {
                    curn++;
                    nextpos = curn + 1;
                }
            } else if (topicFilter.charAt(curf) == '#') {
                curn = curn_end - 1;
            }
            curf++;
            curn++;
        }
        return curn == curn_end && curf == curf_end;
    }
}