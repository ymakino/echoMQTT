package echomqtt.converter;

import echomqtt.json.JValue;
import echomqtt.json.JsonDecoderException;
import echowand.common.Data;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class Integer extends Converter {
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
    
    protected static BigInteger u(Data data) {
        return new BigInteger(data.toString(), 16);
    }
    
    protected static BigInteger s(Data data) {
        
        boolean isNegative = ((data.get(0) & 0x80) != 0);
        
        String str;
        
        if (!isNegative) {
            str = data.toString();
        } else {
            byte[] bytes = new byte[data.size()];
            for (int i=0; i<data.size(); i++) {
                bytes[i] = (byte)(0x00ff & ((data.get(i) ^ (byte)0xff)));
            }
            str = new Data(bytes).toString();
        }
        
        BigInteger num = new BigInteger(str, 16);
        
        if (isNegative) {
            num = num.add(BigInteger.ONE).negate();
        }

        return num;
    }

    @Override
    public JValue convert(Data data) {
        logger.entering(className, "convert", data);
        
        JValue result;
        
        if (isSigned) {
            result = JValue.newNumber(s(data));
        } else {
            result = JValue.newNumber(u(data));
        }
        
        logger.exiting(className, "convert", result);
        return result;
    }

    @Override
    public Data convert(JValue jvalue) throws ConverterException {
        logger.entering(className, "convert", jvalue);
        
        BigDecimal value;
        
        if (jvalue.isNumber()) {
            value = jvalue.asNumber().getValue();
        } else if (jvalue.isString()) {
            value = new BigDecimal(jvalue.asString().getValue());
        } else {
            ConverterException exception = new ConverterException("invalid value: " + jvalue);
            logger.throwing(className, "convert", exception);
            throw exception;
        }
        
        BigInteger num = value.toBigInteger();
        
        byte[] buf = new byte[size];
        
        for (int i=0; i<size; i++) {
            buf[size - i - 1] = (byte)(num.shiftRight(8 * i).intValue() & 0xff);
        }
        
        if (isSigned && value.signum() == 1) {
            buf [0] &= 0x7f;
        }
        
        Data result = new Data(buf);
        logger.exiting(className, "convertString", result);
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("Integer");
        builder.append("{");
        builder.append("size: ").append(size).append(", ");
        builder.append("isSigned: ").append(isSigned);
        builder.append("}");
        
        return builder.toString();
    }
    
    public static void main(String[] args) throws ConverterException, JsonDecoderException {
        HashMap<String, String> map1 = new HashMap<String, String>();
        map1.put("unsigned", "true");
        map1.put("size", "2");
        Integer c1 = new Integer(map1);
        /*
        Converter c2 = new Integer(Arrays.asList(new String[]{"signed", "2"}));
        Converter c3 = new Integer(Arrays.asList(new String[]{"signed", "4"}));
        Converter c4 = new Integer(Arrays.asList(new String[]{"signed", "8"}));
        Converter c5 = new Integer(Arrays.asList(new String[]{"signed", "16"}));
    */
        
        System.out.println(c1.convert(new Data((byte)0xff)));
        System.out.println(c1.convert(JValue.parseJSON("-1")));
        
        System.out.println(c1.convert(new Data((byte)0x12, (byte)0x34)));
        System.out.println(c1.convert(JValue.parseJSON("1")));
    }
}
