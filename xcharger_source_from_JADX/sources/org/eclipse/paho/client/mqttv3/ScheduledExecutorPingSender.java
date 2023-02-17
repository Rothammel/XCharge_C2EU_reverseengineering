package org.eclipse.paho.client.mqttv3;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.eclipse.paho.client.mqttv3.internal.ClientComms;
import org.eclipse.paho.client.mqttv3.logging.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;

public class ScheduledExecutorPingSender implements MqttPingSender {
    /* access modifiers changed from: private */
    public static final String CLASS_NAME = ScheduledExecutorPingSender.class.getName();
    /* access modifiers changed from: private */
    public static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
    /* access modifiers changed from: private */
    public String clientid;
    /* access modifiers changed from: private */
    public ClientComms comms;
    private ScheduledExecutorService executorService;
    private ScheduledFuture scheduledFuture;

    public ScheduledExecutorPingSender(ScheduledExecutorService executorService2) {
        if (executorService2 == null) {
            throw new IllegalArgumentException("ExecutorService cannot be null.");
        }
        this.executorService = executorService2;
    }

    public void init(ClientComms comms2) {
        if (comms2 == null) {
            throw new IllegalArgumentException("ClientComms cannot be null.");
        }
        this.comms = comms2;
        this.clientid = comms2.getClient().getClientId();
    }

    public void start() {
        log.fine(CLASS_NAME, "start", "659", new Object[]{this.clientid});
        schedule(this.comms.getKeepAlive());
    }

    public void stop() {
        log.fine(CLASS_NAME, "stop", "661", (Object[]) null);
        if (this.scheduledFuture != null) {
            this.scheduledFuture.cancel(true);
        }
    }

    public void schedule(long delayInMilliseconds) {
        this.scheduledFuture = this.executorService.schedule(new PingRunnable(this, (PingRunnable) null), delayInMilliseconds, TimeUnit.MILLISECONDS);
    }

    private class PingRunnable implements Runnable {
        private static final String methodName = "PingTask.run";

        private PingRunnable() {
        }

        /* synthetic */ PingRunnable(ScheduledExecutorPingSender scheduledExecutorPingSender, PingRunnable pingRunnable) {
            this();
        }

        public void run() {
            String originalThreadName = Thread.currentThread().getName();
            Thread.currentThread().setName("MQTT Ping: " + ScheduledExecutorPingSender.this.clientid);
            ScheduledExecutorPingSender.log.fine(ScheduledExecutorPingSender.CLASS_NAME, methodName, "660", new Object[]{new Long(System.currentTimeMillis())});
            ScheduledExecutorPingSender.this.comms.checkForActivity();
            Thread.currentThread().setName(originalThreadName);
        }
    }
}
