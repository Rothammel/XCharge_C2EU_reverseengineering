package org.eclipse.paho.client.mqttv3.internal.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import org.eclipse.paho.client.mqttv3.logging.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;

/* loaded from: classes.dex */
public class WebSocketReceiver implements Runnable {
    private static final String CLASS_NAME = WebSocketReceiver.class.getName();
    private static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
    private InputStream input;
    private volatile boolean receiving;
    private boolean running = false;
    private boolean stopping = false;
    private Object lifecycle = new Object();
    private Thread receiverThread = null;
    private PipedOutputStream pipedOutputStream = new PipedOutputStream();

    public WebSocketReceiver(InputStream input, PipedInputStream pipedInputStream) throws IOException {
        this.input = input;
        pipedInputStream.connect(this.pipedOutputStream);
    }

    public void start(String threadName) {
        log.fine(CLASS_NAME, "start", "855");
        synchronized (this.lifecycle) {
            if (!this.running) {
                this.running = true;
                this.receiverThread = new Thread(this, threadName);
                this.receiverThread.start();
            }
        }
    }

    public void stop() {
        this.stopping = true;
        boolean closed = false;
        synchronized (this.lifecycle) {
            log.fine(CLASS_NAME, "stop", "850");
            if (this.running) {
                this.running = false;
                this.receiving = false;
                closed = true;
                closeOutputStream();
            }
        }
        if (closed && !Thread.currentThread().equals(this.receiverThread)) {
            try {
                this.receiverThread.join();
            } catch (InterruptedException e) {
            }
        }
        this.receiverThread = null;
        log.fine(CLASS_NAME, "stop", "851");
    }

    @Override // java.lang.Runnable
    public void run() {
        while (this.running && this.input != null) {
            try {
                log.fine(CLASS_NAME, "run", "852");
                this.receiving = this.input.available() > 0;
                WebSocketFrame incomingFrame = new WebSocketFrame(this.input);
                if (!incomingFrame.isCloseFlag()) {
                    for (int i = 0; i < incomingFrame.getPayload().length; i++) {
                        this.pipedOutputStream.write(incomingFrame.getPayload()[i]);
                    }
                    this.pipedOutputStream.flush();
                } else if (!this.stopping) {
                    throw new IOException("Server sent a WebSocket Frame with the Stop OpCode");
                    break;
                }
                this.receiving = false;
            } catch (IOException e) {
                stop();
            }
        }
    }

    private void closeOutputStream() {
        try {
            this.pipedOutputStream.close();
        } catch (IOException e) {
        }
    }

    public boolean isRunning() {
        return this.running;
    }

    public boolean isReceiving() {
        return this.receiving;
    }
}