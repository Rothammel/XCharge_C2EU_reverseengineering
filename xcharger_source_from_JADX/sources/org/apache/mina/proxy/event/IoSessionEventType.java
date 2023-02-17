package org.apache.mina.proxy.event;

public enum IoSessionEventType {
    CREATED(1),
    OPENED(2),
    IDLE(3),
    CLOSED(4);
    

    /* renamed from: id */
    private final int f194id;

    private IoSessionEventType(int id) {
        this.f194id = id;
    }

    public int getId() {
        return this.f194id;
    }

    public String toString() {
        switch (this) {
            case CREATED:
                return "- CREATED event -";
            case OPENED:
                return "- OPENED event -";
            case IDLE:
                return "- IDLE event -";
            case CLOSED:
                return "- CLOSED event -";
            default:
                return "- Event Id=" + this.f194id + " -";
        }
    }
}
