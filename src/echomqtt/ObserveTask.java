package echomqtt;

import echomqtt.converter.ConverterException;
import echomqtt.json.JObject;
import echomqtt.json.JValue;
import echomqtt.json.JsonDecoderException;
import echomqtt.json.JsonEncoderException;
import echowand.common.EOJ;
import echowand.net.Node;
import echowand.net.SubnetException;
import echowand.service.Service;
import echowand.service.result.ObserveListener;
import echowand.service.result.ObserveResult;
import echowand.service.result.ResultData;
import echowand.service.result.ResultFrame;
import echowand.util.Selector;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
public class ObserveTask {
    private static final Logger logger = Logger.getLogger(ObserveTask.class.getName());
    private static final String className = ObserveTask.class.getName();
    
    private Service service;
    private MQTTManager mqttManager;
    private ObserveResult observeResult;
    
    private LinkedList<PublishRule> publishRules;
    
    private boolean working = false;
    
    public ObserveTask(Service service, MQTTManager mqttManager) throws SubnetException {
        logger.entering(className, "ObserveTask", new Object[]{service, mqttManager});
        
        this.service = service;
        this.mqttManager = mqttManager;
        publishRules = new LinkedList<PublishRule>();
        
        logger.exiting(className, "ObserveTask");
    }
    
    public synchronized boolean start() {
        logger.entering(className, "start");
            
        if (working) {
            logger.exiting(className, "start", false);
            return false;
        }
        
        observeResult = service.doObserve(new ObserveListener() {
            
            private LinkedList<PublishRule> filterPublishRules(ResultFrame resultFrame, ResultData resultData) {
                LinkedList<PublishRule> rules = new LinkedList<PublishRule>();
                
                for (PublishRule publishRule: publishRules) {
                
                    EOJ eoj = publishRule.getEOJ();
                    Node node = null;

                    try {
                        if (publishRule.getNode() != null) {
                            node = service.getSubnet().getRemoteNode(publishRule.getNode());
                        }
                    } catch (SubnetException ex) {
                        logger.logp(Level.WARNING, className, "ObserveListener.filterPublishRules", "invalid node: " + publishRule.getNode(), ex);
                        continue;
                    }

                    if (node != null && !node.equals(resultFrame.getSender())) {
                        continue;
                    }

                    if (eoj.isAllInstance()) {
                        if (!resultData.getEOJ().isMemberOf(eoj.getClassEOJ())) {
                            continue;
                        }
                    } else {
                        if (!resultData.getEOJ().equals(eoj)) {
                            continue;
                        }
                    }
                    
                    rules.add(publishRule);
                }
                
                return rules;
            }
            
            @Override
            public void receive(ObserveResult result, ResultFrame resultFrame, ResultData resultData) {
                logger.entering(className, "ObserveListener.receive", new Object[]{result, resultFrame, resultData});
                
                for (PublishRule publishRule: filterPublishRules(resultFrame, resultData)) {
                    String nodeName = publishRule.getNode();
                    Node node;
                    EOJ eoj;

                    if (nodeName != null) {
                        try {
                            node = service.getRemoteNode(nodeName);
                        } catch (SubnetException ex) {
                            logger.logp(Level.WARNING, className, "ObserveListener.receive", "catched exception", ex);
                            continue;
                        }
                    } else {
                        node = service.getGroupNode();
                    }

                    eoj = publishRule.getEOJ();
                    
                    HashMap<String, JValue> dataMap = new HashMap<String, JValue>();
                    
                    for (HashMap.Entry<String, String> entry: publishRule.getAddition().entrySet()) {
                        try {
                            dataMap.putIfAbsent(entry.getKey(), JValue.parseJSON(entry.getValue()));
                        }   catch (JsonDecoderException ex) {
                            logger.logp(Level.INFO, className, "PublishTimerTask.publish", "catched exception", ex);
                        }
                    }
                    
                    for (PropertyRule propertyRule: publishRule.getPropertyRules()) {
                        if (propertyRule.getEPC() == resultData.getEPC()) {
                            String key = propertyRule.getName();
                            JValue value;
                            
                            try {
                                value = propertyRule.getConverter().convert(resultData.getActualData());
                                dataMap.put(key, value);
                            } catch (ConverterException ex) {
                                logger.logp(Level.WARNING, className, "ObserveListener.receive", "catched exception", ex);
                            }
                        }
                    }
                    
                    if (dataMap.isEmpty()) {
                        continue;
                    }
                    
                    HashMap<String, JValue> jsonMap = new HashMap<String, JValue>();
                    if (publishRule.getDataField() != null) {
                        jsonMap.put(publishRule.getDataField(), JValue.newObject(dataMap));
                    } else {
                        jsonMap.putAll(dataMap);
                    }
                    
                    long timestamp = resultFrame.getTimestamp();
                    LocalDateTime localDateTime = LocalDateTime.ofEpochSecond((int)(timestamp/1000), (int)((timestamp%1000)*1000*1000), ZoneOffset.UTC);
                    ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneOffset.UTC);
                    jsonMap.putIfAbsent("timestamp", JValue.newString(DateTimeFormatter.ISO_INSTANT.format(zonedDateTime)));
                    jsonMap.putIfAbsent("eoj", JValue.newString(resultData.getEOJ().toString()));
                    jsonMap.putIfAbsent("class_eoj", JValue.newString(eoj.getClassEOJ().toString()));
                    jsonMap.putIfAbsent("class_group_code", JValue.newNumber(0x00ff & eoj.getClassGroupCode()));
                    jsonMap.putIfAbsent("class_code", JValue.newNumber(0x00ff & eoj.getClassCode()));
                    jsonMap.putIfAbsent("instance_code", JValue.newNumber(0x00ff & eoj.getInstanceCode()));
                    jsonMap.putIfAbsent("node", JValue.newString(resultFrame.getSender().toString()));
                    jsonMap.putIfAbsent("method", JValue.newString("notify"));
                    
                    HashMap<String, JValue> filterJsonMap = new HashMap<String, JValue>();
                    if (nodeName != null) {
                        filterJsonMap.put("node", JValue.newString(nodeName));
                    }
                    filterJsonMap.put("eoj", JValue.newString(eoj.toString()));
                    jsonMap.put("filter", JValue.newObject(filterJsonMap));
                        
                    try {
                        JObject jobject = JValue.newObject(jsonMap);
                        
                        Selector<PublishTopic> selector = new Selector<PublishTopic>() {
                            @Override
                            public boolean match(PublishTopic object) {
                                if (object.getInstance() > 0) {
                                    if (object.getInstance() != eoj.getInstanceCode()) {
                                        return false;
                                    }
                                }
                                
                                if (object.getNode() == null) {
                                    return true;
                                }

                                try {
                                    Node publishNode = service.getCore().getSubnet().getRemoteNode(object.getNode());
                                    if (node.equals(publishNode)) {
                                        return true;
                                    }
                                } catch (SubnetException ex) {
                                    logger.logp(Level.INFO, className, "ObserveListener.publish", "catched exception", ex);
                                }
                                
                                return false;
                            }
                        };
                        
                        for (PublishTopic publishTopic: publishRule.getPublishTopics(selector)) {
                            String topic = publishTopic.getTopic()
                                    .replaceAll("\\[NODE\\]", resultFrame.getSender().toString())
                                    .replaceAll("\\[EOJ\\]", resultData.getEOJ().toString())
                                    .replaceAll("\\[CLASS_EOJ\\]", eoj.getClassEOJ().toString())
                                    .replaceAll("\\[CLASS_CODE\\]", String.format("%02x", 0x00ff & eoj.getClassCode()))
                                    .replaceAll("\\[CLASS_GROUP_CODE\\]", String.format("%02x", 0x00ff & eoj.getClassGroupCode()))
                                    .replaceAll("\\[INSTANCE_CODE\\]", String.format("%02x", 0x00ff & eoj.getInstanceCode()))
                                    .replaceAll("\\[METHOD\\]", "notify")
                                    .replace('.', '_');
                            mqttManager.publish(topic, jobject);
                        }
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
