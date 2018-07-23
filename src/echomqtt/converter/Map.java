package echomqtt.converter;

import echowand.common.Data;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import echomqtt.json.JsonDecoderException;
import echomqtt.json.JObject;
import echomqtt.json.JString;
import echomqtt.json.JValue;

/**
 *
 * @author ymakino
 */
public class Map extends Converter {
    private static final Logger logger = Logger.getLogger(Map.class.getName());
    private static final String className = Map.class.getName();
    
    private boolean caseInsensitive = true;
    
    private HashMap<String, JValue> mapping;
    
    private Data newData(String s) throws ConverterException {
        if (s.length() == 0 || (s.length() % 2) == 1) {
            ConverterException exception = new ConverterException("invalid data: " + s);
            logger.throwing(className, "newData", exception);
            throw exception;
        }
        
        byte[] bytes = new byte[s.length() / 2];
        
        try {
            for (int i=0; i<bytes.length; i++) {
                String d = s.substring(i*2, i*2 + 2);
                bytes[i] = Byte.parseByte(d, 16);
            }
        } catch (NumberFormatException ex) {
            ConverterException exception = new ConverterException("invalid data: " + s, ex);
            logger.throwing(className, "newData", exception);
            throw exception;
        }
        
        return new Data(bytes);
    }
    
    private void parseMapping(String param) throws ConverterException {
        logger.entering(className, "parseMapping", param);
        
        JValue json;
        
        try {
            json = JValue.parseJSON(param);
        } catch (JsonDecoderException ex) {
            ConverterException exception = new ConverterException("invalid JSON format: " + param, ex);
            logger.throwing(className, "parseMapping", exception);
            throw exception;
        }
        
        JObject object = json.asObject();
        
        if (object == null) {
            ConverterException exception = new ConverterException("invalid format: " + param);
            logger.throwing(className, "parseMapping", exception);
            throw exception;
        }
        
        for (String name : object.getMembers()) {
            if (caseInsensitive) {
                mapping.put(name.toLowerCase(), object.get(name));
            } else {
                mapping.put(name, object.get(name));
            }
        }
        
        logger.exiting(className, "parseMapping");
    }
    
    public Map(HashMap<String, String> params) throws ConverterException {
        logger.entering(className, "Map", params);
        
        mapping = new HashMap<String, JValue>();
        
        String mappingString = null;
        
        for (String key : params.keySet()) {
            String value = params.get(key);
            
            switch (key) {
                case "mapping":
                    mappingString = value;
                    break;
                case "case-insensitive":
                    if (value.toLowerCase().equals("true")) {
                        caseInsensitive = true;
                    } else if (value.toLowerCase().equals("false")) {
                        caseInsensitive = false;
                    } else {
                        ConverterException exception = new ConverterException("invalid parameter: " + key + " = " + value);
                        logger.throwing(className, "Map", exception);
                        throw exception;
                    }
                    break;
                case "case-sensitive":
                    if (value.toLowerCase().equals("true")) {
                        caseInsensitive = false;
                    } else if (value.toLowerCase().equals("false")) {
                        caseInsensitive = true;
                    } else {
                        ConverterException exception = new ConverterException("invalid parameter: " + key + " = " + value);
                        logger.throwing(className, "Map", exception);
                        throw exception;
                    }
                    break;
                default:
                    ConverterException exception = new ConverterException("invalid parameter: " + key + " = " + value);
                    logger.throwing(className, "Map", exception);
                    throw exception;
            }
        }
        
        if (mappingString == null) {
            ConverterException exception = new ConverterException("no mapping");
            logger.throwing(className, "Map", exception);
            throw exception;
        }
        
        parseMapping(mappingString);
        
        logger.exiting(className, "Map");
    }

    @Override
    public JValue convert(Data data) throws ConverterException {
        logger.entering(className, "convert", data);
        
        JValue result = mapping.get(data.toString());
        
        if (result == null) {
            ConverterException exception = new ConverterException("no mapping: " + data);
            logger.throwing(className, "Map", exception);
            throw exception;
        }
        
        logger.exiting(className, "convert", result);
        return result;
    }

    @Override
    public Data convert(JValue jvalue) throws ConverterException {
        logger.entering(className, "convert", jvalue);
        
        if (!jvalue.isString()) {
            ConverterException exception = new ConverterException("invalid parameter: " + jvalue);
            logger.throwing(className, "Map", exception);
            throw exception;
        }
        
        String str = jvalue.asString().getValue();
        JValue dataValue;

        if (caseInsensitive) {
            dataValue = mapping.get(str.toLowerCase());
        } else {
            dataValue = mapping.get(str);
        }
        
        if (dataValue == null) {
            ConverterException exception = new ConverterException("no mapping: " + str);
            logger.throwing(className, "Map", exception);
            throw exception;
        }

        if (!dataValue.isString()) {
            ConverterException exception = new ConverterException("invalid value: " + dataValue);
            logger.throwing(className, "Map", exception);
            throw exception;
        }
        
        Data result = newData(dataValue.asString().getValue());
        logger.exiting(className, "convert", result);
        return result;
    }
    
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        String sep = "";
        builder.append("Map {");
        
        for(String s: mapping.keySet()) {
            builder.append(sep).append(s).append(": ").append(mapping.get(s));
            sep = ", ";
        }
        
        builder.append("}");
        
        return builder.toString();
    }
    
    public static void main(String[] args) throws ConverterException {
        HashMap<String, String> params1 = new HashMap<String, String>();
        params1.put("mapping", "{\"ON\":\"30\", \"OFF\":\"31\"}");
        params1.put("case-insensitive", "true");
        Map map1 = new Map(params1);
        
        HashMap<String, String> params2 = new HashMap<String, String>();
        params2.put("mapping", "{\"30\":\"ON\", \"31\":\"OFF\"}");
        params2.put("case-insensitive", "true");
        Map map2 = new Map(params2);
        
        //System.out.println(map.convert(new Data(new byte[]{0x30})));
        //System.out.println(map.convert(new Data(new byte[]{0x31})));
        System.out.println(map1.convert(JValue.newString("ON")));
        System.out.println(map1.convert(JValue.newString("Off")));
        System.out.println(map2.convert(new Data((byte)0x30)));
        System.out.println(map2.convert(new Data((byte)0x31)));
    }
}
