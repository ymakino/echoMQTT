package echomqtt;

import echowand.common.EOJ;
import echowand.util.Collector;
import echowand.util.Selector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class PublishRule {
    private static final Logger logger = Logger.getLogger(PublishRule.class.getName());
    private static final String className = PublishRule.class.getName();
    
    private String node;
    private EOJ eoj;
    private int interval;
    private int delay;
    private List<PublishTopic> publishTopics;
    private List<PropertyRule> propertyRules;
    private HashMap<String, String> addition;
    private boolean getEnabled;
    private boolean notifyEnabled;
    private String dataField = "data";
    
    public PublishRule(String node, EOJ eoj, int interval, int delay, Collection<PublishTopic> publishTopics, Collection<PropertyRule> propertieRules, HashMap<String, String> addition, boolean getEnabled, boolean notifyEnabled) {
        this.node = node;
        this.eoj = eoj;
        this.interval = interval;
        this.delay = delay;
        this.publishTopics = new LinkedList<PublishTopic>(publishTopics);
        this.propertyRules = new LinkedList<PropertyRule>(propertieRules);
        this.addition = addition;
        this.getEnabled = getEnabled;
        this.notifyEnabled = notifyEnabled;
    }
    
    public PublishRule(EOJ eoj, int interval, Collection<PublishTopic> publishTopics, Collection<PropertyRule> propertieRules, HashMap<String, String> addition, boolean getEnabled, boolean notifyEnabled) {
        this.node = null;
        this.eoj = eoj;
        this.interval = interval;
        this.delay = delay;
        this.publishTopics = new LinkedList<PublishTopic>(publishTopics);
        this.propertyRules = new LinkedList<PropertyRule>(propertieRules);
        this.addition = addition;
        this.getEnabled = getEnabled;
        this.notifyEnabled = notifyEnabled;
    }
    
    public String getNode() {
        return node;
    }
    
    public EOJ getEOJ() {
        return eoj;
    }
    
    public int getInterval() {
        return interval;
    }
    
    public int getDelay() {
        return delay;
    }
    
    public String getDataField() {
        return dataField;
    }
    
    public int countPublishTopics() {
        return publishTopics.size();
    }
    
    public PublishTopic getPublishTopicAt(int index) {
        return publishTopics.get(index);
    }
    
    public List<PublishTopic> getPublishTopics() {
        return new ArrayList<PublishTopic>(publishTopics);
    }
    
    public synchronized List<PublishTopic> getPublishTopics(Selector<? super PublishTopic> selector) {
        logger.entering(className, "getPublishTopics", selector);
        
        Collector<PublishTopic> collector = new Collector<PublishTopic>(selector);
        List<PublishTopic> objectList = collector.collect(new ArrayList<PublishTopic>(publishTopics));
        
        logger.exiting(className, "getPublishTopics", objectList);
        return objectList;
    }
    
    public int countPropertieRules() {
        return propertyRules.size();
    }
    
    public PropertyRule getPropertyRuleAt(int index) {
        return propertyRules.get(index);
    }
    
    public List<PropertyRule> getPropertyRules() {
        return new ArrayList<PropertyRule>(propertyRules);
    }
    
    public HashMap<String, String> getAddition() {
        return addition;
    }
    
    public boolean isGetEnabled() {
        return getEnabled;
    }
    
    public boolean isNotifyEnabled() {
        return notifyEnabled;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("PublishRule {");
        
        if (node != null) {
            builder.append("node: ").append(node).append(", ");
        }
        
        builder.append("eoj: ").append(eoj).append(", ");
        builder.append("interval: ").append(interval).append(", ");
        
        for (PublishTopic publishTopic: publishTopics) {
            builder.append("topic: ").append(publishTopic).append(", ");
        }
        
        builder.append("addition: ").append(addition).append(", ");
        builder.append("get: ").append(getEnabled).append(", ");
        builder.append("notify: ").append(notifyEnabled).append(", ");
        
        String sep = "";
        builder.append("propertyRules: [");
        for (PropertyRule propertyRule: propertyRules) {
            builder.append(sep).append(propertyRule);
            sep = ", ";
        }
        builder.append("]}");
        
        return builder.toString();
    }
}
