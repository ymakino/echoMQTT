package echomqtt.json;

import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class JsonNull extends JsonValue {
    private static final Logger logger = Logger.getLogger(JsonNull.class.getName());
    private static final String className = JsonNull.class.getName();
    
    public JsonNull() {
        logger.entering(className, "JsonNull");
        
        logger.exiting(className, "JsonNull");
    }
    
    public Object getValue() {
        logger.entering(className, "getValue");
        
        logger.exiting(className, "getValue", null);
        return null;
    }
    
    @Override
    public boolean isNull() {
        logger.entering(className, "isNull");
        
        logger.exiting(className, "isNull", true);
        return true;
    }
    
    @Override
    public String toString() {
        return "null";
    }
    
    @Override
    public boolean equals(Object object) {
        return object instanceof JsonNull;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }
}
