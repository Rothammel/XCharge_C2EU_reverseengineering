package org.apache.mina.transport.vmpipe;

import java.net.SocketAddress;

public class VmPipeAddress extends SocketAddress implements Comparable<VmPipeAddress> {
    private static final long serialVersionUID = 3257844376976830515L;
    private final int port;

    public VmPipeAddress(int port2) {
        this.port = port2;
    }

    public int getPort() {
        return this.port;
    }

    public int hashCode() {
        return this.port;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VmPipeAddress)) {
            return false;
        }
        if (this.port != ((VmPipeAddress) o).port) {
            return false;
        }
        return true;
    }

    public int compareTo(VmPipeAddress o) {
        return this.port - o.port;
    }

    public String toString() {
        if (this.port >= 0) {
            return "vm:server:" + this.port;
        }
        return "vm:client:" + (-this.port);
    }
}
