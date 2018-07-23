package echomqtt;

import echowand.common.EOJ;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author ymakino
 */
public class PublishRule {
    private String address;
    private EOJ eoj;
    private int interval;
    private String topic;
    private List<PropertyRule> propertyRules;
    private boolean getEnabled;
    private boolean notifyEnabled;
    
    public PublishRule(String address, EOJ eoj, int interval, String topic, Collection<PropertyRule> propertieRules, boolean getEnabled, boolean notifyEnabled) {
        this.address = address;
        this.eoj = eoj;
        this.interval = interval;
        this.topic = topic;
        this.propertyRules = new LinkedList<PropertyRule>(propertieRules);
        this.getEnabled = getEnabled;
        this.notifyEnabled = notifyEnabled;
    }
    
    public String getAddress() {
        return address;
    }
    
    public EOJ getEOJ() {
        return eoj;
    }
    
    public int getInterval() {
        return interval;
    }
    
    public String getTopic() {
        return topic;
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
        builder.append("address: ").append(address).append(", ");
        builder.append("eoj: ").append(eoj).append(", ");
        builder.append("interval: ").append(interval).append(", ");
        builder.append("topic: ").append(topic).append(", ");
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
