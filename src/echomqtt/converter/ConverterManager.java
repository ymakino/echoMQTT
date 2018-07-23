package echomqtt.converter;

import echomqtt.json.JValue;
import echomqtt.json.JsonDecoderException;
import echowand.common.Data;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class ConverterManager {
    private static final Logger logger = Logger.getLogger(ConverterManager.class.getName());
    private static final String className = ConverterManager.class.getName();
    
    private LinkedList<String> prefixes;
    private HashMap<String, Converter> converters;
    
    public ConverterManager() {
        logger.entering(className, "ConverterManager");
        
        prefixes = new LinkedList<String>(Arrays.asList(new String[]{ConverterManager.class.getPackage().getName(), ""}));
        converters = new HashMap<String, Converter>();
        
        logger.exiting(className, "ConverterManager");
    }
    
    public Converter instantiate(String name, HashMap<String, String> params) {
        logger.entering(className, "instantiate", new Object[]{name, params});
        
        Class cls = null;
        
        for (String prefix : prefixes) {
            try {
                if (prefix.length() != 0 && !prefix.endsWith(".")) {
                    prefix = prefix + ".";
                }
                
                cls = Class.forName(prefix + name);
                
                Constructor<Converter> constructor = cls.getConstructor(HashMap.class);
                
                Converter result = constructor.newInstance(params);
                logger.exiting(className, "instantiate", result);
                return result;
            } catch (ClassNotFoundException ex) {
                logger.logp(Level.WARNING, className, "instantiate", "catched exception", ex);
            } catch (NoSuchMethodException ex) {
                logger.logp(Level.WARNING, className, "instantiate", "catched exception", ex);
            } catch (SecurityException ex) {
                logger.logp(Level.WARNING, className, "instantiate", "catched exception", ex);
            } catch (InstantiationException ex) {
                logger.logp(Level.WARNING, className, "instantiate", "catched exception", ex);
            } catch (IllegalAccessException ex) {
                logger.logp(Level.WARNING, className, "instantiate", "catched exception", ex);
            } catch (IllegalArgumentException ex) {
                logger.logp(Level.WARNING, className, "instantiate", "catched exception", ex);
            } catch (InvocationTargetException ex) {
                logger.logp(Level.WARNING, className, "instantiate", "catched exception", ex);
            }
        }
        
        logger.exiting(className, "instantiate", null);
        return null;
    }
    
    private String serializeName(String str, HashMap<String, String> params) {
        logger.entering(className, "serializeName", new Object[]{str, params});
        
        String sep = "$";
        ArrayList<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        
        StringBuilder builder = new StringBuilder(str);
        for (String key : keys) {
            builder.append(sep).append(key).append(sep).append(params.get(key));
        }
        
        String result = builder.toString();
        logger.exiting(className, "serializeName", result);
        return builder.toString();
    }
    
    public Converter getConverter(String str, HashMap<String, String> params) {
        logger.entering(className, "getConverter", new Object[]{str, params});
        
        String serializedName = serializeName(str, params);
        
        Converter converter = converters.get(serializedName);
        
        if (converter != null) {
            logger.exiting(className, "getConverter", converter);
            return converter;
        }
        
        
        converter = instantiate(str, params);
        
        if (converter != null) {
            converters.put(serializedName, converter);
            logger.exiting(className, "getConverter", converter);
            return converter;
        }
        
        logger.exiting(className, "getConverter", null);
        return null;
    }
    
    public static void main(String[] args) throws ConverterException, JsonDecoderException {
        ConverterManager converterManager = new ConverterManager();
        
        HashMap<String, String> integerParam = new HashMap<String, String>();
        integerParam.put("unsigned", "false");
        integerParam.put("size", "2");
        
        HashMap<String, String> floatParam = new HashMap<String, String>();
        floatParam.put("divide", "10");
        floatParam.put("size", "2");
        
        HashMap<String, String> mapParam1 = new HashMap<String, String>();
        mapParam1.put("mapping", "{\"ON\":\"30\", \"OFF\":\"31\"}");
        
        HashMap<String, String> mapParam2 = new HashMap<String, String>();
        mapParam2.put("mapping", "{\"30\":\"ON\", \"31\":\"OFF\"}");
        
        
        Converter ci = converterManager.getConverter("Integer", integerParam);
        System.out.println(ci.convert(JValue.parseJSON("1234")));
        System.out.println(ci.convert(new Data(new byte[]{0x12})));
        
        Converter cf = converterManager.getConverter("Float", floatParam);
        System.out.println(cf.convert(JValue.parseJSON("1234")));
        System.out.println(cf.convert(new Data(new byte[]{0x12})));
        
        Converter cm1 = converterManager.getConverter("Map", mapParam1);
        System.out.println(cm1.convert(JValue.parseJSON("\"on\"")));
        
        Converter cm2 = converterManager.getConverter("Map", mapParam2);
        System.out.println(cm2.convert(new Data(new byte[]{0x31})));
    }
}
