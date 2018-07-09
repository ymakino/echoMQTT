package echomqtt;

import echomqtt.converter.ConverterException;
import echowand.common.Data;
import echowand.common.EOJ;
import echowand.common.EPC;
import echowand.net.InetNodeInfo;
import echowand.net.SubnetException;
import echowand.service.Service;
import echowand.util.Pair;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import echomqtt.json.JsonDecoderException;
import echomqtt.json.JsonValue;
import echomqtt.json.JsonObject;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/**
 *
 * @author ymakino
 */
public class SubscribeTask {
    private static final Logger logger = Logger.getLogger(SubscribeTask.class.getName());
    private static final String className = SubscribeTask.class.getName();
    
    private Service service;
    private MQTTManager mqttManager;
    private SubscribeRule subscribeRule;
    private SubscribeTaskListener listener;
    
    private boolean working = false;
    
    private class SubscribeTaskListener implements MQTTListener {
        
        @Override
        public void arrived(String topic, String payload) {
            logger.entering(className, "SubscribeTask.arrived", new Object[]{topic, payload});
        
            LinkedList<Pair<EPC, Data>> properties = new LinkedList<Pair<EPC, Data>>();
            
            if (MqttTopic.isMatched(subscribeRule.getTopic(), topic)) {
                
                JsonValue json;
                
                try {
                    json = JsonValue.parseJSON(payload);
                } catch (JsonDecoderException ex) {
                    logger.logp(Level.INFO, className, "SubscribeTask.arrived", "invalid JSON payload: " + payload, ex);
                    logger.exiting(className, "SubscribeTask.arrived");
                    return;
                }
                
                JsonObject object = json.asObject();
                
                if (object == null) {
                    logger.logp(Level.INFO, className, "SubscribeTask.arrived", "invalid payload: " + payload);
                    logger.exiting(className, "SubscribeTask.arrived");
                    return;
                }
                
                for (PropertyRule propertyRule : subscribeRule.getPropertyRules()) {
                    try {
                        JsonValue value = object.get(propertyRule.getName());

                        if (value == null) {
                            continue;
                        }

                        Data data = propertyRule.getConverter().convertString(value.toString());

                        if (data != null) {
                            logger.logp(Level.FINE, className, "SubscribeTask.arrived", "Convert: " + value + " -> " + data);
                            properties.add(new Pair<EPC, Data>(propertyRule.getEPC(), data));
                        }
                    } catch (ConverterException ex) {
                        logger.logp(Level.INFO, className, "SubscribeTask.arrived", "catched exception", ex);
                    }
                }
            
                if (!properties.isEmpty()) {
                    try {
                        InetNodeInfo nodeInfo = new InetNodeInfo(InetAddress.getByName(subscribeRule.getAddress()));
                        EOJ eoj = subscribeRule.getEOJ();
                        logger.logp(Level.FINE, className, "SubscribeTask.arrived", "doSet: " + nodeInfo + " " + eoj + " " + properties);
                        service.doSet(nodeInfo, eoj, properties, 1000, false);
                    } catch (UnknownHostException ex) {
                        logger.logp(Level.INFO, className, "SubscribeTask.arrived", "catched exception", ex);
                    } catch (SubnetException ex) {
                        logger.logp(Level.INFO, className, "SubscribeTask.arrived", "catched exception", ex);
                    }
                }
            }
            
            logger.exiting(className, "SubscribeTask.arrived");
        }
    }
    
    public SubscribeTask(Service service, MQTTManager mqttManager, SubscribeRule subscribeRule) {
        logger.entering(className, "SubscribeTask", new Object[]{service, mqttManager, subscribeRule});
            
        this.service = service;
        this.mqttManager = mqttManager;
        this.subscribeRule = subscribeRule;
        listener = new SubscribeTaskListener();
        
        logger.exiting(className, "SubscribeTask");
    }
    
    public synchronized boolean start() {
        logger.entering(className, "start");
        
        if (working) {
            logger.exiting(className, "start", false);
            return false;
        }
        
        boolean result = mqttManager.addListener(listener);
        
        if (result) {
            working = true;
        }
        
        logger.exiting(className, "start", result);
        return result;
    }
    
    public synchronized boolean cancel() {
        logger.entering(className, "cancel");
        
        if (!working) {
            logger.exiting(className, "cancel", false);
            return false;
        }
        
        boolean result = mqttManager.removeListener(listener);
        
        if (result) {
            working = false;
        }
        
        logger.exiting(className, "cancel", result);
        return result;
    }
}
