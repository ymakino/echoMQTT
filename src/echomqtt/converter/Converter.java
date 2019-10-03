package echomqtt.converter;

import echomqtt.json.JValue;
import echowand.common.Data;
import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class Converter {
    private static final Logger logger = Logger.getLogger(Converter.class.getName());
    private static final String className = Converter.class.getName();
    
    public int getSize() {
        return -1;
    }
    
    public Data pack(Data data) {
        if (getSize() < 0 || data.size() <= getSize()) {
            return data;
        }
        
        byte[] bytes = new byte[getSize()];

        for (int i=0; i<getSize(); i++) {
            bytes[i] = data.get(i);
        }

        return new Data(bytes);
    }
    
    public JValue convert(Data data) throws ConverterException {
        logger.entering(className, "convert", data);
        
        ConverterException exception = new ConverterException("unsupported conversion");
        logger.throwing(className, "convert", exception);
        throw exception;
    }
    
    public Data convert(JValue jvalue) throws ConverterException {
        logger.entering(className, "convert", jvalue);
        
        ConverterException exception = new ConverterException("unsupported conversion");
        logger.throwing(className, "convert", exception);
        throw exception;
    }
}
