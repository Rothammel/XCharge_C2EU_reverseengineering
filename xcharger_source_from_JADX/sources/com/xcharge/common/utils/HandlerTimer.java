package com.xcharge.common.utils;

import android.content.Context;
import android.os.Handler;
import java.util.HashMap;
import java.util.Map;

public class HandlerTimer {
    private Context context = null;
    private Handler handler = null;
    private HashMap<String, TimerRunnable> runnables = null;

    private class TimerRunnable implements Runnable {
        private Object data = null;
        private Handler handler = null;
        private int what = 0;

        public TimerRunnable(Handler handler2, int what2, Object data2) {
            this.handler = handler2;
            this.what = what2;
            this.data = data2;
        }

        public void run() {
            this.handler.sendMessage(this.handler.obtainMessage(this.what, this.data));
        }
    }

    public HandlerTimer(Handler handler2) {
        this.handler = handler2;
    }

    public void init(Context context2) {
        this.context = context2;
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
        if (!isOk) {
            return isOk;
        }
        this.runnables.put(String.valueOf(what), runnable);
        return isOk;
    }

    public boolean timeoutAt(long uptime, int what, Object data) {
        stopTimer(what);
        if (data == null) {
            return this.handler.sendEmptyMessageAtTime(what, uptime);
        }
        TimerRunnable runnable = new TimerRunnable(this.handler, what, data);
        boolean isOk = this.handler.postAtTime(runnable, uptime);
        if (!isOk) {
            return isOk;
        }
        this.runnables.put(String.valueOf(what), runnable);
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
