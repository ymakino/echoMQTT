package echomqtt.converter;

import echomqtt.json.JValue;
import echomqtt.json.JsonDecoderException;
import echowand.common.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class Array extends Converter {
    private static final Logger logger = Logger.getLogger(Array.class.getName());
    private static final String className = Array.class.getName();
    
    private int size;
    private Converter converter;
    private int min;
    private int max;
    private boolean useObject;
    
    public Array(Converter converter, int min, int max, boolean useObject) throws ConverterException {
        logger.entering(className, "Array", new java.lang.Object[]{converter, min, max, useObject});
        
        this.size = 0;
        this.converter = converter;
        this.min = min;
        this.max = max;
        this.useObject = useObject;
        
        if (min < 0) {
            ConverterException exception = new ConverterException("invalid size: " + min);
            logger.throwing(className, "convert", exception);
            throw exception;
        }
        
        if (min > max) {
            ConverterException exception = new ConverterException("invalid size: " + min + " > " + max);
            logger.throwing(className, "convert", exception);
            throw exception;
        }
        
        if (converter.getSize() < 0) {
            ConverterException exception = new ConverterException("invalid size: " + converter);
            logger.throwing(className, "convert", exception);
            throw exception;
        }
        
        if (min != max) {
            this.size = -1;
        } else {
            this.size = converter.getSize() * max;
        }
        
        logger.exiting(className, "Array");
    }
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public JValue convert(Data data) throws ConverterException {
        logger.entering(className, "convert", data);
        
        int count = max;
        
        if (converter.getSize() > 0) {
            count = data.size() / converter.getSize();
            
            if (count < min) {
                ConverterException exception = new ConverterException("invalid size: " + data);
                logger.throwing(className, "convert", exception);
                throw exception;
            }
            
            if (count > max) {
                count = max;
            }
        }
        
        ArrayList<JValue> values = new ArrayList<JValue>(count);
        
        for (int i=0, offset=0; i<count; i++, offset += converter.getSize()) {
            byte[] bytes = data.toBytes(offset, converter.getSize());
            Data data1 = new Data(bytes);
            values.add(converter.convert(data1));
        }
        
        JValue result = convertArray(values);
        
        logger.exiting(className, "convert", result);
        return result;
    }
    
    private JValue convertArray(List<JValue> values) {
        logger.entering(className, "convertArray", values);
        
        if (!useObject) {
            JValue result = JValue.newArray(values);
            logger.exiting(className, "convertArray", result);
            return result;
        }
        
        HashMap<String, JValue> map = new HashMap<String, JValue>();
        
        for (int i=0; i<values.size(); i++) {
            map.put(String.valueOf(i), values.get(i));
        }
        
        JValue result = JValue.newObject(map);
        logger.exiting(className, "convertArray", result);
        return result;
    }
    
    private Data flatten(List<Data> dataList) {
        logger.entering(className, "flatten", dataList);
        
        int total = 0;
        
        for (Data data : dataList) {
            total += data.size();
        }
        
        byte[] bytes = new byte[total];
        int index = 0;
        
        for (Data data : dataList) {
            data.copyBytes(0, bytes, index, data.size());
            index += data.size();
        }
        
        Data result = new Data(bytes);
        logger.exiting(className, "v", result);
        return result;
    }

    @Override
    public Data convert(JValue jvalue) throws ConverterException {
        logger.entering(className, "convert", jvalue);
        
        Data result;
        
        if (jvalue.isArray()) {
            if (jvalue.asArray().size() < min || max < jvalue.asArray().size()) {
                ConverterException exception = new ConverterException("invalid size: " + jvalue);
                logger.throwing(className, "convert", exception);
                throw exception;
            }
            
            ArrayList<Data> dataList = new ArrayList<Data>(jvalue.asArray().size());
            
            for (int i=0; i<jvalue.asArray().size(); i++) {
                JValue v = jvalue.asArray().get(i);
                dataList.add(converter.convert(v));
            }
            
            result = flatten(dataList);
        } else {
            ConverterException exception = new ConverterException("invalid value: " + jvalue);
            logger.throwing(className, "convert", exception);
            throw exception;
        }
        
        logger.exiting(className, "convert", result);
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("Array");
        builder.append("(");
        builder.append(converter.toString());
        builder.append(")");
        
        return builder.toString();
    }
    
    public static void main(String[] args) throws ConverterException, JsonDecoderException {
        HashMap<String, String> m1 = new HashMap<String, String>();
        m1.put("size", "4");
        m1.put("signed", "false");
        Integer ic = new Integer(m1);
        
        HashMap<String, String> m2 = new HashMap<String, String>();
        m2.put("size", "2");
        m2.put("divide", "10");
        m2.put("signed", "true");
        Float fc = new Float(m2);
        
        Converter c1 = new Array(ic, 1, 4, false);
        
        System.out.println(c1);
        
        Data d = c1.convert(JValue.parseJSON("[65536, 1024.6, 0.1]"));
        System.out.println(d);
        System.out.println(c1.convert(d));
        System.out.println(c1.convert(JValue.parseJSON("[0, 1024.6, 0.1]")));
        
        Data d1 = c1.convert(JValue.parseJSON("[0, 6553.5, 0.1]"));
        System.out.println(d1);
        System.out.println(c1.convert(d1));
        System.out.println(c1.convert(JValue.parseJSON("[0, 6553.5, 0.1]")));
    }
}
