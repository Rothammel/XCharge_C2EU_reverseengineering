package org.eclipse.paho.client.mqttv3;

import java.util.Timer;
import java.util.TimerTask;
import org.eclipse.paho.client.mqttv3.internal.ClientComms;
import org.eclipse.paho.client.mqttv3.logging.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;

public class TimerPingSender implements MqttPingSender {
    /* access modifiers changed from: private */
    public static final String CLASS_NAME = TimerPingSender.class.getName();
    /* access modifiers changed from: private */
    public static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
    /* access modifiers changed from: private */
    public ClientComms comms;
    private Timer timer;

    public void init(ClientComms comms2) {
        if (comms2 == null) {
            throw new IllegalArgumentException("ClientComms cannot be null.");
        }
        this.comms = comms2;
    }

    public void start() {
        String clientid = this.comms.getClient().getClientId();
        log.fine(CLASS_NAME, "start", "659", new Object[]{clientid});
        this.timer = new Timer("MQTT Ping: " + clientid);
        this.timer.schedule(new PingTask(this, (PingTask) null), this.comms.getKeepAlive());
    }

    public void stop() {
        log.fine(CLASS_NAME, "stop", "661", (Object[]) null);
        if (this.timer != null) {
            this.timer.cancel();
        }
    }

    public void schedule(long delayInMilliseconds) {
        this.timer.schedule(new PingTask(this, (PingTask) null), delayInMilliseconds);
    }

    private class PingTask extends TimerTask {
        private static final String methodName = "PingTask.run";

        private PingTask() {
        }

        /* synthetic */ PingTask(TimerPingSender timerPingSender, PingTask pingTask) {
            this();
        }

        public void run() {
            TimerPingSender.log.fine(TimerPingSender.CLASS_NAME, methodName, "660", new Object[]{new Long(System.currentTimeMillis())});
            TimerPingSender.this.comms.checkForActivity();
        }
    }
}
