package org.apache.mina.filter.executor;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoEvent;
import org.apache.mina.core.write.WriteRequest;

public class DefaultIoEventSizeEstimator implements IoEventSizeEstimator {
    private final ConcurrentMap<Class<?>, Integer> class2size = new ConcurrentHashMap();

    public DefaultIoEventSizeEstimator() {
        this.class2size.put(Boolean.TYPE, 4);
        this.class2size.put(Byte.TYPE, 1);
        this.class2size.put(Character.TYPE, 2);
        this.class2size.put(Integer.TYPE, 4);
        this.class2size.put(Short.TYPE, 2);
        this.class2size.put(Long.TYPE, 8);
        this.class2size.put(Float.TYPE, 4);
        this.class2size.put(Double.TYPE, 8);
        this.class2size.put(Void.TYPE, 0);
    }

    public int estimateSize(IoEvent event) {
        return estimateSize((Object) event) + estimateSize(event.getParameter());
    }

    public int estimateSize(Object message) {
        if (message == null) {
            return 8;
        }
        int answer = estimateSize(message.getClass(), (Set<Class<?>>) null) + 8;
        if (message instanceof IoBuffer) {
            answer += ((IoBuffer) message).remaining();
        } else if (message instanceof WriteRequest) {
            answer += estimateSize(((WriteRequest) message).getMessage());
        } else if (message instanceof CharSequence) {
            answer += ((CharSequence) message).length() << 1;
        } else if (message instanceof Iterable) {
            for (Object m : (Iterable) message) {
                answer += estimateSize(m);
            }
        }
        return align(answer);
    }

    private int estimateSize(Class<?> clazz, Set<Class<?>> visitedClasses) {
        Integer objectSize = (Integer) this.class2size.get(clazz);
        if (objectSize != null) {
            return objectSize.intValue();
        }
        if (visitedClasses == null) {
            visitedClasses = new HashSet<>();
        } else if (visitedClasses.contains(clazz)) {
            return 0;
        }
        visitedClasses.add(clazz);
        int answer = 8;
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if ((f.getModifiers() & 8) == 0) {
                    answer += estimateSize(f.getType(), visitedClasses);
                }
            }
        }
        visitedClasses.remove(clazz);
        int answer2 = align(answer);
        Integer tmpAnswer = this.class2size.putIfAbsent(clazz, Integer.valueOf(answer2));
        if (tmpAnswer != null) {
            return tmpAnswer.intValue();
        }
        return answer2;
    }

    private static int align(int size) {
        if (size % 8 != 0) {
            return ((size / 8) + 1) * 8;
        }
        return size;
    }
}
