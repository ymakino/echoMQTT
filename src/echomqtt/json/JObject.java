package echomqtt.json;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class JObject extends JValue {
    private static final Logger logger = Logger.getLogger(JObject.class.getName());
    private static final String className = JObject.class.getName();
    
    private HashMap<String, JValue> valueMap;
    
    protected JObject(Map<String, ? extends JValue> valueMap) {
        logger.entering(className, "JObject", valueMap);
        
        this.valueMap = new HashMap<String, JValue>(valueMap);
        
        logger.exiting(className, "JObject");
    }
    
    public Set<String> getMembers() {
        logger.entering(className, "getMembers");
        
        Set<String> result = valueMap.keySet();
        logger.exiting(className, "getMembers", result);
        return result;
    }
    
    public boolean contains(String name) {
        return valueMap.containsKey(name);
    }
    
    public JValue get(String name) {
        return valueMap.get(name);
    }
    
    public JObject alterAdd(String name, JValue value) {
        logger.entering(className, "alterAdd", new Object[]{name, value});
        
        HashMap<String, JValue> newValueMap = new HashMap<String, JValue>(valueMap);
        newValueMap.put(name, value);
        JObject resultValue = new JObject(newValueMap);
        
        logger.exiting(className, "alterAdd", resultValue);
        return resultValue;
    }
    
    public JObject alterAdd(Map<String, ? extends JValue> nameValueMap) {
        logger.entering(className, "alterAdd", nameValueMap);
        
        HashMap<String, JValue> newValueMap = new HashMap<String, JValue>(valueMap);
        newValueMap.putAll(nameValueMap);
        JObject resultValue = new JObject(newValueMap);
        
        logger.exiting(className, "alterAdd", resultValue);
        return resultValue;
    }
    
    public JObject alterRemove(String name) {
        logger.entering(className, "alterRemove", name);
        
        HashMap<String, JValue> newValueMap = new HashMap<String, JValue>(valueMap);
        newValueMap.remove(name);
        JObject resultValue = new JObject(newValueMap);
        
        logger.exiting(className, "alterRemove", resultValue);
        return resultValue;
    }
    
    public JObject alterRemove(Collection<String> names) {
        logger.entering(className, "alterRemove", names);

        HashMap<String, JValue> newValueMap = new HashMap<String, JValue>(valueMap);
        for (String name : names) {
            newValueMap.remove(name);
        }
        JObject resultValue = new JObject(newValueMap);

        logger.exiting(className, "alterRemove", resultValue);
        return resultValue;
    }
    
    @Override
    public boolean isObject() {
        return true;
    }
    
    @Override
    public String toString() {
        boolean isFirst = true;
        
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (String key : getMembers()) {
            if (isFirst) {
                isFirst = false;
            } else {
                builder.append(", ");
            }
            builder.append(key);
            builder.append(": ");
            builder.append(valueMap.get(key));
        }
        builder.append("}");
        return builder.toString();
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof JObject)) {
            return false;
        }
        
        JObject jsonObject = (JObject)object;
        
        if (!getMembers().equals(jsonObject.getMembers())) {
            return false;
        }
        
        for (String member : getMembers()) {
            if (!get(member).equals(jsonObject.get(member))) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }
}
