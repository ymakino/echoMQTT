package echomqtt.converter;

import echomqtt.json.JValue;
import echomqtt.json.JsonDecoderException;
import echowand.common.Data;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class Raw extends Converter {
    private static final Logger logger = Logger.getLogger(Raw.class.getName());
    private static final String className = Raw.class.getName();
    
    private int size = -1;
    
    public Raw() {
        logger.entering(className, "Raw");
        
        logger.exiting(className, "Raw");
    }
    
    public Raw(HashMap<String, String> params) throws ConverterException {
        logger.entering(className, "Raw", params);
        
        for (String key : params.keySet()) {
            String value = params.get(key);
            
            switch (key) {
                case "size":
                    size = java.lang.Integer.parseInt(value);
                    break;
                default:
                    ConverterException exception = new ConverterException("invalid parameter: " + key + " = " + value);
                    logger.throwing(className, "Integer", exception);
                    throw exception;
            }
        }
        
        logger.exiting(className, "Raw");
    }
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public JValue convert(Data data) {
        logger.entering(className, "convert", data);
        
        JValue result = JValue.newString(pack(data).toString());
        
        logger.exiting(className, "convert", result);
        return result;
    }

    @Override
    public Data convert(JValue jvalue) throws ConverterException {
        logger.entering(className, "convert", jvalue);
        
        if (!jvalue.isString()) {
            ConverterException exception = new ConverterException("invalid value: " + jvalue);
            logger.throwing(className, "convert", exception);
            throw exception;
        }
        
        try {
            String str = jvalue.asString().getValue().trim();

            if (str.startsWith("0x") || str.startsWith("0X")) {
                str = str.substring(2);
            }

            if (str.length() % 2 != 0) {
                ConverterException exception = new ConverterException("invalid length: " + str.length() + " (" + jvalue + ")");
                logger.throwing(className, "convert", exception);
                throw exception;
            }

            byte[] bytes = new byte[str.length() / 2];

            for (int i=0; i<str.length(); i+=2) {
                String b = str.substring(i, i+2);
                bytes[i/2] = (byte)java.lang.Integer.parseInt(b, 16);
            }

            Data result = new Data(bytes);
            logger.exiting(className, "convert", result);
            return result;
        } catch (NumberFormatException ex) {
            ConverterException exception = new ConverterException("invalid format: " + jvalue, ex);
            logger.throwing(className, "convert", exception);
            throw exception;
        }
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("Raw");
        builder.append("{");
        builder.append("}");
        
        return builder.toString();
    }
    
    public static void main(String[] args) throws ConverterException, JsonDecoderException {
        Converter c1 = new Raw();
        
        Data d = c1.convert(JValue.parseJSON("\"112233445566778899aabbccddeeffAABBCCDDEEFF\""));
        System.out.println(d);
        System.out.println(c1.convert(d));
        System.out.println(c1.convert(JValue.parseJSON("\"112233445566778899aabbccddeeffAABBCCDDEEFF\"")));
    }
}
