package echomqtt.json;

import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class JsonString extends JsonValue {
    private static final Logger logger = Logger.getLogger(JsonNumber.class.getName());
    private static final String className = JsonNumber.class.getName();
    
    private String value;
    
    protected JsonString(String value) {
        logger.entering(className, "JsonString", value);
        
        this.value = value;
        
        logger.exiting(className, "JsonString");
    }
    
    public String getValue() {
        logger.entering(className, "getValue");
        
        logger.exiting(className, "getValue", value);
        return value;
    }
    
    @Override
    public boolean isString() {
        logger.entering(className, "isString");
        
        logger.exiting(className, "isString", true);
        return true;
    }
    
    @Override
    public String toString() {
        return value;
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof JsonString)) {
            return false;
        }
        
        JsonString valueString = (JsonString)object;
        
        return valueString.getValue().equals(getValue());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }
}
