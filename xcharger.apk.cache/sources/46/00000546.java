package com.xcharge.charger.core.api;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/* loaded from: classes.dex */
public class Sequence {
    private static ConcurrentHashMap<String, AtomicLong> fromSequences = new ConcurrentHashMap<>();

    private static long next(String from) {
        fromSequences.putIfAbsent(from, new AtomicLong(0L));
        AtomicLong seq = fromSequences.get(from);
        return seq.incrementAndGet();
    }

    public static long getCoreDCAPSequence() {
        return next("core");
    }

    public static long getAgentDCAPSequence() {
        return next("agent");
    }

    public static long getCloudDCAPSequence() {
        return next("cloud");
    }
}