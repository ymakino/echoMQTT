package echomqtt;

import echomqtt.json.JObject;
import echomqtt.json.JsonEncoderException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 *
 * @author ymakino
 */
public class MQTTManager {
    private static final Logger logger = Logger.getLogger(MQTTManager.class.getName());
    private static final String className = MQTTManager.class.getName();
    
    private String broker;
    private String clientId;
    private int publishQoS;
    private MqttClient client;
    
    private LinkedList<MQTTListener> listeners;
    
    private class MQTTManagerCallback implements MqttCallback {

        @Override
        public void connectionLost(Throwable thrwbl) {
            logger.entering(className, "MQTTManagerCallback.connectionLost", thrwbl);
            
            logger.logp(Level.INFO, className, "MQTTManagerCallback.connectionLost", "connectionLost", thrwbl);
            
            logger.exiting(className, "MQTTManagerCallback.connectionLost");
        }
        
        private LinkedList<MQTTListener> cloneListeners() {
            synchronized(listeners) {
                return new LinkedList<MQTTListener>(listeners);
            }
        }

        @Override
        public void messageArrived(String topic, MqttMessage mm) throws Exception {
            logger.entering(className, "MQTTManagerCallback.messageArrived", new Object[]{topic, mm});
            
            String payload = new String(mm.getPayload());
            
            for (MQTTListener listener : cloneListeners()) {
                listener.arrived(topic, payload);
            }
            
            logger.exiting(className, "MQTTManagerCallback.messageArrived");
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken imdt) {
            logger.entering(className, "MQTTManagerCallback.deliveryComplete", imdt);
            
            logger.exiting(className, "MQTTManagerCallback.deliveryComplete");
        }
        
    }
    
    public MQTTManager(String broker, String clientId) throws PublisherException {
        logger.entering(className, "MQTTManager", new Object[]{broker, clientId});
            
        this.broker = broker;
        this.clientId = clientId;
        publishQoS = 0;
        client = null;
        listeners = new LinkedList<MQTTListener>();
        
        logger.exiting(className, "MQTTManager");
    }
    
    public void setPublishQoS(int publishQoS) {
        logger.entering(className, "setPublishQos", publishQoS);
        
        this.publishQoS = publishQoS;
        
        logger.exiting(className, "setPublishQos");
    }
    
    public boolean addListener(MQTTListener listener) {
        logger.entering(className, "addListener", listener);
        
        synchronized (listeners) {
            boolean result = listeners.add(listener);
            logger.exiting(className, "addListener", result);
            return result;
        }
    }
    
    public boolean removeListener(MQTTListener listener) {
        logger.entering(className, "removeListener", listener);
        
        synchronized (listeners) {
            boolean result = listeners.remove(listener);
            logger.exiting(className, "removeListener", result);
            return result;
        }
    }
    
    public void connect(boolean cleanSession) throws PublisherException {
        logger.entering(className, "connect", cleanSession);
        
        try {
            MemoryPersistence persistence = new MemoryPersistence();
            client = new MqttClient(broker, clientId, persistence);
            client.setCallback(new MQTTManagerCallback());

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(cleanSession);
            options.setMaxInflight(1000);

            logger.logp(Level.INFO, className, "connect", "Connecting to broker: " + client.getServerURI());
            client.connect(options);
            client.subscribe("#");
        } catch (MqttException ex) {
            PublisherException exception = new PublisherException("catched exception", ex);
            logger.throwing(className, "connect", exception);
            throw exception;
        }
        
        logger.exiting(className, "connect");
    }
    
    public void disconnect() throws PublisherException {
        logger.entering(className, "disconnect");
        
        try {
            client.disconnect();
        } catch (MqttException me) {
            PublisherException exception = new PublisherException("catched exception", me);
            logger.throwing(className, "disconnect", exception);
            throw exception;
        }
        
        logger.exiting(className, "disconnect");
    }
    
    public boolean isConnected() {
        logger.entering(className, "isConnected");
        
        boolean result = (client != null) && client.isConnected();
        logger.exiting(className, "isConnected", result);
        return result;
    }
    
    public void publish(String topic, JObject jobject) throws PublisherException, JsonEncoderException {
        logger.entering(className, "publish", new Object[]{topic, jobject});
        
        String payload = jobject.toJSON();
        
        logger.logp(Level.INFO, className, "publish", "Topic: " + topic + ", Payload: " + payload);
        
        if (!isConnected()) {
            connect(false);
        }
        
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(publishQoS);
            message.setRetained(false);
            client.publish(topic, message);
        } catch (MqttException me) {
            PublisherException exception = new PublisherException("catched exception", me);
            logger.throwing(className, "publish", exception);
            throw exception;
        }
        
        logger.exiting(className, "publish");
    }
}
