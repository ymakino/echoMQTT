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
import echomqtt.json.JValue;
import echomqtt.json.JObject;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/**
 *
 * @author ymakino
 */
public class SetTask {
    private static final Logger logger = Logger.getLogger(SetTask.class.getName());
    private static final String className = SetTask.class.getName();
    
    private Service service;
    private MQTTManager mqttManager;
    private SubscribeRule subscribeRule;
    private SetTaskListener listener;
    
    private boolean working = false;
    
    private class SetTaskListener implements MQTTListener {
        
        @Override
        public boolean arrived(String topic, String payload) {
            logger.entering(className, "SetTask.arrived", new Object[]{topic, payload});
        
            boolean result = false;
            LinkedList<Pair<EPC, Data>> properties = new LinkedList<Pair<EPC, Data>>();
            
            if (!MqttTopic.isMatched(subscribeRule.getTopic(), topic)) {
                logger.exiting(className, "SetTask.arrived", false);
                return false;
            }
            
            JValue json;

            try {
                json = JValue.parseJSON(payload);
            } catch (JsonDecoderException ex) {
                logger.logp(Level.WARNING, className, "SetTask.arrived", "invalid JSON payload: " + payload, ex);
                logger.exiting(className, "SetTask.arrived", false);
                return false;
            }

            JObject jobject = json.asObject();

            if (jobject == null) {
                logger.logp(Level.WARNING, className, "SetTask.arrived", "invalid payload: " + payload);
                logger.exiting(className, "SetTask.arrived", false);
                return false;
            }

            for (PropertyRule propertyRule : subscribeRule.getPropertyRules()) {
                try {
                    JValue jvalue = jobject.get(propertyRule.getName());

                    if (jvalue == null) {
                        continue;
                    }

                    Data data = propertyRule.getConverter().convert(jvalue);

                    if (data != null) {
                        logger.logp(Level.FINE, className, "SetTask.arrived", "Convert: " + jvalue + " -> " + data);
                        properties.add(new Pair<EPC, Data>(propertyRule.getEPC(), data));
                    }
                } catch (ConverterException ex) {
                    logger.logp(Level.WARNING, className, "SetTask.arrived", "catched exception", ex);
                }
            }

            if (!properties.isEmpty()) {
                try {
                    InetNodeInfo nodeInfo = new InetNodeInfo(InetAddress.getByName(subscribeRule.getAddress()));
                    EOJ eoj = subscribeRule.getEOJ();
                    logger.logp(Level.INFO, className, "SetTask.arrived", "doSet: " + nodeInfo + " " + eoj + " " + properties);
                    service.doSet(nodeInfo, eoj, properties, 1000, false);
                    result = true;
                } catch (UnknownHostException ex) {
                    logger.logp(Level.WARNING, className, "SetTask.arrived", "catched exception", ex);
                } catch (SubnetException ex) {
                    logger.logp(Level.WARNING, className, "SetTask.arrived", "catched exception", ex);
                }
            }
            
            logger.exiting(className, "SetTask.arrived", result);
            return result;
        }
    }
    
    public SetTask(Service service, MQTTManager mqttManager, SubscribeRule subscribeRule) {
        logger.entering(className, "SetTask", new Object[]{service, mqttManager, subscribeRule});
            
        this.service = service;
        this.mqttManager = mqttManager;
        this.subscribeRule = subscribeRule;
        listener = new SetTaskListener();
        
        logger.exiting(className, "SetTask");
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
