package org.apache.mina.handler.chain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.ClassUtils;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.chain.IoHandlerCommand;

public class IoHandlerChain implements IoHandlerCommand {
    private static volatile int nextId = 0;
    /* access modifiers changed from: private */
    public final String NEXT_COMMAND = (IoHandlerChain.class.getName() + ClassUtils.PACKAGE_SEPARATOR_CHAR + this.f193id + ".nextCommand");
    private final Entry head = new Entry((Entry) null, (Entry) null, "head", createHeadCommand());

    /* renamed from: id */
    private final int f193id;
    private final Map<String, Entry> name2entry = new ConcurrentHashMap();
    private final Entry tail = new Entry(this.head, (Entry) null, "tail", createTailCommand());

    public IoHandlerChain() {
        int i = nextId;
        nextId = i + 1;
        this.f193id = i;
        Entry unused = this.head.nextEntry = this.tail;
    }

    private IoHandlerCommand createHeadCommand() {
        return new IoHandlerCommand() {
            public void execute(IoHandlerCommand.NextCommand next, IoSession session, Object message) throws Exception {
                next.execute(session, message);
            }
        };
    }

    private IoHandlerCommand createTailCommand() {
        return new IoHandlerCommand() {
            public void execute(IoHandlerCommand.NextCommand next, IoSession session, Object message) throws Exception {
                IoHandlerCommand.NextCommand next2 = (IoHandlerCommand.NextCommand) session.getAttribute(IoHandlerChain.this.NEXT_COMMAND);
                if (next2 != null) {
                    next2.execute(session, message);
                }
            }
        };
    }

    public Entry getEntry(String name) {
        Entry e = this.name2entry.get(name);
        if (e == null) {
            return null;
        }
        return e;
    }

    public IoHandlerCommand get(String name) {
        Entry e = getEntry(name);
        if (e == null) {
            return null;
        }
        return e.getCommand();
    }

    public IoHandlerCommand.NextCommand getNextCommand(String name) {
        Entry e = getEntry(name);
        if (e == null) {
            return null;
        }
        return e.getNextCommand();
    }

    public synchronized void addFirst(String name, IoHandlerCommand command) {
        checkAddable(name);
        register(this.head, name, command);
    }

    public synchronized void addLast(String name, IoHandlerCommand command) {
        checkAddable(name);
        register(this.tail.prevEntry, name, command);
    }

    public synchronized void addBefore(String baseName, String name, IoHandlerCommand command) {
        Entry baseEntry = checkOldName(baseName);
        checkAddable(name);
        register(baseEntry.prevEntry, name, command);
    }

    public synchronized void addAfter(String baseName, String name, IoHandlerCommand command) {
        Entry baseEntry = checkOldName(baseName);
        checkAddable(name);
        register(baseEntry, name, command);
    }

    public synchronized IoHandlerCommand remove(String name) {
        Entry entry;
        entry = checkOldName(name);
        deregister(entry);
        return entry.getCommand();
    }

    public synchronized void clear() throws Exception {
        Iterator<String> it = new ArrayList(this.name2entry.keySet()).iterator();
        while (it.hasNext()) {
            remove(it.next());
        }
    }

    private void register(Entry prevEntry, String name, IoHandlerCommand command) {
        Entry newEntry = new Entry(prevEntry, prevEntry.nextEntry, name, command);
        Entry unused = prevEntry.nextEntry.prevEntry = newEntry;
        Entry unused2 = prevEntry.nextEntry = newEntry;
        this.name2entry.put(name, newEntry);
    }

    private void deregister(Entry entry) {
        Entry prevEntry = entry.prevEntry;
        Entry nextEntry = entry.nextEntry;
        Entry unused = prevEntry.nextEntry = nextEntry;
        Entry unused2 = nextEntry.prevEntry = prevEntry;
        this.name2entry.remove(entry.name);
    }

    private Entry checkOldName(String baseName) {
        Entry e = this.name2entry.get(baseName);
        if (e != null) {
            return e;
        }
        throw new IllegalArgumentException("Unknown filter name:" + baseName);
    }

    private void checkAddable(String name) {
        if (this.name2entry.containsKey(name)) {
            throw new IllegalArgumentException("Other filter is using the same name '" + name + "'");
        }
    }

    public void execute(IoHandlerCommand.NextCommand next, IoSession session, Object message) throws Exception {
        if (next != null) {
            session.setAttribute(this.NEXT_COMMAND, next);
        }
        try {
            callNextCommand(this.head, session, message);
        } finally {
            session.removeAttribute(this.NEXT_COMMAND);
        }
    }

    /* access modifiers changed from: private */
    public void callNextCommand(Entry entry, IoSession session, Object message) throws Exception {
        entry.getCommand().execute(entry.getNextCommand(), session, message);
    }

    public List<Entry> getAll() {
        List<Entry> list = new ArrayList<>();
        for (Entry e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
            list.add(e);
        }
        return list;
    }

    public List<Entry> getAllReversed() {
        List<Entry> list = new ArrayList<>();
        for (Entry e = this.tail.prevEntry; e != this.head; e = e.prevEntry) {
            list.add(e);
        }
        return list;
    }

    public boolean contains(String name) {
        return getEntry(name) != null;
    }

    public boolean contains(IoHandlerCommand command) {
        for (Entry e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
            if (e.getCommand() == command) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(Class<? extends IoHandlerCommand> commandType) {
        for (Entry e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
            if (commandType.isAssignableFrom(e.getCommand().getClass())) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{ ");
        boolean empty = true;
        for (Entry e = this.head.nextEntry; e != this.tail; e = e.nextEntry) {
            if (!empty) {
                buf.append(", ");
            } else {
                empty = false;
            }
            buf.append('(');
            buf.append(e.getName());
            buf.append(':');
            buf.append(e.getCommand());
            buf.append(')');
        }
        if (empty) {
            buf.append("empty");
        }
        buf.append(" }");
        return buf.toString();
    }

    public class Entry {
        private final IoHandlerCommand command;
        /* access modifiers changed from: private */
        public final String name;
        private final IoHandlerCommand.NextCommand nextCommand;
        /* access modifiers changed from: private */
        public Entry nextEntry;
        /* access modifiers changed from: private */
        public Entry prevEntry;

        private Entry(Entry prevEntry2, Entry nextEntry2, String name2, IoHandlerCommand command2) {
            if (command2 == null) {
                throw new IllegalArgumentException("command");
            } else if (name2 == null) {
                throw new IllegalArgumentException("name");
            } else {
                this.prevEntry = prevEntry2;
                this.nextEntry = nextEntry2;
                this.name = name2;
                this.command = command2;
                this.nextCommand = new IoHandlerCommand.NextCommand(IoHandlerChain.this) {
                    public void execute(IoSession session, Object message) throws Exception {
                        IoHandlerChain.this.callNextCommand(Entry.this.nextEntry, session, message);
                    }
                };
            }
        }

        public String getName() {
            return this.name;
        }

        public IoHandlerCommand getCommand() {
            return this.command;
        }

        public IoHandlerCommand.NextCommand getNextCommand() {
            return this.nextCommand;
        }
    }
}
