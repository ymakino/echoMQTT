package echomqtt.json;

import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class JString extends JValue {
    private static final Logger logger = Logger.getLogger(JNumber.class.getName());
    private static final String className = JNumber.class.getName();
    
    private String value;
    
    protected JString(String value) {
        logger.entering(className, "JString", value);
        
        this.value = value;
        
        logger.exiting(className, "JString");
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
        if (!(object instanceof JString)) {
            return false;
        }
        
        JString valueString = (JString)object;
        
        return valueString.getValue().equals(getValue());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }
}
