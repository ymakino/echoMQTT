package echomqtt.converter;

import echowand.common.Data;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class Integer implements Converter {
    private static final Logger logger = Logger.getLogger(Integer.class.getName());
    private static final String className = Integer.class.getName();
    
    private boolean isSigned = true;
    private int size = 2;
    
    public Integer(HashMap<String, String> params) throws ConverterException {
        logger.entering(className, "Integer", params);
        
        for (String key : params.keySet()) {
            String value = params.get(key);
            
            switch (key) {
                case "signed":
                    if (value.toLowerCase().equals("true")) {
                        isSigned = true;
                    } else if (value.toLowerCase().equals("false")) {
                        isSigned = false;
                    } else {
                        ConverterException exception = new ConverterException("invalid parameter: " + key + " = " + value);
                        logger.throwing(className, "Integer", exception);
                        throw exception;
                    }
                    break;
                case "unsigned":
                    if (value.toLowerCase().equals("true")) {
                        isSigned = false;
                    } else if (value.toLowerCase().equals("false")) {
                        isSigned = true;
                    } else {
                        ConverterException exception = new ConverterException("invalid parameter: " + key + " = " + value);
                        logger.throwing(className, "Integer", exception);
                        throw exception;
                    }
                    break;
                case "size":
                    size = java.lang.Integer.parseInt(value);
                    break;
                default:
                    ConverterException exception = new ConverterException("invalid parameter: " + key + " = " + value);
                    logger.throwing(className, "Integer", exception);
                    throw exception;
            }
        }
        
        logger.exiting(className, "Integer");
    }
    
    public static long u(Data data) {
        return Long.parseLong(data.toString(), 16);
    }
    
    public static long s(Data data) {
        
        long num = Long.parseLong(data.toString(), 16);
        
        if ((data.get(0) & 0x80) != 0) {
           long pad = Long.MIN_VALUE;
           while ((pad & num) == 0) {
               pad >>= 1;
           }

           num |= pad;
        }

        return num;
    }

    @Override
    public String convertData(Data data) {
        logger.entering(className, "convertData", data);
        
        String result;
        
        if (isSigned) {
            result = String.format("%d", s(data));
        } else {
            result = String.format("%d", u(data));
        }
        
        logger.exiting(className, "convertData", result);
        return result;
    }

    @Override
    public Data convertString(String str) {
        logger.entering(className, "convertString", str);
        
        BigInteger num = new BigInteger(str);
        
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
        
        builder.append("Integer");
        builder.append("{");
        builder.append("size: ").append(size).append(", ");
        builder.append("isSigned: ").append(isSigned);
        builder.append("}");
        
        return builder.toString();
    }
    
    public static void main(String[] args) throws ConverterException {
        HashMap<String, String> map1 = new HashMap<String, String>();
        map1.put("unsigned", "true");
        map1.put("size", "2");
        Converter c1 = new Integer(map1);
        /*
        Converter c2 = new Integer(Arrays.asList(new String[]{"signed", "2"}));
        Converter c3 = new Integer(Arrays.asList(new String[]{"signed", "4"}));
        Converter c4 = new Integer(Arrays.asList(new String[]{"signed", "8"}));
        Converter c5 = new Integer(Arrays.asList(new String[]{"signed", "16"}));
    */
        
        System.out.println(c1.convertData(new Data((byte)0xff)));
        System.out.println(c1.convertString("-1"));
        
        System.out.println(c1.convertData(new Data((byte)0x12, (byte)0x34)));
        System.out.println(c1.convertString("1"));
    }
}
