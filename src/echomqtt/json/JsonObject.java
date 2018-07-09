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
public class JsonObject extends JsonValue {
    private static final Logger logger = Logger.getLogger(JsonObject.class.getName());
    private static final String className = JsonObject.class.getName();
    
    private HashMap<String, JsonValue> valueMap;
    
    protected JsonObject(Map<String, ? extends JsonValue> valueMap) {
        logger.entering(className, "JsonObject", valueMap);
        
        this.valueMap = new HashMap<String, JsonValue>(valueMap);
        
        logger.exiting(className, "JsonObject");
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
    
    public JsonValue get(String name) {
        return valueMap.get(name);
    }
    
    public JsonObject alterAdd(String name, JsonValue value) {
        logger.entering(className, "alterAdd", new Object[]{name, value});
        
        HashMap<String, JsonValue> newValueMap = new HashMap<String, JsonValue>(valueMap);
        newValueMap.put(name, value);
        JsonObject resultValue = new JsonObject(newValueMap);
        
        logger.exiting(className, "alterAdd", resultValue);
        return resultValue;
    }
    
    public JsonObject alterAdd(Map<String, ? extends JsonValue> nameValueMap) {
        logger.entering(className, "alterAdd", nameValueMap);
        
        HashMap<String, JsonValue> newValueMap = new HashMap<String, JsonValue>(valueMap);
        newValueMap.putAll(nameValueMap);
        JsonObject resultValue = new JsonObject(newValueMap);
        
        logger.exiting(className, "alterAdd", resultValue);
        return resultValue;
    }
    
    public JsonObject alterRemove(String name) {
        logger.entering(className, "alterRemove", name);
        
        HashMap<String, JsonValue> newValueMap = new HashMap<String, JsonValue>(valueMap);
        newValueMap.remove(name);
        JsonObject resultValue = new JsonObject(newValueMap);
        
        logger.exiting(className, "alterRemove", resultValue);
        return resultValue;
    }
    
    public JsonObject alterRemove(Collection<String> names) {
        logger.entering(className, "alterRemove", names);

        HashMap<String, JsonValue> newValueMap = new HashMap<String, JsonValue>(valueMap);
        for (String name : names) {
            newValueMap.remove(name);
        }
        JsonObject resultValue = new JsonObject(newValueMap);

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
        if (!(object instanceof JsonObject)) {
            return false;
        }
        
        JsonObject jsonObject = (JsonObject)object;
        
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
