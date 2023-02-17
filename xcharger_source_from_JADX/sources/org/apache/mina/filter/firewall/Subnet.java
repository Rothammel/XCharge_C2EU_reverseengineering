package org.apache.mina.filter.firewall;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import org.eclipse.paho.client.mqttv3.MqttTopic;

public class Subnet {
    private static final int BYTE_MASK = 255;
    private static final int IP_MASK_V4 = Integer.MIN_VALUE;
    private static final long IP_MASK_V6 = Long.MIN_VALUE;
    private InetAddress subnet;
    private int subnetInt;
    private long subnetLong;
    private long subnetMask;
    private int suffix;

    public Subnet(InetAddress subnet2, int mask) {
        if (subnet2 == null) {
            throw new IllegalArgumentException("Subnet address can not be null");
        } else if (!(subnet2 instanceof Inet4Address) && !(subnet2 instanceof Inet6Address)) {
            throw new IllegalArgumentException("Only IPv4 and IPV6 supported");
        } else if (subnet2 instanceof Inet4Address) {
            if (mask < 0 || mask > 32) {
                throw new IllegalArgumentException("Mask has to be an integer between 0 and 32 for an IPV4 address");
            }
            this.subnet = subnet2;
            this.subnetInt = toInt(subnet2);
            this.suffix = mask;
            this.subnetMask = (long) (Integer.MIN_VALUE >> (mask - 1));
        } else if (mask < 0 || mask > 128) {
            throw new IllegalArgumentException("Mask has to be an integer between 0 and 128 for an IPV6 address");
        } else {
            this.subnet = subnet2;
            this.subnetLong = toLong(subnet2);
            this.suffix = mask;
            this.subnetMask = IP_MASK_V6 >> (mask - 1);
        }
    }

    private int toInt(InetAddress inetAddress) {
        int result = 0;
        for (byte b : inetAddress.getAddress()) {
            result = (result << 8) | (b & 255);
        }
        return result;
    }

    private long toLong(InetAddress inetAddress) {
        long result = 0;
        for (byte b : inetAddress.getAddress()) {
            result = (result << 8) | ((long) (b & 255));
        }
        return result;
    }

    private long toSubnet(InetAddress address) {
        if (address instanceof Inet4Address) {
            return (long) (toInt(address) & ((int) this.subnetMask));
        }
        return toLong(address) & this.subnetMask;
    }

    public boolean inSubnet(InetAddress address) {
        if (address.isAnyLocalAddress()) {
            return true;
        }
        if (address instanceof Inet4Address) {
            if (((int) toSubnet(address)) != this.subnetInt) {
                return false;
            }
            return true;
        } else if (toSubnet(address) != this.subnetLong) {
            return false;
        } else {
            return true;
        }
    }

    public String toString() {
        return this.subnet.getHostAddress() + MqttTopic.TOPIC_LEVEL_SEPARATOR + this.suffix;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Subnet)) {
            return false;
        }
        Subnet other = (Subnet) obj;
        if (other.subnetInt == this.subnetInt && other.suffix == this.suffix) {
            return true;
        }
        return false;
    }
}
