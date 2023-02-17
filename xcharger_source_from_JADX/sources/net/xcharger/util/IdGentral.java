package net.xcharger.util;

public class IdGentral {
    private static IdGentral instance = null;
    private long datacenterId = 0;
    private long datacenterIdBits = 5;
    private long datacenterIdShift = (this.sequenceBits + this.workerIdBits);
    private long lastTimestamp = -1;
    private long maxDatacenterId = ((-1 << ((int) this.datacenterIdBits)) ^ -1);
    private long maxWorkerId = ((-1 << ((int) this.workerIdBits)) ^ -1);
    private long sequence = 0;
    private long sequenceBits = 12;
    private long sequenceMask = ((-1 << ((int) this.sequenceBits)) ^ -1);
    private long timestampLeftShift = ((this.sequenceBits + this.workerIdBits) + this.datacenterIdBits);
    private long twepoch = 1288834974657L;
    private long workerId = 0;
    private long workerIdBits = 5;
    private long workerIdShift = this.sequenceBits;

    public static IdGentral get() {
        if (instance == null) {
            IdGentral idGen = new IdGentral();
            idGen.loadConfig();
            instance = idGen;
        }
        return instance;
    }

    public IdGentral() {
    }

    public IdGentral(long workerId2, long datacenterId2) {
        if (workerId2 > this.maxWorkerId || workerId2 < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", new Object[]{Long.valueOf(this.maxWorkerId)}));
        } else if (datacenterId2 > this.maxDatacenterId || datacenterId2 < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", new Object[]{Long.valueOf(this.maxDatacenterId)}));
        } else {
            this.workerId = workerId2;
            this.datacenterId = datacenterId2;
        }
    }

    public void loadConfig() {
        long worker_id = (long) ((Math.random() * 31.0d) + 1.0d);
        long datacenter_id = (long) ((Math.random() * 31.0d) + 1.0d);
        if (worker_id > this.maxWorkerId || worker_id < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", new Object[]{Long.valueOf(this.maxWorkerId)}));
        } else if (datacenter_id > this.maxDatacenterId || datacenter_id < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", new Object[]{Long.valueOf(this.maxDatacenterId)}));
        } else {
            this.workerId = worker_id;
            this.datacenterId = datacenter_id;
        }
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
            this.sequence = 0;
        }
        this.lastTimestamp = timestamp;
        return ((timestamp - this.twepoch) << ((int) this.timestampLeftShift)) | (this.datacenterId << ((int) this.datacenterIdShift)) | (this.workerId << ((int) this.workerIdShift)) | this.sequence;
    }

    /* access modifiers changed from: protected */
    public long tilNextMillis(long lastTimestamp2) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp2) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /* access modifiers changed from: protected */
    public long timeGen() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            System.out.println(get().nextId());
        }
    }
}
