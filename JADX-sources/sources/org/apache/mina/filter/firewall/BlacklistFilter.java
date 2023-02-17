package org.apache.mina.filter.firewall;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public class BlacklistFilter extends IoFilterAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlacklistFilter.class);
    private final List<Subnet> blacklist = new CopyOnWriteArrayList();

    public void setBlacklist(InetAddress[] addresses) {
        if (addresses == null) {
            throw new IllegalArgumentException("addresses");
        }
        this.blacklist.clear();
        for (InetAddress addr : addresses) {
            block(addr);
        }
    }

    public void setSubnetBlacklist(Subnet[] subnets) {
        if (subnets == null) {
            throw new IllegalArgumentException("Subnets must not be null");
        }
        this.blacklist.clear();
        for (Subnet subnet : subnets) {
            block(subnet);
        }
    }

    public void setBlacklist(Iterable<InetAddress> addresses) {
        if (addresses == null) {
            throw new IllegalArgumentException("addresses");
        }
        this.blacklist.clear();
        for (InetAddress address : addresses) {
            block(address);
        }
    }

    public void setSubnetBlacklist(Iterable<Subnet> subnets) {
        if (subnets == null) {
            throw new IllegalArgumentException("Subnets must not be null");
        }
        this.blacklist.clear();
        for (Subnet subnet : subnets) {
            block(subnet);
        }
    }

    public void block(InetAddress address) {
        if (address == null) {
            throw new IllegalArgumentException("Adress to block can not be null");
        }
        block(new Subnet(address, 32));
    }

    public void block(Subnet subnet) {
        if (subnet == null) {
            throw new IllegalArgumentException("Subnet can not be null");
        }
        this.blacklist.add(subnet);
    }

    public void unblock(InetAddress address) {
        if (address == null) {
            throw new IllegalArgumentException("Adress to unblock can not be null");
        }
        unblock(new Subnet(address, 32));
    }

    public void unblock(Subnet subnet) {
        if (subnet == null) {
            throw new IllegalArgumentException("Subnet can not be null");
        }
        this.blacklist.remove(subnet);
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void sessionCreated(IoFilter.NextFilter nextFilter, IoSession session) {
        if (!isBlocked(session)) {
            nextFilter.sessionCreated(session);
        } else {
            blockSession(session);
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void sessionOpened(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        if (!isBlocked(session)) {
            nextFilter.sessionOpened(session);
        } else {
            blockSession(session);
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void sessionClosed(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        if (!isBlocked(session)) {
            nextFilter.sessionClosed(session);
        } else {
            blockSession(session);
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void sessionIdle(IoFilter.NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
        if (!isBlocked(session)) {
            nextFilter.sessionIdle(session, status);
        } else {
            blockSession(session);
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void messageReceived(IoFilter.NextFilter nextFilter, IoSession session, Object message) {
        if (!isBlocked(session)) {
            nextFilter.messageReceived(session, message);
        } else {
            blockSession(session);
        }
    }

    @Override // org.apache.mina.core.filterchain.IoFilterAdapter, org.apache.mina.core.filterchain.IoFilter
    public void messageSent(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        if (!isBlocked(session)) {
            nextFilter.messageSent(session, writeRequest);
        } else {
            blockSession(session);
        }
    }

    private void blockSession(IoSession session) {
        LOGGER.warn("Remote address in the blacklist; closing.");
        session.closeNow();
    }

    private boolean isBlocked(IoSession session) {
        SocketAddress remoteAddress = session.getRemoteAddress();
        if (remoteAddress instanceof InetSocketAddress) {
            InetAddress address = ((InetSocketAddress) remoteAddress).getAddress();
            for (Subnet subnet : this.blacklist) {
                if (subnet.inSubnet(address)) {
                    return true;
                }
            }
        }
        return false;
    }
}
