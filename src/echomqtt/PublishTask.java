package echomqtt;

import echomqtt.converter.ConverterException;
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
public class PublishTask {
    private static final Logger logger = Logger.getLogger(PublishTask.class.getName());
    private static final String className = PublishTask.class.getName();
    
    private Service service;
    private MQTTManager mqttManager;
    private PublishRule rule;
    private Node node;
    private EOJ eoj;
    private LinkedList<EPC> epcs;
    private Timer timer;
    private int timeout;
    
    private boolean working = false;
    
    public PublishTask(Service service, MQTTManager mqttManager, PublishRule rule) throws SubnetException {
        logger.entering(className, "PublishTask", new Object[]{service, mqttManager, rule});
        
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
        
        logger.exiting(className, "PublishTask");
    }
    
    private class PublishTimerTask extends TimerTask {
        
        private void doGet() throws SubnetException {
            logger.entering(className, "PublishTimerTask.doGet", new Object[]{service, mqttManager, rule});
        
            service.doGet(node, eoj, epcs, timeout, new GetListener() {
                HashMap<String, String> jsonMap = new HashMap<String, String>();

                private boolean generate(EPC epc, Data data) throws ConverterException {
                    logger.entering(className, "PublishTimerTask.generate", new Object[]{epc, data});
                    
                    for (int i=0; i<rule.countPropertieRules(); i++) {
                        PropertyRule propertyRule = rule.getPropertyRuleAt(i);
                        if (!data.isEmpty() && propertyRule.getEPC() == epc) {
                            String key = propertyRule.getName();
                            String value = propertyRule.getConverter().convertData(data);
                            jsonMap.put(key, value);
                        }
                    }
                    
                    boolean result = jsonMap.size() > 0;
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
                    
                    String delimiter = "";
                    StringBuilder builder = new StringBuilder("{");

                    for (String key: jsonMap.keySet()) {
                        builder.append(delimiter);
                        builder.append('"').append(key).append('"').append(':');
                        builder.append(jsonMap.get(key));
                        delimiter = ",";
                    }

                    builder.append("}");

                    try {
                        mqttManager.publish(rule, builder.toString());
                    } catch (PublisherException ex) {
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
