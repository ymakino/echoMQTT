package echomqtt.converter;

import echowand.common.Data;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import echomqtt.json.JsonDecoderException;
import echomqtt.json.JsonObject;
import echomqtt.json.JsonString;
import echomqtt.json.JsonValue;

/**
 *
 * @author ymakino
 */
public class Map implements Converter {
    private static final Logger logger = Logger.getLogger(Map.class.getName());
    private static final String className = Map.class.getName();
    
    private boolean caseInsensitive = true;
    
    private HashMap<String, Data> dataMap;
    private HashMap<Data, String> stringMap;
    
    private Data d(String s) {
        byte[] bytes = new byte[s.length() / 2];
        
        for (int i=0; i<bytes.length; i++) {
            String d = s.substring(i*2, i*2 + 2);
            bytes[i] = Byte.parseByte(d, 16);
        }
        
        return new Data(bytes);
    }
    
    private void parseMapping(String param) throws ConverterException {
        logger.entering(className, "parseMapping", param);
        
        JsonValue json;
        
        try {
            json = JsonValue.parseJSON(param);
        } catch (JsonDecoderException ex) {
            ConverterException exception = new ConverterException("invalid JSON format: " + param, ex);
            logger.throwing(className, "parseMapping", exception);
            throw exception;
        }
        
        JsonObject object = json.asObject();
        
        if (object == null) {
            ConverterException exception = new ConverterException("invalid format: " + param);
            logger.throwing(className, "parseMapping", exception);
            throw exception;
        }
        
        for (String name : object.getMembers()) {
            JsonString value = object.get(name).asString();
            
            if (value == null) {
                ConverterException exception = new ConverterException("invalid value: " + object.get(name));
                logger.throwing(className, "parseMapping", exception);
                throw exception;
            }
            
            Data data = d(value.toString());
            
            if (caseInsensitive) {
                dataMap.put(name.toLowerCase(), data);
            } else {
                dataMap.put(name, data);
            }
            
            stringMap.put(data, name);
        }
        
        logger.exiting(className, "parseMapping");
    }
    
    public Map(HashMap<String, String> params) throws ConverterException {
        logger.entering(className, "Map", params);
        
        dataMap = new HashMap<String, Data>();
        stringMap = new HashMap<Data, String>();
        
        String mapping = null;
        
        for (String key : params.keySet()) {
            String value = params.get(key);
            
            switch (key) {
                case "mapping":
                    mapping = value;
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
                default:
                    ConverterException exception = new ConverterException("invalid parameter: " + key + " = " + value);
                    logger.throwing(className, "Map", exception);
                    throw exception;
            }
        }
        
        if (mapping == null) {
            ConverterException exception = new ConverterException("no mapping");
            logger.throwing(className, "Map", exception);
            throw exception;
        }
            
        parseMapping(mapping);
        
        logger.exiting(className, "Map");
    }

    @Override
    public String convertData(Data data) {
        logger.entering(className, "convertData", data);
        
        String result = stringMap.get(data);
        logger.exiting(className, "convertData", result);
        return result;
    }

    @Override
    public Data convertString(String str) {
        logger.entering(className, "convertString", str);
        
        Data result;
        
        if (caseInsensitive) {
            result = dataMap.get(str.toLowerCase());
        } else {
            result = dataMap.get(str);
        }
        
        logger.exiting(className, "convertString", result);
        return result;
    }
    
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        String sep = "";
        builder.append("Map[{");
        
        for(String s: dataMap.keySet()) {
            builder.append(sep).append(s).append(": ").append(dataMap.get(s));
            sep = ", ";
        }
        
        sep = "";
        builder.append("}, {");
        
        for(Data d: stringMap.keySet()) {
            builder.append(sep).append(d).append(": ").append(stringMap.get(d));
            sep = ", ";
        }
        
        builder.append("}]");
        
        return builder.toString();
    }
    
    public static void main(String[] args) throws ConverterException {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("mapping", "ON:30, OFF:31");
        params.put("case-insensitive", "true");
        Map map = new Map(params);
        
        System.out.println(map.convertData(new Data(new byte[]{0x30})));
        System.out.println(map.convertData(new Data(new byte[]{0x31})));
        System.out.println(map.convertString("ON"));
        System.out.println(map.convertString("Off"));
    }
}
