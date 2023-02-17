package org.eclipse.paho.client.mqttv3.persist;

import java.util.Enumeration;
import java.util.Hashtable;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttPersistable;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

public class MemoryPersistence implements MqttClientPersistence {
    private Hashtable data;

    public void close() throws MqttPersistenceException {
        this.data.clear();
    }

    public Enumeration keys() throws MqttPersistenceException {
        return this.data.keys();
    }

    public MqttPersistable get(String key) throws MqttPersistenceException {
        return (MqttPersistable) this.data.get(key);
    }

    public void open(String clientId, String serverURI) throws MqttPersistenceException {
        this.data = new Hashtable();
    }

    public void put(String key, MqttPersistable persistable) throws MqttPersistenceException {
        this.data.put(key, persistable);
    }

    public void remove(String key) throws MqttPersistenceException {
        this.data.remove(key);
    }

    public void clear() throws MqttPersistenceException {
        this.data.clear();
    }

    public boolean containsKey(String key) throws MqttPersistenceException {
        return this.data.containsKey(key);
    }
}
