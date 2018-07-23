package echomqtt;

import echomqtt.converter.ConverterException;
import echomqtt.json.JObject;
import echomqtt.json.JValue;
import echomqtt.json.JsonEncoderException;
import echowand.net.Node;
import echowand.net.SubnetException;
import echowand.service.Service;
import echowand.service.result.ObserveListener;
import echowand.service.result.ObserveResult;
import echowand.service.result.ResultData;
import echowand.service.result.ResultFrame;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class NotifyTask {
    private static final Logger logger = Logger.getLogger(NotifyTask.class.getName());
    private static final String className = NotifyTask.class.getName();
    
    private Service service;
    private MQTTManager mqttManager;
    private ObserveResult observeResult;
    
    private LinkedList<PublishRule> publishRules;
    
    private boolean working = false;
    
    public NotifyTask(Service service, MQTTManager mqttManager) throws SubnetException {
        logger.entering(className, "NotifyTask", new Object[]{service, mqttManager});
        
        this.service = service;
        this.mqttManager = mqttManager;
        publishRules = new LinkedList<PublishRule>();
        
        logger.exiting(className, "NotifyTask");
    }
    
    public synchronized boolean start() {
        logger.entering(className, "start");
            
        if (working) {
            logger.exiting(className, "start", false);
            return false;
        }
        
        observeResult = service.doObserve(new ObserveListener() {
            @Override
            public void receive(ObserveResult result, ResultFrame resultFrame, ResultData resultData) {
                logger.entering(className, "ObserveListener.receive", new Object[]{result, resultFrame, resultData});
                
                for (PublishRule publishRule: publishRules) {
                    Node node;
                    try {
                        node = service.getSubnet().getRemoteNode(publishRule.getAddress());
                    } catch (SubnetException ex) {
                        logger.logp(Level.WARNING, className, "ObserveListener.receive", "invalid address: " + publishRule.getAddress(), ex);
                        continue;
                    }
                    
                    if (!node.equals(resultFrame.getSender())) {
                        continue;
                    }
                    
                    HashMap<String, JValue> jsonMap = new HashMap<String, JValue>();
                    
                    for (PropertyRule propertyRule: publishRule.getPropertyRules()) {
                        if (propertyRule.getEPC() == resultData.getEPC()) {
                            String key = propertyRule.getName();
                            JValue value;
                            
                            try {
                                value = propertyRule.getConverter().convert(resultData.getActualData());
                                jsonMap.put(key, value);
                            } catch (ConverterException ex) {
                                logger.logp(Level.WARNING, className, "ObserveListener.receive", "catched exception", ex);
                            }
                        }
                    }
                    
                    if (jsonMap.isEmpty()) {
                        continue;
                    }
                    
                    if (!jsonMap.keySet().contains("timestamp")) {
                        LocalDateTime localDateTime = LocalDateTime.now();
                        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
                        jsonMap.put("timestamp", JValue.newString(DateTimeFormatter.ISO_INSTANT.format(zonedDateTime)));
                    }
                        
                    try {
                        JObject jobject = JValue.newObject(jsonMap);
                        mqttManager.publish(publishRule, jobject);
                    } catch (PublisherException ex) {
                        logger.logp(Level.WARNING, className, "ObserveListener.receive", "catched exception", ex);
                    } catch (JsonEncoderException ex) {
                        logger.logp(Level.WARNING, className, "ObserveListener.receive", "catched exception", ex);
                    }
                }
                
                logger.exiting(className, "ObserveListener.receive");
            }
        });
        
        observeResult.disableDataList();
        observeResult.disableFrameList();
        
        working = true;
        
        logger.exiting(className, "start", true);
        return true;
    }
    
    public synchronized boolean cancel() {
        logger.entering(className, "cancel");
        
        if (!working) {
            logger.exiting(className, "cancel", false);
            return false;
        }
        
        observeResult.stopObserve();
        observeResult = null;
        
        working = false;
        
        logger.exiting(className, "cancel", true);
        return true;
    }
    
    public void addRule(PublishRule publishRule) {
        logger.entering(className, "addRule", publishRule);
        
        publishRules.add(publishRule);
        
        logger.exiting(className, "addRule");
    }
}
