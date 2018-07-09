package echomqtt.converter;

import echowand.common.Data;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class Float implements Converter {
    private static final Logger logger = Logger.getLogger(Float.class.getName());
    private static final String className = Float.class.getName();
    
    private int size = 2;
    private double divide = 10;
    
    public Float(HashMap<String, String> params) throws ConverterException {
        logger.entering(className, "Float", params);
        
        for (String key : params.keySet()) {
            String value = params.get(key);
            
            switch (key) {
                case "size":
                    size = java.lang.Integer.parseInt(value);
                    break;
                case "divide":
                    divide = java.lang.Double.parseDouble(value);
                    break;
                default:
                    ConverterException exception = new ConverterException("invalid parameter: " + key + " = " + value);
                    logger.throwing(className, "Float", exception);
                    throw exception;
            }
        }
        
        logger.exiting(className, "Float");
    }
    
    @Override
    public String convertData(Data data) {
        logger.entering(className, "convertData", data);
        
        String result = String.format("%.1f", Integer.s(data) / divide);
        logger.exiting(className, "convertData", result);
        return result;
    }

    @Override
    public Data convertString(String str) {
        logger.entering(className, "convertString", str);
        
        BigInteger num = BigInteger.valueOf((long)(Double.parseDouble(str) * divide));
        
        byte[] buf = new byte[size];
        
        for (int i=0; i<size; i++) {
            buf[size - i - 1] = (byte)(num.shiftRight(8 * i).intValue() & 0xff);
        }
        
        Data result = new Data(buf);
        logger.exiting(className, "convertString", result);
        return result;
    }
    
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("Float");
        builder.append("{");
        builder.append("size: ").append(size).append(", ");
        builder.append("divide: ").append(divide);
        builder.append("}");
        
        return builder.toString();
    }
    
    public static void main(String[] args) throws ConverterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("size", "2");
        map.put("divide", "10");
        Converter c1 = new Float(map);
        
        Data d = c1.convertString("1024.6");
        System.out.println(d);
        System.out.println(c1.convertData(d));
        System.out.println(c1.convertString("1024.6"));
    }
}
