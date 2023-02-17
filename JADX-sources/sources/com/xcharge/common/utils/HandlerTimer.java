package com.xcharge.common.utils;

import android.content.Context;
import android.os.Handler;
import java.util.HashMap;
import java.util.Map;

/* loaded from: classes.dex */
public class HandlerTimer {
    private Handler handler;
    private Context context = null;
    private HashMap<String, TimerRunnable> runnables = null;

    /* loaded from: classes.dex */
    public class TimerRunnable implements Runnable {
        private Object data;
        private Handler handler;
        private int what;

        public TimerRunnable(Handler handler, int what, Object data) {
            HandlerTimer.this = r3;
            this.handler = null;
            this.what = 0;
            this.data = null;
            this.handler = handler;
            this.what = what;
            this.data = data;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.handler.sendMessage(this.handler.obtainMessage(this.what, this.data));
        }
    }

    public HandlerTimer(Handler handler) {
        this.handler = null;
        this.handler = handler;
    }

    public void init(Context context) {
        this.context = context;
        this.runnables = new HashMap<>();
    }

    public void destroy() {
        for (Map.Entry<String, TimerRunnable> entry : this.runnables.entrySet()) {
            this.handler.removeCallbacks(entry.getValue());
        }
        this.runnables.clear();
    }

    public boolean startTimer(long timeout, int what, Object data) {
        stopTimer(what);
        if (data == null) {
            return this.handler.sendEmptyMessageDelayed(what, timeout);
        }
        TimerRunnable runnable = new TimerRunnable(this.handler, what, data);
        boolean isOk = this.handler.postDelayed(runnable, timeout);
        if (isOk) {
            this.runnables.put(String.valueOf(what), runnable);
            return isOk;
        }
        return isOk;
    }

    public boolean timeoutAt(long uptime, int what, Object data) {
        stopTimer(what);
        if (data == null) {
            return this.handler.sendEmptyMessageAtTime(what, uptime);
        }
        TimerRunnable runnable = new TimerRunnable(this.handler, what, data);
        boolean isOk = this.handler.postAtTime(runnable, uptime);
        if (isOk) {
            this.runnables.put(String.valueOf(what), runnable);
            return isOk;
        }
        return isOk;
    }

    public void stopTimer(int what) {
        String key = String.valueOf(what);
        if (this.runnables.containsKey(key)) {
            this.handler.removeCallbacks(this.runnables.get(key));
            this.runnables.remove(key);
        }
        this.handler.removeMessages(what);
    }
}
