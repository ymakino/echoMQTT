package echomqtt.converter;

import echomqtt.json.JValue;
import echomqtt.json.JsonDecoderException;
import echowand.common.Data;
import echowand.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class Object extends Converter {
    private static final Logger logger = Logger.getLogger(Object.class.getName());
    private static final String className = Object.class.getName();
    
    private int size;
    private ArrayList<Pair<String, Converter>> converters;
    
    public Object(Pair<String, Converter>... converters) throws ConverterException {
        logger.entering(className, "Array", converters);
        
        this.size = 0;
        this.converters = new ArrayList<Pair<String, Converter>>(Arrays.asList(converters));
        
        for (Pair<String, Converter> converter: converters) {
            if (converter.second.getSize() < 0) {
                ConverterException exception = new ConverterException("invalid size: " + converter);
                logger.throwing(className, "convert", exception);
                throw exception;
            }
            
            size += converter.second.getSize();
        }
        
        logger.exiting(className, "Array");
    }
    
    public Object(List<Pair<String, Converter>> converters) throws ConverterException {
        logger.entering(className, "Array", converters);
        
        this.size = 0;
        this.converters = new ArrayList<Pair<String, Converter>>(converters);
        
        for (Pair<String, Converter> converter: converters) {
            if (converter.second.getSize() < 0) {
                ConverterException exception = new ConverterException("invalid size: " + converter);
                logger.throwing(className, "convert", exception);
                throw exception;
            }
            
            size += converter.second.getSize();
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
        
        if (data.size() != getSize()) {
            ConverterException exception = new ConverterException("invalid size: " + data);
            logger.throwing(className, "convert", exception);
            throw exception;
        }
        
        HashMap<String, JValue> values = new HashMap<String, JValue>();
        
        int index = 0;
        for (Pair<String, Converter> converter: converters) {
            byte[] bytes = data.toBytes(index, converter.second.getSize());
            Data data1 = new Data(bytes);
            values.put(converter.first, converter.second.convert(data1));
            index += converter.second.getSize();
        }
        
        JValue result = JValue.newObject(values);
        
        logger.exiting(className, "convert", result);
        return result;
    }
    
    private Data flatten(List<Data> dataList) {
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
        
        return new Data(bytes);
    }

    @Override
    public Data convert(JValue jvalue) throws ConverterException {
        logger.entering(className, "convert", jvalue);
        
        Data result;
        
        if (jvalue.isObject()) {
            if (converters.size() != jvalue.asObject().getMembers().size()) {
                ConverterException exception = new ConverterException("invalid size: " + jvalue);
                logger.throwing(className, "convert", exception);
                throw exception;
            }
            
            ArrayList<Data> dataList = new ArrayList<Data>(jvalue.asObject().getMembers().size());
            
            for (Pair<String, Converter> converter : converters) {
                dataList.add(converter.second.convert(jvalue.asObject().get(converter.first)));
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
        builder.append(converters.toString());
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
        
        LinkedList<Pair<String, Converter>> pairs = new LinkedList<>();
        pairs.add(new Pair<String, Converter>("item1", ic));
        pairs.add(new Pair<String, Converter>("item2", fc));
        
        Converter c1 = new Object(pairs);
        
        System.out.println(c1);
        
        Data d = c1.convert(JValue.parseJSON("{\"item1\":65536, \"item2\":1024.6}"));
        System.out.println(d);
        System.out.println(c1.convert(d));
        System.out.println(c1.convert(JValue.parseJSON("{\"item1\":65536, \"item2\":1024.6}")));
    }
}
