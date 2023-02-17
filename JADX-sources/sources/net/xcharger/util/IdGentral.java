package net.xcharger.util;

/* loaded from: classes.dex */
public class IdGentral {
    private static IdGentral instance = null;
    private long datacenterId;
    private long datacenterIdBits;
    private long datacenterIdShift;
    private long lastTimestamp;
    private long maxDatacenterId;
    private long maxWorkerId;
    private long sequence;
    private long sequenceBits;
    private long sequenceMask;
    private long timestampLeftShift;
    private long twepoch;
    private long workerId;
    private long workerIdBits;
    private long workerIdShift;

    public static IdGentral get() {
        if (instance == null) {
            IdGentral idGen = new IdGentral();
            idGen.loadConfig();
            instance = idGen;
        }
        return instance;
    }

    public IdGentral() {
        this.workerId = 0L;
        this.datacenterId = 0L;
        this.sequence = 0L;
        this.twepoch = 1288834974657L;
        this.workerIdBits = 5L;
        this.datacenterIdBits = 5L;
        this.maxWorkerId = ((-1) << ((int) this.workerIdBits)) ^ (-1);
        this.maxDatacenterId = ((-1) << ((int) this.datacenterIdBits)) ^ (-1);
        this.sequenceBits = 12L;
        this.workerIdShift = this.sequenceBits;
        this.datacenterIdShift = this.sequenceBits + this.workerIdBits;
        this.timestampLeftShift = this.sequenceBits + this.workerIdBits + this.datacenterIdBits;
        this.sequenceMask = ((-1) << ((int) this.sequenceBits)) ^ (-1);
        this.lastTimestamp = -1L;
    }

    public IdGentral(long workerId, long datacenterId) {
        this.workerId = 0L;
        this.datacenterId = 0L;
        this.sequence = 0L;
        this.twepoch = 1288834974657L;
        this.workerIdBits = 5L;
        this.datacenterIdBits = 5L;
        this.maxWorkerId = ((-1) << ((int) this.workerIdBits)) ^ (-1);
        this.maxDatacenterId = ((-1) << ((int) this.datacenterIdBits)) ^ (-1);
        this.sequenceBits = 12L;
        this.workerIdShift = this.sequenceBits;
        this.datacenterIdShift = this.sequenceBits + this.workerIdBits;
        this.timestampLeftShift = this.sequenceBits + this.workerIdBits + this.datacenterIdBits;
        this.sequenceMask = ((-1) << ((int) this.sequenceBits)) ^ (-1);
        this.lastTimestamp = -1L;
        if (workerId > this.maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", Long.valueOf(this.maxWorkerId)));
        }
        if (datacenterId > this.maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", Long.valueOf(this.maxDatacenterId)));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public void loadConfig() {
        long worker_id = (long) ((Math.random() * 31.0d) + 1.0d);
        long datacenter_id = (long) ((Math.random() * 31.0d) + 1.0d);
        if (worker_id > this.maxWorkerId || worker_id < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", Long.valueOf(this.maxWorkerId)));
        }
        if (datacenter_id > this.maxDatacenterId || datacenter_id < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", Long.valueOf(this.maxDatacenterId)));
        }
        this.workerId = worker_id;
        this.datacenterId = datacenter_id;
    }

    public synchronized long nextId() {
        long timestamp;
        timestamp = timeGen();
        if (timestamp < this.lastTimestamp) {
        }
        if (this.lastTimestamp == timestamp) {
            this.sequence = (this.sequence + 1) & this.sequenceMask;
            if (this.sequence == 0) {
                timestamp = tilNextMillis(this.lastTimestamp);
            }
        } else {
            this.sequence = 0L;
        }
        this.lastTimestamp = timestamp;
        return ((timestamp - this.twepoch) << ((int) this.timestampLeftShift)) | (this.datacenterId << ((int) this.datacenterIdShift)) | (this.workerId << ((int) this.workerIdShift)) | this.sequence;
    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    protected long timeGen() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            System.out.println(get().nextId());
        }
    }
}
