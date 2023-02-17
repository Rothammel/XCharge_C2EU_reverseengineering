package org.apache.mina.handler.chain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.ClassUtils;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.chain.IoHandlerCommand;

/* loaded from: classes.dex */
public class IoHandlerChain implements IoHandlerCommand {
    private static volatile int nextId = 0;
    private final String NEXT_COMMAND;
    private final Entry head;
    private final int id;
    private final Map<String, Entry> name2entry;
    private final Entry tail;

    public IoHandlerChain() {
        int i = nextId;
        nextId = i + 1;
        this.id = i;
        this.NEXT_COMMAND = IoHandlerChain.class.getName() + ClassUtils.PACKAGE_SEPARATOR_CHAR + this.id + ".nextCommand";
        this.name2entry = new ConcurrentHashMap();
        this.head = new Entry(null, null, "head", createHeadCommand());
        this.tail = new Entry(this.head, null, "tail", createTailCommand());
        this.head.nextEntry = this.tail;
    }

    private IoHandlerCommand createHeadCommand() {
        return new IoHandlerCommand() { // from class: org.apache.mina.handler.chain.IoHandlerChain.1
            @Override // org.apache.mina.handler.chain.IoHandlerCommand
            public void execute(IoHandlerCommand.NextCommand next, IoSession session, Object message) throws Exception {
                next.execute(session, message);
            }
        };
    }

    private IoHandlerCommand createTailCommand() {
        return new IoHandlerCommand() { // from class: org.apache.mina.handler.chain.IoHandlerChain.2
            @Override // org.apache.mina.handler.chain.IoHandlerCommand
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
        Iterator<String> it2 = new ArrayList(this.name2entry.keySet()).iterator();
        while (it2.hasNext()) {
            remove(it2.next());
        }
    }

    private void register(Entry prevEntry, String name, IoHandlerCommand command) {
        Entry newEntry = new Entry(prevEntry, prevEntry.nextEntry, name, command);
        prevEntry.nextEntry.prevEntry = newEntry;
        prevEntry.nextEntry = newEntry;
        this.name2entry.put(name, newEntry);
    }

    private void deregister(Entry entry) {
        Entry prevEntry = entry.prevEntry;
        Entry nextEntry = entry.nextEntry;
        prevEntry.nextEntry = nextEntry;
        nextEntry.prevEntry = prevEntry;
        this.name2entry.remove(entry.name);
    }

    private Entry checkOldName(String baseName) {
        Entry e = this.name2entry.get(baseName);
        if (e == null) {
            throw new IllegalArgumentException("Unknown filter name:" + baseName);
        }
        return e;
    }

    private void checkAddable(String name) {
        if (this.name2entry.containsKey(name)) {
            throw new IllegalArgumentException("Other filter is using the same name '" + name + "'");
        }
    }

    @Override // org.apache.mina.handler.chain.IoHandlerCommand
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

    /* JADX INFO: Access modifiers changed from: private */
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

    /* loaded from: classes.dex */
    public class Entry {
        private final IoHandlerCommand command;
        private final String name;
        private final IoHandlerCommand.NextCommand nextCommand;
        private Entry nextEntry;
        private Entry prevEntry;

        private Entry(Entry prevEntry, Entry nextEntry, String name, IoHandlerCommand command) {
            if (command == null) {
                throw new IllegalArgumentException("command");
            }
            if (name == null) {
                throw new IllegalArgumentException("name");
            }
            this.prevEntry = prevEntry;
            this.nextEntry = nextEntry;
            this.name = name;
            this.command = command;
            this.nextCommand = new IoHandlerCommand.NextCommand() { // from class: org.apache.mina.handler.chain.IoHandlerChain.Entry.1
                @Override // org.apache.mina.handler.chain.IoHandlerCommand.NextCommand
                public void execute(IoSession session, Object message) throws Exception {
                    Entry nextEntry2 = Entry.this.nextEntry;
                    IoHandlerChain.this.callNextCommand(nextEntry2, session, message);
                }
            };
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