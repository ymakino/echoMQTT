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
public class Segment extends Converter {
    private static final Logger logger = Logger.getLogger(Segment.class.getName());
    private static final String className = Segment.class.getName();
    
    private int offset;
    private int size  = -1;
    private Converter converter;
    
    public Segment(int offset, Converter converter) {
        logger.entering(className, "Segment", new java.lang.Object[]{offset, converter});
        
        this.offset = offset;
        this.converter = converter;
        
        logger.exiting(className, "Segment");
    }
    
    public Segment(int offset, int size, Converter converter) {
        logger.entering(className, "Segment", new java.lang.Object[]{offset, converter});
        
        this.offset = offset;
        this.size = size;
        this.converter = converter;
        
        logger.exiting(className, "Segment");
    }
    
    @Override
    public int getSize() {
        if (size < 0) {
            return size;
        }
        
        return offset + size;
    }
    
    @Override
    public JValue convert(Data data) throws ConverterException {
        logger.entering(className, "convert", data);
        
        if (data.size() <= offset) {
            ConverterException exception = new ConverterException("invalid size: " + data);
            logger.throwing(className, "convert", exception);
            throw exception;
        }
        
        byte[] bytes = new byte[data.size() - offset];
        for (int i=0; i<bytes.length; i++) {
            bytes[i] = data.get(i + offset);
        }
        
        JValue result = converter.convert(new Data(bytes));
        
        logger.exiting(className, "convert", result);
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("Segment[");
        builder.append(offset);
        builder.append("..");
        if (getSize() > 0) {
            builder.append(getSize());
        }
        builder.append("]");
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
        
        Converter c1 = new Segment(1, 4, ic);
        Converter c2 = new Segment(1, 2, fc);
        
        System.out.println(c1);
        System.out.println(c2);
        byte[] bytes = new byte[]{1,2,3,4,5,6,7,8};
        System.out.println(c1.convert(new Data(bytes)));
        System.out.println(c2.convert(new Data(bytes)));
    }
}
