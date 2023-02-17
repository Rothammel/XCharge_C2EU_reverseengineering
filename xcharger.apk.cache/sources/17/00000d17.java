package org.apache.mina.core.filterchain;

import java.util.List;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

/* loaded from: classes.dex */
public interface IoFilterChain {

    /* loaded from: classes.dex */
    public interface Entry {
        void addAfter(String str, IoFilter ioFilter);

        void addBefore(String str, IoFilter ioFilter);

        IoFilter getFilter();

        String getName();

        IoFilter.NextFilter getNextFilter();

        void remove();

        void replace(IoFilter ioFilter);
    }

    void addAfter(String str, String str2, IoFilter ioFilter);

    void addBefore(String str, String str2, IoFilter ioFilter);

    void addFirst(String str, IoFilter ioFilter);

    void addLast(String str, IoFilter ioFilter);

    void clear() throws Exception;

    boolean contains(Class<? extends IoFilter> cls);

    boolean contains(String str);

    boolean contains(IoFilter ioFilter);

    void fireExceptionCaught(Throwable th);

    void fireFilterClose();

    void fireFilterWrite(WriteRequest writeRequest);

    void fireInputClosed();

    void fireMessageReceived(Object obj);

    void fireMessageSent(WriteRequest writeRequest);

    void fireSessionClosed();

    void fireSessionCreated();

    void fireSessionIdle(IdleStatus idleStatus);

    void fireSessionOpened();

    IoFilter get(Class<? extends IoFilter> cls);

    IoFilter get(String str);

    List<Entry> getAll();

    List<Entry> getAllReversed();

    Entry getEntry(Class<? extends IoFilter> cls);

    Entry getEntry(String str);

    Entry getEntry(IoFilter ioFilter);

    IoFilter.NextFilter getNextFilter(Class<? extends IoFilter> cls);

    IoFilter.NextFilter getNextFilter(String str);

    IoFilter.NextFilter getNextFilter(IoFilter ioFilter);

    IoSession getSession();

    IoFilter remove(Class<? extends IoFilter> cls);

    IoFilter remove(String str);

    void remove(IoFilter ioFilter);

    IoFilter replace(Class<? extends IoFilter> cls, IoFilter ioFilter);

    IoFilter replace(String str, IoFilter ioFilter);

    void replace(IoFilter ioFilter, IoFilter ioFilter2);
}