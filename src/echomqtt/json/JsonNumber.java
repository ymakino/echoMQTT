package echomqtt.json;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class JsonNumber extends JsonValue {
    private static final Logger logger = Logger.getLogger(JsonNumber.class.getName());
    private static final String className = JsonNumber.class.getName();
    
    private BigDecimal value;
    
    protected JsonNumber(long value) {
        logger.entering(className, "JsonNumber", value);
        
        this.value = BigDecimal.valueOf(value);
        
        logger.exiting(className, "JsonNumber");
    }
    
    protected JsonNumber(double value) {
        logger.entering(className, "JsonNumber", value);
        
        this.value = BigDecimal.valueOf(value);
        
        logger.exiting(className, "JsonNumber");
    }
    
    protected JsonNumber(String value) {
        logger.entering(className, "JsonNumber", value);
        
        this.value = new BigDecimal(value);
        
        logger.exiting(className, "JsonNumber");
    }
    
    protected JsonNumber(BigDecimal value) {
        logger.entering(className, "JsonNumber", value);
        
        this.value = value;
        
        logger.exiting(className, "JsonNumber");
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
        if (!(object instanceof JsonNumber)) {
            return false;
        }
        
        JsonNumber value = (JsonNumber)object;
        
        return value.getValue().equals(getValue());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.value);
        return hash;
    }
}
