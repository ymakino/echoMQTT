package echomqtt;

import echomqtt.converter.ConverterException;
import echomqtt.json.JObject;
import echomqtt.json.JValue;
import echomqtt.json.JsonDecoderException;
import echomqtt.json.JsonEncoderException;
import echowand.common.Data;
import echowand.common.EOJ;
import echowand.common.EPC;
import echowand.net.Node;
import echowand.net.StandardPayload;
import echowand.net.SubnetException;
import echowand.service.Service;
import echowand.service.result.GetListener;
import echowand.service.result.GetResult;
import echowand.service.result.ResultData;
import echowand.service.result.ResultFrame;
import echowand.util.Selector;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class GetTask {
    private static final Logger logger = Logger.getLogger(GetTask.class.getName());
    private static final String className = GetTask.class.getName();
    
    private static final int DEFAULT_TIMEOUT = 5000;
    
    private Service service;
    private MQTTManager mqttManager;
    private PublishRule publishRule;
    private Node node;
    private EOJ eoj;
    private LinkedList<EPC> epcs;
    private Timer timer;
    private int timeout;
    
    private boolean working = false;
    
    public GetTask(Service service, MQTTManager mqttManager, PublishRule publishRule) throws SubnetException {
        logger.entering(className, "GetTask", new Object[]{service, mqttManager, publishRule});
        
        this.service = service;
        this.mqttManager = mqttManager;
        this.publishRule = publishRule;
        
        timeout = DEFAULT_TIMEOUT;
        
        String nodeName = publishRule.getNode();
        
        if (nodeName != null) {
            node = service.getRemoteNode(nodeName);
        } else {
            node = service.getGroupNode();
        }
        
        eoj = publishRule.getEOJ();
        
        epcs = new LinkedList<EPC>();
        for (int i=0; i<publishRule.countPropertieRules(); i++) {
            PropertyRule propertyRule = publishRule.getPropertyRuleAt(i);
            EPC epc = propertyRule.getEPC();
            if (!epcs.contains(epc)) {
                epcs.add(epc);
            }
        }
        
        logger.exiting(className, "GetTask");
    }
    
    private class PublishTimerTask extends TimerTask {
        
        private void doGet() throws SubnetException {
            logger.entering(className, "PublishTimerTask.doGet");
        
            service.doGet(node, eoj, epcs, timeout, new GetListener() {
                
                private HashSet<PublishTopic> published = new HashSet<PublishTopic>();
                
                private boolean generate(HashMap<String, JValue> dataMap, EPC epc, Data data) throws ConverterException {
                    logger.entering(className, "PublishTimerTask.generate", new Object[]{dataMap, epc, data});
                    
                    boolean result = false;
                    
                    for (int i=0; i<publishRule.countPropertieRules(); i++) {
                        PropertyRule propertyRule = publishRule.getPropertyRuleAt(i);
                        if (!data.isEmpty() && propertyRule.getEPC() == epc) {
                            String key = propertyRule.getName();
                            JValue value = propertyRule.getConverter().convert(data);
                            dataMap.put(key, value);
                            result = true;
                        }
                    }
                    
                    logger.exiting(className, "PublishTimerTask.generate", result);
                    return result;
                }

                @Override
                public void receive(GetResult result, ResultFrame resultFrame) {
                    logger.entering(className, "PublishTimerTask.receive", new Object[]{result, resultFrame});
                    
                    if (!service.getSubnet().getGroupNode().equals(node)){
                        if (!resultFrame.getSender().equals(node)) {
                            logger.logp(Level.WARNING, className, "PublishTimerTask.receive", "invalid sender: " + resultFrame.getSender() + " (" + publishRule + " -> " + resultFrame + ")");
                            logger.exiting(className, "PublishTimerTask.publish");
                            return;
                        }
                    }
                    
                    HashMap<String, JValue> dataMap = new HashMap<String, JValue>();
                    HashSet<EPC> remainingEPCs = new HashSet<EPC>();
                    
                    for (PropertyRule propertyRule: publishRule.getPropertyRules()) {
                        remainingEPCs.add(propertyRule.getEPC());
                    }
                    
                    List<ResultData> resultDataList = result.getDataList(resultFrame, true);

                    for (ResultData resultData: resultDataList) {
                        try {
                            generate(dataMap, resultData.getEPC(), resultData.getActualData());
                            remainingEPCs.remove(resultData.getEPC());
                        } catch (ConverterException ex) {
                            logger.logp(Level.WARNING, className, "PublishTimerTask.receive", "cannot convert: " + resultData, ex);
                        }
                    }

                    if (!remainingEPCs.isEmpty()) {
                        StandardPayload payload = resultFrame.getCommonFrame().getEDATA(StandardPayload.class);
                        logger.logp(Level.WARNING, className, "PublishTimerTask.receive", "lack of property: " + resultFrame.getSender() + " " + payload.getSEOJ() + " " + remainingEPCs);
                    }

                    StandardPayload payload = resultFrame.getCommonFrame().getEDATA(StandardPayload.class);

                    publish(dataMap, resultFrame.getSender(), payload.getSEOJ(), resultFrame.getTimestamp());
                    
                    logger.exiting(className, "PublishTimerTask.receive");
                }
                
                public void publish(HashMap<String, JValue> dataMap, Node node, EOJ eoj, long timestamp) {
                    logger.entering(className, "PublishTimerTask.publish", new Object[]{dataMap, node});
                    
                    for (HashMap.Entry<String, String> entry: publishRule.getAddition().entrySet()) {
                        try {
                            dataMap.putIfAbsent(entry.getKey(), JValue.parseJSON(entry.getValue()));
                        }   catch (JsonDecoderException ex) {
                            logger.logp(Level.INFO, className, "PublishTimerTask.publish", "catched exception", ex);
                        }
                    }
                    
                    HashMap<String, JValue> jsonMap = new HashMap<String, JValue>();
                    
                    if (publishRule.getDataField() != null) {
                        jsonMap.put(publishRule.getDataField(), JValue.newObject(dataMap));
                    } else {
                        jsonMap.putAll(dataMap);
                    }
                    
                    LocalDateTime localDateTime = LocalDateTime.ofEpochSecond((int)(timestamp/1000), (int)((timestamp%1000)*1000*1000), ZoneOffset.UTC);
                    ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneOffset.UTC);
                    jsonMap.putIfAbsent("timestamp", JValue.newString(DateTimeFormatter.ISO_INSTANT.format(zonedDateTime)));

                    jsonMap.putIfAbsent("eoj", JValue.newString(eoj.toString()));
                    jsonMap.putIfAbsent("class_eoj", JValue.newString(eoj.getClassEOJ().toString()));
                    jsonMap.putIfAbsent("class_group_code", JValue.newNumber(0x00ff & eoj.getClassGroupCode()));
                    jsonMap.putIfAbsent("class_code", JValue.newNumber(0x00ff & eoj.getClassCode()));
                    jsonMap.putIfAbsent("instance_code", JValue.newNumber(0x00ff & eoj.getInstanceCode()));
                    jsonMap.putIfAbsent("node", JValue.newString(node.toString()));
                    jsonMap.putIfAbsent("method", JValue.newString("get"));
                    jsonMap.putIfAbsent("interval", JValue.newNumber(publishRule.getInterval()));
                    
                    HashMap<String, JValue> requestJsonMap = new HashMap<String, JValue>();
                    if (publishRule.getNode() != null) {
                        requestJsonMap.put("node", JValue.newString(publishRule.getNode()));
                    }
                    requestJsonMap.put("eoj", JValue.newString(publishRule.getEOJ().toString()));
                    jsonMap.putIfAbsent("request", JValue.newObject(requestJsonMap));
                    
                    try {
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
                                    logger.logp(Level.INFO, className, "PublishTimerTask.publish", "catched exception", ex);
                                }
                                
                                return false;
                            }
                        };
                        
                        for (PublishTopic publishTopic: publishRule.getPublishTopics(selector)) {
                            HashMap<String, JValue> eachJsonMap = new HashMap<String, JValue>(jsonMap);

                            String topic = publishTopic.getTopic()
                                    .replaceAll("\\[NODE\\]", node.toString())
                                    .replaceAll("\\[EOJ\\]", eoj.toString())
                                    .replaceAll("\\[CLASS_EOJ\\]", eoj.getClassEOJ().toString())
                                    .replaceAll("\\[CLASS_CODE\\]", String.format("%02x", 0x00ff & eoj.getClassCode()))
                                    .replaceAll("\\[CLASS_GROUP_CODE\\]", String.format("%02x", 0x00ff & eoj.getClassGroupCode()))
                                    .replaceAll("\\[INSTANCE_CODE\\]", String.format("%02x", 0x00ff & eoj.getInstanceCode()))
                                    .replaceAll("\\[METHOD\\]", "get")
                                    .replace('.', '_');
                            JObject jobject = JValue.newObject(eachJsonMap);
                            mqttManager.publish(topic, jobject);
                            published.add(publishTopic);
                        }
                        
                    } catch (PublisherException ex) {
                        logger.logp(Level.INFO, className, "PublishTimerTask.publish", "catched exception", ex);
                    } catch (JsonEncoderException ex) {
                        logger.logp(Level.INFO, className, "PublishTimerTask.publish", "catched exception", ex);
                    }
                    
                    logger.exiting(className, "PublishTimerTask.publish");
                }

                @Override
                public void finish(GetResult result) {
                    logger.entering(className, "PublishTimerTask.finish", result);
                    
                    if (!working) {
                        logger.logp(Level.INFO, className, "PublishTimerTask.finish", "not working");
                        logger.exiting(className, "PublishTimerTask.finish");
                        return;
                    }
                    
                    if (result.countFrames() == 0) {
                        logger.logp(Level.INFO, className, "PublishTimerTask.finish", "no response: " + node + " " + eoj + " " + epcs);
                        logger.exiting(className, "PublishTimerTask.finish");
                        return;
                    } else {
                        for (PublishTopic publishTopic: publishRule.getPublishTopics()) {
                            if (!published.contains(publishTopic)) {
                                logger.logp(Level.INFO, className, "PublishTimerTask.finish", "no response: " + node + " " + eoj + " " + epcs + " (" + publishTopic + ")");
                                logger.exiting(className, "PublishTimerTask.finish");
                                return;
                            }
                        }
                    }
                    
                    logger.exiting(className, "PublishTimerTask.finish");
                }
            });
        }

        @Override
        public void run() {
            logger.entering(className, "PublishTimerTask.run");
                    
            try {
                doGet();
            } catch (SubnetException ex) {
                logger.logp(Level.INFO, className, "PublishTimerTask.run", "catched exception", ex);
            }
            
            logger.exiting(className, "PublishTimerTask.run");
        }
    
    }
    
    public synchronized boolean start() {
        logger.entering(className, "start");
            
        if (working) {
            logger.exiting(className, "start", false);
            return false;
        }
        
        timer = new Timer();
        TimerTask task = new PublishTimerTask();
        timer.scheduleAtFixedRate(task, publishRule.getDelay(), publishRule.getInterval());
        
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
        
        timer.cancel();
        timer = null;
        
        working = false;
        
        logger.exiting(className, "cancel", true);
        return true;
    }
}
