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
public class SubscribeRule {
    private String address;
    private EOJ eoj;
    private String topic;
    private List<PropertyRule> propertyRules;
    
    public SubscribeRule(String address, EOJ eoj, String topic, Collection<PropertyRule> propertieRules) {
        this.address = address;
        this.eoj = eoj;
        this.topic = topic;
        this.propertyRules = new LinkedList<PropertyRule>(propertieRules);
    }
    
    public String getAddress() {
        return address;
    }
    
    public EOJ getEOJ() {
        return eoj;
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
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("SubscribeRule {");
        builder.append("address: ").append(address).append(", ");
        builder.append("eoj: ").append(eoj).append(", ");
        builder.append("topic: ").append(topic).append(", ");
        
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
