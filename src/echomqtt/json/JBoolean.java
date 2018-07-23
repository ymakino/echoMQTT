package echomqtt.json;

import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class JBoolean extends JValue {
    private static final Logger logger = Logger.getLogger(JBoolean.class.getName());
    private static final String className = JBoolean.class.getName();
    
    private boolean value;
    
    protected JBoolean(boolean value) {
        logger.entering(className, "JBoolean");
        
        this.value = value;
        
        logger.exiting(className, "JBoolean");
    }
    
    public boolean getValue() {
        logger.entering(className, "getValue");
        
        logger.exiting(className, "getValue", value);
        return value;
    }
    
    @Override
    public boolean isBoolean() {
        logger.entering(className, "isBoolean");
        
        logger.exiting(className, "isBoolean", true);
        return true;
    }
    
    @Override
    public String toString() {
        return Boolean.toString(value);
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof JBoolean)) {
            return false;
        }
        
        JBoolean valueBoolean = (JBoolean)object;
        
        return valueBoolean.getValue() == getValue();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.value ? 1 : 0);
        return hash;
    }
}
