package com.alibaba.sdk.android.oss.model;

/* loaded from: classes.dex */
public class Range {
    public static final long INFINITE = -1;
    private long begin;
    private long end;

    public Range(long begin, long end) {
        setBegin(begin);
        setEnd(end);
    }

    public long getEnd() {
        return this.end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getBegin() {
        return this.begin;
    }

    public void setBegin(long begin) {
        this.begin = begin;
    }

    public boolean checkIsValid() {
        if (this.begin < -1 || this.end < -1) {
            return false;
        }
        return this.begin < 0 || this.end < 0 || this.begin <= this.end;
    }

    public String toString() {
        return "bytes=" + (this.begin == -1 ? "" : String.valueOf(this.begin)) + "-" + (this.end == -1 ? "" : String.valueOf(this.end));
    }
}
