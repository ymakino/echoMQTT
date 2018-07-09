package echomqtt.json;

/**
 *
 * @author ymakino
 */
public class JsonBoolean extends JsonValue {
    private boolean value;
    
    protected JsonBoolean(boolean value) {
        this.value = value;
    }
    
    public boolean getValue() {
        return value;
    }
    
    @Override
    public boolean isBoolean() {
        return true;
    }
    
    @Override
    public String toString() {
        return Boolean.toString(value);
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof JsonBoolean)) {
            return false;
        }
        
        JsonBoolean valueBoolean = (JsonBoolean)object;
        
        return valueBoolean.getValue() == getValue();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.value ? 1 : 0);
        return hash;
    }
}
