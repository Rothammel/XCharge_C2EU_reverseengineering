package org.eclipse.paho.client.mqttv3.internal;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttAck;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttConnack;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttSuback;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;
import org.eclipse.paho.client.mqttv3.logging.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;

/* loaded from: classes.dex */
public class Token {
    private static final String CLASS_NAME = Token.class.getName();
    private static final Logger log = LoggerFactory.getLogger(LoggerFactory.MQTT_CLIENT_MSG_CAT, CLASS_NAME);
    private String key;
    private volatile boolean completed = false;
    private boolean pendingComplete = false;
    private boolean sent = false;
    private Object responseLock = new Object();
    private Object sentLock = new Object();
    protected MqttMessage message = null;
    private MqttWireMessage response = null;
    private MqttException exception = null;
    private String[] topics = null;
    private IMqttAsyncClient client = null;
    private IMqttActionListener callback = null;
    private Object userContext = null;
    private int messageID = 0;
    private boolean notified = false;

    public Token(String logContext) {
        log.setResourceName(logContext);
    }

    public int getMessageID() {
        return this.messageID;
    }

    public void setMessageID(int messageID) {
        this.messageID = messageID;
    }

    public boolean checkResult() throws MqttException {
        if (getException() != null) {
            throw getException();
        }
        return true;
    }

    public MqttException getException() {
        return this.exception;
    }

    public boolean isComplete() {
        return this.completed;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isCompletePending() {
        return this.pendingComplete;
    }

    protected boolean isInUse() {
        return (getClient() == null || isComplete()) ? false : true;
    }

    public void setActionCallback(IMqttActionListener listener) {
        this.callback = listener;
    }

    public IMqttActionListener getActionCallback() {
        return this.callback;
    }

    public void waitForCompletion() throws MqttException {
        waitForCompletion(-1L);
    }

    public void waitForCompletion(long timeout) throws MqttException {
        log.fine(CLASS_NAME, "waitForCompletion", "407", new Object[]{getKey(), new Long(timeout), this});
        MqttWireMessage resp = waitForResponse(timeout);
        if (resp == null && !this.completed) {
            log.fine(CLASS_NAME, "waitForCompletion", "406", new Object[]{getKey(), this});
            this.exception = new MqttException(32000);
            throw this.exception;
        }
        checkResult();
    }

    protected MqttWireMessage waitForResponse() throws MqttException {
        return waitForResponse(-1L);
    }

    protected MqttWireMessage waitForResponse(long timeout) throws MqttException {
        synchronized (this.responseLock) {
            Logger logger = log;
            String str = CLASS_NAME;
            Object[] objArr = new Object[7];
            objArr[0] = getKey();
            objArr[1] = new Long(timeout);
            objArr[2] = new Boolean(this.sent);
            objArr[3] = new Boolean(this.completed);
            objArr[4] = this.exception == null ? "false" : "true";
            objArr[5] = this.response;
            objArr[6] = this;
            logger.fine(str, "waitForResponse", "400", objArr, this.exception);
            while (!this.completed) {
                if (this.exception == null) {
                    try {
                        log.fine(CLASS_NAME, "waitForResponse", "408", new Object[]{getKey(), new Long(timeout)});
                        if (timeout <= 0) {
                            this.responseLock.wait();
                        } else {
                            this.responseLock.wait(timeout);
                        }
                    } catch (InterruptedException e) {
                        this.exception = new MqttException(e);
                    }
                }
                if (!this.completed) {
                    if (this.exception != null) {
                        log.fine(CLASS_NAME, "waitForResponse", "401", null, this.exception);
                        throw this.exception;
                    } else if (timeout > 0) {
                        break;
                    }
                }
            }
        }
        log.fine(CLASS_NAME, "waitForResponse", "402", new Object[]{getKey(), this.response});
        return this.response;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void markComplete(MqttWireMessage msg, MqttException ex) {
        log.fine(CLASS_NAME, "markComplete", "404", new Object[]{getKey(), msg, ex});
        synchronized (this.responseLock) {
            if (msg instanceof MqttAck) {
                this.message = null;
            }
            this.pendingComplete = true;
            this.response = msg;
            this.exception = ex;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void notifyComplete() {
        log.fine(CLASS_NAME, "notifyComplete", "404", new Object[]{getKey(), this.response, this.exception});
        synchronized (this.responseLock) {
            if (this.exception == null && this.pendingComplete) {
                this.completed = true;
                this.pendingComplete = false;
            } else {
                this.pendingComplete = false;
            }
            this.responseLock.notifyAll();
        }
        synchronized (this.sentLock) {
            this.sent = true;
            this.sentLock.notifyAll();
        }
    }

    public void waitUntilSent() throws MqttException {
        synchronized (this.sentLock) {
            synchronized (this.responseLock) {
                if (this.exception != null) {
                    throw this.exception;
                }
            }
            while (!this.sent) {
                try {
                    log.fine(CLASS_NAME, "waitUntilSent", "409", new Object[]{getKey()});
                    this.sentLock.wait();
                } catch (InterruptedException e) {
                }
            }
            if (!this.sent) {
                if (this.exception == null) {
                    throw ExceptionHelper.createMqttException(6);
                }
                throw this.exception;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void notifySent() {
        log.fine(CLASS_NAME, "notifySent", "403", new Object[]{getKey()});
        synchronized (this.responseLock) {
            this.response = null;
            this.completed = false;
        }
        synchronized (this.sentLock) {
            this.sent = true;
            this.sentLock.notifyAll();
        }
    }

    public IMqttAsyncClient getClient() {
        return this.client;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setClient(IMqttAsyncClient client) {
        this.client = client;
    }

    public void reset() throws MqttException {
        if (isInUse()) {
            throw new MqttException(32201);
        }
        log.fine(CLASS_NAME, "reset", "410", new Object[]{getKey()});
        this.client = null;
        this.completed = false;
        this.response = null;
        this.sent = false;
        this.exception = null;
        this.userContext = null;
    }

    public MqttMessage getMessage() {
        return this.message;
    }

    public MqttWireMessage getWireMessage() {
        return this.response;
    }

    public void setMessage(MqttMessage msg) {
        this.message = msg;
    }

    public String[] getTopics() {
        return this.topics;
    }

    public void setTopics(String[] topics) {
        this.topics = topics;
    }

    public Object getUserContext() {
        return this.userContext;
    }

    public void setUserContext(Object userContext) {
        this.userContext = userContext;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    public void setException(MqttException exception) {
        synchronized (this.responseLock) {
            this.exception = exception;
        }
    }

    public boolean isNotified() {
        return this.notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    public String toString() {
        StringBuffer tok = new StringBuffer();
        tok.append("key=").append(getKey());
        tok.append(" ,topics=");
        if (getTopics() != null) {
            for (int i = 0; i < getTopics().length; i++) {
                tok.append(getTopics()[i]).append(", ");
            }
        }
        tok.append(" ,usercontext=").append(getUserContext());
        tok.append(" ,isComplete=").append(isComplete());
        tok.append(" ,isNotified=").append(isNotified());
        tok.append(" ,exception=").append(getException());
        tok.append(" ,actioncallback=").append(getActionCallback());
        return tok.toString();
    }

    public int[] getGrantedQos() {
        int[] val = new int[0];
        if (this.response instanceof MqttSuback) {
            int[] val2 = ((MqttSuback) this.response).getGrantedQos();
            return val2;
        }
        return val;
    }

    public boolean getSessionPresent() {
        if (!(this.response instanceof MqttConnack)) {
            return false;
        }
        boolean val = ((MqttConnack) this.response).getSessionPresent();
        return val;
    }

    public MqttWireMessage getResponse() {
        return this.response;
    }
}