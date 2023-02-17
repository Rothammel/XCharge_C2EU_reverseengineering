package org.apache.mina.filter.firewall;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* loaded from: classes.dex */
public class Subnet {
    private static final int BYTE_MASK = 255;
    private static final int IP_MASK_V4 = Integer.MIN_VALUE;
    private static final long IP_MASK_V6 = Long.MIN_VALUE;
    private InetAddress subnet;
    private int subnetInt;
    private long subnetLong;
    private long subnetMask;
    private int suffix;

    public Subnet(InetAddress subnet, int mask) {
        if (subnet == null) {
            throw new IllegalArgumentException("Subnet address can not be null");
        }
        if (!(subnet instanceof Inet4Address) && !(subnet instanceof Inet6Address)) {
            throw new IllegalArgumentException("Only IPv4 and IPV6 supported");
        }
        if (subnet instanceof Inet4Address) {
            if (mask < 0 || mask > 32) {
                throw new IllegalArgumentException("Mask has to be an integer between 0 and 32 for an IPV4 address");
            }
            this.subnet = subnet;
            this.subnetInt = toInt(subnet);
            this.suffix = mask;
            this.subnetMask = Integer.MIN_VALUE >> (mask - 1);
        } else if (mask < 0 || mask > 128) {
            throw new IllegalArgumentException("Mask has to be an integer between 0 and 128 for an IPV6 address");
        } else {
            this.subnet = subnet;
            this.subnetLong = toLong(subnet);
            this.suffix = mask;
            this.subnetMask = IP_MASK_V6 >> (mask - 1);
        }
    }

    private int toInt(InetAddress inetAddress) {
        byte[] address = inetAddress.getAddress();
        int result = 0;
        for (byte b : address) {
            result = (result << 8) | (b & 255);
        }
        return result;
    }

    private long toLong(InetAddress inetAddress) {
        byte[] address = inetAddress.getAddress();
        long result = 0;
        for (byte b : address) {
            result = (result << 8) | (b & 255);
        }
        return result;
    }

    private long toSubnet(InetAddress address) {
        return address instanceof Inet4Address ? toInt(address) & ((int) this.subnetMask) : toLong(address) & this.subnetMask;
    }

    public boolean inSubnet(InetAddress address) {
        if (address.isAnyLocalAddress()) {
            return true;
        }
        return address instanceof Inet4Address ? ((int) toSubnet(address)) == this.subnetInt : toSubnet(address) == this.subnetLong;
    }

    public String toString() {
        return this.subnet.getHostAddress() + MqttTopic.TOPIC_LEVEL_SEPARATOR + this.suffix;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Subnet) {
            Subnet other = (Subnet) obj;
            return other.subnetInt == this.subnetInt && other.suffix == this.suffix;
        }
        return false;
    }
}