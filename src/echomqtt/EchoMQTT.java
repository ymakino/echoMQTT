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
import org.eclipse.paho.client.mqttv3.MqttClient;
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
    
    private LinkedList<GetTask> getTasks;
    private LinkedList<SetTask> setTasks;
    private LinkedList<ObserveTask> observeTasks;
    
    public EchoMQTT(Core core) throws PublisherException {
        logger.entering(className, "EchoMQTT", new Object[]{core, broker, clientId});
        
        service = new Service(core);
        this.broker = "tcp://localhost:1883";
        this.clientId = MqttClient.generateClientId();
        mqttManager = new MQTTManager(broker, clientId);
        rulesList = new LinkedList<Rules>();
        working = false;
        
        logger.exiting(className, "EchoMQTT");
    }
    
    public EchoMQTT(Core core, String broker) throws PublisherException {
        logger.entering(className, "EchoMQTT", new Object[]{core, broker, clientId});
        
        service = new Service(core);
        this.broker = broker;
        this.clientId = MqttClient.generateClientId();
        mqttManager = new MQTTManager(broker, clientId);
        rulesList = new LinkedList<Rules>();
        working = false;
        
        logger.exiting(className, "EchoMQTT");
    }
    
    public EchoMQTT(Core core, String broker, String clientId) throws PublisherException {
        logger.entering(className, "EchoMQTT", new Object[]{core, broker, clientId});
        
        service = new Service(core);
        this.broker = broker;
        this.clientId = clientId;
        mqttManager = new MQTTManager(broker, clientId);
        rulesList = new LinkedList<Rules>();
        working = false;
        
        logger.exiting(className, "EchoMQTT");
    }
    
    public void setPublishQoS(int publishQoS) {
        logger.entering(className, "setPublishQoS", publishQoS);
        
        mqttManager.setPublishQoS(publishQoS);
        
        logger.exiting(className, "setPublishQoS");
    }
    
    public GetTask createGetTask(PublishRule rule) throws SubnetException {
        logger.entering(className, "createGetTask", rule);
        
        GetTask task = new GetTask(service, mqttManager, rule);
        
        logger.exiting(className, "createGetTask", task);
        return task;
    }
    
    private void startGetTasks(Rules rules) throws SubnetException {
        for (PublishRule publishRule: rules.getPublishRules()) {
            if (!publishRule.isGetEnabled()) {
                continue;
            }
            
            GetTask getTask = new GetTask(service, mqttManager, publishRule);
            if (getTask.start()) {
                getTasks.add(getTask);
            }
        }
    }
    
    private void startSetTasks(Rules rules) {
        for (SubscribeRule subscribeRule: rules.getSubscribeRules()) {
            SetTask setTask = new SetTask(service, mqttManager, subscribeRule);
            if (setTask.start()) {
                setTasks.add(setTask);
            }
        }
    }
    
    private void startObserveTasks(Rules rules) throws SubnetException {
        logger.entering(className, "startObserveTask", rules);
        
        ObserveTask observeTask = new ObserveTask(service, mqttManager);
        
        for (PublishRule publishRule: rules.getPublishRules()) {
            if (!publishRule.isNotifyEnabled()) {
                continue;
            }
            
            observeTask.addRule(publishRule);
        }
        
        observeTask.start();
        observeTasks.add(observeTask);
        
        logger.exiting(className, "startObserveTask");
    }
    
    public synchronized boolean start() throws SubnetException, PublisherException {
        logger.entering(className, "start");
        
        if (working) {
            logger.exiting(className, "start", false);
            return false;
        }
        
        mqttManager.connect(true);
        
        getTasks = new LinkedList<GetTask>();
        setTasks = new LinkedList<SetTask>();
        observeTasks = new LinkedList<ObserveTask>();
        
        for (Rules rules : rulesList) {
            startGetTasks(rules);
        }
        
        for (Rules rules : rulesList) {
            startSetTasks(rules);
        }
        
        for (Rules rules : rulesList) {
            startObserveTasks(rules);
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
        
        for (GetTask getTask : getTasks) {
            getTask.cancel();
        }
        
        for (SetTask setTask : setTasks) {
            setTask.cancel();
        }
        
        for (ObserveTask observeTask : observeTasks) {
            observeTask.cancel();
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
        System.out.println("Usage: " + name + " [ -i interface ] [ -b broker ] [ -c clientId ] [ -q publishQoS ] xmlfile...");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SubnetException, TooManyObjectsException, SocketException, IOException, PublisherException, ParserConfigurationException, SAXException, InterruptedException {
        String broker = "tcp://127.0.0.1:1883";
        String clientId = MqttClient.generateClientId();
        int publishQoS = -1;
        NetworkInterface nif = null;
        
        LinkedList<String> ruleFiles = new LinkedList<String>();
        
        // LoggerConfig.changeLogLevelAll(RulesParser.class.getName());
        // LoggerConfig.changeLogLevelAll(ObserveTask.class.getName());
        // LoggerConfig.changeLogLevelAll(GetTask.class.getName());
        // LoggerConfig.changeLogLevelAll(JsonDecoder.class.getName());
        // LoggerConfig.changeLogLevelAll(SetTask.class.getName());
        // LoggerConfig.changeLogLevelAll(echomqtt.converter.Map.class.getName());
        // LoggerConfig.changeLogLevelAll(echomqtt.converter.Float.class.getName());
        // LoggerConfig.changeLogLevelAll(echomqtt.converter.Integer.class.getName());
        
        for (int i=0; i<args.length; i++) {
            switch (args[i]) {
                case "-h":
                    showUsage("EchoMQTT");
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
                case "-q":
                    publishQoS = Integer.parseInt(args[++i]);
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
            System.err.println("XML file(s) are required");
            showUsage("EchoMQTT");
            System.exit(1);
        }
        
        Core core;
        
        if (nif == null) {
            core = new Core();
        } else {
            core = new Core(new Inet4Subnet(nif));
        }
        
        core.startService();
        
        EchoMQTT echomqtt = new EchoMQTT(core, broker, clientId);
        
        if (publishQoS >= 0) {
            echomqtt.setPublishQoS(publishQoS);
        }
        
        RulesParser parser = new RulesParser();
        
        for (String ruleFile : ruleFiles) {
            Rules rules = parser.parseXMLFile(ruleFile);
            if (rules == null) {
                showUsage("EchoMQTT");
                System.exit(1);
            }
            echomqtt.addRules(parser.parseXMLFile(ruleFile));
        }
        
        echomqtt.start();
    }
    
}
