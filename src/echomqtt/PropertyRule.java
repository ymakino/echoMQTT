package echomqtt;

import echomqtt.converter.Converter;
import echowand.common.EPC;

/**
 *
 * @author ymakino
 */
public class PropertyRule {
    private EPC epc;
    private Converter converter;
    private String name;
    
    public PropertyRule(EPC epc, String name, Converter converter) {
        this.epc = epc;
        this.name = name;
        this.converter = converter;
    }
    
    public EPC getEPC() {
        return epc;
    }
    
    public Converter getConverter() {
        return converter;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return "{epc: " + epc + ", name: " + name + ", converter: " + converter + "}";
    }
}
