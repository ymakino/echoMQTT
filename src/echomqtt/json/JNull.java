package echomqtt.json;

import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class JNull extends JValue {
    private static final Logger logger = Logger.getLogger(JNull.class.getName());
    private static final String className = JNull.class.getName();
    
    public JNull() {
        logger.entering(className, "JNull");
        
        logger.exiting(className, "JNull");
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
        return object instanceof JNull;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }
}
