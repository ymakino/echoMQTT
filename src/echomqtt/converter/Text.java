package echomqtt.converter;

import echomqtt.json.JValue;
import echomqtt.json.JsonDecoderException;
import echowand.common.Data;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class Text extends Converter {
    private static final Logger logger = Logger.getLogger(Text.class.getName());
    private static final String className = Text.class.getName();
    
    private String encoding = "UTF-8";
    private int size = -1;
    
    public Text(HashMap<String, String> params) throws ConverterException {
        logger.entering(className, "Text", params);
        
        for (String key : params.keySet()) {
            String value = params.get(key);
            
            switch (key) {
                case "size":
                    size = java.lang.Integer.parseInt(value);
                    break;
                case "encoding":
                    encoding = value;
                    break;
                default:
                    ConverterException exception = new ConverterException("invalid parameter: " + key + " = " + value);
                    logger.throwing(className, "Integer", exception);
                    throw exception;
            }
        }
        
        logger.exiting(className, "Text");
    }
    
    @Override
    public int getSize() {
        return size;
    }
    
    public String getEncoding() {
        return encoding;
    }
    
    private byte[] toTrimmedBytes(Data data) {
        logger.entering(className, "toTrimmedBytes", data);
        
        byte[] bytes = data.toBytes();
        
        for (int i=0; i<bytes.length; i++) {
            if (bytes[i] == 0x00) {
                byte[] trimmed = new byte[i];
                System.arraycopy(bytes, 0, trimmed, 0, i);
                bytes = trimmed;
                break;
            }
        }
        
        logger.exiting(className, "toTrimmedBytes", bytes);
        return bytes;
    }
    
    private Data toPaddedData(byte[] bytes) {
        logger.entering(className, "toPaddedData", bytes);
        
        if (getSize() < 0 || bytes.length >= getSize()) {
            return new Data(bytes);
        }
        
        byte[] padded = new byte[getSize()];
        System.arraycopy(bytes, 0, padded, 0, bytes.length);
        
        Data result =  new Data(padded);
        logger.exiting(className, "toPaddedData", result);
        return result;
    }
    
    @Override
    public JValue convert(Data data) throws ConverterException {
        logger.entering(className, "convert", data);
        
        try {
            JValue result = JValue.newString(new String(toTrimmedBytes(data), encoding));
            logger.exiting(className, "convert", result);
            return result;
        } catch (UnsupportedEncodingException ex) {
            ConverterException exception = new ConverterException("invalid encoding: " + encoding, ex);
            logger.throwing(className, "convert", exception);
            throw exception;
        }
        
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
            String str = jvalue.asString().getValue();
            Data result = toPaddedData(str.getBytes(encoding));
            if (getSize() > 0 && result.size() > getSize()) {
                ConverterException exception = new ConverterException("too long text: " + str);
                logger.throwing(className, "convert", exception);
                throw exception;
            }
            logger.exiting(className, "convert", result);
            return result;
        } catch (UnsupportedEncodingException ex) {
            ConverterException exception = new ConverterException("invalid encoding: " + encoding, ex);
            logger.throwing(className, "convert", exception);
            throw exception;
        }
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("Text");
        builder.append("{");
        builder.append("size: ").append(size).append(", ");
        builder.append("encoding: ").append(encoding);
        builder.append("}");
        
        return builder.toString();
    }
    
    public static void main(String[] args) throws ConverterException, JsonDecoderException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("size", "12");
        map.put("encoding", "utf-8");
        Converter c1 = new Text(map);
        
        Data d = c1.convert(JValue.parseJSON("\"AIUEO\""));
        System.out.println(d);
        System.out.println(c1.convert(d));
        System.out.println(c1.convert(JValue.parseJSON("\"AIUEO\"")));
        
        Data d1 = new Data(new byte[]{0x53, 0x43, 0x44, 0x34, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        System.out.println(c1.convert(d1));
        
        System.out.println(c1);
    }
}
