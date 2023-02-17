package org.eclipse.paho.client.mqttv3.internal;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttToken;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPubAck;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPubComp;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish;
import org.eclipse.paho.client.mqttv3.logging.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;

public class CommsCallback implements Runnable {
    private static final String CLASS_NAME = CommsCallback.class.getName();
    private static final int INBOUND_QUEUE_SIZE = 10;
    private static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
    private Future callbackFuture;
    private Thread callbackThread;
    private Hashtable callbacks;
    private ClientComms clientComms;
    private ClientState clientState;
    private Vector completeQueue;
    private Object lifecycle = new Object();
    private boolean manualAcks = false;
    private Vector messageQueue;
    private MqttCallback mqttCallback;
    private boolean quiescing = false;
    private MqttCallbackExtended reconnectInternalCallback;
    public boolean running = false;
    private final Semaphore runningSemaphore = new Semaphore(1);
    private Object spaceAvailable = new Object();
    private String threadName;
    private Object workAvailable = new Object();

    CommsCallback(ClientComms clientComms2) {
        this.clientComms = clientComms2;
        this.messageQueue = new Vector(10);
        this.completeQueue = new Vector(10);
        this.callbacks = new Hashtable();
        log.setResourceName(clientComms2.getClient().getClientId());
    }

    public void setClientState(ClientState clientState2) {
        this.clientState = clientState2;
    }

    public void start(String threadName2, ExecutorService executorService) {
        this.threadName = threadName2;
        synchronized (this.lifecycle) {
            if (!this.running) {
                this.messageQueue.clear();
                this.completeQueue.clear();
                this.running = true;
                this.quiescing = false;
                this.callbackFuture = executorService.submit(this);
            }
        }
    }

    /*  JADX ERROR: IndexOutOfBoundsException in pass: RegionMakerVisitor
        java.lang.IndexOutOfBoundsException: Index 0 out of bounds for length 0
        	at java.base/jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:64)
        	at java.base/jdk.internal.util.Preconditions.outOfBoundsCheckIndex(Preconditions.java:70)
        	at java.base/jdk.internal.util.Preconditions.checkIndex(Preconditions.java:248)
        	at java.base/java.util.Objects.checkIndex(Objects.java:372)
        	at java.base/java.util.ArrayList.get(ArrayList.java:458)
        	at jadx.core.dex.nodes.InsnNode.getArg(InsnNode.java:101)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:611)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:561)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:693)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:123)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:693)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:123)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:598)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:49)
        */
    public void stop() {
        /*
            r6 = this;
            java.lang.Object r1 = r6.lifecycle
            monitor-enter(r1)
            java.util.concurrent.Future r0 = r6.callbackFuture     // Catch:{ all -> 0x0063 }
            if (r0 == 0) goto L_0x000d
            java.util.concurrent.Future r0 = r6.callbackFuture     // Catch:{ all -> 0x0063 }
            r2 = 1
            r0.cancel(r2)     // Catch:{ all -> 0x0063 }
        L_0x000d:
            boolean r0 = r6.running     // Catch:{ all -> 0x0063 }
            if (r0 == 0) goto L_0x0049
            org.eclipse.paho.client.mqttv3.logging.Logger r0 = log     // Catch:{ all -> 0x0063 }
            java.lang.String r2 = CLASS_NAME     // Catch:{ all -> 0x0063 }
            java.lang.String r3 = "stop"
            java.lang.String r4 = "700"
            r0.fine(r2, r3, r4)     // Catch:{ all -> 0x0063 }
            r0 = 0
            r6.running = r0     // Catch:{ all -> 0x0063 }
            java.lang.Thread r0 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x0063 }
            java.lang.Thread r2 = r6.callbackThread     // Catch:{ all -> 0x0063 }
            boolean r0 = r0.equals(r2)     // Catch:{ all -> 0x0063 }
            if (r0 != 0) goto L_0x0049
            java.lang.Object r2 = r6.workAvailable     // Catch:{ InterruptedException -> 0x005c, all -> 0x0066 }
            monitor-enter(r2)     // Catch:{ InterruptedException -> 0x005c, all -> 0x0066 }
            org.eclipse.paho.client.mqttv3.logging.Logger r0 = log     // Catch:{ all -> 0x0059 }
            java.lang.String r3 = CLASS_NAME     // Catch:{ all -> 0x0059 }
            java.lang.String r4 = "stop"
            java.lang.String r5 = "701"
            r0.fine(r3, r4, r5)     // Catch:{ all -> 0x0059 }
            java.lang.Object r0 = r6.workAvailable     // Catch:{ all -> 0x0059 }
            r0.notifyAll()     // Catch:{ all -> 0x0059 }
            monitor-exit(r2)     // Catch:{ all -> 0x0059 }
            java.util.concurrent.Semaphore r0 = r6.runningSemaphore     // Catch:{ InterruptedException -> 0x005c, all -> 0x0066 }
            r0.acquire()     // Catch:{ InterruptedException -> 0x005c, all -> 0x0066 }
            java.util.concurrent.Semaphore r0 = r6.runningSemaphore     // Catch:{ all -> 0x0063 }
            r0.release()     // Catch:{ all -> 0x0063 }
        L_0x0049:
            r0 = 0
            r6.callbackThread = r0     // Catch:{ all -> 0x0063 }
            org.eclipse.paho.client.mqttv3.logging.Logger r0 = log     // Catch:{ all -> 0x0063 }
            java.lang.String r2 = CLASS_NAME     // Catch:{ all -> 0x0063 }
            java.lang.String r3 = "stop"
            java.lang.String r4 = "703"
            r0.fine(r2, r3, r4)     // Catch:{ all -> 0x0063 }
            monitor-exit(r1)     // Catch:{ all -> 0x0063 }
            return
        L_0x0059:
            r0 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x0059 }
            throw r0     // Catch:{ InterruptedException -> 0x005c, all -> 0x0066 }
        L_0x005c:
            r0 = move-exception
            java.util.concurrent.Semaphore r0 = r6.runningSemaphore     // Catch:{ all -> 0x0063 }
            r0.release()     // Catch:{ all -> 0x0063 }
            goto L_0x0049
        L_0x0063:
            r0 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x0063 }
            throw r0
        L_0x0066:
            r0 = move-exception
            java.util.concurrent.Semaphore r2 = r6.runningSemaphore     // Catch:{ all -> 0x0063 }
            r2.release()     // Catch:{ all -> 0x0063 }
            throw r0     // Catch:{ all -> 0x0063 }
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.paho.client.mqttv3.internal.CommsCallback.stop():void");
    }

    public void setCallback(MqttCallback mqttCallback2) {
        this.mqttCallback = mqttCallback2;
    }

    public void setReconnectCallback(MqttCallbackExtended callback) {
        this.reconnectInternalCallback = callback;
    }

    public void setManualAcks(boolean manualAcks2) {
        this.manualAcks = manualAcks2;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v29, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v32, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: org.eclipse.paho.client.mqttv3.MqttToken} */
    /*  JADX ERROR: IndexOutOfBoundsException in pass: RegionMakerVisitor
        java.lang.IndexOutOfBoundsException: Index 0 out of bounds for length 0
        	at java.base/jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:64)
        	at java.base/jdk.internal.util.Preconditions.outOfBoundsCheckIndex(Preconditions.java:70)
        	at java.base/jdk.internal.util.Preconditions.checkIndex(Preconditions.java:248)
        	at java.base/java.util.Objects.checkIndex(Objects.java:372)
        	at java.base/java.util.ArrayList.get(ArrayList.java:458)
        	at jadx.core.dex.nodes.InsnNode.getArg(InsnNode.java:101)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:611)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:561)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:693)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:123)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processLoop(RegionMaker.java:225)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:106)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:49)
        */
    /* JADX WARNING: Multi-variable type inference failed */
    public void run() {
        /*
            r10 = this;
            r3 = 0
            java.lang.Thread r1 = java.lang.Thread.currentThread()
            r10.callbackThread = r1
            java.lang.Thread r1 = r10.callbackThread
            java.lang.String r2 = r10.threadName
            r1.setName(r2)
            java.util.concurrent.Semaphore r1 = r10.runningSemaphore     // Catch:{ InterruptedException -> 0x0018 }
            r1.acquire()     // Catch:{ InterruptedException -> 0x0018 }
        L_0x0013:
            boolean r1 = r10.running
            if (r1 != 0) goto L_0x001c
        L_0x0017:
            return
        L_0x0018:
            r1 = move-exception
            r10.running = r3
            goto L_0x0017
        L_0x001c:
            java.lang.Object r2 = r10.workAvailable     // Catch:{ InterruptedException -> 0x00b8 }
            monitor-enter(r2)     // Catch:{ InterruptedException -> 0x00b8 }
            boolean r1 = r10.running     // Catch:{ all -> 0x00b5 }
            if (r1 == 0) goto L_0x0043
            java.util.Vector r1 = r10.messageQueue     // Catch:{ all -> 0x00b5 }
            boolean r1 = r1.isEmpty()     // Catch:{ all -> 0x00b5 }
            if (r1 == 0) goto L_0x0043
            java.util.Vector r1 = r10.completeQueue     // Catch:{ all -> 0x00b5 }
            boolean r1 = r1.isEmpty()     // Catch:{ all -> 0x00b5 }
            if (r1 == 0) goto L_0x0043
            org.eclipse.paho.client.mqttv3.logging.Logger r1 = log     // Catch:{ all -> 0x00b5 }
            java.lang.String r3 = CLASS_NAME     // Catch:{ all -> 0x00b5 }
            java.lang.String r4 = "run"
            java.lang.String r5 = "704"
            r1.fine(r3, r4, r5)     // Catch:{ all -> 0x00b5 }
            java.lang.Object r1 = r10.workAvailable     // Catch:{ all -> 0x00b5 }
            r1.wait()     // Catch:{ all -> 0x00b5 }
        L_0x0043:
            monitor-exit(r2)     // Catch:{ all -> 0x00b5 }
        L_0x0044:
            boolean r1 = r10.running     // Catch:{ Throwable -> 0x00bd }
            if (r1 == 0) goto L_0x008e
            r8 = 0
            java.util.Vector r2 = r10.completeQueue     // Catch:{ Throwable -> 0x00bd }
            monitor-enter(r2)     // Catch:{ Throwable -> 0x00bd }
            java.util.Vector r1 = r10.completeQueue     // Catch:{ all -> 0x00ba }
            boolean r1 = r1.isEmpty()     // Catch:{ all -> 0x00ba }
            if (r1 != 0) goto L_0x0065
            java.util.Vector r1 = r10.completeQueue     // Catch:{ all -> 0x00ba }
            r3 = 0
            java.lang.Object r1 = r1.elementAt(r3)     // Catch:{ all -> 0x00ba }
            r0 = r1
            org.eclipse.paho.client.mqttv3.MqttToken r0 = (org.eclipse.paho.client.mqttv3.MqttToken) r0     // Catch:{ all -> 0x00ba }
            r8 = r0
            java.util.Vector r1 = r10.completeQueue     // Catch:{ all -> 0x00ba }
            r3 = 0
            r1.removeElementAt(r3)     // Catch:{ all -> 0x00ba }
        L_0x0065:
            monitor-exit(r2)     // Catch:{ all -> 0x00ba }
            if (r8 == 0) goto L_0x006b
            r10.handleActionComplete(r8)     // Catch:{ Throwable -> 0x00bd }
        L_0x006b:
            r7 = 0
            java.util.Vector r2 = r10.messageQueue     // Catch:{ Throwable -> 0x00bd }
            monitor-enter(r2)     // Catch:{ Throwable -> 0x00bd }
            java.util.Vector r1 = r10.messageQueue     // Catch:{ all -> 0x00f6 }
            boolean r1 = r1.isEmpty()     // Catch:{ all -> 0x00f6 }
            if (r1 != 0) goto L_0x0088
            java.util.Vector r1 = r10.messageQueue     // Catch:{ all -> 0x00f6 }
            r3 = 0
            java.lang.Object r1 = r1.elementAt(r3)     // Catch:{ all -> 0x00f6 }
            r0 = r1
            org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish r0 = (org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish) r0     // Catch:{ all -> 0x00f6 }
            r7 = r0
            java.util.Vector r1 = r10.messageQueue     // Catch:{ all -> 0x00f6 }
            r3 = 0
            r1.removeElementAt(r3)     // Catch:{ all -> 0x00f6 }
        L_0x0088:
            monitor-exit(r2)     // Catch:{ all -> 0x00f6 }
            if (r7 == 0) goto L_0x008e
            r10.handleMessage(r7)     // Catch:{ Throwable -> 0x00bd }
        L_0x008e:
            boolean r1 = r10.quiescing     // Catch:{ Throwable -> 0x00bd }
            if (r1 == 0) goto L_0x0097
            org.eclipse.paho.client.mqttv3.internal.ClientState r1 = r10.clientState     // Catch:{ Throwable -> 0x00bd }
            r1.checkQuiesceLock()     // Catch:{ Throwable -> 0x00bd }
        L_0x0097:
            java.util.concurrent.Semaphore r1 = r10.runningSemaphore
            r1.release()
            java.lang.Object r2 = r10.spaceAvailable
            monitor-enter(r2)
            org.eclipse.paho.client.mqttv3.logging.Logger r1 = log     // Catch:{ all -> 0x00b2 }
            java.lang.String r3 = CLASS_NAME     // Catch:{ all -> 0x00b2 }
            java.lang.String r4 = "run"
            java.lang.String r5 = "706"
            r1.fine(r3, r4, r5)     // Catch:{ all -> 0x00b2 }
            java.lang.Object r1 = r10.spaceAvailable     // Catch:{ all -> 0x00b2 }
            r1.notifyAll()     // Catch:{ all -> 0x00b2 }
            monitor-exit(r2)     // Catch:{ all -> 0x00b2 }
            goto L_0x0013
        L_0x00b2:
            r1 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x00b2 }
            throw r1
        L_0x00b5:
            r1 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x00b5 }
            throw r1     // Catch:{ InterruptedException -> 0x00b8 }
        L_0x00b8:
            r1 = move-exception
            goto L_0x0044
        L_0x00ba:
            r1 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x00ba }
            throw r1     // Catch:{ Throwable -> 0x00bd }
        L_0x00bd:
            r6 = move-exception
            org.eclipse.paho.client.mqttv3.logging.Logger r1 = log     // Catch:{ all -> 0x00f9 }
            java.lang.String r2 = CLASS_NAME     // Catch:{ all -> 0x00f9 }
            java.lang.String r3 = "run"
            java.lang.String r4 = "714"
            r5 = 0
            r1.fine(r2, r3, r4, r5, r6)     // Catch:{ all -> 0x00f9 }
            r1 = 0
            r10.running = r1     // Catch:{ all -> 0x00f9 }
            org.eclipse.paho.client.mqttv3.internal.ClientComms r1 = r10.clientComms     // Catch:{ all -> 0x00f9 }
            r2 = 0
            org.eclipse.paho.client.mqttv3.MqttException r3 = new org.eclipse.paho.client.mqttv3.MqttException     // Catch:{ all -> 0x00f9 }
            r3.<init>((java.lang.Throwable) r6)     // Catch:{ all -> 0x00f9 }
            r1.shutdownConnection(r2, r3)     // Catch:{ all -> 0x00f9 }
            java.util.concurrent.Semaphore r1 = r10.runningSemaphore
            r1.release()
            java.lang.Object r2 = r10.spaceAvailable
            monitor-enter(r2)
            org.eclipse.paho.client.mqttv3.logging.Logger r1 = log     // Catch:{ all -> 0x00f3 }
            java.lang.String r3 = CLASS_NAME     // Catch:{ all -> 0x00f3 }
            java.lang.String r4 = "run"
            java.lang.String r5 = "706"
            r1.fine(r3, r4, r5)     // Catch:{ all -> 0x00f3 }
            java.lang.Object r1 = r10.spaceAvailable     // Catch:{ all -> 0x00f3 }
            r1.notifyAll()     // Catch:{ all -> 0x00f3 }
            monitor-exit(r2)     // Catch:{ all -> 0x00f3 }
            goto L_0x0013
        L_0x00f3:
            r1 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x00f3 }
            throw r1
        L_0x00f6:
            r1 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x00f6 }
            throw r1     // Catch:{ Throwable -> 0x00bd }
        L_0x00f9:
            r1 = move-exception
            java.util.concurrent.Semaphore r2 = r10.runningSemaphore
            r2.release()
            java.lang.Object r2 = r10.spaceAvailable
            monitor-enter(r2)
            org.eclipse.paho.client.mqttv3.logging.Logger r3 = log     // Catch:{ all -> 0x0114 }
            java.lang.String r4 = CLASS_NAME     // Catch:{ all -> 0x0114 }
            java.lang.String r5 = "run"
            java.lang.String r9 = "706"
            r3.fine(r4, r5, r9)     // Catch:{ all -> 0x0114 }
            java.lang.Object r3 = r10.spaceAvailable     // Catch:{ all -> 0x0114 }
            r3.notifyAll()     // Catch:{ all -> 0x0114 }
            monitor-exit(r2)     // Catch:{ all -> 0x0114 }
            throw r1
        L_0x0114:
            r1 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x0114 }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.paho.client.mqttv3.internal.CommsCallback.run():void");
    }

    private void handleActionComplete(MqttToken token) throws MqttException {
        synchronized (token) {
            log.fine(CLASS_NAME, "handleActionComplete", "705", new Object[]{token.internalTok.getKey()});
            if (token.isComplete()) {
                this.clientState.notifyComplete(token);
            }
            token.internalTok.notifyComplete();
            if (!token.internalTok.isNotified()) {
                if (this.mqttCallback != null && (token instanceof MqttDeliveryToken) && token.isComplete()) {
                    this.mqttCallback.deliveryComplete((MqttDeliveryToken) token);
                }
                fireActionEvent(token);
            }
            if (token.isComplete() && ((token instanceof MqttDeliveryToken) || (token.getActionCallback() instanceof IMqttActionListener))) {
                token.internalTok.setNotified(true);
            }
        }
    }

    public void connectionLost(MqttException cause) {
        try {
            if (!(this.mqttCallback == null || cause == null)) {
                log.fine(CLASS_NAME, "connectionLost", "708", new Object[]{cause});
                this.mqttCallback.connectionLost(cause);
            }
            if (this.reconnectInternalCallback != null && cause != null) {
                this.reconnectInternalCallback.connectionLost(cause);
            }
        } catch (Throwable t) {
            log.fine(CLASS_NAME, "connectionLost", "720", new Object[]{t});
        }
    }

    public void fireActionEvent(MqttToken token) {
        IMqttActionListener asyncCB;
        if (token != null && (asyncCB = token.getActionCallback()) != null) {
            if (token.getException() == null) {
                log.fine(CLASS_NAME, "fireActionEvent", "716", new Object[]{token.internalTok.getKey()});
                asyncCB.onSuccess(token);
                return;
            }
            log.fine(CLASS_NAME, "fireActionEvent", "716", new Object[]{token.internalTok.getKey()});
            asyncCB.onFailure(token, token.getException());
        }
    }

    public void messageArrived(MqttPublish sendMessage) {
        if (this.mqttCallback != null || this.callbacks.size() > 0) {
            synchronized (this.spaceAvailable) {
                while (this.running && !this.quiescing && this.messageQueue.size() >= 10) {
                    try {
                        log.fine(CLASS_NAME, "messageArrived", "709");
                        this.spaceAvailable.wait(200);
                    } catch (InterruptedException e) {
                    }
                }
            }
            if (!this.quiescing) {
                this.messageQueue.addElement(sendMessage);
                synchronized (this.workAvailable) {
                    log.fine(CLASS_NAME, "messageArrived", "710");
                    this.workAvailable.notifyAll();
                }
            }
        }
    }

    public void quiesce() {
        this.quiescing = true;
        synchronized (this.spaceAvailable) {
            log.fine(CLASS_NAME, "quiesce", "711");
            this.spaceAvailable.notifyAll();
        }
    }

    public boolean isQuiesced() {
        if (this.quiescing && this.completeQueue.size() == 0 && this.messageQueue.size() == 0) {
            return true;
        }
        return false;
    }

    private void handleMessage(MqttPublish publishMessage) throws MqttException, Exception {
        String destName = publishMessage.getTopicName();
        log.fine(CLASS_NAME, "handleMessage", "713", new Object[]{new Integer(publishMessage.getMessageId()), destName});
        deliverMessage(destName, publishMessage.getMessageId(), publishMessage.getMessage());
        if (this.manualAcks) {
            return;
        }
        if (publishMessage.getMessage().getQos() == 1) {
            this.clientComms.internalSend(new MqttPubAck(publishMessage), new MqttToken(this.clientComms.getClient().getClientId()));
        } else if (publishMessage.getMessage().getQos() == 2) {
            this.clientComms.deliveryComplete(publishMessage);
            this.clientComms.internalSend(new MqttPubComp(publishMessage), new MqttToken(this.clientComms.getClient().getClientId()));
        }
    }

    public void messageArrivedComplete(int messageId, int qos) throws MqttException {
        if (qos == 1) {
            this.clientComms.internalSend(new MqttPubAck(messageId), new MqttToken(this.clientComms.getClient().getClientId()));
        } else if (qos == 2) {
            this.clientComms.deliveryComplete(messageId);
            this.clientComms.internalSend(new MqttPubComp(messageId), new MqttToken(this.clientComms.getClient().getClientId()));
        }
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void asyncOperationComplete(org.eclipse.paho.client.mqttv3.MqttToken r10) {
        /*
            r9 = this;
            r4 = 0
            boolean r0 = r9.running
            if (r0 == 0) goto L_0x002e
            java.util.Vector r0 = r9.completeQueue
            r0.addElement(r10)
            java.lang.Object r1 = r9.workAvailable
            monitor-enter(r1)
            org.eclipse.paho.client.mqttv3.logging.Logger r0 = log     // Catch:{ all -> 0x002b }
            java.lang.String r2 = CLASS_NAME     // Catch:{ all -> 0x002b }
            java.lang.String r3 = "asyncOperationComplete"
            java.lang.String r4 = "715"
            r6 = 1
            java.lang.Object[] r6 = new java.lang.Object[r6]     // Catch:{ all -> 0x002b }
            r7 = 0
            org.eclipse.paho.client.mqttv3.internal.Token r8 = r10.internalTok     // Catch:{ all -> 0x002b }
            java.lang.String r8 = r8.getKey()     // Catch:{ all -> 0x002b }
            r6[r7] = r8     // Catch:{ all -> 0x002b }
            r0.fine(r2, r3, r4, r6)     // Catch:{ all -> 0x002b }
            java.lang.Object r0 = r9.workAvailable     // Catch:{ all -> 0x002b }
            r0.notifyAll()     // Catch:{ all -> 0x002b }
            monitor-exit(r1)     // Catch:{ all -> 0x002b }
        L_0x002a:
            return
        L_0x002b:
            r0 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x002b }
            throw r0
        L_0x002e:
            r9.handleActionComplete(r10)     // Catch:{ Throwable -> 0x0032 }
            goto L_0x002a
        L_0x0032:
            r5 = move-exception
            org.eclipse.paho.client.mqttv3.logging.Logger r0 = log
            java.lang.String r1 = CLASS_NAME
            java.lang.String r2 = "asyncOperationComplete"
            java.lang.String r3 = "719"
            r0.fine(r1, r2, r3, r4, r5)
            org.eclipse.paho.client.mqttv3.internal.ClientComms r0 = r9.clientComms
            org.eclipse.paho.client.mqttv3.MqttException r1 = new org.eclipse.paho.client.mqttv3.MqttException
            r1.<init>((java.lang.Throwable) r5)
            r0.shutdownConnection(r4, r1)
            goto L_0x002a
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.paho.client.mqttv3.internal.CommsCallback.asyncOperationComplete(org.eclipse.paho.client.mqttv3.MqttToken):void");
    }

    /* access modifiers changed from: protected */
    public Thread getThread() {
        return this.callbackThread;
    }

    public void setMessageListener(String topicFilter, IMqttMessageListener messageListener) {
        this.callbacks.put(topicFilter, messageListener);
    }

    public void removeMessageListener(String topicFilter) {
        this.callbacks.remove(topicFilter);
    }

    public void removeMessageListeners() {
        this.callbacks.clear();
    }

    /* access modifiers changed from: protected */
    public boolean deliverMessage(String topicName, int messageId, MqttMessage aMessage) throws Exception {
        boolean delivered = false;
        Enumeration keys = this.callbacks.keys();
        while (keys.hasMoreElements()) {
            String topicFilter = (String) keys.nextElement();
            if (MqttTopic.isMatched(topicFilter, topicName)) {
                aMessage.setId(messageId);
                ((IMqttMessageListener) this.callbacks.get(topicFilter)).messageArrived(topicName, aMessage);
                delivered = true;
            }
        }
        if (this.mqttCallback == null || delivered) {
            return delivered;
        }
        aMessage.setId(messageId);
        this.mqttCallback.messageArrived(topicName, aMessage);
        return true;
    }
}
