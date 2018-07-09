package echomqtt;

import echowand.logic.TooManyObjectsException;
import echowand.net.Inet4Subnet;
import echowand.net.SubnetException;
import echowand.service.Core;
import echowand.service.Service;
import echowand.util.LoggerConfig;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import echomqtt.json.JsonDecoder;
import org.xml.sax.SAXException;

/**
 *
 * @author ymakino
 */
public class EchoMQTT {
    private static final Logger logger = Logger.getLogger(EchoMQTT.class.getName());
    private static final String className = EchoMQTT.class.getName();
    
    private Service service;
    private String broker;
    private String clientId;
    private MQTTManager mqttManager;
    private LinkedList<Rules> rulesList;
    private boolean working;
    
    private LinkedList<PublishTask> publishTasks;
    private LinkedList<SubscribeTask> subscribeTasks;
    
    public EchoMQTT(Core core, String broker, String clientId) throws PublisherException {
        logger.entering(className, "Echobridge", new Object[]{core, broker, clientId});
        
        service = new Service(core);
        this.broker = broker;
        this.clientId = clientId;
        mqttManager = new MQTTManager(broker, clientId);
        rulesList = new LinkedList<Rules>();
        working = false;
        
        logger.exiting(className, "Echobridge");
    }
    
    public PublishTask createPublishTask(PublishRule rule) throws SubnetException {
        logger.entering(className, "createPublishTask", rule);
        
        PublishTask task = new PublishTask(service, mqttManager, rule);
        
        logger.exiting(className, "createPublishTask", task);
        return task;
    }
    
    private void startPublishTask(Rules rules) throws SubnetException {
        for (PublishRule publishRule: rules.getPublishRules()) {
            PublishTask publishTask = new PublishTask(service, mqttManager, publishRule);
            if (publishTask.start()) {
                publishTasks.add(publishTask);
            }
        }
    }
    
    private void startSubscribeTask(Rules rules) {
        for (SubscribeRule subscribeRule: rules.getSubscribeRules()) {
            SubscribeTask subscribeTask = new SubscribeTask(service, mqttManager, subscribeRule);
            if (subscribeTask.start()) {
                subscribeTasks.add(subscribeTask);
            }
        }
    }
    
    public synchronized boolean start() throws SubnetException, PublisherException {
        logger.entering(className, "start");
        
        if (working) {
            logger.exiting(className, "start", false);
            return false;
        }
        
        mqttManager.connect(true);
        
        publishTasks = new LinkedList<PublishTask>();
        subscribeTasks = new LinkedList<SubscribeTask>();
        
        for (Rules rules : rulesList) {
            startPublishTask(rules);
        }
        
        for (Rules rules : rulesList) {
            startSubscribeTask(rules);
        }
        
        working = true;
        
        logger.exiting(className, "start", true);
        return true;
    }
    
    public synchronized boolean cancel() throws PublisherException {
        logger.entering(className, "cancel");
        
        if (!working) {
            logger.exiting(className, "cancel", false);
            return false;
        }
        
        for (PublishTask publishTask : publishTasks) {
            publishTask.cancel();
        }
        
        for (SubscribeTask subscribeTask : subscribeTasks) {
            subscribeTask.cancel();
        }
        
        mqttManager.disconnect();
        
        working = false;
        
        logger.exiting(className, "cancel");
        return true;
    }
    
    public synchronized void addRules(Rules rules) {
        logger.entering(className, "addRule", rules);
        
        rulesList.add(rules);
                
        logger.exiting(className, "addRule");
    }
    
    public static void showUsage(String name) {
        System.out.println("Usage: " + name + " [ -i interface ] [ -b broker ] [ -c clientId ] [ xmlfile... ]");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SubnetException, TooManyObjectsException, SocketException, IOException, PublisherException, ParserConfigurationException, SAXException, InterruptedException {
        String broker = "tcp://127.0.0.1:1883";
        String clientId = "echobridge";
        NetworkInterface nif = null;
        
        LinkedList<String> ruleFiles = new LinkedList<String>();
        
        // LoggerConfig.changeLogLevelAll(RulesParser.class.getName());
        // LoggerConfig.changeLogLevelAll(PublishTask.class.getName());
        // LoggerConfig.changeLogLevelAll(JsonDecoder.class.getName());
        // LoggerConfig.changeLogLevelAll(echomqtt.converter.Map.class.getName());
        // LoggerConfig.changeLogLevelAll(echomqtt.converter.Float.class.getName());
        // LoggerConfig.changeLogLevelAll(echomqtt.converter.Integer.class.getName());
        
        for (int i=0; i<args.length; i++) {
            switch (args[i]) {
                case "-h":
                    showUsage("echobridge");
                    return;
                case "-i":
                    if (args[++i].equals("-")) {
                        nif = NetworkInterfaceSelector.select();
                    } else {
                        nif = NetworkInterface.getByName(args[i]);
                    }
                    break;
                case "-c":
                    clientId = args[++i];
                    break;
                case "-b":
                    broker = args[++i];
                    break;
                case "-x":
                    ruleFiles.add(args[++i]);
                    break;
                default:
                    ruleFiles.add(args[i]);
                    break;
            }
        }
        
        if (ruleFiles.size() == 0) {
            showUsage("echobridge");
            return;
        }
        
        Core core;
        
        if (nif == null) {
            core = new Core();
        } else {
            core = new Core(new Inet4Subnet(nif));
        }
        
        core.startService();
        
        EchoMQTT echomqtt = new EchoMQTT(core, broker, clientId);
        
        RulesParser parser = new RulesParser();
        
        for (String ruleFile : ruleFiles) {
            echomqtt.addRules(parser.parseXMLFile(ruleFile));
        }
        
        echomqtt.start();
    }
    
}
