package echomqtt.json;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class JNumber extends JValue {
    private static final Logger logger = Logger.getLogger(JNumber.class.getName());
    private static final String className = JNumber.class.getName();
    
    private BigDecimal value;
    
    protected JNumber(long value) {
        logger.entering(className, "JNumber", value);
        
        this.value = BigDecimal.valueOf(value);
        
        logger.exiting(className, "JNumber");
    }
    
    protected JNumber(double value) {
        logger.entering(className, "JNumber", value);
        
        this.value = BigDecimal.valueOf(value);
        
        logger.exiting(className, "JNumber");
    }
    
    protected JNumber(String value) {
        logger.entering(className, "JNumber", value);
        
        this.value = new BigDecimal(value);
        
        logger.exiting(className, "JNumber");
    }
    
    protected JNumber(BigDecimal value) {
        logger.entering(className, "JNumber", value);
        
        this.value = value;
        
        logger.exiting(className, "JNumber");
    }
    
    public BigDecimal getValue() {
        logger.entering(className, "getValue");
        
        logger.exiting(className, "getValue", value);
        return value;
    }
    
    @Override
    public boolean isNumber() {
        logger.entering(className, "isNumber");
        
        logger.exiting(className, "isNumber", true);
        return true;
    }
    
    @Override
    public String toString() {
        return value.toPlainString();
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof JNumber)) {
            return false;
        }
        
        JNumber value = (JNumber)object;
        
        return value.getValue().equals(getValue());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.value);
        return hash;
    }
}
