package echomqtt;

import echomqtt.converter.ConverterException;
import echomqtt.json.JObject;
import echomqtt.json.JString;
import echomqtt.json.JValue;
import echomqtt.json.JsonEncoderException;
import echowand.common.Data;
import echowand.common.EOJ;
import echowand.common.EPC;
import echowand.net.Node;
import echowand.net.SubnetException;
import echowand.service.Service;
import echowand.service.result.GetListener;
import echowand.service.result.GetResult;
import echowand.service.result.ResultData;
import echowand.service.result.ResultFrame;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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
    
    private Service service;
    private MQTTManager mqttManager;
    private PublishRule rule;
    private Node node;
    private EOJ eoj;
    private LinkedList<EPC> epcs;
    private Timer timer;
    private int timeout;
    
    private boolean working = false;
    
    public GetTask(Service service, MQTTManager mqttManager, PublishRule rule) throws SubnetException {
        logger.entering(className, "GetTask", new Object[]{service, mqttManager, rule});
        
        this.service = service;
        this.mqttManager = mqttManager;
        this.rule = rule;
        
        timeout = 5000;
        
        node = service.getRemoteNode(rule.getAddress());
        eoj = rule.getEOJ();
        
        epcs = new LinkedList<EPC>();
        for (int i=0; i<rule.countPropertieRules(); i++) {
            PropertyRule propertyRule = rule.getPropertyRuleAt(i);
            EPC epc = propertyRule.getEPC();
            if (!epcs.contains(epc)) {
                epcs.add(epc);
            }
        }
        
        logger.exiting(className, "GetTask");
    }
    
    private class PublishTimerTask extends TimerTask {
        
        private void doGet() throws SubnetException {
            logger.entering(className, "PublishTimerTask.doGet", new Object[]{service, mqttManager, rule});
        
            service.doGet(node, eoj, epcs, timeout, new GetListener() {
                HashMap<String, JValue> jsonMap = new HashMap<String, JValue>();

                private boolean generate(EPC epc, Data data) throws ConverterException {
                    logger.entering(className, "PublishTimerTask.generate", new Object[]{epc, data});
                    
                    boolean result = false;
                    
                    for (int i=0; i<rule.countPropertieRules(); i++) {
                        PropertyRule propertyRule = rule.getPropertyRuleAt(i);
                        if (!data.isEmpty() && propertyRule.getEPC() == epc) {
                            String key = propertyRule.getName();
                            JValue value = propertyRule.getConverter().convert(data);
                            jsonMap.put(key, value);
                            result = true;
                        }
                    }
                    
                    logger.exiting(className, "PublishTimerTask.generate", result);
                    return result;
                }

                @Override
                public void receive(GetResult result, ResultFrame resultFrame) {
                    logger.entering(className, "PublishTimerTask.receive", new Object[]{result, resultFrame});
                    
                    try {
                        List<ResultData> resultDataList = result.getDataList(resultFrame, true);
                        
                        for (ResultData resultData: resultDataList) {
                            generate(resultData.getEPC(), resultData.getActualData());
                        }

                        if (jsonMap.size() == rule.countPropertieRules()) {
                            result.finish();
                        }
                    } catch (ConverterException ex) {
                        logger.logp(Level.INFO, className, "PublishTimerTask.receive", "catched exception", ex);
                    }
                    
                    logger.exiting(className, "PublishTimerTask.receive");
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
                        return;
                    }
                    
                    if (jsonMap.size() != rule.countPropertieRules()) {
                        logger.logp(Level.INFO, className, "PublishTimerTask.finish", "invalid data: " + node + " " + eoj + " " + epcs);
                        return;
                    }

                    if (!jsonMap.keySet().contains("timestamp")) {
                        LocalDateTime localDateTime = LocalDateTime.now();
                        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
                        jsonMap.put("timestamp", JValue.newString(DateTimeFormatter.ISO_INSTANT.format(zonedDateTime)));
                    }
                    
                    try {
                        JObject jobject = JValue.newObject(jsonMap);
                        mqttManager.publish(rule, jobject);
                    } catch (PublisherException ex) {
                        logger.logp(Level.INFO, className, "PublishTimerTask.finish", "catched exception", ex);
                    } catch (JsonEncoderException ex) {
                        logger.logp(Level.INFO, className, "PublishTimerTask.finish", "catched exception", ex);
                    }
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
        timer.scheduleAtFixedRate(task, 0, rule.getInterval());
        
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
